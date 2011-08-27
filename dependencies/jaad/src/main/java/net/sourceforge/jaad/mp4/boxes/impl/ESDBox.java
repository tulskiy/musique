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
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.od.ESDescriptor;
import net.sourceforge.jaad.mp4.od.ObjectDescriptor;

/**
 * The entry sample descriptor (ESD) box is a container for entry descriptors.
 * If used, it is located in a sample entry. Instead of an <code>ESDBox</code> a
 * <code>CodecSpecificBox</code> may be present.
 * 
 * @author in-somnia
 */
public class ESDBox extends FullBox {

	private ESDescriptor esd;

	public ESDBox() {
		super("ESD Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		
		esd = (ESDescriptor) ObjectDescriptor.createDescriptor(in);
	}

	public ESDescriptor getEntryDescriptor() {
		return esd;
	}
}
