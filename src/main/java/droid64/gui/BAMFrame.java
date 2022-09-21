package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.border.EtchedBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import droid64.DroiD64;
import droid64.d64.CbmBam;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;

/**<pre style='font-family:sans-serif;'>
 * Created on 25.06.2004
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
 *</pre>
 * @author wolf
 */
public class BAMFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private DiskImage diskImage;
	private String[][] bamEntry;
	private JButton saveButton;

	/**
	 * Constructor
	 * @param topText title
	 * @param diskName name of disk
	 * @param bamEntry bitmap entries
	 * @param diskFileName name of disk image
	 * @param diskImage the disk image
	 * @param writable if writable
	 * @param mainPanel mainPanel
	 */
	public BAMFrame(String topText, String diskName, final String[][] bamEntry, final String diskFileName, final DiskImage diskImage, boolean writable, final MainPanel mainPanel) {
		super(topText);
		this.diskImage = diskImage;
		this.bamEntry = bamEntry;
		saveButton = new JButton(Settings.getMessage(Resources.DROID64_BAM_SAVE));
		saveButton.setToolTipText(Settings.getMessage(Resources.DROID64_BAM_SAVE_TOOLTIP));
		saveButton.setEnabled(false);
		saveButton.addActionListener(ae -> {
			if (diskImage.writeImage(diskFileName)) {
				saveButton.setEnabled(false);
			}
			diskImage.readBAM();
		});

		final JRadioButton viewModeButton = createRadioButton(Resources.DROID64_BAM_VIEW, Resources.DROID64_BAM_VIEW_TOOLTIP, writable);
		final JRadioButton editModeButton = createRadioButton(Resources.DROID64_BAM_EDIT, Resources.DROID64_BAM_EDIT_TOOLTIP, writable);
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(viewModeButton);
		modeGroup.add(editModeButton);
		viewModeButton.setSelected(true);

		final JButton okButton = new JButton(Settings.getMessage(Resources.DROID64_BAM_CLOSE));
		okButton.setMnemonic('o');
		okButton.addActionListener(ae->dispose());

		//Table Column-Width
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		for (int i = 0; i <= diskImage.getMaxSectorCount(); ++i) {
			TableColumn col = new TableColumn(i, i == 0 ? 40 : 10);
			col.setHeaderValue( i > 0 ? Integer.toString(i - 1) : Settings.getMessage(Resources.DROID64_BAM_TRACK));
			columnModel.addColumn(col);
		}

		//Show table model
		TableModel tableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;
			@Override
			public int getRowCount() {
				return bamEntry.length;
			}
			@Override
			public int getColumnCount() {
				return bamEntry[0].length;
			}
			@Override
			public Object getValueAt(int row, int column) {
				if (row < bamEntry.length && column < bamEntry[0].length) {
					return bamEntry[row][column];
				} else {
					return "";
				}
			}
		};

		ColoredTableCellRenderer cellRenderer = new ColoredTableCellRenderer();
		final JTable bamTable = new JTable(tableModel, columnModel);
		bamTable.setGridColor(new Color(230, 230, 255));
		bamTable.setDefaultRenderer( Object.class, cellRenderer );
		bamTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				blockClicked(me, editModeButton.isSelected(), bamTable, mainPanel);
			}
		});

		JPanel modePanel = new JPanel();
		modePanel.add(viewModeButton);
		modePanel.add(editModeButton);
		modePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), Settings.getMessage(Resources.DROID64_BAM_MODE)));

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(modePanel);
		buttonPanel.add(saveButton);
		buttonPanel.add(okButton);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(new JLabel(diskName), BorderLayout.NORTH);
		cp.add(new JScrollPane(bamTable), BorderLayout.CENTER);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((screenSize.width - getSize().getWidth()) / 3),
				(int)((screenSize.height - getSize().getHeight()) / 3));
		pack();
		setSize(screenSize.width/4,screenSize.height/2);
		setVisible(mainPanel != null);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private JRadioButton createRadioButton(String propKey, String toolPropKey, boolean writable) {
		JRadioButton button = new JRadioButton(Settings.getMessage(propKey));
		button.setToolTipText(Settings.getMessage(toolPropKey));
		button.setMargin(new Insets(1, 4, 1, 4));
		button.setEnabled(writable);
		return button;
	}

	private void blockClicked(MouseEvent me, boolean editMode, final JTable bamTable, final MainPanel mainPanel) {
		JTable table = (JTable) me.getSource();
		Point pt = me.getPoint();
		int col = table.columnAtPoint(pt);
		int row = table.rowAtPoint(pt);
		if (row >= 0 && col > 0) {
			if (editMode) {
				toggleBlock(row, col, bamTable);
			} else if (me.getClickCount() == 2 ) {
				viewBlock(row, col, mainPanel);
			}
		}
	}

	private void toggleBlock(int row, int col, final JTable bamTable) {
		if (CbmBam.FREE.equals(bamEntry[row][col])) {
			diskImage.markSectorUsed(row + 1, col - 1);
			bamEntry[row][col] = CbmBam.USED;
		} else if (CbmBam.USED.equals(bamEntry[row][col])) {
			diskImage.markSectorFree(row + 1, col - 1);
			bamEntry[row][col] = CbmBam.FREE;
		}
		saveButton.setEnabled(true);
		bamTable.invalidate();
		bamTable.repaint();
	}

	private void viewBlock(int row, int col, MainPanel mainPanel) {
		try {
			byte[] data = diskImage.getBlock(row+1, col-1);
			if (data != null && data.length > 0) {
				String info =
						Settings.getMessage(Resources.DROID64_BAM_TRACK) + " " + (row + 1) + " " +
								Settings.getMessage(Resources.DROID64_BAM_SECTOR) + " " + (col - 1) +
								" (0x" + Integer.toHexString(diskImage.getSectorOffset(row + 1, col - 1))+")";
				new HexViewDialog(DroiD64.PROGNAME+" - Block view", info, data, data.length, mainPanel, false);
			}
		} catch (CbmException e) {	//NOSONAR
			mainPanel.appendConsole("Failed to get data.\n"+e.getMessage());
		}
	}

}
