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
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * This box contains the degradation priority of each sample. The values are
 * stored in the table, one for each sample. Specifications derived from this
 * define the exact meaning and acceptable range of the priority field.
 * 
 * @author in-somnia
 */
public class DegradationPriorityBox extends FullBox {

	private int[] priorities;

	public DegradationPriorityBox() {
		super("Degradation Priority Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		//get number of samples from SampleSizeBox
		final int sampleCount = ((SampleSizeBox) parent.getChild(BoxTypes.SAMPLE_SIZE_BOX)).getSampleCount();

		priorities = new int[sampleCount];
		for(int i = 0; i<sampleCount; i++) {
			priorities[i] = (int) in.readBytes(2);
		}
	}

	/**
	 * The priority is integer specifying the degradation priority for each
	 * sample.
	 * @return the list of priorities
	 */
	public int[] getPriorities() {
		return priorities;
	}
}
