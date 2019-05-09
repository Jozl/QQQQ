package Server;

import java.io.File;

public class ServerImage {
	File image;
	boolean isReading;
	public ServerImage(File image, boolean isReading) {
		this.image = image;
		this.isReading = isReading;
	}
	
	public boolean isReading() {
		return isReading;
	}

	public File getFile() {
		return image;
	}

	@Override
	public String toString() {
		return "ServerImage [image=" + image.getName() +" l: "+image.length()/1024+ ", isReading=" + isReading + "]";
	}
}
