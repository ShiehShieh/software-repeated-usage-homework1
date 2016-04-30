package File;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

public class SaveToFileTest {

	private SaveToFile saveToFile;
	@Before
	public void setUp() throws Exception {
	}

	@Test
	public void test() {
		saveToFile = new SaveToFile();
		saveToFile.write("write successfully");
		saveToFile.write("write successfully again");
	}
	
	@Test
	public void tes2t() {
		saveToFile = new SaveToFile("D:\\");
		saveToFile.write("write successfully");
		saveToFile.write("write successfully again");
	}

}
