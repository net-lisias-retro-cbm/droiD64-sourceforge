package droid64.d64;

import java.io.ByteArrayOutputStream;
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
public class D80 extends DiskImage {

	/** Name of the image type */
	public final static String IMAGE_TYPE_NAME = "D80";
	/** Max number of directory entries in image : (29 - 1) * 8 = 224 */
	private static final int FILE_NUMBER_LIMIT = 224;
	/** The normal size of a D82 image */
	private final static int D80_SIZE       = 533248;
	/** Maximum number of sectors on any track */
	private static final int MAX_SECTORS    = 29;
	/** Number of tracks of image */
	private final static int TRACK_COUNT	= 77;
	/** Track of disk header block */
	protected final static int HEADER_TRACK	= 39;
	/** Sector of disk header block */
	protected final static int HEADER_SECT	= 0;	
	/** Track of BAM block 1 and BAM block 2 */
	private final static int BAM_TRACK	    = 38;
	/** Sector of BAM block 1 (38/0) */
	private final static int BAM_SECT_1	    = 0;	
	/** Sector of BAM block 2 (38/3) */
	private final static int BAM_SECT_2	    = 3;
	/** Track of first directory block */
	private final static int DIR_TRACK		= 39;
	/** Sector of first directory block (40/3) */
	private final static int DIR_SECT		= 1;
	/** Track number of last track plus one  */
	private static final int LAST_TRACK     = 77;
	
	/** Array of CbmTrack (SecorCount, SectorOffset, ByteOffset)*/
	private final static CbmTrack[] TRACKS = {
			//  Sect SectorsIn Offset Track 
			new CbmTrack( 29,    0, 0x000000),  // 0 (dummy)
			new CbmTrack( 29,    0, 0x000000),	// 1
			new CbmTrack( 29,   29, 0x001d00),	// 2
			new CbmTrack( 29,   58, 0x003a00),	// 3
			new CbmTrack( 29,   87, 0x005700),	// 4
			new CbmTrack( 29,  116, 0x007400),	// 5
			new CbmTrack( 29,  145, 0x009100),	// 6
			new CbmTrack( 29,  174, 0x00ae00),	// 7
			new CbmTrack( 29,  203, 0x00cb00),	// 8
			new CbmTrack( 29,  232, 0x00e800),	// 9
			new CbmTrack( 29,  261, 0x010500),	// 10
			new CbmTrack( 29,  290, 0x012200),	// 11
			new CbmTrack( 29,  319, 0x013f00),	// 12
			new CbmTrack( 29,  348, 0x015c00),	// 13
			new CbmTrack( 29,  377, 0x017900),	// 14
			new CbmTrack( 29,  406, 0x019600),	// 15
			new CbmTrack( 29,  435, 0x01b300),	// 16
			new CbmTrack( 29,  464, 0x01d000),	// 17
			new CbmTrack( 29,  493, 0x01ed00),	// 18
			new CbmTrack( 29,  522, 0x020a00),	// 19
			new CbmTrack( 29,  551, 0x022700),	// 20
			new CbmTrack( 29,  580, 0x024400),	// 21
			new CbmTrack( 29,  609, 0x026100),	// 22
			new CbmTrack( 29,  638, 0x027e00),	// 23
			new CbmTrack( 29,  667, 0x029b00),	// 24
			new CbmTrack( 29,  696, 0x02b800),	// 25
			new CbmTrack( 29,  725, 0x02d500),	// 26
			new CbmTrack( 29,  754, 0x02f200),	// 27
			new CbmTrack( 29,  783, 0x030f00),	// 28
			new CbmTrack( 29,  812, 0x032c00),	// 29
			new CbmTrack( 29,  841, 0x034900),	// 30
			new CbmTrack( 29,  870, 0x036600),	// 31
			new CbmTrack( 29,  899, 0x038300),	// 32
			new CbmTrack( 29,  928, 0x03a000),	// 33
			new CbmTrack( 29,  957, 0x03bd00),	// 34
			new CbmTrack( 29,  986, 0x03da00),	// 35
			new CbmTrack( 29, 1015, 0x03f700),	// 36
			new CbmTrack( 29, 1044, 0x041400),	// 37
			new CbmTrack( 29, 1073, 0x043100),	// 38
			new CbmTrack( 29, 1102, 0x044e00),	// 39
			new CbmTrack( 27, 1131, 0x046b00),	// 40
			new CbmTrack( 27, 1158, 0x048600),	// 41
			new CbmTrack( 27, 1185, 0x04a100),	// 42
			new CbmTrack( 27, 1212, 0x04bc00),	// 43
			new CbmTrack( 27, 1239, 0x04d700),	// 44
			new CbmTrack( 27, 1266, 0x04f200),	// 45
			new CbmTrack( 27, 1293, 0x050d00),	// 46
			new CbmTrack( 27, 1320, 0x052800),	// 47
			new CbmTrack( 27, 1347, 0x054300),	// 48
			new CbmTrack( 27, 1374, 0x055e00),	// 49
			new CbmTrack( 27, 1401, 0x057900),	// 50
			new CbmTrack( 27, 1428, 0x059400),	// 51
			new CbmTrack( 27, 1455, 0x05af00),	// 52
			new CbmTrack( 27, 1482, 0x05ca00),	// 53
			new CbmTrack( 25, 1509, 0x05e500),	// 54
			new CbmTrack( 25, 1534, 0x05fe00),	// 55
			new CbmTrack( 25, 1559, 0x061700),	// 56
			new CbmTrack( 25, 1584, 0x063000),	// 57
			new CbmTrack( 25, 1609, 0x064900),	// 58
			new CbmTrack( 25, 1634, 0x066200),	// 59
			new CbmTrack( 25, 1659, 0x067b00),	// 60
			new CbmTrack( 25, 1684, 0x069400),	// 61
			new CbmTrack( 25, 1709, 0x06ad00),	// 62
			new CbmTrack( 25, 1734, 0x06c600),	// 63
			new CbmTrack( 25, 1759, 0x06df00),	// 64
			new CbmTrack( 23, 1784, 0x06f800),	// 65
			new CbmTrack( 23, 1807, 0x070f00),	// 66
			new CbmTrack( 23, 1830, 0x072600),	// 67
			new CbmTrack( 23, 1853, 0x073d00),	// 68
			new CbmTrack( 23, 1876, 0x075400),	// 69
			new CbmTrack( 23, 1899, 0x076b00),	// 70
			new CbmTrack( 23, 1922, 0x078200),	// 71
			new CbmTrack( 23, 1945, 0x079900),	// 72
			new CbmTrack( 23, 1968, 0x07b000),	// 73
			new CbmTrack( 23, 1991, 0x07c700),	// 74
			new CbmTrack( 23, 2014, 0x07de00),	// 75
			new CbmTrack( 23, 2037, 0x07f500),	// 76
			new CbmTrack( 23, 2060, 0x080c00),	// 77
	};
	
	public D80() {
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	public D80(byte[] imageData) {
		cbmDisk = imageData;
		bam = new CbmBam(TRACKS.length, 5);
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	@Override
	public int getMaxSectors(int trackNumber) {
		return TRACKS[trackNumber].getSectors();
	}

	@Override
	public int getTrackCount() {
		return TRACK_COUNT;
	}

	@Override
	public int getMaxSectorCount() {
		return MAX_SECTORS;
	}

	@Override
	public byte[] getBlock(int track, int sector) throws CbmException {
		if (track >= TRACKS.length || track < 1) {
			throw new CbmException("Track "+track+" is not valid.");
		} else if (sector >= TRACKS[track].getSectors()) {
			throw new CbmException("Sector "+sector+" is not valid.");
		} else {
			int pos = TRACKS[track].getOffset() + sector * BLOCK_SIZE;
			return Arrays.copyOfRange(cbmDisk, pos, pos + BLOCK_SIZE);
		}
	}

	@Override
	public int getBlocksFree() {
		int blocksFree = 0;
		if (cbmDisk != null) {
			for (int track = 1; track <= LAST_TRACK; track++) {
				if (track != DIR_TRACK) {
					blocksFree = blocksFree + bam.getFreeSectors(track);
				}
			}
		}
		return blocksFree;
	}

	@Override
	protected void readImage(String filename) throws CbmException {
		bam = new CbmBam(TRACKS.length, 5);
		readImage(filename, D80_SIZE, IMAGE_TYPE_NAME);
	}

	@Override
	public void readBAM() {
		int bamOffset1 = getSectorOffset(BAM_TRACK, BAM_SECT_1);
		int bamOffset2 = getSectorOffset(BAM_TRACK, BAM_SECT_2);
		int headerOffset = getSectorOffset(HEADER_TRACK, HEADER_SECT);
		
		bam.setDiskName("");
		bam.setDiskId("");
		bam.setDiskDosType(getCbmDiskValue(headerOffset + 2 ));
		for (int track = 1; track <= TRACKS.length; track++) {
			int pos = 0;
			if (track <= 50) {
				pos = bamOffset1 + 0x06 + (track-1) * 5;
			} else {
				pos = bamOffset2 + 0x06 + (track-51) * 5;			
			}
			bam.setFreeSectors(track, (byte) getCbmDiskValue(pos));
			for (int i = 1; i < 4; i++) {
				bam.setTrackBits(track, i, (byte) getCbmDiskValue(pos + i));
			}
		}
		for (int i = 0; i < DISK_NAME_LENGTH; i++) {
			bam.setDiskName(bam.getDiskName() + new Character((char)(getCbmDiskValue(headerOffset + 0x06 + i))));
		}
		for (int i = 0; i < DISK_ID_LENGTH; i++) {
			bam.setDiskId(bam.getDiskId() + new Character((char)(getCbmDiskValue(headerOffset + 0x18 + i))));
		}
		checkImageFormat();
	}

	@Override
	public void readDirectory() {
		if (isCpmImage()) {
			//readCpmDirectory();
			return;
		}
		boolean fileLimitReached = false;
		int track = DIR_TRACK;
		int sector = DIR_SECT;
		int dirPosition = 0;
		int filenumber = 0;
		do {
			if (track >= TRACKS.length) {
				feedbackMessage.append("Error: Track ").append(track).append(" is not within image.\n");
				break;
			}
			int dataPosition = getSectorOffset(track, sector);
			for (int i = 0; i < DIR_ENTRIES_PER_SECTOR; i ++) {
				cbmFile[filenumber] = new CbmFile(cbmDisk, dataPosition + (i * DIR_ENTRY_SIZE));
				if (cbmFile[filenumber].isFileScratched() == false) {
					cbmFile[filenumber].setDirPosition(dirPosition);
					if (filenumber < FILE_NUMBER_LIMIT)  {
						filenumber++;
					} else {
						// Too many files in directory check
						fileLimitReached = true;
					}
				}
				dirPosition++;
			}
			track = getCbmDiskValue(dataPosition + 0);
			sector = getCbmDiskValue(dataPosition + 1);
		} while (track != 0 && !fileLimitReached);
		if (fileLimitReached) {
			feedbackMessage.append("Error: Too many entries in directory (more than ").append(FILE_NUMBER_LIMIT).append(")!\n");
		}
		fileNumberMax = filenumber;
		validate(null);
	}

	@Override
	public void readPartition(int track, int sector, int numBlocks) throws CbmException {
		throw new CbmException("Not yet implemented D82 partitions.");
	}

	@Override
	public byte[] getFileData(int number) throws CbmException {
		if (cbmDisk == null) {
			throw new CbmException("getFileData: No disk data exist.");
		} else if (number >= cbmFile.length) {
			throw new CbmException("getFileData: File number " + number + " does not exist.");
		} else if (isCpmImage()) { 
			feedbackMessage.append("getFileData: CP/M mode.\n");
			throw new CbmException("Not yet implemented for CP/M format.");
		} else if (cbmFile[number].isFileScratched()) {
			throw new CbmException("getFileData: File number " + number + " is deleted.");
		}
		feedbackMessage.append("getFileData: ").append(number).append(" '").append(cbmFile[number].getName()).append("'\n");
		feedbackMessage.append("Tracks / Sectors: ");
		// write ints
		int thisTrack = cbmFile[number].getTrack();
		int thisSector = cbmFile[number].getSector();
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		do {
			if (thisTrack >= TRACKS.length) {
				throw new CbmException("Track " + thisTrack + " outside of image.");
			}
			int nextTrack  =  getCbmDiskValue( TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 0);
			int nextSector =  getCbmDiskValue( TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 1);
			feedbackMessage.append(thisTrack).append("/").append(thisSector).append(" ");
			if (nextTrack > 0) {
				for (int position = 2; position < BLOCK_SIZE; position++) {
					out.write(cbmDisk[ TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + position]);
				}
			} else {
				feedbackMessage.append("\nRemaining bytes: ").append(nextSector).append("\n");
				for (int position = 2; position <= nextSector; position++) {
					out.write(cbmDisk[ TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + position]);
				}
			}
			thisTrack = nextTrack;
			thisSector = nextSector;
		} while (thisTrack != 0);	
		feedbackMessage.append("OK.\n");
		return out.toByteArray();
	}

	@Override
	protected TrackSector saveFileData(byte[] saveData) {
		return null;
	}

	@Override
	protected void setDiskName(String newDiskName, String newDiskID) {
	}

	@Override
	protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber) {
	}

	@Override
	public boolean saveNewImage(String filename, String newDiskName, String newDiskID) {
		return false;
	}

	@Override
	public boolean addDirectoryEntry(CbmFile cbmFile, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes) {
		return false;
	}

	@Override
	public String[][] getBamTable() {
		String[][] bamEntry = new String[TRACK_COUNT][MAX_SECTORS + 1];
		for (int trk = 0; trk < TRACK_COUNT; trk++) {
			for (int sec = 0; sec <= MAX_SECTORS; sec++) {
				bamEntry[trk][sec] =  CbmBam.INVALID;
			}
		}
		for (int trk = 1; trk <= TRACK_COUNT; trk++) {
			int bitCounter = 1;
			bamEntry[trk-1][0] = Integer.toString(trk);
			for (int cnt = 1; cnt < 4; cnt++) {
				for (int bit = 0; bit < 8; bit++) {
					if (bitCounter <= getMaxSectors(trk)) {
						if (trk == BAM_TRACK) {
							bamEntry[trk-1][bitCounter++] = CbmBam.RESERVED;							
						} else if ((getBam().getTrackBits(trk, cnt) & DiskImage.BYTE_BIT_MASKS[bit]) == 0) {
							bamEntry[trk-1][bitCounter++] = CbmBam.USED;
						} else {
							bamEntry[trk-1][bitCounter++] = CbmBam.FREE;
						}
					}
				}
			}
		}		
		return bamEntry;
	}

	@Override
	public int getSectorOffset(int track, int sector) {
		return TRACKS[track].getOffset() + (BLOCK_SIZE * sector);

	}

	@Override
	public void deleteFile(CbmFile cbmFile) throws CbmException {
		throw new CbmException("Not yet implemented D82 delete.");
	}

	@Override
	public Integer validate(List<Integer> repairList) {
		return null;
	}

	@Override
	public boolean isSectorFree(int track, int sector) {
		return false;
	}

	@Override
	public void markSectorFree(int track, int sector) {
	}

	@Override
	public void markSectorUsed(int track, int sector) {
	}

}