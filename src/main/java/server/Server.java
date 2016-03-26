package server;

import org.json.JSONException;
import org.json.JSONObject;
import utils.IOLog;
import utils.Message;
import utils.Pair;

import javax.json.Json;
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

    private static int valid_login_per_min = 0;
    private static int invalid_login_per_min = 0;
    private static int received_msg = 0;
    private static int ignored_msg = 0;
    private static int forwarded_msg = 0;
    private static Object threadLock = new Object();
    private static Object loginLock = new Object();
    private static Object msgLock = new Object();
    private static Object forwardLock = new Object();
    private static IOLog ioLog = new IOLog("server.log", true);
    private Timer loginTimer;

    private static final int SERVER_PORT = 2095;

    private static List user_list = new ArrayList();//登录用户集合
    private static List<ServerThread> thread_list = new ArrayList<ServerThread>();//服务器已启用线程集合
    private static LinkedList<Message> msg_list = new LinkedList<Message>();//存放消息队列
    private static DataSource dataSource;

    /**
     * 创建服务端Socket,创建向客户端发送消息线程,监听客户端请求并处理
     */
    public Server()throws IOException {
        super(SERVER_PORT);//创建ServerSocket
        new PrintOutThread();//创建向客户端发送消息线程

        dataSource = new DataSource();
    }

    public void run() throws IOException {
        loginTimer = new Timer();
        loginTimer.schedule(new CheckLoginCount(), 0, 60000);

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
                                    synchronized (forwardLock) {
                                        ++forwarded_msg;
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

    static class CheckLoginCount extends TimerTask {
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
    static class ServerThread extends Thread {
        private Socket client;
        private PrintWriter out;
        private BufferedReader in;
        private String username = "";
        private String password = "";
        private int num_message = 0;
        private int msg_in_second = 0;
        private int MAX_MESSAGE_PER_SECOND = 5;
        private int MAX_MESSAGE_FOR_TOTAL = 10;
        private Timer timer;

        class CheckMessageCount extends TimerTask {
            public void run() {
                msg_in_second = 0;
            }
        }

        public ServerThread() {
            timer = new Timer();
            return;
        }

        public ServerThread(Socket s)throws IOException {
            timer = new Timer();
            this.client = s;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            start();
        }

        public void login(BufferedReader in, PrintWriter out, DataSource dataSource)throws IOException {
            String line;
            Message msg;

            while(true) {
                try {
                    msg = new Message("{}", this.getId());
                    msg.setValue("event", "login");
                    out.println(msg);
                    line = in.readLine();
                    msg = new Message(line, this.getId());
                    username = msg.getValue("username");
                    password = msg.getValue("password");
                    synchronized (loginLock) {
                        if (password.equals(dataSource.getPassword(username))) {
                            ++valid_login_per_min;
                            msg.setValue("event", "valid");
                            out.println(msg);
                            break;
                        } else {
                            ++invalid_login_per_min;
                            msg.setValue("event", "invalid");
                            out.println(msg);
                        }
                    }
                } catch (JSONException e) {
                    continue;
                }
            }

            num_message = 0;
            msg_in_second = 0;
            timer.schedule(new CheckMessageCount(), 0, 1000);

            return;
        }

        @Override
        public void run() {
            try {
                login(in, out, dataSource);
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
                while(!"bye".equals(line)) {
                    //查看在线用户列表
                    if ("showuser".equals(line)) {
                        out.println(listOnlineUsers());
                    } else {
                        synchronized (msgLock) {
                            if (msg_in_second <= MAX_MESSAGE_PER_SECOND) {
                                msg.setValue("event", "message");
                                msg.setValue("target", "others");
                                msg.setValue("msg", line);
                                pushMessage(msg);
                                ++num_message;
                                ++msg_in_second;
                                ++received_msg;
                            } else {
                                ++ignored_msg;
                            }
                        }
                        if (num_message == MAX_MESSAGE_FOR_TOTAL) {
                            out.println(new Message("{'event':'relogin','target':'itself'}", this.getId()));
                            login(in, out, dataSource);
                        }
                    }
                    line = in.readLine();
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
        Server server = new Server();//启动服务端
        server.run();
    }
}

