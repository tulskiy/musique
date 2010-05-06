/*
 * Copyright (c) 2009 Denis Tulskiy
 *
 * This code is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Lesser General Public License version 3 only, as
 * published by the Free Software Foundation.
 *
 * This code is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
 * FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public License
 * version 3 for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package org.jaudiotagger.audio.wavpack;

import com.wavpack.decoder.WavPackUtils;
import com.wavpack.decoder.WavpackContext;
import org.jaudiotagger.audio.exceptions.CannotReadException;
import org.jaudiotagger.audio.generic.AudioFileReader;
import org.jaudiotagger.audio.generic.GenericAudioHeader;
import org.jaudiotagger.tag.Tag;

import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by IntelliJ IDEA.
 * User: tulskiy
 * Date: 19.07.2009
 * Time: 21:19:54
 */
public class WavPackReader extends AudioFileReader {
    @Override
    protected GenericAudioHeader getEncodingInfo(RandomAccessFile raf) throws CannotReadException, IOException {
        GenericAudioHeader header = new GenericAudioHeader();

        WavpackContext wpc = WavPackUtils.WavpackOpenFileInput(raf);
        header.setPreciseLength((double) WavPackUtils.WavpackGetNumSamples(wpc) / (double) WavPackUtils.WavpackGetSampleRate(wpc));
        header.setSamplingRate((int) WavPackUtils.WavpackGetSampleRate(wpc));
        header.setChannelNumber(WavPackUtils.WavpackGetReducedChannels(wpc));
        header.setBitrate((int) (raf.length() / header.getPreciseLength() / 1000 * 8));
        header.setLossless(true);
        header.setEncodingType("WavPack");

        return header;
    }

    @Override
    protected Tag getTag(RandomAccessFile raf) throws CannotReadException, IOException {
        return null;  //To change body of implemented methods use File | Settings | File Templates.
    }
}
