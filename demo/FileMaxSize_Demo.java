//package test8;
//
//import java.util.HashMap;
//import java.util.Timer;
//import java.util.concurrent.TimeUnit;
//
//
//
//
//
//
//import File.GetPackageByTimer;
//import wheellllll.performance.IntervalLogger;
//import wheellllll.performance.Logger;
//import wheellllll.performance.RealtimeLogger;
//
//public class project8_Demo {
//	Timer timer;
//	public void tp(){
//		//Initial Interval Logger
//		IntervalLogger logger1 = new IntervalLogger();
//		logger1.setLogDir("./log");
//		logger1.setLogPrefix("test");
//		logger1.setLogSuffix("log");
//		//logger1.setDateFormat("yyyy-MM-dd HH_mm_ss");
//		logger1.setInterval(1, TimeUnit.MINUTES);
//		logger1.addIndex("loginSuccess");
//		logger1.addIndex("loginFail");
//		logger1.setFormatPattern("Login Success Number : ${loginSuccess}\nLogin Fail Number : ${loginFail}\n\n");
//
//		//Initial Realtime Logger
//		RealtimeLogger logger2 = new RealtimeLogger();
//		logger2.setLogDir("./log");
//		logger2.setLogPrefix("test");
//		logger2.setFormatPattern("Username : ${username}\nTime : ${time}\nMessage : ${message}\n\n");
//
//
//
//		Logger logger = new RealtimeLogger();
//		logger.setMaxFileSize(500, Logger.SizeUnit.KB); //第一个参数是数值，第二个参数是单位
//		logger.setMaxTotalSize(200, Logger.SizeUnit.MB); //第一个参数是数值，第二个参数是单位
//
//		timer = new Timer();
//		timer.schedule(new GetPackageByTimer("./log"), 0,10000);
//
//		//Test
//		logger1.start();
//		for (int i = 0; i < 300; ++i) {
//		    logger1.updateIndex("loginSuccess", 1);
//		    logger1.updateIndex("loginFail", 2);
//		    HashMap<String, String> map = new HashMap<>();
//		    map.put("username", "Sweet");
//		    map.put("time", "2016-04-21");
//		    map.put("message", "Hello World - " + i);
//		    logger2.log(map);
//		    try {
//		        Thread.sleep(1000);
//		        System.out.println("" + i);
//		    } catch (InterruptedException e) {
//		        e.printStackTrace();
//		    }
//
//		}
//		logger1.stop();
//	}
//
//	public static void main(String[] args){
//		project8_Demo s = new project8_Demo();
//		s.tp();
//	}
//}
