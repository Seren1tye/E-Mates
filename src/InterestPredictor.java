// This program calculates interest (daily/monthly/annually) based on the user's current balance and displays the result.

import java.sql.*;
import java.util.Scanner;

public class InterestPredictor {
    
    private static Scanner in = new Scanner(System.in); // Scanner for user input
    
    // Main method to manage the interest calculation process
    public static void mainInterest(int user_id) {
        String input;
        int choice;
        double interestRate = interestRate(chooseBank()); // Get initial interest rate based on chosen bank
        
        do {
            choice = 0;
            
            // Display menu options for interest period selection
            System.out.println("\nChoose interest period:");
            System.out.println("1. Daily");
            System.out.println("2. Monthly");
            System.out.println("3. Annually");
            System.out.println("4. Choose bank");
            System.out.println("5. Exit");
            System.out.print("\n> ");
            
            input = in.nextLine();   // Handle user input and exceptions for invalid input
            try{
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Error. Please enter a number 1-5.\n");
            }

            // Process user choice and perform relevant actions
            switch(choice){
                case 1:
                    System.out.printf("The daily interest is %.2f", calculateInterest(user_id, choice, interestRate));
                    System.out.println();
                    break;

                case 2:
                    System.out.printf("The monthly interest is %.2f", calculateInterest(user_id, choice, interestRate));
                    System.out.println();
                    break;

                case 3:
                    System.out.printf("The annual interest is %.2f", calculateInterest(user_id, choice, interestRate));
                    System.out.println();
                    break;

                case 4:
                    interestRate = interestRate(chooseBank()); // Update interest rate based on new bank selection
                    break;
                    
                case 5:
                    break;

                default:
                    // Handle invalid menu choices
                    if (Character.isDigit(input.charAt(0))) {
                        System.out.println("Invalid choice.");
                    }
                    break;

            }

        } while (choice != 5); // Loop until the user chooses to exit
    }
    
    // Method to allow user to select a bank and return its ID
    public static int chooseBank(){
        int bankID = 0;
        String input;
        int choice = 0;
        boolean repeat;
        
        do {
            repeat = false;
            // Display menu options for bank selection
            System.out.println("\nChoose bank:");
            System.out.println("1. RHB");
            System.out.println("2. Maybank");
            System.out.println("3. Hong Leong");
            System.out.println("4. Alliance");
            System.out.println("5. AmBank");
            System.out.println("6. Standard Chartered");
            System.out.print("\n> ");
            
            input = in.nextLine();  // Handle user input and exceptions for invalid input
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Error. Please enter a number 1-6.\n");
            }

            // Map user choice to corresponding bank ID
            switch (choice){
                case 1:
                    bankID = 1;
                    break;

                case 2:
                    bankID = 2;
                    break;

                case 3:
                    bankID = 3;
                    break;

                case 4:
                    bankID = 4;
                    break;

                case 5:
                    bankID = 5;
                    break;

                case 6:
                    bankID = 6;
                    break;

                default:
                    if (Character.isDigit(input.charAt(0))) {
                        System.out.println("Invalid choice.");
                    }
                    repeat = true; // Prompt user again if input is invalid
                    break;

            }    
        } while (repeat); // Loop until a valid choice is made
        
        return bankID;
    }
    
    // Method to retrieve the interest rate for a given bank ID
    public static double interestRate(int bankID){
        double interestRate = 0;
        
        try (Connection connection = DB.Connect()){ // Connect to the database
            String query = "SELECT interest_rate FROM bankDetails WHERE bank_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setDouble(1, bankID);
            
            ResultSet rs = pstmt.executeQuery(); // Execute query and retrieve interest rate
            while (rs.next()){
                interestRate = rs.getDouble("interest_rate");
            }
            
        } catch (SQLException e){
            e.printStackTrace(); // Handle SQL exceptions
        }
        
        return interestRate;
    }
    
    // Method to calculate interest based on balance, interest rate, and period
    public static double calculateInterest(int user_id, int period, double interestRate){
        double interest = 0;
        double balance = getBalance(user_id); // Retrieve user's current balance
            
        switch (period){
            case 1:
                interest = balance * interestRate / 365; // Daily interest calculation
                break;

            case 2:
                interest = balance * interestRate / 12; // Monthly interest calculation
                break;

            case 3:
                interest = balance * interestRate; // Annual interest calculation
                break;

        }
        return interest;
    }
    
    // Method to retrieve the user's current balance from the database
    public static double getBalance(int user_id){
        double balance = 0;
        
        try (Connection connection = DB.Connect()){ // Connect to the database
            String query = "SELECT current_amount FROM balance WHERE user_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, user_id);
            
            ResultSet rs = pstmt.executeQuery(); // Execute query and retrieve balance
            
            while (rs.next()){
                balance = rs.getDouble("current_amount");
            }
            
        } catch (SQLException e){
            e.printStackTrace(); // Handle SQL exceptions
        }
        
        return balance;
    }
}
