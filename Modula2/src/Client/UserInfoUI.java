package Client;

import java.awt.Color;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import db.beans.Group;
import db.beans.User;
import javax.swing.BoxLayout;

public class UserInfoUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private User self;

	private JPanel contentPane;
	private JLabel labelName;
	private JLabel labelBG;
	private JLabel labelAccount;

	public UserInfoUI() {
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseExited(MouseEvent e) {
				try {
					Thread.sleep(200);
				} catch (InterruptedException e1) {
					e1.printStackTrace();
				}
				setVisible(false);
			}
		});
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		setUndecorated(true);

		setBackground(new Color(0, 204, 102));
		setBounds(100, 100, 251, 166);

		contentPane.setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBackground(Color.WHITE);
		panel.setBounds(20, 30, 120, 35);
		contentPane.add(panel);
		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
		
		labelAccount = new JLabel("New label");
		panel.add(labelAccount);
		
				labelName = new JLabel("New label");
				panel.add(labelName);
				labelName.setForeground(Color.YELLOW);
		
		labelBG = new JLabel("");
		labelBG.setBounds(0, 0, 251, 166);
		contentPane.add(labelBG);
	}

	public void bind(User user) {
		// 确定是群还是人
		try {
			self = (Group) user;
		} catch (ClassCastException e) {
			self = user;
		}

		labelAccount.setText(self.getAccount());
		labelName.setText(self.getUsername());
		
		ImageIcon icon = new ImageIcon("./src/Client/HeadImages/"+self.getAccount()+".jpg");
		Image temp = icon.getImage().getScaledInstance(labelBG.getHeight(), labelBG.getHeight(),
				Image.SCALE_SMOOTH);
		icon = new ImageIcon(temp);
		labelBG.setIcon(icon);
	}
}
