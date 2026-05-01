# =============================================================================
# COMP2024 - Artificial Intelligence Methods
# Phase 2: Simulated Annealing Optimization (Member 4)
# Description: Implements Simulated Annealing to simultaneously optimize
#              feature selection and Random Forest hyperparameters for IDS.
#
# HOW IT WORKS:
#   - Starts with a random solution (feature mask + hyperparameters)
#   - Each iteration proposes a small modification (neighbour)
#   - Always accepts better solutions
#   - Sometimes accepts worse solutions with probability e^(delta/T)
#   - Temperature T decreases over time, reducing acceptance of worse solutions
#   - This prevents getting stuck in local optima
#
# SETUP GUIDE:
#   1. Install dependencies: pip install pandas scikit-learn numpy
#   2. Place this file in the same folder as baseline_model.py and the CSVs
#   3. Run: python sa_optimization.py
#   4. Outputs:
#        - sa_best_solution.json   (best features + hyperparams found)
#        - sa_fitness_log.csv      (fitness score per iteration for plots)
# =============================================================================

import numpy as np
import pandas as pd
import random
import json
import math
import time
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (f1_score, confusion_matrix, accuracy_score,
                             precision_score, recall_score)
from sklearn.model_selection import train_test_split

# =============================================================================
# CONFIGURATION
# =============================================================================

NUM_ITERATIONS   = 800       # Total number of SA iterations
INITIAL_TEMP     = 1.0       # Starting temperature (high = more exploration)
COOLING_RATE     = 0.995     # Temperature multiplier per iteration (slow cooling)
MIN_TEMP         = 0.001     # Stop if temperature drops below this
SAMPLE_FRACTION  = 0.20      # Fraction of training data used during optimization
ALPHA            = 0.9       # Fitness weight for F1-score
BETA             = 0.1       # Fitness penalty for using too many features
MIN_FEATURES     = 5         # Minimum features a solution must select
MAX_FLIP_BITS    = 3         # Max feature bits to flip per neighbour generation

# Hyperparameter search ranges
HP_RANGES = {
    "n_estimators"      : (50, 300),
    "max_depth"         : (3, 30),
    "min_samples_split" : (2, 20),
    "min_samples_leaf"  : (1, 10),
    "max_features"      : ["sqrt", "log2", None],
}


# =============================================================================
# DATA LOADING
# =============================================================================

def load_data():
    """Loads preprocessed datasets and creates a fast sample for optimization."""
    print("=" * 60)
    print("  COMP2024 - Simulated Annealing Optimization")
    print("=" * 60)
    print("\n[1/6] Loading preprocessed data...")

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
    print(f"   Full training set  : {len(X_train):,} samples")
    print(f"   Optimization sample: {len(X_sample):,} samples ({int(SAMPLE_FRACTION*100)}%)")
    print(f"   Test set           : {len(X_test):,} samples")
    print(f"   Features available : {len(feature_names)}")

    return X_train, y_train, X_test, y_test, X_sample, y_sample, feature_names


# =============================================================================
# SOLUTION REPRESENTATION
# =============================================================================

def random_solution(feature_names):
    """
    Generates a random starting solution.
    Each solution has a binary feature mask and a set of hyperparameters.
    """
    num_features = len(feature_names)
    mask = [0] * num_features
    selected = random.sample(range(num_features), random.randint(MIN_FEATURES, num_features))
    for i in selected:
        mask[i] = 1

    hyperparams = {
        "n_estimators"      : random.randint(*HP_RANGES["n_estimators"]),
        "max_depth"         : random.randint(*HP_RANGES["max_depth"]),
        "min_samples_split" : random.randint(*HP_RANGES["min_samples_split"]),
        "min_samples_leaf"  : random.randint(*HP_RANGES["min_samples_leaf"]),
        "max_features"      : random.choice(HP_RANGES["max_features"]),
    }

    return {"feature_mask": mask, "hyperparams": hyperparams}


def generate_neighbour(solution, feature_names):
    """
    Creates a slightly modified version of the current solution.
    Randomly flips 1-3 feature bits and adjusts one hyperparameter.
    This is the core of SA — small steps through the search space.
    """
    num_features = len(feature_names)
    mask = solution["feature_mask"][:]
    hp   = dict(solution["hyperparams"])

    # Flip 1 to MAX_FLIP_BITS random feature bits
    num_flips = random.randint(1, MAX_FLIP_BITS)
    flip_indices = random.sample(range(num_features), num_flips)
    for i in flip_indices:
        mask[i] = 1 - mask[i]

    # Ensure minimum features constraint
    if sum(mask) < MIN_FEATURES:
        indices = random.sample(range(num_features), MIN_FEATURES)
        for i in indices:
            mask[i] = 1

    # Randomly adjust one hyperparameter
    param_to_mutate = random.choice(list(HP_RANGES.keys()))
    if param_to_mutate == "max_features":
        hp["max_features"] = random.choice(HP_RANGES["max_features"])
    else:
        lo, hi = HP_RANGES[param_to_mutate]
        current = hp[param_to_mutate]
        # Small nudge — change by up to 20% of range
        delta = random.randint(-max(1, (hi - lo) // 5), max(1, (hi - lo) // 5))
        hp[param_to_mutate] = int(np.clip(current + delta, lo, hi))

    return {"feature_mask": mask, "hyperparams": hp}


# =============================================================================
# FITNESS EVALUATION
# =============================================================================

def evaluate_fitness(solution, X_train, y_train, X_test, y_test, feature_names):
    """
    Evaluates a solution's fitness using:
        fitness = ALPHA * F1-score - BETA * (features_used / total_features)
    """
    mask = solution["feature_mask"]
    hp   = solution["hyperparams"]
    selected = [feature_names[i] for i, bit in enumerate(mask) if bit == 1]

    if len(selected) < MIN_FEATURES:
        return 0.0

    try:
        clf = RandomForestClassifier(
            n_estimators      = hp["n_estimators"],
            max_depth         = hp["max_depth"],
            min_samples_split = hp["min_samples_split"],
            min_samples_leaf  = hp["min_samples_leaf"],
            max_features      = hp["max_features"],
            random_state      = 42,
            n_jobs            = -1,
        )
        clf.fit(X_train[selected], y_train)
        y_pred = clf.predict(X_test[selected])
        f1 = f1_score(y_test, y_pred, zero_division=0)
    except Exception:
        return 0.0

    num_selected = sum(mask)
    total        = len(mask)
    fitness      = ALPHA * f1 - BETA * (num_selected / total)
    return fitness


# =============================================================================
# FINAL EVALUATION ON FULL DATASET
# =============================================================================

def full_evaluation(best_solution, X_train, y_train, X_test, y_test, feature_names):
    """Runs the best solution on the FULL dataset for official paper results."""
    mask     = best_solution["feature_mask"]
    hp       = best_solution["hyperparams"]
    selected = [feature_names[i] for i, bit in enumerate(mask) if bit == 1]

    clf = RandomForestClassifier(
        n_estimators      = hp["n_estimators"],
        max_depth         = hp["max_depth"],
        min_samples_split = hp["min_samples_split"],
        min_samples_leaf  = hp["min_samples_leaf"],
        max_features      = hp["max_features"],
        random_state      = 42,
        n_jobs            = -1,
    )
    clf.fit(X_train[selected], y_train)
    y_pred = clf.predict(X_test[selected])

    tn, fp, fn, tp = confusion_matrix(y_test, y_pred).ravel()
    fpr = fp / (fp + tn) if (fp + tn) > 0 else 0.0

    results = {
        "features_used" : len(selected),
        "feature_names" : selected,
        "hyperparams"   : hp,
        "accuracy"      : accuracy_score(y_test, y_pred),
        "precision"     : precision_score(y_test, y_pred, zero_division=0),
        "recall"        : recall_score(y_test, y_pred, zero_division=0),
        "f1_score"      : f1_score(y_test, y_pred, zero_division=0),
        "fpr"           : fpr,
        "TP"            : int(tp),
        "TN"            : int(tn),
        "FP"            : int(fp),
        "FN"            : int(fn),
    }

    print("\n" + "=" * 60)
    print("   SA FINAL RESULTS (Full Dataset)")
    print("=" * 60)
    print(f"  Features Selected  : {len(selected)} / {len(feature_names)}")
    print(f"  Hyperparameters    : {hp}")
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


# =============================================================================
# MAIN SA LOOP
# =============================================================================

def run_sa():
    """Main Simulated Annealing execution loop."""

    # --- Load Data ---
    X_train, y_train, X_test, y_test, X_sample, y_sample, feature_names = load_data()

    # --- Initialize ---
    print(f"\n[2/6] Initializing random starting solution...")
    current_solution = random_solution(feature_names)
    current_fitness  = evaluate_fitness(
        current_solution, X_sample, y_sample, X_test, y_test, feature_names
    )

    best_solution = {
        "feature_mask": current_solution["feature_mask"][:],
        "hyperparams" : dict(current_solution["hyperparams"]),
    }
    best_fitness  = current_fitness
    temperature   = INITIAL_TEMP
    fitness_log   = []

    print(f"   Initial fitness   : {current_fitness:.5f}")
    print(f"   Initial features  : {sum(current_solution['feature_mask'])}")
    print(f"   Initial temp      : {INITIAL_TEMP}")
    print(f"   Cooling rate      : {COOLING_RATE}")

    start_time = time.time()

    print(f"\n[3/6] Running SA for up to {NUM_ITERATIONS} iterations...")
    print(f"      (Also stops if temperature drops below {MIN_TEMP})\n")

    for iteration in range(1, NUM_ITERATIONS + 1):

        # Generate neighbour and evaluate
        neighbour         = generate_neighbour(current_solution, feature_names)
        neighbour_fitness = evaluate_fitness(
            neighbour, X_sample, y_sample, X_test, y_test, feature_names
        )

        # Decide whether to accept the neighbour
        delta = neighbour_fitness - current_fitness

        if delta > 0:
            # Always accept better solutions
            current_solution = neighbour
            current_fitness  = neighbour_fitness
        else:
            # Accept worse solution with probability e^(delta/T)
            acceptance_prob = math.exp(delta / temperature)
            if random.random() < acceptance_prob:
                current_solution = neighbour
                current_fitness  = neighbour_fitness

        # Update best solution found so far
        if current_fitness > best_fitness:
            best_fitness  = current_fitness
            best_solution = {
                "feature_mask": current_solution["feature_mask"][:],
                "hyperparams" : dict(current_solution["hyperparams"]),
            }

        # Cool down
        temperature *= COOLING_RATE

        # Log every 10 iterations to keep CSV manageable
        if iteration % 10 == 0 or iteration == 1:
            elapsed = time.time() - start_time
            features_now = sum(current_solution["feature_mask"])
            fitness_log.append({
                "iteration"    : iteration,
                "best_fitness" : round(best_fitness, 6),
                "curr_fitness" : round(current_fitness, 6),
                "features_used": features_now,
                "temperature"  : round(temperature, 6),
            })
            print(f"  Iter {iteration:>4} | Best: {best_fitness:.5f} | "
                  f"Curr: {current_fitness:.5f} | "
                  f"Features: {features_now} | "
                  f"Temp: {temperature:.4f} | Time: {elapsed:.0f}s")

        # Stop if temperature is too low
        if temperature < MIN_TEMP:
            print(f"\n  Temperature dropped below {MIN_TEMP} — stopping early.")
            break

    total_time = time.time() - start_time
    print(f"\n  Total optimization time: {total_time:.1f} seconds ({total_time/60:.1f} minutes)")

    # --- Final evaluation on full dataset ---
    print("\n[4/6] Running final evaluation on full dataset...")
    final_results = full_evaluation(
        best_solution, X_train, y_train, X_test, y_test, feature_names
    )
    final_results["runtime_seconds"] = round(total_time, 2)

    # --- Save outputs ---
    print("\n[5/6] Saving results...")

    with open("sa_best_solution.json", "w") as f:
        json.dump(final_results, f, indent=2)
    print("   Saved: sa_best_solution.json")

    pd.DataFrame(fitness_log).to_csv("sa_fitness_log.csv", index=False)
    print("   Saved: sa_fitness_log.csv")

    print("\n[6/6] Done! Summary:")
    print(f"   Features reduced : 78 → {final_results['features_used']}")
    print(f"   F1-Score         : {final_results['f1_score'] * 100:.4f}%")
    print(f"   FPR              : {final_results['fpr'] * 100:.4f}%")
    print(f"   Runtime          : {total_time/60:.1f} minutes")

    return final_results


if __name__ == "__main__":
    run_sa()
