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
package net.sourceforge.jaad.mp4.boxes.impl.meta;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;

/**
 * This box contains meta information about a location.
 * 
 * If the location information refers to a time-variant location, the name 
 * should express a high-level location, such as "Finland" for several places in
 * Finland or "Finland-Sweden" for several places in Finland and Sweden. Further
 * details on time-variant locations can be provided as additional notes.
 * 
 * The values of longitude, latitude and altitude provide cursory Global 
 * Positioning System (GPS) information of the media content.
 * 
 * A value of longitude (latitude) that is less than –180 (-90) or greater than 
 * 180 (90) indicates that the GPS coordinates (longitude, latitude, altitude) 
 * are unspecified, i.e. none of the given values for longitude, latitude or 
 * altitude are valid.
 * 
 * @author in-somnia
 */
public class ThreeGPPLocationBox extends ThreeGPPMetadataBox {

	private int role;
	private double longitude, latitude, altitude;
	private String placeName, astronomicalBody, additionalNotes;

	public ThreeGPPLocationBox() {
		super("3GPP Location Information Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		decodeCommon(in);

		placeName = in.readUTFString((int) getLeft(in));
		role = in.read();
		longitude = in.readFixedPoint(16, 16);
		latitude = in.readFixedPoint(16, 16);
		altitude = in.readFixedPoint(16, 16);

		astronomicalBody = in.readUTFString((int) getLeft(in));
		additionalNotes = in.readUTFString((int) getLeft(in));
	}

	/**
	 * A string indicating the name of the place.
	 * 
	 * @return the place's name
	 */
	public String getPlaceName() {
		return placeName;
	}

	/**
	 * The role of the place:<br />
	 * <ol start="0">
	 * <li>"shooting location"</li>
	 * <li>"real location"</li>
	 * <li>"fictional location"</li>
	 * </ol><br />
	 * Other values are reserved. 
	 * 
	 * @return the role of the place
	 */
	public int getRole() {
		return role;
	}

	/**
	 * A floating point number indicating the longitude in degrees. Negative 
	 * values represent western longitude.
	 * 
	 * @return the longitude
	 */
	public double getLongitude() {
		return longitude;
	}

	/**
	 * A floating point number indicating the latitude in degrees. Negative 
	 * values represent southern latitude.
	 * 
	 * @return the latitude
	 */
	public double getLatitude() {
		return latitude;
	}

	/**
	 * A floating point number indicating the altitude in meters. The reference 
	 * altitude, indicated by zero, is set to the sea level.
	 * 
	 * @return the altitude
	 */
	public double getAltitude() {
		return altitude;
	}

	/**
	 * A string indicating the astronomical body on which the location exists, 
	 * e.g. "earth".
	 * 
	 * @return the astronomical body
	 */
	public String getAstronomicalBody() {
		return astronomicalBody;
	}

	/**
	 * A string containing any additional location-related information.
	 * 
	 * @return the additional notes
	 */
	public String getAdditionalNotes() {
		return additionalNotes;
	}
}
