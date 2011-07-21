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

package com.tulskiy.musique.audio.player.dsp;

import org.junit.Test;

import java.util.ArrayList;

/**
 * Author: Denis Tulskiy
 * Date: 26.07.2009
 */
public class DSPSpeedTest {
    @Test public void dummy(){}
    //    @Test
    public void test() {
        ArrayList<String> l = new ArrayList<String>();
        String[] a = new String[1000000];
        int ac = 0;
        String test = "test";
        for (int i = 0; i < 1000000; i++) {
            l.add(test);
            a[ac++] = test;
        }

        long time = System.currentTimeMillis();
        for (int i = 0; i < l.size(); i++)
            l.get(i);
        System.out.println(System.currentTimeMillis() - time);

        time = System.currentTimeMillis();
        for (String i : a) {

        }
        System.out.println(System.currentTimeMillis() - time);
    }
}
