package droid64.gui;

import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.font.TextAttribute;
import java.awt.image.BufferedImage;
import java.awt.print.PageFormat;
import java.awt.print.Pageable;
import java.awt.print.Printable;
import java.awt.print.PrinterException;
import java.text.AttributedString;

import droid64.d64.CbmException;

/**<pre style='font-family:sans-serif;'>
 * Created on 20.02.2016
 *
 *   droiD64 - A graphical filemanager for D64 files
 *   Copyright (C) 2016 Henrik Wetterström
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
 *   eMail: hwetters@users.sourceforge.net
 *   http://droid64.sourceforge.net
 *</pre>
 * @author Henrik
 */
public class PrintPageable implements Pageable {

	/** Pageable manager for text */
	private final static int MODE_TEXT  = 0;
	/** Pageable manager for bytes */
	private final static int MODE_BYTES = 1;
	/** Pageable manager for images */
	private final static int MODE_IMAGE = 2;

	private final static int ROWS_PER_PAGE = 58;
	private final static int BYTES_PER_LINE = 16;
	private final static int ROW_HEIGHT = 11;
	private final static String FORMAT = "%08X: %-" + (BYTES_PER_LINE * 3) + "s  %-" + (BYTES_PER_LINE) + "s";
	
	private final static Font PLAIN_FONT = new Font(Font.SERIF, Font.PLAIN, 10);
	private final static Font TITLE_FONT = new Font(Font.SERIF, Font.BOLD, 10);
	private final static Font MONO_FONT = new Font(Font.MONOSPACED, Font.PLAIN, 8);
	private static Font cbmFont = null;

    private PageFormat documentPageFormat = new PageFormat();
	private String[] lines = null;
	private String title = null;
    private byte[] data = null;
    private BufferedImage image = null;
	private int mode;

	private boolean useCbmFont = false;
	private boolean useMonoFont = false;
	
	/**
	 * Constructor for text pageable
	 * @param lines array of strings to be printed
	 * @param title the title to print in bold before text
	 * @param useCbmFont use a C64 like font
	 * @param useMonoFont use a monospace font
	 */
	public PrintPageable(String[] lines, String title, boolean useCbmFont, boolean useMonoFont) {
		documentPageFormat.setOrientation(PageFormat.PORTRAIT);
		this.lines = lines != null ? lines : new String[0];
		this.title = title;
		this.useCbmFont = useCbmFont;
		this.useMonoFont = useMonoFont;
		
		mode = MODE_TEXT;
		
		if (useCbmFont && (cbmFont == null || MONO_FONT.equals(cbmFont))) {
			try {
				cbmFont = Settings.getCommodoreFont().deriveFont(10);
			} catch (CbmException e) {
				e.printStackTrace();
				cbmFont = MONO_FONT;
			}
		}
	}

	/**
	 * Constructor for text pageable
	 * @param text which is broken down into lines by line breaks.
	 * @param title the title to print in bold before text
	 * @param useCbmFont use a C64 like font
	 * @param useMonoFont use a monospace font
	 */
	public PrintPageable(String text, String title, boolean useCbmFont, boolean useMono) {
		this(text != null ? text.split("[\r\n]+") : new String[0], title, useCbmFont, useMono);
	}
	
	/**
	 * Constructor for printing hex dumps
	 * @param data the data to be printed in hexdump
	 * @param title the title to print in bold before text
	 */
	public PrintPageable(byte[] data, String title) {
		this.title = title;	
		this.data = data;
		mode = MODE_BYTES;
	}

	/**
	 * Constructor for printing images
	 * @param image the image to be printed
	 * @param title not used yet.
	 */
	public PrintPageable(BufferedImage image, String title) {
		this.title = title;	
		this.image = image;
		this.mode = MODE_IMAGE;
	}
	
	@Override
	public int getNumberOfPages() {
		if (mode == MODE_TEXT) {
			return (lines.length / ROWS_PER_PAGE) + 1;	
		} else if (mode == MODE_BYTES) {
			return (data.length / (BYTES_PER_LINE * ROWS_PER_PAGE)) + 1;
		} else if (mode == MODE_IMAGE && image != null) {
			return 1;
		} else {
			return 0;
		}		
	}

	@Override
	public PageFormat getPageFormat(int pageIndex) throws IndexOutOfBoundsException {
		return documentPageFormat;
	}

	@Override
	public Printable getPrintable(int pageIndex) throws IndexOutOfBoundsException {
		if (useCbmFont) {
			return new MultiPrintable(cbmFont);
		} else if (useMonoFont || mode == MODE_BYTES) {
			return new MultiPrintable(MONO_FONT);			
		} else {
			return new MultiPrintable(PLAIN_FONT);			
		}
	}

	class MultiPrintable implements Printable {
		private int posX = 0;
		private int posY = 0;
		private Font font;
		
		public MultiPrintable(Font font) {
			this.font = font;
		}
		
		@Override
		public int print(Graphics graphics, PageFormat pageFormat, int pageIndex) throws PrinterException {
		    try {
		    	if (mode == MODE_TEXT) {
			    	int lineNum = pageIndex * ROWS_PER_PAGE;
			    	if (lineNum > lines.length) {
				         return NO_SUCH_PAGE;
			    	}
				    int rowsLeftOnPage = ROWS_PER_PAGE;
				    posX = 2;
				    posY = 10;
				    Graphics2D g2d = (Graphics2D) graphics;
				    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				    
				    if (lineNum == 0 && title != null) {
				    	drawString(g2d, title, TITLE_FONT);	
				    	rowsLeftOnPage--;
				    }
				    for (;lineNum< lines.length; lineNum++) {
					    drawString(g2d, lines[lineNum], font);
					    if (rowsLeftOnPage-- <= 0) {
						    return PAGE_EXISTS;
					    }
				    }
				    return PAGE_EXISTS;
		    	} else if (mode == MODE_BYTES) {
			    	int dataPos = pageIndex * ROWS_PER_PAGE * BYTES_PER_LINE;
			    	if (dataPos > data.length) {
				         return NO_SUCH_PAGE;
			    	}
				    int rowsLeftOnPage = ROWS_PER_PAGE;
				    posX = 2;
				    posY = 10;
				    Graphics2D g2d = (Graphics2D) graphics;
				    g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
				    if (dataPos == 0) {
				    	drawString(g2d, title, TITLE_FONT);	
				    	rowsLeftOnPage--;
				    }
				    for (;dataPos< data.length; dataPos += BYTES_PER_LINE) {
			    		String hex = "";
			    		String ascii = "";
			    		for (int j=0; j<BYTES_PER_LINE; j++) {
			    			if (dataPos+j < data.length) {
			    				int c = data[dataPos+j] & 0xff;
			    				hex += " " + HexTableModel.HEX[c];
			    				ascii += (c < 0x20 || c > 0x7e)  ? '.' : (char) c;
			    			}
			    		}
				    	String line  = String.format(FORMAT, dataPos, hex, ascii);
					    drawString(g2d, line, font);
					    if (rowsLeftOnPage-- <= 0) {
						    return PAGE_EXISTS;	
					    }
				    }
				    return PAGE_EXISTS;
		    	} else if (mode == MODE_IMAGE) {
	                if (pageIndex == 0) {
	                	Graphics2D g2d = (Graphics2D) graphics;
				    	g2d.translate(pageFormat.getImageableX(), pageFormat.getImageableY());
	                	graphics.drawImage(image, 0, 0, image.getWidth(), image.getHeight(), null);
				    	return PAGE_EXISTS;
	                } else {
				        return NO_SUCH_PAGE;
	                }
		    	} else {
			        return NO_SUCH_PAGE;
		    	}
			} catch (Exception e) {
				e.printStackTrace();
		        return NO_SUCH_PAGE;
			}
		}
		
		private void drawString(Graphics2D g2d, String str, Font font) {
			if (str != null && !str.isEmpty()) {
				AttributedString as = new AttributedString(str);
				as.addAttribute(TextAttribute.FONT, font);
			    g2d.drawString(as.getIterator(), posX, posY);
			}
		    posY += ROW_HEIGHT;
		}
	}
	
}
