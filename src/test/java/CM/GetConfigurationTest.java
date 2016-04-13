import static org.junit.Assert.*;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;


public class GetConfigurationTest {

	public GetConfiguration getConfiguration;
	@Before
	public void setUp() throws Exception {
		getConfiguration = new GetConfiguration();
	}

	@Test
	public void GetConfigurationInfo() throws JSONException {
		getConfiguration.GetConfigurationInfo();
		System.out.println(getConfiguration.getSERVER_IP()+"\n"+
				getConfiguration.getSERVER_PORT()+"\n"+
				getConfiguration.getMAX_MESSAGE_PER_SECOND()+"\n"+
				getConfiguration.getMAX_MESSAGE_FOR_TOTAL()+"\n"+
				getConfiguration.getDBUSER()+"\n"+
				getConfiguration.getDBPW());
	}
	
	@Test
	public void writeJSONFile() throws JSONException {
		String[] a = new String[]{"key1","key2","key3"};
		String[] b = new String[]{"value1","value2","value3"};
		String path = "testoutput.json";
		getConfiguration.writeJSONFile(path, a, b);
	}
	
	@Test
	public void loadData() throws JSONException {
		getConfiguration.loadData();
		System.out.println(getConfiguration.getIntByKey("MAX_MESSAGE_PER_SECOND")+":"
				+getConfiguration.getIntByKey("MAX_MESSAGE_FOR_TOTAL"));
				//≥Ã–Ú–›√ﬂÀƒ Æ√Î
				try {
					Thread.sleep(40000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				System.out.println(getConfiguration.getIntByKey("MAX_MESSAGE_PER_SECOND")+":"
						+getConfiguration.getIntByKey("MAX_MESSAGE_FOR_TOTAL"));
	}

}
