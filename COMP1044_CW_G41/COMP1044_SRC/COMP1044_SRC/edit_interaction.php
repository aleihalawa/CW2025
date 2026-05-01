<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit();
}
require 'db_connect.php';

if (!isset($_GET['id']) || !is_numeric($_GET['id'])) {
    header("Location: view_interactions.php");
    exit();
}
$interaction_id = intval($_GET['id']);

// Fetch customers for the dropdown
$customers = [];
$result = $conn->query("SELECT id, name FROM customers ORDER BY name");
while ($row = $result->fetch_assoc()) {
    $customers[] = $row;
}

// Fetch the current interaction
$stmt = $conn->prepare("SELECT * FROM interaction_history WHERE interaction_id = ?");
$stmt->bind_param("i", $interaction_id);
$stmt->execute();
$interaction = $stmt->get_result()->fetch_assoc();
$stmt->close();

if (!$interaction) {
    exit("Interaction not found.");
}

$success = "";
$error = "";

if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $customer_id = intval($_POST['customer_id']);
    $type = trim($_POST['type']);
    $description = trim($_POST['description']);
    $date = $_POST['date'];

    if ($customer_id && $type && $description && $date) {
        $stmt = $conn->prepare("UPDATE interaction_history SET customer_id=?, date=?, type=?, description=? WHERE interaction_id=?");
        $stmt->bind_param("isssi", $customer_id, $date, $type, $description, $interaction_id);

        if ($stmt->execute()) {
            $success = "Interaction updated!";
            // Fetch the new data for the form
            $interaction = [
                'customer_id' => $customer_id,
                'date' => $date,
                'type' => $type,
                'description' => $description
            ];
        } else {
            $error = "Something went wrong.";
        }
        $stmt->close();
    } else {
        $error = "Please fill out all fields.";
    }
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Edit Interaction - ABB Robotics CRM</title>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link href="https://fonts.googleapis.com/css?family=Nunito:400,600,700" rel="stylesheet">
    <style>
        body { margin: 0; font-family: 'Nunito', sans-serif; background: #f4f8fc; color: #26375a; }
        .banner {
            display: flex; justify-content: space-between; align-items: center;
            background: linear-gradient(90deg, #2068b8 60%, #ffffff 100%);
            padding: 28px 36px 18px 36px; border-bottom: 1.5px solid #dbeafe; min-height: 96px;
        }
        .banner .welcome { color: #fff; font-size: 2.1rem; font-weight: 700; letter-spacing: 1px; }
        .banner .logo {
            height: 54px; background: #fff; border-radius: 10px; padding: 7px 18px 7px 12px;
            box-shadow: 0 4px 14px rgba(32,104,184,0.09); display: flex; align-items: center;
        }
        .banner .logo img { height: 40px; width: auto; display: block; }
        .nav { display: flex; justify-content: flex-start; align-items: center;
            background: #e8f0fa; padding: 0 36px; border-bottom: 1px solid #e0e7ef;
        }
        .nav a {
            display: inline-block; color: #2068b8; font-weight: 600; font-size: 1.1rem;
            text-decoration: none; padding: 16px 22px 13px 0; transition: color 0.18s;
            margin-right: 18px; border-bottom: 2px solid transparent;
        }
        .nav a.active, .nav a:hover { color: #0c3266; border-bottom: 2.5px solid #2068b8; background: #f0f6fd; }
        .container {
            max-width: 540px; margin: 44px auto; background: #fff; border-radius: 16px;
            box-shadow: 0 6px 28px rgba(32,104,184,0.09); padding: 38px 38px 32px 38px;
        }
        h2 { color: #2068b8; font-size: 1.7rem; margin-top: 0; font-weight: 800; letter-spacing: 1px; text-align: center; }
        label { font-weight: 700; font-size: 1rem; color: #2068b8; display: block; margin-bottom: 4px; margin-top: 16px; }
        select, input[type="text"], input[type="date"], textarea {
            width: 100%; padding: 9px 13px; border: 1.4px solid #bdd6f6; border-radius: 6px; font-size: 1.05rem;
            margin-bottom: 6px; background: #fafdff; color: #26375a; font-family: 'Nunito', sans-serif; transition: border 0.18s;
        }
        select:focus, input[type="text"]:focus, input[type="date"]:focus, textarea:focus {
            border: 1.7px solid #2068b8; background: #f0f6fd; outline: none;
        }
        textarea { min-height: 64px; }
        .btn-row {
            display: flex; justify-content: space-between; align-items: center; margin-top: 24px;
        }
        .btn-row .submit-btn {
            background: #2068b8; color: #fff; border: none; padding: 10px 28px; border-radius: 7px;
            font-size: 1.1rem; font-weight: 700; cursor: pointer; transition: background 0.15s; letter-spacing: 0.5px;
        }
        .btn-row .submit-btn:hover { background: #114077; }
        .btn-row .done-btn {
            background: #fff; color: #2068b8; border: 2px solid #2068b8; padding: 10px 28px; border-radius: 7px;
            font-size: 1.1rem; font-weight: 700; text-decoration: none; cursor: pointer;
            transition: background 0.15s, color 0.15s; margin-left: 10px;
        }
        .btn-row .done-btn:hover { background: #2068b8; color: #fff; }
        .success-msg {
            background: #d1f9df; color: #14653f; padding: 12px; border-radius: 6px; margin-bottom: 16px;
            text-align: center; border: 1.4px solid #a4e2b4;
        }
        .error-msg {
            background: #ffeaea; color: #d40000; padding: 12px; border-radius: 6px; margin-bottom: 16px;
            text-align: center; border: 1.4px solid #ffc2c2;
        }
        @media (max-width: 700px) {
            .container { padding: 18px 3vw; }
            .banner, .nav { padding: 12px 6px; }
            h2 { font-size: 1.15rem;}
            label { font-size: 0.98rem;}
        }
    </style>
</head>
<body>
  <div class="banner">
    <div class="welcome">Edit Interaction</div>
    <div class="logo">
      <img src="img/logo.png" alt="ABB Robotics Logo">
    </div>
  </div>
  <nav class="nav">
    <a href="dashboard.php">Dashboard</a>
    <a href="view_customers.php">Customers</a>
    <a href="view_leads.php">Leads</a>
    <a href="view_interactions.php" class="active">Interactions</a>
    <a href="logout.php" style="margin-left:auto;color:#d3342c;">Logout</a>
  </nav>
  <div class="container">
    <h2>Edit Customer Interaction</h2>
    <?php if ($success) echo "<div class='success-msg'>$success</div>"; ?>
    <?php if ($error) echo "<div class='error-msg'>$error</div>"; ?>
    <form method="POST">
        <label>Customer:</label>
        <select name="customer_id" required>
            <option value="">-- Select --</option>
            <?php foreach ($customers as $c): ?>
                <option value="<?= $c['id'] ?>" <?= $c['id'] == $interaction['customer_id'] ? 'selected' : '' ?>>
                    <?= htmlspecialchars($c['name']) ?>
                </option>
            <?php endforeach; ?>
        </select>
        <label>Type:</label>
        <input type="text" name="type" value="<?= htmlspecialchars($interaction['type']) ?>" required>
        <label>Description:</label>
        <textarea name="description" required><?= htmlspecialchars($interaction['description']) ?></textarea>
        <label>Date:</label>
        <input type="date" name="date" value="<?= htmlspecialchars($interaction['date']) ?>" required>
        <div class="btn-row">
            <button type="submit" class="submit-btn">Update Interaction</button>
            <a href="view_interactions.php" class="done-btn">Done</a>
        </div>
    </form>
  </div>
</body>
</html>
