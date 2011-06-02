package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * For a given handler, the primary data may be one of the referenced items when
 * it is desired that it be stored elsewhere, or divided into extents; or the
 * primary metadata may be contained in the meta-box (e.g. in an XML box).
 *
 * Either this box must occur, or there must be a box within the meta-box (e.g.
 * an XML box) containing the primary information in the format required by the
 * identified handler.
 *
 * @author in-somnia
 */
public class PrimaryItemBox extends FullBox {

	private int itemID;

	public PrimaryItemBox() {
		super("Primary Item Box", "pitm");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		itemID = (int) in.readBytes(2);
		left -= 2;
	}

	/**
	 * The item ID is the identifier of the primary item.
	 *
	 * @return the item ID
	 */
	public int getItemID() {
		return itemID;
	}
}
