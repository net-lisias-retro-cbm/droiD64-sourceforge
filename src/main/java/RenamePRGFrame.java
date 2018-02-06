import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;

/*
 * Created on 26.06.2004
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
public class RenamePRGFrame extends JDialog {

	private Container cp;
	private JButton exitButton, okButton;
	private DiskPanel diskPanel;
	private JTextField nameTextField;
	private JComboBox fileTypeBox;

	private static final String[] FileType = { 
		"DEL",
		"SEQ",
		"PRG",
		"USR",
		"REL"
	};
	
	public RenamePRGFrame (String topText, DiskPanel diskPanel_, String oldFileName, int oldFileType)
	{
		//super(topText, true);
		
		setTitle(topText);
		
		diskPanel = diskPanel_;

		diskPanel.setNewPRGType(oldFileType);
		
		setModal(true);

		cp = getContentPane();
		cp.setLayout( new BorderLayout());
		
		JPanel namePanel = new JPanel();
		JPanel idPanel = new JPanel();
		JPanel buttonPanel = new JPanel();
		
		JLabel fileNameLabel = new JLabel("Filename:");
		nameTextField = new JTextField(oldFileName, 16);
		nameTextField.setToolTipText("Enter the new filename here.");
	
		JLabel fileTypeLabel = new JLabel("FileType:");
		fileTypeBox = new JComboBox(FileType);
		fileTypeBox.setToolTipText("Select a filetype here.");
		fileTypeBox.setEditable(false);
		fileTypeBox.setSelectedIndex(oldFileType);
	
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
						diskPanel.setNewPRGNameSuccess(true);
	
						String diskName = nameTextField.getText();
						if (diskName.length() > 16) diskName = diskName.substring(0,16);
						diskPanel.setNewPRGName(diskName);
						
						if (fileTypeBox.getSelectedIndex() != -1)
							diskPanel.setNewPRGType(fileTypeBox.getSelectedIndex());

						dispose();
				}
			}
		});
		
		namePanel.add(fileNameLabel);
		namePanel.add(nameTextField);
		
		idPanel.add(fileTypeLabel);
		idPanel.add(fileTypeBox);
		
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
