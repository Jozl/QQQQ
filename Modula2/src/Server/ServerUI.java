package Server;

import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.Element;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDesktopPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.border.EmptyBorder;

import org.bouncycastle.crypto.agreement.srp.SRP6Client;

import com.ConnectionHelper;
import com.Dao;
import com.Message;
import com.Translater;

import db.beans.User;
import net.miginfocom.layout.UnitValue;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;

public class ServerUI extends JFrame {
	private static final long serialVersionUID = 1L;

	Set<String> onlineUsersList = new HashSet<>();// ���ϣ�û���ظ�Ԫ��
	Map<String, List<Message<?>>> roamingMessageMap = new HashMap<>();
	Map<String, String> fileAttemptMap = new HashMap<>();
	ConnectionHelper helper;
	Dao db = new Dao();

	private JPanel contentPane;
	private JTextField textField;
	private JTextArea textArea;

	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					ServerUI frame = new ServerUI();
					frame.serving();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	public ServerUI() {
		setAlwaysOnTop(true);

		try {
			helper = new ConnectionHelper(true);
		} catch (SocketException e1) {
			e1.printStackTrace();
			return;
		}

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(1000, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		JPanel panel = new JPanel();
		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));

		textField = new JTextField();
		panel.add(textField);
		textField.setColumns(10);

		JButton btnNewButton = new JButton("DOIT");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String command = textField.getText();
				if (!command.isEmpty()) {
					textArea.setText("");
					if (command.equalsIgnoreCase("show online")) {
						for (String user : onlineUsersList) {
							textArea.insert(user + "\n", 0);
						}
					}
					if (command.startsWith("kick")) {
						if (command.equalsIgnoreCase("kick all")) {
							for (String userAccount : onlineUsersList) {
								helper.sendMessageAsServer(new Message.messageBuilder<>().Code(Message.M_USER_KICK)
										.Sender(ConnectionHelper.SERVER).Receiver(userAccount).build());
							}
							onlineUsersList.clear();
							return;
						}

						String userAccount = command.replace("kick out ", "");
						if (onlineUsersList.contains(userAccount)) {
							helper.sendMessageToAllConnections(
									new Message.messageBuilder<String>().Code(Message.M_USER_KICK)
											.Sender(ConnectionHelper.SERVER).Payload(userAccount).build());
							onlineUsersList.remove(userAccount);
							helper.removeConnection(userAccount);
							textArea.insert("succeed!", 0);
						} else {
							textArea.insert("no such user online", 0);
						}
					}
				}
			}
		});
		panel.add(btnNewButton);

		JPanel panel_1 = new JPanel();
		panel_1.setLayout(new BorderLayout(0, 0));

		JScrollPane scrollPane = new JScrollPane();
		panel_1.add(scrollPane, BorderLayout.CENTER);

		JDesktopPane desktopPane = new JDesktopPane();

		JButton btnNewButton_1 = new JButton("\u8E22");
		btnNewButton_1.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				for (String userAccount : onlineUsersList) {
					helper.sendMessageAsServer(new Message.messageBuilder<>().Code(Message.M_USER_KICK)
							.Sender(ConnectionHelper.SERVER).Receiver(userAccount).build());
					helper.removeConnection(userAccount);
				}
				onlineUsersList.clear();
				textArea.setText("");
				textArea.insert("������", 0);
			}
		});
		btnNewButton_1.setBounds(10, 10, 93, 23);
		desktopPane.add(btnNewButton_1);

		JButton btnNewButton_2 = new JButton("\u770B");
		btnNewButton_2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				textArea.setText("");
				for (String user : onlineUsersList) {
					textArea.insert(user + "\n", 0);
				}
			}
		});
		btnNewButton_2.setBounds(321, 10, 93, 23);
		desktopPane.add(btnNewButton_2);

		textArea = new JTextArea();
		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING)
				.addComponent(panel, GroupLayout.PREFERRED_SIZE, 424, GroupLayout.PREFERRED_SIZE)
				.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, 424, GroupLayout.PREFERRED_SIZE)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(desktopPane, GroupLayout.DEFAULT_SIZE, 422, Short.MAX_VALUE).addContainerGap())
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(textArea, GroupLayout.PREFERRED_SIZE, 422, GroupLayout.PREFERRED_SIZE)
						.addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.LEADING).addGroup(gl_contentPane
				.createSequentialGroup()
				.addComponent(panel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(desktopPane, GroupLayout.PREFERRED_SIZE, 73, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(textArea, GroupLayout.PREFERRED_SIZE, 121, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.UNRELATED)
				.addComponent(panel_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addContainerGap(63, Short.MAX_VALUE)));
		contentPane.setLayout(gl_contentPane);
	}

	public void serving() {
		System.out.println("����������");

		// ����˭���ȥ��
		// ���߳�Э��
		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// while (true) {
		// try {
		// Thread.sleep(10000);
		// for (String userAccount : onlineUsersLivingMap.keySet()) {
		// boolean isAlive = onlineUsersLivingMap.get(userAccount);
		// if (!isAlive) {
		// onlineUsersLivingMap.remove(userAccount);
		// helper.removeConnection(userAccount);
		// helper.sendMessageToAllConnections(
		// new Message.messageBuilder<String>().Code(Message.M_USER_OFFLINE)
		// .Sender(ConnectionHelper.SERVER).Payload(userAccount).build());
		// }
		// }
		// } catch (InterruptedException e) {
		// e.printStackTrace();
		// }
		// }
		// }
		// }).start();

		// �ӻ��
		new Thread(new Runnable() {

			@Override
			public void run() {
				User user = null;
				// æ��
				while (true) {
					Message<?> message = helper.waitMessageForever();
					System.out.println("�ֲ�·��");
					switch (message.getCode()) {

					case Message.M_REGIST:
						System.out.println("��Ҫע��");
						user = (User) message.getPayload();
						int account = db.addNewUser(user.setPassword(
								addSalt(user.getPassword(), user.getUsername() + user.getPassword() + "777")));
						System.out.println("��������˺ţ�" + account);
						switch (account) {
						case -1:
						case -2:
							helper.sendMessageAsServer(new Message.messageBuilder<>().Code(Message.M_REGIST_FAIL)
									.Sender(ConnectionHelper.SERVER).build());
							break;
						default:
							helper.sendMessageAsServer(new Message.messageBuilder<String>()
									.Code(Message.M_REGIST_ACCEPT).Sender(ConnectionHelper.SERVER)
									.Receiver(message.getSender()).Payload(String.valueOf(account)).build());
							break;
						}
						break;

					case Message.M_LOGIN:
						System.out.println("���˵�½");

						// user = User.rebulidUser((LinkedTreeMap<String, String>)
						// message.getPayload());// �պ�����
						user = (User) message.getPayload();
						String userName = db.getUser(user.getAccount()).getUsername();
						System.out.println(user);
						if (db.isUserInTable(
								user.setPassword(addSalt(user.getPassword(), userName + user.getPassword() + "777")))) {// ���Ͳ���
																														// �պ��ð�
							// �����û��б� +1
							if (onlineUsersList.add(user.getAccount())) {
								System.out.println("���ܵ�½");
								// ����������Ϣ
								helper.switchConnectionTarget(message.getSender(), user.getAccount());
								// ���͵�¼�ɹ���message
								helper.sendMessageAsServer(new Message.messageBuilder<User>()
										.Code(Message.M_LOGIN_ACCEPT).Receiver(user.getAccount())
										.Payload(db.getUser(user.getAccount()).withdrawPassword()).build());
								// �㲥˭�����ˣ��������user�������ĺ����б�͵�ǰ�����û���Ϣ
								broadcast(user);
								if (roamingMessageMap.containsKey(user.getAccount())) {
									System.out.println("�����л�����˵");
									for (Message<?> hisMessage : roamingMessageMap.get(user.getAccount())) {
										helper.sendMessageAsServer(hisMessage);
									}
								}
							} else {
								System.out.println("�ظ���½");

								helper.sendMessageAsServer(new Message.messageBuilder<>().Code(Message.M_LOGIN_REPEAT)
										.Sender(ConnectionHelper.SERVER).Receiver(message.getSender()).build());
							}
						} else {
							System.out.println("��Ч��½");

							helper.sendMessageAsServer(new Message.messageBuilder<>().Code(Message.M_LOGIN_FAIL)
									.Sender(ConnectionHelper.SERVER).Receiver(message.getSender()).build());
						}
						break;

					case Message.M_USER_LOGOUT:
						helper.removeConnection(message.getSender());
						onlineUsersList.remove(message.getSender());
						helper.sendMessageToAllConnections(
								new Message.messageBuilder<String>().Code(Message.M_USER_LOGOUT)
										.Sender(ConnectionHelper.SERVER).Payload(message.getSender()).build());
						break;

					// case Message.M_USER_BREATHING:
					// onlineUsersLivingMap.remove(message.getSender());
					// onlineUsersLivingMap.put(message.getSender(), true);
					// break;

					case Message.M_IMAGE_REQUEST:
						helper.sendImage(message);
						break;

					case Message.M_IMAGE_UPDATE:
						helper.waitImageUpdate(message.getSender());
						helper.sendMessageToServer(new Message.messageBuilder<>().Code(Message.M_IMAGE_REQUEST)
								.Receiver(message.getSender()).build());
						break;

					case Message.M_FILE_ACCEPT:
						// �������Լ���tcp����Ȼ��
						if (fileAttemptMap.get(message.getReceiver()).equalsIgnoreCase(message.getSender())) {
							// �����ļ��Ǹ�
							helper.sendMessageAsServer(new Message.messageBuilder<String>().Code(Message.M_FILE_ADDRESS)
									.Sender(message.getReceiver()).Receiver(message.getSender())
									.Payload(helper.getConnectionAddress(message.getReceiver()).getAddress() + ":"
											+ helper.getConnectionAddress(message.getReceiver()).getPort())
									.build());
							// �����ļ��Ǹ�
							helper.sendMessageAsServer(
									new Message.messageBuilder<String>().Code(Message.M_FILE_ADDRESS)
											.Sender(message.getSender()).Receiver(message.getReceiver())
											.Payload(helper.getConnectionAddress(message.getSender()).getAddress() + ":"
													+ helper.getConnectionAddress(message.getSender()).getPort())
											.build());
						}
						break;
					case Message.M_FILE_ASK:
						fileAttemptMap.put(message.getSender(), message.getReceiver());
					case Message.M_FILE_READY:
					case Message.M_USER_TO_USER:
						if (message.getReceiver().equalsIgnoreCase(ConnectionHelper.PUBLIC)) {
							System.out.println("һ��Ⱥ��");
							helper.sendMessageToAllConnections(
									message.setSender(ConnectionHelper.PUBLIC + message.getSender()));
							System.out.println("�ҷ���ѽ");
						} else if (helper.containsConnection(message.getReceiver())) {
							System.out.println("һ��˽��");
							helper.sendMessageAsServer(message);
							System.out.println("�ҷ���ѽ");
						} else {
							System.out.println("������Ϣ+1");
							List<Message<?>> messagesList = roamingMessageMap.containsKey(message.getReceiver())
									? roamingMessageMap.remove(message.getReceiver())
									: new ArrayList<>();
							messagesList.add(message);
							roamingMessageMap.put(message.getReceiver(), messagesList);
						}
						break;

					default:
						System.out.println("@@@");
						break;
					}
				}
			}
		}).start();

		// new Thread(new Runnable() {
		//
		// @Override
		// public void run() {
		// helper.waitImage();
		// }
		// }).start();
	}

	// �㲥�����û���Ϣ
	private void broadcast(User user) {
		for (String userAccount : onlineUsersList) {
			if (userAccount.equalsIgnoreCase(user.getAccount())) {
				// List<User> friendsList = (ArrayList<User>)
				// db.getHisFriends(user.getAccount());
				// Map<User, Boolean> onlineMap = new HashMap<>();
				// for (User friend : friendsList) {
				// onlineMap.put(friend, onlineUsersList.contains(friend));
				// }
				helper.sendMessageAsServer(new Message.messageBuilder<List<User>>().Code(Message.M_ONLINE_FRIENDS)
						.Receiver(userAccount).Payload(db.getHisFriends(user.getAccount())).build());
				Set<String> temp = new HashSet<>();
				temp.addAll(onlineUsersList);
				temp.remove(user.getAccount());
				System.out.println(temp);
				helper.sendMessageAsServer(new Message.messageBuilder<Set<String>>().Code(Message.M_ONLINE_LIST)
						.Receiver(userAccount).Payload(temp).build());
			} else {
				helper.sendMessageAsServer(new Message.messageBuilder<User>().Code(Message.M_ONLINE)
						.Receiver(userAccount).Payload(db.getUser(user.getAccount()).withdrawPassword()).build());
			}
		}
	}

	public String addSalt(String password, String salt) {
		return Translater.cryptWithSHA256(password + salt);
	}

	public void sendUpdateSucceed(String sender) {
		helper.sendMessageAsServer(new Message.messageBuilder<>().Code(Message.M_IMAGE_UPDATE_SUCCEED)
				.Receiver(sender).build());
	}

//	public void sendImageUpdateSucceed(String userAccount) {
//		helper.sendMessageAsServer(new Message.messageBuilder<String>().Code(Message.M_IMAGE_UPDATE_SUCCEED)
//				.Receiver(userAccount).build());
//	}
}
