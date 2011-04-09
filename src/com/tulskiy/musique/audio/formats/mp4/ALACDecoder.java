///*
// * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
// *
// * This program is free software: you can redistribute it and/or modify
// * it under the terms of the GNU Lesser General Public License as published by
// * the Free Software Foundation, either version 3 of the License, or
// * (at your option) any later version.
// *
// * This program is distributed in the hope that it will be useful,
// * but WITHOUT ANY WARRANTY; without even the implied warranty of
// * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
// * GNU Lesser General Public License for more details.
// *
// * You should have received a copy of the GNU Lesser General Public License
// * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
// */
//
//package com.tulskiy.musique.audio.formats.mp4;
//
//import com.beatofthedrum.alacdecoder.AlacContext;
//import com.beatofthedrum.alacdecoder.AlacUtils;
//import com.tulskiy.musique.audio.Decoder;
//import com.tulskiy.musique.playlist.Track;
//
//import javax.sound.sampled.AudioFormat;
//
///**
// * Author: Denis Tulskiy
// * Date: 4/2/11
// */
//public class ALACDecoder implements Decoder {
//    private AlacContext alacContext;
//    private AudioFormat audioFormat;
//
//    public static final int destBufferSize = 1024 * 24 * 3; // 24kb buffer = 4096 frames = 1 alac sample (we support max 24bps)
//    private int[] pDestBuffer;
//    private int bps;
//    private int totalSamples;
//
//    @Override
//    public boolean open(Track track) {
//        alacContext = AlacUtils.AlacOpenFileInput(track.getFile().getAbsolutePath());
//        if (alacContext.error) {
//            logger.warning("Error while opening alac file: " + alacContext.error_message);
//            return false;
//        }
//        int channels = AlacUtils.AlacGetNumChannels(alacContext);
//        totalSamples = AlacUtils.AlacGetNumSamples(alacContext);
//        bps = AlacUtils.AlacGetBytesPerSample(alacContext);
//        int sampleRate = AlacUtils.AlacGetSampleRate(alacContext);
//        int bitps = AlacUtils.AlacGetBitsPerSample(alacContext);
//
//        pDestBuffer = new int[destBufferSize];
//        audioFormat = new AudioFormat(sampleRate, bitps, channels, true, false);
//        return true;
//    }
//
//    @Override
//    public AudioFormat getAudioFormat() {
//        return audioFormat;
//    }
//
//    @Override
//    public void seekSample(long sample) {
//        AlacUtils.AlacSetPosition(alacContext, sample);
//    }
//
//    @Override
//    public int decode(byte[] buf) {
//        int bytesUnpacked = AlacUtils.AlacUnpackSamples(alacContext, pDestBuffer);
//        if (bytesUnpacked > 0) {
//            formatSamples(bps, pDestBuffer, buf, bytesUnpacked);
//            return bytesUnpacked;
//        }
//        return -1;
//    }
//
//    private void formatSamples(int bps, int[] src, byte[] dst, int samcnt) {
//        int temp;
//        int counter = 0;
//        int counter2 = 0;
//
//        switch (bps) {
//            case 1:
//                while (samcnt > 0) {
//                    dst[counter] = (byte) (0x00FF & (src[counter] + 128));
//                    counter++;
//                    samcnt--;
//                }
//                break;
//
//            case 2:
//                while (samcnt > 0) {
//                    temp = src[counter2];
//                    dst[counter] = (byte) temp;
//                    counter++;
//                    dst[counter] = (byte) (temp >>> 8);
//                    counter++;
//                    counter2++;
//                    samcnt = samcnt - 2;
//                }
//                break;
//
//            case 3:
//                while (samcnt > 0) {
//                    dst[counter] = (byte) src[counter2];
//                    counter++;
//                    counter2++;
//                    samcnt--;
//                }
//                break;
//        }
//    }
//
//    @Override
//    public void close() {
//        if (alacContext != null)
//            AlacUtils.AlacCloseFile(alacContext);
//    }
//}
