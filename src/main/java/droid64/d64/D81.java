package droid64.d64;

import java.util.Arrays;

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
	/** Number of tracks (80) */
	private final static int TRACK_COUNT	= 80;
	
	private final static int HEADER_TRACK	= 40;
	private final static int HEADER_SECT	= 0;	
	
	private final static int BAM_TRACK		= 40;
	private final static int BAM_SECT_1		= 1;	
	private final static int BAM_SECT_2		= 2;	

	private final static int DIR_TRACK		= 40;
	private final static int DIR_SECT		= 3;

	/** 1 byte for free sectors on track, and one bit per sector (5 bytes / 40 bits) */
	private final static int BYTES_PER_BAM_TRACK = 6;
	/** Blocks per CP/M allocation unit (8 * 256 = 2048). */
	private static final int BLOCKS_PER_ALLOC_UNIT = 8;

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
		cpmFormat = getCpmDiskFormat();
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
	
	private void readCpmDirectory() {
		if (cpmFormat == CPM_TYPE_UNKNOWN) {
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
		
		if (cpmFormat != CPM_TYPE_UNKNOWN) {
			readCpmDirectory();
			return;
		}
		
		boolean fileLimitReached = false;
		int track = DIR_TRACK;
		int sector = DIR_SECT;
		int dirPosition = 0;
		int filenumber = 0;
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
	}
	
	/** {@inheritDoc} */
	public byte[] writeSaveData(int number) throws CbmException {
		if (cbmDisk == null) {
			throw new CbmException("writeSaveData: No disk data exist.");
		} else if (number >= cbmFile.length) {
			throw new CbmException("writeSaveData: File number " + number + " does not exist.");
		} else if (cpmFormat != CPM_TYPE_UNKNOWN) { 
			feedbackMessage.append("writeSaveData: CP/M mode.\n");
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
			throw new CbmException("writeSaveData: File number " + number + " is deleted.");			
		}
		feedbackMessage.append("writeSaveData: ").append(number).append(" '").append(cbmFile[number].getName()).append("'\n");
		feedbackMessage.append("Tracks / Sectors: ");
		int nextTrack;
		int nextSector;
		int track = cbmFile[number].getTrack();
		int sector = cbmFile[number].getSector();
		int counter = 0;
		byte[] saveData = new byte[MAX_PRG];
		do {
			// TODO: writeSaveData(): There are null-pointer exceptions if the source disk contains scratched (hidden) files.
			if (track >= TRACK_COUNT) {
				throw new CbmException("Track " + track + " outside of image.");
			}
			nextTrack  =  getCbmDiskValue( getSectorOffset(track, sector) + 0);
			nextSector =  getCbmDiskValue( getSectorOffset(track, sector) + 1);
			feedbackMessage.append(track).append("/").append(sector).append(" ");
			if (nextTrack > 0) {
				for (int position = 2; position < BLOCK_SIZE; position++) {
					saveData[counter++] = cbmDisk[ getSectorOffset(track, sector) + position];
				}
			} else {
				feedbackMessage.append("\nRemaining bytes: ").append(sector).append("\n");
				for (int position = 2; position < nextSector; position++) {
					saveData[counter++] = cbmDisk[ getSectorOffset(track, sector) + position];
				}
			}
			track = nextTrack;
			sector = nextSector;
		} while (nextTrack != 0);
		feedbackMessage.append("OK.\n");
		return Arrays.copyOfRange(saveData, 0, counter);
	}

	/** {@inheritDoc} */
	protected boolean saveFileData(byte[] saveData) {
		return false;
	}

	/** {@inheritDoc} */
	protected void setDiskName(String newDiskName, String newDiskID) {
		feedbackMessage.append("setDiskName('").append(newDiskName).append("', '").append(newDiskID).append("')\n");
		int hdrOffset = getSectorOffset(HEADER_TRACK, HEADER_SECT);		
		for (int i=0; i < DISK_NAME_LENGTH; i++) {
			if (i < newDiskName.length()) {
				setCbmDiskValue( hdrOffset + 0x04 + i, newDiskName.charAt(i));
			} else {
				setCbmDiskValue( hdrOffset + 0x04 + i, BLANK);				
			}
		}	
		for (int i=0; i < DISK_ID_LENGTH; i++) {
			if (i < newDiskID.length()) {
				setCbmDiskValue( hdrOffset + 0x16 +i, newDiskID.charAt(i));
			} else {
				setCbmDiskValue( hdrOffset + 0x16 +i, BLANK);				
			}
		}
	}

	/** {@inheritDoc} */
	protected void writeDirectoryEntry(int dirEntryNumber) {		
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
	protected boolean addDirectoryEntry(String thisFilename, int thisFiletype, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes) {
		return false;
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
	public void deleteFile(CbmFile cbmFile) throws CbmException {
		throw new CbmException("Delete not yet implemented for D81.");				
	}

}
