package droid64.gui;

import java.awt.Color;
import java.awt.Component;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.SwingConstants;
import javax.swing.table.DefaultTableCellRenderer;

public class ListTableCellRenderer extends DefaultTableCellRenderer {

	private static final long serialVersionUID = 1L;
	
	private EntryTableModel model;
	public ListTableCellRenderer(EntryTableModel model) {
		this.model = model;
	}
		
	public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
		super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
		JLabel label = new JLabel(value != null ? value.toString() : "");
		label.setOpaque(true);
		label.setFont(table.getFont());
		if (model.getMode() == EntryTableModel.MODE_LOCAL) {
			if (model.isFile(row, column)) {
				if (model.isImageFile(row, column)) {
					label.setForeground(Color.RED);
					label.setBackground(Settings.getDirLocalColorBg());	
				} else {
					label.setForeground(Settings.getDirLocalColorFg());
					label.setBackground(Settings.getDirLocalColorBg());
				}
			} else {
				label.setForeground(Color.BLUE);
				label.setBackground(Settings.getDirLocalColorBg());				
			}
			if (column == 3) {
				label.setHorizontalAlignment(SwingConstants.RIGHT);
			}
		} else {
			label.setForeground(table.getForeground());
			label.setBackground(table.getBackground());
		}
		
		if (isSelected) {
			Color tmp = label.getForeground();
			label.setForeground(label.getBackground());
			label.setBackground(tmp);
		}
		
		return label;
	}	
	
}
