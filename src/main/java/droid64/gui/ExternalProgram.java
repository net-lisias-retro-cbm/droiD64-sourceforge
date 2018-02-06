package droid64.gui;

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
	private String description;
	private String label;

	/**
	 * @param command_
	 * @param description_
	 * @param label_
	 */
	public ExternalProgram(String command, String description, String label) {
		this.command = command;
		this.description = description;
		this.label = label;
	}
	
	
	/** {@inheritDoc} */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("ExternalProgram[");
		builder.append(" .command=").append(command);
		builder.append(" .description=").append(description);
		builder.append(" .label=").append(label);
		builder.append("]");
		return builder.toString();
	}
		
	/**
	 * @param command_
	 * @param description_
	 * @param label_
	 */
	public void setValues(String command, String description, String label) {
		this.command = command;
		this.description = description;
		this.label = label;
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
