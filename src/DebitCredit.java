import java.sql.*;
import java.util.Scanner;

public class DebitCredit {

    public static double getBalance(int userId) {
        double balance = 0.0;
        Connection connection = DB.Connect();
        if (connection == null) {
            System.out.println("Failed to connect to the database.");
            return balance;
        }

        String sql = "SELECT current_amount AS balance FROM Balance WHERE user_id = ?";
        try (PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    balance = result.getDouble("balance");
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching balance: " + e.getMessage());
        }
        return balance;
    }

    private static void updateBalance(int userId, double newBalance) {
        String sql = "UPDATE Balance SET current_amount = ? WHERE user_id = ?";
        try (Connection connection = DB.Connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDouble(1, newBalance);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Error updating balance: " + e.getMessage());
        }
    }

    private static void insertTransaction(int userId, double amount, String description, String type) {
        String sql = "INSERT INTO Transactions (user_id, description, debit, credit, balance, transaction_type) " +
                     "VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection connection = DB.Connect(); PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setInt(1, userId);
            statement.setString(2, description);

            if (type.equalsIgnoreCase("Debit")) {
                statement.setDouble(3, amount);
                statement.setDouble(4, 0.0);
            } else {
                statement.setDouble(3, 0.0);
                statement.setDouble(4, amount);
            }

            double currentBalance = getBalance(userId);
            double newBalance = type.equalsIgnoreCase("Credit") ? currentBalance - amount : currentBalance + amount;
            statement.setDouble(5, newBalance);
            statement.setString(6, type);

            statement.executeUpdate();

            updateBalance(userId, newBalance);
        } catch (SQLException e) {
            System.out.println("Error inserting into Transactions: " + e.getMessage());
        }
    }

    public static void debitAmount(int userId, Scanner read) {
        double amount;
        System.out.println("\n=== DEBIT TRANSACTION ===");

        while (true) {
            System.out.print("Enter amount to debit: ");
            amount = read.nextDouble();
            read.nextLine();

            if (amount == 0 || amount < -1) {
                System.out.println("Invalid amount. Please enter a positive value.\n");
            } else if (amount > 500000) {
                System.out.println("Invalid amount. 500,000 is the transaction limit.\n");
            } else if (amount == -1) {
                return;
            } else {
                break;
            }
        }

        System.out.print("Enter a description: ");
        String description = read.nextLine();

        // This is to deduct the debit inputted by the user if savings are enabled
        double savingsPercentage = Savings.getSavingPercentage(userId);

        if (savingsPercentage > 0) {
            double savingsAmount = amount * savingsPercentage / 100;
            Savings.saveDebit(userId, savingsAmount);
            amount = amount - savingsAmount;
        }

        insertTransaction(userId, amount, description, "Debit");
        System.out.println("Debit transaction successful!");
    }

    public static void creditAmount(int userId, Scanner read) {
        double amount;
        System.out.println("\n=== CREDIT TRANSACTION ===");

        while (true) {
            System.out.print("Enter amount to credit: ");
            amount = read.nextDouble();
            read.nextLine();

            double currentBalance = getBalance(userId);

            if (amount == 0 || amount < -1) {
                System.out.println("Invalid amount. Please enter a positive value.\n");
            } else if (amount > 500000) {
                System.out.println("Invalid amount. 500,000 is the transaction limit.\n");
            } else if (amount > currentBalance) {
                System.out.println("Insufficient funds. Your current balance is: " + currentBalance + "\n");
            } else if (amount == -1) {
                return;
            } else {
                break;
            }
        }

        System.out.print("Enter a description: ");
        String description = read.nextLine();

        insertTransaction(userId, amount, description, "Credit");
        System.out.println("Credit transaction successful!\n");
    }
}