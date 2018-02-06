package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
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
import javax.swing.JTextArea;

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
	private static final String MY_DATE = "28.01.2016";
	private static final String BUGS_MESSAGE = "None known. Do intensive testing, find some and mail them to me please.\n\n";
	
	private static final String TODO_MESSAGE = 
		"General:   Do intensive testing\n\n" +
		"General:   Allow users to enter special characters when editing diskname and filenames.\n\n" +
		"General:   Implement drag and drop\n\n" +
		"General:   CP/M write support\n\n" +
		"DiskPanel: Warn users if file will get overwritten.\n\n" +
		"Rename:    Allow user to enter only limited string in the textfields.\n\n" +
 		"WriteDirectoryEntry: what to do if filename exists?\n\n" +
		"SetNewDirectoryEntry: relative files\n\n" +
		"SetNewDirectoryEntry: GEOS files\n\n";
	
	public BugsFrame (String topText) { 
		setTitle(topText + " - "+MY_DATE);

		JTextArea bugsTextArea = new JTextArea(5,60);
		bugsTextArea.setEditable(false);
		bugsTextArea.setText(BUGS_MESSAGE);
		bugsTextArea.setWrapStyleWord(true);
		bugsTextArea.setLineWrap(true);
		bugsTextArea.setCaretPosition(0);

		JPanel bugsPanel = new JPanel();
		bugsPanel.setLayout( new BorderLayout());
		bugsPanel.add(new JLabel("Bugs:"), BorderLayout.NORTH);
		bugsPanel.add(new JScrollPane(bugsTextArea), BorderLayout.SOUTH);
		
		JTextArea todoTextArea = new JTextArea(10,60);
		todoTextArea.setBackground(new Color(230,230,255));
		todoTextArea.setEditable(false);
		todoTextArea.setText(TODO_MESSAGE);
		todoTextArea.setWrapStyleWord(true);
		todoTextArea.setLineWrap(true);
		todoTextArea.setCaretPosition(0);

		JPanel todoPanel = new JPanel();
		todoPanel.setLayout( new BorderLayout());
		todoPanel.add(new JLabel("To do:"), BorderLayout.NORTH);
		todoPanel.add(new JScrollPane(todoTextArea), BorderLayout.CENTER);

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave \"Bugs and ToDo\".");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == okButton ) {
						dispose();
				}
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);

		Container cp = getContentPane();
		cp.setLayout( new BorderLayout());
		cp.add(bugsPanel, BorderLayout.NORTH);		
		cp.add(todoPanel, BorderLayout.CENTER);		
		cp.add(buttonPanel, BorderLayout.SOUTH);		

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
			(int)((dim.width - getSize().getWidth()) / 3),
			(int)((dim.height - getSize().getHeight()) / 3)
		);

		pack();
		setVisible(true);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
