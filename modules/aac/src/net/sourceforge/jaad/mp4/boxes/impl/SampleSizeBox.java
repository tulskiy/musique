/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl;

import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class SampleSizeBox extends FullBox {

	private int sampleSize;
	private final List<Integer> samples;

	public SampleSizeBox() {
		super("Sample Size Box", "stsz");
		samples = new ArrayList<Integer>();
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		sampleSize = (int) in.readBytes(4);
		final int sampleCount = (int) in.readBytes(4);
		left -= 8;
		if(sampleSize==0) {
			int x;
			for(int i = 0; i<sampleCount; i++) {
				x = (int) in.readBytes(4);
				samples.add(Integer.valueOf(x));
				left -= 4;
			}
		}
	}

	public int getSampleSize() {
		return sampleSize;
	}

	public List<Integer> getSamples() {
		return samples;
	}
}
