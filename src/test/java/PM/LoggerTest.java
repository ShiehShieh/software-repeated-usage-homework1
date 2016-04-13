package PM;

import org.junit.Before;
import org.junit.Test;
import sun.rmi.runtime.Log;

import static org.junit.Assert.*;

/**
 * Created by shieh on 4/13/16.
 */
public class LoggerTest {

    Logger logger;

    @Before
    public void setUp() throws Exception {
        logger = new Logger("test.log");
    }

    @Test
    public void addCountType() throws Exception {
        logger.addCountType("type1: ");
        logger.addCountType("type2: ");
        logger.addCountType("type3: ");

        logger.commence();

        Thread.sleep(70000);
    }

    @Test
    public void addCount() throws Exception {
        logger.addCountType("type1: ");
        logger.addCountType("type2: ");
        logger.addCountType("type3: ");

        logger.commence();

        Thread.sleep(70000);

        logger.addCount("type1: ");
        logger.addCount("type2: ");
        logger.addCount("type3: ");

        Thread.sleep(70000);

        logger.terminate();
    }
}