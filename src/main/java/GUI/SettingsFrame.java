package GUI;
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
import javax.swing.JSlider;
import javax.swing.JTabbedPane;

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
	private MainPanel mainPanel;
	private JTabbedPane tabPane;

	private JPanel guiPanel;
	private JButton okButton;
	private static final String[] Colours = { 
		"gray",
		"red",
		"green",
		"blue",
		"light-blue"
	};
	private int rowHeight;
	private JComboBox colourBox;
	private JCheckBox exitConfirmCheckBox;
	private JSlider distSlider;

	private JPanel d64Panel;
	private static final String[] DiskTypes = { 
		"D64",
		"D71",
		"D81"
	};
	private JComboBox diskTypeBox;
	private JPanel diskTypePanel;

	private JPanel generalPanel;
	

	public SettingsFrame (String topText, MainPanel mainPanel_)
	{
		setTitle(topText);
		setModal(true);

		mainPanel = mainPanel_;

		cp = getContentPane();
		cp.setLayout(new BorderLayout());
		tabPane = new JTabbedPane();
		
		drawGeneralPanel();
		drawGuiPanel();
		drawD64Panel();

		tabPane.addTab("GUI", guiPanel);		
		tabPane.addTab("D64", d64Panel);		
		cp.add(tabPane, BorderLayout.NORTH);
		cp.add(generalPanel, BorderLayout.SOUTH);

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
	
	
	private void drawGeneralPanel(){
		generalPanel = new JPanel();
		generalPanel.setLayout(new BorderLayout());
		
		JPanel buttonPanel = new JPanel();
		okButton = new JButton("Ok");
		okButton.setToolTipText("Leave \"Settings\".");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton )
				{
						mainPanel.setExitQuestion(exitConfirmCheckBox.isSelected());
						mainPanel.setColourChoice(colourBox.getSelectedIndex());
						mainPanel.setRowHeight(distSlider.getValue());
						dispose();
				}
			}
		});
		
		buttonPanel.add(okButton);
		
		
		generalPanel.add(new JLabel("Todo: Store settings in a .cfg file."), BorderLayout.NORTH);
		generalPanel.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	
	private void drawGuiPanel(){
		
		guiPanel = new JPanel();
		guiPanel.setLayout(new BorderLayout());


		JPanel panel1 = new JPanel();
		panel1.setLayout(new BorderLayout());
		
		exitConfirmCheckBox = new JCheckBox("Confirm Exit");
		exitConfirmCheckBox.setToolTipText("Whether to confirm quitting the program or not.");
		exitConfirmCheckBox.setSelected(mainPanel.isExitQuestion());
		
		JPanel colourPanel = new JPanel();
		JLabel colourLabel = new JLabel("Colour:");
		colourBox = new JComboBox(Colours);
		colourBox.setToolTipText("Select a colour-sceme here.");
		colourBox.setEditable(false);
		colourBox.setSelectedIndex(mainPanel.getColourChoice());
		colourPanel.add(colourLabel);
		colourPanel.add(colourBox);
		
		JPanel distPanel = new JPanel();
		distPanel.setLayout(new BorderLayout());
		JLabel distMsgLabel = new JLabel("Grid distance:");
		rowHeight = mainPanel.getRowHeight();
		distSlider = new JSlider(JSlider.HORIZONTAL, 8, 20, rowHeight);
		distSlider.setMajorTickSpacing(2);
		distSlider.setMinorTickSpacing(1);
		distSlider.setSnapToTicks(true);
		distSlider.setPaintLabels(true);
		distSlider.setPaintTicks(true);
		distSlider.setToolTipText("Adjust grid spacing in directory window.");
		distPanel.add(distMsgLabel, BorderLayout.NORTH);
		distPanel.add(distSlider, BorderLayout.CENTER);

		panel1.add(exitConfirmCheckBox, BorderLayout.NORTH);
		panel1.add(colourPanel, BorderLayout.CENTER);
		panel1.add(distPanel, BorderLayout.SOUTH);



		guiPanel.add(panel1, BorderLayout.NORTH);		
	}
	
	private void drawD64Panel(){
		d64Panel = new JPanel();
		d64Panel.setLayout(new BorderLayout());

		JPanel diskTypePanel = new JPanel();
		JLabel diskTypeLabel = new JLabel("DiskTypes:");
		diskTypeBox = new JComboBox(DiskTypes);
		diskTypeBox.setToolTipText("Select a disktype here.");
		diskTypeBox.setEditable(false);
		diskTypeBox.setSelectedIndex(0);
		diskTypePanel.add(diskTypeLabel);
		diskTypePanel.add(diskTypeBox);
		
		d64Panel.add(diskTypePanel, BorderLayout.NORTH);
		d64Panel.add(new JLabel("Not implemented yet."), BorderLayout.SOUTH);
	}

		
	/**
	 * @return
	 */
	public int getRowHeight() {
		return rowHeight;
	}

	/**
	 * @param i
	 */
	public void setRowHeight(int i) {
		rowHeight = i;
	}

}