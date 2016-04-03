package Logging;

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;

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
		String res;
		res = new SimpleDateFormat("yyyyMMdd_HHmmss: ").format(Calendar.getInstance().getTime());
		try {
			logFile.write(res+sWriten);
			logFile.flush();
			System.out.println("OK");
		} catch (IOException e) {
			System.out.println("Write Failed");
			e.printStackTrace();
		}
	}
}
