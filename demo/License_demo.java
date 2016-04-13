package server;

import License.License;

/**
 * Created by shieh on 3/20/16.
 */
class ServerThread {
    private int MAX_MESSAGE_PER_SECOND = 5;
    private int MAX_MESSAGE_FOR_TOTAL = 10;
    private License license;

    public void work() {
        license = new License(1,1,1,1);
        try {
        	// 设置指标。设置第一次执行延迟时间，以及以后每次执行间隔。
        	license.setMax(MAX_MESSAGE_PER_SECOND, MAX_MESSAGE_FOR_TOTAL);
        	license.setTime(0, 60000);
        	// 使用前重置License。
            license.reset();
        	// 启动内部计时器。
            license.commence();

            String line = in.readLine();
            while(true) {
            		// 检查每秒信息量是否超标。
                    if (license.checkMsgInSecond()) {
                    	// 未超标，计数加一。
                        license.increaseMsg();
                        ...
                    } else {
                    	...
                    }
                    // 检查总信息量是否超标。注意取反。
                    if (!license.checkTotalMsg()) {
                    	...
                    	// 完成工作后，重置计数。
                        license.reset();
                    }
                }
                line = in.readLine();
            }
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
        	// 停止内部计时器。
            license.cancel();
        }
    }
}