package PM;

import java.util.Timer;

/**
 * Created by shieh on 3/30/16.
 */
public class Logger {
    private Timer loginTimer;
    private CheckCount checkCount;
    private int firstTime;
    private int period;

    public Logger(String logFilename) {
        checkCount = new CheckCount(logFilename);
        loginTimer = new Timer();
        firstTime = 0;
        period = 60000;
    }

    public void setTime(int firstTime, int period) {
        this.firstTime = firstTime;
        this.period = period;
    }

    public void commence() {
        loginTimer.schedule(checkCount, firstTime, period);
        return;
    }

    public void terminate() {
        loginTimer.cancel();
        return;
    }

    public void addCountType(String msg) {
        checkCount.addCountType(msg);
    }

    public void addCount(String msg) {
        checkCount.addCount(msg);
    }

    public void getLock(String msg) {
        checkCount.getLock(msg);
    }
}
