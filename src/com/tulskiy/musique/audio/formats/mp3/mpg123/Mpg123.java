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

/**
 * @Author: Denis Tulskiy
 * @Date: 21.06.2009
 */
package com.tulskiy.musique.audio.formats.mp3.mpg123;

import com.sun.jna.NativeLong;
import com.sun.jna.Platform;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import static com.tulskiy.musique.audio.formats.mp3.mpg123.libmpg123.mpg123_errors.*;

import com.tulskiy.musique.audio.io.PCMOutputStream;

import java.io.File;
import java.nio.ByteBuffer;

public class Mpg123 {
    public static final NativeLong READ_SIZE = new NativeLong(4096);

    private static Mpg123 ourInstance = new Mpg123();

    private libmpg123 lib = libmpg123.INSTANCE;
    private NativeLongByReference rate = new NativeLongByReference();
    private NativeLongByReference bytes = new NativeLongByReference();
    private IntByReference channels = new IntByReference();
    private ByteBuffer pcmBuffer;
    private libmpg123.mpg123_handle_struct mh;
    private File inputFile;
    private boolean finished;
    private NativeLong ZERO = new NativeLong(0);

    public static Mpg123 getInstance() {
        return ourInstance;
    }

    private Mpg123() {
        pcmBuffer = ByteBuffer.allocate(READ_SIZE.intValue());
    }

    public boolean init() {
        if (lib.mpg123_init() != MPG123_OK)
            return false;

        if (mh == null) {
            mh = lib.mpg123_new("", null);
        }

        return (mh != null);
    }

    public boolean open(File inputFile) {
        if (mh == null)
            if (!init()) return false;

        String path = inputFile.getAbsolutePath();
        this.inputFile = inputFile;
        int ret = openFile(path);

        if (ret != MPG123_OK) {
            return false;
        }

        IntByReference num = new IntByReference();
        PointerByReference audio = new PointerByReference();
        if (Platform.isWindows())
            ret = lib.mpg123_decode_frame(mh, num, audio, bytes);
        else {
//            ret = lib.mpg123_decode_frame_64(mh, num, audio, bytes);
            byte[] buf = new byte[1];
            NativeLongByReference done = new NativeLongByReference();
            ret = lib.mpg123_decode(mh, null, new NativeLong(0), buf, new NativeLong(1), done);
        }

        if (ret == MPG123_NEW_FORMAT) {
            lib.mpg123_getformat(mh, rate, channels, new IntByReference());
        }

        return true;
    }

    private int openFile(String path) {
        int ret;
        if (Platform.isWindows()) {
            ret = lib.mpg123_open(mh, path);
        } else {
            ret = lib.mpg123_open_64(mh, path);
        }
        return ret;
    }

    public int getChannels() {
        return channels.getValue();
    }

    public int getRate() {
        return rate.getValue().intValue();
    }

    public String getError() {
        return lib.mpg123_strerror(mh);
    }

    public long getTotalSeconds() {
        return (long) (getLength() / rate.getValue().doubleValue() * 1000);
    }

    public int getLength() {
        if (Platform.isWindows())
            return lib.mpg123_length(mh);
        else
            return lib.mpg123_length_64(mh);
    }

    public int getCurrentFrame() {
        if (Platform.isWindows())
            return lib.mpg123_tell(mh);
        else
            return lib.mpg123_tell_64(mh);
    }

    public long getCurrentSecond() {
        return (long) (getCurrentFrame() / rate.getValue().doubleValue() * 1000);
    }

    public int getTimeFrame(double seconds) {
        if (Platform.isWindows())
            return lib.mpg123_timeframe(mh, seconds);
        else
            return lib.mpg123_timeframe_64(mh, seconds);
    }

    public void seekFrame(int offset) {
        int ret;
        if (Platform.isWindows())
            ret = lib.mpg123_seek_frame(mh, offset, 0);
        else
            ret = lib.mpg123_seek_frame_64(mh, new NativeLong(offset), 0);

        if (ret < 0)
            System.out.println(getError());
    }

    public void seekSample(long sample) {
        if (finished) {
            finished = false;
            lib.mpg123_delete(mh);
            mh = null;
            open(inputFile);
        }
        int ret;
        if (Platform.isWindows())
            ret = lib.mpg123_seek(mh, (int) sample, 0);
        else
            ret = lib.mpg123_seek_64(mh, new NativeLong(sample), 0);
        if (ret != sample)
            System.out.println(getError());
    }

    public int decode(byte[] buf) {
        if (finished)
            return -1;
//        int ret = lib.mpg123_read(mh, pcmBuffer, READ_SIZE, bytes);
        int ret = lib.mpg123_decode(mh, null, ZERO, buf, READ_SIZE, bytes);
        if (ret != MPG123_OK && ret != MPG123_DONE) {
            return -1;
        }
        int len = bytes.getValue().intValue();
        if (ret == MPG123_DONE) {
            finished = true;
        }
        return len;
    }

    public void close() {
        lib.mpg123_delete(mh);
        mh = null;
    }
}
