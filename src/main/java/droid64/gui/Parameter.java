package droid64.gui;

import java.awt.Color;
import java.awt.Font;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**<pre style='font-family:sans-serif;'>
 * Created on 03.05.2018
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
 *   eMail: wolfvoz@users.sourceforge.net
 *   http://droid64.sourceforge.net
 *
 * @author henrik
 * </pre>
 */
public class Parameter {

	// Types of parameters
	/** paramName=text */
	public static final int STRING_PARAM = 1;
	/** paramName=integer */
	public static final int INTEGER_PARAM = 2;
	/** paramName={ yes | no } */
	public static final int BOOLEAN_PARAM = 3;
	/** paramName=r,g,b */
	public static final int COLOR_PARAM = 4;
	/** paramName=fontName */
	public static final int FONT_PARAM = 5;
	/** paramName=string;string;string ... */
	public static final int STRING_LIST_PARAM = 6;
	/** paramName.x=value */
	public static final int INDEXED_STRING_PARAM = 7;

	private static final String DELIM = ";";
	private static final String LF = "\n";
	private static final String EQ = "=";
	private static final String DOT = ".";
	private static final String STRING_LIST_SPLIT_EXPR = "\\s*[;]\\s*";

	private String name;
	private int type;
	private Object value;

	public Parameter(String name, int type, Object value) {
		this.name = name;
		this.type = type;
		this.value = value;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (obj == null || obj.getClass() != this.getClass()) {
			return false;
		}

		Parameter that = (Parameter) obj;
		if (this.type != that.getType()) {
			return false;
		}
		if (!stringsEqual(name, that.name)) {
			return false;
		}
		if (this.value == null || that.value == null) {
			return this.value == null && that.value == null;
		}
		return this.value.equals(that.value);
	}

	@Override
	public int hashCode() {
		return (value != null ? value.hashCode() : 0) + (name != null ? name.hashCode() : 0) + type;
	}

	private boolean stringsEqual(String s1, String s2) {
		if (s1 == s2) {
			return true;
		}
		if (s1 == null || s2 == null) {
			return false;
		}
		return s1.equals(s2);
	}

	public void parseValue(String lineValue) {
		try {
			switch (type) {
			case Parameter.STRING_PARAM:
				setStringValue(lineValue != null ? lineValue.replaceAll("[\\r\\n]+$", "") : null);
				break;
			case Parameter.INTEGER_PARAM:
				setIntegerValue(lineValue != null ? Integer.valueOf(lineValue.trim()) : null);
				break;
			case Parameter.BOOLEAN_PARAM:
				if (lineValue == null) {
					setBooleanValue(false);
				} else {
					setBooleanValue("yes".equals(lineValue.trim()) ? Boolean.TRUE : Boolean.valueOf(lineValue.trim()));
				}
				break;
			case Parameter.COLOR_PARAM:
				splitStringIntoColorParam(lineValue);
				break;
			case Parameter.FONT_PARAM:
				setFontValue(lineValue);
				break;
			case Parameter.STRING_LIST_PARAM:
				setStringListParam(lineValue);
				break;
			default:
			}
		} catch (IllegalArgumentException e) {	//NOSONAR
			System.err.println("Failed to parse setting: "+e.getMessage());	//NOSONAR
		}
	}

	public void setStringListParam(String value) {
		List<String> list = new ArrayList<>();
		setStringListValue(list);
		if (value != null) {
			for (String str : Arrays.asList(value.replaceAll("[\\r\\n]+$", "").split(STRING_LIST_SPLIT_EXPR))) {
				list.add(str);
			}
		}
	}

	private void splitStringIntoColorParam(String str) {
		if (str == null) {
			return;
		}
		String[] split = str.trim().split("\\s*[,;.]\\s*");
		if (split.length >= 3) {
			int r = Integer.parseInt(split[0].trim()) & 0xff;
			int g = Integer.parseInt(split[1].trim()) & 0xff;
			int b = Integer.parseInt(split[2].trim()) & 0xff;
			setColorValue(new Color(r, g, b));
		}
	}

	public int getType() {
		return type;
	}

	public String getName() {
		return name;
	}

	/** Avoid using. Use get&lt;<i>Type</i>&gt;value() instead. */
	public Object getValue() {
		return value;
	}

	@Override
	public String toString() {
		StringBuilder buf = new StringBuilder();
		buf.append("Parameter[");
		buf.append(" .name=").append(name);
		buf.append(" .type=").append(type);
		buf.append(" .value=").append(value);
		buf.append("]");
		return buf.toString();
	}

	// Getters

	public Boolean getBooleanValue() {
		if (type == BOOLEAN_PARAM && (value instanceof Boolean || value == null)) {
			return (Boolean) value;
		}
		throw new IllegalArgumentException(name + " is not a boolean parameter.");
	}

	public Color getColorValue() {
		if (type == COLOR_PARAM && (value instanceof Color || value == null)) {
			return (Color) value;
		}
		throw new IllegalArgumentException(name + " is not a color parameter.");
	}

	public Font getFontValue() {
		if (type == FONT_PARAM && (value instanceof Font || value == null)) {
			return (Font) value;
		}
		throw new IllegalArgumentException(name + " is not a font parameter.");
	}

	@SuppressWarnings("unchecked")
	public List<String> getIndexedStringValue() {
		if (type == INDEXED_STRING_PARAM) {
			if (value == null) {
				value = new ArrayList<>();
			}
			if (value instanceof List) {
				return (List<String>) value;
			}
		}
		throw new IllegalArgumentException(name + " is not an indexed string parameter.");
	}

	public Integer getIntegerValue() {
		if (type == INTEGER_PARAM && (value instanceof Integer || value == null)) {
			return (Integer) value;
		}
		throw new IllegalArgumentException(name + " is not an integer parameter.");
	}

	@SuppressWarnings("unchecked")
	public List<String> getStringListValue() {
		if (type == STRING_LIST_PARAM) {
			if (value == null) {
				value = new ArrayList<String>();
			}
			if (value instanceof List) {
				return (List<String>) value;
			}
		}
		throw new IllegalArgumentException(name + " is not a list parameter. ");
	}

	public String getStringValue() {
		if (type == STRING_PARAM && (value instanceof String || value == null)) {
			return (String) value;
		}
		throw new IllegalArgumentException(name + " is not string parameter.");
	}

	// Setters

	public void setBooleanValue(Boolean value) {
		if (type == BOOLEAN_PARAM) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not a boolean parameter.");
		}
	}

	public void setColorValue(Color value) {
		if (type == COLOR_PARAM) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not a color parameter.");
		}
	}

	public void setFontValue(Font value) {
		if (type == FONT_PARAM) {
			if (value != null) {
				this.value = value;
			}
		} else {
			throw new IllegalArgumentException(name + " is not a font parameter.");
		}
	}

	public void setFontValue(String str) {
		if (str != null) {
			String[] sa = str.split(STRING_LIST_SPLIT_EXPR);
			if (sa.length >= 3) {
				setFontValue(new Font(sa[0].trim(), Integer.parseInt(sa[1].trim()), Integer.parseInt(sa[2].trim())));
			} else {
				throw new IllegalArgumentException(name + " is missing attributes. ");
			}
		}
	}

	public void setIndexedStringValue(List<String> list) {
		if (type == INDEXED_STRING_PARAM) {
			this.value = list;
		} else {
			throw new IllegalArgumentException(name + " is not an indexed list parameter. ");
		}
	}

	public void setIntegerValue(Integer value) {
		if (type == INTEGER_PARAM) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not an integer parameter.");
		}
	}

	public void setStringListValue(List<String> list) {
		if (type == STRING_LIST_PARAM) {
			this.value = list;
		} else {
			throw new IllegalArgumentException(name + " is not a list parameter. ");
		}
	}

	public void setStringValue(String value) {
		if (type == STRING_PARAM) {
			this.value = value;
		} else {
			throw new IllegalArgumentException(name + " is not a string parameter.");
		}
	}

	public String getParamAsString() {
		switch (type) {
		case Parameter.COLOR_PARAM :
			Color c = getColorValue();
			return name + EQ + c.getRed() + "," + c.getGreen() + "," + c.getBlue() + LF;
		case Parameter.FONT_PARAM:
			Font fnt = getFontValue();
			if (fnt != null) {
				return name + EQ + fnt.getName() + DELIM + fnt.getStyle() + DELIM + fnt.getSize() + LF;
			} else {
				return name + EQ + LF;
			}
		case Parameter.STRING_LIST_PARAM:
			return name + EQ + getStringListParamAsString() + LF;
		case Parameter.INDEXED_STRING_PARAM:
			List<String> list = getIndexedStringValue();
			StringBuilder buf = new StringBuilder();
			for (int i=0; i<list.size(); i++) {
				buf.append(name + DOT + i + EQ + list.get(i) + LF);
			}
			return buf.toString();
		default:
			return name + EQ + String.valueOf(value) + LF;
		}
	}

	public String getStringListParamAsString() {
		List<String> list = getStringListValue();
		StringBuilder buf = new StringBuilder();
		for (String str : list) {
			if (buf.length() > 0) {
				buf.append(DELIM);
			}
			buf.append(str);
		}
		return buf.toString();
	}

}
