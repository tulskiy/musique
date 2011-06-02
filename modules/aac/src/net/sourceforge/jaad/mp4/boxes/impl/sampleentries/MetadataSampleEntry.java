package net.sourceforge.jaad.mp4.boxes.impl.sampleentries;

import net.sourceforge.jaad.mp4.MP4InputStream;
import java.io.IOException;

abstract class MetadataSampleEntry extends SampleEntry {

	private String contentEncoding;

	MetadataSampleEntry(String name, String shortName) {
		super(name, shortName);
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		contentEncoding = in.readUTFString((int) left, MP4InputStream.UTF8);
		left -= contentEncoding.length()+1;
	}

	/**
	 * A string providing a MIME type which identifies the content encoding of
	 * the timed metadata. If not present (an empty string is supplied) the
	 * timed metadata is not encoded.
	 * An example for this field is 'application/zip'.
	 * @return the encoding's MIME-type
	 */
	public String getContentEncoding() {
		return contentEncoding;
	}
}
