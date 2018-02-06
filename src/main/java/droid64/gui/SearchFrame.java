package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableColumnModel;
import javax.swing.table.TableColumn;

import droid64.d64.D64;
import droid64.db.DaoFactory;
import droid64.db.DatabaseException;
import droid64.db.Disk;
import droid64.db.DiskFile;
import droid64.db.DiskSearchCriteria;
import droid64.db.SearchResultRow;

/**
 * Search dialog for DroiD64
 * 
 * @author Henrik
 */
public class SearchFrame extends JDialog {

	private static final long serialVersionUID = 1L;
	private MainPanel mainPanel;
	private SearchResultTableModel tableModel = new SearchResultTableModel();

	/** 
	 * Constructor
	 * @param topText String
	 * @param mainPanel MainPanel
	 */
	public SearchFrame (String topText, MainPanel mainPanel) {
		setTitle(topText);
		setModal(true);
		this.mainPanel = mainPanel;
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(drawSearchPanel());
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation( (int)((dim.width - getSize().getWidth()) / 3),	(int)((dim.height - getSize().getHeight()) / 3)	);
		pack();
		setVisible(true);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}
	
	/**
	 * Open selected row from search result table into the specified disk panel.
	 * @param diskPanel DiskPanel where a disk image will be opened.
	 * @param table JTable containing the search results and where one row could be selected by user.
	 */
	private void openSelected(DiskPanel diskPanel, JTable table) {
		int row = table.getSelectedRow();
		if (row >= 0) {
			String path = (String) tableModel.getValueAt(row, 0);
			String file = (String) tableModel.getValueAt(row, 1);
			String f = path + File.separator + file;
			diskPanel.openD64(f, true);
		}
	}
	
	/** Setup the search panel */
	private JPanel drawSearchPanel() {

		// Search criteria
		final JTextField fileNameText = new JTextField("", 20);
		final JTextField diskLabelText = new JTextField("", 20);
		final JTextField diskPathText = new JTextField("", 20);
		final JTextField diskFileNameText = new JTextField("", 20);
		
		DecimalFormat format3 = new DecimalFormat("###");
	    format3.setGroupingUsed(true);
	    format3.setGroupingSize(3);
	    format3.setParseIntegerOnly(false);
		final NumericTextField fileSizeMinField = new NumericTextField("10", 5, format3);
		final NumericTextField fileSizeMaxField = new NumericTextField("250", 5, format3);
		JPanel fileSizePanel = new JPanel();
		fileSizePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		fileSizePanel.add(fileSizeMinField);
		fileSizePanel.add(new JLabel(" - "));
		fileSizePanel.add(fileSizeMaxField);
		
		final String[] fileTypes = { "<Any>", "DEL", "PRG", "REL", "SEQ", "USR" };
		
		final JComboBox<String> fileTypeBox = new JComboBox<String>(fileTypes);
		fileTypeBox.setSelectedIndex(0);
		JPanel fileTypePanel = new JPanel();
		fileTypePanel.setLayout(new FlowLayout(FlowLayout.LEFT));
		fileTypePanel.add(fileTypeBox);
		
		// Search result table
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		for (int i=0; i<tableModel.getColumnCount(); i++) {
			TableColumn col;
			switch (i) {
			case 2: col = new TableColumn(i, 100); break;
			case 3: col = new TableColumn(i, 100); break;
			case 4: col = new TableColumn(i,  20); break;
			case 5: col = new TableColumn(i,  20); break;
			default: col = new TableColumn(i, 80); break;
			}			
			col.setHeaderValue(tableModel.getColumnHeader(i));
			columnModel.addColumn(col);
		}
		final JTable table = new JTable(tableModel, columnModel);			

		// Search button
		final JButton searchButton = new JButton("Search");
		searchButton.setToolTipText("Run search.");
		searchButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==searchButton ) {
					DiskSearchCriteria criteria = new DiskSearchCriteria();
					criteria.setFileName(fileNameText.getText());
					criteria.setDiskLabel(diskLabelText.getText());
					criteria.setDiskPath(diskPathText.getText());
					criteria.setDiskFileName(diskFileNameText.getText());
					
					Integer fileMin = null;
					try {
						fileMin = fileSizeMinField.getLongValue() != null ? new Integer(fileSizeMinField.getLongValue().intValue()) : null;
					} catch (ParseException e) {}
					Integer fileMax = null;
					try {
						fileMax = fileSizeMaxField.getLongValue() != null ? new Integer(fileSizeMaxField.getLongValue().intValue()) : null;
					} catch (ParseException e) {}
					criteria.setFileSizeMin(fileMin);
					criteria.setFileSizeMax(fileMax);
					switch (fileTypeBox.getSelectedIndex()) {
						case 1:  criteria.setFileType(new Integer(D64.TYPE_DEL)); break;
						case 2:  criteria.setFileType(new Integer(D64.TYPE_PRG)); break;
						case 3:  criteria.setFileType(new Integer(D64.TYPE_REL)); break;
						case 4:  criteria.setFileType(new Integer(D64.TYPE_SEQ)); break;
						case 5:  criteria.setFileType(new Integer(D64.TYPE_USR)); break;
						default: criteria.setFileType(null); break;
					}
					runSearch(criteria);
				}
			}
		});
		
		// Close button
		final JButton closeButton = new JButton("Close");
		closeButton.setToolTipText("Close search dialog.");
		closeButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==closeButton ) {
					dispose();
				}
			}
		});
		// Open in left button
		final JButton openSelectedLeftButton = new JButton("Open selected in left");
		openSelectedLeftButton.setToolTipText("Open selected.");
		openSelectedLeftButton.setEnabled(false);
		openSelectedLeftButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if (event.getSource() == openSelectedLeftButton) {
					openSelected(mainPanel.getLeftDiskPanel(), table);
				}
			}
		});		
		// Open in right button
		final JButton openSelectedRightButton = new JButton("Open selected in right");
		openSelectedRightButton.setToolTipText("Open selected.");
		openSelectedRightButton.setEnabled(false);
		openSelectedRightButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==openSelectedRightButton ) {
					openSelected(mainPanel.getRightDiskPanel(), table);
				}
			}
		});		
		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(searchButton);
		buttonPanel.add(openSelectedLeftButton);
		buttonPanel.add(openSelectedRightButton);
		buttonPanel.add(closeButton);

		table.getSelectionModel().addListSelectionListener( new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent e) {
				boolean selected = table.getSelectedRows().length > 0;
				openSelectedLeftButton.setEnabled(selected);
				openSelectedRightButton.setEnabled(selected);
			}
		});
				
        JScrollPane tableScrollPane = new JScrollPane( table);
        table.setFillsViewportHeight(true);
        table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
        table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

        // Title
        JLabel titleLabel = new JLabel("Search");
        titleLabel.setFont(new Font("Verdana",  Font.BOLD, titleLabel.getFont().getSize() * 2));
        
        // Put widgets onto panel
		GridBagConstraints gbc = new GridBagConstraints();		
        int row=0;        
		gbc.fill = GridBagConstraints.HORIZONTAL;
        gbc.gridwidth = 2;
        gbc.gridx = 0;
        gbc.gridy = row++;
        gbc.weighty = 0.1;
        gbc.weightx = 0.0;
        
		JPanel panel = new JPanel();
		panel.setLayout(new GridBagLayout());	
        panel.add(titleLabel, gbc);
        row++;
        
        addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel("File name:"));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, fileNameText);
		
		addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel("File size:"));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, fileSizePanel);
		
		addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel("File type:"));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, fileTypePanel);		

        addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel("Disk label:"));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, diskLabelText);

        addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel("Disk path:"));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, diskPathText);
		
        addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel("Disk file name:"));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, diskFileNameText);
		
		addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel(""));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, tableScrollPane);	

		addToGridBag(0, (row++/2), 0.0, 0.9, gbc, panel, new JPanel());
		addToGridBag(1, (row++/2), 0.0, 0.9, gbc, panel, new JPanel());
		
		addToGridBag(0, (row++/2), 0.0, 0.0, gbc, panel, new JLabel(""));
		addToGridBag(1, (row++/2), 0.5, 0.0, gbc, panel, buttonPanel);	
		return panel;
	}
	
	/**
	 * Wrapper to add a JComponent to a GridBagConstraints.
	 * @param x column
	 * @param y row
	 * @param weightx column weight
	 * @param weighty row weight
	 * @param gbc GridBagConstaints
	 * @param parent Parent JComponent to which to add a component
	 * @param component the new component to add
	 */
	private void addToGridBag(int x, int y, double weightx, double weighty, GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridx = x;
		gbc.gridy = y;
        gbc.gridwidth = 1;
		parent.add(component, gbc);
	}
	
	/**
	 * Perform search using criteria. Update table model with search results.
	 * @param criteria DiskSearchCriteria
	 */
	private void runSearch(DiskSearchCriteria criteria) {
		try {
			List<Disk> result = DaoFactory.getDaoFactory().getDiskDao().search(criteria);
			tableModel.clear();
			for (Disk disk : result) {
				SearchResultRow row = new SearchResultRow();
				row.setPath(disk.getFilePath());
				row.setDisk(disk.getFileName());
				row.setLabel(disk.getLabel());
				DiskFile file = disk.getFileList().get(0);
				row.setFile(file.getName());
				row.setType(file.getFileTypeString());
				row.setSize(new Integer(file.getSize()));
				tableModel.updateDirEntry(row);
			}			
		} catch (DatabaseException e) {
			mainPanel.setStatusBar(e.getMessage());
		}
	}
	
	
}
