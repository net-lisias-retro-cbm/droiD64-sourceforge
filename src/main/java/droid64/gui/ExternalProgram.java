package droid64.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import droid64.d64.DiskImage;

/**<pre style='font-family:Sans,Arial,Helvetica'>
 * Created on 27.07.2004
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
 *   eMail: the@BigBadWolF.de
 *   </pre>
 * @author wolf
 */
public class ExternalProgram {

	private String command;
	private String arguments;
	private String description;
	private String label;
	private boolean forkThread;

	/**
	 * @param command command to run
	 * @param arguments arguments
	 * @param description description
	 * @param label label
	 * @param forkThread spawn a new thread when running the program
	 */
	public ExternalProgram(String command, String arguments, String description, String label, boolean forkThread) {
		this.command = command;
		this.arguments = arguments;
		this.description = description;
		this.label = label;
		this.forkThread = forkThread;
	}

	/**
	 * Get array of strings to execute as external command
	 * @param sourceImage path the disk image, or null if no image.
	 * @param sourceFiles list of files
	 * @param target folder
	 * @param directory current folder
	 * @param imageType type of disk image
	 * @return array of strings, with command first. Return empty array if command is null or empty string.
	 */
	public String[] getExecute(String sourceImage, List<String> sourceFiles, String target, String directory, int imageType) {
		if (command == null || command.isEmpty()) {
			return new String[0];
		}
		List<String> files = new ArrayList<>();
		List<String> imagefiles = new ArrayList<>();
		if (sourceFiles != null && !sourceFiles.isEmpty()) {
			for (String file : sourceFiles) {
				if (file!= null && !file.isEmpty()) {
					files.add(file);
					if (sourceImage != null && !sourceImage.isEmpty()) {
						imagefiles.add(sourceImage + ":" + file);
					}
				}
			}
		}
		return buildArguments(sourceImage, target, imagefiles, files, directory, imageType);
	}

	private String[] buildArguments(String sourceImage, String target, List<String> imagefiles, List<String> files, String directory, int imageType) {
		List<String> args = new ArrayList<>();
		args.add(command);
		for (String s : arguments.split("\\s+")) {
			switch (s) {
			case "{Image}":
				if (sourceImage != null && !sourceImage.isEmpty()) {
					args.add(sourceImage);
				}
				break;
			case "{Files}":
				if (!files.isEmpty()) {
					args.addAll(files);
				}
				break;
			case "{ImageFiles}":
				if (!imagefiles.isEmpty()) {
					args.addAll(imagefiles);
				}
				break;
			case "{Target}":
				if (target != null && !target.isEmpty()) {
					args.add(target);
				}
				break;
			case "{NewFile}":
				String name = FileDialogHelper.openImageFileDialog(directory, null, true);
				if (name == null) {
					return new String[0];
				}
				args.add(name);
				break;
			case "{ImageType}":
				args.add(DiskImage.getImageTypeName(imageType));
				break;
			case "{DriveType}":
				args.add(driveTypeFromImageType(imageType));
				break;
			default:
				args.add(s);
			}
		}
		return args.toArray(new String[args.size()]);
	}

	private String driveTypeFromImageType(int imageType) {
		switch (imageType) {
		case DiskImage.D67_IMAGE_TYPE:
			return "2040";
		case DiskImage.D71_IMAGE_TYPE:
		case DiskImage.D71_CPM_IMAGE_TYPE:
			return "1571";
		case DiskImage.D80_IMAGE_TYPE:
			return "8050";
		case DiskImage.D81_IMAGE_TYPE:
		case DiskImage.D81_CPM_IMAGE_TYPE:
			return "1581";
		case DiskImage.D82_IMAGE_TYPE:
			return "8250";
		case DiskImage.D64_IMAGE_TYPE:
		case DiskImage.D64_CPM_C64_IMAGE_TYPE:
		case DiskImage.D64_CPM_C128_IMAGE_TYPE:
		default:
			return "1541";
		}
	}

	/**
	 * Run the external program.
	 * @param imageFile the image file
	 * @param execArgs arguments as parsed by {@link #getExecute(String, List, String, String, int)}
	 * @param mainPanel used to log output from command
	 */
	public void runProgram(final File imageFile, final String[] execArgs, final MainPanel mainPanel) {
		if (execArgs == null || execArgs.length == 0) {
			mainPanel.appendConsole("No command to execute!");
		}
		try {
			mainPanel.appendConsole("Executing: " + Arrays.toString(execArgs));
			if (forkThread) {
				Thread runner = new Thread() {
					@Override
					public void run() {
						runProgramThread(imageFile, execArgs, mainPanel);
					}
				};
				runner.start();
			} else {
				runProgramThread(imageFile, execArgs, mainPanel);
				mainPanel.getLeftDiskPanel().reloadDiskImage(true);
				mainPanel.getRightDiskPanel().reloadDiskImage(true);
			}
		} catch (Exception e) {	//NOSONAR
			mainPanel.appendConsole("\n"+e.getMessage());
		}
	}

	private void runProgramThread(File imgParentFile, String[] execArgs, MainPanel mainPanel) {
		try {
			ProcessBuilder procBuilder = new ProcessBuilder(execArgs);
			if (imgParentFile != null) {
				procBuilder.directory(imgParentFile);
			}
			procBuilder.redirectErrorStream(true);
			Process process = procBuilder.start();
			InputStreamReader isr = new InputStreamReader(process.getInputStream());
			BufferedReader br = new BufferedReader(isr);
			String line;
			while ((line = br.readLine()) != null) {
				mainPanel.appendConsole(line);
			}
			process.waitFor();
		} catch (Exception e) {	//NOSONAR
			mainPanel.appendConsole("\n"+e.getMessage());
		}
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExternalProgram[");
		builder.append(" .command=").append(command);
		builder.append(" .arguments=").append(arguments);
		builder.append(" .description=").append(description);
		builder.append(" .label=").append(label);
		builder.append(" .forkThread=").append(forkThread);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @param command program to run
	 * @param arguments arguments to program
	 * @param description description
	 * @param label label
	 * @param forkThread forkThread
	 */
	public void setValues(String command, String arguments, String description, String label, boolean forkThread) {
		this.command = command;
		this.arguments = arguments;
		this.description = description;
		this.label = label;
		this.forkThread = forkThread;
	}

	/**
	 * @return arguments
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * @return command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return description
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return label
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param string arguments
	 */
	public void setArguments(String string) {
		arguments = string;
	}

	/**
	 * @param string command
	 */
	public void setCommand(String string) {
		command = string;
	}

	/**
	 * @param string description
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @param string label
	 */
	public void setLabel(String string) {
		label = string;
	}

	public boolean isForkThread() {
		return forkThread;
	}

	public void setForkThread(boolean forkThread) {
		this.forkThread = forkThread;
	}

}
