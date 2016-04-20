package PM;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

public class IOLogTest {

    public IOLog ioLog;
    @Before
    public void setUp() throws Exception {
        ioLog = new IOLog("pathTest.log",true);
    }

    @Test
    public void testIOWrite() throws Exception {
        ioLog.IOWrite("test");
    }
}