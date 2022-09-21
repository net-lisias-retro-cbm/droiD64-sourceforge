package droid64.gui;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import droid64.d64.CbmException;

/*
 * Created on 26.06.2004
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
 */

/**
 * @author wolf
 */
public class RenameFileDialog extends JDialog {

	private static final long serialVersionUID = 1L;

	private static final String[] FILE_TYPE = {
			"DEL", "SEQ", "PRG", "USR",	"REL"
	};

	/**
	 * Constructor for getting new name for a file on a disk image.
	 * @param topText title
	 * @param oldFileName old file name
	 * @param oldFileType old file type
	 * @param mainPanel mainPanel
	 * @param result RenameResult
	 */
	public RenameFileDialog (String topText, String oldFileName, int oldFileType, final MainPanel mainPanel, final RenameResult result) {
		setTitle(topText);
		result.setFileType(oldFileType);
		setModal(true);

		setLayout( new BorderLayout());

		JPanel namePanel = new JPanel();
		JPanel namePanel2 = new JPanel();
		JPanel idPanel = new JPanel();
		JPanel buttonPanel = new JPanel();

		JLabel fileNameLabel = new JLabel("Filename:");
		final JTextField nameTextField = new JTextField("", 16);
		nameTextField.setToolTipText("Enter the new filename here.");
		nameTextField.setDocument(new LimitLengthDocument(16, oldFileName));

		final JTextField nameTextField2 = new JTextField("", 16);
		try {
			nameTextField2.setFont(Settings.getCommodoreFont());
			nameTextField2.setBackground(Settings.getDirColorBg());
			nameTextField2.setForeground(Settings.getDirColorFg());
			nameTextField2.setEditable(false);
			nameTextField2.setText(oldFileName);
			nameTextField2.setBorder(BorderFactory.createCompoundBorder(nameTextField2.getBorder(), BorderFactory.createEmptyBorder(4, 4, 4, 4)));

			nameTextField.getDocument().addDocumentListener(createFieldUpdateListener(nameTextField2));

			namePanel2.add(new JLabel(""));
			namePanel2.add(nameTextField2);
		} catch (CbmException e) {	//NOSONAR
			mainPanel.appendConsole("Failed to setup name field.\n"+e.getMessage());
		}

		JLabel fileTypeLabel = new JLabel("FileType:");
		final JComboBox<String> fileTypeBox = new JComboBox<>(FILE_TYPE);
		fileTypeBox.setToolTipText("Select a filetype here.");
		fileTypeBox.setEditable(false);
		fileTypeBox.setSelectedIndex(oldFileType);

		final JButton exitButton = new JButton("Cancel");
		exitButton.setMnemonic('c');
		exitButton.setToolTipText("Cancel and return.");

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Proceed.");

		ActionListener listener = event -> {
			if (event.getSource() == okButton ) {
				result.setSuccess(true);
				String name = nameTextField.getText();
				if (name.isEmpty()) {
					return;
				}
				result.setFileName(name.length() <= 16 ? name : name.substring(0, 16));
				if (fileTypeBox.getSelectedIndex() != -1) {
					result.setFileType(fileTypeBox.getSelectedIndex());
				}
				dispose();
			} else if ( event.getSource() == exitButton ) {
				result.setSuccess(false);
				dispose();
			}
		};
		exitButton.addActionListener(listener);
		okButton.addActionListener(listener);

		namePanel.add(fileNameLabel);
		namePanel.add(nameTextField);

		idPanel.add(fileTypeLabel);
		idPanel.add(fileTypeBox);

		buttonPanel.add(exitButton);
		buttonPanel.add(okButton);

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.add(namePanel, BorderLayout.NORTH);
		topPanel.add(namePanel2, BorderLayout.SOUTH);

		add(topPanel, BorderLayout.NORTH);
		add(idPanel, BorderLayout.CENTER);
		add(buttonPanel, BorderLayout.SOUTH);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((dim.width - getSize().getWidth()) / 3),
				(int)((dim.height - getSize().getHeight()) / 3)	);
		pack();
		setVisible(mainPanel != null);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	/**
	 * Constructor for getting new name for a file on local file system.
	 * @param topText title
	 * @param oldFileName old file name
	 * @param mainPanel mainPanel
	 * @param result RenameResult
	 */
	public RenameFileDialog (String topText, String oldFileName, final MainPanel mainPanel, final RenameResult result) {
		setTitle(topText);
		setModal(true);

		Container cp = getContentPane();
		cp.setLayout(new BorderLayout());

		final JTextField nameTextField = new JTextField(oldFileName);
		nameTextField.setToolTipText("Enter the new filename here.");

		final JButton okButton = new JButton("OK");
		okButton.setMnemonic('o');
		okButton.setToolTipText("Proceed.");
		okButton.addActionListener(ae -> {
			result.setSuccess(false);
			dispose();
		});

		final JButton exitButton = new JButton("Cancel");
		exitButton.setMnemonic('c');
		exitButton.setToolTipText("Cancel and return.");
		exitButton.addActionListener(ae -> {
			result.setSuccess(true);
			String diskName = nameTextField.getText();
			if (diskName.isEmpty()) {
				return;
			}
			result.setFileName(diskName);
			dispose();
		});

		final JTextField oldNameTextField = new JTextField(oldFileName);
		oldNameTextField.setEditable(false);

		JPanel oldNamePanel = new JPanel(new BorderLayout());
		oldNamePanel.add(new JLabel("Old filename:"), BorderLayout.WEST);
		oldNamePanel.add(oldNameTextField, BorderLayout.CENTER);

		JPanel newNamePanel = new JPanel(new BorderLayout());
		newNamePanel.add(new JLabel("New filename:"), BorderLayout.WEST);
		newNamePanel.add(nameTextField, BorderLayout.CENTER);

		JPanel namePanel = new JPanel(new BorderLayout());
		namePanel.add(oldNamePanel, BorderLayout.NORTH);
		namePanel.add(newNamePanel, BorderLayout.SOUTH);

		JPanel buttonPanel = new JPanel();
		buttonPanel.add(exitButton);
		buttonPanel.add(okButton);

		cp.add(namePanel, BorderLayout.NORTH);
		cp.add(new JPanel(), BorderLayout.CENTER);
		cp.add(buttonPanel, BorderLayout.SOUTH);

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
				(int)((dim.width - getSize().getWidth()) / 3),
				(int)((dim.height - getSize().getHeight()) / 3));
		pack();
		setVisible(mainPanel != null);

		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	}

	private DocumentListener createFieldUpdateListener(final JTextField field) {
		return new DocumentListener() {
			@Override
			public void insertUpdate(DocumentEvent e) {
				updateField(e, field);
			}
			@Override
			public void removeUpdate(DocumentEvent e) {
				updateField(e, field);
			}
			@Override
			public void changedUpdate(DocumentEvent e) {
				updateField(e, field);
			}
		};
	}

	private void updateField(DocumentEvent e, JTextField field) {
		try {
			Document originator = e.getDocument();
			String text = e.getDocument().getText(0, originator.getLength());
			field.setText(text);
		} catch (BadLocationException ex) {}	//NOSONAR
	}

}
