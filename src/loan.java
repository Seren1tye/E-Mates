import java.sql.*;
import java.util.Calendar;
import java.util.Scanner;

public class loan{
    
    static Scanner sc = new Scanner(System.in);
      
    public static void loanMethod (int user_id){
        CreditLoan:{
            while(true){
                
                //getting the values for each variable that will be used eiher for if or to pass into method
                double loan = loan(user_id);
                String status= status(user_id);
                int loan_id= loanId(user_id);

                System.out.println("""

                                ==CREDIT LOAN==   
                                1.Apply
                                2.Repay
                                3.Exit
                                """);
                String menuInput = sc.nextLine();;
                int menu=0;
                try{
                    menu = Integer.parseInt(menuInput);
                }catch(NumberFormatException e){}
                

                switch(menu){
                    case 1:
                        //if loan id is 0 or loan status is fully repaid, then user is allowed to apply for loan
                        //loan id 0 means the user has never applied for any loan, will be explained more in loanID method
                        if(loan_id==0 || status.equals("repaid")){
                            apply(user_id,status, loan_id); //calling the apply method
                        }else{
                            System.out.println("Complete your current loan repayment before applying for a new one.");
                        }
                        break;

                    case 2:
                        //if loan id is 0 or loan status is fully repaid, it will cause logic since theres no loan to repay
                        if(loan_id==0 || status.equals("repaid")){
                            System.out.println("No loan applied.");
                        }else{
                            repay(user_id,loan_id,loan); //calling the repay method
                        }
                        break;

                    case 3:
                        break CreditLoan;
                    default:
                        System.out.println("Invalid input.");
                }
            }
        }
    }
    
    //this method is to get the main balance of the current user
    public static double balanceMain (int user_id){
        double balance=0;
        
        try {
            Connection conn = DB.Connect(); //database connected
            String sql = "select current_amount from balance where user_id = ?; "; //query to get balance of the current user
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1,user_id);
            
            ResultSet rs = statement.executeQuery();
            
            if(rs.next()){
                balance = rs.getDouble("current_amount"); //store it in variable balance
            }
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        return balance;
    }
    
    
    //this method is to check wheter the user has pay the current installment, else debit and credit will be blocked
    public static boolean Overdue(int user_id){
        boolean Overdue=false; //initialized as false
        try {
            Connection conn = DB.Connect(); //database connection
            
            double loan = loan(user_id); //to get the current loan balance by calling the loan method
            Calendar calendar = Calendar.getInstance(); //using the calendar object to check the date for overdue
            Date created_at=new Date(calendar.getTimeInMillis()); //to store the date of when the specific loan has been created
            Date dueDate=new Date(calendar.getTimeInMillis()); // to store the date of when the current monthly installmet has to be repaid
            Date currentDate = new Date(calendar.getTimeInMillis()); //to store the current date
            double M=0,balance=0; // M is monthly installment, balance is loan balance to be repaid
            int totalPeriod=0; //variable to store how many months for the whole loan has to be repaid
            
            //query to get created_at, monthly installment, total period from table called loandetails
            String sql1 = "select created_at, monthly_repay, repayment_period from loandetails where user_id=? and loan_id = (select MAX(loan_id) from loandetails where user_id= ?); ";
            PreparedStatement statement1 = conn.prepareStatement(sql1);
            statement1.setInt(1, user_id);
            statement1.setInt(2, user_id);

            ResultSet rs1= statement1.executeQuery();
            
            if(rs1.next()){
                M = rs1.getDouble("monthly_repay");
                created_at = rs1.getDate("created_at");
                totalPeriod = rs1.getInt("repayment_period");
                 
            }
            //query to the remaining loan balance to be repay, the current monthly installment due date from repay table
            String sql2 = "select loan_balance, due_date from repay where user_id = ? and loan_id = (select MAX(loan_id) from repay where user_id= ?) and repay_id = (select MAX(repay_id) from repay where user_id= ? and loan_id = (select MAX(loan_id) from repay where user_id= ?))";
            PreparedStatement statement2 = conn.prepareStatement(sql2);
            statement2.setInt(1, user_id);
            statement2.setInt(2, user_id);
            statement2.setInt(3, user_id);
            statement2.setInt(4, user_id);
            
            ResultSet rs2= statement2.executeQuery();
            
            if(rs2.next()){
                balance  = rs2.getDouble("loan_balance");
                dueDate = rs2.getDate("due_date");
            }
            
            calendar.setTime(created_at);
            int year1 = calendar.get(Calendar.YEAR);
            int month1 = calendar.get(Calendar.MONTH);
            
            calendar.setTime(dueDate);
            int year2 = calendar.get(Calendar.YEAR);
            int month2 = calendar.get(Calendar.MONTH);
            //diff is the actual months between the date of the loan was applied and the current monthly installment due date
            int diff = (year2-year1)*12 + (month2-month1);
            //period is the remaining months of how many months of monthly installments are left
            int period = totalPeriod-diff;
             
            //M*period is to calculate the remaining total loan balance on the assumption user has paid according to the schedule
            //balance is the actual loan balance based on user own repayment
            //if the actual loan balance is more than remaining of the should be loam balance, it means that user hasnt paid for this months yet or has not follow the schedule
            //and if that condition is true with the current date is more that the due date of this month installment, then overdue is true
            //if both condition does not met, meaing that it is not overdue and method will return false
            if(balance>(M*period)&& currentDate.compareTo(dueDate)>0){
                Overdue=true;
            }  
        } catch (SQLException e){
            e.printStackTrace();
        }
            return Overdue;
    }
    
    //method to get the loan balance
    public static double loan(int user_id){
        double balance=0.00;
        try {
            Connection conn = DB.Connect(); // database connection
            //query to get loan balance from loan details table
            String sql= "select loan_balance from loandetails where user_id=? and loan_id = (select MAX(loan_id) from loandetails where user_id= ?);";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, user_id);
            statement.setInt(2, user_id);
            
            ResultSet rs= statement.executeQuery();
            
            if(rs.next()){
                balance = rs.getDouble("loan_balance");
            } 
        } catch (SQLException e){
            e.printStackTrace();
        }
        return balance;
    }
    
    //method to get the current status of the loan, either active or is fully repaid
    public static String status(int user_id){
        
        String status="repaid"; //intialized as repaid
        try {
            Connection conn = DB.Connect(); //database connection
            //query to get stats from loan details table
            String sql= "select status from loandetails where user_id=? and loan_id = (select MAX(loan_id) from loandetails where user_id= ?);";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, user_id);
            statement.setInt(2, user_id);
            
            ResultSet rs= statement.executeQuery();
            
            if(rs.next()){
                status = rs.getString("status");
            }
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        return status;
    }
    
    //method the loan id of user, since user can apply for another loan given the previous loan has been fully repaid, thus multiple loan id
    public static int loanId(int user_id){
        int y=0; //inialized as 0 meaning user has never applied for loan
        try {
            Connection conn = DB.Connect(); //database connection
            //query to get the latest loan id since the user might have applied for many loans, thus explaining the use of MAX in sql
            String sql= "select MAX(loan_id) as MaxLoanId from loandetails where user_id=?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, user_id);
            
            ResultSet rs= statement.executeQuery();
            
            if(rs.next()){
                y = rs.getInt("MaxLoanId");
            }  
        } catch (SQLException e){
            e.printStackTrace();
        }
        return y;
    }
    
    //method to apply for loan
    public static void apply(int user_id, String status, int loan_id){
        try {
            Connection conn = DB.Connect(); //database connection
            double balanceMain=0; //the main balance of the user
            status = "active"; //set as active since we will be making a new loan
            //all the variables necessary for loan application
            //principal is how many money the user trying to apply (or borrow if its easier to understand)
            //interest is how many interest the bank applied for that loan, meaning how many percentage we are repaying back
            //period is how long the repayment gonna be
            //loan is the balance that need to be repaid
            //M is monthly installment
            double principal,interest,period,loan,M; //all the variables necessary for loan application
            String input;
            
            //to store the date of when the loan was applied
            Date created_at;
            System.out.print("Enter principal: ");
            input=sc.nextLine();
            try{
                principal = Double.parseDouble(input);
            }catch(NumberFormatException e){
                System.out.println("Invalid input.");
                return;
            }
            
            if(principal<=0){
                System.out.println("Please enter a valid amount");
                return;
            }
            
            System.out.print("Enter interest: ");
            input=sc.nextLine();
            try{
                interest = Double.parseDouble(input);
            }catch(NumberFormatException e){
                System.out.println("Invalid input.");
                return;
            }
            double interestCopy= interest;
            interest = interest/(12.0*100.0);

            if(interest<=0){
                System.out.println("Please enter a valid amount");
                return;
            }
        
            System.out.print("Enter period [in month(s)]: ");
            input=sc.nextLine();
            try{
                period = Double.parseDouble(input);
            }catch(NumberFormatException e){
                System.out.println("Invalid input.");
                return;
            }

            if(period<=0){
                System.out.println("Please enter a valid amount");
                return;
            }
        
            M = ( principal * interest ) / ( 1 - (Math.pow( 1+interest, -period ) ) ); //the formula to calculate the monthly installment
            M = Math.round(M * 100.0) / 100.0; //rounding to 2 decimal points given we are handling with money
        
            loan = M*period; //the loan balance that need to be repaid by multiplying M and period
            loan = Math.round(loan * 100.0) / 100.0;
            
            //prepare query to insert the data into database specifically into loan details table which we will need these data for any other actions later
            String sql = "INSERT INTO loandetails (user_id,principal,interest_rate,repayment_period,monthly_repay,loan_balance,status,created_at) VALUES (?,?,?,?,?,?,?,?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            
            //get date for when user apply for loan
            Calendar calendar = Calendar.getInstance();
            created_at = new Date(calendar.getTimeInMillis());
            
            //set parameter
            statement.setInt(1,user_id);
            statement.setDouble(2, principal);
            statement.setDouble(3, interestCopy);
            statement.setDouble(4, period);
            statement.setDouble(5, M);
            statement.setDouble(6, loan);
            statement.setString(7, status);
            statement.setDate(8, created_at);
            
            
            //execute
            statement.executeUpdate();

            System.out.println("Loan application completed!");
            System.out.println("Applied on: "+created_at);
            System.out.println("Total Repayment: "+loan);
            System.out.println("Monthly Repayment: "+M);
            
            balanceMain = balanceMain(user_id); //get the current balance of the user
            balanceMain+= principal; //adding with how much money the user applied for
            balanceMain= Math.round(balanceMain*100.0)/100.0;
            
            //to update the current balance in database
            String sql2 = "update balance set current_amount = ? where user_id = ?;";
            PreparedStatement statement2 = conn.prepareStatement(sql2);
            statement2.setDouble(1,balanceMain);
            statement2.setInt(2, user_id);
            
            statement2.executeUpdate();
            
            //to update the trasanction table for the use of history class 
            String sql6 = "insert into transactions (user_id, description, debit, balance, transaction_type) values (?,?,?,?,?)";
            PreparedStatement statement6 = conn.prepareStatement(sql6);
                    
            statement6.setInt(1,user_id);
            statement6.setString(2, "Loan application.");
            statement6.setDouble(3, principal);
            statement6.setDouble(4, balanceMain);
            statement6.setString(5, "debit");

            statement6.executeUpdate();
            
            
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
      
    public static void repay(int user_id,int loan_id, double loan){
        
        try {
            Connection conn = DB.Connect(); //database connection
            double balanceMain= balanceMain(user_id); //get current main balance
            int installment; //
            Calendar calendar = Calendar.getInstance(); //using calendar object to handle anything related to date
            Date created_at=new Date(calendar.getTimeInMillis()),payment_date= new Date(calendar.getTimeInMillis()),dueDate=new Date(calendar.getTimeInMillis());
            double M=0,repay;
            int period=0;
            
            //query to get all the necessary infos which are date of loan applied, monthly installment, period of repayment
            String sql1 = "select created_at, monthly_repay, repayment_period from loandetails where user_id=? and loan_id = (select MAX(loan_id) from loandetails where user_id= ?); ";
            PreparedStatement statement1 = conn.prepareStatement(sql1);
            statement1.setInt(1, user_id);
            statement1.setInt(2, user_id);

            ResultSet rs= statement1.executeQuery();
            
            if(rs.next()){
                M = rs.getDouble("monthly_repay");
                period = rs.getInt("repayment_period");
                created_at = rs.getDate("created_at");
                calendar.setTime(created_at); 
            }
            
            //to calculate how many installment have been paid
            //by having the actual loan to be repaid substracting the remaining loan balance which result in how much have been paid
            //divide the value with monthly installment and floor it
            double dueCalc = M*period - loan;
            dueCalc = Math.round(dueCalc*100.0)/100.0;
            dueCalc/=M;
            installment = (int) Math.floor(dueCalc);
            
            //to calculate the due date of current installment before paying
            calendar.add(Calendar.MONTH, installment+1);
            dueDate = new Date(calendar.getTimeInMillis());
            
            
            //to calculate the amount of money that need to be pay for this month installment
            //by having the actual loan to be repaid substracting the remaining loan balance which result in how much have been paid
            //get the remainder of it when dividing with monthly installment, which means getting how much we have paid for this month installment
            //update it again by having monthly intallment subtracting it and store it into installment due
            double InstallmentDue= M*period - loan;
            InstallmentDue%= M;
            InstallmentDue = M- InstallmentDue;
            InstallmentDue = Math.round(InstallmentDue*100.0)/100.0;
            String input;
            
            System.out.println("Outstanding Loan Amount: "+loan);
            System.out.println("Next Installment : "+InstallmentDue+ " due on "+dueDate);
            System.out.println("=================================================");
            System.out.print("Enter repayment amount: ");
            input=sc.nextLine();
            try{
                repay = Double.parseDouble(input);
            }catch(NumberFormatException e){
                System.out.println("Invalid input.");
                return;
            }
            
            if(repay>balanceMain){
                System.out.println("Insuficcient funds.");
            }else if (repay>loan){
                System.out.println("You’re paying more than required for your loan.");
            }else if(repay<=0){
                System.out.println("Please a valid amount.");
            }else{
                
                //update the main balance and remaining loan balance
                balanceMain-=repay;
                loan-= repay;
                loan = Math.round(loan*100.0)/100.0;
                System.out.println("Remaining Loan Balance: "+loan);

                //to update how many installment have been paid since user can pay more than one monthly installment
                //which is important to update due date later
                dueCalc = M*period - loan;
                dueCalc = Math.round(dueCalc*100.0)/100.0;
                dueCalc/=M;
                installment = (int) Math.floor(dueCalc);

                //if user paid more than current monthly installment balance, we have to update the due date based on how many installment have been paid
                if(repay>=InstallmentDue){
                    calendar.setTime(created_at); 
                    calendar.add(Calendar.MONTH, installment+1);
                    dueDate = new Date(calendar.getTimeInMillis());
                }

                //to update the amount of money that need to be paid for the next installment
                InstallmentDue= M*period - loan;
                InstallmentDue%= M;
                InstallmentDue = M- InstallmentDue;
                InstallmentDue = Math.round(InstallmentDue*100.0)/100.0;
                
                //if user has paid all loan, then else block will run, if not then it will print the next installment amount and its due date
                if(loan!=0){
                    System.out.println("Next Installment: "+InstallmentDue+" due "+dueDate);
                }else{
                    System.out.println("All loans have been fully settled!");
                    }

                    //to update the database which is specifically repay table
                    String sql2 = "Insert into repay (user_id,loan_id,repayment,loan_balance,payment_date,due_date) values (?,?,?,?,?,?);";
                    PreparedStatement statement2 = conn.prepareStatement(sql2);

                    statement2.setInt(1,user_id);
                    statement2.setInt(2, loan_id);
                    statement2.setDouble(3, repay);
                    statement2.setDouble(4, loan);
                    statement2.setDate(5, payment_date);
                    statement2.setDate(6, dueDate);

                    statement2.executeUpdate();

                    //to update loan balance in loandetails table
                    String sql3 = "update loandetails set loan_balance = ? where user_id = ? and loan_id = ?; ";
                    PreparedStatement statement3 = conn.prepareStatement(sql3);

                    statement3.setDouble(1,loan);
                    statement3.setInt(2,user_id);
                    statement3.setInt(3,loan_id);

                    statement3.executeUpdate();
                    
                    //if loan have been fully repaid, then the status of the loan will be updated to repaid
                    if (loan==0){
                        String sql4 = "update loandetails set status = \"repaid\" where user_id = ? and loan_id = ?; ";
                        PreparedStatement statement4 = conn.prepareStatement(sql4);

                        statement4.setInt(1,user_id);
                        statement4.setInt(2,loan_id);

                        statement4.executeUpdate();
                    }
                    
                    //to record transaction for the use of history class
                    String sql6 = "insert into transactions (user_id, description, credit, balance, transaction_type) values (?,?,?,?,?)";
                    PreparedStatement statement6 = conn.prepareStatement(sql6);
                    
                    statement6.setInt(1,user_id);
                    statement6.setString(2, "Loan repayment");
                    statement6.setDouble(3, repay);
                    statement6.setDouble(4, balanceMain);
                    statement6.setString(5, "credit");

                    statement6.executeUpdate();
                    
                    String sql5 = "update balance set current_amount = ? where user_id = ?;";
                    PreparedStatement statement5 = conn.prepareStatement(sql5);
                    
                    statement5.setDouble(1,balanceMain);
                    statement5.setInt(2, user_id);
                    
                    statement5.executeUpdate();      
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        
    }
    
    //reminder method to remind user regarding loan repayment everytime user log in
    public static void reminder(int user_id, double loan){
        
        try {
            Connection conn = DB.Connect(); //database connection
                       
            int installment;
            Calendar calendar = Calendar.getInstance();
            Date created_at= new Date(calendar.getTimeInMillis()),dueDate=new Date(calendar.getTimeInMillis());
            double M=0,repay;
            int period=0;
            String status="repaid";
            
            String sql1 = "select status, created_at, monthly_repay, repayment_period from loandetails where user_id=? and loan_id = (select MAX(loan_id) from loandetails where user_id= ?); ";
            PreparedStatement statement1 = conn.prepareStatement(sql1);
            statement1.setInt(1, user_id);
            statement1.setInt(2, user_id);

            ResultSet rs= statement1.executeQuery();
            
            if(rs.next()){
                M = rs.getDouble("monthly_repay");
                period = rs.getInt("repayment_period");
                created_at = rs.getDate("created_at");
                calendar.setTime(created_at); 
                status = rs.getString("status");
            }
            
            double dueCalc = M*period - loan;
            dueCalc = Math.round(dueCalc*100.0)/100.0;
            dueCalc/=M;
            installment = (int) Math.floor(dueCalc);
            
            calendar.add(Calendar.MONTH, installment+1);
            dueDate = new Date(calendar.getTimeInMillis());
            
            double InstallmentDue= M*period - loan;
            InstallmentDue%= M;
            InstallmentDue = M- InstallmentDue;
            InstallmentDue = Math.round(InstallmentDue*100.0)/100.0;
            
            if(status.equals("active")){
                System.out.printf("(%.2f due on %s)\n",InstallmentDue,dueDate);
            }
            
        }catch(SQLException e){
            e.printStackTrace();
        }
    }
}
