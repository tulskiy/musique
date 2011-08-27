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
package net.sourceforge.jaad.mp4.boxes.impl.oma;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The OMA DRM Transaction Tracking Box enables transaction tracking as defined 
 * in 'OMA DRM v2.1' section 15.3. The box includes a single transaction-ID and 
 * may appear in both DCF and PDCF.
 * 
 * @author in-somnia
 */
public class OMATransactionTrackingBox extends FullBox {

	private String transactionID;

	public OMATransactionTrackingBox() {
		super("OMA DRM Transaction Tracking Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		transactionID = in.readString(16);
	}

	/**
	 * Returns the transaction-ID of the DCF or PDCF respectively.
	 * 
	 * @return the transaction-ID
	 */
	public String getTransactionID() {
		return transactionID;
	}
}
