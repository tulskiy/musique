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
package net.sourceforge.jaad.mp4.boxes;

import java.util.logging.Level;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.impl.*;
import net.sourceforge.jaad.mp4.boxes.impl.meta.*;

import java.io.IOException;
import java.util.logging.Logger;

public class BoxFactory implements BoxTypes {

	private static final Logger LOGGER = Logger.getLogger("net.sourceforge.jaad.util.mp4.boxes.BoxFactory");

	public static Box parseBox(ContainerBox parent, MP4InputStream in) throws IOException {
		long size = in.readBytes(4);
		long left = size-4;
		if(size==1) {
			size = in.readBytes(8);
			left -= 8;
		}
		long type = in.readBytes(4);
		left -= 4;
		if(type==EXTENDED_TYPE) {
			type = in.readBytes(16);
			left -= 16;
		}

		final BoxImpl box = forType(type);

		//DEBUG
		//System.out.println(box.getShortName());
		//

		box.setParams(size, type, parent, left);
		box.decode(in);
		//if mdat found, don't skip
		left = box.getLeft();
		if(left<0) LOGGER.log(Level.WARNING, "box: {0}, left: {1}", new String[]{box.getShortName(), Long.toString(left)});
		if(box.getType()!=MEDIA_DATA_BOX) in.skipBytes(left);
		return box;
	}

	//TODO: do this without reflection?!
	public static Box parseBox(MP4InputStream in, Class<? extends BoxImpl> boxClass) throws IOException {
		long size = in.readBytes(4);
		long left = size-4;
		if(size==1) {
			size = in.readBytes(8);
			left -= 8;
		}
		long type = in.readBytes(4);
		left -= 4;
		if(type==EXTENDED_TYPE) {
			type = in.readBytes(16);
			left -= 16;
		}

		BoxImpl box = null;
		try {
			box = boxClass.newInstance();
		}
		catch(InstantiationException ex) {
		}
		catch(IllegalAccessException ex) {
		}

		if(box!=null) {
			//DEBUG
			//System.out.println(box.getShortName());
			//

			box.setParams(size, type, null, left);
			box.decode(in);
			in.skipBytes(box.getLeft());
		}
		return box;
	}

	//TODO: this is ugly!
	private static BoxImpl forType(long type) {
		BoxImpl box;
		switch((int) type) {
			//file structure
			case FILE_TYPE_BOX:
				box = new FileTypeBox();
				break;
			case MEDIA_DATA_BOX:
				box = new MediaDataBox();
				break;
			case FREE_SPACE_BOX:
			case SKIP_BOX:
				box = new FreeSpaceBox(type);
				break;
			case PROGRESSIVE_DOWNLOAD_INFORMATION_BOX:
				box = new ProgressiveDownloadInformationBox();
				break;
			//movie structure
			case MOVIE_BOX:
				box = new ContainerBoxImpl("Movie Box", "moov");
				break;
			case MOVIE_HEADER_BOX:
				box = new MovieHeaderBox();
				break;
			//track structure
			case TRACK_BOX:
				box = new ContainerBoxImpl("Track Box", "trak");
				break;
			case TRACK_HEADER_BOX:
				box = new TrackHeaderBox();
				break;
			case TRACK_REFERENCE_BOX:
				box = new TrackReferenceBox();
				break;
			//track media structure
			case MEDIA_BOX:
				box = new ContainerBoxImpl("Media Box", "mdia");
				break;
			case MEDIA_HEADER_BOX:
				box = new MediaHeaderBox();
				break;
			case HANDLER_BOX:
				box = new HandlerBox();
				break;
			case MEDIA_INFORMATION_BOX:
				box = new ContainerBoxImpl("Media Information Box", "minf");
				break;
			case VIDEO_MEDIA_HEADER_BOX:
				box = new VideoMediaHeaderBox();
				break;
			case SOUND_MEDIA_HEADER_BOX:
				box = new SoundMediaHeaderBox();
				break;
			case HINT_MEDIA_HEADER_BOX:
				box = new HintMediaHeaderBox();
				break;
			case NULL_MEDIA_HEADER_BOX:
				box = new FullBox("Null Media Header Box", "nmhd");
				break;
			//sample tables
			case SAMPLE_TABLE_BOX:
				box = new ContainerBoxImpl("Sample Table", "stbl");
				break;
			case SAMPLE_DESCRIPTION_BOX:
				box = new SampleDescriptionBox();
				break;
			case DEGRADATION_PRIORITY_BOX:
				box = new DegradationPriorityBox();
				break;
			case SAMPLE_SCALE_BOX:
				box = new SampleScaleBox();
				break;
			//track time structures
			case TIME_TO_SAMPLE_BOX:
				box = new TimeToSampleBox();
				break;
			case SYNC_SAMPLE_BOX:
				box = new SyncSampleBox();
				break;
			case SHADOW_SYNC_SAMPLE_BOX:
				box = new ShadowSyncSampleBox();
				break;
			case SAMPLE_DEPENDENCY_TYPE_BOX:
				box = new SampleDependencyTypeBox();
				break;
			case EDIT_BOX:
				box = new ContainerBoxImpl("Edit Box", "edts");
				break;
			case EDIT_LIST_BOX:
				box = new EditListBox();
				break;
			//track data layout boxes
			case DATA_INFORMATION_BOX:
				box = new ContainerBoxImpl("Data Information Box", "dinf");
				break;
			case DATA_REFERENCE_BOX:
				box = new DataReferenceBox();
				break;
			case DATA_ENTRY_URL_BOX:
				box = new DataEntryUrlBox();
				break;
			case DATA_ENTRY_URN_BOX:
				box = new DataEntryUrnBox();
				break;
			case SAMPLE_SIZE_BOX:
				box = new SampleSizeBox();
				break;
			case SAMPLE_TO_CHUNK_BOX:
				box = new SampleToChunkBox();
				break;
			case CHUNK_OFFSET_BOX:
			case CHUNK_LARGE_OFFSET_BOX:
				box = new ChunkOffsetBox();
				break;
			case PADDING_BIT_BOX:
				box = new PaddingBitBox();
				break;
			case SUB_SAMPLE_INFORMATION_BOX:
				box = new SubSampleInformationBox();
				break;
			//movie fragments
			case MOVIE_EXTENDS_BOX:
				box = new ContainerBoxImpl("Movie Extends Box", "mvex");
				break;
			case MOVIE_EXTENDS_HEADER_BOX:
				box = new MovieExtendsHeaderBox();
				break;
			case TRACK_EXTENDS_BOX:
				box = new TrackExtendsBox();
				break;
			case MOVIE_FRAGMENT_BOX:
				box = new ContainerBoxImpl("Movie Fragment Box", "moof");
				break;
			case MOVIE_FRAGMENT_HEADER_BOX:
				box = new MovieFragmentHeaderBox();
				break;
			case TRACK_FRAGMENT_BOX:
				box = new ContainerBoxImpl("Track Fragment Box", "traf");
				break;
			//sample group structures
			case SAMPLE_TO_GROUP_BOX:
				box = new SampleToGroupBox();
				break;
			case SAMPLE_GROUP_DESCRIPTION_BOX:
				box = new SampleGroupDescriptionBox();
				break;
			//user data
			case USER_DATA_BOX:
				box = new ContainerBoxImpl("User Data Box", "udta");
				break;
			case COPYRIGHT_BOX:
				box = new CopyrightBox();
				break;
			case TRACK_SELECTION_BOX:
				box = new TrackSelectionBox();
				break;
			//meta data support
			case META_BOX:
				box = new MetaBox();
				break;
            case ILST_BOX:
                box = new IlstBox();
                break;
            case REVERSE_BOX:
                box = new ReverseBox();
                break;
            case MEAN_BOX:
                box = new MeanBox();
                break;
            case NAME_BOX:
                box = new NameBox();
                break;
            case DATA_BOX:
                box = new DataBox();
                break;
            case TOOL_BOX:
                box = new ToolBox();
                break;
			case XML_BOX:
				box = new XMLBox();
				break;
			case BINARY_XML_BOX:
				box = new BinaryXMLBox();
				break;
			case ITEM_LOCATION_BOX:
				box = new ItemLocationBox();
				break;
			case PRIMARY_ITEM_BOX:
				box = new PrimaryItemBox();
				break;
			case ITEM_PROTECTION_BOX:
				box = new ItemProtectionBox();
				break;
			case ITEM_INFORMATION_BOX:
				box = new ItemInformationBox();
				break;
			case ITEM_INFORMATION_ENTRY:
				box = new ItemInformationEntry();
				break;
			case ADDITIONAL_METADATA_CONTAINER_BOX:
				box = new ContainerBoxImpl("Additional Metadata Container Box", "meco");
				break;
			case META_BOX_RELATION_BOX:
				box = new MetaBoxRelationBox();
				break;
			//
			case BIT_RATE_BOX:
				box = new BitRateBox();
				break;
			case ESD_BOX:
				box = new ESDBox();
				break;
			default:
                String name = longToString(type);
//                LOGGER.log(Level.INFO, "unknown box type: {0}", name);
				box = new UnknownBox(name);
		}
		return box;
	}

	//debugging method
	private static String longToString(long l) {
		StringBuilder sb = new StringBuilder();
		sb.append((char) ((l>>24)&0xFF));
		sb.append((char) ((l>>16)&0xFF));
		sb.append((char) ((l>>8)&0xFF));
		sb.append((char) (l&0xFF));
		return sb.toString();
	}
}
