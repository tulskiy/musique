package net.sourceforge.jaad.mp4.boxes.impl;

import java.awt.Color;
import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The video media header contains general presentation information, independent
 * of the coding, for video media
 * @author in-somnia
 */
public class VideoMediaHeaderBox extends FullBox {

	private long graphicsMode;
	private Color color;

	public VideoMediaHeaderBox() {
		super("Video Media Header Box", "vmhd");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		graphicsMode = in.readBytes(2);

		//6 byte RGB color
		color = new Color(in.readBytes(2), in.readBytes(2), in.readBytes(2));
	}

	/**
	 * The graphics mode specifies a composition mode for this video track.
	 * Currently, only one mode is defined:
	 * '0': copy over the existing image
	 */
	public long getGraphicsMode() {
		return graphicsMode;
	}

	/**
	 * A color available for use by graphics modes.
	 */
	public Color getColor() {
		return color;
	}
}
