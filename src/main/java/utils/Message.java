package utils;

import org.json.*;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.TimeoutException;

/**
 * Created by shieh on 3/24/16.
 */
public class Message {
    private long ownerThread;
    private JSONObject jsonObject;
    private ConnectionFactory factory;
    private Connection connection;
    private Channel channel;
    private String queueName;

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

    public void reset(String msg) throws JSONException {
        jsonObject = new JSONObject(msg);
    }

    public void clear() throws JSONException {
        jsonObject = new JSONObject("{}");
    }

    public void init(String queue_name, String hostName) throws IOException, TimeoutException {
        factory = new ConnectionFactory();
        factory.setHost(hostName);
        connection = factory.newConnection();
        channel = connection.createChannel();
        channel.queueDeclare(queue_name, false, false, false, null);
        this.queueName = queue_name;
        return;
    }

    public void bindTo(String exchangeName, String routerKey) throws IOException {
        channel.exchangeDeclare(exchangeName, "direct");
        channel.queueBind(queueName, exchangeName, routerKey);
        channel.queueBind(queueName, exchangeName, "");
        return;
    }

    public String getQueueName() {
        return queueName;
    }

    public Channel getChannel() {
        return channel;
    }

    public void publishToOne(String exchangeName, String routerKey) throws IOException {
        channel.basicPublish(exchangeName, routerKey, null, jsonObject.toString().getBytes());
        return;
    }

    public void publishToAll(String exchangeName) throws IOException, JSONException {
        jsonObject.put("queueName", queueName);
        channel.basicPublish(exchangeName, "", null, jsonObject.toString().getBytes());
        return;
    }

    public void publishToOthers(String exchangeName, String routerKey) throws IOException {
        channel.basicPublish(exchangeName, "!"+routerKey, null, jsonObject.toString().getBytes());
        return;
    }

    public void terminate()throws IOException, TimeoutException {
        channel.close();
        connection.close();
        return;
    }

    @Override
    public String toString() {
        return jsonObject.toString();
    }
}
