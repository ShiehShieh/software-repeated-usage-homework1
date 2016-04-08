package MessageUtils;

import com.rabbitmq.client.*;
import org.json.JSONException;
import org.json.JSONObject;
import PM.Logger;

import java.io.IOException;
import java.io.PrintWriter;

class MessageConsumer extends DefaultConsumer {
    Message msg;
    private PrintWriter out;
    private Logger logger;
    private String logKey;

    public MessageConsumer(Message msg, PrintWriter out, Logger logger, String logKey) {
        super(msg.getChannel());
        this.msg = msg;
        this.out = out;
        this.logger = logger;
        this.logKey = logKey;
        return;
    }

    private void sendMessage(String msg){
        out.println(msg);
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
                               AMQP.BasicProperties properties, byte[] body) throws IOException {
        String message = new String(body, "UTF-8");
        String queueName;
        try {
            queueName = new JSONObject(message).getString("queueName");
            if (!queueName.equals(msg.getQueueName())) {
                sendMessage(message);
                logger.addCount(logKey);
            }
        } catch (JSONException e) {
        }
    }
}


/**
 * Created by shieh on 3/31/16.
 */
public class MessageDeparturer {
    public MessageDeparturer(Message msg, PrintWriter out, Logger logger, String logKey) throws IOException {
        Consumer consumer = new MessageConsumer(msg, out, logger, logKey);
        msg.getChannel().basicConsume(msg.getQueueName(), true, consumer);
    }
}
