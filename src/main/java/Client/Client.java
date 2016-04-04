package Client;

import org.json.JSONException;

import CM.GetConfiguration;
import Logging.CheckCount;
import Logging.IOLog;
import MessageUtils.Message;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by Xiemingyue & Jipengyue on 3/29/16.
 */
public class Client  extends Socket {

	private static GetConfiguration getConfiguration = new GetConfiguration();
    private static String SERVER_IP = getConfiguration.getSERVER_IP();
    private static int SERVER_PORT = getConfiguration.getSERVER_PORT();
    private static Object loginLock = new Object();

    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    private BufferedReader strin;
    private final Object stdinLock = new Object();
    private boolean stdinFlag;
    //经过server判断之后登录成功的次数
    private int loginSuccess=0;
    //登录失败的次数
    private int loginFail=0;
    //发送的消息数量
    private int send_message=0;
    //从服务端收到的消息数量
    private int received_message=0;
    //用于每分钟存入文件的计时器
    private Timer SaveMsgTimer;
    //同步锁
    private static Object SendMsgLock = new Object();
    //写入文件
    private IOLog ClientLog;
    //用于存放登录的用户名和密码
    Map<String,String> map=new HashMap<String,String>();
    //客户端输入
    String input="";
    private String nameForFile="";
    //发送和接受消息次数的计时器
    private Timer SendReceive;
    //登录成功和失败次数的计时器
    private Timer SuccessFail;
    //计数器:登录
    private CheckCount countLogin;
    //计数器：消息
    private CheckCount countMsg;

    public String login_success = "login successfully ";
    public String login_fail = "login failed ";
    public String receive_msg = "receive message ";
    public String send_msg = "send message ";
    /**
     * 与服务器连接，并输入发送消息
     */
    public  Client()throws Exception{
        super(SERVER_IP, SERVER_PORT);
        client =this;
        Message msg;
        out =new PrintWriter(this.getOutputStream(),true);
        in =new BufferedReader(new InputStreamReader(this.getInputStream()));
        strin = new BufferedReader(new InputStreamReader(System.in));
        String input;
        String getFromMap=map.get("username");
        SuccessFail = new Timer();
        countLogin = new CheckCount(new SimpleDateFormat("yyyyMMdd").format(Calendar.getInstance().getTime())+".log");
        countLogin.addCountType(login_success);
        countLogin.addCountType(login_fail);
        SuccessFail.schedule(countLogin,0,60000);
        readLineThread rt = new readLineThread();

        stdinFlag = false;
        while(true){
            Thread.sleep(100);
            if (stdinFlag) {
                System.out.println("a");
                input = strin.readLine();
                msg = new Message("{}", 0);
                msg.setValue("msg", input);
                msg.setValue("event", "message");
                out.println(msg);
                countMsg.addCount(send_msg);

            }
        }
    }

    //创建文件
    public String createFile(String lastname){
        File f = new File(".");
        // fileName表示你创建的文件名；为txt类型；
        String fileName=lastname+".txt";
        //String fileName="1234";
        File file = new File(f,fileName);
        if(!file.exists()){
            try {
                file.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        }
        return fileName;
    }

    //处理登录
    public void loginClient() throws IOException{
        String line;
        Message msgClient;
        String username;
        String password;

        stdinFlag = false;

        while (true) {
            try {
                System.out.print("please input the username：");
                username = strin.readLine();
                System.out.println("please input the password：");
                password = strin.readLine();
                msgClient = new Message("{}", 0);
                msgClient.setValue("event", "login");
                msgClient.setValue("username", username);
                msgClient.setValue("password", password);
                out.println(msgClient);
                line = in.readLine();
                msgClient = new Message(line, 0);
                if (msgClient.getValue("event").equals("valid")) {
                    //登录成功次数+1
                    countLogin.addCount(login_success);
                    //提示用户登录成功
                    System.out.println("login successfully, please input the message:");
                    //将成功登录的用户名和密码存入Map
                    map.put(msgClient.getValue("username"), msgClient.getValue("password"));
                    //  map.put(username, password);
                    nameForFile=username;
                    //关闭计时器：记录登录成功失败
                  //  SuccessFail.cancel();
                    //如果用户名存在的话，进行计数器的创建
                    countMsg = new CheckCount(nameForFile+".log");
                    //将需要计数的东西加入计数器
                    countMsg.addCountType(receive_msg);
                    countMsg.addCountType(send_msg);
                    SendReceive = new Timer();
                    SendReceive.schedule(countMsg, 0,60000);
                    break;
                }
                if (msgClient.getValue("event").equals("invalid")) {//登录失败
                    //登录失败次数+1
                    countLogin.addCount(login_fail);
                    //提示用户登录失败
                    System.out.println("invalid input, please login again");
                }
                else{//用户登录超时，登录失败
                    countLogin.addCount(login_fail);
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

        private BufferedReader buff;
        public readLineThread(){
            try {
                buff =new BufferedReader(new InputStreamReader(client.getInputStream()));
                start();
            }catch (Exception e) {
            }
        }

        @Override
        public void run() {
            Message msgClient;
            try {
                while(true){
                    String result = buff.readLine();
                    msgClient = new Message(result, this.getId());
                    System.out.println(msgClient);
                    //登录成功失败次数的计数器
                    long id=this.getId();

                  
                    //启动记录登录成功或失败的次数的计时器
                    //  if(msgClient.toString()!=null){
                    //  }
                    if(msgClient.getValue("event").equals("quit")){//客户端申请退出，服务端返回确认退出
                        SendReceive.cancel();
                        break;
                    } else if (msgClient.getValue("event").equals("login")) {
                        loginClient();

                    } else if (msgClient.getValue("event").equals("relogin")) {
                        result = buff.readLine();
                        SendReceive.cancel();
                        loginClient();
                    } else if (msgClient.getValue("event").equals("logedin")) {
                        System.out.println("user: "+msgClient.getValue("username")+" loged in.");
                    } else if (msgClient.getValue("event").equals("message")) { //输出服务端发送消息
                        System.out.println(msgClient.getValue("username")+" said: "+msgClient.getValue("msg"));
                        //如果收到了消息
                        countMsg.addCount(receive_msg);
                    }
                    synchronized (stdinLock) {
                        stdinLock.notify();
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
        try {
            new Client();//启动客户端
        }catch (Exception e) {
        }
    }
}