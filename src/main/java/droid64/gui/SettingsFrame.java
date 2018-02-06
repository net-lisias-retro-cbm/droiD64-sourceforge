package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JSlider;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import droid64.db.DaoFactoryImpl;


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
 *  </pre>
 * @author wolf
 */
public class SettingsFrame extends JDialog {

	private static final long serialVersionUID = 1L;

	private MainPanel mainPanel;
	private JComboBox<Object> colourBox;
	private JCheckBox exitConfirmCheckBox;
	private JCheckBox showViewButtonsCheckBox;
	private JSlider distSlider;
	// Plugin settings
	private static final int MAX_PLUGINS = 2;
	private JTextField[] pluginLabelTextField = new JTextField[MAX_PLUGINS];
	private JTextArea[] pluginCommandTextField= new JTextArea[MAX_PLUGINS];
	private JTextArea[] pluginDescriptionTextField = new JTextArea[MAX_PLUGINS];	
	// Database settings
	private JCheckBox useJdbcCheckBox;
	private JTextField jdbcDriver;
	private JTextField jdbcUrl; 
	private JTextField jdbcUser;
	private JPasswordField jdbcPassword;
	private NumericTextField maxRows;
	/** Colors */
	private static final String[] COLORS = { "gray", "red", "green", "blue", "light-blue" };
	/** Height of one table row */
	private int rowHeight;

	private JLabel status = new JLabel();
	
	/** 
	 * Constructor
	 * @param topText String
	 * @param mainPanel MainPanel
	 */
	public SettingsFrame (String topText, MainPanel mainPanel) {
		setTitle(topText);
		setModal(true);

		this.mainPanel = mainPanel;

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("GUI",      drawGuiPanel());	
		tabPane.addTab("Database", drawDatabasePanel());

		JPanel generalPanel = drawGeneralPanel();
	    JPanel pluginPanel[] = drawPluginPanel();
		
		for (int i = 0; i < MAX_PLUGINS; i++) {
			tabPane.addTab("Plugin "+(i+1), pluginPanel[i]);
		}
		cp.add(tabPane, BorderLayout.CENTER);
		cp.add(generalPanel, BorderLayout.SOUTH);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation( (int)((dim.width - getSize().getWidth()) / 3),	(int)((dim.height - getSize().getHeight()) / 3)	);
		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Setup up general settings panel
	 * @return JPanel
	 */
	private JPanel drawGeneralPanel() {
		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		final JButton okButton = new JButton("OK");
		okButton.setToolTipText("Leave 'Settings'.");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton ) {
					mainPanel.setExitQuestion(exitConfirmCheckBox.isSelected());
					mainPanel.setShowViewButtons(showViewButtonsCheckBox.isSelected());
					mainPanel.setColourChoice(colourBox.getSelectedIndex());
					mainPanel.setRowHeight(distSlider.getValue());
					mainPanel.setUseDatabase(useJdbcCheckBox.isSelected());
					mainPanel.setJdbcDriver(jdbcDriver.getText());
					mainPanel.setJdbcUrl(jdbcUrl.getText());
					mainPanel.setJdbcUser(jdbcUser.getText());
					mainPanel.setJdbcPassword(new String(jdbcPassword.getPassword()));
					try {
						mainPanel.setMaxRows(maxRows.getLongValue());
					} catch (ParseException e) {
						System.out.println(e);
						mainPanel.setMaxRows(25L);
						maxRows.setValue(25L);
					}
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
		JPanel txtPanel = new JPanel();
		txtPanel.setAlignmentX(CENTER_ALIGNMENT);
		txtPanel.add(new JLabel("Settings are stored in your home directory."));
		generalPanel.add(txtPanel, BorderLayout.NORTH);
		generalPanel.add(buttonPanel, BorderLayout.SOUTH);
		return generalPanel;
	}
	
	/**
	 * Setup panel with GUI settings
	 * @return JPanel
	 */
	private JPanel drawGuiPanel() {
		JPanel guiPanel = new JPanel();
				
		GridBagConstraints gbc = new GridBagConstraints();		
		guiPanel.setLayout(new GridBagLayout());				
		gbc.fill = GridBagConstraints.HORIZONTAL;

		exitConfirmCheckBox = new JCheckBox("Confirm Exit");
		exitConfirmCheckBox.setToolTipText("Whether to confirm quitting the program or not.");
		exitConfirmCheckBox.setSelected(mainPanel.isExitQuestion());

		showViewButtonsCheckBox = new JCheckBox("Show view buttons");
		showViewButtonsCheckBox.setToolTipText("Show view button row.");
		showViewButtonsCheckBox.setSelected(mainPanel.isShowViewButtons());
		
		
		colourBox = new JComboBox<Object>(COLORS);
		colourBox.setToolTipText("Select a colour scheme.");
		colourBox.setEditable(false);
		colourBox.setSelectedIndex(mainPanel.getColourChoice()<COLORS.length ? mainPanel.getColourChoice() : 0);
		
		rowHeight = mainPanel.getRowHeight();
		distSlider = new JSlider(JSlider.HORIZONTAL, 8, 20, rowHeight);
		distSlider.setMajorTickSpacing(2);
		distSlider.setMinorTickSpacing(1);
		distSlider.setSnapToTicks(true);
		distSlider.setPaintLabels(true);
		distSlider.setPaintTicks(true);
		distSlider.setToolTipText("Adjust grid spacing in directory window.");

		final JTextField defaultImgDir = new JTextField(mainPanel.getDefaultImageDir()!=null ? mainPanel.getDefaultImageDir() : "");
		final JButton defaultImgButton = new JButton("..");
		
		defaultImgButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==defaultImgButton ) {
					String choosen = openImgDirDialog(defaultImgDir.getText());
					if (choosen != null) {
						defaultImgDir.setText(choosen);
						mainPanel.setDefaultImageDir(choosen);
					}
				}
			}
		});		
		final JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(mainPanel.getFontSize(), 8, 72, 1));
		fontSizeSpinner.setToolTipText("Adjust font size.");
		fontSizeSpinner.addChangeListener(new ChangeListener() {
	          public void stateChanged(ChangeEvent event) {
	        	  if (event.getSource() == fontSizeSpinner) {
	        		  try {
	        			  int fs = Integer.parseInt(fontSizeSpinner.getValue().toString());
	        			  mainPanel.setFontSize(fs);
	        		  } catch (NumberFormatException e) {
	        		  }
	        	  }
	          }
	       });
		JPanel imgDirPanel = new JPanel();
		imgDirPanel.setLayout(new BorderLayout());
		imgDirPanel.add(defaultImgDir, BorderLayout.CENTER);
		imgDirPanel.add(defaultImgButton, BorderLayout.EAST);
		
		addToGridBag(0, 0, 0.0, 0.0, gbc, guiPanel, new JPanel());
		addToGridBag(1, 0, 0.5, 0.0, gbc, guiPanel, exitConfirmCheckBox);
		addToGridBag(0, 1, 0.0, 0.0, gbc, guiPanel, new JPanel());
		addToGridBag(1, 1, 0.5, 0.0, gbc, guiPanel, showViewButtonsCheckBox);
		addToGridBag(0, 2, 0.0, 0.0, gbc, guiPanel, new JLabel("Color:"));
		addToGridBag(1, 2, 0.0, 0.0, gbc, guiPanel, colourBox);
		addToGridBag(2, 2, 0.5, 0.0, gbc, guiPanel, new JPanel());
		addToGridBag(0, 3, 0.0, 0.0, gbc, guiPanel, new JLabel("Grid distance:"));		
		addToGridBag(1, 3, 0.0, 0.0, gbc, guiPanel, distSlider);
		addToGridBag(0, 4, 0.0, 0.0, gbc, guiPanel, new JLabel("Default image dir:"));		
		addToGridBag(1, 4, 0.5, 0.0, gbc, guiPanel, imgDirPanel);
		addToGridBag(0, 5, 0.0, 0.0, gbc, guiPanel, new JLabel("Font size:"));		
		addToGridBag(1, 5, 0.0, 0.0, gbc, guiPanel, fontSizeSpinner);
		addToGridBag(2, 5, 0.5, 0.0, gbc, guiPanel, new JPanel());
		addToGridBag(0, 6, 0.5, 0.8, gbc, guiPanel, new JPanel());
		return guiPanel;
	}

	/**
	 * Create the panel with database settings.
	 * @return JPanel
	 */
	private JPanel drawDatabasePanel() {
		JPanel dbPanel = new JPanel();		
		GridBagConstraints gbc = new GridBagConstraints();		
		dbPanel.setLayout(new GridBagLayout());		
		gbc.fill = GridBagConstraints.HORIZONTAL;

		useJdbcCheckBox = new JCheckBox("Use database");
		useJdbcCheckBox.setSelected(mainPanel.isUseDatabase());
		jdbcDriver = new JTextField(mainPanel.getJdbcDriver());
		jdbcUrl = new JTextField(mainPanel.getJdbcUrl());
		jdbcUser = new JTextField(mainPanel.getJdbcUser());
		jdbcPassword = new JPasswordField(mainPanel.getJdbcPassword());
		final JButton testConnectionButton = new JButton("Test connection");
		status.setFont(new Font("Verdana", Font.PLAIN, status.getFont().getSize()));
		
		DecimalFormat format = new DecimalFormat("###");
	    format.setGroupingUsed(true);
	    format.setGroupingSize(3);
	    format.setParseIntegerOnly(false);
		maxRows = new NumericTextField("10", 10, format);
		
		useJdbcCheckBox.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == useJdbcCheckBox) {
					boolean enabled = useJdbcCheckBox.isSelected();
					jdbcDriver.setEnabled(enabled);
					jdbcUrl.setEnabled(enabled);
					jdbcUser.setEnabled(enabled);
					jdbcPassword.setEnabled(enabled);
					maxRows.setEnabled(enabled);
					testConnectionButton.setEnabled(enabled);
				}
			}
		});
		
		testConnectionButton.setToolTipText("Test the JDBC connection.");
		testConnectionButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == testConnectionButton) {
					String errStr = DaoFactoryImpl.testConnection(jdbcDriver.getText(), jdbcUrl.getText(), jdbcUser.getText(), new String(jdbcPassword.getPassword()));
					status.setText(errStr == null ? "OK" : errStr);
				}
			}
		});
		
		boolean jdbcEnabled = useJdbcCheckBox.isSelected();
		jdbcDriver.setEnabled(jdbcEnabled);
		jdbcUrl.setEnabled(jdbcEnabled);
		jdbcUser.setEnabled(jdbcEnabled);
		jdbcPassword.setEnabled(jdbcEnabled);
		maxRows.setEnabled(jdbcEnabled);
		testConnectionButton.setEnabled(jdbcEnabled);
			
		String sqlString = 
				"\nThe database feature requires access to a MySQL database and " +
				"the MySQL ConnectJ (http://dev.mysql.com/downloads/connector/j/) " +
				"in the class path. \n" +
				"The setup_database.sql script to prepare the database tables is included in the DroiD64 jar file.";
		
		final JTextArea messageTextArea = new JTextArea(10,45);

		messageTextArea.setBackground(new Color(230,230,230));
		messageTextArea.setEditable(false);
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setText(sqlString);
		messageTextArea.setCaretPosition(0);
		
		addToGridBag(0, 0, 0.0, 0.0, gbc, dbPanel, new JPanel());
		addToGridBag(1, 0, 0.5, 0.0, gbc, dbPanel, useJdbcCheckBox);
		addToGridBag(0, 1, 0.0, 0.0, gbc, dbPanel, new JLabel("JDBC driver class:"));
		addToGridBag(1, 1, 0.5, 0.0, gbc, dbPanel, jdbcDriver);
		addToGridBag(0, 2, 0.0, 0.0, gbc, dbPanel, new JLabel("Connection URL:"));
		addToGridBag(1, 2, 0.5, 0.0, gbc, dbPanel, jdbcUrl);
		addToGridBag(0, 3, 0.0, 0.0, gbc, dbPanel, new JLabel("User:"));
		addToGridBag(1, 3, 0.5, 0.0, gbc, dbPanel, jdbcUser);
		addToGridBag(0, 4, 0.0, 0.0, gbc, dbPanel, new JLabel("Password:"));
		addToGridBag(1, 4, 0.5, 0.0, gbc, dbPanel, jdbcPassword);
		addToGridBag(0, 5, 0.0, 0.0, gbc, dbPanel, new JLabel("Max rows:"));
		addToGridBag(1, 5, 0.5, 0.0, gbc, dbPanel, maxRows);
		addToGridBag(0, 6, 0.0, 0.0, gbc, dbPanel, new JPanel());
		addToGridBag(1, 6, 0.5, 0.0, gbc, dbPanel, testConnectionButton);
		addToGridBag(0, 7, 0.0, 0.0, gbc, dbPanel, new JLabel("Status:"));
		addToGridBag(1, 7, 0.5, 0.0, gbc, dbPanel, status);
		
		addToGridBag(1, 8, 0.5, 0.9, gbc, dbPanel, messageTextArea);

		return dbPanel;
	}
		
	/**
	 * Setup MAX_PLUGINS of panels for plugins.
	 * @return JPanel[]
	 */
	private JPanel[] drawPluginPanel(){
		JPanel pluginPanel[] = new JPanel[MAX_PLUGINS];
		for (int i = 0; i < MAX_PLUGINS; i++) {
			pluginPanel[i] = new JPanel();
	
			GridBagConstraints gbc = new GridBagConstraints();		
			pluginPanel[i].setLayout(new GridBagLayout());				
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			pluginLabelTextField[i] = new JTextField(mainPanel.getExternalProgram(i).getLabel());
			pluginLabelTextField[i].setToolTipText("Enter label here.");
			pluginLabelTextField[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			pluginCommandTextField[i] = new JTextArea(4, 20);
			pluginCommandTextField[i].setLineWrap(true);
			pluginCommandTextField[i].setWrapStyleWord(true);
			pluginCommandTextField[i].setText(mainPanel.getExternalProgram(i).getCommand());
			pluginCommandTextField[i].setToolTipText("Enter a single command to execute here (no parameters allowed).");
			pluginCommandTextField[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));

			pluginDescriptionTextField[i] = new JTextArea(4, 20);
			pluginDescriptionTextField[i].setLineWrap(true);
			pluginDescriptionTextField[i].setWrapStyleWord(true);
			pluginDescriptionTextField[i].setText(mainPanel.getExternalProgram(i).getDescription());
			pluginDescriptionTextField[i].setToolTipText("Enter description here.");
			pluginDescriptionTextField[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));

			addToGridBag(0, 0, 0.0, 0.0, gbc, pluginPanel[i], new JLabel("Label:"));
			addToGridBag(1, 0, 0.5, 0.0, gbc, pluginPanel[i], pluginLabelTextField[i]);			
			addToGridBag(0, 1, 0.0, 0.0, gbc, pluginPanel[i], new JLabel("Command:"));
			addToGridBag(1, 1, 0.5, 0.0, gbc, pluginPanel[i], pluginCommandTextField[i]);			
			addToGridBag(0, 2, 0.0, 0.0, gbc, pluginPanel[i], new JLabel("Description:"));
			addToGridBag(1, 2, 0.5, 0.0, gbc, pluginPanel[i], pluginDescriptionTextField[i]);
			
			addToGridBag(0, 3, 0.5, 0.9, gbc, pluginPanel[i], new JPanel());

		}
		return pluginPanel;
	}

	
	/**
	 * Open a directory browser dialog.
	 * @param directory String with default directory
	 * @return String with selected directory, or null if nothing was selected.
	 */
	private String openImgDirDialog(String directory) {
		JFileChooser chooser = new JFileChooser(directory);
	    chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
			return chooser.getSelectedFile()+"";
		}
		return null;
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
	
		
	/**
	 * @return row height as int
	 */
	public int getRowHeight() {
		return rowHeight;
	}

	/**
	 * @param rowHeight int
	 */
	public void setRowHeight(int rowHeight) {
		this.rowHeight = rowHeight;
	}

	public void setJdbcPassword(String password) {
		jdbcPassword.setText(password);
	}
	public String getJdbcPassword() {
		return new String(jdbcPassword.getPassword());
	}
	public void setJdbcUser(String user) {
		jdbcUser.setText(user);
	}
	public String getJdbcUser() {
		return jdbcUser.getText();
	}
	public void setJdbcUrl(String url) {
		jdbcUrl.setText(url);
	}
	public String getJdbcUrl() {
		return jdbcUrl.getText();
	}
	public void setJdbcDriver(String className) {
		jdbcDriver.setText(className);
	}
	public String getJdbcDriver() {
		return jdbcDriver.getText();
	}
	public void setUseDatabase(boolean useDatabase) {
		useJdbcCheckBox.setEnabled(useDatabase);
	}
	public boolean isUseDatabase() {
		return useJdbcCheckBox.isEnabled();
	}
	
	public void setMaxRows(long maxRows) {
		this.maxRows.setValue(maxRows>1 ? maxRows : 1);
	}
	
	public long getMaxRows() {		
		try {
			Long value = maxRows.getLongValue();
			return value != null ? value.longValue() : 25L;
		} catch (ParseException e) {
			status.setText(e.getMessage());
			return 25L;
		}
	}
	
}
