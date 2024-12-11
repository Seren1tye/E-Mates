/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Main.java to edit this template
 */
package sem1project;

import java.sql.*;
import java.util.Scanner;
/**
 *
 * @author luqpic
 */
public class Ledger {

    private static final String URL = "jdbc:mysql://localhost:3306/Ledger";
    private static final String USER = "root";
    private static final String PASSWORD = "Yan230305#";
    
 public static void main(String[] args) {
        Scanner read = new Scanner(System.in);
        int UserId = 1; 
        int option;

        do {
            System.out.println("Balance: " + getBalance(UserId));
            System.out.println("1. Debit");
            System.out.println("2. Credit");
            System.out.println("3. Exit");
            System.out.print("Input: ");
            option = read.nextInt();
            read.nextLine(); // Consume newline

            switch (option) {
                case 1:
                    debitAmount(UserId, read);
                    break;
                case 2:
                    creditAmount(UserId, read);
                    break;
                case 3:
                    System.out.println("Thank you for using!");
                    break;
                default:
                    System.out.println("Invalid Input");
            }
        } while (option != 3);
        System.out.println();
    }

    
        private static Connection getConnection() {
            try {
                return DriverManager.getConnection(URL, USER, PASSWORD);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } 
        
        private static double getBalance(int userId) {
            double balance = 0.0;
            Connection conn = getConnection();
            if (conn == null) {
                System.out.println("Failed to establish a database connection.");
                return balance; 
            }
            try (PreparedStatement stmt = conn.prepareStatement(
                    "SELECT SUM(amount) AS balance FROM Transactions WHERE user_id = ?")) {
                stmt.setInt(1, userId);
                try (ResultSet rs = stmt.executeQuery()) {
                    if (rs.next()) {
                        balance = rs.getDouble("balance");
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            return balance;
        }

        

        private static void creditService(int userId, double amount, String description) {
             try (Connection conn = getConnection()) {
            String insertTransaction = "INSERT INTO Transactions (user_id, amount, description) VALUES (?, ?, ?)";
            try (PreparedStatement stmt = conn.prepareStatement(insertTransaction)) {
                stmt.setInt(1, userId);
                stmt.setDouble(2, -amount);
                stmt.setString(3, description);
                stmt.executeUpdate();
            }
        } catch (Exception e) {
            e.printStackTrace();
            }
        }
        
        private static void debitAmount(int userId, Scanner read) {
        System.out.println("\n===DEBIT===");
        System.out.print("Amount: ");
        double amount = read.nextDouble();
        read.nextLine(); // Consume newline
        System.out.print("Description: ");
        String description = read.nextLine();

        if (amount <= 0) {
            System.out.println("Invalid amount. Please enter a positive value.\n");
            return;
        }

        Connection conn = getConnection();
        if (conn == null) {
            System.out.println("Failed to connect to the database.\n");
            return;
        }

        try {
            conn.setAutoCommit(true); // Enable auto-commit for immediate execution
            String query = "INSERT INTO Transactions (user_id, amount, description, transaction_date, transaction_type) VALUES (?, ?, ?, NOW(), 'Debit')";
            try (PreparedStatement stmt = conn.prepareStatement(query)) {
                stmt.setInt(1, userId);
                stmt.setDouble(2, amount); // Positive value for debit
                stmt.setString(3, description);

                int rowsInserted = stmt.executeUpdate();
                if (rowsInserted > 0) {
                    System.out.println("Transaction Successful!\n");
                } else {
                    System.out.println("Transaction Failed.\n");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error processing transaction.\n");
            e.printStackTrace();
        }
    }

        
        private static void creditAmount(int userId, Scanner read) {
            System.out.println("\n===CREDIT===");
            System.out.print("Amount: ");
            double amount = read.nextDouble();
            read.nextLine(); // Consume newline
            System.out.print("Description: ");
            String description = read.nextLine();

            if (amount <= 0) {
                System.out.println("Invalid amount. Please enter a positive value.\n");
                return;
            }

            Connection conn = getConnection();
            if (conn == null) {
                System.out.println("Failed to connect to the database.\n");
                return;
            }

            try {
                conn.setAutoCommit(true); // Enable auto-commit for immediate execution
                String query = "INSERT INTO Transactions (user_id, amount, description, transaction_date, transaction_type) VALUES (?, ?, ?, NOW(), 'Credit')";
                try (PreparedStatement stmt = conn.prepareStatement(query)) {
                    stmt.setInt(1, userId);
                    stmt.setDouble(2, -amount); // Negative value for credit
                    stmt.setString(3, description);

                    int rowsInserted = stmt.executeUpdate();
                    if (rowsInserted > 0) {
                        System.out.println("Transaction Successful!\n");
                    } else {
                        System.out.println("Transaction Failed.\n");
                    }
                }
            } catch (SQLException e) {
                System.out.println("Error processing transaction.\n");
                e.printStackTrace();
            }
        }

}


    
    
    
