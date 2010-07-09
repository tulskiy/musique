/*
 * Copyright (c) 2009 Denis Tulskiy
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

package javazoom.jl.decoder;

import java.io.IOException;
import java.io.InputStream;
import java.io.PushbackInputStream;
import java.io.BufferedInputStream;

/**
 * The <code>Bistream</code> class is responsible for parsing
 * an MPEG audio bitstream.
 * <p/>
 * <b>REVIEW:</b> much of the parsing currently occurs in the
 * various decoders. This should be moved into this class and associated
 * inner classes.
 */
public final class Bitstream implements BitstreamErrors {
    /**
     * Synchronization control constant for the initial
     * synchronization to the start of a frame.
     */
    static byte INITIAL_SYNC = 0;

    /**
     * Synchronization control constant for non-initial frame
     * synchronizations.
     */
    static byte STRICT_SYNC = 1;

    // max. 1730 bytes per frame: 144 * 384kbit/s / 32000 Hz + 2 Bytes CRC
    /**
     * Maximum size of the frame buffer.
     */
    private static final int BUFFER_INT_SIZE = 1730;

    /**
     * The frame buffer that holds the data for the current frame.
     */
    private final int[] framebuffer = new int[BUFFER_INT_SIZE];

    /**
     * Number of valid bytes in the frame buffer.
     */
    private int framesize;

    /**
     * The bytes read from the stream.
     */
    private byte[] frame_bytes = new byte[BUFFER_INT_SIZE * 4];

    /**
     * Index into <code>framebuffer</code> where the next bits are
     * retrieved.
     */
    private int wordpointer;

    /**
     * Number (0-31, from MSB to LSB) of next bit for get_bits()
     */
    private int bitindex;

    /**
     * The current specified syncword
     */
    private int syncword;


    private boolean single_ch_mode;
    //private int 			current_frame_number;
    //private int				last_frame_number;

    private final int bitmask[] = {0,    // dummy
            0x00000001, 0x00000003, 0x00000007, 0x0000000F,
            0x0000001F, 0x0000003F, 0x0000007F, 0x000000FF,
            0x000001FF, 0x000003FF, 0x000007FF, 0x00000FFF,
            0x00001FFF, 0x00003FFF, 0x00007FFF, 0x0000FFFF,
            0x0001FFFF};

    private final PushbackInputStream source;

    private final Header header = new Header();

    private final byte syncbuf[] = new byte[4];

    private Crc16[] crc = new Crc16[1];

    private boolean firstframe = true;


    /**
     * Construct a IBitstream that reads data from a
     * given InputStream.
     *
     * @param source The InputStream to read from.
     */
    public Bitstream(InputStream source) {
        this.source = new PushbackInputStream(
                new BufferedInputStream(source, 30000), BUFFER_INT_SIZE);
        skipID3v2();
        firstframe = true;
        closeFrame();
    }

    /**
     * Find ID3v2 tag header and skip the tag.
     *
     * @return size of ID3v2 frames + header
     * @author JavaZOOM
     */
    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    private void skipID3v2() {
        byte[] id3header = new byte[4];
        try {
            source.read(id3header, 0, 3);
            // Look for ID3v2
            if ((id3header[0] == 'I') && (id3header[1] == 'D') && (id3header[2] == '3')) {
                //skip some flags
                source.skip(3);
                //read the size
                source.read(id3header, 0, 4);
                int size = (id3header[0] << 21) + (id3header[1] << 14) + (id3header[2] << 7) + (id3header[3]);
                if (size > 0)
                    source.skip(size);
            } else {
                source.unread(id3header, 0, 3);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Close the Bitstream.
     */
    public void close() {
        try {
            source.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Reads and parses the next frame from the input source.
     *
     * @return the Header describing details of the frame read,
     *         or null if the end of the stream has been reached.
     */
    public Header readFrame() throws BitstreamException {
        Header result = null;
        try {
            result = readNextFrame();
            // E.B, Parse VBR (if any) first frame.
            if (firstframe) {
                if (result.checkHeader(frame_bytes)) {
                    closeFrame();
                    firstframe = false;
                    return readFrame();
                }
                firstframe = false;
            }
        }
        catch (BitstreamException ex) {
            if ((ex.getErrorCode() == INVALIDFRAME)) {
                // Try to skip this frame.
                try {
                    closeFrame();
                    result = readNextFrame();
                }
                catch (BitstreamException e) {
                    if ((e.getErrorCode() != STREAM_EOF)) {
                        // wrap original exception so stack trace is maintained.
                        throw newBitstreamException(e.getErrorCode(), e);
                    }
                }
            } else if ((ex.getErrorCode() != STREAM_EOF)) {
                // wrap original exception so stack trace is maintained.
                throw newBitstreamException(ex.getErrorCode(), ex);
            }
        }
        return result;
    }

    /**
     * Read next MP3 frame.
     *
     * @return MP3 frame header.
     * @throws BitstreamException
     */
    private Header readNextFrame() throws BitstreamException {
        if (framesize == -1) {
            nextFrame();
        }
        return header;
    }


    /**
     * Read next MP3 frame.
     *
     * @throws BitstreamException
     */
    private void nextFrame() throws BitstreamException {
        // entire frame is read by the header class.
        header.readHeader(this, crc);
    }

    /**
     * Unreads the bytes read from the frame.
     *
     * @throws BitstreamException
     */
    // REVIEW: add new error codes for this.
    public void unreadFrame() throws BitstreamException {
        if (wordpointer == -1 && bitindex == -1 && (framesize > 0)) {
            try {
                source.unread(frame_bytes, 0, framesize);
            } catch (IOException ex) {
                throw newBitstreamException(STREAM_ERROR);
            }
        }
    }

    /**
     * Close MP3 frame.
     */
    public void closeFrame() {
        framesize = -1;
        wordpointer = -1;
        bitindex = -1;
    }

    /**
     * Determines if the next 4 bytes of the stream represent a
     * frame header.
     */
    public boolean isSyncCurrentPosition(int syncmode) throws BitstreamException {
        int read = readBytes(syncbuf, 0, 4);
        int headerstring = ((syncbuf[0] << 24) & 0xFF000000) | ((syncbuf[1] << 16) & 0x00FF0000) | ((syncbuf[2] << 8) & 0x0000FF00) | ((syncbuf[3]) & 0x000000FF);
        try {
            source.unread(syncbuf, 0, 4);
        } catch (IOException e) {
            e.printStackTrace();
        }

        switch (read) {
            case 0:
                return true;
            case 4:
                return isSyncMark(headerstring, syncmode, syncword);
            default:
                return false;
        }
    }


    // REVIEW: this class should provide inner classes to
    // parse the frame contents. Eventually, readBits will
    // be removed.

    public int readBits(int n) {
        return get_bits(n);
    }

    public int readCheckedBits(int n) {
        // REVIEW: implement CRC check.
        return get_bits(n);
    }

    protected BitstreamException newBitstreamException(int errorcode) {
        return new BitstreamException(errorcode, null);
    }

    protected BitstreamException newBitstreamException(int errorcode, Throwable throwable) {
        return new BitstreamException(errorcode, throwable);
    }

    /**
     * Get next 32 bits from bitstream.
     * They are stored in the headerstring.
     * syncmod allows Synchro flag ID
     * The returned value is False at the end of stream.
     */

    public int syncHeader(byte syncmode) throws BitstreamException {
        boolean sync;
        int headerstring;
        // read additional 2 bytes
        int bytesRead = readBytes(syncbuf, 0, 3);

        if (bytesRead != 3) throw new BitstreamException(STREAM_EOF, null);

        headerstring = ((syncbuf[0] << 16) & 0x00FF0000) | ((syncbuf[1] << 8) & 0x0000FF00) | ((syncbuf[2]) & 0x000000FF);

        do {
            headerstring <<= 8;

            if (readBytes(syncbuf, 3, 1) != 1)
                throw new BitstreamException(STREAM_EOF, null);

            headerstring |= (syncbuf[3] & 0x000000FF);

            sync = isSyncMark(headerstring, syncmode, syncword);
        }
        while (!sync);

        return headerstring;
    }

    public boolean isSyncMark(int headerstring, int syncmode, int word) {
        boolean sync;

        if (syncmode == INITIAL_SYNC) {
            sync = ((headerstring & 0xFFE00000) == 0xFFE00000);    // SZD: MPEG 2.5
        } else {
            sync = (headerstring & 0xFFF80C00) == word && (headerstring & 0x000000C0) == 0x000000C0 == single_ch_mode;
        }

        // filter out invalid sample rate
        if (sync)
            sync = (((headerstring >>> 10) & 3) != 3);
        // filter out invalid layer
        if (sync)
            sync = (((headerstring >>> 17) & 3) != 0);
        // filter out invalid version
        if (sync)
            sync = (((headerstring >>> 19) & 3) != 1);

        return sync;
    }

    /**
     * Reads the data for the next frame. The frame is not parsed
     * until parse frame is called.
     */
    int read_frame_data(int bytesize) throws BitstreamException {
        int numread;
        numread = readFully(frame_bytes, 0, bytesize);
        framesize = bytesize;
        wordpointer = -1;
        bitindex = -1;
        return numread;
    }

    /**
     * Parses the data previously read with read_frame_data().
     */
    void parse_frame() throws BitstreamException {
        // Convert Bytes read to int
        int b = 0;
        byte[] byteread = frame_bytes;
        int bytesize = framesize;

        for (int k = 0; k < bytesize; k = k + 4) {
            byte b0;
            byte b1 = 0;
            byte b2 = 0;
            byte b3 = 0;
            b0 = byteread[k];
            if (k + 1 < bytesize) b1 = byteread[k + 1];
            if (k + 2 < bytesize) b2 = byteread[k + 2];
            if (k + 3 < bytesize) b3 = byteread[k + 3];
            framebuffer[b++] = ((b0 << 24) & 0xFF000000) | ((b1 << 16) & 0x00FF0000) | ((b2 << 8) & 0x0000FF00) | (b3 & 0x000000FF);
        }
        wordpointer = 0;
        bitindex = 0;
    }

    /**
     * Read bits from buffer into the lower bits of an unsigned int.
     * The LSB contains the latest read bit of the stream.
     * (1 <= number_of_bits <= 16)
     */
    public int get_bits(int number_of_bits) {
        int returnvalue;
        int sum = bitindex + number_of_bits;

        // E.B
        // There is a problem here, wordpointer could be -1 ?!
        if (wordpointer < 0) wordpointer = 0;
        // E.B : End.

        if (sum <= 32) {
            // all bits contained in *wordpointer
            returnvalue = (framebuffer[wordpointer] >>> (32 - sum)) & bitmask[number_of_bits];
            // returnvalue = (wordpointer[0] >> (32 - sum)) & bitmask[number_of_bits];
            if ((bitindex += number_of_bits) == 32) {
                bitindex = 0;
                wordpointer++; // added by me!
            }
            return returnvalue;
        }

        int Right = (framebuffer[wordpointer] & 0x0000FFFF);
        wordpointer++;
        int Left = (framebuffer[wordpointer] & 0xFFFF0000);
        returnvalue = ((Right << 16) & 0xFFFF0000) | ((Left >>> 16) & 0x0000FFFF);

        returnvalue >>>= 48 - sum;    // returnvalue >>= 16 - (number_of_bits - (32 - bitindex))
        returnvalue &= bitmask[number_of_bits];
        bitindex = sum - 32;
        return returnvalue;
    }

    /**
     * Set the word we want to sync the header to.
     * In Big-Endian byte order
     */
    void setSyncword(int syncword0) {
        syncword = syncword0 & 0xFFFFFF3F;
        single_ch_mode = ((syncword0 & 0x000000C0) == 0x000000C0);
    }

    /**
     * Reads the exact number of bytes from the source
     * input stream into a byte array.
     *
     * @param b    The byte array to read the specified number
     *             of bytes into.
     * @param offs The index in the array where the first byte
     *             read should be stored.
     * @param len  the number of bytes to read.
     * @throws BitstreamException is thrown if the specified
     *                            number of bytes could not be read from the stream.
     */
    private int readFully(byte[] b, int offs, int len)
            throws BitstreamException {
        int nRead = 0;
        try {
            while (len > 0) {
                int bytesread = source.read(b, offs, len);
                if (bytesread == -1) {
                    while (len-- > 0) {
                        b[offs++] = 0;
                    }
                    break;
                    //throw newBitstreamException(UNEXPECTED_EOF, new EOFException());
                }
                nRead = nRead + bytesread;
                offs += bytesread;
                len -= bytesread;
            }
        }
        catch (IOException ex) {
            throw newBitstreamException(STREAM_ERROR, ex);
        }
        return nRead;
    }

    /**
     * Simlar to readFully, but doesn't throw exception when
     * EOF is reached.
     */
    private int readBytes(byte[] b, int offs, int len)
            throws BitstreamException {
        int totalBytesRead = 0;
        try {
            while (len > 0) {
                int bytesread = source.read(b, offs, len);
                if (bytesread == -1) {
                    break;
                }
                totalBytesRead += bytesread;
                offs += bytesread;
                len -= bytesread;
            }
        }
        catch (IOException ex) {
            throw new BitstreamException(STREAM_ERROR, ex);
        }
        return totalBytesRead;
    }

    public long getPosition() {
        try {
            return source.available();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
