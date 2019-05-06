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

		buttonAccept = new JButton("接收文件 ");
		buttonAccept.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				recvFile();
			}
		});
		add(buttonAccept);

		buttonReject = new JButton("拒绝接收");
		buttonReject.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				client.sendMessage(new Message.messageBuilder<Integer>().Code(Message.M_FILE_REJECT)
						.Sender(client.getSelf().getAccount()).Receiver(dialogUI.getTarget().getAccount()).build());
				rejected();
			}
		});
		add(buttonReject);

		buttonClose = new JButton("关闭本窗口");
		buttonClose.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				setVisible(false);
				dialogUI.getProgressBar().setVisible(false);
			}
		});
		
		buttonOpenFile = new JButton("打开文件夹");
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

					// 获取客户机私钥
					PrivateKey privateKey = client.getConnectionHelper().getPrivateKey();
					// 获取服务器公钥
					PublicKey publicKey = client.getConnectionHelper().getPublicKey();

					long fileLen = file.length(); // 计算文件长度
					// 1.发送文件名称、文件长度
					out.writeUTF(file.getName());
					out.writeLong(fileLen);
					out.flush();
					System.out.println("1.发送文件名称、文件长度成功");
					// 2.传送文件内容
					int numRead = 0; // 单次读取的字节数
					int numFinished = 0; // 总完成字节数
					byte[] buffer = new byte[BUFSIZE];
					while (numFinished < fileLen && (numRead = in.read(buffer)) != -1) { // 文件可读
						out.write(buffer, 0, numRead); // 发送
						out.flush();
						numFinished += numRead; // 已完成字节数
						// Thread.sleep(200); // 演示文件传输进度用
						// publish(numFinished + "/" + fileLen + "bytes");
						// setProgress(numFinished * 100 / (int) fileLen);
						dialogUI.getProgressBar().setProgressValue(numFinished * 100 / (int) fileLen);
					} // end while
					dialogUI.getProgressBar().succeed();
					System.out.println("2.传送文件内容成功");
					byte[] fileDigest = in.getMessageDigest().digest(); // 生成文件摘要
					System.out.println("生成的摘要：" + DatatypeConverter.printHexBinary(fileDigest) + "\n\n");
					// 用私钥对摘要加密，形成文件的数字签名
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding"); // 加密器
					cipher.init(Cipher.ENCRYPT_MODE, privateKey);// 用个人私钥初始化加密模式
					byte[] signature = cipher.doFinal(fileDigest);// 计算数字签名

					// 更新显示
					System.out.println("生成的数字签名：" + DatatypeConverter.printHexBinary(signature) + "\n\n");

					// 生成AES对称密钥
					SecretKey secretKey = Cryptography.generateNewKey();
					System.out.println("生成的密钥：" + DatatypeConverter.printHexBinary(secretKey.getEncoded()) + "\n\n");

					// 对数字签名加密
					Cipher cipher2 = Cipher.getInstance("AES");
					cipher2.init(Cipher.ENCRYPT_MODE, secretKey);// 初始化加密器
					byte[] encryptSign = cipher2.doFinal(signature);// 生成加密签名
					System.out.println("用密钥加密后的数字签名：" + DatatypeConverter.printHexBinary(encryptSign) + "\n\n");
					// 对密钥加密
					cipher.init(Cipher.ENCRYPT_MODE, publicKey);// 用服务器公钥初始化加密模式
					byte[] encryptKey = cipher.doFinal(secretKey.getEncoded());// 加密密钥
					System.out.println("对密钥加密：" + DatatypeConverter.printHexBinary(encryptKey) + "\n\n");

					// 3.发送加密后的数字签名
					out.writeInt(encryptSign.length);
					out.flush();
					out.write(encryptSign);
					out.flush();
					System.out.println("3.发送加密的数字签名成功！\n");

					// 4.发送加密密钥
					out.write(encryptKey);// 密文长度为128字节
					out.flush();
					System.out.println("4.发送加密的密钥成功！\n");

					// 5.接收服务器反馈信息
					String response = br.readLine();// 读取返回串
					if (response.equalsIgnoreCase("M_DONE")) { // 服务器成功接收
						System.out.println("5." + file.getName() + "  成功接收！\n");
					} else if (response.equalsIgnoreCase("M_LOST")) { // 服务器接收失败
						System.out.println("5." + file.getName() + "  接收失败！\n");
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
					// 获取服务器私钥
					PrivateKey privateKey = client.getConnectionHelper().getPrivateKey();
					// 获取客户机公钥
					PublicKey publicKey = client.getConnectionHelper().getPublicKey();
					// 获取套接字输入流
					// 1.接收文件名、文件长度
					String filename = in.readUTF(); // 文件名
					int fileLen = (int) in.readLong(); // 文件长度
					System.out.println("1.收到文件名：" + filename + "文件长度：" + fileLen + "字节\n\n");
					// 创建文件输出流
					// 文件输出流
					// 2.接收文件内容，存储为外部文件
					byte[] buffer = new byte[BUFSIZE]; // 读入缓冲区
					int numRead = 0; // 单次读取的字节数
					int numFinished = 0;// 总完成字节数
					while (numFinished < fileLen && (numRead = in.read(buffer)) != -1) { // 输入流可读
						out.write(buffer, 0, numRead);
						numFinished += numRead; // 已完成字节数
						dialogUI.getProgressBar().setProgressValue(numFinished * 100 / (int) fileLen);
					} // end while
					System.out.println("2.接收文件内容结束！\n\n");
					fileSucceed();
					// 3.接收加密的数字签名
					int size = in.readInt();
					byte[] signature = new byte[size];
					in.read(signature);
					System.out.println("3.收到加密的数字签名：" + DatatypeConverter.printHexBinary(signature) + "\n\n");

					// 4.接收加密的密钥
					byte[] encryptKey = new byte[128];
					in.read(encryptKey);
					System.out.println("4.收到加密的密钥：" + DatatypeConverter.printHexBinary(encryptKey) + "\n\n");

					// 用服务器私钥解密密钥
					Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");// 解密器
					cipher.init(Cipher.DECRYPT_MODE, privateKey); // 用服务器私钥初始化解密器
					byte[] decryptKey = cipher.doFinal(encryptKey);// 解密密钥
					System.out.println("密钥解密：" + DatatypeConverter.printHexBinary(decryptKey) + "\n\n");

					// 用密钥解密数字签名
					SecretKey secretKey = new SecretKeySpec(decryptKey, "AES");
					Cipher cipher2 = Cipher.getInstance("AES");// 解密器
					cipher2.init(Cipher.DECRYPT_MODE, secretKey);
					byte[] decryptSign = cipher2.doFinal(signature);// 解密数字签名
					System.out.println("签名解密：" + DatatypeConverter.printHexBinary(decryptSign) + "\n\n");

					// "SHA-256"算法计算的摘要为256位，合32字节
					byte[] sourceDigest = new byte[32]; // 收到的摘要
					cipher.init(Cipher.DECRYPT_MODE, publicKey); // 用客户机公钥初始化解密器
					sourceDigest = cipher.doFinal(decryptSign); // 还原消息摘要
					System.out.println("去掉签名后的摘要：" + DatatypeConverter.printHexBinary(sourceDigest) + "\n\n");
					// 更新显示

					// 5.根据文件输出流重新计算消息摘要
					byte[] computedDigest = new byte[32];// 重新计算的摘要
					computedDigest = out.getMessageDigest().digest();
					// 输出相关提示信息
					System.out
							.println("服务器根据收到的文件重新计算的摘要：" + DatatypeConverter.printHexBinary(computedDigest) + "\n\n");

					// 定义字符输出流
					PrintWriter pw = new PrintWriter(fileSocket.getOutputStream(), true);
					// 比较重新计算的摘要与收到的摘要是否相同
					if (Arrays.equals(sourceDigest, computedDigest)) {// 验证数字签名
						pw.println("M_DONE"); // 回送成功消息
						System.out.println("5." + filename + "  接收成功！\n\n");
					} else {
						pw.println("M_LOST"); // 回送失败消息
						System.out.println("5." + filename + "  接收失败！\n\n");
					} // end if
						// 关闭流
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
