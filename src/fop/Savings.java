package fop;
import java.sql.*;
import java.util.Scanner;

public class Savings { 
    
    private static void activateSavings (Scanner scanner, Connection connection, int savings_id, int user_id) throws SQLException{
        System.out.print("\n==Savings==\nAre you sure you want to activate it? (Y/N): ");
        String option = scanner.next();
        
        if(option.equalsIgnoreCase("Y")){
            System.out.print("Please enter the percentage you wish to deduct from the next debit: ");
            int percentage = scanner.nextInt();
            
            String insertSavingsQuery = "INSERT INTO Savings (savings_id, user_id, amount) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = connection.prepareStatement(insertSavingsQuery)){
                stmt.setInt(1, savings_id);
                stmt.setInt(2, user_id);
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
