package droid64.gui;

import java.util.ArrayList;
import java.util.List;

import javax.swing.JFileChooser;
import javax.swing.JFrame;

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

	/**
	 * @param command
	 * @param arguments
	 * @param description
	 * @param label
	 */
	public ExternalProgram(String command, String arguments, String description, String label) {
		this.command = command;
		this.arguments = arguments;
		this.description = description;
		this.label = label;
	}

	/**
	 * Get array of strings to execute as external command
	 * @param image path the disk image, or null if no image.
	 * @param sourceFiles list of files
	 * @param target
	 * @param newImageChooser
	 * @return array of strings, with command first. Return null if command is null or empty string.
	 */
	public String[] getExecute(String sourceImage, List<String> sourceFiles, String target, JFileChooser newImageChooser) {
		if (command == null || command.isEmpty()) {
			return null;
		}
		List<String> files = new ArrayList<String>();
		List<String> imagefiles = new ArrayList<String>();
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
		List<String> args = new ArrayList<String>();
		args.add(command);
		for (String s : arguments.split("\\s+")) {
			switch (s.trim()) {
			case "{image}":
				if (sourceImage != null && !sourceImage.isEmpty()) {
					args.add(sourceImage);
				}
				break;
			case "{files}":
				if (!files.isEmpty()) {
					args.addAll(files);
				}
				break;
			case "{imagefiles}":
				if (!imagefiles.isEmpty()) {
					args.addAll(imagefiles);
				}
				break;
			case "{target}":
				if (target != null && !target.isEmpty()) {
					args.add(target);
				}
				break;
			case "{newfile}":
				if (newImageChooser!= null) {
					if (newImageChooser.showOpenDialog(new JFrame()) == JFileChooser.APPROVE_OPTION) {
						String name = "" +newImageChooser.getSelectedFile();
						args.add(name);
					} else {
						return null;
					}
				}
				break;
			default:
				args.add(s);
			}
		}
		return args.toArray(new String[args.size()]);
	}

	/** {@inheritDoc} */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExternalProgram[");
		builder.append(" .command=").append(command);
		builder.append(" .arguments=").append(arguments);
		builder.append(" .description=").append(description);
		builder.append(" .label=").append(label);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @param command
	 * @param arguments
	 * @param description
	 * @param label
	 */
	public void setValues(String command, String arguments, String description, String label) {
		this.command = command;
		this.arguments = arguments;
		this.description = description;
		this.label = label;
	}

	/**
	 * @return
	 */
	public String getArguments() {
		return arguments;
	}

	/**
	 * @return
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @return
	 */
	public String getDescription() {
		return description;
	}

	/**
	 * @return
	 */
	public String getLabel() {
		return label;
	}

	/**
	 * @param string
	 */
	public void setArguments(String string) {
		arguments = string;
	}

	/**
	 * @param string
	 */
	public void setCommand(String string) {
		command = string;
	}

	/**
	 * @param string
	 */
	public void setDescription(String string) {
		description = string;
	}

	/**
	 * @param string
	 */
	public void setLabel(String string) {
		label = string;
	}

}
