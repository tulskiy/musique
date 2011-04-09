package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * When the primary data is in XML format and it is desired that the XML be
 * stored directly in the meta-box, either the XMLBox or the BinaryXMLBox is
 * used. The Binary XML Box may only be used when there is a single well-defined
 * binarization of the XML for that defined format as identified by the handler.
 *
 * @see XMLBox
 * @author in-somnia
 */
public class BinaryXMLBox extends FullBox {

	private byte[] data;

	public BinaryXMLBox() {
		super("Binary XML Box", "bxml");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		data = new byte[(int) left];
		in.readBytes(data);
		left = 0;
	}

	/**
	 * The binary data.
	 */
	public byte[] getData() {
		return data;
	}
}
