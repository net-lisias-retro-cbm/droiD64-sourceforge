package droid64.d64;
import java.util.stream.Stream;
public enum DiskImageType {
	UNDEFINED(0, "Undefined", "Undefined image", "", "", "1541", 0),
	D64(1, "D64", "D64 (C1541)", "Normal D64 (C1541 5.25\") image", "2A", "1541", 174848),
	D71(2, "D71", "D71 (C1571)", "Normal D71 (C1571 5.25\") image", "2A", "1571", 349696),
	D81(3, "D81", "D81 (C1581)", "Normal D81 (C1581 3.5\") image", "3D", "1581", 819200),
	T64(4, "T64", "T64 (C1530)", "Normal T64 (tape) image", "", "1530", 0),
	D64_CPM_C64(5, 	"D64 CP/M (C64)", 	"CP/M D64 (C1541)",	"CP/M for C64 on D64 (C1541 5.25\") image", "2A", "1541", 174848),
	D64_CPM_C128(6, "D64 CP/M (C128)", 	"CP/M D64 (C1541)",	"CP/M for C128 on D64 (C1541 5.25\") image", "2A", "1541", 349696),
	D71_CPM(7, 		"D71 CP/M", 		"CP/M D71 (C1571)", "CP/M for C128 on D71 (C1571 5.25\") image", "2A", "1571", 349696),
	D81_CPM(8, 		"D81 CP/M", 		"CP/M D81 (C1581)", "CP/M on D81 (C1581 3.5\") image", "3D", "1581", 819200),
	D82(9,  "D82", "D82 (C8250)", "Normal D81 (C8250 5.25\") image", "2A", "8250", 1066496),
	D80(10, "D80", "D80 (C8050)", "Normal D81 (C8050 5.25\") image", "2A", "8050", 533248),
	D67(11, "D67", "D67 (C2040)", "Normal D81 (C2040 5.25\") image", "2A", "2040", 176640),
	LNX(12, "LNX", "Lynx", "Normal Lynx archive", "2A", "1541", 0),
	D88(13, "D88", "D88 (C8280)", "Normal D88 (C8280 8\") image", "3A", "8280", 1025024);

	public final int type;
	public final String id;
	public final String longName;
	public final String description;
	public final String dosVersion;
	public final String driveName;
	public final int expectedSize;

	private DiskImageType(int type, String id, String longName, String description, String dosVersion, String driveName, int expectedSize) {
		this.type = type;
		this.id = id;
		this.longName = longName;
		this.description = description;
		this.dosVersion = dosVersion;
		this.driveName = driveName;
		this.expectedSize = expectedSize;
	}

	public static String[] getNames() {
		DiskImageType[] values = values();
		String[] names = new String[values.length];
		for (int i=0; i < values.length; i++) {
			names[i] = values[i].id;
		}
		return names;
	}

	public static Stream<DiskImageType> stream() {
		return Stream.of(values());
	}

	@Override
	public String toString() {
		return id;
	}

	public static DiskImageType get(int type) {
		for (DiskImageType ft : values()) {
			if (ft.type == type) {
				return ft;
			}
		}
		return UNDEFINED;
	}
}
