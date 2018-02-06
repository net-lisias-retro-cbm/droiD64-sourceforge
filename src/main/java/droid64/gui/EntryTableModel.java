/*
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
 */

/**
 * @author wolf
 */
package droid64.gui;
import java.util.Vector;

import javax.swing.table.AbstractTableModel;

import droid64.d64.DirEntry;

class EntryTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	protected int nextEmptyRow = 0;
	protected int numRows = 0;

	private static final String[] COLHEADS = { "Nr", "Bl", "Name", "Ty", "Fl", "Tr", "Se" };

	protected Vector<DirEntry> data = new Vector<DirEntry>();

	public EntryTableModel() {
	}

	public String getColumnName(int column) {
		return COLHEADS[column];
	}

	public int getColumnCount() {
		return COLHEADS.length;
	}

	public synchronized int getRowCount() {
		return data.size();
	}

	public synchronized Object getValueAt(int row, int column) {
		try {
			//		{ "Nr", "Bl", "Name", "Ty", "Fl", "Tr", "Se" };
			DirEntry p = data.elementAt(row);
			switch (column) {
				case 0:	return new Integer(p.getNumber());
				case 1:	return new Integer(p.getBlocks());
				case 2:	return p.getName();
				case 3:	return p.getType();
				case 4:	return p.getFlags();
				case 5: return new Integer(p.getTrack());
				case 6: return new Integer(p.getSector());
				default: return "";
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public synchronized void updateDirEntry(DirEntry dirEntry) {
		data.add(dirEntry);
		fireTableRowsInserted(data.size()-1, data.size()-1);
	}

	public synchronized void clear() {
		int oldSize = data.size();
		data.clear();
		fireTableRowsDeleted(0, oldSize);
	}

}
