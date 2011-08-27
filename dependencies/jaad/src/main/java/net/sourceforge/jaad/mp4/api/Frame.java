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
package net.sourceforge.jaad.mp4.api;

public class Frame implements Comparable<Frame> {

	private final Type type;
	private final long offset, size;
	private final double time;
	private byte[] data;

	Frame(Type type, long offset, long size, double time) {
		this.type = type;
		this.offset = offset;
		this.size = size;
		this.time = time;
	}

	public Type getType() {
		return type;
	}

	public long getOffset() {
		return offset;
	}

	public long getSize() {
		return size;
	}

	public double getTime() {
		return time;
	}

	public int compareTo(Frame f) {
		final double d = time-f.time;
		//0 should not happen, since frames don't have the same timestamps
		return (d<0) ? -1 : ((d>0) ? 1 : 0);
	}

	public void setData(byte[] data) {
		this.data = data;
	}

	public byte[] getData() {
		return data;
	}
}
