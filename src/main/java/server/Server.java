package server;

import Authentication.Verification;
import DataSource.DataSource;
import File.SaveToFile;
import MessageUtils.Message;
import MessageUtils.MessageDeparturer;

import PackerUtils.PackerTimer;
import org.apache.log4j.PropertyConfigurator;
import org.json.JSONException;
import utils.Pair;
import utils.Room;
import wheellllll.performance.IntervalLogger;
import wheellllll.performance.RealtimeLogger;
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
import java.util.concurrent.TimeoutException;
import org.apache.log4j.Logger;

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
    private HashMap<String,HashMap<String,Message>> allMsg;

    private IntervalLogger pm;
    private HashMap<String, String> logMap = new HashMap<>();
    private RealtimeLogger messageLogger = new RealtimeLogger();
    // private ArchiveManager am = new ArchiveManager();
    private SaveToFile saveToFile;
    private int secondsOfDay = 86400000;

    public String valid_login_per_min = "validLogin";
    public String invalid_login_per_min = "invalidLogin";
    public String received_msg = "receivedMessage";
    public String ignored_msg = "ignoredMessage";
    public String forwarded_msg = "forwardedMessage";

    private static Logger logger = Logger.getLogger(Server.class);
    private static String LEVEL;

    /**
     * 创建服务端Socket,创建向客户端发送消息线程,监听客户端请求并处理
     */
    public Server(int SERVER_PORT, String logDirname, String dbuser, String dbpw, boolean withLog,
                  int numRoom, int roomSize, String level) throws IOException, TimeoutException, JSONException {
        super(SERVER_PORT);//创建ServerSocket

        this.LEVEL = level;
        PackerTimer packerTimer = new PackerTimer("./log/server/logs","./log/server/zips");
        packerTimer.setInterval(1,TimeUnit.DAYS);
        packerTimer.setPackDateFormat("yyyy-MM-dd mm");
        packerTimer.setbEncryptIt(false);
        packerTimer.start();

        if(LEVEL=="DEBUG"){
            logger.debug("SERVER_PORT:"+SERVER_PORT+";");
        }
        else if(LEVEL=="ERROR"){
            logger.error("SERVER_PORT:"+SERVER_PORT+";");
        }

        this.logDir = logDirname;
        this.withLog = withLog;
        this.roomList = new ArrayList<Room>();
        for (int i = 0; i < numRoom; ++i) {
            roomList.add(new Room(roomSize, i));
        }

        dataSource = new DataSource(dbuser, dbpw);

        Message msg;
        HashMap<String,Message> user2msg;
        allMsg = new HashMap<String,HashMap<String,Message>>();
        ArrayList<Pair<String,String>> res = dataSource.getGroupUser();
        for (Pair<String,String> p : res) {
            msg = new Message("{}", p.getR());
            msg.init(p.getR(), "localhost");
            msg.bindTo(p.getL(), p.getR());
            if (allMsg.containsKey(p.getL())) {
                allMsg.get(p.getL()).put(p.getR(), msg);
            } else {
                user2msg = new HashMap<String,Message>();
                user2msg.put(p.getR(), msg);
                allMsg.put(p.getL(), user2msg);
            }
            System.out.println(p.getL());
            System.out.println(p.getR());
        }
    }

    public void run() throws IOException {
        if (withLog) {
            pm = new IntervalLogger();
            pm.setMaxFileSize(500, wheellllll.performance.Logger.SizeUnit.KB); //第一个参数是数值，第二个参数是单位
            pm.setMaxTotalSize(200, wheellllll.performance.Logger.SizeUnit.MB); //第一个参数是数值，第二个参数是单位
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

            // am.setArchiveDir("./archive");
            // am.setDatePattern("yyyy-MM-dd HH:mm");
            // am.addLogger(pm);
            // am.addLogger(messageLogger);
            // am.setInterval(1, TimeUnit.SECONDS);
            /*
            PackPerWeek msgArchive = new PackPerWeek("./llog","./archive/");
            Timer msgArchiveTimer = new Timer();
            msgArchiveTimer.schedule(msgArchive,secondsOfDay*7,secondsOfDay*7);
            PackPerWeek pmArchive = new PackPerWeek(this.logDir,"./archive/");
            Timer pmArchiveTimer = new Timer();
            pmArchiveTimer.schedule(pmArchive,secondsOfDay*7,secondsOfDay*7);
            */

            PackerTimer packerTimerByDay = new PackerTimer("./log/server/pm","./archive/pm/");
            packerTimerByDay.setInterval(1,TimeUnit.DAYS);
            packerTimerByDay.setPackDateFormat("yyyy-MM-dd mm");
            packerTimerByDay.setbEncryptIt(false);
            packerTimerByDay.start();

            //配置信息归档部分
            PackerTimer packerTimerByWeek = new PackerTimer("./log/server/Msg","./archive/day/");
            packerTimerByWeek.setInterval(7,TimeUnit.DAYS);
            packerTimerByWeek.setPackDateFormat("yyyy-MM-dd mm");
            packerTimerByWeek.setbEncryptIt(false);
            packerTimerByWeek.start();

            pm.start();
            // am.start();

            // saveToFile = new SaveToFile("./log/server/");
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
            // am.stop();
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
                logStatus = verification.login(in, out, dataSource);
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

                username = verification.getUsername();
                password = verification.getPassword();
                License.Availability availability;
                synchronized (threadLock) {
                    user_list.add(username);
                }

                // queueName = String.valueOf(this.getId());
                queueName = username;
                for (String key : allMsg.keySet()) {
                    if (allMsg.get(key).containsKey(username)) {
                        exchangeName = key;
                        msg = allMsg.get(key).get(username);
                    }
                }
                msg.setValue("username", username);
                msg.setValue("target", "others");
                messageDeparturer = new MessageDeparturer(msg, out, pm, forwarded_msg);
                messageDeparturer.beginConsumer();
                msg.setValue("event", "logedin");
                msg.publishToAll(exchangeName);

                String line = in.readLine();
                msg.reset(line);
                while(!"logout".equals(msg.getValue("event"))) {
                    //查看在线用户列表
                    // if ("showuser".equals(msg.getValue("event"))) {
                    if ("message".equals(msg.getValue("event"))) {
                        availability = license.use();
                        if (availability != License.Availability.THROUGHPUTEXCEEDED) {
                            if ("c:showuser".equals(msg.getValue("msg"))) {
                                messageLogger.log(msg.toString());
                                msg.setValue("msg", listOnlineUsers());
                                msg.setValue("event", "list");
                                msg.setValue("target", "itself");
                                msg.publishToOne(exchangeName, username);
                                pm.updateIndex(received_msg, 1);
                            } else {
                                msg.setValue("username", username);
                                msg.setValue("event", "message");
                                msg.setValue("target", "others");
                                msg.publishToAll(exchangeName);
                                messageLogger.log(msg.toString());
                                pm.updateIndex(received_msg, 1);
                            }
                        } else {
                            pm.updateIndex(ignored_msg, 1);
                        }
                        if (availability == License.Availability.CAPACITYEXCEEDED) {
                            out.println(new Message("{'event':'relogin','target':'itself'}", username));
                            logStatus = verification.login(in, out, dataSource);
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
                msg.setValue("username", "quit");
                msg.publishToAll(exchangeName);
            } catch (Exception e) {
                e.printStackTrace();
            } finally { //用户退出聊天室
                try {
                    client.close();
                    messageDeparturer.cancelConsumer();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        //统计在线用户列表
        private String listOnlineUsers() {
            String s = "";
            for (int i = 0; i < user_list.size()-1; i++) {
                s += user_list.get(i)+",";
            }
            s += user_list.get(user_list.size()-1);
            return s;
        }
    }

    public static void main(String[] args) throws IOException, TimeoutException, JSONException {
        Config.setConfigName("configuration/application.conf");                //读取当前目录下的application.conf文件
        PropertyConfigurator.configure("configuration/log4jserver.properties");
        String host = Config.getConfig().getString("SERVER_IP");        //获取host属性，这里会得到localhost
        int port = Config.getConfig().getInt("SERVER_PORT", 9001);        //获取port属性，由于没有设置，故这里会使用默认值9001
        String level = Config.getConfig().getString("LEVEL");
        System.out.println(host);
        System.out.println(port);
        String logDirname = "./log";
        String dbuser = "root";
        String dbpw = "root";
        Server server = new Server(port, logDirname, dbuser, dbpw, true, 10, 10, level);//启动服务端
        server.run();
    }
}

