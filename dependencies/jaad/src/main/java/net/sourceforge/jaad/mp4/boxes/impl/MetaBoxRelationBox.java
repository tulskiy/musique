/*
 *  Copyright (C) 2011 in-somnia
 * 
 *  This file is part of JAAD.
 * 
 *  JAAD is free software; you can redistribute it and/or modify it 
 *  under the terms of the GNU Lesser General Public License as 
 *  published by the Free Software Foundation; either version 3 of the 
 *  License, or (at your option) any later version.
 *
 *  JAAD is distributed in the hope that it will be useful, but WITHOUT 
 *  ANY WARRANTY; without even the implied warranty of MERCHANTABILITY 
 *  or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General 
 *  Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library.
 *  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.jaad.mp4.boxes.impl;

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;

/**
 * The metabox relation box indicates a relation between two meta boxes at the
 * same level, i.e., the top level of the file, the Movie Box, or Track Box. The
 * relation between two meta boxes is unspecified if there is no metabox
 * relation box for those meta boxes. Meta boxes are referenced by specifying
 * their handler types.
 *
 * @author in-somnia
 */
public class MetaBoxRelationBox extends FullBox {

	private long firstMetaboxHandlerType, secondMetaboxHandlerType;
	private int metaboxRelation;

	public MetaBoxRelationBox() {
		super("Meta Box Relation Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		firstMetaboxHandlerType = in.readBytes(4);
		secondMetaboxHandlerType = in.readBytes(4);
		metaboxRelation = in.read();
	}

	/**
	 * The first meta box to be related.
	 */
	public long getFirstMetaboxHandlerType() {
		return firstMetaboxHandlerType;
	}

	/**
	 * The second meta box to be related.
	 */
	public long getSecondMetaboxHandlerType() {
		return secondMetaboxHandlerType;
	}

	/**
	 * The metabox relation indicates the relation between the two meta boxes.
	 * The following values are defined:
	 * <ol start="1">
	 * <li>The relationship between the boxes is unknown (which is the default
	 * when this box is not present)</li>
	 * <li>the two boxes are semantically un-related (e.g., one is presentation,
	 * the other annotation)</li>
	 * <li>the two boxes are semantically related but complementary (e.g., two
	 * disjoint sets of meta-data expressed in two different meta-data systems)
	 * </li>
	 * <li>the two boxes are semantically related but overlap (e.g., two sets of
	 * meta-data neither of which is a subset of the other); neither is
	 * 'preferred' to the other</li>
	 * <li>the two boxes are semantically related but the second is a proper
	 * subset or weaker version of the first; the first is preferred</li>
	 * <li>the two boxes are semantically related and equivalent (e.g., two
	 * essentially identical sets of meta-data expressed in two different
	 * meta-data systems)</li>
	 * </ol>
	 */
	public int getMetaboxRelation() {
		return metaboxRelation;
	}
}
