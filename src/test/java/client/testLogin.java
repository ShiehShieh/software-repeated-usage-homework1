package Client;

/*测试登录*/

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

import org.json.JSONException;

import utils.Message;

public class testLogin {
	    private static BufferedReader strin;
	    static Map<String,String> map = new HashMap<String,String>();
	    static String event;
        public String testThelogin(String event,String Username,String Password) throws IOException{
	    //public static void main(String[] args) throws IOException {	
	    	
	    strin = new BufferedReader(new InputStreamReader(System.in));
	    String line;
        Message msgClient;
        String username;
        String password;
        String LoginFlag="";
        int loginSuccess=0;
        int loginFail=0;
        String setTheCondition="";
        boolean putIntoMap;
        while (true) {
            try {
                System.out.print("please input the username£º");
                username = strin.readLine();
                System.out.println("please input the password£º");
                password = strin.readLine();
                msgClient = new Message("{}", 0);
                msgClient.setValue("event", "login");
                msgClient.setValue("username", username);
                msgClient.setValue("password", password);
           
                if(event.equals("valid")){
                	LoginFlag="valid";
                    ++loginSuccess;
                    //½«³É¹¦µÇÂ¼µÄÓÃ»§ÃûºÍÃÜÂë´æÈëMap
                    map.put(msgClient.getValue("username"), msgClient.getValue("password")); 
                    System.out.println(map.get(username)+";"+map.get(password));
                    if(map.get(username).equals(Username)&&map.get(password).equals(Password)){
                    	putIntoMap=true;
                    }else putIntoMap=false;
                    //ÌáÊ¾ÓÃ»§µÇÂ¼³É¹¦
                    System.out.println("login successfully, please input the message:");
                    break;
                }
                if (event.equals("invalid")) {//µÇÂ¼Ê§°Ü
                	LoginFlag="invalid";
                    ++loginFail;
                    //ÌáÊ¾ÓÃ»§µÇÂ¼Ê§°Ü
                    System.out.println("login failed, please login again");
                }
            } catch (JSONException e) {
                continue;
            }
        }
        if(LoginFlag.equals("valid")&&putIntoMap==true){//²âÊÔ³É¹¦µÇÂ¼Çé¿ö
        	return "success";
        }
        else  if(LoginFlag.equals("invalid"))//²âÊÔÊ§°ÜµÇÂ¼Çé¿ö
        	return "fail";
        
		return "error";
 }
}
