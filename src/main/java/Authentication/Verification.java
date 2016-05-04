package Authentication;

import PM.Logger;
import MessageUtils.Message;
import org.json.JSONException;
import DataSource.DataSource;
import utils.Pair;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;

/**
 * Created by shieh on 3/29/16.
 */
public class Verification {
    private static String username = "";
    private static String password = "";

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public Pair<Integer, Integer> login(BufferedReader in, PrintWriter out, DataSource dataSource, long threadId)throws IOException {
        String line;
        Message msg;
        Pair<Integer, Integer> res = new Pair<Integer, Integer>(0, 0);

        while(true) {
            try {
                msg = new Message("{}", threadId);
                msg.setValue("event", "login");
                out.println(msg);
                line = in.readLine();
                msg = new Message(line, threadId);
                username = msg.getValue("username");
                password = msg.getValue("password");
                if (password.equals(dataSource.getPasswordDB(username))) {
                    res.setL(1);
                    msg.setValue("event", "valid");
                    out.println(msg);
                    break;
                } else {
                    res.setR(res.getR()+1);
                    msg.setValue("event", "invalid");
                    out.println(msg);
                }
            } catch (JSONException e) {
                continue;
            }
        }

        return res;
    }

    public void csLogin(BufferedReader in, PrintWriter out, DataSource dataSource, Logger logger, String valid_login, String invalid_login){
        String loginType = dataSource.getType();
        String line;
        Message msg;
        if(loginType.equals("DB")) {
            while (true) {
                try {
                    msg = new Message("{}", 0);
                    msg.setValue("event", "login");
                    out.println(msg);
                    line = in.readLine();
                    msg = new Message(line, 0);
                    username = msg.getValue("username");
                    password = msg.getValue("password");
                    if (password.equals(dataSource.getPasswordDB(username))) {
                        logger.addCount(valid_login);
                        msg.setValue("event", "valid");
                        out.println(msg);
                        break;
                    } else {
                        logger.addCount(invalid_login);
                        msg.setValue("event", "invalid");
                        out.println(msg);
                    }
                } catch (JSONException e) {
                    e.getStackTrace();
                    continue;
                } catch (IOException e) {
                    e.getStackTrace();
                    continue;
                }
            }
        }else{
            BufferedReader strin = new BufferedReader(new InputStreamReader(System.in));
            while(true){
                try {
                    System.out.println("Please input the Username!\n");
                    username =strin.readLine();
                    System.out.println("Please input the Password!\n");
                    password = strin.readLine();
                    msg = new Message("{}",0);
                    msg.setValue("event","login");
                    msg.setValue("username",username);
                    msg.setValue("password",password);
                    line = dataSource.getPasswordResponse(msg.toString());
                    msg = new Message(line,0);
                    if(msg.getValue("event").equals("valid")){
                        logger.addCount(valid_login);
                        System.out.println("Login successfully!");
                        break;
                    }
                    else if(msg.getValue("event").equals("invalid")){
                        logger.addCount(invalid_login);
                        System.out.println("Invalid input! Please login again!");
                    }
                    else{
                        logger.addCount(invalid_login);
                        System.out.println("Login timeout! Please login again!");
                    }
                }catch (IOException e){
                    e.getStackTrace();
                    continue;
                }catch (JSONException e){
                    e.getStackTrace();
                    continue;
                }
            }
        }
    }
}
