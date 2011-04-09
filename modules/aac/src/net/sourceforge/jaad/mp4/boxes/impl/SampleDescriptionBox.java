/*
 * Copyright (C) 2010 in-somnia
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl;

import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.BoxFactory;
import net.sourceforge.jaad.mp4.boxes.BoxImpl;
import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.ContainerBox;
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.boxes.impl.sampleentries.*;
import java.io.IOException;

/**
 * The sample description table gives detailed information about the coding type
 * used, and any initialization information needed for that coding.
 * @author in-somnia
 */
public class SampleDescriptionBox extends FullBox implements BoxTypes {

	private SampleEntry[] sampleEntries;

	public SampleDescriptionBox() {
		super("Sample Description Box", "stsd");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		final int entryCount = (int) in.readBytes(4);
		left -= 4;
		sampleEntries = new SampleEntry[entryCount];

		final HandlerBox handler = (HandlerBox) (parent.getParent().getParent()).getChild(BoxTypes.HANDLER_BOX);
		final long handlerType = handler.getHandlerType();

		final Class<? extends BoxImpl> boxClass;
		switch((int) handlerType) {
			case HandlerBox.TYPE_VIDEO:
				boxClass = VideoSampleEntry.class;
				break;
			case HandlerBox.TYPE_SOUND:
				boxClass = AudioSampleEntry.class;
				break;
			case HandlerBox.TYPE_HINT:
				boxClass = HintSampleEntry.class;
				break;
			case HandlerBox.TYPE_META:
				if(type==TEXT_METADATA_SAMPLE_ENTRY) boxClass = TextMetadataSampleEntry.class;
				else if(type==XML_METADATA_SAMPLE_ENTRY) boxClass = XMLMetadataSampleEntry.class;
				else boxClass = null;
				break;
			default:
				boxClass = null;
		}

		if(boxClass!=null) {
			for(int i = 0; i<entryCount; i++) {
				sampleEntries[i] = (SampleEntry) BoxFactory.parseBox(in, boxClass);
				if(sampleEntries[i]!=null) left -= sampleEntries[i].getSize();
			}
		}
	}

	public SampleEntry[] getSampleEntries() {
		return sampleEntries;
	}
}
