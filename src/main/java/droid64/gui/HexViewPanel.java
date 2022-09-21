package droid64.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.WindowConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.TableColumnModelEvent;
import javax.swing.event.TableColumnModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.d64.ProgramParser;
import droid64.d64.TrackSector;
import droid64.d64.Utility;

/**
 * Class used to show a hex dump of a byte array.
 *
 * @author Henrik
 */
public class HexViewPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private static final Color ADDR_SELECTED_FOREGROUND  = Color.BLACK;
	private static final Color ADDR_SELECTED_BACKGROUND  = new Color(100,100,100);
	private static final Color ASCII_SELECTED_FOREGROUND = Color.BLACK;
	private static final Color ASCII_SELECTED_BACKGROUND = new Color(100,100,100);
	private static final Color HEX_SELECTED_FOREGROUND   = Color.BLACK;
	private static final Color HEX_SELECTED_BACKGROUND   = new Color(232,232,232);
	private static final Color ADDR_NORMAL_FOREGROUND    = Color.BLACK;
	private static final Color ADDR_NORMAL_BACKGROUND    = new Color(200,200,200);
	private static final Color ASCII_NORMAL_FOREGROUND   = Color.BLACK;
	private static final Color ASCII_NORMAL_BACKGROUND   = new Color(200,200,200);
	private static final Color HEX_NORMAL_ASC_FOREGROUND = Color.BLACK;
	private static final Color HEX_NORMAL_HEX_FOREGROUND = new Color(  0,  0, 80);
	private static final Color HEX_NORMAL_BACKGROUND     = Color.WHITE;

	private static final Color[][] COLORS = {
			{ADDR_SELECTED_FOREGROUND, ADDR_SELECTED_BACKGROUND},
			{ADDR_NORMAL_FOREGROUND, ADDR_NORMAL_BACKGROUND},
			{ASCII_SELECTED_FOREGROUND, ASCII_SELECTED_BACKGROUND},
			{ASCII_NORMAL_FOREGROUND, ASCII_NORMAL_BACKGROUND},
			{HEX_SELECTED_FOREGROUND, HEX_SELECTED_BACKGROUND},
			{HEX_NORMAL_HEX_FOREGROUND, HEX_NORMAL_BACKGROUND},
			{HEX_NORMAL_ASC_FOREGROUND, HEX_NORMAL_BACKGROUND}
	};

	private static final String ASM_MODE = Settings.getMessage(Resources.DROID64_HEXVIEW_ASMMODE);
	private static final String HEX_MODE = Settings.getMessage(Resources.DROID64_HEXVIEW_HEXMODE);
	private final MainPanel mainPanel;
	private final JButton modeButton = new JButton(HEX_MODE);
	private final JTextArea asmTextArea = new JTextArea();
	private final JPanel cards = new JPanel(new CardLayout());
	private final HexTableModel model = new HexTableModel();
	private final JTable table = new JTable(model) {
		private static final long serialVersionUID = 1L;
		private final CustomCellRenderer renderer = new CustomCellRenderer();
		@Override
		public TableCellRenderer getCellRenderer(int row, int column) {
			return renderer;
		}
	};
	private final JToggleButton c64ModeButton = new JToggleButton("C64 mode");
	private final JButton okButton = new JButton(Settings.getMessage(Resources.DROID64_HEXVIEW_CLOSE));

	private final boolean[] c64Modes = {false, false};
	private byte[] data;

	private JFormattedTextField trkField;
	private JFormattedTextField secField;
	private JTextField offset;
	private final JButton goBlockButton = new JButton("---:---");

	private final String title;
	private int selectedTrack = -1;
	private int selectedSector = -1;

	public HexViewPanel (String title,  MainPanel mainPanel, int track, int sector, DiskImage diskImage) throws CbmException {
		this.title = title;
		this.data = diskImage.getBlock(track, sector);
		this.mainPanel = mainPanel;
		setLayout(new BorderLayout());
		add(createTrackSectorPanel(track, sector, diskImage), BorderLayout.NORTH);

		setup(data, data.length, "block", false);

		table.getSelectionModel().addListSelectionListener(ev ->  tableSelectionAction(diskImage));
		table.getColumnModel().addColumnModelListener(new DiskImageTableModelListener(diskImage));
	}

	/**
	 * Constructor
	 * @param title String with window title
	 * @param mainPanel main panel
	 * @param fileName String with the file name to show.
	 * @param data a byte array with the data to show
	 * @param length the length of data to show
	 * @param readLoadAddr read load address
	 */
	public HexViewPanel (String title, MainPanel mainPanel, final String fileName, final byte[] data, final int length, final boolean readLoadAddr) {
		this.title = title;
		this.mainPanel = mainPanel;
		this.data = data;
		setLayout(new BorderLayout());
		add(new JLabel(fileName), BorderLayout.NORTH);

		setup(data, length, fileName, readLoadAddr);
	}

	public void showDialog() {
		final JDialog dialog = new JDialog(mainPanel.getParent(), title, true);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
		okButton.addActionListener(e -> dialog.dispose());
		dialog.pack();
		dialog.setLocationRelativeTo(mainPanel.getParent());
		dialog.setVisible(true);
	}

	private void setup(byte[] data, int length, String label, boolean readLoadAddr) {

		model.loadData(data, length);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		table.setFont(getTableFont(false));
		table.getTableHeader().setReorderingAllowed(false);
		resizeTable(false);

		asmTextArea.setText(Utility.EMPTY);
		asmTextArea.setEditable(false);
		asmTextArea.setFont(getTableFont(false));

		// Setup cards
		cards.add(new JScrollPane(table), HEX_MODE);
		cards.add(new JScrollPane(asmTextArea), ASM_MODE);
		add(cards, BorderLayout.CENTER);

		add(drawButtons(length, readLoadAddr, label), BorderLayout.SOUTH);
		setSize(table.getWidth(), table.getHeight());
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	private void tableSelectionAction(DiskImage diskImage) {
		selectedTrack = -1;
		selectedSector = -1;

		goBlockButton.setText("---:---");
		goBlockButton.setEnabled(false);

		int r = table.getSelectedRow();
		int c = table.getSelectedColumn();
		if (r < 0 || c == 0 || c >= table.getColumnCount() - 1) {
			return;
		}
		int t = Integer.valueOf((String)table.getValueAt(r, c), 16);
		if (t < diskImage.getFirstTrack() || t > diskImage.getTrackCount()) {
			return;
		}
		if (++c >= table.getColumnCount() - 1) {
			c = 1;
			if (++r >= table.getRowCount()) {
				return;
			}
		}
		int s = Integer.valueOf((String)table.getValueAt(r, c), 16);
		if (s < 0 || s > diskImage.getMaxSectors(t)) {
			return;
		}
		selectedTrack = t;
		selectedSector = s;
		goBlockButton.setText(t + ":" + s);
		goBlockButton.setEnabled(true);
	}

	private JPanel createTrackSectorPanel(int track, int sector, final DiskImage diskImage) {
		offset = new JTextField(getOffset(track, sector, diskImage), 8);
		offset.setEditable(false);

		trkField = GuiHelper.getNumField(diskImage.getFirstTrack(), diskImage.getTrackCount() + diskImage.getFirstTrack(), track, 3);
		secField = GuiHelper.getNumField(diskImage.getFirstSector(), diskImage.getLastSector(), sector, 3);

		trkField.addActionListener(event -> {
			int trk = (int) trkField.getValue();
			int sec = (int) secField.getValue();
			int maxSect = diskImage.getMaxSectors(trk + diskImage.getFirstTrack() - 1) + diskImage.getFirstSector() - 1;
			if (sec > maxSect) {
				sec = maxSect;
			}
			loadBlock(trk, sec, diskImage);
		});
		secField.addActionListener(event -> loadBlock((int)trkField.getValue(), (int) secField.getValue(), diskImage));

		goBlockButton.setEnabled(false);
		goBlockButton.setMargin(new Insets(1, 4, 1, 4));
		goBlockButton.addActionListener(ev -> loadBlock(selectedTrack, selectedSector, diskImage));
		JButton downButton = new JButton("-");
		JButton upButton = new JButton("+");
		downButton.setMargin(new Insets(0,4,0,4));
		upButton.setMargin(new Insets(0,4,0,4));

		downButton.addActionListener(ev ->
			loadBlock(diskImage.getSector(diskImage.getSectorOffset((int)trkField.getValue(), (int) secField.getValue()) - 256), diskImage));
		upButton.addActionListener(ev ->
			loadBlock(diskImage.getSector(diskImage.getSectorOffset((int)trkField.getValue(), (int) secField.getValue()) + 256), diskImage));

		JPanel upDownPanel = new JPanel(new GridLayout(1, 2));
		upDownPanel.add(downButton);
		upDownPanel.add(upButton);

		JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		panel.add(new JLabel("Track:"));
		panel.add(trkField);
		panel.add(new JLabel("Sector:"));
		panel.add(secField);
		panel.add(new JLabel("Offset:"));
		panel.add(offset);
		panel.add(upDownPanel);
		panel.add(goBlockButton);
		return panel;
	}

	private String getOffset(int trk, int sec, DiskImage diskImage) {
		return String.format(" $%06X ", diskImage.getSectorOffset(trk, sec));
	}

	private void loadBlock(TrackSector ts, final DiskImage diskImage) {
		if (ts != null) {
			loadBlock(ts.getTrack(), ts.getSector(), diskImage);
		}
	}

	private void loadBlock(int track, int sector, final DiskImage diskImage) {
		try {
			if (track == -1 || sector == -1) {
				return;
			}
			data = diskImage.getBlock(track, sector);
			table.clearSelection();
			asmTextArea.setText(Utility.EMPTY);
			trkField.setValue(track);
			secField.setValue(sector);
			model.loadData(data, data.length);
			offset.setText(getOffset(track, sector, diskImage));
			String code = ProgramParser.parse(data, DiskImage.BLOCK_SIZE, false);
			asmTextArea.setText(code);
			asmTextArea.setCaretPosition(0);
		} catch (CbmException e) { /* ignore */ }
	}

	private JPanel drawButtons(final int length, final boolean readLoadAddr, final String fileName) {
		modeButton.setMnemonic('m');
		modeButton.addActionListener(ae -> switchHexAsmMode(length, readLoadAddr));
		c64ModeButton.setMnemonic('c');
		c64ModeButton.addActionListener(ae -> switchFont(c64ModeButton.isSelected()));

		okButton.setMnemonic('o');
		okButton.setToolTipText(Settings.getMessage(Resources.DROID64_HEXVIEW_CLOSE_TOOLTIP));

		final JButton printButton = new JButton(Settings.getMessage(Resources.DROID64_HEXVIEW_PRINT));
		printButton.setMnemonic('p');
		printButton.addActionListener(ae -> print(data, c64Modes[isHexMode() ? 0 : 1], fileName, isHexMode()));

		final JButton saveButton = new JButton(Settings.getMessage(Resources.DROID64_HEXVIEW_SAVETEXT));
		saveButton.setMnemonic('s');
		saveButton.addActionListener(ae-> saveText(data, fileName, isHexMode()));

		final JButton saveDataButton = new JButton(Settings.getMessage(Resources.DROID64_HEXVIEW_SAVEDATA));
		saveDataButton.setMnemonic('d');
		saveDataButton.addActionListener(ae-> saveData(data, fileName));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(modeButton);
		buttonPanel.add(c64ModeButton);
		buttonPanel.add(printButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(saveDataButton);
		buttonPanel.add(okButton);
		return buttonPanel;
	}

	private void switchFont(boolean useCbmFont) {
		c64ModeButton.setSelected(useCbmFont);
		if (isHexMode()) {
			c64Modes[0] = useCbmFont;
			table.setFont(getTableFont(useCbmFont));
			resizeTable(useCbmFont);
		} else {
			c64Modes[1] = useCbmFont;
			asmTextArea.setFont(getTableFont(useCbmFont));
		}
	}

	private boolean isHexMode() {
		return HEX_MODE.equals(modeButton.getText());
	}

	private void switchHexAsmMode(int length, boolean readLoadAddr) {
		if (isHexMode()) {
			// from hex mode to asm mode
			modeButton.setText(ASM_MODE);
			if (Utility.EMPTY.equals(asmTextArea.getText())) {
				String code = ProgramParser.parse(data, length, readLoadAddr);
				asmTextArea.setText(code);
				asmTextArea.setCaretPosition(0);
			}
			switchFont(c64Modes[1]);
		} else {
			modeButton.setText(HEX_MODE);
			switchFont(c64Modes[0]);
			resizeTable(c64ModeButton.isSelected());
		}

		asmTextArea.setSize(table.getSize());
		CardLayout cl = (CardLayout) cards.getLayout();
		cl.show(cards, modeButton.getText());
	}

	private void resizeTable(boolean useCbmFont) {
		int chrWdt = table.getFontMetrics(getTableFont(useCbmFont)).stringWidth("w");
		int colWdt = chrWdt * 3;
		int addrWdt = chrWdt * 10;
		int ascWdt = chrWdt * model.getColumnCount();
		for (int i=0; i<table.getColumnCount(); i++) {
			TableColumn column = table.getColumnModel().getColumn(i);
			if (i == 0) {
				column.setPreferredWidth(addrWdt);
			} else if (i == (model.getColumnCount() - 1)) {
				column.setPreferredWidth(ascWdt);
			} else {
				column.setPreferredWidth(colWdt);
			}
		}
		int wdt = addrWdt + colWdt*(model.getColumnCount() - 2) + ascWdt;
		int hgt=0;
		for (int i=0; i<Math.min(model.getColumnCount() - 2, table.getRowCount()); i++) {
			hgt += table.getRowHeight(i);
		}
		ToolTipManager.sharedInstance().unregisterComponent(table);
		ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
		table.setPreferredScrollableViewportSize(new Dimension(wdt, hgt));
		table.setSize(wdt, hgt);
		table.invalidate();
	}

	private Font getTableFont(boolean useCbmFont) {
		try {
			return useCbmFont ? Settings.getCommodoreFont() : new Font("Courier", Font.PLAIN, Settings.getFontSize());
		} catch (CbmException e) {
			mainPanel.appendConsole("Failed to set Commodore font. Using default font.\n"+e);
			return new JPanel().getFont();
		}
	}

	private void saveText(byte[] data, String fileName, boolean hexMode) {
		String[] ext = hexMode ? new String[] {".txt", ".asm"} : new String[] {".asm", ".txt"};
		String ftitle = hexMode ? Settings.getMessage(Resources.DROID64_HEXVIEW_SAVEHEX) : Settings.getMessage(Resources.DROID64_HEXVIEW_SAVEASM);
		String fname = FileDialogHelper.openTextFileDialog(ftitle, null, fileName, true, ext);
		if (fname != null) {
			String outString = hexMode ? Utility.hexDump(data) : asmTextArea.getText();
			if (!outString.isEmpty()) {
				writeToFile(new File(fname), outString);
			}
		}
	}

	private void saveData(byte[] data, String fileName) {
		String[] ext = new String[] {".dat", ".bin"};
		String ftitle = Settings.getMessage(Resources.DROID64_HEXVIEW_SAVEDATA);
		String fname = FileDialogHelper.openTextFileDialog(ftitle, null, fileName, true, ext);
		if (fname != null) {
			File file = new File(fname);
			try {
				Utility.writeFile(file, data);
			} catch (CbmException e) {
				mainPanel.appendConsole("Error: failed to write to file "+file.getName()+'\n'+e.getMessage());
			}
		}
	}

	private void writeToFile(File saveFile, String outString) {
		try {
			Utility.writeFile(saveFile, outString);
		} catch (CbmException e) {
			mainPanel.appendConsole("Error: failed to write to file "+saveFile.getName()+'\n'+e.getMessage());
		}
	}

	private void print(final byte[] data, boolean useCbmfont, final String title, boolean hexmode) {
		PrinterJob job = PrinterJob.getPrinterJob();
		if (hexmode) {
			job.setPageable(new PrintPageable(data, useCbmfont, title, mainPanel));
		} else {
			String header = String.format("; Created by %s version %s%n; %s%n%n", DroiD64.PROGNAME, DroiD64.VERSION, new Date().toString());
			if (useCbmfont) {
				header = header.toUpperCase();
			}
			job.setPageable(new PrintPageable(header + asmTextArea.getText(), "; " + title, useCbmfont, true, mainPanel));
		}
		mainPanel.appendConsole("Print " + (hexmode ? "hex" : "asm") + " mode using " + (useCbmfont ? "C64" : "system") + " font");
		if (job.printDialog()) {
			try {
				job.print();
			} catch (PrinterException e) {
				mainPanel.appendConsole("Failed to print: " + e);
			}
		}
	}

	private class DiskImageTableModelListener implements TableColumnModelListener {
		private final DiskImage diskImage;

		public DiskImageTableModelListener(DiskImage diskImage) {
			this.diskImage = diskImage;
		}

		@Override
		public void columnAdded(TableColumnModelEvent ev) { /* ignore*/	}

		@Override
		public void columnRemoved(TableColumnModelEvent ev) { /* ignore*/	}

		@Override
		public void columnMoved(TableColumnModelEvent ev) { /* ignore*/ }

		@Override
		public void columnMarginChanged(ChangeEvent ev) { /* ignore*/ }

		@Override
		public void columnSelectionChanged(ListSelectionEvent ev) {
			if (!ev.getValueIsAdjusting()) {
				tableSelectionAction(diskImage);
			}
		}
	}

	/** Class to handle colors of the table cells */
	private class CustomCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		@Override
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			int color = getColorIndex(column, row, isSelected, table);
			return setColors(rendererComp, COLORS[color][0], COLORS[color][1]);
		}
		private int getColorIndex(int column, int row, boolean isSelected, JTable table) {
			if (column == 0) {
				return isSelected ? 0 : 1;
			} else if (column == table.getColumnCount() - 1) {
				return isSelected ? 2 : 3;
			} else if (isSelected) {
				return 4;
			} else {
				Integer v = model.getByteAt(row, column);
				if (v != null && (v < 0x20 || v > 0x7f) ) {
					return 5;
				} else {
					return 6;
				}
			}
		}
		private Component setColors(Component comp, Color fg, Color bg) {
			comp.setForeground(fg);
			comp.setBackground(bg);
			return comp;
		}
	}
}
