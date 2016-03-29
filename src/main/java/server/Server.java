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
    private Timer loginTimer;
    private boolean withLog = false;
    private String logFile;

    private static List user_list = new ArrayList();//登录用户集合
    private static List<ServerThread> thread_list = new ArrayList<ServerThread>();//服务器已启用线程集合
    private static LinkedList<Message> msg_list = new LinkedList<Message>();//存放消息队列
    private static DataSource dataSource;

    private CheckCount checkCount;

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
            checkCount = new CheckCount(this.logFile);
            checkCount.addCountType(valid_login_per_min);
            checkCount.addCountType(invalid_login_per_min);
            checkCount.addCountType(received_msg);
            checkCount.addCountType(ignored_msg);
            checkCount.addCountType(forwarded_msg);
            loginTimer = new Timer();
            loginTimer.schedule(checkCount, 0, 60000);
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
                                    synchronized (checkCount.getLock(forwarded_msg)) {
                                        checkCount.addCount(forwarded_msg);
                                    }
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
        private Timer timer;
        private Verification verification;
        private MessageCount messageCount;

        public ServerThread() {
            timer = new Timer();
            return;
        }

        public ServerThread(Socket s)throws IOException {
            timer = new Timer();
            this.client = s;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            verification = new Verification();
            messageCount = new MessageCount();
            start();
        }

        @Override
        public void run() {
            try {
                verification.login(in, out, dataSource, checkCount, valid_login_per_min,invalid_login_per_min, this.getId());
                username = verification.getUsername();
                password = verification.getPassword();
                messageCount.reset();
                timer.schedule(messageCount, 0, 1000);

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
                        if (messageCount.getMsgInSecond() <= MAX_MESSAGE_PER_SECOND) {
                            msg.setValue("username", username);
                            msg.setValue("event", "message");
                            msg.setValue("target", "others");
                            pushMessage(msg);
                            messageCount.increaseMsg();
                            synchronized (checkCount.getLock(received_msg)) {
                                checkCount.addCount(received_msg);
                            }
                        } else {
                            synchronized (checkCount.getLock(ignored_msg)) {
                                checkCount.addCount(ignored_msg);
                            }
                        }
                        if (messageCount.getMsgTotal() == MAX_MESSAGE_FOR_TOTAL) {
                            out.println(new Message("{'event':'relogin','target':'itself'}", this.getId()));
                            verification.login(in, out, dataSource, checkCount, valid_login_per_min, invalid_login_per_min, this.getId());
                            username = verification.getUsername();
                            password = verification.getPassword();
                            messageCount.reset();
                        }
                    }
                    line = in.readLine();
                    msg = new Message(line, this.getId());
                    System.out.println(msg);
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
                timer.cancel();
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
        String dbpw = "510894";
        Server server = new Server(SERVER_PORT, logFilename, dbuser, dbpw, true);//启动服务端
        server.run();
    }
}

