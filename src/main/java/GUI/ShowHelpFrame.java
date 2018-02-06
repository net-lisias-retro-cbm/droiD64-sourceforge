package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
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
public class ShowHelpFrame extends JFrame {
	private Container cp;
	private JButton okButton;
	final static private String infoMessage =
		
		//about: description of the program 
		"This is a filemanager for disk-images suitable for the Commodore 64 and others. " +
		"These disk-images are usually stored as .D64 files and have the size of 174848 bytes.\n" +
		"May this tool make life easier for Commodore 64 fans, especially for those using MAC and Linux systems " +
		"as there are tools available for Windows anyway." + 
		
		"\n\n" +
		
		//credits
		"Done from scratch by Wolfram Heyer.\n"+
		"D64 structure and 1541 information taken from some fairlight docs at: " +
		"http://www.fairlight.to/docs/text/formats.zip" + 
		
		"\n\n" +
		
		//greetings
		"Greetings:\n"+
		"The whole VOZ crew: " +
		"Bitbreaker, Drago MacKayb, Final-Conflict, Ivanov, Mephisto, Nitro, Ragnarok, Skud, Syntax, Toxie and Widdy,\n" +
		"other dudes like " +
		"TMA and MRC [Abyss-Connection], Groepaz, Cupid, Deekay, Eyesee, Robocop and the whole Buenzli-crew," +
		"Exile [Anubis] and so on..." +
		
		"\n\n" +
	
		//copyright	
		"-----------------\n\n"+ 
	 	"Copyright (C) 2004 Wolfram Heyer\n\n" +
		 "This program is free software; you can redistribute it and/or modify " +
		 "it under the terms of the GNU General Public License as published by " +
	 	"the Free Software Foundation; either version 2 of the License, or " +
	 	"(at your option) any later version.\n\n" +
		 "This program is distributed in the hope that it will be useful, " +
	 	"but WITHOUT ANY WARRANTY; without even the implied warranty of " +
	 	"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
	 	"GNU General Public License for more details.\n\n" +
		 "You should have received a copy of the GNU General Public License " +
	 	"along with this program; if not, write to the Free Software " +
		 "Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA" +

		"\n"

	;
	

	public ShowHelpFrame (String topText)
	{
		setTitle(topText);
		
		cp = getContentPane();
		cp.setLayout( new BorderLayout());

		JPanel imagePanel = new JPanel();
		ImageIcon imageIcon = new ImageIcon(this.getClass().getResource("ressources/wolf.jpg"));
		imageIcon.setDescription("Me having some breakfast.");
		imagePanel.add(new JLabel(imageIcon, JLabel.CENTER), BorderLayout.CENTER);
		imagePanel.setToolTipText("Me having some breakfast.");

		JPanel messagePanel = new JPanel();
		messagePanel.setLayout( new BorderLayout());
		JLabel messageLabel = new JLabel("About, credits and greetings:");
		JTextArea messageTextArea = new JTextArea(10,45);
		messageTextArea.setFont(new Font("Monospaced", Font.PLAIN, 12));
		messageTextArea.setBackground(new Color(230,230,230));
		//messageTextArea.setForeground(Color.BLACK);		// gives errors on Windows and JRE 1.3!
		messageTextArea.setEditable(false);
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setText(infoMessage);
		messageTextArea.setCaretPosition(0);
		messagePanel.add(messageLabel, BorderLayout.NORTH);
		messagePanel.add(new JScrollPane(messageTextArea), BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel();
		okButton = new JButton("Ok");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave \"About\".");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton )
				{
						dispose();
				}
			}
		});
		buttonPanel.add(okButton);

		cp.add(imagePanel, BorderLayout.NORTH);		
		cp.add(messagePanel, BorderLayout.CENTER);		
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