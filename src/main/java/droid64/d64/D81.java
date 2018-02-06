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
public class D81 extends DiskImage {

	/** Max number of directory entries in image : (40 - 3) * 8 = 296 */
	private static final int FILE_NUMBER_LIMIT = 296;
	/** The normal size of a D81 image (80 * 40  * 256) */
	private final static int D81_SIZE = 819200;
	/** Number of sectors per track (40) */
	private final static int TRACK_SECTORS	= 40;
	/** Number of tracks (80) of image */
	private final static int TRACK_COUNT	= 80;
	/** Track of disk header block */
	protected final static int HEADER_TRACK	= 40;
	/** Sector of disk header block */
	protected final static int HEADER_SECT	= 0;	
	/** Track of BAM block 1 and BAM block 2 */
	private final static int BAM_TRACK	    = 40;
	/** Sector of BAM block 1 (40/1) */
	private final static int BAM_SECT_1	    = 1;	
	/** Sector of BAM block 2 (40/2) */
	private final static int BAM_SECT_2	    = 2;	
	/** Track of first directory block */
	private final static int DIR_TRACK		= 40;
	/** Sector of first directory block (40/3) */
	private final static int DIR_SECT		= 3;
	/** 1 byte for free sectors on track, and one bit per sector (5 bytes / 40 bits) */
	private final static int BYTES_PER_BAM_TRACK = 6;
	/** Blocks per CP/M allocation unit (8 * 256 = 2048). */
	private static final int BLOCKS_PER_ALLOC_UNIT = 8;
	/** Track number of first track (may be above one for sub directories on 1581 disks) */
	private static final int FIRST_TRACK    = 1;
	/** Track number of last track plus one (may be below the physical end of disk for sub directories on 1581 disks) */
	private static final int LAST_TRACK     = 80;
	
	/** Constructor */
	public D81() {
		initCbmFile(FILE_NUMBER_LIMIT);
	}

	/** {@inheritDoc} */
	public int getMaxSectors(int trackNumber) {
		return TRACK_SECTORS;
	}

	/** {@inheritDoc} */
	public byte[] getBlock(int track, int sector) throws CbmException {
		if (track > TRACK_COUNT || track < 1) {
			throw new CbmException("Track "+track+" is not valid.");
		} else if (sector >= TRACK_SECTORS) {
			throw new CbmException("Sector "+sector+" is not valid.");			
		} else {
			int pos = ((track - 1)* TRACK_SECTORS + sector) * BLOCK_SIZE;
			return Arrays.copyOfRange(cbmDisk, pos, pos + BLOCK_SIZE);
		}
	}

	/** {@inheritDoc} */
	public int getBlocksFree() {
		int blocksFree = 0;
		if (cbmDisk != null) {
			for (int track = 1; track <= TRACK_COUNT; track++) {
				if (track != DIR_TRACK) {
					blocksFree = blocksFree + bam.getFreeSectors(track);
				}
			}
		}
		return blocksFree;	
	}

	/** {@inheritDoc} */
	protected void readImage(String filename) throws CbmException {
		bam = new CbmBam(TRACK_COUNT, BYTES_PER_BAM_TRACK);
		readImage(filename, D81_SIZE, "D81");
	}

	/** {@inheritDoc} */
	public void readBAM() {
		int headerOffset = getSectorOffset(HEADER_TRACK, HEADER_SECT);
		int bamOffset1 = getSectorOffset(BAM_TRACK, BAM_SECT_1) + 0x10;		
		int bamOffset2 = getSectorOffset(BAM_TRACK, BAM_SECT_2) + 0x10;		
		bam.setDiskName("");
		bam.setDiskId("");
		bam.setDiskDosType( (byte) getCbmDiskValue(bamOffset1 + 2 ));
		for (int track = 1; track <= TRACK_COUNT; track ++) {
			int bamOffset = ((track-1) < 40 ? bamOffset1 : bamOffset2 ) + ((track-1) % 40) * BYTES_PER_BAM_TRACK;			
			bam.setFreeSectors(track, (byte) getCbmDiskValue(bamOffset));
			for (int cnt = 1; cnt < BYTES_PER_BAM_TRACK; cnt ++) {
				bam.setTrackBits(track, cnt, (byte) getCbmDiskValue(bamOffset + cnt));
			}
		}		
		for (int cnt = 0; cnt < DISK_NAME_LENGTH; cnt ++) {
			bam.setDiskName(bam.getDiskName() + 
					new Character((char)(PETSCII_TABLE[getCbmDiskValue(headerOffset + 0x04 + cnt )] )));
		}
		for (int cnt = 0; cnt < DISK_ID_LENGTH; cnt ++) {
			bam.setDiskId(bam.getDiskId() +
					new Character((char)( PETSCII_TABLE[getCbmDiskValue(headerOffset + 0x16 + cnt)] )));
		}
		checkImageFormat();
	}
	
	/** {@inheritDoc} */
	public String[][] getBamTable() {
		String[][] bamEntry = new String[TRACK_COUNT][TRACK_SECTORS + 1];
		for (int trk = 0; trk < TRACK_COUNT; trk++) {
			for (int sec = 0; sec <= TRACK_SECTORS; sec++) {
				bamEntry[trk][sec] =  CbmBam.INVALID;
			}
		}		
		for (int trk = 1; trk <= TRACK_COUNT; trk++) {
			int sector = 0;
			bamEntry[trk-1][sector++] = Integer.toString(trk);
			for (int cnt = 1; cnt < BYTES_PER_BAM_TRACK; cnt++) {
				for (int bit = 0; bit < 8; bit++) {
					if (sector <= TRACK_SECTORS) {
						if ((getBam().getTrackBits(trk, cnt) & DiskImage.BYTE_BIT_MASKS[bit]) == 0) {
							bamEntry[trk-1][sector++] = CbmBam.USED;
						} else {
							bamEntry[trk-1][sector++] = CbmBam.FREE;
						}
					}
				}
			}
		}
		return bamEntry;
	}
	
	/** Read CP/M directory structure */
	private void readCpmDirectory() {
		if (!isCpmImage()) {
			return;
		}
		final int C1581_DIR_TRACK = 1;
		final int C1581_DIR_SECTOR = 0;
		final int C1581_DIR_SECTOR_COUNT = 16;
		int filenumber = 0;
		CpmFile entry = null;
		for (int sec=0; sec < C1581_DIR_SECTOR_COUNT; sec++) {
			int idx = ((C1581_DIR_TRACK - 1) * TRACK_SECTORS + C1581_DIR_SECTOR + sec) * BLOCK_SIZE;			
			for (int i=0; i < DIR_ENTRIES_PER_SECTOR; i++) {
				CpmFile newFile = getCpmFile(entry, idx + i * DIR_ENTRY_SIZE, true);
				if (newFile != null) {
					cbmFile[filenumber++] = newFile;
					entry = newFile;
				}
			}
		}
		fileNumberMax = filenumber;
	}
	
	/** {@inheritDoc} */
	public void readDirectory() {
		if (isCpmImage()) {
			readCpmDirectory();
			return;
		}
		readDirectory(DIR_TRACK, DIR_SECT, false, 0);
	}
	
	/** Read normal Commodore directory structure */
	private void readDirectory(final int dirTrack, final int dirSector, boolean isPartition, int partitionSectorCount) {
		boolean fileLimitReached = false;
		int dirPosition = 0;
		int filenumber = 0;
		int track = dirTrack;
		int sector = dirSector;
		do {
			if (track >= TRACK_COUNT) {
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
		validate(dirTrack, dirSector, isPartition, partitionSectorCount);
	}

	/**
	 * Read D81 partition
	 * @param track
	 * @param sector
	 * @param numBlocks
	 */
	public void readPartition(int track, int sector, int numBlocks) {		
		String partName = getTrimmedString(getSectorOffset(track, sector) + 0x04, 16);
		String partId = getTrimmedString(getSectorOffset(track, sector) + 0x14, 5);
		int dirTrack = cbmDisk[ getSectorOffset(track, sector) + 0x00] & 0x0ff;
		int dirSector = cbmDisk[ getSectorOffset(track, sector) + 0x01] & 0x0ff;
		feedbackMessage.append("readPartition: ").append(dirTrack).append("/").append(dirSector).append(" '").append(partName).append("' ").append(partId).append("\n");		
		readDirectory(dirTrack, dirSector, true, numBlocks);		
	}
	
	/** {@inheritDoc} */
	public byte[] getFileData(int number) throws CbmException {
		if (cbmDisk == null) {
			throw new CbmException("getFileData: No disk data exist.");
		} else if (number >= cbmFile.length) {
			throw new CbmException("getFileData: File number " + number + " does not exist.");
		} else if (isCpmImage()) { 
			feedbackMessage.append("getFileData: CP/M mode.\n");
			if (cbmFile[number] instanceof CpmFile) {
				CpmFile cpm = (CpmFile)cbmFile[number];
				int dstPos = 0;
				byte[] data = new byte[ cpm.getRecordCount() * CPM_RECORD_SIZE ];
				for (Integer au : cpm.getAllocList()) {
					int srcPos;
					if (au< 195) {
						// (39 * TRACK_SECTORS * BLOCK_SIZE) / (BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE) = 195
						srcPos = au * BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE;
					} else {
						srcPos = (au * BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE) + (20*BLOCK_SIZE);
					}
					
					for (int j=0; j < BLOCKS_PER_ALLOC_UNIT * BLOCK_SIZE; j++) {
						if (dstPos < data.length) {
							data[dstPos++] = cbmDisk[srcPos + j];
						} else {
							break;
						}
					}
				}
				return data;
			} else {
				throw new CbmException("Unknown CP/M format.");
			}
		} else if (cbmFile[number].isFileScratched()) {
			throw new CbmException("getFileData: File number " + number + " is deleted.");			
		}
		feedbackMessage.append("getFileData: ").append(number).append(" '").append(cbmFile[number].getName()).append("'\n");
		feedbackMessage.append("Tracks / Sectors: ");
	
		int track = cbmFile[number].getTrack();
		int sector = cbmFile[number].getSector();
			
		ByteArrayOutputStream out = new ByteArrayOutputStream();
		do {
			if (track > TRACK_COUNT) {
				throw new CbmException("Track " + track + " outside of image.");
			}
			int nextTrack  = getCbmDiskValue(getSectorOffset(track, sector) + 0x00);
			int nextSector = getCbmDiskValue(getSectorOffset(track, sector) + 0x01);
			feedbackMessage.append(track).append("/").append(sector).append(" ");
			if (nextTrack > 0) {
				for (int position = 2; position < BLOCK_SIZE; position++) {
					out.write(cbmDisk[getSectorOffset(track, sector) + position]);					
				}
			} else {
				feedbackMessage.append("\nRemaining bytes: ").append(nextSector).append("\n");
				for (int position = 2; position <= nextSector; position++) {
					out.write(cbmDisk[getSectorOffset(track, sector) + position]);					
				}
			}
			track = nextTrack;
			sector = nextSector;
		} while (track != 0);	
		feedbackMessage.append("OK.\n");
		return out.toByteArray();
	}

	/** {@inheritDoc} */
	protected void setDiskName(String newDiskName, String newDiskID) {
		feedbackMessage.append("setDiskName('").append(newDiskName).append("', '").append(newDiskID).append("')\n");
		int hdrOffset = getSectorOffset(HEADER_TRACK, HEADER_SECT);		
		int bam1Offset = getSectorOffset(BAM_TRACK, BAM_SECT_1);		
		int bam2Offset = getSectorOffset(BAM_TRACK, BAM_SECT_2);		
		for (int i=0; i < DISK_NAME_LENGTH; i++) {
			if (i < newDiskName.length()) {
				setCbmDiskValue( hdrOffset + 0x04 + i, newDiskName.charAt(i));
			} else {
				setCbmDiskValue( hdrOffset + 0x04 + i, BLANK);				
			}
		}
		newDiskID += "\u0240\u0240\u0240\u0240\u0240";
		setCbmDiskValue(bam1Offset + 0x04, newDiskID.charAt(0));
		setCbmDiskValue(bam1Offset + 0x05, newDiskID.charAt(1));
		setCbmDiskValue(bam2Offset + 0x04, newDiskID.charAt(0));
		setCbmDiskValue(bam2Offset + 0x05, newDiskID.charAt(1));
		for (int i=0; i < DISK_ID_LENGTH; i++) {
			if (i < newDiskID.length()) {
				setCbmDiskValue( hdrOffset + 0x16 +i, newDiskID.charAt(i));
			} else {
				setCbmDiskValue( hdrOffset + 0x16 +i, BLANK);				
			}
		}
	}

	/** {@inheritDoc} */
	public boolean saveNewImage(String filename, String newDiskName, String newDiskID) {
		final byte[] EMPTY_BAM1 = {
				 0x28, 0x02, 0x44, (byte) 0xbb, 0x30, 0x30, (byte) 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x24, (byte) 0xf0, -0x1, -0x1, -0x1, -0x1
			};
			final byte[] EMPTY_BAM2 = {
				 0x00, -0x1, 0x44, (byte) 0xbb, 0x30, 0x30, (byte) 0xc0, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1,
				 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1,
				 -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1,
				 -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1, 0x28, -0x1, -0x1, -0x1, -0x1, -0x1		
			};		
		final int bamOffset1 = getSectorOffset(BAM_TRACK, BAM_SECT_1);
		final int bamOffset2 = getSectorOffset(BAM_TRACK, BAM_SECT_2);
		final int hdrOffset = getSectorOffset(HEADER_TRACK, HEADER_SECT);		
		final int dirOffset = getSectorOffset(DIR_TRACK, DIR_SECT);		
		cbmDisk = new byte[D81_SIZE];
		Arrays.fill(cbmDisk, (byte) 0);
		setCbmDiskValue( hdrOffset + 0x00,	40);
		setCbmDiskValue( hdrOffset + 0x01,	3);
		setCbmDiskValue( hdrOffset + 0x02,	0x44);
		setCbmDiskValue( hdrOffset + 0x18,	BLANK);				
		setCbmDiskValue( hdrOffset + 0x19,	0x32);				
		setCbmDiskValue( hdrOffset + 0x1a,	0x44);				
		setCbmDiskValue( hdrOffset + 0x1b,	BLANK);				
		setCbmDiskValue( hdrOffset + 0x1c,	BLANK);	

		setCbmDiskValue( dirOffset + 0x01,	-1);	// next sector on first dir sector
		
		for (int i=0; i<EMPTY_BAM1.length; i++) {
			cbmDisk[bamOffset1 + i] = EMPTY_BAM1[i];
		}
		for (int i=0; i<EMPTY_BAM2.length; i++) {
			cbmDisk[bamOffset2 + i] = EMPTY_BAM2[i];
		}
		setDiskName(cbmFileName(newDiskName), cbmFileName(newDiskID));
		return writeImage(filename);
	}
	
	/** {@inheritDoc} */
	public int getSectorOffset(int track, int sector) {
		return ((track - 1) * TRACK_SECTORS + sector) * BLOCK_SIZE;		
	}
	
	/** {@inheritDoc} */
	public int getTrackCount() {
		return TRACK_COUNT;
	}
	
	/** {@inheritDoc} */
	public int getMaxSectorCount() {
		return TRACK_SECTORS;
	}

	@Override
	public Integer validate(List<Integer> repairList) {
		return validate(DIR_TRACK, DIR_SECT, false, 0);
	}
	
	/**
	 * Validate disk image
	 * @param track directory track
	 * @param sector directory sector
	 * @param isPartition if i is a C1581 partition
	 * @param partitionSectorCount size of partition is if it a partition.
	 * @return number of validation errors
	 * @see validate(List<Integer>)
	 */
	public Integer validate(int track, int sector, boolean isPartition, int partitionSectorCount) {		
		feedbackMessage.append("validate: D81 dirSector ").append(track).append("/").append(sector).append(isPartition ? " partition " : " ");
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
		
		int bamTrack = track;
		
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
				// Detect cyclic references by keeping track of all sectors used by one file and check if a sector is already seen.
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
			}

			List<TrackSector> fileErrorList = new ArrayList<TrackSector>();
			track = cbmFile[n].getTrack();
			sector = cbmFile[n].getSector();

			if (cbmFile[n].getFileType() == TYPE_CBM) {
				int blocks = cbmFile[n].getSizeInBlocks();
				for (int i=0; i<blocks; i++) {					
					if (bamEntry[track][sector] == null) {	
						bamEntry[track][sector] = Boolean.FALSE;	// OK
					} else {
						validationErrorList.add(new ValidationError(track, sector, ValidationError.ERROR_FILE_SECTOR_ALREADY_SEEN, cbmFile[n].getName()));
						errorCount++;						
					}
					if (sector < 39) {
						sector++;
					} else {
						track++;
						sector = 0;
					}
				}
				continue;
			}
			
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
		if (isPartition) {
			int count = 0;
			for (int trk = bamTrack; count<partitionSectorCount && trk <= getTrackCount(); trk++) {
				for (int sec = 0; count<partitionSectorCount && sec < getMaxSectors(trk); sec++) {
					Boolean bamFree = new Boolean(isSectorFree(trk,sec, bamTrack, 0));
					Boolean fileFree = bamEntry[trk][sec];
					if (fileFree == null && bamFree || bamFree.equals(fileFree)) {
						//System.out.println("OK: BAM sector "+trk+"/"+sec+" marked as used");
					} else if (Boolean.FALSE.equals(fileFree) && !Boolean.FALSE.equals(bamFree)) {
						validationErrorList.add(new ValidationError(trk,sec, ValidationError.ERROR_USED_SECTOR_IS_FREE));							
						errorCount++;
					} else if (trk != BAM_TRACK){
						validationErrorList.add(new ValidationError(trk,sec, ValidationError.ERROR_UNUSED_SECTOR_IS_ALLOCATED));							
						warningCount++;
					}
					count++;
				}
			}
		} else {
			for (int trk = 1; trk <= getTrackCount(); trk++) {
				for (int sec = 0; sec < getMaxSectors(trk); sec++) {
					Boolean bamFree = new Boolean(isSectorFree(trk,sec));
					Boolean fileFree = bamEntry[trk][sec];
					if (fileFree == null && bamFree || bamFree.equals(fileFree)) {
						//System.out.println("OK: BAM sector "+trk+"/"+sec+" marked as used");
					} else if (Boolean.FALSE.equals(fileFree) && !Boolean.FALSE.equals(bamFree)) {
						validationErrorList.add(new ValidationError(trk,sec, ValidationError.ERROR_USED_SECTOR_IS_FREE));							
						errorCount++;
					} else if (trk != BAM_TRACK){
						validationErrorList.add(new ValidationError(trk,sec, ValidationError.ERROR_UNUSED_SECTOR_IS_ALLOCATED));							
						warningCount++;
					}
				}
			}
		}
		errors = new Integer(errorCount);
		warnings = new Integer(warningCount);
		return errors;
	}

	/**
	 * Determine if a sector is free<BR>
	 * @param track the track number of sector to check
	 * @param sector the sector number of sector to check
	 * @return when True, the sector is free; otherwise used
	 */
	private boolean isSectorFree(int track, int sector) {		
		int trackPos;
		if (track <= 40) {
			trackPos = getSectorOffset(BAM_TRACK, BAM_SECT_1) + 0x10 + (track - 1) * BYTES_PER_BAM_TRACK + 1;
		} else {
			trackPos = getSectorOffset(BAM_TRACK, BAM_SECT_2) + 0x10 + (track - 41) * BYTES_PER_BAM_TRACK + 1;
		}
		int pos = (sector / 8);		
		int value =  getCbmDiskValue(trackPos + pos) & BYTE_BIT_MASKS[sector & 0x07];
		return value != 0;
	}
	
	/**
	 * Determine if a sector is free<BR>
	 * @param track the track number of sector to check
	 * @param sector the sector number of sector to check
	 * @return when True, the sector is free; otherwise used
	 */
	private boolean isSectorFree(int track, int sector, int bamTrack, int bamSector) {
		int trackPos = getSectorOffset(bamTrack, bamSector) + 0x10 + (track - 1) * BYTES_PER_BAM_TRACK + 1;
		int pos = (sector / 8);		
		int value =  getCbmDiskValue(trackPos + pos) & BYTE_BIT_MASKS[sector & 0x07];
		return value != 0;
	}

	/**
	 * Mark a sector in BAM as used.
	 * @param track trackNumber
	 * @param sector sectorNumber
	 */
	private void markSectorUsed(int track, int sector) {
		//feedbackMessage.append("markBAMused( track=").append(trackNumber).append(" sector=").append(sectorNumber).append(")\n");
		int trackPos;
		if (track <= 40) {
			trackPos = getSectorOffset(BAM_TRACK, BAM_SECT_1) + BYTES_PER_BAM_TRACK * (track - 1) + 0x10;
		} else {
			trackPos = getSectorOffset(BAM_TRACK, BAM_SECT_2) + BYTES_PER_BAM_TRACK * (track - 41) + 0x10;
		}
		int pos = (sector / 8) + 1;
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) & INVERTED_BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) - 1);
	}
	
	/**
	 * Mark a sector in BAM as free.
	 * @param track trackNumber
	 * @param sector sectorNumber
	 */
	private void markSectorFree(int track, int sector) {
		//feedbackMessage.append("markSectorFree: track=").append(trackNumber).append(" sector=").append(sectorNumber).append(")\n");
		int trackPos;
		if (track <= 40) {
			trackPos = getSectorOffset(BAM_TRACK, BAM_SECT_1) + BYTES_PER_BAM_TRACK * (track - 1) + 0x10;
		} else {
			trackPos = getSectorOffset(BAM_TRACK, BAM_SECT_2) + BYTES_PER_BAM_TRACK * (track - 41) + 0x10;
		}
		int pos = (sector / 8) + 1;
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) | BYTE_BIT_MASKS[sector & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) + 1);
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
	 * Determine if there's, at least, one free sector on a track.
	 * @param trackNumber the track number of sector to check.
	 * @return when true, there is at least one free sector on the track.
	 */
	protected boolean isTrackFree(int trackNumber) {
		readBAM();
		int freeSectors = bam.getFreeSectors(trackNumber);
		return freeSectors > 0 ? true : false;
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
				block.track = BAM_TRACK - distance;
				if (block.track >= FIRST_TRACK && block.track <= LAST_TRACK && block.track != BAM_TRACK) {
					// Track within disk limits
					found = isTrackFree(block.track);
				}
				if (!found){
					// Check the track above the directory track
					block.track = BAM_TRACK + distance;
					if (block.track <= LAST_TRACK && block.track != BAM_TRACK) {
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
				int maxSector = TRACK_SECTORS;		// Determine how many sectors there are on that track.
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
						block.sector = block.sector + C1581_INTERLEAVE;	// Move away an "interleave" number of sectors.
						if (geosFormat && block.track >= 25) {
							// Empirical GEOS optimization, get one sector backwards if over track 25.
							block.sector--;
						}
					} else {
						// For a different track of a GEOS-formatted disk, use sector skew.
						block.sector = ((block.track - curTrack) << 1 + 4 + C1581_INTERLEAVE);
					}
					int maxSector = TRACK_SECTORS;	// Get the number of sectors on the current track.					
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
						if (block.track > FIRST_TRACK && block.track <= BAM_TRACK) {
							block.track = block.track - 1 ;
						} else if (block.track < LAST_TRACK && block.track > BAM_TRACK) {
							block.track = block.track + 1 ;
							if (block.track == BAM_TRACK) {
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
							if (block.track == BAM_TRACK) {
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
		}  else {
			feedbackMessage.append("\nsaveFileData: Error: No free sectors on disk. Disk is full.\n");
			return null;
		}
		return firstBlock;
	}
	
	/** {@inheritDoc} */
	public boolean addDirectoryEntry(CbmFile cbmFile, int fileTrack, int fileSector, boolean isCopyFile, int lengthInBytes) {
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
	 * Find first free directory entry.
	 * Looks through the allocated directory sectors.
	 * @return number of next free directory entry, or -1 if none is free.
	 */
	private int findFreeDirEntry() {
		int track = DIR_TRACK;
		int sector = DIR_SECT;
		int dirPosition = 0;
		do {			
			int dataPosition = getSectorOffset(track, sector);
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
			int thisSector = DIR_SECT;
			int entryPosCount = 8;
			while (dirEntryNumber >= entryPosCount) {
				int nextTrack = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00);
				int nextSector = getCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01);				
				if (nextTrack == 0) {
					nextTrack = thisTrack;			
					boolean found = false;
					for (int sec=0; !found && sec < TRACK_SECTORS; sec++) {
						found = isSectorFree(nextTrack, sec);
						nextSector = sec;
					}
					if (found) {
						nextTrack = thisTrack;
						markSectorUsed(nextTrack, nextSector);
						setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x00, nextTrack);
						setCbmDiskValue(getSectorOffset(thisTrack, thisSector) + 0x01, nextSector);	
						setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x00, 0);
						setCbmDiskValue(getSectorOffset(nextTrack, nextSector) + 0x01, -1);
						feedbackMessage.append("Added another directory block ").append(nextTrack).append("/").append(nextSector).append(") for dir entry ").append(dirEntryNumber).append(".\n");						
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
	 * Find offset to a directory entry.
	 * @param dirEntryNumber directory entry number to look up
	 * @return offset in image to directory entry, or -1 if dirEntry is not available.
	 */
	private int getDirectoryEntryPosition(int dirEntryNumber) {
		if (dirEntryNumber < 0 || dirEntryNumber >= FILE_NUMBER_LIMIT) {
			return -1;
		}
		int track = DIR_TRACK;
		int sector = DIR_SECT;
		int entryPosCount = 8;
		while (dirEntryNumber >= entryPosCount && track != 0) {
			int tmpTrack = track;
			track = getCbmDiskValue(getSectorOffset(tmpTrack, sector) + 0x00);
			sector = getCbmDiskValue(getSectorOffset(tmpTrack, sector) + 0x01);
			entryPosCount += 8;			
		}
		if (track == 0) {
			return -1;
		} else {
			return getSectorOffset(track, sector) + (dirEntryNumber & 0x07) * 32;
		}
	}
	
	@Override
	protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber) {		
		feedbackMessage.append("writeDirectoryEntry: cbmFile to dirEntryNumber ").append(dirEntryNumber).append(".\n");
		int pos = getDirectoryEntryPosition(dirEntryNumber);
		if (pos >= 0) {
			setCbmDiskValue(pos + 0, cbmFile.getDirTrack());
			setCbmDiskValue(pos + 1, cbmFile.getDirSector());
			writeSingleDirectoryEntry(cbmFile, pos);
		} else {
			feedbackMessage.append("Error: writeDirectoryEntry failed for entry "+dirEntryNumber+"\n");
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
				int tmpTrack  = getCbmDiskValue(getSectorOffset(track, sector) + 0x00);
				int tmpSector = getCbmDiskValue(getSectorOffset(track, sector) + 0x01);
				markSectorFree(track, sector);
				track = tmpTrack;
				sector = tmpSector;
			}
		} else {
			feedbackMessage.append("Error: Failed to delete ").append(cbmFile.getName());
		}	
	}
	
}
