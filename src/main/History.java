package bankapp;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

public class TransactionHistory {

    public static void displayHistory() {
        try (Connection conn = DatabaseConnection.getConnection();
             Statement stmt = conn.createStatement()) {

            String query = "SELECT date, description, debit, credit, balance FROM TransactionsHistory";
            ResultSet rs = stmt.executeQuery(query);

            System.out.printf("%-12s %-20s %-10s %-10s %-10s%n", "Date", "Description", "Debit", "Credit", "Balance");
            System.out.println("-------------------------------------------------------------");

            while (rs.next()) {
                String date = rs.getString("date");
                String description = rs.getString("description");
                double debit = rs.getDouble("debit");
                double credit = rs.getDouble("credit");
                double balance = rs.getDouble("balance");

                System.out.printf("%-12s %-20s %-10.2f %-10.2f %-10.2f%n",
                        date, description, debit, credit, balance);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {
        displayHistory();
    }
}
