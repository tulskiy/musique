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

package com.tulskiy.musique.audio.formats.ogg;

import com.tulskiy.musique.audio.Encoder;
import com.tulskiy.musique.system.configuration.Configuration;

import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.*;

import javax.sound.sampled.AudioFormat;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.logging.Level;

/**
 * Based on sample VorbisEncoder from vorbis-java
 * <p/>
 * Author: Denis Tulskiy
 * Date: Jul 26, 2010
 */
public class VorbisEncoder implements Encoder {
    private ogg_stream_state os;    // take physical pages, weld into a logical stream of packets

    private ogg_page og;    // one Ogg bitstream page.  Vorbis packets are inside
    private ogg_packet op;    // one raw packet of data for decode

    private vorbis_dsp_state vd;    // central working state for the packet->PCM decoder
    private vorbis_block vb;    // local working space for packet->PCM decode
    private FileOutputStream output;
    private static final float DEFAULT_BITRATE = 0.3f;

    @Override
    public boolean open(File outputFile, AudioFormat fmt, Configuration options) {
        vorbis_info vi = new vorbis_info();
        vorbisenc encoder = new vorbisenc();

        float quality = DEFAULT_BITRATE;
        if (options != null) {
            quality = options.getFloat("vorbis.encoder.quality", DEFAULT_BITRATE);
        }
        logger.log(Level.INFO, "Starting encoding with {0} channels, {1} Hz, quality: {2}",
                new Object[]{fmt.getChannels(), fmt.getSampleRate(), quality});
        if (!encoder.vorbis_encode_init_vbr(vi, fmt.getChannels(), (int) fmt.getSampleRate(), quality)) {
            logger.warning("Failed to Initialize vorbisenc");
            return false;
        }

        vorbis_comment vc = new vorbis_comment();
        vc.vorbis_comment_add_tag("ENCODER", "Java Vorbis Encoder");

        vd = new vorbis_dsp_state();
        if (!vd.vorbis_analysis_init(vi)) {
            System.out.println("Failed to Initialize vorbis_dsp_state");
            return false;
        }

        vb = new vorbis_block(vd);

        java.util.Random generator = new java.util.Random();  // need to randomize seed
        os = new ogg_stream_state(generator.nextInt(256));

        ogg_packet header = new ogg_packet();
        ogg_packet header_comm = new ogg_packet();
        ogg_packet header_code = new ogg_packet();

        vd.vorbis_analysis_headerout(vc, header, header_comm, header_code);

        os.ogg_stream_packetin(header); // automatically placed in its own page
        os.ogg_stream_packetin(header_comm);
        os.ogg_stream_packetin(header_code);

        og = new ogg_page();
        op = new ogg_packet();

        try {
            output = new FileOutputStream(outputFile);

            if (!os.ogg_stream_flush(og))
                return false;

            output.write(og.header, 0, og.header_len);
            output.write(og.body, 0, og.body_len);
            return true;
        } catch (Exception ignored) {
        }

        return false;
    }

    @Override
    public void encode(byte[] buf, int len) {
        try {
            int i;
            // expose the buffer to submit data
            float[][] buffer = vd.vorbis_analysis_buffer(len / 4);
            // uninterleave samples
            for (i = 0; i < len / 4; i++) {
                buffer[0][vd.pcm_current + i] = ((buf[i * 4 + 1] << 8) | (0x00ff & (int) buf[i * 4])) / 32768.f;
                buffer[1][vd.pcm_current + i] = ((buf[i * 4 + 3] << 8) | (0x00ff & (int) buf[i * 4 + 2])) / 32768.f;
            }

            // tell the library how much we actually submitted
            vd.vorbis_analysis_wrote(i);

            analyze();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void analyze() throws IOException {
        // vorbis does some data preanalysis, then divvies up blocks for more involved
        // (potentially parallel) processing.  Get a single block for encoding now
        boolean eos = false;

        while (vb.vorbis_analysis_blockout(vd)) {
            // analysis, assume we want to use bitrate management

            vb.vorbis_analysis(null);
            vb.vorbis_bitrate_addblock();

            while (vd.vorbis_bitrate_flushpacket(op)) {

                // weld the packet into the bitstream
                os.ogg_stream_packetin(op);

                // write out pages (if any)
                while (!eos) {

                    if (!os.ogg_stream_pageout(og)) {
                        break;
                    }

                    output.write(og.header, 0, og.header_len);
                    output.write(og.body, 0, og.body_len);

                    // this could be set above, but for illustrative purposes, I do
                    // it here (to show that vorbis does know where the stream ends)
                    if (og.ogg_page_eos() > 0)
                        eos = true;
                }
            }
        }
    }

    @Override
    public void close() {
        try {
            if (vd != null) {
                vd.vorbis_analysis_wrote(0);
                analyze();
            }
            if (output != null)
                output.close();
        } catch (Exception ignored) {
        }
    }
}
