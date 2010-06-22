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

package org.xiph.libvorbis.books.coupled.resbook_44;

import org.xiph.libvorbis.*;

public class res_books_stereo {

    public vorbis_mapping_template[] _mapres_template_44_stereo;


    public res_books_stereo() {

        vorbis_info_mapping0[] _map_nominal = new vorbis_info_mapping0[]{new vorbis_info_mapping0(1, new int[]{0, 0}, new int[]{0}, new int[]{0}, 1, new int[]{0}, new int[]{1}),
                new vorbis_info_mapping0(1, new int[]{0, 0}, new int[]{1}, new int[]{1}, 1, new int[]{0}, new int[]{1})};

        vorbis_info_residue0 _residue_44_low = new vorbis_info_residue0(0, -1, -1, 9, -1, new int[]{0}, new int[]{-1},
                new float[]{.5f, 1.5f, 2.5f, 2.5f, 4.5f, 8.5f, 16.5f, 32.5f},
                new float[]{.5f, .5f, .5f, 999.f, 4.5f, 8.5f, 16.5f, 32.5f});

        vorbis_info_residue0 _residue_44_mid = new vorbis_info_residue0(0, -1, -1, 10, -1, new int[]{0}, new int[]{-1},
                new float[]{.5f, 1.5f, 1.5f, 2.5f, 2.5f, 4.5f, 8.5f, 16.5f, 32.5f},
                new float[]{.5f, .5f, 999.f, .5f, 999.f, 4.5f, 8.5f, 16.5f, 32.5f});

        vorbis_info_residue0 _residue_44_high = new vorbis_info_residue0(0, -1, -1, 10, -1, new int[]{0}, new int[]{-1},
                new float[]{.5f, 1.5f, 2.5f, 4.5f, 8.5f, 16.5f, 32.5f, 71.5f, 157.5f},
                new float[]{.5f, 1.5f, 2.5f, 3.5f, 4.5f, 8.5f, 16.5f, 71.5f, 157.5f});


        resbook_44s_n1 _resbook_44s_n1 = new resbook_44s_n1();
        resbook_44sm_n1 _resbook_44sm_n1 = new resbook_44sm_n1();

        resbook_44s_0 _resbook_44s_0 = new resbook_44s_0();
        resbook_44sm_0 _resbook_44sm_0 = new resbook_44sm_0();

        resbook_44s_1 _resbook_44s_1 = new resbook_44s_1();
        resbook_44sm_1 _resbook_44sm_1 = new resbook_44sm_1();

        resbook_44s_2 _resbook_44s_2 = new resbook_44s_2();
        resbook_44s_3 _resbook_44s_3 = new resbook_44s_3();
        resbook_44s_4 _resbook_44s_4 = new resbook_44s_4();
        resbook_44s_5 _resbook_44s_5 = new resbook_44s_5();
        resbook_44s_6 _resbook_44s_6 = new resbook_44s_6();
        resbook_44s_7 _resbook_44s_7 = new resbook_44s_7();
        resbook_44s_8 _resbook_44s_8 = new resbook_44s_8();
        resbook_44s_9 _resbook_44s_9 = new resbook_44s_9();


        _mapres_template_44_stereo = new vorbis_mapping_template[]{

                // _res_44s_n1
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_low, _resbook_44s_n1._huff_book__44cn1_s_short, _resbook_44sm_n1._huff_book__44cn1_sm_short, _resbook_44s_n1.books, _resbook_44sm_n1.books),
                                new vorbis_residue_template(2, 0, _residue_44_low, _resbook_44s_n1._huff_book__44cn1_s_long, _resbook_44sm_n1._huff_book__44cn1_sm_long, _resbook_44s_n1.books, _resbook_44sm_n1.books)
                        }),

                // _res_44s_0
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_low, _resbook_44s_0._huff_book__44c0_s_short, _resbook_44sm_0._huff_book__44c0_sm_short, _resbook_44s_0.books, _resbook_44sm_0.books),
                                new vorbis_residue_template(2, 0, _residue_44_low, _resbook_44s_0._huff_book__44c0_s_long, _resbook_44sm_0._huff_book__44c0_sm_long, _resbook_44s_0.books, _resbook_44sm_0.books)
                        }),

                // _res_44s_1
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_low, _resbook_44s_1._huff_book__44c1_s_short, _resbook_44sm_1._huff_book__44c1_sm_short, _resbook_44s_1.books, _resbook_44sm_1.books),
                                new vorbis_residue_template(2, 0, _residue_44_low, _resbook_44s_1._huff_book__44c1_s_long, _resbook_44sm_1._huff_book__44c1_sm_long, _resbook_44s_1.books, _resbook_44sm_1.books)
                        }),

                // _res_44s_2
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_2._huff_book__44c2_s_short, _resbook_44s_2._huff_book__44c2_s_short, _resbook_44s_2.books, _resbook_44s_2.books),
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_2._huff_book__44c2_s_long, _resbook_44s_2._huff_book__44c2_s_long, _resbook_44s_2.books, _resbook_44s_2.books)
                        }),

                // _res_44s_3
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_3._huff_book__44c3_s_short, _resbook_44s_3._huff_book__44c3_s_short, _resbook_44s_3.books, _resbook_44s_3.books),
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_3._huff_book__44c3_s_long, _resbook_44s_3._huff_book__44c3_s_long, _resbook_44s_3.books, _resbook_44s_3.books)
                        }),

                // _res_44s_4
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_4._huff_book__44c4_s_short, _resbook_44s_4._huff_book__44c4_s_short, _resbook_44s_4.books, _resbook_44s_4.books),
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_4._huff_book__44c4_s_long, _resbook_44s_4._huff_book__44c4_s_long, _resbook_44s_4.books, _resbook_44s_4.books)
                        }),

                // _res_44s_5
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_5._huff_book__44c5_s_short, _resbook_44s_5._huff_book__44c5_s_short, _resbook_44s_5.books, _resbook_44s_5.books),
                                new vorbis_residue_template(2, 0, _residue_44_mid, _resbook_44s_5._huff_book__44c5_s_long, _resbook_44s_5._huff_book__44c5_s_long, _resbook_44s_5.books, _resbook_44s_5.books)
                        }),

                // _res_44s_6
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_6._huff_book__44c6_s_short, _resbook_44s_6._huff_book__44c6_s_short, _resbook_44s_6.books, _resbook_44s_6.books),
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_6._huff_book__44c6_s_long, _resbook_44s_6._huff_book__44c6_s_long, _resbook_44s_6.books, _resbook_44s_6.books)
                        }),

                // _res_44s_7
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_7._huff_book__44c7_s_short, _resbook_44s_7._huff_book__44c7_s_short, _resbook_44s_7.books, _resbook_44s_7.books),
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_7._huff_book__44c7_s_long, _resbook_44s_7._huff_book__44c7_s_long, _resbook_44s_7.books, _resbook_44s_7.books)
                        }),

                // _res_44s_8
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_8._huff_book__44c8_s_short, _resbook_44s_8._huff_book__44c8_s_short, _resbook_44s_8.books, _resbook_44s_8.books),
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_8._huff_book__44c8_s_long, _resbook_44s_8._huff_book__44c8_s_long, _resbook_44s_8.books, _resbook_44s_8.books)
                        }),

                // _res_44s_9
                new vorbis_mapping_template(_map_nominal,
                        new vorbis_residue_template[]{
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_9._huff_book__44c9_s_short, _resbook_44s_9._huff_book__44c9_s_short, _resbook_44s_9.books, _resbook_44s_9.books),
                                new vorbis_residue_template(2, 0, _residue_44_high, _resbook_44s_9._huff_book__44c9_s_long, _resbook_44s_9._huff_book__44c9_s_long, _resbook_44s_9.books, _resbook_44s_9.books)
                        }),
        };
    }
}
