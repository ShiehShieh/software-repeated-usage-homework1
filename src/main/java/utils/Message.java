package utils;

import org.json.*;

/**
 * Created by huangli on 3/24/16.
 */
public class Message {
    private long ownerThread;
    private JSONObject jsonObject;

    public Message(String msg, long ownerThread) throws JSONException {
        jsonObject = new JSONObject(msg);
        this.ownerThread = ownerThread;
        return;
    }

    public long getOwner() {
        return ownerThread;
    }

    public String getValue(String key) throws JSONException {
        return jsonObject.getString(key);
    }

    public void setValue(String key, String value) throws JSONException {
        jsonObject.put(key, value);
        return;
    }

    public void setValue(String key, long value) throws JSONException {
        jsonObject.put(key, value);
        return;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
