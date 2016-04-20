package CM;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;


public class GetConfigurationTest {

	public GetConfiguration getConfiguration;
	@Before
	public void setUp() throws Exception {
		getConfiguration = new GetConfiguration();
	}


	@Test
	public void testGetConfigurationInfo() throws Exception {
		getConfiguration.GetConfigurationInfo();
		System.out.println(getConfiguration.getSERVER_IP()+"\n"+
				getConfiguration.getSERVER_PORT()+"\n"+
				getConfiguration.getMAX_MESSAGE_PER_SECOND()+"\n"+
				getConfiguration.getMAX_MESSAGE_FOR_TOTAL()+"\n"+
				getConfiguration.getDBUSER()+"\n"+
				getConfiguration.getDBPW());
	}

	@Test
	public void testReadJSONFile() throws Exception {
		String path = "testoutput.json";
		String lastStr = getConfiguration.ReadJSONFile(path);
		System.out.println(lastStr);
	}

	@Test
	public void testWriteJSONFile() throws Exception {
		String[] a = new String[]{"key1","key2","key3"};
		String[] b = new String[]{"value1","value2","value3"};
		String path = "testoutput.json";
		getConfiguration.writeJSONFile(path, a, b);
	}

	@Test
	public void testGetStringByKey() throws Exception {
		String server_ip = getConfiguration.getStringByKey("SERVER_IP");
		System.out.println(server_ip);
	}

	@Test
	public void testGetIntByKey() throws Exception {
		int server_port = getConfiguration.getIntByKey("SERVER_PORT");
		System.out.println(server_port);
	}

	@Test
	public void testGetSERVER_IP() throws Exception {
		String server_ip = getConfiguration.getSERVER_IP();
		System.out.println(server_ip);
	}

	@Test
	public void testGetSERVER_PORT() throws Exception {
		int server_port = getConfiguration.getSERVER_PORT();
		System.out.println(server_port);
	}

	@Test
	public void testGetMAX_MESSAGE_PER_SECOND() throws Exception {
		int MAX_MESSAGE_PER_SECOND = getConfiguration.getMAX_MESSAGE_PER_SECOND();
		System.out.println(MAX_MESSAGE_PER_SECOND);
	}

	@Test
	public void testGetMAX_MESSAGE_FOR_TOTAL() throws Exception {
		int MAX_MESSAGE_FOR_TOTAL = getConfiguration.getMAX_MESSAGE_FOR_TOTAL();
		System.out.println(MAX_MESSAGE_FOR_TOTAL);
	}

	@Test
	public void testGetDBUSER() throws Exception {
		String DBUSER = getConfiguration.getDBUSER();
		System.out.println(DBUSER);
	}

	@Test
	public void testGetDBPW() throws Exception {
		String DBPW = getConfiguration.getDBPW();
		System.out.println(DBPW);
	}
}