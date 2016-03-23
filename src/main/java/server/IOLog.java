package server;

import java.io.FileWriter;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;

public class IOLog{
	private String logAddr = ""; 
	private FileWriter logFile;
	
	public IOLog(String logAddr, boolean bAppend){
		this.logAddr = logAddr;
		try {
			logFile = new FileWriter(logAddr,bAppend);
		} catch (IOException e) {
			System.out.println("No " + logAddr + " file");
			e.printStackTrace();
		}
	}

	public void IOWrite(String sWriten){
		try {
			logFile.write(sWriten);
			logFile.flush();
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println("Write Failed");
			e.printStackTrace();
		}
	}
}
