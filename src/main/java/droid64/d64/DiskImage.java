package droid64.d64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.zip.GZIPInputStream;
import java.util.zip.GZIPOutputStream;

import droid64.db.Disk;
import droid64.db.DiskFile;

/**<pre style='font-family:sans-serif;'>
 * Created on 1.09.2015
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
 *   http://droid64.sourceforge.net
 *
 * @author henrik
 * </pre>
 */
abstract public class DiskImage {

	/** Unknown or undefined image type */
	public static final int UNKNOWN_IMAGE_TYPE = 0;
	/** Normal D64 (C1541 5.25") image */
	public static final int D64_IMAGE_TYPE = 1;
	/** Normal D71 (C1571 5.25") image */
	public static final int D71_IMAGE_TYPE = 2;
	/** Normal D81 (C1581 3.5") image */
	public static final int D81_IMAGE_TYPE = 3;
	/** Normal T64 image (tape) */
	public static final int T64_IMAGE_TYPE = 4;
	/** CP/M for C64 on a D64 image */
	public static final int D64_CPM_C64_IMAGE_TYPE  = 5;
	/** CP/M for C128 on a D64 image */
	public static final int D64_CPM_C128_IMAGE_TYPE = 6;
	/** CP/M on a D71 image */
	public static final int D71_CPM_IMAGE_TYPE      = 7;
	/** CP/M on a D81 image */
	public static final int D81_CPM_IMAGE_TYPE      = 8;
	
	/** String array to convert imageType to String name */
	public static final String[] IMAGE_TYPE_NAMES = { "Unknown", "D64", "D71", "D81", "T64", "CP/M D64 (C64)" , "CP/M D64 (C128)", "CP/M D71", "CP/M D81" };
	
	public final static String GZIP_EXT = ".gz";
	public final static String D64_EXT = ".d64";
	public final static String D71_EXT = ".d71";
	public final static String D81_EXT = ".d81";
	public final static String T64_EXT = ".t64";
	
	public final static String[] VALID_IMAGE_FILE_EXTENSTIONS = {
			D64_EXT, D64_EXT + GZIP_EXT, 
			D71_EXT, D71_EXT + GZIP_EXT, 
			D81_EXT, D81_EXT + GZIP_EXT, 
			T64_EXT, T64_EXT + GZIP_EXT
			};
		
	/** Type of C64 file (DEL, SEQ, PRG, USR, REL) */
	public static final String[] FILE_TYPES = { "DEL", "SEQ", "PRG", "USR", "REL", "CBM" };

	public static final int TYPE_DEL = 0;
	public static final int TYPE_SEQ = 1;
	public static final int TYPE_PRG = 2;
	public static final int TYPE_USR = 3;
	public static final int TYPE_REL = 4;
	public static final int TYPE_CBM = 5;	// C1581 partition
	
	/** Size of a disk block */
	protected static final int BLOCK_SIZE = 256;
	
	/** PETSCII padding white space character */
	public final static byte BLANK = (byte) 0xa0;
	/** CP/M used byte marker. Single density disks are filled with this value from factory. CP/M use this to detect empty disks are blank. */
	public final static byte UNUSED = (byte) 0xe5;
	/** Max size of a PRG file */
	protected static final int MAX_PRG = 65536;
	/** Eight masks used to mask a bit out of a byte. Starting with LSB. */
	public final static int[] BYTE_BIT_MASKS = { 0x01, 0x02, 0x04, 0x08, 0x10, 0x20, 0x40, 0x80 };
	/** Eight masks used to mask a bit out of a byte. Starting with MSB. */
	public final static int[] REVERSE_BYTE_BIT_MASKS = { 0x80, 0x40, 0x20, 0x10, 0x08, 0x04, 0x02, 0x01 };
	/** Eight masks used to mask all but one bits out of a byte. Starting with LSB. */
	public final static int[] INVERTED_BYTE_BIT_MASKS = { 254, 253, 251, 247, 239, 223, 191, 127 };

	protected final static String CPM_DISKNAME_1 = "CP/M PLUS";
	protected final static String CPM_DISKNAME_2 = "CP/M DISK";
	protected final static String CPM_DISKID_GCR = "65 2A";
	protected final static String CPM_DISKID_1581 = "80 3D";
	/** The GEOS label found in BAM sector on GEOS formatted images */
	protected final static String DOS_LABEL_GEOS = "GEOS format";
	
	/** PETSCII-ASCII mappings (ASCII to PETSCII mapping. Using 0x20 for invisible characters in PETSCII charset) */
	protected static final int[] PETSCII_TABLE = {
		0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, // 00-0f
		0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,	// 10-1f
		0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,	// 20-2f
		0x30, 0x31, 0x32, 0x33,	0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, // 30-3f
		0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,	0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, // 40-4f
		0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b,	0x5c, 0x5d, 0x5e, 0xa4, // 50-5f
		0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,	// 60-6f
		0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,	// 70-7f
		0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,	// 80-8f
		0x20, 0x20, 0x20, 0x20,	0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,	// 90-9f
		0x20, 0xa1, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xab, 0xac, 0xad, 0xae, 0xaf, // a0-af
		0xb0, 0xb1, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xbb, 0xbc, 0xbd, 0xbe, 0xbf,	// b0-bf
		0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, // c0-cf
		0x70, 0x71, 0x72, 0x73,	0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,	// d0-df
		0x20, 0xa1, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xab, 0xac, 0xad, 0xae, 0xaf,	// e0-ef
		0xb0, 0xb1, 0xb2, 0xb3,	0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xbb, 0xbc, 0xbd, 0xbe, 0xbf,	// f0-ff
		0x7e
	};
	/** The BASIC V7 tokens 0x80  to 0xFF */
	private final static String[] BASIC_V7_TOKENS = { 
			// 0x80 - 0xff
			"END",     "FOR",    "NEXT",   "DATA",    "INPUT#",  "INPUT",  "DIM",       "READ",
			"LET",     "GOTO",   "RUN",    "IF",      "RESTORE", "GOSUB",  "RETURN",    "REM",
			"STOP",    "ON",     "WAIT",   "LOAD",    "SAVE",    "VERIFY", "DEF",       "POKE",
			"PRINT#",  "PRINT",  "CONT",   "LIST",    "CLR",     "CMD",    "SYS",       "OPEN",
			"CLOSE",   "GET",    "NEW",    "TAB(",    "TO",      "FN",     "SPC(",      "THEN",
			"NOT",     "STEP",   "+",      "-",	      "*",       "/",      "^",         "AND",
			"OR",      ">",      "=",      "<",	      "SGN",     "INT",    "ABS",       "USR",
			"FRE",     "POS",    "SQR",    "RND",     "LOG",     "EXP",    "COS",       "SIN",
			"TAN",     "ATN",    "PEEK",   "LEN",     "STR$",    "VAL",    "ASC",       "CHR$",
			"LEFT$",   "RIGHT$", "MID$",   "GO",   /* End of BASIC V2 */
			                               "RGR",     "RCLR",   "POT??",     "JOY",
			"RDOT",    "DEC",    "HEX",    "ERR$",    "INSTR",   "ELSE",   "RESUME",    "TRAP",
			"TRON",    "TROFF",  "SOUND",  "VOL",     "AUTO",    "PUDEF",  "GRAPHIC",   "PAINT",
			"CHAR",    "BOX",    "CIRCLE", "GSHAPE",  "SSHAPE",  "DRAW",   "LOCATE",    "COLOR",
			"SCNCLR",  "SCALE",  "HELP",   "DO",      "LOOP",    "EXIT",   "DIRECTORY", "DSAVE",
			"DLOAD",   "HEADER", "SCRATCH","COLLECT", "COPY",    "RENAME", "BACKUP",    "DELETE",
			"RENUMBER","KEY",    "MONITOR","USING",   "UNTIL",   "WHILE",  "BANK??",    "PI"
	};	
	/** The second BASIC V7 tokens starting with 0xCE */
	private static final String[]  BASIC_V7_CE_TOKENS = {
			// 0x00 - 0x0A (invalid: 0x00, 0x01)
			null,   null,       "POT",     "BUMP",  "PEN",  "RSPPOS",  "RSPRITE",  "RSPCOLOR",
			"XOR",  "RWINDOW",  "POINTER"			
	};
	/** The second BASIC V7 tokens starting with 0xFE */
	private static final String[]  BASIC_V7_FE_TOKENS = {
		// 0x00 - 0x26 (invalid: 0x00, 0x01, 0x20, 0x022)
		null,       null,    "BANK",     "FILTER", "PLAY",    "TEMPO",  "MOVSPR", "SPRITE",
		"SPRCOLOR", "RREG",  "ENVELOPE", "SLEEP",  "CATALOG", "DOPEN",  "APPEND", "DCLOSE",
		"BSAVE",    "BLOAD", "RECORD",   "CONCAT", "DVERIFY", "DCLEAR", "SPRSAV", "COLLISION",
		"BEGIN",    "BEND",  "WINDOW",   "BOOT",   "WIDTH",   "SPRDEF", "QUIT",   "STASH",
		null,       "FETCH", null,       "SWAP",   "OFF",     "FAST",   "SLOW"
	};
	/** C1541 sector interleave. The gap between two blocks when saving a file */
	protected static final int C1541_INTERLEAVE = 10;
	/** C1571 sector interleave. The gap between two blocks when saving a file */
	protected static final int C1571_INTERLEAVE = 6;
	/** C1581 sector interleave. The gap between two blocks when saving a file */
	protected static final int C1581_INTERLEAVE = 1;
	/** Size of each directory entry on DIR_TRACK. */
	protected static final int DIR_ENTRY_SIZE = 32;
	/** Number of directory entries per directory sector */
	protected static final int DIR_ENTRIES_PER_SECTOR = 8;
	/** Maximum length of disk name */
	protected static final int DISK_NAME_LENGTH = 16;
	/** Maximum length of disk ID */
	protected static final int DISK_ID_LENGTH = 5;
	/** Size of buffer reading compressed data */
	private final static int INPUT_BUFFER_SIZE = 65536;
	/** Size of buffer writing uncompressed data to byte[] */
	private final static int OUTPUT_BUFFER_SIZE = 1048576;
	/** Size of a CP/M records (128 bytes) */
	protected final static int CPM_RECORD_SIZE = 128;
	/** Type of image (D64, D71, D81, CP/M ... ) */
	protected int imageFormat = UNKNOWN_IMAGE_TYPE;
	/** When True, this is a GEOS-formatted disk, therefore files must be saved the GEOS way. */
	protected boolean geosFormat = false;
	/** True if image is compressed */
	protected boolean compressed;
	/** Error messages are appended here, and get presented in GUI */
	protected StringBuffer feedbackMessage = new StringBuffer();
	/** Data of the whole image. */
	protected byte[] cbmDisk = null;
	/** Number of files in image */
	protected int fileNumberMax;
	/**
	 * A cbmFile holds all additional attributes (like fileName, fileType etc) for a file on the image.<br/>
	 * These attributes are used in the directory and are initialized in initCbmFiles() and filled with data in readDirectory().<br/>
	 * Their index is the directory-position they have in the image file (see readDirectory()). 
	 */
	protected CbmFile[] cbmFile = null;	//new CbmFile[FILE_NUMBER_LIMIT + 1]; 
	/** All attributes which are stored in the BAM of a image file - gets filled with data in readBAM() */
	protected CbmBam bam;
	/** The number of validation errors, or null is no validation has been done. */
	protected Integer errors = null;
	protected Integer warnings = null;
	protected List<ValidationError> validationErrorList = new ArrayList<ValidationError>();
	
	/**
	 * Get number of sectors on specified track
	 * @param trackNumber
	 * @return number of sectors on specified track.
	 */
	abstract public int getMaxSectors(int trackNumber);
	/**
	 * Get numbers of tracks on image.
	 * @return number of tracks.
	 */
	abstract public int getTrackCount();
	/**
	 * Get maximum number of sectors on any track.
	 * @return maximum number of sectors
	 */
	abstract public int getMaxSectorCount();
	/**
	 * Get data on block.
	 * @param track
	 * @param sector
	 * @return data from specified block
	 * @throws CbmException
	 */
	abstract public byte[] getBlock (int track, int sector) throws CbmException;
	/**
	 * Get number of free blocks.
	 * @return blocks free
	 */
	abstract public int getBlocksFree();
	/**
	 * Reads image file.
	 * @param filename	the filename
	 * @throws CbmException
	 */
	abstract protected void readImage(String filename) throws CbmException;
	/**
	 * Reads the BAM of the D64 image and fills bam[] with entries.
	*/
	abstract public void readBAM();
	/**
	 * Reads the directory of the image, fills cbmFile[] with entries.
	 */
	abstract public void readDirectory();
	
	/**
	 * Reads the directory of the partition
	 * @throws CbmException if partition is not supported on the image.
	 */
	abstract public void readPartition(int track, int sector, int numBlocks) throws CbmException;
	
	/**
	 * Get data of a single PRG file.<BR>
	 * @param number the file number in the image
	 * @return byte array file file contents
	 * @throws CbmException
	 */
	abstract public byte[] getFileData(int number) throws CbmException;
	/**
	 * Write the data of a single PRG file to image.
	 * @param saveData byte[]
	 * @return the first track/sector of the file (for use in directory entry).
	 */
	abstract protected TrackSector saveFileData(byte[] saveData);
	/**
	 * Set a disk name and disk-id in BAM.
	 * @param newDiskName the new name of the disk
	 * @param newDiskID the new id of the disk
	 */
	abstract protected void setDiskName(String newDiskName, String newDiskID);
	/**
	 * Copy attributes of bufferCbmFile to a directoryEntry in cbmDisk.
	 * @param cbmFile
	 * @param dirEntryNumber position where to put this entry in the directory
	 */
	abstract protected void writeDirectoryEntry(CbmFile cbmFile, int dirEntryNumber);
	/**
	 * 
	 * @param filename
	 * @param newDiskName
	 * @param newDiskID
	 * @return
	 */
	abstract public boolean saveNewImage(String filename, String newDiskName, String newDiskID);
	/**
	 * Add a directory entry of a single file to the image.<BR>
	 * @param cbmFile the CbmFile
	 * @param destTrack track where file starts
	 * @param destSector sector where file starts
	 * @param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	 * @param lengthInBytes
	 * @return returns true is adding the entry to the directory was successful
	 */
	public abstract boolean addDirectoryEntry(CbmFile cbmFile, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes);
	/**
	 * Parse BAM track bits and store allocated/free blocks as strings.
	 * @return String[track][sector]
	 */
	abstract public String[][] getBamTable();
	/** 
	 * Get offset to start of sector from beginning of image.
	 * @param track
	 * @param sector
	 * @return offset
	 * */
	abstract public int getSectorOffset(int track, int sector);
	/**
	 * Delete a file from disk image
	 * @param cbmFile The file to be deleted
	 * @throws CbmException
	 */
	abstract public void deleteFile(CbmFile cbmFile) throws CbmException;
	
	/**
	 * Validate image
	 * @param repairList list of error codes which should be corrected if found.
	 * @return number or validation errors
	 */
	abstract public Integer validate(List<Integer> repairList);

	
	/** Constructor _*/
	public DiskImage() {
	}

	/** 
	 * Initiate image structure. 
	 * @param fileNumberLimit
	 */
	protected void initCbmFile(int fileNumberLimit) {
		cbmFile = new CbmFile[fileNumberLimit + 1];
		for (int i = 0; i < fileNumberLimit+1; i++) {
			cbmFile[i] = new CbmFile();
		}
	}
	
	public boolean isCpmImage() {
		return 	imageFormat == D64_CPM_C64_IMAGE_TYPE ||
				imageFormat == D64_CPM_C128_IMAGE_TYPE ||
				imageFormat == D71_CPM_IMAGE_TYPE ||
				imageFormat == D81_CPM_IMAGE_TYPE;
	}
	
	/** 
	 * Load disk image from file. Use file name extension to identify type of disk image.
	 * @param filename file name
	 * @return DiskImage
	 * @throws CbmException if image could not be loaded (file missing, file corrupt out of memory etc).
	 */
	public static DiskImage getDiskImage(String filename) throws CbmException {
		String name = filename.toLowerCase();
		DiskImage diskImage = null;
		if (name.endsWith(D64_EXT) || name.endsWith(D64_EXT+GZIP_EXT) ) {
			diskImage = new D64();
			diskImage.readImage(filename);
		} else if (name.endsWith(D81_EXT) || name.endsWith(D81_EXT+GZIP_EXT) ) {
			diskImage = new D81();
			diskImage.readImage(filename);			
		} else if (name.endsWith(D71_EXT) || name.endsWith(D71_EXT+GZIP_EXT) ) {
			diskImage = new D71();
			diskImage.readImage(filename);	
		} else if (name.endsWith(T64_EXT) || name.endsWith(T64_EXT+GZIP_EXT) ) {
			diskImage = new T64();
			diskImage.readImage(filename);
		} else {
			throw new CbmException("Unknown file format.");
		}
		return diskImage;
	}
	
	public static  String checkFileNameExtension(int imageType, boolean compressed, String name) {
		String ext = null;
		switch (imageType) {
			case D64_IMAGE_TYPE: ext = D64_EXT + (compressed ? GZIP_EXT : "") ;	break;
			case D71_IMAGE_TYPE: ext = D71_EXT + (compressed ? GZIP_EXT : "") ;	break;
			case D81_IMAGE_TYPE: ext = D81_EXT + (compressed ? GZIP_EXT : "") ;	break;
			case T64_IMAGE_TYPE: ext = T64_EXT + (compressed ? GZIP_EXT : "") ;	break;
		}
		if (ext != null && name != null && !name.toLowerCase().endsWith(ext.toLowerCase())) {
			name = name + ext;
		}
		return name;
	}
	
	
	/** Get first bytes from a file.
	 * @param fileName
	 * @return int with the first bytes.
	 * @throws IOException
	 */
	private int getFileMagic(String fileName) throws IOException {
		byte[] buffer = new byte[4];
		InputStream is = Files.newInputStream(Paths.get(fileName));
		is.read(buffer);
		is.close();
		return ((buffer[1]&0xff) << 8) | (buffer[0]&0xff);
	}

	/**
	 * Read gzipped D64 image.
	 * @param fileName name of D64 file
	 * @throws CbmException
	 */
	private void readZippedFile(String fileName) throws CbmException {
		GZIPInputStream gis = null;
		try {
			FileInputStream fis = new FileInputStream(fileName);
			gis = new GZIPInputStream(fis, INPUT_BUFFER_SIZE);
			ByteArrayOutputStream bos = new ByteArrayOutputStream(OUTPUT_BUFFER_SIZE);
			while (gis.available() == 1) {
				bos.write(gis.read());
			}
			gis.close();
			gis = null;
			cbmDisk = bos.toByteArray();
			compressed = true;
			bos.close();
		} catch (FileNotFoundException e) {
			throw new CbmException("Failed to open file. "+e.getMessage());
		} catch (IOException e) {
			if (gis != null) {
				try {
					gis.close();
				} catch (IOException e2) { }
			}
			throw new CbmException("Failed to read file. "+e.getMessage());
		}
	}
	
	/**
	 * Load image from disk
	 * @param filename file name of disk image
	 * @param expectedFileSize if uncompressed image is smaller, then throw CbmException. If larger, print warning message.
	 * @param type the type of image to load. Used for logging.
	 * @throws CbmException
	 */
	protected void readImage(String filename, int expectedFileSize, String type) throws CbmException {
		feedbackMessage = new StringBuffer();
		FileInputStream input;
		int magic = 0;
		feedbackMessage.append("Trying to load "+type+" image ").append(filename).append("\n");
		this.cbmDisk = null;
		try {
			magic = getFileMagic(filename) & 0xffff;
		} catch (Exception e) {
			throw new CbmException("Failed to open header. "+ e.getMessage());
		}
		if (magic == GZIPInputStream.GZIP_MAGIC) {
			feedbackMessage.append("GZIP compressed file detected.\n");
			readZippedFile(filename);
		} else {
			File file = new File(filename); 
			if (file.isFile() == false) {
				throw new CbmException("File is not a regular file.");
			} else if (file.length() <= 0) {
				throw new CbmException("File is empty.");		
			} else if (file.length() > Integer.MAX_VALUE) {
				throw new CbmException("File is too large.");
			} else if (file.length() < expectedFileSize) {
				throw new CbmException("File smaller than normal size. A "+type+" file should be " + expectedFileSize + " bytes.");
			} else if (file.length() > expectedFileSize) {
				feedbackMessage.append("Warning: File larger than normal size. A "+type+" file should be ").append(expectedFileSize).append(" bytes.\n");
			}
			try {
				input = new FileInputStream(filename);
			} catch (Exception e){
				throw new CbmException("Failed to open file. "+e.getMessage());
			}
			try {
				this.cbmDisk = new byte[ (int)file.length() ];
				for (int i=0; i<cbmDisk.length; i++) {
					cbmDisk[i] = 0;
				}
				input.read( cbmDisk );
			} catch (Exception e){
				try {
					input.close();
				} catch (Exception e2) { }
				throw new CbmException("Failed to read file. "+e.getMessage());
			}
			try {
				input.close();
			} catch (Exception e){
				throw new CbmException("Failed to close file. "+e.getMessage());
			}
		}
		feedbackMessage.append(type+" disk image was loaded.\n");
	}
	
	/**
	 * Write cbmDisk byte[] as gzipped to output.
	 * @param output FileOutputStream to write gzip data to.
	 * @throws IOException in case or errors
	 */
	private void writeZippedImage(FileOutputStream output) throws IOException  {
		ByteArrayOutputStream byteStream = new ByteArrayOutputStream(cbmDisk.length);
		try {
			GZIPOutputStream zipStream = new GZIPOutputStream(byteStream);
			try {
				zipStream.write(cbmDisk);
			} finally {
				zipStream.close();
			}
		} finally {
			byteStream.close();
		}
		output.write(byteStream.toByteArray());
	}
		
	/**
	 * Reads a PRG file from hard disk<BR>
	 * globals written: feedbackMessage<BR>
	 * @param filename the filename
	 * @throws CbmException
	 */
	public byte[] readPRG(String filename) throws CbmException {
		feedbackMessage.append("readPRG: Trying to load program ").append(filename).append("... \n");
		File file = new File(filename);
		if (file.length() > MAX_PRG) {
			throw new CbmException(" File is too big for a PRG file (more than "+MAX_PRG+" bytes.");
		}
		if (!file.isFile()) {
			throw new CbmException("File is not a regular file.");
		} 
		byte[] saveData = new byte[(int) file.length()];
		if (file.length() > 0) {
			// Only attempt to read if file has contents. No need to read empty files.
			FileInputStream input;
			try {
				input = new FileInputStream(filename);
			} catch (Exception e) {
				throw new CbmException("Failed to open file. "+e.getMessage());
			}		
			try {			
				input.read( saveData );
			} catch (Exception e) {
				try {
					input.close();
				} catch (Exception e2) {}
				throw new CbmException("Could not read filedata.");
			}
			try {
				input.close();
			} catch (Exception e) {
				throw new CbmException("Failed to close file. "+e.getMessage());
			}
		}
		feedbackMessage.append("File is ").append(saveData.length).append(" bytes.");
		return saveData;
	}
	
	/**
	 * Extracts a file of the image to a single file in PRG format.
	 * The filename used in the disk image is converted and then used as output-filename.<BR>
	 * @param number number of directory entry
	 * @param directory the target directory
	 * @throws CbmException
	 */
	public void exportPRG(int number, String directory) throws CbmException {
		feedbackMessage = new StringBuffer();
		byte[] saveData = getFileData(number);
		String filename = pcFilename(cbmFile[number]);
		feedbackMessage.append("Saving ").append(directory).append(filename).append(" (").append(saveData.length).append(" bytes).\n");
		FileOutputStream output;
		try {
			output = new FileOutputStream(directory+filename);
		} catch (Exception e) {
			throw new CbmException("Failed to open output file. "+e.getMessage());
		}
		try {
			output.write(saveData, 0, saveData.length);
		} catch (Exception e) {
			try {
				output.close();
			} catch (Exception e2) {}
			throw new CbmException("Failed to write data. "+e.getMessage());
		}		
		try {
			output.close();
		} catch (Exception e){
			throw new CbmException("Failed to close file. "+e.getMessage());
		}
		feedbackMessage.append("OK.\n");
	}
	
	
	/**
	 * Write the data and the directory entry of a single PRG file to disk image.
	 * @param cbmFile
	 * @param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	 * @param saveData the data to write to the file
	 * @return true if writing was successful (if there was enough space on disk image etc)
	 */
	public boolean saveFile(CbmFile cbmFile, boolean isCopyFile, byte[] saveData) {
		feedbackMessage = new StringBuffer();
		if (isCpmImage()) {
			feedbackMessage.append("saveFile: Not yet implemented for CP/M format.\n");
			return false;
		}
		if (isCopyFile == false) {
			if (cbmFile.getName().toLowerCase().endsWith(".prg")) {
				cbmFile.setName(cbmFile.getName().substring(0, cbmFile.getName().length()-4));				
			}
		}
		TrackSector firstBlock;		
		if (cbmFile.getFileType() == DiskImage.TYPE_DEL && saveData.length == 0) {
			feedbackMessage.append("saveFile: '").append(cbmFile.getName()).append("'  (empty DEL file)\n");
			firstBlock = new TrackSector(0, 0);
		} else {
			feedbackMessage.append("saveFile: '").append(cbmFile.getName()).append("'  ("+saveData.length+" bytes)\n");
			firstBlock = saveFileData(saveData);
		}
		if (firstBlock != null) {
			if (addDirectoryEntry(cbmFile, firstBlock.track, firstBlock.sector, isCopyFile, saveData.length)) {
				return true;
			}
		} else {
			feedbackMessage.append("saveFile: Error occurred.\n");
		}
		return false;
	}
	
	/**
	 * Renames a disk image (label) <BR>
	 * globals_written: cbmDisk<BR>
	 * @param filename	the filename
	 * @param newDiskName	the new name (label) of the disk
	 * @param newDiskID	the new disk-ID
	 * @return <code>true</code> when writing of the D64 file was successful
	 */
	public boolean renameImage(String filename, String newDiskName, String newDiskID){
		feedbackMessage = new StringBuffer("renameD64(): ").append(newDiskName).append(", ").append(newDiskID);
		if (isCpmImage()) {
			feedbackMessage.append("Not yet implemented for CP/M format.\n");
			return false;
		}
		setDiskName(cbmFileName(newDiskName), cbmFileName(newDiskID));
		return writeImage(filename);
	}
	
	/**
	 * Sets new attributes for a single PRG file.<BR>
	 * globals written: cbmFile, feedbackMessage<BR>
	 * @param cbmFileNumber which PRG to rename
	 * @param newPRGName the new name of the PRG-file
	 * @param newPRGType the new type of the PRG-file
	 */
	public void renamePRG(int cbmFileNumber, String newPRGName, int newPRGType) {
		feedbackMessage.append("renamePRG: oldName '").append(cbmFile[cbmFileNumber].getName()).append(" newName '").append(newPRGName).append("'\n");
		CbmFile newFile = new CbmFile(cbmFile[cbmFileNumber]);		
		newFile.setName(cbmFileName(newPRGName));
		newFile.setFileType(newPRGType);
		writeDirectoryEntry(newFile, newFile.getDirPosition());
	}
	
	/**
	 * 
	 * @param previousFile the previously found entry, or null if nothing yet.
	 * @param pos offset into disk image
	 * @return CpmFile if a new file entry was found and prepared. If previous was updated or entry was scratched null is returned.
	 */
	protected CpmFile getCpmFile(CpmFile previousFile, int pos, boolean use16bitau) {
		CpmFile newFile = null;
		int userNum = cbmDisk[pos + 0x00] & 0xff;
		char fileName[] = new char[8];
		for (int n=0; n<fileName.length; n++) {
			fileName[n] = (char) (cbmDisk[pos + 0x01 + n] & 0x7f);
		}
		char fileExt[] = new char[3];
		for (int n=0; n<fileExt.length; n++) {
			fileExt[n] = (char) (cbmDisk[pos + 0x09 + n] & 0x7f);
		}
		boolean readOnly = (cbmDisk[pos + 0x09] & 0x80 ) == 0x80 ? true : false;
		boolean hidden   = (cbmDisk[pos + 0x0a] & 0x80 ) == 0x80 ? true : false;
		boolean archive  = (cbmDisk[pos + 0x0b] & 0x80 ) == 0x80 ? true : false;
		int extNum       =  cbmDisk[pos + 0x0c] & 0xff | ((cbmDisk[pos + 0x0e] & 0xff) << 8);
		int s1           =  cbmDisk[pos + 0x0d] & 0xff;	// Last Record Byte Count
		int rc           =  cbmDisk[pos + 0x0f] & 0xff;	// Record Count
		
		String name = new String(fileName);
		String nameExt = new String(fileExt);
		CpmFile tempFile = null;
		
		if (userNum >=0x00 && userNum <= 0x0f) {
			// Obviously, extNum is in numerical order, but it doesn't always start with 0, and it can skip some numbers.
			if (previousFile == null || !(previousFile.getCpmName().equals(name) && previousFile.getCpmNameExt().equals(nameExt)) ) {
				newFile = new CpmFile();
				newFile.setName(name + "." + nameExt);
				newFile.setFileType(TYPE_PRG);
				newFile.setCpmName(name);
				newFile.setCpmNameExt(nameExt);
				newFile.setReadOnly(readOnly);
				newFile.setArchived(archive);
				newFile.setHidden(hidden);
				newFile.setFileScratched(false);
				newFile.setSizeInBlocks(rc);
				newFile.setSizeInBytes(rc * CPM_RECORD_SIZE);
				tempFile = newFile;
			} else if (previousFile != null) {
				previousFile.setSizeInBlocks(previousFile.getSizeInBlocks() + rc);
				previousFile.setSizeInBytes(previousFile.getSizeInBlocks() * CPM_RECORD_SIZE);
				tempFile = previousFile;
			}
			if (tempFile != null) {
				tempFile.setLastExtNum(extNum);
				tempFile.setLastRecordByteCount(s1);
				tempFile.setRecordCount(extNum*128 + rc);
				if (use16bitau) {
					for (int al=0; al<8; al++) {
						int au = ((cbmDisk[pos + 0x10 + al*2+1] & 0xff)<< 8) | (cbmDisk[pos + 0x10 + al*2+0] & 0xff);
						if (au != 0) {
							tempFile.addAllocUnit(au);
						}
					}					
				} else {
					for (int al=0; al<16; al++) {
						int au = cbmDisk[pos + 16 + al] & 0xff;
						if (au != 0) {
							tempFile.addAllocUnit(au);
						}
					}
				}
			}
		} else if (userNum != (UNUSED & 0xff)) {
			// 0x10 - 0x1f: password entries
			// 0x20: dir label
			// 0x21: timestamp			
			//System.out.println("getCpmFile: unsupported userNum "+userNum);
		}
		
//		if (userNum != 0xe5) { 
//			StringBuffer buf = new StringBuffer(64);
//			buf.append(userNum).append('\t').append(name).append('\t').append(nameExt).append('\t').append(extNum).append('\t').append(rc).append("\t: ");
//			for (int i=0; i<16; i++) {
//				buf.append(HexTableModel.HEX[cbmDisk[pos+16+i]&0xff]).append(' ');
//			}
//			
//			if (tempFile != null) {
//				buf.append(" : ");
//				for (int i=0; i<tempFile.getAllocList().size(); i++) {
//					buf.append(tempFile.getAllocList().get(i)).append(' ');
//				}
//			}
//			System.out.println(buf.toString());
//		}
		return newFile;
	}
	
	/**
	 * Set up variables in a new cbmFile which will be appended to the directory.
	 * These variables will inserted into the directory later.<BR>
	 * globals needed: fileNumberMax<BR>
	 * @param thisFilename this file name
	 * @param thisFiletype  this file type
	 * @param destTrack track number
	 * @param destSector sector number
	 * @param lengthInBytes file length in bytes
	 */
	protected void setNewDirEntry(CbmFile cbmFile, String thisFilename, int thisFileType, int destTrack, int destSector, int lengthInBytes) {
		cbmFile.setFileScratched(false);
		cbmFile.setFileType(thisFileType);
		cbmFile.setFileLocked(false);
		cbmFile.setFileClosed(true);
		cbmFile.setTrack(destTrack);
		cbmFile.setSector(destSector);
		cbmFile.setName(thisFilename);
		cbmFile.setRelTrack( 0);		//TODO: relative files
		cbmFile.setRelSector( 0);	//TODO: relative files
		for (int i = 0; i < 7; i++) {
			cbmFile.setGeos(i,0);		//TODO: GEOS files
		}
		cbmFile.setSizeInBytes(lengthInBytes);
		cbmFile.setSizeInBlocks( ((cbmFile.getSizeInBytes()-2)/254)	);
		if ( ((cbmFile.getSizeInBytes()-2) % 254) >0 ) {
			cbmFile.setSizeInBlocks(cbmFile.getSizeInBlocks()+1);
		}
	}
	
	/**
	 * Get PC filename from a CbmFile.
	 * @param cbmFile the CBM file
	 * @return the PC filename
	 */	
	public static String pcFilename(CbmFile cbmFile) {
		final String VALID_CHARS = "abcdefghijklmnopqrstuvwxyz0123456789 !#$%&()-@_{}.";
		if (cbmFile instanceof CpmFile) {
			return ((CpmFile)cbmFile).getCpmNameAndExt();
		} else {		
			String fileName = cbmFile.getName().toLowerCase();
			for (int i = 0; i < fileName.length(); i++) {
				if (VALID_CHARS.indexOf(fileName.charAt(i)) == -1) {				
					fileName = fileName.substring(0, i) + "_" + fileName.substring(i + 1, fileName.length()); 
				}
			}
			return fileName + "." + FILE_TYPES[cbmFile.getFileType()].toLowerCase();
		}
	}
	
	/**
	 * Convert a PC filename to a proper CBM filename.<BR>
	 * @param orgName orgName
	 * @return the CBM filename
	 */
	public static String cbmFileName(String orgName) {
		final String VALID_CHARS = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 []()/;:<>.-_+&%$@#!";
		char[] fileName = new char[DISK_NAME_LENGTH];
		int out = 0;
		for (int i=0; i<DISK_NAME_LENGTH && i<orgName.length(); i++) {
			char c = Character.toUpperCase(orgName.charAt(i));
			if (VALID_CHARS.indexOf(c) >= 0) {
				fileName[out++] = c;
			}
		}
		return new String(Arrays.copyOfRange(fileName, 0, out));
	}
	
	/**
	 * Get <code>Disk</code> instance of current image. This is used when saving to database.
	 * @return Disk
	 */
	public Disk getDisk() {
		Disk disk = new Disk();
		disk.setLabel(getBam().getDiskName());
		disk.setImageType(imageFormat);
		disk.setErrors(errors);
		disk.setWarnings(warnings);
		for (int filenumber = 0; filenumber <= getFilenumberMax() - 1;	filenumber++) {
			boolean isLocked = getCbmFile(filenumber).isFileLocked();
			boolean isClosed = getCbmFile(filenumber).isFileClosed();
			DiskFile file = new DiskFile();
			file.setName(getCbmFile(filenumber).getName());
			file.setSize(getCbmFile(filenumber).getSizeInBlocks());
			file.setFileType(getCbmFile(filenumber).getFileType());
			file.setFileNum(filenumber);
			file.setFlags((isLocked ? DiskFile.FLAG_LOCKED : 0) | (isClosed ? 0 : DiskFile.FLAG_NOT_CLOSED));
			disk.getFileList().add(file);
		}
		return disk;
	}

	/**
	 * Return a string from a specified position on a block and having the specified length.
	 * @param track track
	 * @param sector sector
	 * @param pos position within block
	 * @param length the length of the returned string
	 * @return String, or null if outside of disk image.
	 */
	private String getStringFromBlock(int track, int sector, int pos, int length) {
		int dataPos = getSectorOffset(track, sector) + pos;
		if (dataPos + length < cbmDisk.length) {
			return new String(Arrays.copyOfRange(cbmDisk, dataPos, dataPos + length));
		} else {
			return null;
		}
	}
	
	protected String getTrimmedString(int pos, int length) {
		byte[] tmp = new byte[length];
		for (int i=0; i< length; i++) {
			byte b = cbmDisk[pos+i];
			tmp[i] = b==0xa0 ? 0x20 : b;
		}
		try {
			return new String(tmp, "ISO-8859-1").trim();
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
			return null;
		}		
	}
	
	/**
	 * Checks, sets and return image format.
	 * @return image format
	 */
	public int checkImageFormat() {
		if (bam.getDiskName()!= null && (CPM_DISKNAME_1.equals(bam.getDiskName().trim()) || CPM_DISKNAME_2.equals(bam.getDiskName().trim()))) {
			if (CPM_DISKID_GCR.equals(bam.getDiskId())) {
				if ("CBM".equals(getStringFromBlock(1, 0, 0, 3))) {
					if (this instanceof D71 && (getCbmDiskValue(BLOCK_SIZE - 1) & 0xff) == 0xff) {
						feedbackMessage.append("CP/M C128 double sided disk detected.\n");
						imageFormat = D71_CPM_IMAGE_TYPE;
						return imageFormat;
					} else if (this instanceof D64) {
						feedbackMessage.append("CP/M C128 single sided disk detected.\n");
						imageFormat = D64_CPM_C128_IMAGE_TYPE;
						return imageFormat;
					}
				} else if (this instanceof D64 ) {
					feedbackMessage.append("CP/M C64 single sided disk detected.\n");					
					imageFormat = D64_CPM_C64_IMAGE_TYPE;
					return imageFormat;
				}
			} else if (this instanceof D81 && CPM_DISKID_1581.equals(bam.getDiskId())) {
				feedbackMessage.append("CP/M 3.5\" disk detected.\n");				
				imageFormat = D81_CPM_IMAGE_TYPE;
				return imageFormat;
			}
		}
		if (this instanceof D64) {
			imageFormat = D64_IMAGE_TYPE;			
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D64.BAM_TRACK, D64.BAM_SECTOR, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof D71) {
			imageFormat = D71_IMAGE_TYPE;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D71.BAM_TRACK_1, D71.BAM_SECT, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof D81) {
			imageFormat = D81_IMAGE_TYPE;
			geosFormat = DOS_LABEL_GEOS.equals(getStringFromBlock(D81.HEADER_TRACK, D81.HEADER_SECT, 0xad, DOS_LABEL_GEOS.length()));
		} else if (this instanceof T64) {
			imageFormat = T64_IMAGE_TYPE;
			geosFormat = false;
		} else {
			imageFormat = UNKNOWN_IMAGE_TYPE;
			geosFormat = false;
		}
		if (geosFormat) {
			feedbackMessage.append("GEOS formatted image detected.\n");			
		}
		return imageFormat;
	}
	
	/**
	 * Writes a image to file system<BR>
	 * @param filename the filename
	 * @return true if successfully written
	 */
	public boolean writeImage(String filename) {
		if (cbmDisk == null) {
			feedbackMessage.append("No disk data. Nothing to write.\n");
			return false;
		}
		feedbackMessage.append("writeImage: Trying to save ").append(compressed ? " compressed " : "").append(filename).append("... \n");
		boolean success = true;
		FileOutputStream output;
		try {
			output = new FileOutputStream(filename);
		} catch (Exception e) {
			feedbackMessage.append("Error: Could not open file for writing.\n");
			feedbackMessage.append(e.getMessage()).append("\n");
			return false;
		}
		try {
			if (compressed) {
				writeZippedImage(output);
			} else {
				output.write(cbmDisk);
			}
			feedbackMessage.append("Saved image file.\n");			
		} catch (Exception e){
			feedbackMessage.append("Error: Could not write filedata.\n");
			feedbackMessage.append(e.getMessage()).append("\n");
			success = false;
		}
		try {
			output.close();
		} catch (Exception e){
			feedbackMessage.append("Error: Could not close file.\n");
			feedbackMessage.append(e.getMessage()).append("\n");
			return false;
		}
		return success;
	}
	
	/**
	 * Switch directory locations of two files to move one of them upwards and the other downwards in the listing.
	 * @param cbmFile1
	 * @param cbmFile2
	 */
	public void switchFileLocations(CbmFile cbmFile1, CbmFile cbmFile2) {
		if (!isCpmImage()) {
			feedbackMessage.append("DiskImage.switchFileLocations: '"+cbmFile1.getName() + "'  '"+cbmFile2.getName()+"'\n");
			int tmpDirTrack = cbmFile2.getDirTrack();
			int tmpDirSector = cbmFile2.getDirSector();
			cbmFile2.setDirTrack(cbmFile1.getDirTrack());		
			cbmFile2.setDirSector(cbmFile1.getDirSector());
			cbmFile1.setDirTrack(tmpDirTrack);
			cbmFile1.setDirSector(tmpDirSector);
			writeDirectoryEntry(cbmFile1, cbmFile2.getDirPosition());
			writeDirectoryEntry(cbmFile2, cbmFile1.getDirPosition());
		}
	}
	
	public static String parseCbmBasicPrg(byte[] prg) {
		if (prg == null || prg.length < 4) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
		int loadAddr = (prg[0] & 0xff) | ((prg[1] & 0xff) << 8);
		//feedbackMessage.append("parseCbmBasicPrg: loadAddr=0x"+Integer.toHexString(loadAddr));
		int pos = 2;
		int nextLine;
		do {
			nextLine = (prg[pos + 0] & 0xff) | ((prg[pos + 1] & 0xff)<<8);
			if (nextLine != 0x000) {
				int lineNum = (prg[pos + 2] & 0xff) | ((prg[pos + 3] & 0xff) << 8);
				buf.append(lineNum).append(' ');
				int quoteCount = 0;
				for (int i = pos + 4; i<prg.length && (prg[i] & 0xff) != 0x00; i++) {
					int op = prg[i] & 0xff;
					
					
					if ((quoteCount & 1) == 0 && op >= 0x80 && op <= 0xff) {
						if (op == 0xce) {
							int op2 = prg[++i] & 0xff;
							if (op2 >=0 && op2 < BASIC_V7_CE_TOKENS.length && BASIC_V7_CE_TOKENS[op2] != null) {
								buf.append(BASIC_V7_CE_TOKENS[op2]);
							} else {
								buf.append("0xCE").append(Integer.toHexString(op2)).append(")");
							}
						} else if (op == 0xfe) {
							int op2 = prg[++i] & 0xff;
							if (op2 >=0 && op2 < BASIC_V7_FE_TOKENS.length && BASIC_V7_FE_TOKENS[op2] != null) {
								buf.append(BASIC_V7_FE_TOKENS[op2]);
							} else {
								buf.append("0xFE").append(Integer.toHexString(op2)).append(")");
							}						
						} else {
							buf.append(BASIC_V7_TOKENS[op - 0x80]);
						}
					//if ((quoteCount & 1) == 0 && op >= 0x80 && op <= 0xcb) {
					//	buf.append(BASIC_V2_TOKENS[op - 0x80]);
					} else if((quoteCount & 1) == 0 && op == 0xff) {
						buf.append("PI");
					} else {
						if (op == 0x22) {
							quoteCount++;
						}
						buf.append(Character.toChars(op));
					}
				}
				buf.append('\n');
				pos = nextLine - loadAddr + 2;
			}
		} while (nextLine != 0x000 && pos < (prg.length-2) && pos>=0);
		return buf.toString();
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
	 * Get byte from a position within disk image.
	 * @param position
	 * @return data at position, or 0 if position is not within the size of image.
	 */
	protected int getCbmDiskValue(int position){ 
		try {
			return (cbmDisk[ position ] & 0xff);
		} catch (ArrayIndexOutOfBoundsException e) {
			// TODO FIXME: handle this better. Can end up here on broken disk images.
			System.err.println("D64.getCbmDiskValue:" + e);
			return 0;
		}
	}

	/**
	 * Set a byte at a position on the disk image.
	 * @param position
	 * @param value
	 */
	protected void setCbmDiskValue(int  position, int value){ 
		if (cbmDisk != null) {
			cbmDisk[ position] = (byte) value;
		}
	}
	
	/**
	 * @return String
	 */
	public String getFeedbackMessage() {
		String res = feedbackMessage.toString();
		feedbackMessage = new StringBuffer();
		return res;
	}
	
	/**
	 * @param string
	 */
	public void setFeedbackMessage(String string) {
		feedbackMessage = new StringBuffer(string);
	}
	
	/**
	 * @return
	 */
	public int getFilenumberMax() {
		return fileNumberMax;
	}
	
	/**
	 * @return
	 */
	public CbmFile getCbmFile(int number) {
		if (number<cbmFile.length && number >= 0) {
			return cbmFile[number];
		} else {
			return null;
		}
	}

	/**
	 * @param number
	 * @param file
	 */
	public void setCbmFile(int number, CbmFile file) {
		cbmFile[number] = file;
	}
	
	/**
	 * @return
	 */
	public CbmBam getBam() {
		return bam;
	}
	
	/**
	 * @return
	 */
	public static String getFileType(int type) {
		return  type < FILE_TYPES.length ? FILE_TYPES[type] : null;
	}
	
	public void setCompressed(boolean compressed) {
		this.compressed  = compressed;
	}

	public int getImageFormat() {
		return imageFormat;
	}
	
	public void setImageFormat(int imageFormat) {
		this.imageFormat  = imageFormat;
	}
	
	public static boolean isImageFileName(File f) {
		if (f.isDirectory()) {
			return false;
		} else {
			for (int i=0; i<DiskImage.VALID_IMAGE_FILE_EXTENSTIONS.length; i++) {
				if (f.getName().toLowerCase().endsWith(DiskImage.VALID_IMAGE_FILE_EXTENSTIONS[i])) {
					return true;
				}
			}
			return false;
		}
	}
	
	/** checks if fileName ends with .del, .seq, .prg, .usr or .rel and returns the corresponding file type.
	 * If there is no matching file extension, TYPE_PRG is return
	 * @param fileName
	 * @return file type
	 */
	public static int getFileTypeFromFileExtension(String fileName) {
		String name = fileName != null ? fileName.toLowerCase() : "";
		if (name.endsWith(".del")) {
			return TYPE_DEL;
		} else if (name.endsWith(".seq")) {
			return TYPE_SEQ;
		} else if (name.endsWith(".usr")) {
			return TYPE_USR;
		} else if (name.endsWith(".rel")) {
			return TYPE_REL;
		} else {
			return TYPE_PRG;
		}
	}
	
	/**
	 * @return the number of validation errors, or null if validation has not been performed.
	 */
	public Integer getErrors() {
		return this.errors;
	}

	public Integer getWarnings() {
		return warnings;
	}
	
	public List<ValidationError> getValidationErrorList() {
		return validationErrorList;
	}
	
}
