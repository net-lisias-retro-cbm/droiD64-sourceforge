package droid64.d64;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
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

	public static final int D64_IMAGE_TYPE = 1;
	public static final int D81_IMAGE_TYPE = 2;
	public static final int D71_IMAGE_TYPE = 3;
	public static final int T64_IMAGE_TYPE = 4;
	
	/** Size of a disk block */
	protected static final int BLOCK_SIZE = 256;
	
	private final static String GZIP_EXT = ".gz";
	private final static String D64_EXT = ".d64";
	private final static String D71_EXT = ".d71";
	private final static String D81_EXT = ".d81";
	private final static String T64_EXT = ".t64";
	
	public final static String[] VALID_IMAGE_FILE_EXTENSTIONS = {
			D64_EXT, D64_EXT + GZIP_EXT, 
			D71_EXT, D71_EXT + GZIP_EXT, 
			D81_EXT, D81_EXT + GZIP_EXT, 
			T64_EXT, T64_EXT + GZIP_EXT
			};
		
	/** Type of C64 file (DEL, SEQ, PRG, USR, REL) */
	public static final String[] FILE_TYPES = { "DEL", "SEQ", "PRG", "USR", "REL" };

	public static final int TYPE_DEL = 0;
	public static final int TYPE_SEQ = 1;
	public static final int TYPE_PRG = 2;
	public static final int TYPE_USR = 3;
	public static final int TYPE_REL = 4;
	
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
	
	/** PETSCII-ASCII mappings */
	protected static final int[] PETSCII_TABLE = {
		//Invisible
		0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 
		0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
		//Visible
		0x20, 0x21, 0x22, 0x23, 0x24, 0x25, 0x26, 0x27, 0x28, 0x29, 0x2a, 0x2b, 0x2c, 0x2d, 0x2e, 0x2f,
		0x30, 0x31, 0x32, 0x33,	0x34, 0x35, 0x36, 0x37, 0x38, 0x39, 0x3a, 0x3b, 0x3c, 0x3d, 0x3e, 0x3f, 
		0x40, 0x41, 0x42, 0x43, 0x44, 0x45, 0x46, 0x47,	0x48, 0x49, 0x4a, 0x4b, 0x4c, 0x4d, 0x4e, 0x4f, 
		0x50, 0x51, 0x52, 0x53, 0x54, 0x55, 0x56, 0x57, 0x58, 0x59, 0x5a, 0x5b,	0x5c, 0x5d, 0x5e, 0x5f, 
		0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f,
		0x70, 0x71, 0x72, 0x73, 0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
		//Invisible
		0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
		0x20, 0x20, 0x20, 0x20,	0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20, 0x20,
		0x20,
		//Visible
		      0xa1, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xab, 0xac, 0xad, 0xae, 0xaf, 
		0xb0, 0xb1, 0xb2, 0xb3, 0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xbb, 0xbc, 0xbd, 0xbe, 0xbf,
		// CBM font trickery: codes 0xc0 - 0xdf are 0x60 - 0x7f
		// 0xc0, 0xc1, 0xc2, 0xc3, 0xc4, 0xc5, 0xc6, 0xc7, 0xc8, 0xc9, 0xca, 0xcb, 0xcc, 0xcd, 0xce, 0xcf,
		// 0xd0, 0xd1, 0xd2, 0xd3, 0xd4, 0xd5, 0xd6, 0xd7, 0xd8, 0xd9, 0xda, 0xdb, 0xdc, 0xdd, 0xde, 0xdf,
		0x60, 0x61, 0x62, 0x63, 0x64, 0x65, 0x66, 0x67, 0x68, 0x69, 0x6a, 0x6b, 0x6c, 0x6d, 0x6e, 0x6f, 
		0x70, 0x71, 0x72, 0x73,	0x74, 0x75, 0x76, 0x77, 0x78, 0x79, 0x7a, 0x7b, 0x7c, 0x7d, 0x7e, 0x7f,
		// CBM font trickery: codes 0xe0 - 0xfe are 0xa0 - 0xbe
		// 0xe0, 0xe1, 0xe2, 0xe3, 0xe4, 0xe5, 0xe6, 0xe7, 0xe8, 0xe9, 0xea, 0xeb, 0xec, 0xed, 0xee, 0xef,
		// 0xf0, 0xf1, 0xf2, 0xf3, 0xf4, 0xf5, 0xf6, 0xf7, 0xf8, 0xf9, 0xfa, 0xfb, 0xfc, 0xfd, 0xfe,
		0x20, 0xa1, 0xa2, 0xa3, 0xa4, 0xa5, 0xa6, 0xa7, 0xa8, 0xa9, 0xaa, 0xab, 0xac, 0xad, 0xae, 0xaf,
		0xb0, 0xb1, 0xb2, 0xb3,	0xb4, 0xb5, 0xb6, 0xb7, 0xb8, 0xb9, 0xba, 0xbb, 0xbc, 0xbd, 0xbe, 0xbf,
		// CBM font trickery: codes 0xff is 0x7e
		0x7e
	};
	
	public static final int C1541_INTERLEAVE = 10;
	public static final int C1571_INTERLEAVE = 6;
	public static final int C1581_INTERLEAVE = 1;
	
	/** Not a CP/M image */
	public final static int CPM_TYPE_UNKNOWN  = 0;	
	/** CP/M for C64 on a D64 image */
	public final static int CPM_TYPE_D64_C64  = 1;
	/** CP/M for C128 on a D64 image */
	public final static int CPM_TYPE_D64_C128 = 2;
	/** CP/M on a D71 image */
	public final static int CPM_TYPE_D71      = 3;
	/** CP/M on a D81 image */
	public final static int CPM_TYPE_D81      = 4;
	
	/** Size of each directory entry on DIR_TRACK. */
	protected static final int DIR_ENTRY_SIZE = 32;
	/** Number of directory entries per directory sector */
	protected static final int DIR_ENTRIES_PER_SECTOR = 8;
	/** Maximum length of disk name */
	protected static final int DISK_NAME_LENGTH = 16;
	/** Maximum length of disk ID */
	protected static final int DISK_ID_LENGTH = 5;
	/** When True, this is a GEOS-formatted disk, therefore files must be saved the GEOS way. */
	protected boolean geosFormat = false;
	/** When true, this is a CP/M formatted disk. */
	protected int cpmFormat = CPM_TYPE_UNKNOWN;
	/** True if image is compressed */
	protected boolean compressed;
	/** Error messages are appended here, and get presented in GUI */
	protected StringBuffer feedbackMessage = new StringBuffer();
	/** Data of the whole image. */
	protected byte[] cbmDisk = null;
	/** Number of files in image */
	protected int fileNumberMax;
	/**
	 * A cbmFile holds all additional attributes (like fileName, fileType etc) for a PRG-file the image.
	 * These attributes are used in the directory.
	 * these are initialized in initCbmFiles() and filled with data in readDirectory()
	 * their index is the directory-position they have in the image file (see readDirectory()) 
	 */
	protected CbmFile[] cbmFile = null;	//new CbmFile[FILE_NUMBER_LIMIT + 1]; 
	/** All attributes which are stored in the BAM of a image file - gets filled with data in readBAM() */
	protected CbmBam bam;
	/** Used by insertPRG and CopyPRG */
	protected CbmFile bufferCbmFile = new CbmFile();
	/** Destination Track  */
	protected int destTrack;
	/** Destination Sector */
	protected int destSector;
	
	private final static int INPUT_BUFFER_SIZE = 65536;
	private final static int OUTPUT_BUFFER_SIZE = 1048576;
	
	/** Size of a CP/M records (128 bytes) */
	protected final static int CPM_RECORD_SIZE = 128;
	
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
	 * Fills saveData with data of a single PRG file.<BR>
	 * @param number the file number in the image
	 * @throws CbmException
	 */
	abstract public byte[] writeSaveData(int number) throws CbmException;
	/**
	 * Write the data of a single PRG file to image.
	 * @param saveData byte[]
	 * @return true if writing was successful.
	 */
	abstract protected boolean saveFileData(byte[] saveData);
	/**
	 * Set a disk name and disk-id in BAM.
	 * @param newDiskName the new name of the disk
	 * @param newDiskID the new id of the disk
	 */
	abstract protected void setDiskName(String newDiskName, String newDiskID);
	/**
	 * Copy attributes of bufferCbmFile to a directoryEntry in cbmDisk.
	 * @param dirEntryNumber position where to put this entry in the directory
	 */
	abstract protected void writeDirectoryEntry(int dirEntryNumber);
	/**
	 * 
	 * @param filename
	 * @param newDiskName
	 * @param newDiskID
	 * @return
	 */
	abstract public boolean saveNewImage(String filename, String newDiskName, String newDiskID);
	/**
	 * Add a directory entry of a single PRG file to the image.<BR>
	 * @param thisFilename the filename
	 * @param thisFiletype the type of the file
	 * @param destTrack track where file starts
	 * @param destSector sector where file starts
	 * @param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	 * @param lengthInBytes
	 * @return returns true is adding the entry to the directory was successful
	 */
	abstract protected boolean addDirectoryEntry(String thisFilename, int thisFiletype, int destTrack, int destSector, boolean isCopyFile, int lengthInBytes);
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
	
	/**
	 * Used when creating a new disk image. CP/M format is normally assigned when image is loaded from disk.
	 * @param cpmFormat
	 * @see #getCpmFormat()
	 */
	public void setCpmFormat(int cpmFormat) {
		this.cpmFormat = cpmFormat;
	}

	/**
	 * Get CP/M format
	 * @return CP/M format
	 * @see #CPM_TYPE_UNKNOWN
	 * @see #CPM_TYPE_D64_C64
	 * @see #CPM_TYPE_D64_C128
	 * @see #CPM_TYPE_D71
	 * @see #CPM_TYPE_D81
	 * 
	 */
	public int getCpmFormat() {
		return cpmFormat;
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
	 * Converts a CBM filename to a PC filename.
	 * @param fileName the CBM file name
	 * @param fileType the CBM file type
	 * @return the PC filename
	 */
	private String pcFilename(String fileName, String fileType) {
		String permitted_chars = ( "abcdefghijklmnopqrstuvwxyz0123456789" );
		for (int position = 0; position < fileName.length(); position++) {
			fileName = fileName.toLowerCase();	//filename[position] := chr( ord(filename[position]) or 32);
			boolean char_permitted = false;
			for (int cnt = 0; cnt < permitted_chars.length(); cnt++) {
				if ( fileName.charAt(position) == permitted_chars.charAt(cnt) ) {
					char_permitted = true;
				}
			}
			if ( char_permitted == false) {
				fileName = fileName.substring(0,position) + "_" + fileName.substring(position+1, fileName.length()); 
			}
		}
		fileName = fileName + "." + fileType.toLowerCase();
		/*		for (position = 1; position <= fileType.length(); position++) {
			fileName = fileName + (char)
		  filename := filename + chr( ord(FileType[cbmFile[number].file_type][position]) or 32); //pascal code
		}
		 */
		return fileName;
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
		byte[] saveData = writeSaveData(number);
		String filename;
		if (cpmFormat != CPM_TYPE_UNKNOWN && cbmFile[number] instanceof CpmFile) {
			filename = ((CpmFile)cbmFile[number]).getCpmNameAndExt();
		} else {
			filename = pcFilename( cbmFile[number].getName(), FILE_TYPES[ cbmFile[number].getFileType() ]);
		}
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
	 * Write the data and the directory entry of a single PRG file to D64-image.<BR>
	 * globals needed: dest_track, dest_sector<BR>
	 * calls: cbmFileName, saveFileData, addDirectoryEntry<BR>
	 * @param thisFileName the file name to write
	 * @param thisFileType the file type to write
	 * @param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	 * @return true if writing was successful (if there was enough space in d64-image etc)
	 */
	public boolean saveFile(String thisFileName, int thisFileType, boolean isCopyFile, byte[] saveData) {
		feedbackMessage = new StringBuffer();
		if (cpmFormat != CPM_TYPE_UNKNOWN) {
			feedbackMessage.append("saveFile: Not yet implemented for CP/M format.\n");
			return false;
		}
		if (isCopyFile == false) {
			if (thisFileName.toLowerCase().endsWith(".prg")) {
				thisFileName = thisFileName.substring(0, thisFileName.length()-4);				
			}
			thisFileName = cbmFileName(thisFileName);
		}
		feedbackMessage.append("saveFile: '").append(thisFileName).append("'  ("+saveData.length+")\n");
		boolean success = saveFileData(saveData);
		if (success) {
			success = addDirectoryEntry(thisFileName, thisFileType, destTrack, destSector, isCopyFile, saveData.length);
		} else {
			feedbackMessage.append("\nsaveFile: Error occurred.\n");
		}
		return success;
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
		if (cpmFormat != CPM_TYPE_UNKNOWN) {
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
		feedbackMessage.append("renamePRG: oldName '").append(bufferCbmFile.getName()).append(" newName '").append(newPRGName).append("'\n");
		bufferCbmFile.copy(cbmFile[cbmFileNumber]);		
		bufferCbmFile.setName(cbmFileName(newPRGName));
		bufferCbmFile.setFileType(newPRGType);
		writeDirectoryEntry(bufferCbmFile.getDirPosition());
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
				newFile.setCpmName(name);
				newFile.setCpmNameExt(nameExt);
				newFile.setReadOnly(readOnly);
				newFile.setArchived(archive);
				newFile.setHidden(hidden);
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
	
//	/**
//	 * Get a single directory entry of the disk image.
//	 * globals needed: cbmDisk<BR>
//	 * @param dataPosition
//	 * @return CbmFile
//	 */
//	protected CbmFile getDirectoryEntry(int dataPosition) {
//		CbmFile entry = new CbmFile();
//		entry.setDirTrack(getCbmDiskValue(dataPosition      + 0x00));
//		entry.setDirSector(getCbmDiskValue(dataPosition     + 0x01));
//		entry.setFileScratched(getCbmDiskValue(dataPosition + 0x02) == 0 ? true : false);
//		entry.setFileType((getCbmDiskValue(dataPosition     + 0x02) & 0x07));
//		entry.setFileLocked((getCbmDiskValue(dataPosition   + 0x02) & 0x40) == 0 ? false : true);
//		entry.setFileClosed((getCbmDiskValue(dataPosition   + 0x02) & 0x80) == 0 ? false : true);
//		entry.setTrack(getCbmDiskValue(dataPosition         + 0x03));
//		entry.setSector(getCbmDiskValue(dataPosition        + 0x04));
//		entry.setName("");
//		for (int i = 0; i < 16; i++){
//			if (getCbmDiskValue(dataPosition + 0x05 + i) != BLANK) {
//				entry.setName(entry.getName()+ (char)(PETSCII_TABLE[getCbmDiskValue(dataPosition + 0x05 + i)]));
//			}
//		}
//		entry.setRelTrack(getCbmDiskValue(dataPosition + 0x15));
//		entry.setRelSector(getCbmDiskValue(dataPosition + 0x16));
//
//		for (int i = 0; i < 7; i++) {
//			entry.setGeos(i, getCbmDiskValue(dataPosition + 0x17 + i));
//		}
//		entry.setSizeInBlocks((char)(
//				getCbmDiskValue(dataPosition + 0x1e) +
//				getCbmDiskValue(dataPosition + 0x1f) * BLOCK_SIZE ));
//		return entry;
//	}
	
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
		feedbackMessage.append("setNewDirEntry()\n");
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
	 * Convert a PC filename to a proper CBM filename.<BR>
	 * @param orgName orgName
	 * @return the CBM filename
	 */
	protected String cbmFileName(String orgName) {
		final String validChars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 []()/;:<>";
		char[] fileName = new char[DISK_NAME_LENGTH];
		int out = 0;
		for (int i=0; i<DISK_NAME_LENGTH && i<orgName.length(); i++) {
			char c = Character.toUpperCase(orgName.charAt(i));
			if (validChars.indexOf(c) >= 0) {
				fileName[out++] = c;
			}
		}
		String newName = new String(Arrays.copyOfRange(fileName, 0, out));
		feedbackMessage.append("Name was '").append(orgName).append("', commodore name is '").append(newName).append("'\n");		
		return newName;
	}
	
	/**
	 * Get <code>Disk</code> instance of current image. This is used when saving to database.
	 * @return Disk
	 */
	public Disk getDisk() {
		Disk disk = new Disk();
		disk.setLabel(getBam().getDiskName());
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
	
	/**
	 * Checks if D64 image is CP/M formatted.
	 * @return CPM_DISK_TYPE.
	 */
	protected int getCpmDiskFormat() {
		if (bam.getDiskName()!= null && (CPM_DISKNAME_1.equals(bam.getDiskName().trim()) || CPM_DISKNAME_2.equals(bam.getDiskName().trim()))) {
			if (CPM_DISKID_GCR.equals(bam.getDiskId())) {
				if ("CBM".equals(getStringFromBlock(1, 0, 0, 3))) {
					if ((getCbmDiskValue(BLOCK_SIZE - 1) & 0xff) == 0xff) {
						feedbackMessage.append("CP/M C128 double sided disk detected.\n");
						return CPM_TYPE_D71;
					} else {
						feedbackMessage.append("CP/M C128 single sided disk detected.\n");
						return CPM_TYPE_D64_C128;
					}
				} else {
					feedbackMessage.append("CP/M C64 single sided disk detected.\n");					
					return CPM_TYPE_D64_C64;
				}
			} else if (CPM_DISKID_1581.equals(bam.getDiskId())) {
				feedbackMessage.append("CP/M 3.5\" disk detected.\n");				
				return CPM_TYPE_D81;
			}
		}		
		return CPM_TYPE_UNKNOWN;
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
	
	private final static String[] BASIC_V2_TOKENS = { 
			// 0x80 - 0xCB
			"END",     "FOR",    "NEXT",   "DATA",	"INPUT#",  "INPUT",  "DIM",    "READ",
			"LET",     "GOTO",   "RUN",    "IF",	"RESTORE", "GOSUB",  "RETURN", "REM",
			"STOP",    "ON",     "WAIT",   "LOAD",	"SAVE",    "VERIFY", "DEF",    "POKE",
			"PRINT#",  "PRINT",  "CONT",   "LIST",	"CLR",     "CMD",    "SYS",    "OPEN",
			"CLOSE",   "GET",    "NEW",    "TAB(",	"TO",      "FN",     "SPC(",   "THEN",
			"NOT",     "STEP",   "+",      "-",		"*",       "/",      "^",      "AND",
			"OR",      ">",      "=",      "<",		"SGN",     "INT",    "ABS",    "USR",
			"FRE",     "POS",    "SQR",    "RND",	"LOG",     "EXP",    "COS",    "SIN",
			"TAN",     "ATN",    "PEEK",   "LEN",	"STR$",    "VAL",    "ASC",    "CHR$",
			"LEFT$",   "RIGHT$", "MID$",   "GO"
		};
	
	public String parseCbmBasicPrg(byte[] prg) {
		if (prg == null || prg.length < 4) {
			return null;
		}
		StringBuffer buf = new StringBuffer();
		int loadAddr = (prg[0] & 0xff) | ((prg[1] & 0xff) << 8);
		feedbackMessage.append("parseCbmBasicPrg: loadAddr=0x"+Integer.toHexString(loadAddr));
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
					if ((quoteCount & 1) == 0 && op >= 0x80 && op <= 0xcb) {
						buf.append(BASIC_V2_TOKENS[op - 0x80]);
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
	
	/** @return True if loaded D64 image is CP/M formatted. */
	public boolean isCpmFormat() {
		return cpmFormat != CPM_TYPE_UNKNOWN;
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
		return cbmFile[number];
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
	
	
	/**
	 * @return
	 */
	public CbmFile getBufferCbmFile() {
		return bufferCbmFile;
	}

	/**
	 * @param file
	 */
	public void setBufferCbmFile(CbmFile file) {
		bufferCbmFile = file;
	}
	
	public void setCompressed(boolean compressed) {
		this.compressed  = compressed;
	}
	
	/**
	 * Container for track and sector
	 */
	class TrackSector {
		int track;
		int sector;
		public TrackSector(int track, int sector) {
			this.track = track;
			this.sector = sector;
		}
		public int getTrack() {
			return track;
		}
		public void setTrack(int track) {
			this.track = track;
		}
		public int getSector() {
			return sector;
		}
		public void setSector(int sector) {
			this.sector = sector;
		}
		public String toString() {
			return "[" + track + ":" + sector + "]";
		}
	}
	
	
}
