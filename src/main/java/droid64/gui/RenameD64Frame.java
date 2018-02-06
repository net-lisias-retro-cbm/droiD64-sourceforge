package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import droid64.d64.CbmException;
import droid64.d64.DiskImage;

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

	private static final long serialVersionUID = 1L;

	private JTextField nameTextField;
	private JTextField idTextField;
	private JCheckBox compressedBox;
	private JCheckBox cpmBox;
	private JComboBox<Object>  diskTypeBox;
	
	public RenameD64Frame (String topText, final DiskPanel diskPanel, String oldDiskName, String oldDiskID, final boolean create) {		
		setTitle(topText);

		final JButton exitButton = new JButton("Cancel");
		exitButton.setMnemonic('c');
		exitButton.setToolTipText("Cancel and return.");
		exitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource() == exitButton ) {
					diskPanel.setNewDiskNameSuccess(false);
					dispose();
				}
			}
		});

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Proceed.");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource() == okButton ) {
					diskPanel.setNewDiskNameSuccess(true);
					String diskName = nameTextField.getText();
					if (diskName.length() > 16) {
						diskName = diskName.substring(0,16);
					}
					diskPanel.setNewDiskName(diskName);
					String diskID = idTextField.getText();
					int diskIdLen = diskID.length();
					int chosenType = diskTypeBox.getSelectedIndex();
					if (diskIdLen > 5) {
						diskID = diskID.substring(0, 5);
					} else if (diskIdLen <= 2) {
						diskID = (diskID + "  ").substring(0,2) + (chosenType == 2 ? " 3D" : " 2A");
					}
					diskPanel.setNewDiskID(diskID);
					if (create) {
						if (chosenType == 0) {
							diskPanel.setNewDiskType(DiskImage.D64_IMAGE_TYPE);
						} else if (chosenType == 1) {
							diskPanel.setNewDiskType(DiskImage.D71_IMAGE_TYPE);
						} else if (chosenType == 2) {
							diskPanel.setNewDiskType(DiskImage.D81_IMAGE_TYPE);
						}
						diskPanel.setNewCompressedDisk(compressedBox.isSelected());
						diskPanel.setNewCpmDisk(cpmBox.isSelected());
					}
					dispose();
				}
			}
		});

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(exitButton);
		buttonPanel.add(okButton);

		JPanel mainPanel = setupMainPanel(oldDiskName, oldDiskID, create);

		
		setModal(true);
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());		
		cp.add(mainPanel, BorderLayout.CENTER);
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

	private JPanel setupMainPanel(String diskName, String diskId, boolean create) {
				
		nameTextField = new JTextField("", 16);
		nameTextField.setToolTipText("The label of your image (max 16 characters)");
		nameTextField.setDocument(new LimitLengthDocument(16, diskName));

		idTextField = new JTextField("", 5);
		idTextField.setToolTipText("The disk ID of your image.");
		idTextField.setDocument(new LimitLengthDocument(5, diskId));
		idTextField.setToolTipText("The disk ID (max 5 characters)");
		
		final String[] diskTypes = { "D64", "D71", "D81" };
		diskTypeBox = new JComboBox<Object>(diskTypes);
		diskTypeBox.setToolTipText("Select a disktype.");
		diskTypeBox.setEditable(false);
		diskTypeBox.setSelectedIndex(0);

		compressedBox = new JCheckBox("Compressed image", false);
		compressedBox.setToolTipText("GZIP new image.");
		cpmBox = new JCheckBox("CP/M formatted", false);
		cpmBox.setToolTipText("Format for CP/M.");
		

		final JTextField nameTextField2 = new JTextField("", 16);
		try {
			nameTextField2.setFont(Settings.getCommodoreFont());
			nameTextField2.setBackground(Settings.getDirColorBg());
			nameTextField2.setForeground(Settings.getDirColorFg());
			nameTextField2.setEditable(false);
			nameTextField2.setText(diskName);
			nameTextField2.setBorder(BorderFactory.createCompoundBorder(nameTextField2.getBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			nameTextField.getDocument().addDocumentListener(new DocumentListener(){
				public void insertUpdate(DocumentEvent e) {
					update(e);
				}
				public void removeUpdate(DocumentEvent e) {
					update(e);				
				}
				public void changedUpdate(DocumentEvent e) {
					update(e);	
				}
				private void update(DocumentEvent e) {				
					try {
						Document originator = e.getDocument();
						String text = e.getDocument().getText(0, originator.getLength());
						nameTextField2.setText(text);
					} catch (BadLocationException ex) { }
				}
			});
		} catch (CbmException e) { };

		final JTextField idTextField2 = new JTextField("", 5);
		try {
			idTextField2.setFont(Settings.getCommodoreFont());
			idTextField2.setBackground(Settings.getDirColorBg());
			idTextField2.setForeground(Settings.getDirColorFg());
			idTextField2.setEditable(false);
			idTextField2.setText(diskId);
			idTextField2.setBorder(BorderFactory.createCompoundBorder(idTextField2.getBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));
			idTextField.getDocument().addDocumentListener(new DocumentListener(){
				public void insertUpdate(DocumentEvent e) {
					update(e);
				}
				public void removeUpdate(DocumentEvent e) {
					update(e);				
				}
				public void changedUpdate(DocumentEvent e) {
					update(e);	
				}
				private void update(DocumentEvent e) {				
					try {
						Document originator = e.getDocument();
						String text = e.getDocument().getText(0, originator.getLength());
						idTextField2.setText(text);
					} catch (BadLocationException ex) { }
				}
			});
		} catch (CbmException e) { };
		
		

		JPanel mainPanel = new JPanel();

		GridBagConstraints gbc = new GridBagConstraints();
		mainPanel.setLayout(new GridBagLayout());				
		gbc.fill = GridBagConstraints.HORIZONTAL;
		int row = 0;
		if (create) {
			JPanel createPanel = new JPanel();
			createPanel.add(diskTypeBox);
			createPanel.add(compressedBox);
			createPanel.add(cpmBox);

			addToGridBag(0, row, 0.0, 0.0, gbc, mainPanel, new JLabel("Image Type:"));
			addToGridBag(1, row, 0.0, 0.0, gbc, mainPanel, createPanel);
			addToGridBag(2, row, 1.0, 0.0, gbc, mainPanel, new JPanel());
			row++;
		}
		addToGridBag(0, row, 0.0, 0.0, gbc, mainPanel, new JLabel("Disk Name:"));
		addToGridBag(1, row, 0.0, 0.0, gbc, mainPanel, nameTextField);
		addToGridBag(2, row, 1.0, 0.0, gbc, mainPanel, idTextField);
		row++;
		addToGridBag(0, row, 0.0, 0.0, gbc, mainPanel, new JLabel(""));
		addToGridBag(1, row, 0.0, 0.0, gbc, mainPanel, nameTextField2);
		addToGridBag(2, row, 1.0, 0.0, gbc, mainPanel, idTextField2);
		row++;
		addToGridBag(0, row, 1.0, 1.0, gbc, mainPanel, new JPanel());
		addToGridBag(1, row, 1.0, 1.0, gbc, mainPanel, new JPanel());
		addToGridBag(2, row, 1.0, 1.0, gbc, mainPanel, new JPanel());
		return mainPanel;
	}
	
	/**
	 * Wrapper to add a JComponent to a GridBagConstraints.
	 * @param x column
	 * @param y row
	 * @param weightx column weight
	 * @param weighty row weight
	 * @param gbc GridBagConstaints
	 * @param parent Parent JComponent to which to add a component
	 * @param component the new component to add
	 */
	private void addToGridBag(int x, int y, double weightx, double weighty, GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridx = x;
		gbc.gridy = y;
		parent.add(component, gbc);
	}

}
