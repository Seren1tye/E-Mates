import java.util.Scanner;

public class ledger_system {

    public static void main(String[] args) {
               
               Scanner scanner = new Scanner(System.in);

        while (true) {
            System.out.println("Welcome to Ledger System");
            System.out.println("1. Register");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Enter your choice: ");
            int choice = scanner.nextInt();

            switch (choice) {
                case 1:
                    user_actions.sign_up(); 
                    break;
                case 2:
                    user_actions.log_in();
                    break;
                case 3:
                    System.out.println("Goodbye!");
                    return;
                default:
                    System.out.println("Invalid choice, please try again.");
            }
        }
        
        
    }
    
}
