package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The Copyright box contains a copyright declaration which applies to the
 * entire presentation, when contained within the Movie Box, or, when contained
 * in a track, to that entire track. There may be multiple copyright boxes using
 * different language codes.
 */
public class CopyrightBox extends FullBox {

	private String languageCode, notice;

	public CopyrightBox() {
		super("Copyright Box", "cprt");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		//1 bit padding, 5*3 bits language code (ISO-639-2/T)
		long l = in.readBytes(2);
		char[] c = new char[3];
		c[0] = (char) (((l>>10)&31)+0x60);
		c[1] = (char) (((l>>5)&31)+0x60);
		c[2] = (char) ((l&31)+0x60);
		languageCode = new String(c);

		notice = in.readUTFString((int) left); //UTF8 or UTF16

		left -= 4+notice.length();
	}

	/**
	 * The language code for the following text. See ISO 639-2/T for the set of
	 * three character codes.
	 */
	public String getLanguageCode() {
		return languageCode;
	}

	/**
	 * The copyright notice.
	 */
	public String getNotice() {
		return notice;
	}
}
