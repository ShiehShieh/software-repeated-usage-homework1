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
    //����server�ж�֮���¼�ɹ��Ĵ���
    private int loginSuccess=0;
    //��¼ʧ�ܵĴ���
    private int loginFail=0;
    //���͵���Ϣ����
    private int send_message=0;
    //�ӷ�����յ�����Ϣ����
    private int received_message=0;
    //����ÿ���Ӵ����ļ��ļ�ʱ��
    private Timer SaveMsgTimer;
    //ͬ����
    private static Object SendMsgLock = new Object();
    //д���ļ�
    private IOLog ClientLog;
    //���ڴ�ŵ�¼���û���������
    Map<String,String> map=new HashMap<String,String>();
    //�ͻ�������
    String input="";
    private String nameForFile="";
    //���ͺͽ�����Ϣ�����ļ�ʱ��
    private Timer SendReceive;
    //��¼�ɹ���ʧ�ܴ����ļ�ʱ��
    private Timer SuccessFail;
    //������:��¼
    private CheckCount countLogin;
    //����������Ϣ
    private CheckCount countMsg;

    public String login_success = "login successfully ";
    public String login_fail = "login failed ";
    public String receive_msg = "receive message ";
    public String send_msg = "send message ";
    /**
     * ����������ӣ������뷢����Ϣ
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

    //�����ļ�
    public String createFile(String lastname){
        File f = new File(".");
        // fileName��ʾ�㴴�����ļ�����Ϊtxt���ͣ�
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

    //�����¼
    public void loginClient() throws IOException{
        String line;
        Message msgClient;
        String username;
        String password;

        stdinFlag = false;

        while (true) {
            try {
                System.out.print("please input the username��");
                username = strin.readLine();
                System.out.println("please input the password��");
                password = strin.readLine();
                msgClient = new Message("{}", 0);
                msgClient.setValue("event", "login");
                msgClient.setValue("username", username);
                msgClient.setValue("password", password);
                out.println(msgClient);
                line = in.readLine();
                msgClient = new Message(line, 0);
                if (msgClient.getValue("event").equals("valid")) {
                    //��¼�ɹ�����+1
                    countLogin.addCount(login_success);
                    //��ʾ�û���¼�ɹ�
                    System.out.println("login successfully, please input the message:");
                    //���ɹ���¼���û������������Map
                    map.put(msgClient.getValue("username"), msgClient.getValue("password"));
                    //  map.put(username, password);
                    nameForFile=username;
                    //�رռ�ʱ������¼��¼�ɹ�ʧ��
                  //  SuccessFail.cancel();
                    //����û������ڵĻ������м������Ĵ���
                    countMsg = new CheckCount(nameForFile+".log");
                    //����Ҫ�����Ķ������������
                    countMsg.addCountType(receive_msg);
                    countMsg.addCountType(send_msg);
                    SendReceive = new Timer();
                    SendReceive.schedule(countMsg, 0,60000);
                    break;
                }
                if (msgClient.getValue("event").equals("invalid")) {//��¼ʧ��
                    //��¼ʧ�ܴ���+1
                    countLogin.addCount(login_fail);
                    //��ʾ�û���¼ʧ��
                    System.out.println("invalid input, please login again");
                }
                else{//�û���¼��ʱ����¼ʧ��
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
     * ���ڼ�������������ͻ��˷�����Ϣ�߳���
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
                    //��¼�ɹ�ʧ�ܴ����ļ�����
                    long id=this.getId();

                  
                    //������¼��¼�ɹ���ʧ�ܵĴ����ļ�ʱ��
                    //  if(msgClient.toString()!=null){
                    //  }
                    if(msgClient.getValue("event").equals("quit")){//�ͻ��������˳�������˷���ȷ���˳�
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
                    } else if (msgClient.getValue("event").equals("message")) { //�������˷�����Ϣ
                        System.out.println(msgClient.getValue("username")+" said: "+msgClient.getValue("msg"));
                        //����յ�����Ϣ
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
            new Client();//�����ͻ���
        }catch (Exception e) {
        }
    }
}