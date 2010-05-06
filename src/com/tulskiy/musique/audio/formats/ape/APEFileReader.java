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

package com.tulskiy.musique.audio.formats.ape;

import com.tulskiy.musique.audio.AudioFileReader;
import com.tulskiy.musique.audio.Decoder;
import com.tulskiy.musique.playlist.Song;
import davaguine.jmac.info.APEFileInfo;
import davaguine.jmac.info.APEHeader;
import davaguine.jmac.tools.RandomAccessFile;

import java.io.File;
import java.io.IOException;

/**
 * @Author: Denis Tulskiy
 * @Date: 26.06.2009
 */
public class APEFileReader extends AudioFileReader {
    private static APEDecoder decoder = new APEDecoder();
    private APETagProcessor tagProcessor = new APETagProcessor();

    public APEFileReader() {
        setUseNativeDecoder(false);
    }

    public Song readSingle(File file) {
        try {
            Song song = new Song();

            song.setFile(file);

            RandomAccessFile ras = new RandomAccessFile(file, "r");
            APEHeader header = new APEHeader(ras);
            APEFileInfo fileInfo = new APEFileInfo();
            header.Analyze(fileInfo);
            parseInfo(song, fileInfo);

            tagProcessor.readAPEv2Tag(song);
            ras.close();
            return song;
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isFileSupported(String ext) {
        return ext.equalsIgnoreCase("ape");
    }

    public void setUseNativeDecoder(boolean useNativeDecoder) {
        System.setProperty("jmac.NATIVE", String.valueOf(useNativeDecoder));
    }

    @Override
    public Decoder getDecoder() {
        return decoder;
    }

    private void parseInfo(Song song, APEFileInfo fileInfo) {
        song.setBitrate(fileInfo.nAverageBitrate);
        song.setChannels(fileInfo.nChannels);
        song.setCodec("Monkey's audio");
        song.setSamplerate(fileInfo.nSampleRate);
//        song.setCustomHeaderField("codec_profile", codec_profiles[fileInfo.nCompressionLevel / 1000]);
        song.setTotalSamples(fileInfo.nTotalBlocks);
//        song.setCustomHeaderNumber("flags", fileInfo.nFormatFlags);
        song.setStartPosition(0);
//        if (fileInfo.spAPEDescriptor != null)
//            song.setCustomHeaderField("version", String.valueOf(fileInfo.spAPEDescriptor.nVersion / 1000f));
    }
}
