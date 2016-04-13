package server;

import MessageUtils.Message;
import MessageUtils.MessageDeparturer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by shieh on 3/20/16.
 */
class ServerThread extends Thread {
    private Socket client;
    private PrintWriter out;
    private BufferedReader in;
    public Message msg;
    public MessageDeparturer messageDeparturer;
    String exchangeName = "test";
    String queueName;

    public ServerThread(Socket s)throws IOException {
        this.client = s;
        out = new PrintWriter(client.getOutputStream(),true);
        in = new BufferedReader(new InputStreamReader(client.getInputStream()));
        start();
    }

    @Override
    public void run() {
        try {

            // 以线程ID标示queue。
            queueName = String.valueOf(this.getId());
            // 初始化。
            msg = new Message("{}", this.getId());
            // 设置字段。
            msg.setValue("username", username);
            msg.setValue("target", "others");
            // 绑定指定频道。
            msg.init(queueName, "localhost");
            msg.bindTo(exchangeName, queueName);
            // 为这个message对象建立departurer，信息会由其自动分发。
            messageDeparturer = new MessageDeparturer(msg, out, logger, forwarded_msg);
            msg.setValue("event", "logedin");
            // 将信息分发给同一exchange中的其她所有人。
            msg.publishToAll(exchangeName);

            String line = in.readLine();
            msg.reset(line);
            while(!"logout".equals(msg.getValue("event"))) {
                //查看在线用户列表
                if ("showuser".equals(msg.getValue("event"))) {
                    out.println(listOnlineUsers());
                } else if ("message".equals(msg.getValue("event"))) {
                    msg.setValue("username", username);
                    msg.setValue("event", "message");
                    msg.setValue("target", "others");
                    msg.publishToAll(exchangeName);
                }
                line = in.readLine();
                msg.reset(line);
            }
            msg.setValue("target", "all");
            msg.setValue("event", "quit");
            msg.publishToAll(exchangeName);
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            try {
                client.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}

