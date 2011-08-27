/*
 *  Copyright (C) 2011 in-somnia
 * 
 *  This file is part of JAAD.
 * 
 *  JAAD is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation; either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  JAAD is distributed in the hope that it will be useful, but WITHOUT 
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl;

import java.awt.Color;
import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The video media header contains general presentation information, independent
 * of the coding, for video media
 * @author in-somnia
 */
public class VideoMediaHeaderBox extends FullBox {

	private long graphicsMode;
	private Color color;

	public VideoMediaHeaderBox() {
		super("Video Media Header Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		graphicsMode = in.readBytes(2);
		//6 byte RGB color
		final int[] c = new int[3];
		for(int i = 0; i<3; i++) {
			c[i] = (in.read()&0xFF)|((in.read()<<8)&0xFF);
		}
		color = new Color(c[0], c[1], c[2]);
	}

	/**
	 * The graphics mode specifies a composition mode for this video track.
	 * Currently, only one mode is defined:
	 * '0': copy over the existing image
	 */
	public long getGraphicsMode() {
		return graphicsMode;
	}

	/**
	 * A color available for use by graphics modes.
	 */
	public Color getColor() {
		return color;
	}
}
