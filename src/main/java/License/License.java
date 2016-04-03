package License;

import java.util.Timer;

/**
 * Created by shieh on 3/30/16.
 */
public class License {
    private int MAX_MSG_IN_SECOND;
    private int MAX_NUM_MESSAGE;
    private int firstTime;
    private int period;
    private MessageCount messageCount;
    private Timer timer;

    public License(int max_msg_in_second, int max_num_message, int firstTime, int period) {
        this.MAX_MSG_IN_SECOND = max_msg_in_second;
        this.MAX_NUM_MESSAGE = max_num_message;
        this.firstTime = firstTime;
        this.period = period;
        messageCount = new MessageCount();
        messageCount.reset();
        timer = new Timer();

        return;
    }

    public void setTime(int firstTime, int period) {
        this.firstTime = firstTime;
        this.period = period;
    }

    public void commence() {
        timer.schedule(messageCount, firstTime, period);
    }

    public void cancel() {
        timer.cancel();
    }

    public void setMax(int a, int b) {
        this.MAX_MSG_IN_SECOND = a;
        this.MAX_NUM_MESSAGE = b;
    }

    public void increaseMsg() {
        messageCount.increaseMsg();
    }

    public void reset() {
        messageCount.reset();
    }

    public boolean checkMsgInSecond() {
        return this.messageCount.getMsgInSecond() <= MAX_MSG_IN_SECOND;
    }

    public boolean checkTotalMsg() {
        return this.messageCount.getTotalMsg() <= MAX_NUM_MESSAGE;
    }
}
