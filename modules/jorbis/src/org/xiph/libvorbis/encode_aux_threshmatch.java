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

public class encode_aux_threshmatch {

    float[] quantthresh;
    int[] quantmap;            // long *quantmap
    int quantvals;
    int threshvals;


    public encode_aux_threshmatch(float[] _quantthresh, int[] _quantmap, int _quantvals, int _threshvals) {

        if (_quantthresh == null)
            quantthresh = null;
        else
            quantthresh = (float[]) _quantthresh.clone();

        if (_quantmap == null)
            quantmap = null;
        else
            quantmap = (int[]) _quantmap.clone();

        quantvals = _quantvals;
        threshvals = _threshvals;
    }

    public encode_aux_threshmatch(encode_aux_threshmatch src) {

        this(src.quantthresh, src.quantmap, src.quantvals, src.threshvals);
    }
}