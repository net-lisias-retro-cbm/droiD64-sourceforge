package droid64.d64;

import java.util.Arrays;
import java.util.List;

/**<pre style='font-family:sans-serif;'>
 * Created on 2015-Oct-15
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2015 Henrik Wetterström
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
 *   http://droid64.sourceforge.net
 *
 * @author Henrik
 * </pre>
 */
public class T64 extends DiskImage {

//	private int tapeVersionLow;
//	private int tapeVersionHigh;
	private int maxEntries;
	private int usedEntries;
	
	public T64() {
	}

	@Override
	public int getMaxSectors(int trackNumber) {
		return 0;
	}

	@Override
	public int getTrackCount() {
		return 0;
	}

	@Override
	public int getMaxSectorCount() {
		return 0;
	}

	@Override
	public byte[] getBlock(int track, int sector) throws CbmException {
		return null;
	}

	@Override
	public int getBlocksFree() {
		return 0;
	}

	
	@Override
	protected void readImage(String filename) throws CbmException {	
		bam = new CbmBam(1, 1);
		readImage(filename, 0x40, "T64");
//		tapeVersionLow  = cbmDisk[0x20] & 0xff;
//		tapeVersionHigh = cbmDisk[0x21] & 0xff;
		maxEntries  = (cbmDisk[0x22] & 0xff) | ((cbmDisk[0x23] & 0xff) << 8);
		usedEntries = (cbmDisk[0x24] & 0xff) | ((cbmDisk[0x25] & 0xff) << 8);
		initCbmFile(maxEntries);
	}

	@Override
	public void readBAM() {		
		bam.setDiskName("");
		bam.setDiskId("");
		for (int i = 0; i < 32; i++) {
			int c = cbmDisk[0x00 + i] & 0xff;
			if (c!=0) {
				bam.setDiskName(bam.getDiskName() + Character.toUpperCase(((char)(PETSCII_TABLE[c]))) );
			}
		}
		checkImageFormat();
	}

	@Override
	public void readDirectory() {		
		int dirPosition = 0;
		int filenumber = 0;
		boolean fileLimitReached = false;
		for (int i=0; i<usedEntries; i++) {
			int pos = 0x40 + i * DIR_ENTRY_SIZE;
			if ((cbmDisk[pos] & 0xff) != 0) {
				int loadStartAddr =  (cbmDisk[pos + 0x02] & 0xff) | ((cbmDisk[pos + 0x03] & 0xff) << 8);
				int loadEndAddr =  (cbmDisk[pos + 0x04] & 0xff) | ((cbmDisk[pos + 0x05] & 0xff) << 8);
				int size = loadEndAddr - loadStartAddr;
				int offset = (cbmDisk[pos + 0x08] & 0xff) | ((cbmDisk[pos + 0x09] & 0xff) << 8)  | ((cbmDisk[pos + 0x0a] & 0xff) << 16  | ((cbmDisk[pos + 0x0b] & 0xff) << 24));
	
				StringBuffer buf = new StringBuffer(16);
				for (int j = 0; j < 16; j++){
					int c = cbmDisk[pos + 0x10 + j] & 0xff;
					if (c != 0x20) {
						buf.append((char)(DiskImage.PETSCII_TABLE[c]));
					}
				}
				CbmFile cf = new CbmFile(buf.toString(), cbmDisk[pos+1]&0x07, dirPosition, dirPosition++, offset, size);
				cf.setTrack( (cbmDisk[pos+0x08]&0xff) | ((cbmDisk[pos+0x09]&0xff)<<8));
				cf.setSector( (cbmDisk[pos+0x0a]&0xff) | ((cbmDisk[pos+0x0b]&0xff)<<8));
				cf.setOffSet(offset);
				cbmFile[filenumber] = cf;				
				if (filenumber < maxEntries)  {
					filenumber++;
				} else {
					// Too many files in directory check
					fileLimitReached = true;
				}
			} else {
				//System.out.println("T64.readDirectory: free entry");				
			}
		}
		if (fileLimitReached) {
			feedbackMessage.append("Error: Too many entries in directory (more than ").append(maxEntries).append(")!\n");
		}
		fileNumberMax = filenumber;
	}

	@Override
	public byte[] getFileData(int number) throws CbmException {
		if (number < cbmFile.length) {
			CbmFile cf = cbmFile[number];
			return Arrays.copyOfRange(cbmDisk, cf.getOffSet(), cf.getOffSet() + cf.getSizeInBytes());
		} else {
			throw new CbmException("T64 file number "+number+" does not exist.");
		}
	}

	@Override
	protected TrackSector saveFileData(byte[] saveData) {
		feedbackMessage.append("saveFileData: not supported for T64 images.\n");
		return null;
	}

	@Override
	protected void setDiskName(String newDiskName, String newDiskID) {
		feedbackMessage.append("setDiskName('").append(newDiskName).append("')\n");
		for (int i=0; i < 32; i++) {
			if (i < newDiskName.length()) {
				setCbmDiskValue(0x00 + i, newDiskName.charAt(i));
			} else {
				setCbmDiskValue(0x00 + i, 0x00);				
			}
		}
	}

	@Override
	protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber) {		
		feedbackMessage.append("writeDirectoryEntry: not supported for T64 images.\n");
	}

	@Override
	public boolean saveNewImage(String filename, String newDiskName, String newDiskID) {
		return false;
	}

	@Override
	public boolean addDirectoryEntry(CbmFile cbmFile, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes) {
		feedbackMessage.append("addDirectoryEntry: not supported for T64 images.\n");
		return false;
	}

	@Override
	public String[][] getBamTable() {
		return new String[0][0];
	}

	@Override
	public int getSectorOffset(int track, int sector) {
		return 0;
	}

	@Override
	public void deleteFile(CbmFile cbmFile) throws CbmException {
		throw new CbmException("Delete not yet implemented for T64.");				
	}

	@Override
	public void readPartition(int track, int sector, int numBlocks) throws CbmException {
		throw new CbmException("T64 images does not support partitions.");
	}

	@Override
	public Integer validate(List<Integer> repairList) {
		feedbackMessage.append("validate: not supported for T64 images.\n");
		return null;
	}
}
