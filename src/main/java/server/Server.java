package server;

import com.sun.tools.javac.comp.Check;
import org.json.JSONException;
import utils.*;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Timer;

/**
 * Created by shieh on 3/20/16.
 */
public class Server extends ServerSocket {
    private static Object threadLock = new Object();
    private boolean withLog = false;
    private String logFile;

    private static List user_list = new ArrayList();//登录用户集合
    private static List<ServerThread> thread_list = new ArrayList<ServerThread>();//服务器已启用线程集合
    private static LinkedList<Message> msg_list = new LinkedList<Message>();//存放消息队列
    private static DataSource dataSource;

    private Logger logger;
    private License license;

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
        new PrintOutThread();//创建向客户端发送消息线程
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
                    synchronized (msg_list) {
                        if (msg_list.size() > 0) {//将缓存在队列中的消息按顺序发送到各客户端，并从队列中清除。
                            Message msg = msg_list.getFirst();
                            for (ServerThread thread : thread_list) {
                                if (msg.getValue("target").equals("all") ||
                                        (msg.getValue("target").equals("others") && msg.getOwner() != thread.getId()) ||
                                        (msg.getValue("target").equals("itself") && msg.getOwner() == thread.getId())) {
                                    thread.sendMessage(msg.toString());
                                    logger.addCount(forwarded_msg);
                                }
                            }
                            msg_list.removeFirst();
                        }
                    }
                } catch (Exception e) {
                }
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
        private int MAX_MESSAGE_PER_SECOND = 5;
        private int MAX_MESSAGE_FOR_TOTAL = 10;
        private Verification verification;
        private License license;

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

                Message msg = new Message("{}", this.getId());
                msg.setValue("username", username);
                msg.setValue("target", "others");

                synchronized(threadLock) {
                    user_list.add(username);
                    thread_list.add(this);
                    msg.setValue("event", "logedin");
                    this.pushMessage(msg);
                }

                String line = in.readLine();
                msg = new Message(line, this.getId());
                while(!"logout".equals(msg.getValue("event"))) {
                    //查看在线用户列表
                    if ("showuser".equals(msg.getValue("event"))) {
                        out.println(listOnlineUsers());
                    } else if ("message".equals(msg.getValue("event"))) {
                        if (license.checkMsgInSecond()) {
                            msg.setValue("username", username);
                            msg.setValue("event", "message");
                            msg.setValue("target", "others");
                            pushMessage(msg);
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
                    msg = new Message(line, this.getId());
                }
                msg.setValue("target", "all");
                msg.setValue("event", "quit");
                pushMessage(msg);
            } catch (Exception e) {
                e.printStackTrace();
            } finally { //用户退出聊天室
                try {
                    client.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                license.cancel();
                synchronized (threadLock) {
                    thread_list.remove(this);
                    user_list.remove(username);
                }
            }
        }

        //放入消息队列末尾，准备发送给客户端
        private void pushMessage(Message msg){
            synchronized(msg_list) {
                msg_list.addLast(msg);
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
        int SERVER_PORT = 2095;
        String logFilename = "server.log";
        String dbuser = "root";
        String dbpw = "root";
        Server server = new Server(SERVER_PORT, logFilename, dbuser, dbpw, true);//启动服务端
        server.run();
    }
}

