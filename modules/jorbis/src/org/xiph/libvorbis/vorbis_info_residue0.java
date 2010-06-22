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

public class vorbis_info_residue0 {

    // block-partitioned VQ coded straight residue
    int begin;            // long begin
    int end;            // long end

    // first stage (lossless partitioning)
    int grouping;        // group n vectors per partition
    int partitions;        // possible codebooks for a partition
    int groupbook;        // huffbook for partitioning
    int[] secondstages;    // expanded out to pointers in lookup   secondstages[64]
    int[] booklist;        // list of second stage books  booklist[256]

    float[] classmetric1;    // classmetric1[64]
    float[] classmetric2;    // classmetric2[64]


    public vorbis_info_residue0(int _begin, int _end, int _grouping, int _partitions, int _groupbook, int[] _secondstages, int[] _booklist, float[] _classmetric1, float[] _classmetric2) {

        begin = _begin;
        end = _end;

        grouping = _grouping;
        partitions = _partitions;
        groupbook = _groupbook;

        secondstages = new int[64];
        System.arraycopy(_secondstages, 0, secondstages, 0, _secondstages.length);

        booklist = new int[256];
        System.arraycopy(_booklist, 0, booklist, 0, _booklist.length);

        classmetric1 = new float[64];
        System.arraycopy(_classmetric1, 0, classmetric1, 0, _classmetric1.length);

        classmetric2 = new float[64];
        System.arraycopy(_classmetric2, 0, classmetric2, 0, _classmetric2.length);
    }

    public vorbis_info_residue0(vorbis_info_residue0 src) {

        this(src.begin, src.end, src.grouping, src.partitions, src.groupbook, src.secondstages, src.booklist, src.classmetric1, src.classmetric2);
    }
}