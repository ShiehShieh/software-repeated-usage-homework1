package Authentication;

import PM.Logger;
import MessageUtils.Message;
import org.json.JSONException;
import DataSource.DataSource;

import java.io.BufferedReader;
import java.io.IOException;
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

    public void login(BufferedReader in, PrintWriter out, DataSource dataSource, Logger logger,
                      String valid_login_per_min, String invalid_login_per_min, long threadId)throws IOException {
        String line;
        Message msg;

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
                    logger.addCount(valid_login_per_min);
                    msg.setValue("event", "valid");
                    out.println(msg);
                    break;
                } else {
                    logger.addCount(invalid_login_per_min);
                    msg.setValue("event", "invalid");
                    out.println(msg);
                }
            } catch (JSONException e) {
                continue;
            }
        }

        return;
    }

    public void csLogin(BufferedReader in, PrintWriter out, DataSource dataSource, Logger logger, String valid_login_per_min, String invalid_login_per_min){
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
                        logger.addCount(valid_login_per_min);
                        msg.setValue("event", "valid");
                        out.println(msg);
                        break;
                    } else {
                        logger.addCount(invalid_login_per_min);
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

        }
    }
}
