package D64;
/*
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
 */

/**
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
	
	public CbmFile(){
		file_scratched = true;
		file_type = 0;
		file_locked = false;
		file_closed = false;
		track = 0;
		sector = 0;
		String name = "";
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
		boolean file_scratched_,
		int file_type_,
		boolean file_locked_,
		boolean file_closed_,
		int track_,
		int sector_,
		String name_,
		int rel_track_,
		int rel_sector_,
		int[] geos_,
		int sizeInBytes_,
		int sizeInBlocks_,
		int dirTrack_,
		int dirSector_,
		int dirPosition_
	){
		file_scratched = file_scratched_;
		file_type = file_type_;
		file_locked = file_locked_;
		file_closed = file_closed_;
		track = track_;
		sector = sector_;
		String name = name_;
		rel_track = rel_track_;
		rel_sector = rel_sector_;
		geos = geos_;
		sizeInBytes = sizeInBytes_;
		sizeInBlocks = sizeInBlocks_;
		dirTrack = dirTrack_;
		dirSector = dirSector_;
		dirPosition = dirPosition_;
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
