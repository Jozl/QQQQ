package Client;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;
import java.net.UnknownHostException;
import java.util.Random;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;

import com.ConnectionHelper;
import com.Message;
import com.Translater;

import db.beans.User;

public class LoginUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private ConnectionHelper helper;

	private JPanel contentPane;
	private JTextField textServerArrd;
	private JTextField textServerPort;
	private JTextField textAccount;
	private JPasswordField passwordFieldPassword;

	public static void main(String[] args) {

		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					LoginUI frame = new LoginUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	private void setServerConnection() {
		InetSocketAddress targetAddr = null;
		try {
			targetAddr = new InetSocketAddress(InetAddress.getByName(textServerArrd.getText().trim()),
					Integer.parseInt(textServerPort.getText().trim()));
		} catch (NumberFormatException e) {
			e.printStackTrace();
		} catch (UnknownHostException e) {
			e.printStackTrace();
		}
		if (targetAddr != null) {
			helper.removeConnection(ConnectionHelper.SERVER);
			helper.addConnection(targetAddr, ConnectionHelper.SERVER);
		} else {
			JOptionPane.showMessageDialog(null, "下面别乱改", null, JOptionPane.ERROR_MESSAGE);
		}

	}

	public LoginUI() {
		try {
			helper = new ConnectionHelper(false);
		} catch (SocketException e1) {
			e1.printStackTrace();
			return;
		}

		setResizable(false);
		setTitle("\u8D26\u53F7\u767B\u5F55");
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BorderLayout(0, 0));

		JLabel labelTitle = new JLabel("QQ 2019 newest");
		contentPane.add(labelTitle, BorderLayout.NORTH);

		JPanel panelCenter = new JPanel();
		contentPane.add(panelCenter);
		panelCenter.setLayout(new BoxLayout(panelCenter, BoxLayout.X_AXIS));

		JLabel labelQQicon = new JLabel("");
		panelCenter.add(labelQQicon);
		labelQQicon.setIcon(new ImageIcon(LoginUI.class.getResource("/img/Login.png")));

		JPanel panelLoginInput = new JPanel();
		panelCenter.add(panelLoginInput);

		JLabel labelaccount = new JLabel("\u8D26\u53F7\uFF1A");
		labelaccount.setFont(new Font("华文新魏", Font.PLAIN, 24));

		textAccount = new JTextField();
		textAccount.setText("827850152");
		textAccount.setColumns(10);

		JLabel labelPassword = new JLabel("\u5BC6\u7801\uFF1A");
		labelPassword.setFont(new Font("华文新魏", Font.PLAIN, 24));

		passwordFieldPassword = new JPasswordField();

		JButton buttonLogin = new JButton("\u767B\u5F55");
		buttonLogin.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String account = textAccount.getText().trim();
				String password = new String(passwordFieldPassword.getPassword()).trim();

				if (!account.isEmpty() && !password.isEmpty()) {
					setServerConnection();
					helper.sendMessageToServer(new Message.messageBuilder<User>().Code(Message.M_LOGIN)
							.Sender("TEMP" + account + new Random().nextInt()).Receiver(ConnectionHelper.SERVER)
							.Payload(new User(account, Translater.cryptWithSHA256(password))).build());

					Message<?> message = helper.waitMessage(3000);

					if (message != null) {
						switch (message.getCode()) {
						case Message.M_LOGIN_ACCEPT:
							dispose();
							new ClientUI(helper, (User) message.getPayload())
									.setVisible(true);
							break;
						case Message.M_LOGIN_FAIL:
							JOptionPane.showMessageDialog(null, "登录失败", null, JOptionPane.ERROR_MESSAGE);
							break;
						case Message.M_LOGIN_REPEAT:
							JOptionPane.showMessageDialog(null, "重复登录", null, JOptionPane.ERROR_MESSAGE);
							break;
						default:
							break;
						}
					} else {
						JOptionPane.showMessageDialog(null, "服务器没开", null, JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		});
		buttonLogin.setFont(new Font("华文新魏", Font.BOLD, 20));

		JButton buttonRegister = new JButton("\u6CE8\u518C");
		buttonRegister.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setServerConnection();
				new RegisterUI(helper).setVisible(true);
			}
		});

		JButton button_1 = new JButton("\u627E\u56DE");
		button_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		GroupLayout gl_panelLoginInput = new GroupLayout(panelLoginInput);
		gl_panelLoginInput.setHorizontalGroup(
				gl_panelLoginInput.createParallelGroup(Alignment.TRAILING).addGroup(Alignment.LEADING,
						gl_panelLoginInput.createSequentialGroup().addContainerGap()
								.addGroup(gl_panelLoginInput.createParallelGroup(Alignment.LEADING, false)
										.addComponent(labelaccount, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE)
										.addComponent(labelPassword, GroupLayout.DEFAULT_SIZE, GroupLayout.DEFAULT_SIZE,
												Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED)
								.addGroup(gl_panelLoginInput.createParallelGroup(Alignment.TRAILING, false)
										.addComponent(buttonLogin, Alignment.LEADING, GroupLayout.DEFAULT_SIZE,
												GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
										.addComponent(passwordFieldPassword, Alignment.LEADING,
												GroupLayout.DEFAULT_SIZE, 118, Short.MAX_VALUE)
										.addComponent(textAccount, Alignment.LEADING, GroupLayout.DEFAULT_SIZE, 118,
												Short.MAX_VALUE))
								.addPreferredGap(ComponentPlacement.RELATED, 19, Short.MAX_VALUE)
								.addGroup(gl_panelLoginInput.createParallelGroup(Alignment.LEADING)
										.addComponent(button_1).addComponent(buttonRegister))
								.addContainerGap()));
		gl_panelLoginInput.setVerticalGroup(gl_panelLoginInput.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelLoginInput.createSequentialGroup().addGap(53)
						.addGroup(gl_panelLoginInput.createParallelGroup(Alignment.BASELINE)
								.addComponent(labelaccount, GroupLayout.PREFERRED_SIZE, 31, GroupLayout.PREFERRED_SIZE)
								.addComponent(textAccount, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE,
										GroupLayout.PREFERRED_SIZE)
								.addComponent(buttonRegister))
						.addPreferredGap(ComponentPlacement.UNRELATED)
						.addGroup(gl_panelLoginInput.createParallelGroup(Alignment.BASELINE).addComponent(labelPassword)
								.addComponent(passwordFieldPassword, GroupLayout.PREFERRED_SIZE,
										GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
								.addComponent(button_1))
						.addGap(29)
						.addComponent(buttonLogin, GroupLayout.PREFERRED_SIZE, 32, GroupLayout.PREFERRED_SIZE)
						.addGap(40)));
		panelLoginInput.setLayout(gl_panelLoginInput);

		JPanel panelSouth = new JPanel();
		contentPane.add(panelSouth, BorderLayout.SOUTH);
		panelSouth.setLayout(new BoxLayout(panelSouth, BoxLayout.X_AXIS));

		JLabel labelServerAddr = new JLabel("\u670D\u52A1\u5668\u5730\u5740");
		panelSouth.add(labelServerAddr);

		textServerArrd = new JTextField();
		textServerArrd.setText("127.0.0.1");
		panelSouth.add(textServerArrd);
		textServerArrd.setColumns(10);

		JLabel labelServerPort = new JLabel("\u670D\u52A1\u5668\u7AEF\u53E3");
		panelSouth.add(labelServerPort);

		textServerPort = new JTextField();
		textServerPort.setEnabled(false);
		textServerPort.setText(String.valueOf(ConnectionHelper.SERVER_PORT_Recv));
		panelSouth.add(textServerPort);
		textServerPort.setColumns(10);
	}
}
