package server;

import Authentication.Verification;
import CM.GetConfiguration;
import DataSource.DataSource;
import License.License;
import Logging.Logger;
import MessageUtils.Message;
import MessageUtils.MessageDeparturer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shieh on 3/20/16.
 */
public class Server extends ServerSocket {
    private static Object threadLock = new Object();
    private boolean withLog = false;
    private String logFile;

    private static List user_list = new ArrayList();//登录用户集合
    private static DataSource dataSource;

    private Logger logger;

    public String valid_login_per_min = "valid login per min: ";
    public String invalid_login_per_min = "invalid login per min: ";
    public String received_msg = "received message: ";
    public String ignored_msg = "ignored message: ";
    public String forwarded_msg = "forwarded message: ";

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
            System.out.println("Logging into " + this.logFile);
            logger = new Logger(this.logFile);
            logger.addCountType(valid_login_per_min);
            logger.addCountType(invalid_login_per_min);
            logger.addCountType(received_msg);
            logger.addCountType(ignored_msg);
            logger.addCountType(forwarded_msg);
            logger.setTime(0, 60000);
            logger.commence();
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
            license = new License(MAX_MESSAGE_PER_SECOND, MAX_MESSAGE_FOR_TOTAL, 0, 1000);
            return;
        }

        public ServerThread(Socket s)throws IOException {
            license = new License(MAX_MESSAGE_PER_SECOND, MAX_MESSAGE_FOR_TOTAL, 0, 1000);
            this.client = s;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            verification = new Verification();
            start();
        }

        @Override
        public void run() {
            try {
                verification.login(in, out, dataSource, logger, valid_login_per_min,invalid_login_per_min, this.getId());
                username = verification.getUsername();
                password = verification.getPassword();
                license.reset();
                license.commence();
                synchronized (threadLock) {
                    user_list.add(username);
                }

                queueName = String.valueOf(this.getId());
                msg = new Message("{}", this.getId());
                msg.setValue("username", username);
                msg.setValue("target", "others");
                msg.init(queueName, "localhost");
                msg.bindTo(exchangeName, queueName);
                messageDeparturer = new MessageDeparturer(msg, out, logger, forwarded_msg);
                msg.setValue("event", "logedin");
                msg.publishToAll(exchangeName);

                String line = in.readLine();
                msg.reset(line);
                while(!"logout".equals(msg.getValue("event"))) {
                    //查看在线用户列表
                    if ("showuser".equals(msg.getValue("event"))) {
                        out.println(listOnlineUsers());
                    } else if ("message".equals(msg.getValue("event"))) {
                        if (license.checkMsgInSecond()) {
                            msg.setValue("username", username);
                            msg.setValue("event", "message");
                            msg.setValue("target", "others");
                            msg.publishToAll(exchangeName);
                            license.increaseMsg();
                            logger.addCount(received_msg);
                        } else {
                            logger.addCount(ignored_msg);
                        }
                        if (!license.checkTotalMsg()) {
                            out.println(new Message("{'event':'relogin','target':'itself'}", this.getId()));
                            verification.login(in, out, dataSource, logger, valid_login_per_min, invalid_login_per_min, this.getId());
                            username = verification.getUsername();
                            password = verification.getPassword();
                            license.reset();
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
                license.cancel();
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
        String logFilename = "server.log";
        String dbuser = "root";
        String dbpw = "510894";
        Server server = new Server(SERVER_PORT, logFilename, dbuser, dbpw, true);//启动服务端
        server.run();
    }
}

