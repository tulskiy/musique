package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

public class PixelAspectRatioBox extends BoxImpl {

	private long hSpacing;
	private long vSpacing;

	public PixelAspectRatioBox() {
		super("Pixel Aspect Ratio Box", "pasp");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		hSpacing = in.readBytes(4);
		vSpacing = in.readBytes(4);
	}

	public long getHorizontalSpacing() {
		return hSpacing;
	}

	public long getVerticalSpacing() {
		return vSpacing;
	}
}
