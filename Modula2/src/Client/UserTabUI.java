package Client;

import java.awt.Color;
import java.awt.Font;
import java.awt.Image;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.channels.FileChannel;
import java.nio.file.CopyOption;
import java.nio.file.Files;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.swing.border.LineBorder;

import com.ImageChooser;
import com.Message;

import db.beans.Group;
import db.beans.User;

public class UserTabUI extends JToolBar {
	private static final long serialVersionUID = 1L;

	private JLabel username;

	private boolean isOnline = false;
	private User self = null;
	private JButton buttonIcon;
	private JLabel spaces;

	public UserTabUI(ClientUI upperUi, User self, Boolean isUserSelf) {

		setFloatable(false);
		this.self = self;

		username = new JLabel(self.getUsername());

		buttonIcon = new JButton();
		buttonIcon.setBounds(new Rectangle(0, 0, 40, 40));
		buttonIcon.setMargin(new Insets(0, 0, 0, 0));
		buttonIcon.setBorder(new LineBorder(new Color(0, 0, 0)));

		ImageIcon icon = new ImageIcon(UserTabUI.class.getResource("/img/LoginBG.JPG"));
		Image temp = icon.getImage().getScaledInstance(buttonIcon.getHeight(), buttonIcon.getHeight(),
				Image.SCALE_DEFAULT);
		icon = new ImageIcon(temp);
		buttonIcon.setIcon(icon);

		spaces = new JLabel("                                                    "
				+ "                                                              "
				+ "                                                              ");
		spaces.setFont(new Font("宋体", Font.PLAIN, 5));
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
				else if (e.getButton() == MouseEvent.BUTTON3) {
					System.out.println("点了一次");
					new Thread(new Runnable() {

						@Override
						public void run() {
							// TODO Auto-generated method stub

							JFileChooser chooser = new ImageChooser();
							chooser.showDialog(null, "你的新头像");
							File image = chooser.getSelectedFile();
							if (image.isFile()) {
								try (FileInputStream in = new FileInputStream(image);
										FileOutputStream out = new FileOutputStream(
												"./src/Client/HeadImages/" + self.getAccount() + ".jpg")) {
									int b = 0;
									while ((b = in.read()) != -1) {
										out.write(b);
									}
								} catch (IOException e1) {
									e1.printStackTrace();
								}
								setHeadImage();
								upperUi.getClient().getConnectionHelper().imageUpload(self.getAccount());
							}
						}
					}).start();
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
	public UserTabUI(ClientUI upperUi, User self) {
		this(upperUi, self, false);
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
//		if(self instanceof Group)
//			return;
		System.out.println("读" + self.getAccount());
		try {
			File file = new File("./src/Client/HeadImages/" + self.getAccount() + ".jpg");
			if (!file.isFile() || file == null)
				return;
			try {
				BufferedImage bufferedImage = ImageIO.read(file);
				ImageIcon icon = new ImageIcon(bufferedImage);
				Image temp = icon.getImage().getScaledInstance(buttonIcon.getHeight(), buttonIcon.getHeight(),
						Image.SCALE_DEFAULT);
				System.out.println("读完" + self.getAccount());
				icon = new ImageIcon(temp);
				buttonIcon.setIcon(icon);
			} catch (NullPointerException e) {
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		// ImageIcon icon = new ImageIcon("./src/Client/HeadImages/" + self.getAccount()
		// + ".jpg");
	}
}
