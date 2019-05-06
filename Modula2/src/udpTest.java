import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class udpTest {
	public static void main(String[] args) {
		new Thread(new Runnable() {
			public void run() {
				byte[] data = new byte[8046];
				DatagramPacket packet = new DatagramPacket(data, data.length,new InetSocketAddress("77.0.0.1", 4321));
				try (DatagramSocket socket = new DatagramSocket(4321);) {
					System.out.println(packet.getAddress()+""+packet.getPort());
					socket.receive(packet);
					System.out.println(packet.getAddress()+""+packet.getPort());
					socket.receive(packet);
					System.out.println(packet.getAddress()+""+packet.getPort());
					socket.receive(packet);
					System.out.println(packet.getAddress()+""+packet.getPort());
					socket.receive(packet);
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
		new Thread(new Runnable() {
			public void run() {
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				byte[] data = new byte[8046];
				DatagramPacket packet = new DatagramPacket(data, data.length,new InetSocketAddress("127.0.0.1", 4321));
				try (DatagramSocket socket = new DatagramSocket(1234);) {
					while(true) {
						socket.send(packet);
					}
				} catch (SocketException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		}).start();
	}
}
