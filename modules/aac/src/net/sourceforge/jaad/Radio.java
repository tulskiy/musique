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
package net.sourceforge.jaad;

import java.io.InputStream;
import java.net.URL;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.SourceDataLine;

/**
 * Command line example, that can decode an AAC file and play it.
 * @author in-somnia
 */
public class Radio {

	private static final String USAGE = "usage:\njaad.Radio <url>";

	public static void main(String[] args) {
		try {
			if(args.length<1) printUsage();
			else decode(args[0]);
		}
		catch(Exception e) {
			System.err.println("error while decoding: "+e.toString());
		}
	}

	private static void printUsage() {
		System.out.println(USAGE);
		System.exit(1);
	}

	private static void decode(String arg) throws Exception {
		final SampleBuffer buf = new SampleBuffer();

		SourceDataLine line = null;
		byte[] b;
		try {
			final URL url = new URL(arg);
			final InputStream in = url.openStream();
			final DecoderConfig conf = DecoderConfig.parseTransportHeader(in, DecoderConfig.MAXIMUM_FRAME_SIZE);
			final Decoder dec = new Decoder(conf);
			while(true) {
				if(!dec.decodeFrame(buf)) break;

				final AudioFormat aufmt = new AudioFormat(buf.getSampleRate(), buf.getBitsPerSample(), buf.getChannels(), true, true);
				if(line!=null&&!line.getFormat().matches(aufmt)) {
					//format has changed (e.g. SBR has started)
					line.stop();
					line.close();
					line = null;
				}
				if(line==null) {
					line = AudioSystem.getSourceDataLine(aufmt);
					line.open();
					line.start();
				}
				b = buf.getData();
				line.write(b, 0, b.length);
			}
		}
		finally {
			if(line!=null) {
				line.stop();
				line.close();
			}
		}
	}
}
