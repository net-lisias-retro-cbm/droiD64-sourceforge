package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;

import droid64.d64.CbmException;

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
public class TextViewFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	private JTextArea textArea;

	private Font normalFont;
	private Font cbmFont;
	
	public TextViewFrame(String windowTitle, String title, byte[] data) {
		setModal(false);
		setModalityType(ModalityType.MODELESS);
		setTitle(windowTitle);
		try {
			if (data == null || data.length == 0) {
				dispose();
			}
			byte[] filtered = new byte[data.length];
			int out = 0;
			for (int in=0; in<data.length; in++) {
				byte c = data[in];
				if ((c>=0x20 && c<=0x7e) || c==0x09 || c==0x0a || c== 0x0d || (c>=0xa0 && c<=0xff)) {
					filtered[out++] = c;
				}
			}
			String s = new String(Arrays.copyOfRange(filtered, 0, out), "ISO-8859-1");
			setup(title, s);			
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	
	public TextViewFrame(JDialog parent, String windowTitle, String title, String message, boolean isModal) {
		super(parent, windowTitle);
		setModal(isModal);
		setTitle(windowTitle);
		setup(title, message);
	}

	public TextViewFrame(JFrame parent, String windowTitle, String title, String message) {
		super(parent, windowTitle);
		setModal(true);
		setTitle(windowTitle);
		setup(title, message);
	}
	
	
	private void setup(final String title, final String message) {
		textArea = new JTextArea(message);
		textArea.setEditable(false);
		
		try {
			cbmFont = Settings.getCommodoreFont();
		} catch (CbmException e1) {
			e1.printStackTrace();
			cbmFont = normalFont;
		}

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave BAM view.");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == okButton ) {
					dispose();
				}
			}
		});		
		
		final JToggleButton c64ModeButton = new JToggleButton("C64 mode");
		c64ModeButton.setMnemonic('c');
		c64ModeButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == c64ModeButton) {
					boolean c64Mode = c64ModeButton.isSelected();
					if (c64Mode) {
						textArea.setFont(cbmFont);
					} else {
						textArea.setFont(normalFont);
					}
				}
			}
		});
		
		final JButton printButton = new JButton("Print");
		printButton.setMnemonic('p');
		printButton.setToolTipText("Print");
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == printButton ) {
					print(message, title, c64ModeButton.isSelected());
				}
			}
		});
		
		JPanel buttonPanel = new JPanel();		
		buttonPanel.add(printButton);
		buttonPanel.add(c64ModeButton);
		buttonPanel.add(okButton);
		
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(new JLabel(title), BorderLayout.NORTH);		
		cp.add(new JScrollPane(textArea), BorderLayout.CENTER);		
		cp.add(buttonPanel, BorderLayout.SOUTH);		

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((screenSize.width - getSize().getWidth()) / 3),
				(int)((screenSize.height - getSize().getHeight()) / 3));
		pack();
		setSize(screenSize.width/6, screenSize.height/2);
		setVisible(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	private void print(final String text, final String title, boolean useCbmFont) {
		PrinterJob job = PrinterJob.getPrinterJob();		
		job.setPageable(new PrintPageable(text, title, useCbmFont, false));
		boolean doPrint = job.printDialog();
		if (doPrint) {
		    try {
		        job.print();
		    } catch (PrinterException e) {
		    	e.printStackTrace();
		    }
		}
	}
	
}
