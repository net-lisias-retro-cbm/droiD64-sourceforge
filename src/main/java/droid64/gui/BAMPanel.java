package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.border.EtchedBorder;
import javax.swing.border.TitledBorder;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableColumnModel;

import droid64.DroiD64;
import droid64.d64.CbmBam;
import droid64.d64.CbmException;
import droid64.d64.DiskImage;
import droid64.d64.Utility;

/**
 * <pre style='font-family:sans-serif;'>
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
 * </pre>
 *
 * @author wolf
 */
public class BAMPanel extends JPanel {

	private static final long serialVersionUID = 1L;

	private final JButton saveButton = new JButton(Settings.getMessage(Resources.DROID64_BAM_SAVE));
	private final JLabel diskNameLabel = new JLabel();
	private final BamTableModel tableModel = new BamTableModel();
	private final JTable bamTable = new JTable(tableModel);
	private final JRadioButton viewModeButton = createRadioButton(Resources.DROID64_BAM_VIEW,
			Resources.DROID64_BAM_VIEW_TOOLTIP);
	private final JRadioButton editModeButton = createRadioButton(Resources.DROID64_BAM_EDIT,
			Resources.DROID64_BAM_EDIT_TOOLTIP);
	private final JButton closeButton = new JButton("Close");
	private final JPopupMenu popMenu = new JPopupMenu();
	private final JMenuItem allocateSelected = GuiHelper.addMenuItem(popMenu, Resources.DROID64_BAM_ALLOCATE, 'a', event -> setSelectedAllocation(true));
	private final JMenuItem freeSelected = GuiHelper.addMenuItem(popMenu, Resources.DROID64_BAM_FREE, 'f', event -> setSelectedAllocation(false));
	private final MainPanel mainPanel;
	private DiskImage diskImage;
	private String diskFileName;

	/**
	 * Constructor
	 *
	 * @param mainPanel mainPanel
	 */
	public BAMPanel(final MainPanel mainPanel) {
		this.mainPanel = mainPanel;
		initGUI();
	}

	private void initGUI() {
		saveButton.setToolTipText(Settings.getMessage(Resources.DROID64_BAM_SAVE_TOOLTIP));
		saveButton.setMnemonic('s');
		saveButton.setEnabled(false);
		saveButton.addActionListener(ae -> {
			if (diskImage.writeImage(diskFileName)) {
				saveButton.setEnabled(false);
			}
			diskImage.readBAM();
		});

		viewModeButton.setMnemonic('v');
		editModeButton.setMnemonic('e');
		viewModeButton.addActionListener(e -> editMode(editModeButton.isSelected()));
		editModeButton.addActionListener(e -> editMode(editModeButton.isSelected()));
		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(viewModeButton);
		modeGroup.add(editModeButton);
		viewModeButton.setSelected(true);

		bamTable.setGridColor(new Color(230, 230, 255));
		bamTable.setDefaultRenderer(Object.class, new ColoredTableCellRenderer());
		bamTable.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		bamTable.setColumnSelectionAllowed(true);
		bamTable.setRowSelectionAllowed(true);
		bamTable.getTableHeader().setReorderingAllowed(false);
		bamTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				blockClicked(me, editModeButton.isSelected(), bamTable, mainPanel);
			}
		});

		bamTable.setComponentPopupMenu(popMenu);
		editMode(false);

		TitledBorder border = BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED),
				Settings.getMessage(Resources.DROID64_BAM_MODE));

		JPanel modePanel = new JPanel();
		modePanel.add(viewModeButton);
		modePanel.add(editModeButton);
		modePanel.setBorder(border);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(modePanel);
		buttonPanel.add(saveButton);
		buttonPanel.add(closeButton);

		setLayout(new BorderLayout());
		add(diskNameLabel, BorderLayout.NORTH);
		add(new JScrollPane(bamTable), BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);
		addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(this)));
	}

	private void editMode(boolean editable) {
		allocateSelected.setVisible(editable);
		freeSelected.setVisible(editable);
		bamTable.setSelectionMode(editable ? ListSelectionModel.SINGLE_INTERVAL_SELECTION : ListSelectionModel.SINGLE_SELECTION);
	}

	private void setSelectedAllocation(boolean used) {
		int[] rows = bamTable.getSelectedRows();
		if (rows.length > 0) {
			for (int row : rows) {
				for (int col : bamTable.getSelectedColumns()) {
					setUsed(row, col, used);
				}
			}
			saveButton.setEnabled(true);
			bamTable.invalidate();
			bamTable.repaint();
		}
	}

	/**
	 * @param diskName     name of disk
	 * @param bamEntry     bitmap entries
	 * @param diskFileName name of disk image
	 * @param diskImage    the disk image
	 * @param writable     if writable
	 */
	public void show(String diskName, final String[][] bamEntry, final String diskFileName, final DiskImage diskImage,
			boolean writable) {
		this.diskFileName = diskFileName;
		this.diskImage = diskImage;
		this.tableModel.setBamEntry(bamEntry);
		this.editModeButton.setEnabled(writable);
		this.viewModeButton.setEnabled(writable);

		diskNameLabel.setText(diskName);
		tableModel.fireTableStructureChanged();

		TableColumnModel columnModel = bamTable.getColumnModel();
		for (int i = 0; i < columnModel.getColumnCount(); i++) {
			columnModel.getColumn(i).setMinWidth(i == 0 ? 50 : 10);
		}

		GuiHelper.setPreferredSize(this, 2, 2);

		final JDialog dialog = new JDialog(mainPanel.getParent(), DroiD64.PROGNAME + " - BAM of this disk image", true);
		dialog.setContentPane(this);
		dialog.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

		closeButton.addActionListener(e -> dialog.dispose());

		dialog.pack();
		dialog.setLocationRelativeTo(mainPanel.getParent());
		dialog.setVisible(true);
	}

	private JRadioButton createRadioButton(String propKey, String toolPropKey) {
		JRadioButton button = new JRadioButton(Settings.getMessage(propKey));
		button.setToolTipText(Settings.getMessage(toolPropKey));
		button.setMargin(new Insets(1, 4, 1, 4));
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
			} else if (me.getClickCount() == 2) {
				viewBlock(row, col, mainPanel);
			}
		}
	}

	private void toggleBlock(int row, int col, final JTable bamTable) {
		setUsed(row, col, tableModel.equals(CbmBam.FREE, row, col));
		saveButton.setEnabled(true);
		bamTable.invalidate();
		bamTable.repaint();
	}

	private void setUsed(int row, int col, boolean used) {
		if (used) {
			diskImage.markSectorUsed(row + 1, col - 1);
			tableModel.setValueAt(CbmBam.USED, row, col);
		} else {
			diskImage.markSectorFree(row + 1, col - 1);
			tableModel.setValueAt(CbmBam.FREE, row, col);
		}
	}


	private void viewBlock(int row, int col, MainPanel mainPanel) {
		try {
			int track = row + diskImage.getFirstTrack();
			int sector = col - 1 + diskImage.getFirstSector();
			new HexViewPanel(DroiD64.PROGNAME + " - Block view", mainPanel, track, sector, diskImage).showDialog();
		} catch (CbmException e) { // NOSONAR
			mainPanel.appendConsole("Failed to get data.\n" + e.getMessage());
		}
	}

	private class BamTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		private String[][] bamEntry = null;

		@Override
		public String getColumnName(int column) {
			return column > 0 ? diskImage.getSectorTitle(column) : Settings.getMessage(Resources.DROID64_BAM_TRACK);
		}

		@Override
		public Class<?> getColumnClass(int columnIndex) {
			return String.class;
		}

		public void setBamEntry(String[][] bamEntry) {
			this.bamEntry = bamEntry;
		}

		public boolean equals(String expected, int row, int col) {
			if (bamEntry != null && row < bamEntry.length && col < bamEntry[0].length) {
				return expected.equals(bamEntry[row][col]);
			}
			return false;
		}

		@Override
		public int getRowCount() {
			return bamEntry != null ? bamEntry.length : 0;
		}

		@Override
		public int getColumnCount() {
			return bamEntry != null ? bamEntry[0].length : 0;
		}

		@Override
		public Object getValueAt(int row, int column) {
			if (bamEntry == null) {
				return Utility.EMPTY;
			} else if (row < bamEntry.length && column < bamEntry[0].length) {
				return bamEntry[row][column];
			} else {
				return Utility.EMPTY;
			}
		}

		public void setValueAt(String value, int row, int column) {
			if (bamEntry != null && row < bamEntry.length && column < bamEntry[0].length) {
				bamEntry[row][column] = value;
			}
		}
	}
}
