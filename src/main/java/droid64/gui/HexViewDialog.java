package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.print.PrinterException;
import java.awt.print.PrinterJob;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.ToolTipManager;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;

/**
 * Class used to show a hex dump of a byte array.
 * 
 * @author Henrik
 */
public class HexViewDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private HexTableModel model;
	
	/** 
	 * Constructor
	 * @param topText String with window title
	 * @param fileName String with the file name to show.
	 * @param data a byte array with the data to show
	 * @param length the length of data to show
	 */
	public HexViewDialog (String topText, final String fileName, final byte[] data, int length) {
		setTitle(topText);
		setModal(true);
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JLabel fileNameLabel = new JLabel(fileName);
		cp.add(fileNameLabel, BorderLayout.NORTH);
		
		JTable hexTable = drawHexPanel(data, length);
		JScrollPane hexScollPane = new JScrollPane(hexTable);
		cp.add(hexScollPane, BorderLayout.CENTER);

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave hex view.");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == okButton ) {
						dispose();
				}
			}
		});
		
		final JButton printButton = new JButton("Print");
		printButton.setMnemonic('p');
		printButton.setToolTipText("Print");
		printButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == printButton ) {
					print(data, fileName);
				}
			}
		});
		
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(printButton);
		buttonPanel.add(okButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);
		
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation( (int)((dim.width - getSize().getWidth()) / 3),	(int)((dim.height - getSize().getHeight()) / 3)	);
		
		cp.setSize(hexTable.getWidth(), hexTable.getHeight());

		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
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
		JTable table  = new JTable(model) {
			private static final long serialVersionUID = 1L;
			public TableCellRenderer getCellRenderer(int row, int column) {
				return renderer;
			}
		};
		table.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
		table.setShowGrid(true);
		table.setFont(new Font("Monospaced", Font.PLAIN, table.getFont().getSize()));
		FontMetrics fontMetrics= table.getFontMetrics(table.getFont());
		int chrWdt = fontMetrics.stringWidth("w");		
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
		for (int i=0; i<Math.min((model.getColumnCount() - 2), table.getRowCount()); i++) {
			hgt += table.getRowHeight(i);
		}		
		ToolTipManager.sharedInstance().unregisterComponent(table);
		ToolTipManager.sharedInstance().unregisterComponent(table.getTableHeader());
		table.setPreferredScrollableViewportSize(new Dimension(wdt, hgt));
		table.setSize(wdt, hgt);
		return table;
	}

	private void print(final byte[] data, final String title) {
		PrinterJob job = PrinterJob.getPrinterJob();
		job.setPageable(new PrintPageable(data, title));
		boolean doPrint = job.printDialog();
		if (doPrint) {
		    try {
		        job.print();
		    } catch (PrinterException e) {
		    	e.printStackTrace();
		    }
		}
	}

	/** Class to handle colors of the table cells */
	private class CustomCellRenderer extends DefaultTableCellRenderer {
		private static final long serialVersionUID = 1L;
		private final Color ADDR_SELECTED_FOREGROUND   = new Color(  0,  0,  0);
		private final Color ADDR_SELECTED_BACKGROUND   = new Color(100,100,100);
		private final Color ASCII_SELECTED_FOREGROUND = new Color(  0,  0,  0);
		private final Color ASCII_SELECTED_BACKGROUND = new Color(100,100,100);
		private final Color HEX_SELECTED_FOREGROUND   = new Color(  0,  0,  0);
		private final Color HEX_SELECTED_BACKGROUND   = new Color(232,232,232);
		private final Color ADDR_NORMAL_FOREGROUND   = new Color(  0,  0,  0);
		private final Color ADDR_NORMAL_BACKGROUND   = new Color(200,200,200);
		private final Color ASCII_NORMAL_FOREGROUND  = new Color(  0,  0,  0);
		private final Color ASCII_NORMAL_BACKGROUND  = new Color(200,200,200);
		private final Color HEX_NORMAL_ASC_FOREGROUND = new Color(  0,  0,  0);
		private final Color HEX_NORMAL_HEX_FOREGROUND = new Color(  0,  0, 80);
		private final Color HEX_NORMAL_BACKGROUND     = new Color(255,255,255);
		/** {inheritDoc} */
		public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
			Component rendererComp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
			if (column == 0) {
				if (isSelected) {
					rendererComp.setForeground(ADDR_SELECTED_FOREGROUND);
					rendererComp.setBackground(ADDR_SELECTED_BACKGROUND);
				} else {
					rendererComp.setForeground(ADDR_NORMAL_FOREGROUND);
					rendererComp.setBackground(ADDR_NORMAL_BACKGROUND);
				}
			} else if (column == table.getColumnCount() - 1) {
				if (isSelected) {
					rendererComp.setForeground(ASCII_SELECTED_FOREGROUND);
					rendererComp.setBackground(ASCII_SELECTED_BACKGROUND);
				} else {
					rendererComp.setForeground(ASCII_NORMAL_FOREGROUND);
					rendererComp.setBackground(ASCII_NORMAL_BACKGROUND);
				}					
			} else {
				if (isSelected) {
					rendererComp.setForeground(HEX_SELECTED_FOREGROUND);
					rendererComp.setBackground(HEX_SELECTED_BACKGROUND);
				} else {
					Integer v = model.getByteAt(row, column);
					if (v!=null && (v < 0x20 || v>0x7f) ) {
						rendererComp.setForeground(HEX_NORMAL_HEX_FOREGROUND);
					} else {
						rendererComp.setForeground(HEX_NORMAL_ASC_FOREGROUND);
					}
					rendererComp.setBackground(HEX_NORMAL_BACKGROUND);
				}
			}
			return rendererComp;
		}
	}
	
}
