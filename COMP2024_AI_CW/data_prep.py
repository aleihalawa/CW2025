# =============================================================================
# COMP2024 - Artificial Intelligence Methods
# Data Preparation — Multi-Day CICIDS2017 (Member 1)
# Description: Loads and combines all relevant days of the CICIDS2017 dataset,
#              applies cleaning, binary encoding, normalization, SMOTE balancing,
#              and stratified sampling. Outputs 4 CSV files ready for use by
#              all other scripts without any changes needed.
#
# DATASET FILES REQUIRED (place in same folder):
#   - Tuesday-WorkingHours.pcap_ISCX.csv
#   - Wednesday-workingHours.pcap_ISCX.csv
#   - Thursday-WorkingHours-Morning-WebAttacks.pcap_ISCX.csv
#   - Thursday-WorkingHours-Afternoon-Infilteration.pcap_ISCX.csv
#   - Friday-WorkingHours-Morning.pcap_ISCX.csv
#   - Friday-WorkingHours-Afternoon-PortScan.pcap_ISCX.csv
#   - Friday-WorkingHours-Afternoon-DDos.pcap_ISCX.csv
#
# SETUP GUIDE:
#   1. pip install pandas numpy scikit-learn imbalanced-learn
#   2. Place all CSV files in the same folder as this script
#   3. Run: python data_prep.py
#
# OUTPUTS:
#   - X_train_final.csv
#   - y_train_final.csv
#   - X_test_final.csv
#   - y_test_final.csv
# =============================================================================

import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler
from sklearn.model_selection import train_test_split
from imblearn.over_sampling import SMOTE

# =============================================================================
# CONFIGURATION
# =============================================================================

# All relevant CICIDS2017 files (Monday excluded — benign only, no attacks)
DATASET_FILES = [
    "Tuesday-WorkingHours.pcap_ISCX.csv",
    "Wednesday-workingHours.pcap_ISCX.csv",
    "Thursday-WorkingHours-Morning-WebAttacks.pcap_ISCX.csv",
    "Thursday-WorkingHours-Afternoon-Infilteration.pcap_ISCX.csv",
    "Friday-WorkingHours-Morning.pcap_ISCX.csv",
    "Friday-WorkingHours-Afternoon-PortScan.pcap_ISCX.csv",
    "Friday-WorkingHours-Afternoon-DDos.pcap_ISCX.csv",
]

MAX_TRAIN_ROWS  = 400000   # Cap on training set size after SMOTE
TEST_SIZE       = 0.20     # 80/20 train/test split
RANDOM_STATE    = 42


# =============================================================================
# STEP 1 — LOAD AND COMBINE ALL FILES
# =============================================================================

print("=" * 60)
print("  COMP2024 — Multi-Day Data Preparation (CICIDS2017)")
print("=" * 60)
print("\n[1/7] Loading and combining dataset files...")

dataframes = []
for file in DATASET_FILES:
    try:
        df_temp = pd.read_csv(file, low_memory=False)
        df_temp.columns = df_temp.columns.str.strip()
        rows, cols = df_temp.shape
        print(f"   ✓ {file}")
        print(f"     Rows: {rows:,}  |  Attack types: {df_temp['Label'].unique().tolist()}")
        dataframes.append(df_temp)
    except FileNotFoundError:
        print(f"   ✗ MISSING: {file} — skipping this file")

df = pd.concat(dataframes, ignore_index=True)
print(f"\n   Combined total rows: {df.shape[0]:,}")
print(f"   Total features     : {df.shape[1] - 1} (excluding label)")


# =============================================================================
# STEP 2 — CLEAN DATA
# =============================================================================

print("\n[2/7] Cleaning data...")
print(f"   Rows before cleaning: {df.shape[0]:,}")

# Replace infinity values with NaN
df.replace([np.inf, -np.inf], np.nan, inplace=True)

# Drop rows with any NaN values
df.dropna(inplace=True)

# Drop duplicate rows
df.drop_duplicates(inplace=True)

print(f"   Rows after cleaning : {df.shape[0]:,}")


# =============================================================================
# STEP 3 — BINARY LABEL ENCODING
# =============================================================================

print("\n[3/7] Applying binary label encoding...")
print("   Attack type distribution before encoding:")
print(df["Label"].value_counts().to_string())

df["Label"] = df["Label"].apply(lambda x: 0 if x.strip() == "BENIGN" else 1)

print("\n   Binary distribution after encoding:")
print(df["Label"].value_counts().to_string())
print(f"   BENIGN (0): {(df['Label'] == 0).sum():,}")
print(f"   ATTACK (1): {(df['Label'] == 1).sum():,}")


# =============================================================================
# STEP 4 — SEPARATE FEATURES AND LABELS
# =============================================================================

print("\n[4/7] Separating features and labels...")
X = df.drop("Label", axis=1)
y = df["Label"]
print(f"   Features (X): {X.shape[1]} columns")
print(f"   Labels   (y): {y.shape[0]:,} rows")


# =============================================================================
# STEP 5 — TRAIN/TEST SPLIT
# =============================================================================

print(f"\n[5/7] Splitting into train ({int((1-TEST_SIZE)*100)}%) and test ({int(TEST_SIZE*100)}%)...")
X_train, X_test, y_train, y_test = train_test_split(
    X, y,
    test_size=TEST_SIZE,
    random_state=RANDOM_STATE,
    stratify=y
)
print(f"   Training set : {X_train.shape[0]:,} rows")
print(f"   Test set     : {X_test.shape[0]:,} rows")


# =============================================================================
# STEP 6 — NORMALIZATION (fit on train only to prevent leakage)
# =============================================================================

print("\n[6/7] Applying MinMax normalization (fit on training set only)...")
scaler = MinMaxScaler()
X_train_scaled = scaler.fit_transform(X_train)
X_test_scaled  = scaler.transform(X_test)

# Convert back to DataFrames with column names
X_train_scaled = pd.DataFrame(X_train_scaled, columns=X.columns)
X_test_scaled  = pd.DataFrame(X_test_scaled,  columns=X.columns)
y_train = y_train.reset_index(drop=True)
y_test  = y_test.reset_index(drop=True)

print("   Normalization complete — no data leakage applied")


# =============================================================================
# STEP 7 — SMOTE + STRATIFIED CAP
# =============================================================================

print("\n[7/7] Applying SMOTE and capping training set...")
print(f"   Before SMOTE:")
print(f"     BENIGN (0): {(y_train == 0).sum():,}")
print(f"     ATTACK (1): {(y_train == 1).sum():,}")

smote = SMOTE(random_state=RANDOM_STATE)
X_train_resampled, y_train_resampled = smote.fit_resample(X_train_scaled, y_train)

print(f"\n   After SMOTE:")
print(f"     BENIGN (0): {(y_train_resampled == 0).sum():,}")
print(f"     ATTACK (1): {(y_train_resampled == 1).sum():,}")
print(f"     Total      : {len(X_train_resampled):,}")

# Apply stratified cap if training set exceeds MAX_TRAIN_ROWS
if len(X_train_resampled) > MAX_TRAIN_ROWS:
    print(f"\n   Capping training set to {MAX_TRAIN_ROWS:,} rows (stratified)...")
    X_train_resampled = pd.DataFrame(X_train_resampled, columns=X.columns)
    y_train_resampled = pd.Series(y_train_resampled)

    X_train_final, _, y_train_final, _ = train_test_split(
        X_train_resampled, y_train_resampled,
        train_size=MAX_TRAIN_ROWS,
        random_state=RANDOM_STATE,
        stratify=y_train_resampled
    )
    print(f"   Final training set : {len(X_train_final):,} rows")
    print(f"     BENIGN (0): {(y_train_final == 0).sum():,}")
    print(f"     ATTACK (1): {(y_train_final == 1).sum():,}")
else:
    X_train_final = pd.DataFrame(X_train_resampled, columns=X.columns)
    y_train_final = pd.Series(y_train_resampled)
    print(f"   Training set under cap — no trimming needed")

# Reset indices
X_train_final = X_train_final.reset_index(drop=True)
y_train_final = y_train_final.reset_index(drop=True)


# =============================================================================
# SAVE OUTPUTS
# =============================================================================

print("\n[8/8] Saving final datasets...")
X_train_final.to_csv("X_train_final.csv", index=False)
y_train_final.to_csv("y_train_final.csv", index=False)
X_test_scaled.to_csv("X_test_final.csv",  index=False)
y_test.to_csv("y_test_final.csv",          index=False)

print("   Saved: X_train_final.csv")
print("   Saved: y_train_final.csv")
print("   Saved: X_test_final.csv")
print("   Saved: y_test_final.csv")

print("\n" + "=" * 60)
print("  DATA PREPARATION COMPLETE")
print("=" * 60)
print(f"  Training samples : {len(X_train_final):,}")
print(f"  Test samples     : {len(X_test_scaled):,}")
print(f"  Features         : {X_train_final.shape[1]}")
print("  Ready for baseline_model.py and all algorithm scripts")
print("=" * 60)
