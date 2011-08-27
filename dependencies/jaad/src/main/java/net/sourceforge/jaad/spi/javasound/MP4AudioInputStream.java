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
package net.sourceforge.jaad.spi.javasound;

import net.sourceforge.jaad.aac.Decoder;
import net.sourceforge.jaad.aac.SampleBuffer;
import net.sourceforge.jaad.mp4.MP4Container;
import net.sourceforge.jaad.mp4.api.AudioTrack;
import net.sourceforge.jaad.mp4.api.Frame;
import net.sourceforge.jaad.mp4.api.Movie;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;
import javax.sound.sampled.AudioFormat;
import net.sourceforge.jaad.mp4.api.Track;

class MP4AudioInputStream extends AsynchronousAudioInputStream {

	private final AudioTrack track;
	private final Decoder decoder;
	private final SampleBuffer sampleBuffer;
	private AudioFormat audioFormat;
	private byte[] saved;

	MP4AudioInputStream(InputStream in, AudioFormat format, long length) throws IOException {
		super(in, format, length);
		final MP4Container cont = new MP4Container(in);
		final Movie movie = cont.getMovie();
		final List<Track> tracks = movie.getTracks(AudioTrack.AudioCodec.AAC);
		if(tracks.isEmpty()) throw new IOException("movie does not contain any AAC track");
		track = (AudioTrack) tracks.get(0);

		decoder = new Decoder(track.getDecoderSpecificInfo());
		sampleBuffer = new SampleBuffer();
	}

	@Override
	public AudioFormat getFormat() {
		if(audioFormat==null) {
			//read first frame
			decodeFrame();
			audioFormat = new AudioFormat(sampleBuffer.getSampleRate(), sampleBuffer.getBitsPerSample(), sampleBuffer.getChannels(), true, true);
			saved = sampleBuffer.getData();
		}
		return audioFormat;
	}

	public void execute() {
		if(saved==null) {
			decodeFrame();
			if(buffer.isOpen()) buffer.write(sampleBuffer.getData());
		}
		else {
			buffer.write(saved);
			saved = null;
		}
	}

	private void decodeFrame() {
		if(!track.hasMoreFrames()) {
			buffer.close();
			return;
		}
		try {
			final Frame frame = track.readNextFrame();
			if(frame==null) {
				buffer.close();
				return;
			}
			decoder.decodeFrame(frame.getData(), sampleBuffer);
		}
		catch(IOException e) {
			buffer.close();
			return;
		}
	}
}
