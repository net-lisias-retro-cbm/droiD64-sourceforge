package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

	/**
	 * Constructor
	 * @param topText
	 * @param diskName
	 * @param bamEntry_
	 */
	public BAMFrame(String topText, String diskName, String[][] bamEntry, final String diskFileName, final DiskImage diskImage) {
		super(topText);
		final JButton saveButton = new JButton("Save");
		saveButton.setToolTipText("Save changed BAM to disk.");
		saveButton.setEnabled(false);
		saveButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == saveButton ) {
					if (diskImage.writeImage(diskFileName)) {
						saveButton.setEnabled(false);
					}
				}
			}
		});
		
		final JRadioButton viewModeButton = new JRadioButton("View");
		viewModeButton.setToolTipText("Double block click to view hex dump.");
		final JRadioButton editModeButton = new JRadioButton("Edit");
		editModeButton.setToolTipText("Click blocks to toggle used/free.");
		viewModeButton.setMargin(new Insets(1, 4, 1, 4));
		editModeButton.setMargin(new Insets(1, 4, 1, 4));

		ButtonGroup modeGroup = new ButtonGroup();
		modeGroup.add(viewModeButton);
		modeGroup.add(editModeButton);
		viewModeButton.setSelected(true);
		
		final JButton okButton = new JButton("Close");
		okButton.setMnemonic('o');
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == okButton ) {
					dispose();
				}
			}
		});

		//Table Column-Width
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		for (int i = 0; i <= diskImage.getMaxSectorCount(); ++i) {
			TableColumn col = new TableColumn(i, i == 0 ? 40 : 10);
			col.setHeaderValue( i > 0 ? Integer.toString(i - 1) : "Track");
			columnModel.addColumn(col);
		}

		//Show table model 
		TableModel tableModel = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;
			private String[][] bamEntry;
			public int getRowCount() {
				return bamEntry.length;
			}
			public int getColumnCount() {
				return bamEntry[0].length;
			}
			public Object getValueAt(int row, int column) {
				if (row < bamEntry.length && column < bamEntry[0].length) {
					return bamEntry[row][column];
				} else {
					return "";
				}
			}
			private AbstractTableModel init(String[][] bamEntry) {
				this.bamEntry = bamEntry;
				return this;
			}
		}.init(bamEntry);

		ColoredTableCellRenderer cellRenderer = new ColoredTableCellRenderer();
		final JTable bamTable = new JTable(tableModel, columnModel);
		bamTable.setGridColor(new Color(230, 230, 255));
		bamTable.setDefaultRenderer( Object.class, cellRenderer );
		bamTable.addMouseListener(new MouseAdapter() {
			private String[][] bamEntry;
			public void mousePressed(MouseEvent me) {
				JTable table = (JTable) me.getSource();
				Point p = me.getPoint();
				int col = table.columnAtPoint(p);
				int row = table.rowAtPoint(p);
				if (row >= 0 && col > 0) {
					if (editModeButton.isSelected()) {
						if (CbmBam.FREE.equals(bamEntry[row][col])) {
							diskImage.markSectorUsed(row + 1, col - 1);
							this.bamEntry[row][col] = CbmBam.USED;
						} else if (CbmBam.USED.equals(bamEntry[row][col])) {
							diskImage.markSectorFree(row + 1, col - 1);
							this.bamEntry[row][col] = CbmBam.FREE;
						}
						saveButton.setEnabled(true);
						bamTable.invalidate();
						bamTable.repaint();
					} else {
						int clickCount = me.getClickCount();
						if (clickCount == 2 ) {
							try {
								byte[] data = diskImage.getBlock(row+1, col-1);
								if (data != null) {
									String info = "Track  "+(row+1)+ " Sector "+(col-1) + " (0x" + Integer.toHexString(diskImage.getSectorOffset(row+1,col-1))+")";
									new HexViewFrame(DroiD64.PROGNAME+" v"+DroiD64.VERSION+" - Block view", info, data, data.length);
								}
							} catch (CbmException e) {}
						}
					}
				}
			}
			private MouseAdapter init(String[][] bamEntry) {
				this.bamEntry = bamEntry;
				return this;
			}
		}.init(bamEntry));
		
		JPanel modePanel = new JPanel();
		modePanel.add(viewModeButton);
		modePanel.add(editModeButton);
		modePanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED), "Mode"));

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
				(int)((screenSize.height - getSize().getHeight()) / 3)
				);
		pack();
		setSize(screenSize.width/4,screenSize.height/2);
		setVisible(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
