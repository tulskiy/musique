/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.plugins.discogs.model;

import javax.swing.DefaultListModel;

/**
 * @author mliauchuk
 */
public abstract class DiscogsDefaultListModel extends DefaultListModel {

	public abstract Object getEx(int index);

	@Override
	public Object elementAt(int index) {
		return get(index);
	}

	@Override
	public Object getElementAt(int index) {
		return get(index);
	}

}
