package droid64.db;

/**
 * Class to keep one row returned from the search query.
 * @author Henrik
 */
public class SearchResultRow {

	private String path;
	private String disk;
	private String label;
	private String file;
	private String type;
	private Integer size;
	
	/**
	 * Default constructor
	 */
	public SearchResultRow() {
	}

	/**
	 * Constructor.
	 * @param path String
	 * @param disk String
	 * @param label String
	 * @param file String
	 * @param type String
	 * @param size Integer
	 */
	public SearchResultRow(String path, String disk, String label, String file, String type, Integer size) {
		this.path = path;
		this.disk = disk;
		this.label = label;
		this.file = file;
		this.type = type;
		this.size = size;
	}
		
	public String getPath() {
		return path;
	}

	public void setPath(String path) {
		this.path = path;
	}

	public String getDisk() {
		return disk;
	}

	public void setDisk(String disk) {
		this.disk = disk;
	}

	public String getFile() {
		return file;
	}

	public void setFile(String file) {
		this.file = file;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public Integer getSize() {
		return size;
	}

	public void setSize(Integer size) {
		this.size = size;
	}

	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append("SearchResultRow[");
		builder.append(" .path=").append(path);
		builder.append(" .disk=").append(disk);
		builder.append(" .label=").append(label);
		builder.append(" .file=").append(file);
		builder.append(" .type=").append(type);
		builder.append(" .size=").append(size);
		builder.append("]");
		return builder.toString();
	}

}
