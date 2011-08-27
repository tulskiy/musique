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
package net.sourceforge.jaad.mp4.api;

import net.sourceforge.jaad.mp4.api.Track.Codec;
import net.sourceforge.jaad.mp4.api.drm.ITunesProtection;
import net.sourceforge.jaad.mp4.boxes.Box;
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.impl.OriginalFormatBox;
import net.sourceforge.jaad.mp4.boxes.impl.SchemeTypeBox;

/**
 * This class contains information about a DRM system.
 */
public abstract class Protection {

	public static enum Scheme {

		ITUNES_FAIR_PLAY(1769239918),
		UNKNOWN(-1);
		private long type;

		private Scheme(long type) {
			this.type = type;
		}
	}

	static Protection parse(Box sinf) {
		Protection p = null;
		if(sinf.hasChild(BoxTypes.SCHEME_TYPE_BOX)) {
			final SchemeTypeBox schm = (SchemeTypeBox) sinf.getChild(BoxTypes.SCHEME_TYPE_BOX);
			final long l = schm.getSchemeType();
			if(l==Scheme.ITUNES_FAIR_PLAY.type) p = new ITunesProtection(sinf);
		}

		if(p==null) p = new UnknownProtection(sinf);
		return p;
	}
	private final Codec originalFormat;

	protected Protection(Box sinf) {
		//original format
		final long type = ((OriginalFormatBox) sinf.getChild(BoxTypes.ORIGINAL_FORMAT_BOX)).getOriginalFormat();
		Codec c;
		//TODO: currently it tests for audio and video codec, can do this any other way?
		if(!(c = AudioTrack.AudioCodec.forType(type)).equals(AudioTrack.AudioCodec.UNKNOWN_AUDIO_CODEC)) originalFormat = c;
		else if(!(c = VideoTrack.VideoCodec.forType(type)).equals(VideoTrack.VideoCodec.UNKNOWN_VIDEO_CODEC)) originalFormat = c;
		else originalFormat = null;
	}

	Codec getOriginalFormat() {
		return originalFormat;
	}

	public abstract Scheme getScheme();

	//default implementation for unknown protection schemes
	private static class UnknownProtection extends Protection {

		UnknownProtection(Box sinf) {
			super(sinf);
		}

		@Override
		public Scheme getScheme() {
			return Scheme.UNKNOWN;
		}
	}
}
