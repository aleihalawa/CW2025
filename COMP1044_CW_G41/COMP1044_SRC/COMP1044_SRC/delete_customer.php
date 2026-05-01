<?php
session_start();
if (!isset($_SESSION['user_id']) || $_SESSION['role'] !== 'Admin') {
    echo "Access denied.";
    exit();
}
require 'db.php';

$id = null;
if ($_SERVER['REQUEST_METHOD'] === 'POST') {
    $id = $_POST['id'] ?? null;
} elseif ($_SERVER['REQUEST_METHOD'] === 'GET') {
    $id = $_GET['id'] ?? null;
}

if ($id) {
    $stmt = $conn->prepare("DELETE FROM customers WHERE id = ?");
    $stmt->bind_param("i", $id);

    if ($stmt->execute()) {
        header("Location: view_customers.php");
        exit();
    } else {
        echo "Error deleting customer: " . $stmt->error;
    }

    $stmt->close();
} else {
    echo "Invalid request: No customer ID provided.";
}
?>
