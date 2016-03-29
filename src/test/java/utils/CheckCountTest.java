package utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huangli on 3/23/16.
 */
public class CheckCountTest {

    utils.CheckCount checkCount;

    String valid_login_per_min = "valid login per min: ";
    String invalid_login_per_min = "invalid login per min: ";
    String received_msg = "received message: ";
    String ignored_msg = "ignored message: ";
    String forwarded_msg = "forwarded message: ";

    @Before
    public void setUp() throws Exception {
        checkCount = new utils.CheckCount("test.log");
        checkCount.addCountType(valid_login_per_min);
        checkCount.addCountType(invalid_login_per_min);
        checkCount.addCountType(received_msg);
        checkCount.addCountType(ignored_msg);
        checkCount.addCountType(forwarded_msg);
    }

    @Test
    public void run() throws Exception {
        checkCount.run();
        checkCount.addCount(valid_login_per_min);
        checkCount.addCount(invalid_login_per_min);
        checkCount.addCount(received_msg);
        checkCount.addCount(ignored_msg);
        checkCount.addCount(forwarded_msg);
        checkCount.run();
    }
}