


import java.sql.*;
import java.util.Calendar;
import java.util.Scanner;

public class loan{
    
    private static final String url = "jdbc:mysql://localhost:3306/loan";
    private static final String user = "root";
    private static final String pass = "password";
    static Scanner sc = new Scanner(System.in);
    
    
    public static void main(String[] args) {
        
        double savings=0,loan=0,balanceMain=0;
        int user_id = 1;
        loan = loan(user_id);
        balanceMain = balanceMain(user_id);
        int mainMenu;
        
        mainf:{
            
            while(true){
                boolean Overdue;
                Overdue= Overdue(user_id,loan);
                
                
                System.out.println("""
                                   == Welcome, Sake ==
                                   """);
                System.out.printf("Balance: %.2f\n",balanceMain);
                System.out.printf("Savings: %.2f\n",savings);
                System.out.printf("Loan: %.2f\n",loan);
                
                System.out.println("""
                                   
                                   == Transaction ==
                                   1.Debit
                                   2.Credit
                                   3.History
                                   4.Savings
                                   5.Credit Loan
                                   6.Deposit Interest Predictor
                                   7.Logout
                                   
                                   """);
                System.out.print(">");
                mainMenu= sc.nextInt();
                
                switch(mainMenu){
                    
                    case 2:
                        if(Overdue){
                            System.out.println("kasi setel loan bulan ini dulu ma");
                        }else{
                            System.out.println("do credit ig");
                        }
                        break;
                    
                    case 5:
                        CreditLoan:{
                            while(true){

                                String status= status(user_id);
                                int loan_id= loanId(user_id);
                                
                                balanceMain = balanceMain(user_id);
                                loan = loan(user_id);
                                int repay_id = repayId(user_id, loan_id);



                                System.out.println("""

                                                1.Apply
                                                2.Repay
                                                3.Exit
                                                """);
                                 int menu = sc.nextInt();

                                switch(menu){
                                    case 1:
                                        if(loan_id==0 || status.equals("repaid")){
                                            apply(user_id,status, loan_id);
                                        }else{
                                            System.out.println("bayo abih lu bos");
                                        }
                                        break;

                                    case 2:
                                        repay(user_id,loan_id,repay_id,loan);
                                        break;

                                    case 3:
                                        break CreditLoan;
                                    default:
                                        System.out.println("error");
                                }
                            }
                        }
                        break;
                        
                    case 7:
                        break mainf;
                }
                
            }
            
        }
        
        
        
        
        System.out.println("Thanks for using our ledger system!");
                
        
    }
    
    public static double balanceMain (int user_id){
        double balance=0;
        
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            
            String sql = "select Balance from balance where UserId = ?; ";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1,user_id);
            
            ResultSet rs = statement.executeQuery();
            
            if(rs.next()){
                balance = rs.getDouble("Balance");
            }
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        return balance;
    }
    
    public static boolean Overdue(int user_id, double loan){
        boolean Overdue=false;
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            
            
            Calendar calendar = Calendar.getInstance();
            Date created_at=new Date(calendar.getTimeInMillis());
            Date dueDate=new Date(calendar.getTimeInMillis());
            Date currentDate = new Date(calendar.getTimeInMillis());
            double M=0,balance=0;
            int totalPeriod=0;
            
            
            String sql1 = "select CreatedAt, MonthlyRepay, RepaymentPeriod from loandetails where UserId=? and LoanId = (select MAX(LoanId) from loandetails where UserId= ?); ";
            PreparedStatement statement1 = conn.prepareStatement(sql1);
            statement1.setInt(1, user_id);
            statement1.setInt(2, user_id);

            ResultSet rs1= statement1.executeQuery();
            
            if(rs1.next()){
                M = rs1.getDouble("MonthlyRepay");
                created_at = rs1.getDate("CreatedAt");
                totalPeriod = rs1.getInt("RepaymentPeriod");
                 
            }
            
            String sql2 = "select Balance, DueDate from repay where UserID = ? and LoanID = (select MAX(LoanID) from repay where UserID= ?) and RepayID = (select MAX(RepayID) from repay where UserId= ? and LoanId = (select MAX(LoanID) from repay where UserID= ?))";
            PreparedStatement statement2 = conn.prepareStatement(sql2);
            statement2.setInt(1, user_id);
            statement2.setInt(2, user_id);
            statement2.setInt(3, user_id);
            statement2.setInt(4, user_id);
            
            ResultSet rs2= statement2.executeQuery();
            
            if(rs2.next()){
                balance  = rs2.getDouble("Balance");
                dueDate = rs2.getDate("DueDate");
            }
            
            calendar.setTime(created_at);
            int year1 = calendar.get(Calendar.YEAR);
            int month1 = calendar.get(Calendar.MONTH);
            
            calendar.setTime(dueDate);
            int year2 = calendar.get(Calendar.YEAR);
            int month2 = calendar.get(Calendar.MONTH);
            
            int diff = (year2-year1)*12 + (month2-month1);
            int period = totalPeriod-diff;
            
            
            currentDate = new Date(calendar.getTimeInMillis());
            
            if(balance>(M*period)&& currentDate.compareTo(dueDate)>0){
                Overdue=true;
            }
            
            
            
        } catch (SQLException e){
            e.printStackTrace();
        }
            return Overdue;
    }
    
    public static double loan(int user_id){
        double balance=0.00;
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            
            String sql= "select Balance from loandetails where UserId=? and LoanId = (select MAX(LoanId) from loandetails where UserId= ?);";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, user_id);
            statement.setInt(2, user_id);
            
            ResultSet rs= statement.executeQuery();
            
            if(rs.next()){
                balance = rs.getDouble("Balance");
            }
         
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        
        return balance;
    }
    
    public static String status(int user_id){
        
        String status="repaid";
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            
            String sql= "select Status from loandetails where UserId=? and LoanId = (select MAX(LoanId) from loandetails where UserId= ?);";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, user_id);
            statement.setInt(2, user_id);
            
            ResultSet rs= statement.executeQuery();
            
            if(rs.next()){
                status = rs.getString("Status");
            }
            
        } catch (SQLException e){
            e.printStackTrace();
        }
        return status;
    }
    
    public static int loanId(int user_id){
        int y=0;
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            
            String sql= "select MAX(LoanId) as MaxLoanId from loandetails where UserId=?;";
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
    
    public static void apply(int user_id, String status, int loan_id){
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            double balanceMain=0;
            status = "active";
            double principal,interest,period,loan,M;
            
            Date created_at;
            System.out.print("Enter principal: ");
            principal = sc.nextDouble();
            
            
            
            System.out.print("Enter interest: ");
            interest = sc.nextDouble();
            double interestCopy= interest;
            interest = interest/(12.0*100.0);
            
        
            System.out.print("Enter period: ");
            period = sc.nextDouble();
        
            M = ( principal * interest ) / ( 1 - (Math.pow( 1+interest, -period ) ) );
            M = Math.round(M * 100.0) / 100.0;
        
            loan = M*period;
            loan = Math.round(loan * 100.0) / 100.0;
            
            //prepare query
            String sql = "INSERT INTO loandetails (UserId,LoanId,Principal,InterestRate,RepaymentPeriod,MonthlyRepay,Balance,Status,CreatedAt) VALUES (?,?,?,?,?,?,?,?,?)";
            PreparedStatement statement = conn.prepareStatement(sql);
            
            
            //get date for when user apply for loan
            Calendar calendar = Calendar.getInstance();
            created_at = new Date(calendar.getTimeInMillis());
            
            
            
            //set parameter
            statement.setInt(1,user_id);
            statement.setInt(2, (loan_id+1));
            statement.setDouble(3, principal);
            statement.setDouble(4, interestCopy);
            statement.setDouble(5, period);
            statement.setDouble(6, M);
            statement.setDouble(7, loan);
            statement.setString(8, status);
            statement.setDate(9, created_at);
            
            
            //execute
            statement.executeUpdate();

            System.out.println("Loan application completed!");
            System.out.println("Applied on: "+created_at);
            System.out.println("Total Repayment: "+loan);
            System.out.println("Monthly Repayment: "+M);
            
            balanceMain = balanceMain(user_id);
            balanceMain+= principal;
            balanceMain= Math.round(balanceMain*100.0)/100.0;
            
            
            String sql2 = "update balance set Balance = ? where UserId = ?;";
            PreparedStatement statement2 = conn.prepareStatement(sql2);
            statement2.setDouble(1,balanceMain);
            statement2.setInt(2, user_id);
            
            statement2.executeUpdate();
            
            
        } catch (SQLException e){
            e.printStackTrace();
        }
    }
    
    public static int repayId(int user_id, int loan_id){
        int repay_id=0;
        double balance=1;
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            
            String sql= "select MAX(RepayId) AS MaxRepayId from repay where UserID=? and LoanID = ?;";
            PreparedStatement statement = conn.prepareStatement(sql);
            statement.setInt(1, user_id);
            statement.setInt(2, loan_id);
            
            ResultSet rs= statement.executeQuery();
            
            if(rs.next()){
                repay_id = rs.getInt("MaxRepayId"); 
            }
            
            String sql2 = "select Balance from repay where UserID = ? and LoanID = ?-1; ";
            PreparedStatement statement2 = conn.prepareStatement(sql2);
            statement2.setInt(1, user_id);
            statement2.setInt(2, loan_id);
            
            ResultSet rs2= statement2.executeQuery();
            
            if(rs2.next()){
                balance = rs2.getDouble("Balance"); 
            }
            
            if(balance==0){
                repay_id=0;
            }
        } catch (SQLException e){
            e.printStackTrace();
        }
        return repay_id;
    }
    
    public static void repay(int user_id,int loan_id, int repay_id, double loan){
        
        try (Connection conn = DriverManager.getConnection(url,user,pass)){
            
            double balanceMain= balanceMain(user_id);
            int installment;
            Calendar calendar = Calendar.getInstance();
            Date created_at=new Date(calendar.getTimeInMillis()),payment_date= new Date(calendar.getTimeInMillis()),dueDate=new Date(calendar.getTimeInMillis());
            double M=0,repay;
            int period=0;
            
            String sql1 = "select CreatedAt, MonthlyRepay, RepaymentPeriod from loandetails where UserId=? and LoanId = (select MAX(LoanId) from loandetails where UserId= ?); ";
            PreparedStatement statement1 = conn.prepareStatement(sql1);
            statement1.setInt(1, user_id);
            statement1.setInt(2, user_id);

            ResultSet rs= statement1.executeQuery();
            
            if(rs.next()){
                M = rs.getDouble("MonthlyRepay");
                period = rs.getInt("RepaymentPeriod");
                created_at = rs.getDate("CreatedAt");
                calendar.setTime(created_at); 
            }
            
            double dueCalc = M*period - loan;
            dueCalc = Math.round(dueCalc*100.0)/100.0;
            dueCalc/=M;
            installment = (int) Math.floor(dueCalc);
            
            
            if (repay_id==0){
                
             
                calendar.add(Calendar.MONTH, 1);
                dueDate = new Date(calendar.getTimeInMillis());
                
                
            }else{
                calendar.add(Calendar.MONTH, installment+1);
                dueDate = new Date(calendar.getTimeInMillis());
            }
            
            double InstallmentDue= M*period - loan;
            InstallmentDue%= M;
            InstallmentDue = M- InstallmentDue;
            InstallmentDue = Math.round(InstallmentDue*100.0)/100.0;
            
            
            System.out.println("Outstanding Loan Amount: "+loan);
            System.out.println("Next Installment : "+InstallmentDue+ " due on "+dueDate);
            System.out.println("=================================================");
            System.out.print("Enter repayment amount: ");
            repay = sc.nextDouble();
            
            if(repay>balanceMain){
                System.out.println("topup dulu ma");
            }else{
                
                balanceMain-=repay;
                loan-= repay;
                loan = Math.round(loan*100.0)/100.0;
                System.out.println("Remaining Loan Balance: "+loan);


                dueCalc = M*period - loan;
                dueCalc = Math.round(dueCalc*100.0)/100.0;
                dueCalc/=M;
                installment = (int) Math.floor(dueCalc);


                if(repay>=InstallmentDue){
                    calendar.setTime(created_at); 
                    calendar.add(Calendar.MONTH, installment+1);
                    dueDate = new Date(calendar.getTimeInMillis());
                }

                InstallmentDue= M*period - loan;
                InstallmentDue%= M;
                InstallmentDue = M- InstallmentDue;
                InstallmentDue = Math.round(InstallmentDue*100.0)/100.0;



                if(loan!=0){
                    System.out.println("Next Installment: "+InstallmentDue+" due "+dueDate);
                }else{
                    System.out.println("All loans have been fully settled!");
                    }


                    String sql2 = "Insert into repay values (?,?,?,?,?,?,?);";
                    PreparedStatement statement2 = conn.prepareStatement(sql2);

                    statement2.setInt(1,user_id);
                    statement2.setInt(2, loan_id);
                    statement2.setInt(3, (repay_id+1));
                    statement2.setDouble(4, repay);
                    statement2.setDouble(5, loan);
                    statement2.setDate(6, payment_date);
                    statement2.setDate(7, dueDate);

                    statement2.executeUpdate();


                    String sql3 = "update loandetails set Balance = ? where UserId = ? and LoanId = ?; ";
                    PreparedStatement statement3 = conn.prepareStatement(sql3);

                    statement3.setDouble(1,loan);
                    statement3.setInt(2,user_id);
                    statement3.setInt(3,loan_id);

                    statement3.executeUpdate();

                    if (loan==0){
                        String sql4 = "update loandetails set Status = \"repaid\" where UserId = ? and LoanId = ?; ";
                        PreparedStatement statement4 = conn.prepareStatement(sql4);

                        statement4.setInt(1,user_id);
                        statement4.setInt(2,loan_id);

                        statement4.executeUpdate();
                    }
                    
                    
                    String sql5 = "update balance set Balance = ? where UserId = ?;";
                    PreparedStatement statement5 = conn.prepareStatement(sql5);
                    
                    statement5.setDouble(1,balanceMain);
                    statement5.setInt(2, user_id);
                    
                    statement5.executeUpdate();
            }
            
            
            
        }catch (SQLException e){
            e.printStackTrace();
        }
        
    }
}
