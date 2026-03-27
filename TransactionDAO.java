import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class TransactionDAO {

    private Connection conn;

    public TransactionDAO(Connection conn) {
        this.conn = conn;
    }

    // ─────────────────────────────────────────
    // BORROW a book (creates a new transaction)
    // ─────────────────────────────────────────
    public boolean borrowBook(int bookId, int borrowerId, int loanDays) {
        
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }

        LocalDate borrowDate = LocalDate.now();
        LocalDate dueDate    = borrowDate.plusDays(loanDays);

        String sql = "INSERT INTO transactions (book_id, borrower_id, borrow_date, due_date) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ps.setInt(2, borrowerId);
            ps.setDate(3, Date.valueOf(borrowDate));
            ps.setDate(4, Date.valueOf(dueDate));
            if (ps.executeUpdate() > 0) {
                // Update book availability status
                updateBookAvailability(bookId, false);
                return true;
            }
            return false;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to record borrow: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // RETURN a book – calculates overdue fee
    // ─────────────────────────────────────────
    public double returnBook(int transactionId) {

        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return -1;
        }

        // First, get the due date to compute fee
        Transaction t = getTransactionById(transactionId);
        if (t == null) {
            System.out.println("  [Error] Transaction not found.");
            return -1;
        }
        if (t.getReturnDate() != null) {
            System.out.println("  [Info] This book was already returned.");
            return -1;
        }

        LocalDate returnDate = LocalDate.now();
        double fee = OverdueCalculator.calculateFee(t.getDueDate(), returnDate);

        String sql = "UPDATE transactions SET return_date=?, overdue_fee=? WHERE transaction_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(returnDate));
            ps.setDouble(2, fee);
            ps.setInt(3, transactionId);
            ps.executeUpdate();
            // Update book availability status back to available
            updateBookAvailability(t.getBookId(), true);
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to update return: " + e.getMessage());
            return -1;
        }

        return fee;
    }

    // ─────────────────────────────────────────
    // RETURN a book WITH CUSTOM DATE
    // ─────────────────────────────────────────
    public double returnBookWithDate(int transactionId, LocalDate customReturnDate) {

        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return -1;
        }

        // First, get the due date to compute fee
        Transaction t = getTransactionById(transactionId);
        if (t == null) {
            System.out.println("  [Error] Transaction not found.");
            return -1;
        }
        if (t.getReturnDate() != null) {
            System.out.println("  [Info] This book was already returned.");
            return -1;
        }

        double fee = OverdueCalculator.calculateFee(t.getDueDate(), customReturnDate);

        String sql = "UPDATE transactions SET return_date=?, overdue_fee=? WHERE transaction_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setDate(1, Date.valueOf(customReturnDate));
            ps.setDouble(2, fee);
            ps.setInt(3, transactionId);
            ps.executeUpdate();
            // Update book availability status back to available
            updateBookAvailability(t.getBookId(), true);
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to update return: " + e.getMessage());
            return -1;
        }

        return fee;
    }

    // ─────────────────────────────────────────
    // GET single transaction
    // ─────────────────────────────────────────
    public Transaction getTransactionById(int id) {

        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return null;
        }

        String sql = "SELECT * FROM transactions WHERE transaction_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("  [Error] " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────
    // GET all active borrows (not yet returned)
    // ─────────────────────────────────────────
    public List<Transaction> getActiveBorrows() {
        
        List<Transaction> list = new ArrayList<>();
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return list;
        }
        String sql = "SELECT * FROM transactions WHERE return_date IS NULL";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("  [Error] " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────
    // GET all overdue (not returned and past due)
    // ─────────────────────────────────────────
    public List<Transaction> getOverdueTransactions() {
        List<Transaction> list = new ArrayList<>();
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return list;
        }
        String sql =
            "SELECT * FROM transactions " +
            "WHERE (return_date IS NULL AND due_date < CURDATE()) " +
            "   OR (return_date IS NOT NULL AND return_date > due_date)";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("  [Error] " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────
    // GET open transaction for a given book
    // ─────────────────────────────────────────
    public Transaction getOpenTransactionByBook(int bookId) {
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return null;
        }
        String sql = "SELECT * FROM transactions WHERE book_id=? AND return_date IS NULL LIMIT 1";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("  [Error] " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────
    // GET all transactions
    // ─────────────────────────────────────────
    public List<Transaction> getAllTransactions() {
        
        List<Transaction> list = new ArrayList<>();
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return list;
        }
        String sql = "SELECT * FROM transactions ORDER BY transaction_id DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("  [Error] " + e.getMessage());
        }
        return list;
    }

    // ─────────────────────────────────────────
    // GET all transactions by borrower
    // ─────────────────────────────────────────
    public List<Transaction> getTransactionsByBorrower(int borrowerId) {

        List<Transaction> list = new ArrayList<>();
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return list;
        }

        String sql = "SELECT * FROM transactions WHERE borrower_id=? ORDER BY borrow_date DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) list.add(mapRow(rs));
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to load borrower transactions: " + e.getMessage());
        }
        return list;
    }

    // Helper – map row to Transaction object
    private Transaction mapRow(ResultSet rs) throws SQLException {
        Date retDate = rs.getDate("return_date");
        return new Transaction(
            rs.getInt("transaction_id"),
            rs.getInt("book_id"),
            rs.getInt("borrower_id"),
            rs.getDate("borrow_date").toLocalDate(),
            rs.getDate("due_date").toLocalDate(),
            retDate != null ? retDate.toLocalDate() : null,
            rs.getDouble("overdue_fee")
        );
    }

    // Helper – update book availability status
    private void updateBookAvailability(int bookId, boolean available) {
        String sql = "UPDATE books SET is_available=? WHERE book_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, bookId);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to update book availability: " + e.getMessage());
        }
    }
}
