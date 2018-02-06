package droid64.db;

/**
 * Class to save search criteria when searching in the database for disks and files.
 * @author Henrik
 */
public class DiskSearchCriteria {

	private String diskLabel;
	private String fileName;
	private Integer fileSizeMin, fileSizeMax;
	private Integer fileType;
	private String diskPath;
	private String diskFileName;
	private Integer imageType;
	
	public String getFileName() {
		return fileName;
	}
	public void setFileName(String fileName) {
		this.fileName = fileName;
	}
	public Integer getFileSizeMin() {
		return fileSizeMin;
	}
	public void setFileSizeMin(Integer fileSizeMin) {
		this.fileSizeMin = fileSizeMin;
	}
	public Integer getFileSizeMax() {
		return fileSizeMax;
	}
	public void setFileSizeMax(Integer fileSizeMax) {
		this.fileSizeMax = fileSizeMax;
	}
	public Integer getFileType() {
		return fileType;
	}
	public void setFileType(Integer fileType) {
		this.fileType = fileType;
	}
	public String getDiskLabel() {
		return diskLabel;
	}
	public void setDiskLabel(String diskLabel) {
		this.diskLabel = diskLabel;
	}
	
	public void setDiskPath(String diskPath) {
		this.diskPath = diskPath;
	}
	public String getDiskPath() {
		return diskPath;
	}	

	public void setDiskFileName(String diskFileName) {
		this.diskFileName = diskFileName;
	}
	
	public String getDiskFileName() {
		return diskFileName;
	}

	public Integer getImageType() {
		return imageType;
	}
	
	public void setImageType(Integer imageType) {
		this.imageType = imageType;
	}
	
	/**
	 * Has search criteria.
	 * @return false if all values are null or empty.
	 */
	public boolean hasCriteria() {
		return !( (fileName==null || fileName.trim().isEmpty()) && 
				  (diskLabel==null || diskLabel.trim().isEmpty()) && 
				  (diskPath==null || diskPath.trim().isEmpty()) && 
				  (diskFileName==null || diskFileName.trim().isEmpty()) && 
				  (imageType==null) &&
				  fileSizeMin==null && fileSizeMax==null && fileType == null);
	}
	
	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("DiskSearchCriteria[");
		builder.append(" .diskLabel=").append(diskLabel);
		builder.append(" .diskPath=").append(diskPath);
		builder.append(" .diskFileName=").append(diskFileName);
		builder.append(" .fileName=").append(fileName);
		builder.append(" .fileSizeMin=").append(fileSizeMin);
		builder.append(" .fileSizeMax=").append(fileSizeMax);
		builder.append(" .imageType=").append(imageType);
		builder.append(" .fileType=").append(fileType).append("]");
		return builder.toString();
	}
	
}