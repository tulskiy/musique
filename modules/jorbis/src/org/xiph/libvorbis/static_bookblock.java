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

public class static_bookblock {

    static_codebook[][] books;


    public static_bookblock(static_codebook[][] _books) {

        books = new static_codebook[12][3];

        for (int i = 0; i < _books.length; i++)
            System.arraycopy(_books[i], 0, books[i], 0, _books[i].length);
    }

    public static_bookblock(static_bookblock src) {

        this(src.books);
    }
}