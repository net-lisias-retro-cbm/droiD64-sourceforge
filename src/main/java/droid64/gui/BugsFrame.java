package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

import droid64.d64.Utility;

/**<pre style='font-family:sans-serif;'>
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
public class BugsFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static String todo = null;

	public BugsFrame (String topText) {
		this(topText, true);
	}

	protected BugsFrame (String topText, boolean visible) {
		setTitle(topText);
		JPanel bugsPanel = createTextPane(Settings.getMessage(Resources.DROID64_BUGS_INGRESS), Resources.DROID64_BUGS_BUGS, BorderLayout.SOUTH);
		JPanel todoPanel = createTextPane(getBugs(), Resources.DROID64_BUGS_TODO, BorderLayout.CENTER);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(bugsPanel, BorderLayout.NORTH);
		cp.add(todoPanel, BorderLayout.CENTER);
		cp.add(drawButtonPanel(), BorderLayout.SOUTH);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((dim.width - getSize().getWidth()) / 3),
				(int)((dim.height - getSize().getHeight()) / 3));

		pack();
		setVisible(visible);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private JPanel drawButtonPanel() {
		final JButton okButton = new JButton(Settings.getMessage(Resources.DROID64_BUGS_OK));
		okButton.setMnemonic('o');
		okButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == okButton ) {
					dispose();
				}
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);
		return buttonPanel;
	}

	private JPanel createTextPane(String message, String labelPropKey, String constraints) {
		JTextPane bugsTextArea = drawTextArea(message);
		JPanel bugsPanel = new JPanel(new BorderLayout());
		bugsPanel.add(new JLabel(Settings.getMessage(labelPropKey)), BorderLayout.NORTH);
		bugsPanel.add(new JScrollPane(bugsTextArea), constraints);
		return bugsPanel;
	}

	private JTextPane drawTextArea(String message) {
		JTextPane textArea = new JTextPane();
		textArea.setContentType(Utility.MIMETYPE_HTML);
		textArea.setEditable(false);
		textArea.setText(message);
		textArea.setCaretPosition(0);
		return textArea;
	}

	private static String getBugs() {
		if (todo == null) {
			todo = Utility.getResource("resources/bugs.html");
		}
		return todo;
	}

}
