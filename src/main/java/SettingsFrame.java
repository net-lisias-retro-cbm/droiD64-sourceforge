import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;

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
public class SettingsFrame extends JDialog {
	private Container cp;
	private JButton okButton;
	private DroiD64 droiD64;
	private static final String[] Colours = { 
		"gray",
		"red",
		"green",
		"blue",
		"light-blue"
	};
	private JComboBox colourBox;
	private JPanel colourPanel;
	private JCheckBox exitConfirmCheckBox;

	public SettingsFrame (String topText, DroiD64 droiD64_)
	{
		setTitle(topText);
		
		droiD64 = droiD64_;

		setModal(true);
		
		cp = getContentPane();
		cp.setLayout( new BorderLayout());

		JPanel imagePanel = new JPanel();
		exitConfirmCheckBox = new JCheckBox("Confirm Exit");
		exitConfirmCheckBox.setToolTipText("Whether to confirm quitting the program or not.");
		exitConfirmCheckBox.setSelected(droiD64.isExitQuestion());
		imagePanel.add(exitConfirmCheckBox, BorderLayout.CENTER);
		
		JPanel messagePanel = new JPanel();
		JLabel messageLabel = new JLabel("Sorry, not implemented yet.");
		messagePanel.add(messageLabel);

		JPanel buttonPanel = new JPanel();
		okButton = new JButton("Ok");
		okButton.setToolTipText("Leave \"Settings\".");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton )
				{
						droiD64.setExitQuestion(exitConfirmCheckBox.isSelected());
						droiD64.setColourChoice(colourBox.getSelectedIndex());
						dispose();
				}
			}
		});
		buttonPanel.add(okButton);

		colourPanel = new JPanel();
		JLabel colourLabel = new JLabel("Colour:");
		colourBox = new JComboBox(Colours);
		colourBox.setToolTipText("Select a colour-sceme here.");
		colourBox.setEditable(false);
		colourBox.setSelectedIndex(droiD64.getColourChoice());
		colourPanel.add(colourLabel);
		colourPanel.add(colourBox);

		cp.add(imagePanel, BorderLayout.NORTH);		
		//cp.add(messagePanel, BorderLayout.CENTER);		
		cp.add(colourPanel, BorderLayout.CENTER);		
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