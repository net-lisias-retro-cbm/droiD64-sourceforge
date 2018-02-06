import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.plaf.ColorUIResource;

/*
 * Created on 21.06.2004
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
public class DroiD64 extends JFrame
{
	private DiskPanel diskPanel1, diskPanel2;
	private BugsFrame bugsFrame;
	private ShowHelpFrame showHelpFrame;
	private SettingsFrame settingsFrame;

	private static final String PROGNAME = "droiD64";
	private static final String VERSION = "0.01b";
	
	private String Progname_ = PROGNAME;
	private String Version_ = VERSION;
	
	public JTextArea textArea;
	private JPanel globalChoicePanel;
	private JButton infoButton, exitButton;
	
	private String feedBackMessage;

	private int colourChoice = 0;
	private int colourPower = 5;
	private int colourPower2 = 35;
	private int lookAndFeelChoice = 1;
	private HashMap hashMap = new HashMap();
	
	private boolean isExitQuestion = true;

	

	private JPanel obenPanel;
	private Container cp;
	public DroiD64()
	{
		super(PROGNAME+" v"+VERSION);

		diskPanel1 = new DiskPanel();
		diskPanel2 = new DiskPanel();
		
		JMenuBar menubar = new JMenuBar();
		menubar.add(createProgramMenu());
		menubar.add(diskPanel1.createD64Menu("Disk 1", "1"));
		menubar.add(diskPanel2.createD64Menu("Disk 2", "2"));
		menubar.add(createHelpMenu());
		setJMenuBar(menubar);

		drawPanel();
		
		diskPanel1.setOtherDiskPanelObject(diskPanel2);
		diskPanel2.setOtherDiskPanelObject(diskPanel1);
		

		Dimension dim = Toolkit.getDefaultToolkit().getScreenSize();
		setLocation(
			(int)((dim.width - getSize().getWidth()) / 4),
			(int)((dim.height - getSize().getHeight()) / 4)
		);
		//setSize(dim.width / 3, dim.height / 3);
	 
		//setLocation(300,200);
		pack();
		setVisible(true);

		saveDefaultValues();
//		setLookAndFeel(lookAndFeelChoice, colourChoice);
		
//		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
	}


	private void drawPanel(){
		cp = getContentPane();
		cp.setLayout( new BorderLayout());

		obenPanel = new JPanel();
		obenPanel.setLayout(new GridLayout(1,2));

		drawGlobalChoicePanel();
		
		diskPanel1.setProgname_(PROGNAME);
		diskPanel1.setVersion_(VERSION);
		diskPanel2.setProgname_(PROGNAME);
		diskPanel2.setVersion_(VERSION);
		
		obenPanel.add(diskPanel1);
		obenPanel.add(diskPanel2);

		cp.add(obenPanel, BorderLayout.NORTH);
		cp.add(globalChoicePanel, BorderLayout.SOUTH);
	}
	
	private void drawGlobalChoicePanel(){
		globalChoicePanel = new JPanel();

		infoButton = new JButton("About");
		infoButton.setMnemonic('a');
		infoButton.setToolTipText("Shows some information about this program.");
		infoButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==infoButton )
				{
					showHelp();
				};
			}
		});

		exitButton = new JButton("Exit");
		exitButton.setMnemonic('x');
		exitButton.setToolTipText("Leave this program.");
		exitButton.addActionListener(new ActionListener(){
			public void actionPerformed(ActionEvent event){
				if ( event.getSource()==exitButton ) {
					exitThisProgram();
				}
			}
		});

		globalChoicePanel.add(infoButton);
		globalChoicePanel.add(exitButton);
		
	}

	
	/**
	 * create a help drag-down menu (just for testing)
   * @return
   */
  private JMenu createHelpMenu()
	{
	  JMenu menu = new JMenu("Help");
	  menu.setMnemonic('h');
	  JMenuItem menuItem;
	  menuItem = new JMenuItem("About", 'a');
	  menu.add (menuItem);
	  menuItem.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent event){
			  if ( event.getActionCommand()== "About"){
			  	showHelp();
			  }
		  }
	  });
	  menu.addSeparator();
	  menuItem = new JMenuItem("Bugs and  To-Do", 'b');
	  menu.add (menuItem);
	  menuItem.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent event){
			  if ( event.getActionCommand()== "Bugs and  To-Do"){
			  	bugsFrame = new BugsFrame(PROGNAME+" v"+VERSION+" - Bugs and ToDo");
			  }
		  }
	  });
	  menuItem = new JMenuItem("Contact", 'c');
	  menu.add (menuItem);
	  menuItem.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent event){
			  if ( event.getActionCommand()== "Contact"){
				  JOptionPane.showMessageDialog(
					 null,
					 "For more information about this program check out this homepage:\n" +
					 "http://droid64.sourceforge.net\n\n" +
					 "You can contact me for any reason concerning "+PROGNAME+" v"+VERSION+" by writing an eMail to\n" +
					 "wolfvoz@users.sourceforge.net " ,
					 "Contact",
					 1
					);
			  }
		  }
	  });
	  return menu;
	}

	/**
	 * create a help drag-down menu (just for testing)
   * @return
   */
  private JMenu createProgramMenu()
	{
	  JMenu menu = new JMenu("Program");
	  menu.setMnemonic('P');
	  JMenuItem menuItem;
	  menuItem = new JMenuItem("Settings", 's');
	  menu.add (menuItem);
	  menuItem.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent event){
			  if ( event.getActionCommand()== "Settings"){
			  	showSettings();
			  }
		  }
	  });
	  menu.addSeparator();
	  menuItem = new JMenuItem("Exit", 'x');
	  menu.add (menuItem);
	  menuItem.addActionListener(new ActionListener(){
		  public void actionPerformed(ActionEvent event){
			  if ( event.getActionCommand()== "Exit"){
			  	exitThisProgram();
			  }
		  }
	  });
	  return menu;
	}
	
	private void exitThisProgram(){
		if (isExitQuestion) {
			if (
			JOptionPane.showConfirmDialog(
									null,
									"Really quit?",
									"Leaving this program...",
									JOptionPane.OK_CANCEL_OPTION
			) == JOptionPane.OK_OPTION){
				System.exit(0);
			}
		}
		else { 
			System.exit(0);
		}
	}

	private void showSettings(){
		settingsFrame = new SettingsFrame(PROGNAME+" v"+VERSION+" - Settings", this);
		setLookAndFeel(lookAndFeelChoice, colourChoice);
	}

	private void showHelp(){
		showHelpFrame = new ShowHelpFrame(PROGNAME+" v"+VERSION+" - About");
	}
		

	public static void main(String[] args) {
		DroiD64 program = new DroiD64();
	}


	private void setLookAndFeel(int lookAndFeelChoice, int colorChoice){
		//	Set look&feel (skin) here
		String plaf = "javax.swing.plaf.metal.MetalLookAndFeel";
	
		switch (lookAndFeelChoice) {
			case 1 : {plaf = "javax.swing.plaf.metal.MetalLookAndFeel"; break;}
			case 2 : {plaf = "com.sun.java.swing.plaf.motif.MotifLookAndFeel"; break;}
			case 3 : {plaf = "com.sun.java.swing.plaf.windows.WindowsLookAndFeel"; break; }
			//case 4 : plaf = new com.incors.plaf.kunststoff.KunststoffLookAndFeel();
		}

		try {
			UIManager.setLookAndFeel(plaf);
		}
		catch (UnsupportedLookAndFeelException e) {
			System.err.println(e.toString());
		}
		catch (ClassNotFoundException e) {
			System.err.println(e.toString());
		}
		catch (InstantiationException e) {
			System.err.println(e.toString());
		}
		catch (IllegalAccessException e) {
			System.err.println(e.toString());
		}

		ColorUIResource cr;
		int valueR, valueG, valueB;
		Object key;
		Object value;
						
		//set back old values
		 Iterator it = hashMap.entrySet().iterator(); 
		 while (it.hasNext()) {
			  Map.Entry entry = (Map.Entry)it.next();
			  key = entry.getKey();
			  value = entry.getValue();
			if (value instanceof javax.swing.plaf.ColorUIResource) {
			cr = (ColorUIResource) value;
			switch (colourChoice) {
				// gray (normal, no change to default values)
				case 0 : {
					UIManager.put(key, value);
					break;
				}
				// red
				case 1 : {
					valueR = cr.getRed()+colourPower;
					if (valueR > 255) valueR = 255;
					valueG = cr.getGreen()-colourPower;
					if (valueG < 0) valueG = 0;
					valueB = cr.getBlue()-colourPower;
					if (valueB < 0) valueB = 0;
					UIManager.put(key, new ColorUIResource(valueR, valueG,  valueB));
					break;
				}
				// green
				case 2 : {
					valueR = cr.getRed()-colourPower;
					if (valueR < 0) valueR = 0;
					valueG = cr.getGreen()+colourPower;
					if (valueG > 255) valueG = 255;
					valueB = cr.getBlue()-colourPower;
					if (valueB < 0) valueB = 0;
					UIManager.put(key, new ColorUIResource(valueR, valueG,  valueB));
					break;
				}
				// blue
				case 3 : {
					valueR = cr.getRed()-colourPower;
					if (valueR < 0) valueR = 0;
					valueG = cr.getGreen()-colourPower;
					if (valueG < 0) valueG = 0;
					valueB = cr.getBlue()+colourPower;
					if (valueB > 255) valueB = 255;
					UIManager.put(key, new ColorUIResource(valueR, valueG,  valueB));
					break;
				}
					// gray-light
				case 4 : {
					valueR = cr.getRed()+colourPower2;
					if (valueR > 255) valueR = 255;
					valueG = cr.getGreen()+colourPower2;
					if (valueG > 255) valueG = 255;
					valueB = cr.getBlue()+colourPower2 +10;
					if (valueB > 255) valueB = 255;
					UIManager.put(key, new ColorUIResource(valueR, valueG,  valueB));
					break;
				}	
			}	// switch
		  } // if
		} //while
		SwingUtilities.updateComponentTreeUI(this);
	}


	private void saveDefaultValues(){
		Object key;
		Object value;

		hashMap.clear();
							
		java.util.Enumeration keys = UIManager.getDefaults().keys();
		while (keys.hasMoreElements()) {
		  key = keys.nextElement();
		  value = UIManager.get (key);
			if (value instanceof javax.swing.plaf.ColorUIResource) {
					hashMap.put(key, value);
			} // if
		} //while
	}

	/**
	 * @return
	 */
	public int getColourChoice() {
		return colourChoice;
	}

	/**
	 * @param i
	 */
	public void setColourChoice(int i) {
		colourChoice = i;
	}

	/**
	 * @return
	 */
	public boolean isExitQuestion() {
		return isExitQuestion;
	}

	/**
	 * @param b
	 */
	public void setExitQuestion(boolean b) {
		if (b) { 
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
		else {
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		isExitQuestion = b;
	}

}
