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

public interface BoxTypes {

	int EXTENDED_TYPE = 1970628964; //uuid
	//box types
	int ADDITIONAL_METADATA_CONTAINER_BOX = 1835361135; //meco
	int AUDIO_SAMPLE_ENTRY_BOX = 1836069985; //mp4a
	int BINARY_XML_BOX = 1652059500; //bxml
	int BIT_RATE_BOX = 1651798644; //btrt
	int CHUNK_OFFSET_BOX = 1937007471; //stco
	int CHUNK_LARGE_OFFSET_BOX = 1668232756; //co64
	int CLEAN_APERTURE_BOX = 1668047216; //clap
	int COPYRIGHT_BOX = 1668313716; //cprt
	int DATA_ENTRY_URN_BOX = 1970433568; //urn
	int DATA_ENTRY_URL_BOX = 1970433056; //url
	int DATA_INFORMATION_BOX = 1684631142; //dinf
	int DATA_REFERENCE_BOX = 1685218662; //dref
	int DEGRADATION_PRIORITY_BOX = 1937007728; //stdp
	int EDIT_BOX = 1701082227; //edts
	int EDIT_LIST_BOX = 1701606260; //elst
	int ESD_BOX = 1702061171; //esds
	int FILE_TYPE_BOX = 1718909296; //ftyp
	int FREE_SPACE_BOX = 1718773093; //free
	int HANDLER_BOX = 1751411826; //hdlr
	int HINT_MEDIA_HEADER_BOX = 1752000612; //hmhd
	int ITEM_INFORMATION_BOX = 1768517222; //iinf
	int ITEM_INFORMATION_ENTRY = 1768842853; //infe
	int ITEM_LOCATION_BOX = 1768714083; //iloc
	int ITEM_PROTECTION_BOX = 1768977007; //ipro
	int MEDIA_BOX = 1835297121; //mdia
	int MEDIA_DATA_BOX = 1835295092; //mdat
	int MEDIA_HEADER_BOX = 1835296868; //mdhd
	int MEDIA_INFORMATION_BOX = 1835626086; //minf
	int META_BOX = 1835365473; //meta
    int ILST_BOX = 1768715124; //ilst
    int REVERSE_BOX = 757935405; //----
    int MEAN_BOX = 0x6D65616E; //mean
    int NAME_BOX = 0x6E616D65; //name
    int DATA_BOX = 0x64617461; //data
    int TOOL_BOX = 0xa9746f6f; //©too
	int META_BOX_RELATION_BOX = 1835364965; //mere
	int MOVIE_BOX = 1836019574; //moov
	int MOVIE_EXTENDS_BOX = 1836475768; //mvex
	int MOVIE_EXTENDS_HEADER_BOX = 1835362404; //mehd
	int MOVIE_FRAGMENT_BOX = 1836019558; //moof
	int MOVIE_FRAGMENT_HEADER_BOX = 1835427940; //mfhd
	int MOVIE_HEADER_BOX = 1836476516; //mvhd
	int NULL_MEDIA_HEADER_BOX = 1852663908; //nmhd
	int PADDING_BIT_BOX = 1885430882; //padb
	int PRIMARY_ITEM_BOX = 1885959277; //pitm
	int PROGRESSIVE_DOWNLOAD_INFORMATION_BOX = 1885628782; //pdin
	int PIXEL_ASPECT_RATIO_BOX = 1885434736; //pasp
	int SAMPLE_DEPENDENCY_TYPE_BOX = 1935963248; //sdtp
	int SAMPLE_DESCRIPTION_BOX = 1937011556; //stsd
	int SAMPLE_GROUP_DESCRIPTION_BOX = 1936158820; //sgpd
	int SAMPLE_SCALE_BOX = 1937011564; //stsl
	int SAMPLE_SIZE_BOX = 1937011578; //stsz
	int SAMPLE_TABLE_BOX = 1937007212; //stbl
	int SAMPLE_TO_CHUNK_BOX = 1937011555; //stsc
	int SAMPLE_TO_GROUP_BOX = 1935828848; //sbgp
	int SHADOW_SYNC_SAMPLE_BOX = 1937011560; //stsh
	int SKIP_BOX = 1936419184; //skip
	int SOUND_MEDIA_HEADER_BOX = 1936549988; //smhd
	int SUB_SAMPLE_INFORMATION_BOX = 1937072755; //subs
	int SYNC_SAMPLE_BOX = 1937011571; //stss
	int TIME_TO_SAMPLE_BOX = 1937011827; //stts
	int TRACK_BOX = 1953653099; //trak
	int TRACK_EXTENDS_BOX = 1953654136; //trex
	int TRACK_FRAGMENT_BOX = 1953653094; //traf
	int TRACK_HEADER_BOX = 1953196132; //tkhd
	int TRACK_REFERENCE_BOX = 1953654118; //tref
	int TRACK_SELECTION_BOX = 1953719660; //tsel
	int USER_DATA_BOX = 1969517665; //udta
	int VIDEO_MEDIA_HEADER_BOX = 1986881636; //vmhd
	int XML_BOX = 2020437024; //xml
	//sample entries
	int TEXT_METADATA_SAMPLE_ENTRY = 1835365492; //mett
	int XML_METADATA_SAMPLE_ENTRY = 1835365496; //metx
}
