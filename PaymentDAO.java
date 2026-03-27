import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PaymentDAO {

    private final Connection conn;
    private boolean paymentsTableReady = false;

    public PaymentDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean recordPayment(int borrowerId, Integer transactionId, double amount, String remarks) {
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }
        if (amount <= 0) {
            System.out.println("  [Error] Payment amount must be greater than zero.");
            return false;
        }
        if (!ensurePaymentsTable()) {
            System.out.println("  [Error] Payments table is unavailable.");
            return false;
        }

        String sql = "INSERT INTO payments (borrower_id, transaction_id, amount, remarks) VALUES (?, ?, ?, ?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerId);
            if (transactionId != null) {
                ps.setInt(2, transactionId);
            } else {
                ps.setNull(2, Types.INTEGER);
            }
            ps.setDouble(3, amount);
            ps.setString(4, remarks);
            boolean paymentInserted = ps.executeUpdate() > 0;
            
            // If payment was recorded for a specific transaction, mark it as returned (if not already)
            // This removes the overdue entry from the display when payment is made
            if (paymentInserted && transactionId != null) {
                markTransactionAsReturned(transactionId);
            }
            
            return paymentInserted;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to record payment: " + e.getMessage());
            return false;
        }
    }
    
    /**
     * Marks a transaction as returned if it hasn't been returned yet.
     * This removes the overdue book from the overdue list when payment is made.
     * @param transactionId the transaction ID
     */
    private void markTransactionAsReturned(Integer transactionId) {
        if (transactionId == null || conn == null) {
            return;
        }
        
        try {
            // Check if transaction exists and hasn't been returned yet
            String checkSql = "SELECT return_date FROM transactions WHERE transaction_id = ?";
            try (PreparedStatement checkPs = conn.prepareStatement(checkSql)) {
                checkPs.setInt(1, transactionId);
                ResultSet rs = checkPs.executeQuery();
                if (rs.next()) {
                    if (rs.getObject("return_date") == null) {
                        // Book hasn't been returned yet, so mark it as returned today
                        java.time.LocalDate today = java.time.LocalDate.now();
                        String updateSql = "UPDATE transactions SET return_date = ? WHERE transaction_id = ?";
                        try (PreparedStatement updatePs = conn.prepareStatement(updateSql)) {
                            updatePs.setDate(1, java.sql.Date.valueOf(today));
                            updatePs.setInt(2, transactionId);
                            int rowsUpdated = updatePs.executeUpdate();
                            if (rowsUpdated > 0) {
                                System.out.println("  [Info] Transaction " + transactionId + " marked as returned on " + today);
                            }
                        }
                    }
                }
            }
        } catch (SQLException e) {
            System.out.println("  [Warning] Could not mark transaction as returned: " + e.getMessage());
            // Don't fail the payment if this step fails - payment was already recorded
        }
    }
    
    /**
     * Gets the total outstanding balance for a borrower (fees minus paid amounts)
     * @param borrowerId the borrower ID
     * @param totalOverdueFees the total overdue fees calculated
     * @return outstanding balance (fees - payments, minimum 0)
     */
    public double getOutstandingBalance(int borrowerId, double totalOverdueFees) {
        double totalPaid = getTotalPaymentsForBorrower(borrowerId);
        return Math.max(totalOverdueFees - totalPaid, 0);
    }

    public double getTotalPayments() {
        if (conn == null) return 0;
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM payments";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to get total payments: " + e.getMessage());
        }
        return 0;
    }

    public double getTotalPaymentsForBorrower(int borrowerId) {
        if (conn == null) return 0;
        String sql = "SELECT COALESCE(SUM(amount), 0) AS total FROM payments WHERE borrower_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return rs.getDouble("total");
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to get borrower payments: " + e.getMessage());
        }
        return 0;
    }

    public List<Payment> getPaymentsForBorrower(int borrowerId) {
        List<Payment> list = new ArrayList<>();
        if (conn == null) return list;
        String sql = "SELECT * FROM payments WHERE borrower_id=? ORDER BY paid_at DESC";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to list payments: " + e.getMessage());
        }
        return list;
    }

    public List<Payment> getAllPayments() {
        List<Payment> list = new ArrayList<>();
        if (conn == null) return list;
        String sql = "SELECT * FROM payments ORDER BY paid_at DESC";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to list payments: " + e.getMessage());
        }
        return list;
    }

    public Map<Integer, Double> getPaymentsByBorrower() {
        Map<Integer, Double> totals = new HashMap<>();
        if (conn == null) return totals;
        String sql = "SELECT borrower_id, COALESCE(SUM(amount), 0) AS total_paid FROM payments GROUP BY borrower_id";
        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                totals.put(rs.getInt("borrower_id"), rs.getDouble("total_paid"));
            }
        } catch (SQLException e) {
            // Check if the table doesn't exist - if so, initialize it
            if (e.getMessage() != null && e.getMessage().contains("doesn't exist")) {
                System.out.println("  [Info] Payments table not found, initializing...");
                if (initializePaymentsTable()) {
                    System.out.println("  [Info] Payments table created. You may now record payments.");
                    paymentsTableReady = true;
                } else {
                    System.out.println("  [Error] Failed to create payments table: " + e.getMessage());
                }
            } else {
                System.out.println("  [Error] Failed to aggregate payments: " + e.getMessage());
            }
        }
        return totals;
    }
    
    private boolean initializePaymentsTable() {
        if (conn == null) return false;
        String sql = "CREATE TABLE IF NOT EXISTS payments (" +
                "  payment_id int(11) NOT NULL AUTO_INCREMENT," +
                "  borrower_id int(11) NOT NULL," +
                "  transaction_id int(11) DEFAULT NULL," +
                "  amount decimal(10,2) NOT NULL," +
                "  remarks varchar(255) DEFAULT NULL," +
                "  paid_at datetime DEFAULT CURRENT_TIMESTAMP," +
                "  PRIMARY KEY (payment_id)" +
                ") ENGINE=InnoDB DEFAULT CHARSET=utf8mb4";
        try (Statement stmt = conn.createStatement()) {
            stmt.executeUpdate(sql);
            paymentsTableReady = true;
            return true;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to create payments table: " + e.getMessage());
            return false;
        }
    }

    private boolean ensurePaymentsTable() {
        if (paymentsTableReady) return true;
        return initializePaymentsTable();
    }

    private Payment mapRow(ResultSet rs) throws SQLException {
        Timestamp ts = rs.getTimestamp("paid_at");
        LocalDateTime paidAt = ts != null ? ts.toLocalDateTime() : null;
        Integer txId = rs.getInt("transaction_id");
        if (rs.wasNull()) {
            txId = null;
        }
        return new Payment(
                rs.getInt("payment_id"),
                rs.getInt("borrower_id"),
                txId,
                rs.getDouble("amount"),
                rs.getString("remarks"),
                paidAt
        );
    }
}
