package droid64.gui;

import javax.swing.table.AbstractTableModel;

/**
 * Table model used by HexViewFrame.
 * @author Henrik
 * @see HexViewDialog
 */
public class HexTableModel extends AbstractTableModel {

	private static final long serialVersionUID = 1L;
	private int bytesPerRow = 16;

	private String[] columnHeaders;
	private byte[] data = null;
	private int length;
	
	/** Used for quick translation from byte to string */
	public static final String[] HEX = {
			"00", "01", "02", "03", "04", "05", "06", "07", "08", "09", "0a", "0b", "0c", "0d", "0e", "0f", 
			"10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "1a", "1b", "1c", "1d", "1e", "1f", 
			"20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "2a", "2b", "2c", "2d", "2e", "2f", 
			"30", "31", "32", "33", "34", "35", "36", "37", "38", "39", "3a", "3b", "3c", "3d", "3e", "3f", 
			"40", "41", "42", "43", "44", "45", "46", "47", "48", "49", "4a", "4b", "4c", "4d", "4e", "4f", 
			"50", "51", "52", "53", "54", "55", "56", "57", "58", "59", "5a", "5b", "5c", "5d", "5e", "5f", 
			"60", "61", "62", "63", "64", "65", "66", "67", "68", "69", "6a", "6b", "6c", "6d", "6e", "6f", 
			"70", "71", "72", "73", "74", "75", "76", "77", "78", "79", "7a", "7b", "7c", "7d", "7e", "7f", 
			"80", "81", "82", "83", "84", "85", "86", "87", "88", "89", "8a", "8b", "8c", "8d", "8e", "8f", 
			"90", "91", "92", "93", "94", "95", "96", "97", "98", "99", "9a", "9b", "9c", "9d", "9e", "9f", 
			"a0", "a1", "a2", "a3", "a4", "a5", "a6", "a7", "a8", "a9", "aa", "ab", "ac", "ad", "ae", "af", 
			"b0", "b1", "b2", "b3", "b4", "b5", "b6", "b7", "b8", "b9", "ba", "bb", "bc", "bd", "be", "bf", 
			"c0", "c1", "c2", "c3", "c4", "c5", "c6", "c7", "c8", "c9", "ca", "cb", "cc", "cd", "ce", "cf", 
			"d0", "d1", "d2", "d3", "d4", "d5", "d6", "d7", "d8", "d9", "da", "db", "dc", "dd", "de", "df", 
			"e0", "e1", "e2", "e3", "e4", "e5", "e6", "e7", "e8", "e9", "ea", "eb", "ec", "ed", "ee", "ef", 
			"f0", "f1", "f2", "f3", "f4", "f5", "f6", "f7", "f8", "f9", "fa", "fb", "fc", "fd", "fe", "ff" };
	
	/**
	 * Constructor
	 * @param data the data to render
	 * @param length the length of the data
	 */
	public HexTableModel(byte[] data, int length) {
		this.data = data;
		this.length = data == null ? 0 : length < data.length ? length : data.length;
		setColumnHeaders();
	}
		
    @Override
    public String getColumnName(int column) {
    	if (column == 0) {
    		return "Address";
    	} else if (column <= bytesPerRow) {
    		return Integer.toHexString(column - 1).toUpperCase();
    	} else if (column == bytesPerRow + 1) {
    		return "ASCII";
    	} else {
    		return "";
    	}
    }
    
	@Override
	public int getRowCount() {
		if (data==null || data.length <1) {
			return 0;
		} else {
			return (length + bytesPerRow  - 1) / bytesPerRow ;
		}		
	}

	@Override
	public int getColumnCount() {
		return bytesPerRow + 2;
	}

	@Override
	public String getValueAt(int rowIndex, int columnIndex) {
		if (data == null) {
			return "";
		} else {
			if (columnIndex == 0) {
				return getIntHexString(rowIndex * bytesPerRow);				
			} else if (columnIndex == bytesPerRow + 1) {
				return getDumpRowString(rowIndex);				
			} else {
				int addr = (rowIndex * bytesPerRow) + columnIndex -1;
				if (addr < data.length && addr < length) {
					return HEX[ (int) (data[addr] & 0xff) ];
				} else {
					return "";
				}				
			}
		}
	}
	
	public Integer getByteAt(int rowIndex, int columnIndex) {
		if (data == null) {
			return null;
		} else {
			if (columnIndex == 0) {
				return null;				
			} else if (columnIndex == bytesPerRow + 1) {
				return null;				
			} else {
				int addr = (rowIndex * bytesPerRow) + columnIndex -1;
				if (addr < data.length && addr < length) {
					return new Integer(data[addr] & 0xff);
				} else {
					return null;
				}				
			}
		}
	}
	
	/**
	 * Get an ASCII dump of the bytes displayed at rowIndex in data.
	 * @param rowIndex int
	 * @return String
	 */
	private String getDumpRowString(int rowIndex) {
		int start = rowIndex * bytesPerRow;
		if (data == null) {
			return "x";
		}
		StringBuffer buf = new StringBuffer();
		for (int i=start; i < start + bytesPerRow; i++) {
			if (i < data.length) {
				byte b = data[i];
				if (b < 0x20 || b > 0x7e) {
					buf.append('.');
				} else {
					buf.append((char)b);
				}
			} else {
				buf.append(' ');
			}
		}
		return buf.toString();
	}

	/**
	 * Convert an int to a hexadecimal string with leading zeroes (unlike Integer.tohexString())
	 * @param i
	 * @return String
	 * @see Integer#toHexString(int)
	 */
	private String getIntHexString(int i) {
		return HEX[i>>24 & 0xff] +  HEX[i>>16 & 0xff] + HEX[i>>8 & 0xff] + HEX[i & 0xff];
	}
	
	/**
	 * Setup all column headers
	 */
    private void setColumnHeaders() {
    	this.columnHeaders = new String[bytesPerRow];
    	columnHeaders[0] = "Address";
    	columnHeaders[bytesPerRow - 1] = "ASCII";
    	for (int i=1; i < bytesPerRow - 1 ; i++) {
    		columnHeaders[i] = Integer.toHexString(i - 1).toUpperCase();
    	}
    }
}
