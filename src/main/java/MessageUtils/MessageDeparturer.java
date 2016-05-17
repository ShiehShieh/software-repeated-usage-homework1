package MessageUtils;

import com.rabbitmq.client.*;
import org.json.JSONException;
import org.json.JSONObject;
import wheellllll.performance.IntervalLogger;

import java.io.IOException;
import java.io.PrintWriter;

class MessageConsumer extends DefaultConsumer {
    Message msg;
    private PrintWriter out;
    private IntervalLogger pm;
    private String logKey;

    public MessageConsumer(Message msg, PrintWriter out, IntervalLogger pm, String logKey) {
        super(msg.getChannel());
        this.msg = msg;
        this.out = out;
        this.pm = pm;
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
        String queueName, target;
        JSONObject jsonObject;
        try {
            jsonObject = new JSONObject(message);
            queueName = jsonObject.getString("queueName");
            target = jsonObject.getString("target");
            if (target.equals("others") && !queueName.equals(msg.getQueueName())) {
                sendMessage(message);
                pm.updateIndex(logKey, 1);
            } else if (target.equals("itself") && queueName.equals(msg.getQueueName())) {
                sendMessage(message);
                pm.updateIndex(logKey, 1);
            }
        } catch (JSONException e) {
        }
    }
}


/**
 * Created by shieh on 3/31/16.
 */
public class MessageDeparturer {
    private PrintWriter fp;
    private Consumer consumer;
    private String tag;
    private Message msg;

    public MessageDeparturer(Message msg, PrintWriter out, IntervalLogger pm, String logKey) throws IOException {
        this.msg = msg;
        consumer = new MessageConsumer(msg, out, pm, logKey);
    }

    public void beginConsumer() throws IOException {
        tag = msg.getChannel().basicConsume(msg.getQueueName(), true, consumer);
    }

    public void cancelConsumer() throws IOException {
        msg.getChannel().basicCancel(tag);
    }

    public void logging(Message msg) {
        fp.println(msg);
    }
}
