/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */

package davaguine.jmac.info;

import davaguine.jmac.tools.JMACException;


/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class ID3Genre {
    private final static String genres[] = {"Blues", "Classic Rock", "Country", "Dance", "Disco", "Funk", "Grunge", "Hip-Hop",
                                            "Jazz", "Metal", "New Age", "Oldies", "Other", "Pop", "R&B", "Rap", "Reggae", "Rock", "Techno",
                                            "Industrial", "Alternative", "Ska", "Death Metal", "Pranks", "Soundtrack", "Euro-Techno", "Ambient",
                                            "Trip-Hop", "Vocal", "Jazz+Funk", "Fusion", "Trance", "Classical", "Instrumental", "Acid", "House", "Game",
                                            "Sound Clip", "Gospel", "Noise", "AlternRock", "Bass", "Soul", "Punk", "Space", "Meditative", "Instrumental Pop",
                                            "Instrumental Rock", "Ethnic", "Gothic", "Darkwave", "Techno-Industrial", "Electronic", "Pop-Folk", "Eurodance",
                                            "Dream", "Southern Rock", "Comedy", "Cult", "Gangsta", "Top 40", "Christian Rap", "Pop/Funk", "Jungle",
                                            "Native American", "Cabaret", "New Wave", "Psychadelic", "Rave", "Showtunes", "Trailer", "Lo-Fi", "Tribal",
                                            "Acid Punk", "Acid Jazz", "Polka", "Retro", "Musical", "Rock & Roll", "Hard Rock", "Folk", "Folk-Rock", "National Folk",
                                            "Swing", "Fast Fusion", "Bebop", "Latin", "Revival", "Celtic", "Bluegrass", "Avantgarde", "Gothic Rock", "Progressive Rock",
                                            "Psychedelic Rock", "Symphonic Rock", "Slow Rock", "Big Band", "Chorus", "Easy Listening", "Acoustic", "Humour",
                                            "Speech", "Chanson", "Opera", "Chamber Music", "Sonata", "Symphony", "Booty Bass", "Primus", "Porn Groove",
                                            "Satire", "Slow Jam", "Club", "Tango", "Samba", "Folklore", "Ballad", "Power Ballad", "Rhythmic Soul", "Freestyle",
                                            "Duet", "Punk Rock", "Drum Solo", "Acapella", "Euro-House", "Dance Hall", "Goa", "Drum & Bass", "Club House", "Hardcore",
                                            "Terror", "Indie", "BritPop", "Black Punk", "Polsk Punk", "Beat", "Christian Gangsta", "Heavy Metal", "Black Metal",
                                            "Crossover", "Contemporary C", "Christian Rock", "Merengue", "Salsa", "Thrash Metal", "Anime", "JPop", "SynthPop"};

    public final static int GENRE_UNDEFINED = 255;

    private int genre = GENRE_UNDEFINED;

    public String toString() {
        return genres[genre];
    }

    public final static String genreString(int genre) {
        if (genre != GENRE_UNDEFINED && (genre < 0 || genre >= genres.length))
            throw new JMACException("Wrong Genre");
        return genres[genre];
    }

    public final static int genreCount() {
        return genres.length;
    }

    public ID3Genre(final int genre) {
        if (genre != GENRE_UNDEFINED && (genre < 0 || genre >= genres.length))
            throw new JMACException("Wrong Genre");
        this.genre = genre;
    }

    public ID3Genre(final String genre) {
        if (genre == null) {
            this.genre = GENRE_UNDEFINED;
            return;
        }
        int nGenreIndex = 0;
        boolean bFound = false;
        while (nGenreIndex < genres.length && !bFound) {
            if (genre.equals(genres[nGenreIndex])) {
                this.genre = nGenreIndex;
                bFound = true;
            }
            nGenreIndex++;
        }
    }

    public int getGenre() {
        return genre;
    }

    public void setGenre(int genre) {
        this.genre = genre;
    }
}
