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
package net.sourceforge.jaad.mp4.api.codec;

import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jaad.mp4.api.DecoderInfo;
import net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec.CodecSpecificBox;
import net.sourceforge.jaad.mp4.boxes.impl.sampleentries.codec.EAC3SpecificBox;

public class EAC3DecoderInfo extends DecoderInfo {

	private EAC3SpecificBox box;
	private IndependentSubstream[] is;

	public EAC3DecoderInfo(CodecSpecificBox box) {
		this.box = (EAC3SpecificBox) box;
		is = new IndependentSubstream[this.box.getIndependentSubstreamCount()];
		for(int i = 0; i<is.length; i++) {
			is[i] = new IndependentSubstream(i);
		}
	}

	public int getDataRate() {
		return box.getDataRate();
	}

	public IndependentSubstream[] getIndependentSubstreams() {
		return is;
	}

	public class IndependentSubstream {

		private final int index;
		private final DependentSubstream[] dependentSubstreams;

		private IndependentSubstream(int index) {
			this.index = index;

			final int loc = box.getDependentSubstreamLocation()[index];
			final List<DependentSubstream> list = new ArrayList<DependentSubstream>();
			for(int i = 0; i<9; i++) {
				if(((loc>>(8-i))&1)==1) list.add(DependentSubstream.values()[i]);
			}
			dependentSubstreams = list.toArray(new DependentSubstream[list.size()]);
		}

		public int getFscod() {
			return box.getFscods()[index];
		}

		public int getBsid() {
			return box.getBsids()[index];
		}

		public int getBsmod() {
			return box.getBsmods()[index];
		}

		public int getAcmod() {
			return box.getAcmods()[index];
		}

		public boolean isLfeon() {
			return box.getLfeons()[index];
		}

		public DependentSubstream[] getDependentSubstreams() {
			return dependentSubstreams;
		}
	}

	public enum DependentSubstream {

		LC_RC_PAIR,
		LRS_RRS_PAIR,
		CS,
		TS,
		LSD_RSD_PAIR,
		LW_RW_PAIR,
		LVH_RVH_PAIR,
		CVH,
		LFE2
	}
}
