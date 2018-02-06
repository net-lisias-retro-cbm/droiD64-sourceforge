package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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

import droid64.d64.ValidationError;

public class ValidationFrame extends JFrame {

	private static final long serialVersionUID = 1L;
	
	private Map<Integer,List<ValidationError>> errorMap = new HashMap<Integer,List<ValidationError>>();
	private JCheckBox[] boxes;
	private List<Integer> keys;
	private JButton repairButton;
	
	public ValidationFrame(List<ValidationError> errors, final DiskPanel diskPanel) {
		super.setTitle("Validation errors");
		parseErrors(errors);
		
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		TableColumn col1 = new TableColumn(0, 10);
		col1.setHeaderValue("");
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
		errorTable.addMouseListener(new MouseAdapter() {
		    public void mousePressed(MouseEvent me) {
		        JTable table = (JTable) me.getSource();
		        Point p = me.getPoint();
		        int row = table.rowAtPoint(p);
		        if (row >= 0) {
		        	textArea.setText(getErrorDataString(keys.get(row), errorMap));
		        }
		    }
		});
		errorTable.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		
		if (errorMap.isEmpty()) {
			textArea.setText("No validation errors found.");
		}
		
		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Leave Validation errors.");
		okButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == okButton ) {
					dispose();
				}
			}
		});
		
		repairButton = new JButton("Repair");
		repairButton.setToolTipText("Repair selected validation errors.");
		repairButton.setEnabled(!errorMap.isEmpty());
		repairButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) {
				if ( event.getSource() == repairButton && !errorMap.isEmpty()) {
					List<Integer> repairList = new ArrayList<Integer>();
					for (int i=0; i<boxes.length; i++) {
						if (boxes[i].isSelected()) {
							repairList.add(keys.get(i));
						}
					}
					if (!repairList.isEmpty()) {
						List<ValidationError> errList  = diskPanel.repairValidationErrors(repairList);
						parseErrors(errList);
						textArea.setText("");
						repairButton.setEnabled(!errorMap.isEmpty());
						errorTable.invalidate();
						errorTable.repaint();
					}
				}
			}
		});		
		
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
		
		Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((screenSize.width - getSize().getWidth()) / 4),
				(int)((screenSize.height - getSize().getHeight()) / 6)
				);
		pack();
		setSize(screenSize.width/4,screenSize.height/2);
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private void parseErrors(List<ValidationError> errorList) {
		errorMap.clear();
		if (errorList != null) {
			for (ValidationError error : errorList) {
				Integer code = new Integer(error.getErrorCode());
				if (!errorMap.containsKey(code)) {
					errorMap.put(code, new ArrayList<ValidationError>());
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
    	StringBuffer buf = new StringBuffer();
		switch (key) {
		case ValidationError.ERROR_PARTITIONS_UNSUPPORTED:
		case ValidationError.ERROR_FILE_SECTOR_OUTSIDE_IMAGE:
		case ValidationError.ERROR_FILE_SECTOR_ALREADY_SEEN:
		case ValidationError.ERROR_FILE_SECTOR_ALREADY_USED: 
		case ValidationError.ERROR_FILE_SECTOR_ALREADY_FREE:
			buf.append(ValidationError.getErrorText(key)).append("\nTrack/Sector\t File\n------------\t ----------------\n");
        	for (ValidationError error : errorList) {
        		buf.append(error.getTrack()).append("/").append(error.getSector()).append("\t ").append(error.getFileName()).append("\n");
        	}
			break;
		default:
			buf.append(ValidationError.getErrorText(key)).append("\nTrack/Sector\n------------\n");
			int i=0;
        	for (ValidationError error : errorList) {
        		buf.append(error.getTrack()).append("/").append(error.getSector()).append(   (i++ & 7) == 7 ? "\n" : "\t ");
        	}
			break;
		}
		return buf.toString();	
	}
	
	class ValidationTableModel extends AbstractTableModel {
		
		private static final long serialVersionUID = 1L;
		
		public int getRowCount() {
			return errorMap.size();
		}
		
		public int getColumnCount() {
			return 4;
		}
		
		public Object getValueAt(int row, int column) {
			switch (column) {
			case 0:  return new Boolean(boxes[row].isSelected());
			case 1:  return keys.get(row).toString();
			case 2:  return Integer.toString(errorMap.get(keys.get(row)).size());
			case 3:  return ValidationError.getErrorText(keys.get(row));
			default: return "";
			}
		}
		
		public void setValueAt(Object value, int row, int column) {
			if (value instanceof Boolean && column == 0 && row < boxes.length && boxes[row] != null) {
				boxes[row].setSelected((Boolean)value);
			}
		}
		
	    public boolean isCellEditable(int row, int column) {
	        return column == 0;
	    }
	    
	    public Class<?> getColumnClass(int column) {
			switch (column) {
			case 0:  return Boolean.class;
			case 1:  return String.class;
			case 2:  return String.class;
			case 3:  return String.class;				
			default: return String.class;
			}
	    }
	}
	
}
