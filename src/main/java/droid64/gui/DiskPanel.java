package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.D64;
import droid64.d64.D71;
import droid64.d64.D81;
import droid64.d64.DirEntry;
import droid64.d64.DiskImage;
import droid64.db.DaoFactory;
import droid64.db.DatabaseException;
import droid64.db.Disk;

/**<pre style='font-family:sans-serif;'>
 * Created on 23.06.2004<br/>
 *
 *   droiD64 - A graphical filemanager for D64 files<br/>
 *   Copyright (C) 2004 Wolfram Heyer<br/>
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
 *   eMail: wolfvoz@users.sourceforge.net <br/>
 *   http://droid64.sourceforge.net
 *
 * @author wolf
 * @author henrik
 */

/*
 * TODO: adjust table-size dynamically to number of directory entries (else: check if empty entry was selected)
 * TODO: delPRG: implement method
 */
public class DiskPanel extends JPanel implements TableModelListener {

	private static final long serialVersionUID = 1L;
	private static final String DIR_FONT_NAME = "droiD64_cbm.ttf";

	private static final Color DIR_BG_COLOR = new Color( 64, 64, 224);
	private static final Color DIR_FG_COLOR = new Color(160,160, 255);	

	private static final Color CPM_DIR_BG_COLOR = new Color( 16,  16,  16);
	private static final Color CPM_DIR_FG_COLOR = new Color(192, 255, 192);
	
	private static int MAX_PLUGINS = 2;

	private static Insets BUTTON_MARGINS = new Insets(1, 8, 1, 8);

	
	private DiskPanel otherDiskPanel;
	public DiskImage diskImage = null;
	private ExternalProgram[] externalProgram = new ExternalProgram[2];
	private EntryTableModel tableModel = new EntryTableModel();

	private JLabel diskLabel, diskName;
	private JTable table;
	JButton[] miscButton = new JButton[MAX_PLUGINS];
	/** File dialog for selecting image file to open */
	private JFileChooser chooser = null;
	
	private String newPRGName;
	private boolean newPRGNameSuccess;
	private int newPRGType;
	private String newDiskName;
	private String newDiskID;
	private int newDiskType;
	private boolean newDiskNameSuccess;
	private boolean isSetD64 = false;	//indicates whether a D64 was selected or not
	private String thisD64Name = "";
	private String loadName = "";
	private String saveName = "";
	private String directory = ".";
	private int rowHeight = 12;
	private boolean useDatabase = false;
	private MainPanel mainPanel;
	private JPanel diskLabelPane;
	private boolean newCompressedDisk = false;
	private boolean newCpmDisk = false;
		
	public DiskPanel(ExternalProgram[] externalProgram, int fontSize, MainPanel mainPanel) {
		this.externalProgram = externalProgram;
		this.mainPanel = mainPanel;
		rowHeight = fontSize + 2;
		JPanel dirPanel = drawDirPanel();
		JPanel choicePanel = drawChoicePanel();
		setupFont(fontSize);
		setLayout(new BorderLayout());

		JPanel feedbackChoicePanel = new JPanel();
		feedbackChoicePanel.setLayout(new BorderLayout());
		feedbackChoicePanel.add(choicePanel, BorderLayout.NORTH);
		
		add(dirPanel, BorderLayout.CENTER);
		add(feedbackChoicePanel, BorderLayout.SOUTH);
		setBorder(BorderFactory.createRaisedBevelBorder());
	}

//	private static Color ACTIVE_COLOR = Color.RED;
//	private static Color INACTIVE_COLOR = Color.GRAY;
	
	public void setActive(boolean active) {
// TODO: use this once buttons have been moved into one shared panel
//		if (active) {
//			setBackground(ACTIVE_COLOR);
//			if (otherDiskPanel != null) {
//				otherDiskPanel.setBackground(INACTIVE_COLOR);
//			}
//		} else {
//			setBackground(INACTIVE_COLOR);			
//			if (otherDiskPanel != null) {
//				otherDiskPanel.setBackground(ACTIVE_COLOR);
//			}
//		}
	}
	
	public void setupFont(int fontSize) {
		try {
			Font ttfFont = Font.createFont(Font.TRUETYPE_FONT,	getClass().getResourceAsStream("resources/"+DIR_FONT_NAME));
			Font scaledFont = ttfFont.deriveFont( new Float(fontSize).floatValue() );
			table.setFont(scaledFont);
			diskLabel.setFont(scaledFont);
		} catch (FontFormatException | IOException e) {
			mainPanel.appendConsole("\n"+e.getMessage());
		}
	}

	private JPanel drawDirPanel() {

		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());

		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		for (int i=0; i<tableModel.getColumnCount(); i++) {
			TableColumn col = new TableColumn(i, i == 2 ? 220 : 1);
			col.setHeaderValue(tableModel.getColumnName(i));
			columnModel.addColumn(col);
		}

		tableModel.addTableModelListener(this);
		table = new JTable(tableModel, columnModel);
		table.setToolTipText("The content of the disk");
		table.setGridColor(new Color(64, 64, 224));
		table.setRowHeight(rowHeight);
		table.setBackground(DIR_BG_COLOR);
		table.setForeground(DIR_FG_COLOR);

		diskLabel = new JLabel("NO DISK");
		diskLabel.setToolTipText("The label of the disk");
		diskLabel.setBackground(DIR_BG_COLOR);
		diskLabel.setForeground(DIR_FG_COLOR);

		diskLabelPane = new JPanel();		
		diskLabelPane.setBackground(DIR_BG_COLOR);
		diskLabelPane.setForeground(DIR_FG_COLOR);
		diskLabelPane.add(diskLabel);

		diskName = new JLabel("No file");
		diskName.setToolTipText("The name of the file");
		diskName.setFont(new Font("Verdana", Font.PLAIN, diskName.getFont().getSize()));

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	setActive(true);
		    }
		});
		
		dirPanel.add(new JScrollPane(diskLabelPane), BorderLayout.NORTH);
		dirPanel.add(scrollPane, BorderLayout.CENTER);
		dirPanel.add(new JScrollPane(diskName), BorderLayout.SOUTH);
		dirPanel.setPreferredSize(new Dimension(50, 300));
		return dirPanel;
	}

	private void showErrorMessage(String error){
		if (error == "noDisk"){
			mainPanel.appendConsole("\nNo disk image file selected. Aborting.");
			JOptionPane.showMessageDialog(mainPanel,
					"No disk loaded.\n"+
							"Open a disk image file first.",
							DroiD64.PROGNAME + " v" + DroiD64.VERSION + " - No disk",
							JOptionPane.ERROR_MESSAGE);
		}
		if (error == "insertError"){
			mainPanel.appendConsole("\nInserting error. Aborting.\n");
			JOptionPane.showMessageDialog(mainPanel,
					"An error occurred while inserting file into disk.\n"+
					"Look up console report message for further information.",
					DroiD64.PROGNAME + " v" + DroiD64.PROGNAME + " - Failure while inserting file",
					JOptionPane.ERROR_MESSAGE );
		}
	}

	public void tableChanged(TableModelEvent e) {
		TableModel model = (TableModel) e.getSource();
		// do something with the data
		table.setModel(model);
		table.revalidate();
	};

	private JPanel drawChoicePanel() {
		JPanel pluginButtonPanel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		pluginButtonPanel.add(new JLabel("Misc "));
		for (int i = 0; i < externalProgram.length; i++) {
			miscButton[i] = new JButton(externalProgram[i].getLabel());
			miscButton[i].setMargin(BUTTON_MARGINS);
			miscButton[i].setToolTipText(externalProgram[i].getDescription());
			miscButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					for (int cnt = 0; cnt < MAX_PLUGINS; cnt ++){
						if ( event.getSource()==miscButton[cnt] ){
							if (isSetD64 == false){
								showErrorMessage("noDisk");
								return;
							}
							doExternalProgram(cnt);
						}
					}
				}
			});
			pluginButtonPanel.add(miscButton[i]);
		}
		
		JPanel panel = new JPanel();
		if (mainPanel.isShowViewButtons()) {
			panel.setLayout(new GridLayout(4, 1, 0, 0));		
			panel.add(drawDiskButtonPanel());
			panel.add(drawFileOperationButtonPanel());
			panel.add(drawOpenFileButtonPanel());
			panel.add(pluginButtonPanel);
		} else {
			panel.setLayout(new GridLayout(3, 1, 0, 0));		
			panel.add(drawDiskButtonPanel());
			panel.add(drawFileOperationButtonPanel());
			panel.add(pluginButtonPanel);
		}
		return panel;
	}
	
	private JPanel drawDiskButtonPanel() {
		final JButton newButton = new JButton("New");
		newButton.setToolTipText("Create a new blank disk.");
		newButton.setMargin(BUTTON_MARGINS);
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == newButton) {
					newDiskImage();
				}
			}
		});
		final JButton loadButton =new JButton("Load");
		loadButton.setToolTipText("Open a disk.");
		loadButton.setMargin(BUTTON_MARGINS);
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == loadButton) {
					openDiskImage(openDiskImageFileDialog(directory), true);
				}
			}
		});
		final JButton unloadDiskButton = new JButton("Unload");
		unloadDiskButton.setToolTipText("Unload a loaded disk image.");
		unloadDiskButton.setMargin(BUTTON_MARGINS);
		unloadDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (isSetD64 && event.getSource() == unloadDiskButton) {
					unloadDisk();
				}
			}
		});
		final JButton showBamButton = new JButton("BAM");
		showBamButton.setToolTipText("Show the BAM of this disk.");
		showBamButton.setMargin(BUTTON_MARGINS);
		showBamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == showBamButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					} else {
						showBAM();
					}
				}
			}
		});
		final JButton renameDiskButton = new JButton("Rename");
		renameDiskButton.setToolTipText("Modify the label of the disk.");
		renameDiskButton.setMargin(BUTTON_MARGINS);
		renameDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == renameDiskButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					} else {
						renameDisk();
					}
				}
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		panel.add(new JLabel("Disk "));
		panel.add(newButton);
		panel.add(loadButton);
		panel.add(unloadDiskButton);
		panel.add(showBamButton);
		panel.add(renameDiskButton);
		return panel;
	}
	
	private JPanel drawOpenFileButtonPanel() {
		final JButton viewButton = new JButton("Text");
		viewButton.setToolTipText("Show text from selected file.");
		viewButton.setMargin(BUTTON_MARGINS);
		viewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == viewButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					showFile();
				}
			}
		});		
		final JButton hexViewButton = new JButton("Hex");
		hexViewButton.setToolTipText("Show hex dump of selected file.");
		hexViewButton.setMargin(BUTTON_MARGINS);
		hexViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == hexViewButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					hexViewFile();
				}
			}
		});

		final JButton basicViewButton = new JButton("Basic");
		basicViewButton.setToolTipText("Show BASIC listing from selected file.");
		basicViewButton.setMargin(BUTTON_MARGINS);
		basicViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == basicViewButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					basicViewFile();
				}
			}
		});
		
		final JButton imageViewButton = new JButton("Image");
		imageViewButton.setToolTipText("Show image from selected file.");
		imageViewButton.setMargin(BUTTON_MARGINS);
		imageViewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == imageViewButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					imageViewFile();
				}
			}
		});
		
		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		panel.add(new JLabel("View "));
		panel.add(viewButton);
		panel.add(hexViewButton);
		panel.add(basicViewButton);
		panel.add(imageViewButton);
		return panel;
	}
	
	private JPanel drawFileOperationButtonPanel() {
		final JButton insertButton = new JButton("In");
		insertButton.setToolTipText("Insert a PRG file into this disk.");
		insertButton.setMargin(BUTTON_MARGINS);
		insertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == insertButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					importPRG();
				}
			}
		});
		final JButton extractButton = new JButton("Out");
		extractButton.setToolTipText("Extract a PRG file from this disk.");
		extractButton.setMargin(BUTTON_MARGINS);
		extractButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == extractButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					exportPRG();
				}
			}
		});
		final JButton copyButton = new JButton("Copy");
		copyButton.setToolTipText("Copy a PRG file to the other disk.");
		copyButton.setMargin(BUTTON_MARGINS);
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == copyButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					copyPRG();
				}
			}
		});
		final JButton renamePRGButton = new JButton("Rename");
		renamePRGButton.setToolTipText("Modify a PRG file in this disk.");
		renamePRGButton.setMargin(BUTTON_MARGINS);
		renamePRGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == renamePRGButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					renamePRG();
				}
			}
		});
		final JButton delPRGButton = new JButton("Delete");
		delPRGButton.setToolTipText("Delete a PRG file in this disk.");
		delPRGButton.setMargin(BUTTON_MARGINS);
		delPRGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == delPRGButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					delPRG();
				}
			}
		});

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.CENTER, 2, 2));
		panel.add(new JLabel("File "));
		panel.add(insertButton);
		panel.add(extractButton);
		panel.add(copyButton);
		panel.add(renamePRGButton);
		panel.add(delPRGButton);
		return panel;
	}
	
	private String openDiskImageFileDialog(String directory) {		
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
		}
		if (chooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			directory = chooser.getSelectedFile().getParent();
			return chooser.getSelectedFile()+"";
		}
		return null;
	}							

	private File[] openPRGFileDialog(String directory) {
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".prg");
				}
			}
			public String getDescription () { return "PRG files"; }  
		};
		JFileChooser chooser = new JFileChooser(directory);		
		chooser.addChoosableFileFilter(fileFilter);
		chooser.setFileFilter(fileFilter);
		chooser.setMultiSelectionEnabled(true);
		if (chooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			this.directory = chooser.getSelectedFile().getParent();
			return chooser.getSelectedFiles();
		}
		return null;
	}							

	private String saveDiskImageFileDialog(String directory) {
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
		JFileChooser chooser = new JFileChooser(directory);
		chooser.addChoosableFileFilter(fileFilter);
		chooser.setFileFilter(fileFilter);
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			this.directory = chooser.getSelectedFile().getParent();
			return chooser.getSelectedFile()+"";
		}
		return null;
	}							

	private String savePRGFileDialog(String directory) {
		JFileChooser chooser = new JFileChooser(directory);
		chooser.setDialogTitle("Choose a target directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showDialog(null, "Select")==JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getCurrentDirectory();
			File file = chooser.getSelectedFile();
			this.directory = dir.getPath()+File.separator;
			this.directory += file.getName()+File.separator;
			return this.directory;
		}
		return null;
	}

	public void openDiskImage(String fileName, boolean updateList) {
		if (fileName != null) {
			loadName = fileName;
			isSetD64 = true;
			thisD64Name = loadName;
			reloadDiskImage(updateList);
		}
	}

	private void doExternalProgram(int which_one){
		mainPanel.appendConsole("Executing \"" + externalProgram[which_one].getCommand() + "\" on disk " + loadName);

		String selectedFile = null;
		for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
			if (table.isRowSelected(i)) {
				selectedFile = diskImage.getCbmFile(i).getName();
				break;
			}
		}
		final String[] args = new String[2];
		args[0] = externalProgram[which_one].getCommand();
		if (selectedFile != null) {
			args[1] = loadName+":"+selectedFile.toLowerCase();	// FIXME: toLowerCase might not be the best here
		} else {
			args[1] = loadName;
		}
		try {
			Thread runner = new Thread() {
				public void run() {
					try {
						Runtime rt = Runtime.getRuntime();
						Process pr = rt.exec(args);
						BufferedReader procout = new BufferedReader(new InputStreamReader(pr.getInputStream()));
						String line;
						while ((line = procout.readLine()) != null)	{
							mainPanel.appendConsole(line);
						}						
					} catch (Exception e) {
						mainPanel.appendConsole("\n"+e.getMessage());
					}
				}
			};
			runner.start();
		} catch (Exception e) {
			mainPanel.appendConsole("\n"+e.getMessage());
		}
	}

	private void showDirectory() {
		mainPanel.appendConsole("There are "	+ diskImage.getFilenumberMax() + " files in this image file.");		
		for (int fileNum = 0; fileNum <= diskImage.getFilenumberMax() - 1;	fileNum++) {
			tableModel.updateDirEntry(new DirEntry(diskImage.getCbmFile(fileNum), fileNum + 1));
		}
		diskLabel.setText(
				diskImage.getBam().getDiskDosType()
				+ " \""	+ diskImage.getBam().getDiskName() + ","
				+ diskImage.getBam().getDiskId()
				+ "\" "+diskImage.getBlocksFree()
				+ " BLOCKS FREE [" + diskImage.getFilenumberMax() + "]"
				);
		tableModel.setCpmMode(diskImage.getCpmFormat() != DiskImage.CPM_TYPE_UNKNOWN );
		setTableColors(diskImage.getCpmFormat());
		TableColumnModel tcm = table.getTableHeader().getColumnModel();
		for (int i=0; i< tcm.getColumnCount(); i++) {
			tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
		}
		table.revalidate();
		repaint();
	}
	
	private void showBAM() {
		String diskName =
				diskImage.getBam().getDiskDosType() + " \"" +
				diskImage.getBam().getDiskName() + "," +
				diskImage.getBam().getDiskId() + "\"";
		new BAMFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - BAM of this disk image", diskName, diskImage.getBamTable(), diskImage);
	}

	private void updateD64File() {
		diskImage.readBAM();
		diskImage.readDirectory();
		mainPanel.appendConsole(diskImage.getFeedbackMessage());

		if (useDatabase) {
			Disk disk = diskImage.getDisk();
			File f = new File(thisD64Name);
			File p = f.getAbsoluteFile().getParentFile();
			String d = p != null ? p.getAbsolutePath() : null;
			disk.setFilePath(d);
			disk.setFileName(f.getName());
			try {
				DaoFactory.getDaoFactory().getDiskDao().save(disk);
			} catch (DatabaseException e) {
				mainPanel.appendConsole("\n"+e.getMessage());
			}
		}
	}

	private void copyPRG() {
		int thisCbmFile;
		diskImage.setFeedbackMessage("");
		boolean success = false;
		boolean writeD64File = false;
		mainPanel.appendConsole("CopyPRG;");
		if (otherDiskPanel.isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}
		mainPanel.appendConsole("The other D64 is \""+otherDiskPanel.getThisD64Name()+"\"");
		//TODO: copyPRG: what to do if filename exists?
		mainPanel.appendConsole("Selected files;");
		for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
			if (table.isRowSelected(i)) {
				try {
					//thisCbmFile = d64.getCbmFile(i).getDirPosition();
					thisCbmFile = i;
					mainPanel.appendConsole("[" + i + "] " + tableModel.getValueAt(i,2) + " DirPosition = "+thisCbmFile);					
					// load the PRG file and write its data into d64.saveData, write its size into d64.saveDataSize
					byte[] saveData = diskImage.writeSaveData(thisCbmFile);
					mainPanel.appendConsole(diskImage.getFeedbackMessage());

					diskImage.setFeedbackMessage("");

					//make d64.saveData and d64.saveDataSize known to the other DiskPanel
					//otherDiskPanel.d64.setSaveData(d64.getSaveData());
					//otherDiskPanel.d64.setSaveDataSize(saveData.length);

					/*
					 * Make d64.cbmFile known to the other DiskPanel and write it to the D64 of the other DiskPanel
					 * Keep in mind: We cannot just copy the object "cbmFile", but we have to copy its values!
					 */
					
					otherDiskPanel.diskImage.getBufferCbmFile().copy(diskImage.getCbmFile(thisCbmFile));
					success = otherDiskPanel.diskImage.saveFile(
							diskImage.getCbmFile(thisCbmFile).getName(),
							diskImage.getCbmFile(thisCbmFile).getFileType(),
							true, saveData
							);
					mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
				} catch (CbmException e) {
					mainPanel.appendConsole("Failure while copying file. \n" + e.getMessage());
					success = false;
				}

				if (success){
					writeD64File = true;
				}
				else {
					mainPanel.appendConsole("\nFailure while copying file.");
				}

			}
		}
		if (writeD64File){
			// write the D64 of the other Diskpanel to disk
			success = otherDiskPanel.diskImage.writeImage(otherDiskPanel.getThisD64Name());
			mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
			if (success == false) {
				mainPanel.appendConsole("Failure while writing D64 file.");				
				showErrorMessage("insertError");
			}
		}
		else {
			showErrorMessage("insertError");
		}
		otherDiskPanel.reloadDiskImage(true);

		// we have to reload the current D64, because the selection will mess up otherwise (exceptions...)
		clearDirTable();
		updateD64File();
		showDirectory();
		//		reloadD64();
	}

	private void hexViewFile() {
		for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
			if (table.isRowSelected(i)) {
				try {
					mainPanel.appendConsole("Hex view " + tableModel.getValueAt(i,2));
					byte[] data = diskImage.writeSaveData(i);
					new HexViewFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Hex view", diskImage.getCbmFile(i).getName(), data, data.length);
				} catch (CbmException e) {
					mainPanel.appendConsole("Hex view failed " + e.getMessage());
				}
			}
		}
	}
	
	private void basicViewFile() {
		for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
			if (table.isRowSelected(i)) {
				try {
					mainPanel.appendConsole("BASIC view " + tableModel.getValueAt(i,2));
					String basic = diskImage.parseCbmBasicPrg(diskImage.writeSaveData(i));
					if (basic != null && !basic.isEmpty()) {
						new TextViewFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - BASIC view", basic);
					} else {
						mainPanel.appendConsole("\nFailed to parse BASIC.");
					}
				} catch (CbmException e) {
					mainPanel.appendConsole("BASIC view failed " + e.getMessage());
				}
			}
		}
	}
	
	private void imageViewFile() {
		List<byte[]> imgList = new ArrayList<byte[]>();
		try {
			for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
				if (table.isRowSelected(i)) {
					mainPanel.appendConsole("\nImage view " + tableModel.getValueAt(i,2));
					byte[] data = diskImage.writeSaveData(i);
					if (data != null && data.length > 0) {
						imgList.add(data);
					}
				}
			}
			if (!imgList.isEmpty()) {
				new ViewImageFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Image view", imgList);
			}
		} catch (CbmException | IOException e) {
			mainPanel.appendConsole("\nImage view failed " + e.getMessage());
		}
	}
	
	private void showFile() {
		for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
			if (table.isRowSelected(i)) {
				try {
					mainPanel.appendConsole("\nView " + tableModel.getValueAt(i,2));
					byte[] data = diskImage.writeSaveData(i);
					new TextViewFrame("View - "+diskImage.getCbmFile(i).getName(), data);
				} catch (CbmException e) {
					mainPanel.appendConsole("\nView failed " + e.getMessage());
				}
			}
		}
	}
	
	private void exportPRG() {
		saveName = savePRGFileDialog(directory);
		if (saveName != null) {
			mainPanel.appendConsole("\nExportPRG: Selected folder: '"+saveName+"'.");
			for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
				if (table.isRowSelected(i)) {
					try {
						diskImage.exportPRG(i, directory);
						mainPanel.appendConsole(diskImage.getFeedbackMessage());
					} catch (CbmException e) {
						mainPanel.appendConsole("Export file failed " + e.getMessage());
					}
				}
			}
		}
	}

	private void importPRG() {
		boolean success = true;
		mainPanel.appendConsole("InsertPRG.");
		File[] loadNames = openPRGFileDialog(directory);
		//TODO: insertPRG: what to do if filename exists?
		if (loadNames != null) {
			try {
				for (int i = 0; success && i < loadNames.length; i++) {
					//filename with path
					loadName = loadNames[i].getAbsolutePath();
					mainPanel.appendConsole("\nSelected file: \""+loadName+"\".");
					byte[] saveData = diskImage.readPRG(loadName);
					mainPanel.appendConsole(diskImage.getFeedbackMessage());
					//filename without path
					loadName = loadNames[i].getName();
					success = diskImage.saveFile(
							loadName,
							D64.TYPE_PRG, 	//TODO: this always sets new FileType = PRG
							false, saveData
							);	
					mainPanel.appendConsole(diskImage.getFeedbackMessage());
				}
			} catch (CbmException e) {
				mainPanel.appendConsole(diskImage.getFeedbackMessage());
				mainPanel.appendConsole("\nFailure.\n"+e.getMessage());
				success = false;
			}
			if (success){
				success = diskImage.writeImage(thisD64Name);
				mainPanel.appendConsole(diskImage.getFeedbackMessage());

			} else {
				mainPanel.appendConsole("\nFailure.");
				showErrorMessage("insertError");
			}
			reloadDiskImage(true);
			otherDiskPanel.reloadDiskImage(true);
		}
	}

	private void newDiskImage() {
		mainPanel.appendConsole("New disk image.");
		newDiskName = "";
		new RenameD64Frame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - New disk",	this, "", "", true );
		if (newDiskNameSuccess) {
			mainPanel.appendConsole("\nNew Diskname is: \""+newDiskName+", "+newDiskID+"\".");
			String saveName = DiskImage.checkFileNameExtension(newDiskType, newCompressedDisk, saveDiskImageFileDialog(directory));
			if ( saveName != null) {
				mainPanel.appendConsole("Selected file: \""+saveName+"\".");
				if (newDiskType == DiskImage.D64_IMAGE_TYPE) {
					diskImage = new D64();					
					diskImage.setCpmFormat(newCpmDisk ? DiskImage.CPM_TYPE_D64_C128 : DiskImage.CPM_TYPE_UNKNOWN);
				} else if (newDiskType == DiskImage.D81_IMAGE_TYPE) {
					diskImage = new D81();
					diskImage.setCpmFormat(newCpmDisk ? DiskImage.CPM_TYPE_D71 : DiskImage.CPM_TYPE_UNKNOWN);
				} else if (newDiskType == DiskImage.D71_IMAGE_TYPE) {
					diskImage = new D71();
					diskImage.setCpmFormat(newCpmDisk ? DiskImage.CPM_TYPE_D81 : DiskImage.CPM_TYPE_UNKNOWN);
				} else {
					mainPanel.appendConsole("Filename with unknown file extension. Can't detect format.\n");
					return;
				}
				diskImage.setCompressed(newCompressedDisk);
				diskImage.saveNewImage(saveName, newDiskName, newDiskID);
				mainPanel.appendConsole(diskImage.getFeedbackMessage());
				isSetD64 = true;
				thisD64Name = saveName;
				reloadDiskImage(true);
			}
		}
	}
	
	private void renameDisk() {
		mainPanel.appendConsole("Rename disk.");
		newDiskName = "";
		new RenameD64Frame(
				DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Rename disk",
				this,
				diskImage.getBam().getDiskName(),
				diskImage.getBam().getDiskId(), false
				);
		if (newDiskNameSuccess) {
			mainPanel.appendConsole("New diskname is: \""+newDiskName+", "+newDiskID+"\".");
			saveName = thisD64Name;
			diskImage.renameImage(saveName, newDiskName, newDiskID);
			mainPanel.appendConsole(diskImage.getFeedbackMessage());
			reloadDiskImage(true);
		}
	}

	private void unloadDisk() {
		mainPanel.appendConsole("Unload disk.");
		diskImage = null;
		diskName.setText("");
		isSetD64 = false;
		diskName.setText("No file");
		diskLabel.setText("NO DISK");
		clearDirTable();
		table.revalidate();
	}
	
	private void renamePRG() {
		mainPanel.appendConsole("RenamePRG:");
		for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
			if (table.isRowSelected(i)) {
				newPRGName = "";
				new RenamePRGFrame(
						DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Rename file ",
						this,
						diskImage.getCbmFile(i).getName(),
						diskImage.getCbmFile(i).getFileType()
						);

				if (newPRGNameSuccess) {
					mainPanel.appendConsole("\nNew filename is: \""+newPRGName+", "+newPRGType+"\".\n" +
							"Information about file number "+i+":\n" +
							"Name: "+diskImage.getCbmFile(i).getName()+"\n" +
							"DirPosition: "+diskImage.getCbmFile(i).getDirPosition()+"\n" +
							"DirTrack: "+diskImage.getCbmFile(i).getDirTrack()+"\n" +
							"DirSector: "+diskImage.getCbmFile(i).getDirSector()+"\n" +
							"SizeInBytes: "+diskImage.getCbmFile(i).getSizeInBytes());
					diskImage.renamePRG(i,newPRGName, newPRGType);
					mainPanel.appendConsole(diskImage.getFeedbackMessage());
					diskImage.writeImage(thisD64Name);
					mainPanel.appendConsole(diskImage.getFeedbackMessage());
				}
			}
		}
		reloadDiskImage(true);
	}

	private void delPRG() {
		try {
			for (int i = 0; i < diskImage.getFilenumberMax(); i++) {
				if (table.isRowSelected(i)) {
					mainPanel.appendConsole("Delete [" + i + "] " + tableModel.getValueAt(i, 2));				
					diskImage.deleteFile(diskImage.getCbmFile(i));
					mainPanel.appendConsole(diskImage.getFeedbackMessage());
				}
			}
			diskImage.writeImage(thisD64Name);
			reloadDiskImage(true);
		} catch (CbmException e) {
			mainPanel.appendConsole("\nError: "+e.getMessage());
		}
	}

	private void clearDirTable(){
		tableModel.clear();
	}

	public void setOtherDiskPanelObject ( DiskPanel otherOne ) {
		otherDiskPanel = otherOne;
	}

	/**
	 * @return
	 */
	public String getThisD64Name() {
		return thisD64Name;
	}

	public void reloadDiskImage(boolean updateList){
		if (isSetD64){
			try {
				diskImage = DiskImage.getDiskImage(thisD64Name);
				diskName.setText(thisD64Name);				
				clearDirTable();
				updateD64File();
				if (updateList) {
					showDirectory();
				}
				mainPanel.setStatusBar("Loaded "+thisD64Name);
			} catch (CbmException e) {
				mainPanel.appendConsole("\nError: "+e.getMessage());
				diskName.setText("No file");
				diskLabel.setText("NO DISK");
				isSetD64 = false;
				repaint();				
			}
		}
	}

	private void setTableColors(int cpmMode) {
		if (cpmMode == DiskImage.CPM_TYPE_UNKNOWN) {
			table.setGridColor(new Color(64, 64, 224));
			table.setBackground(DIR_BG_COLOR);
			table.setForeground(DIR_FG_COLOR);
			diskLabel.setBackground(DIR_BG_COLOR);
			diskLabel.setForeground(DIR_FG_COLOR);
			diskLabelPane.setBackground(DIR_BG_COLOR);
			diskLabelPane.setForeground(DIR_FG_COLOR);
		} else {
			table.setGridColor(CPM_DIR_BG_COLOR);
			table.setBackground(CPM_DIR_BG_COLOR);
			table.setForeground(CPM_DIR_FG_COLOR);
			diskLabel.setBackground(CPM_DIR_BG_COLOR);
			diskLabel.setForeground(CPM_DIR_FG_COLOR);						
			diskLabelPane.setBackground(CPM_DIR_BG_COLOR);
			diskLabelPane.setForeground(CPM_DIR_FG_COLOR);
		}
	}
	
	/**
	 * create a help drag-down menu (just for testing)
	 * @return
	 */
	public JMenu createDiskImageMenu(String title, String mnemonic) {

		JMenu menu = new JMenu(title);
		menu.setMnemonic(mnemonic.charAt(0));

		JMenuItem menuItem;
		menuItem = new JMenuItem("New Disk", 'n');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "New Disk"){
					newDiskImage();
				}
			}
		});
		menuItem = new JMenuItem("Load Disk", 'l');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Load Disk"){
					openDiskImage(openDiskImageFileDialog(directory), true);

				}
			}
		});
		menuItem = new JMenuItem("Show BAM", 'b');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Show BAM"){
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					showBAM();
				}
			}
		});
		menuItem = new JMenuItem("Unload Disk", 'u');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Unload Disk"){
					unloadDisk();
				}
			}
		});
		menu.addSeparator();
		menuItem = new JMenuItem("Insert File", 'i');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Insert File"){
					importPRG();
				}
			}
		});
		menuItem = new JMenuItem("Extract File", 'e');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Extract File"){
					exportPRG();
				}
			}
		});
		menuItem = new JMenuItem("Copy File", 'c');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Copy File"){
					copyPRG();
				}
			}
		});

		menu.addSeparator();
		menuItem = new JMenuItem("Rename Disk", 'r');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Rename Disk"){
					renameDisk();
				}
			}
		});
		menuItem = new JMenuItem("Rename File", 'f');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Rename File"){
					renamePRG();
				}
			}
		});
		menuItem = new JMenuItem("Delete File", 'd');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Delete File"){
					delPRG();
				}
			}
		});
		menu.addSeparator();

		
		menuItem = new JMenuItem("View Text File", 't');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "View Text File"){
					showFile();
				}
			}
		});
		menuItem = new JMenuItem("View Hex File", 'h');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "View Hex File"){
					hexViewFile();
				}
			}
		});
		menuItem = new JMenuItem("View BASIC File", 'h');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "View BASIC File"){
					basicViewFile();
				}
			}
		});		
		menuItem = new JMenuItem("View Image File", 'h');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "View Image File"){
					imageViewFile();
				}
			}
		});	
		return menu;
	}

	/**
	 * @param string
	 */
	public void setNewDiskName(String string) {
		newDiskName = string;
	}

	/**
	 * @param b
	 */
	public void setNewDiskNameSuccess(boolean b) {
		newDiskNameSuccess = b;
	}

	/**
	 * @param string
	 */
	public void setNewDiskID(String string) {
		newDiskID = string;
	}

	/**
	 * @param string
	 */
	public void setNewPRGName(String string) {
		newPRGName = string;
	}

	/**
	 * @param b
	 */
	public void setNewPRGNameSuccess(boolean b) {
		newPRGNameSuccess = b;
	}

	/**
	 * @param i
	 */
	public void setNewPRGType(int i) {
		newPRGType = i;
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
		table.setRowHeight(rowHeight);
		table.revalidate();
	}

	/**
	 * @return
	 */
	public ExternalProgram getExternalProgram(int which_one) {
		return externalProgram[which_one];
	}

	/**
	 * @param programs
	 */
	public void setExternalProgram(ExternalProgram[] programs) {
		externalProgram = programs;
	}

	public void setDirectory(String dir) {
		this.directory = dir;
	}

	public boolean isUseDatabase() {
		return useDatabase;
	}

	public void setUseDatabase(boolean useDatabase) {
		this.useDatabase = useDatabase;
	}

	public void setNewDiskType(int diskType) {
		this.newDiskType = diskType;
	}
	
	public void setNewCompressedDisk(boolean isCompressed) {
		this.newCompressedDisk = isCompressed;
	}

	public void setNewCpmDisk(boolean isCpm) {
		this.newCpmDisk = isCpm;
	}
	
}
