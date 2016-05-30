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

import org.json.JSONException;

import src.main.java.DataSource.DataSource;
import src.main.java.MessageUtils.Message;
import src.main.java.MessageUtils.MessageDeparturer;
import wheellllll.performance.IntervalLogger;
import wheellllll.performance.Logger;

public class UserVerificationServer extends ServerSocket{
	private static Object loginThreadLock = new Object();

	private boolean withLog;
    private String logDir;
    private String zipDir;

    private static DataSource dataSource;
    
    private IntervalLogger pm;
    private String forwarded_msg = "forwardedMessage";
    
    Consumer loginConsumer;
    
    private String username;
    private String password;
    
    public Message loginSuccess;
    public Message loginFail;
    
	 public UserVerificationServer(int SERVER_PORT, String logDirname, String zipDirname, String dbUser, String dbPwd, boolean withLog)
	            throws IOException, TimeoutException, JSONException {

	        super(SERVER_PORT);

	        this.withLog = withLog;
	        this.logDir = logDirname;
	        this.zipDir = zipDirname;

	        dataSource = new DataSource(dbUser, dbPwd);
	        
	        ConnectionFactory factory = new ConnectionFactory();
	        factory.setHost("localhost");
	        Connection connection = factory.newConnection();
	        Channel loginChannel = connection.createChannel();
	        loginChannel.queueDeclare("login_request", true, false, false, null);
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
	                    
	                } catch (JSONException e) {
	                    e.printStackTrace();
	                }
	            }
	        };
	        loginChannel.basicConsume("login_request", true, loginConsumer);
	        
	        loginSuccess = new Message("{}", "");
	        loginSuccess.init("login_success","localhost");
	        loginSuccess.bindTo("login_auth","login_success");
	        
	        loginFail = new Message("{}", "");
	        loginFail.init("login_fail","localhost");
	        loginFail.bindTo("login_auth","login_fail");
	 }
	
	 public void run() throws IOException {
	        if (withLog) {
	            //转发消息计数
	            pm = new IntervalLogger();
	            pm.setMaxFileSize(500, Logger.SizeUnit.KB);
	            pm.setMaxTotalSize(200, Logger.SizeUnit.MB);
	            pm.setLogDir(this.logDir + "/pm");
	            pm.setLogPrefix("Server");
	            pm.setInterval(1, TimeUnit.MINUTES);
	            pm.addIndex(forwarded_msg);
	            pm.start();



	        }
	        try {
	            while(true){
	                if(username!=""&&password!="")
	                {
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
	                }

	                username = "";
	                password = "";
	                
	            }
	        }catch (Exception e) {
	        }finally{
	            close();
	        }
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
