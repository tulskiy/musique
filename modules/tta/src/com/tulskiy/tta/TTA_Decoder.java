/*
 * Based on TTA1-C++ library functions
 * Copyright (c) 2011 Aleksander Djuric. All rights reserved.
 * Distributed under the GNU Lesser General Public License (LGPL).
 * The complete text of the license can be found in the COPYING
 * file included in the distribution.
 */

package com.tulskiy.tta;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Arrays;

import static com.tulskiy.tta.Constants.*;
import static com.tulskiy.tta.Filter.*;
import static com.tulskiy.tta.Macros.*;
import static com.tulskiy.tta.TTACodecStatus.*;

/**
 * Author: Denis Tulskiy
 * Date: 5/30/11
 */
public class TTA_Decoder {
    public static final int MAX_DEPTH = 3;
    public static final int MAX_BPS = (MAX_DEPTH * 8);
    public static final int MIN_BPS = 16;
    public static final int MAX_NCH = 6;

    // TTA audio format
    public static final int TTA_FORMAT_SIMPLE = 1;
    public static final int TTA_FORMAT_ENCRYPTED = 2;

    public static final int[] flt_set = {10, 9, 10};

    boolean seek_allowed;    // seek table flag

    TTA_fifo fifo;
    TTA_codec[] decoder; // decoder (1 per channel)
    byte[] data;    // decoder initialization data
    boolean password_set;    // password protection flag
    long[] seek_table; // the playing position table
    int format;    // tta data format
    int bitrate;    // bitrate (kbps)
    long offset;    // data start position (header size, bytes)
    long frames;    // total count of frames
    int depth;    // bytes per sample
    long flen_std;    // default frame length in samples
    long flen_last;    // last frame length in samples
    long flen;    // current frame length in samples
    int fnum;    // currently playing frame index
    long fpos;    // the current position in frame
    int discard_bytes;
    private int smp_size;

    public TTA_Decoder(FileInputStream inputStream) {
        fifo = new TTA_fifo();
        fifo.io = inputStream;
        data = new byte[8];
    }

    public TTA_info init_get_info(long pos) {
        TTA_info info = new TTA_info();
        // set start position if required
        if (pos > 0) {
            fifo.seek(pos);
        }

        fifo.reader_start();
        pos += read_tta_header(info);

        // check for supported formats
        if (info.format > 2 ||
                info.bps < MIN_BPS ||
                info.bps > MAX_BPS ||
                info.nch > MAX_NCH)
            throw new tta_exception(TTA_FORMAT_ERROR);

        // check for required data is present
        if (info.format == TTA_FORMAT_ENCRYPTED) {
            if (!password_set)
                throw new tta_exception(TTA_PASSWORD_ERROR);
        }

        offset = pos; // size of headers
        format = info.format;
        depth = (info.bps + 7) / 8;
        flen_std = MUL_FRAME_TIME(info.sps);
        flen_last = info.samples % flen_std;
        frames = info.samples / flen_std + (flen_last > 0 ? 1 : 0);
        smp_size = depth * info.nch;
        if (flen_last == 0) flen_last = flen_std;
        bitrate = info.bitrate;

        // allocate memory for seek table data
        seek_table = new long[(int) frames];

        seek_allowed = read_seek_table();
        decoder = new TTA_codec[info.nch];
        for (int i = 0; i < decoder.length; i++) {
            decoder[i] = new TTA_codec();
        }

        frame_init(0, false);
        return info;
    }

    public int read_tta_header(TTA_info info) {
        int size = skip_id3v2();

        fifo.reader_reset();
        byte[] header = new byte[4];
        fifo.read(header, header.length);

        if (!"TTA1".equals(new String(header))) throw new tta_exception(TTA_FORMAT_ERROR);

        info.format = fifo.read_uint16();
        info.nch = fifo.read_uint16();
        info.bps = fifo.read_uint16();
        info.sps = (int) fifo.read_uint32();
        info.samples = (int) fifo.read_uint32();

        if (!fifo.read_crc32())
            throw new tta_exception(TTA_FILE_ERROR);

        size += 22; // sizeof TTA header

        try {
            int datasize = (int) (fifo.io.available() - size);
            int origsize = info.samples * info.bps / 8 * info.nch;
            double compress = (double) datasize / origsize;
            info.bitrate = (int) (compress * info.sps *
                info.nch * info.bps / 1000);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return size;
    } // read_tta_header

    int skip_id3v2() {
        int size = 0;

        fifo.reader_reset();

        // id3v2 header must be at start
        byte[] header = new byte[3];
        fifo.read(header, 3);
        if (!"ID3".equals(new String(header))) {
            fifo.pos = 0;
            return 0;
        }

        fifo.pos += 2; // skip version bytes
        if ((fifo.read_byte() & 0x10) != 0) size += 10;

        size += (fifo.read_byte() & 0x7f);
        size = (size << 7) | (fifo.read_byte() & 0x7f);
        size = (size << 7) | (fifo.read_byte() & 0x7f);
        size = (size << 7) | (fifo.read_byte() & 0x7f);

        fifo.reader_skip_bytes(size);

        return (size + 10);
    } // skip_id3v2

    boolean read_seek_table() {
        long tmp;
        int i;

        if (seek_table == null) return false;

        fifo.reader_reset();

        tmp = offset + (frames + 1) * 4;
        for (i = 0; i < frames; i++) {
            seek_table[i] = tmp;
            tmp += fifo.read_uint32();
        }

        return fifo.read_crc32();
    } // read_seek_table

    void frame_init(int frame, boolean seek_needed) {
        int shift = flt_set[depth - 1];

        if (frame >= frames) return;

        fnum = frame;

        if (seek_needed && seek_allowed) {
            long pos = seek_table[fnum];
            if (pos >= 0) {
                fifo.seek(pos);
            }
            fifo.reader_start();
        }

        if (fnum == frames - 1)
            flen = flen_last;
        else flen = flen_std;

        // init entropy decoder
        for (TTA_codec dec : decoder) {
            filter_init(dec.fst, data, shift);
            rice_init(dec.rice, 10, 10);
            dec.prev = 0;
        }

        fpos = 0;

        fifo.reader_reset();
    } // frame_init

    void filter_init(TTA_fltst fs, byte[] data, int shift) {
        fs.error = 0;
        fs.shift = shift;
        fs.round = 1 << (shift - 1);
        Arrays.fill(fs.dl, 0);
        Arrays.fill(fs.dx, 0);
        fs.qm[0] = data[0];
        fs.qm[1] = data[1];
        fs.qm[2] = data[2];
        fs.qm[3] = data[3];
        fs.qm[4] = data[4];
        fs.qm[5] = data[5];
        fs.qm[6] = data[6];
        fs.qm[7] = data[7];
    } // filter_init

    void rice_init(TTA_adapt rice, int k0, int k1) {
        rice.k0 = k0;
        rice.k1 = k1;
        rice.sum0 = shift_16[k0];
        rice.sum1 = shift_16[k1];
    } // rice_init

    public int process_stream(byte[] output) {
        int current_decoder = 0;
        TTA_codec dec = decoder[current_decoder];
        int ptr = 0;
        int[] cache = new int[MAX_NCH];
        int cp = 0;
        int end, smp;
        int value;
        int ret = 0;

        while (fpos < flen && ptr < output.length) {
            value = fifo.get_value(dec.rice);

            // decompress stage 1: adaptive hybrid filter
            value = hybrid_filter_dec(dec.fst, value);
//            System.out.printf("%d\t%d\t%d\t%d\n", value, dec.fst.error, dec.fst.round, dec.fst.shift);

            // decompress stage 2: fixed order 1 prediction
            value += (dec.prev * ((1 << 5) - 1)) >> 5;
            dec.prev = value;

            if (current_decoder < decoder.length - 1) {
                cache[cp++] = value;
                current_decoder++;
                if (current_decoder < decoder.length)
                    dec = decoder[current_decoder];
                else
                    dec = null;
            } else {
                cache[cp] = value;

                if (decoder.length == 1) {
                    WRITE_BUFFER(cache[cp], output, ptr, depth);
                    ptr += depth;
                } else {
                    end = cp;
                    smp = cp - 1;

                    cache[cp] += cache[smp] / 2;
                    while (smp > 0) {
                        cache[smp] = cache[cp--] - cache[smp];
                        smp--;
                    }
                    cache[smp] = cache[cp] - cache[smp];

                    while (smp <= end) {
                        WRITE_BUFFER(cache[smp], output, ptr, depth);
                        ptr += depth;
                        smp++;
                    }
                }

                cp = 0;
                fpos++;
                ret++;
                dec = decoder[0];
                current_decoder = 0;
            }

            if (fpos == flen) {
                // check frame crc
                boolean crc_flag = fifo.read_crc32();

                if (!crc_flag) {
                    Arrays.fill(output, (byte) 0);
                    if (!seek_allowed) break;
                }

                fnum++;

                // update dynamic info
                bitrate = (fifo.count << 3) / 1070;
//			if (tta_callback)
//				tta_callback(rate, fnum, frames);
                if (fnum == frames) break;

                frame_init(fnum, crc_flag);
            }
        }
        if (ret == 0) {
            return -1;
        }

        ret *= smp_size;
        if (discard_bytes > 0) {
            ret -= discard_bytes;
            if (ret < 0) {
                discard_bytes = Math.abs(ret);
                return 0;
            }
            System.arraycopy(output, discard_bytes, output, 0, ret);
            discard_bytes = 0;
        }

        return ret;
    } // process_stream

    public int get_current_bitrate() {
        return bitrate;
    }

    public void set_position(int sample) {
        int frame = (int) (sample / flen_std);
        if (!seek_allowed || frame >= frames)
            throw new tta_exception(TTA_SEEK_ERROR);

        discard_bytes = (int) ((sample - frame * flen_std) * smp_size);
        frame_init(frame, true);
    } // set_position

    public void close() throws IOException {
        fifo.io.close();
    }
}
