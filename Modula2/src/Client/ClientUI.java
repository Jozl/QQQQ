package Client;

import java.awt.BorderLayout;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ScrollPaneConstants;
import javax.swing.border.EmptyBorder;

import com.ConnectionHelper;
import com.Message;

import db.beans.Group;
import db.beans.User;

public class ClientUI extends JFrame {
	private static final long serialVersionUID = 1L;

	public static int UIWIDTH = 400;

	private JPanel contentPane;
	private JPanel panelUserList;

	private DialogManager dialogManager;
	private UserInfoUI userInfo;
	private UserTabUI selfTab;

	private Client client;
	private User self;

	public ClientUI(ConnectionHelper helper, User self) {
		this.self = self;
		this.dialogManager = new DialogManager(this);
		this.client = new Client(helper, this.self, this);
		this.userInfo = new UserInfoUI();

		setResizable(false);
		setAlwaysOnTop(true);
		setBounds(new Rectangle(1200, 200, 400, 700));
		System.out.println("*********************************");
		System.out.println("Œ“ «" + self);

		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);

		JPanel panelSelf = new JPanel();
		contentPane.add(panelSelf, BorderLayout.NORTH);
		panelSelf.setLayout(new BoxLayout(panelSelf, BoxLayout.Y_AXIS));

		selfTab = new UserTabUI(ClientUI.this, self, true, UserTabUI.class.getResource("/img/LoginBG.JPG"));
		selfTab.setAlignmentX(LEFT_ALIGNMENT);
		panelSelf.add(selfTab);
		panelSelf.revalidate();

		panelUserList = new JPanel();
		panelUserList.setLayout(new BoxLayout(panelUserList, BoxLayout.Y_AXIS));
		JScrollPane scrollPane = new JScrollPane();
		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
		scrollPane.setViewportView(panelUserList);
		scrollPane.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
		contentPane.add(scrollPane);

		JButton btnNewButton = new JButton("New button");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dialogManager.sortUserTab();
			}
		});
		contentPane.add(btnNewButton, BorderLayout.SOUTH);

		addWindowListener(new WindowAdapter() {
			public void windowClosing(WindowEvent e) {
				client.sendMessage(new Message.messageBuilder<>().Code(Message.M_USER_LOGOUT)
						.Sender(client.getSelf().getAccount()).build());
				client.shut();
				dialogManager.closeAll();
				userInfo.dispose();
				dispose();
			}
		});

		dialogManager.updateDialogList(new Group(ConnectionHelper.PUBLIC, "¡ƒÃÏ “"));
		dialogManager.updateDialogOnline(ConnectionHelper.PUBLIC, true);

		client.waittingMessage();
	}

	// public void openDialog(String targetName) {
	// dialogManager.openDialog(targetName);
	// }

	public void setFloatFrame(UserTabUI clientTab, int x, int y) {
		userInfo.bind(clientTab.getUser());
		userInfo.setBounds(x - userInfo.getBounds().width, y, userInfo.getBounds().width, userInfo.getBounds().height);
		userInfo.setVisible(true);
	}

	public DialogManager getDialogManger() {
		return dialogManager;
	}

	public JPanel getPanelUserList() {
		return panelUserList;
	}

	public User getSelf() {
		return self;
	}

	public Client getClient() {
		return client;
	}
	
	public void setSelfHeadImage() {
		selfTab.setHeadImage();
	}

}