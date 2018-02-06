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
public class CbmFile {

	private boolean file_scratched;
	private int file_type;
	private boolean file_locked;
	private boolean file_closed;
	private int track;
	private int sector;
	private String name;	//string[16]
	private int rel_track;
	private int rel_sector;
	private int[] geos = new int[6];	// array[0..5] of int
	private int sizeInBytes;
	private int sizeInBlocks;
	private int dirTrack;	// next directory track
	private int dirSector;	// next directory sector
	private int dirPosition;	// position in directory
	
	public CbmFile() {
		file_scratched = true;
		file_type = 0;
		file_locked = false;
		file_closed = false;
		track = 0;
		sector = 0;
		name = "";
		rel_track = 0;
		rel_sector = 0;
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
			boolean file_scratched, int file_type, boolean file_locked, boolean file_closed, int track, int sector,
			String name, int rel_track, int rel_sector,	int[] geos,	int sizeInBytes, int sizeInBlocks, int dirTrack,
			int dirSector, int dirPosition)
	{
		this.file_scratched = file_scratched;
		this.file_type = file_type;
		this.file_locked = file_locked;
		this.file_closed = file_closed;
		this.track = track;
		this.sector = sector;
		this.name = name;
		this.rel_track = rel_track;
		this.rel_sector = rel_sector;
		this.geos = geos;
		this.sizeInBytes = sizeInBytes;
		this.sizeInBlocks = sizeInBlocks;
		this.dirTrack = dirTrack;
		this.dirSector = dirSector;
		this.dirPosition = dirPosition;
	}
	
	/** {@inheritDoc} */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CbmFile [");
		builder.append(" file_scratched=").append(file_scratched);
		builder.append(" file_type=").append(file_type);
		builder.append(" file_locked=").append(file_locked);
		builder.append(" file_closed=").append(file_closed);
		builder.append(" track=").append(track);
		builder.append(" sector=").append(sector);
		builder.append(" name=").append(name);
		builder.append(" rel_track=").append(rel_track);
		builder.append(" rel_sector=").append(rel_sector);
		builder.append(" geos=").append(Arrays.toString(geos));
		builder.append(" sizeInBytes=").append(sizeInBytes);
		builder.append(" sizeInBlocks=").append(sizeInBlocks);
		builder.append(" dirTrack=").append(dirTrack);
		builder.append(" dirSector=").append(dirSector);
		builder.append(" dirPosition=").append(dirPosition);
		builder.append("]");
		return builder.toString();
	}

	/**
	 * @return
	 */
	public boolean isFile_closed() {
		return file_closed;
	}

	/**
	 * @return
	 */
	public boolean isFile_locked() {
		return file_locked;
	}

	/**
	 * @return
	 */
	public boolean isFile_scratched() {
		return file_scratched;
	}

	/**
	 * @return
	 */
	public int getFile_type() {
		return file_type;
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
	public int getRel_sector() {
		return rel_sector;
	}

	/**
	 * @return
	 */
	public int getRel_track() {
		return rel_track;
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
	public void setFile_closed(boolean b) {
		file_closed = b;
	}

	/**
	 * @param b
	 */
	public void setFile_locked(boolean b) {
		file_locked = b;
	}

	/**
	 * @param b
	 */
	public void setFile_scratched(boolean b) {
		file_scratched = b;
	}

	/**
	 * @param b
	 */
	public void setFile_type(int b) {
		file_type = b;
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
	public void setRel_sector(int b) {
		rel_sector = b;
	}

	/**
	 * @param b
	 */
	public void setRel_track(int b) {
		rel_track = b;
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

}
