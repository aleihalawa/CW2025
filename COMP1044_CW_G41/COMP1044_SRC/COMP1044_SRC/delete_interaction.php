<?php
session_start();
if (!isset($_SESSION['user_id']) || $_SESSION['role'] !== 'Admin') {
    ?>
    <!DOCTYPE html>
    <html lang="en">
    <head>
        <meta charset="UTF-8">
        <title>Access Denied</title>
        <style>
            body { background: #f6f8fb; margin: 0; font-family: 'Nunito', sans-serif; }
            .modal-overlay {
                position: fixed; top: 0; left: 0; width: 100vw; height: 100vh;
                background: rgba(0,0,0,0.45); display: flex; justify-content: center; align-items: center; z-index: 9999;
            }
            .modal-content {
                background: #fff; border-radius: 16px; box-shadow: 0 4px 24px #0003;
                padding: 34px 40px 28px 40px; text-align: center; max-width: 360px; min-width: 240px;
            }
            .modal-content h2 {
                color: #d3342c; margin: 0 0 16px 0; font-size: 2rem;
            }
            .modal-content p {
                color: #444; font-size: 1.09rem; margin-bottom: 18px;
            }
            .close-btn {
                background: #2068b8; color: #fff; border: none; border-radius: 8px;
                padding: 9px 28px; font-size: 1rem; font-weight: 700; cursor: pointer;
                transition: background 0.15s;
            }
            .close-btn:hover {
                background: #114077;
            }
        </style>
    </head>
    <body>
        <div class="modal-overlay">
            <div class="modal-content">
                <h2>Access Denied</h2>
                <p>You do not have permission to perform this action.<br>
                   Please contact your administrator if you believe this is an error.</p>
                <button class="close-btn" onclick="window.history.back();">Close</button>
            </div>
        </div>
    </body>
    </html>
    <?php
    exit();
}
require 'db_connect.php';

$interaction_id = null;

// Allow both POST and GET for deletion
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $interaction_id = isset($_POST['id']) && is_numeric($_POST['id']) ? intval($_POST['id']) : null;
} elseif ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $interaction_id = isset($_GET['id']) && is_numeric($_GET['id']) ? intval($_GET['id']) : null;
}

if (!$interaction_id) {
    header("Location: view_interactions.php");
    exit();
}

$stmt = $conn->prepare("DELETE FROM interaction_history WHERE interaction_id = ?");
$stmt->bind_param("i", $interaction_id);
$stmt->execute();
$stmt->close();

header("Location: view_interactions.php");
exit();
?>
