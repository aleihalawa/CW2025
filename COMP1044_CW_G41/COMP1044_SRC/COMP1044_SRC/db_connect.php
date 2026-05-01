<?php
$host = "localhost";       // Hostname
$username = "root";        // Default XAMPP username
$password = "";            // Default password is blank
$database = "crm_system";  // The database we imported earlier

// Create connection
$conn = new mysqli($host, $username, $password, $database);

// Check connection
if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}

// Optional: Uncomment this to confirm it's working
// echo "Database connected successfully!";
?>
