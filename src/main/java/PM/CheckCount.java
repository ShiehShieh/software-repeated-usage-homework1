package PM;

import java.util.*;

/**
 * Created by shieh on 3/28/16.
 */
public class CheckCount extends TimerTask {
    public  ArrayList<String> msgs = new ArrayList<String>();
    private IOLog ioLog;
    private String logFile;
    public  Map<String,Integer> counts = new HashMap<String,Integer>();
    public  Map<String,Object> locks = new HashMap<String,Object>();

    public CheckCount(String logFilename) {
        this.logFile = logFilename;
        ioLog = new IOLog(logFilename, true);
    }

    public void addCountType(String msg) {
        msgs.add(msg);
        counts.put(msg, 0);
        locks.put(msg, new Object());
    }

    public void addCount(String msg) {
        synchronized (locks.get(msg)) {
            counts.put(msg, counts.get(msg) + 1);
        }
    }

    public Object getLock(String msg) {
        return locks.get(msg);
    }

    public void run() {
        for (int i = 0; i < msgs.size(); i++) {
            ioLog.IOWrite(msgs.get(i) + " " + counts.get(msgs.get(i)) + "\n");
            synchronized (locks.get(msgs.get(i))) {
                counts.put(msgs.get(i), 0);
            }
        }
    }
}