package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JColorChooser;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileFilter;

import droid64.d64.DiskImage;
import droid64.d64.Utility;
import droid64.db.DaoFactory;
import droid64.db.DaoFactoryImpl;

/**<pre style='font-family:Sans,Arial,Helvetica'>
 * Created on 30.06.2004
 *
 *   droiD64 - A graphical filemanager for D64 files
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
 *  </pre>
 * @author wolf
 */
public class SettingsPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final String LBL_FOREGROUND = Settings.getMessage(Resources.DROID64_SETTINGS_FOREGROUND);
	private static final String LBL_BACKGROUND = Settings.getMessage(Resources.DROID64_SETTINGS_BACKGROUND);
	private static final String BROWSELABEL = "...";

	private final JComboBox<String> colourBox = new JComboBox<>(COLORS);
	private final JCheckBox exitConfirmCheckBox = new JCheckBox(Settings.getMessage(Resources.DROID64_SETTINGS_CONFIRMEXIT));
	private final JCheckBox hideConsoleCheckBox = new JCheckBox(Settings.getMessage(Resources.DROID64_SETTINGS_HIDECONSOLE));
	private final JSpinner rowHeightSpinner = new JSpinner(new SpinnerNumberModel(Settings.getRowHeight(), 8, 256, 1));
	private final JSpinner localRowHeightSpinner = new JSpinner(new SpinnerNumberModel(Settings.getLocalRowHeight(), 8, 256, 1));
	private final JTextField winSizePosField = new JTextField(getWindowSizePosString());
	private final JTextField fileExtD64 = new JTextField(Settings.getFileExtensions(DiskImage.D64_IMAGE_TYPE, false));
	private final JTextField fileExtD67 = new JTextField(Settings.getFileExtensions(DiskImage.D67_IMAGE_TYPE, false));
	private final JTextField fileExtD71 = new JTextField(Settings.getFileExtensions(DiskImage.D71_IMAGE_TYPE, false));
	private final JTextField fileExtD81 = new JTextField(Settings.getFileExtensions(DiskImage.D81_IMAGE_TYPE, false));
	private final JTextField fileExtT64 = new JTextField(Settings.getFileExtensions(DiskImage.T64_IMAGE_TYPE, false));
	private final JTextField fileExtD80 = new JTextField(Settings.getFileExtensions(DiskImage.D80_IMAGE_TYPE, false));
	private final JTextField fileExtD82 = new JTextField(Settings.getFileExtensions(DiskImage.D82_IMAGE_TYPE, false));
	private final JTextField fileExtD88 = new JTextField(Settings.getFileExtensions(DiskImage.D88_IMAGE_TYPE, false));
	private final JTextField fileExtLNX = new JTextField(Settings.getFileExtensions(DiskImage.LNX_IMAGE_TYPE, false));
	private final JTextField fileExtD64gz = new JTextField(Settings.getFileExtensions(DiskImage.D64_IMAGE_TYPE, true));
	private final JTextField fileExtD67gz = new JTextField(Settings.getFileExtensions(DiskImage.D67_IMAGE_TYPE, true));
	private final JTextField fileExtD71gz = new JTextField(Settings.getFileExtensions(DiskImage.D71_IMAGE_TYPE, true));
	private final JTextField fileExtD81gz = new JTextField(Settings.getFileExtensions(DiskImage.D81_IMAGE_TYPE, true));
	private final JTextField fileExtT64gz = new JTextField(Settings.getFileExtensions(DiskImage.T64_IMAGE_TYPE, true));
	private final JTextField fileExtD80gz = new JTextField(Settings.getFileExtensions(DiskImage.D80_IMAGE_TYPE, true));
	private final JTextField fileExtD82gz = new JTextField(Settings.getFileExtensions(DiskImage.D82_IMAGE_TYPE, true));
	private final JTextField fileExtD88gz = new JTextField(Settings.getFileExtensions(DiskImage.D88_IMAGE_TYPE, true));
	private final JTextField fileExtLNXgz = new JTextField(Settings.getFileExtensions(DiskImage.LNX_IMAGE_TYPE, true));

	private Font cbmFont = Settings.getCbmFont();
	private Font sysFont = Settings.getSysFont();

	private final JButton colorFgButton = new JButton(LBL_FOREGROUND);
	private final JButton colorBgButton = new JButton(LBL_BACKGROUND);
	private final JButton colorCpmFgButton = new JButton(LBL_FOREGROUND);
	private final JButton colorCpmBgButton = new JButton(LBL_BACKGROUND);
	private final JButton colorLocalFgButton = new JButton(LBL_FOREGROUND);
	private final JButton colorLocalBgButton = new JButton(LBL_BACKGROUND);
	// Plugin settings
	private final JTextField[] pluginLabelTextField = new JTextField[Settings.MAX_PLUGINS];
	private final FilePathPanel[] pluginCommandField= new FilePathPanel[Settings.MAX_PLUGINS];
	private final JTextArea[] pluginArgumentTextField= new JTextArea[Settings.MAX_PLUGINS];
	private final JTextArea[] pluginDescriptionTextField = new JTextArea[Settings.MAX_PLUGINS];
	private final JCheckBox[] forkThreadCheckBox = new JCheckBox[Settings.MAX_PLUGINS];
	// Database settings
	private final JCheckBox useJdbcCheckBox = new JCheckBox(Settings.getMessage(Resources.DROID64_SETTINGS_JDBC_USEDB));
	private final JTextField jdbcDriver = new JTextField(Settings.getJdbcDriver());
	private final JTextField jdbcUrl = new JTextField(Settings.getJdbcUrl());
	private final JTextField jdbcUser = new JTextField(Settings.getJdbcUser());
	private final JPasswordField jdbcPassword = new JPasswordField(Settings.getJdbcPassword());
	private final JFormattedTextField maxRows = SearchPanel.getNumericField(Settings.getMaxRows(), 8);
	private final JComboBox<String> lookAndFeelBox = new JComboBox<>(MainPanel.getLookAndFeelNames());
	private final JComboBox<String> limitTypeBox = new JComboBox<>(DaoFactory.getLimitNames());
	private final List<String> jdbcDriverClasses = GuiHelper.getClassNames(java.sql.Driver.class);

	/** Colors */
	private static final String[] COLORS = { "gray", "red", "green", "blue", "light-blue", "dark-grey", "cyan" };

	private final JTextArea status = new JTextArea();

	private static final String ARGUMENT_TOOLTIP = Settings.getMessage(Resources.PLUGIN_ARGUMENTS_TOOLTIP);

	private static String sqlSetupScript = null;
	private final MainPanel mainPanel;
	private final String title;

	/**
	 * Constructor
	 * @param title String
	 * @param mainPanel MainPanel
	 */
	public SettingsPanel (String title, MainPanel mainPanel) {
		this.title = title;
		this.mainPanel = mainPanel;

		setLayout(new BorderLayout());
		JTabbedPane tabPane = new JTabbedPane();
		tabPane.addTab(Settings.getMessage(Resources.DROID64_SETTINGS_TAB_GUI), drawGuiPanel());
		tabPane.addTab(Settings.getMessage(Resources.DROID64_SETTINGS_TAB_FILES), drawFilesPanel());
		tabPane.addTab(Settings.getMessage(Resources.DROID64_SETTINGS_TAB_COLORS), drawColorPanel());
		tabPane.addTab(Settings.getMessage(Resources.DROID64_SETTINGS_TAB_DATABASE), drawDatabasePanel());

		JTabbedPane pluginTabPane = new JTabbedPane();
		JPanel[] pluginPanel = drawPluginPanel();
		for (int i = 0; i < Settings.MAX_PLUGINS; i++) {
			pluginTabPane.addTab(Integer.toString(i+1), pluginPanel[i]);
		}
		tabPane.addTab(Settings.getMessage(Resources.DROID64_SETTINGS_TAB_PLUGIN), pluginTabPane);

		add(new JScrollPane(tabPane), BorderLayout.CENTER);

		JPanel generalPanel = drawGeneralPanel();
		add(generalPanel, BorderLayout.SOUTH);
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	public void showDialog() {
		JOptionPane.showMessageDialog(mainPanel.getParent(), this, title, JOptionPane.PLAIN_MESSAGE);
		copyValuesToSettings(mainPanel);
		Settings.parseExternalPrograms();
		Settings.saveSettingsToFile();
	}

	/**
	 * Setup up general settings panel
	 * @return JPanel
	 */
	private JPanel drawGeneralPanel() {
		JPanel txtPanel = new JPanel();
		txtPanel.setAlignmentX(CENTER_ALIGNMENT);
		txtPanel.add(new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_SAVEMESSAGE)));
		JPanel generalPanel = new JPanel(new BorderLayout());
		generalPanel.add(txtPanel, BorderLayout.NORTH);
		return generalPanel;
	}

	private JPanel drawFilesPanel() {
		JPanel guiPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		FilePathPanel fp1 = new FilePathPanel(Settings.getDefaultImageDir(), JFileChooser.DIRECTORIES_ONLY, Settings::setDefaultImageDir);
		FilePathPanel fp2 = new FilePathPanel(Settings.getDefaultImageDir2(), JFileChooser.DIRECTORIES_ONLY, Settings::setDefaultImageDir2);

		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, guiPanel, new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_LEFTDIR)));
		GuiHelper.addToGridBag(1, 0, 1.0, 0.0, 2, gbc, guiPanel, fp1);

		GuiHelper.addToGridBag(0, 1, 0.0, 0.0, 1, gbc, guiPanel, new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_RIGHTDIR)));
		GuiHelper.addToGridBag(1, 1, 1.0, 0.0, 2, gbc, guiPanel, fp2);

		addFields(2, Resources.DROID64_SETTINGS_EXT_D64, fileExtD64, fileExtD64gz, guiPanel, gbc);
		addFields(3, Resources.DROID64_SETTINGS_EXT_D67, fileExtD67, fileExtD67gz, guiPanel, gbc);
		addFields(4, Resources.DROID64_SETTINGS_EXT_D71, fileExtD71, fileExtD71gz, guiPanel, gbc);
		addFields(5, Resources.DROID64_SETTINGS_EXT_D80, fileExtD80, fileExtD80gz, guiPanel, gbc);
		addFields(6, Resources.DROID64_SETTINGS_EXT_D81, fileExtD81, fileExtD81gz, guiPanel, gbc);
		addFields(7, Resources.DROID64_SETTINGS_EXT_D82, fileExtD82, fileExtD82gz, guiPanel, gbc);
		addFields(8, Resources.DROID64_SETTINGS_EXT_D88, fileExtD88, fileExtD88gz, guiPanel, gbc);
		addFields(9, Resources.DROID64_SETTINGS_EXT_T64, fileExtT64, fileExtT64gz, guiPanel, gbc);
		addFields(10, Resources.DROID64_SETTINGS_EXT_LNX, fileExtLNX, fileExtLNXgz, guiPanel, gbc);

		GuiHelper.addToGridBag(0, 11, 1.0, 0.8, 3, gbc, guiPanel, new JPanel());
		return guiPanel;
	}

	private void addFields(int row, String propertyKey, JComponent field1, JComponent field2, JPanel panel, GridBagConstraints gbc) {
		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, panel, new JLabel(Settings.getMessage(propertyKey)));
		GuiHelper.addToGridBag(1, row, 0.5, 0.0, 1, gbc, panel, field1);
		GuiHelper.addToGridBag(2, row, 0.5, 0.0, 1, gbc, panel, field2);
	}

	private String getWindowSizePosString() {
		int[] sizeLocation = Settings.getWindow();
		if (sizeLocation.length < 4) {
			return Utility.EMPTY;
		}
		return String.format("%d:%d,%d:%d", sizeLocation[0], sizeLocation[1], sizeLocation[2], sizeLocation[3]);
	}

	/**
	 * Setup panel with GUI settings
	 * @param mainPanel
	 * @return JPanel
	 */
	private JPanel drawGuiPanel() {
		JPanel guiPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		exitConfirmCheckBox.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_CONFIRMEXIT_TOOLTIP));
		exitConfirmCheckBox.setSelected(Settings.getAskQuit());

		hideConsoleCheckBox.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_HIDECONSOLE_TOOLTIP));
		hideConsoleCheckBox.setSelected(Settings.getHideConsole());

		lookAndFeelBox.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_LOOKFEEL_TOOLTIP));
		lookAndFeelBox.setEditable(false);
		lookAndFeelBox.setSelectedIndex(0);
		lookAndFeelBox.setSelectedIndex(Settings.getLookAndFeel() < MainPanel.getLookAndFeelNames().length ? Settings.getLookAndFeel() : 0);

		rowHeightSpinner.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_GRIDSPACING_TOOLTIP));

		localRowHeightSpinner.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_GRIDSPACING_TOOLTIP));

		final JSpinner fontSizeSpinner = new JSpinner(new SpinnerNumberModel(Settings.getFontSize(), 8, 144, 1));
		final JSpinner localFontSizeSpinner = new JSpinner(new SpinnerNumberModel(Settings.getLocalFontSize(), 8, 144, 1));
		fontSizeSpinner.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_FONTSIZE_TOOLTIP));
		localFontSizeSpinner.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_FONTSIZE_TOOLTIP));

		fontSizeSpinner.addChangeListener(event ->
			Settings.setFontSize(Integer.parseInt(fontSizeSpinner.getValue().toString())));
		localFontSizeSpinner.addChangeListener(event ->
			Settings.setLocalFontSize(Integer.parseInt(localFontSizeSpinner.getValue().toString())));

		JButton getWinSizeButton = new JButton(Settings.getMessage(Resources.DROID64_SETTINGS_WINDOWSIZE_FETCH));
		getWinSizeButton.addActionListener(ae -> {
			Dimension size = mainPanel.getParent().getSize();
			Point location = mainPanel.getParent().getLocation();
			String str = String.format("%d:%d,%d:%d", (int)size.getWidth(), (int)size.getHeight(), (int)location.getX(), (int)location.getY());
			winSizePosField.setText(str);
		});
		// system font
		JTextField sysFontField = new JTextField(Parameter.getFontAsDisplayString(sysFont));
		JCheckBox sysBundledFont = new JCheckBox(Utility.EMPTY, sysFont == null);
		sysFontField.setEditable(false);
		final JButton browseSysFontButton = new JButton(BROWSELABEL);
		browseSysFontButton.addActionListener(event -> {
			Font font = new FontChooser(mainPanel.getParent(), "Select system font").show(sysFont);
			if (font != null) {
				sysFont = font;
				sysFontField.setText(Parameter.getFontAsDisplayString(font));
				sysBundledFont.setEnabled(false);
			}
		});
		sysBundledFont.addItemListener(event -> {
			if (sysBundledFont.isSelected()) {
				sysFont = null;
				sysFontField.setText(Utility.EMPTY);
			} else if (sysFont == null) {
				sysBundledFont.setSelected(true);
			}
		});
		// commodore font
		JTextField cbmFontField = new JTextField(Parameter.getFontAsDisplayString(cbmFont));
		JCheckBox cbmBundledFont = new JCheckBox(Utility.EMPTY, cbmFont == null);
		cbmFontField.setEditable(false);
		final JButton browseCbmFontButton = new JButton(BROWSELABEL);
		browseCbmFontButton.addActionListener(event -> {
			Font font = new FontChooser(mainPanel.getParent(), "Select Commodore font").show(cbmFont);
			if (font != null) {
				cbmFont = font;
				cbmFontField.setText(Parameter.getFontAsDisplayString(font));
				cbmBundledFont.setSelected(false);
			}
		});
		cbmBundledFont.addItemListener(event -> {
			if (cbmBundledFont.isSelected()) {
				cbmFont = null;
				cbmFontField.setText(Utility.EMPTY);
			} else if (cbmFont == null) {
				cbmBundledFont.setSelected(true);
			}
		});
		// font panels
		JPanel sysFontPanelButtons = new JPanel(new BorderLayout());
		sysFontPanelButtons.add(sysBundledFont, BorderLayout.WEST);
		sysFontPanelButtons.add(browseSysFontButton, BorderLayout.EAST);

		JPanel sysFontFilePanel = new JPanel(new BorderLayout());
		sysFontFilePanel.add(sysFontField, BorderLayout.CENTER);
		sysFontFilePanel.add(sysFontPanelButtons, BorderLayout.EAST);

		JPanel cbmFontPanelButtons = new JPanel(new BorderLayout());
		cbmFontPanelButtons.add(cbmBundledFont, BorderLayout.WEST);
		cbmFontPanelButtons.add(browseCbmFontButton, BorderLayout.EAST);

		JPanel cbmFontFilePanel = new JPanel(new BorderLayout());
		cbmFontFilePanel.add(cbmFontField, BorderLayout.CENTER);
		cbmFontFilePanel.add(cbmFontPanelButtons, BorderLayout.EAST);

		JPanel winSizePanel = new JPanel(new BorderLayout());
		winSizePanel.add(winSizePosField, BorderLayout.CENTER);
		winSizePanel.add(getWinSizeButton, BorderLayout.EAST);

		JPanel fontSizePanel1 = new JPanel(new BorderLayout());
		fontSizePanel1.add(fontSizeSpinner, BorderLayout.WEST);
		fontSizePanel1.add(new JPanel(), BorderLayout.CENTER);

		JPanel fontSizePanel2 = new JPanel(new BorderLayout());
		fontSizePanel2.add(localFontSizeSpinner, BorderLayout.WEST);
		fontSizePanel2.add(new JPanel(), BorderLayout.CENTER);

		JPanel checkboxPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		checkboxPanel.add(exitConfirmCheckBox);
		checkboxPanel.add(hideConsoleCheckBox);

		JPanel localRowHeightPanel = new JPanel(new BorderLayout());
		localRowHeightPanel.add(rowHeightSpinner, BorderLayout.WEST);
		localRowHeightPanel.add(new JPanel(), BorderLayout.CENTER);
		JPanel imageRowHeightPanel = new JPanel(new BorderLayout());
		imageRowHeightPanel.add(localRowHeightSpinner, BorderLayout.WEST);
		imageRowHeightPanel.add(new JPanel(), BorderLayout.CENTER);

		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, guiPanel, new JPanel());
		GuiHelper.addToGridBag(1, 0, 0.0, 0.0, 1, gbc, guiPanel, checkboxPanel);
		GuiHelper.addToGridBag(2, 0, 0.0, 0.0, 1, gbc, guiPanel, new JPanel());

		addField(1, Resources.DROID64_SETTINGS_LOOKFEEL, lookAndFeelBox, guiPanel, gbc);
		addField(2, Resources.DROID64_SETTINGS_GRIDSPACING_IMAGE, localRowHeightPanel, guiPanel, gbc);
		addField(3, Resources.DROID64_SETTINGS_GRIDSPACING_LOCAL, imageRowHeightPanel, guiPanel, gbc);
		addField(4, Resources.DROID64_SETTINGS_FONTSIZE_IMAGE, fontSizePanel1, guiPanel, gbc);
		addField(5, Resources.DROID64_SETTINGS_FONTSIZE_LOCAL, fontSizePanel2, guiPanel, gbc);
		addField(6, Resources.DROID64_SETTINGS_WINDOWSIZE, winSizePanel, guiPanel, gbc);
		addField(7, Resources.DROID64_SETTINGS_SYSFONT, sysFontFilePanel, guiPanel, gbc);
		addField(8, Resources.DROID64_SETTINGS_CBMFONT, cbmFontFilePanel, guiPanel, gbc);
		GuiHelper.addToGridBag(0, 9, 0.5, 0.8, 3, gbc, guiPanel, new JPanel());
		return guiPanel;
	}

	private void addField(int row, String propertyKey, JComponent component, JPanel panel, GridBagConstraints gbc) {
		GuiHelper.addToGridBag(0, row, 0.0, 0.0, 1, gbc, panel, new JLabel(Settings.getMessage(propertyKey)));
		GuiHelper.addToGridBag(1, row, 1.0, 0.0, 1, gbc, panel, component);
		GuiHelper.addToGridBag(2, row, 0.0, 0.0, 1, gbc, panel, new JPanel());
	}

	private int[] parseWindowSizePos(String str) {
		String[] strArr = str.trim().split("\\s*[,:]\\s*");
		if (strArr.length < 4) {
			return null;
		}
		try {
			int[] intArr = new int[4];
			for (int i=0; i < intArr.length; i++) {
				intArr[i] = Integer.valueOf(strArr[i]);
			}
			return intArr;
		} catch (NumberFormatException e) {
			return null;
		}
	}

	private void copyValuesToSettings(final MainPanel mainPanel) {
		Settings.setAskQuit(exitConfirmCheckBox.isSelected());
		Settings.setHideConsole(hideConsoleCheckBox.isSelected());
		Settings.setColourChoice(colourBox.getSelectedIndex());
		Settings.setRowHeight(Integer.parseInt(rowHeightSpinner.getValue().toString()));
		Settings.setLookAndFeel(lookAndFeelBox.getSelectedIndex());
		Settings.setLocalRowHeight(Integer.parseInt(localRowHeightSpinner.getValue().toString()));
		Settings.setUseDb(useJdbcCheckBox.isSelected());
		Settings.setJdbcDriver(jdbcDriver.getText());
		Settings.setJdbcUrl(jdbcUrl.getText());
		Settings.setJdbcUser(jdbcUser.getText());
		Settings.setJdbcPassword(new String(jdbcPassword.getPassword()));
		Settings.setJdbcLimitType(limitTypeBox.getSelectedIndex());
		Settings.setWindow(parseWindowSizePos(winSizePosField.getText()));
		Settings.setSysFont(sysFont);
		Settings.setCbmFont(cbmFont);

		Settings.setFileExtensions(DiskImage.D64_IMAGE_TYPE, fileExtD64.getText(), false);
		Settings.setFileExtensions(DiskImage.D67_IMAGE_TYPE, fileExtD67.getText(), false);
		Settings.setFileExtensions(DiskImage.D71_IMAGE_TYPE, fileExtD71.getText(), false);
		Settings.setFileExtensions(DiskImage.D80_IMAGE_TYPE, fileExtD80.getText(), false);
		Settings.setFileExtensions(DiskImage.D81_IMAGE_TYPE, fileExtD81.getText(), false);
		Settings.setFileExtensions(DiskImage.D82_IMAGE_TYPE, fileExtD82.getText(), false);
		Settings.setFileExtensions(DiskImage.LNX_IMAGE_TYPE, fileExtLNX.getText(), false);
		Settings.setFileExtensions(DiskImage.T64_IMAGE_TYPE, fileExtT64.getText(), false);
		Settings.setFileExtensions(DiskImage.D64_IMAGE_TYPE, fileExtD64gz.getText(), true);
		Settings.setFileExtensions(DiskImage.D67_IMAGE_TYPE, fileExtD67gz.getText(), true);
		Settings.setFileExtensions(DiskImage.D71_IMAGE_TYPE, fileExtD71gz.getText(), true);
		Settings.setFileExtensions(DiskImage.D80_IMAGE_TYPE, fileExtD80gz.getText(), true);
		Settings.setFileExtensions(DiskImage.D81_IMAGE_TYPE, fileExtD81gz.getText(), true);
		Settings.setFileExtensions(DiskImage.D82_IMAGE_TYPE, fileExtD82gz.getText(), true);
		Settings.setFileExtensions(DiskImage.D88_IMAGE_TYPE, fileExtD88gz.getText(), true);
		Settings.setFileExtensions(DiskImage.LNX_IMAGE_TYPE, fileExtLNXgz.getText(), true);
		Settings.setFileExtensions(DiskImage.T64_IMAGE_TYPE, fileExtT64gz.getText(), true);

		try {
			Settings.setMaxRows(Integer.parseInt(maxRows.getText()));
		} catch (NumberFormatException e) {
			maxRows.setValue(25L);
		}

		for (int i = 0; i < pluginCommandField.length; i++) {
			mainPanel.setPluginButtonLabel(i, pluginLabelTextField[i].getText());
			Settings.setExternalProgram(i,
					pluginCommandField[i].getPath(),
					pluginArgumentTextField[i].getText(),
					pluginDescriptionTextField[i].getText(),
					pluginLabelTextField[i].getText(),
					forkThreadCheckBox[i].isSelected());
		}
	}

	private void setupColorButton(final JButton fgButton, final JButton bgButton, final String fg, final String bg) {
		fgButton.setForeground(Settings.getColorParam(fg));
		fgButton.setBackground(Settings.getColorParam(bg));
		bgButton.setForeground(Settings.getColorParam(fg));
		bgButton.setBackground(Settings.getColorParam(bg));

		fgButton.addActionListener(event -> {
			Color c = JColorChooser.showDialog(fgButton, LBL_FOREGROUND, fgButton.getForeground());
			fgButton.setForeground(c);
			bgButton.setForeground(c);
			Settings.setColorParam(fg, fgButton.getForeground());
			Settings.setColorParam(bg, bgButton.getBackground());
		});
		bgButton.addActionListener(event -> {
			Color c = JColorChooser.showDialog(bgButton, LBL_BACKGROUND, bgButton.getBackground());
			fgButton.setBackground(c);
			bgButton.setBackground(c);
			Settings.setColorParam(fg, fgButton.getForeground());
			Settings.setColorParam(bg, bgButton.getBackground());
		});
	}

	/**
	 * Setup Color settings
	 * @return JPanel
	 */
	private JPanel drawColorPanel() {

		colourBox.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_COLOR_SCHEME_TOOLTIP));
		colourBox.setEditable(false);
		colourBox.setSelectedIndex(Settings.getColourChoice()<COLORS.length ? Settings.getColourChoice() : 0);

		setupColorButton(colorFgButton, colorBgButton, Settings.SETTING_DIR_FG, Settings.SETTING_DIR_BG);
		setupColorButton(colorCpmFgButton, colorCpmBgButton, Settings.SETTING_DIR_CPM_FG, Settings.SETTING_DIR_CPM_BG);
		setupColorButton(colorLocalFgButton, colorLocalBgButton, Settings.SETTING_DIR_LOCAL_FG, Settings.SETTING_DIR_LOCAL_BG);

		final JButton colorActiveBorderButton = new JButton(Settings.getMessage(Resources.DROID64_SETTINGS_COLOR_ACTIVE));
		colorActiveBorderButton.setBorder(BorderFactory.createLineBorder(Settings.getActiveBorderColor(), 3));
		colorActiveBorderButton.addActionListener(ae -> {
			Color c = JColorChooser.showDialog(colorActiveBorderButton, LBL_FOREGROUND, Settings.getActiveBorderColor());
			colorActiveBorderButton.setBorder(BorderFactory.createLineBorder(c));
			Settings.setActiveBorderColor(c);
		});
		final JButton colorInactiveBorderButton = new JButton(Settings.getMessage(Resources.DROID64_SETTINGS_COLOR_INACTIVE));
		colorInactiveBorderButton.setBorder(BorderFactory.createLineBorder(Settings.getInactiveBorderColor(), 3));
		colorInactiveBorderButton.addActionListener(ae -> {
			Color c = JColorChooser.showDialog(colorInactiveBorderButton, LBL_BACKGROUND, Settings.getInactiveBorderColor());
			colorInactiveBorderButton.setBorder(BorderFactory.createLineBorder(c));
			Settings.setInactiveBorderColor(c);
		});

		final JButton colorResetButton = new JButton(Settings.getMessage(Resources.DROID64_SETTINGS_COLOR_RESET));
		colorResetButton.addActionListener(ae -> resetButtonColors(colorActiveBorderButton, colorInactiveBorderButton));

		JPanel colorPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		addFields(0, Resources.DROID64_SETTINGS_COLOR_THEME, colourBox, new JPanel(), colorPanel, gbc);
		addFields(1, Utility.EMPTY, colorResetButton, new JPanel(), colorPanel, gbc);
		addFields(2, Resources.DROID64_SETTINGS_COLOR_CBM, colorFgButton, colorBgButton, colorPanel, gbc);
		addFields(3, Resources.DROID64_SETTINGS_COLOR_CPM, colorCpmFgButton, colorCpmBgButton, colorPanel, gbc);
		addFields(4, Resources.DROID64_SETTINGS_COLOR_LOCAL, colorLocalFgButton, colorLocalBgButton, colorPanel, gbc);
		addFields(5, Resources.DROID64_SETTINGS_COLOR_BORDER, colorActiveBorderButton, colorInactiveBorderButton, colorPanel, gbc);

		GuiHelper.addToGridBag(0, 6, 0.5, 0.8, 3, gbc, colorPanel, new JPanel());
		return colorPanel;
	}

	private void resetButtonColors(JButton colorActiveBorder, JButton colorInactiveBorder) {
		colorFgButton.setForeground(Settings.DIR_FG_COLOR_C64);
		colorFgButton.setBackground(Settings.DIR_BG_COLOR_C64);
		colorBgButton.setForeground(Settings.DIR_FG_COLOR_C64);
		colorBgButton.setBackground(Settings.DIR_BG_COLOR_C64);
		colorCpmFgButton.setForeground(Settings.DIR_FG_COLOR_CPM);
		colorCpmFgButton.setBackground(Settings.DIR_BG_COLOR_CPM);
		colorCpmBgButton.setForeground(Settings.DIR_FG_COLOR_CPM);
		colorCpmBgButton.setBackground(Settings.DIR_BG_COLOR_CPM);
		colorLocalFgButton.setForeground(Settings.DIR_FG_COLOR_LOCAL);
		colorLocalFgButton.setBackground(Settings.DIR_BG_COLOR_LOCAL);
		colorLocalBgButton.setForeground(Settings.DIR_FG_COLOR_LOCAL);
		colorLocalBgButton.setBackground(Settings.DIR_BG_COLOR_LOCAL);
		colorActiveBorder.setBorder(BorderFactory.createLineBorder(Settings.ACTIVE_BORDER_COLOR));
		colorInactiveBorder.setBorder(BorderFactory.createLineBorder(Settings.INACTIVE_BORDER_COLOR));
		Settings.setDirColors(colorBgButton.getBackground(), colorFgButton.getForeground());
		Settings.setDirCpmColors(colorCpmBgButton.getBackground(), colorCpmFgButton.getForeground());
		Settings.setDirLocalColors(colorLocalBgButton.getBackground(), colorLocalFgButton.getForeground());
		Settings.setActiveBorderColor(Settings.ACTIVE_BORDER_COLOR);
		Settings.setInactiveBorderColor(Settings.INACTIVE_BORDER_COLOR);
	}

	/**
	 * Create the panel with database settings.
	 * @return JPanel
	 */
	private JPanel drawDatabasePanel() {
		JPanel dbPanel = new JPanel(new GridBagLayout());
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.HORIZONTAL;

		useJdbcCheckBox.setSelected(Settings.getUseDb());
		final JButton testConnectionButton = new JButton(Settings.getMessage(Resources.DROID64_SETTINGS_JDBC_TEST));
		status.setFont(new Font("Verdana", Font.PLAIN, status.getFont().getSize()));
		status.setLineWrap(true);
		status.setWrapStyleWord(true);
		status.setEditable(false);
		useJdbcCheckBox.addActionListener(ae-> {
			boolean enabled = useJdbcCheckBox.isSelected();
			jdbcDriver.setEnabled(enabled);
			jdbcUrl.setEnabled(enabled);
			jdbcUser.setEnabled(enabled);
			jdbcPassword.setEnabled(enabled);
			maxRows.setEnabled(enabled);
			testConnectionButton.setEnabled(enabled);
		});

		testConnectionButton.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_JDBC_TEST_TOOLTIP));
		testConnectionButton.addActionListener(ae -> {
			String errStr = DaoFactoryImpl.testConnection(jdbcDriver.getText(), jdbcUrl.getText(), jdbcUser.getText(), new String(jdbcPassword.getPassword()));
			status.setText(errStr == null ? Settings.getMessage(Resources.DROID64_SETTINGS_OK) : errStr);
		});

		boolean jdbcEnabled = useJdbcCheckBox.isSelected();
		jdbcDriver.setEnabled(jdbcEnabled);
		jdbcUrl.setEnabled(jdbcEnabled);
		jdbcUser.setEnabled(jdbcEnabled);
		jdbcPassword.setEnabled(jdbcEnabled);
		maxRows.setEnabled(jdbcEnabled);
		testConnectionButton.setEnabled(jdbcEnabled);

		limitTypeBox.setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_JDBC_LIMIT_TOOLTIP));
		limitTypeBox.setEditable(false);
		limitTypeBox.setSelectedIndex(0);
		limitTypeBox.setSelectedIndex(Settings.getJdbcLimitType() < DaoFactory.getLimitNames().length ? Settings.getJdbcLimitType() : 0);

		JButton jdbcDriverBrowse = new JButton(BROWSELABEL);
		jdbcDriverBrowse.setEnabled(!jdbcDriverClasses.isEmpty());
		jdbcDriverBrowse.addActionListener(e->browseJdbcDrivers());

		if (jdbcDriver.getText().isEmpty() && jdbcDriverClasses.size() == 1) {
			jdbcDriver.setText(jdbcDriverClasses.get(0));
		}

		final String sqlString = Settings.getMessage("jdbc.feature.message", Settings.getMessage(Resources.JDBC_MYSQL_URL), Settings.getMessage(Resources.JDBC_POSTGRESQL_URL));
		final String sql = getSqlSetupScript(mainPanel);
		final JButton viewSqlButton = new JButton(Settings.getMessage(Resources.DROID64_SETTINGS_JDBC_SQL));
		viewSqlButton.addActionListener(ae -> new TextViewPanel(mainPanel).show(sql, "DroiD64","SQL database setup", Utility.MIMETYPE_TEXT));

		final JTextArea messageTextArea = new JTextArea(10,45);
		messageTextArea.setBackground(new Color(230,230,230));
		messageTextArea.setEditable(false);
		messageTextArea.setWrapStyleWord(true);
		messageTextArea.setLineWrap(true);
		messageTextArea.setText(sqlString);
		messageTextArea.setCaretPosition(0);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(viewSqlButton);
		buttonPanel.add(testConnectionButton);
		buttonPanel.add(new JPanel());

		GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());
		GuiHelper.addToGridBag(1, 0, 0.5, 0.0, 1, gbc, dbPanel, useJdbcCheckBox);
		GuiHelper.addToGridBag(2, 0, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());

		JPanel p = new JPanel(new BorderLayout());
		p.add(jdbcDriver, BorderLayout.CENTER);
		p.add(jdbcDriverBrowse, BorderLayout.EAST);

		addField(1, Resources.DROID64_SETTINGS_JDBC_CLASS, p, dbPanel, gbc);
		addField(2, Resources.DROID64_SETTINGS_JDBC_URL, jdbcUrl, dbPanel, gbc);
		addField(3, Resources.DROID64_SETTINGS_JDBC_USER, jdbcUser, dbPanel, gbc);
		addField(4, Resources.DROID64_SETTINGS_JDBC_PASS, jdbcPassword, dbPanel, gbc);
		addField(5, Resources.DROID64_SETTINGS_JDBC_ROWS, maxRows, dbPanel, gbc);
		addField(6, Resources.DROID64_SETTINGS_JDBC_LIMIT, limitTypeBox, dbPanel, gbc);

		GuiHelper.addToGridBag(0, 7, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());
		GuiHelper.addToGridBag(1, 7, 0.5, 0.0, 2, gbc, dbPanel, buttonPanel);

		addField(8, Resources.DROID64_SETTINGS_JDBC_STATUS, status, dbPanel, gbc);

		GuiHelper.addToGridBag(0, 9, 0.0, 0.0, 1, gbc, dbPanel, new JPanel());
		gbc.fill = GridBagConstraints.BOTH;
		GuiHelper.addToGridBag(1, 9, 0.5, 0.9, 1, gbc, dbPanel, new JScrollPane(messageTextArea));

		return dbPanel;
	}

	private void browseJdbcDrivers() {
	    DefaultListModel<String> classModel = new DefaultListModel<>();
	    jdbcDriverClasses.forEach(classModel::addElement);
		JList<String> classList = new JList<>(classModel);
		classList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		classList.setSelectedValue(Settings.getJdbcDriver(), true);
		if (JOptionPane.OK_OPTION == JOptionPane.showConfirmDialog(this, new JScrollPane(classList), "Choose JDBC driver", JOptionPane.OK_CANCEL_OPTION)) {
			String drv = classList.getSelectedValue();
			if (drv != null) {
				jdbcDriver.setText(drv);
			}
		}
	}

	/**
	 * Setup MAX_PLUGINS of panels for plugins.
	 * @param mainPanel
	 * @return JPanel[]
	 */
	private JPanel[] drawPluginPanel() {
		JPanel[] pluginPanel = new JPanel[Settings.MAX_PLUGINS];

		FileFilter fileFilter = new FileFilter() {
			@Override
			public boolean accept(File f) {
				return f.canExecute();
			}

			@Override
			public String getDescription() {
				return "Executable";
			}};

		for (int i = 0; i < Settings.MAX_PLUGINS; i++) {
			pluginPanel[i] = new JPanel(new GridBagLayout());
			GridBagConstraints gbc = new GridBagConstraints();
			gbc.fill = GridBagConstraints.HORIZONTAL;

			pluginLabelTextField[i] = getTextField(Resources.DROID64_SETTINGS_EXE_LABEL_TOOLTIP);

			pluginCommandField[i] = new FilePathPanel(Settings.getDefaultImageDir(), JFileChooser.FILES_ONLY, null);
			pluginCommandField[i].setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_CHOOSE_TOOLTIP));
			pluginCommandField[i].setFileFilter(fileFilter);

			pluginArgumentTextField[i] = getTextArea(ARGUMENT_TOOLTIP);
			pluginDescriptionTextField[i] = getTextArea(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_DESCR_TOOLTIP));
			forkThreadCheckBox[i] = new JCheckBox(Utility.EMPTY, true);
			forkThreadCheckBox[i].setToolTipText(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_FORK_TOOLTIP));

			if (i < Settings.getExternalPrograms().length && Settings.getExternalPrograms()[i] != null) {
				ExternalProgram prg = Settings.getExternalPrograms()[i];
				pluginLabelTextField[i].setText(prg.getLabel());
				pluginCommandField[i].setPath(prg.getCommand());
				pluginArgumentTextField[i].setText(prg.getArguments());
				pluginDescriptionTextField[i].setText(prg.getDescription());
				forkThreadCheckBox[i].setSelected(prg.isForkThread());
			}

			GuiHelper.addToGridBag(0, 0, 0.0, 0.0, 1, gbc, pluginPanel[i], new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_LABEL)));
			GuiHelper.addToGridBag(1, 0, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginLabelTextField[i]);

			GuiHelper.addToGridBag(0, 1, 0.0, 0.0, 1, gbc, pluginPanel[i], new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_COMMAND)));
			GuiHelper.addToGridBag(1, 1, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginCommandField[i]);

			GuiHelper.addToGridBag(0, 2, 0.0, 0.0, 1, gbc, pluginPanel[i], new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_ARGS)));
			GuiHelper.addToGridBag(1, 2, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginArgumentTextField[i]);

			GuiHelper.addToGridBag(0, 3, 0.0, 0.0, 1, gbc, pluginPanel[i], new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_DESCR)));
			GuiHelper.addToGridBag(1, 3, 0.5, 0.0, 1, gbc, pluginPanel[i], pluginDescriptionTextField[i]);

			GuiHelper.addToGridBag(0, 4, 0.0, 0.0, 1, gbc, pluginPanel[i], new JLabel(Settings.getMessage(Resources.DROID64_SETTINGS_EXE_FORK)));
			GuiHelper.addToGridBag(1, 4, 0.5, 0.0, 1, gbc, pluginPanel[i], forkThreadCheckBox[i]);

			GuiHelper.addToGridBag(0, 5, 0.5, 1.0, 2, gbc, pluginPanel[i], new JPanel());
		}
		return pluginPanel;
	}

	private JTextArea getTextArea(String toolTip) {
		JTextArea area = new JTextArea(4, 20);
		area.setLineWrap(true);
		area.setWrapStyleWord(true);
		area.setToolTipText(toolTip);
		area.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		area.setText(Utility.EMPTY);
		return area;
	}

	private JTextField getTextField(String toolTipPropertyKey) {
		JTextField field = new JTextField();
		field.setToolTipText(Settings.getMessage(toolTipPropertyKey));
		field.setBorder(BorderFactory.createLineBorder(Color.BLACK));
		field.setText(Utility.EMPTY);
		return field;
	}

	private static String getSqlSetupScript(final MainPanel mainPanel) {
		if (sqlSetupScript == null) {
			try (InputStream in = SettingsPanel.class.getResourceAsStream("/setup_database.sql");
					BufferedReader input = new BufferedReader(new InputStreamReader(in)))
			{
				StringBuilder buf = new StringBuilder();
				for (String line = input.readLine(); line != null; line = input.readLine()) {
					buf.append(line);
					buf.append('\n');
				}
				sqlSetupScript = buf.toString();
			} catch (IOException e) {	//NOSONAR
				mainPanel.appendConsole("Failed to find SQL setup script.\n"+e.getMessage());
			}
		}
		return sqlSetupScript;
	}
}
