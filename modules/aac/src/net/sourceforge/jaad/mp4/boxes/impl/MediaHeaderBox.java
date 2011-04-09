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
package net.sourceforge.jaad.mp4.boxes.impl;

import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;
import java.util.Date;
import net.sourceforge.jaad.mp4.boxes.Utils;

/**
 * The media header declares overall information that is media-independent, and relevant to characteristics of
 * the media in a track.
 */
public class MediaHeaderBox extends FullBox {

	private Date creationTime, modificationTime;
	private long timeScale, duration;
	private String language;

	public MediaHeaderBox() {
		super("Media Header Box", "mdhd");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);
		if(version==1) {
			creationTime = Utils.getDate(in.readBytes(8));
			modificationTime = Utils.getDate(in.readBytes(8));
			timeScale = in.readBytes(4);
			duration = in.readBytes(8);
		}
		else {
			creationTime = Utils.getDate(in.readBytes(4));
			modificationTime = Utils.getDate(in.readBytes(4));
			timeScale = in.readBytes(4);
			duration = in.readBytes(4);
		}

		//1 bit padding, 5*3 bits language code (ISO-639-2/T)
		final long l = in.readBytes(2);
		char[] c = new char[3];
		c[0] = (char) (((l>>10)&31)+0x60);
		c[1] = (char) (((l>>5)&31)+0x60);
		c[2] = (char) ((l&31)+0x60);
		language = new String(c);

		in.skipBytes(2); //pre-defined: 0

		left = 0;
	}

	/**
	 * The creation time is an integer that declares the creation time of the
	 * presentation in seconds since midnight, Jan. 1, 1904, in UTC time.
	 * @return the creation time
	 */
	public Date getCreationTime() {
		return creationTime;
	}

	/**
	 * The modification time is an integer that declares the most recent time
	 * the presentation was modified in seconds since midnight, Jan. 1, 1904,
	 * in UTC time.
	 */
	public Date getModificationTime() {
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
	 * The duration is an integer that declares length of the presentation (in
	 * the indicated timescale). This property is derived from the
	 * presentation's tracks: the value of this field corresponds to the
	 * duration of the longest track in the presentation.
	 * @return the duration of the longest track
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
