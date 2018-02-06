package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

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
	private static final String[] COLHEADS = {
		"Track",
		"1", "2", "3", "4", "5", "6","7","8","9","10",
		"11","12","13","14","15","16","17","18","19","20",
		"21" };

	/**
	 * Constructor
	 * @param topText
	 * @param diskName
	 * @param bamEntry_
	 */
	public BAMFrame(String topText, String diskName, String[][] bamEntry_) {
		super(topText);
		final String[][] bamEntry = bamEntry_;
		
		//Table Column-Width		
		DefaultTableColumnModel cm = new DefaultTableColumnModel();
		for (int i = 0; i < COLHEADS.length; ++i) {
			TableColumn col = new TableColumn(i, i == 0 ? 50 : 10);
			col.setHeaderValue(COLHEADS[i]);
			cm.addColumn(col);
		}

		//Show table model 
		TableModel tm = new AbstractTableModel() {
			private static final long serialVersionUID = 1L;
			public int getRowCount() {
				return bamEntry.length;
			}
			public int getColumnCount() {
				return bamEntry[0].length;
			}
			public Object getValueAt(int row, int column) {
				return bamEntry[row][column];
			}
		};

		JTable bamTable = new JTable(tm, cm);
		bamTable.setToolTipText("the BAM of the disk");
		//		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		//		table.setAlignmentX(JTable.RIGHT_ALIGNMENT);
		bamTable.setGridColor(new Color(230, 230, 255));
		bamTable.setDefaultRenderer( Object.class, new ColoredTableCellRenderer() );

		JPanel buttonPanel = new JPanel();
		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave BAM view.");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == okButton ) {
					dispose();
				}
			}
		});
		buttonPanel.add(okButton);
		
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JLabel diskNameLabel = new JLabel(diskName);
		cp.add(diskNameLabel, BorderLayout.NORTH);		
		cp.add(new JScrollPane(bamTable), BorderLayout.CENTER);		
		cp.add(buttonPanel, BorderLayout.SOUTH);		

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((dim.width - getSize().getWidth()) / 3),
				(int)((dim.height - getSize().getHeight()) / 3)
				);
		// setLocation(300,200);
		pack();
		setVisible(true);
		this.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

}
