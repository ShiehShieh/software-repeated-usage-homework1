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
            while(true){//�����ͻ������������̴߳���
                Socket socket = accept();
                new ServerThread(socket);
            }
        }catch (Exception e) {
        }finally{
            close();
        }
	}
   
	public class ServerThread extends Thread{
		 //����Json��ȡ������Ϣ
	    private int MAX_MESSAGE_PER_SECOND;
	    private int MAX_MESSAGE_FOR_TOTAL;
	    public GetConfiguration getConfiguration;
	    ServerThread(Socket socket){
	    	getConfiguration = new GetConfiguration();
	    	//��̬���ã������ļ���Ϣ����ʱ���Զ�����
	    	getConfiguration.loadData();
	    	//��ӡ��̬������Ϣ
	    	System.out.println("MAX_MESSAGE_PER_SECOND:"+getConfiguration.getMAX_MESSAGE_PER_SECOND());
	    	System.out.println("MAX_MESSAGE_FOR_TOTAL:"+getConfiguration.getMAX_MESSAGE_FOR_TOTAL());
	    	
	    }
	    
	    @Override
	    public void run() {
	        //ִ���߳�run����
	    }
	}

}
