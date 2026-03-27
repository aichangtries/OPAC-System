import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class Database {

    private static final String URL      = "jdbc:mysql://localhost:3306/opac_system";
    private static final String USER     = "root";
    private static final String PASSWORD = "";

    private static Connection connection;

    public static Connection connect() {
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            connection = DriverManager.getConnection(URL, USER, PASSWORD);

            if (connection != null) {
                System.out.println("  [DB] Connected to the database!");
            }

        } catch (ClassNotFoundException | SQLException e) {
            System.out.println("  [DB] Failed to connect to the database!");
            e.printStackTrace();
        }

        return connection;
    }

    public static void disconnect() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
                System.out.println("  [DB] Disconnected from the database.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static Connection getConnection() {
        return connection;
    }

    public static void main(String[] args) {
        connect();
        disconnect();
    }
}
