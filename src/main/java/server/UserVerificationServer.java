package src.main.java.server;

import static java.lang.Thread.sleep;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.rabbitmq.client.*;
import org.json.JSONException;

import src.main.java.DataSource.DataSource;
import src.main.java.MessageUtils.Message;
import src.main.java.MessageUtils.MessageDeparturer;
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
    
    Consumer loginConsumer;
    
    private String username;
    private String password;
    
    public Message loginSuccess;
    public Message loginFail;

	private Channel loginChannel;
    
	 public UserVerificationServer(int SERVER_PORT, String logDirname, String zipDirname, String dbUser, String dbPwd, boolean withLog)
	            throws IOException, TimeoutException, JSONException {

	        super(SERVER_PORT);

	        this.withLog = withLog;
	        this.logDir = logDirname;
	        this.zipDir = zipDirname;

	        dataSource = new DataSource(dbUser, dbPwd);

			loginSuccess = new Message("{}", "");
			loginSuccess.init("login_success","localhost");
			loginSuccess.bindTo("login_auth","login_success");

			loginFail = new Message("{}", "");
			loginFail.init("login_fail","localhost");
			loginFail.bindTo("login_auth","login_fail");
	        
	        ConnectionFactory factory = new ConnectionFactory();
	        factory.setHost("localhost");
	        Connection connection = factory.newConnection();
	        loginChannel = connection.createChannel();
	        loginChannel.queueDeclare("login_request", true, false, false, null);



	 }
	
	 public void run() throws IOException {
			if (withLog) {
				//登录计数
				valid_pm = new IntervalLogger();
				valid_pm.setMaxFileSize(500, Logger.SizeUnit.KB);
				valid_pm.setMaxTotalSize(200, Logger.SizeUnit.MB);
				valid_pm.setLogDir(this.logDir + "/pm/valid_login");
				valid_pm.setLogPrefix("Server");
				valid_pm.setInterval(1, TimeUnit.MINUTES);
				valid_pm.addIndex(valid_login);
				valid_pm.start();

				invalid_pm = new IntervalLogger();
				invalid_pm.setMaxFileSize(500, Logger.SizeUnit.KB);
				invalid_pm.setMaxTotalSize(200, Logger.SizeUnit.MB);
				invalid_pm.setLogDir(this.logDir + "/pm/invalid_login");
				invalid_pm.setLogPrefix("Server");
				invalid_pm.setInterval(1, TimeUnit.MINUTES);
				invalid_pm.addIndex(invalid_login);
				invalid_pm.start();
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
						 synchronized (loginThreadLock) {
							 loginSuccess.publishToOne("login_auth","login_success");
						 }
					 }
					 //登录失败
					 else {
						 msg.setValue("event", "invalid");
						 loginFail.reset(msg.toString());
						 synchronized (loginThreadLock) {
							 loginFail.publishToOne("login_auth","login_fail");
						 }
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
