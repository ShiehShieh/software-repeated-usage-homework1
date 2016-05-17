package src.main.java.Client;

import org.json.JSONException;

import src.main.java.PackerUtils.PackPerDay;
import src.main.java.PackerUtils.PackPerWeek;
import wheellllll.config.Config;
import wheellllll.performance.IntervalLogger;
import MessageUtils.Message;
import wheellllll.performance.Logger;
import wheellllll.performance.RealtimeLogger;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.TimeUnit;

/**
 * Created by Xiemingyue & Jipengyue on 3/29/16.
 */
public class Client  extends Socket {

    private static String SERVER_IP;
    private static int SERVER_PORT;

    private IntervalLogger pm;
    public String login_success = "login successfully";
    public String login_fail = "login failed";
    public String receive_msg = "receive message";
    public String send_msg = "send message";

    private static Object loginLock = new Object();

    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader strin;
    private final Object stdinLock = new Object();
    private boolean stdinFlag;
    private Timer timer;
    private Timer timer2;

    //用于存放登录的用户名和密码
    Map<String,String> map=new HashMap<String,String>();
    private String nameForFile="";

    private RealtimeLogger pm_Msg;
    HashMap<String, String> mapLog = new HashMap<>();

    private static List user_list = new ArrayList();

    public  Client(String SERVER_IP, int SERVER_PORT, String logDir)throws Exception{
        super(SERVER_IP, SERVER_PORT);
        client =this;
        Message msg;
        out =new PrintWriter(this.getOutputStream(),true);
        in =new BufferedReader(new InputStreamReader(this.getInputStream()));
        strin = new BufferedReader(new InputStreamReader(System.in));

        pm = new IntervalLogger();
        pm.setLogDir(logDir + "/pm/");
        System.out.println(logDir+"/pm");
        pm.setLogPrefix("Client");
        pm.setLogSuffix("log");
        pm.setDateFormat("yyyy-MM-dd HH_mm_ss");

        pm.setMaxFileSize(100, IntervalLogger.SizeUnit.KB);
        pm.setMaxTotalSize(1, IntervalLogger.SizeUnit.MB);

        pm.setInterval(1,TimeUnit.MINUTES);
        pm.setInitialDelay(1);

        pm.addIndex(login_success);
        pm.addIndex(login_fail);
        pm.addIndex(receive_msg);
        pm.addIndex(send_msg);

        pm.start();

        String input;

        pm_Msg = new RealtimeLogger();
        pm_Msg.setLogDir(logDir + "/Msg/");
        pm_Msg.setLogPrefix("Msg");
        pm_Msg.setFormatPattern("Username : ${username}\nTime : ${time}\nMessage : ${message}\n\n");

        pm_Msg.setMaxFileSize(100,RealtimeLogger.SizeUnit.KB);
        pm_Msg.setMaxFileSize(1,RealtimeLogger.SizeUnit.MB);


        PackPerDay packPerDay = new PackPerDay("./log/client/Msg","./archive/day/");
        PackPerWeek packPerWeek = new PackPerWeek("./archive/day/","./archive/week/");

        timer = new Timer();
        timer.schedule(packPerDay,86400000,86400000);


        timer2 = new Timer();
        timer2.schedule(packPerWeek,86400000*7,86400000*7);

        readLineThread rt = new readLineThread();

        stdinFlag = false;
        while(true){
            Thread.sleep(100);
            if (stdinFlag) {
                input = strin.readLine();
                if(input.equals("quit") == false) {
                    msg = new Message("{}", 0);
                    msg.setValue("msg", input);
                    msg.setValue("event", "message");
                    msg.setValue("username", nameForFile);
                    out.println(msg);
                    System.out.println(msg);

                    mapLog.put("username", nameForFile);
                    mapLog.put("time", new Date().toString());
                    mapLog.put("message", input);
                    pm_Msg.log(mapLog);

                    pm.updateIndex(send_msg, 1);
                }
                else {
                    msg = new Message("{}", 0);
                    msg.setValue("msg", input);
                    msg.setValue("event", "logout");
                    msg.setValue("username", nameForFile);
                    System.out.println(msg);
                    out.println(msg);
                    rt.readLinestop();
                    break;
                }
            }
        }
        System.out.println("Bye");
    }

    protected void finalized(){
        if(pm != null){
            pm.stop();
        }
        timer.cancel();
        timer2.cancel();
    }

    //登录
    public void loginClient() throws IOException{
        String line;
        Message msgClient;
        String username;
        String password;
        stdinFlag = false;

        while (true) {
            try {
                System.out.print("please input the username?");
                username = strin.readLine();
                System.out.println("please input the password?");
                password = strin.readLine();
                msgClient = new Message("{}", 0);
                msgClient.setValue("event", "login");
                msgClient.setValue("username", username);
                msgClient.setValue("password", password);
                out.println(msgClient);
                line = in.readLine();
                msgClient = new Message(line, 0);
                if (msgClient.getValue("event").equals("valid")) {
                    pm.updateIndex(login_success,1);
                    System.out.println("login successfully, please input the message:");
                    map.put(msgClient.getValue("username"), msgClient.getValue("password"));
                    nameForFile = username;
                    break;
                }
                if (msgClient.getValue("event").equals("invalid")) {                            //登录失败
                    pm.updateIndex(login_fail,1);
                    System.out.println("invalid input, please login again");
                }
                else{                                                                           //用户登录超时，登录失败
                    pm.updateIndex(login_fail,1);
                    System.out.println("login timeout, please login again:");
                }

            } catch (JSONException e) {
                continue;
            }
        }
        stdinFlag = true;
    }

    /**
     * 用于监听服务器端向客户端发送消息线程类
     */
    class readLineThread extends Thread{

        private volatile boolean isStop = false;
        private BufferedReader buff;
        public readLineThread(){
            try {
                buff =new BufferedReader(new InputStreamReader(client.getInputStream()));
                start();
            }catch (Exception e) {
            }
        }

        public void readLinestop() {
            isStop = true;
            this.interrupt();
        }

        @Override
        public void run() {
            Message msgClient;
            try {
                while(!isStop){
                    String result = buff.readLine();
                    System.out.println(result);
                    if(result != null){

                        msgClient = new Message(result, this.getId());
                        System.out.println(msgClient);

                        long id=this.getId();
                        if(msgClient.getValue("event").equals("list")){
                            String[] arr = msgClient.getValue("msg").split(",");
                            for(int i=0;i<arr.length;i++){
                                user_list.add(arr[i]);
                                System.out.println(arr[i]);
                            }
                        } else
                        if(msgClient.getValue("event").equals("quit")){
                            System.out.println("user: "+msgClient.getValue("username")+" quit.");
                            for(int i=0;i<user_list.size();i++){
                                if(user_list.get(i).equals(msgClient.getValue("username"))){
                                    user_list.remove(i);
                                    break;
                                }
                            }
                        } else if (msgClient.getValue("event").equals("login")) {
                            loginClient();

                        } else if (msgClient.getValue("event").equals("relogin")) {
                            loginClient();
                        } else if (msgClient.getValue("event").equals("logedin")) {
                            System.out.println("user: "+msgClient.getValue("username")+" loged in.");
                            if(user_list.size()!=0)
                                user_list.add(msgClient.getValue("username"));
                            for(int i=0;i<user_list.size();i++)
                                System.out.print(user_list.get(i)+" ");
                        } else if (msgClient.getValue("event").equals("message")) {
                            System.out.println(msgClient.getValue("username")+" said: "+msgClient.getValue("msg"));
                            //???????
                            mapLog.put("username", msgClient.getValue("username"));
                            mapLog.put("time", new Date().toString());
                            mapLog.put("message", msgClient.getValue("msg"));
                            pm_Msg.log(mapLog);
                            System.out.println(msgClient.getValue("username")+": "+msgClient.getValue("msg"));
                            pm_Msg.log(msgClient.getValue("username")+": "+msgClient.getValue("msg"));
                            pm.updateIndex(receive_msg,1);
                        }
                        synchronized (stdinLock) {
                            stdinLock.notify();
                        }
                    }
                }
                in.close();
                out.close();
                client.close();
            }catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void main(String[] args) {
        Config.setConfigName("./configuration/application.conf");
        String host = Config.getConfig().getString("SERVER_IP");
        int port = Config.getConfig().getInt("SERVER_PORT");
        String logDir = "./log/client";

        try {
            Client client = new Client(host, port, logDir);
            client.finalized();
        }catch (Exception e) {
        }
    }
}