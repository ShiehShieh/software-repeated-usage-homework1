

import java.awt.Color;  
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener; 
import javax.swing.ImageIcon;
import javax.swing.JButton;  
import javax.swing.JFrame;  
import javax.swing.JLabel;  
import javax.swing.JPanel;  
import javax.swing.JPasswordField;
import javax.swing.JTextField;  

public class LoginInterface extends JFrame{
       /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	JTextField jTextField ;
       JPasswordField jPasswordField;
       JLabel jLabel1,jLabel2,jLabel3,jLabel4;
       JButton jb1,jb2; 
       ImageIcon background;
       JPanel jp;
       LoginInterface(){
         jTextField = new JTextField(12);
         jPasswordField = new JPasswordField(12);
         background = new ImageIcon("image\\bkg.jpg");//����ͼƬ
         jLabel1 = new JLabel("�û���");
         jLabel2 = new JLabel("����");
         jLabel3 = new JLabel("");
         jb1 = new JButton("ȷ��");
         jb2 = new JButton("ȡ��");       
         jLabel4 = new JLabel(background);
         jp = new JPanel();
         
         jp = (JPanel) this.getContentPane();

         this.setLayout(null);
         
         jLabel4.setBounds(0,0,background.getIconWidth(),background.getIconHeight());
         JPanel imagePanel = (JPanel) this.getContentPane();  
         imagePanel.setOpaque(false);  
         // �ѱ���ͼƬ��ӵ��ֲ㴰�����ײ���Ϊ����  
         this.getLayeredPane().add(jLabel4, new Integer(Integer.MIN_VALUE));
         
         jLabel1.setBounds(50,40,50,30);
         this.add(jLabel1);
         jLabel1.setOpaque(false);
         
         jPasswordField.setBounds(130,90,160,30);
         this.add(jPasswordField);
         jPasswordField.setOpaque(false);
         
         jLabel2.setBounds(60,90,50,30);
         this.add(jLabel2);
         jLabel2.setOpaque(false);
         
         jb1.setBounds(80,140,70,30);
         this.add(jb1);
         jb1.setOpaque(false);
      
         jb2.setBounds(190,140,70,30);
         this.add(jb2);
         jb2.setOpaque(false);
         
         jTextField.setBounds(130,40,160,30);
         this.add(jTextField);
         jTextField.setOpaque(false);
         
         jLabel3.setBounds(100,190,160,30);
         this.add(jLabel3);
         jLabel3.setOpaque(false);
         jLabel3.setFont(new java.awt.Font("Dialog",1,15)); 
         jLabel3.setForeground(Color.red);
         
         jb1.addActionListener(new ActionListener() {    //��¼�¼�
             @Override
             public void actionPerformed(ActionEvent e) {
            	String name = null;
              	String psd = null;
              	
              	if(jTextField.getText().length()!=0){
                  	name = jTextField.getText().toString();
                  }
              	
              	else{
              		 jLabel3.setText("�������û���");
              	 }
              	if(jPasswordField.getPassword().length!=0){
              		char[] pass = jPasswordField.getPassword();
              	    psd = new String(pass);
              		
                  }
              	else{
              		jLabel3.setText("����������");
              	}
              	if(jTextField.getText().length()==0&&jPasswordField.getPassword().length==0){
              		jLabel3.setText("�������û���������");
              	}
              	
              	//jp.setVisible(false);
              	if(jTextField.getText().length()!=0&&jPasswordField.getPassword().length!=0){
              		 
              		    //���û���Ϣ���к���
              		    //String name:�û���     String psd:����
              		
              		System.out.println("name:"+name+"\n"+"psd:"+psd);
              		
              		
              		
              		
              		
              		
					if(true){         //������ȷ,�򿪶Ի�����
              			ChatInterface chatInterface = new ChatInterface(name);
					}
					
                  	dispose();
              	}
              	
              	
              	
              }
             
         });
         
         jb2.addActionListener(new ActionListener() {     //ȡ���¼�
             public void actionPerformed(ActionEvent e) {
             	
             	 if(jTextField.getText().length()!=0){
                 	jTextField.setText("");
                 }
             	if(jPasswordField.getPassword().length!=0){
             		jPasswordField.setText("");
                 }
             	if(jLabel3.getText().length()!=0){
             		jLabel3.setText("");
             	}
          
             }
         });

         
         this.setSize(380, 260);
         this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         this.setVisible(true);
         this.setTitle("��½");
          
     }

 }
