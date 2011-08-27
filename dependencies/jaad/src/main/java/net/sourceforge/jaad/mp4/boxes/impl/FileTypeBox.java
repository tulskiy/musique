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

import net.sourceforge.jaad.mp4.boxes.BoxImpl;
import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;

//TODO: 3gpp brands
public class FileTypeBox extends BoxImpl {

	public static final String BRAND_ISO_BASE_MEDIA = "isom";
	public static final String BRAND_ISO_BASE_MEDIA_2 = "iso2";
	public static final String BRAND_ISO_BASE_MEDIA_3 = "iso3";
	public static final String BRAND_MP4_1 = "mp41";
	public static final String BRAND_MP4_2 = "mp42";
	public static final String BRAND_MOBILE_MP4 = "mmp4";
	public static final String BRAND_QUICKTIME = "qm  ";
	public static final String BRAND_AVC = "avc1";
	public static final String BRAND_AUDIO = "M4A ";
	public static final String BRAND_AUDIO_2 = "M4B ";
	public static final String BRAND_AUDIO_ENCRYPTED = "M4P ";
	public static final String BRAND_MP7 = "mp71";
	protected String majorBrand, minorVersion;
	protected String[] compatibleBrands;

	public FileTypeBox() {
		super("File Type Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		majorBrand = in.readString(4);
		minorVersion = in.readString(4);
		compatibleBrands = new String[(int) getLeft(in)/4];
		for(int i = 0; i<compatibleBrands.length; i++) {
			compatibleBrands[i] = in.readString(4);
		}
	}

	public String getMajorBrand() {
		return majorBrand;
	}

	public String getMinorVersion() {
		return minorVersion;
	}

	public String[] getCompatibleBrands() {
		return compatibleBrands;
	}
}
