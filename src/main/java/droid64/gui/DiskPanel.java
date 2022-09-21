package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.dnd.DnDConstants;
import java.awt.dnd.DropTarget;
import java.awt.dnd.DropTargetDropEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.TransferHandler;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import droid64.DroiD64;
import droid64.d64.BasicParser;
import droid64.d64.CbmException;
import droid64.d64.CbmFile;
import droid64.d64.CpmFile;
import droid64.d64.D64;
import droid64.d64.D67;
import droid64.d64.D71;
import droid64.d64.D80;
import droid64.d64.D81;
import droid64.d64.D82;
import droid64.d64.DirEntry;
import droid64.d64.DiskImage;
import droid64.d64.T64;
import droid64.d64.Utility;
import droid64.d64.ValidationError;
import droid64.db.DaoFactory;
import droid64.db.DatabaseException;
import droid64.db.Disk;

/**<pre style='font-family:sans-serif;'>
 * Created on 23.06.2004<br>
 *
 *   droiD64 - A graphical filemanager for D64 files<br>
 *   Copyright (C) 2004 Wolfram Heyer<br>
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
 *   eMail: wolfvoz@users.sourceforge.net <br>
 *   http://droid64.sourceforge.net
 *
 * @author wolf
 * @author henrik
 * </pre>
 */
public class DiskPanel extends JPanel implements TableModelListener {

	private static final long serialVersionUID = 1L;

	private static final String PARENT_DIR = "..";
	private static final String ZIP_EXT = ".zip";

	private DiskPanel otherDiskPanel;
	private DiskImage diskImage = null;
	private EntryTableModel tableModel;
	private MainPanel mainPanel;

	/** Disk label from loaded disk image */
	private JLabel diskLabel;
	/** Name (path) of current directory or loaded image */
	private JComboBox<String> diskName;

	private JTable table;
	private JPanel diskLabelPane;

	/** True when a disk image is loaded */
	private boolean imageLoaded = false;
	/** Path and file name of loaded disk image */
	private String diskImageFileName = "";
	/** Path to currently used directory on file system */
	private String currentImagePath = null;
	/** True when a Zip file has been loaded */
	private boolean zipFileLoaded = false;
	private String directory = ".";
	private int rowHeight = 12;
	/** True when this is the active disk panel */
	private boolean active = false;
	/** This is the row of the last opened file in the file list */
	private Integer openedRow = null;

	private List<String> diskNameHistory = new ArrayList<>();

	public DiskPanel(final MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		tableModel = new EntryTableModel();
		tableModel.setMode(EntryTableModel.MODE_LOCAL);
		rowHeight = Settings.getFontSize() + 2;
		JPanel dirPanel = drawDirPanel();
		setTableColors();
		setLayout(new BorderLayout());
		add(dirPanel, BorderLayout.CENTER);
		setBorder(BorderFactory.createRaisedBevelBorder());
		setupDragDrop(this);
	}

	public void setActive(boolean active) {
		if (active) {
			setBackground(Settings.getActiveBorderColor());
			this.active = true;
			this.table.grabFocus();
			this.table.requestFocus();
			if (otherDiskPanel != null) {
				otherDiskPanel.active = false;
				otherDiskPanel.setBackground(Settings.getInactiveBorderColor());
			}
			mainPanel.setButtonState();
		} else {
			setBackground(Settings.getInactiveBorderColor());
			this.active = false;
			if (otherDiskPanel != null) {
				otherDiskPanel.active = true;
				otherDiskPanel.setBackground(Settings.getActiveBorderColor());
				otherDiskPanel.table.grabFocus();
				otherDiskPanel.table.requestFocus();
			}
		}
		setTableColors();
		otherDiskPanel.setTableColors();
	}

	public boolean isActive() {
		return this.active;
	}

	private void setupDragDrop(final DiskPanel diskPanel) {
		setDropTarget(new DropTarget() {
			private static final long serialVersionUID = 1L;
			@Override
			public synchronized void drop(DropTargetDropEvent event) {
				try {
					handleDropEvent(diskPanel, event);
				} catch (Exception e) {	//NOSONAR
					mainPanel.appendConsole("Failed to open dropped item.\n"+e.getMessage()+"\n");
				}
			}
		});

		table.setTransferHandler(new TransferHandler() {
			private static final long serialVersionUID = 1L;

			@Override
			public int getSourceActions(JComponent c) {
				return DnDConstants.ACTION_COPY;
			}

			@Override
			public Transferable createTransferable(JComponent comp) {
				JTable dropTable = (JTable) comp;
				int row = dropTable.getSelectedRow();
				if (!imageLoaded) {
					String value = getLocalFilename(row);
					if (new File(value).exists()) {
						return new StringSelection(value);
					}
					return null;
				} else {
					return new StringSelection((String) dropTable.getValueAt(row, 1));
				}
			}

			@Override
			public boolean canImport(TransferHandler.TransferSupport info) {
				return info.isDataFlavorSupported(DataFlavor.stringFlavor);
			}
		});
		table.setDragEnabled(true);
	}

	private void handleDropEvent(final DiskPanel diskPanel, DropTargetDropEvent event) throws UnsupportedFlavorException, IOException {
		Transferable trans = event.getTransferable();
		if (trans.isDataFlavorSupported(DataFlavor.javaFileListFlavor)) {
			event.acceptDrop(DnDConstants.ACTION_COPY);
			@SuppressWarnings("unchecked")
			List<File> fileList = (List<File>) trans.getTransferData(DataFlavor.javaFileListFlavor);
			for (File file : fileList) {
				mainPanel.appendConsole("Dropped file "+file+"\n");
				doubleClickedLocalfile(file, -1);
				diskPanel.setActive(true);
			}
		} else if (trans.isDataFlavorSupported(DataFlavor.stringFlavor)) {
			event.acceptDrop(DnDConstants.ACTION_COPY);
			String s = (String) trans.getTransferData(DataFlavor.stringFlavor);
			File file = new File(s);
			doubleClickedLocalfile(file, -1);
			diskPanel.setActive(true);
		} else {
			mainPanel.appendConsole("Unsupported drop type.\n");
		}
	}

	private JPanel drawDirPanel() {
		final TableColumnModel columnModel = tableModel.getTableColumnModel();
		tableModel.addTableModelListener(this);

		table = new JTable(tableModel);
		table.setColumnModel(columnModel);
		table.setDefaultRenderer(Object.class, new ListTableCellRenderer(tableModel));
		table.setGridColor(Settings.getDirColorBg());
		table.setRowHeight(rowHeight);
		table.setBackground(Settings.getDirColorBg());
		table.setForeground(Settings.getDirColorFg());
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyPressed(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyReleased(KeyEvent ev) {
				processFileTableKeyEvent(ev.getKeyCode());
			}
		});
		table.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				setActive(true);
				int clickCount = me.getClickCount();
				int row = table.rowAtPoint(me.getPoint());
				if (clickCount == 2 && row >= 0) {
					doubleClickedRow(row);
				}
			}
		});

		MouseAdapter mousePressedToActivate = new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent me) {
				setActive(true);
			}
		};

		diskLabel = new JLabel(Settings.getMessage(Resources.DROID64_NODISK));
		diskLabel.setBackground(Settings.getDirColorBg());
		diskLabel.setForeground(Settings.getDirColorFg());
		diskLabel.addMouseListener(mousePressedToActivate);

		diskLabelPane = new JPanel();
		diskLabelPane.setBackground(Settings.getDirColorBg());
		diskLabelPane.setForeground(Settings.getDirColorFg());
		diskLabelPane.add(diskLabel);
		diskLabelPane.addMouseListener(mousePressedToActivate);

		diskName = new JComboBox<>(diskNameHistory.toArray(new String[0]));
		diskName.setFont(new Font("Verdana", Font.PLAIN, Settings.getFontSize()));
		diskName.setEditable(true);
		diskName.addMouseListener(mousePressedToActivate);
		diskName.addKeyListener(createDiskNameKeyListener());
		diskName.addActionListener(ev -> {
			String selected = (String) diskName.getSelectedItem();
			File dir = new File(selected);
			if (dir.isDirectory()) {
				setDiskName(selected);
				loadLocalDirectory(selected);
			}
			if (!active) {
				setActive(true);
			}
		});
		diskName.addItemListener(ae -> {
			if (!active) {
				setActive(true);
			}
		});

		final JButton backButton = new JButton("<");
		backButton.addActionListener(ae -> {
			unloadDisk();
			setActive(true);
		});

		JPanel diskNamePanel = new JPanel(new BorderLayout());
		diskNamePanel.add(diskName, BorderLayout.CENTER);
		diskNamePanel.add(backButton, BorderLayout.EAST);

		JScrollPane scrollPane = new JScrollPane(table);
		scrollPane.addMouseListener(mousePressedToActivate);

		JPanel dirPanel = new JPanel(new BorderLayout());
		dirPanel.add(diskLabelPane, BorderLayout.NORTH);
		dirPanel.add(scrollPane, BorderLayout.CENTER);
		dirPanel.add(diskNamePanel, BorderLayout.SOUTH);
		dirPanel.setPreferredSize(new Dimension(50, 300));
		return dirPanel;
	}

	private void processFileTableKeyEvent(int keyCode) {
		int row = table.getSelectedRow();
		if (keyCode == KeyEvent.VK_TAB) {
			setActive(!isActive());
		} else if (keyCode == KeyEvent.VK_LEFT) {
			if (imageLoaded) {
				unloadDisk();
			} else if (PARENT_DIR.equals(table.getValueAt(0, 1))) {
				doubleClickedLocalfile(new File(currentImagePath).getParentFile(), 0);
			}
		} else if (keyCode == KeyEvent.VK_DELETE || keyCode == KeyEvent.VK_BACK_SPACE) {
			table.clearSelection();
			table.invalidate();
			table.repaint();
		} else if (row >= 0) {
			if (keyCode == KeyEvent.VK_ENTER) {
				doubleClickedRow(row - 1);
			} else if (keyCode == KeyEvent.VK_RIGHT && (row > 0 || !PARENT_DIR.equals(table.getValueAt(0, 1)))) {
				doubleClickedRow(row);
			}
		}
	}

	private KeyListener createDiskNameKeyListener() {
		return new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ev) { /* Not used */ }
			@Override
			public void keyPressed(KeyEvent ev) {
				/* Not used */
				if (!active) {
					setActive(true);
				}
			}
			@Override
			public void keyReleased(KeyEvent ev) {
				if (ev.getKeyCode() == KeyEvent.VK_ENTER) {
					String selected = (String) diskName.getSelectedItem();
					File dir = new File(selected);
					if (dir.isDirectory()) {
						setDiskName(selected);
						loadLocalDirectory(selected);
					}
				}
			}
		};
	}

	private void setDiskName(String name) {
		if (name == null) {
			return;
		}
		ActionListener[] listeners = diskName.getActionListeners();
		for (final ActionListener listener : listeners) {
			diskName.removeActionListener(listener);
		}

		if (!new File(name).isDirectory()) {
			if (diskName.getItemCount() > 0) {
				diskName.setSelectedIndex(0);
				diskName.setSelectedItem(name);
			}
			for (final ActionListener listener : listeners) {
				diskName.addActionListener(listener);
			}
			return;
		}
		if (!diskNameHistory.contains(name)) {
			diskNameHistory.add(name);
			diskName.addItem(name);
		}
		for (int i=0; i<diskName.getItemCount(); i++) {
			if (name.equals(diskName.getItemAt(i))) {
				diskName.setSelectedIndex(i);
				break;
			}
		}
		for (final ActionListener listener : listeners) {
			diskName.addActionListener(listener);
		}
	}

	private String getLocalFilename(int row) {
		return this.currentImagePath + File.separator + table.getValueAt(row, 1);
	}

	private void doubleClickedRow(int row) {
		if (row < 0) {
			return;
		}
		try {
			openedRow = null;
			if (imageLoaded) {
				CbmFile file = diskImage.getCbmFile(row);
				doubleClickedImageFileEntry(file, row);
			} else if (zipFileLoaded) {
				String entryName = (String) table.getValueAt(row, 1);
				doubleClickedZipFile(entryName, row);
			} else {
				File file;
				if (PARENT_DIR.equals(table.getValueAt(row, 1))) {
					file = new File(currentImagePath).getParentFile();
				} else {
					file = new File(getLocalFilename(row));
				}
				doubleClickedLocalfile(file, row);
			}
		} catch (CbmException e) {	//NOSONAR
			mainPanel.appendConsole("\n"+e.getMessage());
		}
		mainPanel.setButtonState();
	}

	private void doubleClickedImageFileEntry(CbmFile file, int row) throws CbmException {
		if (file != null && file.getFileType() == CbmFile.TYPE_CBM) {
			clearDirTable();
			diskImage.readPartition(file.getTrack(), file.getSector(), file.getSizeInBlocks());
			showDirectory();
		} else if (file != null){
			hexViewFile(row);
		}
	}

	private void doubleClickedZipFile(String zipName, int row) throws CbmException {
		mainPanel.appendConsole("doubleClickedZipFile: "+zipName);

		if (DiskImage.isImageFileName(zipName)) {
			mainPanel.appendConsole("doubleClickedZipFile: is image");
			diskImage = DiskImage.getDiskImage(zipName, getFileData(row));
			setDiskName(currentImagePath + File.separator + zipName);
			diskImageFileName = zipName;
			clearDirTable();
			imageLoaded = true;
			zipFileLoaded = true;
			updateImageFile();
			showDirectory();
		} else if (PARENT_DIR.equals(zipName)) {
			File file = new File(currentImagePath).getParentFile();
			if (file.exists() && file.isDirectory()) {
				loadLocalDirectory(file.getPath());
			}
		} else {
			hexViewFile(row);
		}
		mainPanel.appendConsole("doubleClickedZipFile: done");
	}

	private void doubleClickedLocalfile(File file, int row) {
		if (file.exists()) {
			if (DiskImage.isImageFileName(file)) {
				openDiskImage(file.getPath(), true);
				openedRow = Integer.valueOf(row);
			} else if (file.isDirectory()) {
				loadLocalDirectory(file.getPath());
			} else if (file.isFile()) {
				if (file.getName().toLowerCase().endsWith(ZIP_EXT)) {
					loadZipFile(file);
				} else if (row >= 0) {
					hexViewFile(row);
				} else {
					addFile(file);
				}
			}
		}
	}

	private void showNoDiskLoadedMessage() {
		mainPanel.appendConsole("\nNo disk image file selected. Aborting.");
		JOptionPane.showMessageDialog(mainPanel.getParent(),
				Settings.getMessage(Resources.DROID64_INFO_NOIMAGELOADED),
				DroiD64.PROGNAME + " - No disk",
				JOptionPane.ERROR_MESSAGE);
	}

	@Override
	public void tableChanged(TableModelEvent e) {
		TableModel model = (TableModel) e.getSource();
		table.setModel(model);
		table.revalidate();
	}

	public void openDiskImage(String fileName, boolean updateList) {
		if (fileName != null) {
			if (new File(fileName).canRead()) {
				mainPanel.appendConsole("openDiskImage: "+fileName);
				diskImageFileName = fileName;
				imageLoaded = true;
				reloadDiskImage(updateList);
			} else {
				mainPanel.appendConsole("Error: fail to open file "+fileName);
			}
		}
	}

	/**
	 * Execute external program (plugin) and write its stdout and stderr to the console.
	 * @param prg external program to be executed.
	 */
	public void doExternalProgram(ExternalProgram prg) {
		List<String> selectedFiles = getSelectedFiles();
		String imgFileName = null;
		if (imageLoaded) {
			if (zipFileLoaded) {
				try {
					File tmpfile = File.createTempFile("droid64_", ".img");
					tmpfile.deleteOnExit();
					imgFileName = tmpfile.getAbsolutePath();
					diskImage.writeImage(imgFileName);
					mainPanel.appendConsole("Write temporary image to "+imgFileName);
				} catch (IOException e) {	//NOSONAR
					mainPanel.appendConsole("Failed to create image tempfile from zip file.\n"+e.getMessage());
					return;
				}
			} else {
				imgFileName = diskImageFileName;
			}
		}
		String otherPath = otherDiskPanel.imageLoaded ? otherDiskPanel.diskImageFileName : otherDiskPanel.currentImagePath;
		int imageType = imageLoaded ? diskImage.getImageFormat() : DiskImage.UNKNOWN_IMAGE_TYPE;
		String[] execArgs = prg.getExecute(imgFileName, selectedFiles, otherPath, directory, imageType);
		File imgParentFile;
		if (imgFileName != null) {
			imgParentFile = new File(imgFileName).getParentFile();
		} else if (currentImagePath != null) {
			imgParentFile = new File(currentImagePath);
		} else {
			imgParentFile = null;
		}
		prg.runProgram(imgParentFile, execArgs, mainPanel);
	}

	private List<String> getSelectedFiles() {
		List<String> selectedFiles = new ArrayList<>();
		for (int row = 0; row < table.getRowCount(); row++) {
			if (table.isRowSelected(row)) {
				String selectedFile;
				if (imageLoaded) {
					selectedFile = diskImage.getCbmFile(row).getName().toLowerCase();
				} else {
					selectedFile = currentImagePath + File.separator + ((String) tableModel.getValueAt(row, 1));
				}
				selectedFiles.add(selectedFile);
			}
		}
		return selectedFiles;
	}

	private void showDirectory() {
		if (imageLoaded) {
			mainPanel.appendConsole("Found " + diskImage.getFilesUsedCount() + " files in this "+DiskImage.getImageTypeName(diskImage.getImageFormat()) + " image file.");
			for (int fileNum = 0; fileNum <= diskImage.getFilesUsedCount() - 1;	fileNum++) {
				if (diskImage.getCbmFile(fileNum) != null) {
					tableModel.updateDirEntry(new DirEntry(diskImage.getCbmFile(fileNum), fileNum + 1));
				} else {
					tableModel.updateDirEntry(null);
				}
			}
			String label = diskImage.getBam().getDiskDosType()
					+ " \""	+ diskImage.getBam().getDiskName() + "," + diskImage.getBam().getDiskId()
					+ "\" "+diskImage.getBlocksFree() + " BLOCKS FREE [" + diskImage.getFilesUsedCount() + "]";
			diskLabel.setText(label);
			tableModel.setMode(diskImage.isCpmImage() ? EntryTableModel.MODE_CPM : EntryTableModel.MODE_CBM);
			TableColumnModel tcm = table.getTableHeader().getColumnModel();
			for (int i=0; i< tcm.getColumnCount(); i++) {
				tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
			}
			setTableColors();
			table.setColumnModel(tableModel.getTableColumnModel());
			table.revalidate();
			repaint();
		} else {
			mainPanel.appendConsole("no image loaded");
		}
	}

	/**
	 * Load contents from a directory on local file system.
	 * @param path to a directory to open
	 */
	public void loadLocalDirectory(String path) {
		File dir = new File(path != null ? path : Settings.getDefaultImageDir());
		if (dir.isDirectory()) {
			mainPanel.appendConsole("loadLocalDirectory: "+dir.getAbsolutePath());
			clearDirTable();
			int fileNum = 0;
			File parentFile = dir.getParentFile();
			if (parentFile != null) {
				DirEntry parent = new DirEntry(parentFile, ++fileNum);
				parent.setName(PARENT_DIR);
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
				setDiskName(path);
				diskLabel.setText("FILE SYSTEM");
			} else {
				diskLabel.setText("ERROR");
			}
		} else if (dir.getName().toLowerCase().endsWith(ZIP_EXT)) {
			loadZipFile(dir);
			return;
		} else {
			clearDirTable();
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
	 * Load a zip file
	 * @param file the zip file to load
	 */
	private void loadZipFile(File file) {
		mainPanel.appendConsole("loadZipFile: "+file.getName());
		try {
			List<DirEntry> list = Utility.getZipFileEntries(file, 2);
			clearDirTable();
			DirEntry parent = new DirEntry(file.getParentFile(), 1);
			parent.setName(PARENT_DIR);
			tableModel.updateDirEntry(parent);
			list.forEach(entry -> tableModel.updateDirEntry(entry));
			tableModel.setMode(EntryTableModel.MODE_LOCAL);
			TableColumnModel tcm = table.getTableHeader().getColumnModel();
			for (int i=0; i< tcm.getColumnCount(); i++) {
				tcm.getColumn(i).setHeaderValue(tableModel.getColumnName(i));
			}
			setDiskName(file.getAbsolutePath());
			diskLabel.setText("ZIP FILE");
			zipFileLoaded = true;
			currentImagePath = file.getAbsolutePath();
			setTableColors();
			table.setColumnModel(tableModel.getTableColumnModel());
			table.revalidate();
			repaint();
		} catch (CbmException e) {	//NOSONAR
			mainPanel.appendConsole("loadZipFile: "+e.getMessage());
		}
	}

	public void showBAM() {
		if (!imageLoaded){
			showNoDiskLoadedMessage();
			return;
		}
		String[][] bamTab = diskImage.getBamTable();
		if (bamTab != null && bamTab.length > 0) {
			String name =
					diskImage.getBam().getDiskDosType() + " \"" +
							diskImage.getBam().getDiskName() + "," +
							diskImage.getBam().getDiskId() + "\"";
			new BAMFrame(DroiD64.PROGNAME+" - BAM of this disk image", name, bamTab, diskImageFileName, diskImage, isWritableImageLoaded(), mainPanel);
			diskImage.readBAM();
		} else {
			mainPanel.appendConsole("No BAM available.");
		}
	}

	private void updateImageFile() {
		if (imageLoaded) {
			diskImage.readBAM();
			diskImage.readDirectory();
			mainPanel.appendConsole("updateImageFile: "+diskImage.getFeedbackMessage());
			if (Settings.getUseDb()) {
				Disk disk = diskImage.getDisk();
				File f = new File(diskImageFileName);
				File p = f.getAbsoluteFile().getParentFile();
				String d = p != null ? p.getAbsolutePath() : null;
				disk.setFilePath(d);
				disk.setFileName(f.getName());
				disk.setImageType(diskImage.getImageFormat());
				disk.setHostName(Utility.getHostName());
				try {
					DaoFactory.getDaoFactory().getDiskDao().save(disk);
				} catch (DatabaseException e) {	//NOSONAR
					mainPanel.appendConsole("\n"+e.getMessage());
				}
			}
		} else {
			mainPanel.appendConsole("updateImageFile: no image loaded");
		}
	}

	private void logFileSaveFailedAbort(String filename) {
		mainPanel.appendConsole("Failed to save copy of "+filename+".\nAborting copy.");
	}

	public void copyFile() {
		if (otherDiskPanel.zipFileLoaded) {
			mainPanel.appendConsole("Error: copying to Zip files not supported.");
			return;
		}
		boolean filesCopied = false;
		try {
			boolean success = false;
			if (imageLoaded) {
				diskImage.setFeedbackMessage("");
				for (int row = 0; row < table.getRowCount(); row++) {
					if (table.isRowSelected(row)) {
						filesCopied = true;
						mainPanel.appendConsole("Disk copy [" + row + "] " + tableModel.getValueAt(row, 2));
						byte[] saveData = diskImage.getFileData(row);
						mainPanel.appendConsole(diskImage.getFeedbackMessage());
						CbmFile source = diskImage.getCbmFile(row);
						CbmFile copy;
						if (source instanceof CpmFile) {
							copy = new CpmFile((CpmFile) source);
						} else {
							copy = new CbmFile(source);
						}
						if (otherDiskPanel.imageLoaded) {
							// Copy file from image to image
							success = otherDiskPanel.diskImage.saveFile(copy, true, saveData);
							mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
						} else {
							// Copy file from image to local file system
							String outName = otherDiskPanel.currentImagePath + File.separator + Utility.pcFilename(copy);
							mainPanel.appendConsole("DiskPanel.copyFile: "+outName+" class="+copy.getClass().getName());
							File targetFile = new File(outName);
							success = Utility.writeFileSafe(targetFile, saveData);
						}
						if (!success) {
							logFileSaveFailedAbort(copy.getName());
							break;
						}
					}
				}
				if (success && otherDiskPanel.imageLoaded) {
					otherDiskPanel.diskImage.writeImage(otherDiskPanel.diskImageFileName);
					mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
				}
			} else if (zipFileLoaded) {
				for (int row = 0; row < table.getRowCount(); row++) {
					if (table.isRowSelected(row)) {
						String filename = (String) tableModel.getValueAt(row, 1);
						byte[] data = getFileData(row);
						filesCopied = true;
						mainPanel.appendConsole("Zip copy [" + row + "] " + filename);
						if (otherDiskPanel.imageLoaded) {
							// Copy file from local file system to disk image
							CbmFile cbmFile = new CbmFile();
							cbmFile.setName(Utility.cbmFileName(filename, DiskImage.DISK_NAME_LENGTH));
							cbmFile.setFileType(DiskImage.getFileTypeFromFileExtension(filename));
							success = otherDiskPanel.diskImage.saveFile(cbmFile, false, data);
							mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
							if (!success) {
								logFileSaveFailedAbort(filename);
								break;
							}
						} else if (otherDiskPanel.zipFileLoaded) {
							mainPanel.appendConsole("Target is a zip file");
						} else {
							// Copy file from file system to file system (no images involved)
							File targetFile = new File(otherDiskPanel.currentImagePath + File.separator + filename);
							success = Utility.writeFileSafe(targetFile, data);
						}
					}
				}
				if (success && otherDiskPanel.imageLoaded) {
					otherDiskPanel.diskImage.writeImage(otherDiskPanel.diskImageFileName);
					mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
				}
			} else {
				// local file system
				for (int row = 0; row < table.getRowCount(); row++) {
					if (table.isRowSelected(row)) {
						String filename = (String) tableModel.getValueAt(row, 1);
						File sourceFile = new File(currentImagePath + File.separator + filename);
						if (sourceFile.isFile()) {
							filesCopied = true;
							mainPanel.appendConsole("Local copy [" + row + "] " + filename);
							if (otherDiskPanel.imageLoaded) {
								// Copy file from local file system to disk image
								byte[] data = Utility.readFile(sourceFile);
								CbmFile cbmFile = new CbmFile();
								cbmFile.setName(Utility.cbmFileName(filename, DiskImage.DISK_NAME_LENGTH));
								cbmFile.setFileType(DiskImage.getFileTypeFromFileExtension(filename));
								success = otherDiskPanel.diskImage.saveFile(cbmFile, false, data);
								mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
								if (!success) {
									logFileSaveFailedAbort(filename);
									break;
								}
							} else {
								// Copy file from file system to file system (no images involved)
								File targetFile = new File(otherDiskPanel.currentImagePath + File.separator + filename);
								Utility.writeFileSafe(sourceFile, targetFile);
							}
						} else {
							mainPanel.appendConsole("Error: Is not a plain file (" + filename+")");
						}
					}
				}
				if (success && otherDiskPanel.imageLoaded) {
					otherDiskPanel.diskImage.writeImage(otherDiskPanel.diskImageFileName);
					mainPanel.appendConsole(otherDiskPanel.diskImage.getFeedbackMessage());
				}
			}
		} catch (CbmException e) {	//NOSONAR
			mainPanel.appendConsole("Error: Failed to copy files. \n" + e.getMessage());
		}
		if (filesCopied) {
			otherDiskPanel.reloadDiskImage(true);
		}
	}

	private void addFile(File file) {
		if (imageLoaded) {
			try {
				byte[] data = Utility.readFile(file);
				CbmFile cbmFile = new CbmFile();
				cbmFile.setName(Utility.cbmFileName(file.getName(), DiskImage.DISK_NAME_LENGTH));
				cbmFile.setFileType(DiskImage.getFileTypeFromFileExtension(file.getName()));
				boolean success = diskImage.saveFile(cbmFile, false, data);
				mainPanel.appendConsole(diskImage.getFeedbackMessage());
				if (success) {
					diskImage.writeImage(diskImageFileName);
					mainPanel.appendConsole("Saved file. "+diskImage.getFeedbackMessage());

				} else {
					mainPanel.appendConsole("Failed to save file. "+diskImage.getFeedbackMessage());
				}
				reloadDiskImage(true);
			} catch (CbmException e) { //NOSONAR
				mainPanel.appendConsole("Failed to add file.\n"+e.getMessage());
			}
		} else if (!zipFileLoaded) {
			File targetFile = new File(currentImagePath + File.separator + file.getName());
			mainPanel.appendConsole("Add file on local filsystem.\n"+targetFile.getAbsolutePath());
			Utility.writeFileSafe(file, targetFile);
			loadLocalDirectory(currentImagePath);
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
			} else if (zipFileLoaded) {
				return Utility.getDataFromZipFileEntry(currentImagePath, (String) table.getValueAt(fileNum, 1));
			} else {
				File file = new File(getLocalFilename(fileNum));
				return Files.readAllBytes(file.toPath());
			}
		} catch (CbmException | IOException e) {	//NOSONAR
			mainPanel.appendConsole("Failed to get file data : " + e.getMessage());
		}
		return new byte[0];
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

	public void calcMd5Checksum() {
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				if (table.isRowSelected(i)) {
					String sum = Utility.calcMd5Checksum(getFileData(i));
					String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
					mainPanel.appendConsole(sum+"  " + name);
				}
			}
		} catch (CbmException e) {
			mainPanel.appendConsole("MD5 failed " + e);
		}
	}

	private void hexViewFile(int fileNum) {
		byte[] data = getFileData(fileNum);
		if (data == null || data.length == 0) {
			mainPanel.appendConsole("No data");
			return;
		}
		String name = imageLoaded ? diskImage.getCbmFile(fileNum).getName() : getLocalFilename(fileNum);
		new HexViewDialog(DroiD64.PROGNAME+" - Hex view", name, data, data.length, mainPanel, true);
	}

	public void basicViewFile() {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
				mainPanel.appendConsole("BASIC view '" + name + "'");
				byte[] data = getFileData(i);
				String basic = BasicParser.parseCbmBasicPrg(data);
				if (basic != null && !basic.isEmpty()) {
					new TextViewDialog((JDialog) null, DroiD64.PROGNAME+" - BASIC view", name, basic, false, Utility.MIMETYPE_TEXT, mainPanel);
				} else {
					mainPanel.appendConsole("Failed to parse BASIC.");
				}
			}
		}
	}

	public void imageViewFile() {
		List<byte[]> imgList = new ArrayList<>();
		List<String> imgNameList = new ArrayList<>();
		try {
			for (int i = 0; i < table.getRowCount(); i++) {
				if (table.isRowSelected(i)) {
					String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
					mainPanel.appendConsole("Image view '" + name + "'");
					byte[] data = getFileData(i);
					if (data.length > 0) {
						imgList.add(data);
						imgNameList.add(name);
					}
				}
			}
			if (!imgList.isEmpty()) {
				new ViewImageFrame(DroiD64.PROGNAME+" - Image view", imgList, imgNameList, mainPanel);
			}
		} catch (IOException e) {	//NOSONAR
			mainPanel.appendConsole("Image view failed " + e.getMessage());
		}
	}

	public void showFile() {
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
				mainPanel.appendConsole("View '" + name + "'");
				byte[] data = getFileData(i);
				if (data.length > 0) {
					new TextViewDialog(DroiD64.PROGNAME+" - Text", name, data, mainPanel);
				}
			}
		}
	}

	protected void newDiskImage() {
		mainPanel.appendConsole("New disk image.");
		RenameResult result = new RenameResult();
		new RenameDiskImageDialog(DroiD64.PROGNAME+" - New disk", "", "", true, mainPanel, result);
		if (result.isSuccess()) {
			mainPanel.appendConsole("New Diskname is: \""+result.getDiskName()+", "+result.getDiskID()+"\".");
			String defaultName = Settings.checkFileNameExtension(result.getDiskType(), result.isCompressedDisk(), result.getDiskName());
			String imgName = FileDialogHelper.openImageFileDialog(currentImagePath, defaultName, true);
			if (imgName == null) {
				return;
			}
			String saveName = Settings.checkFileNameExtension(result.getDiskType(), result.isCompressedDisk(), imgName);
			if (saveName != null) {
				createNewDisk(result, saveName);
				reloadDiskImage(true);
			}
		}
	}

	protected void createNewDisk(RenameResult result, String fileName) {
		mainPanel.appendConsole("Selected file: \"" + fileName + "\".");
		if (result.getDiskType() == DiskImage.D64_IMAGE_TYPE) {
			diskImage = new D64();
			diskImage.setImageFormat(result.isCpmDisk() ? DiskImage.D64_CPM_C128_IMAGE_TYPE : DiskImage.D64_IMAGE_TYPE);
		} else if (result.getDiskType() == DiskImage.D67_IMAGE_TYPE) {
			diskImage = new D67();
			diskImage.setImageFormat(DiskImage.D67_IMAGE_TYPE);
		} else if (result.getDiskType() == DiskImage.D81_IMAGE_TYPE) {
			diskImage = new D81();
			diskImage.setImageFormat(result.isCpmDisk() ? DiskImage.D81_CPM_IMAGE_TYPE : DiskImage.D81_IMAGE_TYPE);
		} else if (result.getDiskType() == DiskImage.D71_IMAGE_TYPE) {
			diskImage = new D71();
			diskImage.setImageFormat(result.isCpmDisk() ? DiskImage.D71_CPM_IMAGE_TYPE : DiskImage.D71_IMAGE_TYPE);
		} else if (result.getDiskType() == DiskImage.D80_IMAGE_TYPE) {
			diskImage = new D80();
			diskImage.setImageFormat(DiskImage.D80_IMAGE_TYPE);
		} else if (result.getDiskType() == DiskImage.D82_IMAGE_TYPE) {
			diskImage = new D82();
			diskImage.setImageFormat(DiskImage.D82_IMAGE_TYPE);
		} else if (result.getDiskType() == DiskImage.T64_IMAGE_TYPE) {
			diskImage = new T64();
			diskImage.setImageFormat(DiskImage.T64_IMAGE_TYPE);
		} else {
			mainPanel.appendConsole("Filename with unknown file extension. Can't detect format.\n");
			return;
		}
		diskImage.setCompressed(result.isCompressedDisk());
		diskImage.saveNewImage(fileName, result.getDiskName(), result.getDiskID());
		mainPanel.appendConsole(diskImage.getFeedbackMessage());
		imageLoaded = true;
		diskImageFileName = fileName;
	}

	public void renameDisk() {
		mainPanel.appendConsole("Rename disk.");
		if (diskImage == null || diskImage.getBam() == null) {
			return;
		}
		RenameResult result = new RenameResult();
		new RenameDiskImageDialog(DroiD64.PROGNAME+" - Rename disk",
				diskImage.getBam().getDiskName(), diskImage.getBam().getDiskId(), false, mainPanel, result);
		if (result.isSuccess()) {
			mainPanel.appendConsole("New diskname is: \""+result.getDiskName()+", "+result.getDiskID()+"\".");
			diskImage.renameImage(diskImageFileName, result.getDiskName(), result.getDiskID());
			mainPanel.appendConsole(diskImage.getFeedbackMessage());
			reloadDiskImage(true);
		}
	}

	public void validateDisk() {
		if (diskImage == null) {
			return;
		}
		Integer errors = diskImage.validate(new ArrayList<Integer>());
		mainPanel.appendConsole(diskImage.getFeedbackMessage());
		if (errors != null) {
			Integer warnings = diskImage.getWarnings();
			mainPanel.appendConsole("Validated disk and found "+errors+" errors and "+warnings+" warnings.");
			new ValidationFrame(diskImage.getValidationErrorList(), isVisible() ? this : null);
		}
	}

	public void unloadDisk() {
		mainPanel.appendConsole("Unload disk.");
		if (isImageLoaded()) {
			diskImage = null;
			setDiskName(null);
			diskImageFileName = "";
			imageLoaded = false;
			diskLabel.setText(Settings.getMessage("droid64.nodisk"));
			clearDirTable();
			loadLocalDirectory(currentImagePath);
			table.revalidate();
			if (openedRow != null && openedRow < table.getRowCount() && openedRow >= 0) {
				// Select and scroll to last opened file
				table.getSelectionModel().setSelectionInterval(openedRow, openedRow);
				table.scrollRectToVisible(new Rectangle(table.getCellRect(openedRow, 0, true)));
				openedRow = null;
			}
		} else {
			String dir = getCurrentImagePath();
			File dirfile = dir != null ? new File(dir) : null;
			if (dirfile != null && dirfile.getParent() != null) {
				loadLocalDirectory(dirfile.getParent());
			}
		}
	}

	public String getCurrentImagePath() {
		return currentImagePath;
	}

	public void renameFile() {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: renaming files in Zip files not supported.");
			return;
		}
		boolean filesRenamed = false;
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				if (imageLoaded) {
					CbmFile cbmFile = diskImage.getCbmFile(i);
					String filename = cbmFile.getName();
					mainPanel.appendConsole("RenameFile: " + filename);
					RenameResult result = new RenameResult();
					new RenameFileDialog(DroiD64.PROGNAME+" - Rename file ", filename, cbmFile.getFileType(), mainPanel, result);
					if (result.isSuccess()) {
						filesRenamed = true;
						mainPanel.appendConsole("New filename is: \""+result.getFileName()+"\" "+CbmFile.getFileType(result.getFileType())+".\n" +
								"["+cbmFile.getDirPosition()+"] \"" + filename + "\"");
						diskImage.renameFile(i, result.getFileName(), result.getFileType());
						mainPanel.appendConsole(diskImage.getFeedbackMessage());
					}
				} else {
					// local file
					String filename = (String) tableModel.getValueAt(i, 1);
					mainPanel.appendConsole("RenameFile: '" + filename + "'");
					File oldFile = new File(currentImagePath + File.separator + filename);
					if (oldFile.isFile()) {
						RenameResult result = new RenameResult();
						new RenameFileDialog(DroiD64.PROGNAME+" - Rename file ", filename, mainPanel, result);
						if (result.isSuccess() && !filename.equals(result.getFileName())) {
							File newFile = new File(currentImagePath + File.separator + result.getFileName());
							if (!newFile.exists()) {
								filesRenamed = true;
								mainPanel.appendConsole("New filename is: \""+result.getFileName()+"\"");
								if (!oldFile.renameTo(newFile)) {
									mainPanel.appendConsole("Error: Failed to rename "+newFile.getName());
								}
							} else {
								mainPanel.appendConsole("Error: File "+newFile.getName()+" already exists.");
							}
						}
					}
				}
			}
		}
		if (filesRenamed) {
			if (imageLoaded) {
				diskImage.writeImage(diskImageFileName);
				mainPanel.appendConsole(diskImage.getFeedbackMessage());
			}
			reloadDiskImage(true);
		}
	}

	public void newFile() {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: add new files to Zip files not supported.");
			return;
		}
		RenameResult result = new RenameResult();
		new RenameFileDialog(DroiD64.PROGNAME+" - New file ", "", CbmFile.TYPE_DEL, mainPanel, result);
		if (result.isSuccess()) {
			newFile(result);
			reloadDiskImage(true);
		}
	}

	protected void newFile(RenameResult result) {
		CbmFile cbmFile = new CbmFile();
		cbmFile.setName(result.getFileName());
		cbmFile.setFileType(result.getFileType());
		mainPanel.appendConsole("newFile: " + cbmFile.getName());
		diskImage.addDirectoryEntry(cbmFile, 0, 0, false, 0);
		diskImage.writeImage(diskImageFileName);
		mainPanel.appendConsole(diskImage.getFeedbackMessage());
	}

	public void deleteFile() {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: deleting from Zip files not supported.");
			return;
		}
		boolean deletedFiles = false;
		for (int i = 0; i < table.getRowCount(); i++) {
			if (table.isRowSelected(i)) {
				String name = imageLoaded ? diskImage.getCbmFile(i).getName() : getLocalFilename(i);
				mainPanel.appendConsole("Delete [" + i + "] " + name);
				if (imageLoaded) {
					try {
						diskImage.deleteFile(diskImage.getCbmFile(i));
						mainPanel.appendConsole(diskImage.getFeedbackMessage());
						deletedFiles = true;
					} catch (CbmException e) {
						mainPanel.appendConsole("Error: "+e.getMessage());
					}
				} else {
					File file = new File(name);
					try {
						Files.delete(file.toPath());
						deletedFiles = true;
					} catch (IOException e) {
						mainPanel.appendConsole("Failed to delete "+file);
					}
				}
			}
		}
		if (deletedFiles) {
			if (imageLoaded) {
				diskImage.writeImage(diskImageFileName);
			}
			reloadDiskImage(true);
		}
	}

	private void clearDirTable(){
		tableModel.clear();
		zipFileLoaded = false;
	}

	public void setOtherDiskPanelObject ( DiskPanel otherOne ) {
		otherDiskPanel = otherOne;
	}

	public void reloadDiskImage(boolean updateList){
		mainPanel.appendConsole("reloadDiskImage "+imageLoaded+"\n");
		if (imageLoaded ){
			try {
				diskImage = DiskImage.getDiskImage(diskImageFileName);
				setDiskName(diskImageFileName);
				clearDirTable();
				updateImageFile();
				if (updateList) {
					showDirectory();
				}
			} catch (CbmException e) {	//NOSONAR
				mainPanel.appendConsole("\nError: "+e.getMessage());
				setDiskName(null);
				diskLabel.setText(Settings.getMessage(Resources.DROID64_NODISK));
				imageLoaded = false;
				repaint();
			}
		} else {
			loadLocalDirectory(currentImagePath);
		}
	}

	public void setTableColors() {
		try {
			int mode = tableModel.getMode();
			Color colorFg;
			Color colorBg;
			Color colorGrid;
			Font font;
			if (EntryTableModel.MODE_CBM == mode) {
				colorBg = active ? Settings.getDirColorBg() : Settings.getDirColorBg().darker();
				colorFg = active ? Settings.getDirColorFg() : Settings.getDirColorFg().darker();
				colorGrid = active ? Settings.getDirColorBg() : Settings.getDirColorBg().darker();
				table.setRowHeight(Settings.getRowHeight());
				font = Settings.getCommodoreFont();
			} else if (EntryTableModel.MODE_CPM == mode) {
				colorBg = active ? Settings.getDirCpmColorBg() : Settings.getDirCpmColorBg().darker();
				colorFg = active ? Settings.getDirCpmColorFg() : Settings.getDirCpmColorFg().darker();
				colorGrid = active ? Settings.getDirCpmColorBg() : Settings.getDirCpmColorBg().darker();
				table.setRowHeight(Settings.getRowHeight());
				font = Settings.getCommodoreFont();
			} else if (EntryTableModel.MODE_LOCAL == mode) {
				colorBg = active ? Settings.getDirLocalColorBg() : Settings.getDirLocalColorBg().darker();
				colorFg = Settings.getDirLocalColorFg();
				colorGrid = Settings.getDirLocalColorBg();
				table.setRowHeight(Settings.getLocalRowHeight());
				Font tmpFont = new JTextArea("").getFont();
				font = new Font(tmpFont.getName(), tmpFont.getStyle(), Settings.getLocalFontSize());
			} else {
				mainPanel.appendConsole("DiskPanel.setTableColors: unknown mode "+mode);
				return;
			}
			table.setGridColor(colorGrid);
			table.setBackground(colorBg);
			table.setForeground(colorFg);
			diskLabel.setBackground(colorBg);
			diskLabel.setForeground(colorFg);
			diskLabelPane.setBackground(colorBg);
			diskLabelPane.setForeground(colorFg);
			table.setFont(font);
			diskLabel.setFont(font);
		} catch (CbmException e) {	//NOSONAR
			mainPanel.appendConsole("Failed to set font."+e.getMessage());
		}
	}

	private ActionListener createDiskImageMenuActionListener() {
		return event -> {
			String cmd = event.getActionCommand();
			if (Resources.DROID64_MENU_DISK_NEW.equals(cmd)) {
				newDiskImage();
			} else if (Resources.DROID64_MENU_DISK_LOAD.equals(cmd)) {
				openedRow = null;
				openDiskImage(FileDialogHelper.openImageFileDialog(currentImagePath, null, false), true);
			} else if (Resources.DROID64_MENU_DISK_SHOWBAM.equals(cmd)) {
				showBAM();
			} else if (Resources.DROID64_MENU_DISK_UNLOAD.equals(cmd)) {
				unloadDisk();
			} else if (Resources.DROID64_MENU_DISK_COPYFILE.equals(cmd)) {
				copyFile();
			} else if (Resources.DROID64_MENU_DISK_RENAMEDISK.equals(cmd)) {
				renameDisk();
			} else if (Resources.DROID64_MENU_DISK_RENAMEFILE.equals(cmd)) {
				renameFile();
			} else if (Resources.DROID64_MENU_DISK_DELETEFILE.equals(cmd)) {
				deleteFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWTEXT.equals(cmd)) {
				showFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWHEX.equals(cmd)) {
				hexViewFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWBASIC.equals(cmd)) {
				basicViewFile();
			} else if (Resources.DROID64_MENU_DISK_VIEWIMAGE.equals(cmd)) {
				imageViewFile();
			} else if (Resources.DROID64_MENU_DISK_PRINTDIR.equals(cmd)) {
				printDir();
			} else if (Resources.DROID64_MENU_DISK_MD5.equals(cmd)) {
				calcMd5Checksum();
			} else if (Resources.DROID64_MENU_DISK_MIRROR.equals(cmd)) {
				openOtherPanel();
			}
		};
	}

	private void openOtherPanel() {
		if (otherDiskPanel.isImageLoaded() || otherDiskPanel.isZipFileLoaded()) {
			openDiskImage(otherDiskPanel.getDiskImageFileName(), true);
		} else  {
			loadLocalDirectory(otherDiskPanel.getCurrentImagePath());
		}
	}

	/**
	 * create a help drag-down menu (just for testing)
	 * @param propertyKey the propertyKey for the menu title
	 * @param mnemonic menu mnemonic
	 * @return the menu
	 */
	public JMenu createDiskImageMenu(String propertyKey, String mnemonic) {
		JMenu menu = new JMenu(Settings.getMessage(propertyKey));
		menu.setMnemonic(mnemonic.charAt(0));
		ActionListener listener = createDiskImageMenuActionListener();
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_NEW, 'n', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_LOAD, 'l', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_SHOWBAM, 'b', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_UNLOAD, 'u', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_COPYFILE, 'c', listener);
		menu.addSeparator();
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_RENAMEDISK, 'r', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_RENAMEFILE, 'f', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_DELETEFILE, 'd', listener);
		menu.addSeparator();
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWTEXT,  't', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWHEX,   'h', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWBASIC, 'a', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_VIEWIMAGE, 'g', listener);
		menu.addSeparator();
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_PRINTDIR, 'p', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_MD5, '5', listener);
		MainPanel.addMenuItem(menu, Resources.DROID64_MENU_DISK_MIRROR, 'm', listener);
		return menu;
	}

	private void printDir() {
		String title;
		List<String> lines = new ArrayList<>();
		if (imageLoaded) {
			title = diskImageFileName;
			String label = String.format("0 \"%-16s\" %-5s", diskImage.getBam().getDiskName(), diskImage.getBam().getDiskId());
			mainPanel.appendConsole("Print image "+ diskImageFileName);
			lines.add(label);
			for (int i=0; i < diskImage.getFilesUsedCount(); i++) {
				CbmFile file = diskImage.getCbmFile(i);
				if (file instanceof CpmFile) {
					lines.add(((CpmFile)file).asDirString());
				} else {
					lines.add(file.asDirString());
				}
			}
		} else if (zipFileLoaded) {
			title = currentImagePath;
			mainPanel.appendConsole("Print zip file " + currentImagePath);
			int i = 0;
			DirEntry entry;
			do {
				entry = tableModel.getDirEntry(i++);
				if (entry != null && !PARENT_DIR.equals(entry.getName())) {
					String row = String.format("%8d %-20s\t %s", entry.getBlocks(), entry.getName(), entry.getFlags());
					lines.add(row);
				}
			} while (entry != null);
		} else {
			title = directory;
			mainPanel.appendConsole("Print directory "+ directory);
			int i = 0;
			DirEntry entry;
			do {
				entry = tableModel.getDirEntry(i++);
				if (entry != null) {
					String row = String.format("%s\t %8d\t %s", entry.getFlags(), entry.getBlocks(), entry.getName());
					lines.add(row);
				}
			} while (entry != null);
		}
		try {
			PrinterJob job = PrinterJob.getPrinterJob();
			job.setPageable(new PrintPageable(lines.toArray(new String[lines.size()]), title, imageLoaded, true, mainPanel));
			if (job.printDialog()) {
				job.print();
			}
		} catch (Exception e) { // NOSONAR
			mainPanel.appendConsole(e.getMessage());
		}
	}

	/**
	 * @return number of rows
	 */
	public int getRowHeight() {
		return rowHeight;
	}

	/**
	 * @param i height of rows
	 */
	public void setRowHeight(int i) {
		rowHeight = i;
		table.setRowHeight(rowHeight);
		table.revalidate();
	}

	public void setDirectory(String dir) {
		this.directory = dir;
	}

	public boolean isImageLoaded() {
		return imageLoaded;
	}

	public String getDiskImageFileName() {
		return diskImageFileName;
	}

	public boolean isWritableImageLoaded() {
		if (imageLoaded && !zipFileLoaded && !diskImage.isCpmImage()) {
			switch (diskImage.getImageFormat()) {
			case DiskImage.D64_IMAGE_TYPE:
			case DiskImage.D67_IMAGE_TYPE:
			case DiskImage.D71_IMAGE_TYPE:
			case DiskImage.D80_IMAGE_TYPE:
			case DiskImage.D81_IMAGE_TYPE:
			case DiskImage.D82_IMAGE_TYPE:
			case DiskImage.T64_IMAGE_TYPE:
				return true;
			default:
				return false;
			}
		} else {
			return false;
		}
	}

	public boolean isZipFileLoaded() {
		return zipFileLoaded;
	}

	public String getDirectory() {
		return directory;
	}

	public void moveFile(final boolean upwards) {
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: files can't be moved in zip file.");
			return;
		} else if (!imageLoaded) {
			mainPanel.appendConsole(Settings.getMessage(Resources.DROID64_ERROR_NOIMAGEMOUNTED));
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
					for (int j=i + 1; j < diskImage.getFilesUsedCount(); j++) {
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
		if (zipFileLoaded) {
			mainPanel.appendConsole("Error: files can't be sorted in zip file.");
			return;
		} else if (!imageLoaded) {
			mainPanel.appendConsole(Settings.getMessage(Resources.DROID64_ERROR_NOIMAGEMOUNTED));
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
			return new ArrayList<>();
		}
		diskImage.validate(repairList);
		diskImage.writeImage(diskImageFileName);
		reloadDiskImage(true);
		return diskImage.getValidationErrorList();
	}

	/**
	 * Comparator class for comparing File entries with directories first
	 */
	class FileComparator implements Comparator<File> {
		@Override
		public int compare(File a, File b) {
			if (a.isDirectory() && !b.isDirectory()) {
				return -1;
			} else if (!a.isDirectory() && b.isDirectory()) {
				return 1;
			}
			return a.getName().compareTo(b.getName());
		}
	}

}
