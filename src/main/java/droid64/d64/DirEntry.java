package droid64.d64;
/**<pre style='font-family:sans-serif;'>
 * Created on 05.07.2004
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
 *</pre>
 * @author wolf
 */
public class DirEntry {
//	{ "Nr", "Bl", "Name", "Ty", "Fl", "Tr", "Se" };

	private int number;
	private int blocks;
	private String name;
	private String type;
	private String flags;
	private int track;
	private int sector;

	public DirEntry() {
	}

	public DirEntry(CbmFile cbmFile, int fileNum) {
		number = fileNum;
		if (cbmFile instanceof CpmFile) {
			CpmFile cpm = (CpmFile) cbmFile;
			blocks = cpm.getRecordCount();
			name = cpm.getCpmName();
			type = cpm.getCpmNameExt();			
			flags =  (cpm.isReadOnly() ? "R" : "-") + (cpm.isHidden() ? "H" : "-") + (cpm.isArchived() ? "A" : "-");
			track = cbmFile.getTrack();
			sector = cbmFile.getSector();
		} else {
			blocks = cbmFile.getSizeInBlocks();
			type = DiskImage.getFileType(cbmFile.getFileType());
			name= " \"" + cbmFile.getName() + "\"";

			flags = (cbmFile.isFileLocked() ? "<" : "") + (cbmFile.isFileClosed() ? "" : "*");
			track = cbmFile.getTrack();
			sector = cbmFile.getSector();
		}		
	}
	
	/** {@inheritDoc} */
	public String toString() {
		return String.format("%3d %3d \"%16s\" %3s%3s %2d %2d", number, blocks, name!=null ? name : "", type!=null?type:"", flags!=null?flags:"", track, sector);
	}
	
	/**
	 * @return
	 */
	public int getBlocks() {
		return blocks;
	}

	/**
	 * @return
	 */
	public String getFlags() {
		return flags;
	}

	/**
	 * @return
	 */
	public String getName() {
		return name;
	}

/**
 * @return
 */
public int getNumber() {
	return number;
}

	/**
	 * @return
	 */
	public int getSector() {
		return sector;
	}

	/**
	 * @return
	 */
	public int getTrack() {
		return track;
	}

	/**
	 * @return
	 */
	public String getType() {
		return type;
	}

	/**
	 * @param i
	 */
	public void setBlocks(int i) {
		blocks = i;
	}

	/**
	 * @param string
	 */
	public void setFlags(String string) {
		flags = string;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

/**
 * @param i
 */
public void setNumber(int i) {
	number = i;
}

	/**
	 * @param i
	 */
	public void setSector(int i) {
		sector = i;
	}

	/**
	 * @param i
	 */
	public void setTrack(int i) {
		track = i;
	}

	/**
	 * @param string
	 */
	public void setType(String string) {
		type = string;
	}

}
