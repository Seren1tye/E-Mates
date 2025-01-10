import java.sql.*;
import java.util.Scanner;
import org.mindrot.jbcrypt.BCrypt;

public class user_actions {

    // Method to handle user registration 
    public static void sign_up() {
        try {
            Connection connection = DB.Connect(); // Establish a connection to the database
            Scanner scanner = new Scanner(System.in);

            System.out.println("\n== Please fill in the form ==");
            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            // Validate that the name contains only alphanumeric characters and spaces
            if (!name.matches("[a-zA-Z0-9\\s]+")) {
                System.out.println("\nError: Name must be alphanumeric and cannot contain special characters.\n");
                return; // Exit the method if the name is invalid
            }

            System.out.print("Enter your email: ");
            String email = scanner.nextLine();

            // Validate the email format using a regular expression
            if (!email.matches("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")) {
                System.out.println("Error: Invalid email format.");
                return; // Exit the method if the email format is invalid
            }

            // Check if the email is already registered in the database
            String emailCheckQuery = "SELECT COUNT(*) FROM users WHERE email = ?";
            PreparedStatement emailCheckStatement = connection.prepareStatement(emailCheckQuery);
            emailCheckStatement.setString(1, email);
            ResultSet resultSet = emailCheckStatement.executeQuery();
            resultSet.next();

            // If the email is already registered, display an error message
            if (resultSet.getInt(1) > 0) {
                System.out.println("\nError: This email is already registered.\n");
                return; // Exit the method if the email is already in use
            }

            System.out.print("Enter your password: ");
            String pass = scanner.nextLine();

            // Validate that the password is at least 8 characters long
            if (pass.length() < 8) {
                System.out.println("\nError: Password must be at least 8 characters long.\n");
                return; // Exit the method if the password is too short
            }

            System.out.print("Confirm your password: ");
            String confirmPass = scanner.nextLine();

            // Check if the password and confirmation password match
            if (!pass.equals(confirmPass)) {
                System.out.println("Error: Passwords do not match!");
                return; // Exit the method if the passwords do not match
            }

            // Hash the password using BCrypt for secure storage
            String hashedPassword = BCrypt.hashpw(pass, BCrypt.gensalt());

            // SQL query to insert the new user into the database
            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            // Set the parameters for the SQL query
            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, hashedPassword);

            // Execute the insert query and check if the user was successfully added
            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Registration successful!!! \n");
            }
        } catch (SQLException e) {
            System.out.println("Database Error: " + e.getMessage()); // Handle SQL exceptions
        }
    }

    // Method to handle user login
    public static String[] log_in() {
        int userId = -1; // Default value for user ID if login fails
        String userName = null; // Default value for user name if login fails
        try {
            Connection connection = DB.Connect(); // Establish a connection to the database
            Scanner scanner = new Scanner(System.in);
            System.out.println("== Please enter your email and password ==");
            System.out.print("Email: ");
            String email = scanner.nextLine();

            System.out.print("Password: ");
            String pass = scanner.nextLine();

            // SQL query to fetch user details based on the provided email
            String sql = "SELECT user_id, name, password FROM users WHERE email = ?";
            PreparedStatement statement = connection.prepareStatement(sql);
            statement.setString(1, email);

            ResultSet resultSet = statement.executeQuery();

            // Check if the email exists in the database
            if (resultSet.next()) {
                String storedHashedPassword = resultSet.getString("password"); // Get the stored hashed password
                // Verify the provided password against the stored hashed password
                if (BCrypt.checkpw(pass, storedHashedPassword)) {
                    userId = resultSet.getInt("user_id"); // Get the user ID
                    userName = resultSet.getString("name"); // Get the user's name
                } else {
                    System.out.println("\nInvalid email or password.\n"); // Incorrect password
                }
            } else {
                System.out.println("\nInvalid email or password.\n"); // Email not found
            }
        } catch (SQLException e) {
            System.out.println("Error during login."); // Handle SQL exceptions
            e.printStackTrace();
        }
        // Return the user ID and user name as an array
        return new String[]{String.valueOf(userId), userName};
    }
}
