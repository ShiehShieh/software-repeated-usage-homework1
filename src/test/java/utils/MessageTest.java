package utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huangli on 3/24/16.
 */
public class MessageTest {

    Message msg;

    @Before
    public void setUp() throws Exception {
        msg = new Message("{}", 100);
    }

    @Test
    public void getOwner() throws Exception {
        System.out.println(msg.getOwner());
    }

    @Test
    public void getsetValue() throws Exception {
        msg.setValue("a", 2);
        msg.setValue("b", "22");
        System.out.println(msg.getValue("a"));
        System.out.println(msg.getValue("b"));
    }

    @Test
    public void toStringTest() throws Exception {
        msg.setValue("a", 2);
        msg.setValue("b", "22");
        System.out.println(msg);
    }
}