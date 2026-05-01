<?php
$host = 'localhost';
$username = 'root';
$password = ''; // default for XAMPP is blank
$database = 'crm_system'; // change to your actual DB name if different

$conn = new mysqli($host, $username, $password, $database);

if ($conn->connect_error) {
    die("Connection failed: " . $conn->connect_error);
}
?>
