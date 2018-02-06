package droid64.d64;

import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
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
public class D71 extends DiskImage {

	/** D71 format is restricted to a maximum of 144 directory entries (18 sectors with 8 entries each). Track 18 has 19 sectors, of which the first is the BAM. */
	private static final int FILE_NUMBER_LIMIT = 144;
	/** Number of tracks */
	private static final int TRACK_COUNT = 70;
	/** Maximum number of sectors on any track */
	private static final int MAX_SECTORS = 21;
	/** Array of CbmTrack (SecorCount, SectorOffset, ByteOffset)*/
	private final static CbmTrack[] TRACKS = {
			//Sect SectorsIn Offset Track 
			new CbmTrack( 21,    0, 0x00000 ),   //  0 (dummy)
			new CbmTrack( 21,    0, 0x00000 ),   //  1
			new CbmTrack( 21,   21, 0x01500 ),   //  2
			new CbmTrack( 21,   42, 0x02a00 ),   //  3
			new CbmTrack( 21,   63, 0x03f00 ),   //  4
			new CbmTrack( 21,   84, 0x05400 ),   //  5
			new CbmTrack( 21,  105, 0x06900 ),   //  6
			new CbmTrack( 21,  126, 0x07e00 ),   //  7
			new CbmTrack( 21,  147, 0x09300 ),   //  8
			new CbmTrack( 21,  168, 0x0a800 ),   //  9
			new CbmTrack( 21,  189, 0x0bd00 ),   // 10
			new CbmTrack( 21,  210, 0x0d200 ),   // 11
			new CbmTrack( 21,  231, 0x0e700 ),   // 12
			new CbmTrack( 21,  252, 0x0fc00 ),   // 13
			new CbmTrack( 21,  273, 0x11100 ),   // 14
			new CbmTrack( 21,  294, 0x12600 ),   // 15
			new CbmTrack( 21,  315, 0x13b00 ),   // 16
			new CbmTrack( 21,  336, 0x15000 ),   // 17
			new CbmTrack( 19,  357, 0x16500 ),   // 18
			new CbmTrack( 19,  376, 0x17800 ),   // 19
			new CbmTrack( 19,  395, 0x18b00 ),   // 20
			new CbmTrack( 19,  414, 0x19e00 ),   // 21
			new CbmTrack( 19,  433, 0x1b100 ),   // 22
			new CbmTrack( 19,  452, 0x1c400 ),   // 23
			new CbmTrack( 19,  471, 0x1d700 ),   // 24
			new CbmTrack( 18,  490, 0x1ea00 ),   // 25
			new CbmTrack( 18,  508, 0x1fc00 ),   // 26
			new CbmTrack( 18,  526, 0x20e00 ),   // 27
			new CbmTrack( 18,  544, 0x22000 ),   // 28
			new CbmTrack( 18,  562, 0x23200 ),   // 29
			new CbmTrack( 18,  580, 0x24400 ),   // 30
			new CbmTrack( 17,  598, 0x25600 ),   // 31
			new CbmTrack( 17,  615, 0x26700 ),   // 32
			new CbmTrack( 17,  632, 0x27800 ),   // 33
			new CbmTrack( 17,  649, 0x28900 ),   // 34
			new CbmTrack( 17,  666, 0x29a00 ),   // 35
			new CbmTrack( 21,  683, 0x2ab00 ),   // 36
			new CbmTrack( 21,  704, 0x2c000 ),   // 37
			new CbmTrack( 21,  725, 0x2d500 ),   // 38
			new CbmTrack( 21,  746, 0x2ea00 ),   // 39
			new CbmTrack( 21,  767, 0x2ff00 ),   // 40
			new CbmTrack( 21,  788, 0x31400 ),   // 41
			new CbmTrack( 21,  809, 0x32900 ),   // 42
			new CbmTrack( 21,  830, 0x33e00 ),   // 43
			new CbmTrack( 21,  851, 0x35300 ),   // 44
			new CbmTrack( 21,  872, 0x36800 ),   // 45
			new CbmTrack( 21,  893, 0x37d00 ),   // 46
			new CbmTrack( 21,  914, 0x39200 ),   // 47
			new CbmTrack( 21,  935, 0x3a700 ),   // 48
			new CbmTrack( 21,  956, 0x3bc00 ),   // 49
			new CbmTrack( 21,  977, 0x3d100 ),   // 50
			new CbmTrack( 21,  998, 0x3e600 ),   // 51
			new CbmTrack( 21, 1019, 0x3fb00 ),   // 52
			new CbmTrack( 19, 1040, 0x41000 ),   // 53
			new CbmTrack( 19, 1059, 0x42300 ),   // 54
			new CbmTrack( 19, 1078, 0x43600 ),   // 55
			new CbmTrack( 19, 1097, 0x44900 ),   // 56
			new CbmTrack( 19, 1116, 0x45c00 ),   // 57
			new CbmTrack( 19, 1135, 0x46f00 ),   // 58
			new CbmTrack( 19, 1154, 0x48200 ),   // 59
			new CbmTrack( 18, 1173, 0x49500 ),   // 60
			new CbmTrack( 18, 1191, 0x4a700 ),   // 61
			new CbmTrack( 18, 1209, 0x4b900 ),   // 62
			new CbmTrack( 18, 1227, 0x4cb00 ),   // 63
			new CbmTrack( 18, 1245, 0x4dd00 ),   // 64
			new CbmTrack( 18, 1263, 0x4ef00 ),   // 65
			new CbmTrack( 17, 1281, 0x50100 ),   // 66
			new CbmTrack( 17, 1298, 0x51200 ),   // 67
			new CbmTrack( 17, 1315, 0x52300 ),   // 68
			new CbmTrack( 17, 1332, 0x53400 ),   // 69
			new CbmTrack( 17, 1349, 0x54500 ),   // 70
	};

	/** The normal size of a D71 image (1366 * 256) */
	private final static int D71_SIZE = 349696;
	/** Track number of directory */
	private final static int DIR_TRACK   = 18;
	/** Sector number of directory */
	private final static int DIR_SECT    = 1;
	/** Track number of BAM 1 */
	protected final static int BAM_TRACK_1 = 18;
	/** Track number of BAM 2 */
	protected final static int BAM_TRACK_2 = 53;
	/** Sector number of BAM 1 and 2 */
	protected final static int BAM_SECT  = 0;
	/** CP/M sector skew (distance between two sectors within one allocation unit) */
	private static final int CPM_SECTOR_SKEW = 5;
	/** Number of 256 bytes blocks (3 blocks are not used) */
	private static final int CPM_BLOCK_COUNT = 680; 
	/** Table for finding track/sector of CP/M allocation unit */
	private static final int[][] CPM_ZONES = {
			// FirstTrack, SectorsPerTrack, SectorsInZone, ReservedZoneSectors
			{ 1, 21, 357, 2},
			{18, 19, 133, 1},
			{25, 18, 108, 0},
			{31, 17,  85, 0}
	};
	
	private static final int BLOCKS_PER_ALLOC_UNIT = 8;

	/** Track number of first track (may be above one for sub directories on 1581 disks) */
	private static final int FIRST_TRACK = 1;
	/** Track number of last track plus one (may be below the physical end of disk for sub directories on 1581 disks) */
	private static final int LAST_TRACK = 70;

	public D71() {
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	@Override
	public int getMaxSectors(int trackNumber) {
		int result = TRACKS[trackNumber].getSectors();
		return result;
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

	/** {@inheritDoc} */
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

	/** {@inheritDoc} */
	protected void readImage(String filename) throws CbmException {
		bam = new CbmBam(TRACKS.length, 4);
		readImage(filename, D71_SIZE, "D71");
	}

	/** {@inheritDoc} */
	public void readBAM() {
		int bamOffset1 = getSectorOffset(BAM_TRACK_1, BAM_SECT);
		int bamOffset2 = getSectorOffset(BAM_TRACK_2, BAM_SECT);
		bam.setDiskName("");
		bam.setDiskId("");
		bam.setDiskDosType( (byte) getCbmDiskValue(bamOffset1 + 2 ));
		for (byte track = 1; track <= TRACKS.length; track++) {
			if (track <= 35) {
				bam.setFreeSectors(track, (byte) getCbmDiskValue(bamOffset1 + 0x04 + (track-1) * 4));
				for (int i = 1; i < 4; i++) {
					bam.setTrackBits(track, i, (byte) getCbmDiskValue(bamOffset1 + 0x04 + (track-1) * 4 + i));
				}
			} else {
				bam.setFreeSectors(track, (byte) getCbmDiskValue(bamOffset1 + 0xdd + (track-1-35) * 1));
				for (int i = 0; i < 3; i++) {
					bam.setTrackBits(track, i+1, (byte) getCbmDiskValue(bamOffset2 + (track-36) * 3 + i));
				}				
			}
		}
		for (int i = 0; i < DISK_NAME_LENGTH; i++) {
			bam.setDiskName(bam.getDiskName() + new Character((char)(PETSCII_TABLE[getCbmDiskValue(bamOffset1 + 0x90 + i)])));
		}
		for (int i = 0; i < DISK_ID_LENGTH; i++) {
			bam.setDiskId(bam.getDiskId() + new Character((char)(PETSCII_TABLE[getCbmDiskValue(bamOffset1 + 0xa2 + i)])));
		}
		checkImageFormat();
	}

	private void readCpmDirectory() {
		if (!isCpmImage()) {
			return;
		}
		final int C128_DS_DIR_TRACK = 1;
		final int[] C128_DS_DIR_SECTORS = { 10, 15, 20, 4, 9, 14, 19, 3, 8, 13, 18, 2, 7, 12, 17, 1 };
		int filenumber = 0;
		CpmFile entry = null;
		for (int s=0; s<C128_DS_DIR_SECTORS.length; s++) {
			int pos = TRACKS[C128_DS_DIR_TRACK].getOffset() + C128_DS_DIR_SECTORS[s] * BLOCK_SIZE;
			for (int i=0; i < DIR_ENTRIES_PER_SECTOR; i++) {
				CpmFile newFile = getCpmFile(entry, pos + (i * DIR_ENTRY_SIZE), false);
				if (newFile != null) {
					cbmFile[filenumber++] = newFile;
					entry = newFile;
				}
			}
		}
		fileNumberMax = filenumber;
	}
	
	@Override
	public void readDirectory() {
		if (isCpmImage()) {
			readCpmDirectory();
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

	/**
	 * Get track/sector from CP/M sector number.
	 * @param num  CP/M sector number
	 * @return TrackSector of specified CP/M sector 
	 */
	private TrackSector getCpmTrackSector(int num) {
		int upperHalf = 0;
		int trk = 0;
		int sec = 0;
		if (num >= CPM_BLOCK_COUNT) {
			num = num - CPM_BLOCK_COUNT;
			upperHalf = 35;
			if (num > CPM_BLOCK_COUNT) {
				return null;
			}
		}
		for (int i=0; i<4; i++) {
			num += CPM_ZONES[i][3]; 
			if (num < CPM_ZONES[i][2]) {
				trk = CPM_ZONES[i][0] + num / CPM_ZONES[i][1];
				sec = (CPM_SECTOR_SKEW * num) % CPM_ZONES[i][1];
				return new TrackSector(trk + upperHalf, sec);
			}
			num -= CPM_ZONES[i][2];
		}
		return new TrackSector(trk + upperHalf, sec);
	}

	@Override
	public byte[] getFileData(int number) throws CbmException {
		if (cbmDisk == null) {
			throw new CbmException("getFileData: No disk data exist.");
		} else if (number >= cbmFile.length) {
			throw new CbmException("getFileData: File number " + number + " does not exist.");
		} else if (isCpmImage()) { 
			feedbackMessage.append("getFileData: CP/M mode.\n");
			if (cbmFile[number] instanceof CpmFile) {
				CpmFile cpm = (CpmFile)cbmFile[number];
				byte[] data = new byte[ cpm.getRecordCount() * CPM_RECORD_SIZE ];
				int dstPos = 0;
				for (Integer au : cpm.getAllocList()) {
					for (int r=0; r < BLOCKS_PER_ALLOC_UNIT; r++) {
						TrackSector ts = getCpmTrackSector(au * BLOCKS_PER_ALLOC_UNIT + r);
						int srcPos = TRACKS[ts.getTrack()].getOffset() + ts.getSector() * BLOCK_SIZE;
						for (int c=0; c < BLOCK_SIZE; c++) {
							if (dstPos < data.length) {
								data[dstPos++] = cbmDisk[srcPos + c];
							} else {
								break;
							}
						}
					}
				}
				return data;
			} else {
				throw new CbmException("Not yet implemented for CP/M format.");
			}
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

	/** {@inheritDoc} */
	protected TrackSector saveFileData(byte[] saveData) {		
		if (isCpmImage()) {
			feedbackMessage.append("Not yet implemented for CP/M format.\n");
			return null;
		}
		int usedBlocks = 0;
		int dataRemain = saveData.length;
		feedbackMessage.append("SaveFileData: ").append(dataRemain).append(" bytes of data.\n");
		TrackSector firstBlock = findFirstCopyBlock();
		if (firstBlock != null) {
			TrackSector block = new TrackSector(firstBlock.track, firstBlock.sector);
			int thisTrack;
			int thisSector;
			int dataPos = 0;	
			while (dataRemain >= 0 && block != null) {
				feedbackMessage.append(dataRemain).append(" bytes remain: block ").append(block.track).append("/").append(block.sector).append("\n");
				thisTrack = block.track;
				thisSector = block.sector;
				markSectorUsed(thisTrack, thisSector);
				if (dataRemain >= (BLOCK_SIZE - 2)) {
					block = findNextCopyBlock(block);
					if (block != null) {
						fillSector(thisTrack, thisSector, dataPos, block.track, block.sector, saveData);
						usedBlocks++;
						dataRemain = dataRemain - (BLOCK_SIZE - 2);
						dataPos = dataPos + (BLOCK_SIZE - 2);
					} else {
						feedbackMessage.append("\nsaveFileData: Error: Not enough free sectors on disk. Disk is full.\n");
						firstBlock = null;
					}
				} else {
					fillSector(thisTrack, thisSector, dataPos, 0, (dataRemain + 1), saveData);
					usedBlocks++;
					dataRemain = -1;
				}
			}
			if (dataRemain <= 0) {
				feedbackMessage.append("All data written ("+usedBlocks+" blocks).\n");
			}
		} else {
			feedbackMessage.append("\nsaveFileData: Error: No free sectors on disk. Disk is full.\n");
			return null;
		}
		return firstBlock;
	}
	
	/**
	 * Fill sector in image with data. Pad with zeroes if saveData is smaller than BLOCK_SIZE - 2.
	 * @param track track number
	 * @param sector sector number
	 * @param dataPosition start filling sector at this position in saveData
	 * @param nextTrack next track
	 * @param nextSector next sector
	 * @param saveData data to fill sector with
	 */
	private void fillSector(int track, int sector, int dataPosition, int nextTrack, int nextSector, byte[] saveData) {
		//feedbackMessage.append("fillSector: block ").append(track).append("/").append(sector).append(" position=0x")
		//	.append(Integer.toHexString(dataPosition)).append(" next block ").append(nextTrack).append("/").append(nextSector).append("\n");
		setCbmDiskValue(getSectorOffset(track, sector) + 0x00, nextTrack);
		setCbmDiskValue(getSectorOffset(track, sector) + 0x01, nextSector);
		for (int position = 0; position < (BLOCK_SIZE - 2); position++) {
			int value = saveData.length > dataPosition + position ? saveData[dataPosition + position] & 0xff : 0;
			setCbmDiskValue(getSectorOffset(track, sector) + 0x02 + position, value);			
		}
	}
	
	/**
	 * Mark a sector in BAM as used.
	 * @param track trackNumber
	 * @param sector sectorNumber
	 */
	private void markSectorUsed(int track, int sector) {
		//feedbackMessage.append("markBAMused( track=").append(trackNumber).append(" sector=").append(sectorNumber).append(")\n");
		int trackPos;
		int freePos;
		if (track <= 35) {
			trackPos = getSectorOffset(BAM_TRACK_1, BAM_SECT) + 4 * (track);
			freePos = trackPos;
		} else {
			trackPos = getSectorOffset(BAM_TRACK_2, BAM_SECT) + 3 * (track - 36);
			freePos = getSectorOffset(BAM_TRACK_1, BAM_SECT) + 0xdd + (track - 36);
		}
		int pos = (sector / 8) + (track<=35 ? 1 : 0);
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) & INVERTED_BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(freePos, getCbmDiskValue(freePos) - 1);
	}
	
	/**
	 * Mark a sector in BAM as free.
	 * @param track trackNumber
	 * @param sector sectorNumber
	 */
	private void markSectorFree(int track, int sector) {
		//feedbackMessage.append("markSectorFree: track=").append(trackNumber).append(" sector=").append(sectorNumber).append(")\n");
		int trackPos;
		int freePos;
		if (track <= 35) {
			trackPos = getSectorOffset(BAM_TRACK_1, BAM_SECT) + 4 * (track);
			freePos = trackPos;
		} else {
			trackPos = getSectorOffset(BAM_TRACK_2, BAM_SECT) + 3 * (track - 36);
			freePos = getSectorOffset(BAM_TRACK_1, BAM_SECT) + 0xdd + (track - 36);
		}
		int pos = (sector / 8) + (track<=35 ? 1 : 0);
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) | BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(freePos, getCbmDiskValue(freePos) + 1);
	}

	/**
	 * Determine if a sector is free<BR>
	 * @param Track_number the track number of sector to check
	 * @param Sector_number the sector number of sector to check
	 * @return when True, the sector is free; otherwise used
	 */
	private boolean isSectorFree(int track, int sector) {
		int trackPos;
		if (track <= 35) {
			trackPos = getSectorOffset(BAM_TRACK_1, BAM_SECT) + 4 * (track) + 1;
		} else {
			trackPos = getSectorOffset(BAM_TRACK_2, BAM_SECT) + 3 * (track - 36);
		}
		int pos = (sector / 8);		
		int value =  getCbmDiskValue(trackPos + pos) & BYTE_BIT_MASKS[sector & 0x07];
		return value != 0;
	}
	
	/**
	 * Find a sector for the first block of the file,
	 * @return track/sector or null if none is available.
	 */
	private TrackSector findFirstCopyBlock() {
		TrackSector block = new TrackSector(0, 0);
		if (geosFormat) {
			// GEOS formatted disk, so use the other routine, from track one upwards.
			block.track = 1;
			block.sector = 0;
			block = findNextCopyBlock(block);
		} else {
			boolean found = false;	// No free sector found yet
			int distance = 1;		// On a normal disk, start looking checking the tracks just besides the directory track.
			while (!found && distance < 128) {
				// Search until we find a track with free blocks or move too far from the directory track.
				block.track = BAM_TRACK_1 - distance;
				if (block.track >= FIRST_TRACK && block.track <= LAST_TRACK && block.track != BAM_TRACK_2) {
					// Track within disk limits
					found = isTrackFree(block.track);
				}
				if (!found){
					// Check the track above the directory track
					block.track = BAM_TRACK_1 + distance;
					if (block.track <= LAST_TRACK && block.track != BAM_TRACK_2) {
						// Track within disk limits
						found = isTrackFree(block.track);
					}
				}
				if (!found) {
					// Move one track further away from the directory track and try again.
					distance++;
				}
			}

			//// If the whole disk is full and we're allowed to use the directory track for file data then try there too.
			//if (!found && CopyToDirTrack) {
			//  block.track = DIR_TRACK;
			//  found = isTrackFree(block.track);
			//}

			if (found) {
				// Found a track with, at least one free sector, so search for a free sector in it.
				int maxSector = getMaximumSectors(block.track);		// Determine how many sectors there are on that track.
				block.sector = 0;									// Start off with sector zero.
				do {
					found = isSectorFree(block.track, block.sector);
					if (!found) {
						block.sector++;	// Try the next sector.
					}
				} while (!found && block.sector <= maxSector);	// Repeat until there is a free sector or run off the track.
			} else {
				// Disk full. No tracks with any free blocks.
				block = null;
			}
		}
		if (block != null) {
			feedbackMessage.append("firstCopyBlock: The first block will be ").append(block.track).append("/").append(block.sector).append(".\n");
		} else {
			feedbackMessage.append("firstCopyBlock: Error: Disk is full!\n");
		}
		return block;
	}
	
	/**
	 * Find a sector for the next block of the file, using variables Track and Sector<BR>
	 * globals needed: GEOSFormat, CopyToDirTrack<BR>
	 * globals written: track_, sector<BR>
	 * @return when True, a sector was found; otherwise no more sectors left
	 */
	private TrackSector findNextCopyBlock(TrackSector block) {
		boolean found = false;
		int curSector=0;
		int curTrack=0;
		if ((block.track == 0) || (block.track > LAST_TRACK)) {
			// If we somehow already ran off the disk then there are no more free sectors left.
			found = false;
		} else {
			int tries = 3;			// Set the number of tries to three.
			found = false;			// We found no free sector yet.
			curTrack = block.track;		// Remember the current track number.
			while (!found && tries > 0) {
				// Keep trying until we find a free sector or run out of tries.
				if (isTrackFree(block.track)) {
					
					// If there's, at least, one free sector on the track then get searching.
					if (block.track == curTrack || !geosFormat) {
						// If this is a non-GEOS disk or we're still on the same track of a GEOS-formatted disk then...
						block.sector = block.sector + C1571_INTERLEAVE;	// Move away an "interleave" number of sectors.
						if (geosFormat && block.track >= 25) {
							// Empirical GEOS optimization, get one sector backwards if over track 25.
							block.sector--;
						}
					} else {
						// For a different track of a GEOS-formatted disk, use sector skew.
						block.sector = ((block.track - curTrack) << 1 + 4 + C1571_INTERLEAVE);
					}
					int maxSector = getMaximumSectors(block.track);	// Get the number of sectors on the current track.					
					while (block.sector >= maxSector) {
						// If we ran off the track then correct the result.
						block.sector = (block.sector - maxSector) + 1;	// Subtract the number of sectors on the track.
						if (block.sector > 0 && !geosFormat) {
							// Empirical optimization, get one sector backwards if beyond sector zero.
							block.sector--;
						}
					}
					curSector = block.sector;	// Remember the first sector to be checked.
					do {
						found = isSectorFree(block.track, block.sector);
						if (!found) {
							block.sector++;	// Try next sector
						}
						if (block.sector >= maxSector) {
							block.sector = 0;	// Went off track, wrap around to sector 0.
						}
					} while (!found && block.sector != curSector);	// Continue until finding a free sector, or we are back on the curSector again.
					if (!found) {
						// According to the free sector counter in BAM, this track should have free sectors, but it didn't.
						// Try a different track. Obviously, this disk needs to be validated.
						feedbackMessage.append("Warning: Track ").append(block.track).append(" should have at least one free sector, but didn't.");
						if (block.track > FIRST_TRACK && block.track <= BAM_TRACK_1) {
							block.track = block.track - 1 ;
						} else if (block.track < LAST_TRACK && block.track > BAM_TRACK_1) {
							block.track = block.track + 1 ;
							if (block.track == BAM_TRACK_2) {
								block.track = block.track + 1 ;
							}
						} else {
							tries--;
						}					
					}
				} else {
					// if IsTrackFree(track) = FALSE then
					//if GEOSFormat then {	//If the current track is used up completely then...
					//	Inc(track);	//Move one track upwards on a GEOS-formatted disk.
					//	if (track = DirTrack) or (track = DirTrack2) then Inc(track_);	//Skip the directory tracks on the way.
					//	if track = LastTrack then Tries = 0;	//If we ran off the disk then there are no more tries.
					//} else {
					if (block.track == DIR_TRACK) {
						// If we already tried the directory track then there are no more tries.
						tries = 0;
					} else {
						if (block.track < DIR_TRACK) {
							block.track --;	//If we're below the directory track then move one track downwards.
							if (block.track < FIRST_TRACK) {
								block.track = (DIR_TRACK + 1); //If we ran off the disk then step back to the track just above the directory track and zero the sector number.
								block.sector = 0;
								//If there are no tracks available above the directory track then there are no tries left; otherwise just decrease the number of tries.
								if (block.track <= LAST_TRACK) {
									tries--; 
								} else { 
									tries = 0; 
								}
							}
						} else {	//if track >= DirTrack then.
							block.track++;	//If we're above the directory track then move one track upwards.
							if (block.track == BAM_TRACK_2) {
								block.track++;
							}
							// if (track_ = dirTrack2) track_++;	//Skip the secondary directory track on the way.
							if (block.track > LAST_TRACK) {
								block.track = (DIR_TRACK - 1);	//If we ran off the disk then step back to the track just below the directory track and zero the sector number.
								block.sector = 0;
								//If there are no tracks available below the directory track then there are no tries left; otherwise just decrease the number of tries.
								if (block.track >= FIRST_TRACK) {
									tries--;
								} else {
									tries = 0;
								}
							}
						} //track_ < DirTrack}
					} //track_ = DirTrack}
					//} //if GEOSFormat then {}
				} //if IsTrackFree(track_) then {}

				/*
			  if not Found and (Tries = 0) and (track_ <> DirTrack) and CopyToDirTrack then {
			   //If we haven't found any free sector, ran out of tries and haven't tried the directory track yet,
				although it's declared as available for file data, then give the directory track an extra try}
				track_ = DirTrack;
				Inc(Tries);
			  };
				 */
			} //while not Found and (Tries > 0) do {}
		} //if not (track_ = 0) or (track_ >= MaxTrack) then {}
		//feedbackMessage.append("findNextCopyBlock: next block ").append(track).append("/").append(sector).append(".\n");
		return found ? block : null;
	}
	
	
	/**
	 * Determine the number of sectors (or the highest valid sector number plus one) for a track<BR>
	 * @param trackNumber track_number
	 * @return the number of sectors on the track
	 */
	private int getMaximumSectors(int trackNumber) {
		int result = TRACKS[trackNumber].getSectors();
		return result;
	}
	
	
	@Override
	protected void setDiskName(String newDiskName, String newDiskID) {
		feedbackMessage.append("setDiskName('").append(newDiskName).append("', '").append(newDiskID).append("')\n");
		for (int i=0; i < DISK_NAME_LENGTH; i++) {
			if (i < newDiskName.length()) {
				setCbmDiskValue( TRACKS[BAM_TRACK_1].getOffset() + (BLOCK_SIZE*BAM_SECT) + 144 + i,     newDiskName.charAt(i));
			} else {
				setCbmDiskValue( TRACKS[BAM_TRACK_1].getOffset() + (BLOCK_SIZE*BAM_SECT) + 144 + i,     BLANK);
			}
		}
		for (int i=0; i < DISK_ID_LENGTH; i++) {
			if (i < newDiskID.length()) {
				setCbmDiskValue( TRACKS[BAM_TRACK_1].getOffset() + (BLOCK_SIZE*BAM_SECT) + 162 +i,      newDiskID.charAt(i));
			} else {
				setCbmDiskValue( TRACKS[BAM_TRACK_1].getOffset() + (BLOCK_SIZE*BAM_SECT) + 162 +i,      BLANK);
			}
		}
	}

	@Override
	protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber) {
		int thisTrack = DIR_TRACK;
		int thisSector = 1;
		feedbackMessage.append("writeDirectoryEntry: bufferCbmFile to dirEntryNumber ").append(dirEntryNumber).append(".\n");
		if (dirEntryNumber > 7) {
			while (dirEntryNumber > 7) {
				thisTrack  = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00);
				thisSector = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01);
				feedbackMessage.append("LongDirectory: "+dirEntryNumber+" dirEntrys remain, next block: "+thisTrack+"/"+thisSector+"\n");
				dirEntryNumber = dirEntryNumber - 8;
			}
		}
		int pos = getSectorOffset(thisTrack, thisSector) + (dirEntryNumber * 32);
		setCbmDiskValue(pos + 0, cbmFile.getDirTrack());
		setCbmDiskValue(pos + 1, cbmFile.getDirSector());
		writeSingleDirectoryEntry(cbmFile, pos);
	}

	@Override
	public boolean saveNewImage(String filename, String newDiskName, String newDiskID) {
        final int[] newD71Bam1Data = { /* block 18/0 */
                0x12, 0x01, 0x41, 0x80, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, //00016500
                0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, //00016510
                0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, //00016520  
                0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, //00016530
                0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x11, 0xfc, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07, //00016540
                0x13, 0xff, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07, //00016550
                0x13, 0xff, 0xff, 0x07, 0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03, //00016560
                0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03, 0x11, 0xff, 0xff, 0x01, //00016570
                0x11, 0xff, 0xff, 0x01, 0x11, 0xff, 0xff, 0x01, 0x11, 0xff, 0xff, 0x01, 0x11, 0xff, 0xff, 0x01, //00016580
                0x31, 0x32, 0x33, 0x34, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, //00016590
                0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0x00, 0x00, 0x00, 0x00, 0x00, //000165a0
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //000165b0
                0x00, 0x00, 0x00, 0x00, 0x00 ,0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, //000165c0
                0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x15, 0x15, 0x15, //000165d0
                0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x15, 0x00, 0x13, //000165e0
                0x13, 0x13, 0x13, 0x13, 0x13, 0x12, 0x12, 0x12, 0x12, 0x12, 0x12, 0x11, 0x11, 0x11, 0x11, 0x11, //000165f0
                0x00, 0xff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00  //00016600
		};
        final int[] newD71Bam2Data = { /* block 53/0 */
        	  0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff,   //00041000
        	  0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff,   //00041010
        	  0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f, 0xff, 0xff, 0x1f,   //00041020
        	  0xff, 0xff, 0x1f, 0x00, 0x00, 0x00, 0xff, 0xff, 0x07, 0xff, 0xff, 0x07, 0xff, 0xff, 0x07, 0xff,   //00041030
        	  0xff, 0x07, 0xff, 0xff, 0x07, 0xff, 0xff, 0x07, 0xff, 0xff, 0x03, 0xff, 0xff, 0x03, 0xff, 0xff,   //00041040
        	  0x03, 0xff, 0xff, 0x03, 0xff, 0xff, 0x03, 0xff, 0xff, 0x03, 0xff, 0xff, 0x01, 0xff, 0xff, 0x01,   //00041050
        	  0xff, 0xff, 0x01, 0xff, 0xff, 0x01, 0xff, 0xff, 0x01, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,   //00041060
        };

		cbmDisk = new byte[D71_SIZE];
		Arrays.fill(cbmDisk, (byte) 0);
		for (int i = 0; i < newD71Bam1Data.length; i++) {
			setCbmDiskValue(0x16500 + i, newD71Bam1Data[i]);
		}
		for (int i = 0; i < newD71Bam2Data.length; i++) {
			setCbmDiskValue(0x41000 + i, newD71Bam2Data[i]);
		}		
		
		setDiskName(cbmFileName(newDiskName), cbmFileName(newDiskID));
		return writeImage(filename);
	}

	@Override
	public boolean addDirectoryEntry(CbmFile cbmFile, int fileTrack, int fileSector, boolean isCopyFile, int lengthInBytes){		
		feedbackMessage.append("addDirectoryEntry: \"").append(cbmFile.getName()).append("\", ").append(FILE_TYPES[cbmFile.getFileType()]).append(", ").append(fileTrack).append("/").append(fileSector).append("\n");
		if (isCpmImage()) {
			feedbackMessage.append("Not yet implemented for CP/M format.\n");
			return false;
		}
		if (isCopyFile) {
			// This a substitute for setNewDirectoryEntry(thisFilename, thisFiletype, destTrack, destSector, dirPosition)
			// since we do not need to set other values than destTrack and destSector when copying a file. 
			cbmFile.setTrack(fileTrack);
			cbmFile.setSector(fileSector);
		} else {
			setNewDirEntry(cbmFile, cbmFile.getName(), cbmFile.getFileType(), fileTrack, fileSector, lengthInBytes);
		}
		cbmFile.setDirTrack(0);
		cbmFile.setDirSector(-1);
		int dirEntryNumber = findFreeDirEntry();
		if (dirEntryNumber != -1 && setNewDirLocation(cbmFile, dirEntryNumber)) {
			writeSingleDirectoryEntry(cbmFile, getDirectoryEntryPosition(dirEntryNumber));
			fileNumberMax++;	// increase the maximum file numbers
			return true;
		} else {
			feedbackMessage.append("Error: Could not find a free sector on track "+DIR_TRACK+" for new directory entries.\n");
			return false;
		}		
	}

	/**
	 * Copy attributes of bufferCbmFile to a location in cbmDisk.<BR>
	 * globals needed: bufferCbmFile<BR>
	 * globals written: cbmDisk<BR>
	 * @param where data position where to write to cbmDisk
	 */
	private void writeSingleDirectoryEntry(CbmFile cbmFile, int where){
		if (isCpmImage()) {
			feedbackMessage.append("Not yet implemented for CP/M format.\n");
			return ;
		}
		feedbackMessage.append("writeSingleDirectoryEntry: dirpos="+cbmFile.getDirPosition()+"\n");
		// file attributes
		setCbmDiskValue(where + 2, 0);
		if (cbmFile.isFileScratched() == false){
			setCbmDiskValue(where + 2, cbmFile.getFileType());
			if (cbmFile.isFileLocked()){
				setCbmDiskValue(where + 2, (getCbmDiskValue(where + 2) | 64));
			}
			if (cbmFile.isFileClosed()){
				setCbmDiskValue(where + 2, (getCbmDiskValue(where + 2) | 128));
			}
		}
		//file track / sector (where to start reading)
		setCbmDiskValue(where + 3, cbmFile.getTrack());
		setCbmDiskValue(where + 4, cbmFile.getSector());
		// FileName		
		for (int position = 0; position <= 15; position++) {
			setCbmDiskValue(where + 5 + position, BLANK);
		}
		for (int position = 0; position <= cbmFile.getName().length()-1; position++) {
			setCbmDiskValue(where + 5 + position, cbmFile.getName().charAt(position));
		}
		// relative Track/Sector
		setCbmDiskValue(where + 21, cbmFile.getRelTrack());
		setCbmDiskValue(where + 22, cbmFile.getRelSector());
		// GEOS
		for (int position = 0; position <= 5; position++) {
			setCbmDiskValue(where + 23 + position, cbmFile.getGeos(position) );
		}
		// Size
		setCbmDiskValue(where + 30, cbmFile.getSizeInBlocks() );
		setCbmDiskValue(where + 31, cbmFile.getSizeInBlocks() / BLOCK_SIZE );
	}
	
	/**
	 * Find offset to a directory entry.
	 * @param dirEntryNumber directory entry number to look up
	 * @return offset in image to directory entry, or -1 if dirEntry is not available.
	 */
	private int getDirectoryEntryPosition(int dirEntryNumber) {
		if (dirEntryNumber < 0 || dirEntryNumber >= FILE_NUMBER_LIMIT) {
			return -1;
		}
		int track = DIR_TRACK;
		int sector = 1;
		int entryPosCount = 8;
		while (dirEntryNumber >= entryPosCount && track != 0) {
			track = getCbmDiskValue(getSectorOffset(track, sector) + 0x00);
			sector = getCbmDiskValue(getSectorOffset(track, sector) + 0x01);
			entryPosCount += 8;			
		}
		if (track == 0) {
			return -1;
		} else {
			return getSectorOffset(track, sector) + (dirEntryNumber & 0x07) * 32;
		}
	}
	
	/**
	 * Iterate directory sectors to find the specified directory entry. If needed, attempt to allocate more directory sectors
	 * and continue iterating until either directory entry is available or FILE_NUMBER_LIMIT is reached,
	 * globals written: bufferCbmFile<BR>
	 * @param dirEntryNumber position where to put this entry in the directory
	 * @return returns true if a free directory block was found
	 */
	private boolean setNewDirLocation(CbmFile cbmFile, int dirEntryNumber){
		if (dirEntryNumber < 0 || dirEntryNumber >= FILE_NUMBER_LIMIT) {
			feedbackMessage.append( "Error: Invalid directory entry number ").append(dirEntryNumber).append(" at setNewDirectoryLocation.\n");
			return false;
		} else if ( (dirEntryNumber & 0x07) != 0) {	
			// If this is not the eighth entry we are lucky and do not need to do anything...
			cbmFile.setDirTrack(0);
			cbmFile.setDirSector(0);
			return true;
		} else {
			//find the correct entry where to write new values for dirTrack and dirSector
			int thisTrack = DIR_TRACK;
			int thisSector = 1;
			int entryPosCount = 8;
			while (dirEntryNumber >= entryPosCount) {
				int nextTrack = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00);
				int nextSector = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01);				
				if (nextTrack == 0) {
					nextTrack = thisTrack;
					final int[]  dirSectors = { 
							1, 4, 7, 10, 13, 16,
							2, 5, 8, 11, 14, 17,
							3, 6, 9, 12, 15, 18 };				
					boolean found = false;
					for (int i=0; !found && i<dirSectors.length; i++ ) {
						nextSector = dirSectors[i];
						found = isSectorFree(nextTrack, nextSector);
					}
					if (found) {
						nextTrack = thisTrack;
						markSectorUsed(nextTrack, nextSector);
						setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00, nextTrack);
						setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01, nextSector);	
						setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x00, 0);
						setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x01, -1);
						feedbackMessage.append("Allocated additonal directory sector (").append(nextTrack).append("/").append(nextSector).append(") for dir entry ").append(dirEntryNumber).append(".\n");						
					} else {
						feedbackMessage.append( "Error: no more directory sectors. Can't add file.\n");
						return false;
					}
				}
				thisTrack = nextTrack;
				thisSector = nextSector;
				entryPosCount += 8;
			}	
			return true;
		}
	}
	
	/**
	 * Find first free directory entry.
	 * Looks through the allocated directory sectors.
	 * @return number of next free directory entry, or -1 if none is free.
	 */
	private int findFreeDirEntry() {
		int track = DIR_TRACK;
		int sector = 1;
		int dirPosition = 0;
		do {
			int dataPosition = TRACKS[track].getOffset() + (BLOCK_SIZE * sector);
			for (int i = 0; i < DIR_ENTRIES_PER_SECTOR; i++) {
				int fileType = cbmDisk[dataPosition + (i * DIR_ENTRY_SIZE) + 0x02] & 0xff; 
				if (fileType  == 0) {
					// Free or scratched entry
					return dirPosition;					
				}
				dirPosition++;
			}
			track = getCbmDiskValue( dataPosition + 0);
			sector = getCbmDiskValue( dataPosition + 1);
		} while (track != 0);
		if (dirPosition < FILE_NUMBER_LIMIT + 2) {
			// next entry, on a new dir sector. not yet hit max number of entries.
			return dirPosition; 
		} else {
			// Hit max number of file entries. can't add more.
			feedbackMessage.append("Error: No free directory entry avaiable.\n");
			return -1;
		}
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
						if (trk == BAM_TRACK_1 || trk == BAM_TRACK_2) {
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
		feedbackMessage = new StringBuffer();
		if (isCpmImage()) {
			throw new CbmException("Delete not yet implemented for CP/M format.");
		}

		cbmFile.setFileType(0);
		cbmFile.setFileScratched(true);
		int track = DIR_TRACK;
		int sector = 1;
		int dirEntryNumber = cbmFile.getDirPosition();
		int dirEntryPos = getDirectoryEntryPosition(dirEntryNumber);
		if (dirEntryPos != -1) {
			//feedbackMessage.append("Delete: [").append(dirEntryNumber).append("] ").append(cbmFile.getName());
			setCbmDiskValue(dirEntryPos + 0x02, 0);
			// Free used blocks		
			track = cbmFile.getTrack();
			sector = cbmFile.getSector();
			while (track != 0) {
				int tmpTrack = getCbmDiskValue( TRACKS[track].getOffset() + (BLOCK_SIZE * sector) + 0 );
				int tmpSector = getCbmDiskValue( TRACKS[track].getOffset() + (BLOCK_SIZE * sector) + 1 );
				markSectorFree(track, sector);
				track = tmpTrack;
				sector = tmpSector;
			}
		} else {
			feedbackMessage.append("Error: Failed to delete ").append(cbmFile.getName());
		}		
	}

	@Override
	public void readPartition(int track, int sector, int numBlocks) throws CbmException {
		throw new CbmException("D71 images does not support partitions.");
	}

	@Override
	public Integer validate(List<Integer> repairList) {
		
		// init to null
		Boolean[][] bamEntry = new Boolean[getTrackCount() + 1][getMaxSectorCount()];
		for (int trk = 0; trk < bamEntry.length; trk++) {
			for (int sec = 0; sec < bamEntry[trk].length; sec++) {
				bamEntry[trk][sec] =  null;
			}
		}	

		// read all the chains of BAM/directory blocks. Mark each block as used and also check that
		// the block is not already marked as used. It would mean a block is referred to twice.
		
		// first check the chain of directory blocks.
		
		int track = BAM_TRACK_1;
		int sector = BAM_SECT;
		int errorCount = 0;
		int warningCount = 0;
		List<TrackSector> dirErrorList = new ArrayList<TrackSector>();
		do {
			if (errorCount > 1000) {
				validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_TOO_MANY));
				break;
			} else if (track >= bamEntry.length || sector >= bamEntry[track].length) {
				validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_DIR_SECTOR_OUTSIDE_IMAGE));
				errorCount++;
				break;
			} else if (bamEntry[track][sector] == null) {
				bamEntry[track][sector] = Boolean.FALSE;
				//System.out.println("OK: dir sector "+track+"/"+sector+" marked as used");
			} else {
				errorCount++;				
				// TODO: if here, save track/sector in a list, and use it to see if error has already 
				// been seen for this track/sector. If so, then we are in a loop, and should break out of loop.
				
				TrackSector thisBlock = new TrackSector(track, sector);
				if (dirErrorList.contains(thisBlock)) {
					validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_DIR_SECTOR_ALREADY_SEEN));
					break;
				} else {
					dirErrorList.add(thisBlock);
				}

				if (bamEntry[track][sector].equals(Boolean.FALSE)) {
					validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_DIR_SECTOR_ALREADY_USED));
				} else {
					validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_DIR_SECTOR_ALREADY_FREE));
				}
			}
			int tmpTrack = track;
			int tmpSector = sector;
			track = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x00);
			sector = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x01);
		} while (track != 0);
		
		// follow each file and check data blocks
		for (int n=0; n < cbmFile.length; n++) {
			if (cbmFile[n].getFileType() == TYPE_DEL) {
				continue;
			} else if (cbmFile[n].getFileType() == TYPE_CBM) {
				validationErrorList.add(new ValidationError(track, sector, ValidationError.ERROR_PARTITIONS_UNSUPPORTED, cbmFile[n].getName()));
				errorCount++;
				continue;
			}
			List<TrackSector> fileErrorList = new ArrayList<TrackSector>();
			track = cbmFile[n].getTrack();
			sector = cbmFile[n].getSector();
			if (track != 0) {
				do {
					if (errorCount > 1000) {
						validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_TOO_MANY));
						break;
					} else if (track >= bamEntry.length || sector >= bamEntry[track].length) {
						validationErrorList.add(new ValidationError(track, sector, ValidationError.ERROR_FILE_SECTOR_OUTSIDE_IMAGE, cbmFile[n].getName()));
						errorCount++;
						break;
					}
					if (bamEntry[track][sector] == null) {
						bamEntry[track][sector] = Boolean.FALSE;	// OK
					} else {
						errorCount++;
						// Detect cyclic references by keeping track of all sectors used by one file and check if a sector is already seen.
						TrackSector thisBlock = new TrackSector(track, sector);
						if (fileErrorList.contains(thisBlock)) {
							validationErrorList.add(new ValidationError(track, sector, ValidationError.ERROR_FILE_SECTOR_ALREADY_SEEN, cbmFile[n].getName()));
							break;
						} else {
							fileErrorList.add(thisBlock);
						}
						if (bamEntry[track][sector].equals(Boolean.FALSE)) {
							validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_FILE_SECTOR_ALREADY_USED));							
						} else {
							validationErrorList.add(new ValidationError(track,sector, ValidationError.ERROR_FILE_SECTOR_ALREADY_FREE));
						}
					}
					int tmpTrack = track;
					int tmpSector = sector;
					track = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x00);
					sector = getCbmDiskValue(getSectorOffset(tmpTrack, tmpSector) + 0x01);
				} while (track != 0);
			}
		}
		
		// iterate BAM and verify used blocks is matching what we got when following data chains above.
		for (int trk = 1; trk <= getTrackCount(); trk++) {
			for (int sec = 0; sec < getMaxSectors(trk); sec++) {
				Boolean bamFree = new Boolean(isSectorFree(trk,sec));
				Boolean fileFree = bamEntry[trk][sec];
				if (fileFree == null && bamFree || bamFree.equals(fileFree)) {
					//System.out.println("OK: BAM sector "+trk+"/"+sec+" marked as used");
				} else if (Boolean.FALSE.equals(fileFree) && !Boolean.FALSE.equals(bamFree)) {
					validationErrorList.add(new ValidationError(trk,sec, ValidationError.ERROR_USED_SECTOR_IS_FREE));							
					errorCount++;
				} else if (trk != BAM_TRACK_1 && trk != BAM_TRACK_2){
					validationErrorList.add(new ValidationError(trk,sec, ValidationError.ERROR_UNUSED_SECTOR_IS_ALLOCATED));							
					warningCount++;
				}
			}
		}
		errors = new Integer(errorCount);
		warnings = new Integer(warningCount);
		return errors;
	}

}
