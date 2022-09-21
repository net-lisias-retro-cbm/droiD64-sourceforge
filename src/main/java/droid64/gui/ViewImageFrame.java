package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.image.BufferedImage;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.util.List;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

/**<pre style='font-family:sans-serif;'>
 * Created on 2015-Oct-15
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2015 Henrik Wetterström
 *
 *   This program is free software; you can redistribute it and/or modify
 *   it under the terms of the GNU General Public License as published by
 *   the Free Software Foundation; either version 2 of the License, or
 *   (at your option) any later version.
 *
 *   This program is distributed in the hope that it will be useful,
 *   but WITHOUT ANY WARRANTY; without even the implied warranty of
 *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *   GNU General Public License for more details.
 *
 *   You should have received a copy of the GNU General Public License
 *   along with this program; if not, write to the Free Software
 *   Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 *
 *   http://droid64.sourceforge.net
 *
 * @author Henrik
 * </pre>
 */
public class ViewImageFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private List<byte[]> dataList;
	private List<String> nameList;
	private int currentIndex = 0;
	private MainPanel mainPanel;

	public ViewImageFrame(String title, List<byte[]> dataList, List<String> nameList, MainPanel mainPanel) throws IOException {
		this.mainPanel = mainPanel;
		if (dataList == null || dataList.isEmpty()) {
			dispose();
		} else {
			this.dataList = dataList;
			this.nameList = nameList;
			setTitle(title);
			setup();
		}
	}

	private void setup() throws IOException {
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JPanel panel = new JPanel();
		cp.add(new JScrollPane(panel), BorderLayout.CENTER);

		final CbmPicture picture = getNextImage();
		final BufferedImage image = picture.getImage();
		final ImagePanel imgPanel = drawImagePanel(panel, image, picture);

		JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.addActionListener(ae -> dispose());

		JButton printButton = new JButton("Print");
		printButton.setMnemonic('p');
		printButton.setToolTipText("Print");
		printButton.addActionListener(ae -> print(image, picture.getName()));

		JButton saveButton = new JButton("Save PNG");
		saveButton.setMnemonic('s');
		saveButton.setEnabled(image != null);
		saveButton.addActionListener(ae -> saveImage(image, imgPanel));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(printButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(okButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation( (int)((dim.width - getSize().getWidth()) / 3),	(int)((dim.height - getSize().getHeight()) / 3)	);
		pack();
		setVisible(mainPanel != null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private ImagePanel drawImagePanel(JPanel parentPanel, BufferedImage image, CbmPicture picture) throws IOException {
		final ImagePanel imgPanel = new ImagePanel(picture);
		if (image != null) {
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.VERTICAL;
			imgPanel.setBorder(BorderFactory.createLineBorder(Color.BLACK));
			if (dataList.size() > 1) {
				imgPanel.addMouseListener(new MouseListener() {
					@Override
					public void mouseClicked(MouseEvent e) {
						clickedImage(imgPanel);
					}
					@Override
					public void mousePressed(MouseEvent e) { /* Not used */}
					@Override
					public void mouseReleased(MouseEvent e) { /* Not used */}
					@Override
					public void mouseEntered(MouseEvent e) { /* Not used */}
					@Override
					public void mouseExited(MouseEvent e) { /* Not used */}
				});
			}
			addToGridBag(0, 0, 0.0, 0.0, gbc, parentPanel, imgPanel);
		}
		return imgPanel;
	}

	private void clickedImage(ImagePanel imgPanel) {
		try {
			imgPanel.setImage(getNextImage());
		} catch (IOException e) {
			mainPanel.appendConsole("Failed to get image: " + e.getMessage());
		}
	}

	private void saveImage(BufferedImage image, ImagePanel imgPanel) {
		try {
			String filename = FileDialogHelper.openTextFileDialog("Save PNG file", null, imgPanel.getName(), true, new String[]{ ".png" });
			if (filename != null) {
				ImageIO.write(image, "png", new File(filename));
			}
		} catch (IOException e) {	//NOSONAR
			mainPanel.appendConsole("Failed to save PNG image.\n"+e.getMessage());
		}
	}

	private CbmPicture getNextImage() {
		currentIndex = currentIndex % dataList.size();
		byte[] data = dataList.get(currentIndex);
		String name = currentIndex < nameList.size() ? nameList.get(currentIndex) : null;
		CbmPicture cbm = new CbmPicture(data, name);
		currentIndex++;
		return cbm;
	}

	private void addToGridBag(int x, int y, double weightx, double weighty, GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		parent.add(component, gbc);
	}

	private void print(BufferedImage image, String title) {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPageable(new PrintPageable(image, title, mainPanel));
		boolean doPrint = job.printDialog();
		if (doPrint) {
			try {
				job.print();
			} catch (PrinterException e) {	//NOSONAR
				mainPanel.appendConsole("Failed to print image.\n"+e.getMessage());
			}
		}
	}
}
