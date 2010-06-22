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

public class encode_aux_nearestmatch {

    // pre-calculated partitioning tree

    int[] ptr0;        // long *ptr0
    int[] ptr1;        // long *ptr1

    int[] p;        // long *p // decision points (each is an entry)
    int[] q;        // long *q // decision points (each is an entry)
    int aux;        // long aux // number of tree entries
    int alloc;        // long alloc


    public encode_aux_nearestmatch(int[] _ptr0, int[] _ptr1, int[] _p, int[] _q, int _aux, int _alloc) {

        if (_ptr0 == null)
            ptr0 = null;
        else
            ptr0 = (int[]) _ptr0.clone();

        if (_ptr1 == null)
            ptr1 = null;
        else
            ptr1 = (int[]) _ptr1.clone();

        if (_p == null)
            p = null;
        else
            p = (int[]) _p.clone();

        if (_q == null)
            q = null;
        else
            q = (int[]) _q.clone();

        aux = _aux;
        alloc = _alloc;
    }

    public encode_aux_nearestmatch(encode_aux_nearestmatch src) {

        this(src.ptr0, src.ptr1, src.p, src.q, src.aux, src.alloc);
    }
}