package fop;
import java.sql.*;
import java.util.Scanner;

public class Savings {
    
    private static final String url = "jdbc:mysql://localhost:3306/ledger";
    private static final String user = "root";
    private static final String password = "password";
    
    public static void main (String[] args){
        Scanner keyboard = new Scanner(System.in); 
        int user_id = 1;

        try (Connection conn = DriverManager.getConnection(url, user, password)){
            String userQuery = "SELECT * FROM Balance WHERE user_id = ?";
            
            try (PreparedStatement stmt = conn.prepareStatement(userQuery)){
                stmt.setInt(1, user_id);
                ResultSet rs = stmt.executeQuery();
                
                if (rs.next()){
                    double balance = rs.getDouble("current_amount");
                    System.out.println("== Welcome ==");
                    System.out.println("Balance: " + balance);
                    System.out.println("Savings: unavailable" );
                    System.out.println("Loan: unavailable");
                    System.out.println("== Transaction ==");
                    System.out.println("1. Debit \n2. Credit\n3. Savings");
                    int input = keyboard.nextInt();
                    
                    switch(input){
                        case 1:
                            System.out.println("unavailable");
                            break;
                        case 2:
                            System.out.println("unavailable");
                            break;
                        case 3:
                            activateSavings(keyboard, conn, user_id);
                            break;
                        default:
                            System.out.println("invalid");
                    }
                }
                else{
                    System.out.println("User ID not found");
                }
            }
            
        }
        catch (SQLException e){
            e.printStackTrace();
        }
        
    }
    
    private static void activateSavings (Scanner keyboard, Connection conn, int user_id) throws SQLException{
        System.out.print("\n==Savings==\nAre you sure you want to activate it? (Y/N): ");
        String option = keyboard.next();
        
        if(option.equalsIgnoreCase("Y")){
            System.out.print("Please enter the percentage you wish to deduct from the next debit: ");
            int percentage = keyboard.nextInt();
            
            String insertSavingsQuery = "INSERT INTO Savings (user_id, username, user_input, amount) VALUES (?, ?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertSavingsQuery)){
                stmt.setInt(1, user_id);
                stmt.setString(2, "Default user");
                stmt.setString(3, "Y");
                stmt.setDouble(4, percentage);
                
                stmt.executeUpdate();
                System.out.println("Savings Settings added successfully!!!");
            }
        }
        else {
            System.out.println("Savings activation cancelled.");
        }
    }
}
