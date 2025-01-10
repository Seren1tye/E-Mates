import java.sql.*;
import java.util.Scanner;

public class DebitCredit {

    //Method to fetch the current balance of a user from the database
    public static double getBalance(int userId) {
        double balance = 0.0;
        Connection connection = DB.Connect(); //Establish a connection to the database
        if (connection == null) {
            System.out.println("Failed to connect to the database.");
            return balance; //Returns 0 if the connection fails
        }

        //SQL query to fetch the current balance for a specific user
        String sql = "SELECT current_amount AS balance FROM Balance WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId); //Set the user ID parameter in the query
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    balance = result.getDouble("balance"); //Retrieve the balance from the result set
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching balance: " + e.getMessage()); //Handle SQL exceptions
        }
        return balance; //Return the fetched balance
    }

    //Method to update the balance of a user in the database
    private static void updateBalance(int userId, double newBalance) {
        //SQL query to update the current balance for a specific user
        String sql = "UPDATE Balance SET current_amount = ? WHERE user_id = ?";
        try (Connection connection = DB.Connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, newBalance); //Set the new balance
            statement.setInt(2, userId); //Set the user ID
            statement.executeUpdate(); //Execute the update query
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage()); //Handle SQL exceptions
        }
    }

    //Method to insert a new transaction record into the Transactions table
    private static void insertTransaction(int userId, double amount, String description, String type) {
        //SQL query to insert a new transaction
        String sql = "INSERT INTO Transactions (user_id, description, debit, credit, balance, transaction_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DB.Connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId); //Set the user ID
            statement.setString(2, description); //Set the transaction description

            //Set debit or credit amount based on the transaction type
            if (type.equalsIgnoreCase("Debit")) {
                statement.setDouble(3, amount); //Set debit amount
                statement.setDouble(4, 0.0); //Credit amount is 0 for debit transactions
            } else {
                statement.setDouble(3, 0.0); //Debit amount is 0 for credit transactions
                statement.setDouble(4, amount); //Set credit amount
            }

            //Calculate the new balance after the transaction
            double currentBalance = getBalance(userId);
            double newBalance = type.equalsIgnoreCase("Credit") ? currentBalance - amount : currentBalance + amount;
            statement.setDouble(5, newBalance); //Set the new balance
            statement.setString(6, type); //Set the transaction type

            statement.executeUpdate(); //Execute the insert query

            updateBalance(userId, newBalance); //Update the user's balance in the Balance table
        } catch (SQLException e) {
            System.out.println("Error inserting into Transactions: " + e.getMessage()); //Handle SQL exceptions
        }
    }

    //Method to handle debit transactions
    public static void debitAmount(int userId, Scanner read) {
        double amount;
        System.out.println("\n=== DEBIT TRANSACTION ===");

        //Loop to validate the debit amount entered by the user
        while (true) {
            System.out.print("Enter amount to debit: ");
            amount = read.nextDouble();
            read.nextLine();

            if (amount == 0 || amount < -1) {
                System.out.println("\nInvalid amount. Please enter a positive value.\n");
            } else if (amount > 500000) {
                System.out.println("\nInvalid amount. 500,000 is the transaction limit.\n");
            } else if (amount == -1) {
                return; //Exit the method if the user enters -1
            } else {
                break; //Exit the loop if the amount is valid
            }
        }

        System.out.print("Enter a description: ");
        String description = read.nextLine();

        //Check if savings are enabled and deduct the savings amount from the debit
        double savingsPercentage = Savings.getSavingPercentage(userId);
        if (savingsPercentage > 0) {
            double savingsAmount = amount * savingsPercentage / 100;
            Savings.saveDebit(userId, savingsAmount); //Save the savings amount
            amount = amount - savingsAmount; //Deduct the savings amount from the debit
        }

        insertTransaction(userId, amount, description, "Debit"); //Insert the debit transaction
        System.out.println("\nDebit transaction successful!\n");
    }

    //Method to handle credit transactions
    public static void creditAmount(int userId, Scanner read) {
        double amount;
        System.out.println("\n=== CREDIT TRANSACTION ===");

        //Loop to validate the credit amount entered by the user
        while (true) {
            System.out.print("Enter amount to credit: ");
            amount = read.nextDouble();
            read.nextLine();

            double currentBalance = getBalance(userId); //Get the current balance of the user

            if (amount == 0 || amount < -1) {
                System.out.println("\nInvalid amount. Please enter a positive value.\n");
            } else if (amount > 500000) {
                System.out.println("\nInvalid amount. 500,000 is the transaction limit.\n");
            } else if (amount > currentBalance) {
                System.out.println("\nInsufficient funds. Your current balance is: " + currentBalance + "\n");
            } else if (amount == -1) {
                return; //Exit the method if the user enters -1
            } else {
                break; //Exit the loop if the amount is valid
            }
        }

        System.out.print("Enter a description: ");
        String description = read.nextLine();

        insertTransaction(userId, amount, description, "Credit"); //Insert the credit transaction
        System.out.println("\nCredit transaction successful!\n");
    }
}
