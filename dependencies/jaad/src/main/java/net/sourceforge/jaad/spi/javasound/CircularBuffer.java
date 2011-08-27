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
package net.sourceforge.jaad.spi.javasound;

/**
 * CircularBuffer for asynchronous reading.
 * Adopted from Tritonus (http://www.tritonus.org/).
 * @author in-somnia
 */
class CircularBuffer {

	private static final int BUFFER_SIZE = 327670;
	private final byte[] data;
	private final Trigger trigger;
	private long readPos, writePos;
	private boolean open;

	CircularBuffer(Trigger trigger) {
		this.trigger = trigger;
		data = new byte[BUFFER_SIZE];
		readPos = 0;
		writePos = 0;
		open = true;
	}

	public void close() {
		open = false;
	}

	public boolean isOpen() {
		return open;
	}

	public int availableRead() {
		return (int) (writePos-readPos);
	}

	public int availableWrite() {
		return BUFFER_SIZE-availableRead();
	}

	private int getReadPos() {
		return (int) (readPos%BUFFER_SIZE);
	}

	private int getWritePos() {
		return (int) (writePos%BUFFER_SIZE);
	}

	public int read(byte[] b) {
		return read(b, 0, b.length);
	}

	public int read(byte[] b, int off, int len) {
		if(!isOpen()) {
			if(availableRead()>0) len = Math.min(len, availableRead());
			else return -1;
		}
		synchronized(this) {
			if(trigger!=null&&availableRead()<len) {
				trigger.execute();
			}
			len = Math.min(availableRead(), len);
			int remaining = len;
			while(remaining>0) {
				while(availableRead()==0) {
					try {
						wait();
					}
					catch(InterruptedException e) {
					}
				}
				int available = Math.min(availableRead(), remaining);
				int toRead;
				while(available>0) {
					toRead = Math.min(available, BUFFER_SIZE-getReadPos());
					System.arraycopy(data, getReadPos(), b, off, toRead);
					readPos += toRead;
					off += toRead;
					available -= toRead;
					remaining -= toRead;
				}
				notifyAll();
			}
			return len;
		}
	}

	public int write(byte[] b) {
		return write(b, 0, b.length);
	}

	public int write(byte[] b, int off, int len) {
		synchronized(this) {
			int remaining = len;
			while(remaining>0) {
				while(availableWrite()==0) {
					try {
						wait();
					}
					catch(InterruptedException e) {
					}
				}
				int available = Math.min(availableWrite(), remaining);
				int toWrite;
				while(available>0) {
					toWrite = Math.min(available, BUFFER_SIZE-getWritePos());
					System.arraycopy(b, off, data, getWritePos(), toWrite);
					writePos += toWrite;
					off += toWrite;
					available -= toWrite;
					remaining -= toWrite;
				}
				notifyAll();
			}
			return len;
		}
	}

	static interface Trigger {

		void execute();
	}
}
