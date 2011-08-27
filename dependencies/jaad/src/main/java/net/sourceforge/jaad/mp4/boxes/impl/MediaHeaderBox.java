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

import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;
import net.sourceforge.jaad.mp4.boxes.Utils;

/**
 * The media header declares overall information that is media-independent, and relevant to characteristics of
 * the media in a track.
 */
public class MediaHeaderBox extends FullBox {

	private long creationTime, modificationTime, timeScale, duration;
	private String language;

	public MediaHeaderBox() {
		super("Media Header Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		
		final int len = (version==1) ? 8 : 4;
		creationTime = in.readBytes(len);
		modificationTime = in.readBytes(len);
		timeScale = in.readBytes(4);
		duration = Utils.detectUndetermined(in.readBytes(len));

		language = Utils.getLanguageCode(in.readBytes(2));

		in.skipBytes(2); //pre-defined: 0
	}

	/**
	 * The creation time is an integer that declares the creation time of the
	 * presentation in seconds since midnight, Jan. 1, 1904, in UTC time.
	 * @return the creation time
	 */
	public long getCreationTime() {
		return creationTime;
	}

	/**
	 * The modification time is an integer that declares the most recent time
	 * the presentation was modified in seconds since midnight, Jan. 1, 1904,
	 * in UTC time.
	 */
	public long getModificationTime() {
		return modificationTime;
	}

	/**
	 * The time-scale is an integer that specifies the time-scale for this
	 * media; this is the number of time units that pass in one second. For
	 * example, a time coordinate system that measures time in sixtieths of a
	 * second has a time scale of 60.
	 * @return the time-scale
	 */
	public long getTimeScale() {
		return timeScale;
	}

	/**
	 * The duration is an integer that declares the duration of this media (in 
	 * the scale of the timescale). If the duration cannot be determined then 
	 * duration is set to -1.
	 * @return the duration of this media
	 */
	public long getDuration() {
		return duration;
	}

	/**
	 * Language code for this media as defined in ISO 639-2/T.
	 * @return the language code
	 */
	public String getLanguage() {
		return language;
	}
}
