package droid64.gui;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.Toolkit;
import java.awt.event.ActionListener;

import javax.swing.JComponent;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.UIManager;
import javax.swing.plaf.FontUIResource;

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

	public static JMenuItem addMenuItem(JMenu menu, String propertyKey, int mnemonic, ActionListener listener) {
		JMenuItem menuItem = new JMenuItem(Settings.getMessage(propertyKey), mnemonic);
		menuItem.setActionCommand(propertyKey);
		menuItem.addActionListener(listener);
		menu.add (menuItem);
		return menuItem;
	}

	public static void setDefaultFonts() {
		Font plainFont = new Font("Verdana", Font.PLAIN, Settings.getFontSize());
		Font boldFont = new Font("Verdana", Font.BOLD, Settings.getFontSize());
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

}
