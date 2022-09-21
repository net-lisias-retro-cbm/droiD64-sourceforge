package droid64.gui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.Window;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.ServiceLoader;
import java.util.stream.Collectors;

import javax.swing.JComponent;
import javax.swing.JFormattedTextField;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;
import javax.swing.text.JTextComponent;
import javax.swing.text.NumberFormatter;

import droid64.d64.Utility;

public class GuiHelper {

	private GuiHelper() {
		super();
	}

	public static void setLocation(Container component, int widthFraction, int heightFraction) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setLocation(
				(int)((dim.width - component.getSize().getWidth()) / widthFraction),
				(int)((dim.height - component.getSize().getHeight()) / heightFraction));
	}

	public static void setLocation(Container component, float widthFraction, float heightFraction) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setLocation(
				(int)((dim.width - component.getSize().getWidth()) * widthFraction),
				(int)((dim.height - component.getSize().getHeight()) * heightFraction));
	}

	public static void setSize(Container component, int widthFraction, int heightFraction) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setSize(dim.width/widthFraction, dim.height/heightFraction);
	}

	public static void setPreferredSize(JComponent component, int widthFraction, int heightFraction) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setPreferredSize(new Dimension(dim.width/widthFraction, dim.height/heightFraction));
	}

	public static void setSize(Container component, float widthFraction, float heightFraction) {
		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		component.setSize((int) (dim.width * widthFraction), (int) (dim.height * heightFraction));
	}

	/**
	 * Wrapper to add a JComponent to a GridBagConstraints.
	 * @param x column
	 * @param y row
	 * @param weightx column weight
	 * @param weighty row weight
	 * @param wdt the grid width
	 * @param gbc GridBagConstaints
	 * @param parent Parent JComponent to which to add a component
	 * @param component the new component to add
	 */
	public static void addToGridBag(int x, int y, double weightx, double weighty, int wdt, GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = wdt;
		parent.add(component, gbc);
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
	public static  void addToGridBag(int x, int y, double weightx, double weighty, GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.weightx = weightx;
		gbc.weighty = weighty;
		gbc.gridx = x;
		gbc.gridy = y;
		parent.add(component, gbc);
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
	public static void addToGridBag(int x, int y, double weightx, GridBagConstraints gbc, JComponent parent, JComponent component) {
		gbc.weightx = weightx;
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		parent.add(component, gbc);
	}

	public static JMenuItem addMenuItem(JMenu menu, String propertyKey, Character mnemonic, ActionListener listener) {
		JMenuItem menuItem;
		if (mnemonic== null) {
			menuItem = new JMenuItem(Utility.getMessage(propertyKey));
		} else {
			menuItem = new JMenuItem(Utility.getMessage(propertyKey), mnemonic);
		}
		menuItem.setActionCommand(propertyKey);
		menuItem.addActionListener(listener);
		menu.add (menuItem);
		return menuItem;
	}

	public static JMenuItem addMenuItem(JPopupMenu menu, String propertyKey, Character mnemonic, ActionListener listener) {
		JMenuItem menuItem;
		if (mnemonic== null) {
			menuItem = new JMenuItem(Utility.getMessage(propertyKey));
		} else {
			menuItem = new JMenuItem(Utility.getMessage(propertyKey), mnemonic);
		}
		menuItem.setActionCommand(propertyKey);
		menuItem.addActionListener(listener);
		menu.add (menuItem);
		return menuItem;
	}

	public static void setDefaultFonts() {
		Font plainFont = new Font("Verdana", Font.PLAIN, Setting.LOCAL_FONT_SIZE.getInteger());
		Font boldFont = new Font("Verdana", Font.BOLD, Setting.LOCAL_FONT_SIZE.getInteger());
		UIManager.put("Button.font",            new FontUIResource(plainFont));
		UIManager.put("CheckBox.font",          new FontUIResource(plainFont));
		UIManager.put("ComboBox.font",          new FontUIResource(plainFont));
		UIManager.put("RadioButton.font",       new FontUIResource(plainFont));
		UIManager.put("FormattedTextField.font",new FontUIResource(plainFont));
		UIManager.put("Label.font",             new FontUIResource(boldFont));
		UIManager.put("List.font",              new FontUIResource(plainFont));
		UIManager.put("Menu.font",              new FontUIResource(plainFont));
		UIManager.put("MenuItem.font",          new FontUIResource(plainFont));
		UIManager.put("OptionPane.messageFont", new FontUIResource(plainFont));
		UIManager.put("Slider.font",            new FontUIResource(plainFont));
		UIManager.put("Spinner.font",           new FontUIResource(plainFont));
		UIManager.put("TabbedPane.font",        new FontUIResource(plainFont));
		UIManager.put("Table.font",             new FontUIResource(plainFont));
		UIManager.put("TableHeader.font",       new FontUIResource(plainFont));
		UIManager.put("TextArea.font",          new FontUIResource(plainFont));
		UIManager.put("TextField.font",         new FontUIResource(plainFont));
		UIManager.put("ToggleButton.font",      new FontUIResource(plainFont));
		UIManager.put("ToolTip.font",           new FontUIResource(plainFont));
		UIManager.put("TitledBorder.font",      new FontUIResource(plainFont));
	}

	public static JFormattedTextField getNumField(int min, int max, int initialValue, int columns) {
		NumberFormatter nf = new NumberFormatter();
		nf.setMinimum(Integer.valueOf(min));
		nf.setMaximum(Integer.valueOf(max));
		JFormattedTextField field = new JFormattedTextField(nf);
		field.setValue(initialValue);
		field.setColumns(columns);
		return field;
	}

	public static void hierarchyListenerResizer(Window window) {
		if (window instanceof Dialog) {
			Dialog dialog = (Dialog) window;
			if (!dialog.isResizable()) {
				dialog.setResizable(true);
			}
		}
	}

	public static void showErrorMessage(Component parent, String title, String message, Object...args) {
		JOptionPane.showMessageDialog(parent, String.format(message, args), title, JOptionPane.ERROR_MESSAGE);
	}

	public static void showInfoMessage(Component parent, String title, String message, Object...args) {
		JOptionPane.showMessageDialog(parent, String.format(message, args), title, JOptionPane.INFORMATION_MESSAGE);
	}

	public static void showException(Component parent, String title, Throwable throwable, String message, Object...args) {
		JTextArea messageText = new JTextArea(String.format(message, args));
		messageText.setEditable(false);
		JTextArea stacktrace = new JTextArea(toString(throwable));
		stacktrace.setEditable(false);
		JPanel panel = new JPanel(new BorderLayout());
		panel.add(messageText, BorderLayout.NORTH);
		panel.add(new JScrollPane(stacktrace), BorderLayout.CENTER);
		panel.addHierarchyListener(e -> GuiHelper.hierarchyListenerResizer(SwingUtilities.getWindowAncestor(panel)));
		GuiHelper.setPreferredSize(panel, 2, 3);
		JOptionPane.showMessageDialog(parent, panel, title, JOptionPane.ERROR_MESSAGE);
	}

	public static String toString(Throwable throwable) {
		StringWriter writer = new StringWriter();
		throwable.printStackTrace(new PrintWriter(writer));
		return writer.toString();
	}

	/**
	 * Get sorted list with names of classes which instantiates <code>clazz</code>.
	 * Interfaces are excluded from results.
	 * @param <T> the type of clazz.
	 * @param clazz the super class/interface.
	 * @return List of class names
	 */
	public static <T> List<String> getClassNames(Class<T> clazz) {
		ServiceLoader<T> loader = ServiceLoader.load(clazz);
		List<Class<?>> list = new ArrayList<>();
		loader.forEach(drv -> list.add(drv.getClass()));
		return list.stream().filter(p->!p.isInterface()).map(Class::getName).sorted((a,b)->a.compareTo(b)).collect(Collectors.toList());
	}

	/**
	 * Add keyboard navigation to text component
	 * @param component the text component
	 * @param scrollPane the scrollpane with the scrollbars to move
	 */
	public static void keyNavigateTextArea(JTextComponent component, JScrollPane scrollPane) {
		component.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyPressed(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyReleased(KeyEvent ev) {
				int keyCode = ev.getKeyCode();
				if (keyCode == KeyEvent.VK_HOME) {
					JScrollBar vertical = scrollPane.getVerticalScrollBar();
					vertical.setValue(vertical.getMinimum());
				} else if (keyCode == KeyEvent.VK_END) {
					JScrollBar vertical = scrollPane.getVerticalScrollBar();
					vertical.setValue(vertical.getMaximum() );
				}
			}
		});
	}

	/**
	 * Add keyboard navigation to table
	 * @param table the table
	 */
	public static void keyNavigateTable(JTable table) {
		table.addKeyListener(new KeyListener() {
			@Override
			public void keyTyped(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyPressed(KeyEvent ev) { /* Not used */ }

			@Override
			public void keyReleased(KeyEvent ev) {
				int keyCode = ev.getKeyCode();
				if (keyCode == KeyEvent.VK_HOME) {
					table.getSelectionModel().setSelectionInterval(0, 0);
					table.scrollRectToVisible(table.getCellRect(0, 0, true));
				} else if (keyCode == KeyEvent.VK_END) {
					int lastRow = table.getRowCount() - 1;
					table.getSelectionModel().setSelectionInterval(lastRow, lastRow);
					table.scrollRectToVisible(table.getCellRect(lastRow, 0, true));
				}
			}
		});
	}
}
