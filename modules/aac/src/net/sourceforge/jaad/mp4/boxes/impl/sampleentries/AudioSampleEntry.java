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
package net.sourceforge.jaad.mp4.boxes.impl.sampleentries;

import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;

public class AudioSampleEntry extends SampleEntry {

	private int channelCount, sampleSize, sampleRate;

	public AudioSampleEntry() {
		super("Audio Sample Entry", "mp4a");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		in.skipBytes(8); //reserved
		channelCount = (int) in.readBytes(2);
		sampleSize = (int) in.readBytes(2);
		in.skipBytes(2); //pre-defined: 0
		in.skipBytes(2); //reserved
		sampleRate = ((int) in.readBytes(4))>>16;
		left -= 20;

		readChildren(in);
	}

	public int getChannelCount() {
		return channelCount;
	}

	public int getSampleRate() {
		return sampleRate;
	}

	public int getSampleSize() {
		return sampleSize;
	}
}
