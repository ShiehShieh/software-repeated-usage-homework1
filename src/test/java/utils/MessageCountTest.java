package utils;

import License.MessageCount;
import org.junit.Before;
import org.junit.Test;

/**
 * Created by shieh on 3/29/16.
 */
public class MessageCountTest {

    MessageCount messageCount;

    @Before
    public void setUp() throws Exception {
        messageCount = new MessageCount();
    }

    @Test
    public void run() throws Exception {
        messageCount.increaseMsg();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getTotalMsg());
        messageCount.run();
    }

    @Test
    public void increaseMsg() throws Exception {
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getTotalMsg());
        messageCount.increaseMsg();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getTotalMsg());
    }

    @Test
    public void reset() throws Exception {
        messageCount.increaseMsg();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getTotalMsg());
        messageCount.reset();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getTotalMsg());
        messageCount.increaseMsg();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getTotalMsg());
        messageCount.reset();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getTotalMsg());
    }
}