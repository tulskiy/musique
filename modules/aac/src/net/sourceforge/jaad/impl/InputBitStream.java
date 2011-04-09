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
package net.sourceforge.jaad.impl;

import net.sourceforge.jaad.AACException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.util.logging.Level;

public class InputBitStream extends BitStream {

	private static final int BYTE_MASK = 0xff;
	private final PushbackInputStream in;
	private final byte[] readBuf;

	public InputBitStream(InputStream in) {
		super();
		this.in = new PushbackInputStream(in, 4);
		readBuf = new byte[4];
	}

	@Override
	public void destroy() {
		reset();
		if(in!=null) {
			try {
				in.close();
			}
			catch(Exception e) {
			}
		}
	}

	@Override
	public int getBitsLeft() {
		int i = 0;
		try {
			i = in.available();
		}
		catch(Exception e) {
		}
		return 8*i+bitsCached;
	}

	/**
	 * Reads the next four bytes.
	 * @param peek if true, the stream pointer will not be increased
	 */
	@Override
	protected int readCache(boolean peek) throws AACException {
		int x = -1;
		boolean eos = false;
		try {
			if(in.read(readBuf)==-1) eos = true;
			else {
				x = ((readBuf[0]&BYTE_MASK)<<24)|((readBuf[1]&BYTE_MASK)<<16)
						|((readBuf[2]&BYTE_MASK)<<8)|(readBuf[3]&BYTE_MASK);
				if(peek) in.unread(readBuf);
			}
		}
		catch(IOException e) {
			Constants.LOGGER.log(Level.SEVERE, "", e);
			throw new AACException(e);
		}
		if(eos) throw new AACException("end of stream", true);
		else return x;
	}
}
