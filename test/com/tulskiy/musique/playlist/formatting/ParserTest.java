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

package com.tulskiy.musique.playlist.formatting;

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;

import static org.junit.Assert.assertEquals;

import org.junit.Before;
import org.junit.Test;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class ParserTest {
    Track s;

    @Before
    public void setUp() {
        s = new Track();
    }

    @Test
    public void testBrackets() {
        Expression t = Parser.parse("[%artist% - ]%title%");

        s.setMeta("title", "title");
        assertEquals("title", t.eval(s));

        s.setMeta("artist", "artist");
        assertEquals("artist - title", t.eval(s));
    }

    @Test
    public void testIf3() {
        Expression t = Parser.parse("$if3(%artist%, %title%, %albumArtist%, unknown)");

        s.setMeta("artist", "artist");
        assertEquals("artist", t.eval(s));
        s.setMeta("artist", null);
        s.setMeta("title", ("title"));
        assertEquals("title", t.eval(s));
        s.setMeta("title", "");
        s.setMeta("albumArtist", "album artist");
        assertEquals("album artist", t.eval(s));
        s.setMeta("albumArtist", null);

        assertEquals("unknown", t.eval(s));
    }

    @Test
    public void testIf1() {
        Expression t = Parser.parse("$if1(%artist%,%artist%,%title%)");

        s.setMeta("artist", "artist");
        s.setMeta("title", ("title"));
        assertEquals("artist", t.eval(s));

        s.setMeta("artist", null);
        assertEquals("title", t.eval(s));
    }

    @Test
    public void testQuot() {
        Expression t = Parser.parse("'%artist%'%title%");

        s.setMeta("artist", "artist here");
        s.setMeta("title", "title here");

        assertEquals("%artist%title here", t.eval(s));
    }

    @Test
    public void testSmth() {
        Expression t = Parser.parse("$if1($strcmp(%albumArtist%,%artist%),%artist%,$if3(%album%,Unknown))");

        s.setMeta("albumArtist", "album artist");
        s.setMeta("year", "year");
        s.setMeta("album", "album");
        s.setDiscNumber("1");
        s.setTrackNumber("10");
        s.setMeta("artist", "artist");
        s.setMeta("title", ("title"));

//        System.out.println(t.eval(s));
    }
}
