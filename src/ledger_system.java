import java.util.Scanner;
import java.util.InputMismatchException;

public class ledger_system {
    private static Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        int userId = -1;
        
        while (true) {
            try {
                // Main menu display
                System.out.println("\n=== Ledger System ===");
                System.out.println("1. Register");
                System.out.println("2. Login");
                System.out.println("3. Exit");
                System.out.println("Enter your choice: ");
                System.out.print("\n>");

                // Read user input with exception handling
                int choice;
                try {
                    choice = scanner.nextInt();
                    scanner.nextLine(); // Consume newline
                } catch (InputMismatchException e) {
                    System.out.println("Error: Please enter a number (1-3)");
                    scanner.nextLine(); // Clear invalid input
                    continue;
                }

                switch (choice) {
                    case 1:
                        try {
                            user_actions.sign_up();
                        } catch (Exception e) {
                            System.out.println("Registration Error: " + e.getMessage());
                        }
                        break;

                    case 2:
                        try {
                            String[] loginResult = user_actions.log_in();
                            userId = Integer.parseInt(loginResult[0]);
                            String userName = loginResult[1];

                            if (userId != -1) {
                                System.out.println("\nLogin successful!!!\n");
                                transactionsMenu(userId, userName);
                            }
                        } catch (NumberFormatException e) {
                            System.out.println("Error: Invalid user ID format");
                        } catch (Exception e) {
                            System.out.println("Login Error: " + e.getMessage());
                        }
                        break;

                    case 3:
                        System.out.println("Goodbye!");
                        System.exit(0); // Program shuts down properly
                        return;

                    default:
                        System.out.println("Invalid choice, please try again (1-3).");
                }

            } catch (Exception e) {
                System.out.println("An unexpected error occurred: " + e.getMessage());
                System.out.println("Please try again.");
            }
        }
    }

    // Sub-menu for transactions
private static void transactionsMenu(int userId, String userName) {
    Scanner read = new Scanner(System.in);
    int option;

    do {
        // Display user balance and personalized welcome message
        System.out.println("\n=== Welcome, " + userName + " ==="); // Display the user's name
        System.out.printf("Balance: %.2f\n", DebitCredit.getBalance(userId));
        System.out.printf("Savings: %.2f\n", Savings.viewSavings(userId));
        System.out.printf("Loan: %.2f\n", loan.loan(userId)); 

        System.out.println("\n=== Transactions ===");
        System.out.println("1. Debit");
        System.out.println("2. Credit");
        System.out.println("3. History");
        System.out.println("4. Savings");
        System.out.println("5. Credit Loan");
        System.out.println("6. Deposit Interest Predictor");
        System.out.println("7. Logout");
        System.out.print("\n> ");
        option = read.nextInt();

        read.nextLine(); // Consume newline

        switch (option) {
            case 1:
                if(loan.Overdue(userId)){
                    System.out.println("Please pay this month's installment to proceed with this action.");
                }else{
                    DebitCredit.debitAmount(userId, read); // Debit transaction
                }
                break;
            case 2:
                if(loan.Overdue(userId)){
                    System.out.println("Please pay this month's installment to proceed with this action.");
                }else{
                    DebitCredit.creditAmount(userId, read); // Credit transaction
                }
                break;
            case 3:
                    history.mainHistory(userId, userName); // Display transaction history
                break;
            case 4:
                    Savings.activateSavings(userId);
                break;
            case 5:
                loan.loanMethod(userId);
                break;
            case 6:
                InterestPredictor.mainInterest(userId);
                break;
            case 7:
                System.out.println("Returning to Main Menu...");
                break;
            default:
                System.out.println("Invalid input, please try again.");
        }
    } while (option != 7); // Repeat until user exits
}

}

