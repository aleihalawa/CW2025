<?php
session_start();
if (!isset($_SESSION['user_id'])) {
    header("Location: login.php");
    exit();
}
if (!isset($_SESSION['reminder_shown'])) {
    $_SESSION['reminder_shown'] = false;
}
require 'db_connect.php';

$search = "";
if (isset($_GET['search'])) {
    $search = trim($_GET['search']);
    $stmt = $conn->prepare(
        "SELECT * FROM leads WHERE name LIKE ? OR email LIKE ? OR phone LIKE ? OR company LIKE ? OR status LIKE ?"
    );
    $like = "%$search%";
    $stmt->bind_param("sssss", $like, $like, $like, $like, $like);
    $stmt->execute();
    $result = $stmt->get_result();
    $stmt->close();
} else {
    $result = $conn->query("SELECT * FROM leads ORDER BY name ASC");
}
$username = isset($_SESSION['user_name']) ? $_SESSION['user_name'] : 'User';

$reminderLeads = [];
$today = date('Y-m-d');
$stmt2 = $conn->prepare("SELECT name, follow_up_date FROM leads WHERE follow_up_date IS NOT NULL AND follow_up_date <= ?");
$stmt2->bind_param("s", $today);
$stmt2->execute();
$reminderResult = $stmt2->get_result();
while ($row = $reminderResult->fetch_assoc()) {
    $reminderLeads[] = $row;
}
$stmt2->close();
?>
<!DOCTYPE html>
<html lang="en">
<head>
    <title>View Leads - ABB Robotics CRM</title>
    <meta charset="UTF-8" />
    <meta name="viewport" content="width=device-width, initial-scale=1.0"/>
    <link href="https://fonts.googleapis.com/css?family=Nunito:400,600,700" rel="stylesheet">
    <script src="https://cdn.jsdelivr.net/npm/sweetalert2@11"></script>
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
            max-width: 960px;
            margin: 42px auto;
            background: #fff;
            border-radius: 16px;
            box-shadow: 0 6px 28px rgba(32,104,184,0.09);
            padding: 36px 36px 28px 36px;
        }
        .add-btn {
            background: #2068b8;
            color: #fff;
            font-weight: 700;
            font-size: 1.08rem;
            padding: 10px 32px;
            border: none;
            border-radius: 7px;
            margin-bottom: 18px;
            margin-top: 2px;
            text-decoration: none;
            display: inline-block;
            transition: background 0.16s;
            box-shadow: 0 3px 10px rgba(32,104,184,0.10);
        }
        .add-btn:hover {
            background: #114077;
            color: #fff;
        }
        h2 {
            color: #2068b8;
            font-size: 1.6rem;
            margin-top: 0;
            font-weight: 800;
            letter-spacing: 1px;
            text-align: center;
        }
        form {
            margin-bottom: 18px;
            display: flex;
            align-items: center;
            gap: 12px;
            justify-content: center;
        }
        input[type="text"] {
            padding: 9px 13px;
            border: 1.4px solid #bdd6f6;
            border-radius: 6px;
            font-size: 1.05rem;
            background: #fafdff;
            color: #26375a;
            font-family: 'Nunito', sans-serif;
            transition: border 0.18s;
            width: 260px;
        }
        input[type="text"]:focus {
            border: 1.7px solid #2068b8;
            background: #f0f6fd;
            outline: none;
        }
        button, a.btn-reset {
            background: #2068b8;
            color: #fff;
            border: none;
            padding: 9px 22px;
            border-radius: 6px;
            font-weight: 600;
            font-size: 1rem;
            cursor: pointer;
            text-decoration: none;
            transition: background 0.2s;
            letter-spacing: 0.5px;
        }
        button:hover, a.btn-reset:hover {
            background: #114077;
            color: #fff;
        }
        a.btn-reset {
            background: #f5faff;
            color: #2068b8;
            border: 1.2px solid #bdd6f6;
            margin-left: 2px;
        }
        a.btn-reset:hover {
            background: #d6ecfd;
        }
        table {
            border-collapse: collapse;
            width: 100%;
            margin-top: 18px;
        }
        th, td {
            border: 1.3px solid #dbeafe;
            padding: 12px 10px;
            text-align: left;
        }
        th {
            background: #e8f0fa;
            color: #2068b8;
            font-weight: 800;
            font-size: 1.07rem;
            letter-spacing: 0.6px;
        }
        tr:nth-child(even) { background: #f6fafd; }
        tr:hover { background: #f0f6fd; }
        td a {
            color: #2068b8;
            text-decoration: none;
            font-weight: 700;
            margin-right: 8px;
        }
        td a:hover {
            text-decoration: underline;
            color: #0c3266;
        }
        @media (max-width: 900px) {
            .container { padding: 14px 3vw; }
            th, td { padding: 7px 6px; }
            h2 { font-size: 1.1rem; }
        }
    </style>
</head>
<body>
  <?php
  if (!empty($reminderLeads) && $_SESSION['reminder_shown'] === false): ?>
  <script>
    document.addEventListener('DOMContentLoaded', function() {
      let reminders = <?php echo json_encode($reminderLeads); ?>;
      let html = '<div style="text-align:left;">You have lead(s) needing follow-up:<ul style="padding-left: 24px;">';
      reminders.forEach(function(lead) {
          html += '<li><b>' + lead.name + '</b> &ndash; Follow-up date: <b>' + lead.follow_up_date + '</b></li>';
      });
      html += '</ul></div>';
      Swal.fire({
          icon: 'info',
          title: 'Follow-Up Reminder',
          html: html,
          confirmButtonColor: '#2068b8',
          background: '#f4f8fc',
          color: '#26375a',
          customClass: {
              popup: 'swal2-popup-leads'
          }
      });
    });
  </script>
  <?php
  $_SESSION['reminder_shown'] = true;
  endif;
  ?>
  <div class="banner">
    <div class="welcome">Lead List</div>
    <div class="logo">
      <img src="img/logo.png" alt="ABB Robotics Logo">
    </div>
  </div>
  <nav class="nav">
    <a href="dashboard.php">Dashboard</a>
    <a href="view_customers.php">Customers</a>
    <a href="view_leads.php" class="active">Leads</a>
    <a href="view_interactions.php">Interactions</a>
    <a href="logout.php" style="margin-left:auto;color:#d3342c;">Logout</a>
  </nav>
  <div class="container">
    <h2>Lead List</h2>
    <a href="add_lead.php" class="add-btn">+ Add Lead</a>
    <form method="GET">
        <input type="text" name="search" placeholder="Search by name, email, phone, company or status" value="<?= htmlspecialchars($search) ?>">
        <button type="submit">Search</button>
        <a href="view_leads.php" class="btn-reset">Reset</a>
    </form>
    <table>
        <tr>
            <th>Name</th>
            <th>Email</th>
            <th>Phone</th>
            <th>Company</th>
            <th>Status</th>
            <th>Actions</th>
        </tr>
        <?php while ($row = $result->fetch_assoc()): ?>
            <tr>
                <td><?= htmlspecialchars($row['name']) ?></td>
                <td><?= htmlspecialchars($row['email']) ?></td>
                <td><?= htmlspecialchars($row['phone']) ?></td>
                <td><?= htmlspecialchars($row['company']) ?></td>
                <td><?= htmlspecialchars($row['status']) ?></td>
                <td>
                    <a href="edit_lead.php?id=<?= $row['id'] ?>">Edit</a>
                    <?php if (isset($_SESSION['role']) && $_SESSION['role'] === 'Admin'): ?>
                        <a href="delete_lead.php?id=<?= $row['id'] ?>" onclick="return confirm('Are you sure?')">Delete</a>
                    <?php endif; ?>
                </td>
            </tr>
        <?php endwhile; ?>
    </table>
  </div>
</body>
</html>
