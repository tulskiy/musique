package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;
import net.sourceforge.jaad.mp4.boxes.BoxTypes;

public class FreeSpaceBox extends BoxImpl {

	public FreeSpaceBox(long type) {
		super("Free Space Box", (type==BoxTypes.FREE_SPACE_BOX) ? "free" : "skip");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		//no need to read, box will be skipped
	}
}
