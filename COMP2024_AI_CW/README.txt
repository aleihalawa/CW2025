============================================================
  COMP2024 Coursework — Group MoSalah
  AI-Based Intrusion Detection System Using Metaheuristic Optimization
============================================================

GROUP MEMBERS
-------------
  Member 1 — Ziad Ahmed      (Data Architect)
  Member 2 — Alei Ahmed      (Baseline Engineer / Group Leader)
  Member 3 — Belal Ibrahim   (PSO Specialist / Paper Writer)
  Member 4 — Mohammed Senan  (SA Specialist / Visualizations)

------------------------------------------------------------
REQUIREMENTS
------------------------------------------------------------
  Python 3.8 or above

  Install all dependencies using:
    pip install pandas scikit-learn numpy imbalanced-learn matplotlib seaborn

------------------------------------------------------------
DATASET
------------------------------------------------------------
  This project uses the CICIDS2017 dataset (4 days):
    - Tuesday-WorkingHours.pcap_ISCX.csv
    - Wednesday-workingHours.pcap_ISCX.csv
    - Thursday-WorkingHours-Morning-WebAttacks.pcap_ISCX.csv
    - Thursday-WorkingHours-Afternoon-Infilteration.pcap_ISCX.csv
    - Friday-WorkingHours-Morning.pcap_ISCX.csv
    - Friday-WorkingHours-Afternoon-DDos.pcap_ISCX.csv
    - Friday-WorkingHours-Afternoon-PortScan.pcap_ISCX.csv

  Monday was excluded as it contains only benign traffic.

  All CSV files must remain in the same folder as the scripts.

------------------------------------------------------------
RUN ORDER
------------------------------------------------------------
  Run the scripts in this exact order:

  STEP 1 — data_prep.py
    Loads and cleans all 7 CICIDS2017 CSV files.
    Applies binary encoding, Min-Max scaling, SMOTE balancing,
    and caps training data at 400,000 samples.
    Outputs: X_train_final.csv, y_train_final.csv,
             X_test_final.csv,  y_test_final.csv

    NOTE: If the 4 processed CSV files are already present
    in the folder, you can skip this step and proceed to Step 2.
    Pre-processed files are included to save runtime.

  STEP 2 — baseline_model.py
    Trains a Random Forest on all 78 features with default settings.
    Outputs performance metrics and the confusion matrix.

  STEP 3 — ga_optimization.py
    Runs the Genetic Algorithm for joint feature selection
    and hyperparameter optimization.
    Estimated runtime: ~53 minutes
    Outputs: ga_best_solution.json, ga_fitness_log.csv

    NOTE: Pre-computed result files are included in the folder.
    You can inspect ga_best_solution.json without re-running.

  STEP 4 — pso_optimization.py
    Runs Particle Swarm Optimization.
    Estimated runtime: ~67 minutes
    Outputs: pso_best_solution.json, pso_fitness_log.csv

    NOTE: Pre-computed result files are included in the folder.

  STEP 5 — sa_optimization.py
    Runs Simulated Annealing.
    Estimated runtime: ~276 minutes
    Outputs: sa_best_solution.json, sa_fitness_log.csv

    NOTE: Pre-computed result files are included in the folder.

  STEP 6 — visualizations.py
    Generates confusion matrix and feature importance charts
    using the baseline model results.
    Outputs: confusion_matrix.png, feature_importance.png

------------------------------------------------------------
PRE-COMPUTED RESULTS
------------------------------------------------------------
  All result files are already included in this folder:

  JSON result files (best solutions found):
    ga_best_solution.json
    pso_best_solution.json
    sa_best_solution.json

  Fitness log files (convergence history):
    ga_fitness_log.csv
    pso_fitness_log.csv
    sa_fitness_log.csv

  Visualization files (generated charts):
    confusion_matrix.png
    feature_importance.png
    group_convergence_comparison.png
    metrics_comparison.png
    feature_reduction_comparison.png

------------------------------------------------------------
IMPORTANT NOTES
------------------------------------------------------------
  - All files must remain in the same folder. Do not move
    scripts or data files into subfolders.

  - The optimization scripts (Steps 3-5) have long runtimes.
    Pre-computed result files are included so you do not need
    to re-run them unless you wish to verify the results.

  - Each optimization script uses a 20% stratified subsample
    of the training data during optimization for speed.
    Final evaluation is always performed on the full dataset.

  - Random seeds are not fixed in the metaheuristic scripts,
    so re-running may produce slightly different results.
    The pre-computed JSON files contain the exact results
    reported in the paper.

============================================================
