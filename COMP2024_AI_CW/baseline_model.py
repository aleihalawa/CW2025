# =============================================================================
# COMP2024 - Artificial Intelligence Methods
# Phase 1 & 2: Baseline Model (Member 2)
# Description: Trains a Random Forest classifier on preprocessed CICIDS2017
#              data and evaluates performance metrics. Accepts a feature list
#              AND custom hyperparameters as input so Phase 2 metaheuristics
#              can call this script with different feature subsets and configs.
#
# SETUP GUIDE:
#   1. Install dependencies: pip install pandas scikit-learn
#   2. Ensure the following CSVs from Member 1 are in the same directory:
#        - X_train_final.csv
#        - y_train_final.csv
#        - X_test_final.csv
#        - y_test_final.csv
#   3. Run: python baseline_model.py
#
# PHASE 2 USAGE (for Members 3 and 4):
#   from baseline_model import run_baseline
#
#   # Feature selection only:
#   results = run_baseline(feature_list=["Feature A", "Feature B"])
#
#   # Hyperparameter tuning only:
#   results = run_baseline(hyperparams={"n_estimators": 200, "max_depth": 10})
#
#   # Both together (recommended):
#   results = run_baseline(
#       feature_list=["Feature A", "Feature B"],
#       hyperparams={"n_estimators": 200, "max_depth": 10,
#                    "min_samples_split": 5, "min_samples_leaf": 2,
#                    "max_features": "sqrt"}
#   )
# =============================================================================

import pandas as pd
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import (accuracy_score, precision_score,
                             recall_score, f1_score, confusion_matrix)


# -----------------------------------------------------------------------------
# DATA LOADING
# -----------------------------------------------------------------------------

def load_data():
    """
    Loads the preprocessed, split, and balanced datasets produced by Member 1.

    Returns:
        X_train, y_train, X_test, y_test as DataFrames.
    """
    print("1. Loading preprocessed data from Member 1...")
    X_train = pd.read_csv("X_train_final.csv")
    y_train = pd.read_csv("y_train_final.csv")
    X_test  = pd.read_csv("X_test_final.csv")
    y_test  = pd.read_csv("y_test_final.csv")
    print(f"   Training samples : {len(X_train):,}  |  Test samples: {len(X_test):,}")
    print(f"   Total features available: {X_train.shape[1]}")
    return X_train, y_train, X_test, y_test


# -----------------------------------------------------------------------------
# FEATURE SELECTION (KEY FUNCTION FOR PHASE 2)
# -----------------------------------------------------------------------------

def select_features(X_train, X_test, feature_list=None):
    """
    Filters the dataset to only the specified features.
    If no feature list is provided, ALL features are used (baseline mode).

    This is the core function that Phase 2 metaheuristics will call,
    passing in different feature subsets to evaluate.

    Args:
        X_train      : Full training feature DataFrame.
        X_test       : Full test feature DataFrame.
        feature_list : List of column names to keep, or None for all features.

    Returns:
        Filtered X_train and X_test DataFrames.
    """
    if feature_list is None:
        print("2. No feature list provided — using ALL features (baseline mode).")
        return X_train, X_test

    # Validate that all requested features actually exist in the dataset
    missing = [f for f in feature_list if f not in X_train.columns]
    if missing:
        raise ValueError(f"The following features were not found in the dataset: {missing}")

    print(f"2. Selecting {len(feature_list)} features from the dataset...")
    return X_train[feature_list], X_test[feature_list]


# -----------------------------------------------------------------------------
# MODEL TRAINING
# -----------------------------------------------------------------------------

def train_model(X_train, y_train, hyperparams=None):
    """
    Trains a Random Forest classifier with default or custom hyperparameters.

    Args:
        X_train    : Training features.
        y_train    : Training labels.
        hyperparams: Dict of RF hyperparameters, or None for defaults.
                     Supported keys:
                       - n_estimators     (int,   default 100)
                       - max_depth        (int or None, default None)
                       - min_samples_split(int,   default 2)
                       - min_samples_leaf (int,   default 1)
                       - max_features     (str or None, default 'sqrt')

    Returns:
        Trained RandomForestClassifier object.
    """
    # Default hyperparameters (Phase 1 baseline settings)
    default_params = {
        "n_estimators"      : 100,
        "max_depth"         : None,
        "min_samples_split" : 2,
        "min_samples_leaf"  : 1,
        "max_features"      : "sqrt",
    }

    # Merge defaults with any custom hyperparams passed in
    if hyperparams is not None:
        # Only accept valid keys — ignore anything unexpected
        valid_keys = set(default_params.keys())
        filtered = {k: v for k, v in hyperparams.items() if k in valid_keys}
        default_params.update(filtered)
        print(f"3. Training Random Forest with custom hyperparameters: {filtered}")
    else:
        print("3. Training Random Forest with default hyperparameters...")

    clf = RandomForestClassifier(
        n_estimators      = default_params["n_estimators"],
        max_depth         = default_params["max_depth"],
        min_samples_split = default_params["min_samples_split"],
        min_samples_leaf  = default_params["min_samples_leaf"],
        max_features      = default_params["max_features"],
        random_state      = 42,
        n_jobs            = -1,
    )
    clf.fit(X_train, y_train.values.ravel())
    return clf


# -----------------------------------------------------------------------------
# EVALUATION
# -----------------------------------------------------------------------------

def evaluate_model(clf, X_test, y_test, feature_count):
    """
    Evaluates the trained model and prints all required metrics.
    Includes FPR (False Positive Rate) which is critical in IDS evaluation.

    Args:
        clf           : Trained classifier.
        X_test        : Test features.
        y_test        : True test labels.
        feature_count : Number of features used (for reporting).

    Returns:
        Dictionary of all computed metrics.
    """
    print("4. Evaluating model on unseen test data...")
    y_pred = clf.predict(X_test)

    # Standard classification metrics
    acc  = accuracy_score(y_test, y_pred)
    prec = precision_score(y_test, y_pred, zero_division=0)
    rec  = recall_score(y_test, y_pred, zero_division=0)
    f1   = f1_score(y_test, y_pred, zero_division=0)

    # Confusion matrix for FPR calculation
    tn, fp, fn, tp = confusion_matrix(y_test, y_pred).ravel()
    fpr = fp / (fp + tn) if (fp + tn) > 0 else 0.0

    results = {
        "features_used" : feature_count,
        "accuracy"      : acc,
        "precision"     : prec,
        "recall"        : rec,
        "f1_score"      : f1,
        "fpr"           : fpr,
        "TP"            : int(tp),
        "TN"            : int(tn),
        "FP"            : int(fp),
        "FN"            : int(fn),
    }

    # Print results table
    print("\n" + "=" * 50)
    print("   PHASE 1: BASELINE MODEL RESULTS")
    print("=" * 50)
    print(f"  Features Used      : {feature_count}")
    print(f"  Accuracy           : {acc  * 100:.4f}%")
    print(f"  Precision          : {prec * 100:.4f}%")
    print(f"  Recall (TPR)       : {rec  * 100:.4f}%")
    print(f"  F1-Score           : {f1   * 100:.4f}%")
    print(f"  False Positive Rate: {fpr  * 100:.4f}%")
    print("-" * 50)
    print(f"  TP: {tp}  |  TN: {tn}  |  FP: {fp}  |  FN: {fn}")
    print("=" * 50)

    return results


# -----------------------------------------------------------------------------
# MAIN ENTRY POINT
# -----------------------------------------------------------------------------

def run_baseline(feature_list=None, hyperparams=None):
    """
    Full pipeline: load data, select features, train, and evaluate.

    This function is the main entry point for Phase 2 metaheuristics.
    Members 3 and 4 should import and call this function like:

        from baseline_model import run_baseline

        results = run_baseline(
            feature_list=["feature_A", "feature_B", ...],
            hyperparams={"n_estimators": 200, "max_depth": 10, ...}
        )

    Args:
        feature_list : List of feature names to use. None = all 78 features.
        hyperparams  : Dict of RF hyperparameter overrides. None = defaults.

    Returns:
        Dictionary of evaluation metrics:
        {features_used, accuracy, precision, recall, f1_score, fpr, TP, TN, FP, FN}
    """
    X_train, y_train, X_test, y_test = load_data()
    X_train_sel, X_test_sel = select_features(X_train, X_test, feature_list)
    model = train_model(X_train_sel, y_train, hyperparams=hyperparams)
    results = evaluate_model(model, X_test_sel, y_test, X_train_sel.shape[1])
    return results


if __name__ == "__main__":
    # When run directly, use ALL features to establish the baseline benchmark
    run_baseline(feature_list=None)
