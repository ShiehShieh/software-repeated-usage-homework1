package License;

import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by huangli on 3/28/16.
 */
public class MessageCount extends TimerTask {
    public int msg_in_second;
    public int num_message;

    public MessageCount() {
        this.msg_in_second = 0;
        this.num_message = 0;
    }

    synchronized public void run() {
        this.msg_in_second = 0;
    }
    public int getMsgInSecond() {
        return this.msg_in_second;
    }
    public int getTotalMsg() {
        return this.num_message;
    }
    synchronized public void increaseMsg() {
        this.msg_in_second += 1;
        this.num_message += 1;
    }
    synchronized public void reset() {
        this.msg_in_second = 0;
        this.num_message = 0;
    }
}
