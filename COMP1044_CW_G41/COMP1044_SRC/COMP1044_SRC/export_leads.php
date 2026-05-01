<?php
session_start();
if (!isset($_SESSION['user_id']) || $_SESSION['role'] !== 'Admin') {
    echo "Access denied.";
    exit();
}

require 'db.php';

// Set headers to download the file
header('Content-Type: text/csv');
header('Content-Disposition: attachment; filename="leads.csv"');

// Open output stream
$output = fopen("php://output", "w");

// Write the column headers
fputcsv($output, ['ID', 'Name', 'Email', 'Phone', 'Status', 'Follow-up Date', 'Notes', 'Created']);

// Fetch data
$result = $conn->query("SELECT id, name, email, phone, status, follow_up_date, notes, created_at FROM leads");

while ($row = $result->fetch_assoc()) {
    fputcsv($output, $row);
}

fclose($output);
exit();
?>
