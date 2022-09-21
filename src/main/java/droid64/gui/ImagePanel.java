package droid64.gui;

import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

public class ImagePanel extends JPanel {
	private static final long serialVersionUID = 1L;
	private transient Image image;
	private String name;

	public ImagePanel(CbmPicture picture) throws IOException {
		BufferedImage bufImage = picture.getImage();
		this.image = picture.getImage();
		this.name = picture.getName();
		Dimension dim = new Dimension(bufImage.getWidth(), bufImage.getHeight());
		setSize(dim);
		setMinimumSize(dim);
		setPreferredSize(dim);
	}

	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		g.drawImage(image, 0, 0, this);
	}

	public void setImage(CbmPicture picture) throws IOException {
		BufferedImage img = picture.getImage();
		this.image = img;
		this.name = picture.getName();
		Dimension dim = new Dimension(img.getWidth(), img.getHeight());
		setSize(dim);
		setMinimumSize(dim);
		setPreferredSize(dim);
		invalidate();
		repaint();
	}

	@Override
	public String getName() {
		return name;
	}
}
