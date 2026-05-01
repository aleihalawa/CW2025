
-- DROP EXISTING TABLES (clean import)
DROP TABLE IF EXISTS interaction_history;
DROP TABLE IF EXISTS leads;
DROP TABLE IF EXISTS customers;
DROP TABLE IF EXISTS users;

-- USERS TABLE
CREATE TABLE users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL,
    email VARCHAR(100) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    role ENUM('Admin', 'SalesRep') NOT NULL
);

-- CUSTOMERS TABLE
CREATE TABLE customers (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    company VARCHAR(100),
    email VARCHAR(100) NOT NULL,
    phone VARCHAR(20),
    address VARCHAR(255),
    created_by_user_id INT,
    FOREIGN KEY (created_by_user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- LEADS TABLE
CREATE TABLE leads (
    id INT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone VARCHAR(20),
    company VARCHAR(100),
    status ENUM('New', 'Contacted', 'In Progress', 'Closed') DEFAULT 'New',
    follow_up_date DATE,
    notes TEXT,
    user_id INT,
    FOREIGN KEY (user_id) REFERENCES users(id) ON DELETE SET NULL
);

-- INTERACTION HISTORY TABLE
CREATE TABLE interaction_history (
    interaction_id INT AUTO_INCREMENT PRIMARY KEY,
    customer_id INT,
    date DATE NOT NULL,
    type ENUM('Call', 'Email', 'Meeting', 'Other') NOT NULL,
    description TEXT,
    FOREIGN KEY (customer_id) REFERENCES customers(id) ON DELETE CASCADE
);

-- SAMPLE USERS
INSERT INTO users (username, email, password, role) VALUES
('adminuser', 'admin@abb.com', 'admin_hashed_pw', 'Admin'),
('salesrep1', 'rep1@abb.com', 'rep1_hashed_pw', 'SalesRep');

-- SAMPLE CUSTOMERS
INSERT INTO customers (name, company, email, phone, address, created_by_user_id) VALUES
('John Doe', 'FutureTech', 'john@futuretech.com', '0123456789', '123 Industrial Road', 2),
('Jane Smith', 'AutoMotion', 'jane@automotion.com', '0987654321', '456 Robotics Lane', 2);

-- SAMPLE LEADS
INSERT INTO leads (name, email, phone, company, status, follow_up_date, notes, user_id) VALUES
('Alice Lead', 'alice@example.com', '0111222333', 'QuantumSoft', 'In Progress', '2025-04-30', 'Very interested. Follow-up soon.', 2);

-- SAMPLE INTERACTIONS
INSERT INTO interaction_history (customer_id, date, type, description) VALUES
(1, '2025-04-20', 'Email', 'Discussed product specs'),
(2, '2025-04-22', 'Call', 'Requested quote');


-- Fixing users with plain text passwords
DELETE FROM users;

INSERT INTO users (username, email, password, role) VALUES
('adminuser', 'admin@abb.com', 'admin123', 'Admin'),
('salesrep1', 'rep1@abb.com', 'rep123', 'SalesRep');