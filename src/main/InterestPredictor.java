//This program will calculate the monthly interest based on the amount deposited (entered by user) and stores it in a database.

package interestpredictor;

import java.sql.*;
import java.util.Scanner;

public class InterestPredictor {
    
    private static String url = "jdbc:mysql://localhost:3306/ledgerdb";
    private static String user = "root";
    private static String password = "fir";
    static Scanner in = new Scanner(System.in);
    
    public static void main(String[] args) {
        int user_id = 1;
        int choice;
        
        do {
            System.out.println("1. Deposit");
            System.out.println("2. Exit");
            choice = in.nextInt();
            System.out.println();
            switch(choice){
                case 1:
                    double interest = calculateInterest(user_id);
                    System.out.printf("Monthly interest: %.2f", interest);
                    System.out.println();
                    break;
                    
                case 2:
                    break;
                    
                default:
                    System.out.println("Invalid choice.");
                    break;
            }
            
        } while(choice != 2);
    }
    
    public static int chooseBank(){
        int bankID = 0;
        
        System.out.println("Choose bank:");
        System.out.println("1. RHB");
        System.out.println("2. Maybank");
        System.out.println("3. Hong Leong");
        System.out.println("4. Alliance");
        System.out.println("5. AmBank");
        System.out.println("6. Standard Chartered");
        int bank = in.nextInt();
        
        switch (bank){
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
                break;
                
        }
        return bankID;
    }
    
    public static double interestRate(int bankID){
        double rate = 0;
        
        switch (bankID){
            case 1:
                rate = 0.026;
                break;
                
            case 2:
                rate = 0.025;
                break;
                
            case 3:
                rate = 0.023;
                break;
                
            case 4:
                rate = 0.0285;
                break;
                
            case 5:
                rate = 0.0255;
                break;
                
            case 6:
                rate = 0.0265;
                break;
                
            default:
                break;
                
        }
        return rate;
    }
    
    public static double calculateInterest(int user_id){
        double interest = 0;
        
        try(Connection connection = DriverManager.getConnection(url, user, password)){
            double interestRate = interestRate(chooseBank());
            
            System.out.print("\nEnter deposit amount: ");
            double deposit = in.nextDouble();
            
            PreparedStatement pstmt = connection.prepareStatement("UPDATE interest SET deposit = ? WHERE user_id = ?");
            pstmt.setDouble(2, user_id);
            pstmt.setDouble(1, deposit);
            pstmt.executeUpdate();
            
            interest = deposit * interestRate / 12;
            PreparedStatement pstmt2 = connection.prepareStatement("UPDATE interest SET interest = ? WHERE user_id = ?");
            pstmt2.setInt(2, user_id);
            pstmt2.setDouble(1, interest);
            pstmt2.executeUpdate();
            
            Statement stmt = connection.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM interest");
            while(rs.next()){
                interest = rs.getDouble("interest");
            }
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return interest;
    }
}
    
    

