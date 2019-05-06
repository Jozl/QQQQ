import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import Client.UserTabUI;
import db.beans.User;

import javax.swing.BoxLayout;
import javax.swing.JToolBar;
import javax.swing.JButton;

public class testtt extends JFrame {

	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					testtt frame = new testtt();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public testtt() {
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(new BoxLayout(contentPane, BoxLayout.Y_AXIS));
		
		JToolBar toolBar = new JToolBar();
		contentPane.add(toolBar);
		
		JButton btnNewButton = new JButton("New button");
		toolBar.add(btnNewButton);
		
		JToolBar toolBar_1 = new JToolBar();
		contentPane.add(new UserTabUI(null, new User(), UserTabUI.class.getResource("/img/LoginBG.JPG")));
		contentPane.add(toolBar_1);
		
		JButton btnNewButton_2 = new JButton("New button");
		toolBar_1.add(btnNewButton_2);
		
		JButton btnNewButton_3 = new JButton("New button");
		toolBar_1.add(btnNewButton_3);
		btnNewButton_3.setAlignmentX(0.25f);
		
		JButton btnNewButton_1 = new JButton("New button");
		toolBar_1.add(btnNewButton_1);
	}

}
