/*
 * THIS FILE IS PART OF THE OggVorbis SOFTWARE CODEC SOURCE CODE.
 * USE, DISTRIBUTION AND REPRODUCTION OF THIS LIBRARY SOURCE IS
 * GOVERNED BY A BSD-STYLE SOURCE LICENSE INCLUDED WITH THIS SOURCE
 * IN 'COPYING'. PLEASE READ THESE TERMS BEFORE DISTRIBUTING.
 *
 * THE OggVorbis SOURCE CODE IS (C) COPYRIGHT 1994-2002
 * by the Xiph.Org Foundation http://www.xiph.org/
 */

package org.xiph;

import org.xiph.libogg.ogg_packet;
import org.xiph.libogg.ogg_page;
import org.xiph.libogg.ogg_stream_state;
import org.xiph.libvorbis.*;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class VorbisEncoder {

    static vorbisenc encoder;

    static ogg_stream_state os;    // take physical pages, weld into a logical stream of packets

    static ogg_page og;    // one Ogg bitstream page.  Vorbis packets are inside
    static ogg_packet op;    // one raw packet of data for decode

    static vorbis_info vi;    // struct that stores all the static vorbis bitstream settings

    static vorbis_comment vc;    // struct that stores all the user comments

    static vorbis_dsp_state vd;    // central working state for the packet->PCM decoder
    static vorbis_block vb;    // local working space for packet->PCM decode

    static int READ = 1024;
    static byte[] readbuffer = new byte[READ * 4];

    static int page_count = 0;
    static int block_count = 0;

    /**
     * VorbisEncoder.java
     * <p/>
     * Usage:
     * java -cp VorbisEncoder <Input File[.wav]> <Output File[.ogg]>
     */
    public static void main(String[] args) {
        boolean eos = false;

        vi = new vorbis_info();

        encoder = new vorbisenc();

        if (!encoder.vorbis_encode_init_vbr(vi, 2, 44100, .3f)) {
            System.out.println("Failed to Initialize vorbisenc");
            return;
        }

        vc = new vorbis_comment();
        vc.vorbis_comment_add_tag("ENCODER", "Java Vorbis Encoder");

        vd = new vorbis_dsp_state();

        if (!vd.vorbis_analysis_init(vi)) {
            System.out.println("Failed to Initialize vorbis_dsp_state");
            return;
        }

        vb = new vorbis_block(vd);

        java.util.Random generator = new java.util.Random();  // need to randomize seed
        os = new ogg_stream_state(generator.nextInt(256));

        System.out.print("Writing header.");
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

            FileOutputStream fos = new FileOutputStream("testfiles/1.ogg");

            while (!eos) {

                if (!os.ogg_stream_flush(og))
                    break;

                fos.write(og.header, 0, og.header_len);
                fos.write(og.body, 0, og.body_len);
                System.out.print(".");
            }
            System.out.print("Done.\n");

            FileInputStream fin = new FileInputStream("testfiles/mp3/sample.wav");
            fin.skip(44);
            System.out.print("Encoding.");
            while (!eos) {

                int i;
                int bytes = fin.read(readbuffer, 0, READ * 4); // stereo hardwired here

                int break_count = 0;

                if (bytes == 0) {

                    // end of file.  this can be done implicitly in the mainline,
                    // but it's easier to see here in non-clever fashion.
                    // Tell the library we're at end of stream so that it can handle
                    // the last frame and mark end of stream in the output properly

                    vd.vorbis_analysis_wrote(0);

                } else {

                    // data to encode

                    // expose the buffer to submit data
                    float[][] buffer = vd.vorbis_analysis_buffer(READ);

                    // uninterleave samples
                    for (i = 0; i < bytes / 4; i++) {
                        buffer[0][vd.pcm_current + i] = ((readbuffer[i * 4 + 1] << 8) | (0x00ff & (int) readbuffer[i * 4])) / 32768.f;
                        buffer[1][vd.pcm_current + i] = ((readbuffer[i * 4 + 3] << 8) | (0x00ff & (int) readbuffer[i * 4 + 2])) / 32768.f;
                    }

                    // tell the library how much we actually submitted
                    vd.vorbis_analysis_wrote(i);
                }

                // vorbis does some data preanalysis, then divvies up blocks for more involved
                // (potentially parallel) processing.  Get a single block for encoding now

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
                                break_count++;
                                break;
                            }

                            fos.write(og.header, 0, og.header_len);
                            fos.write(og.body, 0, og.body_len);

                            // this could be set above, but for illustrative purposes, I do
                            // it here (to show that vorbis does know where the stream ends)
                            if (og.ogg_page_eos() > 0)
                                eos = true;
                        }
                    }
                }
                System.out.print(".");
            }

            fin.close();
            fos.close();

            System.out.print("Done.\n");

        } catch (Exception e) {
            System.out.println("\n" + e);
            e.printStackTrace(System.out);
        }
    }
}
