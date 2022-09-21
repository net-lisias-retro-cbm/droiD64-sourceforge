package droid64.gui;
import java.awt.Color;
import java.awt.Font;
import java.io.File;
import java.util.List;
/** Parameter types */
public enum ParameterType {
	/** paramName=text */
	STRING(String.class),
	/** paramName=integer */
	INTEGER(Integer.class),
	/** paramName={ yes | no } */
	BOOLEAN(Boolean.class),
	/** paramName=r,g,b */
	COLOR(Color.class),
	/** paramName=fontName */
	FONT(Font.class),
	/** paramName=string;string;string ... */
	STRING_LIST(List.class),
	/** paramName.x=value */
	INDEXED_STRING(List.class),
	/** paramName=filename */
	FILE(File.class);
	protected final Class<?> clazz;
	private ParameterType(Class<?> clazz) {
		this.clazz = clazz;
	}
}