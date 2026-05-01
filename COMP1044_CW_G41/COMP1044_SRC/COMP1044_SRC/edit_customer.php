<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit();
}
require 'db.php';

// --- Your original edit customer PHP logic here ---

// Example only; use your original logic below!
$id = $_GET['id'] ?? null;
$success = "";
$error = "";

if ($id && $_SERVER['REQUEST_METHOD'] === 'POST') {
    $name = trim($_POST['name']);
    $company = trim($_POST['company']);
    $email = trim($_POST['email']);
    $phone = trim($_POST['phone']);
    $address = trim($_POST['address']);

    if (!empty($name) && !empty($company)) {
        if (!empty($email) && !filter_var($email, FILTER_VALIDATE_EMAIL)) {
            $error = "Invalid email format.";
        } elseif (!preg_match('/^\d+$/', $phone)) {
            $error = "Phone number must contain digits only.";
        } else {
            $stmt = $conn->prepare("UPDATE customers SET name=?, email=?, phone=?, address=?, company=? WHERE id=?");
            $stmt->bind_param("sssssi", $name, $email, $phone, $address, $company, $id);

            if ($stmt->execute()) {
                $success = "Customer updated successfully!";
            } else {
                $error = "Failed to update customer: " . $stmt->error;
            }

            $stmt->close();
        }
    } else {
        $error = "Name and Company are required.";
    }
}

if ($id) {
    $stmt = $conn->prepare("SELECT * FROM customers WHERE id=?");
    $stmt->bind_param("i", $id);
    $stmt->execute();
    $customer = $stmt->get_result()->fetch_assoc();
    $stmt->close();
} else {
    $customer = null;
}
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>Edit Customer - ABB Robotics CRM</title>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link href="https://fonts.googleapis.com/css?family=Nunito:400,600,700" rel="stylesheet">
    <style>
        body {
            margin: 0;
            font-family: 'Nunito', sans-serif;
            background: #f4f8fc;
            color: #26375a;
        }
        .banner {
            display: flex;
            justify-content: space-between;
            align-items: center;
            background: linear-gradient(90deg, #2068b8 60%, #ffffff 100%);
            padding: 28px 36px 18px 36px;
            border-bottom: 1.5px solid #dbeafe;
            min-height: 96px;
        }
        .banner .welcome {
            color: #fff;
            font-size: 2.1rem;
            font-weight: 700;
            letter-spacing: 1px;
        }
        .banner .logo {
            height: 54px;
            background: #fff;
            border-radius: 10px;
            padding: 7px 18px 7px 12px;
            box-shadow: 0 4px 14px rgba(32,104,184,0.09);
            display: flex;
            align-items: center;
        }
        .banner .logo img {
            height: 40px;
            width: auto;
            display: block;
        }
        .nav {
            display: flex;
            justify-content: flex-start;
            align-items: center;
            background: #e8f0fa;
            padding: 0 36px;
            border-bottom: 1px solid #e0e7ef;
        }
        .nav a {
            display: inline-block;
            color: #2068b8;
            font-weight: 600;
            font-size: 1.1rem;
            text-decoration: none;
            padding: 16px 22px 13px 0;
            transition: color 0.18s;
            margin-right: 18px;
            border-bottom: 2px solid transparent;
        }
        .nav a.active,
        .nav a:hover {
            color: #0c3266;
            border-bottom: 2.5px solid #2068b8;
            background: #f0f6fd;
        }
        .container {
            max-width: 520px;
            margin: 42px auto;
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 6px 28px rgba(32,104,184,0.09);
            padding: 38px 38px 32px 38px;
        }
        h1 {
            color: #2068b8;
            font-size: 1.8rem;
            margin-bottom: 18px;
            margin-top: 0;
            font-weight: 800;
            letter-spacing: 1px;
            text-align: center;
        }
        label {
            font-weight: 700;
            font-size: 1rem;
            color: #2068b8;
            display: block;
            margin-bottom: 4px;
            margin-top: 18px;
        }
        input[type="text"], input[type="email"] {
            width: 100%;
            padding: 9px 13px;
            border: 1.4px solid #bdd6f6;
            border-radius: 6px;
            font-size: 1.05rem;
            margin-bottom: 6px;
            background: #fafdff;
            color: #26375a;
            font-family: 'Nunito', sans-serif;
            transition: border 0.18s;
        }
        input[type="text"]:focus, input[type="email"]:focus {
            border: 1.7px solid #2068b8;
            background: #f0f6fd;
            outline: none;
        }
        textarea {
            width: 100%;
            padding: 9px 13px;
            border: 1.4px solid #bdd6f6;
            border-radius: 6px;
            font-size: 1.05rem;
            min-height: 64px;
            background: #fafdff;
            color: #26375a;
            font-family: 'Nunito', sans-serif;
            margin-bottom: 8px;
            transition: border 0.18s;
        }
        textarea:focus {
            border: 1.7px solid #2068b8;
            background: #f0f6fd;
            outline: none;
        }
        .btn-row {
            display: flex;
            justify-content: space-between;
            align-items: center;
            margin-top: 28px;
        }
        .btn-row .submit-btn {
            background: #2068b8;
            color: #fff;
            border: none;
            padding: 10px 28px;
            border-radius: 7px;
            font-size: 1.1rem;
            font-weight: 700;
            cursor: pointer;
            transition: background 0.15s;
            letter-spacing: 0.5px;
        }
        .btn-row .submit-btn:hover {
            background: #114077;
        }
        .btn-row .done-btn {
            background: #fff;
            color: #2068b8;
            border: 2px solid #2068b8;
            padding: 10px 28px;
            border-radius: 7px;
            font-size: 1.1rem;
            font-weight: 700;
            text-decoration: none;
            cursor: pointer;
            transition: background 0.15s, color 0.15s;
            margin-left: 10px;
        }
        .btn-row .done-btn:hover {
            background: #2068b8;
            color: #fff;
        }
        .success-msg {
            background: #d1f9df;
            color: #14653f;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 16px;
            text-align: center;
            border: 1.4px solid #a4e2b4;
        }
        .error-msg {
            background: #ffeaea;
            color: #d40000;
            padding: 12px;
            border-radius: 6px;
            margin-bottom: 16px;
            text-align: center;
            border: 1.4px solid #ffc2c2;
        }
        @media (max-width: 700px) {
            .container { padding: 18px 3vw; }
            .banner, .nav { padding: 12px 6px; }
            h1 { font-size: 1.15rem;}
            label { font-size: 0.98rem;}
        }
    </style>
</head>
<body>
  <div class="banner">
    <div class="welcome">Edit Customer</div>
    <div class="logo">
      <img src="img/logo.png" alt="ABB Robotics Logo">
    </div>
  </div>
  <nav class="nav">
    <a href="dashboard.php">Dashboard</a>
    <a href="view_customers.php" class="active">Customers</a>
    <a href="view_leads.php">Leads</a>
    <a href="view_interactions.php">Interactions</a>
    <a href="logout.php" style="margin-left:auto;color:#d3342c;">Logout</a>
  </nav>
  <div class="container">
    <h1>Edit Customer</h1>
    <?php if ($success) echo "<div class='success-msg'>$success</div>"; ?>
    <?php if ($error) echo "<div class='error-msg'>$error</div>"; ?>
    <?php if ($customer): ?>
    <form method="POST">
        <label>Name:</label>
        <input type="text" name="name" value="<?= htmlspecialchars($customer['name']) ?>" required>

        <label>Company:</label>
        <input type="text" name="company" value="<?= htmlspecialchars($customer['company']) ?>" required>

        <label>Email:</label>
        <input type="email" name="email" value="<?= htmlspecialchars($customer['email']) ?>">

        <label>Phone:</label>
        <input type="text" name="phone" pattern="\d+" title="Phone number must contain digits only" value="<?= htmlspecialchars($customer['phone']) ?>" required>

        <label>Address:</label>
        <textarea name="address"><?= htmlspecialchars($customer['address']) ?></textarea>

        <div class="btn-row">
            <button type="submit" class="submit-btn">Update Customer</button>
            <a href="view_customers.php" class="done-btn">Done</a>
        </div>
    </form>
    <?php else: ?>
      <div class="error-msg">Customer not found.</div>
    <?php endif; ?>
  </div>
</body>
</html>
