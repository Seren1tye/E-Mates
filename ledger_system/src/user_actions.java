import java.sql.*;
import java.util.Scanner;

public class user_actions {
    
        public static void sign_up(){
            
            try{
                Connection connection = DB.Connect();
                        Scanner scanner = new Scanner(System.in);

            // User input
            System.out.print("Enter your name: ");
            String name = scanner.nextLine();

            System.out.print("Enter your email: ");
            String email = scanner.nextLine();

            System.out.print("Enter your password: ");
            String pass = scanner.nextLine();

            System.out.print("Confirm your password: ");
            String confirmPass = scanner.nextLine();

            if (!pass.equals(confirmPass)) {
                System.out.println("Passwords do not match!");
                return;
            }

            // SQL to insert data
            String sql = "INSERT INTO users (name, email, password) VALUES (?, ?, ?)";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, name);
            statement.setString(2, email);
            statement.setString(3, pass); // For now, plaintext (later secure this with hashing)

            int rowsInserted = statement.executeUpdate();

            if (rowsInserted > 0) {
                System.out.println("Registration successful!");
            }
            }catch(SQLException e){
                System.out.println(e);
            }
            
            
        }
        public static void log_in(){
            try{
                Connection connection = DB.Connect();
                        Scanner scanner = new Scanner(System.in);

            // User input
            System.out.print("Enter your email: ");
            String email = scanner.nextLine();

            System.out.print("Enter your password: ");
            String pass = scanner.nextLine();

            // SQL to validate user
            String sql = "SELECT * FROM users WHERE email = ? AND password = ?";
            PreparedStatement statement = connection.prepareStatement(sql);

            statement.setString(1, email);
            statement.setString(2, pass);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                System.out.println("Login successful! Welcome, " + resultSet.getString("name") + "!");
            } else {
                System.out.println("Invalid email or password.");
            }
            }catch(SQLException e){
                System.out.println(e);
            }
        
        }
    
    
}
