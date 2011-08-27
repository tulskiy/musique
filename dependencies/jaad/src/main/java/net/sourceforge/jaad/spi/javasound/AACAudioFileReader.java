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

import net.sourceforge.jaad.adts.ADTSDemultiplexer;
import net.sourceforge.jaad.aac.syntax.BitStream;
import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import javax.sound.sampled.AudioFileFormat;
import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import javax.sound.sampled.spi.AudioFileReader;

public class AACAudioFileReader extends AudioFileReader {

	public static final AudioFileFormat.Type AAC = new AudioFileFormat.Type("AAC", "aac");
	public static final AudioFileFormat.Type MP4 = new AudioFileFormat.Type("MP4", "mp4");
	private static final AudioFormat.Encoding AAC_ENCODING = new AudioFormat.Encoding("AAC");

	@Override
	public AudioFileFormat getAudioFileFormat(InputStream in) throws UnsupportedAudioFileException, IOException {
		try {
			if(!in.markSupported()) in = new BufferedInputStream(in);
			in.mark(4);
			return getAudioFileFormat(in, AudioSystem.NOT_SPECIFIED);
		}
		finally {
			in.reset();
		}
	}

	@Override
	public AudioFileFormat getAudioFileFormat(URL url) throws UnsupportedAudioFileException, IOException {
		final InputStream in = url.openStream();
		try {
			return getAudioFileFormat(in);
		}
		finally {
			if(in!=null) in.close();
		}
	}

	@Override
	public AudioFileFormat getAudioFileFormat(File file) throws UnsupportedAudioFileException, IOException {
		InputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(file));
			in.mark(1000);
			final AudioFileFormat aff = getAudioFileFormat(in, (int) file.length());
			in.reset();
			return aff;
		}
		finally {
			if(in!=null) in.close();
		}
	}

	private AudioFileFormat getAudioFileFormat(InputStream in, int mediaLength) throws UnsupportedAudioFileException, IOException {
		final byte[] b = new byte[8];
		in.read(b);
		boolean canHandle = false;
		if(new String(b, 4, 4).equals("ftyp")) canHandle = true;
		else {
			final BitStream bit = new BitStream(b);
			try {
				ADTSDemultiplexer adts = new ADTSDemultiplexer(in);
				canHandle = true;
			}
			catch(Exception e) {
				canHandle = false;
			}
		}
		if(canHandle) {
			final AudioFormat format = new AudioFormat(AAC_ENCODING, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, mediaLength, AudioSystem.NOT_SPECIFIED, AudioSystem.NOT_SPECIFIED, true);
			return new AudioFileFormat(AAC, format, AudioSystem.NOT_SPECIFIED);
		}
		else throw new UnsupportedAudioFileException();
	}

	//================================================
	@Override
	public AudioInputStream getAudioInputStream(InputStream in) throws UnsupportedAudioFileException, IOException {
		try {
			if(!in.markSupported()) in = new BufferedInputStream(in);
			in.mark(1000);
			final AudioFileFormat aff = getAudioFileFormat(in, AudioSystem.NOT_SPECIFIED);
			in.reset();
			return new MP4AudioInputStream(in, aff.getFormat(), aff.getFrameLength());
		}
		catch(UnsupportedAudioFileException e) {
			in.reset();
			throw e;
		}
		catch(IOException e) {
			in.reset();
			throw e;
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(URL url) throws UnsupportedAudioFileException, IOException {
		final InputStream in = url.openStream();
		try {
			return getAudioInputStream(in);
		}
		catch(UnsupportedAudioFileException e) {
			if(in!=null) in.close();
			throw e;
		}
		catch(IOException e) {
			if(in!=null) in.close();
			throw e;
		}
	}

	@Override
	public AudioInputStream getAudioInputStream(File file) throws UnsupportedAudioFileException, IOException {
		final InputStream in = new FileInputStream(file);
		try {
			return getAudioInputStream(in);
		}
		catch(UnsupportedAudioFileException e) {
			if(in!=null) in.close();
			throw e;
		}
		catch(IOException e) {
			if(in!=null) in.close();
			throw e;
		}
	}
}
