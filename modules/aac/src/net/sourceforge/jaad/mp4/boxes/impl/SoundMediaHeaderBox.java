package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The sound media header contains general presentation information, independent
 * of the coding, for audio media. This header is used for all tracks containing
 * audio.
 *
 * @author in-somnia
 */
public class SoundMediaHeaderBox extends FullBox {

	private double balance;

	public SoundMediaHeaderBox() {
		super("Sound Media Header Box", "smhd");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		balance = in.readFixedPoint(2, MP4InputStream.MASK8);
		in.skipBytes(2); //reserved
		left -= 4;
	}

	/**
	 * The balance is a floating-point number that places mono audio tracks in a
	 * stereo space: 0 is centre (the normal value), full left is -1.0 and full
	 * right is 1.0.
	 *
	 * @return the stereo balance for a mono track
	 */
	public double getBalance() {
		return balance;
	}
}
