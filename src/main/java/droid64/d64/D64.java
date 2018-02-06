package droid64.d64;
import java.util.Arrays;

/**<pre style='font-family:sans-serif;'>
 * Created on 21.06.2004
 *
 *   droiD64 - A graphical file manager for D64 files
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
 *
 * @author wolf
 * @author henrik
 * </pre>
 */
public class D64 extends DiskImage {

	/** D64 format is restricted to a maximum of 144 directory entries (18 sectors with 8 entries each). Track 18 has 19 sectors, of which the first is the BAM. */
	private static final int FILE_NUMBER_LIMIT = 144;

	/** Number of tracks */
	private static final int TRACK_COUNT = 35;
	/** Maximum number of sectors on any track */
	private static final int MAX_SECTORS = 21;
	
	/** Offsets of tracks */
	private final static CbmTrack[] TRACKS = {
			new CbmTrack(21,	  0,	      0	),	// $00000}   0 dummy
			// sectors_, sectors_in_, offset_
			new CbmTrack(21,	  0,	      0	),	// $00000}   1
			new CbmTrack(21,	 21,	   5376	),	// $01500}   2
			new CbmTrack(21,	 42,	  10752	),	// $02a00}   3
			new CbmTrack(21,	 63,	  16128	),	// $03f00}   4
			new CbmTrack(21,	 84,	  21504	),	// $05400}   5
			new CbmTrack(21,	105,	  26880	),	// $06900}   6
			new CbmTrack(21,	126,	  32256	),	// $07e00}   7
			new CbmTrack(21,	147,	  37632	),	// $09300}   8
			new CbmTrack(21,	168,	  43008	),	// $0a800}   9
			new CbmTrack(21,	189,	  48384	),	// $0bd00}  10
			new CbmTrack(21,	210,	  53760	),	// $0d200}  11
			new CbmTrack(21,	231,	  59136	),	// $0e700}  12
			new CbmTrack(21,	252,	  64512	),	// $0fc00}  13
			new CbmTrack(21,	237,	  69888	),	// $11100}  14
			new CbmTrack(21,	295,	  75264	),	// $12600}  15
			new CbmTrack(21,	315,	  80640	),	// $13b00}  16
			new CbmTrack(21,	336,	  86016	),	// $15000}  17
			new CbmTrack(19,	357,	  91392	),	// $16500}  18
			new CbmTrack(19,	376,	  96256	),	// $17800}  19
			new CbmTrack(19,	395,	 101120	),	// $18b00}  20
			new CbmTrack(19,	414,	 105984	),	// $19e00}  21
			new CbmTrack(19,	433,	 110848	),	// $1b100}  22
			new CbmTrack(19,	452,	 115712	),	// $1c400}  23
			new CbmTrack(19,	471,	 120576	),	// $1d700}  24
			new CbmTrack(18,	490,	 125440	),	// $1ea00}  25
			new CbmTrack(18,	508,	 130048	),	// $1fc00}  26
			new CbmTrack(18,	526,	 134656	),	// $20e00}  27
			new CbmTrack(18,	544,	 139264	),	// $22000}  28
			new CbmTrack(18,	562,	 143872	),	// $23200}  29
			new CbmTrack(18,	580,	 148480	),	// $24400}  30
			new CbmTrack(17,	598,	 153088	),	// $25600}  31
			new CbmTrack(17,	615,	 157440	),	// $26700}  32
			new CbmTrack(17,	632,	 161792	),	// $27800}  33
			new CbmTrack(17,	649,	 166144	),	// $28900}  34
			new CbmTrack(17,	666,	 170496	),	// $29a00}  35
			new CbmTrack(17,	683,	 174848	),	// $2ab00}  36
			new CbmTrack(17,	700,	 179200	),	// $2bc00}  37
			new CbmTrack(17,	717,	 183552	),	// $2cd00}  38
			new CbmTrack(17,	734,	 187904	),	// $2de00}  39
			new CbmTrack(17,	751,	 192256	),	// $2ef00}  40
	};

	/** The normal size of a D64 image (683 * 256) */
	private final static int D64_SIZE = 174848;
	/** Track number of directory track */
	protected static final int DIR_TRACK = 18;
	/** Track number of secondary directory track (for 1571 disks); 255 (a non-existent track), if not available */
	protected static final int DIR_TRACK2 = 255;

	private static final int BAM_TRACK = 18;
	private static final int BAM_SECTOR = 0;
	
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

	/** Blocks per CP/M allocation unit (4 * 256 = 1024). */
	private static final int BLOCKS_PER_ALLOC_UNIT = 4;

	/** Track number of first track (may be above one for sub directories on 1581 disks) */
	private int firstTrack = 1;
	/** Track number of last track plus one (may be below the physical end of disk for sub directories on 1581 disks) */
	private int lastTrack = 35;
	/** Track number of current block of file */
	private int track;
	/** Sector number of current block of file  */
	private int sector;

	/**
	 * Constructor
	 */
	public D64() {
		initCbmFile(FILE_NUMBER_LIMIT);
	}
		
	/** {inheritDoc} */
	protected void readImage(String filename) throws CbmException {
		bam = new CbmBam(TRACKS.length, 4);
		readImage(filename, D64_SIZE, "D64");
	}

	/**
	 * Reads the BAM of the D64 image and fills bam[] with entries.<br/>
	 * <pre>
	 * Bytes:$00-01: Track/Sector location of the first directory sector (should be set to 18/1 
	 *          but it doesn't matter,  and don't trust  what is there,  always go to  18/1 for 
	 *          first directory entry)
	 *		02: Disk DOS version type (see note below) $41 ("A")
	 *		03: Unused
	 *	 04-8F: BAM entries for each track, in groups  of  four  bytes  per
	 *			track, starting on track 1 (see below for more details)
	 *	 90-9F: Disk Name (padded with $A0)
	 *	 A0-A1: Filled with $A0
	 *	 A2-A3: Disk ID
	 *		A4: Usually $A0
	 *	 A5-A6: DOS type, usually "2A"
	 *	 A7-AA: Filled with $A0
	 *	 AB-FF: Normally unused ($00), except for 40 track extended format,
	 *			see the following two entries:
	 *	 AC-BF: DOLPHIN DOS track 36-40 BAM entries (only for 40 track)
	 *	 C0-D3: SPEED DOS track 36-40 BAM entries (only for 40 track)
	 *
	 *  BAM_struct = record
	 *  diskDosType : byte;
	 *  trackBits   : array [1..40, 1..4] of byte;
	 *  diskName    : string[16];
	 *  diskId      : string[2];
	 *  DOS_type    : string[2];
	 *</pre>
	 */
	public void readBAM() {
		int bamOffset = getSectorOffset(BAM_TRACK, BAM_SECTOR);
		bam.setDiskName("");
		bam.setDiskId("");
		bam.setDiskDosType( (byte) getCbmDiskValue(bamOffset + 2 ));
		for (byte track = 1; track < TRACKS.length; track++) {		
			bam.setFreeSectors(track, (byte) getCbmDiskValue(bamOffset + 4 + (track-1) * 4));
			for (int i = 1; i < 4; i++) {
				bam.setTrackBits(track, i, (byte) getCbmDiskValue(bamOffset + 4 + (track-1) * 4 + i));
			}
		}
		for (int i = 0; i < DISK_NAME_LENGTH; i++) {
			bam.setDiskName(bam.getDiskName() + new Character((char)(PETSCII_TABLE[getCbmDiskValue(bamOffset + 144 + i)])));
		}
		for (int i = 0; i < DISK_ID_LENGTH; i++) {
			bam.setDiskId(bam.getDiskId() +	new Character((char)( PETSCII_TABLE[getCbmDiskValue(bamOffset + 162 + i)])));
		}
		cpmFormat = getCpmDiskFormat();
	}
	
	/**
	 * Get track/sector from CP/M sector number.
	 * @param num  CP/M sector number
	 * @return TrackSector of specified CP/M sector 
	 */
	private TrackSector getCpmTrackSector(int num) {
		int trk = 0;
		int sec = 0;
		if (num >= CPM_BLOCK_COUNT) {
			return null;
		}
		if (cpmFormat == CPM_TYPE_D64_C64) {
			if (num >= 544) {
				return null;
			}
			trk = num / 17 + 3;
			if (trk >= 18) {
				trk++;
			}
			sec = num % 17;
			return new TrackSector(trk, sec);
		} else if (cpmFormat == CPM_TYPE_D64_C128) {		
			for (int i=0; i<4; i++) {
				num += CPM_ZONES[i][3]; 
				if (num < CPM_ZONES[i][2]) {
					trk = CPM_ZONES[i][0] + num / CPM_ZONES[i][1];
					sec = (CPM_SECTOR_SKEW * num) % CPM_ZONES[i][1];
					return new TrackSector(trk, sec);
				}
				num -= CPM_ZONES[i][2];
			}
			return new TrackSector(trk, sec);
		} else {
			return null;
		}
	}
	
	// TODO FIXME:
	private void readCpmDirectory() {
		if (cpmFormat == CPM_TYPE_UNKNOWN) {
			return;
		}
		if (cpmFormat == CPM_TYPE_D64_C128) {		
			final int C128_SS_DIR_TRACK = 1;
			final int[] C128_SS_DIR_SECTORS = { 10, 15, 20, 4, 9, 14, 19, 3 };
			int filenumber = 0;
			CpmFile entry = null;
			for (int s=0; s<C128_SS_DIR_SECTORS.length; s++) {
				int idx = TRACKS[C128_SS_DIR_TRACK].getOffset() + C128_SS_DIR_SECTORS[s] * BLOCK_SIZE;
				for (int i=0; i < DIR_ENTRIES_PER_SECTOR; i++) {
					CpmFile newFile = getCpmFile(entry, idx + i * DIR_ENTRY_SIZE, false);
					if (newFile != null) {
						cbmFile[filenumber++] = newFile;
						entry = newFile;
					}
				}
			}
			fileNumberMax = filenumber;
		} else if (cpmFormat == CPM_TYPE_D64_C64) {
			final int C64_SS_DIR_TRACK = 3;
			final int[] C64_SS_DIR_SECTORS = { 1, 2, 3, 4, 5, 6 ,7 };
			int filenumber = 0;
			CpmFile entry = null;
			for (int s=0; s<C64_SS_DIR_SECTORS.length; s++) {
				int idx = TRACKS[C64_SS_DIR_TRACK].getOffset() + C64_SS_DIR_SECTORS[s] * BLOCK_SIZE;
				for (int i=0; i < DIR_ENTRIES_PER_SECTOR; i++) {
					CpmFile newFile = getCpmFile(entry, idx + i * DIR_ENTRY_SIZE, false);
					if (newFile != null) {
						cbmFile[filenumber++] = newFile;
						entry = newFile;
					}
				}
			}
			fileNumberMax = filenumber;
		}

	}

	/** {@inheritDoc} */
	public int getSectorOffset(int track, int sector) {
		return TRACKS[track].getOffset() + (BLOCK_SIZE * sector);
	}
	
	/** {@inheritDoc} */
	public void readDirectory() {

		if (cpmFormat != CPM_TYPE_UNKNOWN) {
			readCpmDirectory();
			return;
		}

		boolean fileLimitReached = false;
		int track = DIR_TRACK;
		int sector = 1;
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
				//System.out.println("'"+cbmFile[filenumber]+"'");
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
				byte[] data = new byte[ cpm.getRecordCount() * CPM_RECORD_SIZE ];
				int dstPos = 0;
				for (Integer au : cpm.getAllocList()) {
					for (int r=0; r < BLOCKS_PER_ALLOC_UNIT; r++) {
						TrackSector ts = getCpmTrackSector(au * BLOCKS_PER_ALLOC_UNIT + r);
						int srcPos = TRACKS[ts.getTrack()].getOffset() + ts.getSector() * BLOCK_SIZE;
						for (int c=0; c<BLOCK_SIZE; c++) {
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
				throw new CbmException("CP/M format but not a CP/M file.\n");
			}
		} else if (cbmFile[number].isFileScratched()) {
			throw new CbmException("writeSaveData: File number " + number + " is deleted.");			
		}
		feedbackMessage.append("writeSaveData: ").append(number).append(" '").append(cbmFile[number].getName()).append("'\n");
		feedbackMessage.append("Tracks / Sectors: ");
				
		int thisTrack = cbmFile[number].getTrack();
		int thisSector = cbmFile[number].getSector();
		int counter = 0;
		byte[] saveData = new byte[MAX_PRG];
		do {
			if (thisTrack >= TRACKS.length) {
				throw new CbmException("Track " + thisTrack + " outside of image.");
			}
			int nextTrack  =  getCbmDiskValue( TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 0);
			int nextSector =  getCbmDiskValue( TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 1);
			feedbackMessage.append(thisTrack).append("/").append(thisSector).append(" ");
			if (nextTrack > 0) {
				for (int position = 2; position < BLOCK_SIZE; position++) {
					saveData[counter++] = cbmDisk[ TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + position];
				}
			} else {
				feedbackMessage.append("\nRemaining bytes: ").append(nextSector).append("\n");
				for (int position = 2; position <= nextSector; position++) {
					saveData[counter++] = cbmDisk[ TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + position];
				}
			}
			thisTrack = nextTrack;
			thisSector = nextSector;
		} while (thisTrack != 0);
		feedbackMessage.append("OK.\n");
		return Arrays.copyOfRange(saveData, 0, counter);
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

	/**
	 * Find a sector for the first block of the file, using variables Track and Sector<BR>
	 * globals needed: GEOSFormat, CopyToDirTrack<BR>
	 * globals written: track_, sector<BR>
	 * @return when True, a sector was found; otherwise no more sectors left
	 */
	private boolean findFirstCopyBlock() {
		int maxSector;
		int distance;
		boolean found = false;	//We found no free sector yet

		if (geosFormat) {
			// If this is a GEOS-formatted disk then use the other routine, from track one upwards.
			track = 1;
			sector = 0;
			found = findNextCopyBlock();
			//TrackSector trackSector = new TrackSector(track, sector);

		} else {
			distance = 1;	// If it's a normal disk then we start off with tracks just besides the directory track.
			while ((found == false) && (distance < 128)) {
				// Search until we find a free block or moved too far from the directory track.
				track =  (DIR_TRACK - distance);	//Check the track below the directory track first.
				if ((track >= firstTrack) && (track <= lastTrack)) {
					found = isTrackFree(track);		// If the track is inside the valid range then check if there's a free sector on it.
				}
				if ( found == false){
					track = (DIR_TRACK + distance);	// If no luck then check the track above the directory track.
					if (track <= lastTrack) {
						found = isTrackFree(track);	// If the track is inside the valid range then check if there's a free sector on it.
					}
				}
				if ( found == false) {
					distance++;	// If no luck either then move one track away from the directory track and try again.
				}
			}

			//// If the whole disk is full and we're allowed to use the directory track for file data then try there too.
			//if ( (found == false) && (CopyToDirTrack)) {
			//  track_ = dirTrack;
			//  found = isTrackFree(track_);
			//}

			if (found) {
				// If we finally found a track with, at least, one free sector then search for a free sector in it.
				maxSector = getMaximumSectors(track);		// Determine how many sectors there are on that track.
				sector = 0;									// Start off with sector zero.
				do {
					found = isSectorFree(track, sector);	// Check if the current sector is free.
					if (found == false) {
						// If it isn't then go on to the next sector.
						sector++;
					}
				} while ((found == false) || (sector >= maxSector));	// Repeat the check until we find a free sector or run off the track.
			}

		}

		//TrackSector trackSector = new TrackSector(track, sector);

		feedbackMessage.append("firstCopyBlock(): The first block will be at Track ").append(track).append(" Sector ").append(sector).append(".\n");
		return found;
	}

	/**
	 * Find a sector for the next block of the file, using variables Track and Sector<BR>
	 * globals needed: GEOSFormat, CopyToDirTrack<BR>
	 * globals written: track_, sector<BR>
	 * @return when True, a sector was found; otherwise no more sectors left
	 */
	private boolean findNextCopyBlock() {
		boolean found = false;
		int curSector;
		int curTrack;
		if ((track == 0) || (track > lastTrack)) {
			// If we somehow already ran off the disk then there are no more free sectors left.
			found = false;
		} else {
			int tries = 3;			// Set the number of tries to three.
			found = false;		// We found no free sector yet.
			curTrack = track;	// Remember the current track number.
			while ( (!found) && (tries > 0)) {
				// Keep trying until we find a free sector or run out of tries.
				if (isTrackFree(track)) {
					// If there's, at least, one free sector on the track then get searching.
					if (track == curTrack || !geosFormat) {
						// If this is a non-GEOS disk or we're still on the same track of a GEOS-formatted disk then...
						sector = sector + C1541_INTERLEAVE;	// Move away an "interleave" number of sectors.
						if (geosFormat && track >= 25) {
							// Empirical GEOS optimization, get one sector backwards if over track 25.
							sector--;
						}
					} else {
						// For a different track of a GEOS-formatted disk, use sector skew.
						sector = ((track - curTrack) << 1 + 4 + C1541_INTERLEAVE);
					}
					int maxSector = getMaximumSectors(track);	// Get the number of sectors on the current track.
					while (sector >= maxSector) {
						// If we ran off the track then correct the result.
						sector = (sector - maxSector)+1;	// Subtract the number of sectors on the track.
						if (sector > 0 && !geosFormat) {
							// Empirical optimization, get one sector backwards if beyond sector zero.
							sector--;
						}
					}
					curSector = sector;	//Remember the sector we finally arrived at.
					do {
						found = isSectorFree(track, sector);	//Check if the current sector is free}
						if (!found) {
							// If it isn't then go to the next sector.
							sector++; 
						}
						if (sector >= maxSector) {
							// If we ran off the track then wrap around to sector zero.
							sector = 0; 
						}
					} while (!found && sector != curSector);	//Keep searching until we find a free sector or arrive back at the original sector.
				} else {
					// if IsTrackFree(track) = FALSE then
					//if GEOSFormat then {	//If the current track is used up completely then...
					//	Inc(track);	//Move one track upwards on a GEOS-formatted disk.
					//	if (track = DirTrack) or (track = DirTrack2) then Inc(track_);	//Skip the directory tracks on the way.
					//	if track = LastTrack then Tries = 0;	//If we ran off the disk then there are no more tries.
					//} else {
					if (track == DIR_TRACK) {
						// If we already tried the directory track then there are no more tries.
						tries = 0;
					} else {
						if (track < DIR_TRACK) {
							track --;	//If we're below the directory track then move one track downwards.
							if (track < firstTrack) {
								track = (DIR_TRACK + 1); //If we ran off the disk then step back to the track just above the directory track and zero the sector number.
								sector = 0;
								//If there are no tracks available above the directory track then there are no tries left; otherwise just decrease the number of tries.
								if (track <= lastTrack) {
									tries--; 
								} else { 
									tries = 0; 
								}
							}
						} else {	//if track >= DirTrack then.
							track++;	//If we're above the directory track then move one track upwards.
							// if (track_ = dirTrack2) track_++;	//Skip the secondary directory track on the way.
							if (track > lastTrack) {
								track = (DIR_TRACK - 1);	//If we ran off the disk then step back to the track just below the directory track and zero the sector number.
								sector = 0;
								//If there are no tracks available below the directory track then there are no tries left; otherwise just decrease the number of tries.
								if (track >= firstTrack) {
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
		feedbackMessage.append("findNextCopyBlock(): next block at track ").append(track).append(" sector ").append(sector).append(".\n");
		return found;
	}
	
	/**
	 * Mark a sector in BAM as used.
	 * @param trackNumber trackNumber
	 * @param sectorNumber sectorNumber
	 */
	private void markSectorUsed(int trackNumber, int sectorNumber) {
		//feedbackMessage.append("markBAMused( track=").append(trackNumber).append(" sector=").append(sectorNumber).append(")\n");
		int trackPos = TRACKS[BAM_TRACK].getOffset() + (BLOCK_SIZE * BAM_SECTOR) + (trackNumber * 4);
		int pos = (sectorNumber / 8) + 1;
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) & INVERTED_BYTE_BIT_MASKS[sectorNumber & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) - 1);
	}
	
	/**
	 * Mark a sector in BAM as free.
	 * @param trackNumber trackNumber
	 * @param sectorNumber sectorNumber
	 */
	private void markSectorFree(int trackNumber, int sectorNumber) {
		//feedbackMessage.append("markSectorFree: track=").append(trackNumber).append(" sector=").append(sectorNumber).append(")\n");
		int trackPos = TRACKS[BAM_TRACK].getOffset() + (BLOCK_SIZE * BAM_SECTOR) + (trackNumber * 4);
		int pos = (sectorNumber / 8) + 1;
		setCbmDiskValue(trackPos + pos, getCbmDiskValue(trackPos + pos) | BYTE_BIT_MASKS[sectorNumber & 0x07] );
		setCbmDiskValue(trackPos, getCbmDiskValue(trackPos) + 1);
	}
	
	/**
	 * Determine if a sector is free<BR>
	 * @param Track_number the track number of sector to check
	 * @param Sector_number the sector number of sector to check
	 * @return when True, the sector is free; otherwise used
	 */
	private boolean isSectorFree(int track, int sector) {
		int trackPos = TRACKS[BAM_TRACK].getOffset() + (BLOCK_SIZE * BAM_SECTOR) + (track * 4);
		int pos = (sector / 8) + 1;
		int value =  getCbmDiskValue(trackPos + pos) & BYTE_BIT_MASKS[sector & 0x07];
		return value != 0;
	}
	
	/** {@inheritDoc} */
	protected void setDiskName(String newDiskName, String newDiskID){
		feedbackMessage.append("setDiskName: '").append(newDiskName).append("', '").append(newDiskID).append("'\n");
		for (int i=0; i < DISK_NAME_LENGTH; i++) {
			if (i < newDiskName.length()) {
				setCbmDiskValue( TRACKS[BAM_TRACK].getOffset() + (BLOCK_SIZE*BAM_SECTOR) + 144 + i,	newDiskName.charAt(i));
			} else {
				setCbmDiskValue( TRACKS[BAM_TRACK].getOffset() + (BLOCK_SIZE*BAM_SECTOR) + 144 + i,	BLANK);				
			}
		}	
		for (int i=0; i < DISK_ID_LENGTH; i++) {
			if (i < newDiskID.length()) {
				setCbmDiskValue( TRACKS[BAM_TRACK].getOffset() + (BLOCK_SIZE*BAM_SECTOR) + 162 +i,	newDiskID.charAt(i));
			} else {
				setCbmDiskValue( TRACKS[BAM_TRACK].getOffset() + (BLOCK_SIZE*BAM_SECTOR) + 162 +i,	BLANK);				
			}
		}
	}

	/**
	 * Fill sector in D64 with data. Pad with zeroes if saveData is smaller than BLOCK_SIZE - 2.
	 * @param trackNumber track number
	 * @param sectorNumber sector number
	 * @param dataPosition data position
	 * @param nextTrack next track
	 * @param nextSector next sector
	 * @param saveData data to fill sector with
	 */
	private void fillSector(int trackNumber, int sectorNumber, int dataPosition, int nextTrack, int nextSector, byte[] saveData) {
		feedbackMessage.append("fillSector: track=").append(trackNumber).append(" sector=").append(sectorNumber).append(" position=")
		.append(dataPosition).append(" nextTrack=").append(nextTrack).append(" nextSector=").append(nextSector).append("\n");
		setCbmDiskValue( TRACKS[trackNumber].getOffset() + (BLOCK_SIZE * sectorNumber) + 0, nextTrack);
		setCbmDiskValue( TRACKS[trackNumber].getOffset() + (BLOCK_SIZE * sectorNumber) + 1, nextSector);
		for (int position = 0; position < (BLOCK_SIZE - 2); position++) {
			int value = saveData.length > dataPosition + position ? saveData[dataPosition + position] & 0xff : 0;
			setCbmDiskValue( TRACKS[trackNumber].getOffset() + (BLOCK_SIZE * sectorNumber) + 2 + position, value);
		}
	}

	/** {@inheritDoc} */
	protected boolean saveFileData(byte[] saveData) {
		if (cpmFormat != CPM_TYPE_UNKNOWN) {
			feedbackMessage.append("Not yet implemented for CP/M format.\n");
			return false;
		}
		int usedBlocks = 0;
		int dataRemain = saveData.length;
		feedbackMessage.append("SaveFileData: ").append(dataRemain).append(" bytes of data.\n");
		boolean success = findFirstCopyBlock();
		if (success) {
			int thisTrack;
			int thisSector;
			int dataPos = 0;
			destTrack = track;
			destSector = sector;
			while (dataRemain >= 0 && success) {
				//feedbackMessage.append(dataRemain).append(" bytes remain: Track ").append(track).append(", Sector ").append(sector).append("\n");
				thisTrack = track;
				thisSector = sector;
				markSectorUsed(thisTrack, thisSector);
				if (dataRemain >= (BLOCK_SIZE - 2)) {
					success = findNextCopyBlock();
					if (success) {
						fillSector(thisTrack, thisSector, dataPos, track, sector, saveData);
						usedBlocks++;
						dataRemain = dataRemain - (BLOCK_SIZE - 2);
						dataPos = dataPos + (BLOCK_SIZE - 2);
					} else {
						feedbackMessage.append("\nsaveFileData: Error: Not enough free sectors on disk. Disk is full.\n");					
					}
				} else {
					fillSector(thisTrack, thisSector, dataPos, 0, (dataRemain + 1), saveData);
					usedBlocks++;
					dataRemain = -1;
				}
			}
			if (success) {
				feedbackMessage.append("All data written ("+usedBlocks+" blocks).\n");
			}
		} else {
			feedbackMessage.append("\nsaveFileData: Error: No free sectors on disk. Disk is full.\n");
		}
		return success;
	}

	/**
	 * Iterate directory sectors to find the specified directory entry. If needed, attempt to allocate more directory sectors
	 * and continue iterating until either directory entry is available or FILE_NUMBER_LIMIT is reached,
	 * globals written: bufferCbmFile<BR>
	 * @param dirEntryNumber position where to put this entry in the directory
	 * @return returns true if a free directory block was found
	 */
	private boolean setNewDirLocation(int dirEntryNumber){
		if (dirEntryNumber < 0 || dirEntryNumber >= FILE_NUMBER_LIMIT) {
			feedbackMessage.append( "Error: Invalid directory entry number ").append(dirEntryNumber).append(" at setNewDirectoryLocation.\n");
			return false;
		} else if ( (dirEntryNumber & 0x07) != 0) {	
			// If this is not the eighth entry we are lucky and do not need to do anything...
			bufferCbmFile.setDirTrack(0);
			bufferCbmFile.setDirSector(0);
			return true;
		} else {
			//find the correct entry where to write new values for dirTrack and dirSector
			int thisTrack = DIR_TRACK;
			int thisSector = 1;
			int entryPosCount = 8;
			while (dirEntryNumber >= entryPosCount) {
				int nextTrack = getCbmDiskValue(TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 0x00);
				int nextSector = getCbmDiskValue(TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 0x01);				
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
						setCbmDiskValue(TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 0x00, nextTrack);
						setCbmDiskValue(TRACKS[thisTrack].getOffset() + (BLOCK_SIZE * thisSector) + 0x01, nextSector);	
						setCbmDiskValue(TRACKS[nextTrack].getOffset() + (BLOCK_SIZE * nextSector) + 0x00, 0);
						setCbmDiskValue(TRACKS[nextTrack].getOffset() + (BLOCK_SIZE * nextSector) + 0x01, -1);
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
			track = getCbmDiskValue(TRACKS[track].getOffset() + (BLOCK_SIZE * sector) + 0x00);
			sector = getCbmDiskValue(TRACKS[track].getOffset() + (BLOCK_SIZE * sector) + 0x01);
			entryPosCount += 8;			
		}
		if (track == 0) {
			return -1;
		} else {
			return TRACKS[track].getOffset() + (BLOCK_SIZE * sector) + (dirEntryNumber & 0x07) * 32;
		}
	}
	
	
	/** {@inheritDoc} */
	protected void writeDirectoryEntry(int dirEntryNumber){
		int this_track = DIR_TRACK;
		int this_sector = 1;
		feedbackMessage.append("writeDirectoryEntry: bufferCbmFile to dirEntryNumber ").append(dirEntryNumber).append(".\n");
		if (dirEntryNumber > 7) {
			while (dirEntryNumber > 7) {
				this_track  = getCbmDiskValue( TRACKS[this_track].getOffset() + (BLOCK_SIZE*this_sector) + 0 );
				this_sector = getCbmDiskValue( TRACKS[this_track].getOffset() + (BLOCK_SIZE*this_sector) + 1 );

				feedbackMessage.append("LongDirectory: "+dirEntryNumber+" dirEntrys remain, next: Track "+this_track+", Sector "+this_sector+"\n");
				dirEntryNumber = dirEntryNumber - 8;
			}
		}
		int where = TRACKS[this_track].getOffset() + (BLOCK_SIZE*this_sector) + (dirEntryNumber * 32);
		setCbmDiskValue(where + 0, bufferCbmFile.getDirTrack());
		setCbmDiskValue(where + 1, bufferCbmFile.getDirSector());
		writeSingleDirectoryEntry(where);	 
	}

	/**
	 * Copy attributes of bufferCbmFile to a location in cbmDisk.<BR>
	 * globals needed: bufferCbmFile<BR>
	 * globals written: cbmDisk<BR>
	 * @param where data position where to write to cbmDisk
	 */
	private void writeSingleDirectoryEntry(int where){
		feedbackMessage.append("writeSingleDirectoryEntry\n");
		if (cpmFormat != CPM_TYPE_UNKNOWN) {
			feedbackMessage.append("Not yet implemented for CP/M format.\n");
			return ;
		}
		// file attributes
		setCbmDiskValue(where + 2, 0);
		if (bufferCbmFile.isFileScratched() == false){
			setCbmDiskValue(where + 2, bufferCbmFile.getFileType());

			if (bufferCbmFile.isFileLocked()){
				setCbmDiskValue(where + 2, (getCbmDiskValue(where + 2) | 64));
			}
			if (bufferCbmFile.isFileClosed()){
				setCbmDiskValue(where + 2, (getCbmDiskValue(where + 2) | 128));
			}
		}
		//file track / sector (where to start reading)
		setCbmDiskValue(where + 3, bufferCbmFile.getTrack());
		setCbmDiskValue(where + 4, bufferCbmFile.getSector());
		// FileName		
		for (int position = 0; position <= 15; position++) {
			setCbmDiskValue(where + 5 + position, BLANK);
		}
		for (int position = 0; position <= bufferCbmFile.getName().length()-1; position++) {
			setCbmDiskValue(where + 5 + position, bufferCbmFile.getName().charAt(position));
		}
		// relative Track/Sector
		setCbmDiskValue(where + 21, bufferCbmFile.getRelTrack());
		setCbmDiskValue(where + 22, bufferCbmFile.getRelSector());
		// GEOS
		for (int position = 0; position <= 5; position++) {
			setCbmDiskValue(where + 23 + position, bufferCbmFile.getGeos(position) );
		}
		// Size
		setCbmDiskValue(where + 30, bufferCbmFile.getSizeInBlocks() );
		setCbmDiskValue(where + 31, bufferCbmFile.getSizeInBlocks() / BLOCK_SIZE );
	}

	/** {@inheritDoc} */
	protected boolean addDirectoryEntry(String thisFilename, int thisFiletype, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes){
		feedbackMessage.append("\naddDirectoryEntry (\"").append(thisFilename).append("\", ").append(FILE_TYPES[thisFiletype]).append(", T[").append(destTrack).append("], S[").append(destSector).append("])\n");
		if (cpmFormat != CPM_TYPE_UNKNOWN) {
			feedbackMessage.append("Not yet implemented for CP/M format.\n");
			return false;
		}
		if (isCopyFile) {
			// This a substitute for setNewDirectoryEntry(thisFilename, thisFiletype, destTrack, destSector, dirPosition)
			// since we do not need to set other values than destTrack and destSector when copying a file. 
			bufferCbmFile.setTrack(destTrack);
			bufferCbmFile.setSector(destSector);
		} else {
			setNewDirEntry(bufferCbmFile, thisFilename, thisFiletype, destTrack, destSector, lengthInBytes);
		}
		bufferCbmFile.setDirTrack(0);
		bufferCbmFile.setDirSector(-1);
		int dirEntryNumber = findFreeDirEntry();
		if (dirEntryNumber != -1 && setNewDirLocation(dirEntryNumber)) {
			writeSingleDirectoryEntry(getDirectoryEntryPosition(dirEntryNumber));
			fileNumberMax++;	// increase the maximum file numbers
			return true;
		} else {
			feedbackMessage.append("Error: Could not find a free sector on track "+DIR_TRACK+" for new directory entries.\n");
			return false;
		}
	}

	/**
	 * Writes a new D64 file<BR>
	 * globals_written: cbmDisk<BR>
	 * @param filename	the filename
	 * @param newDiskName	the new name (label) of the disk
	 * @param newDiskID	the new disk-ID
	 * @return <code>true</code> when writing of the D64 file was successful
	 */
	public boolean saveNewImage(String filename, String newDiskName, String newDiskID){
		final int[] newD64Data = {
				0x12, 0x01, 0x41, 0x00, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f,	//00016500 (dec = 91392)
				0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f,	//00016510
				0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f,	//00016520  
				0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f,	//00016530
				0x15, 0xff, 0xff, 0x1f, 0x15, 0xff, 0xff, 0x1f, 0x11, 0xfc, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07,	//00016540
				0x13, 0xff, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07, 0x13, 0xff, 0xff, 0x07,	//00016550
				0x13, 0xff, 0xff, 0x07, 0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03,	//00016560
				0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03, 0x12, 0xff, 0xff, 0x03, 0x11, 0xff, 0xff, 0x01,	//00016570
				0x11, 0xff, 0xff, 0x01, 0x11, 0xff, 0xff, 0x01, 0x11, 0xff, 0xff, 0x01, 0x11, 0xff, 0xff, 0x01,	//00016580
				0x31, 0x32, 0x33, 0x34, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0,	//00016590
				0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0xa0, 0x00, 0x00, 0x00, 0x00, 0x00,	//000165a0
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	//000165b0
				0x00, 0x00, 0x00, 0x00, 0x00 ,0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	//000165c0
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	//000165d0
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	//000165e0
				0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00,	//000165f0
				0x00, 0xff, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00, 0x00	//00016600
		};
		cbmDisk = new byte[D64_SIZE];
		Arrays.fill(cbmDisk, (byte) 0);
		
		if (cpmFormat == CPM_TYPE_UNKNOWN) {
			for (int i = 0; i < newD64Data.length; i++) {
				setCbmDiskValue(91392+i, newD64Data[i]);
			}
			setDiskName(cbmFileName(newDiskName), cbmFileName(newDiskID));
			return writeImage(filename);
		} else if (cpmFormat == CPM_TYPE_D64_C128) {
			final int C128_SS_DIR_TRACK = 1;
			final int[] C128_SS_DIR_SECTORS = { 10, 15, 20, 4, 9, 14, 19, 3 };			
			for (int s=0; s<C128_SS_DIR_SECTORS.length; s++) {
				int offset = getSectorOffset(C128_SS_DIR_TRACK, C128_SS_DIR_SECTORS[s]);
				for (int i=2; i<256; i++) {
					cbmDisk[offset+i] = (byte) UNUSED;
				}
			}
			cbmDisk[0] = 'C';
			cbmDisk[1] = 'B';
			cbmDisk[2] = 'M';
			cbmDisk[255] = 0x00;
			setDiskName(CPM_DISKNAME_1, CPM_DISKID_GCR);						
			return writeImage(filename);
		} else if (cpmFormat == CPM_TYPE_D64_C64) {			
			final int C64_SS_DIR_TRACK = 3;
			final int[] C64_SS_DIR_SECTORS = { 1, 2, 3, 4, 5, 6 ,7 };
			for (int s=0; s<C64_SS_DIR_SECTORS.length; s++) {
				int offset = getSectorOffset(C64_SS_DIR_TRACK, C64_SS_DIR_SECTORS[s]);
				for (int i=2; i<256; i++) {
					cbmDisk[offset+i] = (byte) UNUSED;
				}
			}
			cbmDisk[255] = 0x00;
			setDiskName(CPM_DISKNAME_1, CPM_DISKID_GCR);						
			return writeImage(filename);
		} else {
			return false;
		}
	}

	/** {@inheritDoc} */
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
						if ((getBam().getTrackBits(trk, cnt) & DiskImage.BYTE_BIT_MASKS[bit]) == 0) {
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
	
	/** {@inheritDoc} */
	public int getMaxSectors(int trackNumber) {
		return TRACKS[trackNumber].getSectors();
	}

	/** {@inheritDoc} */
	public int getBlocksFree() {
		int blocksFree = 0;
		if (cbmDisk != null) {
			for (int track = 1; track <= lastTrack; track++) {
				if (track != DIR_TRACK) {
					blocksFree = blocksFree + bam.getFreeSectors(track);
				}
			}
		}
		return blocksFree;
	}

	/** {@inheritDoc} */
	public byte[] getBlock (int track, int sector) throws CbmException {
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
	public String toString() {
		StringBuffer buf = new StringBuffer();
		buf.append("D64[");
		buf.append(" compressed=").append(compressed);
		buf.append(" cpmFormat=").append(cpmFormat);
		buf.append(" blocksFree=").append(getBlocksFree());
		buf.append(" cbmFile=[");
		for (int i=0; cbmFile!=null && i<cbmFile.length && i<fileNumberMax; i++) {
			if (i>0) {
				buf.append(", ");
			}
			buf.append(this.cbmFile[i]);
		}
		buf.append("]");
		buf.append(" filenumber_max=").append(fileNumberMax);		
		buf.append("]");
		return buf.toString();
	}

	/** {@inheritDoc} */
	public int getTrackCount() {
		return TRACK_COUNT;
	}

	/** {@inheritDoc} */
	public int getMaxSectorCount() {
		return MAX_SECTORS;
	}

	@Override
	public void deleteFile(CbmFile cbmFile) throws CbmException {
		feedbackMessage = new StringBuffer();
		if (cpmFormat != CPM_TYPE_UNKNOWN) {
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

}

