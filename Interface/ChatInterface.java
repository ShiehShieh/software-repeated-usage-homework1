import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;


public class ChatInterface extends JFrame{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextArea jTextArea1,jTextArea2;
	JButton jbutton;
	JScrollPane jScrollPane1,jScrollPane2;
	
	String username;
	
	ChatInterface(String name){
		
		username = name;
		
		this.setLayout(null);
		
		jTextArea1 = new JTextArea("receive");              //接受信息文本框
		jTextArea1.setSelectedTextColor(Color.RED);
		jTextArea1.setLineWrap(true);        
		jTextArea1.setWrapStyleWord(true);
		//jTextArea1.setBackground(Color.red);		
        
		jScrollPane1 = new JScrollPane(jTextArea1);
        jScrollPane1.setBounds(10,10,585,290);
        this.add(jScrollPane1);
        
        jTextArea2 = new JTextArea("send");                     //发送信息文本框
		jTextArea2.setSelectedTextColor(Color.RED);
		jTextArea2.setLineWrap(true);        
		jTextArea2.setWrapStyleWord(true);
		//jTextArea2.setBackground(Color.blue);
		
        
		jScrollPane2 = new JScrollPane(jTextArea2);
        jScrollPane2.setBounds(10,310,500,40);
        this.add(jScrollPane2);
        
        jbutton = new JButton("发送");
        jbutton.setBounds(520,310,75,40);
        this.add(jbutton);
        
        jbutton.addActionListener(new ActionListener() {        //发送按钮事件
            public void actionPerformed(ActionEvent e) {
            	
            	 //发送消息事件
            	
            	String message = jTextArea2.getText();       //得到将要发送的消息
            	System.out.println(message);
            	
            	String content = "";
            	content+="line1\n";
            	content+="line2\n";
            	jTextArea1.setText(content);                   //设置接受到的消息显示在对话款中
            	
         
            }
        });

       
         
	    this.setSize(620, 400);
	    this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
	    this.setVisible(true);
	    this.setTitle(name);
	    
	}

	
	
 
    
}
