package GUI;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontFormatException;
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
import javax.swing.JTextField;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import D64.CbmFile;
import D64.D64;
import D64.DirEntry;

/*
 * Created on 23.06.2004
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

/*
 * TODO: adjust table-size dynamically to number of directory entries (else: check if empty entry was selected)
 * TODO: delPRG: implement method
 */
public class DiskPanel extends JPanel implements TableModelListener {
	

	private Font f;
	private final String FONTNAME = "droiD64_cbm.ttf";
	private final float FONTSIZE = 10;
	/*
	String fontFileName = "test.ttf";
	float fontSize = 11;

	String fontFileName = "CBM-64.TTF";
	float fontSize = 11;

			 String fontFileName = "Adore64.ttf";
	 float fontSize = 8;
		 
	 String fontFileName = "Commodore64-v5.ttf";
	 float fontSize = 8;
	*/

	private DiskPanel otherDiskPanel;
	private RenamePRGFrame renamePRGFrame;
	private String newPRGName;
	private boolean newPRGNameSuccess;
	private int newPRGType;

	private RenameD64Frame newD64Frame;
	private String newDiskName;
	private String newDiskID;
	private boolean newDiskNameSuccess;
	
	private JTable table;
	
	private int blocks_free = 644;

	private JPanel choicePanel, dirPanel, feedBackPanel;
	private BAMFrame bamFrame;
	
	private boolean isSetD64 = false;	//indicates whether a D64 was selected or not
	private String thisD64Name = "";

	private JLabel diskLabel, diskName;
	private JButton showBamButton, loadButton, extractButton, copyButton, insertButton, newButton;
	private JButton renameDiskButton, renamePRGButton, delPRGButton;
	private JButton misc01Button, misc02Button;
	private String progname_, version_;
	private String loadName = "";
	private String saveName = "";
	private String directory = ".";
	private JTextField textFeld1;
	private JTextField textFeld2;

	public D64 d64 = new D64();
	private String feedBackMessage = "";

	private JTextArea textArea;
	
	private int rowHeight = 10;

	private ExternalProgram[] externalProgram = new ExternalProgram[2];
	

	
	public DiskPanel() {}
	
	public void startDiskPanel(){
		//externalProgram[0] = new ExternalProgram();
		//externalProgram[1] = new ExternalProgram();
		
		makeFont();
		
		drawDirPanel();
		drawChoicePanel();
		drawFeedBackPanel();

		setLayout(new BorderLayout());

		add(dirPanel, BorderLayout.NORTH);
		add(choicePanel, BorderLayout.CENTER);
		add(feedBackPanel, BorderLayout.SOUTH);
		setBorder(BorderFactory.createRaisedBevelBorder());
	}

	private void makeFont(){
		try {
			f = Font.createFont( 
				Font.TRUETYPE_FONT,
				this.getClass().getResourceAsStream("ressources/"+FONTNAME)
			);
		}
		catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		catch (FontFormatException e) {
			e.printStackTrace();
		}
		catch (IOException e) {
			e.printStackTrace();
		}

		f = f.deriveFont( FONTSIZE );
	}

	private EntryTableModel tm = new EntryTableModel();
	private void drawDirPanel() {

		dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());

		//Table Column-Width		
		DefaultTableColumnModel cm = new DefaultTableColumnModel();
		for (int i = 0; i < tm.getCOLHEADS().length; ++i) {
			TableColumn col = new TableColumn(i, i == 2 ? 220 : 1);
			col.setHeaderValue(tm.getCOLHEADS()[i]);
			cm.addColumn(col);
		}

		tm.addTableModelListener(this);
		
		table = new JTable(tm, cm);
		//table.setBackground(new Color(230,230,255));
		table.setToolTipText("the content of the disk");
		//		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		//		table.setAlignmentX(JTable.RIGHT_ALIGNMENT);
		table.setGridColor(new Color(230, 230, 255));
		//table.setFont(new Font("Monospaced", Font.BOLD, 14));
		table.setFont(f);
		table.setRowHeight(rowHeight);
		//table.addColumn();
		//table.setShowVerticalLines(false);

		diskLabel = new JLabel("NO FILE");
		diskLabel.setToolTipText("the label of the disk");
		//diskLabel.setFont(new Font("Monospaced", Font.BOLD, 12));
		diskLabel.setFont(f);
		//		diskLabel.setAlignmentX(JLabel.CENTER_ALIGNMENT);

		//		diskLabel.setHorizontalTextPosition(JLabel.CENTER);
		//		setAlignmentX(JPanel.CENTER_ALIGNMENT);

		diskName = new JLabel("no file");
		diskName.setToolTipText("the name of the file");
		//diskName.setFont(new Font("Monospaced", Font.PLAIN, 12));
		//diskName.setFont(f);

		dirPanel.add(new JScrollPane(diskLabel), BorderLayout.NORTH);
		dirPanel.add(new JScrollPane(table), BorderLayout.CENTER);
		dirPanel.add(new JScrollPane(diskName), BorderLayout.SOUTH);
		dirPanel.setPreferredSize(new Dimension(50, 300));
	}
	

	private void showErrorMessage(String error){
		if (error == "noDisk"){
			feedBackMessage = feedBackMessage + "\nNo D64 file selected. Aborting...\n\n";
			textArea.setText(feedBackMessage);
			JOptionPane.showMessageDialog(
				null,
				"No disk loaded.\n"+
				"Open a D64 image first.",
				progname_+" v"+ version_+" - No disk",
				JOptionPane.ERROR_MESSAGE
			  );
		}
		if (error == "insertError"){
			feedBackMessage = feedBackMessage + "\nInserting error. Aborting...\n\n";
			textArea.setText(feedBackMessage);
			JOptionPane.showMessageDialog(
				null,
				"An error occurred while inserting file into disk.\n"+
				"Look up console report message for further information.",
				progname_+" v"+ version_+" - Failure while inserting file",
				JOptionPane.ERROR_MESSAGE
			  );
		}
	}

	public void tableChanged(TableModelEvent e) {
		int row = e.getFirstRow();
		int column = e.getColumn();
		TableModel model = (TableModel) e.getSource();
		// do something with the data
		table.setModel(model);
		table.revalidate();
	};

	
	private static int MAX_PLUGINS = 2;
	JButton[] miscButton = new JButton[MAX_PLUGINS];
	
	private void drawChoicePanel() {
		JPanel choice1Panel, choice2Panel, choice3Panel;

		choicePanel = new JPanel();
		choicePanel.setLayout(new BorderLayout());

		choice1Panel = new JPanel();
		choice2Panel = new JPanel();
		choice3Panel = new JPanel();


		newButton = new JButton("New");
		newButton.setToolTipText("Create a new blank disk.");
		newButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == newButton) {
					/*
					 * Insert nice function here
					 */
					newD64();
				}
			}
		});

//		loadKnopf =new JButton(new ImageIcon(this.getClass().getResource("file_open.gif")));
		loadButton =new JButton("Load");
		loadButton.setToolTipText("Open a disk.");
		loadButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == loadButton) {
					{
						loadD64();
					}
				}
			}
		});

		showBamButton = new JButton("BAM");
		showBamButton.setToolTipText("Show the BAM of this disk.");
		showBamButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == showBamButton) {
					/*
					 * Insert nice function here
					 */
						if (isSetD64 == false){
							showErrorMessage("noDisk");
							return;
						}

					showBAM();
					//showDirectory();
				}
			}
		});

		renameDiskButton = new JButton("Rename");
		renameDiskButton.setToolTipText("Modify the label of the disk.");
		renameDiskButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == renameDiskButton) {
					/*
					 * Insert nice function here
					 */
					renameDisk();
				}
			}
		});

		choice1Panel.add(new JLabel("Disk"));
		choice1Panel.add(newButton);
		choice1Panel.add(loadButton);
		choice1Panel.add(showBamButton);
		choice1Panel.add(renameDiskButton);




		insertButton = new JButton("In");
		insertButton.setToolTipText("Insert a PRG file into this disk.");
		insertButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == insertButton) {
					/*
					 * Insert nice function here
					 */
					insertPRG();
				}
			}
		});

		extractButton = new JButton("Out");
		extractButton.setToolTipText("Extract a PRG file from this disk.");
		extractButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == extractButton) {
					/*
					 * Insert nice function here
					 */
					extractPRG();
				}
			}
		});

		copyButton = new JButton("Copy");
		copyButton.setToolTipText("Copy a PRG file to the other disk.");
		copyButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == copyButton) {
					/*
					 * Insert nice function here
					 */
					copyPRG();
				}
			}
		});


		renamePRGButton = new JButton("Rename");
		renamePRGButton.setToolTipText("Modify a PRG file in this disk.");
		renamePRGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == renamePRGButton) {
					/*
					 * Insert nice function here
					 */
					renamePRG();
				}
			}
		});

		delPRGButton = new JButton("Delete");
		delPRGButton.setToolTipText("Delete a PRG file in this disk.");
		delPRGButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == delPRGButton) {
					/*
					 * Insert nice function here
					 */
					delPRG();
				}
			}
		});

		choice2Panel.add(new JLabel("File"));
		choice2Panel.add(insertButton);
		choice2Panel.add(extractButton);
		choice2Panel.add(renamePRGButton);
		choice2Panel.add(delPRGButton);
		choice2Panel.add(copyButton);




		choice3Panel.add(new JLabel("Misc"));

		for (int i = 0; i < externalProgram.length; i++) {
			miscButton[i] = new JButton(externalProgram[i].getLabel());
			miscButton[i].setToolTipText(externalProgram[i].getDescription());
			miscButton[i].addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent event) {
					for (int cnt = 0; cnt < MAX_PLUGINS; cnt ++){
						if ( event.getSource()==miscButton[cnt] ){
							/*
							 * Insert nice function here
							 */
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




		choicePanel.add(choice1Panel, BorderLayout.NORTH);
		choicePanel.add(choice2Panel, BorderLayout.CENTER);
		choicePanel.add(choice3Panel, BorderLayout.SOUTH);
	}

	private void drawFeedBackPanel() {
		feedBackPanel = new JPanel();
		feedBackPanel.setLayout(new BorderLayout());

		JLabel feedBackLabel = new JLabel("output:");
		feedBackLabel.setToolTipText("The status-report is displayed here.");
		feedBackLabel.setFont(new Font("Monospaced", Font.PLAIN, 12));

		textArea = new JTextArea(10, 60);
		textArea.setToolTipText("The status-report is displayed here.");
		textArea.setFont(new Font("Monospaced", Font.PLAIN, 12));

		feedBackPanel.add(feedBackLabel, BorderLayout.NORTH);
		feedBackPanel.add(new JScrollPane(textArea), BorderLayout.SOUTH);
	}



	private String openD64FileDialog(String directory_) {
		JFileChooser chooser = new JFileChooser(directory_);
		chooser.addChoosableFileFilter(new FileFilter() {
		public boolean accept(File f) {
		  if (f.isDirectory()) return true;
		  return f.getName().toLowerCase().endsWith(".d64");
		}
		public String getDescription () { return "D64 image"; }  
	  });
	  chooser.setMultiSelectionEnabled(false);
	  if (chooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
		directory = chooser.getSelectedFile().getParent();
		return chooser.getSelectedFile()+"";
	  }
	  return null;
	}							

	private File[] openPRGFileDialog(String directory_) {
		JFileChooser chooser = new JFileChooser(directory_);
		chooser.addChoosableFileFilter(new FileFilter() {
		public boolean accept(File f) {
		  if (f.isDirectory()) return true;
		  return f.getName().toLowerCase().endsWith(".prg");
		}
		public String getDescription () { return "PRGimage"; }  
	  });
	  chooser.setMultiSelectionEnabled(true);
	  if (chooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
		directory = chooser.getSelectedFile().getParent();
		return chooser.getSelectedFiles();
	  }
	  return null;
	}							


	private String saveD64FileDialog(String directory_) {
		JFileChooser chooser = new JFileChooser(directory_);
		chooser.addChoosableFileFilter(new FileFilter() {
		public boolean accept(File f) {
		  if (f.isDirectory()) return true;
		  return f.getName().toLowerCase().endsWith(".d64");
		}
		public String getDescription () { return "D64 image"; }  
	  });
	  chooser.setMultiSelectionEnabled(false);
	  if (chooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
		directory = chooser.getSelectedFile().getParent();
		return chooser.getSelectedFile()+"";
	  }
	  return null;
	}							

	private String savePRGFileDialog(String directory_) {
		JFileChooser chooser = new JFileChooser(directory_);
		chooser.setDialogTitle("Choose a target directory");
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showDialog(null, "Select")==JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getCurrentDirectory();
			File file = chooser.getSelectedFile();
			directory = dir.getPath()+File.separator;
			directory += file.getName()+File.separator;
			return directory;
		}
		return null;
	}
 	





	private void loadD64(){
		loadName = openD64FileDialog(directory);
		if (loadName != null) {
			isSetD64 = true;
			thisD64Name = loadName;

			reloadD64();
		}
	}

	private void doExternalProgram(int which_one){
		feedBackMessage =
			feedBackMessage
				+ "Executing \""+externalProgram[which_one].getCommand()+"\" on disk "+loadName+"\n";
		
		String[] doThat = new String[2];
		
		doThat[0] = externalProgram[which_one].getCommand();
		doThat[1] = loadName;

		try
		{
			Runtime rt = Runtime.getRuntime();
			Process pr = rt.exec(doThat);
			
			BufferedReader procout = new BufferedReader(
				new InputStreamReader(pr.getInputStream())
			);
			String line;
			while ((line = procout.readLine()) != null)
			{
			  //System.out.println(line);
			  feedBackMessage =feedBackMessage + line + "\n";
			}
		}
		catch (Exception e)
		{
		  System.err.println(e.toString());
		}

		textArea.setText(feedBackMessage);
	}


	private DirEntry dirEntry;
	private void showDirectory() {
		String flags = "";
		int filenumber;
		int entry_number = 0;
		String this_entry;

		updateD64File();

		feedBackMessage =
			feedBackMessage
				+ "There are "
				+ d64.getFilenumber_max()
				+ " files in this D64 file.\n\n";

		for (filenumber = 0;
			filenumber <= d64.getFilenumber_max() - 1;
			filenumber++) {

			//Size, Name, Type, Locked, Closed}
			this_entry = filenumber + " ";

			this_entry =
				this_entry + d64.getCbmFile(filenumber).getSizeInBlocks() + " ";

			this_entry =
				this_entry
					+ " \""
					+ d64.getCbmFile(filenumber).getName()
					+ "\" ";

			this_entry =
				this_entry
					+ d64.getFileType(d64.getCbmFile(filenumber).getFile_type());
			flags = "";
			if (d64.getCbmFile(filenumber).isFile_locked())
				flags = "< ";
			if (d64.getCbmFile(filenumber).isFile_closed() == false)
				flags = flags + "* ";
			//{Track / Sector}
			this_entry =
				this_entry
					+ d64.getCbmFile(filenumber).getTrack()
					+ "/"
					+ d64.getCbmFile(filenumber).getSector();

			dirEntry = new DirEntry();
			dirEntry.setNumber(filenumber + 1);
			dirEntry.setBlocks(d64.getCbmFile(filenumber).getSizeInBlocks());
			dirEntry.setName( " \"" + d64.getCbmFile(filenumber).getName() + "\"" );
			dirEntry.setType( d64.getFileType(d64.getCbmFile(filenumber).getFile_type()) );
			dirEntry.setFlags( flags );
			dirEntry.setTrack( d64.getCbmFile(filenumber).getTrack() );
			dirEntry.setSector( d64.getCbmFile(filenumber).getSector() );
			
			tm.updateDirEntry(dirEntry);

			entry_number++;

//			feedBackMessage = feedBackMessage + this_entry + "\n";
		}

		diskLabel.setText(
			d64.getBam().getDisk_DOS_type()
				+ " \""
				+ d64.getBam().getDisk_name()
				+ ","
				+ d64.getBam().getDisk_ID()
				+ "\" "+d64.getBlocksFree()+" BLOCKS FREE ["
				+ d64.getFilenumber_max() + "]"
				);

		table.revalidate();
		repaint();

		textArea.setText(feedBackMessage);
	}




	private void showBAM() {
		int[] preAND = { 1, 2, 4, 8, 16, 32, 64, 128 };
		String[][] bamEntry = new String[35][22];

		int cnt;
		int track_number;
		int bit;
		int bit_counter;
		String diskName;
		int myLastTrack = 35; // 40

		for (int i = 0; i <= (myLastTrack-1); i++) {
			for (int j = 1; j <= 21; j++) {
				bamEntry[i][j] = " ";
			}
		}

		diskName =
				d64.getBam().getDisk_DOS_type()
				+ " \""
				+ d64.getBam().getDisk_name()
				+ ","
				+ d64.getBam().getDisk_ID()
				+ "\"";
				
		for (track_number = 1; track_number <= myLastTrack; track_number++) {
			bit_counter = 0;
			bamEntry[track_number-1][0] = track_number+"";
			for (cnt = 1; cnt <= 3; cnt++) {
				for (bit = 0; bit <= 7; bit++) {
					bit_counter++;
					if (bit_counter <= d64.getMaxSectors(track_number)) {
						if (
								(d64.getBam().getTrack_bits(track_number, cnt) & preAND[bit]) == 0
						) {
							bamEntry[track_number-1][bit_counter] = "x";
						} else {
							bamEntry[track_number-1][bit_counter] = "-";
						}
					}
				}
			}
		}
		bamFrame = new BAMFrame(progname_+" v"+version_+" - BAM of this D64-image", diskName, bamEntry);
	}

	private void updateD64File() {
		d64.readBAM();
		feedBackMessage = feedBackMessage  +d64.getFeedbackMessage() + "\n\n";
		d64.readDirectory();
		feedBackMessage = feedBackMessage  +d64.getFeedbackMessage() + "\n\n";
	}

	private void copyPRG() {
		int thisCbmFile;
		CbmFile cbmFileTest;
		
		feedBackMessage = "";
		d64.setFeedbackMessage("");
		boolean success = false;
		boolean writeD64File = false;
		
		feedBackMessage = feedBackMessage + "CopyPRG:\n";

		if (isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}
		
		if (otherDiskPanel.isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}

		feedBackMessage = feedBackMessage + "The other D64 is \""+otherDiskPanel.getThisD64Name()+"\"\n";

		//TODO: copyPRG: what to do if filename exists?
		feedBackMessage = feedBackMessage + "Selected PRGs: \n";
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				//thisCbmFile = d64.getCbmFile(i).getDirPosition();
				thisCbmFile = i;
				feedBackMessage =
					feedBackMessage + "[" + i + "] " + tm.getValueAt(i,2) + " DirPosition = "+thisCbmFile+"\n";

				// load the PRG file and write its data into d64.saveData, write its size into d64.saveDataSize
				success = d64.writeSaveData(thisCbmFile);
				feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
				
				if (success) {
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
				}
			
				if (success){
					 writeD64File = true;
				}
				else {
					feedBackMessage = feedBackMessage + "Failure while copying PRG file.\n";
				}
				
			}
		}
		if (writeD64File){
			// write the D64 of the other Diskpanel to disk
			success = otherDiskPanel.d64.writeD64(otherDiskPanel.getThisD64Name());
			otherDiskPanel.feedBackMessage = otherDiskPanel.feedBackMessage + otherDiskPanel.d64.getFeedbackMessage();
			otherDiskPanel.textArea.setText(otherDiskPanel.feedBackMessage);
			if (success == false) {
				feedBackMessage = feedBackMessage + "Failure while writing D64 file.\n";
				showErrorMessage("insertError");
			}
		}
		else {
			showErrorMessage("insertError");
		}
		otherDiskPanel.reloadD64();
		otherDiskPanel.textArea.setText(otherDiskPanel.feedBackMessage);

		// we have to reload the current D64, because the selection will mess up otherwise (exceptions...)
		clearDirTable();
		showDirectory();
//		reloadD64();
		textArea.setText(feedBackMessage);
		
	}




	private void extractPRG() {

		feedBackMessage = feedBackMessage + "\nTesting-Status!.\n\n";
		
		if (isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}

		saveName = savePRGFileDialog(directory);
		if (saveName != null) {
			feedBackMessage = feedBackMessage + "\nSelected file: \""+saveName+"\".\n\n";
		
			feedBackMessage = feedBackMessage + "ExtractPRG:\n";
	
			feedBackMessage = feedBackMessage + "Selected PRGs: \n";
			for (int i = 0; i < d64.getFilenumber_max(); i++) {
				if (table.isRowSelected(i)) {
					feedBackMessage =
//					feedBackMessage + "[" + i + "] " + data[i][2] + "\n";
						feedBackMessage + "[" + i + "] " + tm.getValueAt(i,2) + "\n";
					//test
					d64.extractPRG(i, directory);
					feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
				}
			}
		}
		textArea.setText(feedBackMessage);
	}

	private void insertPRG() {
		boolean success = false;
		
		feedBackMessage = feedBackMessage + "InsertPRG:\n";

		if (isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}

		feedBackMessage = feedBackMessage + "\nTesting-Status!.\n\n";
		
		File[] loadNames = openPRGFileDialog(directory);
		//TODO: insertPRG: what to do if filename exists?
		if ( loadNames != null) {
			for (int i = 0; i < loadNames.length; i++) {
				//filename with path
				loadName = loadNames[i].getAbsolutePath();
				feedBackMessage = feedBackMessage + "\nSelected file: \""+loadName+"\".\n";

				success = d64.readPRG(loadName);
				feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
				
				//filename without path
				loadName = loadNames[i].getName();

				if (success){
					success = d64.saveFile(
						loadName,
						2,												//TODO: this always sets new FileType = PRG
						false
					);	
					feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
				}	

				if (success){
					
					success = d64.writeD64(thisD64Name);
					feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
					
				}

				if (success == false){
					feedBackMessage = feedBackMessage + "Failure.\n";
					showErrorMessage("insertError");
				}
				

				reloadD64();
				otherDiskPanel.reloadD64();
				textArea.setText(feedBackMessage);
			}
		}

	}

	private void newD64() {
		
		feedBackMessage = feedBackMessage + "-------\nNewD64:\n";

		newDiskName = "";
		newD64Frame = new RenameD64Frame(
			progname_+" v"+version_+" - New disk",
			this,
			"",
			""
		);
		
		if (newDiskNameSuccess) {
			feedBackMessage = feedBackMessage + "New Diskname is: \""+newDiskName+", "+newDiskID+"\".\n";
			textArea.setText(feedBackMessage);
			String saveName = saveD64FileDialog(directory);
			if ( saveName != null) {
					feedBackMessage = feedBackMessage + "\nSelected file: \""+saveName+"\".\n";
					d64.saveNewD64(saveName, newDiskName, newDiskID);
					feedBackMessage = feedBackMessage + d64.getFeedbackMessage();

					isSetD64 = true;
					thisD64Name = saveName;
					reloadD64();
			}
		}

		textArea.setText(feedBackMessage);
	}

	private void renameDisk() {

			feedBackMessage = feedBackMessage + "-------\nRenameDisk:\n";
	
			
		if (isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}
	
			newDiskName = "";
			newD64Frame = new RenameD64Frame(
				progname_+" v"+version_+" - Rename disk",
				this,
				d64.getBam().getDisk_name(),
				d64.getBam().getDisk_ID()
			);
			
			if (newDiskNameSuccess) {
				feedBackMessage = feedBackMessage + "New Diskname is: \""+newDiskName+", "+newDiskID+"\".\n";
				textArea.setText(feedBackMessage);
				
				saveName = thisD64Name;
				d64.renameD64(saveName, newDiskName, newDiskID);
				feedBackMessage = feedBackMessage + d64.getFeedbackMessage();

				reloadD64();
			}
	
		textArea.setText(feedBackMessage);
	}

	private void renamePRG() {

			feedBackMessage = feedBackMessage + "\nRenamePRG:\n\n";
		
		if (isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}
		
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				newPRGName = "";
				renamePRGFrame = new RenamePRGFrame(
					progname_+" v"+version_+" - Rename file ",
					this,
					d64.getCbmFile(i).getName(),
					d64.getCbmFile(i).getFile_type()
				);
				
				if (newPRGNameSuccess) {
					feedBackMessage = feedBackMessage + "New filename is: \""+newPRGName+", "+newPRGType+"\".\n";
					feedBackMessage = feedBackMessage + "Information about file number "+i+":\n";
					feedBackMessage = feedBackMessage + "Name: "+d64.getCbmFile(i).getName()+"\n";
					feedBackMessage = feedBackMessage + "DirPosition: "+d64.getCbmFile(i).getDirPosition()+"\n";
					feedBackMessage = feedBackMessage + "DirTrack: "+d64.getCbmFile(i).getDirTrack()+"\n";
					feedBackMessage = feedBackMessage + "DirSector: "+d64.getCbmFile(i).getDirSector()+"\n";
					feedBackMessage = feedBackMessage + "SizeInBytes: "+d64.getCbmFile(i).getSizeInBytes()+"\n";
					d64.renamePRG(i,newPRGName, newPRGType);
					feedBackMessage = feedBackMessage + d64.getFeedbackMessage();

					d64.writeD64(thisD64Name);
					feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
				}
			}
		}

			textArea.setText(feedBackMessage);
			
			reloadD64();
		}




	private void delPRG() {

		if (isSetD64 == false){
			showErrorMessage("noDisk");
			return;
		}
	
		feedBackMessage = feedBackMessage + "Selected PRGs: \n";
		for (int i = 0; i < d64.getFilenumber_max(); i++) {
			if (table.isRowSelected(i)) {
				feedBackMessage =
//				feedBackMessage + "[" + i + "] " + data[i][2] + "\n";
				feedBackMessage + "[" + i + "] " + tm.getValueAt(i,2) + "\n";
			}
		}

		feedBackMessage = feedBackMessage + "\nNot implemented yet.\n\n";
		
		textArea.setText(feedBackMessage);
	}




/**
	 * @param string
	 */
	public void setProgname_(String string) {
		progname_ = string;
	}

	/**
	 * @param string
	 */
	public void setVersion_(String string) {
		version_ = string;
	}
	
	private void clearDirTable(){
		tm.clear();
//		for (int i = 0; i < 144; i++) {
//			for (int j = 0; j < 7; j++) {
////				data[i][j] = " ";
//				tm.setValueAt(i,j) = " ";
//			}
//		}
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

	public void reloadD64(){
		if (isSetD64){
			diskName.setText(thisD64Name);
			d64.readD64(thisD64Name);
			
			//d64.readBAM does not clear d64.feedbackMessage. Therefore it will not be added here.
			//feedBackMessage = feedBackMessage + d64.getFeedbackMessage();
			
			clearDirTable();
			showDirectory();
		}
	}


	/**
	 * create a help drag-down menu (just for testing)
   * @return
   */
  public JMenu createD64Menu(String title, String mnemonic)
	{
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
				loadD64();
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

}
