package Client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;

import com.Message;

import db.beans.User;

public class UserTabUI extends JToolBar {
	private static final long serialVersionUID = 1L;

	private JLabel username;

	private boolean isOnline = false;
	private User self = null;
	private JButton buttonIcon;
	private JLabel spaces;

	public UserTabUI(ClientUI upperUi, User self, Boolean isUserSelf, URL iconPath) {

		setFloatable(false);
		this.self = self;

		username = new JLabel(self.getUsername());

		buttonIcon = new JButton();
		buttonIcon.setBounds(new Rectangle(0, 0, 40, 40));
		buttonIcon.setMargin(new Insets(0, 0, 0, 0));
		buttonIcon.setBorder(new LineBorder(new Color(0, 0, 0)));

		ImageIcon icon = new ImageIcon(iconPath);
		Image temp = icon.getImage().getScaledInstance(buttonIcon.getHeight(), buttonIcon.getHeight(),
				Image.SCALE_DEFAULT);
		icon = new ImageIcon(temp);
		buttonIcon.setIcon(icon);

		spaces = new JLabel("                                                    "
				+ "                                                              "
				+ "                                                              ");
		spaces.setFont(new Font("ËÎÌו", Font.PLAIN, 5));
		spaces.setBorder(null);
		spaces.setEnabled(false);

		add(buttonIcon);
		add(username);
		add(spaces);

		buttonIcon.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (!isUserSelf)
					upperUi.getDialogManger().openDialog(self.getAccount());
				else {
					upperUi.getClient().getConnectionHelper().sendMessageToServer(new Message.messageBuilder<>()
							.Code(Message.M_IMAGE_UPDATE).Sender(upperUi.getSelf().getAccount()).build());
				}
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				upperUi.setFloatFrame(UserTabUI.this, e.getXOnScreen(), e.getYOnScreen());
			}
		});
		if (!isUserSelf) {

			MouseAdapter bgmMouseAdapter = new MouseAdapter() {

				@Override
				public void mouseClicked(MouseEvent e) {
					upperUi.getDialogManger().openDialog(self.getAccount());
				}

				@Override
				public void mouseEntered(MouseEvent e) {
					setBackground(new Color(0, 204, 153));
					spaces.setBackground(getBackground());
				}

				@Override
				public void mouseExited(MouseEvent e) {
					setBackground(isOnline ? Color.GREEN : Color.GRAY);
					spaces.setBackground(getBackground());
				}
			};
			addMouseListener(bgmMouseAdapter);
			spaces.addMouseListener(bgmMouseAdapter);
			setOnline(false);
		}
	}

	/**
	 * @wbp.parser.constructor
	 */
	public UserTabUI(ClientUI upperUi, User self, URL iconPath) {
		this(upperUi, self, false, iconPath);
	}

	public void setOnline(boolean isOnline) {
		if (isOnline) {
			setBackground(Color.GREEN);
			spaces.setBackground(getBackground());
			this.isOnline = true;
		} else {
			setBackground(Color.GRAY);
			spaces.setBackground(getBackground());
			this.isOnline = false;
		}
	}

	public boolean isOnline() {
		return isOnline;
	}

	public User getUser() {
		return self;
	}

	public void setHeadImage() {
		ImageIcon icon = new ImageIcon("./src/Client/HeadImages/" + self.getAccount() + ".jpg");
		Image temp = icon.getImage().getScaledInstance(buttonIcon.getHeight(), buttonIcon.getHeight(),
				Image.SCALE_AREA_AVERAGING);
		icon = new ImageIcon(temp);
		buttonIcon.setIcon(icon);
	}
}
