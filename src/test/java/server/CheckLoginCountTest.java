package server;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huangli on 3/23/16.
 */
public class CheckLoginCountTest {

    Server.CheckLoginCount checkLoginCount;

    @Before
    public void setUp() throws Exception {
        checkLoginCount = new Server.CheckLoginCount();
    }

    @Test
    public void run() throws Exception {
        checkLoginCount.run();
    }
}