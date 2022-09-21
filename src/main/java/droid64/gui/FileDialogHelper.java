package droid64.gui;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

import droid64.d64.Utility;

public class FileDialogHelper {

	private static JFileChooser textFileChooser = null;
	private static JFileChooser imageFileChooser = null;
	private static JFileChooser fontFileChooser = null;

	private FileDialogHelper() {
		super();
	}

	/**
	 * Select a file name for save/open a text file
	 * @param title dialog title
	 * @param directory default directory
	 * @param defaultName default name
	 * @param saveMode if true open save mode dialog
	 * @param extFilter strings of file extensions. the first entry will be added to defaultName if no other matches
	 * @return chosen file name
	 */
	public static String openTextFileDialog(String title, String directory, String defaultName, boolean saveMode, String[] extFilter) {
		if (textFileChooser == null) {
			textFileChooser = new JFileChooser(directory);
			FileFilter fileFilter = getFileFilter("Text", extFilter);
			textFileChooser.addChoosableFileFilter(fileFilter);
			textFileChooser.setFileFilter(fileFilter);
			textFileChooser.setMultiSelectionEnabled(false);
		} else if (directory != null) {
			textFileChooser.setCurrentDirectory(new File(directory));
		}
		textFileChooser.setSelectedFile(getDefaultFile(defaultName, extFilter));
		if (saveMode) {
			textFileChooser.setDialogTitle(title != null ? title : "Save text file ");
			if (textFileChooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
				return textFileChooser.getSelectedFile()+Utility.EMPTY;
			}
		} else {
			textFileChooser.setDialogTitle(title != null ? title : "Open text file");
			if (textFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
				return textFileChooser.getSelectedFile()+Utility.EMPTY;
			}
		}
		return null;
	}

	public static String openFontFileDialog(String title, String directory, String defaultName, String[] extFilter) {
		if (fontFileChooser == null) {
			fontFileChooser = new JFileChooser(directory);
			FileFilter fileFilter = getFileFilter("TrueType Font", extFilter);
			fontFileChooser.addChoosableFileFilter(fileFilter);
			fontFileChooser.setFileFilter(fileFilter);
			fontFileChooser.setMultiSelectionEnabled(false);
		} else if (directory != null) {
			fontFileChooser.setCurrentDirectory(new File(directory));
		}
		fontFileChooser.setSelectedFile(getDefaultFile(defaultName, extFilter));
		fontFileChooser.setDialogTitle(title != null ? title : "Open font file");
		if (fontFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
			return fontFileChooser.getSelectedFile()+Utility.EMPTY;
		}
		return null;
	}

	/**
	 * Select a file name for save/open a text file
	 * @param directory default directory
	 * @param defaultName default file name
	 * @param saveMode if true open save mode dialog
	 * @return chosen file name
	 */
	public static String openTextFileDialog(String directory, String defaultName, boolean saveMode) {
		return openTextFileDialog(null, directory, defaultName, saveMode, null);
	}

	/**
	 * Setup file dialog for disk images.
	 * @param directory the directory to open dialog in
	 * @param defaultName a name to be preselected, or null if none
	 * @param saveMode if the dialog should ask for saving new file or browse for existing files
	 * @return name of chosen image file
	 */
	public static String openImageFileDialog(String directory, String defaultName, boolean saveMode) {
		if (imageFileChooser == null) {
			imageFileChooser = new JFileChooser(directory);
			FileFilter fileFilter = getDiskImageFileFilter();
			imageFileChooser.addChoosableFileFilter(fileFilter);
			imageFileChooser.setFileFilter(fileFilter);
			imageFileChooser.setMultiSelectionEnabled(false);
		} else {
			if (directory != null) {
				imageFileChooser.setCurrentDirectory(new File(directory));
			}
		}
		if (defaultName != null) {
			imageFileChooser.setSelectedFile(new File(defaultName));
		}
		if (saveMode) {
			imageFileChooser.setDialogTitle("Save disk image");
			imageFileChooser.setDialogType(JFileChooser.FILES_ONLY | JFileChooser.SAVE_DIALOG);
			if (imageFileChooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
				return imageFileChooser.getSelectedFile()+Utility.EMPTY;
			}
		} else {
			imageFileChooser.setDialogTitle("Load disk image");
			imageFileChooser.setDialogType(JFileChooser.FILES_ONLY | JFileChooser.OPEN_DIALOG);
			if (imageFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
				return imageFileChooser.getSelectedFile()+Utility.EMPTY;
			}
		}
		return null;
	}

	protected static FileFilter getFileFilter(final String extName, final String[] extFilter) {
		return new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory() || extFilter == null || extFilter.length == 0) {
					return true;
				} else {
					String lname = f.getName().toLowerCase();
					for (String ext : extFilter) {
						if (lname.endsWith(ext)) {
							return true;
						}
					}
				}
				return false;
			}
			@Override
			public String getDescription () { return extName; }
		};
	}

	protected static FileFilter getDiskImageFileFilter() {
		return new FileFilter() {
			@Override
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				} else {
					Map<Integer,List<String>> map = Settings.getFileExtensionMap();
					for (Entry<Integer,List<String>> entry : map.entrySet()) {
						for (String ext : entry.getValue()) {
							if (f.getName().toLowerCase().endsWith(ext.toLowerCase())) {
								return true;
							}
						}
					}
					return false;
				}
			}
			@Override
			public String getDescription () { return "Disk images"; }
		};
	}

	protected static File getDefaultFile(String defaultName, String[] extFilter) {
		if (defaultName != null && !defaultName.isEmpty()) {
			if (extFilter == null || extFilter.length == 0) {
				return new File(defaultName);
			} else {
				String lname = defaultName.toLowerCase();
				for (String ext : extFilter) {
					if (lname.endsWith(ext)) {
						return new File(defaultName);
					}
				}
				return new File(defaultName + extFilter[0]);
			}
		} else {
			return null;
		}
	}

}
