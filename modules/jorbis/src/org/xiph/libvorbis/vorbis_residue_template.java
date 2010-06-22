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

public class vorbis_residue_template {

    int res_type;
    int limit_type; // 0 lowpass limited, 1 point stereo limited

    vorbis_info_residue0 res;

    static_codebook book_aux;
    static_codebook book_aux_managed;
    static_bookblock books_base;
    static_bookblock books_base_managed;


    public vorbis_residue_template(int _res_type, int _limit_type, vorbis_info_residue0 _res,
                                   static_codebook _book_aux, static_codebook _book_aux_managed, static_bookblock _books_base, static_bookblock _books_base_managed) {

        res_type = _res_type;
        limit_type = _limit_type;

        res = _res;

        book_aux = _book_aux;
        book_aux_managed = _book_aux_managed;
        books_base = _books_base;
        books_base_managed = _books_base_managed;
    }

    public vorbis_residue_template(vorbis_residue_template src) {

        this(src.res_type, src.limit_type, new vorbis_info_residue0(src.res),
                new static_codebook(src.book_aux), new static_codebook(src.book_aux_managed), new static_bookblock(src.books_base), new static_bookblock(src.books_base_managed));
    }
}
