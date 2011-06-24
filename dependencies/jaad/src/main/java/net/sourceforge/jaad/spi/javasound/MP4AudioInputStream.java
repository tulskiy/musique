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
package net.sourceforge.jaad.spi.javasound;

import net.sourceforge.jaad.Decoder;
import net.sourceforge.jaad.DecoderConfig;
import net.sourceforge.jaad.SampleBuffer;
import net.sourceforge.jaad.mp4.AudioFrame;
import net.sourceforge.jaad.mp4.MP4Reader;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;

class MP4AudioInputStream extends AsynchronousAudioInputStream {

	private final MP4Reader mp4;
	private final Decoder decoder;
	private final SampleBuffer sampleBuffer;
	private AudioFormat audioFormat;
	private byte[] saved;

	MP4AudioInputStream(InputStream in, AudioFormat format, long length) throws IOException {
		super(in, format, length);
		mp4 = new MP4Reader(in);
		final DecoderConfig conf = DecoderConfig.parseMP4DecoderSpecificInfo(mp4.getDecoderSpecificInfo());
		decoder = new Decoder(conf);
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
		if(!mp4.hasMoreFrames()) {
			buffer.close();
			return;
		}
		try {
			final AudioFrame frame = mp4.readNextFrame();
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
