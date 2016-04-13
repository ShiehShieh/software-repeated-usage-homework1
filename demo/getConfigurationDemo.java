package Demo;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;

import CM.GetConfiguration;
public class getConfigurationDemo extends ServerSocket{
	
	
	public getConfigurationDemo(int SERVER_PORT) throws IOException {
		super(SERVER_PORT);
		// TODO Auto-generated constructor stub
	}

	public void run() throws IOException{
		try {
            while(true){//监听客户端请求，启动线程处理
                Socket socket = accept();
                new ServerThread(socket);
            }
        }catch (Exception e) {
        }finally{
            close();
        }
	}
   
	public class ServerThread extends Thread{
		 //将从Json获取配置信息
	    private int MAX_MESSAGE_PER_SECOND;
	    private int MAX_MESSAGE_FOR_TOTAL;
	    public GetConfiguration getConfiguration;
	    ServerThread(Socket socket){
	    	getConfiguration = new GetConfiguration();
	    	//动态配置，配置文件信息更改时将自动读入
	    	getConfiguration.loadData();
	    	//打印动态配置信息
	    	System.out.println("MAX_MESSAGE_PER_SECOND:"+getConfiguration.getMAX_MESSAGE_PER_SECOND());
	    	System.out.println("MAX_MESSAGE_FOR_TOTAL:"+getConfiguration.getMAX_MESSAGE_FOR_TOTAL());
	    	
	    }
	    
	    @Override
	    public void run() {
	        //执行线程run方法
	    }
	}

}
