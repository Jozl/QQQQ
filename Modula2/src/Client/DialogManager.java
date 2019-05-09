package Client;

import java.awt.Component;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import com.ConnectionHelper;
import com.Message;

import db.beans.Group;
import db.beans.User;

public class DialogManager {
	private Map<String, UserTabUI> dialogTabMap = new ConcurrentHashMap<>();
	private Map<String, DialogUI> dialogMap = new HashMap<>();

	private ClientUI clientUI;

	public DialogManager(ClientUI clientUI) {
		this.clientUI = clientUI;
	}

	public DialogUI getDialog(String targetAccount) {
		return dialogMap.get(targetAccount);
	}

	public void updateDialogList(List<User> targets) {
		for (User target : targets) {
			if (target instanceof Group)
				continue;
			updateDialogList(target);
		}
	}

	public void updateDialogList(User target) {
		if (!dialogTabMap.containsKey(target.getAccount())) {
			UserTabUI clientUITab = new UserTabUI(clientUI, target);

			dialogTabMap.put(target.getAccount(), clientUITab);
			dialogMap.put(target.getAccount(), new DialogUI(clientUI.getClient(), clientUI.getSelf(), target));

			clientUITab.setAlignmentX(Component.LEFT_ALIGNMENT);
			// clientUITab.setHeadImage();
			clientUI.getPanelUserList().add(clientUITab);
			clientUI.getPanelUserList().revalidate();

			clientUI.getClient().getConnectionHelper().imageRequest(target.getAccount(), clientUI.getClient());
		}
	}

	public void updateDialogList(String userAccount) {
		try {
			dialogTabMap.get(userAccount).setHeadImage();
		} catch (NullPointerException e) {
			clientUI.setSelfHeadImage();
		}
	}

	public void updateDialogOnline(String userAccount, boolean online) {
		dialogTabMap.get(userAccount).setOnline(online);
		sortUserTab();
	}

	public void updateDialogOnline(User user, boolean online) {
		updateDialogList(user);
		dialogTabMap.get(user.getAccount()).setOnline(online);
		sortUserTab();
	}

	// public void refreshHeadImage(String userAccount) {
	// dialogTabMap.get(userAccount).setHeadImage();
	// }

	public void sortUserTab() {
		for (UserTabUI tabUI : dialogTabMap.values()) {
			if (!tabUI.isOnline())
				clientUI.getPanelUserList().add(tabUI);
		}
		clientUI.getPanelUserList().revalidate();
	}

	public void openDialog(String targetAccount) {
		DialogUI dialog = dialogMap.get(targetAccount);
		if (!dialog.isValid()) {
			dialog.validate();
			dialog.setVisible(true);
		} else {
			// 已经打开
			dialog.setAlwaysOnTop(true);
		}
	}

	public void closeAll() {
		for (DialogUI dialog : dialogMap.values()) {
			dialog.dispose();
		}
	}

	public void deliveMessage(Message<?> message) {
		String sender = message.getSender().startsWith(ConnectionHelper.PUBLIC) ? ConnectionHelper.PUBLIC
				: message.getSender();
		DialogUI dialog = dialogMap.get(sender);
		openDialog(sender);
		dialog.insertMessage(message);
	}
}
