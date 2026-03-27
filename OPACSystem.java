import java.sql.Connection;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

/**
 * ============================================================
 *   OPAC SYSTEM – Online Public Access Catalog
 *   Features: Books CRUD | Borrow & Return | Overdue Fees | Search
 * ============================================================
 */
public class OPACSystem {

    static Scanner sc = new Scanner(System.in);
    static Connection     conn;
    static BookDAO        bookDAO;
    static BorrowerDAO    borrowerDAO;
    static TransactionDAO transactionDAO;
    static PaymentDAO     paymentDAO;

    public static void main(String[] args) {

        conn = Database.connect();

        if (conn == null ) {
            System.out.println(" [ERROR] Database connection is not connected!");
            System.out.println(" Please check your database server or configuration. Running in Limited mode.");
    
        }
        bookDAO        = new BookDAO(conn);
        borrowerDAO    = new BorrowerDAO(conn);
        transactionDAO = new TransactionDAO(conn);
        paymentDAO     = new PaymentDAO(conn);

        System.out.println();
        System.out.println("  ╔══════════════════════════════════════════╗");
        System.out.println("  ║       OPAC SYSTEM  - Library Manager     ║");
        System.out.println("  ╚══════════════════════════════════════════╝");

        boolean running = true;
        while (running) {
            printRoleMenu();
            int choice = readInt("  Enter choice: ");

            switch (choice) {
                case 1 -> runAdministratorPortal();
                case 2 -> runMemberPortal();
                case 0 -> {
                    System.out.println("\n  Goodbye! Closing OPAC System...");
                    Database.disconnect();
                    running = false;
                }
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ══════════════════════════════════════════
    //  ROLE MENU / ADMIN MENU
    // ══════════════════════════════════════════
    static void printRoleMenu() {
        System.out.println();
        System.out.println("  ┌─────────────────────────────┐");
        System.out.println("  │        USER PORTAL          │");
        System.out.println("  ├─────────────────────────────┤");
        System.out.println("  │  1. Administrator           │");
        System.out.println("  │  2. Member                  │");
        System.out.println("  │  0. Exit                    │");
        System.out.println("  └─────────────────────────────┘");
    }

    static void printMainMenu() {
        System.out.println();
        System.out.println("  ┌─────────────────────────────┐");
        System.out.println("  │     ADMINISTRATOR MENU      │");
        System.out.println("  ├─────────────────────────────┤");
        System.out.println("  │  1. Book Management         │");
        System.out.println("  │  2. Borrow / Return         │");
        System.out.println("  │  3. Search Books            │");
        System.out.println("  │  4. Overdue Report          │");
        System.out.println("  │  5. Borrower Management     │");
        System.out.println("  │  0. Back                    │");
        System.out.println("  └─────────────────────────────┘");
    }

    static void runAdministratorPortal() {
        boolean inAdmin = true;
        while (inAdmin) {
            printMainMenu();
            int choice = readInt("  Enter choice: ");

            switch (choice) {
                case 1 -> bookMenu();
                case 2 -> borrowReturnMenu();
                case 3 -> searchMenu();
                case 4 -> overdueMenu();
                case 5 -> borrowerMenu();
                case 0 -> inAdmin = false;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    // ══════════════════════════════════════════
    //  BOOK MANAGEMENT (CRUD)
    // ══════════════════════════════════════════
    static void bookMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("  ┌─────────────────────────────┐");
            System.out.println("  │       BOOK MANAGEMENT       │");
            System.out.println("  ├─────────────────────────────┤");
            System.out.println("  │  1. View All Books          │");
            System.out.println("  │  2. Add Book                │");
            System.out.println("  │  3. Edit Book               │");
            System.out.println("  │  4. Delete Book             │");
            System.out.println("  │  0. Back                    │");
            System.out.println("  └─────────────────────────────┘");

            int choice = readInt("  Choice: ");
            switch (choice) {
                case 1 -> viewAllBooks();
                case 2 -> addBook();
                case 3 -> editBook();
                case 4 -> deleteBook();
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void viewAllBooks() {
        List<Book> books = bookDAO.getAllBooks();
        if (books.isEmpty()) {
            System.out.println("  No books found.");
            return;
        }
        printBookHeader();
        books.forEach(b -> System.out.println("  " + b));
        System.out.println("  " + "─".repeat(119));
        System.out.println("  Total: " + books.size() + " book(s).");
    }

    static void addBook() {
        System.out.println("\n  ── ADD NEW BOOK ──");
        String title  = readString("  Title    : ");
        String author = readString("  Author   : ");
        String cat    = readString("  Category : ");

        if (bookDAO.addBook(title, author, cat)) {
            System.out.println("  ( ദ്ദി ˙ᗜ˙ ) Book added successfully.");
            System.out.println("  (Dewey Decimal automatically assigned based on category)");
        } else {
            System.out.println("  ✘ Failed to add book.");
        }
    }

    static void editBook() {
        System.out.println("\n  ── EDIT BOOK ──");
        int id = readInt("  Enter Book ID to edit: ");
        Book book = bookDAO.getBookById(id);
        if (book == null) {
            System.out.println("  [!] Book not found.");
            return;
        }

        System.out.println("  Current details: " + book);
        System.out.println("  (Press Enter to keep existing value)");

        String title  = readStringOptional("  New Title    [" + book.getTitle()  + "]: ", book.getTitle());
        String author = readStringOptional("  New Author   [" + book.getAuthor() + "]: ", book.getAuthor());
        String cat    = readStringOptional("  New Category [" + book.getCategory() + "]: ", book.getCategory());

        if (bookDAO.updateBook(id, title, author, cat)) {
            System.out.println("  ✔ Book updated successfully.");
            System.out.println("  (Dewey Decimal automatically updated based on category)");
        } else {
            System.out.println("  ✘ Failed to update book.");
        }
    }

    static void deleteBook() {
        System.out.println("\n  ── DELETE BOOK ──");
        int id = readInt("  Enter Book ID to delete: ");
        Book book = bookDAO.getBookById(id);
        if (book == null) {
            System.out.println("  [!] Book not found.");
            return;
        }

        System.out.println("  Book: " + book.getTitle() + " by " + book.getAuthor());
        String confirm = readString("  Confirm delete? (yes/no): ");
        if (confirm.equalsIgnoreCase("yes")) {
            if (bookDAO.deleteBook(id)) {
                System.out.println("  ✔ Book deleted.");
            } else {
                System.out.println("  ✘ Failed to delete. Book may have active transactions.");
            }
        } else {
            System.out.println("  Delete cancelled.");
        }
    }

    // ══════════════════════════════════════════
    //  BORROW & RETURN
    // ══════════════════════════════════════════
    static void borrowReturnMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("  ┌─────────────────────────────┐");
            System.out.println("  │       BORROW / RETURN       │");
            System.out.println("  ├─────────────────────────────┤");
            System.out.println("  │  1. Borrow a Book           │");
            System.out.println("  │  2. Return a Book           │");
            System.out.println("  │  3. View All Active Borrows │");
            System.out.println("  │  4. View All Transactions   │");
            System.out.println("  │  0. Back                    │");
            System.out.println("  └─────────────────────────────┘");

            int choice = readInt("  Choice: ");
            switch (choice) {
                case 1 -> borrowBook();
                case 2 -> returnBook();
                case 3 -> viewActiveBorrows();
                case 4 -> viewAllTransactions();
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void borrowBook() {
        System.out.println("\n  ── BORROW A BOOK ──");
        int bookId = readInt("  Book ID        : ");
        Book book = bookDAO.getBookById(bookId);

        if (book == null) {
            System.out.println("  [!] Book not found.");
            return;
        }
        if (!book.isAvailable()) {
            System.out.println("  [!] Book is currently borrowed. Not available.");
            return;
        }

        int borrowerId = readInt("  Borrower ID    : ");
        Borrower borrower = borrowerDAO.getBorrowerById(borrowerId);
        if (borrower == null) {
            System.out.println("  [!] Borrower not found. Please register the borrower first.");
            return;
        }

        int days = readInt("  Loan period (days): ");

        LocalDate dueDate = LocalDate.now().plusDays(days);
        System.out.println("  Borrow Date : " + LocalDate.now());
        System.out.println("  Due Date    : " + dueDate);

        if (transactionDAO.borrowBook(bookId, borrowerId, days)) {
            bookDAO.setAvailability(bookId, false);
            System.out.println("  ✔ Book borrowed successfully.");
            System.out.printf("    \"%s\" borrowed by %s. Due: %s%n",
                    book.getTitle(), borrower.getName(), dueDate);
        } else {
            System.out.println("  ✘ Failed to borrow book.");
        }
    }

    static void returnBook() {
        System.out.println("\n  ── RETURN A BOOK ──");

        //To check if there is something to return
        List<Transaction> activeList = transactionDAO.getActiveBorrows();
        if (activeList.isEmpty()) {
            //this will go back to menu
            System.out.println("  [!] There are no active borrows at the moment.");
            return; 
        }

        // Show active borrows for reference
        viewActiveBorrows();

        int txId = readInt("\n  Enter Transaction ID to return: ");
        Transaction tx = transactionDAO.getTransactionById(txId);

        if (tx == null) {
            System.out.println("  [!] Transaction not found.");
            return;
        }
        if (tx.getReturnDate() != null) {
            System.out.println("  [!] This book was already returned on " + tx.getReturnDate());
            return;
        }

        // Ask for return date
        LocalDate returnDate = LocalDate.now();
        System.out.println("\n  Use today's date? (Press Enter for today, or enter date as YYYY-MM-DD): ");
        String dateInput = readString("  Return Date: ").trim();
        
        if (!dateInput.isEmpty()) {
            try {
                returnDate = LocalDate.parse(dateInput);
            } catch (Exception e) {
                System.out.println("  [!] Invalid date format. Using today's date.");
                returnDate = LocalDate.now();
            }
        }

        // Show overdue preview
        System.out.println("\n  ── Return Summary ────────────────────");
        System.out.println("  Due Date    : " + tx.getDueDate());
        System.out.println("  Return Date : " + returnDate);
        System.out.println(OverdueCalculator.getOverdueSummary(tx.getDueDate(), returnDate));
        System.out.println("  ─────────────────────────────────────────");

        double fee = transactionDAO.returnBookWithDate(txId, returnDate);
        if (fee >= 0) {
            // Mark book as available again
            bookDAO.setAvailability(tx.getBookId(), true);
            System.out.printf("  ✔ Book returned on %s. Overdue Fee: PHP %.2f%n", returnDate, fee);
        } else {
            System.out.println("  ✘ Return failed.");
        }
    }

    static void viewActiveBorrows() {
        List<Transaction> list = transactionDAO.getActiveBorrows();
        if (list.isEmpty()) {
            System.out.println("  No active borrows.");
            return;
        }
        System.out.println();
        System.out.printf("  %-6s %-20s %-12s %-12s %-12s %-12s %-12s%n",
                "TX ID", "Book ID", "Borrower", "Borrow Date", "Due Date", "Days Left", "Status");
        System.out.println("  " + "─".repeat(92));
        
        for (Transaction t : list) {
            LocalDate today = LocalDate.now();
            long daysLeft = ChronoUnit.DAYS.between(today, t.getDueDate());
            String status = daysLeft < 0 
                ? String.format("OVERDUE (%d days)", Math.abs(daysLeft))
                : daysLeft <= 3
                ? String.format("DUE SOON (%d days)", daysLeft)
                : "Active";
            
            System.out.printf("  %-6d %-20d %-12d %-12s %-12s %-12d %-12s%n",
                    t.getTransactionId(), t.getBookId(), t.getBorrowerId(),
                    t.getBorrowDate(), t.getDueDate(), Math.abs(daysLeft), status);
        }
        System.out.println("  " + "─".repeat(92));
        System.out.println("  Total: " + list.size() + " active loan(s).");
    }

    static void viewAllTransactions() {
        List<Transaction> list = transactionDAO.getAllTransactions();
        if (list.isEmpty()) {
            System.out.println("  No transactions found.");
            return;
        }
        printTransactionHeader();
        list.forEach(t -> System.out.println("  " + t));
        printLine();
        System.out.println("  Total: " + list.size() + " transaction(s).");
    }

    // ══════════════════════════════════════════
    //  SEARCH MENU
    // ══════════════════════════════════════════
    static void searchMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("  ┌─────────────────────────────┐");
            System.out.println("  │        SEARCH BOOKS         │");
            System.out.println("  ├─────────────────────────────┤");
            System.out.println("  │  1. Search by Title         │");
            System.out.println("  │  2. Search by Author        │");
            System.out.println("  │  3. Search by Category      │");
            System.out.println("  │  4. Search by Dewey Decimal │");
            System.out.println("  │  0. Back                    │");
            System.out.println("  └─────────────────────────────┘");

            int choice = readInt("  Choice: ");
            switch (choice) {
                case 1 -> {
                    String kw = readString("  Enter title keyword: ");
                    displaySearchResults(bookDAO.searchByTitle(kw));
                }
                case 2 -> {
                    String kw = readString("  Enter author keyword: ");
                    displaySearchResults(bookDAO.searchByAuthor(kw));
                }
                case 3 -> {
                    String kw = readString("  Enter category keyword: ");
                    displaySearchResults(bookDAO.searchByCategory(kw));
                }
                case 4 -> {
                    String kw = readString("  Enter Dewey Decimal number: ");
                    displaySearchResults(bookDAO.searchByDeweyDecimal(kw));
                }
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void displaySearchResults(List<Book> results) {
        if (results.isEmpty()) {
            System.out.println("  No matching books found.");
            return;
        }
        printBookHeader();
        results.forEach(b -> System.out.println("  " + b));
        System.out.println("  " + "─".repeat(119));
        System.out.println("  Found: " + results.size() + " result(s).");
    }

    // ══════════════════════════════════════════
    //  OVERDUE REPORT
    // ══════════════════════════════════════════
    static void overdueMenu() {
        System.out.println("\n  ── OVERDUE REPORT ────────────────────");
        System.out.println("  Today: " + LocalDate.now());
        System.out.println("  Fee Rate: PHP " + OverdueCalculator.getFeePerDay() + " per day");
        System.out.println();

        // Get currently overdue (not yet returned)
        List<Transaction> overdueList = transactionDAO.getOverdueTransactions();
        Map<Integer, Double> borrowerPayments = (paymentDAO != null)
            ? paymentDAO.getPaymentsByBorrower()
            : Collections.emptyMap();
        Map<Integer, Double> remainingPayments = new HashMap<>(borrowerPayments);

        if (overdueList.isEmpty()) {
            System.out.println("  ✔ No overdue books today!");
            return;
        }

        System.out.printf("  %-6s %-6s %-12s %-12s %-10s %-14s %-14s%n",
            "TX ID", "Book", "Borrower ID", "Due Date", "Days Over", "Fee (PHP)", "Outstanding");
        System.out.println("  " + "─".repeat(94));

        double grossFees = 0;
        double outstandingFees = 0;
        for (Transaction t : overdueList) {
            long daysOver = OverdueCalculator.getDaysOverdue(t.getDueDate(), null);
            double fee    = OverdueCalculator.calculateFee(t.getDueDate(), null);
            double available = remainingPayments.getOrDefault(t.getBorrowerId(), 0.0);
            if (available < 0) available = 0;
            double applied = Math.min(fee, available);
            double outstanding = fee - applied;
            remainingPayments.put(t.getBorrowerId(), available - applied);
            grossFees += fee;
            outstandingFees += outstanding;

                System.out.printf("  %-6d %-6d %-12d %-12s %-10d PHP %-10.2f PHP %-10.2f%n",
                    t.getTransactionId(), t.getBookId(), t.getBorrowerId(),
                    t.getDueDate(), daysOver, fee, outstanding);
        }
            System.out.println("  " + "─".repeat(94));
        System.out.printf("  Total Overdue Books: %d  |  Total Current Fees: PHP %.2f%n",
            overdueList.size(), grossFees);
        double paymentsApplied = Math.max(grossFees - outstandingFees, 0);
        System.out.printf("  Payments Applied   : PHP %.2f%n", paymentsApplied);
        System.out.printf("  Outstanding Total  : PHP %.2f%n", outstandingFees);
    }

    // ══════════════════════════════════════════
    //  BORROWER MANAGEMENT
    // ══════════════════════════════════════════
    static void borrowerMenu() {
        boolean back = false;
        while (!back) {
            System.out.println();
            System.out.println("  ┌─────────────────────────────┐");
            System.out.println("  │     BORROWER MANAGEMENT     │");
            System.out.println("  ├─────────────────────────────┤");
            System.out.println("  │  1. View All Borrowers      │");
            System.out.println("  │  2. Add Borrower            │");
            System.out.println("  │  3. Delete Borrower         │");
            System.out.println("  │  0. Back                    │");
            System.out.println("  └─────────────────────────────┘");

            int choice = readInt("  Choice: ");
            switch (choice) {
                case 1 -> {
                    List<Borrower> list = borrowerDAO.getAllBorrowers();
                    if (list.isEmpty()) {
                        System.out.println("  No borrowers registered.");
                    } else {
                        System.out.println();
                        System.out.printf("  %-6s %-30s%n", "ID", "Name");
                        System.out.println(""  + "  " + "─".repeat(42));
                        list.forEach(b -> System.out.println("  " + b));
                        System.out.println(""  + "  " + "─".repeat(42));
                    }
                }
                case 2 -> {
                    String name = readString("  Borrower Name: ");
                    if (borrowerDAO.addBorrower(name)) {
                        System.out.println("  ✔ Borrower added.");
                    } else {
                        System.out.println("  ✘ Failed to add borrower.");
                    }
                }
                case 3 -> deleteBorrower();
                case 0 -> back = true;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void deleteBorrower() {
        System.out.println("\n  ── DELETE BORROWER ──");
        int borrowerId = readInt("  Borrower ID: ");
        Borrower borrower = borrowerDAO.getBorrowerById(borrowerId);

        if (borrower == null) {
            System.out.println("  [!] Borrower not found.");
            return;
        }

        // Check if borrower has active borrows
        long activeBorrows = transactionDAO.getActiveBorrows().stream()
                .filter(t -> t.getBorrowerId() == borrowerId)
                .count();

        if (activeBorrows > 0) {
            System.out.println("  [!] Cannot delete borrower with active book loans (" + activeBorrows + " active).");
            System.out.println("     Please return all books first.");
            return;
        }

        System.out.println("  Borrower: " + borrower.getName());
        String confirm = readString("  Are you sure? (yes/no): ");

        if (confirm.equalsIgnoreCase("yes")) {
            if (borrowerDAO.deleteBorrower(borrowerId)) {
                System.out.println("  ✔ Borrower deleted successfully.");
            } else {
                System.out.println("  ✘ Failed to delete borrower.");
            }
        } else {
            System.out.println("  Deletion cancelled.");
        }
    }

    // ══════════════════════════════════════════
    //  MEMBER PORTAL
    // ══════════════════════════════════════════
    static void runMemberPortal() {
        if (borrowerDAO == null) {
            System.out.println("  [!] Borrower data unavailable in limited mode.");
            return;
        }

        Borrower borrower = selectBorrower();
        if (borrower == null) {
            System.out.println("  Returning to role selection.");
            return;
        }

        boolean inMember = true;
        while (inMember) {
            printMemberMenu(borrower);
            int choice = readInt("  Choice: ");

            switch (choice) {
                case 1 -> showMemberDashboard(borrower);
                case 2 -> showMemberBorrowedBooks(borrower);
                case 3 -> searchMenu();
                case 4 -> {
                    borrower = selectBorrower();
                    if (borrower == null) {
                        System.out.println("  Logging out of member portal.");
                        inMember = false;
                    }
                }
                case 0 -> inMember = false;
                default -> System.out.println("  [!] Invalid option.");
            }
        }
    }

    static void printMemberMenu(Borrower borrower) {
        System.out.println();
        System.out.println("  ┌─────────────────────────────┐");
        System.out.println("  │        MEMBER PORTAL        │");
        System.out.println("  ├─────────────────────────────┤");
        System.out.printf("  │  Logged in as: %-12s │%n", borrower.getName());
        System.out.println("  ├─────────────────────────────┤");
        System.out.println("  │  1. View Dashboard          │");
        System.out.println("  │  2. View Borrowed Books     │");
        System.out.println("  │  3. Search Catalogue        │");
        System.out.println("  │  4. Switch Member           │");
        System.out.println("  │  0. Log-out                 │");
        System.out.println("  └─────────────────────────────┘");
    }

    static Borrower selectBorrower() {
        List<Borrower> list = borrowerDAO.getAllBorrowers();
        if (list.isEmpty()) {
            System.out.println("  [!] No borrowers available. Please ask an administrator to add members first.");
            return null;
        }

        System.out.println();
        System.out.printf("  %-6s %-30s%n", "ID", "Name");
        System.out.println("  " + "─".repeat(40));
        list.forEach(b -> System.out.printf("  %-6d %-30s%n", b.getBorrowerId(), b.getName()));
        System.out.println("  " + "─".repeat(40));

        while (true) {
            int id = readInt("  Enter Borrower ID (0 to cancel): ");
            if (id == 0) {
                return null;
            }
            Borrower borrower = borrowerDAO.getBorrowerById(id);
            if (borrower != null) {
                return borrower;
            }
            System.out.println("  [!] Invalid borrower. Please try again.");
        }
    }

    static void showMemberDashboard(Borrower borrower) {
        if (transactionDAO == null) {
            System.out.println("  [!] Transactions unavailable in limited mode.");
            return;
        }

        List<Transaction> txList = transactionDAO.getTransactionsByBorrower(borrower.getBorrowerId());
        long activeLoans = txList.stream().filter(t -> t.getReturnDate() == null).count();
        long returned    = txList.size() - activeLoans;
        double totalOverdue = txList.stream().mapToDouble(OPACSystem::calculateAccruedFee).sum();
        double totalPaid = paymentDAO != null ? paymentDAO.getTotalPaymentsForBorrower(borrower.getBorrowerId()) : 0;
        double outstanding = Math.max(totalOverdue - totalPaid, 0);

        System.out.println();
        System.out.println("  ── MEMBER DASHBOARD ─────────────────────");
        System.out.println("  Member      : " + borrower.getName());
        System.out.println("  Active Loans: " + activeLoans);
        System.out.println("  Returned    : " + returned);
        System.out.printf ("  Total Overdue Fees : PHP %.2f%n", totalOverdue);
        System.out.printf ("  Total Payments     : PHP %.2f%n", totalPaid);
        System.out.printf ("  Outstanding Balance: PHP %.2f%n", outstanding);
        System.out.println("  ─────────────────────────────────────────");
    }

    static void showMemberBorrowedBooks(Borrower borrower) {
        if (transactionDAO == null) {
            System.out.println("  [!] Transactions unavailable in limited mode.");
            return;
        }

        List<Transaction> txList = transactionDAO.getTransactionsByBorrower(borrower.getBorrowerId());
        if (txList.isEmpty()) {
            System.out.println("  No borrowed books recorded for this member.");
            return;
        }

        System.out.println();
        System.out.printf("  %-6s %-10s %-12s %-12s %-12s %-12s%n",
                "TX ID", "Book", "Borrow Date", "Due Date", "Return Date", "Status");
        System.out.println("  " + "─".repeat(72));

        for (Transaction t : txList) {
            String status;
            if (t.getReturnDate() == null) {
                long daysLeft = ChronoUnit.DAYS.between(LocalDate.now(), t.getDueDate());
                status = daysLeft < 0
                        ? "OVERDUE (" + Math.abs(daysLeft) + "d)"
                        : "Active (" + daysLeft + "d left)";
            } else {
                status = t.getReturnDate().isAfter(t.getDueDate()) ? "Returned Late" : "Returned";
            }

            System.out.printf("  %-6d %-10d %-12s %-12s %-12s %-12s%n",
                    t.getTransactionId(),
                    t.getBookId(),
                    t.getBorrowDate(),
                    t.getDueDate(),
                    t.getReturnDate() != null ? t.getReturnDate() : "—",
                    status);
        }
        System.out.println("  " + "─".repeat(72));
    }

    static double calculateAccruedFee(Transaction t) {
        if (t == null || t.getDueDate() == null) return 0;
        LocalDate comparison = t.getReturnDate() != null ? t.getReturnDate() : LocalDate.now();
        long days = ChronoUnit.DAYS.between(t.getDueDate(), comparison);
        if (days <= 0) return 0;
        return days * OverdueCalculator.getFeePerDay();
    }

    // ══════════════════════════════════════════
    //  DISPLAY HELPERS
    // ══════════════════════════════════════════
    static void printBookHeader() {
        System.out.println();
        System.out.printf("  %-5s  %-30s  %-22s  %-12s  %-20s  %-10s%n",
                "ID", "Title", "Author", "Category", "Dewey", "Status");
        System.out.println("  " + "─".repeat(123));
    }

    static void printTransactionHeader() {
        System.out.println();
        System.out.printf("  %-6s %-6s %-10s %-12s %-12s %-14s %-12s%n",
                "TX ID", "Book", "Borrower", "Borrow Date", "Due Date", "Return Date", "Fee (PHP)");
        printLine();
    }

    static void printLine() {
        System.out.println("  " + "─".repeat(91));
    }

    // ══════════════════════════════════════════
    //  INPUT HELPERS
    // ══════════════════════════════════════════
    static int readInt(String prompt) {
        while (true) {
            System.out.print(prompt);
            try {
                return Integer.parseInt(sc.nextLine().trim());
            } catch (NumberFormatException e) {
                System.out.println("  [!] Please enter a valid number.");
            }
        }
    }

    static String readString(String prompt) {
        System.out.print(prompt);
        return sc.nextLine().trim();
    }

    static String readStringOptional(String prompt, String defaultValue) {
        System.out.print(prompt);
        String input = sc.nextLine().trim();
        return input.isEmpty() ? defaultValue : input;
    }
}
