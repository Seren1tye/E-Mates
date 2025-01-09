import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

public class DB {
    public static Connection Connect() {
        String url = "jdbc:mysql://localhost:3306/ledger_system";
        String username = "root"; // Replace with your database username
        String password = "password"; // Replace with your database password

        Connection connection = null;
        try {
            connection = DriverManager.getConnection(url, username, password);
            //System.out.println("Connected to the database!");
        } catch (SQLException e) {
            System.out.println("Failed to connect to the database: " + e.getMessage());
        }
        return connection;
    }
}
