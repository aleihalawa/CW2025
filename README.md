# ABB Robotics CRM System

A web-based Customer Relationship Management (CRM) system built as a university database coursework project. Designed around a fictional ABB Robotics sales team, it demonstrates full-stack PHP/MySQL development with role-based access control.

## Features

- **Authentication** — session-based login with role-based access (Admin / Sales Rep)
- **Customer Management** — create, view, edit, and delete customer records
- **Lead Tracking** — manage sales leads through a status pipeline: New → Contacted → In Progress → Closed
- **Interaction History** — log customer interactions by type (Call, Email, Meeting, Other)
- **CSV Export** — Admin-only export of customer and lead data
- **Responsive UI** — clean dashboard with card-based navigation

## Tech Stack

| Layer    | Technology          |
|----------|---------------------|
| Backend  | PHP (procedural)    |
| Database | MySQL               |
| Frontend | HTML, CSS           |
| Server   | Apache via XAMPP    |

## Database Schema

Four relational tables with foreign key constraints:

- `users` — stores credentials and roles
- `customers` — customer records linked to the user who created them
- `leads` — sales pipeline with follow-up dates and notes
- `interaction_history` — interaction log tied to customers

See [`COMP1044_CW_G41/COMP1044_Database.sql`](COMP1044_CW_G41/COMP1044_Database.sql) for the full schema and [`COMP1044_CW_G41/COMP1044_ERD.pdf`](COMP1044_CW_G41/COMP1044_ERD.pdf) for the Entity Relationship Diagram.

## Running Locally

**Prerequisites:** [XAMPP](https://www.apachefriends.org/) (Apache + MySQL)

1. Clone the repo into your XAMPP `htdocs` folder:
   ```bash
   git clone https://github.com/aleihalawa/CW2025.git
   ```

2. Start **Apache** and **MySQL** in the XAMPP Control Panel.

3. Open [phpMyAdmin](http://localhost/phpmyadmin), create a database named `crm_system`, and import:
   ```
   COMP1044_CW_G41/COMP1044_Database.sql
   ```

4. Navigate to the app in your browser:
   ```
   http://localhost/CW2025/COMP1044_CW_G41/COMP1044_SRC/COMP1044_SRC/login.php
   ```

5. Log in with the demo credentials:

   | Role     | Username    | Password  |
   |----------|-------------|-----------|
   | Admin    | adminuser   | admin123  |
   | Sales Rep| salesrep1   | rep123    |

## Project Files

```
COMP1044_CW_G41/
├── COMP1044_Database.sql       # Full DB schema + seed data
├── COMP1044_ERD.pdf            # Entity Relationship Diagram
├── COMP1044_WBS.pdf            # Work Breakdown Structure
└── COMP1044_SRC/
    └── login.php               # Entry point
    └── dashboard.php           # Main hub
    └── view/add/edit/delete_*.php  # CRUD pages per module
    └── export_*.php            # Admin CSV exports
    └── db_connect.php          # DB connection config
```

---

*COMP1044 Database & Interfaces Coursework — Group 41*
