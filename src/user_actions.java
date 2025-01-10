
import java.sql.*;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class user_actions {

    public static void sign_up() {
        try {
            Connection connection = DB.Connect();
            Scanner scanner = new Scanner(System.in);

            System.out.println("\n== Please fill in the form ==");
            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            if (!name.matches("[a-zA-Z0-9\\s]+")) {
                System.out.println("\nError: Name must be alphanumeric and cannot contain special characters.\n");
                return;
            }

            System.out.print("Enter your email: ");
            String email = scanner.nextLine();

            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("Error: Invalid email format.");
                return;
            }

            String emailCheckQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            PreparedStatement emailCheckStatement = connection.prepareStatement(emailCheckQuery);
            emailCheckStatement.setString(1, email);
            ResultSet resultSet = emailCheckStatement.executeQuery();
            resultSet.next();

            if (resultSet.getInt(1) > 0) {
                System.out.println("\nError: This email is already registered.\n");
                return;
            }

            System.out.print("Enter your password: ");
            String pass = scanner.nextLine();

            if (pass.length() < 8) {
                System.out.println("\nError: Password must be at least 8 characters long.\n");
                return;
            }

            System.out.print("Confirm your password: ");
            String confirmPass = scanner.nextLine();

            if (!pass.equals(confirmPass)) {
                System.out.println("Error: Passwords do not match!");
                return;
            }

            String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());

            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, hashedPassword);

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Registration successful!!! \n");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage());
        }
    }

    public static String[] log_in() {
        int userId = -1;
        String userName = null;
        try {
            Connection connection = DB.Connect();
            Scanner scanner = new Scanner(System.in);
            System.out.println("== Please enter your email and password ==");
            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String pass = scanner.nextLine();

            String sql = "SELECT user_id, name, password FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password");
                if (BCrypt.checkpw(pass, storedHashedPassword)) {
                    userId = resultSet.getInt("user_id");
                    userName = resultSet.getString("name");
                } else {
                    System.out.println("\nInvalid email or password.\n");
                }
            } else {
                System.out.println("\nInvalid email or password.\n");
            }
        } catch (SQLException e) {
            System.out.println("Error during login.");
            e.printStackTrace();
        }
        return new String[]{String.valueOf(userId), userName};
    }
}
