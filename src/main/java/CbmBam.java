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
public class CbmBam {

	private int disk_DOS_type;
	private int[] freeSectors = new int[40]; // array[1..40] of int
	private int[][] track_bits = new int[40][3]; // array[1..40, 1..3] of int
	private String disk_name;	//string[16]
	private String disk_ID;	//string[5]

	public CbmBam() {
	}
	
	public CbmBam(int disk_DOS_type_, int[] freeSectors_, int[][] track_bits_, String disk_name_, String disk_ID_) {
		disk_DOS_type = disk_DOS_type_;
		freeSectors = freeSectors_;
		track_bits = track_bits_;
		disk_name = disk_name_;
		disk_ID = disk_ID_;
	}

	/**
	 * @return
	 */
	public int getDisk_DOS_type() {
		return disk_DOS_type;
	}

	/**
	 * @return
	 */
	public String getDisk_ID() {
		return disk_ID;
	}

	/**
	 * @return
	 */
	public String getDisk_name() {
		return disk_name;
	}

	/**
	 * @param track_number the track number (1..40)
	 * @return
	 */
	public int getFreeSectors(int track_number) {
		//substract 1, because we are counting Track from 1 to 40, arrays are 0 to 39
		return freeSectors[track_number-1];
	}

	/**
	 * @param track_number the track number (1..40)
	 * @param byte_number the byte_number (1..3)
	 * @return
	 */
	public int getTrack_bits(int track_number, int byte_number) {
		//substract 1, because we are counting Track from 1 to 40, arrays are 0 to 39
		return track_bits[track_number-1][byte_number-1];
	}

	/**
	 * @param b
	 */
	public void setDisk_DOS_type(int b) {
		disk_DOS_type = b;
	}

	/**
	 * @param string
	 */
	public void setDisk_ID(String string) {
		disk_ID = string;
	}

	/**
	 * @param string
	 */
	public void setDisk_name(String string) {
		disk_name = string;
	}

	/**
	 * @param where
	 * @param value
	 */
	public void setFreeSectors(int where, int value) {
		//substract 1, because we are counting Track from 1 to 40, arrays are 0 to 39
		freeSectors[where-1] = value;
	}

	/**
	 * @param which_track
	 * @param which_byte
	 * @param value
	 */
	public void setTrack_bits(int which_track, int which_byte, int value) {
		//substract 1, because we are counting Track from 1 to 40, arrays are 0 to 39
		track_bits[which_track-1][which_byte-1] = value;
	}

}
