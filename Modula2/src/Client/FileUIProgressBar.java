package Client;

import javax.swing.JToolBar;
import javax.swing.JProgressBar;
import java.awt.Color;

public class FileUIProgressBar extends JToolBar {
	private static final long serialVersionUID = 1L;
	
	private JProgressBar progressBar;
	
	FileUIProgressBar(){
		setFloatable(false);
		
		progressBar = new JProgressBar();
		add(progressBar);
		setVisible(false);
	}

	public void setProgressValue(int i) {
		progressBar.setValue(i);
	}
	
	public void succeed() {
		progressBar.setForeground(Color.GREEN);
	}
}
