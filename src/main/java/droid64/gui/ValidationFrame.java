package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import droid64.d64.Utility;
import droid64.d64.ValidationError;

public class ValidationFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	private static final String HEADER_TRACK_SECTOR = "\nTrack/Sector\n------------\n";
	private static final String HEADER_TRACK_SECTOR_FILE = "\nTrack/Sector\t File\n------------\t ----------------\n";

	private Map<Integer,List<ValidationError>> errorMap = new HashMap<>();
	private JCheckBox[] boxes;
	private List<Integer> keys;
	private JButton repairButton;

	public ValidationFrame(List<ValidationError> errors, final DiskPanel diskPanel) {
		super.setTitle("Validation errors");
		parseErrors(errors);

		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		TableColumn col1 = new TableColumn(0, 10);
		col1.setHeaderValue(Utility.EMPTY);
		columnModel.addColumn(col1);
		TableColumn col2 = new TableColumn(1, 30);
		col2.setHeaderValue("Error code");
		columnModel.addColumn(col2);
		TableColumn col3 = new TableColumn(2, 30);
		col3.setHeaderValue("Error count");
		columnModel.addColumn(col3);
		TableColumn col4 = new TableColumn(3, 80);
		col4.setHeaderValue("Error text");
		columnModel.addColumn(col4);

		TableModel tableModel = new ValidationTableModel();

		final JTextArea textArea = new JTextArea();
		textArea.setLineWrap(true);
		textArea.setEditable(false);

		final JTable errorTable = new JTable(tableModel, columnModel);
		errorTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		errorTable.addMouseListener(new MouseAdapter() {
			@Override
			public void mousePressed(MouseEvent me) {
				int row = errorTable.rowAtPoint(me.getPoint());
				if (row >= 0) {
					textArea.setText(getErrorDataString(keys.get(row), errorMap));
				}
			}
		});

		if (errorMap.isEmpty()) {
			textArea.setText("No validation errors found.");
		}

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave Validation errors.");
		okButton.addActionListener(ae -> dispose());

		repairButton = new JButton("Repair");
		repairButton.setToolTipText("Repair selected validation errors.");
		repairButton.setEnabled(!errorMap.isEmpty());
		repairButton.addActionListener(ae -> repair(errorTable, diskPanel, textArea));

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		JSplitPane splitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitPane.setContinuousLayout(true);
		splitPane.setTopComponent(new JScrollPane(errorTable));
		splitPane.setBottomComponent(new JScrollPane(textArea));

		cp.add(splitPane, BorderLayout.CENTER);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(repairButton);

		buttonPanel.add(okButton);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		GuiHelper.setLocation(this,  4, 6);
		pack();
		GuiHelper.setSize(this,  4, 2);
		setVisible(diskPanel != null);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void repair(JTable errorTable, DiskPanel diskPanel, JTextArea textArea) {
		if (!errorMap.isEmpty()) {
			return;
		}
		List<Integer> repairList = new ArrayList<>();
		for (int i=0; i<boxes.length; i++) {
			if (boxes[i].isSelected()) {
				repairList.add(keys.get(i));
			}
		}
		if (!repairList.isEmpty()) {
			List<ValidationError> errList  = diskPanel.repairValidationErrors(repairList);
			parseErrors(errList);
			textArea.setText(Utility.EMPTY);
			repairButton.setEnabled(!errorMap.isEmpty());
			errorTable.invalidate();
			errorTable.repaint();
		}
	}

	private void parseErrors(List<ValidationError> errorList) {
		errorMap.clear();
		if (errorList != null) {
			for (ValidationError error : errorList) {
				Integer code = Integer.valueOf(error.getErrorCode());
				if (!errorMap.containsKey(code)) {
					errorMap.put(code, new ArrayList<>());
				}
				errorMap.get(code).add(error);
			}
		}
		boxes = new JCheckBox[errorMap.size()];
		for (int i=0; i < boxes.length; i++) {
			boxes[i] = new JCheckBox();
		}
		Integer[] keyArr = errorMap.keySet().toArray(new Integer[errorMap.size()]);
		Arrays.sort(keyArr);
		keys = Arrays.asList(keyArr);
	}

	private String getErrorDataString(Integer key, Map<Integer,List<ValidationError>> errorMap) {
		if (key == null) {
			return null;
		}
		List<ValidationError> errorList = errorMap.get(key);
		if (errorList == null) {
			return null;
		}
		boolean hasFile = hasFile(key);
		StringBuilder buf = new StringBuilder();
		buf.append(ValidationError.getErrorText(key));
		buf.append(hasFile ? HEADER_TRACK_SECTOR_FILE : HEADER_TRACK_SECTOR);
		int i=0;
		for (ValidationError error : errorList) {
			if (hasFile) {
				buf.append(error.getTrack()).append('/').append(error.getSector()).append("\t ").append(error.getFileName()).append('\n');
			} else {
				buf.append(error.getTrack()).append('/').append(error.getSector()).append( (i++ & 7) == 7 ? "\n" : "\t ");
			}
		}
		return buf.toString();
	}

	private boolean hasFile(Integer key) {
		switch (key) {
		case ValidationError.ERROR_PARTITIONS_UNSUPPORTED:
		case ValidationError.ERROR_FILE_SECTOR_OUTSIDE_IMAGE:
		case ValidationError.ERROR_FILE_SECTOR_ALREADY_SEEN:
		case ValidationError.ERROR_FILE_SECTOR_ALREADY_USED:
		case ValidationError.ERROR_FILE_SECTOR_ALREADY_FREE:
			return true;
		default:
			return false;
		}
	}

	class ValidationTableModel extends AbstractTableModel {
		private static final long serialVersionUID = 1L;

		@Override
		public int getRowCount() {
			return errorMap.size();
		}

		@Override
		public int getColumnCount() {
			return 4;
		}

		@Override
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:  return Boolean.valueOf(boxes[row].isSelected());
			case 1:  return keys.get(row).toString();
			case 2:  return Integer.toString(errorMap.get(keys.get(row)).size());
			case 3:  return ValidationError.getErrorText(keys.get(row));
			default: return Utility.EMPTY;
			}
		}

		@Override
		public void setValueAt(Object value, int row, int column) {
			if (value instanceof Boolean && column == 0 && row < boxes.length && boxes[row] != null) {
				boxes[row].setSelected((Boolean)value);
			}
		}

		@Override
		public boolean isCellEditable(int row, int column) {
			return column == 0;
		}

		@Override
		public Class<?> getColumnClass(int column) {
			if (column == 0) {
				return Boolean.class;
			} else {
				return String.class;
			}
		}
	}

}
