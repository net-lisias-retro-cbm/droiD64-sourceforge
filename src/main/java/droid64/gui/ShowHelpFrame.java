package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;

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
	final static private String INFO_MESSAGE =
			//about: description of the program
			"<html>" +
			"<h1>About, credits and greetings</h1>" +
			"<p>This is a filemanager for disk-images suitable for the Commodore 64 and others. " +
			"These disk-images are usually stored as .D64 files and have the size of 174848 bytes.</p>" +
			"<p>May this tool make life easier for Commodore 64 fans, especially for those using MAC and Linux systems " +
			"as there are tools available for Windows anyway." +
			"</p>" +
			//credits
			"<p>Done from scratch by Wolfram Heyer.<br/>"+
			"Fiel format structures and 1541 information taken from some fairlight docs at: " +
			"<a href=\"http://www.fairlight.to/docs/text/formats.zip\">http://www.fairlight.to/docs/text/formats.zip</a><br/>" +
			"Additions, corrections and a few bugs fixed by Henrik Wetterström in 2015/2016." +
			"</p>" +
			//greetings
			"<p><b>Greetings:</b></p>"+
			"<p>The whole VOZ crew: <br/>" +
			"Bitbreaker, Drago MacKayb, Final-Conflict, Ivanov, Mephisto, Nitro, Ragnarok, Skud, Syntax, Toxie and Widdy,<br/>" +
			"other dudes like " +
			"TMA and MRC [Abyss-Connection], Groepaz, Cupid, Deekay, Eyesee, Robocop and the whole Buenzli-crew," +
			"Exile [Anubis] and so on..." +
			"</p>" +
			//copyright
			"<hr/>"+
			"<p>Copyright (C) 2004 Wolfram Heyer</p>" +
			"<p>This program is free software; you can redistribute it and/or modify " +
			"it under the terms of the GNU General Public License as published by " +
			"the Free Software Foundation; either version 2 of the License, or " +
			"(at your option) any later version.<br/>" +
			"This program is distributed in the hope that it will be useful, " +
			"but WITHOUT ANY WARRANTY; without even the implied warranty of " +
			"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the " +
			"GNU General Public License for more details.<br/>" +
			"You should have received a copy of the GNU General Public License " +
			"along with this program; if not, write to the Free Software " +
			"Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA" +
			"</p></html>";

	/**
	 * Constructor
	 * @param topText
	 */
	public ShowHelpFrame (String topText) {
		setTitle(topText);

		Container cp = getContentPane();
		cp.setLayout( new BorderLayout());

		JPanel imagePanel = new JPanel();
		ImageIcon imageIcon = new ImageIcon(getClass().getResource("resources/wolf.jpg"));
		imageIcon.setDescription("Me having some breakfast.");
		imagePanel.add(new JLabel(imageIcon, JLabel.CENTER), BorderLayout.CENTER);
		imagePanel.setToolTipText("Me having some breakfast.");

		JTextPane messageTextArea = new JTextPane();
		messageTextArea.setContentType("text/html");
		messageTextArea.setBackground(new Color(230,230,230));
		messageTextArea.setEditable(false);
		messageTextArea.setText(INFO_MESSAGE);
		messageTextArea.setCaretPosition(0);

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave \"About\".");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton ) {
					dispose();
				}
			}
		});
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(okButton);

		cp.add(imagePanel, BorderLayout.NORTH);
		cp.add(new JScrollPane(messageTextArea), BorderLayout.CENTER);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		pack();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setSize(dim.width/4, dim.height/2);
		setLocation(
				(int)((dim.width - getSize().getWidth()) / 3),
				(int)((dim.height - getSize().getHeight()) / 3)
				);

		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
