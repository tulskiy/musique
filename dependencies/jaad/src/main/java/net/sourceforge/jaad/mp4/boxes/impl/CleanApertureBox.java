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

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

public class CleanApertureBox extends BoxImpl {

	private long cleanApertureWidthN;
	private long cleanApertureWidthD;
	private long cleanApertureHeightN;
	private long cleanApertureHeightD;
	private long horizOffN;
	private long horizOffD;
	private long vertOffN;
	private long vertOffD;

	public CleanApertureBox() {
		super("Clean Aperture Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		cleanApertureWidthN = in.readBytes(4);
		cleanApertureWidthD = in.readBytes(4);
		cleanApertureHeightN = in.readBytes(4);
		cleanApertureHeightD = in.readBytes(4);
		horizOffN = in.readBytes(4);
		horizOffD = in.readBytes(4);
		vertOffN = in.readBytes(4);
		vertOffD = in.readBytes(4);
	}

	public long getCleanApertureWidthN() {
		return cleanApertureWidthN;
	}

	public long getCleanApertureWidthD() {
		return cleanApertureWidthD;
	}

	public long getCleanApertureHeightN() {
		return cleanApertureHeightN;
	}

	public long getCleanApertureHeightD() {
		return cleanApertureHeightD;
	}

	public long getHorizOffN() {
		return horizOffN;
	}

	public long getHorizOffD() {
		return horizOffD;
	}

	public long getVertOffN() {
		return vertOffN;
	}

	public long getVertOffD() {
		return vertOffD;
	}
}
