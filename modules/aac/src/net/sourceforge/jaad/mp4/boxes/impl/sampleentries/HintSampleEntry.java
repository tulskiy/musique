package net.sourceforge.jaad.mp4.boxes.impl.sampleentries;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;

public class HintSampleEntry extends SampleEntry {

	private byte[] data;

	public HintSampleEntry() {
		super("Hint Sample Entry", "hint");
		data = new byte[0];
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		data = new byte[(int) left];
		in.readBytes(data);
		left = 0;
	}

	public byte[] getData() {
		return data;
	}
}
