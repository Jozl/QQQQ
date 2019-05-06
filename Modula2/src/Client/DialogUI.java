package Client;

import java.awt.Color;
import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.sql.Timestamp;

import javax.swing.BoxLayout;
import javax.swing.GroupLayout;
import javax.swing.GroupLayout.Alignment;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.SwingUtilities;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

import com.ConnectionHelper;
import com.Message;

import db.beans.User;

public class DialogUI extends JFrame {
	private static final long serialVersionUID = 1L;

	private JPanel contentPane;
	private JTextArea textAreaInput;
	private JScrollPane panelDisplay;
	private JPanel panelHisMessage;

	Client client;
	private User self;
	private User target;
	private FileUI fileUI;
	private FileUIProgressBar progressBar;

	public DialogUI(Client client, User selfU, User targetU) {
		this.client = client;
		this.self = selfU;
		this.target = targetU;
		fileUI = new FileUI(client, this);
		progressBar = new FileUIProgressBar();
		JPanel fileUIPanel = new JPanel();
		fileUIPanel.add(fileUI);
		fileUIPanel.add(progressBar);

		setTitle(target.getUsername());
		setDefaultCloseOperation(DISPOSE_ON_CLOSE);
		setBounds(100, 100, 772, 492);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);

		panelDisplay = new JScrollPane();
		panelDisplay.setAlignmentY(Component.TOP_ALIGNMENT);

		JButton buttonSendMessage = new JButton("发送");
		buttonSendMessage.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				String text = textAreaInput.getText();
				if (text.isEmpty())
					return;
				Message<String> message = new Message.messageBuilder<String>().Code(Message.M_USER_TO_USER)
						.Sender(self.getAccount()).Receiver(target.getAccount()).Payload(text).build();
				client.sendMessage(message);
				insertMessage(message);
			}
		});

		JScrollPane panelInput = new JScrollPane();

		JToolBar toolBar_1 = new JToolBar();
		toolBar_1.setFloatable(false);

		GroupLayout gl_contentPane = new GroupLayout(contentPane);
		gl_contentPane.setHorizontalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
				.addGroup(gl_contentPane.createSequentialGroup()
						.addComponent(panelInput, GroupLayout.DEFAULT_SIZE, 679, Short.MAX_VALUE)
						.addPreferredGap(ComponentPlacement.UNRELATED).addComponent(buttonSendMessage))
				.addComponent(toolBar_1, GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
				.addComponent(panelDisplay, GroupLayout.DEFAULT_SIZE, 746, Short.MAX_VALUE)
				.addGroup(Alignment.LEADING, gl_contentPane.createSequentialGroup().addContainerGap()
						.addComponent(fileUIPanel, GroupLayout.DEFAULT_SIZE, 726, Short.MAX_VALUE).addContainerGap()));
		gl_contentPane.setVerticalGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING).addGroup(gl_contentPane
				.createSequentialGroup().addComponent(fileUIPanel, GroupLayout.DEFAULT_SIZE, 72, Short.MAX_VALUE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(panelDisplay, GroupLayout.PREFERRED_SIZE, 264, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addComponent(
						toolBar_1, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
				.addPreferredGap(ComponentPlacement.RELATED)
				.addGroup(gl_contentPane.createParallelGroup(Alignment.TRAILING)
						.addGroup(gl_contentPane.createSequentialGroup().addComponent(buttonSendMessage)
								.addContainerGap())
						.addComponent(panelInput, GroupLayout.PREFERRED_SIZE, 66, GroupLayout.PREFERRED_SIZE))));
		fileUIPanel.setLayout(new BoxLayout(fileUIPanel, BoxLayout.Y_AXIS));

		panelHisMessage = new JPanel();
		panelDisplay.setViewportView(panelHisMessage);
		panelHisMessage.setLayout(new BoxLayout(panelHisMessage, BoxLayout.Y_AXIS));

		JButton buttonSendFile = new JButton("\u53D1\u9001\u6587\u4EF6");
		buttonSendFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				JFileChooser chooser = new JFileChooser("");
				chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
				chooser.showDialog(new JLabel(), "选择");
				File file = chooser.getSelectedFile();
				if (file.isFile()) {
					client.sendMessage(new Message.messageBuilder<String>().Code(Message.M_FILE_ASK)
							.Sender(self.getAccount()).Receiver(target.getAccount()).Payload(file.getName()).build());
					prepareFile(file, false);
				}
			}
		});

		JButton btnNewButton = new JButton("插入图片");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// JFileChooser chooser = new ImageChooser();
				// chooser.showDialog(null, "选择");
				// File file = chooser.getSelectedFile();
				// if (file != null) {
				// try {
				//// BufferedImage image = ImageIO.read(file);
				//// // TypeAdapter
				//// client.sendMessage(new
				// Message.messageBuilder<String>().Code(Message.M_FILE_IMAGE)
				//// .Sender(self.getAccount()).Receiver(target.getAccount())
				//// .Payload(Translater.fromBufferedImageToBase64(image,
				//// file.getName().substring(file.getName().lastIndexOf(".") +
				// 1).toUpperCase()))
				//// .build());
				// } catch (IOException e1) {
				// e1.printStackTrace();
				// }
				// }
			}
		});
		toolBar_1.add(btnNewButton);
		toolBar_1.add(buttonSendFile);

		textAreaInput = new JTextArea();
		panelInput.setViewportView(textAreaInput);
		textAreaInput.setLineWrap(true);
		contentPane.setLayout(gl_contentPane);
	}

	public void insertMessage(Message<?> message) {
		String sender = message.getSender().startsWith(ConnectionHelper.PUBLIC)
				? message.getSender().replaceAll(ConnectionHelper.PUBLIC, "")
				: message.getSender();
		String text = (String) message.getPayload();
		Timestamp timestamp = message.getTimestamp();

		JLabel labelSender = new JLabel("<html><h3 align=\"right\">" + sender + "  " + timestamp + "</h3></html>");
		String htmlText = "<html>";
		for (String line : text.split("\n")) {
			htmlText += "<h2>" + line + "<br></h2>";
		}
		htmlText += "</html>";
		JLabel labelText = new JLabel(htmlText);
		if (sender.equalsIgnoreCase(self.getAccount())) {
			labelSender.setHorizontalAlignment(JLabel.RIGHT);
			labelText.setHorizontalAlignment(JLabel.RIGHT);
		}

		labelSender.setBorder(new LineBorder(new Color(0, 0, 0)));
		labelText.setBorder(new LineBorder(new Color(0, 0, 0)));

		panelHisMessage.add(labelSender);
		panelHisMessage.add(labelText);
		panelHisMessage.revalidate();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				panelDisplay.getVerticalScrollBar().setValue(panelDisplay.getVerticalScrollBar().getMaximum());
			}
		});
		// panelDisplay.revalidate();
	}

	public FileUI fileUI() {
		return fileUI;
	}

	public void prepareFile(File file, boolean isReceiver) {
		fileUI.prepareFile(file, isReceiver);
	}

	public void prepareFile(String fileName, boolean isReceiver) {
		fileUI.prepareFile(fileName, isReceiver);
	}

	public User getTarget() {
		return target;
	}

	public void rejected() {
		fileUI.setVisible(false);
	}

	public FileUIProgressBar getProgressBar() {
		return progressBar;
	}
}
