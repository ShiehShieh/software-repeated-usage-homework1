package server;

import com.sun.glass.ui.SystemClipboard;
import com.sun.tools.javac.comp.Check;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.sql.*;
import java.util.Timer;
import java.util.TimerTask;
import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Created by shieh on 3/20/16.
 */
public class Server extends ServerSocket {

    private int valid_login_per_min = 0;
    private int invalid_login_per_min = 0;
    private int received_msg = 0;
    private int ignored_msg = 0;
    private int forwarded_msg = 0;
    private Timer loginTimer;
    private static Object threadLock = new Object();
    private static Object loginLock = new Object();
    private static Object msgLock = new Object();
    private static Object forwardLock = new Object();
    private IOLog ioLog;

    private static final int SERVER_PORT = 2095;

    private static List user_list = new ArrayList();//登录用户集合
    private static List<ServerThread> thread_list = new ArrayList<ServerThread>();//服务器已启用线程集合
    private static LinkedList<Pair<Long, String> > msg_list = new LinkedList<Pair<Long, String> >();//存放消息队列
    private Connection conn;
    private String dbuser = "root";
    private String dbpw = "root";

    /**
     * 创建服务端Socket,创建向客户端发送消息线程,监听客户端请求并处理
     */
    public Server()throws IOException {
        super(SERVER_PORT);//创建ServerSocket
        new PrintOutThread();//创建向客户端发送消息线程

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

        loginTimer = new Timer();
        loginTimer.schedule(new CheckLoginCount(), 0, 60000);
        ioLog = new IOLog("server.log", true);

        try {
            while(true){//监听客户端请求，启个线程处理
                Socket socket = accept();
                new ServerThread(socket);
            }
        }catch (Exception e) {
        }finally{
            close();
        }

        try {
            conn.close();
        } catch (Exception e) {
            ;
        }
    }

    public String getPassword(String username) {
        String password = "rootpassword";
        String sql = "select password from tb_user where username = ?";
        ResultSet rs;
        PreparedStatement ps = null;
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
        return password;
    }

    /**
     * 监听是否有输出消息请求线程类,向客户端发送消息
     */
    class PrintOutThread extends Thread{

        public PrintOutThread(){
            start();
        }

        @Override
        public void run() {
            while(true){
                // System.out.println(message_list.size());
                try {
                    sleep(100);
                } catch (Exception e) {
                    ;
                }
                synchronized(msg_list) {
                    if (msg_list.size() > 0) {//将缓存在队列中的消息按顺序发送到各客户端，并从队列中清除。
                        Pair<Long, String> message = msg_list.getFirst();
                        for (ServerThread thread : thread_list) {
                            if (thread.getId() != message.getL()) {
                                thread.sendMessage(message.getR());
                                synchronized (forwardLock) {
                                    ++forwarded_msg;
                                }
                            }
                        }
                        msg_list.removeFirst();
                    }
                }
            }
        }
    }

    class CheckLoginCount extends TimerTask {
        public void run() {
            String res;
            res = new SimpleDateFormat("yyyyMMdd_HHmmss: ").format(Calendar.getInstance().getTime());
            ioLog.IOWrite(res + "valid login per min: " + valid_login_per_min + "\n");
            ioLog.IOWrite(res + "invalid login per min: " + invalid_login_per_min + "\n");
            ioLog.IOWrite(res + "received message: " + received_msg + "\n");
            ioLog.IOWrite(res + "ignored message: " + ignored_msg + "\n");
            ioLog.IOWrite(res + "forwarded message: " + forwarded_msg + "\n");
            synchronized (loginLock) {
                valid_login_per_min = 0;
                invalid_login_per_min = 0;
            }
            synchronized (msgLock) {
                received_msg = 0;
                ignored_msg = 0;
            }
            synchronized (forwardLock) {
                forwarded_msg = 0;
            }
        }
    }

    /**
     * 服务器线程类
     */
    class ServerThread extends Thread {
        private Socket client;
        private PrintWriter out;
        private BufferedReader in;
        private String username = "";
        private String password = "";
        private int num_message = 0;
        private int msg_in_second = 0;
        private int MAX_MESSAGE_PER_SECOND = 5;
        private int MAX_MESSAGE_FOR_TOTAL = 100;
        private Timer timer;

        class CheckMessageCount extends TimerTask {
            public void run() {
                msg_in_second = 0;
            }
        }

        public ServerThread(Socket s)throws IOException {
            timer = new Timer();
            this.client = s;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            start();
        }

        public void login()throws IOException {
            int i;
            String line;
            String[] parts;

            while(true) {
                out.println("Login");
                for (i = 0; i < 2; ++i) {
                    line = in.readLine();
                    parts = line.split(":");
                    if (parts.length == 2) {
                        if (parts[0].equals("username")) {
                            username = parts[1];
                        } else if (parts[0].equals("password")) {
                            password = parts[1];
                        }
                    }
                }
                if (username.equals("") || password.equals("")) {
                    continue;
                }
                synchronized (loginLock) {
                    if (password.equals(getPassword(username))) {
                        ++valid_login_per_min;
                        break;
                    } else {
                        ++invalid_login_per_min;
                        out.println("Incorrect password");
                    }
                }
            }

            out.println("成功连上聊天室:");
            num_message = 0;
            msg_in_second = 0;
            timer.schedule(new CheckMessageCount(), 0, 1000);

            return;
        }

        @Override
        public void run() {
            try {
                login();

                synchronized(threadLock) {
                    user_list.add(username);
                    thread_list.add(this);
                    out.println(username + "你好,可以开始聊天了...");
                    this.pushMessage("Client<" + username + ">进入聊天室...");
                }

                String line = in.readLine();
                while(!"bye".equals(line)) {
                    //查看在线用户列表
                    if ("showuser".equals(line)) {
                        out.println(listOnlineUsers());
                    } else {
                        synchronized (msgLock) {
                            if (msg_in_second <= MAX_MESSAGE_PER_SECOND) {
                                pushMessage("Client<" + username + "> say : " + line);
                                ++num_message;
                                ++msg_in_second;
                                ++received_msg;
                            } else {
                                ++ignored_msg;
                            }
                        }
                        if (num_message == MAX_MESSAGE_FOR_TOTAL) {
                            out.println("Redo login");
                            login();
                        }
                    }
                    line = in.readLine();
                }
                out.println("quit");
            } catch (Exception e) {
                e.printStackTrace();
            } finally { //用户退出聊天室
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                timer.cancel();
                synchronized (threadLock) {
                    thread_list.remove(this);
                    user_list.remove(username);
                }
                pushMessage("Client<" + username +">退出了聊天室");
            }
        }

        //放入消息队列末尾，准备发送给客户端
        private void pushMessage(String msg){
            synchronized(msg_list) {
                msg_list.addLast(new Pair<Long, String>(this.getId(), msg));
            }
        }

        //向客户端发送一条消息
        private void sendMessage(String msg){
            out.println(msg);
        }

        //统计在线用户列表
        private String listOnlineUsers() {
            String s ="--- 在线用户列表 ---\015\012";
            for (int i =0; i < user_list.size(); i++) {
                s +="[" + user_list.get(i) +"]\015\012";
            }
            s +="--------------------";
            return s;
        }
    }

    public static void main(String[] args)throws IOException {
        new Server();//启动服务端
    }
}

class Pair<L,R> {
    private L l;
    private R r;
    public Pair(L l, R r){
        this.l = l;
        this.r = r;
    }
    public L getL(){ return l; }
    public R getR(){ return r; }
    public void setL(L l){ this.l = l; }
    public void setR(R r){ this.r = r; }
}
