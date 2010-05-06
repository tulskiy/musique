/*
** BitsUtils.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

class BitsUtils {
    ////////////////////////// Bitstream functions ////////////////////////////////
    // Open the specified BitStream using the specified buffer pointers. It is
    // assumed that enough buffer space has been allocated for all data that will
    // be written, otherwise an error will be generated.
    static void bs_open_write(Bitstream bs, int buffer_start, int buffer_end) {
        bs.error = 0;
        bs.sr = 0;
        bs.bc = 0;
        bs.buf_index = buffer_start;
        bs.start_index = bs.buf_index;
        bs.end = (int) buffer_end;
        bs.active = 1; // indicates that the bitstream is being used
    }

    // This function is only called from the putbit() and putbits() when
    // the buffer is full, which is now flagged as an error.
    static void bs_wrap(Bitstream bs) {
        bs.buf_index = bs.start_index;
        bs.error = 1;
    }

    // This function calculates the approximate number of bytes remaining in the
    // bitstream buffer and can be used as an early-warning of an impending overflow.
    static long bs_remain_write(Bitstream bs) {
        long bytes_written;

        if (bs.error > 0) {
            return (long) -1;
        }

        return bs.end - bs.buf_index;
    }

    // This function forces a flushing write of the standard BitStream, and
    // returns the total number of bytes written into the buffer.
    static long bs_close_write(WavpackStream wps) {
        Bitstream bs = wps.wvbits;
        long bytes_written = 0;

        if (bs.error != 0) {
            return (long) -1;
        }

        while ((bs.bc != 0) || (((bs.buf_index - bs.start_index) & 1) != 0)) {
            WordsUtils.putbit_1(wps);
        }

        bytes_written = bs.buf_index - bs.start_index;

        return bytes_written;
    }

    // This function forces a flushing write of the correction BitStream, and
    // returns the total number of bytes written into the buffer.
    static long bs_close_correction_write(WavpackStream wps) {
        Bitstream bs = wps.wvcbits;
        long bytes_written = 0;

        if (bs.error != 0) {
            return (long) -1;
        }

        while ((bs.bc != 0) || (((bs.buf_index - bs.start_index) & 1) != 0)) {
            WordsUtils.putbit_correction_1(wps);
        }

        bytes_written = bs.buf_index - bs.start_index;

        return bytes_written;
    }
}
