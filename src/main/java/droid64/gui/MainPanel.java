package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.PrintWriter;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.border.Border;
import javax.swing.border.EtchedBorder;
import javax.swing.filechooser.FileFilter;
import javax.swing.plaf.ColorUIResource;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.DefaultCaret;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.db.DaoFactory;
import droid64.db.DaoFactoryImpl;
import droid64.db.DatabaseException;
import droid64.db.Disk;

/**<pre>
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
 * @author wolf
 *</pre>
 */
public class MainPanel extends JFrame {
	
	private final static long serialVersionUID = 1L;
	private final static int FEEDBACK_PANEL_ROWS = 10;
	private final static int FEEDBACK_PANEL_COLS = 80;
	private final static int COLOUR_POWER_1 = 5;
	private final static int COLOUR_POWER_2 = 35;
	
	public static Insets BUTTON_MARGINS = new Insets(1, 4, 1, 4);
	/** Left disk panel */
	private DiskPanel diskPanel1;
	/** Right disk panel */
	private DiskPanel diskPanel2;
	/** True when scanning for disk images is running. */
	private boolean scannerActive = false;
	private HashMap<Object, Object> colorHashMap = new HashMap<Object, Object>();
	/** The menu shown when database is used */
	private JMenu searchMenu;
	/** The console */
	private JTextArea consoleTextArea = null;
	/** The plugin buttons. Used to be able to change the label from settings. */
	private JButton[] pluginButtons = new JButton[Settings.MAX_PLUGINS];
	/** File save dialog for disk images */
	private JFileChooser chooser = null;
	/** File save dialog for console text etc. */
	private JFileChooser textFileChooser = null;
	/** The split pane used for the console at the bottom and the rest in the upper half. */	
	private JSplitPane splitPane = null;
	/** The position of the divider in the splitPane. */
	private int dividerLoc = 0;
	/** The size of the divider in the splitPane. */
	private int dividerSize = 0;
	
	// Buttons are put into GUI in numerical order, left to right, and then next row.
	// Keep them unique or strange things will happen.
	private final static Integer LOAD_DISK_BUTTON     =  1;
	private final static Integer BAM_BUTTON           =  2;
	private final static Integer UP_BUTTON            =  3;
	private final static Integer COPY_FILE_BUTTON     =  4;
	private final static Integer RENAME_FILE_BUTTON   =  5;
	private final static Integer VIEW_TEXT_BUTTON     =  6;
	private final static Integer PLUGIN_2_BUTTON      =  7;
	private final static Integer HIDE_CONSOLE_BUTTON  =  8;
	private final static Integer UNLOAD_DISK_BUTTON   =  9;
	private final static Integer VALIDATE_DISK_BUTTON = 10;
	private final static Integer DOWN_BUTTON          = 11;
	private final static Integer NEW_FILE_BUTTON      = 12;	 
	private final static Integer VIEW_IMAGE_BUTTON    = 13;
	private final static Integer VIEW_BASIC_BUTTON    = 14;
	private final static Integer PLUGIN_3_BUTTON      = 15;
	private final static Integer SETTINGS_BUTTON      = 16;
	private final static Integer NEW_DISK_BUTTON      = 17;
	private final static Integer RENAME_DISK_BUTTON   = 18;
	private final static Integer SORT_FILES_BUTTON    = 19;
	private final static Integer DELETE_FILE_BUTTON   = 20;
	private final static Integer VIEW_HEX_BUTTON      = 21;
	private final static Integer PLUGIN_1_BUTTON      = 22;
	private final static Integer PLUGIN_4_BUTTON      = 23;
	private final static Integer EXIT_BUTTON          = 24;

	/** Number of columns of buttons. */
	private final static int NUM_BUTTON_COLUMNS = 8;
	/** Number of rows of buttons. */
	private final static int NUM_BUTTON_ROWS = 3;
	/** Array with the button ID for each plugin button */
	private final static int[] PLUGIN_IDS = { PLUGIN_1_BUTTON, PLUGIN_2_BUTTON, PLUGIN_3_BUTTON, PLUGIN_4_BUTTON };
	/** Map containing all buttons */
	private Map<Integer,JComponent> buttonMap = new TreeMap<Integer,JComponent>();
	
	private final static String LOOK_AND_FEEL_CLASSES[] = {
		"javax.swing.plaf.metal.MetalLookAndFeel",
		"com.sun.java.swing.plaf.windows.WindowsLookAndFeel",
		"com.sun.java.swing.plaf.motif.MotifLookAndFeel",
		"com.sun.java.swing.plaf.gtk.GTKLookAndFeel"
	};
	
	/**
	 * Constructor
	 */
	public MainPanel() {
		super( DroiD64.PROGNAME+" v"+DroiD64.VERSION + " - " + DroiD64.TITLE );

		doSettings();
		diskPanel1 = new DiskPanel(this);
		diskPanel2 = new DiskPanel(this);
		diskPanel1.setDirectory(Settings.getDefaultImageDir());
		diskPanel2.setDirectory(Settings.getDefaultImageDir2());
		diskPanel1.setOtherDiskPanelObject(diskPanel2);
		diskPanel2.setOtherDiskPanelObject(diskPanel1);
		diskPanel1.setActive(true);
		// Setup GUI
		drawPanel();
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
		
		diskPanel1.loadLocalDirectory(Settings.getDefaultImageDir());
		diskPanel2.loadLocalDirectory(Settings.getDefaultImageDir2());
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
	
	private DiskPanel getActiveDiskPanel() {
		if (diskPanel1 != null && diskPanel1.isActive()) {
			return diskPanel1;
		} else if (diskPanel2 != null && diskPanel2.isActive()) {
			return diskPanel2;			
		} else {
			return null;
		}
	}
	
	private DiskPanel getInactiveDiskPanel() {
		if (diskPanel1 != null && diskPanel1.isActive()) {
			return diskPanel2;
		} else if (diskPanel2 != null && diskPanel2.isActive()) {
			return diskPanel1;			
		} else {
			return null;
		}
	}
	
	/**
	 * Setup main panel
	 */
	private void drawPanel() {
		final JPanel dirListPanel = new JPanel();
		dirListPanel.setLayout(new GridLayout(1,2));
		dirListPanel.add(diskPanel1);
		dirListPanel.add(diskPanel2);
		
		// Create all buttons into the buttonMap
		createDiskOperationButtons();
		createFileOperationButtons();
		createViewFileButtons();
		createOtherButtons();
		// Put buttons in GUI
		JPanel buttonPanel = new JPanel();
		buttonPanel.setLayout(new GridLayout(NUM_BUTTON_ROWS, NUM_BUTTON_COLUMNS));
		for (Integer key : buttonMap.keySet()) {
			buttonPanel.add(buttonMap.get(key));
		}

		JPanel listButtonPanel = new JPanel();
		listButtonPanel.setLayout(new BorderLayout());		
		listButtonPanel.add(dirListPanel, BorderLayout.CENTER);
		listButtonPanel.add(buttonPanel, BorderLayout.SOUTH);

		JPanel consolePanel = createConsolePanel();
		
		splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setTopComponent(listButtonPanel);
		splitPane.setBottomComponent(consolePanel);

        setContentPane(splitPane);
	}
	
	private void createOtherButtons() {
		ExternalProgram[] externalPrograms = Settings.getExternalPrograms();
		for (int i = 0; i < pluginButtons.length && i < externalPrograms.length; i++) {
			String label = externalPrograms[i] != null ? externalPrograms[i].getLabel() : null;
			String tooltip = externalPrograms[i] != null ? externalPrograms[i].getDescription() : null; 
			pluginButtons[i] = new JButton(label != null ? label : "");
			pluginButtons[i].setMargin(BUTTON_MARGINS);
			pluginButtons[i].setToolTipText(tooltip);
			pluginButtons[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					for (int cnt = 0; cnt < Settings.getExternalPrograms().length; cnt ++){
						if ( event.getSource() == pluginButtons[cnt] ){
							DiskPanel diskPanel = getActiveDiskPanel();
							if (diskPanel != null) {
								diskPanel.doExternalProgram(Settings.getExternalPrograms()[cnt]);;
							}
						}
					}
				}
			});
			if (i < PLUGIN_IDS.length && i<pluginButtons.length) {
				pluginButtons[i].setBackground(adjustedColor(pluginButtons[i].getBackground(), -20, 20, 20));
				buttonMap.put(PLUGIN_IDS[i], pluginButtons[i]);
			}
		}
		final JToggleButton consoleHideButton = new JToggleButton("Hide console");
		consoleHideButton.setMnemonic('e');
		consoleHideButton.setMargin(BUTTON_MARGINS);
		consoleHideButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == consoleHideButton) {
					boolean show = !consoleHideButton.isSelected();					
					splitPane.getBottomComponent().setVisible(show);
					if (show) {
						splitPane.setDividerLocation(dividerLoc);
						splitPane.setDividerSize(dividerSize);
					} else {
						dividerLoc = splitPane.getDividerLocation();
						dividerSize = splitPane.getDividerSize();
						splitPane.setDividerSize(0);
					}
				}
			}
		});
		consoleHideButton.setBackground(adjustedColor(consoleHideButton.getBackground(), 30, -20, -20));
		buttonMap.put(HIDE_CONSOLE_BUTTON, consoleHideButton);
		final JButton settingsButton = new JButton("Settings");
		settingsButton.setMargin(BUTTON_MARGINS);
		settingsButton.setToolTipText("Open the settings dialog for DroiD64.");
		settingsButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (event.getSource() == settingsButton ) {
					showSettings();
				};
			}
		});
		settingsButton.setBackground(adjustedColor(settingsButton.getBackground(), 30, -20, -20));
		buttonMap.put(SETTINGS_BUTTON, settingsButton);
		final JButton exitButton = new JButton("Exit");
		exitButton.setMargin(BUTTON_MARGINS);
		exitButton.setMnemonic('x');
		exitButton.setToolTipText("Leave this program.");
		exitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (event.getSource() == exitButton ) {
					exitThisProgram();
				}
			}
		});
		exitButton.setBackground(adjustedColor(exitButton.getBackground(), 30, -20, -20));
		buttonMap.put(EXIT_BUTTON, exitButton);
	}
	
	
	private Color adjustedColor(Color color, int red, int green, int blue) {
		
		
		int newRed = color.getRed() + red;
		int newGreen = color.getGreen() + green;
		int newBlue = color.getBlue() + blue;
		
		return new Color(
				newRed >=0 ? (newRed < 256 ? newRed : 255) : 0, 
				newGreen >= 0 ? (newGreen < 256 ? newGreen : 255) : 0, 
				newBlue >= 0 ? (newBlue < 256 ? newBlue : 255) : 0);
	}
	
	
	private void createDiskOperationButtons() {
		final JButton newDiskButton = new JButton("New Disk");
		newDiskButton.setMnemonic('n');
		newDiskButton.setToolTipText("Create a new blank disk.");
		newDiskButton.setMargin(BUTTON_MARGINS);
		newDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == newDiskButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null) {
						diskPanel.newDiskImage();
					}
				}
			}
		});
		newDiskButton.setBackground(adjustedColor(newDiskButton.getBackground(), -20, -20, 20));
		buttonMap.put(NEW_DISK_BUTTON, newDiskButton);

		final JButton loadDiskButton =new JButton("Load Disk");
		loadDiskButton.setMnemonic('l');
		loadDiskButton.setToolTipText("Open a disk.");
		loadDiskButton.setMargin(BUTTON_MARGINS);
		loadDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == loadDiskButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null) {
						diskPanel.openDiskImage(newDiskImageFileDialog(diskPanel.getDirectory()), true);						
					}
				}
			}
		});
		loadDiskButton.setBackground(adjustedColor(loadDiskButton.getBackground(), -20, -20, 20));
		buttonMap.put(LOAD_DISK_BUTTON, loadDiskButton);

		final JButton unloadDiskButton = new JButton("Unload Disk");
		unloadDiskButton.setMnemonic('u');
		unloadDiskButton.setToolTipText("Unload a loaded disk image.");
		unloadDiskButton.setMargin(BUTTON_MARGINS);
		unloadDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				DiskPanel diskPanel = getActiveDiskPanel();
				if  (diskPanel != null && event.getSource() == unloadDiskButton) {
					if (diskPanel.isImageLoaded()) {
						diskPanel.unloadDisk();						
					} else {
						String dir = diskPanel.getCurrentImagePath();
						File dirfile = dir != null ? new File(dir) : null;
						if (dirfile != null && dirfile.getParent() != null) {
							diskPanel.loadLocalDirectory(dirfile.getParent());
						}
					}
				}
			}
		});
		unloadDiskButton.setBackground(adjustedColor(unloadDiskButton.getBackground(), -20, -20, 20));
		buttonMap.put(UNLOAD_DISK_BUTTON, unloadDiskButton);

		final JButton showBamButton = new JButton("BAM");
		showBamButton.setMnemonic('b');
		showBamButton.setToolTipText("Show the BAM of this disk.");
		showBamButton.setMargin(BUTTON_MARGINS);
		showBamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == showBamButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null && diskPanel.isImageLoaded()) {
						diskPanel.showBAM();						
					} else {
						showErrorMessage("noDisk");
					}
				}
			}
		});
		showBamButton.setBackground(adjustedColor(showBamButton.getBackground(), -20, -20, 20));
		buttonMap.put(BAM_BUTTON, showBamButton);

		final JButton renameDiskButton = new JButton("Rename Disk");
		renameDiskButton.setMnemonic('r');
		renameDiskButton.setToolTipText("Modify the label of the disk.");
		renameDiskButton.setMargin(BUTTON_MARGINS);
		renameDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == renameDiskButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null && diskPanel.isImageLoaded()) {
						diskPanel.renameDisk();
					} else {
						showErrorMessage("noDisk");
						return;
					}
				}
			}
		});
		renameDiskButton.setBackground(adjustedColor(renameDiskButton.getBackground(), -20, -20, 20));
		buttonMap.put(RENAME_DISK_BUTTON, renameDiskButton);

		final JButton validateDiskButton = new JButton("Validate Disk");
		validateDiskButton.setMnemonic('v');
		validateDiskButton.setToolTipText("Validate the disk.");
		validateDiskButton.setMargin(BUTTON_MARGINS);
		validateDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == validateDiskButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null && diskPanel.isImageLoaded()) {
						diskPanel.validateDisk();
					} else {
						showErrorMessage("noDisk");
						return;
					}
				}
			}
		});
		validateDiskButton.setBackground(adjustedColor(validateDiskButton.getBackground(), -20, -20, 20));
		buttonMap.put(VALIDATE_DISK_BUTTON, validateDiskButton);
	}
	
	private void createViewFileButtons() {
		final JButton viewTextButton = new JButton("View Text");
		viewTextButton.setMnemonic('t');
		viewTextButton.setToolTipText("Show text from selected file.");
		viewTextButton.setMargin(BUTTON_MARGINS);
		viewTextButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == viewTextButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null) {
						diskPanel.showFile();						
					}
				}
			}
		});
		viewTextButton.setBackground(adjustedColor(viewTextButton.getBackground(), 20, 20, -20));
		buttonMap.put(VIEW_TEXT_BUTTON, viewTextButton);

		final JButton hexViewButton = new JButton("View Hex");
		hexViewButton.setMnemonic('h');
		hexViewButton.setToolTipText("Show hex dump of selected file.");
		hexViewButton.setMargin(BUTTON_MARGINS);
		hexViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == hexViewButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					
					if (diskPanel != null) {
						diskPanel.hexViewFile();						
					}
				}
			}
		});
		hexViewButton.setBackground(adjustedColor(hexViewButton.getBackground(), 20, 20, -20));
		buttonMap.put(VIEW_HEX_BUTTON, hexViewButton);

		final JButton basicViewButton = new JButton("View Basic");
		basicViewButton.setMnemonic('s');
		basicViewButton.setToolTipText("Show BASIC listing from selected file.");
		basicViewButton.setMargin(BUTTON_MARGINS);
		basicViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == basicViewButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					
					if (diskPanel != null) {
						diskPanel.basicViewFile();						
					}
				}
			}
		});
		basicViewButton.setBackground(adjustedColor(basicViewButton.getBackground(), 20, 20, -20));
		buttonMap.put(VIEW_BASIC_BUTTON, basicViewButton);

		final JButton imageViewButton = new JButton("View Image");
		imageViewButton.setMnemonic('m');
		imageViewButton.setToolTipText("Show image from selected file.");
		imageViewButton.setMargin(BUTTON_MARGINS);
		imageViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == imageViewButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null) {
						diskPanel.imageViewFile();						
					}
				}
			}
		});
		imageViewButton.setBackground(adjustedColor(imageViewButton.getBackground(), 20, 20, -20));
		buttonMap.put(VIEW_IMAGE_BUTTON, imageViewButton);
	}
	
	private void createFileOperationButtons() {
		final JButton upButton = new JButton("Up");
		upButton.setToolTipText("Move file upwards in directory listing.");
		upButton.setMargin(BUTTON_MARGINS);
		upButton.setBackground(adjustedColor(upButton.getBackground(), -20, 20, -20));
		buttonMap.put(UP_BUTTON, upButton);

		final JButton downButton = new JButton("Down");
		downButton.setMargin(BUTTON_MARGINS);		
		downButton.setToolTipText("Move file downwards in directory listing.");
		downButton.setBackground(adjustedColor(downButton.getBackground(), -20, 20, -20));
		buttonMap.put(DOWN_BUTTON, downButton);

		ActionListener upDownListener = new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				DiskPanel diskPanel = getActiveDiskPanel();
				if (diskPanel != null && diskPanel.isImageLoaded() && (event.getSource() == upButton || event.getSource() == downButton)) {
					diskPanel.moveFile(event.getSource() == upButton);
					
				}
			}};
			
		upButton.addActionListener(upDownListener);
		downButton.addActionListener(upDownListener);
		
		final JButton sortButton = new JButton("Sort Files");
		sortButton.setToolTipText("Sort file entries by name.");
		sortButton.setMargin(BUTTON_MARGINS);
		sortButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == sortButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null) {
						diskPanel.sortFiles();
					}
				}
			}
		});
		sortButton.setBackground(adjustedColor(sortButton.getBackground(), -20, 20, -20));
		buttonMap.put(SORT_FILES_BUTTON, sortButton);

		final JButton copyButton = new JButton("Copy File");
		copyButton.setMnemonic('c');
		copyButton.setToolTipText("Copy files to the other disk.");
		copyButton.setMargin(BUTTON_MARGINS);
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == copyButton) {
					DiskPanel diskPanel1 = getActiveDiskPanel();
					DiskPanel diskPanel2 = getInactiveDiskPanel();
					if (diskPanel1 != null && diskPanel2 != null) {
						diskPanel1.copyPRG();
					}
				}
			}
		});
		copyButton.setBackground(adjustedColor(copyButton.getBackground(), 20, -20, 20));
		buttonMap.put(COPY_FILE_BUTTON, copyButton);

		final JButton renamePRGButton = new JButton("Rename File");
		renamePRGButton.setMnemonic('r');
		renamePRGButton.setToolTipText("Rename a file in this disk.");
		renamePRGButton.setMargin(BUTTON_MARGINS);
		renamePRGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == renamePRGButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null) {
						diskPanel.renamePRG();
					}
				}
			}
		});
		renamePRGButton.setBackground(adjustedColor(renamePRGButton.getBackground(), 20, -20, 20));
		buttonMap.put(RENAME_FILE_BUTTON, renamePRGButton);

		final JButton delPRGButton = new JButton("Delete File");
		delPRGButton.setMnemonic('d');
		delPRGButton.setToolTipText("Delete files from this disk.");
		delPRGButton.setMargin(BUTTON_MARGINS);
		delPRGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == delPRGButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null) {
						diskPanel.delPRG();
					}
				}
			}
		});
		delPRGButton.setBackground(adjustedColor(delPRGButton.getBackground(), 20, -20, 20));
		buttonMap.put(DELETE_FILE_BUTTON, delPRGButton);

		final JButton newFileButton = new JButton("New File");
		newFileButton.setMnemonic('w');
		newFileButton.setToolTipText("Create a file on this disk.");
		newFileButton.setMargin(BUTTON_MARGINS);
		newFileButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == newFileButton) {
					DiskPanel diskPanel = getActiveDiskPanel();
					if (diskPanel != null && diskPanel.isImageLoaded()) {
						diskPanel.newFile();
					} else {
						showErrorMessage("noDisk");
					}
				}
			}
		});
		newFileButton.setBackground(adjustedColor(newFileButton.getBackground(), 20, -20, 20));
		buttonMap.put(NEW_FILE_BUTTON, newFileButton);
	}
		
	private JPanel createConsolePanel() {
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
	
	private String newDiskImageFileDialog(String directory) {
		if (chooser == null) {
			chooser = new JFileChooser(directory);
			FileFilter fileFilter = new FileFilter() {			
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					} else {
						for (int i=0; i<DiskImage.VALID_IMAGE_FILE_EXTENSTIONS.length; i++) {
							if (f.getName().toLowerCase().endsWith(DiskImage.VALID_IMAGE_FILE_EXTENSTIONS[i])) {
								return true;
							}
						}
						return false;
					}
				}
				public String getDescription () { return "Disk images"; }  
			};
			chooser.addChoosableFileFilter(fileFilter);
			chooser.setFileFilter(fileFilter);
			chooser.setMultiSelectionEnabled(false);
			chooser.setDialogTitle("Load disk image");
		} else {
			chooser.setCurrentDirectory(new File(directory));
		}
		if (chooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			directory = chooser.getSelectedFile().getParent();
			return chooser.getSelectedFile()+"";
		}
		return null;
	}
	
	public void appendConsole(String message) {
		if (consoleTextArea != null) {
			if (!"".equals(message)) {
				consoleTextArea.setText(consoleTextArea.getText()+"\n"+message);
				DefaultCaret caret = (DefaultCaret) consoleTextArea.getCaret();
				caret.setUpdatePolicy(DefaultCaret.OUT_BOTTOM);		
			}
		} else {
			System.out.println(message);
		}
	}
	
	private void showErrorMessage(String error){
		if (error == "noDisk"){
			appendConsole("\nNo disk image file selected. Aborting.");
			JOptionPane.showMessageDialog(this,
					"No disk loaded.\n"+
							"Open a disk image file first.",
							DroiD64.PROGNAME + " v" + DroiD64.VERSION + " - No disk",
							JOptionPane.ERROR_MESSAGE);
		}
		if (error == "insertError"){
			appendConsole("\nInserting error. Aborting.\n");
			JOptionPane.showMessageDialog(this,
					"An error occurred while inserting file into disk.\n"+
					"Look up console report message for further information.",
					DroiD64.PROGNAME + " v" + DroiD64.PROGNAME + " - Failure while inserting file",
					JOptionPane.ERROR_MESSAGE );
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
		final JFrame mainPanel = this;
		menuItem = new JMenuItem("Release notes", 'r');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Release notes"){
					new TextViewFrame(mainPanel, DroiD64.PROGNAME, "Release Notes", RELEASE_NOTES);
				}				
			}
		});
		
		menuItem = new JMenuItem("Contact", 'c');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			final static String msg = 
					"For more information about this program check out this homepage:\n" +
					"http://droid64.sourceforge.net\n\n" +
					"You can contact us for any reason concerning "+DroiD64.PROGNAME+" v"+DroiD64.VERSION+" by writing an email to\n" +
					"hwetters@users.sourceforge.net or wolfvoz@users.sourceforge.net ";
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
	 * Select a file name for saving a text file
	 * @param directory
	 * @return chosen file name
	 */
	private String openFileDialog(String directory) {	
		if (textFileChooser == null) {
			textFileChooser = new JFileChooser(directory);
			FileFilter fileFilter = new FileFilter() {
				public boolean accept(File f) {
					if (f.isDirectory()) {
						return true;
					} else {
							if (f.getName().toLowerCase().endsWith(".txt")) {
								return true;
							}
						return false;
					}
				}
				public String getDescription () { return "Text"; }
			};
			textFileChooser.addChoosableFileFilter(fileFilter);
			textFileChooser.setFileFilter(fileFilter);
			textFileChooser.setMultiSelectionEnabled(false);
			textFileChooser.setDialogTitle("Save text");
		} else {
			textFileChooser.setCurrentDirectory(new File(directory));
		}
		if (textFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			directory = textFileChooser.getSelectedFile().getParent();
			return textFileChooser.getSelectedFile()+"";
		}
		return null;
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
		menuItem = new JMenuItem("Save console", 'a');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Save console") {					
					String fileName = openFileDialog(getActiveDiskPanel().getCurrentImagePath());
					if (fileName != null) {
						try {
							writeFile(new File(fileName), consoleTextArea.getText());
						} catch (CbmException e) {	}
					}
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
		
		searchMenu.setEnabled(Settings.getUseDb());
		searchMenu.setToolTipText(Settings.getUseDb() ? null : "You must configure and enable database to use search.");
		searchMenu.setVisible(Settings.getUseDb());
		return searchMenu;
	}	
	
	/**
	 * Good bye?
	 */
	private void exitThisProgram() {
		if (Settings.getAskQuit()) {
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
	private void showSettings() {
		doSettings();
		new SettingsFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Settings", this);
		doSettings();
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
			appendConsole("Disk scanner is already active.");
		} else {
			JFileChooser chooser = new JFileChooser(Settings.getDefaultImageDir());
			chooser.setToolTipText("Select directory to start scanning for disk images in.");
			chooser.setDialogTitle("Choose directory to recursively scan for images");
			chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
			chooser.setMultiSelectionEnabled(false);
			if (chooser.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				File dir = chooser.getSelectedFile();
				final String path = dir != null ? dir.getAbsolutePath() : null;
				appendConsole("Scan for disk images in "+path);
				Thread scanner = new Thread() {
					public void run() {
						try {
							int numDisks = scanForD64Files(path);
							appendConsole("Done scanning. Found "+numDisks + " disk images in " + path);
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
			appendConsole("Got "+diskList.size()+" disks.");
			int deletedFileCount = 0;
			for (Disk disk : diskList) {
				File f = new File(disk.getFilePath() + File.separator + disk.getFileName());
				if (!f.exists() || !f.isFile()) {
					deletedFileCount++;
					disk.setDelete();
					DaoFactory.getDaoFactory().getDiskDao().delete(disk);
					appendConsole("Removing infor for "+f.getPath());
				}
			}
			appendConsole("Sync done. Removed "+deletedFileCount+" disk from database.");
		} catch (DatabaseException e) {
			appendConsole("Sync failed: "+e.getMessage());
		}
	}

	/**
	 * Apply settings to GUI
	 */
	private void doSettings() {
		setLookAndFeel(Settings.getLookAndFeel(), Settings.getColourChoice());
		if (searchMenu != null) {
			searchMenu.setEnabled(Settings.getUseDb());
			searchMenu.setVisible(Settings.getUseDb());
		}
		setDefaultFonts();
		if (Settings.getUseDb()) {
			try {
				DaoFactoryImpl.initialize(Settings.getJdbcDriver(), Settings.getJdbcUrl(), Settings.getJdbcUser(), Settings.getJdbcPassword(), Settings.getMaxRows());
			} catch (DatabaseException e) {
				appendConsole("Load settings failed: "+e.getMessage());
			}
		}
	}
	
	private void setDefaultFonts() {
		Font plainFont = new Font("Verdana", Font.PLAIN, Settings.getFontSize());
		Font boldFont = new Font("Verdana", Font.BOLD, Settings.getFontSize());
		UIManager.put("Button.font",            new FontUIResource(plainFont)); 
		UIManager.put("CheckBox.font",          new FontUIResource(plainFont)); 
		UIManager.put("ComboBox.font",          new FontUIResource(plainFont)); 
		UIManager.put("RadioButton.font",       new FontUIResource(plainFont)); 
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
		UIManager.put("ToggleButton.font",      new FontUIResource(plainFont));
		UIManager.put("ToolTip.font",           new FontUIResource(plainFont));
		UIManager.put("TitledBorder.font",      new FontUIResource(plainFont));
		
		if (diskPanel1 != null) {
			diskPanel1.setTableColors();
		}
		if (diskPanel2 != null) {
			diskPanel2.setTableColors();
		}		
	}

	private void showHelp() {
		new ShowHelpFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - About");		
	}
	
	public static String[] getLookAndFeelNames() {
		String[] looks = new String[LOOK_AND_FEEL_CLASSES.length];
		for (int i=0; i < looks.length; i++) {
			String x[] = LOOK_AND_FEEL_CLASSES[i].split("[.]");
			looks[i] = x[x.length - 1];
		}
		return looks;
	}
	
	private void setLookAndFeel(int lookAndFeel, int colorChoice){
		//	Set look&feel (skin)
		try {
			String plaf = LOOK_AND_FEEL_CLASSES[lookAndFeel < LOOK_AND_FEEL_CLASSES.length && lookAndFeel >= 0 ? lookAndFeel: 1];
			UIManager.setLookAndFeel(plaf);
		} catch (UnsupportedLookAndFeelException e) {
			appendConsole("Look and feel failed: "+e.getMessage());
		} catch (ClassNotFoundException e) {
			appendConsole("Look and feel failed: "+e.getMessage());
		} catch (InstantiationException e) {
			System.err.println(e.toString());
		} catch (IllegalAccessException e) {
			System.err.println(e.toString());
		}
		
		// Color theme
		Iterator<?> it = colorHashMap.entrySet().iterator(); 
		while (it.hasNext()) {
			@SuppressWarnings("rawtypes")
			Map.Entry entry = (Map.Entry)it.next();
			Object key = entry.getKey();
			Object value = entry.getValue();
			if (value instanceof javax.swing.plaf.ColorUIResource) {
				ColorUIResource cr = (ColorUIResource) value;
				switch (Settings.getColourChoice()) {
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
		colorHashMap.clear();
		Enumeration<Object> keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
			Object key = keys.nextElement();
			Object value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.ColorUIResource) {
				colorHashMap.put(key, (ColorUIResource) value);
			}
		}
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
		appendConsole("Scanning "+dirName);
		File dir = new File(dirName);
		File[] files = dir.listFiles();
		Arrays.sort(files);
		int diskCount = 0;
		for (int i=0; i<files.length; i++) {
			if (files[i].isDirectory()) {
				diskCount += scanForD64Files(files[i].getAbsolutePath());
			} else if (files[i].isFile()) {
				String name = files[i].getName();
				boolean found = false;
				for (int j=0; !found && j<DiskImage.VALID_IMAGE_FILE_EXTENSTIONS.length; j++) {
					if (name.toLowerCase().endsWith(DiskImage.VALID_IMAGE_FILE_EXTENSTIONS[j])) {
						found = true;
					}
				}
				if (found) {
					String fileName = dirName + File.separator + name;
					try {
						DiskImage diskImage =  DiskImage.getDiskImage(fileName);
						diskImage.readBAM();
						diskImage.readDirectory();
					    Disk disk = diskImage.getDisk();
					    disk.setFilePath(dirName);
					    disk.setFileName(name);
						DaoFactory.getDaoFactory().getDiskDao().save(disk);
						diskCount++;
						appendConsole("Saved info for " + fileName);
					} catch (DatabaseException e) {
						appendConsole(fileName +" : "+e.getMessage());
					} catch (CbmException e) {
						appendConsole(fileName +" : "+e.getMessage());
					}
				}
			}
		}
		return diskCount;
	}

	public void setPluginButtonLabel(int num, String label) {
		if (num < pluginButtons.length && pluginButtons[num] != null) {
			pluginButtons[num].setText(label);
		}
	}
	
	private void writeFile(File outFile, String data) throws CbmException {
		if (outFile == null || data == null) {
			throw new CbmException("Required data is missing.");
		}
		try {
			PrintWriter out = new PrintWriter(outFile);
			out.println(data);
			out.close();
			appendConsole("Writo to file: " + outFile.getAbsolutePath());
		} catch (Exception e) {
			appendConsole("Failed to write to file: " + e.getMessage());
			throw new CbmException("Failed to write to file. "+e.getMessage());
		}
	}
	
	private final static String RELEASE_NOTES = 
			"Version 0.05b:" +
					"\n\t- Refactored code." +
					"\n\t- New look & feel." +
					"\n\t- Gzipped images." +
					"\n\t- Database support." +
			"\nVersion 0.065b:" +
					"\n\t- D71, D81 and T64 read support." +
					"\n\t- CP/M read support (D64, D71, D81)." +
					"\n\t- Click sector in BAM to see hex dump." +
					"\n\t- View text, hexdump and BASIC from files." +
					"\n\t- Unload image." +
					"\n\t- View Koala images." +
					"\n\t- Bug fix for insert prg into D64." +
					"\n\t- Merged the two consoles into one shared." +
					"\n\t- Clear console menu option." +
					"\n\t- Implemented delete files from D64"+
			"\nVersion 0.1b:" +
					"\n\t- Merged buttons panels into one shared set of buttons."+
					"\n\t- Refactored settings." +
					"\n\t- D81 partition read support." +
					"\n\t- Double click file to open hex view or D81 partition." +
					"\n\t- Validation of D64, D71 and D81." +
					"\n\t- Move disk image files up and down." +
					"\n\t- Browse local file system." +
					"\n\t- Color settings." +
					"\n\t- Change size of console." +
					"\n\t- Change look and feel from settings GUI." +
					"\n\t- Colors on buttons." +
					"\n\t- Save console to file."+
					"\n\t- View BASIC V7 (i.e. C128)." +
					"\n\t- D71 and D81 write support." +
					"\n\t- PostgreSQL database support." +
					"\n\t- Show SQL for setting up database." +
					"\n\t- Many bug fixes." +
			"\nVersion 0.11b:" +
					"\n\t- Edit BAM mode added." +
					"\n\t- Show C64 font when (re-)naming files and disks." +
					"\n\t- Printing added (file list, texts, hexdumps, images)." +
					"\n\t- View texts using C64 font mode."
					;
	
}
