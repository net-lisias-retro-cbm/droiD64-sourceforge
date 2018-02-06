package GUI;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
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
import javax.swing.JScrollPane;
import javax.swing.JSlider;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

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

	private JPanel[] pluginPanel = new JPanel[MAX_PLUGINS];
	private final static int MAX_PLUGINS = 2;
	private JTextField[] pluginLabelTextField = new JTextField[MAX_PLUGINS];
	private JTextArea[] pluginCommandTextField= new JTextArea[MAX_PLUGINS];
	private JTextArea[] pluginDescriptionTextField = new JTextArea[MAX_PLUGINS];
	

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
	    drawPluginPanel();

		tabPane.addTab("GUI", guiPanel);		
		tabPane.addTab("D64", d64Panel);		
		for (int i = 0; i < MAX_PLUGINS; i++) {
			tabPane.addTab("Plugin "+(i+1), pluginPanel[i]);
		}		
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
						for (int i = 0; i < pluginCommandTextField.length; i++) {
							mainPanel.setExternalProgram(
								i,
								pluginCommandTextField[i].getText(),
								pluginDescriptionTextField[i].getText(),
								pluginLabelTextField[i].getText()
							);
						}
						dispose();
				}
			}
		});
		
		buttonPanel.add(okButton);
		
		
		generalPanel.add(new JLabel("Settings are stored in your home directory."), BorderLayout.NORTH);
		generalPanel.add(buttonPanel, BorderLayout.SOUTH);
	}
	
	
	
	private void drawGuiPanel(){
		guiPanel = new JPanel();
		guiPanel.setLayout(new GridLayout(3,1));

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
		distPanel.setLayout(new GridLayout(2,1));
		JLabel distMsgLabel = new JLabel("Grid distance:");
		rowHeight = mainPanel.getRowHeight();
		distSlider = new JSlider(JSlider.HORIZONTAL, 8, 20, rowHeight);
		distSlider.setMajorTickSpacing(2);
		distSlider.setMinorTickSpacing(1);
		distSlider.setSnapToTicks(true);
		distSlider.setPaintLabels(true);
		distSlider.setPaintTicks(true);
		distSlider.setToolTipText("Adjust grid spacing in directory window.");
		distPanel.add(distMsgLabel);
		distPanel.add(distSlider);

		guiPanel.add(exitConfirmCheckBox);
		guiPanel.add(colourPanel);
		guiPanel.add(distPanel);
	}
	
	private void drawD64Panel(){
		d64Panel = new JPanel();
		d64Panel.setLayout(new GridLayout(2,1));

		JPanel diskTypePanel = new JPanel();
		JLabel diskTypeLabel = new JLabel("DiskTypes:");
		diskTypeBox = new JComboBox(DiskTypes);
		diskTypeBox.setToolTipText("Select a disktype here.");
		diskTypeBox.setEditable(false);
		diskTypeBox.setSelectedIndex(0);
		diskTypePanel.add(diskTypeLabel);
		diskTypePanel.add(diskTypeBox);
		
		d64Panel.add(diskTypePanel);
		d64Panel.add(new JLabel("Not implemented yet."));
	}

	private void drawPluginPanel(){
		for (int i = 0; i < MAX_PLUGINS; i++) {
			pluginPanel[i] = new JPanel();
			pluginPanel[i].setLayout(new BorderLayout());
	
			JPanel pluginLabelPanel = new JPanel();
			pluginLabelPanel.setLayout(new BorderLayout());
			pluginLabelTextField[i] = new JTextField(mainPanel.getExternalProgram(i).getLabel());
			pluginLabelTextField[i].setToolTipText("Enter label here.");
			pluginLabelPanel.add(new JLabel("Label:"), BorderLayout.WEST);
			pluginLabelPanel.add(pluginLabelTextField[i], BorderLayout.CENTER);
			
			JPanel pluginCommandPanel = new JPanel();
			pluginCommandPanel.setLayout(new BorderLayout());
			pluginCommandTextField[i] = new JTextArea(4, 20);
			pluginCommandTextField[i].setLineWrap(true);
			pluginCommandTextField[i].setWrapStyleWord(true);
			pluginCommandTextField[i].setText(mainPanel.getExternalProgram(i).getCommand());
			pluginCommandTextField[i].setToolTipText("Enter a single command to execute here (no parameters allowed).");
			pluginCommandPanel.add(new JLabel("Command:"), BorderLayout.WEST);
			pluginCommandPanel.add(new JScrollPane(pluginCommandTextField[i]), BorderLayout.CENTER);
	
			JPanel pluginDescriptionPanel = new JPanel();
			pluginDescriptionPanel.setLayout(new BorderLayout());
			pluginDescriptionTextField[i] = new JTextArea(4, 20);
			pluginDescriptionTextField[i].setLineWrap(true);
			pluginDescriptionTextField[i].setWrapStyleWord(true);
			pluginDescriptionTextField[i].setText(mainPanel.getExternalProgram(i).getDescription());
			pluginDescriptionTextField[i].setToolTipText("Enter description here.");
			pluginDescriptionPanel.add(new JLabel("Description:"), BorderLayout.WEST);
			pluginDescriptionPanel.add(new JScrollPane(pluginDescriptionTextField[i]), BorderLayout.CENTER);
	
			pluginPanel[i].add(pluginLabelPanel, BorderLayout.NORTH);
			pluginPanel[i].add(pluginCommandPanel, BorderLayout.CENTER);
			pluginPanel[i].add(pluginDescriptionPanel, BorderLayout.SOUTH);
		}
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