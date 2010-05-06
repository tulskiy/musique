/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.playlist.formatting;

import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import static org.junit.Assert.assertEquals;
import org.junit.Before;
import org.junit.Test;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
public class ParserTest {
    Song s;
    Parser parser = new Parser();

    @Before
    public void setUp() {
        s = new Song();
    }

    @Test
    public void testBrackets() {
        Expression t = parser.parse("[%artist% - ]%title%");

        s.setTitle("title");
        assertEquals("title", t.eval(s));

        s.setArtist("artist");
        assertEquals("artist - title", t.eval(s));
    }

    @Test
    public void testIf3() {
        Expression t = parser.parse("$if3(%artist%, %title%, %albumArtist%, unknown)");

        s.setArtist("artist");
        assertEquals("artist", t.eval(s));
        s.setArtist(null);
        s.setTitle("title");
        assertEquals("title", t.eval(s));
        s.setTitle("");
        s.setAlbumArtist("album artist");
        assertEquals("album artist", t.eval(s));
        s.setAlbumArtist(null);

        assertEquals("unknown", t.eval(s));
    }

    @Test
    public void testIf1() {
        Expression t = parser.parse("$if1(%artist%,%artist%,%title%)");

        s.setArtist("artist");
        s.setTitle("title");
        assertEquals("artist", t.eval(s));

        s.setArtist(null);
        assertEquals("title", t.eval(s));
    }

    @Test
    public void testQuot() {
        Expression t = parser.parse("'%artist%'%title%");

        s.setArtist("artist here");
        s.setTitle("title here");

        assertEquals("%artist%title here", t.eval(s));
    }

    @Test
    public void testSmth() {
        Expression t = parser.parse("$if1($strcmp(%albumArtist%,%artist%),%artist%,$if3(%album%,Unknown))");

        s.setAlbumArtist("album artist");
        s.setYear("year");
        s.setAlbum("album");
        s.setDiscNumber("1");
        s.setTrackNumber("10");
        s.setArtist("artist");
        s.setTitle("title");

//        System.out.println(t.eval(s));
    }
}
