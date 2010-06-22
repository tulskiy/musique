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

public class encode_aux_pigeonhole {

    float min;
    float del;

    int mapentries;
    int quantvals;
    int[] pigeonmap;    // long *pigeonmap

    int fittotal;        // long fittotal
    int[] fitlist;        // long *fitlist
    int[] fitmap;        // long *fitmap
    int[] fitlength;    // long *fitlength


    public encode_aux_pigeonhole(float _min, float _del, int _mapentries, int _quantvals, int[] _pigeonmap, int _fittotal, int[] _fitlist, int[] _fitmap, int[] _fitlength) {

        min = _min;
        del = _del;

        mapentries = _mapentries;
        quantvals = _quantvals;

        if (_pigeonmap == null)
            pigeonmap = null;
        else
            pigeonmap = (int[]) _pigeonmap.clone();

        fittotal = _fittotal;

        if (_fitlist == null)
            fitlist = null;
        else
            fitlist = (int[]) _fitlist.clone();

        if (_fitmap == null)
            fitmap = null;
        else
            fitmap = (int[]) _fitmap.clone();

        if (_fitlength == null)
            fitlength = null;
        else
            fitlength = (int[]) _fitlength.clone();
    }

    public encode_aux_pigeonhole(encode_aux_pigeonhole src) {

        this(src.min, src.del, src.mapentries, src.quantvals, src.pigeonmap, src.fittotal, src.fitlist, src.fitmap, src.fitlength);
    }
}