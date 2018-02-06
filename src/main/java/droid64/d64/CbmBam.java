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
public class CbmBam {

	private int diskDosType;
	private int[] freeSectors = new int[40]; // array[1..40] of int
	private int[][] trackBits = new int[40][3]; // array[1..40, 1..3] of int
	private String diskName;	//string[16]
	private String diskId;	//string[5]

	public CbmBam() {
	}
	
	public CbmBam(int diskDosType, int[] freeSectors, int[][] trackBits, String diskName, String diskId) {
		this.diskDosType = diskDosType;
		this.freeSectors = freeSectors;
		this.trackBits = trackBits;
		this.diskName = diskName;
		this.diskId = diskId;
	}

	/**
	 * @return
	 */
	public int getDiskDosType() {
		return diskDosType;
	}

	/**
	 * @return
	 */
	public String getDiskId() {
		return diskId;
	}

	/**
	 * @return
	 */
	public String getDiskName() {
		return diskName;
	}

	/**
	 * @param track_number the track number (1..40)
	 * @return int
	 */
	public int getFreeSectors(int track_number) {
		//subtract 1, because we are counting Track from 1 to 40, arrays are 0 to 39
		return freeSectors[track_number-1];
	}

	/**
	 * @param trackNumber the track number (1..40)
	 * @param byteNumber the byte number (1..3)
	 * @return int
	 */
	public int getTrackBits(int trackNumber, int byteNumber) {
		//subtract 1, because we are counting Track from 1 to 40, arrays are 0 to 39
		return trackBits[trackNumber-1][byteNumber-1];
	}

	/**
	 * @param b
	 */
	public void setDiskDosType(int b) {
		diskDosType = b;
	}

	/**
	 * @param string
	 */
	public void setDiskId(String string) {
		diskId = string;
	}

	/**
	 * @param string
	 */
	public void setDiskName(String string) {
		diskName = string;
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
	public void setTrack_bits(int whichTrack, int whichByte, int value) {
		//substract 1, because we are counting Track from 1 to 40, arrays are 0 to 39
		trackBits[whichTrack-1][whichByte-1] = value;
	}

	/** {@inheritDoc} */
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("CbmBam [");
		builder.append(" diskDosType=").append(diskDosType);
		builder.append(" freeSectors=").append(Arrays.toString(freeSectors));
		builder.append(" trackBits=").append(Arrays.toString(trackBits));
		builder.append(" diskName=").append(diskName);
		builder.append(" diskId=").append(diskId);
		builder.append("]");
		return builder.toString();
	}
}
