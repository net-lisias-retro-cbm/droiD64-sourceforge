package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

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
import javax.swing.JTextArea;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.D64;
import droid64.d64.DirEntry;
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
 */

/*
 * TODO: adjust table-size dynamically to number of directory entries (else: check if empty entry was selected)
 * TODO: delPRG: implement method
 */
public class DiskPanel extends JPanel implements TableModelListener {

	private static final long serialVersionUID = 1L;
	private static final String DIR_FONT_NAME = "droiD64_cbm.ttf";

	private static final int FEEDBACK_PANEL_ROWS = 10;
	private static final int FEEDBACK_PANEL_COLS = 40;

	private static final Color DIR_BG_COLOR = new Color(64,64,224);
	private static final Color DIR_FG_COLOR = new Color(160,160,255);	

	private static int MAX_PLUGINS = 2;

	private DiskPanel otherDiskPanel;
	public D64 d64 = new D64();
	private DirEntry dirEntry;
	private ExternalProgram[] externalProgram = new ExternalProgram[2];
	private EntryTableModel tm = new EntryTableModel();

	private JLabel diskLabel, diskName;
	private JTable table;
	private JTextArea textArea;
	JButton[] miscButton = new JButton[MAX_PLUGINS];

	private String newPRGName;
	private boolean newPRGNameSuccess;
	private int newPRGType;
	private String newDiskName;
	private String newDiskID;
	private boolean newDiskNameSuccess;
	private boolean isSetD64 = false;	//indicates whether a D64 was selected or not
	private String thisD64Name = "";
	private String loadName = "";
	private String saveName = "";
	private String directory = ".";
	private String feedBackMessage = "";
	private int rowHeight = 12;
	private boolean useDatabase = false;
	private MainPanel mainPanel;

	public DiskPanel(ExternalProgram[] externalProgram, int fontSize, MainPanel mainPanel) {
		this.externalProgram = externalProgram;
		this.mainPanel = mainPanel;
		rowHeight = fontSize + 2;
		JPanel dirPanel = drawDirPanel();
		JPanel choicePanel = drawChoicePanel();
		JPanel feedBackPanel = drawFeedBackPanel();
		setupFont(fontSize);
		setLayout(new BorderLayout());

		JPanel feedbackChoicePanel = new JPanel();
		feedbackChoicePanel.setLayout(new BorderLayout());
		feedbackChoicePanel.add(choicePanel, BorderLayout.NORTH);
		feedbackChoicePanel.add(feedBackPanel, BorderLayout.CENTER);
		
		add(dirPanel, BorderLayout.CENTER);
		add(feedbackChoicePanel, BorderLayout.SOUTH);
		setBorder(BorderFactory.createRaisedBevelBorder());
	}

	public void setupFont(int fontSize) {
		try {
			Font ttfFont = Font.createFont(Font.TRUETYPE_FONT,	getClass().getResourceAsStream("resources/"+DIR_FONT_NAME));
			Font scaledFont = ttfFont.deriveFont( new Float(fontSize).floatValue() );
			table.setFont(scaledFont);
			diskLabel.setFont(scaledFont);
		} catch (FileNotFoundException e) {
			feedBackMessage += "\n" + e + "\n";
		} catch (FontFormatException e) {
			feedBackMessage += "\n" + e + "\n";
		} catch (IOException e) {
			feedBackMessage += "\n" + e + "\n";
		}
	}

	private JPanel drawDirPanel() {

		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());

		//Table Column-Width		
		DefaultTableColumnModel cm = new DefaultTableColumnModel();

		for (int i=0; i<tm.getColumnCount(); i++) {
			TableColumn col = new TableColumn(i, i == 2 ? 220 : 1);
			col.setHeaderValue(tm.getColumnName(i));
			cm.addColumn(col);
		}

		tm.addTableModelListener(this);

		table = new JTable(tm, cm);
		table.setToolTipText("The content of the disk");
		table.setGridColor(new Color(64, 64, 224));
		table.setRowHeight(rowHeight);
		table.setBackground(DIR_BG_COLOR);
		table.setForeground(DIR_FG_COLOR);

		diskLabel = new JLabel("NO DISK");
		diskLabel.setToolTipText("The label of the disk");
		diskLabel.setBackground(DIR_BG_COLOR);
		diskLabel.setForeground(DIR_FG_COLOR);

		JPanel diskLabelPane = new JPanel();		
		diskLabelPane.setBackground(DIR_BG_COLOR);
		diskLabelPane.setForeground(DIR_FG_COLOR);
		diskLabelPane.add(diskLabel);

		diskName = new JLabel("No file");
		diskName.setToolTipText("The name of the file");
		diskName.setFont(new Font("Verdana", Font.PLAIN, diskName.getFont().getSize()));

		dirPanel.add(new JScrollPane(diskLabelPane), BorderLayout.NORTH);
		dirPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		dirPanel.add(new JScrollPane(diskName), BorderLayout.SOUTH);
		dirPanel.setPreferredSize(new Dimension(50, 300));
		return dirPanel;
	}

	private void showErrorMessage(String error){
		if (error == "noDisk"){
			feedBackMessage += "\nNo D64 file selected. Aborting...\n\n";
			textArea.setText(feedBackMessage);
			JOptionPane.showMessageDialog(
					null,
					"No disk loaded.\n"+
							"Open a D64 image first.",
							DroiD64.PROGNAME+" v"+ DroiD64.VERSION+" - No disk",
							JOptionPane.ERROR_MESSAGE
					);
		}
		if (error == "insertError"){
			feedBackMessage += "\nInserting error. Aborting...\n\n";
			textArea.setText(feedBackMessage);
			JOptionPane.showMessageDialog(
					null,
					"An error occurred while inserting file into disk.\n"+
							"Look up console report message for further information.",
							DroiD64.PROGNAME+" v"+ DroiD64.PROGNAME+" - Failure while inserting file",
							JOptionPane.ERROR_MESSAGE
					);
		}
	}

	public void tableChanged(TableModelEvent e) {
		TableModel model = (TableModel) e.getSource();
		// do something with the data
		table.setModel(model);
		table.revalidate();
	};

	private JPanel drawChoicePanel() {
		JPanel choice1Panel = new JPanel();
		JPanel choice2Panel = new JPanel();
		JPanel choice3Panel = new JPanel();

		final JButton newButton = new JButton("New");
		newButton.setToolTipText("Create a new blank disk.");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == newButton) {
					newD64();
				}
			}
		});

		final JButton loadButton =new JButton("Load");
		loadButton.setToolTipText("Open a disk.");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == loadButton) {
					openD64(openD64FileDialog(directory), true);
				}
			}
		});

		final JButton showBamButton = new JButton("BAM");
		showBamButton.setToolTipText("Show the BAM of this disk.");
		showBamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == showBamButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					showBAM();
				}
			}
		});

		final JButton renameDiskButton = new JButton("Rename");
		renameDiskButton.setToolTipText("Modify the label of the disk.");
		renameDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == renameDiskButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}	
					renameDisk();
				}
			}
		});
		
		JLabel diskTxtLabel = new JLabel("Disk");
		choice1Panel.add(diskTxtLabel);

		choice1Panel.add(newButton);
		choice1Panel.add(loadButton);
		choice1Panel.add(showBamButton);
		choice1Panel.add(renameDiskButton);

		final JButton insertButton = new JButton("In");
		insertButton.setToolTipText("Insert a PRG file into this disk.");
		insertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == insertButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					insertPRG();
				}
			}
		});

		final JButton extractButton = new JButton("Out");
		extractButton.setToolTipText("Extract a PRG file from this disk.");
		extractButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == extractButton) {
					if (isSetD64 == false){
						showErrorMessage("noDisk");
						return;
					}
					extractPRG();
				}
			}
		});

		final JButton copyButton = new JButton("Copy");
		copyButton.setToolTipText("Copy a PRG file to the other disk.");
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



		final JButton hexViewButton = new JButton("Hex view");
		hexViewButton.setToolTipText("Show hex dump of selected file.");
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

		JLabel fileLabel = new JLabel("File"); 
		choice2Panel.add(fileLabel);

		choice2Panel.add(insertButton);
		choice2Panel.add(extractButton);
		choice2Panel.add(renamePRGButton);
		choice2Panel.add(delPRGButton);
		choice2Panel.add(copyButton);
		choice2Panel.add(hexViewButton);

		JLabel miscLabel = new JLabel("Misc"); 
		choice3Panel.add(miscLabel);


		for (int i = 0; i < externalProgram.length; i++) {
			miscButton[i] = new JButton(externalProgram[i].getLabel());
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
			choice3Panel.add(miscButton[i]);
		}
		
		JPanel choicePanel = new JPanel();
		choicePanel.setLayout(new GridLayout(3,1));		
		choicePanel.add(choice1Panel);
		choicePanel.add(choice2Panel);
		choicePanel.add(choice3Panel);
		return choicePanel;
	}
	
	private JPanel drawFeedBackPanel() {
		JPanel feedBackPanel = new JPanel();
		feedBackPanel.setLayout(new BorderLayout());
		JLabel feedBackLabel = new JLabel("Output:");
		feedBackLabel.setToolTipText("The status-report is displayed here.");
		textArea = new JTextArea(FEEDBACK_PANEL_ROWS, FEEDBACK_PANEL_COLS);
		textArea.setToolTipText("The status-report is displayed here.");
		textArea.setEditable(false);
		feedBackPanel.add(feedBackLabel, BorderLayout.NORTH);
		feedBackPanel.add(new JScrollPane(textArea), BorderLayout.SOUTH);
		return feedBackPanel;
	}

	private String openD64FileDialog(String directory) {		
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".d64") || f.getName().toLowerCase().endsWith(".d64.gz");
				}
			}
			public String getDescription () { return "D64 images"; }  
		};
		JFileChooser chooser = new JFileChooser(directory);
		chooser.addChoosableFileFilter(fileFilter);
		chooser.setFileFilter(fileFilter);
		chooser.setMultiSelectionEnabled(false);
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

	private String saveD64FileDialog(String directory) {
		FileFilter fileFilter = new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					return f.getName().toLowerCase().endsWith(".d64") || f.getName().toLowerCase().endsWith(".d64.gz");
				}
			}
			public String getDescription () { return "D64 images"; }  
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

	public void openD64(String fileName, boolean updateList) {
		if (fileName != null) {
			loadName = fileName;
			isSetD64 = true;
			thisD64Name = loadName;
			reloadD64(updateList);
		}
	}

	private void doExternalProgram(int which_one){
		feedBackMessage += "Executing \"" + externalProgram[which_one].getCommand() + "\" on disk " + loadName + "\n";
		
		String selectedFile = null;
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				selectedFile = d64.getCbmFile(i).getName();
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
							feedBackMessage += line + "\n";
						}						
					} catch (Exception e) {
						feedBackMessage += "\n" + e.getMessage() + "\n";
						textArea.setText(feedBackMessage);
					}
				}
			};
			runner.start();
		} catch (Exception e) {
			feedBackMessage += "\n" + e.getMessage() + "\n";
			textArea.setText(feedBackMessage);
		}
	}

	private void showDirectory() {

		feedBackMessage += "There are "	+ d64.getFilenumber_max() + " files in this D64 file.\n\n";

		for (int filenumber = 0; filenumber <= d64.getFilenumber_max() - 1;	filenumber++) {
			boolean isLocked = d64.getCbmFile(filenumber).isFile_locked();
			boolean isClosed = d64.getCbmFile(filenumber).isFile_closed();
			String flags = (isLocked ? "<" : "") + (isClosed ? "" : "*");
			dirEntry = new DirEntry();
			dirEntry.setNumber(filenumber + 1);
			dirEntry.setBlocks(d64.getCbmFile(filenumber).getSizeInBlocks());
			dirEntry.setName( " \"" + d64.getCbmFile(filenumber).getName() + "\"" );
			dirEntry.setType( d64.getFileType(d64.getCbmFile(filenumber).getFile_type()) );
			dirEntry.setFlags( flags );
			dirEntry.setTrack( d64.getCbmFile(filenumber).getTrack() );
			dirEntry.setSector( d64.getCbmFile(filenumber).getSector() );
			tm.updateDirEntry(dirEntry);
		}

		diskLabel.setText(
				d64.getBam().getDiskDosType()
				+ " \""	+ d64.getBam().getDiskName() + ","
				+ d64.getBam().getDiskId()
				+ "\" "+d64.getBlocksFree()
				+ " BLOCKS FREE [" + d64.getFilenumber_max() + "]"
				);
		table.revalidate();
		textArea.setText(feedBackMessage);
		repaint();
	}

	private void showBAM() {
		final int[] preAND = { 1, 2, 4, 8, 16, 32, 64, 128 };
		final int myLastTrack = 35; // 40
		String[][] bamEntry = new String[35][22];
		for (int i = 0; i <= (myLastTrack-1); i++) {
			for (int j = 1; j <= 21; j++) {
				bamEntry[i][j] = " ";
			}
		}
		String diskName =
				d64.getBam().getDiskDosType()
				+ " \""
				+ d64.getBam().getDiskName()
				+ ","
				+ d64.getBam().getDiskId()
				+ "\"";
		for (int track_number = 1; track_number <= myLastTrack; track_number++) {
			int bit_counter = 0;
			bamEntry[track_number-1][0] = track_number+"";
			for (int cnt = 1; cnt <= 3; cnt++) {
				for (int bit = 0; bit <= 7; bit++) {
					bit_counter++;
					if (bit_counter <= d64.getMaxSectors(track_number)) {
						if ((d64.getBam().getTrackBits(track_number, cnt) & preAND[bit]) == 0) {
							bamEntry[track_number-1][bit_counter] = "x";
						} else {
							bamEntry[track_number-1][bit_counter] = "-";
						}
					}
				}
			}
		}
		new BAMFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - BAM of this D64-image", diskName, bamEntry);
	}

	private void updateD64File() {
		d64.readBAM();
		d64.readDirectory();
		feedBackMessage += d64.getFeedbackMessage();

		if (useDatabase) {
			Disk disk = d64.getDisk();
			File f = new File(thisD64Name);
			File p = f.getAbsoluteFile().getParentFile();
			String d = p != null ? p.getAbsolutePath() : null;
			disk.setFilePath(d);
			disk.setFileName(f.getName());
			try {
				DaoFactory.getDaoFactory().getDiskDao().save(disk);
			} catch (DatabaseException e) {
				System.out.println(e);
				feedBackMessage += e;
			}
		}

	}

	private void copyPRG() {
		int thisCbmFile;

		feedBackMessage = "";
		d64.setFeedbackMessage("");
		boolean success = false;
		boolean writeD64File = false;

		feedBackMessage += "CopyPRG:\n";

		if (otherDiskPanel.isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}

		feedBackMessage += "The other D64 is \""+otherDiskPanel.getThisD64Name()+"\"\n";

		//TODO: copyPRG: what to do if filename exists?
		feedBackMessage += "Selected PRGs: \n";
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				try {
					//thisCbmFile = d64.getCbmFile(i).getDirPosition();
					thisCbmFile = i;
					feedBackMessage += "[" + i + "] " + tm.getValueAt(i,2) + " DirPosition = "+thisCbmFile+"\n";

					// load the PRG file and write its data into d64.saveData, write its size into d64.saveDataSize
					d64.writeSaveData(thisCbmFile);
					feedBackMessage += d64.getFeedbackMessage();

					d64.setFeedbackMessage("");

					//make d64.saveData and d64.saveDataSize known to the other DiskPanel
					otherDiskPanel.d64.setSaveData(d64.getSaveData());
					otherDiskPanel.d64.setSaveDataSize(d64.getSaveDataSize());

					/*
					 * Make d64.cbmFile known to the other DiskPanel and write it to the D64 of the other DiskPanel
					 * Keep in mind: We cannot just copy the object "cbmFile", but we have to copy its values!
					 */
					otherDiskPanel.d64.copyCbmFile(d64.getCbmFile(thisCbmFile), otherDiskPanel.d64.getBufferCbmFile() );

					success = otherDiskPanel.d64.saveFile(
							d64.getCbmFile(thisCbmFile).getName(),
							d64.getCbmFile(thisCbmFile).getFile_type(),
							true
							);

					otherDiskPanel.feedBackMessage = otherDiskPanel.feedBackMessage + otherDiskPanel.d64.getFeedbackMessage();
					otherDiskPanel.textArea.setText(otherDiskPanel.feedBackMessage);
				} catch (CbmException e) {
					feedBackMessage += "Failure while copying PRG file. "+e.getMessage();
					success = false;
				}

				if (success){
					writeD64File = true;
				}
				else {
					feedBackMessage += "Failure while copying PRG file.\n";
				}

			}
		}
		if (writeD64File){
			// write the D64 of the other Diskpanel to disk
			success = otherDiskPanel.d64.writeD64(otherDiskPanel.getThisD64Name());
			otherDiskPanel.feedBackMessage = otherDiskPanel.feedBackMessage + otherDiskPanel.d64.getFeedbackMessage();
			otherDiskPanel.textArea.setText(otherDiskPanel.feedBackMessage);
			if (success == false) {
				feedBackMessage += "Failure while writing D64 file.\n";
				showErrorMessage("insertError");
			}
		}
		else {
			showErrorMessage("insertError");
		}
		otherDiskPanel.reloadD64(true);
		otherDiskPanel.textArea.setText(otherDiskPanel.feedBackMessage);

		// we have to reload the current D64, because the selection will mess up otherwise (exceptions...)
		clearDirTable();
		updateD64File();
		showDirectory();
		//		reloadD64();
		textArea.setText(feedBackMessage);

	}

	//TODO
	private void hexViewFile() {
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				try {
					feedBackMessage += "Hex view " + tm.getValueAt(i,2) + "\n";
					d64.writeSaveData(i);
					byte[] data = d64.getSaveData();
					new HexViewFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Hex view", d64.getCbmFile(i).getName(), data, d64.getSaveDataSize());
				} catch (CbmException e) {
					feedBackMessage += "Hex view failed. " + e.getMessage();
				}
			}
		}
	}

	private void extractPRG() {
		feedBackMessage += "\nTesting-Status!.\n\n";
		saveName = savePRGFileDialog(directory);
		if (saveName != null) {
			feedBackMessage += "\nSelected file: \""+saveName+"\".\n\n";
			feedBackMessage += "ExtractPRG:\n";
			feedBackMessage += "Selected PRGs: \n";
			for (int i = 0; i < d64.getFilenumber_max(); i++) {
				if (table.isRowSelected(i)) {
					try {
						feedBackMessage += "[" + i + "] " + tm.getValueAt(i,2) + "\n";
						//test
						d64.extractPRG(i, directory);
						feedBackMessage += d64.getFeedbackMessage();
					} catch (CbmException e) {
						feedBackMessage += "Extract file failed. " + e.getMessage();
					}
				}
			}
		}
		textArea.setText(feedBackMessage);
	}

	private void insertPRG() {
		boolean success = false;
		feedBackMessage += "InsertPRG:\n";
		feedBackMessage += "\nTesting-Status!.\n\n";
		File[] loadNames = openPRGFileDialog(directory);
		//TODO: insertPRG: what to do if filename exists?
		if ( loadNames != null) {
			for (int i = 0; i < loadNames.length; i++) {
				try {
					//filename with path
					loadName = loadNames[i].getAbsolutePath();
					feedBackMessage += "\nSelected file: \""+loadName+"\".\n";
					d64.readPRG(loadName);
					feedBackMessage += d64.getFeedbackMessage();
					//filename without path
					loadName = loadNames[i].getName();
					success = d64.saveFile(
							loadName,
							D64.TYPE_PRG, 	//TODO: this always sets new FileType = PRG
							false
							);	
					feedBackMessage += d64.getFeedbackMessage();	
					if (success){
						success = d64.writeD64(thisD64Name);
						feedBackMessage += d64.getFeedbackMessage();
		
					}
					if (success == false){
						feedBackMessage += "Failure.\n";
						showErrorMessage("insertError");
					}
					reloadD64(true);
					otherDiskPanel.reloadD64(true);
					textArea.setText(feedBackMessage);
				} catch (CbmException e) {
					feedBackMessage += "Error: "+e.getMessage();
				}
			}
		}
	}

	private void newD64() {
		feedBackMessage += "-------\nNewD64:\n";
		newDiskName = "";
		new RenameD64Frame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - New disk",	this, "", "" );
		if (newDiskNameSuccess) {
			feedBackMessage += "New Diskname is: \""+newDiskName+", "+newDiskID+"\".\n";
			textArea.setText(feedBackMessage);
			String saveName = saveD64FileDialog(directory);
			if ( saveName != null) {
				feedBackMessage += "\nSelected file: \""+saveName+"\".\n";
				d64.saveNewD64(saveName, newDiskName, newDiskID);
				feedBackMessage += d64.getFeedbackMessage();
				isSetD64 = true;
				thisD64Name = saveName;
				reloadD64(true);
			}
		}
		textArea.setText(feedBackMessage);
	}

	private void renameDisk() {
		feedBackMessage += "-------\nRenameDisk:\n";
		newDiskName = "";
		new RenameD64Frame(
				DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Rename disk",
				this,
				d64.getBam().getDiskName(),
				d64.getBam().getDiskId()
				);
		if (newDiskNameSuccess) {
			feedBackMessage += "New Diskname is: \""+newDiskName+", "+newDiskID+"\".\n";
			textArea.setText(feedBackMessage);
			saveName = thisD64Name;
			d64.renameD64(saveName, newDiskName, newDiskID);
			feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
			reloadD64(true);
		}

		textArea.setText(feedBackMessage);
	}

	private void renamePRG() {
		feedBackMessage += "\nRenamePRG:\n\n";
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				newPRGName = "";
				new RenamePRGFrame(
						DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Rename file ",
						this,
						d64.getCbmFile(i).getName(),
						d64.getCbmFile(i).getFile_type()
						);

				if (newPRGNameSuccess) {
					feedBackMessage += "New filename is: \""+newPRGName+", "+newPRGType+"\".\n" +
							"Information about file number "+i+":\n" +
							"Name: "+d64.getCbmFile(i).getName()+"\n" +
							"DirPosition: "+d64.getCbmFile(i).getDirPosition()+"\n" +
							"DirTrack: "+d64.getCbmFile(i).getDirTrack()+"\n" +
							"DirSector: "+d64.getCbmFile(i).getDirSector()+"\n" +
							"SizeInBytes: "+d64.getCbmFile(i).getSizeInBytes()+"\n";
					d64.renamePRG(i,newPRGName, newPRGType);
					feedBackMessage += d64.getFeedbackMessage();

					d64.writeD64(thisD64Name);
					feedBackMessage += d64.getFeedbackMessage();
				}
			}
		}
		textArea.setText(feedBackMessage);
		reloadD64(true);
	}

	private void delPRG() {
		feedBackMessage += "Selected PRGs: \n";
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				feedBackMessage += "[" + i + "] " + tm.getValueAt(i,2) + "\n";
			}
		}
		feedBackMessage += "\nNot implemented yet.\n\n";
		textArea.setText(feedBackMessage);
	}

	private void clearDirTable(){
		tm.clear();
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

	public void reloadD64(boolean updateList){
		if (isSetD64){
			try {
				diskName.setText(thisD64Name);
				d64.readD64(thisD64Name);
				clearDirTable();
				updateD64File();
				if (updateList) {
					showDirectory();
				}
				mainPanel.setStatusBar("Loaded "+thisD64Name);
			} catch (CbmException e) {
				textArea.setText(feedBackMessage+"\nError: " + e.getMessage()+"\n");
				diskName.setText("No file");
				diskLabel.setText("NO DISK");
				isSetD64 = false;
				repaint();				
			}
		}
	}

	/**
	 * create a help drag-down menu (just for testing)
	 * @return
	 */
	public JMenu createD64Menu(String title, String mnemonic) {

		JMenu menu = new JMenu(title);
		menu.setMnemonic(mnemonic.charAt(0));

		JMenuItem menuItem;
		menuItem = new JMenuItem("New Disk", 'n');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "New Disk"){
					newD64();
				}
			}
		});
		menuItem = new JMenuItem("Load Disk", 'l');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Load Disk"){
					//loadD64();
					openD64(openD64FileDialog(directory), true);

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

		menu.addSeparator();
		menuItem = new JMenuItem("Insert File", 'i');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Insert File"){
					insertPRG();
				}
			}
		});
		menuItem = new JMenuItem("Extract File", 'e');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Extract File"){
					extractPRG();
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

}
