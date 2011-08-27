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
package net.sourceforge.jaad.util.wav;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

public class WaveFileWriter {

	private static final int HEADER_LENGTH = 44;
	private static final int RIFF = 1380533830; //'RIFF'
	private static final long WAVE_FMT = 6287401410857104416l; //'WAVEfmt '
	private static final int DATA = 1684108385; //'data'
	private static final int BYTE_MASK = 0xFF;
	private final RandomAccessFile out;
	private final int sampleRate;
	private final int channels;
	private final int bitsPerSample;
	private int bytesWritten;

	public WaveFileWriter(File output, int sampleRate, int channels, int bitsPerSample) throws IOException {
		this.sampleRate = sampleRate;
		this.channels = channels;
		this.bitsPerSample = bitsPerSample;
		bytesWritten = 0;

		out = new RandomAccessFile(output, "rw");
		out.write(new byte[HEADER_LENGTH]); //space for the header
	}

	public void write(byte[] data) throws IOException {
		write(data, 0, data.length);
	}

	public void write(byte[] data, int off, int len) throws IOException {
		//convert to little endian
		byte tmp;
		for(int i = off; i<off+data.length; i += 2) {
			tmp = data[i+1];
			data[i+1] = data[i];
			data[i] = tmp;
		}
		out.write(data, off, len);
		bytesWritten += data.length;
	}

	public void write(short[] data) throws IOException {
		write(data, 0, data.length);
	}

	public void write(short[] data, int off, int len) throws IOException {
		for(int i = off; i<off+data.length; i++) {
			out.write(data[i]&BYTE_MASK);
			out.write((data[i]>>8)&BYTE_MASK);
			bytesWritten += 2;
		}
	}

	public void close() throws IOException {
		writeWaveHeader();
		out.close();
	}

	private void writeWaveHeader() throws IOException {
		out.seek(0);
		final int bytesPerSec = (bitsPerSample+7)/8;

		out.writeInt(RIFF); //wave label
		out.writeInt(Integer.reverseBytes(bytesWritten+36)); //length in bytes without header
		out.writeLong(WAVE_FMT);
		out.writeInt(Integer.reverseBytes(16)); //length of pcm format declaration area
		out.writeShort(Short.reverseBytes((short) 1)); //is PCM
		out.writeShort(Short.reverseBytes((short) channels)); //number of channels
		out.writeInt(Integer.reverseBytes(sampleRate)); //sample rate
		out.writeInt(Integer.reverseBytes(sampleRate*channels*bytesPerSec)); //bytes per second
		out.writeShort(Short.reverseBytes((short) (channels*bytesPerSec))); //bytes per sample time
		out.writeShort(Short.reverseBytes((short) bitsPerSample)); //bits per sample
		out.writeInt(DATA); //data section label
		out.writeInt(Integer.reverseBytes(bytesWritten)); //length of raw pcm data in bytes
	}
}
