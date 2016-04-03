import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;

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
	private String DBUSER;
	private String DBPW;
	
	
	GetConfiguration() throws JSONException{
		GetConfigurationInfo();
				
	}
	
	
	//解析JSON,将得到的所有配置信息存储在私有变量中
	public void GetConfigurationInfo() throws JSONException{
		content = ReadJSONFile("configuration.json");  
		JSONObject jsonObject = new JSONObject(content);
		SERVER_IP = jsonObject.getString("SERVER_IP");
		SERVER_PORT = jsonObject.getInt("SERVER_PORT");
		MAX_MESSAGE_PER_SECOND = jsonObject.getInt("MAX_MESSAGE_PER_SECOND");
		MAX_MESSAGE_FOR_TOTAL = jsonObject.getInt("MAX_MESSAGE_FOR_TOTAL");
		DBUSER = jsonObject.getString("DBUSER");
		DBPW = jsonObject.getString("DBPW");
		
		System.out.println(SERVER_IP+"\n"+SERVER_PORT+"\n"+
		MAX_MESSAGE_PER_SECOND+"\n"+MAX_MESSAGE_FOR_TOTAL+
		"\n"+DBUSER+"\n"+DBPW);
	}
	
	//读取文件，将文件内容以字符串形式返回
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
	
	
	//将指定的配置信息写入指定文件中
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

//	public static void main(String[] args) throws JSONException{
//			GetConfiguration c = new GetConfiguration();
//			String[] a = new String[]{"key1","key2","key3"};
//			String[] b = new String[]{"value1","value2","value3"};
//			String path = "output.json";
//			c.writeJSONFile(path, a, b);
//	}

	public String getSERVER_IP() {
		return SERVER_IP;
	}

	public int getSERVER_PORT() {
		return SERVER_PORT;
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
