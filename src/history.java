import java.io.FileWriter;
import java.io.IOException;
import java.sql.*;
import java.util.Scanner;

import java.util.ArrayList;
import javax.swing.JFrame;
import org.knowm.xchart.CategoryChart;
import org.knowm.xchart.CategoryChartBuilder;
import org.knowm.xchart.XChartPanel;

public class history {
    public static void mainHistory(int userId, String userName) {
        try (Connection conn = DB.Connect()) {
            if (conn == null) {
                System.out.println("Unable to establish a database connection. Exiting...");
                return;
            }

            Scanner scanner = new Scanner(System.in);

            while (true) {
                System.out.println("\n== Transaction Menu ==");
                System.out.println("1. View Transaction History");
                System.out.println("2. Export to CSV");
                System.out.println("3. Data Visualization");
                System.out.println("4. Exit");
                System.out.print("Choose an option: ");
                int choice = scanner.nextInt();
                scanner.nextLine(); // Consume newline character

                switch (choice) {
                    case 1:
                        viewAndManageTransactionHistory(userId, scanner);
                        break;
                    case 2:
                        exportToCSV(userId, userName);
                        break;
                    case 3:
                        dataVisualisation(userId);
                        break;
                    case 4:
                        System.out.println("Exiting...");
                        return;
                    default:
                        System.out.println("Invalid option. Try again.");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private static void viewAndManageTransactionHistory(int userId, Scanner scanner) throws SQLException {
        String baseQuery = "SELECT date, description, debit, credit, balance FROM Transactions WHERE user_id = ?";
        String currentQuery = baseQuery;

        System.out.println("\n== Transaction History ==");
        displayTransactions(currentQuery, userId);

        while (true) {
            System.out.println("\n== Transaction Options ==");
            System.out.println("1. Apply Filtering");
            System.out.println("2. Apply Sorting");
            System.out.println("3. Back to Main Menu");
            System.out.print("Choose an option: ");
            int choice = scanner.nextInt();
            scanner.nextLine(); // Consume newline character

            switch (choice) {
                case 1:
                    currentQuery = applyFilter(baseQuery, scanner); // Always start filtering from the original query
                    displayTransactions(currentQuery, userId);
                    break;
                case 2:
                    currentQuery = applySort(baseQuery, scanner); // Sorting also starts from the original query
                    displayTransactions(currentQuery, userId);
                    break;
                case 3:
                    return; // Back to main menu
                default:
                    System.out.println("Invalid option. Try again.");
            }
        }
    }

    private static void displayTransactions(String query, int userId) throws SQLException {
        try (Connection conn = DB.Connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery()) {
                System.out.printf("%-20s %-20s %-10s %-10s %-10s%n", "Date", "Description", "Debit", "Credit", "Balance");
                System.out.println("-----------------------------------------------------------------------");
                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;
                    String date = rs.getString("date");
                    String description = rs.getString("description");
                    double debit = rs.getDouble("debit");
                    double credit = rs.getDouble("credit");
                    double balance = rs.getDouble("balance");

                    System.out.printf("%-20s %-20s %-10.2f %-10.2f %-10.2f%n",
                            date, description, debit, credit, balance);
                }

                if (!hasData) {
                    System.out.println("No transaction history found for the given criteria.");
                }
            }
        }
    }

    private static String applyFilter(String baseQuery, Scanner scanner) {
        StringBuilder queryBuilder = new StringBuilder(baseQuery);

        System.out.println("\n== Filtering Options ==");
        System.out.println("1. Date Range");
        System.out.println("2. Transaction Type (debit/credit)");
        System.out.println("3. Amount Range");
        System.out.print("Choose a filtering option: ");
        int filterChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        switch (filterChoice) {
            case 1:
                System.out.print("Enter start date (YYYY-MM-DD): ");
                String startDate = scanner.nextLine();
                System.out.print("Enter end date (YYYY-MM-DD): ");
                String endDate = scanner.nextLine();
                queryBuilder.append(" AND date BETWEEN '").append(startDate).append("' AND '").append(endDate).append("'");
                break;
            case 2:
                System.out.print("Enter transaction type (debit/credit): ");
                String type = scanner.nextLine();
                queryBuilder.append(" AND transaction_type = '").append(type).append("'");
                break;
            case 3:
                System.out.print("Enter minimum amount: ");
                double minAmount = scanner.nextDouble();
                System.out.print("Enter maximum amount: ");
                double maxAmount = scanner.nextDouble();
                scanner.nextLine(); // Consume newline character
                queryBuilder.append(" AND ((debit BETWEEN ").append(minAmount).append(" AND ").append(maxAmount)
                            .append(") OR (credit BETWEEN ").append(minAmount).append(" AND ").append(maxAmount).append("))");
                break;
            default:
                System.out.println("Invalid filtering option. No filter applied.");
        }

        return queryBuilder.toString();
    }

    private static String applySort(String baseQuery, Scanner scanner) {
        StringBuilder queryBuilder = new StringBuilder(baseQuery);

        System.out.println("\n== Sorting Options ==");
        System.out.println("1. By Date (Newest to Oldest)");
        System.out.println("2. By Date (Oldest to Newest)");
        System.out.println("3. By Amount (Highest to Lowest)");
        System.out.println("4. By Amount (Lowest to Highest)");
        System.out.print("Choose a sorting option: ");
        int sortChoice = scanner.nextInt();
        scanner.nextLine(); // Consume newline character

        switch (sortChoice) {
            case 1:
                queryBuilder.append(" ORDER BY date DESC");
                break;
            case 2:
                queryBuilder.append(" ORDER BY date ASC");
                break;
            case 3:
                queryBuilder.append(" ORDER BY GREATEST(debit, credit) DESC");
                break;
            case 4:
                queryBuilder.append(" ORDER BY GREATEST(debit, credit) ASC");
                break;
            default:
                System.out.println("Invalid sorting option. No sorting applied.");
        }

        return queryBuilder.toString();
    }

    private static void exportToCSV(int userId, String userName) throws SQLException {
        String query = "SELECT date, description, debit, credit, balance FROM Transactions WHERE user_id = ?";
        try (Connection conn = DB.Connect();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setInt(1, userId);

            try (ResultSet rs = stmt.executeQuery();
                 FileWriter writer = new FileWriter("TransactionHistory_" + userName + ".csv")) {

                // Write CSV headers
                writer.append("Date,Description,Debit,Credit,Balance\n");

                boolean hasData = false;

                // Write data to CSV
                while (rs.next()) {
                    hasData = true;
                    String date = rs.getString("date");
                    String description = rs.getString("description");
                    double debit = rs.getDouble("debit");
                    double credit = rs.getDouble("credit");
                    double balance = rs.getDouble("balance");

                    writer.append(String.format("%s,%s,%.2f,%.2f,%.2f\n",
                            date, description, debit, credit, balance));
                }

                if (hasData) {
                    System.out.println("Transaction history exported to TransactionHistory_" + userName + ".csv");
                } else {
                    System.out.println("No transaction history found for the given User ID.");
                }
            } catch (IOException e) {
                System.out.println("Error writing to CSV file: " + e.getMessage());
            }
        }
    }
    
    private static void dataVisualisation(int userId) {
        Scanner scanner = new Scanner(System.in);
        int choice;

        do {
            System.out.println("\nWhat do you want to see?");
            System.out.println("1. Spending Trends");
            System.out.println("2. Savings Growth");
            System.out.println("3. Loan Repayments");
            System.out.println("4. Exit");
            System.out.print("\n> ");
            // Get user input
            while (!scanner.hasNextInt()) {
                System.out.println("Invalid input.");
                scanner.next(); // Consume invalid input
            }
            
            choice = scanner.nextInt();

            // Handle user choice
            switch (choice) {
                case 1:
                    displaySpendingTrends(userId);
                    break;
                case 2:
                    displaySavingsGrowth(userId);
                    break;
                case 3:
                    displayLoanRepayments(userId);
                    break;
                case 4:
                    System.out.println("Exiting...");
                    break;
                default:
                    System.out.println("Invalid input.");
            }
        } while (choice != 4);
    }
    
    private static void displaySpendingTrends(int userId){
        ArrayList<String> dates = new ArrayList<>();
        ArrayList<Double> debitValue = new ArrayList<>();
        ArrayList<Double> creditValue = new ArrayList<>();
        
        String query = "SELECT date, debit, credit FROM Transactions WHERE user_id = ? ORDER BY date ASC";
        
        try (Connection conn = DB.Connect();
         PreparedStatement statement = conn.prepareStatement(query)){
            
            statement.setInt(1, userId);
            
            try (ResultSet rs = statement.executeQuery()){
                boolean hasData = false;
                
                while (rs.next()){
                    hasData = true;
                    String date = rs.getString("date");
                    double debit = rs.getDouble("debit");
                    double credit = rs.getDouble("credit");
                    
                    dates.add(date);
                    debitValue.add(debit);
                    creditValue.add(credit);
                }
                if (!hasData){
                    System.out.println("No transaction data available to display");
                    return;
                }
            }
            
        } catch (SQLException e){
            System.out.println("An error occured: " + e.getMessage());
            return;
        }
        
        // Create a bar chart using XChart
        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Spending Trends")
                .xAxisTitle("Date")
                .yAxisTitle("Amount")
                .build();
        
        // Add series to the chart
        chart.addSeries("Debit", dates, debitValue);
        chart.addSeries("Credit", dates, creditValue);
        
        // Display the chart in a JFrame
        JFrame frame = new JFrame("Spending Trends");
        frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE); // Only hide the window on close
        frame.add(new XChartPanel<>(chart)); // Embed the chart panel in the JFrame
        frame.pack();
        frame.setLocationRelativeTo(null); // Center the window on the screen
        frame.setVisible(true);
        System.out.println("The savings growth chart is displayed. You can close the chart window to return to the menu.");
    }
    
    private static void displaySavingsGrowth(int userId) {
        ArrayList<String> transferDates = new ArrayList<>();
        ArrayList<Double> savingsAmounts = new ArrayList<>();

        // Query to get the transfer_date and amount from the Savings table
        String query = "SELECT transfer_date, amount FROM Savings WHERE user_id = ? ORDER BY transfer_date ASC";

        try (Connection conn = DB.Connect();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;
                    String transferDate = rs.getString("transfer_date");
                    double amount = rs.getDouble("amount");

                    if (transferDate != null && !transferDate.isEmpty()) {
                        transferDates.add(transferDate);  // Store the transfer date as String
                        savingsAmounts.add(amount);  // Store the savings amount as Double
                    }
                }

                if (!hasData) {
                    System.out.println("No savings data available to display.");
                    return;
                }
            }
        } catch (SQLException e) {
            System.out.println("An error occurred while retrieving savings data: " + e.getMessage());
            return;
        }

        // Ensure the transferDates and savingsAmounts lists have the same size
        if (transferDates.isEmpty() || savingsAmounts.isEmpty()) {
            System.out.println("No valid savings data available for visualization.");
            return;
        }

        // Create a bar chart using XChart
        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Savings Growth Over Time")
                .xAxisTitle("Transfer Date")
                .yAxisTitle("Savings Amount")
                .build();

        // Add series to the chart
        chart.addSeries("Savings Growth", transferDates, savingsAmounts);

        // Display the chart in a JFrame
        JFrame frame = new JFrame("Savings Growth");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Only close the chart window
        frame.add(new XChartPanel<>(chart));  // Embed the chart panel in the JFrame
        frame.pack();
        frame.setLocationRelativeTo(null);  // Center the window on the screen
        frame.setVisible(true);

        System.out.println("The savings growth chart is displayed. You can close the chart window to return to the menu.");
    }

    private static void displayLoanRepayments(int userId) {
        ArrayList<String> repaymentDates = new ArrayList<>();
        ArrayList<Double> repayments = new ArrayList<>();
        ArrayList<Double> remainingBalances = new ArrayList<>();

        // Query to fetch loan repayment data
        String query = "SELECT payment_date, repayment, loan_balance FROM Repay WHERE user_id = ? ORDER BY payment_date ASC";

        try (Connection conn = DB.Connect();
             PreparedStatement statement = conn.prepareStatement(query)) {

            statement.setInt(1, userId);

            try (ResultSet rs = statement.executeQuery()) {
                boolean hasData = false;

                while (rs.next()) {
                    hasData = true;
                    String paymentDate = rs.getString("payment_date");
                    double repayment = rs.getDouble("repayment");
                    double loanBalance = rs.getDouble("loan_balance");

                    repaymentDates.add(paymentDate);
                    repayments.add(repayment);
                    remainingBalances.add(loanBalance);
                }

                if (!hasData) {
                    System.out.println("No loan repayment data available to display.");
                    return;
                }
            }

        } catch (SQLException e) {
            System.out.println("An error occurred: " + e.getMessage());
            return;
        }

        // Create the chart for loan repayments over time
        CategoryChart chart = new CategoryChartBuilder()
                .width(800)
                .height(600)
                .title("Loan Repayments Over Time")
                .xAxisTitle("Payment Date")
                .yAxisTitle("Amount")
                .build();

        // Add repayment series
        chart.addSeries("Repayment", repaymentDates, repayments);
        // Add remaining balance series
        chart.addSeries("Remaining Balance", repaymentDates, remainingBalances);

        // Display the chart in a JFrame
        JFrame frame = new JFrame("Loan Repayments Over Time");
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);  // Only close the chart window
        frame.add(new XChartPanel<>(chart));  // Embed the chart panel in the JFrame
        frame.pack();
        frame.setLocationRelativeTo(null);  // Center the window on the screen
        frame.setVisible(true);

        System.out.println("The loan repayments chart is displayed. You can close the chart window to return to the menu.");
    }


}
