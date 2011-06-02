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
package net.sourceforge.jaad.mp4;

/**
 * The AudioFrame is read and returned by the <code>MP4Reader</code>.
 * @author in-somnia
 */
public class AudioFrame implements Comparable<AudioFrame> {

	private final long offset;
	private final int size;
	private final double time;
	private byte[] data;

	AudioFrame(long offset, int size, double time) {
		this.offset = offset;
		this.size = size;
		this.time = time;
	}

	public long getOffset() {
		return offset;
	}

	public int getSize() {
		return size;
	}

	public double getTime() {
		return time;
	}

	void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}

	//frames are sorted by timestamp
	public int compareTo(AudioFrame af) {
		int ret = 0;
		if(time>af.getTime()||(time==af.getTime()&&offset>af.getOffset())) ret = 1;
		else if(time<af.getTime()||(time==af.getTime()&&offset<af.getOffset())) ret = -1;
		return ret;
	}
}
