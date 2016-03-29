package Client;

/*测试代码*/

import static org.junit.Assert.*;

import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.junit.Before;
import org.junit.Test;

import Client.Client.SaveRecord;
import utils.IOLog;

public class ClientNewTest {
	IOLog ClientLog = new IOLog("clientTest.log", true);
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void testClientNew() {
		// assertNotEquals(unexpected, actual);
	}

	@Test
	public void testCreatFile()throws Exception {
		   testCreatFile testFile=new testCreatFile();
		   boolean test=testFile.userCreateFile();
	       assertEquals(true, test);
	}

	@Test
	public void testLoginClient() throws IOException {
		testLogin login=new testLogin();
		assertEquals("success", login.testThelogin("valid", "username", "password"));
		assertEquals("fail", login.testThelogin("invalid", "username", "password"));
		assertEquals("error", login.testThelogin("test", "username", "password"));
	}
	@Test
	public void  testSaveToFile() throws Exception{
		 testSave save = new testSave();
		 assertEquals(true,save.save());
	}
 
	 

}
