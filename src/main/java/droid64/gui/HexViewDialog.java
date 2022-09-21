package droid64.gui;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Date;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToggleButton;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

import droid64.DroiD64;
import droid64.d64.CbmException;
import droid64.d64.ProgramParser;
import droid64.d64.Utility;

/**
 * Class used to show a hex dump of a byte array.
 *
 * @author Henrik
 */
public class HexViewDialog extends JDialog {

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

	private HexTableModel model;
	private static final String ASM_MODE = Settings.getMessage(Resources.DROID64_HEXVIEW_ASMMODE);
	private static final String HEX_MODE = Settings.getMessage(Resources.DROID64_HEXVIEW_HEXMODE);
	private MainPanel mainPanel;
	private JButton modeButton;
	private JTextArea asmTextPane;
	private JPanel cards;
	private JTable table;

	/**
	 * Constructor
	 * @param topText String with window title
	 * @param fileName String with the file name to show.
	 * @param data a byte array with the data to show
	 * @param length the length of data to show
	 * @param mainPanel main panel
	 * @param readLoadAddr read load address
	 */
	public HexViewDialog (String topText, final String fileName, final byte[] data, final int length, MainPanel mainPanel, final boolean readLoadAddr) {
		setTitle(topText);
		setModal(true);
		this.mainPanel = mainPanel;
		setLayout(new BorderLayout());

		// Setup title
		add(new JLabel(fileName), BorderLayout.NORTH);

		// Setup hex panel
		JTable hexTable = drawHexPanel(data, length);
		// Setup assembler panel
		JPanel disasmPanel = new JPanel(new BorderLayout());
		asmTextPane = new JTextArea();
		asmTextPane.setText("");
		asmTextPane.setEditable(false);
		asmTextPane.setFont(getTableFont(false));

		disasmPanel.add(asmTextPane, BorderLayout.CENTER);
		JScrollPane asmScrollPane = new JScrollPane(disasmPanel);

		// Setup cards
		cards = new JPanel(new CardLayout());
		cards.add(new JScrollPane(hexTable), HEX_MODE);
		cards.add(asmScrollPane, ASM_MODE);

		add(cards, BorderLayout.CENTER);
		add(drawButtons(data, length, readLoadAddr, fileName), BorderLayout.SOUTH);
		setSize(hexTable.getWidth(), hexTable.getHeight());

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation( (int)((dim.width - getSize().getWidth()) / 3),	(int)((dim.height - getSize().getHeight()) / 3)	);

		pack();
		setVisible(mainPanel != null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private JPanel drawButtons(final byte[] data, final int length, final boolean readLoadAddr, final String fileName) {
		// Setup buttons
		modeButton = new JButton(ASM_MODE);
		modeButton.addActionListener(ae -> switchHexAsmMode(data, length, readLoadAddr));

		final JButton okButton = new JButton(Settings.getMessage(Resources.DROID64_HEXVIEW_CLOSE));
		okButton.setMnemonic('o');
		okButton.setToolTipText(Settings.getMessage(Resources.DROID64_HEXVIEW_CLOSE_TOOLTIP));
		okButton.addActionListener(ae -> dispose());

		final JToggleButton c64ModeButton = new JToggleButton("C64 mode");
		c64ModeButton.addActionListener(ae -> {
			table.setFont(getTableFont(c64ModeButton.isSelected()));
			asmTextPane.setFont(getTableFont(c64ModeButton.isSelected()));
		});

		final JButton printButton = new JButton(Settings.getMessage(Resources.DROID64_HEXVIEW_PRINT));
		printButton.setMnemonic('p');
		printButton.addActionListener(ae -> print(data, c64ModeButton.isSelected(), fileName, ASM_MODE.equals(modeButton.getText()), asmTextPane.getText()));

		final JButton saveButton = new JButton(Settings.getMessage(Resources.DROID64_HEXVIEW_SAVETEXT));
		saveButton.setMnemonic('s');
		saveButton.addActionListener(ae-> saveText(data, fileName, ASM_MODE.equals(modeButton.getText()), asmTextPane.getText()));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(modeButton);
		buttonPanel.add(c64ModeButton);
		buttonPanel.add(printButton);
		buttonPanel.add(saveButton);
		buttonPanel.add(okButton);
		return buttonPanel;
	}

	private void switchHexAsmMode(byte[] data, int length, boolean readLoadAddr) {
		String mode = modeButton.getText();
		if (ASM_MODE.equals(mode)) {
			modeButton.setText(HEX_MODE);
			if ("".equals(asmTextPane.getText())) {
				String code = ProgramParser.parse(data, length, readLoadAddr);
				asmTextPane.setText(code);
				asmTextPane.setCaretPosition(0);
			}
		} else {
			modeButton.setText(ASM_MODE);
		}
		CardLayout cl = (CardLayout) cards.getLayout();
		cl.show(cards, mode);
	}

	private void saveText(byte[] data, String fileName, boolean hexMode, String asm) {
		String[] ext = hexMode ? new String[] {".txt", ".asm"} : new String[] {".asm", ".txt"};
		String title = hexMode ? Settings.getMessage(Resources.DROID64_HEXVIEW_SAVEHEX) : Settings.getMessage(Resources.DROID64_HEXVIEW_SAVEASM);
		String fname = FileDialogHelper.openTextFileDialog(title, null, fileName, true, ext);
		if (fname != null) {
			String outString = hexMode ? Utility.hexDump(data) : asm;
			if (!outString.isEmpty()) {
				writeToFile(new File(fname), outString);
			}
		}
	}

	private void writeToFile(File saveFile, String outString) {
		try (PrintWriter out = new PrintWriter(saveFile)) {
			out.println( outString );
		} catch (FileNotFoundException e) {	//NOSONAR
			mainPanel.appendConsole("Error: failed to write to file "+saveFile.getName());
		}
	}

	/**
	 * Setup the table used to display the data
	 * @param data a byte[] with the data to show.
	 * @param length the length of the data to show.
	 * @return the created  JTable instance
	 */
	private JTable drawHexPanel(byte[] data, int length) {
		model = new HexTableModel(data, length);
		final CustomCellRenderer renderer = new CustomCellRenderer();
		table  = new JTable(model) {
			private static final long serialVersionUID = 1L;
			@Override
			public TableCellRenderer getCellRenderer(int row, int column) {
				return renderer;
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		table.setFont(getTableFont(false));

		int chrWdt = Math.max(table.getFontMetrics(getTableFont(true)).stringWidth("w"), table.getFontMetrics(getTableFont(false)).stringWidth("w"));
		int colWdt = chrWdt * 3;
		int addrWdt = chrWdt * 10;
		int ascWdt = chrWdt * (model.getColumnCount() - 2 +2);
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
		return table;
	}

	private Font getTableFont(boolean useCbmFont) {
		try {
			return useCbmFont ? Settings.getCommodoreFont() : new Font("Courier", Font.PLAIN, Settings.getFontSize());
		} catch (CbmException e) {
			mainPanel.appendConsole("Failed to set Commodore font. Using default font.\n"+e);
			return new JPanel().getFont();
		}
	}

	private void print(final byte[] data, boolean useCbmfont, final String title, boolean hexmode, String asmText) {
		PrinterJob job = PrinterJob.getPrinterJob();
		if (hexmode) {
			job.setPageable(new PrintPageable(data, useCbmfont, title, mainPanel));
		} else {
			String header = "; Created by " + DroiD64.PROGNAME + " version " + DroiD64.VERSION + "\n" + "; "
					+ new Date() + "\n \n";
			if (useCbmfont) {
				header = header.toUpperCase();
			}
			job.setPageable(new PrintPageable(header + asmText, "; " + title, useCbmfont, true, mainPanel));
		}
		if (job.printDialog()) {
			try {
				job.print();
			} catch (PrinterException e) {
				mainPanel.appendConsole("Failed to print: " + e);
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
