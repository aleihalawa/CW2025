<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit();
}
$username = isset($_SESSION['user_name']) ? $_SESSION['user_name'] : 'User';
?>
<!DOCTYPE html>
<html lang="en">
<head>
  <meta charset="UTF-8" />
  <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
  <title>Dashboard - ABB Robotics CRM</title>
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
      width: auto;
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
      padding: 32px 8vw 32px 8vw;
      display: flex;
      flex-wrap: wrap;
      gap: 34px;
      justify-content: flex-start;
      background: #f4f8fc;
      min-height: 90vh;
    }
    .card {
      flex: 0 1 310px;
      background: #fff;
      border-radius: 15px;
      box-shadow: 0 6px 28px rgba(32,104,184,0.09);
      padding: 32px 24px 22px 24px;
      display: flex;
      flex-direction: column;
      align-items: flex-start;
      justify-content: flex-start;
      transition: box-shadow 0.18s;
      border: 1.5px solid #e5eaf3;
    }
    .card:hover {
      box-shadow: 0 10px 40px rgba(32,104,184,0.13);
      border-color: #b6daf7;
    }
    .card h2 {
      font-size: 1.3rem;
      margin: 0 0 12px 0;
      color: #2068b8;
      font-weight: 700;
    }
    .card p {
      color: #4061a2;
      margin-bottom: 18px;
      font-size: 1.06rem;
    }
    .card a {
      margin-top: auto;
      padding: 9px 22px;
      color: #fff;
      background: #2068b8;
      border-radius: 6px;
      font-weight: 600;
      font-size: 1rem;
      text-decoration: none;
      transition: background 0.2s;
      letter-spacing: 0.5px;
    }
    .card a:hover {
      background: #114077;
    }
    .admin-only {
      margin-top: auto;
      color: #fff;
      background: #b5bbc9;
      padding: 9px 22px;
      border-radius: 6px;
      font-weight: 600;
      font-size: 1rem;
      letter-spacing: 0.5px;
      cursor: not-allowed;
      text-decoration: none;
      pointer-events: none;
    }
    @media (max-width: 1000px) {
      .container { padding: 28px 3vw; gap: 20px; }
      .banner, .nav { padding: 16px 12px; }
    }
    @media (max-width: 700px) {
      .container { flex-direction: column; align-items: stretch; }
      .banner { flex-direction: column; gap: 20px; min-height: 72px;}
      .banner .welcome { font-size: 1.2rem;}
      .banner .logo { height: 32px; padding: 3px 8px;}
      .banner .logo img { height: 28px;}
    }
  </style>
</head>
<body>
  <div class="banner">
    <div class="welcome">Welcome, <?= htmlspecialchars($username) ?>!</div>
    <div class="logo">
      <img src="img/logo.png" alt="ABB Robotics Logo">
    </div>
  </div>
  <nav class="nav">
    <a href="dashboard.php" class="active">Dashboard</a>
    <a href="view_customers.php">Customers</a>
    <a href="view_leads.php">Leads</a>
    <a href="view_interactions.php">Interactions</a>
    <a href="logout.php" style="margin-left:auto;color:#d3342c;">Logout</a>
  </nav>
  <div class="container">
    <div class="card">
      <h2>Customers</h2>
      <p>View, add, or edit your customer records.</p>
      <a href="view_customers.php">Manage Customers</a>
    </div>
    <div class="card">
      <h2>Leads</h2>
      <p>Track, update, and follow up on sales leads.</p>
      <a href="view_leads.php">Manage Leads</a>
    </div>
    <div class="card">
      <h2>Interactions</h2>
      <p>Log and review customer interaction history.</p>
      <a href="view_interactions.php">Interaction History</a>
    </div>
    <?php if ($_SESSION['role'] === 'Admin'): ?>
      <div class="card">
        <h2>Export Data</h2>
        <p>Download your customer and lead data as CSV files.</p>
        <a href="export_customers.php">Export Customers</a>
      </div>
    <?php else: ?>
      <div class="card">
        <h2>Admin Only</h2>
        <p>This feature is available for Admins only.</p>
        <span class="admin-only">Admin Only</span>
      </div>
    <?php endif; ?>
  </div>
</body>
</html>
