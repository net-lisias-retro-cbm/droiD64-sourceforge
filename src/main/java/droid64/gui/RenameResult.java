package droid64.gui;

public class RenameResult {

	private String fileName = "";
	private boolean success = false;
	private int fileType = 0;
	private String diskName = "";
	private String diskID = "";
	private int diskType = 0;
	private boolean compressedDisk = false;
	private boolean cpmDisk = false;

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public boolean isSuccess() {
		return success;
	}

	public void setSuccess(boolean success) {
		this.success = success;
	}

	public int getFileType() {
		return fileType;
	}

	public void setFileType(int fileType) {
		this.fileType = fileType;
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

	public void setDiskType(int diskType) {
		this.diskType = diskType;
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

}
