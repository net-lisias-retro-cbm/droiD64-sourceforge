package droid64.gui;

import droid64.d64.Utility;

public class RenameResult {

	private String fileName = Utility.EMPTY;
	private int fileType = 0;
	private String diskName = Utility.EMPTY;
	private String diskID = Utility.EMPTY;
	private int diskType = 0;
	private boolean compressedDisk = false;
	private boolean cpmDisk = false;
	private int partitionSectorCount;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
	}

	public void setDiskType(int diskType) {
		this.diskType = diskType;
	}

	public String getDiskName() {
		return diskName;
	}

	public void setDiskName(String diskName) {
		this.diskName = diskName;
	}

	public String getDiskID() {
		return diskID;
	}

	public void setDiskID(String diskID) {
		this.diskID = diskID;
	}

	public int getDiskType() {
		return diskType;
	}

	public boolean isCompressedDisk() {
		return compressedDisk;
	}

	public void setCompressedDisk(boolean compressedDisk) {
		this.compressedDisk = compressedDisk;
	}

	public boolean isCpmDisk() {
		return cpmDisk;
	}

	public void setCpmDisk(boolean cpmDisk) {
		this.cpmDisk = cpmDisk;
	}

	public int getPartitionSectorCount() {
		return partitionSectorCount;
	}

	public void setPartitionSectorCount(int partitionSectorCount) {
		this.partitionSectorCount = partitionSectorCount;
	}

	@Override
	public String toString() {
		return "RenameResult [fileName=" + fileName + ", fileType="
				+ fileType + ", diskName=" + diskName + ", diskID=" + diskID + ", diskType=" + diskType
				+ ", compressedDisk=" + compressedDisk + ", cpmDisk=" + cpmDisk + ", partitionSectorCount="
				+ partitionSectorCount + "]";
	}
}
