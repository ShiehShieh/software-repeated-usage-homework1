package server;

import License.License;
import PM.Logger;

/**
 * Created by shieh on 3/20/16.
 */
public class Server {
    private boolean withLog = true;
    private String logFile;

    private Logger logger;

    public String valid_login_per_min = "valid login per min: ";
    public String invalid_login_per_min = "invalid login per min: ";
    public String received_msg = "received message: ";
    public String ignored_msg = "ignored message: ";
    public String forwarded_msg = "forwarded message: ";

    public void work() throws IOException {
        if (withLog) {
            System.out.println("PM into " + this.logFile);
            // 用日志文件名初始化Logger.
            logger = new Logger(this.logFile);
            // 以字符串标示需要记录的内容。
            logger.addCountType(valid_login_per_min);
            logger.addCountType(invalid_login_per_min);
            logger.addCountType(received_msg);
            logger.addCountType(ignored_msg);
            logger.addCountType(forwarded_msg);
            // 设定第一次记录延迟，以及以后每次间隔。
            logger.setTime(0, 60000);
            // 开始记录。
            logger.commence();
        }

        while(true) {
            if (2 > 1) {
                if (license.checkMsgInSecond()) {
                    // 将以该字符串标示的指标加一。
                    logger.addCount(received_msg);
                } else {
                    // 将以该字符串标示的指标加一。
                    logger.addCount(ignored_msg);
                }
            }
        }
        // 终止日志。
        logger.terminate();
    }
}

