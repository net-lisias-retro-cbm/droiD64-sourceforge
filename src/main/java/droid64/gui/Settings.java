package droid64.gui;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.MissingResourceException;
import java.util.ResourceBundle;
import java.util.TreeSet;

import javax.swing.JPanel;
import javax.swing.JTextField;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.d64.Utility;

public class Settings {

	private static final String DROID64 = "droid64";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(DROID64);
	public static final String DIR_FONT_NAME = "droiD64_cbm.ttf";
	private static final JPanel DEFAULT_PANEL = new JPanel();
	public static final int MAX_PLUGINS = 4;

	private static final String DELIM = ";";
	private static final String LF = "\n";
	private static final String EQ = "=";

	public static final Color DIR_BG_COLOR_C64 = new Color( 64, 64, 224);
	public static final Color DIR_FG_COLOR_C64 = new Color(160,160, 255);
	public static final Color DIR_BG_COLOR_CPM = new Color( 16,  16,  16);
	public static final Color DIR_FG_COLOR_CPM = new Color(192, 255, 192);
	public static final Color DIR_BG_COLOR_LOCAL = DEFAULT_PANEL.getBackground();
	public static final Color DIR_FG_COLOR_LOCAL = DEFAULT_PANEL.getForeground();
	public static final Color ACTIVE_BORDER_COLOR = Color.RED;
	public static final Color INACTIVE_BORDER_COLOR = Color.GRAY;

	/** Default name of settings file (without path). */
	private static final String DEFAULT_SETTING_FILE_NAME = ".droiD64.cfg";
	/** Name (with path) actually used. */
	private static String settingsFileName = null;

	// Setting file parameters
	private static final String SETTING_ASK_QUIT            = "ask_quit";
	public  static final String SETTING_DIR_BG              = "color_dir_bg";
	public  static final String SETTING_DIR_FG              = "color_dir_fg";
	public  static final String SETTING_DIR_CPM_BG          = "color_dir_cpm_bg";
	public  static final String SETTING_DIR_CPM_FG          = "color_dir_cpm_fg";
	public  static final String SETTING_DIR_LOCAL_BG        = "color_dir_local_bg";
	public  static final String SETTING_DIR_LOCAL_FG        = "color_dir_local_fg";
	private static final String SETTING_BORDER_ACTIVE       = "color_border_active";
	private static final String SETTING_BORDER_INACTIVE     = "color_border_inactive";
	private static final String SETTING_COLOUR              = "colour";
	private static final String SETTING_DEFAULT_IMAGE_DIR   = "default_image_dir";
	private static final String SETTING_DEFAULT_IMAGE_DIR2  = "default_image_dir2";
	private static final String SETTING_FONT_SIZE           = "font_size";
	private static final String SETTING_ROW_HEIGHT          = "row_height";
	private static final String SETTING_LOCAL_ROW_HEIGHT    = "local_row_height";
	private static final String SETTING_LOCAL_FONT_SIZE     = "local_font_size";
	private static final String SETTING_PLUGIN_DESCRIPTION  = "plugin_description";
	private static final String SETTING_PLUGIN_COMMAND      = "plugin_command";
	private static final String SETTING_PLUGIN_ARGUMENTS    = "plugin_arguments";
	private static final String SETTING_PLUGIN_LABEL        = "plugin_label";
	private static final String SETTING_PLUGIN_FORK         = "plugin_fork";
	private static final String SETTING_USE_DB              = "use_database";
	private static final String SETTING_JDBC_DRIVER         = "jdbc_driver";
	private static final String SETTING_JDBC_URL            = "jdbc_url";
	private static final String SETTING_JDBC_USER           = "jdbc_user";
	private static final String SETTING_JDBC_PASS       	= "jdbc_password";
	private static final String SETTING_JDBC_LIMIT_TYPE		= "jdbc_limit_type";
	private static final String SETTING_MAX_ROWS            = "max_rows";
	private static final String SETTING_LOOK_AND_FEEL       = "look_and_feel";
	private static final String SETTING_WINDOW              = "window";

	private static final String SETTING_SYS_FONT            = "sys_font";
	private static final String SETTING_CBM_FONT            = "cbm_font";

	private static final String SETTING_FILE_EXT_D64        = "file_ext_d64";
	private static final String SETTING_FILE_EXT_D67        = "file_ext_d67";
	private static final String SETTING_FILE_EXT_D71        = "file_ext_d71";
	private static final String SETTING_FILE_EXT_D81        = "file_ext_d81";
	private static final String SETTING_FILE_EXT_T64        = "file_ext_t64";
	private static final String SETTING_FILE_EXT_D80        = "file_ext_d80";
	private static final String SETTING_FILE_EXT_D82        = "file_ext_d82";
	private static final String SETTING_FILE_EXT_LNX        = "file_ext_lnx";
	private static final String SETTING_FILE_EXT_D64_GZ        = "file_ext_d64_gz";
	private static final String SETTING_FILE_EXT_D67_GZ        = "file_ext_d67_gz";
	private static final String SETTING_FILE_EXT_D71_GZ        = "file_ext_d71_gz";
	private static final String SETTING_FILE_EXT_D81_GZ        = "file_ext_d81_gz";
	private static final String SETTING_FILE_EXT_T64_GZ        = "file_ext_t64_gz";
	private static final String SETTING_FILE_EXT_D80_GZ        = "file_ext_d80_gz";
	private static final String SETTING_FILE_EXT_D82_GZ        = "file_ext_d82_gz";
	private static final String SETTING_FILE_EXT_LNX_GZ        = "file_ext_lnx_gz";

	// For convenience
	private static final String USER_HOME = System.getProperty("user.home");

	private static ExternalProgram[] externalPrograms = new ExternalProgram[MAX_PLUGINS];
	private static Font commodoreFont = null;
	private static Font commodoreScaledFont = null;
	private static Integer commodoreFontSize = null;

	/** Setting parameter definitions */
	private static final Map<String,Parameter> settingTypeMap = initMap();

	private Settings() {
	}

	private static Map<String,Parameter> initMap() {
		Map<String,Parameter> map = new HashMap<>();
		map.put(SETTING_ASK_QUIT,			new Parameter(SETTING_ASK_QUIT,				Parameter.BOOLEAN_PARAM,	Boolean.TRUE));
		map.put(SETTING_BORDER_ACTIVE,		new Parameter(SETTING_BORDER_ACTIVE,		Parameter.COLOR_PARAM,		ACTIVE_BORDER_COLOR));
		map.put(SETTING_BORDER_INACTIVE,	new Parameter(SETTING_BORDER_INACTIVE,		Parameter.COLOR_PARAM,		INACTIVE_BORDER_COLOR));
		map.put(SETTING_COLOUR,				new Parameter(SETTING_COLOUR,				Parameter.INTEGER_PARAM,	Integer.valueOf(1)));
		map.put(SETTING_DEFAULT_IMAGE_DIR,	new Parameter(SETTING_DEFAULT_IMAGE_DIR,	Parameter.STRING_PARAM,		USER_HOME));
		map.put(SETTING_DEFAULT_IMAGE_DIR2,	new Parameter(SETTING_DEFAULT_IMAGE_DIR2,	Parameter.STRING_PARAM,		USER_HOME));
		map.put(SETTING_DIR_BG,				new Parameter(SETTING_DIR_BG,				Parameter.COLOR_PARAM,		DIR_BG_COLOR_C64));
		map.put(SETTING_DIR_FG,				new Parameter(SETTING_DIR_FG,				Parameter.COLOR_PARAM,		DIR_FG_COLOR_C64));
		map.put(SETTING_DIR_CPM_BG,			new Parameter(SETTING_DIR_CPM_BG,			Parameter.COLOR_PARAM,		DIR_BG_COLOR_CPM));
		map.put(SETTING_DIR_CPM_FG,			new Parameter(SETTING_DIR_CPM_FG,			Parameter.COLOR_PARAM,		DIR_FG_COLOR_CPM));
		map.put(SETTING_DIR_LOCAL_BG,		new Parameter(SETTING_DIR_LOCAL_BG,			Parameter.COLOR_PARAM,		DIR_BG_COLOR_LOCAL));
		map.put(SETTING_DIR_LOCAL_FG,		new Parameter(SETTING_DIR_LOCAL_FG,			Parameter.COLOR_PARAM,		DIR_FG_COLOR_LOCAL));
		map.put(SETTING_FONT_SIZE,			new Parameter(SETTING_FONT_SIZE,			Parameter.INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_LOCAL_FONT_SIZE,	new Parameter(SETTING_LOCAL_FONT_SIZE,		Parameter.INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_WINDOW,				new Parameter(SETTING_WINDOW,				Parameter.STRING_PARAM,		Utility.EMPTY));
		map.put(SETTING_JDBC_DRIVER,		new Parameter(SETTING_JDBC_DRIVER,			Parameter.STRING_PARAM,		"com.mysql.jdbc.Driver"));
		map.put(SETTING_JDBC_URL,			new Parameter(SETTING_JDBC_URL,				Parameter.STRING_PARAM,		"jdbc:mysql://localhost:3306/droid64"));
		map.put(SETTING_JDBC_USER,			new Parameter(SETTING_JDBC_USER,			Parameter.STRING_PARAM,		DROID64));
		map.put(SETTING_JDBC_PASS,			new Parameter(SETTING_JDBC_PASS,			Parameter.STRING_PARAM,		"uridium"));
		map.put(SETTING_JDBC_LIMIT_TYPE,	new Parameter(SETTING_JDBC_LIMIT_TYPE,		Parameter.INTEGER_PARAM,	Integer.valueOf(0)));
		map.put(SETTING_MAX_ROWS,			new Parameter(SETTING_MAX_ROWS,				Parameter.INTEGER_PARAM,	Integer.valueOf(25)));
		map.put(SETTING_ROW_HEIGHT,			new Parameter(SETTING_ROW_HEIGHT,			Parameter.INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_LOCAL_ROW_HEIGHT,	new Parameter(SETTING_LOCAL_ROW_HEIGHT,		Parameter.INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_USE_DB,				new Parameter(SETTING_USE_DB,				Parameter.BOOLEAN_PARAM,	Boolean.FALSE));
		map.put(SETTING_LOOK_AND_FEEL,		new Parameter(SETTING_LOOK_AND_FEEL,		Parameter.INTEGER_PARAM,	Integer.valueOf(1)));
		map.put(SETTING_PLUGIN_COMMAND,		new Parameter(SETTING_PLUGIN_COMMAND,		Parameter.INDEXED_STRING_PARAM,
				Arrays.asList( "d64copy", "x64", "128", "cbmctrl" )));
		map.put(SETTING_PLUGIN_ARGUMENTS,	new Parameter(SETTING_PLUGIN_ARGUMENTS,		Parameter.INDEXED_STRING_PARAM,
				Arrays.asList( "{Image} 8", "-drive8type {DriveType} {ImageFiles}", "-drive8type {DriveType} {ImageFiles}", "dir 8" )));
		map.put(SETTING_PLUGIN_DESCRIPTION,	new Parameter(SETTING_PLUGIN_DESCRIPTION,	Parameter.INDEXED_STRING_PARAM,
				Arrays.asList(
						"Transfer this disk image to a real floppy.", "Invoke VICE 64 emulator with this disk image",
						"Invoke VICE 128 emulator with this disk image", "List files using OpenCBM" )));
		map.put(SETTING_PLUGIN_LABEL,		new Parameter(SETTING_PLUGIN_LABEL,			Parameter.INDEXED_STRING_PARAM,
				Arrays.asList( "d64copy", "VICE 64", "VICE 128", "CBM dir" )));
		map.put(SETTING_PLUGIN_FORK,		new Parameter(SETTING_PLUGIN_FORK,			Parameter.INDEXED_STRING_PARAM,
				Arrays.asList( "true", "true", "true", "true" )));
		map.put(SETTING_FILE_EXT_D64,		new Parameter(SETTING_FILE_EXT_D64,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".d64".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D67,		new Parameter(SETTING_FILE_EXT_D67,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".d67".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D71,		new Parameter(SETTING_FILE_EXT_D71,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".d71".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D81,		new Parameter(SETTING_FILE_EXT_D81,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".d81".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_T64,		new Parameter(SETTING_FILE_EXT_T64,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".t64".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D80,		new Parameter(SETTING_FILE_EXT_D80,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".d80".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D82,		new Parameter(SETTING_FILE_EXT_D82,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".d82".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_LNX,		new Parameter(SETTING_FILE_EXT_LNX,			Parameter.STRING_LIST_PARAM,	Arrays.asList(".lnx".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D64_GZ,	new Parameter(SETTING_FILE_EXT_D64_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("d64.gz".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D67_GZ,	new Parameter(SETTING_FILE_EXT_D67_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("d67.gz".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D71_GZ,	new Parameter(SETTING_FILE_EXT_D71_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("d71.gz".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D81_GZ,	new Parameter(SETTING_FILE_EXT_D81_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("d81.gz".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_T64_GZ,	new Parameter(SETTING_FILE_EXT_T64_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("t64.gz".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D80_GZ,	new Parameter(SETTING_FILE_EXT_D80_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("d80.gz".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_D82_GZ,	new Parameter(SETTING_FILE_EXT_D82_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("d82.gz".split(DELIM)) ));
		map.put(SETTING_FILE_EXT_LNX_GZ,	new Parameter(SETTING_FILE_EXT_LNX_GZ,		Parameter.STRING_LIST_PARAM,	Arrays.asList("lnx.gz".split(DELIM)) ));
		map.put(SETTING_SYS_FONT,			new Parameter(SETTING_SYS_FONT,				Parameter.FONT_PARAM,			null));
		map.put(SETTING_CBM_FONT,			new Parameter(SETTING_CBM_FONT,				Parameter.FONT_PARAM,			null));

		return map;
	}

	public static void setSysFont(Font font) {
		setFontParameter(SETTING_SYS_FONT, font);
	}
	public static Font getSysFont() {
		return getFontParameter(SETTING_SYS_FONT);
	}
	public static void setCbmFont(Font font) {
		commodoreFont = null;
		setFontParameter(SETTING_CBM_FONT, font);
	}
	public static Font getCbmFont() {
		return getFontParameter(SETTING_CBM_FONT);
	}

	public static void setActiveBorderColor(Color color) {
		setColorParam(SETTING_BORDER_ACTIVE, color);
	}
	public static Color getActiveBorderColor() {
		return getColorParam(SETTING_BORDER_ACTIVE);
	}
	public static void setInactiveBorderColor(Color color) {
		setColorParam(SETTING_BORDER_INACTIVE, color);
	}
	public static Color getInactiveBorderColor() {
		return getColorParam(SETTING_BORDER_INACTIVE);
	}
	public static void setDirColors(Color bg, Color fg) {
		setColorParam(SETTING_DIR_BG, bg);
		setColorParam(SETTING_DIR_FG, fg);
	}
	public static Color getDirColorBg() {
		return getColorParam(SETTING_DIR_BG);
	}
	public static Color getDirColorFg() {
		return getColorParam(SETTING_DIR_FG);
	}
	public static void setDirCpmColors(Color bg, Color fg) {
		setColorParam(SETTING_DIR_CPM_BG, bg);
		setColorParam(SETTING_DIR_CPM_FG, fg);
	}
	public static Color getDirCpmColorBg() {
		return getColorParam(SETTING_DIR_CPM_BG);
	}
	public static Color getDirCpmColorFg() {
		return getColorParam(SETTING_DIR_CPM_FG);
	}
	public static void setDirLocalColors(Color bg, Color fg) {
		setColorParam(SETTING_DIR_LOCAL_BG, bg);
		setColorParam(SETTING_DIR_LOCAL_FG, fg);
	}
	public static Color getDirLocalColorBg() {
		return getColorParam(SETTING_DIR_LOCAL_BG);
	}
	public static Color getDirLocalColorFg() {
		return getColorParam(SETTING_DIR_LOCAL_FG);
	}
	public static void parseExternalPrograms() {
		List<ExternalProgram> list = new ArrayList<>();
		List <String> cmdList = getIndexStringList(SETTING_PLUGIN_COMMAND);
		List <String> argList = getIndexStringList(SETTING_PLUGIN_ARGUMENTS);
		List <String> descrList = getIndexStringList(SETTING_PLUGIN_DESCRIPTION);
		List <String> labelList = getIndexStringList(SETTING_PLUGIN_LABEL);
		List <String> forkList = getIndexStringList(SETTING_PLUGIN_FORK);
		for (int i=0; i < Settings.MAX_PLUGINS; i++) {
			String cmd = i <cmdList.size() ? cmdList.get(i) : Utility.EMPTY;
			String args = i <argList.size() ? argList.get(i) : Utility.EMPTY;
			String descr = i <descrList.size() ? descrList.get(i) : Utility.EMPTY;
			String label = i <labelList.size() ? labelList.get(i) : Utility.EMPTY;
			boolean forkThread = i <forkList.size() ? Boolean.valueOf(forkList.get(i)) : true;
			if (Utility.EMPTY.equals(cmd) && Utility.EMPTY.equals(descr) && Utility.EMPTY.equals(label)) {
				list.add(null);
			} else {
				list.add(new ExternalProgram(cmd, args, descr, label, forkThread));
			}
		}
		externalPrograms = list.toArray(new ExternalProgram[Settings.MAX_PLUGINS]);
	}

	public static ExternalProgram[] getExternalPrograms() {
		return externalPrograms;
	}

	private static List<String> getIndexStringList(String key) {
		Parameter param = settingTypeMap.get(key);
		if (param != null) {
			return param.getIndexedStringValue();
		} else {
			return new ArrayList<>();
		}
	}

	public static void setExternalProgram(int num, String cmd, String args, String descr, String label, boolean forkThread) {
		setIndexedString(SETTING_PLUGIN_COMMAND, num, cmd);
		setIndexedString(SETTING_PLUGIN_ARGUMENTS, num, args);
		setIndexedString(SETTING_PLUGIN_DESCRIPTION, num, descr);
		setIndexedString(SETTING_PLUGIN_LABEL, num, label);
		setIndexedString(SETTING_PLUGIN_FORK, num, Boolean.toString(forkThread));
	}
	private static void setIndexedString(String key, int num, String value) {
		Parameter param = settingTypeMap.get(key);
		List<String> list = param.getIndexedStringValue();
		for (int i=list.size(); i <= num; i++) {
			list.add(Utility.EMPTY);
		}
		list.set(num, value);
	}

	//////////////////////////

	public static void setWindow(int[] sizeLocation) {
		if (sizeLocation != null && sizeLocation.length >= 4) {
			String value = String.format("%d:%d,%d:%d", sizeLocation[0], sizeLocation[1], sizeLocation[2], sizeLocation[3]);
			setStringParameter(SETTING_WINDOW, value);
		} else {
			setStringParameter(SETTING_WINDOW, Utility.EMPTY);
		}
	}

	public static int[] getWindow() {
		String str = getStringParameter(SETTING_WINDOW, null);
		if (str == null) {
			return new int[0];
		} else {
			String[] split = str.split("\\s*[,:]\\s*");
			if (split.length < 4) {
				return new int[0];
			}
			int[] arr = new int[4];
			for (int i=0; i<arr.length; i++) {
				arr[i] = Integer.valueOf(split[i]);
			}
			return arr;
		}
	}

	public static boolean getAskQuit() {
		return Boolean.TRUE.equals(getBooleanParameter(SETTING_ASK_QUIT));
	}
	public static void setAskQuit(boolean askQuit) {
		setBooleanParameter(SETTING_ASK_QUIT, askQuit);
	}

	public static int getColourChoice() {
		return getIntegerParameter(SETTING_COLOUR, 1);
	}
	public static void setColourChoice(int choice) {
		setIntegerParameter(SETTING_COLOUR, choice);
	}

	public static String getDefaultImageDir() {
		return getStringParameter(SETTING_DEFAULT_IMAGE_DIR, ".");
	}
	public static void setDefaultImageDir(String dir) {
		setStringParameter(SETTING_DEFAULT_IMAGE_DIR, dir);
	}

	public static String getDefaultImageDir2() {
		return getStringParameter(SETTING_DEFAULT_IMAGE_DIR2, ".");
	}
	public static void setDefaultImageDir2(String dir2) {
		setStringParameter(SETTING_DEFAULT_IMAGE_DIR2, dir2);
	}

	public static int getFontSize() {
		return getIntegerParameter(SETTING_FONT_SIZE, 10);
	}
	public static void setFontSize(int fontSize) {
		setIntegerParameter(SETTING_FONT_SIZE, fontSize);
	}

	public static int getLocalFontSize() {
		return getIntegerParameter(SETTING_LOCAL_FONT_SIZE, 10);
	}
	public static void setLocalFontSize(int fontSize) {
		setIntegerParameter(SETTING_LOCAL_FONT_SIZE, fontSize);
	}

	public static int getLookAndFeel() {
		return getIntegerParameter(SETTING_LOOK_AND_FEEL, 1);
	}
	public static void setLookAndFeel(int laf) {
		setIntegerParameter(SETTING_LOOK_AND_FEEL, laf);
	}

	public static String getJdbcDriver() {
		return getStringParameter(SETTING_JDBC_DRIVER, "com.mysql.jdbc.Driver");
	}
	public static void setJdbcDriver(String driver) {
		setStringParameter(SETTING_JDBC_DRIVER, driver);
	}

	public static String getJdbcUrl() {
		return getStringParameter(SETTING_JDBC_URL, "jdbc:mysql://localhost:3306/droid64");
	}
	public static void setJdbcUrl(String url) {
		setStringParameter(SETTING_JDBC_URL, url);
	}

	public static String getJdbcUser() {
		return getStringParameter(SETTING_JDBC_USER, DROID64);
	}
	public static void setJdbcUser(String usr) {
		setStringParameter(SETTING_JDBC_USER, usr);
	}

	public static String getJdbcPassword() {
		return getStringParameter(SETTING_JDBC_PASS, "uridium");
	}
	public static void setJdbcPassword(String pass) {
		setStringParameter(SETTING_JDBC_PASS, pass);
	}

	public static int getMaxRows() {
		return getIntegerParameter(SETTING_MAX_ROWS, 25);
	}
	public static void setMaxRows(long rows) {
		int r = (int) (rows > Integer.MAX_VALUE ? Integer.MAX_VALUE : rows);
		setIntegerParameter(SETTING_MAX_ROWS, r);
	}

	public static int getJdbcLimitType() {
		return getIntegerParameter(SETTING_JDBC_LIMIT_TYPE, 0);
	}
	public static void setJdbcLimitType(int limitType) {
		setIntegerParameter(SETTING_JDBC_LIMIT_TYPE, limitType);
	}

	public static int getRowHeight() {
		return getIntegerParameter(SETTING_ROW_HEIGHT, 10);
	}
	public static void setRowHeight(int hgt) {
		setIntegerParameter(SETTING_ROW_HEIGHT, hgt);
	}

	public static int getLocalRowHeight() {
		return getIntegerParameter(SETTING_LOCAL_ROW_HEIGHT, 10);
	}
	public static void setLocalRowHeight(int hgt) {
		setIntegerParameter(SETTING_LOCAL_ROW_HEIGHT, hgt);
	}

	public static boolean getUseDb() {
		return Boolean.TRUE.equals(getBooleanParameter(SETTING_USE_DB));
	}
	public static void setUseDb(boolean useDb) {
		setBooleanParameter(SETTING_USE_DB, useDb);
	}

	public static Map<Integer,List<String>> getFileExtensionMap() {
		Map<Integer,List<String>> map = new HashMap<>();
		map.put(DiskImage.D64_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_D64), getStringListParam(SETTING_FILE_EXT_D64_GZ)));
		map.put(DiskImage.D67_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_D67), getStringListParam(SETTING_FILE_EXT_D67_GZ)));
		map.put(DiskImage.D71_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_D71), getStringListParam(SETTING_FILE_EXT_D71_GZ)));
		map.put(DiskImage.D80_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_D80), getStringListParam(SETTING_FILE_EXT_D80_GZ)));
		map.put(DiskImage.D81_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_D81), getStringListParam(SETTING_FILE_EXT_D81_GZ)));
		map.put(DiskImage.D82_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_D82), getStringListParam(SETTING_FILE_EXT_D82_GZ)));
		map.put(DiskImage.LNX_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_LNX), getStringListParam(SETTING_FILE_EXT_LNX_GZ)));
		map.put(DiskImage.T64_IMAGE_TYPE, joinLists(getStringListParam(SETTING_FILE_EXT_T64), getStringListParam(SETTING_FILE_EXT_T64_GZ)));
		return map;
	}

	private static <T> List<T> joinLists(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<>();
		list.addAll(list1);
		list.addAll(list2);
		return list;
	}

	/**
	 * Check if name ends with a matching file extension. <br>
	 * If so, return name, else return name with the first matching name+extension.
	 * @param imageType image type
	 * @param compressed compressed
	 * @param name name
	 * @return string
	 */
	public static String checkFileNameExtension(int imageType, boolean compressed, String name) {
		if (name == null || name.isEmpty()) {
			return name;
		}
		List<String> extensions = getFileExtensionList(imageType, compressed);
		if (extensions.isEmpty()) {
			return name;
		}
		for (String ext : extensions) {
			if (name.toLowerCase().endsWith(ext.toLowerCase())) {
				return name;
			}
		}
		return name + extensions.get(0);
	}

	public static void setFileExtensions(int imgType, String value, boolean compressed) {
		switch (imgType) {
		case DiskImage.D64_IMAGE_TYPE:
		case DiskImage.D64_CPM_C64_IMAGE_TYPE:
		case DiskImage.D64_CPM_C128_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_D64_GZ : SETTING_FILE_EXT_D64, value);
			break;
		case DiskImage.D67_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_D67_GZ : SETTING_FILE_EXT_D67, value);
			break;
		case DiskImage.D71_IMAGE_TYPE:
		case DiskImage.D71_CPM_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_D71_GZ : SETTING_FILE_EXT_D71, value);
			break;
		case DiskImage.D80_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_D80_GZ : SETTING_FILE_EXT_D80, value);
			break;
		case DiskImage.D81_IMAGE_TYPE:
		case DiskImage.D81_CPM_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_D81_GZ : SETTING_FILE_EXT_D81, value);
			break;
		case DiskImage.D82_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_D82_GZ : SETTING_FILE_EXT_D82, value);
			break;
		case DiskImage.LNX_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_LNX_GZ : SETTING_FILE_EXT_LNX, value);
			break;
		case DiskImage.T64_IMAGE_TYPE:
			setStringListParam(compressed ? SETTING_FILE_EXT_T64_GZ : SETTING_FILE_EXT_T64, value);
			break;
		default:
			break;
		}
	}

	public static String getFileExtensions(int imgType, boolean compressed) {
		List<String> typeList =  getFileExtensionList(imgType, compressed);
		if (typeList.isEmpty()) {
			return null;
		} else {
			boolean b = false;
			StringBuilder buf = new StringBuilder();
			for (String ext : typeList) {
				if (b) {
					buf.append(DELIM);
				} else {
					b = true;
				}
				buf.append(ext);
			}
			return buf.toString();
		}
	}

	private static List<String> getFileExtensionList(int imgType, boolean compressed) {
		switch (imgType) {
		case DiskImage.D64_IMAGE_TYPE:
		case DiskImage.D64_CPM_C64_IMAGE_TYPE:
		case DiskImage.D64_CPM_C128_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_D64_GZ : SETTING_FILE_EXT_D64);
		case DiskImage.D67_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_D67_GZ : SETTING_FILE_EXT_D67);
		case DiskImage.D71_IMAGE_TYPE:
		case DiskImage.D71_CPM_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_D71_GZ : SETTING_FILE_EXT_D71);
		case DiskImage.D80_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_D80_GZ : SETTING_FILE_EXT_D80);
		case DiskImage.D81_IMAGE_TYPE:
		case DiskImage.D81_CPM_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_D81_GZ : SETTING_FILE_EXT_D81);
		case DiskImage.D82_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_D82_GZ : SETTING_FILE_EXT_D82);
		case DiskImage.T64_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_T64_GZ : SETTING_FILE_EXT_T64);
		case DiskImage.LNX_IMAGE_TYPE:
			return getStringListParam(compressed ? SETTING_FILE_EXT_LNX_GZ : SETTING_FILE_EXT_LNX);
		default:
			return new ArrayList<>();
		}
	}

	//////////////////////////////////

	public static String getAllAsString() {
		StringBuilder buf = new StringBuilder();
		new TreeSet<String>(settingTypeMap.keySet()).forEach(key -> buf.append(key).append(EQ).append(settingTypeMap.get(key)).append(LF));
		return buf.toString();
	}

	public static void loadSettingsFromFile() {
		if (USER_HOME != null) {
			settingsFileName = USER_HOME + File.separator + DEFAULT_SETTING_FILE_NAME;
		} else {
			settingsFileName = DEFAULT_SETTING_FILE_NAME;
		}
		loadSettingsFromFile(settingsFileName);
	}

	/**
	 * Load settings from file
	 * @param fileName the configuration file
	 */
	protected static void loadSettingsFromFile(String fileName) {
		settingsFileName = getSettingFileName(fileName);
		if ((new File(settingsFileName)).exists()) {
			System.out.println("loadSettings: exists settingsFileName=" + settingsFileName);	//NOSONAR
			try (FileReader fr = new FileReader(settingsFileName); BufferedReader br = new BufferedReader(fr)) {
				String line;
				while ((line = br.readLine()) != null) {
					if ( !line.trim().startsWith("#") && line.contains(EQ) ) {
						String lineKey = line.substring( 0, line.indexOf('=') ) .trim();
						String lineValue = line.substring(line.indexOf('=')+1).trim();
						parseParameter(settingTypeMap.get(lineKey), lineKey, lineValue);
					}
				}
				parseExternalPrograms();
			} catch (IOException e) {	//NOSONAR
				System.err.println("Load settings failed: "+e.getMessage());	// NOSONAR
			}
		} else {
			parseExternalPrograms();
			System.out.println("loadSettings: Using defaults. No existing file. " + new File(settingsFileName).getAbsolutePath());	// NOSONAR
		}
	}

	private static void parseParameter(Parameter param, String lineKey, String lineValue) {
		if (param != null) {
			param.parseValue(lineValue);
		} else {
			if (!parseIndexedParam(lineKey, lineValue) && !migrateConfig(lineKey, lineValue)) {
				System.err.println("Unknown parameter '" + lineKey + "'");	//NOSONAR
			}
		}
	}

	private static  boolean migrateConfig(String key, String value) {
		switch (key) {
		case "plugin0_command": return parseIndexedParam(SETTING_PLUGIN_COMMAND+".0", value);
		case "plugin1_command": return parseIndexedParam(SETTING_PLUGIN_COMMAND+".1", value);
		case "plugin0_description": return parseIndexedParam(SETTING_PLUGIN_DESCRIPTION+".0", value);
		case "plugin1_description": return parseIndexedParam(SETTING_PLUGIN_DESCRIPTION+".1", value);
		case "plugin0_label": return parseIndexedParam(SETTING_PLUGIN_LABEL+".0", value);
		case "plugin1_label": return parseIndexedParam(SETTING_PLUGIN_LABEL+".1", value);
		default:
			return false;
		}
	}

	private static boolean parseIndexedParam(String key, String value) {
		String[] split = key.split("[.]");
		if (split.length == 2) {
			try {
				int index = Integer.parseInt(split[1]);
				Parameter param = null;
				switch (split[0]) {
				case SETTING_PLUGIN_COMMAND:
					param = settingTypeMap.get(SETTING_PLUGIN_COMMAND);
					break;
				case SETTING_PLUGIN_ARGUMENTS:
					param = settingTypeMap.get(SETTING_PLUGIN_ARGUMENTS);
					break;
				case SETTING_PLUGIN_DESCRIPTION:
					param = settingTypeMap.get(SETTING_PLUGIN_DESCRIPTION);
					break;
				case SETTING_PLUGIN_LABEL:
					param = settingTypeMap.get(SETTING_PLUGIN_LABEL);
					break;
				case SETTING_PLUGIN_FORK:
					param = settingTypeMap.get(SETTING_PLUGIN_FORK);
					break;
				default:
					// Unknown
					break;
				}
				if (parseIndexedParamValue(index, param, value)) {
					return true;
				}
			} catch (NumberFormatException e) { /* Don't care */ }
		}
		return false;
	}

	private static boolean parseIndexedParamValue(int index, Parameter param, String value) {
		if (index >= 0 && param != null && param.getType() == Parameter.INDEXED_STRING_PARAM) {
			List<String> list = param.getIndexedStringValue();
			for (int i=list.size(); i<=index; i++) {
				list.add(null);
			}
			list.set(index, value);
			return true;
		}
		return false;
	}

	/** Save settings to file */
	public static void saveSettingsToFile() {
		saveSettingsToFile(settingsFileName);
	}

	/**
	 * Save settings to file.
	 * @param fileName the configuration file.
	 */
	protected static void saveSettingsToFile(String fileName) {
		try (FileWriter output = new FileWriter(fileName)) {
			output.write("# Configuration file for " + DroiD64.PROGNAME + " v" + DroiD64.VERSION + LF);
			output.write("# Saved " + new Date() + LF);
			output.write("#" + LF);
			List<String> settingKeyList = new ArrayList<>(settingTypeMap.keySet());
			Collections.sort(settingKeyList);
			for (String key : settingKeyList) {
				Parameter param = settingTypeMap.get(key);
				output.write(param.getParamAsString());
			}
			output.write("# End of file\n");
		} catch (IOException | IllegalArgumentException e) {	//NOSONAR
			System.err.println("Save settings failed: " + e.getMessage()); //NOSONAR
		}
	}

	public static String getStringParameter(String name) {
		return getStringParameter(name, null);
	}

	public static String getStringParameter(String name, String defaultValue) {
		Parameter param = settingTypeMap.get(name);
		return param != null ? param.getStringValue() : defaultValue;
	}

	public static void setStringParameter(String name, String value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null) {
			param.setStringValue(value);
		}
	}

	public static Integer getIntegerParameter(String name) {
		return getIntegerParameter(name, null);
	}

	public static Integer getIntegerParameter(String name, Integer defaultValue) {
		Parameter param = settingTypeMap.get(name);
		return param != null ? param.getIntegerValue() : defaultValue;
	}

	public static void setIntegerParameter(String name, int value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null) {
			param.setIntegerValue(value);
		}
	}

	public static Boolean getBooleanParameter(String name) {
		Parameter param = settingTypeMap.get(name);
		return param != null ? param.getBooleanValue() : null;
	}

	public static void setBooleanParameter(String name, boolean value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null) {
			param.setBooleanValue(value);
		}
	}

	private static String getSettingFileName(String tryFileName) {
		String fileName = tryFileName;
		if (fileName == null) {
			String userHome = null;
			try {
				userHome = System.getProperty("user.home");
			} catch (SecurityException e) {	/** ignore */
			} finally {
				if (userHome != null) {
					fileName = userHome + File.separator + DEFAULT_SETTING_FILE_NAME;
				} else {
					fileName = DEFAULT_SETTING_FILE_NAME;
				}
			}
		}
		return fileName;
	}

	public static Font getFontParameter(String name) {
		Parameter param = settingTypeMap.get(name);
		return param != null ? param.getFontValue() : null;
	}

	public static void setFontParameter(String name, Font font) {
		Parameter param = settingTypeMap.get(name);
		if (param != null) {
			param.setFontValue(font);
		}
	}

	public static void setColorParam(String name, Color value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.getType() == Parameter.COLOR_PARAM) {
			param.setColorValue(value);
		}
	}

	public static Color getColorParam(String name) {
		Parameter param = settingTypeMap.get(name);
		return param != null ? param.getColorValue() : null;
	}

	public static List<String> getStringListParam(String name) {
		Parameter param = settingTypeMap.get(name);
		return param != null ? param.getStringListValue() : new ArrayList<>();
	}

	public static void setStringListParam(String name, String value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null) {
			param.parseValue(value);
		}
	}

	public static Font getCommodoreFont() throws CbmException {
		if (commodoreFont == null) {
			try {
				commodoreFont = getCbmFont();
				if (commodoreFont == null) {
					commodoreFont = Font.createFont(Font.TRUETYPE_FONT, Settings.class.getResourceAsStream("resources/" + Settings.DIR_FONT_NAME));
				}
			} catch (FontFormatException | IOException e) {
				throw new CbmException("Failed to create font.", e);
			}
		}
		if (commodoreScaledFont == null || commodoreFontSize != getFontSize()) {
			commodoreFontSize = getFontSize();
			commodoreScaledFont = commodoreFont.deriveFont((float) commodoreFontSize);
		}
		return commodoreScaledFont;
	}

	public static Font getSystemFont() {
		if (getSysFont() == null) {
			return new JTextField().getFont();
		} else {
			return getSysFont();
		}
	}

	/**
	 * Get message property
	 * @param key name of property
	 * @return the message, or the key of the property could not be found.
	 */
	public static String getMessage(String key) {
		try {
			return RESOURCE_BUNDLE.getString(key);
		} catch (MissingResourceException e) {
			return key;
		}
	}

}
