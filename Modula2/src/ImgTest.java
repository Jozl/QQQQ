import java.awt.BorderLayout;
import java.awt.Image;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileFilter;

import org.bouncycastle.crypto.tls.NewSessionTicket;

import com.ImageChooser;
import com.Message;

public class ImgTest {
	public static void main(String[] args) {
		JFileChooser chooser = new ImageChooser();
		chooser.showDialog(null, "Ñ¡Ôñ");
		try {
			Image image = ImageIO.read(chooser.getSelectedFile());
//			ArrayList<Object> arrayList = new ArrayList<Object>();
//			arrayList.add("123123");
//			arrayList.add(image);
//			Message message = new Message.messageBuilder<>().Payload(arrayList).build();
//			System.out.println(().getPayload()).getClass());
			
			JFrame frame = new JFrame();
		    JLabel label = new JLabel(new ImageIcon(image));
		    frame.getContentPane().add(label, BorderLayout.CENTER);
		    frame.pack();
		    frame.setVisible(true); 
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
