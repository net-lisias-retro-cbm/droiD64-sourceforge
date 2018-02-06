package GUI;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/*
 * Created on 25.06.2004
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
public class RenameD64Frame extends JDialog {
	private Container cp;
	private JButton exitButton, okButton;
	private DiskPanel diskPanel;
	private JTextField nameTextField, idTextField;
	
	public RenameD64Frame (String topText, DiskPanel diskPanel_, String oldDiskName, String oldDiskID)
	{
		//super(topText, true);
		
		setTitle(topText);
		
		diskPanel = diskPanel_;
		
		setModal(true);

		cp = getContentPane();
		cp.setLayout( new BorderLayout());
		
		JPanel namePanel = new JPanel();
		JPanel idPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		
		JLabel diskNameLabel = new JLabel("Diskname:");
		nameTextField = new JTextField(oldDiskName, 16);
		nameTextField.setToolTipText("Enter the new label of your D64 here.");
	
		JLabel idNameLabel = new JLabel("Disk-ID:");
		idTextField = new JTextField(oldDiskID, 5);
		idTextField.setToolTipText("Enter the new ID of your D64 here.");
	
		exitButton = new JButton("Cancel");
		exitButton.setMnemonic('c');
		exitButton.setToolTipText("Cancel and return.");
		exitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==exitButton )
				{
						diskPanel.setNewDiskNameSuccess(false);
						dispose();
				}
			}
		});
		
		okButton = new JButton("Ok");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Proceed.");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton )
				{
						diskPanel.setNewDiskNameSuccess(true);
	
						String diskName = nameTextField.getText();
						if (diskName.length() > 16) diskName = diskName.substring(0,16);
						diskPanel.setNewDiskName(diskName);

						String diskID = idTextField.getText();
						if (diskID.length() > 5) diskID = diskID.substring(0,5);
						diskPanel.setNewDiskID(diskID);

						dispose();
				}
			}
		});
		
		namePanel.add(diskNameLabel);
		namePanel.add(nameTextField);
		
		idPanel.add(idNameLabel);
		idPanel.add(idTextField);
		
		buttonPanel.add(exitButton);
		buttonPanel.add(okButton);

		cp.add(namePanel, BorderLayout.NORTH);		
		cp.add(idPanel, BorderLayout.CENTER);		
		cp.add(buttonPanel, BorderLayout.SOUTH);		

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
			(int)((dim.width - getSize().getWidth()) / 3),
			(int)((dim.height - getSize().getHeight()) / 3)
		);
//		setLocation(300,200);
		pack();
		setVisible(true);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
