package Client;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import com.ConnectionHelper;
import com.ImageChooser;
import com.Message;

import db.beans.User;

public class Client {
	private ConnectionHelper helper;
	private User self;
	private ClientUI clientUI;
	private DialogManager dialogManager;

	private Thread waiThread;

	public Client(ConnectionHelper helper, User self, ClientUI clientUI) {
		this.helper = helper;
		this.self = self;
		this.clientUI = clientUI;
		this.dialogManager = clientUI.getDialogManger();
	}

	public void waittingMessage() {
		waiThread = new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					handleMessage(helper.waitMessageForever());
				}
			}
		});
		waiThread.start();
	}

	public void shut() {
		helper.shut();
	}

	@SuppressWarnings("unchecked")
	public void handleMessage(Message<?> message) {
		switch (message.getCode()) {
		// case Message.M_USER_LOGOUT:
		// User user = User.rebulidUser((LinkedTreeMap<String, String>)
		// message.getPayload());
		// dialogManager.updateDialogOnline(user.getAccount(), true);
		// break;
		case Message.M_USER_KICK:
			if (message.getReceiver().equals(self.getAccount())) {
				JOptionPane.showMessageDialog(null, "服务器强制你下线");
				dialogManager.closeAll();
				clientUI.dispose();
			} else {
			}
			break;
		case Message.M_USER_LOGOUT:
			dialogManager.updateDialogOnline(message.getPayload().toString(), false);
			break;
		case Message.M_ONLINE:
			dialogManager.updateDialogOnline((User) message.getPayload(), true);
			break;
		case Message.M_ONLINE_LIST:
			for (String userAccount : (List<String>) message.getPayload()) {
				dialogManager.updateDialogOnline(userAccount, true);
			}
			break;
		case Message.M_ONLINE_FRIENDS:
			List<User> list = (List<User>) message.getPayload();
			helper.imageRequest(self.getAccount(), this);
			dialogManager.updateDialogList(list);
			break;
//		case Message.M_IMAGE_REQUEST:
//			JFileChooser chooser = new ImageChooser();
//			chooser.showDialog(null, "你的新头像");
//			File image = chooser.getSelectedFile();
//			if (image.isFile()) {
//				helper.sendHeadImage(image, self.getAccount());
//			}
//			break;
//		case Message.M_IMAGE_UPDATE_SUCCEED:
//			clientUI.setSelfHeadImage();
//			break;
		case Message.M_IMAGE_UPDATE:
			String userAccount = (String) message.getPayload();
			helper.imageRequest(userAccount, this);
			break;
		case Message.M_FILE_ASK:
			// 发文件请求
			dialogManager.deliveMessage(message);
			dialogManager.getDialog(message.getSender()).prepareFile((String) message.getPayload(), true);
			break;
		case Message.M_FILE_READY:
			// 已准备好tcp socket 来接收文件
			dialogManager.getDialog(message.getSender()).fileUI().sendFile(
					helper.getConnectionAddress(ConnectionHelper.SERVER).getAddress(),
					Integer.parseInt(message.getPayload().toString()));
			break;
		case Message.M_FILE_REJECT:
			// 被拒绝
			dialogManager.getDialog(message.getSender()).rejected();
			break;
		case Message.M_USER_TO_USER:
			if (!message.getSender().contains(self.getAccount()))
				dialogManager.deliveMessage(message);
			break;
		// case Message.M_FILE_ADDRESS:
		// try {
		// InetSocketAddress address = new InetSocketAddress(
		// InetAddress
		// .getByName(message.getPayload().toString().split(":")[0].replace("/", "")),
		// Integer.parseInt(message.getPayload().toString().split(":")[1]));
		// dialogManager.getDialog(message.getSender()).fileUI().setAddr(address);
		// } catch (NumberFormatException | UnknownHostException e) {
		// e.printStackTrace();
		// }
		// break;
		default:
			break;
		}
	}

	public void sendMessage(Message<?> message) {
		helper.sendMessageToServer(message);
	}

	public ConnectionHelper getConnectionHelper() {
		return helper;
	}

	public User getSelf() {
		return self;
	}
	
	public DialogManager getDialogManager() {
		return dialogManager;
	}

	public void updateHeadImage(String userAccount) {
		dialogManager.updateDialogList(userAccount);
	}
}
