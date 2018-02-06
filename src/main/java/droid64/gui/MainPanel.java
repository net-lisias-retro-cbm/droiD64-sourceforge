package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.BevelBorder;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.db.DaoFactory;
import droid64.db.DaoFactoryImpl;
import droid64.db.DatabaseException;
import droid64.db.Disk;

/*
 * Created on 21.06.2004
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
public class MainPanel extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final int MAX_PLUGINS = 2;

	private final static String SETTING_FILE_NAME = ".droiD64.cfg";
	
	private DiskPanel diskPanel1, diskPanel2;
	private JButton infoButton, exitButton;
	private JLabel statusBar;
	
	private int colourChoice = 0;
	private final static int COLOUR_POWER_1 = 5;
	private final static int COLOUR_POWER_2 = 35;
	private int lookAndFeelChoice = 1;
	private boolean isExitQuestion = true;
	private boolean showViewButtons = true;
	private int rowHeight = 8;
	private String settingsFileName="";
	private String defaultImageDir = null;
	
	private HashMap<Object, Object> hashMap = new HashMap<Object, Object>();
	private HashMap<String,String> settingsData = new HashMap<String,String>();
	private ExternalProgram[] externalProgram = new ExternalProgram[MAX_PLUGINS];
	
	private int fontSize = 10;
		
	private boolean isUseDatabase = false;
	private String jdbcDriver = "com.mysql.jdbc.Driver";
	private String jdbcUrl = "jdbc:mysql://localhost:3306/droid64";
	private String jdbcUser = "droid64";
	private String jdbcPassword = "uridium";
	private long maxRows = 25L;
	
	// Setting file parameters
	private static final String SETTING_ASK_QUIT = "ask_quit";
	private static final String SETTING_COLOUR = "colour";
	private static final String SETTING_DEFAULT_IMAGE_DIR = "default_image_dir";
	private static final String SETTING_FONT_SIZE = "font_size";
	private static final String SETTING_ROW_HEIGHT = "row_height";
	private static final String SETTING_YES = "yes";
	private static final String SETTING_NO = "no";
	private static final String SETTING_PLUGIN = "plugin";
	private static final String SETTING_LABEL = "label";
	private static final String SETTING_DESCRIPTION = "description";
	private static final String SETTING_COMMAND = "command";
	private static final String SETTING_USE_DB = "use_database";
	private final static String SETTING_JDBC_DRIVER = "jdbc_driver";
	private final static String SETTING_JDBC_URL = "jdbc_url";
	private final static String SETTING_JDBC_USER = "jdbc_user";
	private final static String SETTING_JDBC_PASSWORD = "jdbc_password";
	private final static String SETTING_MAX_ROWS = "max_rows";
	private static final String SETTING_SHOW_VIEW_BUTTONS = "show_view_buttons";
	
	private JMenu searchMenu;
	private boolean scannerActive = false;

	/**
	 * Constructor
	 */
	public MainPanel() {
		super( DroiD64.PROGNAME+" v"+DroiD64.VERSION + " - " + DroiD64.TITLE );
		externalProgram[0] = new ExternalProgram();
		externalProgram[1] = new ExternalProgram();
		// Set correct settingsFileName for loadSettings and storeSettings
		String userHome = null;
		try {
			userHome = System.getProperty("user.home");
		} catch (SecurityException e) {
		} finally {
			if (userHome != null) {
				settingsFileName = userHome + "/" + SETTING_FILE_NAME;
			} else {
				settingsFileName = SETTING_FILE_NAME;
			}			
		}
		loadSettings(settingsFileName);
		diskPanel1 = new DiskPanel(externalProgram, fontSize, this);
		diskPanel2 = new DiskPanel(externalProgram, fontSize, this);
		diskPanel1.setDirectory(defaultImageDir == null ? "." : defaultImageDir);
		diskPanel2.setDirectory(defaultImageDir == null ? "." : defaultImageDir);
		diskPanel1.setExternalProgram(externalProgram);
		diskPanel2.setExternalProgram(externalProgram);
		diskPanel1.setOtherDiskPanelObject(diskPanel2);
		diskPanel2.setOtherDiskPanelObject(diskPanel1);
		drawPanel();
		// draw the panel before setting up the menues. In case the
		// database is not available do we need the status bar for errors.
		setupMenuBar();
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((dim.width - getSize().getWidth()) / 4),
				(int)((dim.height - getSize().getHeight()) / 4)
				);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		addWindowListener(
				new WindowAdapter(){
					public void windowClosing(WindowEvent e){
						exitThisProgram();
					}
				});
		saveDefaultValues();
		doSettings();
		pack();
		setVisible(true);
	}

	/**
	 * Setup menu
	 */
	public void setupMenuBar() {
		JMenuBar menubar = new JMenuBar();
		menubar.add(createProgramMenu());
		menubar.add(diskPanel1.createDiskImageMenu("Disk 1", "1"));
		menubar.add(diskPanel2.createDiskImageMenu("Disk 2", "2"));
		menubar.add(createSearchMenu());		
		menubar.add(createHelpMenu());
		setJMenuBar(menubar);
		menubar.revalidate();
		menubar.repaint();
	}
	
	/**
	 * Setup main panel
	 */
	private void drawPanel(){
		Container cp = getContentPane();
		cp.setLayout( new BorderLayout());

		JPanel obenPanel = new JPanel();
		obenPanel.setLayout(new GridLayout(1,2));
		obenPanel.add(diskPanel1);
		obenPanel.add(diskPanel2);		
		cp.add(obenPanel, BorderLayout.CENTER);
		
		JPanel globalChoicePanel = drawGlobalChoicePanel();
		cp.add(globalChoicePanel, BorderLayout.SOUTH);
	}

	/**
	 * Create panel with global buttons (About, Exit)
	 * @return JPanel
	 */
	private JPanel drawGlobalChoicePanel(){

		statusBar = new JLabel("Welcome to "+DroiD64.PROGNAME+" "+DroiD64.VERSION);
		statusBar.setHorizontalAlignment(SwingConstants.LEFT);
		statusBar.setFont(new Font("Verdana", Font.PLAIN, statusBar.getFont().getSize()));
		
		JPanel statusPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		statusPanel.setBorder(new BevelBorder(BevelBorder.LOWERED));
		statusPanel.setPreferredSize(new Dimension(getWidth(), statusBar.getHeight()+4));
		statusPanel.setLayout(new BoxLayout(statusPanel, BoxLayout.X_AXIS));
		statusPanel.add(statusBar);
				
		infoButton = new JButton("About");
		infoButton.setMnemonic('a');
		infoButton.setToolTipText("Shows some information about this program.");
		infoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (event.getSource()==infoButton ) {
					showHelp();
				};
			}
		});

		exitButton = new JButton("Exit");
		exitButton.setMnemonic('x');
		exitButton.setToolTipText("Leave this program.");
		exitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (event.getSource() == exitButton ) {
					exitThisProgram();
				}
			}
		});

		JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		buttonPanel.add(infoButton);
		buttonPanel.add(exitButton);

		JPanel globalChoicePanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		globalChoicePanel.setLayout(new GridLayout(2, 1, 1, 1));
		globalChoicePanel.add(buttonPanel);
		globalChoicePanel.add(statusPanel);
		
		JPanel bottomPanel = new JPanel(new BorderLayout());
		bottomPanel.add(drawConsolePanel(), BorderLayout.CENTER);
		bottomPanel.add(globalChoicePanel, BorderLayout.SOUTH);
		
		return bottomPanel;
	}

	
	private JTextArea consoleTextArea = null;
	private static final int FEEDBACK_PANEL_ROWS = 10;
	private static final int FEEDBACK_PANEL_COLS = 80;
	
	private JPanel drawConsolePanel() {
		JPanel feedBackPanel = new JPanel();
		Border border = BorderFactory.createEtchedBorder(EtchedBorder.LOWERED);
		feedBackPanel.setBorder(BorderFactory.createTitledBorder(border, "Output"));
		feedBackPanel.setLayout(new BorderLayout());
		consoleTextArea = new JTextArea(FEEDBACK_PANEL_ROWS, FEEDBACK_PANEL_COLS);
		consoleTextArea.setToolTipText("The status report is displayed here.");
		consoleTextArea.setEditable(false);
		feedBackPanel.add(new JScrollPane(consoleTextArea), BorderLayout.CENTER);
		return feedBackPanel;
	}
	
	public void appendConsole(String message) {
		if (!"".equals(message)) {
			consoleTextArea.setText(consoleTextArea.getText()+"\n"+message);
		}
	}
	

	/**
	 * Create a help drag-down menu (just for testing)
	 * @return JMenu
	 */
	private JMenu createHelpMenu() {
		JMenu menu = new JMenu("Help");
		menu.setMnemonic('h');
		JMenuItem menuItem;
		menuItem = new JMenuItem("About", 'a');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "About"){
					showHelp();
				}
			}
		});

		menuItem = new JMenuItem("Bugs and To-Do", 'b');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Bugs and To-Do"){
					new BugsFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Bugs and ToDo");
				}
			}
		});
		
		menuItem = new JMenuItem("Release notes", 'r');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			final static String msg = 
					"Version 0.05b:" +
							"\n\t- Refactored code." +
							"\n\t- New look & feel." +
							"\n\t- Gzipped images." +
							"\n\t- Database support.\n" +
					"Version 0.065b:" +
							"\n\t- D71, D81 and T64 read support." +
							"\n\t- CP/M read support (D64, D71, D81)." +
							"\n\t- Click sector in BAM to see hex dump." +
							"\n\t- View text, hexdump and BASIC from files." +
							"\n\t- Unload image." +
							"\n\t- View Koala images." +
							"\n\t- Bug fix for insert prg into D64." +
							"\n\t- Merged the two consoles into one shared." +
							"\n\t- Clear console menu option." +
							"\n\t- Implemented delete files from D64"
							;
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Release notes"){
					new TextViewFrame("Release Notes", msg);
				}				
			}
		});
		
		menuItem = new JMenuItem("Contact", 'c');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			final static String msg = 
					"For more information about this program check out this homepage:\n" +
					"http://droid64.sourceforge.net\n\n" +
					"You can contact me for any reason concerning "+DroiD64.PROGNAME+" v"+DroiD64.VERSION+" by writing an eMail to\n" +
					"wolfvoz@users.sourceforge.net ";
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Contact"){
					// Use JTextArea to allow user to copy & paste our text
					JTextArea info = new JTextArea(msg);
					info.setEditable(false);
					JOptionPane.showMessageDialog(null, info, "Contact", JOptionPane.INFORMATION_MESSAGE);
				}
			}
		});
		return menu;
	}

	/**
	 * Create a help drag-down menu (just for testing)
	 * @return JMenu
	 */
	private JMenu createProgramMenu() {
		JMenu menu = new JMenu("Program");		
		menu.setMnemonic('P');
		JMenuItem menuItem = new JMenuItem("Settings", 's');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Settings"){
					showSettings();
				}
			}
		});
		menu.addSeparator();
		menuItem = new JMenuItem("Clear console", 'c');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Clear console"){
					consoleTextArea.setText("");
				}
			}
		});
		menu.addSeparator();
		menuItem = new JMenuItem("Exit", 'x');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Exit"){
					exitThisProgram();
				}
			}
		});
		return menu;
	}

	/**
	 * Setup search menu. Requires database.
	 * @return JMenu
	 */
	private JMenu createSearchMenu() {
		searchMenu = new JMenu("Search");
		searchMenu.setMnemonic('S');
		final JMenuItem searchMenuItem = new JMenuItem("Search...", 's');
		searchMenu.add (searchMenuItem);
		searchMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource() == searchMenuItem){
					showSearch();
				}
			}
		});
		
		final JMenuItem scanMenuItem = new JMenuItem("Scan for disk images...", 'i');
		searchMenu.add (scanMenuItem);
		scanMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource() == scanMenuItem){
					showScanForImages();
				}
			}
		});
		
		final JMenuItem syncMenuItem = new JMenuItem("Sync database and files", 'y');
		searchMenu.add (syncMenuItem);
		syncMenuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource() == syncMenuItem){
					syncDatabase();
				}
			}
		});
		
		searchMenu.setEnabled(isUseDatabase);
		searchMenu.setToolTipText(isUseDatabase ? null : "You must configure and enable database to use search.");
		searchMenu.setVisible(isUseDatabase);
		return searchMenu;
	}	
	
	/**
	 * Good bye?
	 */
	private void exitThisProgram(){
		//System.out.println(diskPanel1.getFeedback());
		if (isExitQuestion) {
			if (JOptionPane.showConfirmDialog(
						null,
						"Really quit?",
						"Leaving this program...",
						JOptionPane.OK_CANCEL_OPTION
						) == JOptionPane.OK_OPTION) {
				System.exit(0);
			}
		} else { 
			System.exit(0);
		}
	}

	/**
	 * Show, edit and save settings.
	 */
	private void showSettings(){
		loadSettings(settingsFileName);
		new SettingsFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Settings", this);
		doSettings();
		storeSettings(settingsFileName);
	}

	/**
	 * Open search frame. Requires database.
	 */
	private void showSearch() {
		new SearchFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Search", this);
	}
	
	/**
	 * Recursively search a folder for D64 images.
	 */
	private synchronized void showScanForImages() {
		if (scannerActive) {
			statusBar.setText("Disk scanner is already active.");
		} else {
			JFileChooser chooser = new JFileChooser(this.getDefaultImageDir());
			chooser.setToolTipText("Select directory to start scanning for disk images in.");
			chooser.setDialogTitle("Choose directory to recursively scan for images");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File dir = chooser.getSelectedFile();
				final String path = dir != null ? dir.getAbsolutePath() : null;
				statusBar.setText("Scan for disk images in "+path);
				Thread scanner = new Thread() {
					public void run() {
						try {
							int numDisks = scanForD64Files(path);
							statusBar.setText("Done scanning. Found "+numDisks + " disk images in " + path);
						} finally {
							scannerActive = false;
						}
					}
				};
				scannerActive = true;
				scanner.start();				
			}
		}
	}

	/**
	 * Get a list of all stored disks from database, and check verify that files still exists in file system.
	 */
	private void syncDatabase() {
		try {
			List<Disk> diskList = DaoFactory.getDaoFactory().getDiskDao().getAllDisks();
			statusBar.setText("Got "+diskList.size()+" disks.");
			int deletedFileCount = 0;
			for (Disk disk : diskList) {
				File f = new File(disk.getFilePath() + File.separator + disk.getFileName());
				if (!f.exists() || !f.isFile()) {
					deletedFileCount++;
					disk.setDelete();
					DaoFactory.getDaoFactory().getDiskDao().delete(disk);
					statusBar.setText("Removing "+disk.getFileName());
				}
			}
			statusBar.setText("Sync done. Removed "+deletedFileCount+" disk from database.");
		} catch (DatabaseException e) {
			statusBar.setText("Sync failed: "+e.getMessage());
		}
	}

	/**
	 * Apply settings to GUI
	 */
	private void doSettings(){
		setLookAndFeel(lookAndFeelChoice, colourChoice);
		diskPanel1.setRowHeight(rowHeight);
		diskPanel2.setRowHeight(rowHeight);
		diskPanel1.setExternalProgram(externalProgram);
		diskPanel2.setExternalProgram(externalProgram);
		diskPanel1.setUseDatabase(isUseDatabase);
		diskPanel2.setUseDatabase(isUseDatabase);
	}

	public static int stringToInt(String c) {
		try {
			return Integer.parseInt(c); 
		} catch (NumberFormatException e) {
			System.err.println("Error: integer parse error. "+e.getMessage());
			return 0;
		}
	}

	/** Load settings from file.
	 * @param filename name of settings file
	 * @return true if load was successful
	 */
	private boolean loadSettings(String filename) {;		
		try {
			String line;
			BufferedReader br = new BufferedReader( new FileReader(filename) );
			settingsData.clear();
			while ((line = br.readLine()) != null) {
				if ( !line.trim().startsWith("#") && line.contains("=") ) {
					String line_key = line.substring( 0, line.indexOf("=") ) .trim();
					String line_value = line.substring(line.indexOf("=")+1).trim();
					settingsData.put(line_key, line_value);
					
					
					
					switch (line_key.toLowerCase()) {
						case SETTING_ASK_QUIT:
							setExitQuestion("yes".equalsIgnoreCase(line_value));
							break;
						case SETTING_SHOW_VIEW_BUTTONS:
							setShowViewButtons("yes".equalsIgnoreCase(line_value));
							break;
						case SETTING_ROW_HEIGHT:
							setRowHeight(stringToInt(line_value));
							break;
						case SETTING_COLOUR:
							setColourChoice(stringToInt(line_value));
							break;
						case SETTING_DEFAULT_IMAGE_DIR:
							setDefaultImageDir(line_value);
							break;
						case SETTING_FONT_SIZE:
							setFontSize(stringToInt(line_value));
							break;
						case SETTING_USE_DB:
							setUseDatabase("yes".equalsIgnoreCase(line_value));
							break;
						case SETTING_JDBC_DRIVER:
							setJdbcDriver(line_value);			
							break;
						case SETTING_JDBC_URL:
							setJdbcUrl(line_value);			
							break;
						case SETTING_JDBC_USER:
							setJdbcUser(line_value);
							break;
						case SETTING_JDBC_PASSWORD:
							setJdbcPassword(line_value);
							break;
						case SETTING_MAX_ROWS:
							setMaxRows(line_value);
							break;
						default:
							if (line_key.startsWith(SETTING_PLUGIN)) {						
								for (int i = 0; i < externalProgram.length; i++) {
									String x = Integer.toString(i)+"_";
									if ((SETTING_PLUGIN+x+SETTING_LABEL).equalsIgnoreCase(line_key)) {
										setExternalProgram(i, externalProgram[i].getCommand(), externalProgram[i].getDescription(), line_value);
									} else if ((SETTING_PLUGIN+x+SETTING_COMMAND).equalsIgnoreCase(line_key)) {
										setExternalProgram(i, line_value, externalProgram[i].getDescription(), externalProgram[i].getLabel());
									} else if ((SETTING_PLUGIN+x+SETTING_DESCRIPTION).equalsIgnoreCase(line_key)) {								
										setExternalProgram(i, externalProgram[i].getCommand(), line_value, externalProgram[i].getLabel() );
									}
								}
							}
							break;
					}
					


				}
			}
			br.close();
		} catch (IOException e) {
			System.err.println(e.toString());
			System.out.println("Setting default values and storing settings-file ["+settingsFileName+"].");
			setExitQuestion(isExitQuestion);
			setColourChoice(colourChoice);
			setRowHeight(12);
			setFontSize(10);
			//	externalProgram[0].setValues("/usr/local/bin/d64copy -t  serial2 -w -B -d 1 9", "Transfer this disk image to a real floppy.", "cbm4linux");
			setExternalProgram(0, "/home/wolf/bin/d64_to_floppy.sh", "Transfer this disk image to a real floppy (bash-script: \"d64copy -t serial2 -w -B -d 1 $1 8\").", "cbm4linux");
			setExternalProgram(1, "/usr/local/bin/x64", "Invoke VICE emulator with this disk image.", "VICE");
			storeSettings(filename);
			return false;
		}
		setDefaultFonts();
		if (isUseDatabase) {
			try {
				DaoFactoryImpl.initialize(jdbcDriver, jdbcUrl, jdbcUser, jdbcPassword, maxRows);
			} catch (DatabaseException e) {
				if (statusBar!=null) {
				statusBar.setText("Load settings failed: "+e.getMessage());
				} else {
					System.err.println(e);
				}
			}
		}
		return true;
	}
	
	private void setDefaultFonts() {
		Font plainFont = new Font("Verdana", Font.PLAIN, fontSize);
		Font boldFont = new Font("Verdana", Font.BOLD, fontSize);
		UIManager.put("Button.font",            new FontUIResource(plainFont)); 
		UIManager.put("CheckBox.font",          new FontUIResource(plainFont)); 
		UIManager.put("ComboBox.font",          new FontUIResource(plainFont)); 
		UIManager.put("FormattedTextField.font",new FontUIResource(plainFont)); 
		UIManager.put("Label.font",             new FontUIResource(boldFont)); 
		UIManager.put("List.font",              new FontUIResource(plainFont)); 
		UIManager.put("Menu.font",              new FontUIResource(plainFont)); 
		UIManager.put("MenuItem.font",          new FontUIResource(plainFont)); 
		UIManager.put("OptionPane.messageFont", new FontUIResource(plainFont));  
		UIManager.put("Slider.font",            new FontUIResource(plainFont));  
		UIManager.put("Spinner.font",           new FontUIResource(plainFont)); 
		UIManager.put("TabbedPane.font",        new FontUIResource(plainFont)); 
		UIManager.put("Table.font",             new FontUIResource(plainFont)); 
		UIManager.put("TableHeader.font",       new FontUIResource(plainFont)); 
		UIManager.put("TextArea.font",          new FontUIResource(plainFont)); 
		UIManager.put("TextField.font",         new FontUIResource(plainFont)); 
		UIManager.put("ToolTip.font",           new FontUIResource(plainFont));
		UIManager.put("TitledBorder.font",      new FontUIResource(plainFont));
		
		if (diskPanel1 != null) {
			diskPanel1.setupFont(fontSize);
		}
		if (diskPanel2 != null) {
			diskPanel2.setupFont(fontSize);
		}		
	}
	
	
	public void setDefaultImageDir(String dir) {
		settingsData.put(SETTING_DEFAULT_IMAGE_DIR, dir != null ? dir : "");
		defaultImageDir = dir;
	}

	public String getDefaultImageDir() {
		return defaultImageDir;
	}

	public void setFontSize(int fontSize) {
		settingsData.put(SETTING_FONT_SIZE, fontSize > 0 ? Integer.toString(fontSize) : "10");
		this.fontSize = fontSize;
		if (fontSize > rowHeight) {
			rowHeight = fontSize;
		}
	}
	
	public int getFontSize() {
		return fontSize;
	}	
	
	private boolean storeSettings(String filename){
		try {		
			FileWriter output = new FileWriter(filename);
			output.write("# Configuration file for "+DroiD64.PROGNAME+" v"+DroiD64.VERSION+"\n");
			output.write("# Saved " + new Date() + "\n");
			output.write("#\n");
			List<String> settingKeyList = new ArrayList<String>(settingsData.keySet());
			Collections.sort(settingKeyList);		
			for (String key : settingKeyList) {
				output.write(key + "=" + settingsData.get(key) + "\n");
			}
			output.write("# End of file\n");
			output.close();
			if (statusBar != null) {
				statusBar.setText("Saved settings.");
			}
		} catch (IOException e){
			if (statusBar != null) {
				statusBar.setText("Save settings failed: "+e.getMessage());
			} else {
				System.err.println("Save settings failed: "+e.getMessage());
			}
			return false;
		}
		return true;
	}

	private void showHelp(){
		new ShowHelpFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - About");
	}


	private void setLookAndFeel(int lookAndFeelChoice, int colorChoice){
		//	Set look&feel (skin) here
		String plaf = "javax.swing.plaf.metal.MetalLookAndFeel";

		switch (lookAndFeelChoice) {
		case 1 : {plaf = "javax.swing.plaf.metal.MetalLookAndFeel"; break;}
		case 2 : {plaf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel"; break;}
		case 3 : {plaf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; break; }
		//case 4 : plaf = new com.incors.plaf.kunststoff.KunststoffLookAndFeel();
		}

		try {
			UIManager.setLookAndFeel(plaf);
		} catch (UnsupportedLookAndFeelException e) {
			System.err.println(e.toString());
		} catch (ClassNotFoundException e) {
			System.err.println(e.toString());
		} catch (InstantiationException e) {
			System.err.println(e.toString());
		} catch (IllegalAccessException e) {
			System.err.println(e.toString());
		}
		
		//set back old values
		Iterator<?> it = hashMap.entrySet().iterator(); 
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof javax.swing.plaf.ColorUIResource) {
				ColorUIResource cr = (ColorUIResource) value;
				switch (colourChoice) {
				// gray (normal, no change to default values)
				case 0 :
					UIManager.put(key, value);
					break;
				// red
				case 1 :
					putColor(key, cr.getRed()+COLOUR_POWER_1, cr.getGreen()-COLOUR_POWER_1, cr.getBlue()-COLOUR_POWER_1);
					break;
				// green
				case 2 :
					putColor(key, cr.getRed()-COLOUR_POWER_1, cr.getGreen()+COLOUR_POWER_1, cr.getBlue()-COLOUR_POWER_1);
					break;
				// blue
				case 3 :
					putColor(key, cr.getRed()-COLOUR_POWER_1, cr.getGreen()-COLOUR_POWER_1, cr.getBlue()+COLOUR_POWER_1);
					break;
				// gray-light
				case 4 : 
					putColor(key, cr.getRed()+COLOUR_POWER_2, cr.getGreen()+COLOUR_POWER_2, cr.getBlue()+COLOUR_POWER_2 + 10);
					break;
				} // switch
			} // if
		} //while
		setDefaultFonts();
		SwingUtilities.updateComponentTreeUI(this);
		pack();
		invalidate();
		repaint();
	}

	private void putColor(Object key, int red, int green, int blue) {
		
		red = red < 0 ? 0 : red;
		red = red > 255 ? 255 : red;
		green = green < 0 ? 0 : green;
		green = green > 255 ? 255 : green;
		blue = blue < 0 ? 0 : blue;
		blue = blue > 255 ? 255 : blue;		
		UIManager.put(key, new ColorUIResource(red > 255 ? 255 : red, green > 255 ? 255 : green,  blue > 255 ? 255 : blue));		
	}
	
	private void saveDefaultValues(){
		hashMap.clear();
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.ColorUIResource) {
				hashMap.put(key, value);
			}
		}
	}

	/**
	 * @return
	 */
	public int getColourChoice() {
		return colourChoice;
	}

	/**
	 * @param i
	 */
	public void setColourChoice(int i) {
		settingsData.put(SETTING_COLOUR, Integer.toString(i));
		colourChoice = i;
	}

	/**
	 * @return
	 */
	public boolean isExitQuestion() {
		return isExitQuestion;
	}

	/**
	 * @param b
	 */
	public void setExitQuestion(boolean b) {
		if (b) { 
			settingsData.put(SETTING_ASK_QUIT, SETTING_YES);
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		} else {
			settingsData.put(SETTING_ASK_QUIT, SETTING_NO);
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}
		isExitQuestion = b;
	}
	
	public boolean isUseDatabase() {
		return isUseDatabase;
	}
	
	/** Whether or not to enable the database features.
	 * @param b if true enable database features.
	 */
	public void setUseDatabase(boolean b) {
		isUseDatabase = b;
		if (b) { 
			settingsData.put(SETTING_USE_DB, SETTING_YES);
		} else {
			settingsData.put(SETTING_USE_DB, SETTING_NO);
		}
		if (searchMenu != null) {
			searchMenu.setEnabled(b);
			searchMenu.setVisible(b);
		}
		if (this.diskPanel1 != null) {
			diskPanel1.setUseDatabase(b);
		}
		if (this.diskPanel2 != null) {
			diskPanel2.setUseDatabase(b);
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
		settingsData.put(SETTING_ROW_HEIGHT, Integer.toString(i) );
		rowHeight = i;
	}

	/**
	 * @return
	 */
	public ExternalProgram getExternalProgram(int whichOne) {
		return externalProgram.length>whichOne ? externalProgram[whichOne] : null;
	}

	public void setJdbcPassword(String password) {
		settingsData.put(SETTING_JDBC_PASSWORD, password);
		jdbcPassword = password;
	}
	public String getJdbcPassword() {
		return jdbcPassword;
	}
	public void setJdbcUser(String user) {
		settingsData.put(SETTING_JDBC_USER, user);
		jdbcUser = user;
	}
	public String getJdbcUser() {
		return jdbcUser;
	}
	public void setJdbcUrl(String url) {
		settingsData.put(SETTING_JDBC_URL, url);
		jdbcUrl = url;
	}
	public String getJdbcUrl() {
		return jdbcUrl;
	}
	public void setJdbcDriver(String className) {
		settingsData.put(SETTING_JDBC_DRIVER, className);
		jdbcDriver = className;
	}
	public String getJdbcDriver() {
		return jdbcDriver;
	}
	
	public void setMaxRows(String maxRows) {
		Long value;
		try {
			 value = Long.parseLong(maxRows);
		} catch (NumberFormatException e) {
			value = 25L;
		}
		setMaxRows(value);
	}
	
	public void setMaxRows(Long maxRows) {
		long value = maxRows!=null ? maxRows.longValue() : 25L;
		this.maxRows = value > 1 ? value : 25L;
		settingsData.put(SETTING_MAX_ROWS, Long.toString(this.maxRows));
	}
	
	public long getMaxRows() {
		return this.maxRows;
	}

	public DiskPanel getLeftDiskPanel() {
		return this.diskPanel1;
	}
	public DiskPanel getRightDiskPanel() {
		return this.diskPanel2;
	}
	
	
	/**
	 * Recursively scan dirName for D64 images and add to database.
	 * @param dirName directory to start searching in.
	 * @return number of found disk images
	 */
	public int scanForD64Files(String dirName) {
		statusBar.setText("Scanning "+dirName);
		File dir = new File(dirName);
		File[] files = dir.listFiles();
		Arrays.sort(files);
		int diskCount = 0;
		for (int i=0; i<files.length; i++) {
			if (files[i].isDirectory()) {
				diskCount += scanForD64Files(files[i].getAbsolutePath());
			} else if (files[i].isFile()) {
				String name = files[i].getName();
				if (name.endsWith(".d64") || name.endsWith(".d64.gz")) {
					String fileName = dirName + File.separator + name;
					try {
//						D64 d64 = new D64();
//						d64.readImage(fileName);
						
						DiskImage d64 =  DiskImage.getDiskImage(fileName);
						
						d64.readBAM();
					    d64.readDirectory();
					    Disk disk = d64.getDisk();
					    disk.setFilePath(dirName);
					    disk.setFileName(name);
						DaoFactory.getDaoFactory().getDiskDao().save(disk);
						diskCount++;
						statusBar.setText("Saved " + fileName);
					} catch (DatabaseException e) {
						statusBar.setText(fileName +" : "+e.getMessage());
					} catch (CbmException e) {
						statusBar.setText(fileName +" : "+e.getMessage());
					}
				}
			}
		}
		return diskCount;
	}
	
	public void setStatusBar(String text) {
		statusBar.setText(text);
		statusBar.repaint();
	}
	
	/**
	 * @param whichOne
	 * @param command
	 * @param description
	 * @param label
	 */
	public void setExternalProgram(int whichOne, String command, String description, String label) {
		String which = Integer.toString(whichOne) + "_";
		settingsData.put(SETTING_PLUGIN +which+ SETTING_LABEL, label);
		settingsData.put(SETTING_PLUGIN +which+ SETTING_COMMAND, command);
		settingsData.put(SETTING_PLUGIN +which+ SETTING_DESCRIPTION, description);
		externalProgram[whichOne].setValues(command, description, label);
	}

	public boolean isShowViewButtons() {
		return showViewButtons;
	}

	public void setShowViewButtons(boolean b) {
		if (b) { 
			settingsData.put(SETTING_SHOW_VIEW_BUTTONS, SETTING_YES);
		} else {
			settingsData.put(SETTING_SHOW_VIEW_BUTTONS, SETTING_NO);
		}
		showViewButtons = b;
	}

}
