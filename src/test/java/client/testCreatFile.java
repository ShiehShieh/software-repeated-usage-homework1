package Client;

/*测试文件新建*/

import java.io.File;
import java.io.IOException;

public class testCreatFile {
	 public static String createFile(String lastname){
	        File f = new File(".");
	        // fileName±íÊ¾Äã´´½¨µÄÎÄ¼þÃû£»ÎªtxtÀàÐÍ£»
	        String fileName=lastname+".txt";
	        //String fileName="1234";
	        File file = new File(f,fileName);
	        if(!file.exists()){
	            try {
	                file.createNewFile();
	            } catch (IOException e) {
	                // TODO Auto-generated catch block
	                e.printStackTrace();
	            }
	        }
	        return fileName;
	    }
	 
	 public Boolean userCreateFile() {
	       createFile("test1");
	       String path="";
	       File f=new File(".");
	       File[] array=f.listFiles();
	       String filename="";
	       int i=0;
	       boolean test;
	       while(true){
	    	   i++;
	    	   filename=array[i].getName();
	    	   if(filename.equals("test1.txt")){
	    		   test=true;
		    	   System.out.println(filename+" exists!");
		    	   break;
		       }else test=false;
	       }
	       return test;
	    }
}
