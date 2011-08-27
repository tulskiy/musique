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
package net.sourceforge.jaad.aac;

import java.io.IOException;

/**
 * Standard exception, thrown when decoding of an AAC frame fails.
 * The message gives more detailed information about the error.
 * @author in-somnia
 */
public class AACException extends IOException {

	private final boolean eos;

	public AACException(String message) {
		this(message, false);
	}

	public AACException(String message, boolean eos) {
		super(message);
		this.eos = eos;
	}

	public AACException(Throwable cause) {
		super(cause);
		eos = false;
	}

	boolean isEndOfStream() {
		return eos;
	}
}
