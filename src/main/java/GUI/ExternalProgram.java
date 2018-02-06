/*
 * Created on 27.07.2004
 *
 *   NAME - WHAT IT DOES
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
 */
package GUI;

/**
 * @author wolf
 *
 * To change the template for this generated type comment go to
 * Window>Preferences>Java>Code Generation>Code and Comments
 */
public class ExternalProgram {

	private String command;
	private String description;
	private String label;
	
	public ExternalProgram() {
	}

	/**
	 * @param command_
	 * @param description_
	 * @param label_
	 */
	public void setValues(String command_, String description_, String label_) {
		command = command_;
		description = description_;
		label = label_;
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
