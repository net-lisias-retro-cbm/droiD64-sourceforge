package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.text.MessageFormat;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import droid64.d64.Utility;

/**<pre style='font-family:Sans,Arial,Helvetica'>
 * Created on 30.06.2004
 *
 *   droiD64 - A graphical filemanager for D64 files
 *   Copyright (C) 2004 Wolfram Heyer
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
 *   eMail: wolfvoz@users.sourceforge.net
 *   http://droid64.sourceforge.net
 *</pre>
 * @author wolf
 */
public class ShowHelpFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static String about = null;

	public ShowHelpFrame (String topText) {
		this(topText, true);
	}

	/**
	 * Constructor
	 * @param topText title
	 * @param visible if dialog should be visible
	 */
	protected ShowHelpFrame (String topText, boolean visible) {
		setTitle(topText);

		setLayout( new BorderLayout());

		JPanel imagePanel = new JPanel();
		ImageIcon imageIcon = new ImageIcon(getClass().getResource("resources/wolf.jpg"));
		imageIcon.setDescription("Me having some breakfast.");
		imagePanel.add(new JLabel(imageIcon, JLabel.CENTER), BorderLayout.CENTER);
		imagePanel.setToolTipText("Me having some breakfast.");

		JTextPane messageTextArea = new JTextPane();
		messageTextArea.setContentType(Utility.MIMETYPE_HTML);
		messageTextArea.setBackground(new Color(230,230,230));
		messageTextArea.setEditable(false);
		messageTextArea.setText(getAbout());
		messageTextArea.setCaretPosition(0);

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.addActionListener(ae -> dispose());
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);

		add(imagePanel, BorderLayout.NORTH);
		add(new JScrollPane(messageTextArea), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(dim.width/4, dim.height/2);
		setLocation(
				(int)((dim.width - getSize().getWidth()) / 3),
				(int)((dim.height - getSize().getHeight()) / 3));

		setVisible(visible);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private static String getAbout() {
		if (about == null) {
			String str = Utility.getResource("resources/about.html");
			Object[] args = {
					System.getProperty("java.vendor"),
					System.getProperty("java.version"),
					System.getProperty("os.name"),
					System.getProperty("os.version"),
					System.getProperty("os.arch"),
			};
			about = MessageFormat.format(str, args);
		}
		return about;
	}

}
