package utils;

import java.util.TimerTask;

/**
 * Created by huangli on 3/28/16.
 */
public class MessageCount extends TimerTask {
    public int msg_in_second = 0;
    public int num_message = 0;
    public void run() {
        this.msg_in_second = 0;
    }
    public int getMsgInSecond() {
        return this.msg_in_second;
    }
    public int getMsgTotal() {
        return this.num_message;
    }
    public void increaseMsg() {
        this.msg_in_second += 1;
        this.num_message += 1;
    }
    public void reset() {
        this.msg_in_second = 0;
        this.num_message = 0;
    }
}
