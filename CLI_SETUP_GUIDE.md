# LQRY CLI System - Setup & Run Guide

## 🎯 Prerequisites

Ensure you have:
- **Java JDK 8+** (Test with: `java -version`)
- **MySQL 5.7+** (Test with: `mysql --version`)
- **MySQL Server running** on `localhost:3306`

---

## 📋 Step 1: Database Setup

### 1.1 Create the Database and Load Schema

```bash
# Connect to MySQL
mysql -u root -p

# Enter password (default is empty, just press Enter)
```

Once in MySQL prompt:
```sql
-- Create database
CREATE DATABASE opac_system;

-- Use the database
USE opac_system;

-- Import the schema and data
SOURCE opac_system.sql;

-- Verify (check if tables exist)
SHOW TABLES;
```

**Expected output:**
```
+-----------------------+
| Tables_in_opac_system |
+-----------------------+
| books                 |
| borrowers             |
| dewey_classifications |
| transactions          |
+-----------------------+
```

### 1.2 Verify Initial Data

```sql
SELECT * FROM books;
SELECT * FROM borrowers;
SELECT * FROM transactions;
```

**Expected results:**
- 4 books (The Great Gatsby, Clean Code, Astronomy Today, To Kill a Mockingbird)
- 3 borrowers (John Doe, Jane Smith, Michael Johnson)
- 1 transaction (Book 4 borrowed by John Doe)

---

## 🔧 Step 2: Compile Java Files

Navigate to the project directory:

```bash
cd "C:\Users\Alessandra Dagdag\Downloads\DIPROGLANG (2)\DIPROGLANG"
```

### Option A: Compile All Files

```bash
javac -cp ".;mysql-connector-j-9.6.0.jar" *.java
```

**This compiles:**
- OPACSystem.java
- Database.java
- Book.java, BookDAO.java
- Borrower.java, BorrowerDAO.java
- Transaction.java, TransactionDAO.java
- DeweyDecimal.java
- OverdueCalculator.java
- DatabaseSetup.java

### Option B: Compile Individual Files (if needed)

```bash
javac -cp ".;mysql-connector-j-9.6.0.jar" Database.java
javac -cp ".;mysql-connector-j-9.6.0.jar" Book.java
javac -cp ".;mysql-connector-j-9.6.0.jar" BookDAO.java
# ... and so on
```

### Verify Compilation

Check for `.class` files:
```bash
dir *.class
```

You should see `.class` files for all Java files.

---

## 🚀 Step 3: Run the OPAC System

```bash
java -cp ".;mysql-connector-j-9.6.0.jar" OPACSystem
```

### Expected Output:

```
  ╔══════════════════════════════════════════╗
  ║       OPAC SYSTEM  - Library Manager     ║
  ╚══════════════════════════════════════════╝

  [DB] Connected to the database!

  ┌─────────────────────────────┐
  │         MAIN MENU           │
  ├─────────────────────────────┤
  │  1. Book Management         │
  │  2. Borrow / Return         │
  │  3. Search Books            │
  │  4. Overdue Report          │
  │  5. Borrower Management     │
  │  0. Exit                    │
  └─────────────────────────────┘
  
  Enter choice: 
```

---

## 📚 Step 4: Using the CLI Menu

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

### Error: "Failed to connect to the database!"

**Solution:** 
- Check MySQL is running: `mysql -u root -p`
- Verify database exists: `SHOW DATABASES;`
- Check username/password in `Database.java` (currently: user="root", password="")

### Error: "ClassNotFoundException: com.mysql.cj.jdbc.Driver"

**Solution:**
- Verify `mysql-connector-j-9.6.0.jar` is in the same directory
- Ensure classpath includes the JAR: `-cp ".;mysql-connector-j-9.6.0.jar"`

### Error: "Book not found" when trying to edit

**Solution:**
- View all books first: `1 → 1`
- Check the correct Book ID
- Remember: IDs start from 1, not 0

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

