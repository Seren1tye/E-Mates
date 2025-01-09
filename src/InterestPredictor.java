//This program will calculate the interest(daily/monthly/annually) based on the current balance and displays it.

import java.sql.*;
import java.util.Scanner;

public class InterestPredictor {
    
    private static Scanner in = new Scanner(System.in);
    
    public static void mainInterest(int user_id) {
        String input;
        int choice = 0;
        double interestRate = interestRate(chooseBank());
        
        do {
            System.out.println("\nChoose interest period:");
            System.out.println("1. Daily");
            System.out.println("2. Monthly");
            System.out.println("3. Annually");
            System.out.println("4. Choose bank");
            System.out.println("5. Exit");
            System.out.print("\n> ");
            
            input = in.nextLine();   //If input is not a number, exception message will be output
            try{
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Input is not a number. " + e.getMessage());
            }
            System.out.println();

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
                    interestRate = interestRate(chooseBank());
                    break;
                    
                case 5:
                    break;

                default:
                    // if number other than 1-5 is entered, print message. If input is not a number, message is not printed.
                    if (Character.isDigit(input.charAt(0))) {
                        System.out.println("Invalid choice.");
                    }
                    break;

            }

        } while (choice != 5);
    }
    
    //Method to choose bank ID
    public static int chooseBank(){
        int bankID = 0;
        String input;
        int choice = 0;
        boolean repeat;
        
        do {
            repeat = false;
            System.out.println("\nChoose bank:");
            System.out.println("1. RHB");
            System.out.println("2. Maybank");
            System.out.println("3. Hong Leong");
            System.out.println("4. Alliance");
            System.out.println("5. AmBank");
            System.out.println("6. Standard Chartered");
            System.out.print("\n> ");
            
            input = in.nextLine();  //If input is not a number, exception message will be output
            try {
                choice = Integer.parseInt(input);
            } catch (NumberFormatException e) {
                System.out.print("Input is not a number. " + e.getMessage());
            }
            System.out.println();

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
                    repeat = true;
                    break;

            }    
        } while (repeat);
        
        return bankID;
    }
    
    //Method to choose interest rate for bank
    public static double interestRate(int bankID){
        double interestRate = 0;
        
        try (Connection connection = DB.Connect()){
            String query = "SELECT interest_rate FROM bankDetails WHERE bank_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setDouble(1, bankID);
            
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()){
                interestRate = rs.getDouble("interest_rate");
            }
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return interestRate;
    }
    
    //Method to calculate interest from the balance and interest rate (daily/monthly/annually)
    public static double calculateInterest(int user_id, int period, double interestRate){
        double interest = 0;
        double balance = getBalance(user_id);
            
        switch (period){
            case 1:
                interest = balance * interestRate / 365;
                break;

            case 2:
                interest = balance * interestRate / 12;
                break;

            case 3:
                interest = balance * interestRate;
                break;

        }
        return interest;
    }
    
    //Method to retrieve  current balance amount from database
    public static double getBalance(int user_id){
        double balance = 0;
        
        try (Connection connection = DB.Connect()){
            String query = "SELECT current_amount FROM balance WHERE user_id = ?";
            PreparedStatement pstmt = connection.prepareStatement(query);
            pstmt.setInt(1, user_id);
            
            ResultSet rs = pstmt.executeQuery();
            
            while (rs.next()){
                balance = rs.getDouble("current_amount");
            }
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return balance;
    }
}
    
    
