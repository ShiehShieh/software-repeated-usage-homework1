package server;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;

/**
 * Created by huangli on 3/24/16.
 */
public class DataSource {
    private Connection conn;
    private String dbuser;
    private String dbpw;

    public DataSource(String dbuser, String dbpw) {
        this.dbuser = dbuser;
        this.dbpw = dbpw;

        try{
            //加载MySql的驱动类
            Class.forName("com.mysql.jdbc.Driver") ;
        } catch(Exception e){
            System.out.println("找不到驱动程序类 ，加载驱动失败！");
            e.printStackTrace() ;
        }

        try {
            conn = DriverManager.getConnection("jdbc:mysql://127.0.0.1:3306/reusable",dbuser,dbpw);
        } catch (Exception e) {
            System.out.println("Connection error.");
            e.printStackTrace() ;
        }
    }

    public String getPassword(String username) {
        String password = null;
        String sql = "select password from tb_user where username = ?";
        ResultSet rs;
        PreparedStatement ps;
        try {
            ps = conn.prepareStatement(sql);
            ps.setString(1, username);
            rs = ps.executeQuery();
            if (rs.next()) {
                password = rs.getString(1);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        if (password == null) {
            System.out.println("User do not exist.");
        }
        return password;
    }
}
