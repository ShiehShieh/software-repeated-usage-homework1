package src.main.java.server;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
import org.json.JSONException;

import src.main.java.DataSource.DataSource;
import src.main.java.MessageUtils.Message;
import src.main.java.MessageUtils.MessageDeparturer;
import src.main.java.PackerUtils.PackerTimer;
import utils.Pair;
import wheellllll.config.Config;
import wheellllll.performance.IntervalLogger;
import wheellllll.performance.Logger;

public class UserVerificationServer extends ServerSocket{
	private static Object loginThreadLock = new Object();

	private boolean withLog;
    private String logDir;
    private String zipDir;

    private static DataSource dataSource;
    
    private IntervalLogger valid_pm;
	private IntervalLogger invalid_pm;
    private String valid_login = "validLogin";
	private String invalid_login = "invalidLogin";

	private PackerTimer validPacker;
	private PackerTimer validPackerWeek;
	private PackerTimer invalidPacker;
	private PackerTimer invalidPackerWeek;
    
    Consumer loginConsumer;
    
    private String username;
    private String password;
    
    public Message loginSuccess;

    public Message loginFailMsg;
	public Message loginSucessMsg;

	public Message loginSuccessSend;

	private Channel loginChannel;

	//用户群租分发
	private HashMap<String,HashMap<String,Message>> allMsg;
    
	 public UserVerificationServer(int SERVER_PORT, String logDirname, String zipDirname, String dbUser, String dbPwd, boolean withLog)
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
				 loginSuccessSend = new Message("{}", p.getR());
				 loginSuccessSend.init(p.getR(), "localhost");
				 loginSuccessSend.bindTo(p.getL(), p.getR());
				 if (allMsg.containsKey(p.getL())) {
					 allMsg.get(p.getL()).put(p.getR(), loginSuccessSend);
				 }
				 else {
					 user2msg = new HashMap<String, Message>();
					 user2msg.put(p.getR(), loginSuccessSend);
					 allMsg.put(p.getL(), user2msg);
				 }
			 }

			loginSuccess = new Message("{}", "");
			loginSuccess.init("login_success","localhost");
			loginSuccess.bindTo("login_auth","login_success");

		 	loginFailMsg = new Message("{}", "");
		 	loginFailMsg.init("login_fail_msg","localhost");
		 	loginFailMsg.bindTo("login_auth","login_fail_msg");

		 	loginSucessMsg = new Message("{}", "");
		 	loginSucessMsg.init("login_success_msg","localhost");
		 	loginSucessMsg.bindTo("login_auth","login_success_msg");
	        
	        ConnectionFactory factory = new ConnectionFactory();
	        factory.setHost("localhost");
	        Connection connection = factory.newConnection();
	        loginChannel = connection.createChannel();
	        loginChannel.queueDeclare("login_request", true, false, false, null);



	 }
	
	 public void run() throws IOException {
			if (withLog) {
				//合法登录计数
				valid_pm = new IntervalLogger();
				valid_pm.setMaxFileSize(500, Logger.SizeUnit.KB);
				valid_pm.setMaxTotalSize(200, Logger.SizeUnit.MB);
				valid_pm.setLogDir(this.logDir + "/pm/valid_login");
				valid_pm.setLogPrefix("Server");
				valid_pm.setInterval(1, TimeUnit.MINUTES);
				valid_pm.addIndex(valid_login);
				valid_pm.start();

				//不合法登录计数
				invalid_pm = new IntervalLogger();
				invalid_pm.setMaxFileSize(500, Logger.SizeUnit.KB);
				invalid_pm.setMaxTotalSize(200, Logger.SizeUnit.MB);
				invalid_pm.setLogDir(this.logDir + "/pm/invalid_login");
				invalid_pm.setLogPrefix("Server");
				invalid_pm.setInterval(1, TimeUnit.MINUTES);
				invalid_pm.addIndex(invalid_login);
				invalid_pm.start();

				//合法消息计数打包
				validPacker = new PackerTimer(this.logDir + "/pm/validLogin", this.zipDir + "/pm/day/validLogin");
				validPacker.setInterval(1,TimeUnit.DAYS);
				validPacker.setDelay(1,TimeUnit.DAYS);
				validPacker.setPackDateFormat("yyyy-MM-dd");
				validPacker.setbEncryptIt(true);
				validPacker.start();

				//合法接收消息计数周信息归档
				validPackerWeek = new PackerTimer(this.zipDir + "/pm/day/validLogin", this.zipDir + "/pm/week/validLogin");
				validPackerWeek.setInterval(7,TimeUnit.DAYS);
				validPackerWeek.setDelay(7,TimeUnit.DAYS);
				validPackerWeek.setPackDateFormat("yyyy-MM-dd");
				validPackerWeek.setbUnpack(true);
				validPackerWeek.setbEncryptIt(true);
				validPackerWeek.start();

				//不合法接收消息打包
				invalidPacker = new PackerTimer(this.logDir + "/pm/invalidLogin", this.zipDir + "/pm/day/invalidLogin");
				invalidPacker.setInterval(1,TimeUnit.DAYS);
				invalidPacker.setDelay(1,TimeUnit.DAYS);
				invalidPacker.setPackDateFormat("yyyy-MM-dd");
				invalidPacker.setbEncryptIt(true);
				invalidPacker.start();

				//不合法接收消息周信息归档
				invalidPackerWeek = new PackerTimer(this.zipDir + "/pm/day/invalidLogin", this.zipDir + "/pm/week/invalidLogin");
				invalidPackerWeek.setInterval(7,TimeUnit.DAYS);
				invalidPackerWeek.setDelay(7,TimeUnit.DAYS);
				invalidPackerWeek.setPackDateFormat("yyyy-MM-dd");
				invalidPackerWeek.setbUnpack(true);
				invalidPackerWeek.setbEncryptIt(true);
				invalidPackerWeek.start();
			}


			loginConsumer = new DefaultConsumer(loginChannel) {
			 @Override
			 public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body)
					 throws IOException {
				 String getMsg = new String(body, "UTF-8");
				 try {
					 Message loginMsg = new Message("{}", "");
					 loginMsg.reset(getMsg);
					 username = loginMsg.getValue("username");
					 password = loginMsg.getValue("password");

					 Message msg = new Message("{}", "");
					 msg.setValue("username", username);
					 msg.setValue("password", password);
					 //登陆成功
					 if (password.equals(dataSource.getPasswordDB(username))) {
						 msg.setValue("event", "valid");
						 loginSuccess.reset(msg.toString());
						 loginSucessMsg.reset(msg.toString());
						 synchronized (loginThreadLock) {
							 loginSuccess.publishToOne("login_auth","login_success");
							 loginSucessMsg.publishToOne("login_auth","login_success_msg");
						 }

						 String exchangeName = "";
						 for (String key : allMsg.keySet()) {
							 if (allMsg.get(key).containsKey(username)) {
								 exchangeName = key;
								 loginSuccessSend = allMsg.get(key).get(username);
							 }
						 }
						 loginSuccessSend.setValue("target", "others");
						 loginSuccessSend.setValue("event", "logedin");
						 loginSuccessSend.setValue("username", username);
						 loginSuccessSend.publishToAll(exchangeName);

						 valid_pm.setIndex(valid_login, 1);
					 }
					 //登录失败
					 else {
						 msg.setValue("event", "invalid");
						 loginFailMsg.reset(msg.toString());
						 synchronized (loginThreadLock) {
							 loginFailMsg.publishToOne("login_auth","login_fail");
						 }

						 invalid_pm.setIndex(invalid_login, 1);
					 }

				 } catch (JSONException e) {
					 e.printStackTrace();
				 }
			 }
			};
			loginChannel.basicConsume("login_request", true, loginConsumer);
	    }
	
	
	public static void main(String[] args) throws IOException, TimeoutException, JSONException {
        Config.setConfigName("./application.conf");
        String host = Config.getConfig().getString("SERVER_IP");
        int port = 9003;
        String logDirname = "./log/server";
        String zipDirname = "./archive/server";
        String dbUser = "root";
        String dbPwd = "Wsy_07130713";
        UserVerificationServer userVerificationServer = new UserVerificationServer(port, logDirname, zipDirname, dbUser, dbPwd, true);
        userVerificationServer.run();
    }
}
