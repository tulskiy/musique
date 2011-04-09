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

import java.io.IOException;
import net.sourceforge.jaad.mp4.MP4InputStream;

public abstract class BoxImpl implements Box {

	private String name, shortName;
	protected long size, type, left;
	protected ContainerBox parent;

	protected BoxImpl(String name, String shortName) {
		this.name = name;
		this.shortName = shortName;
	}

	public void setParams(long size, long type, ContainerBox parent, long left) {
		this.size = size;
		this.type = type;
		this.parent = parent;
		this.left = left;
	}

	long getLeft() {
		return left;
	}

	/**
	 * Decodes the specified input stream by reading this box and all of its
	 * children (if any) and returns the number of bytes left in the box (which
	 * should be normally 0).
	 * @param in an input stream
	 * @throws IOException if an reading error occurs
	 */
	public abstract void decode(MP4InputStream in) throws IOException;

	public long getType() {
		return type;
	}

	public long getSize() {
		return size;
	}

	public ContainerBox getParent() {
		return parent;
	}

	public String getName() {
		return name;
	}

	public String getShortName() {
		return shortName;
	}

	@Override
	public String toString() {
		return name+" ["+shortName+"]";
	}

	public String toTreeString(int off) {
		StringBuilder sb = new StringBuilder();
		for(int i = 0; i<off; i++) {
			sb.append(" ");
		}
		sb.append(getShortName()+" ("+getName()+")");
		return sb.toString();
	}
}
