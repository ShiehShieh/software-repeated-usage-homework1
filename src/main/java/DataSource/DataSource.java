package DataSource;

import utils.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;

/**
 * Created by shieh on 3/24/16.
 */
public class DataSource {
    private Connection conn;
    private String dbuser;
    private String dbpw;
    private PrintWriter out;
    private BufferedReader in;
    private String sourceType;


    public DataSource(BufferedReader in, PrintWriter out) {
        this.out = out;
        this.in = in;
        this.sourceType = "remote";
    }

    public DataSource(String dbuser, String dbpw) {
        this.dbuser = dbuser;
        this.dbpw = dbpw;
        this.sourceType = "DB";

        try{
            //加载MySql的驱动类
            Class.forName("com.mysql.jdbc.Driver");
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

    public String getType() {
        return this.sourceType;
    }

    public String getPasswordDB(String username) {
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

    public ArrayList<Pair<String,String>> getGroupUser() {
        ArrayList<Pair<String,String>> res = new ArrayList<Pair<String,String>>();
        Pair<String,String> tmp;
        String sql = "select * from GroupUser";
        ResultSet rs;
        PreparedStatement ps;
        try {
            ps = conn.prepareStatement(sql);
            rs = ps.executeQuery();
            while (rs.next()) {
                tmp = new Pair<String,String>();
                tmp.setL(rs.getString(2));
                tmp.setR(rs.getString(3));
                res.add(tmp);
            }
            rs.close();
            ps.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return res;
    }

    public String getPasswordResponse(String msg) throws IOException {
        String res;
        out.println(msg);
        res = in.readLine();
        return res;
    }
}
