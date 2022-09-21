package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GraphicsEnvironment;
import java.awt.GridLayout;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.border.Border;
import javax.swing.border.CompoundBorder;
import javax.swing.border.EmptyBorder;
import javax.swing.border.EtchedBorder;

public class FontChooser extends JDialog {

	private static final long serialVersionUID = 1L;

	/** Preview text string */
	private static final String PREVIEW_TEXT = "The quick brown fox jumps over the lazy dog.";
	/** List of font sizes */
	private static final Integer[] FONT_SIZES = { 8, 10, 11, 12, 14, 16, 18, 20, 24, 28, 36, 40, 48, 60, 72 };
	/** Index of default font size */
	private static final int DEFAULT_SIZE_INDEX = 4;

	/** Selected font */
	private Font resultFont;

	/** Font name list */
	private JList<String> fontNameList;
	/** Font size list */
	private JList<Integer> fontSizeList;
	/** Bold check box */
	private JCheckBox boldBox;
	/** Italic check box */
	private JCheckBox italicBox;
	/** Preview selected font */
	private JTextArea previewArea;

	public FontChooser(final Frame owner, final String title, final Font currentFont) {
		super(owner, title, true);
		String[] fontList = GraphicsEnvironment.getLocalGraphicsEnvironment().getAvailableFontFamilyNames();

		int nameIdx = 0;
		int sizeIdx = DEFAULT_SIZE_INDEX;
		if (currentFont != null) {
			for (int i=0; i < fontList.length; i++) {
				if (fontList[i].equals(currentFont.getName())) {
					nameIdx = i;
					break;
				}
			}
			for (int i=0; i < FONT_SIZES.length; i++) {
				if (FONT_SIZES[i].equals(currentFont.getSize())) {
					sizeIdx = i;
					break;
				}
			}
		}
		fontNameList = new JList<>(fontList);
		fontNameList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontNameList.setSelectedIndex(nameIdx);
		fontNameList.ensureIndexIsVisible(nameIdx);
		fontNameList.addListSelectionListener(event -> updatePreviewFont());

		fontSizeList = new JList<>(FONT_SIZES);
		fontSizeList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		fontSizeList.setSelectedIndex(sizeIdx);
		fontNameList.ensureIndexIsVisible(sizeIdx);
		fontSizeList.addListSelectionListener(event -> updatePreviewFont());

		previewArea = new JTextArea(PREVIEW_TEXT);
		previewArea.setEditable(false);
		previewArea.setSize(200, 50);
		previewArea.setLineWrap(true);
		previewArea.setWrapStyleWord(true);
		previewArea.setBackground(getBackground());
		Border border = new CompoundBorder(
				BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(EtchedBorder.LOWERED)),
				new EmptyBorder(8, 8, 8, 8));
		previewArea.setBorder(border);

		JPanel topRight = new JPanel(new GridLayout(1, 2));
		topRight.add(new JScrollPane(fontSizeList), BorderLayout.WEST);
		topRight.add(new JScrollPane(drawFontAttrPanel(currentFont)), BorderLayout.EAST);

		JPanel panel = new JPanel(new BorderLayout());
		panel.add(new JScrollPane(fontNameList), BorderLayout.CENTER);
		panel.add(topRight, BorderLayout.EAST);
		panel.add(new JScrollPane(previewArea), BorderLayout.SOUTH);

		setLayout(new BorderLayout());
		add(panel, BorderLayout.CENTER);
		add(drawButtonPanel(), BorderLayout.SOUTH);
		setMinimumSize(new Dimension(16, 16));

		updatePreviewFont();
		GuiHelper.setLocation(this, 0.5f, 0.5f);
	}

	private JPanel drawFontAttrPanel(Font currentFont) {
		boldBox = new JCheckBox("Bold", currentFont != null && (currentFont.getStyle() & Font.BOLD) != 0);
		boldBox.setMnemonic('b');
		boldBox.addItemListener(event -> updatePreviewFont());
		italicBox = new JCheckBox("Italic", currentFont != null && (currentFont.getStyle() & Font.ITALIC) != 0);
		italicBox.setMnemonic('i');
		italicBox.addItemListener(event -> updatePreviewFont());
		JPanel fontAttrPanel = new JPanel();
		fontAttrPanel.setLayout(new BoxLayout(fontAttrPanel, BoxLayout.Y_AXIS));
		fontAttrPanel.add(boldBox);
		fontAttrPanel.add(italicBox);
		return fontAttrPanel;
	}

	private JPanel drawButtonPanel() {
		JButton okButton = new JButton("OK");
		okButton.addActionListener(event -> {
			setVisible(false);
			dispose();
		});
		JButton cancelButton = new JButton("Cancel");
		cancelButton.addActionListener(event -> {
			resultFont = null;
			setVisible(false);
			dispose();
		});
		JPanel buttonPanel = new JPanel(new FlowLayout());
		buttonPanel.add(okButton);
		buttonPanel.add(cancelButton);
		return buttonPanel;
	}

	/**
	 * Update preview field with the current font
	 */
	private void updatePreviewFont() {
		String resultName = fontNameList.getSelectedValue();
		int resultSize = fontSizeList.getSelectedValue();
		int attrs = boldBox.isSelected() ? Font.BOLD : Font.PLAIN;
		if (italicBox.isSelected()) {
			attrs |= Font.ITALIC;
		}
		resultFont = new Font(resultName, attrs, resultSize);
		previewArea.setFont(resultFont);
		pack();
	}

	/**
	 * @return the selected font, or null if none was selected.
	 */
	public Font getSelectedFont() {
		return resultFont;
	}
}
