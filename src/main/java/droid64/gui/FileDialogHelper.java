package droid64.gui;

import java.io.File;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.filechooser.FileFilter;

public class FileDialogHelper {

	private static JFileChooser textFileChooser = null;
	private static JFileChooser imageFileChooser = null;

	private FileDialogHelper() {
		// not used
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
			FileFilter fileFilter = getTextFileFilter(extFilter);
			textFileChooser.addChoosableFileFilter(fileFilter);
			textFileChooser.setFileFilter(fileFilter);
			textFileChooser.setMultiSelectionEnabled(false);
		} else if (directory != null) {
			textFileChooser.setCurrentDirectory(new File(directory));
		}
		textFileChooser.setSelectedFile(getDefaultFile(defaultName, extFilter));
		if (saveMode) {
			textFileChooser.setDialogTitle(title != null ? title : "Save text file ");
			if (textFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
				return textFileChooser.getSelectedFile()+"";
			}
		} else {
			textFileChooser.setDialogTitle(title != null ? title : "Open text file");
			if (textFileChooser.showSaveDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
				return textFileChooser.getSelectedFile()+"";
			}
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
				return imageFileChooser.getSelectedFile()+"";
			}
		} else {
			imageFileChooser.setDialogTitle("Load disk image");
			imageFileChooser.setDialogType(JFileChooser.FILES_ONLY | JFileChooser.OPEN_DIALOG);
			if (imageFileChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
				return imageFileChooser.getSelectedFile()+"";
			}
		}
		return null;
	}

	private static FileFilter getTextFileFilter(final String[] extFilter) {
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
			public String getDescription () { return "Text"; }
		};
	}

	private static FileFilter getDiskImageFileFilter() {
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

	private static File getDefaultFile(String defaultName, String[] extFilter) {
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
