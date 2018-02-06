package D64;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

/*
 * Created on 21.06.2004
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
public class D64 {

//Constants
	private static final String[] FileType = { 
		"DEL",
		"SEQ",
		"PRG",
		"USR",
		"REL"
	};
	
	private CbmTrack[] tracks = new CbmTrack[41];
	
	/**
	 * 
	 */
	public D64() {
		initDefaults();
	}

	

	// d64 format ist restricted to a maximum of 144 directory entries
	private static final int filenumber_limit = 144;

	private static int[] myEncoding = {
		//invisible
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32,32,
		
		//visible
		32,33,34,35,36,37,38,39,
		40,41,42,43,44,45,46,47,48,49,
		50,51,52,53,54,55,56,57,58,59,
		60,61,62,63,64,65,66,67,68,69,
		70,71,72,73,74,75,76,77,78,79,
		80,81,82,83,84,85,86,87,88,89,
		90,91,92,93,94,95,96,97,98,99,
		100,101,102,103,104,105,106,107,108,109,
		110,111,112,113,114,115,116,117,118,119,
		120,121,122,123,124,125,126,127,
		
		//invisible
		32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32, 32, 32, 32, 32, 32, 32, 32, 32, 32,
		32,
		
		//visible
		161,162,163,164,165,166,167,168,169,
		170,171,172,173,174,175,176,177,178,179,
		180,181,182,183,184,185,186,187,188,189,
		190,191,
		
		/*
		cbm font trickery: codes 192-223 are 96-127
		192,193,194,195,196,197,198,199,
		200,201,202,203,204,205,206,207,208,209,
		210,211,212,213,214,215,216,217,218,219,
		220,221,222,223,
		*/
		96,97,98,99,
		100,101,102,103,104,105,106,107,108,109,
		110,111,112,113,114,115,116,117,118,119,
		120,121,122,123,124,125,126,127,

		/*
		cbm font trickery: codes 224-254 are 160-190
		224,225,226,227,228,229,
		230,231,232,233,234,235,236,237,238,239,
		240,241,242,243,244,245,246,247,248,249,
		250,251,252,253,254,
		*/
		
		32,161,162,163,164,165,166,167,168,169,
		170,171,172,173,174,175,176,177,178,179,
		180,181,182,183,184,185,186,187,188,189,
		190,191,
		
		/*
		cbm font trickery: codes 255 is 126
		255
		*/
		126
	};


	private void initTracks(){
		//int sectors_, 				int sectors_in_, 			int offset_
		tracks[1]		= new CbmTrack(21,	0,		 0				);	//$00000}
		tracks[2]		= new CbmTrack(21,	21,		 5376		);	//$01500}
		tracks[3]		= new CbmTrack(21,	42,		 10752		);	//$02a00}
		tracks[4]		= new CbmTrack(21,	63,		 16128		);	//$03f00}
		tracks[5]		= new CbmTrack(21,	84,		 21504		);	//$05400}
		tracks[6]		= new CbmTrack(21,	105,	 26880		);	//$06900}
		tracks[7]		= new CbmTrack(21,	126,	 32256		);	//$07e00}
		tracks[8]		= new CbmTrack(21,	147,	 37632		);	//$09300}
		tracks[9]		= new CbmTrack(21,	168,	 43008		);	//$0a800}
		tracks[10]	= new CbmTrack(21,	189,	 48384		);	//$0bd00}
		
		tracks[11]	= new CbmTrack(21,	210,	 53760		);	//$0d200}
		tracks[12]	= new CbmTrack(21,	231,	 59136		);	//$0e700}
		tracks[13]	= new CbmTrack(21,	252,	 64512		);	//$0fc00}
		tracks[14]	= new CbmTrack(21,	237,	 69888		);	//$11100}
		tracks[15]	= new CbmTrack(21,	295,	 75264		);	//$12600}
		tracks[16]	= new CbmTrack(21,	315,	 80640		);	//$13b00}
		tracks[17]	= new CbmTrack(21,	336,	 86016		);	//$15000}
		tracks[18]	= new CbmTrack(19,	357,	 91392		);	//$16500}
		tracks[19]	= new CbmTrack(19,	376,	 96256		);	//$17800}
		tracks[20]	= new CbmTrack(19,	395,	 101120	);	// $18b00}
		
		tracks[21]	= new CbmTrack(19,	414,	 105984	);	// $19e00}
		tracks[22]	= new CbmTrack(19,	433,	 110848	);	// $1b100}
		tracks[23]	= new CbmTrack(19,	452,	 115712	);	// $1c400}
		tracks[24]	= new CbmTrack(19,	471,	 120576	);	// $1d700}
		tracks[25]	= new CbmTrack(18,	490,	 125440	);	// $1ea00}
		tracks[26]	= new CbmTrack(18,	508,	 130048	);	// $1fc00}
		tracks[27]	= new CbmTrack(18,	526,	 134656	);	// $20e00}
		tracks[28]	= new CbmTrack(18,	544,	 139264	);	// $22000}
		tracks[29]	= new CbmTrack(18,	562,	 143872	);	// $23200}
		tracks[30]	= new CbmTrack(18,	580,	 148480	);	// $24400}
		
		tracks[31]	= new CbmTrack(17,	598,	 153088	);	// $25600}
		tracks[32]	= new CbmTrack(17,	615,	 157440	);	// $26700}
		tracks[33]	= new CbmTrack(17,	632,	 161792	);	// $27800}
		tracks[34]	= new CbmTrack(17,	649,	 166144	);	// $28900}
		tracks[35]	= new CbmTrack(17,	666,	 170496	);	// $29a00}
		tracks[36]	= new CbmTrack(17,	683,	 174848	);	// $2ab00}
		tracks[37]	= new CbmTrack(17,	700,	 179200	);	// $2bc00}
		tracks[38]	= new CbmTrack(17,	717,	 183552	);	// $2cd00}
		tracks[39]	= new CbmTrack(17,	734,	 187904	);	// $2de00}
		tracks[40]	= new CbmTrack(17,	751,	 192256	);	// $2ef00}
	}
	
	private void initCbmFiles(){
		for (int i = 0; i < filenumber_limit+1; i++) {
			cbmFile[i] = new CbmFile();
		}
	}
	
	private int Read_Only = 0;
	private int Write_Only = 1;
	private int Read_Write = 2;
	
//vars
	private byte[] cbmDisk = new byte[174848];	//data of the whole d64 image - gets filled with data in readD64()
	private CbmBam bam = new CbmBam();		// all attributes which are stored in the BAM of a D64 file - gets filled with data in readBAM()
	
	/*
	 * cbmFile: a cbmFile holds all additional attributes (like fileName, fileType etc) for a PRG-file in d64 image. These attributes are used in the directory.
	 * these are initialized in initCbmFiles() and filled with data in readDirectory()
	 * their index is the directory-position they have in the D64 file (see readDirectory()) 
	 */
	private CbmFile[] cbmFile = new CbmFile[filenumber_limit+1]; 
	private int filenumber_max;	// number of files in d64 image
	private CbmFile bufferCbmFile = new CbmFile();	//used by insertPRG and CopyPRG
	private CbmFile singleDirFile = new CbmFile();	//used by readSingleDirectory and others
	
	private byte[] saveData = new byte[65536];	// here goes the data for a one single PRG file
	private int saveDataSize;	// size of saveData in bytes - is set to file.length in readPRG()
	
	private int dest_track;
	private int dest_sector;
	
	private boolean geosFormat;	//{When True, this is a GEOS-formatted disk, therefore, files have to be saved the GEOS way onto it}
	private boolean copyToDirTrack;	//When True, free sectors on the directory track are also allowed to hold file data if the disk otherwise gets full}

	private int firstTrack;	//Track number of first track (may be above one for subdirectories on 1581 disks)}
	private int lastTrack;	//Track number of last track plus one (may be below the physical end of disk for subdirectories on 1581 disks)}

	private int maxTrack;	//Track number of physically last track plus one}

	private int dirTrack;	//Track number of directory track}
	private int dirTrack2;	//Track number of secondary directory track (for 1571 disks); 255 (a non-existent track), if not available}

	private int interleave;	//Soft interleave}

	private int track_; 	//Track number of current block of file}
	private int sector;	//Sector number of current block of file}
	
	private String feedbackMessage;






	/**
	Reads the d64 file<BR>
	globals_needed: (none)<BR>
	globals_written: disk,  feedbackMessage<BR>
	@param filename	the filename
	@return <code>true</code> when file was found and has size of 174848 bytes
	*/
	public boolean readD64(String filename){

		FileInputStream input;
		boolean success = true;

		feedbackMessage = "Trying to load "+filename+"... ";
		
		try{
			input = new FileInputStream(filename);
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not open file.\n";
			return false;
		}
		
		File file = new File(filename); 
		
		if (file.length() != 174848) {
			success = false;
			feedbackMessage = feedbackMessage+"error: File has incorrect size. A D64 file should be 174848 bytes.\n";
		} 
		
		if (file.isFile() == false) {
			success = false;
			feedbackMessage = feedbackMessage+"error: File is not a regular file.\n";
		} 
		
		if (success)
		try{
			input.read( cbmDisk );
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not read filedata.\n";
			return false;
		}
		
		try{
			input.close();
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not close file.\n";
			return false;
		}

		if (success)
			feedbackMessage = feedbackMessage+"ok.\n";
		
		return success;
	}






	/**
	Writes a d64 file<BR>
	globals needed: disk<BR>
	globals written: feedbackMessage<BR>
	@param filename the filename
	@return
	*/
	public boolean writeD64(String filename){
		FileOutputStream output;
		boolean success = true;

		feedbackMessage = "writeD64: Trying to save "+filename+"... ";
		
		try{
			output = new FileOutputStream(filename);
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not open file for writing.\n";
			return false;
		}
		
		try{
			output.write( cbmDisk );
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not write filedata.\n";
			return false;
		}
		
		try{
			output.close();
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not close file.\n";
			return false;
		}

		if (success)
			feedbackMessage = feedbackMessage+"ok.\n";
		
		return success;
	}






	/**
	Reads a PRG file from harddisk<BR>
	globals needed:<BR>
	globals written: saveData, saveDataSize,  feedbackMessage<BR>
	@param filename the filename
	@return returns true when file was found
	*/
	public boolean readPRG(String filename){

		//ByteArrayInputStream input;
		FileInputStream input;
		boolean success = true;
		
		feedbackMessage = "readPRG(): Trying to load "+filename+"... ";
				
		try{
			input = new FileInputStream(filename);
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not open file.\n";
			return false;
		}
				
		File file = new File(filename); 
				
		if (file.length() > 65536) {
			success = false;
			feedbackMessage = feedbackMessage+"error: File is too big for a PRG file (more than 65536 bytes).\n";
		}
				
		if (file.isFile() == false) {
			success = false;
			feedbackMessage = feedbackMessage+"error: File is not a regular file.\n";
		} 
				
		if (success)
		try{
			input.read( saveData );
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not read filedata.\n";
			return false;
		}
				
		try{
			input.close();
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"error: Could not close file.\n";
			return false;
		}

		if (success) {
			saveDataSize =  (int) file.length(); 
			feedbackMessage = feedbackMessage+"ok.\n";
		}

				
		return success;
	}






	/**
	Reads the BAM of the d64 image, fills bam[] with entries<BR>
	globals needed:<BR>
	globals written: bam[]<BR>
	*/
	public void readBAM(){
/*
	  Bytes:$00-01: Track/Sector location of the first directory sector (should
					be set to 18/1 but it doesn't matter, and don't trust  what
					is there, always go to 18/1 for first directory entry)
				02: Disk DOS version type (see note below)
					  $41 ("A")
				03: Unused
			 04-8F: BAM entries for each track, in groups  of  four  bytes  per
					track, starting on track 1 (see below for more details)
			 90-9F: Disk Name (padded with $A0)
			 A0-A1: Filled with $A0
			 A2-A3: Disk ID
				A4: Usually $A0
			 A5-A6: DOS type, usually "2A"
			 A7-AA: Filled with $A0
			 AB-FF: Normally unused ($00), except for 40 track extended format,
					see the following two entries:
			 AC-BF: DOLPHIN DOS track 36-40 BAM entries (only for 40 track)
			 C0-D3: SPEED DOS track 36-40 BAM entries (only for 40 track)
	 
	   BAM_struct = record
	   disk_DOS_type : byte;
	   track_bits : array [1..40, 1..4] of byte;
	   disk_name : string[16];
	   disk_ ID : string[2];
	   DOS_type : string[2];
*/
	 int cnt;
	 byte track_number;
	 byte this_track;
	 byte this_sector;
	 

	 // {be verbose for testing}
	 //feedbackMessage = feedbackMessage + "Reading BAM...\n";
	 
	 bam.setDisk_name("");
	 bam.setDisk_ID("");

	 this_track = 18;
	 this_sector = 0;
 
	 bam.setDisk_DOS_type((byte)
	 	getCbmDiskValue(tracks[this_track].getOffset() + (256*this_sector) + 2 )
	  );
 

	 for (track_number = 1; track_number <= 40; track_number ++) {
	 	bam.setFreeSectors(track_number, (byte)
		getCbmDiskValue(tracks[this_track].getOffset() + (256*this_sector) + (track_number * 4))
	 	);
	 for (cnt = 1; cnt <= 3; cnt ++)
	 	bam.setTrack_bits(track_number, (byte) cnt, (byte)
		getCbmDiskValue(tracks[this_track].getOffset() + (256*this_sector) + (track_number * 4) +cnt)
	 	);
	 }
 
 	bam.setDisk_name("");
	 for (cnt = 0; cnt <= 15; cnt ++)
		bam.setDisk_name(
			bam.getDisk_name() + 
			new Character((char)(myEncoding[getCbmDiskValue(tracks[this_track].getOffset() + (256*this_sector) + 144 + cnt )]))
		);
 
   bam.setDisk_ID("");
	for (cnt = 0; cnt <= 4; cnt ++)
		bam.setDisk_ID(
			bam.getDisk_ID() +
			new Character((char)( myEncoding[getCbmDiskValue(tracks[this_track].getOffset() + (256*this_sector) + 162 + cnt)] ))
		);
	}






	/**
	Reads the directory of the d64 image, fills cbmFile[] with entries<BR>
	globals needed: cbmDisk<BR>
	globals written: cbmFile[], filenumber_max<BR>
	*/
	//public int zaehler = 0;
	public void readDirectory(){
		boolean filelimit_reached = false;
		int this_track = 18;
		int this_sector = 1;
		int dirPosition = 0;
		int filenumber = 0;
		int dataPosition = 0;

		feedbackMessage = "readDirectory()\n";
		
		do {
			dataPosition = tracks[this_track].getOffset() + (256*this_sector);
			for (int cnt = 0; cnt <= 7; cnt ++){
				readSingleDirectorEntry(dataPosition + (cnt*32));
				copyCbmFile( singleDirFile, cbmFile[filenumber]);
				if (cbmFile[filenumber].isFile_scratched() == false) {
					cbmFile[filenumber].setDirPosition(dirPosition);
					if (filenumber < filenumber_limit) filenumber++;	// too many files in directory check
					else {
						filelimit_reached = true;
					}
				}
				dirPosition ++;
			}
			this_track = getCbmDiskValue( dataPosition + 0);
			this_sector = getCbmDiskValue( dataPosition + 1);
		}
		while (this_track != 0);
		
		if (filelimit_reached) {
			feedbackMessage = feedbackMessage + "Error: Too many fileentries in directory (more than "+filenumber_limit+")!\n";
		}

		filenumber_max = filenumber;
	}




	/**
	Find next free directory entry<BR>
	globals needed: cbmDisk<BR>
	@return number of next free directory entry, returns -1 is there was none
	*/
	private int freeDirEntry(){
		int this_track = 18;
		int this_sector = 1;
		int dirPosition = 0;
		int dataPosition = 0;
		
		//be verbose
		feedbackMessage = feedbackMessage + "freeDirEntry() : ";
		
		do {
			dataPosition = tracks[this_track].getOffset() + (256*this_sector);
			for (int cnt = 0; cnt <= 7; cnt ++){
				readSingleDirectorEntry(dataPosition + (cnt*32));
				if (singleDirFile.isFile_scratched()) return dirPosition;
				dirPosition ++;
			}
			this_track = getCbmDiskValue( dataPosition + 0);
			this_sector = getCbmDiskValue( dataPosition + 1);
		}
		while (this_track != 0);

		//maybe not correct...
		if (dirPosition < filenumber_limit+2) return dirPosition;
		else return -1;
	}

	/**
	Reads a single directory entry of the d64 image, fills singleDirFile with entries<BR>
	globals needed: cbmDisk<BR>
	globals written: singleDirEntry[]<BR>
	*/
	public void readSingleDirectorEntry(int dataPosition){
		int position;
		int next_track;
		int next_sector;
		int this_track = 18;
		int this_sector = 1;
		
		if (getCbmDiskValue( dataPosition + 2) == 0){
			singleDirFile.setFile_scratched(true);
		}
		else{
			singleDirFile.setFile_scratched(false);
		}

		//System.out.println("cnt = "+cnt+", filenumber = "+filenumber+" Name= "+singleDirFile.getName()+", is scratched = "+singleDirFile.isFile_scratched());
		
		//singleDirFile.setDirPosition(dirPosition);
		
		singleDirFile.setDirTrack(getCbmDiskValue(dataPosition + 0));
		singleDirFile.setDirSector(getCbmDiskValue(dataPosition + 1));
		
		singleDirFile.setFile_type( 
			(getCbmDiskValue( dataPosition + 2) & 7)
		);
		
		if (	(getCbmDiskValue(dataPosition + 2) & 64) == 0	){
			singleDirFile.setFile_locked(false);
		}
		else{
			singleDirFile.setFile_locked(true);
		}
		
		if (	(getCbmDiskValue( dataPosition + 2) & 128) == 0	){
			singleDirFile.setFile_closed(false);
		}
		else{
			singleDirFile.setFile_closed(true);
		}
		
		singleDirFile.setTrack(getCbmDiskValue(dataPosition + 3));
		singleDirFile.setSector(getCbmDiskValue(dataPosition+ 4));
		
		singleDirFile.setName("");
		for (position = 0; position <= 15; position ++){
			if (getCbmDiskValue(dataPosition + 5 + position) != 160){
				singleDirFile.setName(
					singleDirFile.getName()+
				(char)( myEncoding[getCbmDiskValue( dataPosition + 5 + position)])
				);
			}
		}
		
		singleDirFile.setRel_track(getCbmDiskValue(dataPosition + 21));
		singleDirFile.setRel_sector(getCbmDiskValue(dataPosition + 22));
		
		for (position = 0; position <= 5; position ++){
			singleDirFile.setGeos(position, getCbmDiskValue(dataPosition + 23 + position));
		}

		singleDirFile.setSizeInBlocks((char)(
			getCbmDiskValue(dataPosition + 30) +
			getCbmDiskValue( dataPosition + 31) * 256
		));
		
	}


	/**
	Sets new attributes for a single PRG file.<BR>
	globals needed:<BR>
	globals written: cbmFile, feedbackMessage<BR>
	@param cbmFileNumber which PRG to rename
	@param newPRGName the new name of the PRG-file
	@param newPRGType the new type of the PRG-file
	 */
	public void renamePRG(int cbmFileNumber, String newPRGName, int newPRGType) {
		feedbackMessage = "renamePRG():\n";
		
		feedbackMessage = feedbackMessage + "oldName: "+bufferCbmFile.getName();

		copyCbmFile(cbmFile[cbmFileNumber], bufferCbmFile);

		bufferCbmFile.setName(
			cbmFileName(newPRGName)
		);
		bufferCbmFile.setFile_type(newPRGType);
		
		writeDirectoryEntry(bufferCbmFile.getDirPosition());
	}



	/**
	Fills saveData with data of a single PRG file.<BR>
	globals needed: cbmDisk<BR>
	globals written: saveData, saveDataSize, feedbackMessage<BR>
	@param number the filenumber in the d64 image
	@return true if writing was successful
	 */
	public boolean writeSaveData(int number){
		int next_track;
		int next_sector;
		int this_track = 18;
		int this_sector = 1;
		int position;
		int counter;
		
		boolean success = true;

		feedbackMessage = feedbackMessage + "-----------\n";
		feedbackMessage = feedbackMessage + "writeSaveData(): Filling saveData... \n";
		feedbackMessage = feedbackMessage + "Tracks / Sectors: ";
		
		// write ints
		this_track = cbmFile[number].getTrack();
		this_sector = cbmFile[number].getSector();

		counter = 0;
		
		do{
			/*
			 * TODO: writeSaveData(): There are null-pointer exceptions if the source disk contains scratched (hidden) files.
			 */
			next_track =  getCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + 0);
			next_sector =  getCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + 1);

			feedbackMessage = feedbackMessage + this_track+"/"+this_sector+" ";
			
			if (next_track > 0){
				for (position = 2; position <= 255; position++) {
					saveData[counter] = cbmDisk[ tracks[this_track].getOffset() + (256*this_sector) + position];
					counter ++;
				}
			}
			else {
				feedbackMessage = feedbackMessage + "\nRemaining bytes: "+next_sector+"\n";
				for (position = 2; position <= next_sector; position++) {
					saveData[counter] = cbmDisk[ tracks[this_track].getOffset() + (256*this_sector) + position];
					counter ++;
				}
			}
			
			this_track = next_track;
			this_sector = next_sector;
			
		} while( next_track != 0);
		
		saveDataSize = counter;
		
		if (success)
			feedbackMessage = feedbackMessage+"Ok.\n";
		
		return success;
	}




	/**
	Writes a PRG file to disk.<BR>
	globals needed: saveData, saveDataSize<BR>
	globals written: feedbackMessage<BR>
	@param filename the filename in PC format
	@param directory the target directory
	@return true if writing was successful
	 */
	private boolean writePRG(String filename, String directory){
		int next_track;
		int next_sector;
		int this_track = 18;
		int this_sector = 1;
		int position;
		
		//ByteArrayOutputStream output;
		FileOutputStream output;
		boolean success = true;

		feedbackMessage = feedbackMessage + "-----------\n";
		feedbackMessage = feedbackMessage + "Trying to save "+directory+filename+" ("+saveDataSize+" bytes)... \n";
		
		//open file for writing
		try{
			output = new FileOutputStream(directory+filename);
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"Error: Could not open file for writing.\n";
			return false;
		}
		
		
		for (position = 0; position <= saveDataSize; position++) {
			try{
				output.write( saveData[position] );
			}
			catch (Exception e){
				System.err.println(e.toString());
				feedbackMessage = feedbackMessage+"Error: Could not write filedata.\n";
				return false;
			}
		}
		

	// close file and end
		try{
			output.close();
		}
		catch (Exception e){
			System.err.println(e.toString());
			feedbackMessage = feedbackMessage+"Error: Could not close file.\n";
			return false;
		}

		if (success)
			feedbackMessage = feedbackMessage+"Ok.\n";
		
		return success;
	}





	/**
	 * Converts a CBM filename to a pc filename 
	 * @param fileName the CBM filename
	 * @param fileType the CBM filetype
	 * @return the PC filename
	 */
	private String pcFilename(String fileName, String fileType){
		String permitted_chars = ("abcdefghijklmnopqrstuvwxyz0123456789");
		boolean char_permitted;
		int position;
		int cnt;
		
		for (position = 0; position < fileName.length(); position++) {
			fileName = fileName.toLowerCase();	//filename[position] := chr( ord(filename[position]) or 32);
			char_permitted = false;
			for (cnt = 0; cnt < permitted_chars.length(); cnt++) {
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
	Extracts a file of the d64 image to a single file in PRG format.
	The filename used in the d64 image is converted and then used as output-filename.<BR>
	globals needed:<BR>
	globals written:<BR>
	@param number number of d64-directory entry
	@param directory the target directory
	@return
	*/
	public void extractPRG(int number, String directory){
		feedbackMessage = "";
		writeSaveData(number);
		writePRG(
			pcFilename(
				cbmFile[number].getName(),
				FileType[ cbmFile[number].getFile_type() ]
			),
			directory
		);
	}






	/**
	Determine if there's, at least, one free sector on a track<BR>
	globals needed:<BR>
	globals written:<BR>
	@param Track_number the track number of sector to check
	@return when True, there's, at least, one free sector on the track
	*/
	private boolean isTrackFree(int track_number){
		boolean success = false;
		
		//get the latest informations from BAM
		 readBAM();
		
		if ( bam.getFreeSectors(track_number) > 0) success = true;

		//be verbose
		 String thisMessage ="\nIsTrackFree("+track_number+") = "+bam.getFreeSectors(track_number)+" = ";
		 if (success) thisMessage = thisMessage + "free.\n";
		 	else thisMessage = thisMessage + "used.\n";
		 
		//System.out.println(thisMessage);
		 
		//feedbackMessage = feedbackMessage + thisMessage;
		 return success;
	}






	/**
	Determine if a sector is free<BR>
	globals needed:<BR>
	globals written:<BR>
	@param Track_number the track number of sector to check
	@param Sector_number the sector number of sector to check
	@return when True, the sector is free; otherwise used
	*/
	private boolean isSectorFree(int track_number, int sector_number){
		int[] preAND = {
			1,2,4,8,16,32,64,128
		};
		
		int cnt;
		int bit;
		int bit_counter = 0;
		boolean free = false;
		
		//sector_number++;

		//get the latest informations from BAM
		 readBAM();

		for (int i = 1; i <= 3; i++) {
			for (int j = 0; j <= 7; j++) {
				if (
					(bit_counter < tracks[track_number].getSectors()) &&
					(bit_counter == sector_number) &&
					((bam.getTrack_bits(track_number,i) & preAND[j]) > 0)
				){
					free = true;
				}
				bit_counter++;
			}
			
		}

	//be verbose
	/*
	String this_message = "IsSectorFree(Track "+track_number+" Sector "+sector_number+") = ";

	if (free) this_message = this_message + "free.\n";
		   else this_message = this_message + "used.\n";

	feedbackMessage = feedbackMessage + this_message;
	*/
		return free;
	}






	/**
	Determine the number of sectors (or the highest valid sector number plus one) for a track<BR>
	globals needed: Track<BR>
	globals written:<BR>
	@param track_number track_number
	@return the number of sectors on the track
	*/
	private int getMaximumSectors(int track_number){
		//be verbose
		int result = tracks[track_number].getSectors();
		//feedbackMessage = feedbackMessage + "Getting sectors for track "+track_number+":"+ result+"\n";
		return result;
	}






	/**
	Find a sector for the first block of the file, using variables Track and Sector<BR>
	globals needed: GEOSFormat, CopyToDirTrack<BR>
	globals written: track_, sector<BR>
	@param
	@return when True, a sector was found; otherwise no more sectors left
	*/
	private boolean firstCopyBlock(){
		boolean found;
		int maxSector;
		int distance;

		//We found no free sector yet}
		  found = false;

		//If this is a GEOS-formatted disk then use the other routine, from track one upwards}
		  if (isGeosFormat()){
			track_ = 1;
			sector = 0;
			found = nextCopyBlock();
		  }
		  else{
			distance = 1;	//If it's a normal disk then we start off with tracks just besides the directory track}

			while ((found == false) && (distance < 128)) {	//Search until we find a free block or moved too far from the directory track}
			 track_ =  (dirTrack - distance);	//Check the track below the directory track first}
			 if ((track_ >= firstTrack) && (track_ < lastTrack)) found = isTrackFree(track_);	//If the track is inside the valid range then check if there's a free sector on it}
        
			 if ( found == false){
				track_ = (dirTrack + distance);	//If no luck then check the track above the directory track}
				if (track_ < lastTrack) found = isTrackFree(track_);	//If the track is inside the valid range then check if there's a free sector on it}
			 }
      
			 if ( found == false) distance++;	//If no luck either then move one track away from the directory track and try again}
			}

		 /*
		   //If the whole disk is full and we're allowed to use the directory track for file data then try there, too}
			if ( (found == false) && (CopyToDirTrack)) {
			  track_ = dirTrack;
			  found = isTrackFree(track_);
			}
		 */    

		   //If we finally found a track with, at least, one free sector then search for a free sector in it}
			if (found) {
			  maxSector = getMaximumSectors(track_);	//Determine how many sectors there are on that track}
			  sector = 0;	//Start off with sector zero}
			  do {
				found = isSectorFree(track_, sector);	//Check if the current sector is free}
				if (found==false) sector++;	//If it isn't then go on to the next sector}
			  } while ((found == false) || (sector >= maxSector));	//Repeat the check until we find a free sector or run off the track}
			}
    
		  }//else?

		  //be verbose for testing}
		  //feedbackMessage = feedbackMessage + "firstCopyBlock(): The first CopyBlock will be at Track "+track_+" Sector "+sector+".\n";

		return found;
	}






	/**
	Find a sector for the next block of the file, using variables Track and Sector<BR>
	globals needed: GEOSFormat, CopyToDirTrack<BR>
	globals written: track_, sector<BR>
	@param
	@return when True, a sector was found; otherwise no more sectors left
	*/
	private boolean nextCopyBlock(){
		  boolean Found;
		  int Tries;
		  int MaxSector;
		  int CurSector;
		  int CurTrack;

		  if ((track_ == 0) || (track_ >= maxTrack)){
		   //If we somehow already ran off the disk then there are no more free sectors left}
			Found = false;
		  }
  
		  else {
			Tries = 3;	//Set the number of tries to three}
			Found = false;	//We found no free sector yet}
			CurTrack = track_;	//Remember the current track number}
   
			while ( (Found == false) && (Tries > 0)) {	//Keep trying until we find a free sector or run out of tries}

			  MaxSector = getMaximumSectors(track_);	//Get the number of sectors on the current track}
			  if (isTrackFree(track_)==true) {	//If there's, at least, one free sector on the track then get searching}
				if ((track_ == CurTrack) || (geosFormat ==false)) {	//If this is a non-GEOS disk or we're still on the same track of a GEOS-formatted disk then...}
				  sector = (sector + interleave);	//Move away an "interleave" number of sectors}
				  if ((geosFormat==true) && (track_ >= 25)) sector--;	//Empirical GEOS optimization, get one sector backwards if over track 25}
				}
				else {
				  sector = ((track_ - CurTrack) << 1 + 4 + interleave);	//For a different track of a GEOS-formatted disk, use sector skew}
				};
     
				while (sector >= MaxSector) {	 //If we ran off the track then correct the result}
				  sector = (sector - MaxSector)+1;	//Subtract the number of sectors on the track}
				  if ((sector > 0) && (geosFormat == false)) sector--;	//Empirical optimization, get one sector backwards if beyond sector zero}
				};
        
				CurSector = sector;	//Remember the sector we finally arrived at}
				do {
				   Found = isSectorFree(track_, sector);	//Check if the current sector is free}
				  if (Found==false) sector++;	//If it isn't then go to the next sector}
				  if (sector >= MaxSector) sector = 0;	//If we ran off the track then wrap around to sector zero}
				} while ((Found==false) && (sector != CurSector));	//Keep searching until we find a free sector or arrive back at the original sector}
			  }
      
			  else { 	//if IsTrackFree(track_) = FALSE then}

			/*
				if GEOSFormat then {	//If the current track is used up completely then...}
				  Inc(track_);	//Move one track upwards on a GEOS-formatted disk}
				  if (track_ = DirTrack) or (track_ = DirTrack2) then Inc(track_);	//Skip the directory tracks on the way}
				  if track_ = LastTrack then Tries = 0;	//If we ran off the disk then there are no more tries}
				}
				else {
			*/
				  if (track_ == dirTrack) {	//If we already tried the directory track then there are no more tries}
					Tries = 0;
				  }
          
				  else {

				   if (track_ < dirTrack) {
					  track_ --;	//If we're below the directory track then move one track downwards}
					  if (track_ < firstTrack) {
						track_ = (dirTrack + 1); //If we ran off the disk then step back to the track just above the directory track and zero the sector number}
						sector = 0;
						//If there are no tracks available above the directory track then there are no tries left; otherwise just decrease the number of tries}
						if (track_ < lastTrack) Tries --; else Tries = 0;
					  }
					}

					else {	//if track_ >= DirTrack then}
					  track_++;	//If we're above the directory track then move one track upwards}
					 /*
					  if (track_ = dirTrack2) track_++;	//Skip the secondary directory track on the way}
					*/
					  if (track_ == lastTrack) {
						track_ = (dirTrack - 1);	//If we ran off the disk then step back to the track just below the directory track and zero the sector number}
						sector = 0;
					   //If there are no tracks available below the directory track then there are no tries left; otherwise just decrease the number of tries}
						if (track_ >= firstTrack) Tries --; else Tries = 0;
					  }
					} //track_ < DirTrack}
            
				  } //track_ = DirTrack}
				  /*
				} //if GEOSFormat then {}
				*/
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
  
		   //be verbose for testing}
		  //feedbackMessage =  feedbackMessage + "nextCopyBlock(): The next CopyBlock will be at Track "+track_+" Sector "+sector+".\n";
		return Found;
	}






	/**
	Mark a single sector in BAM as free.<BR>
	globals needed:<BR>
	globals written:<BR>
	@param track_number track_number
	@param sector_number sector_number
	@return
	*/
	private void markBAMfree(int track_number, int sector_number){
		int[] preAND = {
			//254, 253, 251, 247, 239, 223, 191, 127
			1,2,4,8,16,32,64,128
		};
		
		int this_track;
		int this_sector;
		int position;

		//be verbose for testing
		feedbackMessage =  feedbackMessage + "markBAMfree("+track_number+", "+sector_number+")\n";

		//Sector start with 1 in function FirstCopyBlock and NextCopyBlock, but start with 0 in reality} 
		sector_number--;
 
		 //BAM Location
			 this_track =18;
			 this_sector =0;
		 
		 //write BAM
		 position = ((sector_number/8)+1);
		 //writeln(position,', ', (sector_number mod 8), ', ',preAND[sector_number mod 8]);
		setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + (track_number*4) + (position),
			 getCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + (track_number*4) + (position)) | preAND[(sector_number % 8)-1]
		);
		 
		setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + (track_number*4),
			getCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + (track_number*4) +1)
		);
	 }






	 /**
	 Mark a single sector in BAM as used.<BR>
	 globals needed:<BR>
	 globals written:<BR>
	@param track_number track_number
	@param sector_number sector_number
	 @return
	 */
	 private void markBAMused(int track_number,int sector_number){
	 	int[] preAND = {
			254, 253, 251, 247, 239, 223, 191, 127
	 	};
	 	
	 	int this_track;
	 	int this_sector;
	 	int position;

	  //be verbose for testing
	  //feedbackMessage =  feedbackMessage + "markBAMused("+track_number+", "+sector_number+")\n";

	  //Sector start with 1 in function FirstCopyBlock and NextCopyBlock, but start with 0 in reality 
	 // sector_number--;
 
	  //BAM Location
	  this_track=18;
	  this_sector=0;
 
	  //write BAM
	  position = ((sector_number / 8)+1);
	  //System.out.println(sector_number+" / "+sector_number % 8);
	  //writeln('position = ',position,',  (sector_number mod 8) = ', (sector_number mod 8), ', preAND[sector_number mod 8] = ',preAND[sector_number mod 8]);
	  setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + (track_number*4) + (position),
	 	getCbmDiskValue( 
	 	tracks[this_track].getOffset()
	 	 + (256*this_sector) + (track_number*4) + (position) )
		& preAND[(sector_number % 8)]
		);
	  
		setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + (track_number*4),
			getCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + (track_number*4) ) -1
		);
	}
	
 /**
 Set a diskname and disk-id in BAM.<BR>
 globals needed:<BR>
 globals written:<BR>
 @param newDiskName the new name of the disk
 @param newDiskID the new id of the disk
 @return
 */
 private void setDiskName(String newDiskName, String newDiskID){
	int this_track;
	int this_sector;

  //be verbose for testing
  feedbackMessage =  feedbackMessage + "setDiskName("+newDiskName+", "+newDiskID+")\n";

  //BAM Location
  this_track=18;
  this_sector=0;

	//fill DiskName with $a0 
  	for (int i = 0; i < 16; i++) {
		setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + 144 +i,	160);
  	}
	//fill DiskID with $a0 
	  for (int i = 0; i < 5; i++) {
		setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + 162 +i,	160);
	  }

	//set DiskName
	for (int i = 0; i < newDiskName.length(); i++) {
		setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + 144 +i,	newDiskName.charAt(i));
	}
		  
	//set DiskID
	for (int i = 0; i < newDiskID.length(); i++) {
	   setCbmDiskValue( tracks[this_track].getOffset() + (256*this_sector) + 162 +i,	newDiskID.charAt(i));
	}
}





	/**
	Fill d64-data with saveData<BR>
	globals needed: saveData<BR>
	globals written:<BR>
	@param track_number track_number
	@param sector_number sector_number
	@param saveSize saveSize
	@param dataPosition dataPosition
	@param next_track next_track
	@param next_sector next_sector
	@return
	*/
	private void fillSector(int track_number, int sector_number, int saveSize, int dataPosition, int next_track, int next_sector){
		
		//be verbose for testing
		//feedbackMessage =  feedbackMessage + "fillSector("+track_number+", "+sector_number+", "+saveSize+", "+dataPosition+", "+next_track+", "+next_sector+")\n";
		
		//write next track
		setCbmDiskValue( tracks[track_number].getOffset() + (256*sector_number) + 0, next_track);
		//write next sector
		setCbmDiskValue( tracks[track_number].getOffset() + (256*sector_number) + 1, next_sector);
		
		for (int position = 0; position <= saveSize; position++) {
			setCbmDiskValue( tracks[track_number].getOffset() + (256*sector_number) + 2 + position, getSaveDataValue(dataPosition+position));
		}
	}




	/**
	Fill d64-data with zeroes<BR>
	globals needed:<BR>
	globals written:<BR>
	@param track_number track_number
	@param sector_number sector_number
	@return
	*/
	private void clearSector(int track_number, int sector_number){
		//be verbose for testing
		feedbackMessage =  feedbackMessage + "clearSector("+track_number+", "+sector_number+")\n";
		
		for (int position = 0; position <= 255; position++) {
			setCbmDiskValue( tracks[track_number].getOffset() + (256*(sector_number-1)) + position, 0);
		}
	}






	/**
	Write the data of a single PRG file to d64-image.<BR>
	globals needed: saveDataSize<BR>
	globals written: dest_track, dest_sector<BR>
	@param
	@return true if writing was successful (if there was enough space in d64-image)
	*/
	private boolean saveFileData(){
		int this_track;
		int this_sector;
		int data_remain = saveDataSize;
		int data_posi = 0;
		boolean success = true;
		
		//be verbose
		feedbackMessage =  feedbackMessage + "Need to write "+data_remain+" bytes of data.\n";
		feedbackMessage =  feedbackMessage + "Soft-interleave is set to "+interleave+".\n";
		
		success = firstCopyBlock();
		
		if (success) {
			dest_track = track_;
			dest_sector = sector;
		}
		else {
			feedbackMessage = feedbackMessage + "\nsaveFileData: Copy Error: Could not find a single free sector on disk [Disk is full].";
		}

		while( (data_remain > 254) && (success) ){

			this_track = track_;
			this_sector = sector;
			
			//be verbose
			//feedbackMessage =  feedbackMessage + "------\n\nRemaining bytes: "+data_remain+".\n";
			feedbackMessage =  feedbackMessage + data_remain+" bytes remain: Track "+this_track+", Sector "+this_sector+"\n";
			
			markBAMused(this_track, this_sector);

			success = nextCopyBlock();
						
			if (success) {
				fillSector(this_track, this_sector, 253, data_posi, track_, sector);

				data_remain = data_remain - 254;
				data_posi = data_posi + 254;
			}
			else {
				feedbackMessage = feedbackMessage + "\nsaveFileData: Copy Error: Could not find enough free sectors on disk [Disk is full].";
			}
		}
		
		if (success) {
			//writeln;	
			feedbackMessage =  feedbackMessage + "-----\n\nWriting remaining data...\n";
			feedbackMessage =  feedbackMessage + "Remaining bytes: "+data_remain+".\n";
			//writeln;

			this_track = track_;
			this_sector = sector;
			markBAMused(this_track, this_sector);
			clearSector(this_track, this_sector);
			fillSector(this_track, this_sector, data_remain, data_posi, 0, (data_remain+2));
 
			feedbackMessage =  feedbackMessage + "All data written.\n";
		}

		return success;
	}




/**
Sets the new location of a file in a directory.<BR>
globals needed:<BR>
globals written: bufferCbmFile<BR>
@param dirEntryNumber position where to put this entry in the directory
@return returns true if a free directory block was found
*/
private boolean setNewDirectoryLocation(int dirEntryNumber){
	int this_track, this_sector;
	boolean found = false;
	boolean success = false;

	//be verbose
	feedbackMessage =  feedbackMessage + "setNewDirectoryLocation("+dirEntryNumber+")...\n";
	
	//System.out.println(dirEntryNumber % 8);
	
	if ( (dirEntryNumber % 8) != 0) {	//if this is not the eighth entry we are lucky and do not need to do anything...
		bufferCbmFile.setDirTrack(0);
		bufferCbmFile.setDirSector(0);
		success = true;
	}
	else {

		/*
		 * find the correct entry where to write new values for dirTrack and dirSector
		 */
		
		this_track = 18;
		this_sector = 1;
	
		if (dirEntryNumber > 7) {
			while (dirEntryNumber > (7+8)) {
				this_track = getCbmDiskValue(  tracks[this_track].getOffset() + (256*this_sector) + 0 );
				this_sector = getCbmDiskValue(  tracks[this_track].getOffset() + (256*this_sector) + 1 );
				 
				feedbackMessage =  feedbackMessage + "LongDirectory: "+dirEntryNumber+" dirEntrys remain, next: Track "+this_track+", Sector "+this_sector+"\n";
				dirEntryNumber = dirEntryNumber - 8;
			}
		}
		 
		int where = tracks[this_track].getOffset() + (256*this_sector);


		/*
		 * Now we have to find a new free directory track and sector
		 */
		this_track = 18;
		this_sector = 1;

		// find next free sector for directory entries
		while (
			(found == false) &&
			(this_sector < getMaximumSectors(this_track))
		) {
			this_sector++;
			found = isSectorFree(this_track, this_sector);
		}

		
		//write the new DirTrack and DirSector
		if (found){
			success = true;
			markBAMused(this_track, this_sector);
			setCbmDiskValue(where + 0, this_track);
			setCbmDiskValue(where + 1, this_sector-1);
		}
	}
	return success;
}


	/**
	Copy attributes of bufferCbmFile to a directoryEntry in cbmDisk.<BR>
	globals needed: bufferCbmFile, filenumber_max<BR>
	globals written: cbmDisk<BR>
	@param dirEntryNumber position where to put this entry in the directory
	@return
	*/
	private void writeDirectoryEntry(int dirEntryNumber){
		int position;
		int this_track;
		int this_sector;

		//be verbose
		feedbackMessage =  feedbackMessage + "writeDirectoryEntry: bufferCbmFile to dirEntryNumer "+dirEntryNumber+"...\n";
	
		this_track = 18;
		this_sector = 1;
	
		if (dirEntryNumber > 7) {
			while (dirEntryNumber > 7) {
				this_track = getCbmDiskValue(  tracks[this_track].getOffset() + (256*this_sector) + 0 );
				this_sector = getCbmDiskValue(  tracks[this_track].getOffset() + (256*this_sector) + 1 );
				 
				feedbackMessage =  feedbackMessage + "LongDirectory: "+dirEntryNumber+" dirEntrys remain, next: Track "+this_track+", Sector "+this_sector+"\n";
				dirEntryNumber = dirEntryNumber - 8;
			}
		}
		 
		int where = tracks[this_track].getOffset() + (256*this_sector) + (dirEntryNumber*32);
		
		setCbmDiskValue(where + 0, bufferCbmFile.getDirTrack());
		setCbmDiskValue(where + 1, bufferCbmFile.getDirSector());
	
		writeSingleDirectoryEntry(where);	 
				
		// be verbose
		feedbackMessage =  feedbackMessage + "ok.\n";
	}



/**
Copy attributes of bufferCbmFile to a location in cbmDisk.<BR>
globals needed: bufferCbmFile<BR>
globals written: cbmDisk<BR>
@param where dataposition where to write to cbmDisk
@return
*/
private void writeSingleDirectoryEntry(int where){
  //be verbose
  feedbackMessage =  feedbackMessage + "writeSingleDirectoryEntry()\n";

  // file attributes
  setCbmDiskValue(where + 2, 0);
  if (bufferCbmFile.isFile_scratched() == false){
	setCbmDiskValue(where + 2, bufferCbmFile.getFile_type());
			
		if (bufferCbmFile.isFile_locked()){
			setCbmDiskValue(where + 2,
			 (getCbmDiskValue(where + 2) | 64));
		}
	
		if (bufferCbmFile.isFile_closed()){
			setCbmDiskValue(where + 2,
			(getCbmDiskValue(where + 2) | 128));
		}
	}
		
	//file track / sector (where to start reading)
	setCbmDiskValue(where + 3, bufferCbmFile.getTrack());
	setCbmDiskValue(where + 4, bufferCbmFile.getSector());

	// FileName		
	for (int position = 0; position <= 15; position++) {
		setCbmDiskValue(where + 5 + position,
		160);
	}
	
	for (int position = 0; position <= bufferCbmFile.getName().length()-1; position++) {
		setCbmDiskValue(where + 5 + position,
		bufferCbmFile.getName().charAt(position));
	}

	// relative Track/Sector
	setCbmDiskValue(where + 21, bufferCbmFile.getRel_track());
	setCbmDiskValue(where + 22, bufferCbmFile.getRel_sector());
	
	// GEOS
	for (int position = 0; position <= 5; position++) {
		setCbmDiskValue(where + 23 + position,
		bufferCbmFile.getGeos(position) );
	}
	
	// Size
	setCbmDiskValue(where + 30, bufferCbmFile.getSizeInBlocks() );
	setCbmDiskValue(where + 31, bufferCbmFile.getSizeInBlocks() / 256 );
}

	





	/**
	Set up variables in a new cbmFile which will be appended to the directory.
	These variables will inserted into the direcoty later.<BR>
	globals needed: filenumber_max, saveDataSize<BR>
	globals written:<BR>
	@param this_filename this_filename
	@param this_filetype  this_filetype
	@param track_number track_number
	@param sector_number sector_number
	*/
	private void setNewDirectoryEntry(String this_filename, int this_filetype, int dest_track, int dest_sector){

		//be verbose
		feedbackMessage =  feedbackMessage + "setNewDirectoryEntry ()\n";
		
		bufferCbmFile.setFile_scratched(false);
		bufferCbmFile.setFile_type(this_filetype);
		bufferCbmFile.setFile_locked(false);
		bufferCbmFile.setFile_closed(true);
		bufferCbmFile.setTrack(dest_track);
		bufferCbmFile.setSector(dest_sector);

		bufferCbmFile.setName(this_filename);
		
		bufferCbmFile.setRel_track( 0);	//TODO: relative files
		bufferCbmFile.setRel_sector( 0);	//TODO: relative files
		
		for (int i = 0; i <= 5; i++) {
			bufferCbmFile.setGeos(i,0);	//TODO: GEOS files
		}
		
		bufferCbmFile.setSizeInBytes(saveDataSize);

		bufferCbmFile.setSizeInBlocks(
			((bufferCbmFile.getSizeInBytes()-2)/254)
		);
		if ( ((bufferCbmFile.getSizeInBytes()-2) % 254) >0 )
			bufferCbmFile.setSizeInBlocks(bufferCbmFile.getSizeInBlocks()+1);
		

	}






	/**
	Add a directory entry of a single PRG file to d64-image.<BR>
	globals needed:<BR>
	globals written: filenumber_max<BR>
	calls: setNewDirectoryEntry, freeDirEntry, setNewDirectoryLocation, writeDirectoryEntry<BR>
	@param this_filename the filename
	@param this_filetype the type of the file
	@param dest_track_ track where file starts
	@param dest_sector_ sector where file starts
	@param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	@return returns true is adding the entry to the directory was successful
	*/
	private boolean addDirectoryEntry(String this_filename, int this_filetype, int dest_track_, int dest_sector_, boolean isCopyFile){
		
		 //be verbose
		feedbackMessage =  feedbackMessage + "\n-----\n";
		feedbackMessage =  feedbackMessage + "addDirectoryEntry (\""+this_filename+"\", "+FileType[this_filetype]+", T["+dest_track+"], S["+dest_sector+"])\n";
 
		if (isCopyFile){
			/*
			 * This a substitute for setNewDirectoryEntry(this_filename, this_filetype, dest_track_, dest_sector_, dirPosition)
			 * since we do not need to set other values than dest_track and dest_sector when copying a file. 
			 */
			bufferCbmFile.setTrack(dest_track_);
			bufferCbmFile.setSector(dest_sector_);
		}
		else {
			setNewDirectoryEntry(this_filename, this_filetype, dest_track_, dest_sector_);
		}

		bufferCbmFile.setDirTrack(0);
		bufferCbmFile.setDirSector(255);

		int dirEntryNumber = freeDirEntry();
		if (setNewDirectoryLocation(dirEntryNumber)) {
			writeDirectoryEntry(dirEntryNumber);
	
			// increase the maximum filenumbers
			filenumber_max++;
			return true;
		}
		else {
			feedbackMessage =  feedbackMessage + "Error: Could not find a free sector on track 18 for new directory entries...\n";
			return false;
		}
	}



	/**
	Convert a PC filename to a proper cbm filename.<BR>
	globals needed:<BR>
	globals written:<BR>
	@param this_filename this_filename
	@return the cbm filename
	*/
	private String cbmFileName(String this_filename){
		String permitted_chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789 []()/;:<>";
		
		int position;
		int cnt2;
		String fileName;
		boolean allowed;
		
		fileName = this_filename.toUpperCase();

		if (fileName.length() > 16) {
			fileName = fileName.substring(0,16);
		}
		
		for (position = 0; position < fileName.length(); position++) {
			allowed = false;
			for (cnt2 = 0; cnt2 < permitted_chars.length(); cnt2++){
				if ( fileName.charAt(position) == permitted_chars.charAt(cnt2)) allowed = true;
			}
			if (allowed == false) {
				fileName = fileName.substring(0,position) + "." + fileName.substring(position+1, fileName.length()); 
			}
		}
		
	 //be verbose
		feedbackMessage = feedbackMessage + "\nOld filename was \""+this_filename+"\", new filename is \""+fileName+"\"\n\n";

		return fileName;
	}







	/**
	Set some default values.<BR>
	globals needed:<BR>
	globals written: geosFormat, copyToDirTrack, firstTrack, lastTrack, maxTrack, dirTrack, interleave<BR>
	*/
	public void initDefaults(){
		feedbackMessage = "";
		initTracks();
		initCbmFiles();
		
		geosFormat = false;
		copyToDirTrack = false;
		
		firstTrack = 1;
		lastTrack = 36;
		
		maxTrack = 37;
		
		dirTrack = 18;
		
		interleave = 10;	//for 1541 drives
//		interleave = 6;	//for 1571 drives
//		interleave = 1;	//for 1581 drives
	}






	/**
	Write the data and the directory entry of a single PRG file to d64-image.<BR>
	globals needed: dest_track, dest_sector<BR>
	globals written:<BR>
	calls: cbmFileName, saveFileData, addDirectoryEntry<BR>
	@param this_filename the filename to write
	@param this_filetype the filetype to write
	@param isCopyFile indicates whether a file is copied or whether a file gets inserted into the directory
	@return true if writing was successful (if there was enough space in d64-image etc)
	*/
	public boolean saveFile(String this_filename, int this_filetype, boolean isCopyFile){
		boolean success;
		
		feedbackMessage = "saveFile()\n";

		if (isCopyFile == false) {
			this_filename = this_filename.substring(0, this_filename.length()-4);
			this_filename = cbmFileName(this_filename);
		}
		
		success = saveFileData();
		
		if (success){
			success = addDirectoryEntry(this_filename, this_filetype, dest_track, dest_sector, isCopyFile);
		}
		else {
			feedbackMessage = feedbackMessage + "\nCopy Error: Some Copy Error occurred.\n";
		}

		return success;
	}


/**
Writes a new D64 file<BR>
globals_needed: (none)<BR>
globals_written: cbmDisk<BR>
@param filename	the filename
@param newDiskName	the new name (label) of the disk
@param newDiskID	the new disk-ID
@return <code>true</code> when writing of the d64 file was successful
*/
public boolean saveNewD64(String filename, String newDiskName, String newDiskID){
	int[] newD64Data = {
		//00016500 - dec = 91392	
		0x012, 0x001, 0x041 , 0x000 , 0x015 , 0x0ff , 0x0ff , 0x01f  , 0x015 , 0x0ff , 0x0ff , 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f,
		//00016510
		0x015, 0x0ff, 0x0ff, 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f  , 0x015 , 0x0ff , 0x0ff , 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f,
		//00016520  
		0x015, 0x0ff , 0x0ff , 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f  , 0x015 , 0x0ff , 0x0ff , 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f,
		//00016530  
		0x015 , 0x0ff , 0x0ff , 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f  , 0x015 , 0x0ff , 0x0ff , 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f,
		//00016540  
		0x015 , 0x0ff , 0x0ff , 0x01f , 0x015 , 0x0ff , 0x0ff , 0x01f  , 0x011 , 0x0fc , 0x0ff , 0x007 , 0x013 , 0x0ff , 0x0ff , 0x007,
		//00016550  
		0x013 , 0x0ff , 0x0ff , 0x007 , 0x013 , 0x0ff , 0x0ff , 0x007  , 0x013 , 0x0ff , 0x0ff , 0x007 , 0x013 , 0x0ff , 0x0ff , 0x007,
		//00016560  
		0x013 , 0x0ff , 0x0ff , 0x007 , 0x012 , 0x0ff , 0x0ff , 0x003  , 0x012 , 0x0ff , 0x0ff , 0x003 , 0x012 , 0x0ff , 0x0ff , 0x003,
		//00016570  
		0x012 , 0x0ff , 0x0ff , 0x003 , 0x012 , 0x0ff , 0x0ff , 0x003  , 0x012 , 0x0ff , 0x0ff , 0x003 , 0x011 , 0x0ff , 0x0ff , 0x001,
		//00016580  
		0x011 , 0x0ff , 0x0ff , 0x001 , 0x011 , 0x0ff , 0x0ff , 0x001  , 0x011 , 0x0ff , 0x0ff , 0x001 , 0x011 , 0x0ff , 0x0ff , 0x001,
		//00016590  
		0x031 , 0x032 , 0x033 , 0x034 , 0x0a0 , 0x0a0 , 0x0a0 , 0x0a0  , 0x0a0 , 0x0a0 , 0x0a0 , 0x0a0, 0x0a0, 0x0a0, 0x0a0 , 0x0a0,
		//000165a0  
		0x0a0 , 0x0a0 , 0x0a0 , 0x0a0 , 0x0a0 , 0x0a0 , 0x0a0 , 0x0a0  , 0x0a0 , 0x0a0 , 0x0a0, 00,00, 00, 00 ,00,
		//000165b0  
		00, 00, 00, 00, 00 ,00 ,00 ,00 , 00 ,00,00, 00 ,00,00, 00, 00,
		//000165c0 
		00 ,00, 00, 00, 00 ,00, 00, 00,  00, 00, 00, 00, 00, 00, 00, 00,
		//000165d0  
		00, 00 ,00, 00 ,00 ,00 ,00 ,00 , 00, 00, 00, 00, 00, 00, 00 ,00,
		//000165e0  
		00 ,00 ,00, 00 ,00, 00 ,00, 00 ,00, 00, 00, 00, 00, 00, 00, 00,
		//000165f0  
		00 ,00, 00 ,00, 00 ,00, 00 ,00,  00, 00, 00, 00 ,00 ,00 ,00,00,
		//00016600  
		00, 0x0ff ,00, 00 ,00, 00 ,00, 00,  00, 00,00, 00, 00, 00, 00, 00
	};

	boolean success = false;
		
	feedbackMessage = "saveNewD64():\n";
		
	for (int i = 0; i < cbmDisk.length; i++) {
		cbmDisk[i] = 0;
	}

	for (int i = 0; i < newD64Data.length; i++) {
		setCbmDiskValue(91392+i, newD64Data[i]);
	}
	
	setDiskName(cbmFileName(newDiskName), cbmFileName(newDiskID));
		
	success = writeD64(filename);
	return success;
}

/**
Renames a D64 file<BR>
globals_needed: (none)<BR>
globals_written: cbmDisk<BR>
@param filename	the filename
@param newDiskName	the new name (label) of the disk
@param newDiskID	the new disk-ID
@return <code>true</code> when writing of the d64 file was successful
*/
public boolean renameD64(String filename, String newDiskName, String newDiskID){
	boolean success = false;

	feedbackMessage = "renameD64():\n";
		
	setDiskName(cbmFileName(newDiskName), cbmFileName(newDiskID));
		
	success = writeD64(filename);
	return success;
}

	/**
	 * @return copyToDirTrack
	 */
	public boolean isCopyToDirTrack() {
		return copyToDirTrack;
	}


	/**
	 * @return geosFormat
	 */
	public boolean isGeosFormat() {
		return geosFormat;
	}


	/**
	 * @param b
	 */
	public void setCopyToDirTrack(boolean b) {
		copyToDirTrack = b;
	}


	/**
	 * @param b
	 */
	public void setGeosFormat(boolean b) {
		geosFormat = b;
	}


	/**
	 * @return
	 */
	public int getDirTrack() {
		return dirTrack;
	}

	/**
	 * @return
	 */
	public int getDirTrack2() {
		return dirTrack2;
	}

	/**
	 * @return
	 */
	public int getFirstTrack() {
		return firstTrack;
	}

	/**
	 * @return
	 */
	public int getInterleave() {
		return interleave;
	}

	/**
	 * @return
	 */
	public int getLastTrack() {
		return lastTrack;
	}

	/**
	 * @return
	 */
	public int getMaxTrack() {
		return maxTrack;
	}

	/**
	 * @param b
	 */
	public void setDirTrack(int b) {
		dirTrack = b;
	}

	/**
	 * @param b
	 */
	public void setDirTrack2(int b) {
		dirTrack2 = b;
	}

	/**
	 * @param b
	 */
	public void setFirstTrack(int b) {
		firstTrack = b;
	}

	/**
	 * @param b
	 */
	public void setInterleave(int b) {
		interleave = b;
	}

	/**
	 * @param b
	 */
	public void setLastTrack(int b) {
		lastTrack = b;
	}

	/**
	 * @param b
	 */
	public void setMaxTrack(int b) {
		maxTrack = b;
	}

	/**
	 * @return
	 */
	public String getFeedbackMessage() {
		return feedbackMessage;
	}
	
	/**
	 * @return
	 */
	public int getFilenumber_max() {
		return filenumber_max;
	}

/**
 * @return
 */
public String getFileType(int which_one) {
	return FileType[which_one];
}

/**
 * @return
 */
public byte[] getCbmDisk() {
	return cbmDisk;
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
	public int getMaxSectors(int track_number) {
		return tracks[track_number].getSectors();
	}

	private int getCbmDiskValue(int position){ 
		//System.out.println("getCbmDiskValue("+wo+") = "+(cbmDisk[ wo ] & 0xff));
		//System.out.println("tracks[18]getOffSet() = "+tracks[18].getOffset());
		return (cbmDisk[  position ] & 0xff);
}

	private void setCbmDiskValue(int  position, int value){ 
		cbmDisk[ position] = (byte)value;
}

	private int getSaveDataValue(int  position){ 
		return saveData[  position ] & 0xff;
}

	/**
	 * @return
	 */
	public CbmFile getCbmFile(int whichone) {
		return cbmFile[whichone];
	}

	/**
	 * @return
	 */
	public byte[] getSaveData() {
		return saveData;
	}

	/**
	 * @return
	 */
	public int getSaveDataSize() {
		return saveDataSize;
	}

	/**
	 * @param bs
	 */
	public void setSaveData(byte[] bs) {
		saveData = bs;
	}

	/**
	 * @param i
	 */
	public void setSaveDataSize(int i) {
		saveDataSize = i;
	}

	/**
	 * @param whichone
	 * @param file
	 */
	public void setCbmFile(int whichone, CbmFile file) {
		cbmFile[whichone] = file;
	}

	/**
	 * @param string_
	 */
	public void setFeedbackMessage(String string_) {
		feedbackMessage = string_;
	}

	/**
	 * @param i
	 */
	public void setFilenumber_max(int i) {
		filenumber_max = i;
	}
	
	/**
	Copies a cbmFile to bufferCbmFile.<BR>
	globals needed:<BR>
	globals written:<BR>
	@param source the source cbmFile
	@param destination the destination cbmFile
	@return
	*/
	public void copyCbmFile(CbmFile source, CbmFile destination){
		
		destination.setFile_closed(source.isFile_closed());
		destination.setFile_locked(source.isFile_locked());
		destination.setFile_scratched(source.isFile_scratched());
		destination.setFile_type(source.getFile_type());
		
		for (int i = 0; i <= 5; i++) {
			destination.setGeos(i,source.getGeos(i));
		}
		
		destination.setName(source.getName());

		destination.setDirPosition(source.getDirPosition());
		destination.setDirTrack(source.getDirTrack());
		destination.setDirSector(source.getDirSector());

		destination.setRel_track(source.getRel_track());
		destination.setRel_sector(source.getRel_sector());

		destination.setTrack(source.getTrack());
		destination.setSector(source.getSector());

		destination.setRel_sector(source.getRel_sector());

		destination.setSizeInBytes(source.getSizeInBytes());

		destination.setSizeInBlocks(source.getSizeInBlocks());
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

}
