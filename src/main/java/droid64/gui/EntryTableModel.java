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
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import droid64.d64.DirEntry;

class EntryTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	protected int nextEmptyRow = 0;
	protected int numRows = 0;

	public final static int MODE_CBM   = 1;
	public final static int MODE_CPM   = 2;
	public final static int MODE_LOCAL = 3;

	private int mode = MODE_CBM;
				
	private static final String[] COLHEADS_CBM = { "Nr", "Bl", "Name", "Ty", "Fl", "Tr", "Se" };
	private static final String[] COLHEADS_CPM = { "Nr", "Rec", "Name", "Ext", "Attr", "Tr", "Se" };
	private static final String[] COLHEADS_LOCAL = { "Nr", "Name", "Attr", "Size" };
	
	private static final int[] COLWIDTH_CBM = {1,1,220, 1,1,1,1};
	private static final int[] COLWIDTH_CPM = {1,1,220,1,1,1,1};
	private static final int[] COLWIDTH_LOCAL = {1, 220, 1,1};
	
	
	protected Vector<DirEntry> data = new Vector<DirEntry>();

	public EntryTableModel() {
	}

	public String getColumnName(int column) {
		switch (mode) {
		case MODE_CBM: return column < COLHEADS_CBM.length ? COLHEADS_CBM[column] : "";
		case MODE_CPM: return column < COLHEADS_CPM.length ?  COLHEADS_CPM[column] : "";
		case MODE_LOCAL: return column < COLHEADS_LOCAL.length ? COLHEADS_LOCAL[column] : "";
		default: return "";
		}
	}

	public int getColumnCount() {
		switch (mode) {
		case MODE_CBM: return COLHEADS_CBM.length;
		case MODE_CPM: return COLHEADS_CPM.length;
		case MODE_LOCAL: return COLHEADS_LOCAL.length;
		default: return 0;
		}		
	}

	public synchronized int getRowCount() {
		return data.size();
	}

	public synchronized boolean isFile(int row, int column) {
		DirEntry p = data.elementAt(row);
		return p.isFile();
	}

	public synchronized boolean isImageFile(int row, int column) {
		DirEntry p = data.elementAt(row);
		return p.isImageFile();
	}
	
	public synchronized Object getValueAt(int row, int column) {
		try {
			//		{ "Nr", "Bl", "Name", "Ty", "Fl", "Tr", "Se" };
			DirEntry p = data.elementAt(row);
			switch (mode) {
				case MODE_CBM:
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
				case MODE_CPM:
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
				case MODE_LOCAL:
					switch (column) {
					case 0:	return new Integer(p.getNumber());
					case 1:	return p.getName();
					case 2:	return p.getFlags();
					case 3:	return new Integer(p.getBlocks());
					case 4:	return "";
					case 5: return "";
					case 6: return "";
					default: return "";
				}
				default:
					return "";
			}
		} catch (ArrayIndexOutOfBoundsException e) {
			return "";
		}
	}

	public TableColumnModel getTableColumnModel() {
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		for (int i=0; i<getColumnCount(); i++) {
			int wdt = 1;
			switch (mode) {
				case MODE_CBM: wdt = COLWIDTH_CBM[i]; break;
				case MODE_CPM: wdt = COLWIDTH_CPM[i]; break;
				case MODE_LOCAL: wdt = COLWIDTH_LOCAL[i]; break;
			}
			TableColumn col = new TableColumn(i, wdt);
			col.setHeaderValue(getColumnName(i));
			columnModel.addColumn(col);
		}
		return columnModel;
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

	public void setMode(int mode) {
		this.mode = mode;
	}
	
	public int getMode() {
		return mode;
	}

}
