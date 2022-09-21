package droid64.d64;
import java.util.stream.Stream;
public enum FileType {
	DEL(0, "DEL file"),
	SEQ(1, "SEQ file"),
	PRG(2, "PRG file"),
	USR(3, "USR file"),
	REL(4, "REL file"),
	CBM(5, "CBM partition file");
	public final int type;
	public final String description;
	private FileType(int type, String description) {
		this.type = type;
		this.description = description;
	}
	public static FileType get(int type) {
		for (FileType ft : values()) {
			if (ft.type == type) {
				return ft;
			}
		}
		return null;
	}
	public static String[] getNames() {
		FileType[] values = values();
		String[] names = new String[values.length];
		for (int i=0; i < values.length; i++) {
			names[i] = values[i].name();
		}
		return names;
	}
	public static Stream<FileType> stream() {
		return Stream.of(values());
	}
}
