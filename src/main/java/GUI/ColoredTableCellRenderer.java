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
package GUI;
import java.awt.Color;
import java.awt.Component;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.border.Border;
import javax.swing.table.TableCellRenderer;

public class ColoredTableCellRenderer implements TableCellRenderer
{
  public Component getTableCellRendererComponent(
    JTable table,
	Object value,
    boolean isSelected,
    boolean hasFocus,
    int row,
    int column
  )
  {
    //Label erzeugen
    JLabel label = new JLabel((String)value);
    label.setOpaque(true);
    Border b = BorderFactory.createEmptyBorder(1, 1, 1, 1);
    label.setBorder(b);
    label.setFont(table.getFont());
    label.setForeground(table.getForeground());
	label.setBackground(table.getBackground());
      //Angezeigte Spalte in Modellspalte umwandeln
      column = table.convertColumnIndexToModel(column);
/*       
      if (column == 1) {
        int numpages = Integer.parseInt((String)value);
        if (numpages >= 250) {
          label.setBackground(Color.red);
        } else if (numpages >= 200) {
          label.setBackground(Color.orange);
        } else {
          label.setBackground(Color.yellow);
        }
      }
   */
	   if (value == "x")   {
		   label.setBackground(new Color(255,200,200));	//red
	    }
		if (value == "-")  {
			label.setBackground(new Color(200,255,200));	//green
		 }
		 value = " ";
    return label;
  }
}