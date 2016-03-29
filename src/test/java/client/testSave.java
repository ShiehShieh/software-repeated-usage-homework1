package Client;

/*测试文件保存*/

import java.io.BufferedReader;
import java.io.Console;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Scanner;

import utils.IOLog;
public class testSave {
	  int before=0;
	  int after=0;
	  static String filename="clientTest.log";
	  static IOLog ClientLog = new IOLog(filename, true);
	  public boolean save()throws IOException {
		    int send_message=0;
			int received_message=0;
			int loginSuccess=0;
			int loginFail=0;
			boolean hasWrote;
			Scanner sc= new Scanner(System.in);
			ArrayList<String> arr=new ArrayList<String>();
			String line="";
			String res;
	        res = new SimpleDateFormat("yyyyMMdd_HHmmss: ").format(Calendar.getInstance().getTime());
	        ClientLog.IOWrite(res+"client send message : " +  send_message + "\n");
	        ClientLog.IOWrite(res+"client receive message: " + received_message + "\n");
	        ClientLog.IOWrite(res+"client login success: " + loginSuccess + "\n");
	        ClientLog.IOWrite(res+"client login fail: " + loginFail + "\n");
	           File f=new File(".");
		       File[] array=f.listFiles();
		       int i=0;
		      
		     before =  judgeExist(filename,array);
		       //send_Message±ä³É10
		       send_message=10;
		    
		      ClientLog.IOWrite(res+"client send message : " +  send_message + "\n");
		      ClientLog.IOWrite(res+"client receive message: " + received_message + "\n");
		      ClientLog.IOWrite(res+"client login success: " + loginSuccess + "\n");
		      ClientLog.IOWrite(res+"client login fail: " + loginFail + "\n");
		    
		      after = judgeExist(filename,array);
		      if(after-before==4)
		      return true;
		      else return false ;
	    }
	  public static int countTheLine(String filename) throws IOException{//ÊýËãÎÄ¼þÄÚÈÝÓÐ¼¸ÐÐ
		  int count=0;
		  String str="";
		  FileReader fr = new FileReader(filename);
		  BufferedReader bfr =new BufferedReader(fr);
	      while((str = bfr.readLine()) != null){
	      count++;
	      System.out.println(str);
	     }
	      System.out.println(count + " lines read");
	      fr.close();
		  return count;
	    }
	  public static int judgeExist(String filename,File[]array) throws IOException{
		  int i=0;
		  String filenameGet="";
		  int count=0;
		  while(true){
	    	   i++;
	    	   filenameGet=array[i].getName();
	    	   if(filenameGet.equals(filename)){
		       System.out.println(filename+" exists!");
		       count=countTheLine(filename);
		       break;
		       } 
	       }
		  return count;
	  }
}
