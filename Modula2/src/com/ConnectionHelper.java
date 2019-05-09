package com;

import java.awt.image.BufferedImage;
import java.awt.image.Kernel;
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
import java.nio.Buffer;
import java.nio.ByteBuffer;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
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
import javax.imageio.stream.FileImageInputStream;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLSocket;
import javax.net.ssl.TrustManagerFactory;

import Client.Client;
import Client.ClientUI;
import Server.ServerImage;
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
	private boolean uploadingFlag = false;
	private static final int portNum = 5;
	public static final String SERVER = "Server";
	public static final String PUBLIC = "Public";

	private ServerSocketChannel serverSocketChannel;
	private Selector selector;

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

		if (portInTCP == SERVER_PORT_In) {
			try {
				serverSocketChannel = ServerSocketChannel.open();
				selector = Selector.open();
				serverSocketChannel.configureBlocking(false);
				System.out.println("非阻塞");
				ServerSocket serverSocket = serverSocketChannel.socket();
				serverSocket.setReuseAddress(true);
				serverSocket.bind(new InetSocketAddress(portInTCP));
				serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
				System.out.println("图片服务器启动");
			} catch (ClosedChannelException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
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

	public void waitingImageRequests() {
		new Thread(new Runnable() {

			@Override
			public void run() {
				try {
					while (selector.select() > 0) {
						Set<SelectionKey> selectedKeys = selector.selectedKeys();
						Iterator<SelectionKey> iterator = selectedKeys.iterator();
						while (iterator.hasNext()) {
							SelectionKey key = null;
							try {
								key = (SelectionKey) iterator.next();
								iterator.remove();
								if (key.isAcceptable()) {
									SocketChannel socketChannel = serverSocketChannel.accept();
									socketChannel.configureBlocking(false);
									System.out.println("接收到来自 ：" + socketChannel.getRemoteAddress() + "的连接");
									socketChannel.register(selector, SelectionKey.OP_READ | SelectionKey.OP_WRITE);
								}
								if (key.isReadable()) {
									SocketChannel socketChannel = (SocketChannel) key.channel();
									ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
									socketChannel.read(byteBuffer);
									byteBuffer.flip();
									String imageCtrlStr = Charset.forName("UTF-8").newDecoder().decode(byteBuffer)
											.toString();
									byteBuffer.clear();
									System.out.println(imageCtrlStr);

									if (imageCtrlStr.startsWith("request")) {
										while (uploadingFlag)
											;
										File image = new File("./src/Server/HeadImages/"
												+ imageCtrlStr.replace("request", "") + ".jpg");
										if (!image.isFile() || image.length() == 0) {
											image = new File("./src/Server/HeadImages/Default.jpg");
										}

										System.out.println(image.getName());
										key.attach(new ServerImage(image, true));
									}
									if (imageCtrlStr.startsWith("upload")) {
										String userAccount = imageCtrlStr.replace("upload", "");
										uploadingFlag = true;
										File image = new File("./src/Server/HeadImages/" + userAccount + ".jpg");
										ServerImage serverImage = new ServerImage(image, false);
										FileOutputStream fileOut = new FileOutputStream(serverImage.getFile());
										byte[] buffer = new byte[1024];
										int n = 0;
										while (buffer.length == 1024) {
											socketChannel.read(byteBuffer);
											byteBuffer.flip();
											buffer = new byte[byteBuffer.remaining()];
											byteBuffer.get(buffer, 0, buffer.length);
											fileOut.write(buffer, 0, buffer.length);
											byteBuffer.clear();
										}
										fileOut.close();
										uploadingFlag = false;
										sendMessageToAllConnections(new Message.messageBuilder<String>()
												.Code(Message.M_IMAGE_UPDATE).Payload(userAccount).build());
										// key.cancel();
									}
								}
								if (key.isWritable()) {
									if (key.attachment() == null)
										break;

									ServerImage serverImage = (ServerImage) key.attachment();

									if (serverImage.isReading()) {
										SocketChannel socketChannel = (SocketChannel) key.channel();
										FileInputStream fileIn = new FileInputStream(serverImage.getFile());
										ByteBuffer byteBuffer = ByteBuffer.allocate(1024);
										byte[] buffer = new byte[1024];
										int n = 0;
										socketChannel.write(ByteBuffer.wrap("start!\r\n".getBytes()));
										while ((n = fileIn.read(buffer)) != -1) {
											socketChannel.write(ByteBuffer.wrap(buffer, 0, n));
										}
										socketChannel.write(ByteBuffer.wrap("保险".getBytes()));

										key.cancel();
									}
								}
							} catch (IOException e) {
								e.printStackTrace();
								try {
									if (key != null) {
										key.cancel();
										key.channel().close();
									}
								} catch (IOException e1) {
									e1.printStackTrace();
								}
							}
						}
					}
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}).start();

	}

	// public void imageRequests(List<String> requests, Client caller) {
	// for (String requestImageName : requests) {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// imageRequest(requestImageName);
	// caller.updateHeadImage(requestImageName);
	// }
	// // **************************
	// }).start();
	// }
	// }

	public void imageRequest(String requestImageName, Client caller) {
		try {
			Socket socket = new Socket(getConnectionAddress(SERVER).getAddress(), SERVER_PORT_In);

			BufferedOutputStream fileOut = new BufferedOutputStream(
					new FileOutputStream(new File("./src/Client/HeadImages/" + requestImageName + ".jpg")));

			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
			Scanner scanner = new Scanner(in);

			byte[] buffer = new byte[1024];
			out.write(("request" + requestImageName).getBytes());
			out.flush();
			System.out.println("发了request字符串");

			boolean waiting = true;
			while (waiting) {
				waiting = !scanner.nextLine().startsWith("start!");
			}
			System.out.println("start");
			int n = 1024;
			while (n == 1024) {
				n = in.read(buffer);
				fileOut.write(buffer, 0, n);
			}
			System.out.println("end");
			scanner.close();
			out.close();
			fileOut.close();
			socket.close();
			caller.updateHeadImage(requestImageName);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public void imageUpload(String uploadImageName) {
		try {
			Socket socket = new Socket(getConnectionAddress(SERVER).getAddress(), SERVER_PORT_In);
			System.out.println("upload" + uploadImageName);

			BufferedInputStream fileIn = new BufferedInputStream(
					new FileInputStream(new File("./src/Client/HeadImages/" + uploadImageName + ".jpg")));

			BufferedOutputStream out = new BufferedOutputStream(socket.getOutputStream());
			BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
			Scanner scanner = new Scanner(in);

			System.out.println("发了upload字符串");

			// boolean waiting = true;
			// while (waiting) {
			// waiting = !scanner.nextLine().startsWith("start!");
			// }
			int n = 0;
			byte[] buffer = new byte[1024];
			out.write(("upload" + uploadImageName).getBytes());
			out.flush();
			while ((n = fileIn.read(buffer)) != -1) {
				out.write(buffer, 0, n);
				out.flush();
			}
			scanner.close();
			out.close();
			fileIn.close();
			socket.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	// public void sendImage(Message<?> message) {
	// InetSocketAddress remoteAddr = getConnectionAddress(message.getSender());
	// try {
	// Socket socket = new Socket(remoteAddr.getAddress(),
	// getTcpRecvPortFromUdpRecvPort(getUdpRecvPortFromUdpSendPort(remoteAddr.getPort())));
	// try {
	// for (String userAccount : (List<String>) message.getPayload()) {
	// System.out.println("now server" + userAccount);
	// sendImage(socket, remoteAddr, userAccount);
	// }
	// System.out.println("for loop end");
	// } catch (ClassCastException e) {
	// e.printStackTrace();
	// sendImage(socket, remoteAddr, (String) message.getPayload());
	// }
	// socket.close();
	// } catch (IOException e1) {
	// e1.printStackTrace();
	// }
	// }

	// private void sendImage(Socket socket, InetSocketAddress remoteAddr, String
	// imageName) {
	// System.out.println("写");
	// File image = new File("./src/Server/HeadImages/" + imageName + ".jpg");
	// if (!image.isFile()) {
	// image = new File("./src/Server/HeadImages/Default.jpg");
	// }
	//
	// System.out.println(image.getName());
	// try {
	// BufferedOutputStream out = new
	// BufferedOutputStream(socket.getOutputStream());
	// BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
	// BufferedInputStream fin = new BufferedInputStream(new
	// FileInputStream(image));
	//
	// out.write(7);
	// int b=-1;
	// while ((b=in.read()) != 8)
	// System.out.println(b);
	// ;
	//
	// while (uploadingFlag)
	// ;
	//
	// byte[] buffer = new byte[1024];
	// int n = 0;
	// while ((n = fin.read(buffer)) != -1) {
	// out.write(buffer, 0, n);
	// }
	// in.close();
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	//
	// System.out.println("写完");
	// }

	// public void waitImages(List<String> imageList, Client caller, int num) {
	// new Thread(new Runnable() {
	//
	// @Override
	// public void run() {
	// try (ServerSocket serverSocket = new ServerSocket(getPortInTCP());) {
	// Socket socket = serverSocket.accept();
	// for (String account : imageList) {
	// fetchImage(socket, account);
	// }
	// caller.updateHeadImages();
	// socket.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	// }).start();
	// }
	//
	// public void waitImage(String account, Client caller) {
	// try (ServerSocket serverSocket = new ServerSocket(getPortInTCP());) {
	// Socket socket = serverSocket.accept();
	// fetchImage(socket, account).close();
	// caller.updateHeadImages();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// private BufferedInputStream fetchImage(Socket socket, String imageName) {
	// System.out.println("fetch" + imageName);
	// File image = new File("./src/Client/HeadImages/" + imageName + ".jpg");
	//
	// try {
	// FileOutputStream fout = new FileOutputStream(image);
	// BufferedOutputStream out = new
	// BufferedOutputStream(socket.getOutputStream());
	// BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
	//
	// int b = -1;
	// while ((b = in.read() )!= 7)
	// System.out.println(b);
	// ;
	// out.write(8);
	//
	// byte[] buffer = new byte[1024];
	// int n = 0;
	// while ((n = in.read(buffer)) != -1) {
	// fout.write(buffer, 0, n);
	// }
	// fout.close();
	//
	// return in;
	// } catch (FileNotFoundException e) {
	// e.printStackTrace();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// return null;
	// }

	private int getTcpRecvPortFromUdpRecvPort(int p) {
		return p + 3;
	}

	private int getTcpSendPortFromUdpRecvPort(int p) {
		return p + 2;
	}

	private int getUdpRecvPortFromUdpSendPort(int p) {
		return p + 1;
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

	// public void sendHeadImage(File image, String selfAccount) {
	// System.out.println("c 开始发");
	//
	// InetSocketAddress remoteAddr = getConnectionAddress(SERVER);
	// try (Socket socket = new Socket(remoteAddr.getAddress(),
	// getTcpRecvPortFromUdpRecvPort(getUdpRecvPortFromUdpSendPort(remoteAddr.getPort()))))
	// {
	//
	// File imageLocal = new File("./src/Client/HeadImages/" + selfAccount +
	// ".jpg");
	//
	// BufferedOutputStream out = new
	// BufferedOutputStream(socket.getOutputStream());
	// BufferedOutputStream localOut = new BufferedOutputStream(new
	// FileOutputStream(imageLocal));
	// BufferedInputStream in = new BufferedInputStream(new FileInputStream(image));
	//
	// byte[] buffer = new byte[1024];
	// int n = 0;
	// while ((n = in.read(buffer)) != -1) {
	// out.write(buffer, 0, n);
	// localOut.write(buffer, 0, n);
	// }
	//
	// localOut.close();
	// out.close();
	// in.close();
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
	//
	// public void waitHeadImage(String sender, ServerUI caller) {
	// System.out.println("s 开始等");
	// try (ServerSocket serverSocket = new ServerSocket(getPortInTCP());) {
	// Socket socket = serverSocket.accept();
	//
	// File image = new File("./src/Server/HeadImages/" + sender + ".jpg");
	//
	// FileOutputStream out = new FileOutputStream(image);
	// BufferedInputStream in = new BufferedInputStream(socket.getInputStream());
	//
	// uploadingFlag = true;
	// byte[] buffer = new byte[1024];
	// int n = 0;
	// while ((n = in.read(buffer)) != -1) {
	// out.write(buffer, 0, n);
	// }
	// out.close();
	// in.close();
	// socket.close();
	// uploadingFlag = false;
	// caller.sendUpdateSucceed(sender);
	// } catch (IOException e) {
	// e.printStackTrace();
	// }
	// }
}
