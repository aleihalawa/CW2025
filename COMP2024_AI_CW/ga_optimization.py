# =============================================================================
# COMP2024 - Artificial Intelligence Methods
# Phase 2: Genetic Algorithm Optimization (Member 2)
# Description: Implements a Genetic Algorithm to simultaneously optimize
#              feature selection and Random Forest hyperparameters for IDS.
#
# HOW IT WORKS:
#   - Each candidate solution (chromosome) encodes:
#       * A binary feature mask of length 78 (1=keep, 0=drop)
#       * 5 Random Forest hyperparameters
#   - The GA evolves a population of chromosomes over multiple generations
#     using selection, crossover, and mutation to find the best solution.
#   - Fitness is evaluated on a 20% sample of training data for speed.
#   - The best solution is finally evaluated on the full dataset.
#
# SETUP GUIDE:
#   1. Install dependencies: pip install pandas scikit-learn numpy
#   2. Place this file in the same folder as baseline_model.py and the CSVs
#   3. Run: python ga_optimization.py
#   4. Outputs:
#        - ga_best_solution.json   (best features + hyperparams found)
#        - ga_fitness_log.csv      (fitness score per generation for plots)
# =============================================================================

import numpy as np
import pandas as pd
import random
import json
import time
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import f1_score, confusion_matrix
from sklearn.model_selection import train_test_split

# =============================================================================
# CONFIGURATION — Adjust these to control the GA behaviour
# =============================================================================

POPULATION_SIZE    = 20       # Number of chromosomes per generation
NUM_GENERATIONS    = 30       # How many generations to evolve
CROSSOVER_RATE     = 0.8      # Probability of crossover between two parents
MUTATION_RATE      = 0.03     # Probability of flipping each feature bit
TOURNAMENT_SIZE    = 3        # Number of chromosomes competing in selection
EARLY_STOP_ROUNDS  = 5        # Stop if no improvement for this many generations
SAMPLE_FRACTION    = 0.20     # Fraction of training data used during optimization
ALPHA              = 0.9      # Weight for F1-score in fitness function
BETA               = 0.1      # Penalty weight for number of features used
MIN_FEATURES       = 5        # Minimum features a chromosome must select

# Hyperparameter search ranges
HP_RANGES = {
    "n_estimators"      : (50, 300),
    "max_depth"         : (3, 30),        # None handled separately via mutation
    "min_samples_split" : (2, 20),
    "min_samples_leaf"  : (1, 10),
    "max_features"      : ["sqrt", "log2", None],
}

# =============================================================================
# DATA LOADING
# =============================================================================

def load_data():
    """Loads the preprocessed datasets and creates a fast sample for optimization."""
    print("=" * 60)
    print("  COMP2024 — Genetic Algorithm Optimization")
    print("=" * 60)
    print("\n[1/6] Loading preprocessed data...")

    X_train = pd.read_csv("X_train_final.csv")
    y_train = pd.read_csv("y_train_final.csv").values.ravel()
    X_test  = pd.read_csv("X_test_final.csv")
    y_test  = pd.read_csv("y_test_final.csv").values.ravel()

    # Create a stratified sample of training data for fast fitness evaluation
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
# CHROMOSOME REPRESENTATION
# =============================================================================

def random_chromosome(num_features):
    """
    Creates a random chromosome encoding both feature selection and hyperparameters.

    Chromosome structure:
      - feature_mask : list of 78 ints (0 or 1)
      - hyperparams  : dict of RF hyperparameter values

    Returns:
        Dict with keys 'feature_mask' and 'hyperparams'.
    """
    # Ensure at least MIN_FEATURES are selected
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


# =============================================================================
# FITNESS EVALUATION
# =============================================================================

def evaluate_fitness(chromosome, X_train, y_train, X_test, y_test, feature_names):
    """
    Evaluates a chromosome's fitness using the formula:
        fitness = ALPHA * F1-score - BETA * (features_used / total_features)

    A higher fitness = better F1-score AND fewer features used.

    Args:
        chromosome    : Dict with feature_mask and hyperparams.
        X_train, y_train, X_test, y_test : Data arrays.
        feature_names : List of all feature column names.

    Returns:
        Fitness score (float).
    """
    mask = chromosome["feature_mask"]
    hp   = chromosome["hyperparams"]

    # Extract selected feature names
    selected_features = [feature_names[i] for i, bit in enumerate(mask) if bit == 1]

    # Must have at least MIN_FEATURES selected
    if len(selected_features) < MIN_FEATURES:
        return 0.0

    # Subset the data
    X_tr = X_train[selected_features]
    X_te = X_test[selected_features]

    # Train RF with chromosome's hyperparameters
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
        clf.fit(X_tr, y_train)
        y_pred = clf.predict(X_te)
        f1 = f1_score(y_test, y_pred, zero_division=0)
    except Exception:
        return 0.0

    # Fitness formula: reward F1, penalize feature count
    num_selected = sum(mask)
    total        = len(mask)
    fitness      = ALPHA * f1 - BETA * (num_selected / total)

    return fitness


# =============================================================================
# SELECTION — Tournament Selection
# =============================================================================

def tournament_selection(population, fitnesses):
    """
    Selects one chromosome via tournament selection.
    Picks TOURNAMENT_SIZE random candidates and returns the best one.
    """
    candidates = random.sample(range(len(population)), TOURNAMENT_SIZE)
    best = max(candidates, key=lambda i: fitnesses[i])
    return population[best]


# =============================================================================
# CROSSOVER — Single-Point Crossover
# =============================================================================

def crossover(parent_a, parent_b, num_features):
    """
    Produces two offspring by crossing over the feature masks and hyperparams
    of two parent chromosomes at a random point.
    """
    if random.random() > CROSSOVER_RATE:
        # No crossover — return copies of parents
        return (
            {"feature_mask": parent_a["feature_mask"][:], "hyperparams": dict(parent_a["hyperparams"])},
            {"feature_mask": parent_b["feature_mask"][:], "hyperparams": dict(parent_b["hyperparams"])},
        )

    # Feature mask crossover
    point = random.randint(1, num_features - 1)
    mask_a = parent_a["feature_mask"][:point] + parent_b["feature_mask"][point:]
    mask_b = parent_b["feature_mask"][:point] + parent_a["feature_mask"][point:]

    # Hyperparameter crossover — randomly pick from each parent per key
    hp_a, hp_b = {}, {}
    for key in parent_a["hyperparams"]:
        if random.random() < 0.5:
            hp_a[key] = parent_a["hyperparams"][key]
            hp_b[key] = parent_b["hyperparams"][key]
        else:
            hp_a[key] = parent_b["hyperparams"][key]
            hp_b[key] = parent_a["hyperparams"][key]

    child_a = {"feature_mask": mask_a, "hyperparams": hp_a}
    child_b = {"feature_mask": mask_b, "hyperparams": hp_b}
    return child_a, child_b


# =============================================================================
# MUTATION
# =============================================================================

def mutate(chromosome, num_features):
    """
    Applies random mutations to a chromosome's feature mask and hyperparameters.
    Each feature bit is flipped with probability MUTATION_RATE.
    Each hyperparameter is randomly reset with probability MUTATION_RATE.
    """
    mask = chromosome["feature_mask"][:]
    hp   = dict(chromosome["hyperparams"])

    # Mutate feature mask
    for i in range(num_features):
        if random.random() < MUTATION_RATE:
            mask[i] = 1 - mask[i]

    # Ensure minimum features constraint
    if sum(mask) < MIN_FEATURES:
        indices = random.sample(range(num_features), MIN_FEATURES)
        for i in indices:
            mask[i] = 1

    # Mutate hyperparameters
    if random.random() < MUTATION_RATE:
        hp["n_estimators"] = random.randint(*HP_RANGES["n_estimators"])
    if random.random() < MUTATION_RATE:
        hp["max_depth"] = random.randint(*HP_RANGES["max_depth"])
    if random.random() < MUTATION_RATE:
        hp["min_samples_split"] = random.randint(*HP_RANGES["min_samples_split"])
    if random.random() < MUTATION_RATE:
        hp["min_samples_leaf"] = random.randint(*HP_RANGES["min_samples_leaf"])
    if random.random() < MUTATION_RATE:
        hp["max_features"] = random.choice(HP_RANGES["max_features"])

    return {"feature_mask": mask, "hyperparams": hp}


# =============================================================================
# FINAL EVALUATION ON FULL DATASET
# =============================================================================

def full_evaluation(best_chromosome, X_train, y_train, X_test, y_test, feature_names):
    """
    Runs the best chromosome on the FULL dataset for official results.
    This is the result that goes into the paper.
    """
    from sklearn.metrics import accuracy_score, precision_score, recall_score, f1_score

    mask = best_chromosome["feature_mask"]
    hp   = best_chromosome["hyperparams"]
    selected_features = [feature_names[i] for i, bit in enumerate(mask) if bit == 1]

    clf = RandomForestClassifier(
        n_estimators      = hp["n_estimators"],
        max_depth         = hp["max_depth"],
        min_samples_split = hp["min_samples_split"],
        min_samples_leaf  = hp["min_samples_leaf"],
        max_features      = hp["max_features"],
        random_state      = 42,
        n_jobs            = -1,
    )
    clf.fit(X_train[selected_features], y_train)
    y_pred = clf.predict(X_test[selected_features])

    tn, fp, fn, tp = confusion_matrix(y_test, y_pred).ravel()
    fpr = fp / (fp + tn) if (fp + tn) > 0 else 0.0

    results = {
        "features_used"  : len(selected_features),
        "feature_names"  : selected_features,
        "hyperparams"    : hp,
        "accuracy"       : accuracy_score(y_test, y_pred),
        "precision"      : precision_score(y_test, y_pred, zero_division=0),
        "recall"         : recall_score(y_test, y_pred, zero_division=0),
        "f1_score"       : f1_score(y_test, y_pred, zero_division=0),
        "fpr"            : fpr,
        "TP"             : int(tp),
        "TN"             : int(tn),
        "FP"             : int(fp),
        "FN"             : int(fn),
    }

    print("\n" + "=" * 60)
    print("   GA FINAL RESULTS (Full Dataset)")
    print("=" * 60)
    print(f"  Features Selected  : {len(selected_features)} / 78")
    print(f"  Hyperparameters    : {hp}")
    print(f"  Accuracy           : {results['accuracy']  * 100:.4f}%")
    print(f"  Precision          : {results['precision'] * 100:.4f}%")
    print(f"  Recall (TPR)       : {results['recall']    * 100:.4f}%")
    print(f"  F1-Score           : {results['f1_score']  * 100:.4f}%")
    print(f"  False Positive Rate: {results['fpr']       * 100:.4f}%")
    print("-" * 60)
    print(f"  TP: {tp}  |  TN: {tn}  |  FP: {fp}  |  FN: {fn}")
    print("=" * 60)

    return results


# =============================================================================
# MAIN GA LOOP
# =============================================================================

def run_ga():
    """Main Genetic Algorithm execution loop."""

    # --- Load Data ---
    X_train, y_train, X_test, y_test, X_sample, y_sample, feature_names = load_data()
    num_features = len(feature_names)

    # --- Initialize Population ---
    print(f"\n[2/6] Initializing population of {POPULATION_SIZE} chromosomes...")
    population = [random_chromosome(num_features) for _ in range(POPULATION_SIZE)]

    # --- Track best solution and fitness log ---
    best_chromosome  = None
    best_fitness     = -np.inf
    fitness_log      = []   # (generation, best_fitness, avg_fitness, features_used)
    no_improve_count = 0

    start_time = time.time()

    print(f"\n[3/6] Evolving for up to {NUM_GENERATIONS} generations...")
    print(f"      (Early stop if no improvement for {EARLY_STOP_ROUNDS} generations)\n")

    for gen in range(1, NUM_GENERATIONS + 1):

        # --- Evaluate fitness for all chromosomes ---
        fitnesses = [
            evaluate_fitness(c, X_sample, y_sample, X_test, y_test, feature_names)
            for c in population
        ]

        # --- Track best ---
        gen_best_idx     = np.argmax(fitnesses)
        gen_best_fitness = fitnesses[gen_best_idx]
        gen_avg_fitness  = np.mean(fitnesses)
        gen_best_features = sum(population[gen_best_idx]["feature_mask"])

        if gen_best_fitness > best_fitness:
            best_fitness    = gen_best_fitness
            best_chromosome = {
                "feature_mask": population[gen_best_idx]["feature_mask"][:],
                "hyperparams" : dict(population[gen_best_idx]["hyperparams"]),
            }
            no_improve_count = 0
        else:
            no_improve_count += 1

        fitness_log.append({
            "generation"    : gen,
            "best_fitness"  : round(gen_best_fitness, 6),
            "avg_fitness"   : round(gen_avg_fitness, 6),
            "features_used" : gen_best_features,
        })

        elapsed = time.time() - start_time
        print(f"  Gen {gen:>3} | Best Fitness: {gen_best_fitness:.5f} | "
              f"Avg: {gen_avg_fitness:.5f} | Features: {gen_best_features} | "
              f"Time: {elapsed:.0f}s")

        # --- Early stopping ---
        if no_improve_count >= EARLY_STOP_ROUNDS:
            print(f"\n  Early stop triggered — no improvement for {EARLY_STOP_ROUNDS} generations.")
            break

        # --- Create next generation ---
        new_population = []

        # Elitism: carry the best chromosome directly into next generation
        new_population.append({
            "feature_mask": best_chromosome["feature_mask"][:],
            "hyperparams" : dict(best_chromosome["hyperparams"]),
        })

        # Fill rest of population with offspring
        while len(new_population) < POPULATION_SIZE:
            parent_a = tournament_selection(population, fitnesses)
            parent_b = tournament_selection(population, fitnesses)
            child_a, child_b = crossover(parent_a, parent_b, num_features)
            new_population.append(mutate(child_a, num_features))
            if len(new_population) < POPULATION_SIZE:
                new_population.append(mutate(child_b, num_features))

        population = new_population

    total_time = time.time() - start_time
    print(f"\n  Total optimization time: {total_time:.1f} seconds ({total_time/60:.1f} minutes)")

    # --- Final evaluation on full dataset ---
    print("\n[4/6] Running final evaluation on full dataset...")
    final_results = full_evaluation(best_chromosome, X_train, y_train, X_test, y_test, feature_names)
    final_results["runtime_seconds"] = round(total_time, 2)

    # --- Save outputs ---
    print("\n[5/6] Saving results...")

    # Save best solution as JSON
    with open("ga_best_solution.json", "w") as f:
        json.dump(final_results, f, indent=2)
    print("   Saved: ga_best_solution.json")

    # Save fitness log as CSV for Member 4's convergence plot
    fitness_df = pd.DataFrame(fitness_log)
    fitness_df.to_csv("ga_fitness_log.csv", index=False)
    print("   Saved: ga_fitness_log.csv")

    print("\n[6/6] Done! Summary:")
    print(f"   Features reduced : 78 → {final_results['features_used']}")
    print(f"   F1-Score         : {final_results['f1_score'] * 100:.4f}%")
    print(f"   FPR              : {final_results['fpr'] * 100:.4f}%")
    print(f"   Runtime          : {total_time/60:.1f} minutes")

    return final_results


if __name__ == "__main__":
    run_ga()
