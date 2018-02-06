package GUI;
import java.awt.BorderLayout;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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
public class MainPanel extends JFrame
{
	private DiskPanel diskPanel1, diskPanel2;
	private BugsFrame bugsFrame;
	private ShowHelpFrame showHelpFrame;
	private SettingsFrame settingsFrame;

	private static final String PROGNAME = "droiD64";
	private static final String VERSION = "0.04b";
	
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

	private int rowHeight = 8;

	private JPanel obenPanel;
	private Container cp;

//	private String settingsFileName = getClass().getResource("../droiD64.cfg").getFile();
	private String settingsFileName;
	private HashMap settingsData = new HashMap();

	private static final int MAX_PLUGINS = 2;
	private ExternalProgram[] externalProgram = new ExternalProgram[MAX_PLUGINS];

	public MainPanel()
	{
		super( PROGNAME+" v"+VERSION + " - Beta-Version-Warning: MAY HAVE ERRORS! USE ONLY ON BACKUPS! LOOK AT \"BUGS AND TO-DO\"!" );

		externalProgram[0] = new ExternalProgram();
		externalProgram[1] = new ExternalProgram();

		//set correct settingsFileName for loadSettings and storeSettings
		try {
			settingsFileName = System.getProperty("user.home")+"/.droiD64.cfg";
		}
		catch (Exception e) {
			System.out.println(e);
			settingsFileName = ".droiD64.cfg";
			System.out.println("Setting default filename \""+settingsFileName+"\" for config-file.");
		}

		loadSettings(settingsFileName);
		
		diskPanel1 = new DiskPanel();
		diskPanel2 = new DiskPanel();
		
		diskPanel1.setExternalProgram(externalProgram);
		diskPanel2.setExternalProgram(externalProgram);

		diskPanel1.setOtherDiskPanelObject(diskPanel2);
		diskPanel2.setOtherDiskPanelObject(diskPanel1);

		diskPanel1.startDiskPanel();
		diskPanel2.startDiskPanel();
		
		JMenuBar menubar = new JMenuBar();
		menubar.add(createProgramMenu());
		menubar.add(diskPanel1.createD64Menu("Disk 1", "1"));
		menubar.add(diskPanel2.createD64Menu("Disk 2", "2"));
		menubar.add(createHelpMenu());
		setJMenuBar(menubar);

		drawPanel();
		
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

		doSettings();


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
		loadSettings(settingsFileName);
		settingsFrame = new SettingsFrame(PROGNAME+" v"+VERSION+" - Settings", this);
		doSettings();
		storeSettings(settingsFileName);
	}
	
	private void doSettings(){
		setLookAndFeel(lookAndFeelChoice, colourChoice);
		diskPanel1.setRowHeight(rowHeight);
		diskPanel2.setRowHeight(rowHeight);
		diskPanel1.setExternalProgram(externalProgram);
		diskPanel2.setExternalProgram(externalProgram);
	}
	
	public static int stringToInt(String c) {
		int value = 0;
		char myChar;
		
		for (int i = 0; i < c.length(); i++) {
			
			myChar = c.toCharArray()[i];
			switch (myChar) {
				case 48 : { value = value*10 + 0; break; }
				case 49 : { value = value*10 + 1; break; }
				case 50 : { value = value*10 + 2; break; }
				case 51 : { value = value*10 + 3; break; }
				case 52 : { value = value*10 + 4; break; }
				case 53 : { value = value*10 + 5; break; }
				case 54 : { value = value*10 + 6; break; }
				case 55 : { value = value*10 + 7; break; }
				case 56 : { value = value*10 + 8; break; }
				case 57 : { value = value*10 + 9; break; }
				default : return value;
			}
		}
		return value;
	}

	private boolean loadSettings(String filename){
		BufferedReader f;
		String line, line_key, line_value;

		try {
			f = new BufferedReader( new FileReader( filename) );
			settingsData.clear();
		     while ((line = f.readLine()) != null) {
		     	if ( line.trim().startsWith("#")==false ) {
					line_key = line.substring( 0, line.indexOf("=") ) .trim();
					line_value = line.substring(line.indexOf("=")+1).trim();
		     		//System.out.println("\""+line_key+"\" = \""+line_value+"\"");
		     		settingsData.put(line_key, line_value);

		     		if (line_key.equalsIgnoreCase("ask_quit")) {
		     			if (line_value.equalsIgnoreCase("yes")) setExitQuestion(true); else setExitQuestion(false);
		     		}
					if (line_key.equalsIgnoreCase("row_height")) {
						
						setRowHeight(stringToInt(line_value));
					}
					if (line_key.equalsIgnoreCase("colour")) {
						setColourChoice(stringToInt(line_value));
					}

					for (int i = 0; i < externalProgram.length; i++) {
						if (line_key.equalsIgnoreCase("plugin"+i+"_label")) {
							setExternalProgram(i, externalProgram[i].getCommand(), externalProgram[i].getDescription(), line_value);
						}
						if (line_key.equalsIgnoreCase("plugin"+i+"_command")) {
							setExternalProgram(i, line_value, externalProgram[i].getDescription(), externalProgram[i].getLabel());
						}
						if (line_key.equalsIgnoreCase("plugin"+i+"_description")) {
							setExternalProgram(i, externalProgram[i].getCommand(), line_value, externalProgram[i].getLabel() );
						}
					}
		     		
		     	}
		      }
		      f.close();
		    } catch (IOException e) {
				System.err.println(e.toString());
				System.out.println("Setting default values and storing settings-file ["+settingsFileName+"].");
				setExitQuestion(isExitQuestion);
				setColourChoice(colourChoice);
				setRowHeight(rowHeight);
	//			externalProgram[0].setValues("/usr/local/bin/d64copy -t  serial2 -w -B -d 1 9", "Transfer this disk image to a real floppy.", "cbm4linux");
				setExternalProgram(0, "/home/wolf/bin/d64_to_floppy.sh", "Transfer this disk image to a real floppy (bash-script: \"d64copy -t serial2 -w -B -d 1 $1 8\").", "cbm4linux");
				setExternalProgram(1, "/usr/local/bin/x64", "Invoke VICE emulator with this disk image.", "VICE");
				storeSettings(filename);
				return false;
		     }
		
		return true;
	}
		
	private boolean storeSettings(String filename){

		FileWriter output;
		try {		
	     output = new FileWriter(filename);

		 output.write("# Configuration file for "+PROGNAME+" v"+VERSION+"\n");
		 output.write("#\n");

		 Iterator it = settingsData.entrySet().iterator(); 
		 while (it.hasNext()) {
		   Map.Entry entry = (Map.Entry)it.next();
	       output.write(entry.getKey()+"="+entry.getValue() + "\n");
		 }
	      output.close();
	    }
		
		catch (IOException e){
			System.err.println(e.toString());
			return false;
		}
		
		
		//System.out.println("Settings stored in \""+filename+"\".");

		return true;

	}

	private void showHelp(){
		showHelpFrame = new ShowHelpFrame(PROGNAME+" v"+VERSION+" - About");
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
		settingsData.put("colour", i+"" );
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
			settingsData.put("ask_quit", "yes");
			setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
		}
		else {
			settingsData.put("ask_quit", "no");
			setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		}

		isExitQuestion = b;
	}

	/**
	 * @return
	 */
	public int getRowHeight() {
		return rowHeight;
	}

	/**
	 * @param i
	 */
	public void setRowHeight(int i) {
		settingsData.put("row_height", i+"" );
		rowHeight = i;
	}

	/**
	 * @return
	 */
	public ExternalProgram getExternalProgram(int which_one) {
		return externalProgram[which_one];
	}


	/**
	 * @param which_one
	 * @param command_
	 * @param description_
	 * @param label_
	 */
	public void setExternalProgram(int which_one, String command_, String description_, String label_) {
		settingsData.put("plugin"+which_one+"_label", label_);
		settingsData.put("plugin"+which_one+"_command", command_);
		settingsData.put("plugin"+which_one+"_description", description_);
		externalProgram[which_one].setValues(command_, description_, label_);
	}

}
