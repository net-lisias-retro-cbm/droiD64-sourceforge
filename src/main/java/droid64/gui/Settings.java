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

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;

public class Settings {

	private static final String DROID64 = "droid64";
	private static final ResourceBundle RESOURCE_BUNDLE = ResourceBundle.getBundle(DROID64);
	public static final String DIR_FONT_NAME = "droiD64_cbm.ttf";
	private static final JPanel DEFAULT_PANEL = new JPanel();
	public static final int MAX_PLUGINS = 4;

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

	// Types of parameters
	private static final int STRING_PARAM 			= 1;
	private static final int INTEGER_PARAM	 		= 2;
	private static final int BOOLEAN_PARAM 			= 3;
	private static final int COLOR_PARAM			= 4;
	private static final int FONT_PARAM				= 5;
	private static final int STRING_LIST_PARAM		= 6;
	private static final int INDEXED_STRING_PARAM	= 7;	// param_name.1=value

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
		map.put(SETTING_ASK_QUIT,			new Parameter(SETTING_ASK_QUIT,				BOOLEAN_PARAM,	Boolean.TRUE));
		map.put(SETTING_BORDER_ACTIVE,		new Parameter(SETTING_BORDER_ACTIVE,		COLOR_PARAM,	ACTIVE_BORDER_COLOR));
		map.put(SETTING_BORDER_INACTIVE,	new Parameter(SETTING_BORDER_INACTIVE,		COLOR_PARAM,	INACTIVE_BORDER_COLOR));
		map.put(SETTING_COLOUR,				new Parameter(SETTING_COLOUR,				INTEGER_PARAM,	Integer.valueOf(1)));
		map.put(SETTING_DEFAULT_IMAGE_DIR,	new Parameter(SETTING_DEFAULT_IMAGE_DIR,	STRING_PARAM,	USER_HOME));
		map.put(SETTING_DEFAULT_IMAGE_DIR2,	new Parameter(SETTING_DEFAULT_IMAGE_DIR2,	STRING_PARAM,	USER_HOME));
		map.put(SETTING_DIR_BG,				new Parameter(SETTING_DIR_BG,				COLOR_PARAM,	DIR_BG_COLOR_C64));
		map.put(SETTING_DIR_FG,				new Parameter(SETTING_DIR_FG,				COLOR_PARAM,	DIR_FG_COLOR_C64));
		map.put(SETTING_DIR_CPM_BG,			new Parameter(SETTING_DIR_CPM_BG,			COLOR_PARAM,	DIR_BG_COLOR_CPM));
		map.put(SETTING_DIR_CPM_FG,			new Parameter(SETTING_DIR_CPM_FG,			COLOR_PARAM,	DIR_FG_COLOR_CPM));
		map.put(SETTING_DIR_LOCAL_BG,		new Parameter(SETTING_DIR_LOCAL_BG,			COLOR_PARAM,	DIR_BG_COLOR_LOCAL));
		map.put(SETTING_DIR_LOCAL_FG,		new Parameter(SETTING_DIR_LOCAL_FG,			COLOR_PARAM,	DIR_FG_COLOR_LOCAL));
		map.put(SETTING_FONT_SIZE,			new Parameter(SETTING_FONT_SIZE,			INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_LOCAL_FONT_SIZE,	new Parameter(SETTING_LOCAL_FONT_SIZE,		INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_WINDOW,				new Parameter(SETTING_WINDOW,				STRING_PARAM,	""));
		map.put(SETTING_JDBC_DRIVER,		new Parameter(SETTING_JDBC_DRIVER,			STRING_PARAM,	"com.mysql.jdbc.Driver"));
		map.put(SETTING_JDBC_URL,			new Parameter(SETTING_JDBC_URL,				STRING_PARAM,	"jdbc:mysql://localhost:3306/droid64"));
		map.put(SETTING_JDBC_USER,			new Parameter(SETTING_JDBC_USER,			STRING_PARAM,	DROID64));
		map.put(SETTING_JDBC_PASS,			new Parameter(SETTING_JDBC_PASS,			STRING_PARAM,	"uridium"));
		map.put(SETTING_JDBC_LIMIT_TYPE,	new Parameter(SETTING_JDBC_LIMIT_TYPE,		INTEGER_PARAM,	Integer.valueOf(0)));
		map.put(SETTING_MAX_ROWS,			new Parameter(SETTING_MAX_ROWS,				INTEGER_PARAM,	Integer.valueOf(25)));
		map.put(SETTING_ROW_HEIGHT,			new Parameter(SETTING_ROW_HEIGHT,			INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_LOCAL_ROW_HEIGHT,	new Parameter(SETTING_LOCAL_ROW_HEIGHT,		INTEGER_PARAM,	Integer.valueOf(10)));
		map.put(SETTING_USE_DB,				new Parameter(SETTING_USE_DB,				BOOLEAN_PARAM,	Boolean.FALSE));
		map.put(SETTING_LOOK_AND_FEEL,		new Parameter(SETTING_LOOK_AND_FEEL,		INTEGER_PARAM,	Integer.valueOf(1)));
		map.put(SETTING_PLUGIN_COMMAND,		new Parameter(SETTING_PLUGIN_COMMAND,		INDEXED_STRING_PARAM,
				Arrays.asList( "d64copy", "x64", "128", "cbmctrl" )));
		map.put(SETTING_PLUGIN_ARGUMENTS,	new Parameter(SETTING_PLUGIN_ARGUMENTS,		INDEXED_STRING_PARAM,
				Arrays.asList( "{Image} 8", "-drive8type {DriveType} {ImageFiles}", "-drive8type {DriveType} {ImageFiles}", "dir 8" )));
		map.put(SETTING_PLUGIN_DESCRIPTION,	new Parameter(SETTING_PLUGIN_DESCRIPTION,	INDEXED_STRING_PARAM,
				Arrays.asList(
						"Transfer this disk image to a real floppy.", "Invoke VICE 64 emulator with this disk image",
						"Invoke VICE 128 emulator with this disk image", "List files using OpenCBM" )));
		map.put(SETTING_PLUGIN_LABEL,		new Parameter(SETTING_PLUGIN_LABEL,			INDEXED_STRING_PARAM,
				Arrays.asList( "d64copy", "VICE 64", "VICE 128", "CBM dir" )));
		map.put(SETTING_PLUGIN_FORK,		new Parameter(SETTING_PLUGIN_FORK,			INDEXED_STRING_PARAM,
				Arrays.asList( "true", "true", "true", "true" )));
		map.put(SETTING_FILE_EXT_D64,		new Parameter(SETTING_FILE_EXT_D64,			STRING_LIST_PARAM,	Arrays.asList(".d64".split(";")) ));
		map.put(SETTING_FILE_EXT_D67,		new Parameter(SETTING_FILE_EXT_D67,			STRING_LIST_PARAM,	Arrays.asList(".d67".split(";")) ));
		map.put(SETTING_FILE_EXT_D71,		new Parameter(SETTING_FILE_EXT_D71,			STRING_LIST_PARAM,	Arrays.asList(".d71".split(";")) ));
		map.put(SETTING_FILE_EXT_D81,		new Parameter(SETTING_FILE_EXT_D81,			STRING_LIST_PARAM,	Arrays.asList(".d81".split(";")) ));
		map.put(SETTING_FILE_EXT_T64,		new Parameter(SETTING_FILE_EXT_T64,			STRING_LIST_PARAM,	Arrays.asList(".t64".split(";")) ));
		map.put(SETTING_FILE_EXT_D80,		new Parameter(SETTING_FILE_EXT_D80,			STRING_LIST_PARAM,	Arrays.asList(".d80".split(";")) ));
		map.put(SETTING_FILE_EXT_D82,		new Parameter(SETTING_FILE_EXT_D82,			STRING_LIST_PARAM,	Arrays.asList(".d82".split(";")) ));
		map.put(SETTING_FILE_EXT_LNX,		new Parameter(SETTING_FILE_EXT_LNX,			STRING_LIST_PARAM,	Arrays.asList(".lnx".split(";")) ));
		map.put(SETTING_FILE_EXT_D64_GZ,	new Parameter(SETTING_FILE_EXT_D64_GZ,		STRING_LIST_PARAM,	Arrays.asList("d64.gz".split(";")) ));
		map.put(SETTING_FILE_EXT_D67_GZ,	new Parameter(SETTING_FILE_EXT_D67_GZ,		STRING_LIST_PARAM,	Arrays.asList("d67.gz".split(";")) ));
		map.put(SETTING_FILE_EXT_D71_GZ,	new Parameter(SETTING_FILE_EXT_D71_GZ,		STRING_LIST_PARAM,	Arrays.asList("d71.gz".split(";")) ));
		map.put(SETTING_FILE_EXT_D81_GZ,	new Parameter(SETTING_FILE_EXT_D81_GZ,		STRING_LIST_PARAM,	Arrays.asList("d81.gz".split(";")) ));
		map.put(SETTING_FILE_EXT_T64_GZ,	new Parameter(SETTING_FILE_EXT_T64_GZ,		STRING_LIST_PARAM,	Arrays.asList("t64.gz".split(";")) ));
		map.put(SETTING_FILE_EXT_D80_GZ,	new Parameter(SETTING_FILE_EXT_D80_GZ,		STRING_LIST_PARAM,	Arrays.asList("d80.gz".split(";")) ));
		map.put(SETTING_FILE_EXT_D82_GZ,	new Parameter(SETTING_FILE_EXT_D82_GZ,		STRING_LIST_PARAM,	Arrays.asList("d82.gz".split(";")) ));
		map.put(SETTING_FILE_EXT_LNX_GZ,	new Parameter(SETTING_FILE_EXT_LNX_GZ,		STRING_LIST_PARAM,	Arrays.asList("lnx.gz".split(";")) ));
		return map;
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
			String cmd = i <cmdList.size() ? cmdList.get(i) : "";
			String args = i <argList.size() ? argList.get(i) : "";
			String descr = i <descrList.size() ? descrList.get(i) : "";
			String label = i <labelList.size() ? labelList.get(i) : "";
			boolean forkThread = i <forkList.size() ? Boolean.valueOf(forkList.get(i)) : true;
			if ("".equals(cmd) && "".equals(descr) && "".equals(label)) {
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
			List <String> list = param.getListValue();
			return list == null ? new ArrayList<String>() : list;
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
		List<String> list = param.getListValue();
		for (int i=list.size(); i <= num; i++) {
			list.add("");
		}
		list.set(num, value);
	}

	//////////////////////////

	public static void setWindow(int[] sizeLocation) {
		if (sizeLocation != null && sizeLocation.length >= 4) {
			String value = String.format("%d:%d,%d:%d", sizeLocation[0], sizeLocation[1], sizeLocation[2], sizeLocation[3]);
			setStringParameter(SETTING_WINDOW, value);
		} else {
			setStringParameter(SETTING_WINDOW, "");
		}
	}

	public static int[] getWindow() {
		String str = getStringParameter(SETTING_WINDOW);
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
		Integer size = getIntegerParameter(SETTING_COLOUR);
		return size != null ? size.intValue() : 1;
	}
	public static void setColourChoice(int choice) {
		setIntegerParameter(SETTING_COLOUR, choice);
	}

	public static String getDefaultImageDir() {
		String dir = getStringParameter(SETTING_DEFAULT_IMAGE_DIR);
		return dir != null ? dir : ".";
	}
	public static void setDefaultImageDir(String dir) {
		setStringParameter(SETTING_DEFAULT_IMAGE_DIR, dir);
	}

	public static String getDefaultImageDir2() {
		String dir2 = getStringParameter(SETTING_DEFAULT_IMAGE_DIR2);
		return dir2 != null ? dir2 : ".";
	}
	public static void setDefaultImageDir2(String dir2) {
		setStringParameter(SETTING_DEFAULT_IMAGE_DIR2, dir2);
	}

	public static int getFontSize() {
		Integer size = getIntegerParameter(SETTING_FONT_SIZE);
		return size != null ? size.intValue() : 10;
	}
	public static void setFontSize(int fontSize) {
		setIntegerParameter(SETTING_FONT_SIZE, fontSize);
	}

	public static int getLocalFontSize() {
		Integer size = getIntegerParameter(SETTING_LOCAL_FONT_SIZE);
		return size != null ? size.intValue() : 10;
	}
	public static void setLocalFontSize(int fontSize) {
		setIntegerParameter(SETTING_LOCAL_FONT_SIZE, fontSize);
	}

	public static int getLookAndFeel() {
		Integer laf = getIntegerParameter(SETTING_LOOK_AND_FEEL);
		return laf != null ? laf.intValue() : 1;
	}
	public static void setLookAndFeel(int laf) {
		setIntegerParameter(SETTING_LOOK_AND_FEEL, laf);
	}

	public static String getJdbcDriver() {
		String driver = getStringParameter(SETTING_JDBC_DRIVER);
		return driver != null ? driver : "com.mysql.jdbc.Driver";
	}
	public static void setJdbcDriver(String driver) {
		setStringParameter(SETTING_JDBC_DRIVER, driver);
	}

	public static String getJdbcUrl() {
		String url = getStringParameter(SETTING_JDBC_URL);
		return url != null ? url : "jdbc:mysql://localhost:3306/droid64";
	}
	public static void setJdbcUrl(String url) {
		setStringParameter(SETTING_JDBC_URL, url);
	}

	public static String getJdbcUser() {
		String url = getStringParameter(SETTING_JDBC_USER);
		return url != null ? url : DROID64;
	}
	public static void setJdbcUser(String url) {
		setStringParameter(SETTING_JDBC_USER, url);
	}

	public static String getJdbcPassword() {
		String pass = getStringParameter(SETTING_JDBC_PASS);
		return pass != null ? pass : "uridium";
	}
	public static void setJdbcPassword(String pass) {
		setStringParameter(SETTING_JDBC_PASS, pass);
	}

	public static int getMaxRows() {
		Integer rows = getIntegerParameter(SETTING_MAX_ROWS);
		return rows != null ? rows.intValue() : 25;
	}
	public static void setMaxRows(long rows) {
		int r = (int) (rows > Integer.MAX_VALUE ? Integer.MAX_VALUE : rows);
		setIntegerParameter(SETTING_MAX_ROWS, r);
	}

	public static int getJdbcLimitType() {
		Integer limitType = getIntegerParameter(SETTING_JDBC_LIMIT_TYPE);
		return limitType != null ? limitType : 0;
	}
	public static void setJdbcLimitType(int limitType) {
		setIntegerParameter(SETTING_JDBC_LIMIT_TYPE, limitType);
	}

	public static int getRowHeight() {
		Integer rows = getIntegerParameter(SETTING_ROW_HEIGHT);
		return rows != null ? rows.intValue() : 10;
	}
	public static void setRowHeight(int hgt) {
		setIntegerParameter(SETTING_ROW_HEIGHT, hgt);
	}

	public static int getLocalRowHeight() {
		Integer rows = getIntegerParameter(SETTING_LOCAL_ROW_HEIGHT);
		return rows != null ? rows.intValue() : 10;
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
					buf.append(';');
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
		for(String key: new TreeSet<String>(settingTypeMap.keySet())) {
			buf.append(key).append("=").append(settingTypeMap.get(key)).append('\n');

		}
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
			try (FileReader fr = new FileReader(settingsFileName)) {
				String line;
				BufferedReader br = new BufferedReader(fr);
				while ((line = br.readLine()) != null) {
					if ( !line.trim().startsWith("#") && line.contains("=") ) {
						String lineKey = line.substring( 0, line.indexOf('=') ) .trim();
						String lineValue = line.substring(line.indexOf('=')+1).trim();
						parseParameter(settingTypeMap.get(lineKey), lineKey, lineValue);
					}
				}
				br.close();
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
			try {
				switch (param.type) {
				case STRING_PARAM:
					param.value = lineValue;
					break;
				case INTEGER_PARAM:
					param.value = Integer.valueOf(lineValue);
					break;
				case BOOLEAN_PARAM:
					param.value = "yes".equals(lineValue) ? Boolean.TRUE : Boolean.valueOf(lineValue);
					break;
				case COLOR_PARAM:
					splitStringIntoColorParam(lineValue, param);
					break;
				case FONT_PARAM:
					Font fnt = getFontFromString(lineValue);
					if (fnt != null) {
						param.value = fnt;
					}
					break;
				case STRING_LIST_PARAM:
					param.value = Arrays.asList(lineValue.split("[ \t]*[;][ \t]*"));
					break;
				default:
				}
			} catch (IllegalArgumentException e) {	//NOSONAR
				System.err.println("Failed to parse setting: "+e.getMessage());	//NOSONAR
			}
		} else {
			if (!parseIndexedParam(lineKey, lineValue) && !migrateConfig(lineKey, lineValue)) {
				System.err.println("Unknown parameter '" + lineKey + "'");	//NOSONAR
			}
		}
	}

	private static void splitStringIntoColorParam(String str, Parameter param) {
		String[] split = str.split("[ \t]*[,;.][ \t]*");
		if (split.length >= 3) {
			int r = Integer.parseInt(split[0]) & 0xff;
			int g = Integer.parseInt(split[1]) & 0xff;
			int b = Integer.parseInt(split[2]) & 0xff;
			param.value = new Color(r, g, b);
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
		if (index >= 0 && param != null && param.type == INDEXED_STRING_PARAM) {
			@SuppressWarnings("unchecked")
			List<String> list = (List<String>) param.value;
			if (list == null || list.isEmpty()) {
				list = new ArrayList<>(index+1);
				list.set(index, value);
			} else {
				for (int i=list.size(); i<=index; i++) {
					list.add(null);
				}
				list.set(index, value);
			}
			param.value = list;
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
			output.write("# Configuration file for "+DroiD64.PROGNAME+" v"+DroiD64.VERSION+"\n");
			output.write("# Saved " + new Date() + "\n");
			output.write("#\n");
			List<String> settingKeyList = new ArrayList<>(settingTypeMap.keySet());
			Collections.sort(settingKeyList);
			for (String key : settingKeyList) {
				Parameter param = settingTypeMap.get(key);
				String value;
				switch (param.type) {
				case COLOR_PARAM :
					Color c = (Color) param.value;
					value = c.getRed()+","+c.getGreen()+","+c.getBlue();
					output.write(key + "=" + value + "\n");
					break;
				case FONT_PARAM:
					Font fnt = (Font) param.value;
					value = fnt.getName()+";"+fnt.getStyle()+";"+fnt.getSize();
					output.write(key + "=" + value + "\n");
					break;
				case STRING_LIST_PARAM:
					value = getStringListParamAsString(param.name);
					output.write(key + "=" + value + "\n");
					break;
				case INDEXED_STRING_PARAM:
					@SuppressWarnings("unchecked")
					List<String> list = (List<String>) param.value;
					for (int i=0; i<list.size(); i++) {
						output.write(key + "."+i+"=" + list.get(i) + "\n");
					}
					break;
				default:
					value = param.value.toString();
					output.write(key + "=" + value + "\n");
					break;
				}
			}
			output.write("# End of file\n");
		} catch (IOException e) {	//NOSONAR
			System.err.println("Save settings failed: " + e.getMessage()); //NOSONAR
		}
	}

	public static String getStringParameter(String name) {
		Parameter param = settingTypeMap.get(name);
		if (param == null || param.type != STRING_PARAM) {
			return null;
		} else {
			return (String) param.value;
		}
	}

	public static void setStringParameter(String name, String value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == STRING_PARAM) {
			param.setValue(value);
		}
	}

	public static Integer getIntegerParameter(String name) {
		Parameter param = settingTypeMap.get(name);
		if (param == null || param.type != INTEGER_PARAM) {
			return null;
		} else {
			return (Integer) param.value;
		}
	}

	public static void setIntegerParameter(String name, int value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == INTEGER_PARAM) {
			param.value = value;
		}
	}

	public static Boolean getBooleanParameter(String name) {
		Parameter param = settingTypeMap.get(name);
		if (param == null || param.type != BOOLEAN_PARAM) {
			return null;	//NOSONAR
		} else {
			return (Boolean) param.value;
		}
	}

	public static void setBooleanParameter(String name, boolean value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == BOOLEAN_PARAM) {
			param.value = value;
		}
	}

	private static String getSettingFileName(String tryFileName) {
		String fileName = tryFileName;
		if (fileName == null) {
			String userHome = null;
			try {
				userHome = System.getProperty("user.home");
			} catch (SecurityException e) {	//NOSONAR
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
		if (param == null || param.type != FONT_PARAM) {
			return null;
		} else {
			return (Font) param.value;
		}
	}

	public static void setFontParameter(String name, Font font) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == FONT_PARAM) {
			param.value = font;
		}
	}

	private static Font getFontFromString(String str) {
		if (str != null) {
			String[] s = str.split("[ \t]*[;][ \t]*");
			if (s.length >= 3) {
				return new Font(s[0], Integer.parseInt(s[1]), Integer.parseInt(s[2]));
			}
		}
		return null;
	}

	public static void setColorParam(String name, Color value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == COLOR_PARAM) {
			param.value = value;
		}
	}

	public static Color getColorParam(String name) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == COLOR_PARAM) {
			return (Color) param.value;
		} else {
			return null;
		}
	}

	@SuppressWarnings("unchecked")
	public static List<String> getStringListParam(String name) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == STRING_LIST_PARAM) {
			return (List<String>) param.value;
		} else {
			return new ArrayList<>();
		}
	}

	@SuppressWarnings("unchecked")
	public static void setStringListParam(String name, String value) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == STRING_LIST_PARAM) {
			param.value = new ArrayList<String>();
			if (value != null) {
				for (String str : Arrays.asList(value.split("\\s*;\\s*"))) {
					((List<String>)param.value).add(str);
				}
			}
		}
	}

	@SuppressWarnings("unchecked")
	public static String getStringListParamAsString(String name) {
		Parameter param = settingTypeMap.get(name);
		if (param != null && param.type == STRING_LIST_PARAM) {
			StringBuilder buf = new StringBuilder();
			for (String str : (List<String>) param.value) {
				if (buf.length()>0) {
					buf.append(";");
				}
				buf.append(str);
			}
			return buf.toString();
		} else {
			return "";
		}
	}

	/** Class used to store one parameter.*/
	static class Parameter {
		String name;
		int type;
		Object value;
		public Parameter(String name, int type, Object value) {
			this.name = name;
			this.type = type;
			this.value = value;
		}
		public String getStringValue() {
			if (type == STRING_PARAM) {
				return (String) value;
			} else {
				return "";
			}
		}
		public Integer getIntegerValue() {
			if (type == INTEGER_PARAM) {
				return (Integer) value;
			} else {
				return 0;
			}
		}
		public Boolean getBooleanValue() {
			if (type == INTEGER_PARAM) {
				return (Boolean) value;
			} else {
				return Boolean.FALSE;
			}
		}
		@SuppressWarnings("unchecked")
		public List<String> getListValue() {
			if (type == INDEXED_STRING_PARAM) {
				return (List<String>) value;
			} else {
				return new ArrayList<>();
			}
		}
		public void setValue(String value) {
			this.value = value;
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
	}

	public static Font getCommodoreFont() throws CbmException {
		if (commodoreFont == null) {
			try {
				commodoreFont = Font.createFont(Font.TRUETYPE_FONT, Settings.class.getResourceAsStream("resources/"+Settings.DIR_FONT_NAME));
			} catch (FontFormatException | IOException e) {
				throw new CbmException("Failed to create font.", e);
			}
		}
		if (commodoreScaledFont == null || commodoreFontSize != getFontSize()) {
			commodoreFontSize = getFontSize();
			commodoreScaledFont = commodoreFont.deriveFont((float)commodoreFontSize);
		}
		return commodoreScaledFont;
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
