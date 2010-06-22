/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
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

package org.xiph.libvorbis;

class vorbis_info_mode {

    int blockflag;
    int windowtype;
    int transformtype;
    int mapping;


    public vorbis_info_mode(int _blockflag, int _windowtype, int _transformtype, int _mapping) {

        blockflag = _blockflag;
        windowtype = _windowtype;
        transformtype = _transformtype;
        mapping = _mapping;
    }

    public vorbis_info_mode(vorbis_info_mode src) {

        this(src.blockflag, src.windowtype, src.transformtype, src.mapping);
    }
}
