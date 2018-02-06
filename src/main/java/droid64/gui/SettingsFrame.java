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
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DecimalFormat;
import java.text.ParseException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
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

	private JComboBox<Object> colourBox;
	private JCheckBox exitConfirmCheckBox;
	private JSlider distSlider;
	private JSlider distSlider2;
	// Plugin settings
	private JTextField[] pluginLabelTextField = new JTextField[Settings.MAX_PLUGINS];
	private JTextArea[] pluginCommandTextField= new JTextArea[Settings.MAX_PLUGINS];
	private JTextArea[] pluginDescriptionTextField = new JTextArea[Settings.MAX_PLUGINS];	
	// Database settings
	private JCheckBox useJdbcCheckBox;
	private JTextField jdbcDriver;
	private JTextField jdbcUrl; 
	private JTextField jdbcUser;
	private JPasswordField jdbcPassword;
	private NumericTextField maxRows;
	private JComboBox<Object> lookAndFeelBox;
	/** Colors */
	private static final String[] COLORS = { "gray", "red", "green", "blue", "light-blue" };

	private JLabel status = new JLabel();
	
	private final static String JDBC_POSTGRESQL_URL = "https://jdbc.postgresql.org/";
	private final static String JDBC_MYSQL_URL = "http://dev.mysql.com/downloads/connector/j";
	
	
	private static String SQL = null;
	static {
		InputStream in = SettingsFrame.class.getResourceAsStream("/setup_database.sql");
		BufferedReader input = new BufferedReader(new InputStreamReader(in));
		StringBuffer buf = new StringBuffer();		
		try {
			for (String line = input.readLine(); line != null; line = input.readLine()) {
	            buf.append(line);
	            buf.append("\n");
			}
			SQL = buf.toString();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private final JDialog settingsFrame = this;
	
	/** 
	 * Constructor
	 * @param topText String
	 * @param mainPanel MainPanel
	 */
	public SettingsFrame (String topText, MainPanel mainPanel) {
		setTitle(topText);
		setModal(true);		

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab("GUI",      drawGuiPanel());	
		tabPane.addTab("Colors", drawColorPanel());

		tabPane.addTab("Database", drawDatabasePanel());

	    JPanel pluginPanel[] = drawPluginPanel();
		for (int i = 0; i < Settings.MAX_PLUGINS; i++) {
			tabPane.addTab("Plugin "+(i+1), pluginPanel[i]);
		}
		cp.add(tabPane, BorderLayout.CENTER);

		JPanel generalPanel = drawGeneralPanel(mainPanel);
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
	private JPanel drawGeneralPanel(final MainPanel mainPanel) {
		JPanel generalPanel = new JPanel();
		generalPanel.setLayout(new BorderLayout());
		JPanel buttonPanel = new JPanel();
		final JButton okButton = new JButton("OK");
		okButton.setToolTipText("Leave 'Settings'.");
		okButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==okButton ) {
					Settings.setAskQuit(exitConfirmCheckBox.isSelected());
					Settings.setColourChoice(colourBox.getSelectedIndex());
					Settings.setRowHeight(distSlider.getValue());
					Settings.setLookAndFeel(lookAndFeelBox.getSelectedIndex());
					Settings.setLocalRowHeight(distSlider2.getValue());
					Settings.setUseDb(useJdbcCheckBox.isSelected());
					Settings.setJdbcDriver(jdbcDriver.getText());
					Settings.setJdbcUrl(jdbcUrl.getText());
					Settings.setJdbcUser(jdbcUser.getText());
					Settings.setJdbcPassword(new String(jdbcPassword.getPassword()));
					try {
						Settings.setMaxRows(maxRows.getLongValue());
					} catch (ParseException e) {
						maxRows.setValue(25L);
					}
					for (int i = 0; i < pluginCommandTextField.length; i++) {
						mainPanel.setPluginButtonLabel(i, pluginLabelTextField[i].getText());
						Settings.setExternalProgram(i,
							pluginCommandTextField[i].getText(),
							pluginDescriptionTextField[i].getText(),
							pluginLabelTextField[i].getText()
						);
					}
					Settings.parseExternalPrograms();
					Settings.saveSettingsToFile();
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
		exitConfirmCheckBox.setSelected(Settings.getAskQuit());

		lookAndFeelBox = new JComboBox<Object>(MainPanel.getLookAndFeelNames());
		lookAndFeelBox.setToolTipText("Select look and feel.");
		lookAndFeelBox.setEditable(false);
		lookAndFeelBox.setSelectedIndex(0);
		lookAndFeelBox.setSelectedIndex(Settings.getLookAndFeel() < MainPanel.getLookAndFeelNames().length ? Settings.getLookAndFeel() : 0);
		
		distSlider = new JSlider(JSlider.HORIZONTAL, 8, 20, Settings.getRowHeight());
		distSlider.setMajorTickSpacing(2);
		distSlider.setMinorTickSpacing(1);
		distSlider.setSnapToTicks(true);
		distSlider.setPaintLabels(true);
		distSlider.setPaintTicks(true);
		distSlider.setToolTipText("Adjust grid spacing in directory window.");

		distSlider2 = new JSlider(JSlider.HORIZONTAL, 8, 20, Settings.getLocalRowHeight());
		distSlider2.setMajorTickSpacing(2);
		distSlider2.setMinorTickSpacing(1);
		distSlider2.setSnapToTicks(true);
		distSlider2.setPaintLabels(true);
		distSlider2.setPaintTicks(true);
		distSlider2.setToolTipText("Adjust grid spacing in directory window.");
		
		final JTextField defaultImgDir = new JTextField(Settings.getDefaultImageDir());
		final JButton defaultImgButton = new JButton("..");
		
		defaultImgButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==defaultImgButton ) {
					String choosen = openImgDirDialog(defaultImgDir.getText());
					if (choosen != null) {
						defaultImgDir.setText(choosen);
						Settings.setDefaultImageDir(choosen);
					}
				}
			}
		});
		
		final JTextField defaultImgDir2 = new JTextField(Settings.getDefaultImageDir2());
		final JButton defaultImgButton2 = new JButton("..");
		
		defaultImgButton2.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==defaultImgButton2 ) {
					String choosen = openImgDirDialog(defaultImgDir2.getText());
					if (choosen != null) {
						defaultImgDir2.setText(choosen);
						Settings.setDefaultImageDir2(choosen);
					}
				}
			}
		});	
		
		final JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(Settings.getFontSize(), 8, 72, 1));
		fontSizeSpinner.setToolTipText("Adjust font size.");
		fontSizeSpinner.addChangeListener(new ChangeListener() {
	          public void stateChanged(ChangeEvent event) {
	        	  if (event.getSource() == fontSizeSpinner) {
	        		  try {
	        			  int fs = Integer.parseInt(fontSizeSpinner.getValue().toString());
	        			  Settings.setFontSize(fs);
	        		  } catch (NumberFormatException e) {
	        		  }
	        	  }
	          }
	       });
		
		final JSpinner localFontSizeSpinner = new JSpinner(new SpinnerNumberModel(Settings.getLocalFontSize(), 8, 72, 1));
		localFontSizeSpinner.setToolTipText("Adjust font size.");
		localFontSizeSpinner.addChangeListener(new ChangeListener() {
	          public void stateChanged(ChangeEvent event) {
	        	  if (event.getSource() == localFontSizeSpinner) {
	        		  try {
	        			  int fs = Integer.parseInt(localFontSizeSpinner.getValue().toString());
	        			  Settings.setLocalFontSize(fs);
	        		  } catch (NumberFormatException e) {
	        		  }
	        	  }
	          }
	       });		
		
		
		JPanel imgDirPanel = new JPanel();
		imgDirPanel.setLayout(new BorderLayout());
		imgDirPanel.add(defaultImgDir, BorderLayout.CENTER);
		imgDirPanel.add(defaultImgButton, BorderLayout.EAST);

		JPanel imgDirPanel2 = new JPanel();
		imgDirPanel2.setLayout(new BorderLayout());
		imgDirPanel2.add(defaultImgDir2, BorderLayout.CENTER);
		imgDirPanel2.add(defaultImgButton2, BorderLayout.EAST);
		
		addToGridBag(0, 0, 0.0, 0.0, gbc, guiPanel, new JPanel());
		addToGridBag(1, 0, 0.5, 0.0, gbc, guiPanel, exitConfirmCheckBox);
		addToGridBag(2, 0, 0.0, 0.0, gbc, guiPanel, new JPanel());
		
		addToGridBag(0, 1, 0.0, 0.0, gbc, guiPanel, new JLabel("Look & feel:"));
		addToGridBag(1, 1, 0.5, 0.0, gbc, guiPanel, lookAndFeelBox);
		addToGridBag(2, 1, 0.0, 0.0, gbc, guiPanel, new JPanel());
		
		addToGridBag(0, 2, 0.0, 0.0, gbc, guiPanel, new JLabel("Disk Image Grid distance:"));		
		addToGridBag(1, 2, 0.0, 0.0, gbc, guiPanel, distSlider);
		addToGridBag(2, 2, 0.0, 0.0, gbc, guiPanel, new JPanel());

		addToGridBag(0, 3, 0.0, 0.0, gbc, guiPanel, new JLabel("Local Files Grid distance:"));		
		addToGridBag(1, 3, 0.0, 0.0, gbc, guiPanel, distSlider2);
		addToGridBag(2, 3, 0.0, 0.0, gbc, guiPanel, new JPanel());
		
		addToGridBag(0, 4, 0.0, 0.0, gbc, guiPanel, new JLabel("Left default image dir:"));		
		addToGridBag(1, 4, 1.0, 0.0, gbc, guiPanel, imgDirPanel);
		addToGridBag(2, 4, 0.0, 0.0, gbc, guiPanel, new JPanel());

		addToGridBag(0, 5, 0.0, 0.0, gbc, guiPanel, new JLabel("Right default image dir:"));		
		addToGridBag(1, 5, 1.0, 0.0, gbc, guiPanel, imgDirPanel2);
		addToGridBag(2, 5, 0.0, 0.0, gbc, guiPanel, new JPanel());
		
		JPanel fontSizePanel1 = new JPanel();
		fontSizePanel1.setLayout(new BorderLayout());
		fontSizePanel1.add(fontSizeSpinner, BorderLayout.WEST);
		fontSizePanel1.add(new JPanel(), BorderLayout.CENTER);
				
		addToGridBag(0, 6, 0.0, 0.0, gbc, guiPanel, new JLabel("Disk Image Font size:"));		
		addToGridBag(1, 6, 0.0, 0.0, gbc, guiPanel, fontSizePanel1);
		addToGridBag(2, 6, 1.0, 0.0, gbc, guiPanel, new JPanel());

		JPanel fontSizePanel2 = new JPanel();
		fontSizePanel2.setLayout(new BorderLayout());
		fontSizePanel2.add(localFontSizeSpinner, BorderLayout.WEST);
		fontSizePanel2.add(new JPanel(), BorderLayout.CENTER);
		
		addToGridBag(0, 7, 0.0, 0.0, gbc, guiPanel, new JLabel("Local Files Font size:"));		
		addToGridBag(1, 7, 0.0, 0.0, gbc, guiPanel, fontSizePanel2);
		addToGridBag(2, 7, 1.0, 0.0, gbc, guiPanel, new JPanel());
		
		addToGridBag(0, 8, 0.5, 0.8, gbc, guiPanel, new JPanel());
		return guiPanel;
	}

	
	
	private void setupColorButton(final JButton fgButton, final JButton bgButton, final String fg, final String bg) {	
		fgButton.setForeground(Settings.getColorParam(fg));
		fgButton.setBackground(Settings.getColorParam(bg));
		fgButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource()==fgButton ) {
					Color c = JColorChooser.showDialog(fgButton, "Foreground", fgButton.getForeground());		
					fgButton.setForeground(c);
					bgButton.setForeground(c);
					Settings.setColorParam(fg, fgButton.getForeground());
					Settings.setColorParam(bg, bgButton.getBackground());					
				}
			}
		});
		bgButton.setForeground(Settings.getColorParam(fg));
		bgButton.setBackground(Settings.getColorParam(bg));	
		bgButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource()==bgButton ) {
					Color c = JColorChooser.showDialog(bgButton, "Background", bgButton.getBackground());		
					fgButton.setBackground(c);
					bgButton.setBackground(c);
					Settings.setColorParam(fg, fgButton.getForeground());
					Settings.setColorParam(bg, bgButton.getBackground());	
				}
			}
		});
	}
	
	/**
	 * Setup Color settings
	 * @return JPanel
	 */
	private JPanel drawColorPanel() {
		
		colourBox = new JComboBox<Object>(COLORS);
		colourBox.setToolTipText("Select a colour scheme.");
		colourBox.setEditable(false);
		colourBox.setSelectedIndex(Settings.getColourChoice()<COLORS.length ? Settings.getColourChoice() : 0);
		
		final JButton colorFgButton = new JButton("Foreground");
		final JButton colorBgButton = new JButton("Background");
		setupColorButton(colorFgButton, colorBgButton, Settings.SETTING_DIR_FG, Settings.SETTING_DIR_BG);
		
		final JButton colorCpmFgButton = new JButton("Foreground");
		final JButton colorCpmBgButton = new JButton("Background");
		setupColorButton(colorCpmFgButton, colorCpmBgButton, Settings.SETTING_DIR_CPM_FG, Settings.SETTING_DIR_CPM_BG);
		
		final JButton colorLocalFgButton = new JButton("Foreground");
		final JButton colorLocalBgButton = new JButton("Background");
		setupColorButton(colorLocalFgButton, colorLocalBgButton, Settings.SETTING_DIR_LOCAL_FG, Settings.SETTING_DIR_LOCAL_BG);
		
		final JButton colorActiveBorderButton = new JButton("Active border");
		colorActiveBorderButton.setBorder(BorderFactory.createLineBorder(Settings.getActiveBorderColor(), 3));
		colorActiveBorderButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource()==colorActiveBorderButton ) {
					Color c = JColorChooser.showDialog(colorActiveBorderButton, "Foreground", Settings.getActiveBorderColor());		
					colorActiveBorderButton.setBorder(BorderFactory.createLineBorder(c));
					Settings.setActiveBorderColor(c);
				}
			}
		});
		final JButton colorInactiveBorderButton = new JButton("Inactive border");
		colorInactiveBorderButton.setBorder(BorderFactory.createLineBorder(Settings.getInactiveBorderColor(), 3));
		colorInactiveBorderButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource()==colorInactiveBorderButton ) {
					Color c = JColorChooser.showDialog(colorInactiveBorderButton, "Background", Settings.getInactiveBorderColor());		
					colorInactiveBorderButton.setBorder(BorderFactory.createLineBorder(c));
					Settings.setInactiveBorderColor(c);
				}
			}
		});

		final JButton colorResetButton = new JButton("Reset");
		colorResetButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource()==colorResetButton ) {
					colorFgButton.setForeground(Settings.DIR_FG_COLOR_C64);
					colorFgButton.setBackground(Settings.DIR_BG_COLOR_C64);
					colorBgButton.setForeground(Settings.DIR_FG_COLOR_C64);
					colorBgButton.setBackground(Settings.DIR_BG_COLOR_C64);
					colorCpmFgButton.setForeground(Settings.DIR_FG_COLOR_CPM);
					colorCpmFgButton.setBackground(Settings.DIR_BG_COLOR_CPM);
					colorCpmBgButton.setForeground(Settings.DIR_FG_COLOR_CPM);
					colorCpmBgButton.setBackground(Settings.DIR_BG_COLOR_CPM);
					colorLocalFgButton.setForeground(Settings.DIR_FG_COLOR_LOCAL);
					colorLocalFgButton.setBackground(Settings.DIR_BG_COLOR_LOCAL);
					colorLocalBgButton.setForeground(Settings.DIR_FG_COLOR_LOCAL);
					colorLocalBgButton.setBackground(Settings.DIR_BG_COLOR_LOCAL);					
					
					colorActiveBorderButton.setBorder(BorderFactory.createLineBorder(Settings.ACTIVE_BORDER_COLOR));
					colorInactiveBorderButton.setBorder(BorderFactory.createLineBorder(Settings.INACTIVE_BORDER_COLOR));

					Settings.setDirColors(colorBgButton.getBackground(), colorFgButton.getForeground());
					Settings.setDirCpmColors(colorCpmBgButton.getBackground(), colorCpmFgButton.getForeground());
					Settings.setDirLocalColors(colorLocalBgButton.getBackground(), colorLocalFgButton.getForeground());
					Settings.setActiveBorderColor(Settings.ACTIVE_BORDER_COLOR);
					Settings.setInactiveBorderColor(Settings.INACTIVE_BORDER_COLOR);
				}
			}
		});

		JPanel colorPanel = new JPanel();
		
		GridBagConstraints gbc = new GridBagConstraints();		
		colorPanel.setLayout(new GridBagLayout());		
		gbc.fill = GridBagConstraints.HORIZONTAL;
		
		addToGridBag(0, 0, 0.0, 0.0, gbc, colorPanel, new JLabel("Color theme:"));
		addToGridBag(1, 0, 0.0, 0.0, gbc, colorPanel, colourBox);
		addToGridBag(2, 0, 0.5, 0.0, gbc, colorPanel, new JPanel());
		addToGridBag(3, 0, 1.0, 0.0, gbc, colorPanel, new JPanel());
		
		addToGridBag(0, 1, 0.0, 0.0, gbc, colorPanel, new JLabel(""));
		addToGridBag(1, 1, 0.0, 0.0, gbc, colorPanel, colorResetButton);
		addToGridBag(2, 1, 0.0, 0.0, gbc, colorPanel, new JPanel());
		addToGridBag(3, 1, 1.0, 0.0, gbc, colorPanel, new JPanel());
		
		addToGridBag(0, 2, 0.0, 0.0, gbc, colorPanel, new JLabel("Commodore"));
		addToGridBag(1, 2, 0.0, 0.0, gbc, colorPanel, colorFgButton);
		addToGridBag(2, 2, 0.0, 0.0, gbc, colorPanel, colorBgButton);
		addToGridBag(3, 2, 1.0, 0.0, gbc, colorPanel, new JPanel());

		addToGridBag(0, 3, 0.0, 0.0, gbc, colorPanel, new JLabel("CP/M"));
		addToGridBag(1, 3, 0.0, 0.0, gbc, colorPanel, colorCpmFgButton);
		addToGridBag(2, 3, 0.0, 0.0, gbc, colorPanel, colorCpmBgButton);
		addToGridBag(3, 3, 1.0, 0.0, gbc, colorPanel, new JPanel());
	
		addToGridBag(0, 4, 0.0, 0.0, gbc, colorPanel, new JLabel("File system"));
		addToGridBag(1, 4, 0.0, 0.0, gbc, colorPanel, colorLocalFgButton);
		addToGridBag(2, 4, 0.0, 0.0, gbc, colorPanel, colorLocalBgButton);
		addToGridBag(3, 4, 1.0, 0.0, gbc, colorPanel, new JPanel());
		
		addToGridBag(0, 5, 0.0, 0.0, gbc, colorPanel, new JLabel("Border"));
		addToGridBag(1, 5, 0.0, 0.0, gbc, colorPanel, colorActiveBorderButton);
		addToGridBag(2, 5, 0.0, 0.0, gbc, colorPanel, colorInactiveBorderButton);
		addToGridBag(3, 5, 1.0, 0.0, gbc, colorPanel, new JPanel());
		
		addToGridBag(0, 6, 0.5, 0.8, gbc, colorPanel, new JPanel());
		return colorPanel;
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
		useJdbcCheckBox.setSelected(Settings.getUseDb());
		jdbcDriver = new JTextField(Settings.getJdbcDriver());
		jdbcUrl = new JTextField(Settings.getJdbcUrl());
		jdbcUser = new JTextField(Settings.getJdbcUser());
		jdbcPassword = new JPasswordField(Settings.getJdbcPassword());
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
				"\nThe database feature requires access to a SQL database and a JDBC driver for the database in the class path.\n" +
				"MySQL ConnectJ ("+ JDBC_MYSQL_URL +") and PostgreSQL (" + JDBC_POSTGRESQL_URL + ") has been fouind working with DroiD64.\n" +
				"The setup_database.sql script to prepare the database tables is included in the DroiD64 jar file.";
		
		final JTextArea messageTextArea = new JTextArea(10,45);

		final JButton viewSqlButton = new JButton("Database SQL");
		viewSqlButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == viewSqlButton) {
					new TextViewFrame(settingsFrame, "DroiD64","SQL database setup", SQL, true);
				}
			}
		});
		
		messageTextArea.setBackground(new Color(230,230,230));
		messageTextArea.setEditable(false);
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setText(sqlString);
		messageTextArea.setCaretPosition(0);
		
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(viewSqlButton);
		buttonPanel.add(testConnectionButton);
		buttonPanel.add(new JPanel());
		
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
		addToGridBag(1, 6, 0.5, 0.0, gbc, dbPanel, buttonPanel);
		addToGridBag(0, 7, 0.0, 0.0, gbc, dbPanel, new JLabel("Status:"));
		addToGridBag(1, 7, 0.5, 0.0, gbc, dbPanel, status);
		addToGridBag(0, 8, 0.0, 0.0, gbc, dbPanel, new JPanel());
		
		gbc.fill = GridBagConstraints.BOTH;

		addToGridBag(1, 8, 0.5, 0.9, gbc, dbPanel, new JScrollPane(messageTextArea));

		return dbPanel;
	}
		
	/**
	 * Setup MAX_PLUGINS of panels for plugins.
	 * @return JPanel[]
	 */
	private JPanel[] drawPluginPanel(){
		JPanel pluginPanel[] = new JPanel[Settings.MAX_PLUGINS];
		
		for (int i = 0; i < Settings.MAX_PLUGINS; i++) {
			pluginPanel[i] = new JPanel();
	
			GridBagConstraints gbc = new GridBagConstraints();		
			pluginPanel[i].setLayout(new GridBagLayout());				
			gbc.fill = GridBagConstraints.HORIZONTAL;
			
			pluginLabelTextField[i] = new JTextField();
			pluginLabelTextField[i].setToolTipText("Enter label here.");
			pluginLabelTextField[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
						
			pluginCommandTextField[i] = new JTextArea(4, 20);
			pluginCommandTextField[i].setLineWrap(true);
			pluginCommandTextField[i].setWrapStyleWord(true);
			pluginCommandTextField[i].setToolTipText("Enter a single command to execute here (no parameters allowed).");
			pluginCommandTextField[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));

			pluginDescriptionTextField[i] = new JTextArea(4, 20);
			pluginDescriptionTextField[i].setLineWrap(true);
			pluginDescriptionTextField[i].setWrapStyleWord(true);
			pluginDescriptionTextField[i].setToolTipText("Enter description here.");
			pluginDescriptionTextField[i].setBorder(BorderFactory.createLineBorder(Color.BLACK));
			
			if (i < Settings.getExternalPrograms().length && Settings.getExternalPrograms()[i] != null) {
				pluginLabelTextField[i].setText(Settings.getExternalPrograms()[i].getLabel());
				pluginCommandTextField[i].setText(Settings.getExternalPrograms()[i].getCommand());
				pluginDescriptionTextField[i].setText(Settings.getExternalPrograms()[i].getDescription());
			} else {
				pluginLabelTextField[i].setText("");
				pluginCommandTextField[i].setText("");
				pluginDescriptionTextField[i].setText("");				
			}
			
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
	
}
