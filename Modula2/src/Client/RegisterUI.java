package Client;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Random;

import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.ConnectionHelper;
import com.Message;
import com.Translater;

import db.beans.User;

public class RegisterUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextField textPassword1;
	private JTextField textPassword2;
	private JTextField textUsername;
	private JButton buttonPost;
	private JLabel labelPassword2;
	private JLabel labelPassword1;
	private JLabel labelUsername;

	public RegisterUI(ConnectionHelper helper) {

		// **+
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		buttonPost = new JButton("\u63D0\u4EA4\u6CE8\u518C\u4FE1\u606F");
		buttonPost.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String username = textUsername.getText();
				String password1 = textPassword1.getText();
				String password2 = textPassword2.getText();
				String tempAccount = "TEMP-" + new Random().nextInt();
				if (username.isEmpty() || password1.isEmpty() || password2.isEmpty()) {
					JOptionPane.showMessageDialog(RegisterUI.this, "你没填完整");
				} else if (!password1.equals(password2)) {
					JOptionPane.showMessageDialog(RegisterUI.this, "两次密码不一致");
				} else {
					helper.sendMessageToServer(new Message.messageBuilder<User>().Code(Message.M_REGIST)
							.Sender(tempAccount).Receiver(ConnectionHelper.SERVER)
							.Payload(new User(null, Translater.cryptWithSHA256(password2), username, null)).build());
				}

				Message<?> message = helper.waitMessage(3000);
				if (message != null) {
					// HTML格式 大概吧
					JOptionPane.showMessageDialog(RegisterUI.this, new JLabel(
							"<html><font size='4'>注册成功</font><br/><font size='4'>这是你的QQ号</font><br/><font size='6' color='blue'>"
									+ message.getPayload() + "</font><br/><font size='4'>快去登录试试8</font><br/></html>"));
					dispose();
				}
			}
		});

		textPassword1 = new JTextField();
		textPassword1.setColumns(10);

		textPassword2 = new JTextField();
		textPassword2.setColumns(10);

		textUsername = new JTextField();
		textUsername.setColumns(10);

		labelUsername = new JLabel("\u6635\u79F0\uFF1A");
		labelUsername.setFont(new Font("宋体", Font.BOLD | Font.ITALIC, 18));

		labelPassword1 = new JLabel("\u5BC6\u7801\uFF1A");
		labelPassword1.setFont(new Font("宋体", Font.BOLD | Font.ITALIC, 18));

		labelPassword2 = new JLabel("\u5BC6\u7801\uFF1A");
		labelPassword2.setFont(new Font("宋体", Font.BOLD | Font.ITALIC, 18));
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane
				.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
						gl_contentPane.createSequentialGroup().addContainerGap()
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addComponent(labelPassword1, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(labelPassword2, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(labelUsername, GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE))
								.addGap(18)
								.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
										.addComponent(textPassword1, GroupLayout.DEFAULT_SIZE, 137, Short.MAX_VALUE)
										.addComponent(textUsername, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 137,
												Short.MAX_VALUE)
										.addComponent(textPassword2, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 137,
												Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED).addComponent(buttonPost)
								.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup().addContainerGap(51, Short.MAX_VALUE)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
								.addComponent(textUsername, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(labelUsername))
						.addPreferredGap(ComponentPlacement.RELATED)
						.addGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
								.addGroup(gl_contentPane.createSequentialGroup().addGap(146).addComponent(buttonPost))
								.addGroup(gl_contentPane.createSequentialGroup().addGap(33)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
												.addComponent(textPassword1, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(labelPassword1))
										.addGap(36)
										.addGroup(gl_contentPane.createParallelGroup(Alignment.BASELINE)
												.addComponent(textPassword2, GroupLayout.PREFERRED_SIZE,
														GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
												.addComponent(labelPassword2))))
						.addContainerGap()));
		contentPane.setLayout(gl_contentPane);
	}
}
