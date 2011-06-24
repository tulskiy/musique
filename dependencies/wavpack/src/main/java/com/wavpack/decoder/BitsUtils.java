package com.wavpack.decoder;

import java.io.RandomAccessFile;

/*
** BitsUtils.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

class BitsUtils {
    static Bitstream getbit(Bitstream bs) {
        int uns_buf = 0;

        if (bs.bc > 0) {
            bs.bc--;
        } else {
            bs.ptr++;
            bs.buf_index++;
            bs.bc = 7;

            if (bs.ptr == bs.end) {
                // wrap call here
                bs = bs_read(bs);
            }
            uns_buf = (int) (bs.buf[bs.buf_index] & 0xff);
            bs.sr = uns_buf;
        }

        if ((bs.sr & 1) > 0) {
            bs.sr = bs.sr >> 1;
            bs.bitval = 1;
            return bs;
        } else {
            bs.sr = bs.sr >> 1;
            bs.bitval = 0;
            return bs;
        }
    }

    static long getbits(int nbits, Bitstream bs) {
        int uns_buf;
        long value;

        while ((nbits) > bs.bc) {
            bs.ptr++;
            bs.buf_index++;

            if (bs.ptr == bs.end) {
                bs = bs_read(bs);
            }
            uns_buf = (int) (bs.buf[bs.buf_index] & 0xff);
            bs.sr = bs.sr | (uns_buf << bs.bc); // values in buffer must be unsigned
            bs.bc += 8;
        }

        value = bs.sr;

        if (bs.bc > 32) {
            bs.bc -= (nbits);
            bs.sr = (bs.ptr) >> (8 - bs.bc);
        } else {
            bs.bc -= (nbits);
            bs.sr >>= (nbits);
        }

        return (value);
    }

    static Bitstream bs_open_read(Bitstream bs, byte[] stream, short buffer_start, short buffer_end, RandomAccessFile file,
                                  long file_bytes, int passed) {
        //   CLEAR (*bs);

        bs.buf = stream;
        bs.buf_index = buffer_start;
        bs.end = buffer_end;
        bs.sr = 0;
        bs.bc = 0;

        if (passed != 0) {
            bs.ptr = (short) (bs.end - 1);
            bs.file_bytes = file_bytes;
            bs.file = file;
        } else {
            /* Strange to set an index to -1, but the very first call to getbit will iterate this */
            bs.buf_index = -1;
            bs.ptr = (short) -1;
        }

        return bs;
    }

    static Bitstream bs_read(Bitstream bs) {
        byte[] buf = bs.temp_buf;
        if (bs.file_bytes > 0) {
            long bytes_read, bytes_to_read;

            bytes_to_read = 65536;

            if (bytes_to_read > bs.file_bytes)
                bytes_to_read = bs.file_bytes;

            try {
                bytes_read = bs.file.read(buf, 0, (int) bytes_to_read);
                bs.buf_index = 0;
                bs.buf = buf;
            } catch (Exception e) {
                System.err.println("Big error while reading file: " + e);
                bytes_read = 0;
            }

            if (bytes_read > 0) {
                bs.end = (short) (bytes_read);
                bs.file_bytes -= bytes_read;
            } else {
                for (int i = 0; i < bs.end - bs.buf_index; i++) {
                    bs.buf[i] = -1;
                }
                bs.error = 1;
            }
        } else {
            bs.error = 1;
        }

        if (bs.error > 0) {
            for (int i = 0; i < bs.end - bs.buf_index; i++) {
                bs.buf[i] = -1;
            }
        }

        bs.ptr = 0;
        bs.buf_index = 0;

        return bs;
    }
}