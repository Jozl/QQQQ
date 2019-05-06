package com;

import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.filechooser.FileFilter;

public class ImageChooser extends JFileChooser {
	private static final long serialVersionUID = 1L;

	public ImageChooser() {
		super();
		setAcceptAllFileFilterUsed(false);
		setFileFilter(new ImageFileFilter());
	}

	class ImageFileFilter extends FileFilter {
		@Override
		public boolean accept(File f) {
			if (f.getName().endsWith(".jpg") || f.isDirectory())
				return true;
			return false;
		}

		@Override
		public String getDescription() {
			return "ͼƬ(*.jpg)";
		}

	}
}
