package Client;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.InetAddress;
import java.security.DigestInputStream;
import java.security.DigestOutputStream;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.util.Arrays;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JToolBar;
import javax.xml.bind.DatatypeConverter;

import com.Cryptography;
import com.Message;

public class FileUI extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private File file;
	private static final int BUFSIZE = 8192;
	private Client client;
	private DialogUI dialogUI;

	private JLabel labelFileName = new JLabel();
	private JButton buttonAccept;
	private JButton buttonReject;
	private JButton buttonClose;
	private JButton buttonOpenFile;

	public FileUI(Client client, DialogUI dialogUI) {
		this.client = client;
		this.dialogUI = dialogUI;
		setFloatable(false);
		setVisible(false);
		setBounds(100, 100, 743, 39);

		labelFileName = new JLabel("New label");
		add(labelFileName);

		buttonAccept = new JButton("�����ļ� ");
		buttonAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recvFile();
			}
		});
		add(buttonAccept);

		buttonReject = new JButton("�ܾ�����");
		buttonReject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.sendMessage(new Message.messageBuilder<Integer>().Code(Message.M_FILE_REJECT)
						.Sender(client.getSelf().getAccount()).Receiver(dialogUI.getTarget().getAccount()).build());
				rejected();
			}
		});
		add(buttonReject);

		buttonClose = new JButton("�رձ�����");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dialogUI.getProgressBar().setVisible(false);
			}
		});
		
		buttonOpenFile = new JButton("���ļ���");
		buttonOpenFile.setVisible(false);
		buttonOpenFile.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(new File("./Downloads"));
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		add(buttonOpenFile);
		add(buttonClose);
	}

	public void rejected() {
		this.file = null;
		dialogUI.getProgressBar().setVisible(false);
	}

	public void prepareFile(File file, boolean isReceiver) {
		this.file = file;
		prepareFile(file.getName(), isReceiver);
	}

	public void prepareFile(String fileName, boolean isReceiver) {
		setTwoVisible(true);

		labelFileName.setText(fileName);

		buttonAccept.setVisible(isReceiver);
		buttonReject.setVisible(isReceiver);
	}

	public void sendFile(InetAddress address, int port) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SSLSocket fileSocket = client.getConnectionHelper().prepareSSLAsSender(address, port);

				try (DataOutputStream out = new DataOutputStream(
						new BufferedOutputStream(fileSocket.getOutputStream()));
						DataInputStream din = new DataInputStream(new BufferedInputStream(new FileInputStream(file)));
						DigestInputStream in = new DigestInputStream(din, MessageDigest.getInstance("SHA-256"));
						BufferedReader br = new BufferedReader(new InputStreamReader(fileSocket.getInputStream()));) {

					// ��ȡ�ͻ���˽Կ
					PrivateKey privateKey = client.getConnectionHelper().getPrivateKey();
					// ��ȡ��������Կ
					PublicKey publicKey = client.getConnectionHelper().getPublicKey();

					long fileLen = file.length(); // �����ļ�����
					// 1.�����ļ����ơ��ļ�����
					out.writeUTF(file.getName());
					out.writeLong(fileLen);
					out.flush();
					System.out.println("1.�����ļ����ơ��ļ����ȳɹ�");
					// 2.�����ļ�����
					int numRead = 0; // ���ζ�ȡ���ֽ���
					int numFinished = 0; // ������ֽ���
					byte[] buffer = new byte[BUFSIZE];
					while (numFinished < fileLen && (numRead = in.read(buffer)) != -1) { // �ļ��ɶ�
						out.write(buffer, 0, numRead); // ����
						out.flush();
						numFinished += numRead; // ������ֽ���
						// Thread.sleep(200); // ��ʾ�ļ����������
						// publish(numFinished + "/" + fileLen + "bytes");
						// setProgress(numFinished * 100 / (int) fileLen);
						dialogUI.getProgressBar().setProgressValue(numFinished * 100 / (int) fileLen);
					} // end while
					dialogUI.getProgressBar().succeed();
					System.out.println("2.�����ļ����ݳɹ�");
					byte[] fileDigest = in.getMessageDigest().digest(); // �����ļ�ժҪ
					System.out.println("���ɵ�ժҪ��" + DatatypeConverter.printHexBinary(fileDigest) + "\n\n");
					// ��˽Կ��ժҪ���ܣ��γ��ļ�������ǩ��
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // ������
					cipher.init(Cipher.ENCRYPT_MODE, privateKey);// �ø���˽Կ��ʼ������ģʽ
					byte[] signature = cipher.doFinal(fileDigest);// ��������ǩ��

					// ������ʾ
					System.out.println("���ɵ�����ǩ����" + DatatypeConverter.printHexBinary(signature) + "\n\n");

					// ����AES�Գ���Կ
					SecretKey secretKey = Cryptography.generateNewKey();
					System.out.println("���ɵ���Կ��" + DatatypeConverter.printHexBinary(secretKey.getEncoded()) + "\n\n");

					// ������ǩ������
					Cipher cipher2 = Cipher.getInstance("AES");
					cipher2.init(Cipher.ENCRYPT_MODE, secretKey);// ��ʼ��������
					byte[] encryptSign = cipher2.doFinal(signature);// ���ɼ���ǩ��
					System.out.println("����Կ���ܺ������ǩ����" + DatatypeConverter.printHexBinary(encryptSign) + "\n\n");
					// ����Կ����
					cipher.init(Cipher.ENCRYPT_MODE, publicKey);// �÷�������Կ��ʼ������ģʽ
					byte[] encryptKey = cipher.doFinal(secretKey.getEncoded());// ������Կ
					System.out.println("����Կ���ܣ�" + DatatypeConverter.printHexBinary(encryptKey) + "\n\n");

					// 3.���ͼ��ܺ������ǩ��
					out.writeInt(encryptSign.length);
					out.flush();
					out.write(encryptSign);
					out.flush();
					System.out.println("3.���ͼ��ܵ�����ǩ���ɹ���\n");

					// 4.���ͼ�����Կ
					out.write(encryptKey);// ���ĳ���Ϊ128�ֽ�
					out.flush();
					System.out.println("4.���ͼ��ܵ���Կ�ɹ���\n");

					// 5.���շ�����������Ϣ
					String response = br.readLine();// ��ȡ���ش�
					if (response.equalsIgnoreCase("M_DONE")) { // �������ɹ�����
						System.out.println("5." + file.getName() + "  �ɹ����գ�\n");
					} else if (response.equalsIgnoreCase("M_LOST")) { // ����������ʧ��
						System.out.println("5." + file.getName() + "  ����ʧ�ܣ�\n");
					} // end if
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				} catch (NoSuchPaddingException e) {
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				}

			}
		}).start();
	}

	public void recvFile() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				SSLServerSocket serverSocket = client.getConnectionHelper().prepareSSLAsRecver();

				client.sendMessage(new Message.messageBuilder<Integer>().Code(Message.M_FILE_READY)
						.Sender(client.getSelf().getAccount()).Receiver(dialogUI.getTarget().getAccount())
						.Payload(serverSocket.getLocalPort()).build());
				SSLSocket fileSocket = null;
				try {
					fileSocket = (SSLSocket) serverSocket.accept();
				} catch (IOException e1) {
					e1.printStackTrace();
				}
				File file = new File(
						"./Downloads/" + dialogUI.getTarget().getAccount() + "--" + labelFileName.getText());
				if (!file.exists()) {
					try {
						file.createNewFile();
					} catch (IOException e) {
						e.printStackTrace();
					}
				}
				try (DataInputStream in = new DataInputStream(new BufferedInputStream(fileSocket.getInputStream()));
						BufferedOutputStream fout = new BufferedOutputStream(new FileOutputStream(file));
						DigestOutputStream out = new DigestOutputStream(fout, MessageDigest.getInstance("SHA-256"));) {
					// ��ȡ������˽Կ
					PrivateKey privateKey = client.getConnectionHelper().getPrivateKey();
					// ��ȡ�ͻ�����Կ
					PublicKey publicKey = client.getConnectionHelper().getPublicKey();
					// ��ȡ�׽���������
					// 1.�����ļ������ļ�����
					String filename = in.readUTF(); // �ļ���
					int fileLen = (int) in.readLong(); // �ļ�����
					System.out.println("1.�յ��ļ�����" + filename + "�ļ����ȣ�" + fileLen + "�ֽ�\n\n");
					// �����ļ������
					// �ļ������
					// 2.�����ļ����ݣ��洢Ϊ�ⲿ�ļ�
					byte[] buffer = new byte[BUFSIZE]; // ���뻺����
					int numRead = 0; // ���ζ�ȡ���ֽ���
					int numFinished = 0;// ������ֽ���
					while (numFinished < fileLen && (numRead = in.read(buffer)) != -1) { // �������ɶ�
						out.write(buffer, 0, numRead);
						numFinished += numRead; // ������ֽ���
						dialogUI.getProgressBar().setProgressValue(numFinished * 100 / (int) fileLen);
					} // end while
					System.out.println("2.�����ļ����ݽ�����\n\n");
					fileSucceed();
					// 3.���ռ��ܵ�����ǩ��
					int size = in.readInt();
					byte[] signature = new byte[size];
					in.read(signature);
					System.out.println("3.�յ����ܵ�����ǩ����" + DatatypeConverter.printHexBinary(signature) + "\n\n");

					// 4.���ռ��ܵ���Կ
					byte[] encryptKey = new byte[128];
					in.read(encryptKey);
					System.out.println("4.�յ����ܵ���Կ��" + DatatypeConverter.printHexBinary(encryptKey) + "\n\n");

					// �÷�����˽Կ������Կ
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// ������
					cipher.init(Cipher.DECRYPT_MODE, privateKey); // �÷�����˽Կ��ʼ��������
					byte[] decryptKey = cipher.doFinal(encryptKey);// ������Կ
					System.out.println("��Կ���ܣ�" + DatatypeConverter.printHexBinary(decryptKey) + "\n\n");

					// ����Կ��������ǩ��
					SecretKey secretKey = new SecretKeySpec(decryptKey, "AES");
					Cipher cipher2 = Cipher.getInstance("AES");// ������
					cipher2.init(Cipher.DECRYPT_MODE, secretKey);
					byte[] decryptSign = cipher2.doFinal(signature);// ��������ǩ��
					System.out.println("ǩ�����ܣ�" + DatatypeConverter.printHexBinary(decryptSign) + "\n\n");

					// "SHA-256"�㷨�����ժҪΪ256λ����32�ֽ�
					byte[] sourceDigest = new byte[32]; // �յ���ժҪ
					cipher.init(Cipher.DECRYPT_MODE, publicKey); // �ÿͻ�����Կ��ʼ��������
					sourceDigest = cipher.doFinal(decryptSign); // ��ԭ��ϢժҪ
					System.out.println("ȥ��ǩ�����ժҪ��" + DatatypeConverter.printHexBinary(sourceDigest) + "\n\n");
					// ������ʾ

					// 5.�����ļ���������¼�����ϢժҪ
					byte[] computedDigest = new byte[32];// ���¼����ժҪ
					computedDigest = out.getMessageDigest().digest();
					// ��������ʾ��Ϣ
					System.out
							.println("�����������յ����ļ����¼����ժҪ��" + DatatypeConverter.printHexBinary(computedDigest) + "\n\n");

					// �����ַ������
					PrintWriter pw = new PrintWriter(fileSocket.getOutputStream(), true);
					// �Ƚ����¼����ժҪ���յ���ժҪ�Ƿ���ͬ
					if (Arrays.equals(sourceDigest, computedDigest)) {// ��֤����ǩ��
						pw.println("M_DONE"); // ���ͳɹ���Ϣ
						System.out.println("5." + filename + "  ���ճɹ���\n\n");
					} else {
						pw.println("M_LOST"); // ����ʧ����Ϣ
						System.out.println("5." + filename + "  ����ʧ�ܣ�\n\n");
					} // end if
						// �ر���
				} catch (IOException e) {
					e.printStackTrace();
				} catch (NoSuchAlgorithmException e1) {
					e1.printStackTrace();
				} catch (NoSuchPaddingException e) {
					e.printStackTrace();
				} catch (InvalidKeyException e) {
					e.printStackTrace();
				} catch (IllegalBlockSizeException e) {
					e.printStackTrace();
				} catch (BadPaddingException e) {
					e.printStackTrace();
				}
				try {
					fileSocket.close();
					serverSocket.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
	}

	public void setTwoVisible(boolean isVisable) {
		setVisible(isVisable);
		dialogUI.getProgressBar().setVisible(isVisable);
	}

	public String getFileName() {
		return file.getName();
	}
	
	private void fileSucceed() {
		dialogUI.getProgressBar().succeed();	
		buttonAccept.setVisible(false);
		buttonReject.setVisible(false);
		buttonOpenFile.setVisible(true);
	}
}
