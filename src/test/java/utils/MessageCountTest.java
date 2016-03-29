package utils;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created by huangli on 3/29/16.
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
        System.out.println(messageCount.getMsgTotal());
        messageCount.run();
    }

    @Test
    public void increaseMsg() throws Exception {
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getMsgTotal());
        messageCount.increaseMsg();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getMsgTotal());
    }

    @Test
    public void reset() throws Exception {
        messageCount.increaseMsg();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getMsgTotal());
        messageCount.reset();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getMsgTotal());
        messageCount.increaseMsg();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getMsgTotal());
        messageCount.reset();
        System.out.println(messageCount.getMsgInSecond());
        System.out.println(messageCount.getMsgTotal());
    }
}