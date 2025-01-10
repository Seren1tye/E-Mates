import java.sql.*;
import java.util.Scanner;
import java.util.*;
import java.util.concurrent.*;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;

public class Savings {

    // Method to activate savings
    public static void activateSavings(int userId) {
        if(SavingTransfer.verify(userId))
            return;
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
                    System.out.print("Please enter the percentage you wish to deduct from the next debit (1-99): ");
                    while (!scanner.hasNextDouble()) {
                        System.out.println("Invalid input. Please enter a number between 1 and 99.");
                        scanner.next();
                    }
                    percentage = scanner.nextDouble();
                    scanner.nextLine();

                    if (percentage <= 0 || percentage >= 100) {
                        System.out.println("Invalid percentage. Please enter a valid percentage between 1 and 99.");
                    }
                } while (percentage <= 0 || percentage >= 100);
                
                // Calculate transfer_date
                LocalDate now = LocalDate.now();
                LocalDate transferDate = now.with(TemporalAdjusters.lastDayOfMonth()); // Last day of current month
                
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
                
                // Set initial transfer date to last day of current month
                String savingsQuery = """
                    INSERT INTO Savings (user_id, amount, transfer_date, activation_date)
                    VALUES (?, 0, ?, CURDATE())
                    ON DUPLICATE KEY UPDATE transfer_date = ?;
                """;

                try (PreparedStatement statement = DB.Connect().prepareStatement(savingsQuery)) {
                    statement.setInt(1, userId);
                    statement.setDate(2, java.sql.Date.valueOf(transferDate));  // Set transfer date to last day of current month
                    statement.setDate(3, java.sql.Date.valueOf(transferDate));  // Update transfer date if exists
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
            // Calculate transfer date as the last day of the current month
            LocalDate now = LocalDate.now();
            LocalDate transferDate = now.with(TemporalAdjusters.lastDayOfMonth()); // Last day of current month

            // SQL query to insert/update savings
            String updateSavingsQuery = """
                INSERT INTO Savings (user_id, amount, transfer_date) 
                VALUES (?, ?, ?) 
                ON DUPLICATE KEY UPDATE 
                    amount = amount + ?, 
                    transfer_date = IFNULL(transfer_date, ?);
            """;

            // Prepare and execute the statement
            try (PreparedStatement statement = DB.Connect().prepareStatement(updateSavingsQuery)) {
                statement.setInt(1, userId);
                statement.setDouble(2, savingsAmount);
                statement.setDate(3, java.sql.Date.valueOf(transferDate));  // Set transfer date to last day of current month
                statement.setDouble(4, savingsAmount);
                statement.setDate(5, java.sql.Date.valueOf(transferDate));  // Update transfer date if exists
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
            String savingsQuery = "SELECT amount, transfer_date FROM Savings WHERE user_id = ?";
            try (PreparedStatement statement = DB.Connect().prepareStatement(savingsQuery)) {
                statement.setInt(1, userId);
                ResultSet rs = statement.executeQuery();

                if (rs.next()) {
                    double totalSavings = rs.getDouble("amount");
                    java.sql.Date transferDate = rs.getDate("transfer_date"); // Use java.sql.Date here
                    java.util.Date currentDate = new java.util.Date(); // Use java.util.Date here

                    // Calculate the last day of the current month
                    LocalDate now = LocalDate.now();
                    LocalDate lastDayOfMonth = now.with(TemporalAdjusters.lastDayOfMonth());

                    // Check if transfer date has been reached
                    if (!currentDate.before(java.sql.Date.valueOf(lastDayOfMonth))) {
                        if (totalSavings > 0) {
                            // Update user's balance
                            String updateBalanceQuery = "UPDATE Balance SET current_amount = current_amount + ? WHERE user_id = ?";
                            try (PreparedStatement balanceStmt = DB.Connect().prepareStatement(updateBalanceQuery)) {
                                balanceStmt.setDouble(1, totalSavings);
                                balanceStmt.setInt(2, userId);
                                balanceStmt.executeUpdate();
                            }

                            // Record the transaction
                            String recordTransactionQuery = """
                                INSERT INTO Transactions (user_id, description, credit, balance, transaction_type)
                                VALUES (?, 'Savings Transfer', ?, ?, 'credit');
                            """;
                            try (PreparedStatement transactionStmt = DB.Connect().prepareStatement(recordTransactionQuery)) {
                                transactionStmt.setInt(1, userId);
                                transactionStmt.setDouble(2, totalSavings);
                                transactionStmt.setDouble(3, totalSavings); // Assuming balance is updated immediately
                                transactionStmt.executeUpdate();
                            }

                            // Reset savings amount and update transfer_date
                            String resetSavingsQuery = """
                                UPDATE Savings 
                                SET amount = 0, transfer_date = ?
                                WHERE user_id = ?;
                            """;
                            try (PreparedStatement resetStmt = DB.Connect().prepareStatement(resetSavingsQuery)) {
                                resetStmt.setDate(1, java.sql.Date.valueOf(lastDayOfMonth)); // Set the last day of current month
                                resetStmt.setInt(2, userId);
                                resetStmt.executeUpdate();
                                System.out.println("Savings of " + totalSavings + " successfully transferred to your balance.");
                            }
                        } else {
                            System.out.println("No savings to transfer.");
                        }
                    } else {
                        System.out.println("Transfer date has not been reached yet.");
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred during the savings transfer: " + e.getMessage());
        }
    }
}

    // This is done on SQL
    
    // Method to transfer the amount in savings into the user's balance at the end of the month
//    public static void monthlySavingsTransfer() {
//        // Executor service to execute tasks on a schedule
//        ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
//
//        // Calculate the delay until the first day of the next month at midnight
//        LocalDate now = LocalDate.now();
//        LocalDate nextMonthFirstDay = now.plusMonths(1).withDayOfMonth(1);
//        long initialDelay = java.time.Duration.between(now.atStartOfDay(), nextMonthFirstDay.atStartOfDay()).toMillis();
//
//        // Schedule the task to run at the first day of every month at midnight
//        scheduler.scheduleAtFixedRate(() -> {
//            try {
//                System.out.println("Scheduled task started at: " + new java.util.Date());
//                transferSavingsToBalance(); // Ensure this method is properly transferring savings
//                System.out.println("Scheduled task completed at: " + new java.util.Date());
//            } catch (Exception e) {
//                System.out.println("Error during scheduled savings transfer: " + e.getMessage());
//                e.printStackTrace();
//            }
//        }, initialDelay, TimeUnit.DAYS.toMillis(1), TimeUnit.MILLISECONDS); // Run daily for better accuracy
//
//        // Prevent the scheduler from shutting down immediately
//        try {
//            Thread.sleep(Long.MAX_VALUE);
//        } catch (InterruptedException e) {
//            System.out.println("Scheduler interrupted: " + e.getMessage());
//        }
//    }


    // method to transfer savings of all users to their respective balances
//    private static void transferSavingsToBalance() {
//        try {
//            String query = "SELECT user_id FROM Users";
//            try (PreparedStatement statement = DB.Connect().prepareStatement(query);
//                 ResultSet rs = statement.executeQuery()) {
//
//                while (rs.next()) {
//                    int userId = rs.getInt("user_id");
//                    transferToBalance(userId); // Transfers the savings for each user
//                }
//            }
//        } catch (SQLException e) {
//            System.out.println("Error fetching users for savings transfer: " + e.getMessage());
//        }
//    }
