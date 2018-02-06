package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JDialog;
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
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.CbmFile;
import droid64.d64.D64;
import droid64.d64.D71;
import droid64.d64.D81;
import droid64.d64.DirEntry;
import droid64.d64.DiskImage;
import droid64.d64.ValidationError;
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
public class DiskPanel extends JPanel implements TableModelListener {

	private static final long serialVersionUID = 1L;
	
	private DiskPanel otherDiskPanel;
	public DiskImage diskImage = null;
	private EntryTableModel tableModel;
	private MainPanel mainPanel;

	/** Disk label from loaded disk image */
	private JLabel diskLabel;
	/** Name (path) of current directory or loaded image */
	private JTextField diskName;
	private JTable table;
	/** File dialog for selecting image file to open */
	private JFileChooser chooser = null;
	private JPanel diskLabelPane;
	
	private String newPRGName;
	private boolean newPRGNameSuccess;
	private int newPRGType;
	private String newDiskName;
	private String newDiskID;
	private int newDiskType;
	private boolean newDiskNameSuccess;
	/** True when a disk image is loaded */
	private boolean imageLoaded = false;
	/** Path and file name of loaded disk image */
	private String diskImageFileName = "";
	/** Path to currently used directory on file system */
	private String currentImagePath = null;
	private String saveName = "";
	private String directory = ".";
	private int rowHeight = 12;
	private boolean newCompressedDisk = false;
	private boolean newCpmDisk = false;
	private int panelNum = 0;
	private static int panelNumCounter = 0;
	/** True when this is the active disk panel */
	private boolean active = false;
	/** This is the row of the last opened file in the file list */
	private Integer openedRow = null;
	
	public DiskPanel(MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		tableModel = new EntryTableModel();
		tableModel.setMode(EntryTableModel.MODE_LOCAL);
		rowHeight = Settings.getFontSize() + 2;
		panelNum = panelNumCounter++;
		JPanel dirPanel = drawDirPanel();
		setTableColors();
		setLayout(new BorderLayout());
		add(dirPanel, BorderLayout.CENTER);
		setBorder(BorderFactory.createRaisedBevelBorder());
	}

	public int getPanelNum() {
		return panelNum;
	}
	
	public void setActive(boolean active) {
		if (active) {
			setBackground(Settings.getActiveBorderColor());
			this.active = true;
			if (otherDiskPanel != null) {
				otherDiskPanel.active = false;
				otherDiskPanel.setBackground(Settings.getInactiveBorderColor());
			}
		} else {
			setBackground(Settings.getInactiveBorderColor());
			this.active = false;
			if (otherDiskPanel != null) {
				otherDiskPanel.active = true;
				otherDiskPanel.setBackground(Settings.getActiveBorderColor());
			}
		}
		setTableColors();
		otherDiskPanel.setTableColors();
	}

	public boolean isActive() {
		return this.active;
	}
	
	private JPanel drawDirPanel() {

		JPanel dirPanel = new JPanel();
		dirPanel.setLayout(new BorderLayout());

		TableColumnModel columnModel = tableModel.getTableColumnModel();
		tableModel.addTableModelListener(this);
				
		table = new JTable(tableModel);
		table.setColumnModel(columnModel);
		table.setDefaultRenderer(Object.class, new ListTableCellRenderer(tableModel));
		table.setGridColor(Settings.getDirColorBg());
		table.setRowHeight(rowHeight);
		table.setBackground(Settings.getDirColorBg());
		table.setForeground(Settings.getDirColorFg());
		table.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	setActive(true);		    	
	        	int clickCount = me.getClickCount();
		        int row = table.rowAtPoint(me.getPoint());
	        	if (clickCount == 2 && row >= 0) {
	        		doubleClickedRow(row);
	        	}
		    }
		});
		
		diskLabel = new JLabel("NO DISK");
		diskLabel.setBackground(Settings.getDirColorBg());
		diskLabel.setForeground(Settings.getDirColorFg());
		diskLabel.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	setActive(true);
		    }
		});
		
		diskLabelPane = new JPanel();		
		diskLabelPane.setBackground(Settings.getDirColorBg());
		diskLabelPane.setForeground(Settings.getDirColorFg());
		diskLabelPane.add(diskLabel);
		diskLabelPane.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	setActive(true);
		    }
		});
		
		diskName = new JTextField("No file");
		diskName.setFont(new Font("Verdana", Font.PLAIN, Settings.getFontSize()));
		diskName.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		    	setActive(true);
		    }
		});
		diskName.addKeyListener(new KeyListener() {
			public void keyTyped(KeyEvent ev) {
			}
			public void keyPressed(KeyEvent ev) {	
			}
			public void keyReleased(KeyEvent ev) {
				if (ev.getKeyCode() == KeyEvent.VK_ENTER) {
					File dir = new File(diskName.getText());
					if (dir.isDirectory()) {
						loadLocalDirectory(diskName.getText());
					}
				}
			}
		});
		
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

	private String getLocalFilename(int row) {
		return this.currentImagePath + File.separator + table.getValueAt(row, 1);
	}
	
	private void doubleClickedRow(int row) {
		openedRow = null;
		if (imageLoaded) {
			CbmFile file = diskImage.getCbmFile(row);
			if (file != null && file.getFileType() == DiskImage.TYPE_CBM) {
				try {
					clearDirTable();
					diskImage.readPartition(file.getTrack(), file.getSector(), file.getSizeInBlocks());
					showDirectory();
				} catch (CbmException e) {
					e.printStackTrace();
				}
			} else if (file != null){
				hexViewFile(row);
			}
		} else {
			File file;
			if ("..".equals((String) table.getValueAt(row, 1))) {
				file = new File(currentImagePath).getParentFile();
			} else {
				file = new File(getLocalFilename(row));
			}
			if (file.exists()) {
				if (DiskImage.isImageFileName(file)) {
					openDiskImage(file.getPath(), true);
					openedRow = new Integer(row);
				} else if (file.isDirectory()) {
					loadLocalDirectory(file.getPath());
				} else if (file.isFile()) {
					hexViewFile(row);
				}
			}			
		}
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
	
	private String saveDiskImageFileDialog(String directory, String defaultName) {
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
		chooser.setDialogTitle("New disk image");
		chooser.addChoosableFileFilter(fileFilter);
		chooser.setFileFilter(fileFilter);
		chooser.setSelectedFile(new File(defaultName));
		chooser.setMultiSelectionEnabled(false);
		if (chooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			this.directory = chooser.getSelectedFile().getParent();
			return chooser.getSelectedFile()+"";
		}
		return null;
	}

	public void openDiskImage(String fileName, boolean updateList) {
		if (fileName != null) {
			if (new File(fileName).canRead()) {
				mainPanel.appendConsole("openDiskImage: "+fileName);
				diskImageFileName = fileName;
				imageLoaded = true;
				reloadDiskImage(updateList);
				diskName.setEditable(false);
			} else {
				mainPanel.appendConsole("Error: fail to open file "+fileName);
			}
		}
	}

	public void doExternalProgram(ExternalProgram prg) {		
		mainPanel.appendConsole("Executing \"" + prg.getCommand() + "\" with disk " + diskImageFileName);
		String selectedFile = null;
		List<String> argList = new ArrayList<String>();
		argList.add(prg.getCommand());
		for (int row = 0; row < table.getRowCount(); row++) {
			if (table.isRowSelected(row)) {
				if (imageLoaded) {
					selectedFile = diskImage.getCbmFile(row).getName().toLowerCase();
					break;
				} else {
					argList.add(currentImagePath + File.separator + ((String) tableModel.getValueAt(row, 1)));
				}
			}
		}
		if (imageLoaded) {
			argList.add( diskImageFileName + (selectedFile!= null ? ":"+selectedFile : ""));			
		}
		try {
			final String[] args = argList.toArray(new String[argList.size()]);
			Thread runner = new Thread() {
				public void run() {
					try {
						Process pr = Runtime.getRuntime().exec(args);
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
		if (imageLoaded) {
			mainPanel.appendConsole("There are " + diskImage.getFilenumberMax() + " files in this image file.");
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
			tableModel.setMode(diskImage.isCpmImage() ? EntryTableModel.MODE_CPM : EntryTableModel.MODE_CBM);
			TableColumnModel tcm = table.getTableHeader().getColumnModel();
			for (int i=0; i< tcm.getColumnCount(); i++) {
				tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
			}
			setTableColors();
			table.setColumnModel(tableModel.getTableColumnModel());
			table.revalidate();
			repaint();
		}
	}
	
	/**
	 * Load contents from a directory on local file system.
	 * @param path to a directory to open
	 */
	public void loadLocalDirectory(String path) {
		File dir = new File(path != null ? path : Settings.getDefaultImageDir());
		mainPanel.appendConsole("loadLocalDirectory: "+dir.getAbsolutePath());
		clearDirTable();
		if (dir.isDirectory()) {
			int fileNum = 0;
			File parentFile = dir.getParentFile();
			if (parentFile != null) {
				DirEntry parent = new DirEntry(parentFile, ++fileNum);
				parent.setName("..");
				tableModel.updateDirEntry(parent);
			}
			this.currentImagePath = path;
			File[] files = dir.listFiles();
			if (files != null) {
				Arrays.sort(files, new FileComparator());
				for (File file : files) {
					if (!file.getName().startsWith(".")) {
						tableModel.updateDirEntry(new DirEntry(file, ++fileNum));
					}
				}
				diskName.setText(path);
				diskName.setEditable(true);
				diskLabel.setText("FILE SYSTEM");
			} else {
				diskLabel.setText("ERROR");				
			}
		} else {
			diskLabel.setText("ERROR");			
		}
		
		tableModel.setMode(EntryTableModel.MODE_LOCAL);
		TableColumnModel tcm = table.getTableHeader().getColumnModel();
		for (int i=0; i< tcm.getColumnCount(); i++) {
			tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
		}
		setTableColors();
		table.setColumnModel(tableModel.getTableColumnModel());
		table.revalidate();
		repaint();
	}
	
	/**
	 * Comparator class for comparing File entries with directories first
	 */
	class FileComparator implements Comparator<File> {
		public int compare(File a, File b) {
			if (a.isDirectory() && !b.isDirectory()) {
				return -1;
			}
			if (!a.isDirectory() && b.isDirectory()) {
				return 1;
			}				
	        return a.getName().compareTo(b.getName());
		}		
	}
	
	public void showBAM() {
		String diskName =
				diskImage.getBam().getDiskDosType() + " \"" +
				diskImage.getBam().getDiskName() + "," +
				diskImage.getBam().getDiskId() + "\"";
		new BAMFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - BAM of this disk image", diskName, diskImage.getBamTable(), diskImage);
	}

	private void updateD64File() {
		if (imageLoaded) {
			diskImage.readBAM();
			diskImage.readDirectory();
			mainPanel.appendConsole(diskImage.getFeedbackMessage());
			if (Settings.getUseDb()) {
				Disk disk = diskImage.getDisk();
				File f = new File(diskImageFileName);
				File p = f.getAbsoluteFile().getParentFile();
				String d = p != null ? p.getAbsolutePath() : null;
				disk.setFilePath(d);
				disk.setFileName(f.getName());
				disk.setImageType(diskImage.getImageFormat());
				try {
					DaoFactory.getDaoFactory().getDiskDao().save(disk);
				} catch (DatabaseException e) {
					mainPanel.appendConsole("\n"+e.getMessage());
				}
			}
		}
	}

	public void copyPRG() {
		boolean filesCopied = false;
		try {
			boolean success = false;
			if (imageLoaded) {
				diskImage.setFeedbackMessage("");
				for (int row = 0; row < table.getRowCount(); row++) {
					if (table.isRowSelected(row)) {
						filesCopied = true;
						mainPanel.appendConsole("Copy [" + row + "] " + tableModel.getValueAt(row, 2));
						byte[] saveData = diskImage.getFileData(row);
						mainPanel.appendConsole(diskImage.getFeedbackMessage());						
						CbmFile cbmFile = diskImage.getCbmFile(row).clone();
						if (otherDiskPanel.imageLoaded) {
							// Copy file from image to image
							success = otherDiskPanel.diskImage.saveFile(cbmFile, true, saveData);
							mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
						} else {
							// Copy file from image to local file system
							String outName = otherDiskPanel.currentImagePath + File.separator + DiskImage.pcFilename(cbmFile);
							mainPanel.appendConsole("DiskPanel.copyPRG: "+outName+" class="+cbmFile.getClass().getName());							
							File targetFile = new File(outName);
							writeFile(targetFile, saveData);
						}
						if (!success) {
							mainPanel.appendConsole("Failed to save copy of "+cbmFile.getName()+".\nAborting copy.");
							break;
						}
					}
				}
				if (success) {
					if (otherDiskPanel.imageLoaded) {
						success = otherDiskPanel.diskImage.writeImage(otherDiskPanel.diskImageFileName);
						mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
					}
				}
			} else {
				// local file system
				for (int row = 0; row < table.getRowCount(); row++) {
					if (table.isRowSelected(row)) {
						String filename = (String) tableModel.getValueAt(row, 1);
						File sourceFile = new File(currentImagePath + File.separator + filename);
						if (sourceFile.isFile()) {
							filesCopied = true;
							mainPanel.appendConsole("Copy [" + row + "] " + filename);
							if (otherDiskPanel.imageLoaded) {
								// Copy file from local file system to disk image
								byte[] data = readFile(sourceFile);
								CbmFile cbmFile = new CbmFile();
								cbmFile.setName(DiskImage.cbmFileName(filename));
								cbmFile.setFileType(DiskImage.getFileTypeFromFileExtension(filename));
								success = otherDiskPanel.diskImage.saveFile(cbmFile, false, data);	
								mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
								if (!success) {
									mainPanel.appendConsole("Failed to save copy of "+filename+".\nAborting copy.");
									break;
								}
							} else {
								// Copy file from file system to file system (no images involved)
								File targetFile = new File(otherDiskPanel.currentImagePath + File.separator + filename);
								writeFile(sourceFile, targetFile);
							}
						} else {
							mainPanel.appendConsole("Error: Is not a plain file (" + filename+")");							
						}
					}
				}
				if (success) {
					if (otherDiskPanel.imageLoaded) {
						success = otherDiskPanel.diskImage.writeImage(otherDiskPanel.diskImageFileName);
						mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
					}
				}
			}
		} catch (CbmException e) {
			mainPanel.appendConsole("Error: Failed to copy files. \n" + e.getMessage());
		}
		if (filesCopied) {
			otherDiskPanel.reloadDiskImage(true);
		}
	}

	private byte[] readFile(File file) throws CbmException {
		FileInputStream input;		
		try {
			input = new FileInputStream(file);
		} catch (Exception e){
			throw new CbmException("Failed to open file. "+e.getMessage());
		}
		byte[] data;
		try {
			data = new byte[ (int)file.length() ];
			for (int i=0; i<data.length; i++) {
				data[i] = 0;
			}
			input.read( data );
		} catch (Exception e){
			try {
				input.close();
			} catch (Exception e2) { }
			throw new CbmException("Failed to read file. "+e.getMessage());
		}
		try {
			input.close();
		} catch (Exception e){
			throw new CbmException("Failed to close file. "+e.getMessage());
		}
		return data;
	}
	
	
	private void writeFile(File sourceFile, File targetFile) throws CbmException {
		if (targetFile == null || sourceFile == null) {
			throw new CbmException("Required data is missing.");
		}
		
		FileInputStream input;
		try {
			input = new FileInputStream(sourceFile);
		} catch (Exception e) {
			throw new CbmException("Failed to open input file. "+e.getMessage());
		}
		FileOutputStream output;
		try {
			output = new FileOutputStream(targetFile);
		} catch (Exception e) {
			try {
				input.close();
			} catch (IOException e1) { }
			throw new CbmException("Failed to open output file. "+e.getMessage());
		}
		
		try {
		    byte[] buffer = new byte[256];
		    int bytesRead = 0;
		    while ((bytesRead = input.read(buffer)) != -1) {
		        output.write(buffer, 0, bytesRead);
		    }
		} catch (Exception e) {
			try {
				input.close();
			} catch (IOException e1) { }
			try {
				output.close();
			} catch (Exception e2) {}
			throw new CbmException("Failed to write data. "+e.getMessage());
		}
		try {
			output.close();
		} catch (Exception e){
			try {
				input.close();
			} catch (IOException e1) { }
			throw new CbmException("Failed to close output file. "+e.getMessage());
		}
		try {
			input.close();
		} catch (Exception e){
			throw new CbmException("Failed to close input file. "+e.getMessage());
		}
	}

	
	/**
	 * Write data to file on local file system.
	 * @param filename the name of the file (without path)
	 * @param data the data to write
	 * @throws CbmException
	 */
	private void writeFile(File targetFile, byte[] data) throws CbmException {
		if (targetFile == null || data == null) {
			throw new CbmException("Required data is missing.");
		}
		mainPanel.appendConsole("Export to file: " + targetFile.getAbsolutePath());
		FileOutputStream output;
		try {
			output = new FileOutputStream(targetFile);
		} catch (Exception e) {
			throw new CbmException("Failed to open output file. "+e.getMessage());
		}
		try {
			output.write(data, 0, data.length);
		} catch (Exception e) {
			try {
				output.close();
			} catch (Exception e2) {}
			throw new CbmException("Failed to write data. "+e.getMessage());
		}		
		try {
			output.close();
		} catch (Exception e){
			throw new CbmException("Failed to close file. "+e.getMessage());
		}
	}
	
	/** Get data from file.
	 * @param file number in listing
	 * @return data
	 */
	private byte[] getFileData(int fileNum) {
		try {
			if (imageLoaded) {
				return diskImage.getFileData(fileNum);
			} else {
				File file = new File(getLocalFilename(fileNum));
				byte[] data = Files.readAllBytes(file.toPath());
				return data;
			}
		} catch (CbmException | IOException e) {
			mainPanel.appendConsole("Failed to get file data : " + e.getMessage());
		}
		return null;
	}
	
	
	public void hexViewFile() {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
				mainPanel.appendConsole("Hex view '" + name + "'");
				hexViewFile(i);
			}
		}
	}
	
	private void hexViewFile(int fileNum) {
		byte[] data = getFileData(fileNum);
		String name = imageLoaded ? diskImage.getCbmFile(fileNum).getName() : getLocalFilename(fileNum);
		new HexViewFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Hex view", name, data, data.length);
	}
	
	public void basicViewFile() {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
				mainPanel.appendConsole("BASIC view '" + name + "'");		
				byte[] data = getFileData(i);
				String basic = DiskImage.parseCbmBasicPrg(data);
				if (basic != null && !basic.isEmpty()) {
					new TextViewFrame((JDialog) null, DroiD64.PROGNAME+" - BASIC view", name, basic, false);
				} else {
					mainPanel.appendConsole("Failed to parse BASIC.");
				}
			}
		}
	}

	public void imageViewFile() {
		List<byte[]> imgList = new ArrayList<byte[]>();
		List<String> imgNameList = new ArrayList<String>();
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				if (table.isRowSelected(i)) {
					String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);				
					mainPanel.appendConsole("Image view '" + name + "'");
					byte[] data = getFileData(i);
					if (data != null && data.length > 0) {
						imgList.add(data);
						imgNameList.add(name);
					}
				}
			}
			if (!imgList.isEmpty()) {
				new ViewImageFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Image view", imgList, imgNameList);
			}
		} catch (IOException e) {
			mainPanel.appendConsole("Image view failed " + e.getMessage());
		}
	}
	
	public void showFile() {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
				mainPanel.appendConsole("View '" + name + "'");
				byte[] data = getFileData(i);
				if (data != null && data.length > 0) {
					new TextViewFrame(DroiD64.PROGNAME+" - Text", name, data);
				}
			}
		}
	}

	public void newDiskImage() {
		mainPanel.appendConsole("New disk image.");
		newDiskName = "";
		new RenameD64Frame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - New disk",	this, "", "", true );
		if (newDiskNameSuccess) {
			mainPanel.appendConsole("New Diskname is: \""+newDiskName+", "+newDiskID+"\".");
			String defaultName = newDiskName!= null && !newDiskName.trim().isEmpty() ? newDiskName.trim() : null;
			switch (newDiskType) {
			case DiskImage.D64_IMAGE_TYPE: defaultName = newDiskName + DiskImage.D64_EXT; break;
			case DiskImage.D71_IMAGE_TYPE: defaultName = newDiskName + DiskImage.D71_EXT; break;
			case DiskImage.D81_IMAGE_TYPE: defaultName = newDiskName + DiskImage.D81_EXT; break;
			case DiskImage.T64_IMAGE_TYPE: defaultName = newDiskName + DiskImage.T64_EXT; break;
			}
			if (newCompressedDisk && defaultName != null) {
				defaultName = defaultName + DiskImage.GZIP_EXT;
			} 
			String saveName = DiskImage.checkFileNameExtension(newDiskType, newCompressedDisk, saveDiskImageFileDialog(currentImagePath, defaultName));
			if ( saveName != null) {
				mainPanel.appendConsole("Selected file: \""+saveName+"\".");
				if (newDiskType == DiskImage.D64_IMAGE_TYPE) {
					diskImage = new D64();					
					diskImage.setImageFormat(newCpmDisk ? DiskImage.D64_CPM_C128_IMAGE_TYPE : DiskImage.D64_IMAGE_TYPE);
				} else if (newDiskType == DiskImage.D81_IMAGE_TYPE) {
					diskImage = new D81();
					diskImage.setImageFormat(newCpmDisk ? DiskImage.D71_CPM_IMAGE_TYPE : DiskImage.D71_IMAGE_TYPE);
				} else if (newDiskType == DiskImage.D71_IMAGE_TYPE) {
					diskImage = new D71();
					diskImage.setImageFormat(newCpmDisk ? DiskImage.D81_CPM_IMAGE_TYPE : DiskImage.D81_IMAGE_TYPE);
				} else {
					mainPanel.appendConsole("Filename with unknown file extension. Can't detect format.\n");
					return;
				}
				diskImage.setCompressed(newCompressedDisk);
				diskImage.saveNewImage(saveName, newDiskName, newDiskID);
				mainPanel.appendConsole(diskImage.getFeedbackMessage());
				imageLoaded = true;
				diskImageFileName = saveName;
				reloadDiskImage(true);
			}
		}
	}
	
	public void renameDisk() {
		mainPanel.appendConsole("Rename disk.");
		newDiskName = "";
		new RenameD64Frame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Rename disk", this,
				diskImage.getBam().getDiskName(), diskImage.getBam().getDiskId(), false);
		if (newDiskNameSuccess) {
			mainPanel.appendConsole("New diskname is: \""+newDiskName+", "+newDiskID+"\".");
			saveName = diskImageFileName;
			diskImage.renameImage(saveName, newDiskName, newDiskID);
			mainPanel.appendConsole(diskImage.getFeedbackMessage());
			reloadDiskImage(true);
		}
	}

	public void validateDisk() {
		Integer errors = diskImage.validate(null);
		mainPanel.appendConsole(diskImage.getFeedbackMessage());
		if (errors != null) {
			Integer warnings = diskImage.getWarnings();
			mainPanel.appendConsole("Validated disk and found "+errors+" errors and "+warnings+" warnings.");
			new ValidationFrame(diskImage.getValidationErrorList(), this);
		}
	}
	
	public void unloadDisk() {
		mainPanel.appendConsole("Unload disk.");
		diskImage = null;
		diskName.setText("");
		diskImageFileName = "";
		imageLoaded = false;
		diskName.setText("No file");
		diskLabel.setText("NO DISK");
		clearDirTable();
		loadLocalDirectory(currentImagePath);
		table.revalidate();
		if (openedRow != null && openedRow.intValue() < table.getRowCount()) {
			// Select and scroll to last opened file
			table.getSelectionModel().setSelectionInterval(openedRow, openedRow);
			table.scrollRectToVisible(new Rectangle(table.getCellRect(openedRow, 0, true)));
			openedRow = null;
		}
	}
	
	public String getCurrentImagePath() {
		return currentImagePath;
	}
	
	public void renamePRG() {
		boolean filesRenamed = false;
		boolean success = false;
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				newPRGName = "";
				if (imageLoaded) {
					CbmFile cbmFile = diskImage.getCbmFile(i);
					String filename = cbmFile.getName();
					mainPanel.appendConsole("RenamePRG: " + filename);
					new RenamePRGFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Rename file ", this, filename, cbmFile.getFileType());
					if (newPRGNameSuccess) {
						filesRenamed = true;
						mainPanel.appendConsole("New filename is: \""+newPRGName+"\" "+DiskImage.getFileType(newPRGType)+".\n" +
								"["+cbmFile.getDirPosition()+"] \"" + filename + "\"");
						diskImage.renamePRG(i, newPRGName, newPRGType);
						mainPanel.appendConsole(diskImage.getFeedbackMessage());
						success = true;
					}
				} else {
					// local file
					String filename = (String) tableModel.getValueAt(i, 1);
					mainPanel.appendConsole("RenamePRG: '" + filename + "'");
					File oldFile = new File(currentImagePath + File.separator + filename);
					if (oldFile.isFile()) {
						new RenamePRGFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Rename file ", this, filename);
						if (newPRGNameSuccess && !filename.equals(newPRGName)) {
							File newFile = new File(currentImagePath + File.separator + newPRGName);
							if (!newFile.exists()) {
								filesRenamed = true;
								mainPanel.appendConsole("New filename is: \""+newPRGName+"\"");
								oldFile.renameTo(newFile);
							} else {
								mainPanel.appendConsole("Error: File "+newFile.getName()+" already exists.");
							}
						}
					}
				}
			}
		}
		if (filesRenamed) {
			if (success && imageLoaded) {
				diskImage.writeImage(diskImageFileName);
				mainPanel.appendConsole(diskImage.getFeedbackMessage());
			}		
			reloadDiskImage(true);
		}
	}

	public void newFile() {
		newPRGName = "";
		new RenamePRGFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - New file ", this, "", DiskImage.TYPE_DEL );
		if (newPRGNameSuccess) {
			CbmFile cbmFile = new CbmFile();
			cbmFile.setName(DiskImage.cbmFileName(newPRGName));
			cbmFile.setFileType(newPRGType);
			mainPanel.appendConsole("newFile: " + cbmFile.getName());
			diskImage.addDirectoryEntry(cbmFile, 0, 0, false, 0);
			diskImage.writeImage(diskImageFileName);
			reloadDiskImage(true);
		}
	}
	
	public void delPRG() {
		try {
			boolean deletedFiles = false;
			for (int i = 0; i < table.getRowCount(); i++) {
				if (table.isRowSelected(i)) {
					String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
					mainPanel.appendConsole("Delete [" + i + "] " + name);
					if (imageLoaded) {
						diskImage.deleteFile(diskImage.getCbmFile(i));
						mainPanel.appendConsole(diskImage.getFeedbackMessage());
						deletedFiles = true;
					} else {
						File file = new File(name);
						if (file.isFile()) {
							file.delete();
						}
						deletedFiles = true;
					}
				}
			}
			if (deletedFiles) {
				if (imageLoaded) {
					diskImage.writeImage(diskImageFileName);
				}
				reloadDiskImage(true);
			}
		} catch (CbmException e) {
			mainPanel.appendConsole("Error: "+e.getMessage());
		}
	}

	private void clearDirTable(){
		tableModel.clear();
	}

	public void setOtherDiskPanelObject ( DiskPanel otherOne ) {
		otherDiskPanel = otherOne;
	}

	public void reloadDiskImage(boolean updateList){
		if (imageLoaded ){
			try {
				diskImage = DiskImage.getDiskImage(diskImageFileName);
				diskName.setText(diskImageFileName);
				clearDirTable();
				updateD64File();
				if (updateList) {
					showDirectory();
				}
			} catch (CbmException e) {
				mainPanel.appendConsole("\nError: "+e.getMessage());
				diskName.setText("No file");
				diskLabel.setText("NO DISK");
				imageLoaded = false;
				repaint();
			}
		} else {
			loadLocalDirectory(currentImagePath);
		}
	}

	public void setTableColors() {
		int mode = tableModel.getMode();
		if (active) {
			switch (mode) {
			case EntryTableModel.MODE_CBM:
				table.setGridColor(Settings.getDirColorBg());
				table.setBackground(Settings.getDirColorBg());
				table.setForeground(Settings.getDirColorFg());
				diskLabel.setBackground(Settings.getDirColorBg());
				diskLabel.setForeground(Settings.getDirColorFg());
				diskLabelPane.setBackground(Settings.getDirColorBg());
				diskLabelPane.setForeground(Settings.getDirColorFg());
				table.setRowHeight(Settings.getRowHeight());
				try {
					table.setFont(Settings.getCommodoreFont());
				} catch (CbmException e) {
					mainPanel.appendConsole("Failed to set font."+e.getMessage());
				}
				break;
			case EntryTableModel.MODE_CPM:
				table.setGridColor(Settings.getDirCpmColorBg());
				table.setBackground(Settings.getDirCpmColorBg());
				table.setForeground(Settings.getDirCpmColorFg());
				diskLabel.setBackground(Settings.getDirCpmColorBg());
				diskLabel.setForeground(Settings.getDirCpmColorFg());
				diskLabelPane.setBackground(Settings.getDirCpmColorBg());
				diskLabelPane.setForeground(Settings.getDirCpmColorFg());
				table.setRowHeight(Settings.getRowHeight());
				try {
					table.setFont(Settings.getCommodoreFont());
				} catch (CbmException e) {
					mainPanel.appendConsole("Failed to set font."+e.getMessage());
				}
				break;
			case EntryTableModel.MODE_LOCAL:
				table.setGridColor(Settings.getDirLocalColorBg());
				table.setBackground(Settings.getDirLocalColorBg());
				table.setForeground(Settings.getDirLocalColorFg());
				diskLabel.setBackground(Settings.getDirLocalColorBg());
				diskLabel.setForeground(Settings.getDirLocalColorFg());
				diskLabelPane.setBackground(Settings.getDirLocalColorBg());
				diskLabelPane.setForeground(Settings.getDirLocalColorFg());
				table.setRowHeight(Settings.getLocalRowHeight());
				Font font = (new JTextArea("")).getFont();
				Font font2 = new Font(font.getName(), font.getStyle(), Settings.getLocalFontSize());
				table.setFont(font2);
				break;
			default:
				System.out.println("DiskPanel.setTableColors: unknown mode "+mode);
			}
		} else {
			
			switch (mode) {
			case EntryTableModel.MODE_CBM:
				table.setGridColor(Settings.getDirColorBg().darker());
				table.setBackground(Settings.getDirColorBg().darker());
				table.setForeground(Settings.getDirColorFg().darker());
				diskLabel.setBackground(Settings.getDirColorBg().darker());
				diskLabel.setForeground(Settings.getDirColorFg().darker());
				diskLabelPane.setBackground(Settings.getDirColorBg().darker());
				diskLabelPane.setForeground(Settings.getDirColorFg().darker());
				table.setRowHeight(Settings.getRowHeight());
				try {
					table.setFont(Settings.getCommodoreFont());
				} catch (CbmException e) {
					mainPanel.appendConsole("Failed to set font."+e.getMessage());
				}
				break;
			case EntryTableModel.MODE_CPM:
				table.setGridColor(Settings.getDirCpmColorBg().darker());
				table.setBackground(Settings.getDirCpmColorBg().darker());
				table.setForeground(Settings.getDirCpmColorFg().darker());
				diskLabel.setBackground(Settings.getDirCpmColorBg().darker());
				diskLabel.setForeground(Settings.getDirCpmColorFg().darker());
				diskLabelPane.setBackground(Settings.getDirCpmColorBg().darker());
				diskLabelPane.setForeground(Settings.getDirCpmColorFg().darker());
				table.setRowHeight(Settings.getRowHeight());
				try {
					table.setFont(Settings.getCommodoreFont());
				} catch (CbmException e) {
					mainPanel.appendConsole("Failed to set font."+e.getMessage());
				}
				break;
			case EntryTableModel.MODE_LOCAL:
				table.setGridColor(Settings.getDirLocalColorBg());
				table.setBackground(Settings.getDirLocalColorBg().darker());
				table.setForeground(Settings.getDirLocalColorFg());
				diskLabel.setBackground(Settings.getDirLocalColorBg().darker());
				diskLabel.setForeground(Settings.getDirLocalColorFg());
				diskLabelPane.setBackground(Settings.getDirLocalColorBg().darker());
				diskLabelPane.setForeground(Settings.getDirLocalColorFg());
				table.setRowHeight(Settings.getLocalRowHeight());
				Font font = (new JTextArea("")).getFont();
				Font font2 = new Font(font.getName(), font.getStyle(), Settings.getLocalFontSize());
				table.setFont(font2);
				break;
			default:
				System.out.println("DiskPanel.setTableColors: unknown mode "+mode);
			}
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
				if (event.getActionCommand()== "Load Disk"){
					openedRow = null;
					openDiskImage(openDiskImageFileDialog(currentImagePath), true);
				}
			}
		});
		menuItem = new JMenuItem("Show BAM", 'b');
		menu.add (menuItem);
		menuItem.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getActionCommand()== "Show BAM"){
					if (imageLoaded == false){
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

	public void setDirectory(String dir) {
		this.directory = dir;
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
	
	public boolean isImageLoaded() {
		return imageLoaded;
	}
	
	public String getDirectory() {
		return directory;
	}

	public void moveFile(final boolean upwards) {
		if (!imageLoaded) {
			return;
		}
		boolean save = false;
		int newPos = -1;
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				if ((upwards && i==0) || (!upwards && i == table.getRowCount() - 1)) {
					// Can't be moved any more. Is already first or last.
					continue;
				}
				if (upwards) {
					for (int j=i - 1; j >= 0; j--) {
						if (!diskImage.getCbmFile(j).isFileScratched()) {
							newPos = j;
							break;
						}
					}
				} else {
					for (int j=i + 1; j < diskImage.getFilenumberMax(); j++) {
						if (!diskImage.getCbmFile(j).isFileScratched()) {
							newPos = j;
							break;
						}
					}
				}
				if (newPos >= 0) {
					CbmFile cbmFile1 = new CbmFile(diskImage.getCbmFile(i));
					CbmFile cbmFile2 = new CbmFile(diskImage.getCbmFile(newPos));
					diskImage.switchFileLocations(cbmFile1, cbmFile2);
					save = true;
					diskImage.readDirectory();
				}
			}
		}
		if (save) {
			diskImage.writeImage(diskImageFileName);
		}
		reloadDiskImage(true);
		if (save && newPos >= 0) {
			table.setRowSelectionInterval(newPos, newPos);
		}
	}
	
	public void sortFiles() {
		if (!imageLoaded) {
			return;
		}		
		int rowCount =  table.getRowCount();
		boolean swapped = true;
		for (int i = 0; i < (rowCount - 1) && swapped; i++) {
			swapped = false;
			for (int j = 0; j < (rowCount - i - 1); j++) {
				CbmFile cbmFile1 = diskImage.getCbmFile(j);
				CbmFile cbmFile2 = diskImage.getCbmFile(j + 1);
				if (cbmFile1.compareTo(cbmFile2) > 0) {
					diskImage.setCbmFile(j, cbmFile2);
					diskImage.setCbmFile(j + 1, cbmFile1);
					diskImage.switchFileLocations(cbmFile1, cbmFile2);
					swapped = true;
					diskImage.readDirectory();
				}
			}
		}
		diskImage.writeImage(diskImageFileName);
		reloadDiskImage(true);
	}

	public List<ValidationError> repairValidationErrors(List<Integer> repairList) {
		if (!imageLoaded) {
			return null;
		}	
		diskImage.validate(repairList);
		diskImage.writeImage(diskImageFileName);
		reloadDiskImage(true);
		return diskImage.getValidationErrorList();
	}
	
}
