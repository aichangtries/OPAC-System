# LQRY - OPAC System | CLI Setup & Run Guide

**Project Name:** LQRY (Library Query System)  
**System:** Online Public Access Catalog (OPAC)  
**Version:** 1.0  
**Date:** March 27, 2026

---

## 🎯 Prerequisites

Ensure you have the following installed on your system:

- **Java JDK 8 or higher** 
  - Verify installation: `java -version`
  - Download: https://www.oracle.com/java/technologies/downloads/

- **MySQL Server 5.7 or higher**
  - Verify installation: `mysql --version`
  - Download: https://dev.mysql.com/downloads/mysql/
  - **Ensure MySQL Server is running** (listening on `localhost:3306`)

- **MySQL JDBC Driver** (included in project)
  - File: `mysql-connector-j-9.6.0.jar`

---

## 📋 Step 1: Prepare Project Environment

### 1.1 Extract Project Files

Extract all project files to your desired location:
```
<PROJECT_DIRECTORY>/
├── OPACSystem.java
├── APIServer.java
├── Database.java
├── Book.java, BookDAO.java
├── Borrower.java, BorrowerDAO.java
├── Transaction.java, TransactionDAO.java
├── Payment.java, PaymentDAO.java
├── OverdueCalculator.java
├── DeweyDecimal.java
├── opac_system.sql
├── index.html (frontend)
└── mysql-connector-j-9.6.0.jar
```

**Replace `<PROJECT_DIRECTORY>` with your actual path.**

- **Windows example:** `C:\Users\YourName\Projects\OPAC-System`
- **macOS/Linux example:** `/Users/YourName/Projects/OPAC-System`

### 1.2 Verify JDBC Driver

Ensure `mysql-connector-j-9.6.0.jar` is present in `<PROJECT_DIRECTORY>`:  
```bash
ls <PROJECT_DIRECTORY>/mysql-connector-j-9.6.0.jar
```

---

## 📋 Step 2: Database Setup

### 2.1 Create the Database and Load Schema

**Connect to MySQL:**
```bash
mysql -u <DB_USERNAME> -p
```

**Enter your MySQL password** (default is often empty for local installations)

**At the MySQL prompt, execute:**
```sql
-- Create the OPAC database
CREATE DATABASE opac_system;

-- Switch to the database
USE opac_system;

-- Load the schema and initial data
SOURCE <PROJECT_DIRECTORY>/opac_system.sql;

-- Verify tables were created
SHOW TABLES;
```

**Placeholders:**
- `<DB_USERNAME>` = Your MySQL username (default: `root`)
- `<PROJECT_DIRECTORY>` = Full path to your project folder

**Expected output:**
```
+-----------------------+
| Tables_in_opac_system |
+-----------------------+
| books                 |
| borrowers             |
| dewey_classifications |
| payments              |
| transactions          |
+-----------------------+
```

### 2.2 Verify Initial Data

```sql
SELECT COUNT(*) FROM books;
SELECT COUNT(*) FROM borrowers;
SELECT COUNT(*) FROM transactions;
```

**Expected results:**
- 4 books in database
- 3 borrowers in database
- Sample transaction records

---

## 🔧 Step 3: Configure Database Connection (if needed)

If your MySQL is running on a **different host, port, or credentials**, edit `Database.java`:

```java
// Find this section in Database.java:
private static final String URL = "jdbc:mysql://localhost:3306/opac_system";
private static final String USER = "root";      // Change if needed
private static final String PASSWORD = "";     // Enter your password
```

**Placeholders to update:**
- `localhost` → Your MySQL host (default: localhost)
- `3306` → Your MySQL port (default: 3306)
- `root` → Your MySQL username
- `""` → Your MySQL password

After changes, recompile: `javac -cp ".;mysql-connector-j-9.6.0.jar" Database.java`

---

## 🔧 Step 4: Compile Java Files

Navigate to your project directory:

```bash
cd <PROJECT_DIRECTORY>
```

### Option A: Compile All Files (Recommended)

```bash
javac -cp ".;mysql-connector-j-9.6.0.jar" *.java
```

**On macOS/Linux, use `:` instead of `;`:**
```bash
javac -cp ".:mysql-connector-j-9.6.0.jar" *.java
```

### Option B: Compile Individual Files (if needed)

```bash
javac -cp ".;mysql-connector-j-9.6.0.jar" Database.java
javac -cp ".;mysql-connector-j-9.6.0.jar" Book.java BookDAO.java
```

### Verify Compilation

Check for `.class` files:
```bash
ls *.class
```

You should see `.class` files for all compiled Java files.

---

## 🚀 Step 5: Run the OPAC CLI System

```bash
java -cp ".;mysql-connector-j-9.6.0.jar" OPACSystem
```

**On macOS/Linux, use `:` instead of `;`:**
```bash
java -cp ".:mysql-connector-j-9.6.0.jar" OPACSystem
```

### Expected Output

```
  ╔══════════════════════════════════════════╗
  ║       OPAC SYSTEM  - Library Manager     ║
  ╚══════════════════════════════════════════╝

  [DB] Connected to database successfully!

  ┌─────────────────────────────┐
  │      ADMINISTRATOR MENU      │
  ├─────────────────────────────┤
  │  1. Book Management         │
  │  2. Borrow / Return         │
  │  3. Search Books            │
  │  4. Overdue Report          │
  │  5. Borrower Management     │
  │  0. Exit                    │
  └─────────────────────────────┘
  
  Enter choice: _
```

If you see this menu, **the CLI is running successfully!**

---
```bash
dir *.class
```

You should see `.class` files for all Java files.

---

## � Step 6: Using the OPAC CLI

### Main Menu Options

### Main Menu Options

| Option | Function |
|--------|----------|
| **1** | Book Management (View, Add, Edit, Delete) |
| **2** | Borrow / Return Books |
| **3** | Search Books by Title, Author, or Category |
| **4** | View Overdue Books & Calculate Fees |
| **5** | Borrower Management (View, Add, Edit, Delete) |
| **0** | Exit the system |

---

## 🔄 Example: Adding a Book via CLI

```
Main Menu -> 1 (Book Management)
            1 (View All Books)             [see current books]
            2 (Add Book)
            
  ── ADD NEW BOOK ──
  Title    : Harry Potter and the Sorcerer's Stone
  Author   : J.K. Rowling
  Category : Literature
  
  ✔ Book added successfully.
  (Dewey Decimal automatically assigned based on category)
```

---

## 📝 Example: Borrowing a Book

```
Main Menu -> 2 (Borrow / Return)
            1 (Borrow a Book)
            
  ── BORROW A BOOK ──
  Book ID        : 1
  Borrower ID    : 1
  Loan period (days): 14
  
  Borrow Date : 2026-03-23
  Due Date    : 2026-04-06
  
  ✔ Book borrowed successfully.
    "The Great Gatsby" borrowed by John Doe. Due: 2026-04-06
```

---

## 🌐 Website Auto-Sync

When you make changes in the CLI (add/edit/delete books), the **website will automatically display them** within 3 seconds because:

1. ✅ Database is updated via CLI
2. ✅ Java backend API reads from database
3. ✅ Website fetches from API every 3 seconds
4. ✅ Display updates automatically

---

## 🛠️ Troubleshooting

### ❌ Error: "Failed to connect to the database!"

**Causes:**
- MySQL Server is not running
- Database doesn't exist
- Wrong username/password credentials

**Solutions:**
1. **Start MySQL Server:**
   - Windows: Open "Services" and start MySQL
   - macOS: `brew services start mysql`
   - Linux: `sudo systemctl start mysql`

2. **Verify database exists:**
   ```bash
   mysql -u <DB_USERNAME> -p
   SHOW DATABASES;
   ```

3. **Check Database.java configuration** (see Step 3)

---

### ❌ Error: "ClassNotFoundException: com.mysql.cj.jdbc.Driver"

**Causes:**
- `mysql-connector-j-9.6.0.jar` is missing or in wrong location
- Classpath doesn't include the JAR file

**Solutions:**
1. **Verify JAR exists:**
   ```bash
   ls <PROJECT_DIRECTORY>/mysql-connector-j-9.6.0.jar
   ```

2. **Ensure correct compilation command:**
   - Windows: `javac -cp ".;mysql-connector-j-9.6.0.jar" *.java`
   - macOS/Linux: `javac -cp ".:mysql-connector-j-9.6.0.jar" *.java`

---

### ❌ Error: "Port 5000 is already in use" (When running API Server)

**Solution:** The API server is already running or another program uses port 5000
- Kill the existing process or use a different port in APIServer.java:
  ```java
  server.bind(new InetSocketAddress(5001), 0); // Change 5001 to any available port
  ```

---

### ❌ Error: "Book not found" when trying to edit

**Causes:**
- Book ID doesn't exist
- Wrong ID format

**Solutions:**
1. View all books first to get valid IDs
2. Book IDs start from 1, not 0
3. Check database: `SELECT book_id, title FROM books;`

---

### ❌ Changes in CLI don't appear on website

**Causes:**
- API Server not running
- Frontend not refreshing
- Incorrect API URL in HTML

**Solutions:**
1. **Start API Server:**
   ```bash
   java -cp ".;mysql-connector-j-9.6.0.jar" APIServer
   ```

2. **Verify API is working:**
   ```bash
   curl http://localhost:5000/api/books
   ```

3. **Check API URL in HTML** (index.html, line 1241):
   ```javascript
   const API_BASE_URL = 'http://localhost:5000/api';
   ```

---

## 📊 Database Schema

```sql
-- Books Table
CREATE TABLE books (
  book_id INT PRIMARY KEY AUTO_INCREMENT,
  title VARCHAR(255),
  author VARCHAR(255),
  category VARCHAR(100),
  dewey_decimal VARCHAR(50),
  is_available TINYINT(1) DEFAULT 1
);

-- Borrowers Table
CREATE TABLE borrowers (
  borrower_id INT PRIMARY KEY AUTO_INCREMENT,
  name VARCHAR(255)
);

-- Transactions Table
CREATE TABLE transactions (
  transaction_id INT PRIMARY KEY AUTO_INCREMENT,
  book_id INT,
  borrower_id INT,
  borrow_date DATE,
  due_date DATE,
  return_date DATE,
  overdue_fee DECIMAL(10,2),
  FOREIGN KEY (book_id) REFERENCES books(book_id),
  FOREIGN KEY (borrower_id) REFERENCES borrowers(borrower_id)
);

-- Dewey Classifications Table
CREATE TABLE dewey_classifications (
  classification_id INT PRIMARY KEY AUTO_INCREMENT,
  code VARCHAR(20) UNIQUE,
  description VARCHAR(255),
  keywords VARCHAR(500)
);
```

---

## 🎯 Quick Start Script

Save as `run.bat` in your project directory:

```batch
@echo off
echo Compiling Java files...
javac -cp ".;mysql-connector-j-9.6.0.jar" *.java

echo.
echo Starting OPAC System...
java -cp ".;mysql-connector-j-9.6.0.jar" OPACSystem

pause
```

Then just double-click `run.bat` to start!

---

## 📱 Full Workflow

1. **CLI Changes** → Database Updated
   ```
   CLI: Add book "1984" by George Orwell
   ↓
   MySQL: INSERT INTO books (...)
   ```

2. **Website Syncs** → Auto-updates in 3 seconds
   ```
   Website polls: GET /api/books
   ↓
   Backend returns: Updated book list
   ↓
   Display: "1984" appears on website
   ```

3. **Multiple Users**
   ```
   Person A: Uses CLI to add/edit books
   Person B: Views website (auto-refreshes)
   ↓
   Both see the same data immediately!
   ```

---

## ✅ Verification Checklist

- [ ] MySQL running and accessible
- [ ] `opac_system` database created with schema
- [ ] Sample data inserted (4 books, 3 borrowers)
- [ ] All `.java` files compile to `.class` files
- [ ] `OPACSystem` runs with menu displayed
- [ ] CLI operations update the database
- [ ] Website fetches and displays the same data
- [ ] Auto-refresh works (3-second sync)

Once all ✓, you're ready to use the system!

