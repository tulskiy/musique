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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.FullBox;
import net.sourceforge.jaad.mp4.od.Descriptor;
import net.sourceforge.jaad.mp4.od.ObjectDescriptor;

/**
 * The IPMPInfoBox contains IPMP Descriptors which document the protection
 * applied to the stream.
 *
 * The IPMP Descriptor is defined in ISO/IEC 14496-1. This is a part of the
 * MPEG-4 object descriptors (OD) that describe how an object can be accessed
 * and decoded. In the ISO Base Media File Format, IPMP Descriptor can be
 * carried directly in IPMPInfoBox without the need for OD stream.
 *
 * The presence of IPMP Descriptor in this IPMPInfoBox indicates the associated
 * media stream is protected by the IPMP Tool described in the IPMP Descriptor.
 *
 * Each IPMP Descriptor has an IPMP-toolID, which identifies the required IPMP
 * tool for protection. An independent registration authority (RA) is used so
 * any party can register its own IPMP Tool and identify this without
 * collisions.
 *
 * The IPMP Descriptor carries IPMP information for one or more IPMP Tool
 * instances, it includes but not limited to IPMP Rights Data, IPMP Key Data,
 * Tool Configuration Data, etc.
 *
 * More than one IPMP Descriptors can be carried in this IPMPInfoBox if this
 * media stream is protected by more than one IPMP Tools.
 *
 * @author in-somnia
 */
public class IPMPInfoBox extends FullBox {

	private List</*IPMP*/Descriptor> ipmpDescriptors;

	public IPMPInfoBox() {
		super("IPMP Info Box");
	}

	@Override
	public void decode(MP4InputStream in) throws IOException {
		super.decode(in);

		ipmpDescriptors = new ArrayList</*IPMP*/Descriptor>();
		/*IPMP*/Descriptor desc;
		while(getLeft(in)>0) {
			desc = (/*IPMP*/Descriptor) ObjectDescriptor.createDescriptor(in);
			ipmpDescriptors.add(desc);
		}
	}

	/**
	 * The contained list of IPMP descriptors.
	 *
	 * @return the IPMP descriptors
	 */
	public List</*IPMP*/Descriptor> getIPMPDescriptors() {
		return Collections.unmodifiableList(ipmpDescriptors);
	}
}
