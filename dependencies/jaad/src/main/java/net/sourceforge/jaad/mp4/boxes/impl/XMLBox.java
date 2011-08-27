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

/**
 * When the primary data is in XML format and it is desired that the XML be
 * stored directly in the meta-box, either the XMLBox or the BinaryXMLBox is
 * used. The Binary XML Box may only be used when there is a single well-defined
 * binarization of the XML for that defined format as identified by the handler.
 *
 * @see BinaryXMLBox
 * @author in-somnia
 */
public class XMLBox extends FullBox {

	private String content;

	public XMLBox() {
		super("XML Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		content = in.readUTFString((int) getLeft(in));
	}

	/**
	 * The XML content.
	 */
	public String getContent() {
		return content;
	}
}
