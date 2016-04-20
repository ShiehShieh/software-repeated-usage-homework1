package server;

import Authentication.Verification;
import CM.GetConfiguration;
import DataSource.DataSource;
import MessageUtils.Message;
import MessageUtils.MessageDeparturer;

import wheellllll.performance.*;
import wheellllll.license.*;
import wheellllll.config.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by shieh on 3/20/16.
 */
public class Server extends ServerSocket {
    private static Object threadLock = new Object();
    private boolean withLog = false;
    private String logFile;

    private static List user_list = new ArrayList();//登录用户集合
    private static DataSource dataSource;

    private PerformanceManager pm;

    public String valid_login_per_min = "valid login per min";
    public String invalid_login_per_min = "invalid login per min";
    public String received_msg = "received message";
    public String ignored_msg = "ignored message";
    public String forwarded_msg = "forwarded message";

    /**
     * 创建服务端Socket,创建向客户端发送消息线程,监听客户端请求并处理
     */
    public Server(int SERVER_PORT, String logFilename, String dbuser, String dbpw, boolean withLog)throws IOException {
        super(SERVER_PORT);//创建ServerSocket
        this.logFile = logFilename;
        this.withLog = withLog;

        dataSource = new DataSource(dbuser, dbpw);
    }

    public void run() throws IOException {
        if (withLog) {
            System.out.println("PM into " + this.logFile);
            pm = new PerformanceManager();
            LogUtils.setLogPrefix("Server");  //设置输出的文件名
            LogUtils.setLogPath(this.logFile);   //设置输出文件的路径
            pm.setTimeUnit(TimeUnit.SECONDS);   //时间单位为秒
            pm.setInitialDelay(1);              //延时1秒后执行
            pm.setPeriod(60);                    //循环周期为60秒即1分钟
            pm.addIndex(valid_login_per_min);
            pm.addIndex(invalid_login_per_min);
            pm.addIndex(received_msg);
            pm.addIndex(ignored_msg);
            pm.addIndex(forwarded_msg);
            pm.start();
        }

        try {
            while(true){//监听客户端请求，启个线程处理
                Socket socket = accept();
                new ServerThread(socket);
            }
        }catch (Exception e) {
        }finally{
            close();
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
        private int MAX_MESSAGE_PER_SECOND = 5;
        private int MAX_MESSAGE_FOR_TOTAL = 10;
        private Verification verification;
        private License license;
        public Message msg;
        public MessageDeparturer messageDeparturer;
        String exchangeName = "test";
        String queueName;

        public ServerThread() {
            License license = new License(License.LicenseType.BOTH, MAX_MESSAGE_FOR_TOTAL, MAX_MESSAGE_PER_SECOND);
            return;
        }

        public ServerThread(Socket s)throws IOException {
            License license = new License(License.LicenseType.BOTH, MAX_MESSAGE_FOR_TOTAL, MAX_MESSAGE_PER_SECOND);
            this.client = s;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            verification = new Verification();
            start();
        }

        @Override
        public void run() {
            try {
                verification.login(in, out, dataSource, pm, valid_login_per_min,invalid_login_per_min, this.getId());
                username = verification.getUsername();
                password = verification.getPassword();
                License.Availability availability;
                synchronized (threadLock) {
                    user_list.add(username);
                }

                queueName = String.valueOf(this.getId());
                msg = new Message("{}", this.getId());
                msg.setValue("username", username);
                msg.setValue("target", "others");
                msg.init(queueName, "localhost");
                msg.bindTo(exchangeName, queueName);
                messageDeparturer = new MessageDeparturer(msg, out, pm, forwarded_msg);
                msg.setValue("event", "logedin");
                msg.publishToAll(exchangeName);

                String line = in.readLine();
                msg.reset(line);
                while(!"logout".equals(msg.getValue("event"))) {
                    //查看在线用户列表
                    if ("showuser".equals(msg.getValue("event"))) {
                        out.println(listOnlineUsers());
                    } else if ("message".equals(msg.getValue("event"))) {
                        availability = license.use();
                        if (availability != License.Availability.THROUGHPUTEXCEEDED) {
                            msg.setValue("username", username);
                            msg.setValue("event", "message");
                            msg.setValue("target", "others");
                            msg.publishToAll(exchangeName);
                            pm.updateIndex(received_msg, 1);
                        } else {
                            pm.updateIndex(ignored_msg, 1);
                        }
                        if (availability == License.Availability.CAPACITYEXCEEDED) {
                            out.println(new Message("{'event':'relogin','target':'itself'}", this.getId()));
                            verification.login(in, out, dataSource, pm, valid_login_per_min, invalid_login_per_min, this.getId());
                            username = verification.getUsername();
                            password = verification.getPassword();
                            license.reset(License.LicenseType.BOTH);
                        }
                    }
                    line = in.readLine();
                    msg.reset(line);
                }
                msg.setValue("target", "all");
                msg.setValue("event", "quit");
                msg.publishToAll(exchangeName);
            } catch (Exception e) {
                e.printStackTrace();
            } finally { //用户退出聊天室
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
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
        GetConfiguration getConfiguration = new GetConfiguration();
        int SERVER_PORT = getConfiguration.getSERVER_PORT();
        String logFilename = "./log";
        String dbuser = "root";
        String dbpw = "510894";
        Server server = new Server(SERVER_PORT, logFilename, dbuser, dbpw, true);//启动服务端
        server.run();
    }
}

