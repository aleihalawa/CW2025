# COMP2024 - Artificial Intelligence Methods
import numpy as np
import pandas as pd
import json
import time
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (f1_score, confusion_matrix, accuracy_score,
                             precision_score, recall_score)
from sklearn.model_selection import train_test_split


# PSO settings
NUM_PARTICLES     = 20
NUM_ITERATIONS    = 30
W_MAX             = 0.9      # inertia starts high (more exploration)
W_MIN             = 0.4      # inertia ends low (more exploitation)
C1                = 1.5      # how much each particle trusts its own best
C2                = 1.5      # how much each particle trusts the swarm best
EARLY_STOP_ROUNDS = 5
SAMPLE_FRACTION   = 0.20
ALPHA             = 0.9      # fitness weight for F1-score
BETA              = 0.1      # fitness penalty for using too many features
MIN_FEATURES      = 5
FEATURE_THRESHOLD = 0.5      # position values above this = feature selected

# Random Forest hyperparameter bounds
HP_BOUNDS = {
    "n_estimators"     : (50, 300),
    "max_depth"        : (3, 30),
    "min_samples_split": (2, 20),
    "min_samples_leaf" : (1, 10),
    "max_features_idx" : (0, 2),
}
MAX_FEATURES_MAP = {0: "sqrt", 1: "log2", 2: None}


def load_data():
    print("=" * 60)
    print("  COMP2024 - Particle Swarm Optimization")
    print("=" * 60)
    print("\n[1/6] Loading data...")

    X_train = pd.read_csv("X_train_final.csv")
    y_train = pd.read_csv("y_train_final.csv").values.ravel()
    X_test  = pd.read_csv("X_test_final.csv")
    y_test  = pd.read_csv("y_test_final.csv").values.ravel()

    X_sample, _, y_sample, _ = train_test_split(
        X_train, y_train,
        train_size=SAMPLE_FRACTION,
        random_state=42,
        stratify=y_train
    )

    feature_names = X_train.columns.tolist()
    print(f"   Training set : {len(X_train):,} samples")
    print(f"   Sample used  : {len(X_sample):,} samples ({int(SAMPLE_FRACTION*100)}%)")
    print(f"   Test set     : {len(X_test):,} samples")
    print(f"   Features     : {len(feature_names)}")

    return X_train, y_train, X_test, y_test, X_sample, y_sample, feature_names


def decode_particle(position, feature_names):
    # convert continuous position vector into features + hyperparams
    num_features = len(feature_names)
    feature_vals = position[:num_features]
    feature_mask = (feature_vals > FEATURE_THRESHOLD).astype(int)

    # make sure we always have at least MIN_FEATURES selected
    if sum(feature_mask) < MIN_FEATURES:
        top_idx = np.argsort(feature_vals)[-MIN_FEATURES:]
        feature_mask[top_idx] = 1

    selected = [feature_names[i] for i, bit in enumerate(feature_mask) if bit == 1]

    hp_vec = position[num_features:]
    hyperparams = {
        "n_estimators"     : int(np.clip(round(hp_vec[0]), *HP_BOUNDS["n_estimators"])),
        "max_depth"        : int(np.clip(round(hp_vec[1]), *HP_BOUNDS["max_depth"])),
        "min_samples_split": int(np.clip(round(hp_vec[2]), *HP_BOUNDS["min_samples_split"])),
        "min_samples_leaf" : int(np.clip(round(hp_vec[3]), *HP_BOUNDS["min_samples_leaf"])),
        "max_features"     : MAX_FEATURES_MAP[int(np.clip(round(hp_vec[4]), *HP_BOUNDS["max_features_idx"]))],
    }

    return selected, hyperparams


def initialize_particles(num_particles, num_features):
    dim = num_features + 5

    pos_min = np.array(
        [0.0] * num_features +
        [HP_BOUNDS["n_estimators"][0], HP_BOUNDS["max_depth"][0],
         HP_BOUNDS["min_samples_split"][0], HP_BOUNDS["min_samples_leaf"][0],
         HP_BOUNDS["max_features_idx"][0]]
    )
    pos_max = np.array(
        [1.0] * num_features +
        [HP_BOUNDS["n_estimators"][1], HP_BOUNDS["max_depth"][1],
         HP_BOUNDS["min_samples_split"][1], HP_BOUNDS["min_samples_leaf"][1],
         HP_BOUNDS["max_features_idx"][1]]
    )

    positions  = pos_min + np.random.uniform(0, 1, (num_particles, dim)) * (pos_max - pos_min)
    velocities = np.random.uniform(-1, 1, (num_particles, dim)) * (pos_max - pos_min) * 0.1

    return positions, velocities, pos_min, pos_max


def evaluate_fitness(position, X_train, y_train, X_test, y_test, feature_names):
    selected, hyperparams = decode_particle(position, feature_names)

    if len(selected) < MIN_FEATURES:
        return 0.0, selected, hyperparams

    try:
        clf = RandomForestClassifier(
            n_estimators      = hyperparams["n_estimators"],
            max_depth         = hyperparams["max_depth"],
            min_samples_split = hyperparams["min_samples_split"],
            min_samples_leaf  = hyperparams["min_samples_leaf"],
            max_features      = hyperparams["max_features"],
            random_state      = 42,
            n_jobs            = -1,
        )
        clf.fit(X_train[selected], y_train)
        y_pred = clf.predict(X_test[selected])
        f1     = f1_score(y_test, y_pred, zero_division=0)
    except Exception:
        return 0.0, selected, hyperparams

    fitness = ALPHA * f1 - BETA * (len(selected) / len(feature_names))
    return fitness, selected, hyperparams


def full_evaluation(best_position, X_train, y_train, X_test, y_test, feature_names):
    selected, hyperparams = decode_particle(best_position, feature_names)

    clf = RandomForestClassifier(
        n_estimators      = hyperparams["n_estimators"],
        max_depth         = hyperparams["max_depth"],
        min_samples_split = hyperparams["min_samples_split"],
        min_samples_leaf  = hyperparams["min_samples_leaf"],
        max_features      = hyperparams["max_features"],
        random_state      = 42,
        n_jobs            = -1,
    )
    clf.fit(X_train[selected], y_train)
    y_pred = clf.predict(X_test[selected])

    tn, fp, fn, tp = confusion_matrix(y_test, y_pred).ravel()
    fpr = fp / (fp + tn) if (fp + tn) > 0 else 0.0

    results = {
        "features_used": len(selected),
        "feature_names": selected,
        "hyperparams"  : hyperparams,
        "accuracy"     : accuracy_score(y_test, y_pred),
        "precision"    : precision_score(y_test, y_pred, zero_division=0),
        "recall"       : recall_score(y_test, y_pred, zero_division=0),
        "f1_score"     : f1_score(y_test, y_pred, zero_division=0),
        "fpr"          : fpr,
        "TP"           : int(tp),
        "TN"           : int(tn),
        "FP"           : int(fp),
        "FN"           : int(fn),
    }

    print("\n" + "=" * 60)
    print("   PSO FINAL RESULTS (Full Dataset)")
    print("=" * 60)
    print(f"  Features Selected  : {len(selected)} / {len(feature_names)}")
    print(f"  Hyperparameters    : {hyperparams}")
    print(f"  Accuracy           : {results['accuracy']  * 100:.4f}%")
    print(f"  Precision          : {results['precision'] * 100:.4f}%")
    print(f"  Recall (TPR)       : {results['recall']    * 100:.4f}%")
    print(f"  F1-Score           : {results['f1_score']  * 100:.4f}%")
    print(f"  False Positive Rate: {results['fpr']       * 100:.4f}%")
    print("-" * 60)
    print(f"  TP: {tp}  |  TN: {tn}  |  FP: {fp}  |  FN: {fn}")
    print("=" * 60)
    print("\n  Selected Features:")
    for i, feat in enumerate(selected, 1):
        print(f"    {i:>2}. {feat}")
    print("=" * 60)

    return results


def run_pso():
    X_train, y_train, X_test, y_test, X_sample, y_sample, feature_names = load_data()
    num_features = len(feature_names)

    print(f"\n[2/6] Initializing {NUM_PARTICLES} particles...")
    positions, velocities, pos_min, pos_max = initialize_particles(NUM_PARTICLES, num_features)

    personal_best_pos     = positions.copy()
    personal_best_fitness = np.full(NUM_PARTICLES, -np.inf)
    global_best_pos       = None
    global_best_fitness   = -np.inf

    fitness_log      = []
    no_improve_count = 0

    start = time.time()

    print(f"\n[3/6] Running PSO for up to {NUM_ITERATIONS} iterations...")
    print(f"      Particles: {NUM_PARTICLES}  |  C1: {C1}  |  C2: {C2}")
    print(f"      Inertia: {W_MAX} to {W_MIN} (adaptive)\n")

    for iteration in range(1, NUM_ITERATIONS + 1):

        # inertia decreases over time so particles explore early and exploit later
        w = W_MAX - (W_MAX - W_MIN) * (iteration / NUM_ITERATIONS)

        iter_fitnesses = []

        for i in range(NUM_PARTICLES):
            fitness, _, _ = evaluate_fitness(
                positions[i], X_sample, y_sample, X_test, y_test, feature_names
            )
            iter_fitnesses.append(fitness)

            if fitness > personal_best_fitness[i]:
                personal_best_fitness[i] = fitness
                personal_best_pos[i]     = positions[i].copy()

            if fitness > global_best_fitness:
                global_best_fitness  = fitness
                global_best_pos      = positions[i].copy()
                no_improve_count     = 0

        no_improve_count += 1

        for i in range(NUM_PARTICLES):
            r1 = np.random.uniform(0, 1, positions.shape[1])
            r2 = np.random.uniform(0, 1, positions.shape[1])

            cognitive     = C1 * r1 * (personal_best_pos[i] - positions[i])
            social        = C2 * r2 * (global_best_pos - positions[i])
            velocities[i] = w * velocities[i] + cognitive + social
            positions[i]  = np.clip(positions[i] + velocities[i], pos_min, pos_max)

        best_iter_idx = np.argmax(iter_fitnesses)
        sel_feats, _  = decode_particle(positions[best_iter_idx], feature_names)

        fitness_log.append({
            "iteration"    : iteration,
            "best_fitness" : round(global_best_fitness, 6),
            "avg_fitness"  : round(float(np.mean(iter_fitnesses)), 6),
            "features_used": len(sel_feats),
        })

        elapsed = time.time() - start
        print(f"  Iter {iteration:>3} | Best: {global_best_fitness:.5f} | "
              f"Avg: {np.mean(iter_fitnesses):.5f} | "
              f"Features: {len(sel_feats)} | Inertia: {w:.3f} | Time: {elapsed:.0f}s")

        if no_improve_count >= EARLY_STOP_ROUNDS:
            print(f"\n  Early stop: no improvement for {EARLY_STOP_ROUNDS} iterations.")
            break

    total_time = time.time() - start
    print(f"\n  Runtime: {total_time:.2f} seconds ({total_time/60:.1f} minutes)")

    print("\n[4/6] Running final evaluation on full dataset...")
    final_results = full_evaluation(
        global_best_pos, X_train, y_train, X_test, y_test, feature_names
    )
    final_results["runtime_seconds"] = round(total_time, 2)

    print("\n[5/6] Saving results...")
    with open("pso_best_solution.json", "w") as f:
        json.dump(final_results, f, indent=2)
    print("   Saved: pso_best_solution.json")

    pd.DataFrame(fitness_log).to_csv("pso_fitness_log.csv", index=False)
    print("   Saved: pso_fitness_log.csv")

    print("\n[6/6] Done!")
    print(f"   Features : {len(feature_names)} -> {final_results['features_used']}")
    print(f"   F1-Score : {final_results['f1_score'] * 100:.4f}%")
    print(f"   FPR      : {final_results['fpr'] * 100:.4f}%")
    print(f"   Runtime  : {total_time/60:.1f} minutes")

    return final_results


if __name__ == "__main__":
    run_pso()
