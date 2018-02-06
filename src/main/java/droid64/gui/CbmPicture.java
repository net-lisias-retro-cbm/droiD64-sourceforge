package droid64.gui;

import java.awt.image.BufferedImage;
import java.io.IOException;

/**<pre style='font-family:sans-serif;'>
 * Created on 2015-Oct-17
 *
 *   droiD64 - A graphical file manager for D64 files
 *   Copyright (C) 2015 Henrik Wetterström
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
 *   http://droid64.sourceforge.net
 *
 * @author Henrik
 * </pre>
 */
public class CbmPicture {

	private byte[] data;	
	private final static int[] COLORS = {
		0x000000, 0xffffff, 0x68372b, 0x70a4b2,
		0x6f3d86, 0x588d43, 0x352879, 0xb8c76f,
		0x6f4f25, 0x433900, 0x9a6759, 0x444444,
		0x6c6c6c, 0x9ad284,	0x6c5eb5, 0x959595
	};
	
	private final static int KOALA_COLOR_RAM = 0x2328;
	private final static int KOALA_SCREEN_RAM = 0x1f40;
	private final static int KOALA_BACKGROUND = 0x2710;
	private final static int KOALA_RLE_BYTE = 0xfe;
	private final static int KOALA_SIZE = 10003;
	
	public CbmPicture(byte[] data) throws IOException {
		if (data.length < KOALA_SIZE) {
			this.data = decompress(data);
		} else {
			this.data = data;
		}
		if (this.data.length < 40 * 200 + 2) {
			throw new IOException("Not enough data for CBM picture.");
		}		
	}
	
	public BufferedImage getImage() {
		BufferedImage image = null;
		image = new BufferedImage(320, 200, BufferedImage.TYPE_INT_RGB);
		boolean colorMode = true;
		int[] colors = new int[4];
		int background = data[KOALA_BACKGROUND + 2] & 0x0f;
		colors[0] = COLORS[background];
		for (int i=0; i<25; i++) {
			for (int j=0; j<40; j++) {
				int screenColor = data [2 + KOALA_SCREEN_RAM + i*40 + j] & 0xff;
				int colorRam = data [2 + KOALA_COLOR_RAM + i*40 + j] & 0x0f;
				colors[1] = COLORS[screenColor >> 4];
				colors[2] = COLORS[screenColor & 0x0f];
				colors[3] = COLORS[colorRam];
				for (int k=0; k < 8; k++) {
					int b = data[2 + ((i * 40 + j) << 3) + k] & 0xff;
					int x = j << 3;
					int y = (i << 3) + k;
					if (colorMode) {
						int c0 = (b >> 6) & 0x03; 
						int c1 = (b >> 4) & 0x03; 
						int c2 = (b >> 2) & 0x03; 
						int c3 = b & 0x03;
						image.setRGB(x++, y, colors[c0]);
						image.setRGB(x++, y, colors[c0]);
						image.setRGB(x++, y, colors[c1]);
						image.setRGB(x++, y, colors[c1]);
						image.setRGB(x++, y, colors[c2]);
						image.setRGB(x++, y, colors[c2]);
						image.setRGB(x++, y, colors[c3]);
						image.setRGB(x++, y, colors[c3]);
					} else {
						image.setRGB(x++, y, colors[ (b & 0x80) == 0 ? 0 : 1]);
						image.setRGB(x++, y, colors[ (b & 0x40) == 0 ? 0 : 1]);
						image.setRGB(x++, y, colors[ (b & 0x20) == 0 ? 0 : 1]);
						image.setRGB(x++, y, colors[ (b & 0x10) == 0 ? 0 : 1]);
						image.setRGB(x++, y, colors[ (b & 0x08) == 0 ? 0 : 1]);
						image.setRGB(x++, y, colors[ (b & 0x04) == 0 ? 0 : 1]);
						image.setRGB(x++, y, colors[ (b & 0x02) == 0 ? 0 : 1]);
						image.setRGB(x++, y, colors[ (b & 0x01) == 0 ? 0 : 1]);
					}
				}
			}
		}
		return image;
	}
	
	private byte[] decompress(byte[] inData) {
		byte[] outData = new byte[ KOALA_SIZE ];
		int in = 0;
		int out = 0;
		while (in < inData.length && out < outData.length) {
			int b = inData[in++] & 0xff;
			if (b == KOALA_RLE_BYTE) {
				byte v = inData[in++];
				int c = inData[in++] & 0xff;
				if (c==0) {
					c = 256;
				}
				for (int j=0; j < c && out <outData.length; j++) {
					outData[out++] = v;
				}
			} else {
				outData[out++] = (byte) b;
			}
		}		
		return outData;
	}
	
	
}
