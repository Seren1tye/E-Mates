
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Calendar;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */

/**
 *
 * @author user
 */
public class SavingTransfer {
    public static void transferMethod(int user_id){
        Calendar calendar = Calendar.getInstance(); 
        Date currentDate = new Date(calendar.getTimeInMillis());
        Date transferDate=new Date(calendar.getTimeInMillis());
        try{
            Connection conn = DB.Connect();
            String sql1 = "select transfer_date from savings where user_id = ? and savings_id=(SELECT MAX(savings_id) FROM savings where user_id = ?);";
            PreparedStatement statement1 = conn.prepareStatement(sql1);
            statement1.setInt(1, user_id);
            statement1.setInt(2, user_id);
            
            ResultSet rs1= statement1.executeQuery();
            
            if(rs1.next()){
                transferDate = rs1.getDate("transfer_date");
            }
            System.out.println(currentDate);
            System.out.println(transferDate);
            if(currentDate.compareTo(transferDate)==0) {
                return;
            } else {
            }
            if(currentDate.compareTo(transferDate)>0){
                double amount =0;
                String sql2 = "select SUM(amount) as sum_amount from savings where user_id = ?;";
                PreparedStatement statement2 = conn.prepareStatement(sql2);
                statement2.setInt(1, user_id);
                
                ResultSet rs2= statement2.executeQuery();

                if(rs2.next()){
                    amount = rs2.getDouble("sum_amount");
                }
                double balanceMain = loan.balanceMain(user_id);
                balanceMain+=amount;
                balanceMain= Math.round(balanceMain*100.0)/100.0;

                String sql3 = "update balance set current_amount = ? where user_id = ?;";
                PreparedStatement statement3 = conn.prepareStatement(sql3);
                statement3.setDouble(1,balanceMain);
                statement3.setInt(2, user_id);

                statement3.executeUpdate();
                
                String sql4 = "update savings set amount = ? where user_id = ?;";
                PreparedStatement statement4 = conn.prepareStatement(sql4);
                statement4.setDouble(1,0);
                statement4.setInt(2, user_id);

                statement4.executeUpdate();
                
                if(amount!=0){
                    String sql6 = "insert into transactions (user_id, description, debit, balance, transaction_type) values (?,?,?,?,?)";
                    PreparedStatement statement6 = conn.prepareStatement(sql6);

                    statement6.setInt(1,user_id);
                    statement6.setString(2, "Saving Transfer.");
                    statement6.setDouble(3, amount);
                    statement6.setDouble(4, balanceMain);
                    statement6.setString(5, "debit");

                    statement6.executeUpdate();
                    
                    String sql5 = "update savingssettings set percentage = ? where user_id = ?;";
                    PreparedStatement statement5 = conn.prepareStatement(sql5);
                    statement5.setDouble(1,0);
                    statement5.setInt(2, user_id);

                    statement5.executeUpdate();
                }
                
                
            }
        }catch (SQLException e){
            e.printStackTrace();
        }
        
    }
    public static void main(String[] args) {
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);

        // Get the last day of the current month
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        System.out.println(currentDay);
        System.out.println(lastDayOfMonth);

    }
    public static boolean verify(int user_id){
        Calendar calendar = Calendar.getInstance();
        int currentDay = calendar.get(Calendar.DAY_OF_MONTH);
        int lastDayOfMonth = calendar.getActualMaximum(Calendar.DAY_OF_MONTH);
        if (currentDay == lastDayOfMonth) {
            System.out.println("Today is the end of the month. Please try tomorrow.");
            return true;
        }
        return false;
    }
}
