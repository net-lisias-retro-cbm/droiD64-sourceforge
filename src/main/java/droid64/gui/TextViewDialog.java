package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PageFormat;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
import javax.swing.JToggleButton;
import javax.swing.RepaintManager;

import droid64.d64.CbmException;
import droid64.d64.Utility;

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
public class TextViewDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextPane textPane;
	private MainPanel mainPanel;
	private Font normalFont;
	private Font cbmFont;

	public TextViewDialog(String windowTitle, String title, byte[] data, MainPanel mainPanel) {
		setModal(false);
		setModalityType(ModalityType.MODELESS);
		setTitle(windowTitle);
		try {
			if (data == null || data.length == 0) {
				dispose();
			} else {
				setup(title, filter(data), null, mainPanel);
			}
		} catch (UnsupportedEncodingException e) {
			mainPanel.appendConsole("Failed to encode text.\n" + e);
		}
	}

	public TextViewDialog(JDialog parent, String windowTitle, String title, String message, boolean isModal, String mimetype, MainPanel mainPanel) {
		super(parent, windowTitle);
		setModal(isModal);
		setTitle(windowTitle);
		setup(title, message, mimetype, mainPanel);
	}

	public TextViewDialog(JFrame parent, String windowTitle, String title, String message, String mimetype, MainPanel mainPanel) {
		super(parent, windowTitle);
		setModal(true);
		setTitle(windowTitle);
		setup(title, message, mimetype, mainPanel);
	}

	private String filter(byte[] data) throws UnsupportedEncodingException {
		byte[] filtered = new byte[data.length];
		int out = 0;
		for (int in=0; in<data.length; in++) {
			byte c = data[in];
			if ((c>=0x20 && c<=0x7e) || c==0x09 || c==0x0a || c== 0x0d || (c>=0xa0 && c<=0xff)) {
				filtered[out++] = c;
			}
		}
		return new String(Arrays.copyOfRange(filtered, 0, out), "ISO-8859-1");
	}

	private void setup(final String title, final String message, final String mimetype, final MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		textPane = new JTextPane();
		if (mimetype != null) {
			textPane.setContentType(mimetype);
		} else {
			textPane.setContentType(Utility.MIMETYPE_HTML);
		}
		textPane.setText(message);
		textPane.setEditable(false);
		textPane.setCaretPosition(0);

		try {
			cbmFont = Settings.getCommodoreFont();
		} catch (CbmException e) {
			mainPanel.appendConsole("Failed to set Commodore font. Using default font.\n"+e);
			cbmFont = normalFont;
		}

		final JButton okButton = new JButton("OK");
		final JToggleButton c64ModeButton = new JToggleButton("C64 mode");
		final JButton saveButton = new JButton("Save");
		final JButton printButton = new JButton("Print");

		okButton.setMnemonic('o');
		c64ModeButton.setMnemonic('c');
		saveButton.setMnemonic('s');
		printButton.setMnemonic('p');

		ActionListener listener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == okButton ) {
					dispose();
				} else if ( event.getSource() == c64ModeButton ) {
					if (c64ModeButton.isSelected()) {
						textPane.setFont(cbmFont);
					} else {
						textPane.setFont(normalFont);
					}
				} else if ( event.getSource() == saveButton ) {
					save(message);
				} else if ( event.getSource() == printButton ) {
					print(message, title, c64ModeButton.isSelected());
				}
			}
		};

		okButton.addActionListener(listener);
		c64ModeButton.addActionListener(listener);
		saveButton.addActionListener(listener);
		printButton.addActionListener(listener);

		JPanel buttonPanel = new JPanel();
		if (!Utility.MIMETYPE_HTML.equals(mimetype)) {
			buttonPanel.add(printButton);
			buttonPanel.add(c64ModeButton);
		}
		buttonPanel.add(saveButton);
		buttonPanel.add(okButton);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(new JLabel(title), BorderLayout.NORTH);
		cp.add(new JScrollPane(textPane), BorderLayout.CENTER);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((screenSize.width - getSize().getWidth()) / 3),
				(int)((screenSize.height - getSize().getHeight()) / 3));
		pack();
		setSize(screenSize.width/6, screenSize.height/2);
		setVisible(mainPanel != null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void save(String text) {
		if (text.isEmpty()) {
			return;
		}
		String saveFileName = FileDialogHelper.openTextFileDialog("Save text file", null, "", true, new String[] {".txt", ".asm"});
		if (saveFileName != null) {
			try {
				Utility.writeFile(new File(saveFileName), text);
			} catch (CbmException e) {
				mainPanel.appendConsole("Error: Failed to write to file "+saveFileName+"\n" + e);
			}
		}
	}

	private void print(final String text, final String title, boolean useCbmFont) {
		try {
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPageable(new PrintPageable(text, title, useCbmFont, false, mainPanel));
			if (job.printDialog()) {
				job.print();
			}
		} catch (PrinterException e) {
			mainPanel.appendConsole("Failed to print text.\n"+e);
		}
	}

	public int print(Graphics g, PageFormat pf, int pageIndex) {
		Graphics2D g2 = (Graphics2D) g;
		g2.setColor(Color.black);

		RepaintManager.currentManager(this).setDoubleBufferingEnabled(false);
		Dimension d = this.getSize();
		double panelWidth = d.width;
		double panelHeight = d.height;
		double pageWidth = pf.getImageableWidth();
		double pageHeight = pf.getImageableHeight();
		double scale = pageWidth / panelWidth;
		int totalNumPages = (int) Math.ceil(scale * panelHeight / pageHeight);

		// Check for empty pages
		if (pageIndex >= totalNumPages) {
			return Printable.NO_SUCH_PAGE;
		}
		g2.translate(pf.getImageableX(), pf.getImageableY());
		g2.translate(0f, -pageIndex * pageHeight);
		g2.scale(scale, scale);
		paint(g2);
		return Printable.PAGE_EXISTS;
	}

}
