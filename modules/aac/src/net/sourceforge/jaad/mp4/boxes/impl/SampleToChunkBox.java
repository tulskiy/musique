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

public class SampleToChunkBox extends FullBox {

	public static class SampleToChunkEntry {

		private final int firstChunk, samplesPerChunk, sampleDescriptionIndex;

		SampleToChunkEntry(int firstChunk, int samplesPerChunk, int sampleDescriptionIndex) {
			this.firstChunk = firstChunk;
			this.samplesPerChunk = samplesPerChunk;
			this.sampleDescriptionIndex = sampleDescriptionIndex;
		}

		public int getFirstChunk() {
			return firstChunk;
		}

		public int getSampleDescriptionIndex() {
			return sampleDescriptionIndex;
		}

		public int getSamplesPerChunk() {
			return samplesPerChunk;
		}
	}
	private final List<SampleToChunkEntry> entries;

	public SampleToChunkBox() {
		super("Sample To Chunk Box", "stsc");
		entries = new ArrayList<SampleToChunkEntry>();
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		final int entryCount = (int) in.readBytes(4);
		left -= 4;
		int firstChunk, samplesPerChunk, sampleDescriptionIndex;
		for(int i = 0; i<entryCount; i++) {
			firstChunk = (int) in.readBytes(4);
			samplesPerChunk = (int) in.readBytes(4);
			sampleDescriptionIndex = (int) in.readBytes(4);
			entries.add(new SampleToChunkEntry(firstChunk, samplesPerChunk, sampleDescriptionIndex));
			left -= 12;
		}
	}

	public List<SampleToChunkEntry> getEntries() {
		return entries;
	}
}
