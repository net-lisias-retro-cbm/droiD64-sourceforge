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
import java.text.NumberFormat;
import java.util.List;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFormattedTextField;
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
import javax.swing.text.NumberFormatter;

import droid64.d64.CbmFile;
import droid64.d64.DiskImage;
import droid64.d64.Utility;
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
public class SearchDialog extends JDialog {

	private static final long serialVersionUID = 1L;
	private MainPanel mainPanel;
	private SearchResultTableModel tableModel = new SearchResultTableModel();

	private JTextField fileNameText = new JTextField("", 20);
	private JTextField diskLabelText = new JTextField("", 20);
	private JTextField diskPathText = new JTextField("", 20);
	private JTextField diskFileNameText = new JTextField("", 20);
	private JTextField hostNameText = new JTextField("", 20);
	private JFormattedTextField fileSizeMinField = getNumericField(10, 8);
	private JFormattedTextField fileSizeMaxField = getNumericField(250, 8);

	/**
	 * Constructor
	 * @param topText String
	 * @param mainPanel MainPanel
	 */
	public SearchDialog (String topText, MainPanel mainPanel) {
		setTitle(topText);
		setModal(true);
		this.mainPanel = mainPanel;
		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());
		cp.add(drawSearchPanel(), BorderLayout.CENTER);
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation( (int)((dim.width - getSize().getWidth()) / 3),	(int)((dim.height - getSize().getHeight()) / 3)	);
		pack();
		setVisible(mainPanel != null);
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
			diskPanel.openDiskImage(f, true);
		}
	}

	public static JFormattedTextField getNumericField(int initValue, int columns) {
		NumberFormat longFormat = NumberFormat.getIntegerInstance();
		NumberFormatter numberFormatter = new NumberFormatter(longFormat) {
			private static final long serialVersionUID = 1L;
			@Override
			public Long stringToValue(String text) {
				if (text == null || text.isEmpty()) {
					return null;
				}
				try {
					return Long.parseLong(text);
				} catch (NumberFormatException e) {
					return null;
				}
			}
		};
		numberFormatter.setValueClass(Long.class); //optional, ensures you will always get a long value
		numberFormatter.setAllowsInvalid(false); //this is the key!!
		numberFormatter.setMinimum(0l); //Optional
		JFormattedTextField field = new JFormattedTextField(numberFormatter);
		field.setText(Integer.toString(initValue));
		field.setColumns(columns);
		return field;
	}

	/** Setup the search panel */
	private JPanel drawSearchPanel() {

		JPanel fileSizePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fileSizePanel.add(fileSizeMinField);
		fileSizePanel.add(new JLabel(" - "));
		fileSizePanel.add(fileSizeMaxField);

		final String[] fileTypes = { "<Any>", "DEL", "PRG", "REL", "SEQ", "USR" };

		final JComboBox<String> fileTypeBox = new JComboBox<>(fileTypes);
		fileTypeBox.setSelectedIndex(0);
		JPanel fileTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		fileTypePanel.add(fileTypeBox);

		final String[] imageTypes = DiskImage.getImageTypeNames().clone();
		final JComboBox<String> imageTypeBox = new JComboBox<>(imageTypes);
		imageTypes[0] = "<Any>";
		imageTypeBox.setSelectedIndex(0);
		JPanel imageTypePanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
		imageTypePanel.add(imageTypeBox);

		// Search result table
		DefaultTableColumnModel columnModel = new DefaultTableColumnModel();
		for (int i=0; i<tableModel.getColumnCount(); i++) {
			TableColumn col;
			switch (i) {
			case 2:
			case 3:
				col = new TableColumn(i, 100);
				break;
			case 4:
			case 5:
				col = new TableColumn(i,  20);
				break;
			default:
				col = new TableColumn(i, 80);
				break;
			}
			col.setHeaderValue(tableModel.getColumnHeader(i));
			columnModel.addColumn(col);
		}
		final JTable table = new JTable(tableModel, columnModel);
		// Buttons
		final JButton searchButton = new JButton(Settings.getMessage(Resources.DROID64_SEARCH_SEARCH));
		final JButton closeButton = new JButton(Settings.getMessage(Resources.DROID64_SEARCH_CLOSE));
		final JButton openSelectedLeftButton = new JButton(Settings.getMessage(Resources.DROID64_SEARCH_OPENLEFT));
		final JButton openSelectedRightButton = new JButton(Settings.getMessage(Resources.DROID64_SEARCH_OPENRIGHT));
		openSelectedLeftButton.setEnabled(false);
		openSelectedRightButton.setEnabled(false);
		ActionListener buttonListener = new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent event) {
				if (event.getSource() == searchButton) {
					search(fileTypeBox.getSelectedIndex(), imageTypeBox.getSelectedIndex());
				} else if (event.getSource() == closeButton) {
					dispose();
				} else if (event.getSource() == openSelectedLeftButton) {
					openSelected(mainPanel.getLeftDiskPanel(), table);
				} else if ( event.getSource() == openSelectedRightButton ) {
					openSelected(mainPanel.getRightDiskPanel(), table);
				}
			}
		};
		searchButton.addActionListener(buttonListener);
		closeButton.addActionListener(buttonListener);
		openSelectedLeftButton.addActionListener(buttonListener);
		openSelectedRightButton.addActionListener(buttonListener);
		// Button panel
		JPanel buttonPanel = new JPanel();
		buttonPanel.add(searchButton);
		buttonPanel.add(openSelectedLeftButton);
		buttonPanel.add(openSelectedRightButton);
		buttonPanel.add(closeButton);

		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				boolean selected = table.getSelectedRows().length > 0;
				openSelectedLeftButton.setEnabled(selected);
				openSelectedRightButton.setEnabled(selected);
			}
		});

		JScrollPane tableScrollPane = new JScrollPane( table);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_ALL_COLUMNS);
		table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);

		// Title
		JLabel titleLabel = new JLabel(Settings.getMessage(Resources.DROID64_SEARCH_SEARCH));
		titleLabel.setFont(new Font("Verdana",  Font.BOLD, titleLabel.getFont().getSize() * 2));

		// Put widgets onto panel
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.fill = GridBagConstraints.BOTH;
		gbc.gridwidth = 2;
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weighty = 0.0;
		gbc.weightx = 0.0;

		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(titleLabel, gbc);

		addComponent(1, Resources.DROID64_SEARCH_FILENAME, panel, fileNameText, gbc);
		addComponent(2, Resources.DROID64_SEARCH_FILESIZE, panel, fileSizePanel, gbc);
		addComponent(3, Resources.DROID64_SEARCH_FILETYPE, panel, fileTypePanel, gbc);
		addComponent(4, Resources.DROID64_SEARCH_IMAGETYPE, panel, imageTypePanel, gbc);
		addComponent(5, Resources.DROID64_SEARCH_IMAGELABEL, panel, diskLabelText, gbc);
		addComponent(6, Resources.DROID64_SEARCH_IMAGEPATH, panel, diskPathText, gbc);
		addComponent(7, Resources.DROID64_SEARCH_IMAGEFILE, panel, diskFileNameText, gbc);
		addComponent(8, Resources.DROID64_SEARCH_HOSTNAME, panel, hostNameText, gbc);
		gbc.weighty = 1.0;
		addComponent(9, "", panel, tableScrollPane, gbc);
		gbc.weighty = 0.0;
		addComponent(10, "", panel, buttonPanel, gbc);
		return panel;
	}

	private void addComponent(int row, String propKey, JPanel parent, JComponent component, GridBagConstraints gbc) {
		addToGridBag(0, row, 0.0, gbc, parent, new JLabel(Settings.getMessage(propKey)));
		addToGridBag(1, row, 0.5, gbc, parent, component);
	}

	protected void search(int selectedFileType, int imageType ) {
		DiskSearchCriteria criteria = new DiskSearchCriteria();
		criteria.setFileName(fileNameText.getText());
		criteria.setDiskLabel(diskLabelText.getText());
		criteria.setDiskPath(diskPathText.getText());
		criteria.setDiskFileName(diskFileNameText.getText());
		criteria.setFileSizeMin(Utility.parseInteger(fileSizeMinField.getText(), 10));
		criteria.setFileSizeMax(Utility.parseInteger(fileSizeMaxField.getText(), 250));
		criteria.setHostName(hostNameText.getText());

		switch (selectedFileType) {
		case 1:
			criteria.setFileType(Integer.valueOf(CbmFile.TYPE_DEL));
			break;
		case 2:
			criteria.setFileType(Integer.valueOf(CbmFile.TYPE_PRG));
			break;
		case 3:
			criteria.setFileType(Integer.valueOf(CbmFile.TYPE_REL));
			break;
		case 4:
			criteria.setFileType(Integer.valueOf(CbmFile.TYPE_SEQ));
			break;
		case 5:
			criteria.setFileType(Integer.valueOf(CbmFile.TYPE_USR));
			break;
		default:
			criteria.setFileType(null);
			break;
		}
		criteria.setImageType(imageType > 0 && imageType < DiskImage.getImageTypeNames().length ? Integer.valueOf(imageType) : null);
		runSearch(criteria);
	}

	/**
	 * Wrapper to add a JComponent to a GridBagConstraints.
	 * @param x column
	 * @param y row
	 * @param weightx column weight
	 * @param gbc GridBagConstaints
	 * @param parent Parent JComponent to which to add a component
	 * @param component the new component to add
	 */
	private void addToGridBag(int x, int y, double weightx, GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.weightx = weightx;
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
				DiskFile file = disk.getFileList().get(0);
				SearchResultRow row = new SearchResultRow(
						disk.getFilePath(), disk.getFileName(), disk.getLabel(),
						file.getName(), file.getFileTypeString(), Integer.valueOf(file.getSize()), disk.getHostName());
				tableModel.updateDirEntry(row);
			}
		} catch (DatabaseException e) {	//NOSONAR
			if (mainPanel != null) {
				mainPanel.appendConsole(e.getMessage());
			}
		}
	}

}
