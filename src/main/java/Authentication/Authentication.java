package Authentication;

import DataSource.DataSource;
import Logging.Logger;
import MessageUtils.Message;
import org.json.JSONException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * Created by shieh on 4/6/16.
 */
public class Authentication {
    private static String username = "";
    private static String password = "";

    public String getUsername() {
        return this.username;
    }

    public String getPassword() {
        return this.password;
    }

    public boolean authenticate(DataSource dataSource, String username, String password) throws IOException, JSONException {
        Message msg;
        if ("remote".equals(dataSource.getType())) {
            msg = new Message("{}", 0);
            msg.setValue("event", "login");
            msg.setValue("username", username);
            msg.setValue("password", password);
            msg = new Message(dataSource.getPasswordResponse(msg.toString()), 0);
            return "valid".equals(msg.getValue("event"));
        } else if ("DB".equals(dataSource.getType())) {
            return password.equals(dataSource.getPasswordDB(username));
        }
        return false;
    }
}
