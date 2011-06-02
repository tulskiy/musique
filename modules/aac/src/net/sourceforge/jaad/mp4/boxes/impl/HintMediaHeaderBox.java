package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The hint media header contains general information, independent of the
 * protocol, for hint tracks.
 *
 * @author in-somnia
 */
public class HintMediaHeaderBox extends FullBox {

	private long maxPDUsize, avgPDUsize, maxBitrate, avgBitrate;

	public HintMediaHeaderBox() {
		super("Hint Media Header Box", "hmhd");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		maxPDUsize = in.readBytes(2);
		avgPDUsize = in.readBytes(2);

		maxBitrate = in.readBytes(4);
		avgBitrate = in.readBytes(4);

		in.skipBytes(4); //reserved

		left -= 16;
	}

	/**
	 * The maximum PDU size gives the size in bytes of the largest PDU (protocol
	 * data unit) in this hint stream.
	 */
	public long getMaxPDUsize() {
		return maxPDUsize;
	}

	/**
	 * The average PDU size gives the average size of a PDU over the entire
	 * presentation.
	 */
	public long getAveragePDUsize() {
		return avgPDUsize;
	}

	/**
	 * The maximum bitrate gives the maximum rate in bits/second over any window
	 * of one second.
	 */
	public long getMaxBitrate() {
		return maxBitrate;
	}

	/**
	 * The average bitrate gives the average rate in bits/second over the entire
	 * presentation.
	 */
	public long getAverageBitrate() {
		return avgBitrate;
	}
}
