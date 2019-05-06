package com;

import java.awt.image.BufferedImage;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import Client.Client;
import Server.ServerUI;

public class ConnectionHelper {
	private List<String> targetList;
	private Map<String, InetSocketAddress> targetAddr;

	private int portSendUDP;
	private int portRecvUDP;
	private int portSSL;
	private int portOutTCP;
	private int portInTCP;

	private DatagramSocket datagramSocketSend;
	private DatagramSocket datagramSocketRecv;
	private SSLServerSocket sslServerSocket;
	private SSLSocket sslSocket;

	private PrivateKey privateKey;
	private PublicKey publicKey;

	public static final int SERVER_PORT_Send = 50001;
	public static final int SERVER_PORT_Recv = 50002;
	public static final int SERVER_PORT_SSL = 50003;
	public static final int SERVER_PORT_Out = 50004;
	public static final int SERVER_PORT_In = 50005;
	private static final int portNum = 5;
	public static final String SERVER = "Server";
	public static final String PUBLIC = "Public";

	public ConnectionHelper(boolean isServer) throws SocketException {
		this();
		int[] ports = findValidPorts();
		portSendUDP = isServer ? SERVER_PORT_Send : ports[0];
		portRecvUDP = isServer ? SERVER_PORT_Recv : ports[1];
		portSSL = isServer ? SERVER_PORT_SSL : ports[2];
		portOutTCP = isServer ? SERVER_PORT_Out : ports[3];
		portInTCP = isServer ? SERVER_PORT_In : ports[4];
		datagramSocketSend = new DatagramSocket(portSendUDP);
		datagramSocketRecv = new DatagramSocket(portRecvUDP);
	}

	public int[] findValidPorts() {
		int p[] = { SERVER_PORT_Send, SERVER_PORT_Recv, SERVER_PORT_SSL, SERVER_PORT_Out, SERVER_PORT_In };

		int round = 0;

		while (true) {
			round += portNum;
			for (int i = 0; i < portNum; i++) {
				p[i] += round;
			}
			try (DatagramSocket socket = new DatagramSocket(p[0]);) {
			} catch (SocketException e) {
				continue;
			}
			break;
		}

		return p;
	}

	public SSLSocket prepareSSLAsSender(InetAddress remoteAddr, int remotePort) {
		try (InputStream key = ConnectionHelper.class
				.getResourceAsStream("/com/KeyStore/FileSenderKeyStore/client.keystore");
				InputStream tkey = ConnectionHelper.class
						.getResourceAsStream("/com/KeyStore/FileSenderKeyStore/tclient.keystore");) {
			String CLIENT_KEY_STORE_PASSWORD = "123456"; // client.keystore私钥库密码
			String CLIENT_TRUST_KEY_STORE_PASSWORD = "123456";// tclient.keystore公钥库密码
			SSLContext ctx = SSLContext.getInstance("SSL"); // SSL上下文
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509"); // 私钥管理器
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");// 公钥管理器
			KeyStore ks = KeyStore.getInstance("JKS");// 私钥库对象
			KeyStore tks = KeyStore.getInstance("JKS");// 公钥库对象
			ks.load(key, CLIENT_KEY_STORE_PASSWORD.toCharArray());// 加载私钥库
			tks.load(tkey, CLIENT_TRUST_KEY_STORE_PASSWORD.toCharArray());// 加载公钥库
			kmf.init(ks, CLIENT_KEY_STORE_PASSWORD.toCharArray());// 私钥库访问初始化
			tmf.init(tks);// 公钥库访问初始化
			// 用私钥库和公钥库初始化SSL上下文
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			// 获取客户机私钥
			privateKey = (PrivateKey) ks.getKey("client", CLIENT_KEY_STORE_PASSWORD.toCharArray());
			// 获取服务器公钥
			publicKey = (PublicKey) tks.getCertificate("server").getPublicKey();

			// 用SSLSocket连接服务器
			sslSocket = (SSLSocket) ctx.getSocketFactory().createSocket(remoteAddr, remotePort);
			return sslSocket;
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public SSLServerSocket prepareSSLAsRecver() {
		try (InputStream key = ConnectionHelper.class
				.getResourceAsStream("/com/KeyStore/FileReceiverKeyStore/server.keystore");
				InputStream tkey = ConnectionHelper.class
						.getResourceAsStream("/com/KeyStore/FileReceiverKeyStore/tserver.keystore");) {
			String SERVER_KEY_STORE_PASSWORD = "123456"; // server.keystore密码
			String SERVER_TRUST_KEY_STORE_PASSWORD = "123456";// tserver.keystore密码

			SSLContext ctx = SSLContext.getInstance("SSL");// SSL上下文
			KeyManagerFactory kmf = KeyManagerFactory.getInstance("SunX509");
			TrustManagerFactory tmf = TrustManagerFactory.getInstance("SunX509");
			KeyStore ks = KeyStore.getInstance("JKS");
			KeyStore tks = KeyStore.getInstance("JKS");
			// 加载私钥证书库
			ks.load(key, SERVER_KEY_STORE_PASSWORD.toCharArray());
			// 加载公钥证书库
			tks.load(tkey, SERVER_TRUST_KEY_STORE_PASSWORD.toCharArray());
			kmf.init(ks, SERVER_KEY_STORE_PASSWORD.toCharArray());
			tmf.init(tks);
			ctx.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

			// 获取服务器私钥
			privateKey = (PrivateKey) ks.getKey("server", SERVER_KEY_STORE_PASSWORD.toCharArray());
			// 获取客户机公钥
			publicKey = (PublicKey) tks.getCertificate("client").getPublicKey();

			sslServerSocket = (SSLServerSocket) ctx.getServerSocketFactory().createServerSocket(portSSL);
			return sslServerSocket;
		} catch (UnrecoverableKeyException e) {
			e.printStackTrace();
		} catch (KeyManagementException e) {
			e.printStackTrace();
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		} catch (KeyStoreException e) {
			e.printStackTrace();
		} catch (CertificateException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public ConnectionHelper() {
		targetList = new ArrayList<>();
		targetAddr = new HashMap<>();
	}

	public void addConnection(InetSocketAddress targetAddr, String target) {
		this.targetAddr.put(target, targetAddr);
		this.targetList.add(target);
	}

	public void removeConnection(String target) {
		targetAddr.remove(target);
		targetList.remove(target);
	}

	public boolean containsConnection(String target) {
		return targetList.contains(target);
	}

	public InetSocketAddress getConnectionAddress(String target) {
		return new InetSocketAddress(targetAddr.get(target).getAddress(), targetAddr.get(target).getPort() - 1);
	}

	public void switchConnectionTarget(String oldTarget, String newTarget) {
		this.targetAddr.put(newTarget, this.targetAddr.remove(oldTarget));
		this.targetList.remove(oldTarget);
		this.targetList.add(newTarget);
	}

	// 向目标主机发送指定message
	private boolean sendMessage(Message<?> message, boolean isServer) {

		byte[] data = Translater.fromMessageToJson(message).getBytes();
		DatagramPacket packet = new DatagramPacket(data, data.length,
				targetAddr.get(isServer ? message.getReceiver() : SERVER));
		try {
			System.out.println("发送message给：" + message.getReceiver() + packet.getPort() + "\n" + message);
			datagramSocketSend.send(packet);
		} catch (IOException e) {
			e.printStackTrace();
		}
		return true;
	}

	public int getPortOutTCP() {
		return portOutTCP;
	}

	public int getPortInTCP() {
		return portInTCP;
	}

	// 群发
	public void sendMessageToAllConnections(Message<?> message) {
		/**
		 * 不要Receiver
		 */
		for (String target : targetList) {
			if (target.startsWith("TEMP"))
				return;
			sendMessage(message.setReceiver(target), true);
		}
	}

	public void sendMessageToServer(Message<?> message) {
		sendMessage(message, false);
	}

	public void sendMessageAsServer(Message<?> message) {
		sendMessage(message, true);
	}

	// 接收消息
	public Message<?> waitMessage(int soTime, boolean autoAddConnetion) {
		try {
			datagramSocketRecv.setSoTimeout(soTime);

			byte[] data = new byte[8192];
			DatagramPacket packet = new DatagramPacket(data, data.length);
			datagramSocketRecv.receive(packet);

			Message<?> message = Translater.fromJsonToMessage(new String(data).trim());
			System.out.println("收到message\n" + message);
			if (autoAddConnetion && message != null && !check(message.getSender())) {
				addConnection(
						new InetSocketAddress(packet.getAddress(), getUdpRecvPortFromUdpSendPort(packet.getPort())),
						message.getSender());
			}
			return message;
		} catch (SocketException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return null;
	}

	public Message<?> waitMessageForever() {
		return waitMessage(0, true);
	}

	public Message<?> waitMessage(int soTime) {
		return waitMessage(soTime, false);
	}

	public Message<?> waitMessage() {
		return waitMessage(0, false);
	}

	private boolean check(String target) {
		return targetList.contains(target);
	}

	public PrivateKey getPrivateKey() {
		return privateKey;
	}

	public PublicKey getPublicKey() {
		return publicKey;
	}

	public void shut() {
	}

	public void updateImage(Message<?> message, File image, String userAccount) {
		System.out.println("我要 换头像");
		sendImage(message, false, image, userAccount);
	}

	public void sendImage(Message<?> message) {
		sendImage(message, true, null, null);
	}

	private void sendImage(Message<?> message, boolean isServer, File imagee,  String self) {
		@SuppressWarnings("unchecked")
		List<String> imageList = (List<String>) message.getPayload();

		InetSocketAddress remoteAddr = getConnectionAddress(message.getSender());
		for (String userAccount : imageList) {
			try (Socket socket = new Socket(remoteAddr.getAddress(),isServer?
					getTcpRecvPortFromUdpRecvPort(getUdpRecvPortFromUdpSendPort(remoteAddr.getPort())):
					getTcpSendPortFromUdpRecvPort(getUdpRecvPortFromUdpSendPort(remoteAddr.getPort())));) {

				BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
				FileOutputStream fileOut = null;
				
				File image  = new File("./src/Server/HeadImages/" + userAccount + ".jpg");
				if (!image.isFile() ) {
					if(imagee == null)
						image = new File("./src/Server/HeadImages/Default.jpg");
					else
						image = new File("./src/"+(isServer?"Server":"Client")+"/HeadImages/"+self+".jpg");
					fileOut = new FileOutputStream(image);
				}
			BufferedInputStream in;
			if(imagee == null)
				in = new BufferedInputStream(new FileInputStream(image));
			else {
				in = new BufferedInputStream(new FileInputStream(imagee));
			}

				byte[] buffer = new byte[1024];
				int n = 0;
				while ((n = in.read(buffer)) != -1) {
					out.write(buffer, 0, n);
					if(fileOut != null)
						fileOut.write(buffer, 0, n);
				}
				if(fileOut != null)
					fileOut.close();
				in.close();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

	}

	public void waitImageUpdate(List<String> imageList, Object user, String sender) {
		System.out.println("等客户端 发图片");
		waitImage(imageList, user, false, sender);
	}

	public void waitImage(List<String> imageList, Object user) {
		waitImage(imageList, user, true, null);
	}

	private void waitImage(List<String> imageList, Object user, boolean isClient, String sender) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try (ServerSocket serverSocket = new ServerSocket(isClient?getPortInTCP():getPortOutTCP());) {
					for (String userAccount : imageList) {
						Socket socket = serverSocket.accept();
						System.out.println("获取连接");

						BufferedInputStream in = new BufferedInputStream(socket.getInputStream());

						File image = new File(
								"./src/" + (isClient ? "Client" : "Server") + "/HeadImages/" + userAccount + ".jpg");
						FileOutputStream out = new FileOutputStream(image);

						byte[] buffer = new byte[1024];
						int n = 0;
						while ((n = in.read(buffer)) != -1) {
							out.write(buffer, 0, n);
						}
						out.close();

						System.out.println("写入本地" + (isClient ? "Client" : "Server") + userAccount);

						socket.close();
					}
					System.out.println("dialog END");
					if (user.getClass() == Client.class && isClient)
						((Client) user).updateHeadImages();
					if (user.getClass() == ServerUI.class && !isClient)
						((ServerUI) user).sendUpdateSucceed(sender);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();
		// sendMessageToServer(message);(
		// new
		// Message.messageBuilder().Code(Message.M_IMAGE_REQUEST).Sender(userAccount).build());
	}

	private int getTcpRecvPortFromUdpRecvPort(int p) {
		return p + 3;
	}
	
	private int getTcpSendPortFromUdpRecvPort(int p) {
		return p + 2;
	}

	private int getUdpRecvPortFromUdpSendPort(int p) {
		return p + 1;
	}

	// public void sendImageUpdate(File image) {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// try (ServerSocket serverSocket = new ServerSocket(getPortInTCP());) {
	// Socket socket = serverSocket.accept();
	// System.out.println("获取连接");
	//
	// BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
	//
	// FileOutputStream out = new FileOutputStream(image);
	//
	// byte[] buffer = new byte[1024];
	// int n = 0;
	// while ((n = in.read(buffer)) != -1) {
	// out.write(buffer, 0, n);
	// }
	// out.close();
	//
	// socket.close();
	// System.out.println("update END");
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }).start();
	// }
	//
	// public void waitImageUpdate(String sender, ServerUI serverUI) {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// try (ServerSocket serverSocket = new ServerSocket(getPortInTCP());) {
	// Socket socket = serverSocket.accept();
	// System.out.println("获取连接");
	//
	// BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
	//
	// File image = new File("./src/Client/HeadImages/" + sender + ".jpg");
	// FileOutputStream out = new FileOutputStream(image);
	//
	// byte[] buffer = new byte[1024];
	// int n = 0;
	// while ((n = in.read(buffer)) != -1) {
	// out.write(buffer, 0, n);
	// }
	// out.close();
	//
	// System.out.println("get" + sender);
	//
	// socket.close();
	// System.out.println("dialog END");
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }).start();
	//
	// // serverUI.sendImageUpdateSucceed(sender);
	// }
}
