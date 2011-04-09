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

import net.sourceforge.jaad.AACException;
import net.sourceforge.jaad.Decoder;
import net.sourceforge.jaad.DecoderConfig;
import net.sourceforge.jaad.SampleBuffer;
import java.io.IOException;
import java.io.InputStream;
import javax.sound.sampled.AudioFormat;

class AACAudioInputStream extends AsynchronousAudioInputStream {

	private final Decoder decoder;
	private final SampleBuffer sampleBuffer;
	private AudioFormat format = null;
	private byte[] saved;

	AACAudioInputStream(InputStream in, AudioFormat format, long length) throws IOException {
		super(in, format, length);
		final DecoderConfig conf = DecoderConfig.parseTransportHeader(in, DecoderConfig.MAXIMUM_FRAME_SIZE);
		decoder = new Decoder(conf);
		sampleBuffer = new SampleBuffer();
	}

	@Override
	public AudioFormat getFormat() {
		if(format==null) {
			//read first frame
			try {
				if(!decoder.decodeFrame(sampleBuffer)) return null;
				format = new AudioFormat(sampleBuffer.getSampleRate(), sampleBuffer.getBitsPerSample(), sampleBuffer.getChannels(), true, true);
				saved = sampleBuffer.getData();
			}
			catch(AACException ex) {
				return null;
			}
		}
		return format;
	}

	public void execute() {
		try {
			if(saved==null) {
				if(!decoder.decodeFrame(sampleBuffer)) {
					buffer.close();
					return;
				}
				buffer.write(sampleBuffer.getData());
			}
			else {
				buffer.write(saved);
				saved = null;
			}
		}
		catch(IOException e) {
			buffer.close();
			return;
		}
	}
}
