package CM;


import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;
import org.json.JSONObject;



/**
 * Created by xuawai on 3/30/16
 *
 */
public class GetConfiguration {
	
	private String content;
	private String SERVER_IP;
	private int SERVER_PORT;
	private int MAX_MESSAGE_PER_SECOND;
	private int MAX_MESSAGE_FOR_TOTAL;
	private String PATH;
	private String DBUSER;
	private String DBPW;
	private JSONObject jsonObject,jsonObjectMutable,jsonObjectImmutable;
	private Timer timer;
	
	public GetConfiguration(){
		try {
			GetConfigurationInfo();
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
				
	}
	
	
	//����JSON,���õ�������������Ϣ�洢��˽�б�����
	public void GetConfigurationInfo() throws JSONException{
		content = ReadJSONFile("configuration.json");  
		jsonObject = new JSONObject(content);
		
		jsonObjectMutable = jsonObject.getJSONObject("mutable");
		
		MAX_MESSAGE_PER_SECOND = jsonObjectMutable.getInt("MAX_MESSAGE_PER_SECOND");
		MAX_MESSAGE_FOR_TOTAL = jsonObjectMutable.getInt("MAX_MESSAGE_FOR_TOTAL");
		PATH = jsonObjectMutable.getString("PATH");
		
		jsonObjectImmutable = jsonObject.getJSONObject("immutable");
		
		SERVER_IP = jsonObjectImmutable.getString("SERVER_IP");
		SERVER_PORT = jsonObjectImmutable.getInt("SERVER_PORT");
		DBUSER = jsonObjectImmutable.getString("DBUSER");
		DBPW = jsonObjectImmutable.getString("DBPW");
		
//		System.out.println(SERVER_IP+"\n"+SERVER_PORT+"\n"+
//		MAX_MESSAGE_PER_SECOND+"\n"+MAX_MESSAGE_FOR_TOTAL+
//		"\n"+DBUSER+"\n"+DBPW);
	}
	
	//��ȡ�ļ������ļ��������ַ�����ʽ����
	public String ReadJSONFile(String Path){     
		
		BufferedReader reader = null;
		String laststr = "";
		
		try{
			FileInputStream fileInputStream = new FileInputStream(Path);
			InputStreamReader inputStreamReader = new InputStreamReader(fileInputStream, "UTF-8");
			reader = new BufferedReader(inputStreamReader);
			String tempString = null;
			while((tempString = reader.readLine()) != null){
				laststr += tempString;
			}
			reader.close();
		}catch(IOException e){
			e.printStackTrace();
			}finally{
				if(reader != null){
					try {
						reader.close();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
			}
		
		return laststr;
		
	}
	
	
	//��ָ����������Ϣд��ָ���ļ���
	public void writeJSONFile(String path,String[] key,String[] value){
		BufferedWriter writer = null;
		StringBuilder str = new StringBuilder();
		str.append('{');
		for(int i=0;i<key.length;i++){
			str.append('"');
			str.append(key[i]);
			str.append('"');
			str.append(':');
			str.append('"');
			str.append(value[i]);
			str.append('"');
			if(i!=key.length-1)
				str.append(',');
		}
		str.append('}');
		
		 try {
			 writer = new BufferedWriter(new FileWriter(path));
			 writer.write(str.toString());
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			if(writer != null){
				try {
					writer.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		
	}
	
	//��ѯ�ӿڣ�����keyֵ��ѯ��Ӧ��value
	//����ֵΪstring����
	public String getStringByKey(String key){
		String value = null;
		String value1 = null,value2 = null;
		value1 = jsonObjectMutable.optString(key);
		value2 = jsonObjectImmutable.optString(key);
		if(value1!="")
			value = value1;
		else
			value = value2;
		return value;
	}
	
	//����ֵΪint����
	public int getIntByKey(String key){
		int value = 0;
		int value1 = 0,value2 = 0;
		value1 = jsonObjectMutable.optInt(key);
		value2 = jsonObjectImmutable.optInt(key);
		if(value1!=0)
			value = value1;
		else
			value = value2;
		return value;
	}
	

	//��̬����������Ϣ
	public void load() throws JSONException{
		content = ReadJSONFile("configuration.json");  
		jsonObject = new JSONObject(content);
		
		jsonObjectMutable = jsonObject.getJSONObject("mutable");
		
		MAX_MESSAGE_PER_SECOND = jsonObjectMutable.getInt("MAX_MESSAGE_PER_SECOND");
		MAX_MESSAGE_FOR_TOTAL = jsonObjectMutable.getInt("MAX_MESSAGE_FOR_TOTAL");
		
	}
	
	public void loadData(){
		timer = new Timer();
		//30s��ȡһ��
		timer.schedule(new LoadFileTimerTask(), 0,30000);
	}

	class LoadFileTimerTask extends TimerTask {

		  @Override

		  public void run() {

		    System.out.println("TestTimerTask is running......");
		    try {
				load();
			} catch (JSONException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		    

		  }

	}
//	public static void main(String[] args) throws JSONException{
//			GetConfiguration c = new GetConfiguration();
//			String[] a = new String[]{"key1","key2","key3"};
//			String[] b = new String[]{"value1","value2","value3"};
//			String path = "output.json";
//			c.writeJSONFile(path, a, b);
//			System.out.println(c.getStringByKey("SERVER_IP")+":"
//			+c.getIntByKey("SERVER_PORT"));
//			c.loadData();
//	}

	public String getSERVER_IP() {
		return SERVER_IP;
	}

	public int getSERVER_PORT() {
		return SERVER_PORT;
	}

	public String getPATH(){
		return PATH;
	}
	
	public int getMAX_MESSAGE_PER_SECOND() {
		return MAX_MESSAGE_PER_SECOND;
	}

	public int getMAX_MESSAGE_FOR_TOTAL() {
		return MAX_MESSAGE_FOR_TOTAL;
	}

	public String getDBUSER() {
		return DBUSER;
	}

	public String getDBPW() {
		return DBPW;
	}
	
	
	
	
}
