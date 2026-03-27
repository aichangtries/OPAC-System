import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class BookDAO {

    private Connection conn;

    public BookDAO(Connection conn) {
        this.conn = conn;
    }

    // ─────────────────────────────────────────
    // CREATE – Add a new book
    // ─────────────────────────────────────────
    public boolean addBook(String title, String author, String category) {
        
        if (conn == null) {
            System.out.println(" [Error] Database is not connected.");
            return false;
        }

        // Automatically get Dewey Decimal from database based on category
        String deweyDecimal = DeweyDecimal.getClassification(conn, category);

        String sql = "INSERT INTO books (title, author, category, dewey_decimal, is_available) VALUES (?, ?, ?, ?, 1)";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, category);
            ps.setString(4, deweyDecimal);
            int rows = ps.executeUpdate();
            return rows > 0;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to add book: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // READ – Get all books
    // ─────────────────────────────────────────
    public List<Book> getAllBooks() {
        List<Book> books = new ArrayList<>();

        if (conn == null) {
            System.out.println(" [Error] Database is not connected.");
            return books;
        }
        String sql = "SELECT * FROM books";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(sql)) {
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to retrieve books: " + e.getMessage());
        }
        return books;
    }

    // ─────────────────────────────────────────
    // READ – Get single book by ID
    // ─────────────────────────────────────────
    public Book getBookById(int bookId) {

        if (conn == null) {
            System.out.println(" [Error] Database is not connected.");
            return null;
        }

        String sql = "SELECT * FROM books WHERE book_id = ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) return mapRow(rs);
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to get book: " + e.getMessage());
        }
        return null;
    }

    // ─────────────────────────────────────────
    // SEARCH – By title, author, category, dewey
    // ─────────────────────────────────────────
    public List<Book> searchByTitle(String keyword) {
        return search("title", keyword);
    }

    public List<Book> searchByAuthor(String keyword) {
        return search("author", keyword);
    }

    public List<Book> searchByCategory(String keyword) {
        return search("category", keyword);
    }

    public List<Book> searchByDeweyDecimal(String keyword) {
        return search("dewey_decimal", keyword);
    }

    private List<Book> search(String column, String keyword) {
        
        List<Book> books = new ArrayList<>();
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return books;
        }
        String sql = "SELECT * FROM books WHERE " + column + " LIKE ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, "%" + keyword + "%");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                books.add(mapRow(rs));
            }
        } catch (SQLException e) {
            System.out.println("  [Error] Search failed: " + e.getMessage());
        }
        return books;
    }

    // ─────────────────────────────────────────
    // UPDATE – Edit book details
    // ─────────────────────────────────────────
    public boolean updateBook(int bookId, String title, String author, String category) {
        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }

        // Automatically get Dewey Decimal from database based on category
        String deweyDecimal = DeweyDecimal.getClassification(conn, category);

        String sql = "UPDATE books SET title=?, author=?, category=?, dewey_decimal=? WHERE book_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, title);
            ps.setString(2, author);
            ps.setString(3, category);
            ps.setString(4, deweyDecimal);
            ps.setInt(5, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to update book: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // UPDATE – Availability flag
    // ─────────────────────────────────────────
    public boolean setAvailability(int bookId, boolean available) {

        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }

        String sql = "UPDATE books SET is_available=? WHERE book_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setBoolean(1, available);
            ps.setInt(2, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to update availability: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // DELETE – Remove a book
    // ─────────────────────────────────────────
    public boolean deleteBook(int bookId) {

        if (conn == null) {
            System.out.println("  [Error] Database is not connected.");
            return false;
        }

        String sql = "DELETE FROM books WHERE book_id=?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, bookId);
            return ps.executeUpdate() > 0;
        } catch (SQLException e) {
            System.out.println("  [Error] Failed to delete book: " + e.getMessage());
            return false;
        }
    }

    // ─────────────────────────────────────────
    // Helper – Map ResultSet row → Book object
    // ─────────────────────────────────────────
    private Book mapRow(ResultSet rs) throws SQLException {
        return new Book(
            rs.getInt("book_id"),
            rs.getString("title"),
            rs.getString("author"),
            rs.getString("category"),
            rs.getString("dewey_decimal"),
            rs.getBoolean("is_available")
        );
    }
}
