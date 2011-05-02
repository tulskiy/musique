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
import java.util.ArrayList;
import java.util.List;
import net.sourceforge.jaad.mp4.MP4InputStream;

public class FullContainerBox extends FullBox implements ContainerBox {

	protected List<Box> children;

	protected FullContainerBox(String name, String shortName) {
		super(name, shortName);
		children = new ArrayList<Box>(4);
	}

	protected void readChildren(MP4InputStream in) throws IOException {
		Box box;
		while(left>0) {
			box = BoxFactory.parseBox(this, in);
			left -= box.getSize();
			if(box!=null) children.add(box);
		}
	}

	protected void readChildren(MP4InputStream in, long count) throws IOException {
		Box box;
		for(long l = 0; l<count; l++) {
			box = BoxFactory.parseBox(this, in);
			left -= box.getSize();
			if(box!=null) children.add(box);
		}
	}

	public Box getChild(int type) {
		return getChild(type, 0);
	}

	public Box getChild(int type, int num) {
		Box box = null, b = null;
		for(int i = 0; i<children.size(); i++) {
			b = children.get(i);
			if(b.getType()==type) {
				if(num>0) num--;
				else {
					box = b;
					break;
				}
			}
		}
		return box;
	}

	public boolean containsChild(int type) {
		boolean b = false;
		for(Box box : children) {
			if(box.getType()==type) {
				b = true;
				break;
			}
		}
		return b;
	}
}
