package server;

import Authentication.Verification;
import DataSource.DataSource;
import File.SaveToFile;
import MessageUtils.Message;
import MessageUtils.MessageDeparturer;

import utils.PackPerDay;
import utils.PackPerWeek;
import utils.Pair;
import utils.Room;
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
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.concurrent.TimeUnit;

/**
 * Created by shieh on 3/20/16.
 */
public class Server extends ServerSocket {
    private static Object threadLock = new Object();
    private boolean withLog = false;
    private String logDir;
    private ArrayList<Room> roomList;

    private static List user_list = new ArrayList();//登录用户集合
    private static DataSource dataSource;

    private IntervalLogger pm;
    private HashMap<String, String> logMap = new HashMap<>();
    private RealtimeLogger messageLogger = new RealtimeLogger();
    private ArchiveManager am = new ArchiveManager();
    private SaveToFile saveToFile;

    public String valid_login_per_min = "validLogin";
    public String invalid_login_per_min = "invalidLogin";
    public String received_msg = "receivedMessage";
    public String ignored_msg = "ignoredMessage";
    public String forwarded_msg = "forwardedMessage";

    /**
     * 创建服务端Socket,创建向客户端发送消息线程,监听客户端请求并处理
     */
    public Server(int SERVER_PORT, String logDirname, String dbuser, String dbpw, boolean withLog,
                  int numRoom, int roomSize)throws IOException {
        super(SERVER_PORT);//创建ServerSocket
        this.logDir = logDirname;
        this.withLog = withLog;
        this.roomList = new ArrayList<Room>();
        for (int i = 0; i < numRoom; ++i) {
            roomList.add(new Room(roomSize, i));
        }

        dataSource = new DataSource(dbuser, dbpw);
    }

    public void run() throws IOException {
        if (withLog) {
            pm = new IntervalLogger();
            pm.setLogDir(this.logDir);   //设置输出文件的路径
            pm.setLogPrefix("Server");  //设置输出的文件名
            pm.setInterval(1, TimeUnit.SECONDS);   //时间单位为秒
            pm.addIndex(valid_login_per_min);
            pm.addIndex(invalid_login_per_min);
            pm.addIndex(received_msg);
            pm.addIndex(ignored_msg);
            pm.addIndex(forwarded_msg);

            messageLogger.setLogDir("./llog");
            messageLogger.setLogPrefix("msg");
            messageLogger.setFormatPattern("Username : ${username}\nTime : ${time}\nMessage : ${message}\n\n");

            am.setArchiveDir("./archive");
            am.setDatePattern("yyyy-MM-dd HH:mm");
            am.addLogger(pm);
            am.addLogger(messageLogger);
            am.setInterval(1, TimeUnit.SECONDS);

            pm.start();
            am.start();

            saveToFile = new SaveToFile("./log/server/");
            PackPerDay packPerDay = new PackPerDay("./log/server","./archive/day/");
            PackPerWeek packPerWeek = new PackPerWeek("./archive/day/","./archive/week/");
            Timer timer;
            timer = new Timer();
            timer.schedule(packPerDay,86400000,86400000);

            Timer timer2;
            timer2 = new Timer();
            timer2.schedule(packPerWeek,86400000*7,86400000*7);
        }

        try {
            while(true){//监听客户端请求，启个线程处理
                Socket socket = accept();
                new ServerThread(socket);
            }
        }catch (Exception e) {
        }finally{
            close();
            pm.stop();
            am.stop();
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
        private Room room;
        public Message msg;
        public MessageDeparturer messageDeparturer;
        String exchangeName = "test";
        String queueName;

        public ServerThread() {
            license = new License(License.LicenseType.BOTH, MAX_MESSAGE_FOR_TOTAL, MAX_MESSAGE_PER_SECOND);
            return;
        }

        public ServerThread(Socket s)throws IOException {
            license = new License(License.LicenseType.BOTH, MAX_MESSAGE_FOR_TOTAL, MAX_MESSAGE_PER_SECOND);
            this.client = s;
            out = new PrintWriter(client.getOutputStream(),true);
            in = new BufferedReader(new InputStreamReader(client.getInputStream()));
            verification = new Verification();
            start();
        }

        @Override
        public void run() {
            Pair<Integer, Integer> logStatus;
            try {
                logStatus = verification.login(in, out, dataSource, this.getId());
                pm.updateIndex(valid_login_per_min, logStatus.getL());
                pm.updateIndex(invalid_login_per_min, logStatus.getR());

                for (Room r : roomList) {
                    if (r.addOne(this.getId())) {
                        room = r;
                        break;
                    }
                }
                if (room == null) {
                }

                exchangeName = String.valueOf(room.getID());

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
                            System.out.println(msg);
                            msg.setValue("username", username);
                            msg.setValue("event", "message");
                            msg.setValue("target", "others");
                            msg.publishToAll(exchangeName);
                            messageLogger.log(msg.toString());
                            pm.updateIndex(received_msg, 1);
                        } else {
                            pm.updateIndex(ignored_msg, 1);
                        }
                        if (availability == License.Availability.CAPACITYEXCEEDED) {
                            out.println(new Message("{'event':'relogin','target':'itself'}", this.getId()));
                            logStatus = verification.login(in, out, dataSource, this.getId());
                            pm.updateIndex(valid_login_per_min, logStatus.getL());
                            pm.updateIndex(invalid_login_per_min, logStatus.getR());

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
        Config.setConfigName("./application.conf");                //读取当前目录下的application.conf文件
        String host = Config.getConfig().getString("SERVER_IP");        //获取host属性，这里会得到localhost
        int port = Config.getConfig().getInt("SERVER_PORT", 9001);        //获取port属性，由于没有设置，故这里会使用默认值9001
        System.out.println(host);
        System.out.println(port);
        String logDirname = "./log";
        String dbuser = "root";
        String dbpw = "510894";
        Server server = new Server(port, logDirname, dbuser, dbpw, true, 10, 10);//启动服务端
        server.run();
    }
}

