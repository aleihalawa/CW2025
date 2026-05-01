# =============================================================================
# COMP2024 - Artificial Intelligence Methods
# Phase 1: Visualizations (Member 4)
# Description: Generates visual outputs for the baseline Random Forest model
#              including a Confusion Matrix and Feature Importance Plot.
#
# SETUP GUIDE:
#   1. Install dependencies: pip install pandas scikit-learn matplotlib seaborn
#   2. Ensure the following CSVs from Member 1 are in the same directory:
#        - X_train_final.csv
#        - y_train_final.csv
#        - X_test_final.csv
#        - y_test_final.csv
#   3. Run: python visualizations.py
#   4. Outputs saved as:
#        - confusion_matrix.png
#        - feature_importance.png
# =============================================================================

import pandas as pd
import matplotlib.pyplot as plt
import seaborn as sns
from sklearn.ensemble import RandomForestClassifier
from sklearn.metrics import confusion_matrix


# -----------------------------------------------------------------------------
# DATA LOADING
# -----------------------------------------------------------------------------

def load_data():
    """Loads the preprocessed datasets produced by Member 1."""
    print("1. Loading preprocessed data...")
    X_train = pd.read_csv("X_train_final.csv")
    y_train = pd.read_csv("y_train_final.csv")
    X_test  = pd.read_csv("X_test_final.csv")
    y_test  = pd.read_csv("y_test_final.csv")
    print(f"   Training samples: {len(X_train):,}  |  Test samples: {len(X_test):,}")
    return X_train, y_train, X_test, y_test


# -----------------------------------------------------------------------------
# MODEL TRAINING
# -----------------------------------------------------------------------------

def train_model(X_train, y_train):
    """Trains the same baseline Random Forest used in baseline_model.py."""
    print("2. Training Random Forest model...")
    clf = RandomForestClassifier(random_state=42, n_jobs=-1)
    clf.fit(X_train, y_train.values.ravel())
    return clf


# -----------------------------------------------------------------------------
# CONFUSION MATRIX PLOT
# -----------------------------------------------------------------------------

def plot_confusion_matrix(clf, X_test, y_test):
    """
    Generates and saves a confusion matrix heatmap.
    Shows TP, TN, FP, FN counts with clear labels.
    Output: confusion_matrix.png
    """
    print("3. Generating Confusion Matrix...")
    y_pred = clf.predict(X_test)

    cm = confusion_matrix(y_test, y_pred)

    fig, ax = plt.subplots(figsize=(7, 6))

    sns.heatmap(
        cm,
        annot=True,
        fmt=",d",
        cmap="Blues",
        xticklabels=["Predicted BENIGN", "Predicted ATTACK"],
        yticklabels=["Actual BENIGN",    "Actual ATTACK"],
        linewidths=0.5,
        linecolor="grey",
        ax=ax
    )

    ax.set_title("Confusion Matrix — Baseline Random Forest (CICIDS2017)",
                 fontsize=13, fontweight="bold", pad=15)
    ax.set_ylabel("Actual Label", fontsize=11)
    ax.set_xlabel("Predicted Label", fontsize=11)

    # Annotate each cell with its meaning
    tn, fp, fn, tp = cm.ravel()
    cell_labels = [
        (0, 0, f"TN\n{tn:,}"),
        (0, 1, f"FP\n{fp:,}"),
        (1, 0, f"FN\n{fn:,}"),
        (1, 1, f"TP\n{tp:,}"),
    ]
    for row, col, label in cell_labels:
        ax.text(col + 0.5, row + 0.75, label,
                ha="center", va="center",
                fontsize=9, color="grey")

    plt.tight_layout()
    plt.savefig("confusion_matrix.png", dpi=150, bbox_inches="tight")
    plt.close()
    print("   Saved: confusion_matrix.png")


# -----------------------------------------------------------------------------
# FEATURE IMPORTANCE PLOT
# -----------------------------------------------------------------------------

def plot_feature_importance(clf, feature_names, top_n=20):
    """
    Generates and saves a horizontal bar chart of the top N most important
    features as ranked by the trained Random Forest model.
    Output: feature_importance.png
    """
    print("4. Generating Feature Importance Plot...")

    # Extract importance scores from the trained model
    importances = clf.feature_importances_
    importance_df = pd.DataFrame({
        "Feature":    feature_names,
        "Importance": importances
    }).sort_values("Importance", ascending=False).head(top_n)

    # Reverse so highest importance is at the top of the chart
    importance_df = importance_df.sort_values("Importance", ascending=True)

    fig, ax = plt.subplots(figsize=(10, 8))

    bars = ax.barh(
        importance_df["Feature"],
        importance_df["Importance"],
        color=plt.cm.Blues(
            [0.4 + 0.6 * (i / top_n) for i in range(len(importance_df))]
        ),
        edgecolor="grey",
        linewidth=0.5
    )

    # Add value labels at the end of each bar
    for bar, val in zip(bars, importance_df["Importance"]):
        ax.text(
            bar.get_width() + 0.001,
            bar.get_y() + bar.get_height() / 2,
            f"{val:.4f}",
            va="center", ha="left", fontsize=8, color="dimgrey"
        )

    ax.set_title(
        f"Top {top_n} Feature Importances — Baseline Random Forest (CICIDS2017)",
        fontsize=13, fontweight="bold", pad=15
    )
    ax.set_xlabel("Mean Decrease in Impurity (Importance Score)", fontsize=11)
    ax.set_ylabel("Feature", fontsize=11)
    ax.spines["top"].set_visible(False)
    ax.spines["right"].set_visible(False)

    plt.tight_layout()
    plt.savefig("feature_importance.png", dpi=150, bbox_inches="tight")
    plt.close()
    print("   Saved: feature_importance.png")


# -----------------------------------------------------------------------------
# MAIN
# -----------------------------------------------------------------------------

def main():
    X_train, y_train, X_test, y_test = load_data()
    model = train_model(X_train, y_train)
    plot_confusion_matrix(model, X_test, y_test)
    plot_feature_importance(model, X_train.columns.tolist(), top_n=20)
    print("\nDone! Check confusion_matrix.png and feature_importance.png in your project folder.")

if __name__ == "__main__":
    main()
