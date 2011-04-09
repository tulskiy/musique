package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

public class DataEntryUrlBox extends FullBox {

	private String location;

	public DataEntryUrlBox() {
		super("Data Entry Url Box", "url ");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		if((flags&1)==0) {
			location = in.readUTFString((int) left, MP4InputStream.UTF8);
			left -= location.length()+1;
		}
	}

	public String getLocation() {
		return location;
	}
}
