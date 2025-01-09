import java.sql.*;
import java.util.Scanner;
import java.util.*;
import java.util.concurrent.*;

// CHANGE LAPTOP DATE
public class Savings {

    // Method to activate savings
    public static void activateSavings(int userId) {
        Scanner scanner = new Scanner(System.in);
        try {
            System.out.println("== Savings Activation ==");

            String option;
            // do-while loop to only accept the inputs "Y" or "N"
            do {
                System.out.print("Are you sure you want to activate savings? (Y/N): ");
                option = scanner.nextLine();

                // makes it not case-sensitive
                if (!option.equalsIgnoreCase("Y") && !option.equalsIgnoreCase("N")) {
                    System.out.println("Invalid input. Please enter 'Y' or 'N'.");
                }
            } while (!option.equalsIgnoreCase("Y") && !option.equalsIgnoreCase("N"));

            if (option.equalsIgnoreCase("Y")) {
                double percentage;
                // do-while loop to accept a valid percentage in between 1 - 100
                do {
                    System.out.print("Please enter the percentage you wish to deduct from the next debit (1-100): ");
                    while (!scanner.hasNextDouble()) {
                        System.out.println("Invalid input. Please enter a number between 1 and 100.");
                        scanner.next();
                    }
                    percentage = scanner.nextDouble();
                    scanner.nextLine();

                    if (percentage <= 0 || percentage >= 100) {
                        System.out.println("Invalid percentage. Please enter a valid percentage between 1 and 100.");
                    }
                } while (percentage <= 0 || percentage >= 100);

                // Query for SQL
                String savingsPercentageQuery = """
                    INSERT INTO SavingsSettings (user_id, percentage)
                    VALUES (?, ?)
                    ON DUPLICATE KEY UPDATE percentage = ?;
                """;

                try (PreparedStatement statement = DB.Connect().prepareStatement(savingsPercentageQuery)) {
                    statement.setInt(1, userId);
                    statement.setDouble(2, percentage);
                    statement.setDouble(3, percentage);
                    statement.executeUpdate();
                    System.out.println("Savings settings updated successfully!");
                }
            
            // If the user types "N" then they disable the savings function
            // Percentage is set to 0 so their debit doesn't get deducted
            } else if (option.equalsIgnoreCase("N")) {
                String disableSavingsQuery = """
                    INSERT INTO SavingsSettings (user_id, percentage)
                    VALUES (?, 0)
                    ON DUPLICATE KEY UPDATE percentage = 0;
                """;

                try (PreparedStatement statement = DB.Connect().prepareStatement(disableSavingsQuery)) {
                    statement.setInt(1, userId);
                    statement.executeUpdate();
                    System.out.println("Savings functionality has been disabled.");
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred while activating savings: " + e.getMessage());
        }
    }


    // Method to get savings percentage
    public static double getSavingPercentage(int userId) {
        // initial percentage is at 0
        double percentage = 0.0;
        try {
            String query = "SELECT percentage FROM SavingsSettings WHERE user_id = ?";

            try (PreparedStatement statement = DB.Connect().prepareStatement(query)) {
                statement.setInt(1, userId);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    percentage = rs.getDouble("percentage");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
        }
        return percentage;
    }

    // Method to calculate and save a percentage of the debit amount to savings
    public static double saveDebit(int userId, double savingsAmount) {
        try {
            String updateSavingsQuery = """
                INSERT INTO Savings (user_id, amount)
                VALUES (?, ?)
                ON DUPLICATE KEY UPDATE amount = amount + ?;
            """;

            try (PreparedStatement statement = DB.Connect().prepareStatement(updateSavingsQuery)) {
                statement.setInt(1, userId);
                statement.setDouble(2, savingsAmount);
                statement.setDouble(3, savingsAmount);
                statement.executeUpdate();
                System.out.println("Savings of " + savingsAmount + " successfully added.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while saving debit: " + e.getMessage());
        }
        return savingsAmount;
    }

    // Method to view total savings
    public static double viewSavings(int userId) {
        double totalSavings = 0.0;
        try {
            // SUM is used to add up total_savings
            String query = "SELECT SUM(amount) AS total_savings FROM Savings WHERE user_id = ?";

            try (PreparedStatement statement = DB.Connect().prepareStatement(query)) {
                statement.setInt(1, userId);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    totalSavings = rs.getDouble("total_savings");
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while fetching savings: " + e.getMessage());
        }
        return totalSavings;
    }

    // Method to transfer savings to the balance at the end of the month
    public static void transferToBalance(int userId) {
        try {
            // Get the total savings
            double totalSavings = viewSavings(userId);

            if (totalSavings > 0) {
                // Update the user's balance by adding the savings
                String updateBalanceQuery = "UPDATE Balance SET current_amount = current_amount + ? WHERE user_id = ?";

                try (PreparedStatement balanceStmt = DB.Connect().prepareStatement(updateBalanceQuery)) {
                    balanceStmt.setDouble(1, totalSavings);
                    balanceStmt.setInt(2, userId);
                    balanceStmt.executeUpdate();
                }

                // Reset the user's savings to 0
                String resetSavingsQuery = "DELETE FROM Savings WHERE user_id = ?";

                try (PreparedStatement savingsStmt = DB.Connect().prepareStatement(resetSavingsQuery)) {
                    savingsStmt.setInt(1, userId);
                    savingsStmt.executeUpdate();
                }

                System.out.println("Savings of " + totalSavings + " successfully transferred to your balance.");
            } else {
                System.out.println("No savings to transfer.");
            }
        } catch (SQLException e) {
            System.out.println("An error occurred during the savings transfer: " + e.getMessage());
        }
    }

    // Method to transfer the amount in savings into the user's balance at the end of the month
    public static void monthlySavingsTransfer() {
        // Executor service to execute tasks on a schedule
        ScheduledExecutorService scheduler = Executors.newScheduledThreadPool(1);

        // Calculate the delay until the first day of the next month at midnight
        Calendar now = Calendar.getInstance();
        Calendar nextMonth = Calendar.getInstance();
        nextMonth.add(Calendar.MONTH, 1); // Move to the next month
        nextMonth.set(Calendar.DAY_OF_MONTH, 1); // Set to the first day
        nextMonth.set(Calendar.HOUR_OF_DAY, 0); // Set to midnight
        nextMonth.set(Calendar.MINUTE, 0);
        nextMonth.set(Calendar.SECOND, 0);
        
        // Calculate the initial delay until the next month's first day at midnight
        long initialDelay = nextMonth.getTimeInMillis() - now.getTimeInMillis();
        System.out.println("Initial delay calculated: " + initialDelay + " ms");

        // Schedule the task to run at the first day of every month at midnight
        scheduler.scheduleAtFixedRate(() -> {
            try {
                System.out.println("Scheduled task started at: " + new java.util.Date());
                transferSavingsToBalance();
                System.out.println("Scheduled task completed at: " + new java.util.Date());
            } catch (Exception e) {
                System.out.println("Error during scheduled savings transfer: " + e.getMessage());
                e.printStackTrace();
            }
        }, initialDelay, TimeUnit.DAYS.toMillis(30), TimeUnit.MILLISECONDS);
    }

    // method to transfer savings of all users to their respective balances
    private static void transferSavingsToBalance() {
        try {
            String query = "SELECT user_id FROM Users";
            try (PreparedStatement statement = DB.Connect().prepareStatement(query);
                 ResultSet rs = statement.executeQuery()) {

                while (rs.next()) {
                    int userId = rs.getInt("user_id");
                    transferToBalance(userId);
                }
            }
        } catch (SQLException e) {
            System.out.println("Error fetching users for savings transfer: " + e.getMessage());
        }
    }
}
