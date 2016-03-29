package utils;

import org.json.JSONException;
import server.DataSource;

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

    public void login(BufferedReader in, PrintWriter out, DataSource dataSource, CheckCount checkCount,
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
                if (password.equals(dataSource.getPassword(username))) {
                    synchronized (checkCount.getLock(valid_login_per_min)) {
                        checkCount.addCount(valid_login_per_min);
                        msg.setValue("event", "valid");
                        out.println(msg);
                        break;
                    }
                } else {
                    synchronized (checkCount.getLock(invalid_login_per_min)) {
                        checkCount.addCount(invalid_login_per_min);
                        msg.setValue("event", "invalid");
                        out.println(msg);
                    }
                }
            } catch (JSONException e) {
                continue;
            }
        }

        return;
    }
}
