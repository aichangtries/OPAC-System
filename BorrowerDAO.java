import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BorrowerDAO {

    private Connection conn;

    public BorrowerDAO(Connection conn) {
        this.conn = conn;
    }

    public boolean addBorrower(String name) {

        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }

        String sql = "INSERT INTO borrowers (name) VALUES (?)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to add borrower: " + e.getMessage());
            return false;
        }
    }

    public Borrower getBorrowerById(int id) {

        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return null;
        }

        String sql = "SELECT * FROM borrowers WHERE borrower_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Borrower(rs.getInt("borrower_id"), rs.getString("name"));
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to get borrower: " + e.getMessage());
        }
        return null;
    }

    public List<Borrower> getAllBorrowers() {
        
        List<Borrower> list = new ArrayList<>();
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return list;
        }
        String sql = "SELECT * FROM borrowers";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(new Borrower(rs.getInt("borrower_id"), rs.getString("name")));
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to list borrowers: " + e.getMessage());
        }
        return list;
    }

    public boolean updateBorrower(int borrowerId, String name) {
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }
        
        String sql = "UPDATE borrowers SET name=? WHERE borrower_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, name);
            ps.setInt(2, borrowerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to update borrower: " + e.getMessage());
            return false;
        }
    }

    public boolean deleteBorrower(int borrowerId) {
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }
        
        String sql = "DELETE FROM borrowers WHERE borrower_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, borrowerId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to delete borrower: " + e.getMessage());
            return false;
        }
    }
}
