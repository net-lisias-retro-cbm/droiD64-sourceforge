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
public class CbmTrack {

	private int sectors;
	private int sectors_in;
	private int offset;
	
	public CbmTrack(){
	}
	
	public CbmTrack(int sectors_, int sectors_in_, int offset_){
		sectors = sectors_;
		sectors_in = sectors_in_;
		offset = offset_;
	}
	
	public void setAllValues(int sectors_, int sectors_in_, int offset_){
		sectors = sectors_;
		sectors_in = sectors_in_;
		offset = offset_;
	}

	/**
	 * @return
	 */
	public int getOffset() {
		return offset;
	}

	/**
	 * @return
	 */
	public int getSectors() {
		return sectors;
	}

	/**
	 * @return
	 */
	public int getSectors_in() {
		return sectors_in;
	}

	/**
	 * @param d
	 */
	public void setOffset(int d) {
		offset = d;
	}

	/**
	 * @param b
	 */
	public void setSectors(int b) {
		sectors = b;
	}

	/**
	 * @param b
	 */
	public void setSectors_in(int b) {
		sectors_in = b;
	}

}
