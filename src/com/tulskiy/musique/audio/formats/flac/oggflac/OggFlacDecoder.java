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

package com.tulskiy.musique.audio.formats.flac.oggflac;

import com.jcraft.jogg.Packet;
import com.jcraft.jogg.Page;
import com.jcraft.jogg.StreamState;
import com.jcraft.jogg.SyncState;
import org.kc7bfi.jflac.FLACDecoder;
import org.kc7bfi.jflac.metadata.Metadata;
import org.kc7bfi.jflac.metadata.StreamInfo;
import org.kc7bfi.jflac.util.RingBuffer;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.util.Arrays;

/**
 * @Author: Denis Tulskiy
 * @Date: 22.07.2009
 */
public class OggFlacDecoder {
    private static final int CHUNKSIZE = 8500;
    private static final byte SUPPORTED_MAPPING = 1;

    public static final int OV_FALSE = -1;
    public static final int OV_EOF = -2;
    public static final int OV_HOLE = -3;

    public static final int OV_EREAD = -128;
    public static final int OV_EFAULT = -129;
    public static final int OV_EIMPL = -130;
    public static final int OV_EINVAL = -131;
    public static final int OV_EBADHEADER = -133;
    public static final int OV_EVERSION = -134;
    public static final int OV_ENOTAUDIO = -135;
    public static final int OV_EBADPACKET = -136;
    public static final int OV_EBADLINK = -137;
    public static final int OV_ENOSEEK = -138;

    private RandomAccessFile input;
    private StreamState os = new StreamState();
    private SyncState oy = new SyncState();
    private long offset;
    private Page page = new Page();
    private Packet packet = new Packet();
    private OggInputStream inputStream;
    private StreamInfo streamInfo;
    private Metadata[] metadata;
    private FLACDecoder decoder;
    private int serialno = -1;
    private int links;
    private long[] offsets;

    byte majorVersion;
    byte minorVersion;
    int headerPackets;
    int pcm_offset;

    public int open(RandomAccessFile input) {
        this.input = input;
        oy.clear();
        oy.init();
        inputStream = new OggInputStream();
        decoder = new FLACDecoder(inputStream);
        long end = 0;
        try {
            end = input.length();
        } catch (IOException e) {
            e.printStackTrace();
        }
        getNextPage(1);
        if (bisect_forward_serialno(0, end, end + 1, serialno, 0) < 0) {
            clear();
            return OV_EREAD;
        }
        readMetadata();
        return 0;
    }

    private void clear() {
        os.clear();
        oy.clear();
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public int readMetadata() {
        seekHelper(0);
        getNextPacket();

        DataInputStream dis = new DataInputStream(inputStream);
        byte[] firstPacket = new byte[]{0x7F, 'F', 'L', 'A', 'C'};
        try {
            byte[] packetHeader = new byte[5];
            dis.read(packetHeader);
            if (!Arrays.equals(packetHeader, firstPacket))
                return OV_EBADPACKET;
            majorVersion = dis.readByte();
            if (majorVersion > SUPPORTED_MAPPING)
                return OV_EVERSION;
            minorVersion = dis.readByte();
            headerPackets = dis.readShort();
            streamInfo = decoder.readStreamInfo();
            //read other headers
            metadata = new Metadata[headerPackets];
            for (int i = 0; i < headerPackets; i++) {
                getNextPacket();
                metadata[i] = decoder.readNextMetadata();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        return 0;
    }

    public int getData() {
        int index = oy.buffer(CHUNKSIZE);
        byte[] buffer = oy.data;
        int bytes;
        try {
            bytes = input.read(buffer, index, CHUNKSIZE);
        } catch (Exception e) {
            return OV_EREAD;
        }
        oy.wrote(bytes);
        if (bytes == -1) {
            bytes = 0;
        }
        return bytes;
    }

    public void seekHelper(long offset) {
        try {
            input.seek(offset);
            this.offset = offset;
            oy.reset();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public int getNextPage(long boundary) {
        if (boundary > 0)
            boundary += offset;
        while (true) {
            int more;
            if (boundary > 0 && offset >= boundary)
                return OV_FALSE;
            more = oy.pageseek(page);
            if (more < 0) {
                offset -= more;
            } else {
                if (more == 0) {
                    if (boundary == 0)
                        return OV_FALSE;
                    int ret = getData();
                    if (ret == 0)
                        return OV_EOF;
                    if (ret < 0)
                        return OV_EREAD;
                } else {
                    int ret = (int) offset; //!!!
                    offset += more;
                    if (serialno == -1) {
                        serialno = page.serialno();
                        os.init(serialno);
                    }
//                    System.out.println(page.granulepos() + " " + page.pageno());
                    return ret;
                }
            }
        }
    }

    public int getNextPacket() {
        while (os.packetout(packet) == 0) {
            int ret = getNextPage(1);
            if (ret < 0)
                return ret;
            os.pagein(page);
        }
        inputStream.add(packet.packet_base, packet.packet, packet.bytes);
        return 0;
    }

    public InputStream getInputStream() {
        return inputStream;
    }

    public StreamInfo getStreamInfo() {
        return streamInfo;
    }

    public FLACDecoder getDecoder() {
        return decoder;
    }

    int bisect_forward_serialno(long begin, long searched, long end,
                                int currentno, int m) {
        long endsearched = end;
        long next = end;
        int ret;

        while (searched < endsearched) {
            long bisect;
            if (endsearched - searched < CHUNKSIZE) {
                bisect = searched;
            } else {
                bisect = (searched + endsearched) / 2;
            }

            seekHelper(bisect);
            ret = getNextPage(-1);
            if (ret == OV_EREAD)
                return OV_EREAD;
            if (ret < 0 || page.serialno() != currentno) {
                endsearched = bisect;
                if (ret >= 0)
                    next = ret;
            } else {
                searched = ret + page.header_len + page.body_len;
            }
        }
        seekHelper(next);
        ret = getNextPage(-1);
        if (ret == OV_EREAD)
            return OV_EREAD;

        if (searched >= end || ret == -1) {
            links = m + 1;
            offsets = new long[m + 2];
            offsets[m + 1] = searched;
        } else {
            ret = bisect_forward_serialno(next, offset, end, page.serialno(), m + 1);
            if (ret == OV_EREAD)
                return OV_EREAD;
        }
        offsets[m] = begin;
        return 0;
    }


    public void flush() {
        os.reset();
        oy.reset();
        inputStream.flush();
    }

    public Metadata[] getMetadata() {
        return metadata;
    }

    class OggInputStream extends InputStream {
        RingBuffer buf = new RingBuffer(65536);
        byte[] single = new byte[1];

        public void add(byte[] b, int off, int len) {
            buf.put(b, off, len);
        }

        @Override
        public int read() throws IOException {
            if (read(single, 0, 1) > 0)
                return single[0];
            return -1;
        }

        @Override
        public int read(byte[] b, int off, int len) throws IOException {
            while (buf.getAvailable() < len) {
                int ret = getNextPacket();
                if (ret < 0)
                    break;
            }
            if (buf.getAvailable() <= 0)
                return -1;
            return buf.get(b, off, len);
        }

        public void flush() {
            buf.empty();
        }
    }
}
