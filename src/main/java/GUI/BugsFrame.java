package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.GregorianCalendar;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;

/*
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
 */

/**
 * @author wolf
 */
public class BugsFrame extends JFrame {
	private Container cp;
	private JButton okButton;
	
	final static private String myDate = "20.07.2004";
	
	final static private String bugsMessage = 
		"None known. Do intensive testing, find some and mail them to me please.\n"+
		"\n";
	
	final static private String todoMessage = 
		"Settings:\n" +
		"Store settings in some kind of .ini file.\n"+
		"\n" +
		"DiskPanel:\n" +
		"Implement \"Reload D64\" function.\n"+
		"\n" +
		"general: allow users to enter special characters when editing diskname and filenames.\n"+
		"\n" +
		"general: allow users to combine external transfer programs within\n"+
		"\n" +
		"general: do intensive testing\n"+
		"\n" +
		"general: implement drag and drop\n"+
		"\n" +
		"delPRG: implement method\n"+
		"\n" +
		"Settings: implement methods\n" +
		"\n" +
		"DiskPanel:\n" +
		"Warn users if file will get overwritten. Include nice .D64 and .PRG extensions automatically.\n"+
		"\n" +
		"renameDisk and renamePRG:\n" +
		"Allow user to enter only limited string in the textfields.\n"+
		"\n" +
		"renamePRG: add some more parameters\n"+
		"\n" +
 		"insertPRG: this always sets new FileType = PRG\n"+
		"\n" +
 		"d64.writeDirectoryEntry: what to do if filename exists?\n" +
		"\n" +
		"d64.setNewDirectoryEntry: relative files\n" +
		"\n" +
		"d64.setNewDirectoryEntry: GEOS files\n" +
		"\n";
	
	public BugsFrame (String topText)
	{
		GregorianCalendar cal = new GregorianCalendar();
		//String myDate = 
		//	cal.get(Calendar.DATE) + "."+(cal.get(Calendar.MONTH)+1) + "."+cal.get(Calendar.YEAR) +" " +
		//	cal.get(Calendar.HOUR_OF_DAY) + ":"+cal.get(Calendar.MINUTE) + ":"+cal.get(Calendar.SECOND);  
		setTitle(topText + " - "+myDate);
		
		//setModal(true);

		cp = getContentPane();
		cp.setLayout( new BorderLayout());

		JPanel bugsPanel = new JPanel();
		bugsPanel.setLayout( new BorderLayout());
		JLabel bugsLabel = new JLabel("Bugs:");
		JTextArea bugsTextArea = new JTextArea(10,60);
		bugsTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		bugsTextArea.setBackground(new Color(255,230,230));
		//bugsTextArea.setForeground(Color.BLACK);	// gives errors on Windows and JRE 1.3!
		bugsTextArea.setEditable(false);
		bugsTextArea.setText(bugsMessage);
		bugsTextArea.setWrapStyleWord(true);
		bugsTextArea.setLineWrap(true);
		bugsTextArea.setCaretPosition(0);
		bugsPanel.add(bugsLabel, BorderLayout.NORTH);
		bugsPanel.add(new JScrollPane(bugsTextArea), BorderLayout.SOUTH);
		
		JPanel todoPanel = new JPanel();
		todoPanel.setLayout( new BorderLayout());
		JLabel todoLabel = new JLabel("To do:");
		JTextArea todoTextArea = new JTextArea(10,60);
		todoTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		todoTextArea.setBackground(new Color(230,230,255));
		//todoTextArea.setForeground(Color.BLACK);	// gives errors on Windows and JRE 1.3!
		todoTextArea.setEditable(false);
		todoTextArea.setText(todoMessage);
		todoTextArea.setWrapStyleWord(true);
		todoTextArea.setLineWrap(true);
		todoTextArea.setCaretPosition(0);
		todoPanel.add(todoLabel, BorderLayout.NORTH);
		todoPanel.add(new JScrollPane(todoTextArea), BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel();
		okButton = new JButton("Ok");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave \"Bugs and ToDo\".");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton )
				{
						dispose();
				}
			}
		});
		buttonPanel.add(okButton);

		cp.add(bugsPanel, BorderLayout.NORTH);		
		cp.add(todoPanel, BorderLayout.CENTER);		
		cp.add(buttonPanel, BorderLayout.SOUTH);		

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
			(int)((dim.width - getSize().getWidth()) / 3),
			(int)((dim.height - getSize().getHeight()) / 3)
		);
//		setLocation(300,200);
//		setSize(400,400);
		pack();
		setVisible(true);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}