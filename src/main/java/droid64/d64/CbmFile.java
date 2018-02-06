package droid64.d64;

import java.util.Arrays;
/**<pre style='font-family:sans-serif;'>
 * Created on 21.06.2004
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
public class CbmFile implements Comparable<CbmFile>,Cloneable {

	private boolean fileScratched;
	private int fileType;
	private boolean fileLocked;
	private boolean fileClosed;
	private int track;
	private int sector;
	private String name;		//string[16]
	private int relTrack;
	private int relSector;
	/** FileStructure, FileType, Year, Month, Day. Hour, Minute */
	private int[] geos = new int[7];
	private int sizeInBytes;
	private int sizeInBlocks;
	private int dirTrack;		// next directory track
	private int dirSector;		// next directory sector
	private int dirPosition;	// position in directory
	private int offSet;
		
	public static final int GEOS_NORMAL    = 0x00;
	public static final int GEOS_BASIC     = 0x01;
	public static final int GEOS_ASM       = 0x02;
	public static final int GEOS_DATA      = 0x03;
	public static final int GEOS_SYS       = 0x04;
	public static final int GEOS_DESK_ACC  = 0x05;
	public static final int GEOS_APPL      = 0x06;
	public static final int GEOS_APPL_DATA = 0x07;
	public static final int GEOS_FONT      = 0x08;
	public static final int GEOS_PRT_DRV   = 0x09;
	public static final int GEOS_INPUT_DRV = 0x0a;
	public static final int GEOS_DISK_DRV  = 0x0b;
	public static final int GEOS_SYS_BOOT  = 0x0c;
	public static final int GEOS_TEMP      = 0x0d;
	public static final int GEOS_AUTOEXEC  = 0x0e;
	public static final int GEOS_UNDFINED  = 0xff;
	
	public CbmFile() {
		fileScratched = true;
		fileType = 0;
		fileLocked = false;
		fileClosed = false;
		track = 0;
		sector = 0;
		name = "";
		relTrack = 0;
		relSector = 0;
		geos[0] = 0;
		geos[1] = 0;
		geos[2] = 0;
		geos[3] = 0;
		geos[4] = 0;
		geos[5] = 0;
		sizeInBytes = 0;
		sizeInBlocks = 0;
		dirTrack = 0;
		dirSector = 0;
		dirPosition = 0;
	}

	public CbmFile(
			boolean fileScratched, int fileType, boolean fileLocked, boolean fileClosed, int track, int sector,
			String name, int relTrack, int relSector, int[] geos, int sizeInBytes, int sizeInBlocks, int dirTrack,
			int dirSector, int dirPosition)
	{
		this.fileScratched = fileScratched;
		this.fileType = fileType;
		this.fileLocked = fileLocked;
		this.fileClosed = fileClosed;
		this.track = track;
		this.sector = sector;
		this.name = name;
		this.relTrack = relTrack;
		this.relSector = relSector;
		this.geos = geos;
		this.sizeInBytes = sizeInBytes;
		this.sizeInBlocks = sizeInBlocks;
		this.dirTrack = dirTrack;
		this.dirSector = dirSector;
		this.dirPosition = dirPosition;
	}
	
	public CbmFile(CbmFile that) {
		this.fileScratched = that.fileScratched;
		this.fileType = that.fileType;
		this.fileLocked = that.fileLocked;
		this.fileClosed = that.fileClosed;
		this.track = that.track;
		this.sector = that.sector;
		this.name = that.name;
		this.relTrack = that.relTrack;
		this.relSector = that.relSector;
		this.geos[0] = that.geos[0];
		this.geos[1] = that.geos[1];
		this.geos[2] = that.geos[2];
		this.geos[3] = that.geos[3];
		this.geos[4] = that.geos[4];
		this.geos[5] = that.geos[5];
		this.sizeInBytes = that.sizeInBytes;
		this.sizeInBlocks = that.sizeInBlocks;
		this.dirTrack = that.dirTrack;
		this.dirSector = that.dirSector;
		this.dirPosition = that.dirPosition;
	}
	
	public CbmFile(String name, int fileType, int dirPosition, int track, int sector, int size) {
		this.name = name;
		this.fileType = fileType;
		this.dirPosition = dirPosition;
		this.track = track;
		this.sector = sector;
		this.sizeInBytes = size;
		fileScratched = false;
		fileLocked = false;
		fileClosed = false;
	}

	@Override
	public CbmFile clone() {
		return new CbmFile(this);
	}
	
	/**
	 * Construct entry from position in disk image.
	 * @param data byte[]
	 * @param position int
	 */
	public CbmFile(byte[] data, int position) {
		dirTrack = data[position + 0x00] & 0xff;
		dirSector = data[position + 0x01] & 0xff;
		fileScratched = (data[position + 0x02] & 0xff) == 0 ? true : false;
		fileType = data[position + 0x02] & 0x07;
		fileLocked = (data[position + 0x02] & 0x40) == 0 ? false : true;
		fileClosed = (data[position + 0x02] & 0x80) == 0 ? false : true;		
		track = data[position + 0x03] & 0xff;
		sector = data[position + 0x04] & 0xff;
		StringBuffer buf = new StringBuffer();
		for (int i = 0; i < 16; i++){
			int c = data[position + 0x05 + i] & 0xff;
			if (c != (DiskImage.BLANK & 0xff)) {
				buf.append((char)(DiskImage.PETSCII_TABLE[c]));
			}
		}
		name = buf.toString();
		relTrack = data[position + 0x15] & 0xff;
		relSector = data[position + 0x16] & 0xff;
		for (int i = 0; i < geos.length; i++) {
			geos[i] = data[position + 0x17 +i] & 0xff;
		}
		sizeInBlocks = (data[position + 0x1e] & 0xff) |	((data[position + 0x1f] & 0xff) * 256) ;
	}
	
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CbmFile [");
		toString(builder);
		builder.append("]");
		return builder.toString();
	}
	
	public String asDirString() {
		return String.format("%-5s%-18s %s%-3s%s", sizeInBlocks, "\""+name+"\"",
				fileClosed ? " " : "*", 
				(fileType < DiskImage.FILE_TYPES.length ? DiskImage.FILE_TYPES[fileType] : "???" ),
				fileLocked ? "<" : " " );
	}
	
	/** {@inheritDoc} */
	protected void toString(StringBuilder builder) {
		builder.append(" fileScratched=").append(fileScratched);
		builder.append(" fileType=").append(fileType);
		builder.append(" fileLocked=").append(fileLocked);
		builder.append(" fileClosed=").append(fileClosed);
		builder.append(" track=").append(track);
		builder.append(" sector=").append(sector);
		builder.append(" name=").append(name);
		builder.append(" relTrack=").append(relTrack);
		builder.append(" relSector=").append(relSector);
		builder.append(" geos=").append(Arrays.toString(geos));
		builder.append(" sizeInBytes=").append(sizeInBytes);
		builder.append(" sizeInBlocks=").append(sizeInBlocks);
		builder.append(" dirTrack=").append(dirTrack);
		builder.append(" dirSector=").append(dirSector);
		builder.append(" dirPosition=").append(dirPosition);
	}

	/**
	 * @return
	 */
	public boolean isFileClosed() {
		return fileClosed;
	}

	/**
	 * @return
	 */
	public boolean isFileLocked() {
		return fileLocked;
	}

	/**
	 * @return
	 */
	public boolean isFileScratched() {
		return fileScratched;
	}

	/**
	 * @return
	 */
	public int getFileType() {
		return fileType;
	}

	/**
	 * @return
	 */
	public int getGeos(int whichone) {
		return geos[whichone];
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
	public int getRelSector() {
		return relSector;
	}

	/**
	 * @return
	 */
	public int getRelTrack() {
		return relTrack;
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
	public int getSizeInBytes() {
		return sizeInBytes;
	}

	/**
	 * @return
	 */
	public int getTrack() {
		return track;
	}

	/**
	 * @param b
	 */
	public void setFileClosed(boolean b) {
		fileClosed = b;
	}

	/**
	 * @param b
	 */
	public void setFileLocked(boolean b) {
		fileLocked = b;
	}

	/**
	 * @param b
	 */
	public void setFileScratched(boolean b) {
		fileScratched = b;
	}

	/**
	 * @param b
	 */
	public void setFileType(int b) {
		fileType = b;
	}

	/**
	 * @param bs
	 */
	public void setGeos(int where, int bs) {
		geos[where] = bs;
	}

	/**
	 * @param string
	 */
	public void setName(String string) {
		name = string;
	}

	/**
	 * @param b
	 */
	public void setRelSector(int b) {
		relSector = b;
	}

	/**
	 * @param b
	 */
	public void setRelTrack(int b) {
		relTrack = b;
	}

	/**
	 * @param b
	 */
	public void setSector(int b) {
		sector = b;
	}

	/**
	 * @param i
	 */
	public void setSizeInBytes(int i) {
		sizeInBytes = i;
	}

	/**
	 * @param b
	 */
	public void setTrack(int b) {
		track = b;
	}

	/**
	 * @return
	 */
	public int getDirSector() {
		return dirSector;
	}

	/**
	 * @return
	 */
	public int getDirTrack() {
		return dirTrack;
	}

	/**
	 * @return
	 */
	public int getSizeInBlocks() {
		return sizeInBlocks;
	}

	/**
	 * @param i
	 */
	public void setDirSector(int i) {
		dirSector = i;
	}

	/**
	 * @param i
	 */
	public void setDirTrack(int i) {
		dirTrack = i;
	}

	/**
	 * @param i
	 */
	public void setSizeInBlocks(int i) {
		sizeInBlocks = i;
	}

	/**
	 * @return
	 */
	public int getDirPosition() {
		return dirPosition;
	}

	/**
	 * @param i
	 */
	public void setDirPosition(int i) {
		dirPosition = i;
	}

	public int getOffSet() {
		return offSet;
	}

	public void setOffSet(int offSet) {
		this.offSet = offSet;
	}

	@Override
	public int compareTo(CbmFile that) {
		if (this.name == null && that.name == null) {
			return 0;
		} else if (this.name == null || that.name == null) {
			return this.name == null ? -1 : 1;
		} else {
			return name.compareTo(that.name);
		}
	}
	

}
