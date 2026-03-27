/**
 * ============================================================
 *   DEWEY DECIMAL CLASSIFICATION SYSTEM
 *   Automatically assigns Dewey Decimal codes based on category
 *   Classifications are stored in the database
 * ============================================================
 */
import java.sql.*;

public class DeweyDecimal {

    /**
     * Get Dewey Decimal classification for a category by querying the database
     */
    public static String getClassification(Connection conn, String category) {
        if (category == null || category.trim().isEmpty()) {
            return "000 - General Works";
        }

        String cat = category.toLowerCase().trim();
        
        try {
            // First try: exact keyword match
            String sql = "SELECT code, description FROM dewey_classifications " +
                        "WHERE LOWER(keywords) LIKE ? " +
                        "ORDER BY LENGTH(keywords) ASC LIMIT 1";
            
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                ps.setString(1, "%" + cat + "%");
                java.sql.ResultSet rs = ps.executeQuery();
                
                if (rs.next()) {
                    String code = rs.getString("code");
                    String description = rs.getString("description");
                    return code + " - " + description;
                }
            }
            
            // Fallback: if no match found, try matching parts of keywords
            sql = "SELECT code, description FROM dewey_classifications " +
                  "ORDER BY FIELD(code, '005', '100', '200', '300', '400', '500', '600', '700', '800', '900') " +
                  "LIMIT 1";
            try (java.sql.PreparedStatement ps = conn.prepareStatement(sql)) {
                java.sql.ResultSet rs = ps.executeQuery();
                if (rs.next()) {
                    String code = rs.getString("code");
                    String description = rs.getString("description");
                    return code + " - " + description;
                }
            }
            
        } catch (java.sql.SQLException e) {
            System.out.println("  [Error] Failed to get Dewey classification: " + e.getMessage());
        }
        
        // Default fallback
        return "000 - General Works";
    }

    /**
     * Get just the code without description
     */
    public static String getClassificationCode(Connection conn, String category) {
        String full = getClassification(conn, category);
        return full.split(" - ")[0]; // Extract just the code part
    }

    /**
     * Get a detailed description of the Dewey Decimal system
     */
    public static void printDeweySystemInfo() {
        System.out.println("\n  ╔══════════════════════════════════════════════════════╗");
        System.out.println("  ║       DEWEY DECIMAL CLASSIFICATION SYSTEM             ║");
        System.out.println("  ╚══════════════════════════════════════════════════════╝");
        System.out.println("  000-099 → Computer science, Information & General");
        System.out.println("  100-199 → Philosophy & Psychology");
        System.out.println("  200-299 → Religion");
        System.out.println("  300-399 → Social Sciences");
        System.out.println("  400-499 → Language");
        System.out.println("  500-599 → Science");
        System.out.println("  600-699 → Technology & Applied Sciences");
        System.out.println("  700-799 → Arts & Recreation");
        System.out.println("  800-899 → Literature");
        System.out.println("  900-999 → History & Geography");
        System.out.println();
    }
}
