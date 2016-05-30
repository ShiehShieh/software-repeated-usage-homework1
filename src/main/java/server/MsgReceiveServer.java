package src.main.java.server;

import com.rabbitmq.client.*;
import org.json.JSONException;
import src.main.java.DataSource.DataSource;
import src.main.java.MessageUtils.Message;
import src.main.java.PackerUtils.PackerTimer;
import utils.Pair;
import wheellllll.config.Config;
import wheellllll.license.License;
import wheellllll.performance.IntervalLogger;
import wheellllll.performance.Logger;
import wheellllll.performance.RealtimeLogger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Created by Siyao on 16/5/27.
 */
public class MsgReceiveServer extends ServerSocket{

    private static Object loginThreadLock = new Object();
    private static Object listThreadLock = new Object();

    private boolean withLog = true;

    private String logDir;
    private String zipDir;
    private IntervalLogger pm;
    private String received_msg = "receivedMessage";

    private static DataSource dataSource;

    private RealtimeLogger messageLogger;

    private PackerTimer pmPacker;
    private PackerTimer pmPackerWeek;
    private PackerTimer msgPacker;
    private PackerTimer msgPackerWeek;

    public Message msg;
    public Message loginMsg;
    public Message normalMsg;
    public Message logoutMsg;
    public Message reloginMsg;

    private Consumer loginSuccessConsumer;

    //在线用户集合 即登录成功用户集合
    private static List<String> user_list = new ArrayList<String>();

    //用户群租分发
    private HashMap<String,HashMap<String,Message>> allMsg;

    public MsgReceiveServer(int SERVER_PORT, String logDirname, String zipDirname, String dbUser, String dbPwd, boolean withLog)
            throws IOException, TimeoutException, JSONException {

        super(SERVER_PORT);

        this.withLog = withLog;
        this.logDir = logDirname;
        this.zipDir = zipDirname;

        dataSource = new DataSource(dbUser, dbPwd);

        HashMap<String,Message> user2msg;
        allMsg = new HashMap<String,HashMap<String,Message>>();
        ArrayList<Pair<String,String>> res = dataSource.getGroupUser();
        for (Pair<String,String> p : res) {
            msg = new Message("{}", p.getR());
            msg.init(p.getR(), "localhost");
            msg.bindTo(p.getL(), p.getR());
            if (allMsg.containsKey(p.getL())) {
                allMsg.get(p.getL()).put(p.getR(), msg);
            }
            else {
                user2msg = new HashMap<String, Message>();
                user2msg.put(p.getR(), msg);
                allMsg.put(p.getL(), user2msg);
            }
        }

        loginMsg = new Message("{}", "");
        loginMsg.init("login_request","localhost");
        loginMsg.bindTo("login_auth","login_request");

        normalMsg = new Message("{}", "");
        normalMsg.init("normal_msg","localhost");
        normalMsg.bindTo("msg_send","normal_msg");

        logoutMsg = new Message("{}", "");
        logoutMsg.init("logout_msg","localhost");
        logoutMsg.bindTo("msg_send","logout_msg");

        reloginMsg = new Message("{}", "");
        reloginMsg.init("relogin_msg","localhost");
        reloginMsg.bindTo("msg_send","relogin_msg");

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = factory.newConnection();
        Channel loginSuccessChannel = connection.createChannel();
        loginSuccessChannel.queueDeclare("login_success", true, false, false, null);
        loginSuccessConsumer = new DefaultConsumer(loginSuccessChannel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
                    throws IOException {
                String sLoginSuccessMsg = new String(body, "UTF-8");
                try {
                    Message loginSuccessMsg = new Message("{}", "");
                    loginSuccessMsg.reset(sLoginSuccessMsg);
                    String loginSuccessUsername = loginSuccessMsg.getValue("username");
                    user_list.add(loginSuccessUsername);
                    System.out.println(user_list.size());
                } catch (JSONException e) {
                    e.printStackTrace();
                }
            }
        };
        loginSuccessChannel.basicConsume("login_success", true, loginSuccessConsumer);

    }

    public void run() throws IOException {
        if (withLog) {
            //接受消息计数
            pm = new IntervalLogger();
            pm.setMaxFileSize(500, Logger.SizeUnit.KB);
            pm.setMaxTotalSize(200, Logger.SizeUnit.MB);
            pm.setLogDir(this.logDir + "/pm");
            pm.setLogPrefix("Server");
            pm.setInterval(1, TimeUnit.MINUTES);
            pm.addIndex(received_msg);
            pm.start();

            //接收消息存档
            messageLogger = new RealtimeLogger();
            messageLogger.setLogDir(this.logDir + "/msg");
            messageLogger.setLogPrefix("msg");
            messageLogger.setFormatPattern("Username : ${username}\nTime : ${time}\nMessage : ${message}\n\n");

            //接收消息计数打包
            pmPacker = new PackerTimer(this.logDir + "/pm", this.zipDir + "/pm/day");
            pmPacker.setInterval(1,TimeUnit.DAYS);
            pmPacker.setDelay(1,TimeUnit.DAYS);
            pmPacker.setPackDateFormat("yyyy-MM-dd");
            pmPacker.setbEncryptIt(true);
            pmPacker.start();

            //接收消息计数周信息归档
            pmPackerWeek = new PackerTimer(this.zipDir + "/pm/day", this.zipDir + "/pm/week");
            pmPackerWeek.setInterval(7,TimeUnit.DAYS);
            pmPackerWeek.setDelay(7,TimeUnit.DAYS);
            pmPackerWeek.setPackDateFormat("yyyy-MM-dd");
            pmPackerWeek.setbUnpack(true);
            pmPackerWeek.setbEncryptIt(true);
            pmPackerWeek.start();

            //接收消息打包
            msgPacker = new PackerTimer(this.logDir + "/msg", this.zipDir + "/msg/day");
            msgPacker.setInterval(1,TimeUnit.DAYS);
            msgPacker.setDelay(1,TimeUnit.DAYS);
            msgPacker.setPackDateFormat("yyyy-MM-dd");
            msgPacker.setbEncryptIt(true);
            msgPacker.start();

            //接收消息周信息归档
            msgPackerWeek = new PackerTimer(this.zipDir + "/msg/day", this.zipDir + "/msg/week");
            msgPackerWeek.setInterval(7,TimeUnit.DAYS);
            msgPackerWeek.setDelay(7,TimeUnit.DAYS);
            msgPackerWeek.setPackDateFormat("yyyy-MM-dd");
            msgPackerWeek.setbUnpack(true);
            msgPackerWeek.setbEncryptIt(true);
            msgPackerWeek.start();

        }

        try {
            while(true){
                Socket client = accept();
                new ReceiveMsgServerThread(client);
            }
        }catch (Exception e) {
        }finally{
            close();
            pm.stop();
            pmPacker.stop();
            pmPackerWeek.stop();
            msgPacker.stop();
            msgPackerWeek.stop();
        }
    }


    class ReceiveMsgServerThread extends Thread {

        private Socket client;
        private BufferedReader in;

        private License license;
        private int MAX_MESSAGE_PER_SECOND = 5;
        private int MAX_MESSAGE_FOR_TOTAL = 10;

        String username = null;
        String queueName = null;
        String exchangeName = null;

        Message msg;

        public ReceiveMsgServerThread() {
            license = new License(License.LicenseType.BOTH, MAX_MESSAGE_FOR_TOTAL, MAX_MESSAGE_PER_SECOND);
            return;
        }

        public ReceiveMsgServerThread(Socket s) throws IOException, JSONException {
            license = new License(License.LicenseType.BOTH, MAX_MESSAGE_FOR_TOTAL, MAX_MESSAGE_PER_SECOND);
            this.client = s;

            in = new BufferedReader(new InputStreamReader(client.getInputStream()));

            start();
        }

        @Override
        public void run() {

            try {
                msg = new Message("{}", "");
                String sMsg = in.readLine();
                System.out.println(sMsg);
                if(sMsg != null){
                    msg.reset(sMsg);

                    while(!msg.getValue("event").equals("logout")){
                        //许可验证
                        License.Availability availability = license.use();

                        //许可验证通过
                        if(availability == License.Availability.AVAILABLE){
                            //转发登录消息至鉴权
                            if(msg.getValue("event").equals("login")){
                                loginMsg.reset(sMsg);

                                synchronized (loginThreadLock) {
                                    loginMsg.publishToOne("login_auth","login_request");
                                }

                                //重置验证
                                license.reset(License.LicenseType.BOTH);
                            }

                            //转发普通消息
                            else if(msg.getValue("event").equals("message") && user_list.contains(msg.getValue("username"))){
                                pm.updateIndex(received_msg,1);

                                if(queueName == null){
                                    username = msg.getValue("username");
                                    queueName = msg.getValue("username");
                                    for (String key : allMsg.keySet()) {
                                        if (allMsg.get(key).containsKey(queueName)) {
                                            exchangeName = key;
                                            System.out.println(exchangeName);
                                            msg = allMsg.get(key).get(queueName);
                                            msg.reset(sMsg);
                                        }
                                    }
                                }

                                if(msg.getValue("msg").equals("c:showuser")){
                                    msg.setValue("msg", listOnlineUsers());
                                    msg.setValue("event", "list");
                                    msg.setValue("target", "itself");
                                    msg.publishToOne(exchangeName, msg.getValue("username"));

                                    messageLogger.log(msg.toString());
                                }
                                else{
                                    msg.setValue("target", "others");
                                    msg.publishToAll(exchangeName);

                                    messageLogger.log(msg.toString());
                                }
                            }
                        }
                        else{
                            reloginMsg.setValue("username", username);
                            reloginMsg.publishToOne("msg_send","relogin_msg");
                        }

                        sMsg = in.readLine();
                        msg.reset(sMsg);
                        System.out.println(sMsg);
                    }

                    //转发登出消息
                    if(msg.getValue("event").equals("logout") && user_list.contains(msg.getValue("username"))){
                        logoutMsg.reset(sMsg);
                        logoutMsg.publishToOne("msg_send","logout_msg");

                        msg.setValue("target", "others");
                        msg.setValue("event", "quit");
                        msg.setValue("username", msg.getValue("username"));
                        msg.publishToAll(exchangeName);

                        synchronized (listThreadLock) {
                            Iterator<String> it_user = user_list.iterator();
                            while(it_user.hasNext()){
                                if(it_user.next().equals(msg.getValue("username"))){
                                    it_user.remove();
                                    break;
                                }
                            }
                        }

                        client.close();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String listOnlineUsers() {
            String s = "";
            for (int i = 0; i < user_list.size()-1; i++) {
                s += user_list.get(i)+",";
                System.out.println(s);
            }
            s += user_list.get(user_list.size()-1);
            return s;
        }
    }
    public static void main(String[] args) throws IOException, TimeoutException, JSONException {
        Config.setConfigName("./application.conf");
        String host = Config.getConfig().getString("SERVER_IP");
        int port = Config.getConfig().getInt("SERVER_PORT", 9001);
        String logDirname = "./log/server";
        String zipDirname = "./archive/server";
        String dbUser = "root";
        String dbPwd = "Wsy_07130713";
        MsgReceiveServer msgReceiveServer = new MsgReceiveServer(port, logDirname, zipDirname, dbUser, dbPwd, true);
        msgReceiveServer.run();
    }
}
