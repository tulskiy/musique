package net.sourceforge.jaad.mp4.boxes.impl.samplegroupentries;

import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;

public abstract class SampleGroupDescriptionEntry extends BoxImpl {

	protected SampleGroupDescriptionEntry(String name, String shortName) {
		super(name, shortName);
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
	}
}
