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

package com.tulskiy.musique.audio.formats.mp3.mpg123;

import com.sun.jna.Library;
import com.sun.jna.Native;
import com.sun.jna.NativeLong;
import com.sun.jna.ptr.ByteByReference;
import com.sun.jna.ptr.IntByReference;
import com.sun.jna.ptr.NativeLongByReference;
import com.sun.jna.ptr.PointerByReference;

import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;

/// JNA Wrapper for library <b>mpg123</b>

public interface libmpg123 extends Library {
    public static final libmpg123 INSTANCE = (libmpg123) Native.loadLibrary("mpg123", libmpg123.class);

    /**
     * Enumeration of the parameters types that it is possible to set/get.<br>
     * <i>native declaration : mpg123.h:53</i><br>
     * enum values
     */
    public static interface mpg123_parms {
        /// < set verbosity value for enabling messages to stderr, >= 0 makes sense (integer)
        public static final int MPG123_VERBOSE = 0;
        /// < set all flags, p.ex val = MPG123_GAPLESS|MPG123_MONO_MIX (integer)
        public static final int MPG123_FLAGS = 1;
        /// < add some flags (integer)
        public static final int MPG123_ADD_FLAGS = 2;
        /// < when value > 0, force output rate to that value (integer)
        public static final int MPG123_FORCE_RATE = 3;
        /// < 0=native rate, 1=half rate, 2=quarter rate (integer)
        public static final int MPG123_DOWN_SAMPLE = 4;
        /// < one of the RVA choices above (integer)
        public static final int MPG123_RVA = 5;
        /// < play a frame N times (integer)
        public static final int MPG123_DOWNSPEED = 6;
        /// < play every Nth frame (integer)
        public static final int MPG123_UPSPEED = 7;
        /// < start with this frame (skip frames before that, integer)
        public static final int MPG123_START_FRAME = 8;
        /// < decode only this number of frames (integer)
        public static final int MPG123_DECODE_FRAMES = 9;
        /// < stream contains ICY metadata with this interval (integer)
        public static final int MPG123_ICY_INTERVAL = 10;
        /// < the scale for output samples (amplitude - integer or float according to com.tulskiy.musique.jna.mpg123 output format, normally integer)
        public static final int MPG123_OUTSCALE = 11;
        /// < timeout for reading from a stream (not supported on win32, integer)
        public static final int MPG123_TIMEOUT = 12;
        /// < remove some flags (inverse of MPG123_ADD_FLAGS, integer)
        public static final int MPG123_REMOVE_FLAGS = 13;
        /// < Try resync on frame parsing for that many bytes or until end of stream (<0 ... integer).
        public static final int MPG123_RESYNC_LIMIT = 14;
        /// < Set the frame index size (if supported). Values <0 mean that the index is allowed to grow dynamically in these steps (in positive direction, of course) -- Use this when you really want a full index with every individual frame.
        public static final int MPG123_INDEX_SIZE = 15;
        /// < Decode/ignore that many frames in advance for layer 3. This is needed to fill bit reservoir after seeking, for example (but also at least one frame in advance is needed to have all "normal" data for layer 3). Give a positive integer value, please.
        public static final int MPG123_PREFRAMES = 16;
    }

    /**
     * Flag bits for MPG123_FLAGS, use the usual binary or to combine.<br>
     * <i>native declaration : mpg123.h:75</i><br>
     * enum values
     */
    public static interface mpg123_param_flags {
        /// <     0111 Force some mono mode: This is a test bitmask for seeing if any mono forcing is active.
        public static final int MPG123_FORCE_MONO = 7;
        /// <     0001 Force playback of left channel only.
        public static final int MPG123_MONO_LEFT = 1;
        /// <     0010 Force playback of right channel only.
        public static final int MPG123_MONO_RIGHT = 2;
        /// <     0100 Force playback of mixed mono.
        public static final int MPG123_MONO_MIX = 4;
        /// <     1000 Force stereo output.
        public static final int MPG123_FORCE_STEREO = 8;
        /// < 00010000 Force 8bit formats.
        public static final int MPG123_FORCE_8BIT = 16;
        /// < 00100000 Suppress any printouts (overrules verbose).
        public static final int MPG123_QUIET = 32;
        /// < 01000000 Enable gapless decoding (default on if libmpg123 has support).
        public static final int MPG123_GAPLESS = 64;
        /// < 10000000 Disable resync stream after error.
        public static final int MPG123_NO_RESYNC = 128;
        /// < 000100000000 Enable small buffer on non-seekable streams to allow some peek-ahead (for better MPEG sync).
        public static final int MPG123_SEEKBUFFER = 256;
        /// < 001000000000 Enable fuzzy seeks (guessing byte offsets or using approximate seek points from Xing TOC)
        public static final int MPG123_FUZZY = 512;
        /// < 010000000000 Force floating point output (32 or 64 bits depends on com.tulskiy.musique.jna.mpg123 internal precision).
        public static final int MPG123_FORCE_FLOAT = 1024;
    }

    /**
     * choices for MPG123_RVA<br>
     * <i>native declaration : mpg123.h:92</i><br>
     * enum values
     */
    public static interface mpg123_param_rva {
        /// < RVA disabled (default).
        public static final int MPG123_RVA_OFF = 0;
        /// < Use mix/track/radio gain.
        public static final int MPG123_RVA_MIX = 1;
        /// < Use album/audiophile gain
        public static final int MPG123_RVA_ALBUM = 2;
        /// < The maximum RVA code, may increase in future.
        public static final int MPG123_RVA_MAX = libmpg123.mpg123_param_rva.MPG123_RVA_ALBUM;
    }

    /**
     * Enumeration of the message and error codes and returned by libmpg123 functions.<br>
     * <i>native declaration : mpg123.h:126</i><br>
     * enum values
     */
    public static interface mpg123_errors {
        /// < Message: Track ended. Stop decoding.
        public static final int MPG123_DONE = -12;
        /// < Message: Output format will be different on next call. Note that some libmpg123 versions between 1.4.3 and 1.8.0 insist on you calling mpg123_getformat() after getting this message code. Newer verisons behave like advertised: You have the chance to call mpg123_getformat(), but you can also just continue decoding and get your data.
        public static final int MPG123_NEW_FORMAT = -11;
        /// < Message: For feed reader: "Feed me more!" (call mpg123_feed() or mpg123_decode() with some new input data).
        public static final int MPG123_NEED_MORE = -10;
        /// < Generic Error
        public static final int MPG123_ERR = -1;
        /// < Success
        public static final int MPG123_OK = 0;
        /// < Unable to set up output format!
        public static final int MPG123_BAD_OUTFORMAT = 1;
        /// < Invalid channel number specified.
        public static final int MPG123_BAD_CHANNEL = 2;
        /// < Invalid sample rate specified.
        public static final int MPG123_BAD_RATE = 3;
        /// < Unable to allocate memory for 16 to 8 converter table!
        public static final int MPG123_ERR_16TO8TABLE = 4;
        /// < Bad parameter id!
        public static final int MPG123_BAD_PARAM = 5;
        /// < Bad buffer given -- invalid pointer or too small size.
        public static final int MPG123_BAD_BUFFER = 6;
        /// < Out of memory -- some malloc() failed.
        public static final int MPG123_OUT_OF_MEM = 7;
        /// < You didn't initialize the library!
        public static final int MPG123_NOT_INITIALIZED = 8;
        /// < Invalid decoder choice.
        public static final int MPG123_BAD_DECODER = 9;
        /// < Invalid com.tulskiy.musique.jna.mpg123 handle.
        public static final int MPG123_BAD_HANDLE = 10;
        /// < Unable to initialize frame buffers (out of memory?).
        public static final int MPG123_NO_BUFFERS = 11;
        /// < Invalid RVA mode.
        public static final int MPG123_BAD_RVA = 12;
        /// < This build doesn't support gapless decoding.
        public static final int MPG123_NO_GAPLESS = 13;
        /// < Not enough buffer space.
        public static final int MPG123_NO_SPACE = 14;
        /// < Incompatible numeric data types.
        public static final int MPG123_BAD_TYPES = 15;
        /// < Bad equalizer band.
        public static final int MPG123_BAD_BAND = 16;
        /// < Null pointer given where valid storage address needed.
        public static final int MPG123_ERR_NULL = 17;
        /// < Error reading the stream.
        public static final int MPG123_ERR_READER = 18;
        /// < Cannot seek from end (end is not known).
        public static final int MPG123_NO_SEEK_FROM_END = 19;
        /// < Invalid 'whence' for seek function.
        public static final int MPG123_BAD_WHENCE = 20;
        /// < Build does not support stream timeouts.
        public static final int MPG123_NO_TIMEOUT = 21;
        /// < File access error.
        public static final int MPG123_BAD_FILE = 22;
        /// < Seek not supported by stream.
        public static final int MPG123_NO_SEEK = 23;
        /// < No stream opened.
        public static final int MPG123_NO_READER = 24;
        /// < Bad parameter handle.
        public static final int MPG123_BAD_PARS = 25;
        /// < Bad parameters to mpg123_index()
        public static final int MPG123_BAD_INDEX_PAR = 26;
        /// < Lost track in bytestream and did not try to resync.
        public static final int MPG123_OUT_OF_SYNC = 27;
        /// < Resync failed to find valid MPEG data.
        public static final int MPG123_RESYNC_FAIL = 28;
        /// < No 8bit encoding possible.
        public static final int MPG123_NO_8BIT = 29;
        /// < Stack aligmnent error
        public static final int MPG123_BAD_ALIGN = 30;
        /// < NULL input buffer with non-zero size...
        public static final int MPG123_NULL_BUFFER = 31;
        /// < Relative seek not possible (screwed up file offset)
        public static final int MPG123_NO_RELSEEK = 32;
        /// < You gave a null pointer somewhere where you shouldn't have.
        public static final int MPG123_NULL_POINTER = 33;
        /// < Bad key value given.
        public static final int MPG123_BAD_KEY = 34;
        /// < No frame index in this build.
        public static final int MPG123_NO_INDEX = 35;
        /// < Something with frame index went wrong.
        public static final int MPG123_INDEX_FAIL = 36;
        /// < Something prevents a proper decoder setup
        public static final int MPG123_BAD_DECODER_SETUP = 37;
        /// < This feature has not been built into libmpg123.
        public static final int MPG123_MISSING_FEATURE = 38;
        /// < A bad value has been given, somewhere.
        public static final int MPG123_BAD_VALUE = 39;
        /// < Low-level seek failed.
        public static final int MPG123_LSEEK_FAILED = 40;
    }

    /**
     * An enum over all sample types possibly known to mpg123.<br>
     * The values are designed as bit flags to allow bitmasking for encoding families.<br>
     * *  Note that (your build of) libmpg123 does not necessarily support all these.<br>
     * Usually, you can expect the 8bit encodings and signed 16 bit.<br>
     * Also 32bit float will be usual beginning with mpg123-1.7.0 .<br>
     * What you should bear in mind is that (SSE, etc) optimized routines are just for<br>
     * signed 16bit (and 8bit derived from that). Other formats use plain C code.<br>
     * *  All formats are in native byte order. On a little endian machine this should mean<br>
     * that you can just feed the MPG123_ENC_SIGNED_32 data to common 24bit hardware that<br>
     * ignores the lowest byte (or you could choose to do rounding with these lower bits).<br>
     * <i>native declaration : mpg123.h:237</i><br>
     * enum values
     */
    public static interface mpg123_enc_enum {
        /// < 0000 0000 1111 Some 8 bit  integer encoding.
        public static final int MPG123_ENC_8 = 15;
        /// < 0000 0100 0000 Some 16 bit integer encoding.
        public static final int MPG123_ENC_16 = 64;
        /// < 0001 0000 0000 Some 32 bit integer encoding.
        public static final int MPG123_ENC_32 = 256;
        /// < 0000 1000 0000 Some signed integer encoding.
        public static final int MPG123_ENC_SIGNED = 128;
        /// < 1110 0000 0000 Some float encoding.
        public static final int MPG123_ENC_FLOAT = 3584;
        /// <           1101 0000 signed 16 bit
        public static final int MPG123_ENC_SIGNED_16 = (MPG123_ENC_16 | MPG123_ENC_SIGNED | 16);
        /// <           0110 0000 unsigned 16 bit
        public static final int MPG123_ENC_UNSIGNED_16 = (MPG123_ENC_16 | 32);
        /// <           0000 0001 unsigned 8 bit
        public static final int MPG123_ENC_UNSIGNED_8 = 1;
        /// <           1000 0010 signed 8 bit
        public static final int MPG123_ENC_SIGNED_8 = (MPG123_ENC_SIGNED | 2);
        /// <           0000 0100 ulaw 8 bit
        public static final int MPG123_ENC_ULAW_8 = 4;
        /// <           0000 1000 alaw 8 bit
        public static final int MPG123_ENC_ALAW_8 = 8;
        /// < 0001 0001 1000 0000 signed 32 bit
        public static final int MPG123_ENC_SIGNED_32 = MPG123_ENC_32 | MPG123_ENC_SIGNED | 4096;
        /// < 0010 0001 0000 0000 unsigned 32 bit
        public static final int MPG123_ENC_UNSIGNED_32 = MPG123_ENC_32 | 8192;
        /// <      0010 0000 0000 32bit float
        public static final int MPG123_ENC_FLOAT_32 = 512;
        /// <      0100 0000 0000 64bit float
        public static final int MPG123_ENC_FLOAT_64 = 1024;
        public static final int MPG123_ENC_ANY = (MPG123_ENC_SIGNED_16 | MPG123_ENC_UNSIGNED_16 | MPG123_ENC_UNSIGNED_8 | MPG123_ENC_SIGNED_8 | MPG123_ENC_ULAW_8 | MPG123_ENC_ALAW_8 | MPG123_ENC_SIGNED_32 | MPG123_ENC_UNSIGNED_32 | MPG123_ENC_FLOAT_32 | MPG123_ENC_FLOAT_64);
    }

    /**
     * They can be combined into one number (3) to indicate mono and stereo...<br>
     * <i>native declaration : mpg123.h:261</i><br>
     * enum values
     */
    public static interface mpg123_channelcount {
        public static final int MPG123_MONO = 1;
        public static final int MPG123_STEREO = 2;
    }

    /**
     * <i>native declaration : mpg123.h:456</i><br>
     * enum values
     */
    public static interface mpg123_channels {
        /// < The Left Channel.
        public static final int MPG123_LEFT = 1;
        /// < The Right Channel.
        public static final int MPG123_RIGHT = 2;
        /// < Both left and right channel; same as MPG123_LEFT|MPG123_RIGHT
        public static final int MPG123_LR = 3;
    }

    /**
     * Enumeration of the mode types of Variable Bitrate<br>
     * <i>native declaration : mpg123.h:502</i><br>
     * enum values
     */
    public static interface mpg123_vbr {
        /// < Constant Bitrate Mode (default)
        public static final int MPG123_CBR = 0;
        /// < Variable Bitrate Mode
        public static final int MPG123_VBR = 1;
        /// < Average Bitrate Mode
        public static final int MPG123_ABR = 2;
    }

    /**
     * Enumeration of the MPEG Versions<br>
     * <i>native declaration : mpg123.h:509</i><br>
     * enum values
     */
    public static interface mpg123_version {
        /// < MPEG Version 1.0
        public static final int MPG123_1_0 = 0;
        /// < MPEG Version 2.0
        public static final int MPG123_2_0 = 1;
        /// < MPEG Version 2.5
        public static final int MPG123_2_5 = 2;
    }

    /**
     * Enumeration of the MPEG Audio mode.<br>
     * Only the mono mode has 1 channel, the others have 2 channels.<br>
     * <i>native declaration : mpg123.h:518</i><br>
     * enum values
     */
    public static interface mpg123_mode {
        /// < Standard Stereo.
        public static final int MPG123_M_STEREO = 0;
        /// < Joint Stereo.
        public static final int MPG123_M_JOINT = 1;
        /// < Dual Channel.
        public static final int MPG123_M_DUAL = 2;
        /// < Single Channel.
        public static final int MPG123_M_MONO = 3;
    }

    /**
     * Enumeration of the MPEG Audio flag bits<br>
     * <i>native declaration : mpg123.h:527</i><br>
     * enum values
     */
    public static interface mpg123_flags {
        /// < The bitstream is error protected using 16-bit CRC.
        public static final int MPG123_CRC = 1;
        /// < The bitstream is copyrighted.
        public static final int MPG123_COPYRIGHT = 2;
        /// < The private bit has been set.
        public static final int MPG123_PRIVATE = 4;
        /// < The bitstream is an original, not a copy.
        public static final int MPG123_ORIGINAL = 8;
    }

    /**
     * The key values for state information from mpg123_getstate().<br>
     * <i>native declaration : mpg123.h:581</i><br>
     * enum values
     */
    public static interface mpg123_state {
        /// < Query if positons are currently accurate (integer value, 0 if false, 1 if true)
        public static final int MPG123_ACCURATE = 1;
    }

    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h</i>
    public static final int MPG123_ID3 = 3;
    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h</i>
    public static final int MPG123_ICY = 12;
    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h</i>
    public static final int MPG123_NEW_ID3 = 1;
    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h</i>
    public static final int MPG123_NEW_ICY = 4;

    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h:607</i>

    public static class mpg123_string extends com.sun.jna.Structure {
        /// Allocate a new mpg123_string struct on the heap

        public mpg123_string() {
        }

        /// Cast data at given memory location (pointer + offset) as an existing mpg123_string struct

        public mpg123_string(com.sun.jna.Pointer pointer, int offset) {
            super();
            useMemory(pointer, offset);
            read();
        }

        /// Create an instance that shares its memory with another mpg123_string instance

        public mpg123_string(mpg123_string struct) {
            this(struct.getPointer(), 0);
        }

        public static class ByReference extends mpg123_string implements com.sun.jna.Structure.ByReference {
            /// Allocate a new mpg123_string.ByRef struct on the heap

            public ByReference() {
            }

            /// Create an instance that shares its memory with another mpg123_string instance

            public ByReference(mpg123_string struct) {
                super(struct.getPointer(), 0);
            }
        }

        public static class ByValue extends mpg123_string implements com.sun.jna.Structure.ByValue {
            /// Allocate a new mpg123_string.ByVal struct on the heap

            public ByValue() {
            }

            /// Create an instance that shares its memory with another mpg123_string instance

            public ByValue(mpg123_string struct) {
                super(struct.getPointer(), 0);
            }
        }

        /// < pointer to the string data
        public ByteByReference p;
        /// < raw number of bytes allocated
        public NativeLong size;
        /// < number of used bytes (including closing zero byte)
        public NativeLong fill;
    }

    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h:659</i>

    public static class mpg123_text extends com.sun.jna.Structure {
        /// Allocate a new mpg123_text struct on the heap

        public mpg123_text() {
        }

        /// Cast data at given memory location (pointer + offset) as an existing mpg123_text struct

        public mpg123_text(com.sun.jna.Pointer pointer, int offset) {
            super();
            useMemory(pointer, offset);
            read();
        }

        /// Create an instance that shares its memory with another mpg123_text instance

        public mpg123_text(mpg123_text struct) {
            this(struct.getPointer(), 0);
        }

        public static class ByReference extends mpg123_text implements com.sun.jna.Structure.ByReference {
            /// Allocate a new mpg123_text.ByRef struct on the heap

            public ByReference() {
            }

            /// Create an instance that shares its memory with another mpg123_text instance

            public ByReference(mpg123_text struct) {
                super(struct.getPointer(), 0);
            }
        }

        public static class ByValue extends mpg123_text implements com.sun.jna.Structure.ByValue {
            /// Allocate a new mpg123_text.ByVal struct on the heap

            public ByValue() {
            }

            /// Create an instance that shares its memory with another mpg123_text instance

            public ByValue(mpg123_text struct) {
                super(struct.getPointer(), 0);
            }
        }

        /// < Three-letter language code (not terminated).
        public byte[] lang = new byte[(3)];
        /// < The ID3v2 text field id, like TALB, TPE2, ... (4 characters, no string termination).
        public byte[] id = new byte[(4)];
        /// < Empty for the generic comment...
        public libmpg123.mpg123_string description;
        /// < ...
        public libmpg123.mpg123_string text;
    }

    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h:672</i>

    public static class mpg123_id3v2 extends com.sun.jna.Structure {
        /// Allocate a new mpg123_id3v2 struct on the heap

        public mpg123_id3v2() {
        }

        /// Cast data at given memory location (pointer + offset) as an existing mpg123_id3v2 struct

        public mpg123_id3v2(com.sun.jna.Pointer pointer, int offset) {
            super();
            useMemory(pointer, offset);
            read();
        }

        /// Create an instance that shares its memory with another mpg123_id3v2 instance

        public mpg123_id3v2(mpg123_id3v2 struct) {
            this(struct.getPointer(), 0);
        }

        public static class ByReference extends mpg123_id3v2 implements com.sun.jna.Structure.ByReference {
            /// Allocate a new mpg123_id3v2.ByRef struct on the heap

            public ByReference() {
            }

            /// Create an instance that shares its memory with another mpg123_id3v2 instance

            public ByReference(mpg123_id3v2 struct) {
                super(struct.getPointer(), 0);
            }
        }

        public static class ByValue extends mpg123_id3v2 implements com.sun.jna.Structure.ByValue {
            /// Allocate a new mpg123_id3v2.ByVal struct on the heap

            public ByValue() {
            }

            /// Create an instance that shares its memory with another mpg123_id3v2 instance

            public ByValue(mpg123_id3v2 struct) {
                super(struct.getPointer(), 0);
            }
        }

        /// < 3 or 4 for ID3v2.3 or ID3v2.4.
        public byte version;
        /// < Title string (pointer into text_list).
        public mpg123_string title;
        /// < Artist string (pointer into text_list).
        public mpg123_string artist;
        /// < Album string (pointer into text_list).
        public mpg123_string album;
        /// < The year as a string (pointer into text_list).
        public mpg123_string year;
        /// < Genre String (pointer into text_list). The genre string(s) may very well need postprocessing, esp. for ID3v2.3.
        public mpg123_string genre;
        /// < Pointer to last encountered comment text with empty description.
        public mpg123_string comment;
        /**
         * Encountered ID3v2 fields are appended to these lists.<br>
         * There can be multiple occurences, the pointers above always point to the last encountered data.<br>
         * < Array of comments.
         */
        public mpg123_text comment_list;
        /// < Number of comments.
        public NativeLong comments;
        /// < Array of ID3v2 text fields (including USLT)
        public mpg123_text text;
        /// < Numer of text fields.
        public NativeLong texts;
        /// < The array of extra (TXXX) fields.
        public mpg123_text extra;
        /// < Number of extra text (TXXX) fields.
        public NativeLong extras;
    }

    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h:695</i>

    public static class mpg123_id3v1 extends com.sun.jna.Structure {
        /// Allocate a new mpg123_id3v1 struct on the heap

        public mpg123_id3v1() {
        }

        /// Cast data at given memory location (pointer + offset) as an existing mpg123_id3v1 struct

        public mpg123_id3v1(com.sun.jna.Pointer pointer, int offset) {
            super();
            useMemory(pointer, offset);
            read();
        }

        /// Create an instance that shares its memory with another mpg123_id3v1 instance

        public mpg123_id3v1(mpg123_id3v1 struct) {
            this(struct.getPointer(), 0);
        }

        public static class ByReference extends mpg123_id3v1 implements com.sun.jna.Structure.ByReference {
            /// Allocate a new mpg123_id3v1.ByRef struct on the heap

            public ByReference() {
            }

            /// Create an instance that shares its memory with another mpg123_id3v1 instance

            public ByReference(mpg123_id3v1 struct) {
                super(struct.getPointer(), 0);
            }
        }

        public static class ByValue extends mpg123_id3v1 implements com.sun.jna.Structure.ByValue {
            /// Allocate a new mpg123_id3v1.ByVal struct on the heap

            public ByValue() {
            }

            /// Create an instance that shares its memory with another mpg123_id3v1 instance

            public ByValue(mpg123_id3v1 struct) {
                super(struct.getPointer(), 0);
            }
        }

        /// < Always the string "TAG", the classic intro.
        public byte[] tag = new byte[(3)];
        /// < Title string.
        public byte[] title = new byte[(30)];
        /// < Artist string.
        public byte[] artist = new byte[(30)];
        /// < Album string.
        public byte[] album = new byte[(30)];
        /// < Year string.
        public byte[] year = new byte[(4)];
        /// < Comment string.
        public byte[] comment = new byte[(30)];
        /// < Genre index.
        public byte genre;
    }

    /**
     * Data structure for storing information about a frame of MPEG Audio<br>
     * <i>native declaration : mpg123.h:535</i>
     */
    public static class mpg123_frameinfo extends com.sun.jna.Structure {
        /// Allocate a new mpg123_frameinfo struct on the heap

        public mpg123_frameinfo() {
        }

        /// Cast data at given memory location (pointer + offset) as an existing mpg123_frameinfo struct

        public mpg123_frameinfo(com.sun.jna.Pointer pointer, int offset) {
            super();
            useMemory(pointer, offset);
            read();
        }

        /// Create an instance that shares its memory with another mpg123_frameinfo instance

        public mpg123_frameinfo(mpg123_frameinfo struct) {
            this(struct.getPointer(), 0);
        }

        public static class ByReference extends mpg123_frameinfo implements com.sun.jna.Structure.ByReference {
            /// Allocate a new mpg123_frameinfo.ByRef struct on the heap

            public ByReference() {
            }

            /// Create an instance that shares its memory with another mpg123_frameinfo instance

            public ByReference(mpg123_frameinfo struct) {
                super(struct.getPointer(), 0);
            }
        }

        public static class ByValue extends mpg123_frameinfo implements com.sun.jna.Structure.ByValue {
            /// Allocate a new mpg123_frameinfo.ByVal struct on the heap

            public ByValue() {
            }

            /// Create an instance that shares its memory with another mpg123_frameinfo instance

            public ByValue(mpg123_frameinfo struct) {
                super(struct.getPointer(), 0);
            }
        }

        /**
         * < The MPEG version (1.0/2.0/2.5).
         */
        public int version;
        /// < The MPEG Audio Layer (MP1/MP2/MP3).
        public int layer;
        /// < The sampling rate in Hz.
        public NativeLong rate;
        /**
         * < The audio mode (Mono, Stereo, Joint-stero, Dual Channel).
         */
        public int mode;
        /// < The mode extension bit flag.
        public int mode_ext;
        /// < The size of the frame (in bytes).
        public int framesize;
        /**
         * < MPEG Audio flag bits.
         */
        public int flags;
        /// < The emphasis type.
        public int emphasis;
        /// < Bitrate of the frame (kbps).
        public int bitrate;
        /// < The target average bitrate.
        public int abr_rate;
        /**
         * < The VBR mode.
         */
        public int vbr;
    }

    /// <i>native declaration : com.tulskiy.musique.jna.mpg123.h:816</i>

    public interface r_lseek extends com.sun.jna.Callback {
        int invoke(int int1, int int2, int int3);
    }

    /**
     * Original signature : <code>int mpg123_init()</code><br>
     * <i>native declaration : mpg123.h:35</i>
     *
     * @return
     */
    int mpg123_init();

    /**
     * Original signature : <code>void mpg123_exit()</code><br>
     * <i>native declaration : mpg123.h:39</i>
     */
    void mpg123_exit();

    /**
     * Original signature : <code>mpg123_handle* mpg123_new(const char*, int*)</code><br>
     * <i>native declaration : mpg123.h:47</i>
     *
     * @param decoder
     * @param error
     * @return
     */
    mpg123_handle_struct mpg123_new(String decoder, IntBuffer error);

    /**
     * Original signature : <code>void mpg123_delete(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:50</i>
     *
     * @param mh
     */
    void mpg123_delete(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_param(mpg123_handle*, mpg123_parms, long, double)</code><br>
     * <i>native declaration : mpg123.h:104</i><br>
     *
     * @param mh
     * @param type   @see mpg123.Mpg123Library#mpg123_parms
     * @param value
     * @param fvalue
     * @return
     */
    int mpg123_param(mpg123_handle_struct mh, int type, NativeLong value, double fvalue);

    /**
     * Original signature : <code>int mpg123_getparam(mpg123_handle*, mpg123_parms, long*, double*)</code><br>
     * <i>native declaration : mpg123.h:108</i><br>
     *
     * @param mh
     * @param type @see mpg123.Mpg123Library#mpg123_parms
     * @param val
     * @param fval
     * @return
     */
    int mpg123_getparam(mpg123_handle_struct mh, int type, NativeLongByReference val, DoubleBuffer fval);

    /**
     * Original signature : <code>char* mpg123_plain_strerror(int)</code><br>
     * <i>native declaration : mpg123.h:176</i>
     *
     * @param errcode
     * @return
     */
    ByteByReference mpg123_plain_strerror(int errcode);

    /**
     * Original signature : <code>char* mpg123_strerror(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:182</i>
     *
     * @param mh
     * @return
     */
    String mpg123_strerror(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_errcode(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:185</i>
     *
     * @param mh
     * @return
     */
    int mpg123_errcode(mpg123_handle_struct mh);

    /**
     * Original signature : <code>char** mpg123_decoders()</code><br>
     * <i>native declaration : mpg123.h:199</i>
     *
     * @return
     */
    PointerByReference mpg123_decoders();

    /**
     * Original signature : <code>char** mpg123_supported_decoders()</code><br>
     * <i>native declaration : mpg123.h:202</i>
     *
     * @return
     */
    String[] mpg123_supported_decoders();

    /**
     * Original signature : <code>int mpg123_decoder(mpg123_handle*, const char*)</code><br>
     * <i>native declaration : mpg123.h:205</i>
     *
     * @param mh
     * @param decoder_name
     * @return
     */
    int mpg123_decoder(mpg123_handle_struct mh, String decoder_name);

    /**
     * Original signature : <code>char* mpg123_current_decoder(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:212</i>
     *
     * @param mh
     * @return
     */
    String mpg123_current_decoder(mpg123_handle_struct mh);

    /**
     * Original signature : <code>void mpg123_rates(const long**, size_t*)</code><br>
     * <i>native declaration : mpg123.h:272</i>
     *
     * @param list
     * @param number
     */
    void mpg123_rates(PointerByReference list, NativeLongByReference number);

    /**
     * Original signature : <code>void mpg123_encodings(const int**, size_t*)</code><br>
     * <i>native declaration : mpg123.h:278</i>
     *
     * @param list
     * @param number
     */
    void mpg123_encodings(PointerByReference list, NativeLongByReference number);

    /**
     * Original signature : <code>int mpg123_format_none(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:282</i>
     *
     * @param mh
     * @return
     */
    int mpg123_format_none(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_format_all(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:286</i>
     *
     * @param mh
     * @return
     */
    int mpg123_format_all(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_format(mpg123_handle*, long, int, int)</code><br>
     * <i>native declaration : mpg123.h:294</i>
     *
     * @param mh
     * @param rate
     * @param channels
     * @param encodings
     * @return
     */
    int mpg123_format(mpg123_handle_struct mh, NativeLong rate, int channels, int encodings);

    /**
     * Original signature : <code>int mpg123_format_support(mpg123_handle*, long, int)</code><br>
     * <i>native declaration : mpg123.h:300</i>
     *
     * @param mh
     * @param rate
     * @param encoding
     * @return
     */
    int mpg123_format_support(mpg123_handle_struct mh, NativeLong rate, int encoding);

    /**
     * Original signature : <code>int mpg123_getformat(mpg123_handle*, long*, int*, int*)</code><br>
     * <i>native declaration : mpg123.h:303</i>
     *
     * @param mh
     * @param rate
     * @param channels
     * @param encoding
     * @return
     */
    int mpg123_getformat(mpg123_handle_struct mh, NativeLongByReference rate, IntByReference channels, IntByReference encoding);

    /**
     * Original signature : <code>int mpg123_open(mpg123_handle*, const char*)</code><br>
     * <i>native declaration : mpg123.h:322</i>
     *
     * @param mh
     * @param path
     * @return
     */
    int mpg123_open(mpg123_handle_struct mh, String path);

    int mpg123_open_64(mpg123_handle_struct mh, String path);

    /**
     * Original signature : <code>int mpg123_open_fd(mpg123_handle*, int)</code><br>
     * <i>native declaration : mpg123.h:327</i>
     *
     * @param mh
     * @param fd
     * @return
     */
    int mpg123_open_fd(mpg123_handle_struct mh, int fd);

    /**
     * Original signature : <code>int mpg123_open_feed(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:332</i>
     *
     * @param mh
     * @return
     */
    int mpg123_open_feed(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_close(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:335</i>
     *
     * @param mh
     * @return
     */
    int mpg123_close(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_read(mpg123_handle*, unsigned char*, size_t, size_t*)</code><br>
     * <i>native declaration : mpg123.h:342</i>
     *
     * @param mh
     * @param outmemory
     * @param outmemsize
     * @param done
     * @return
     */
    int mpg123_read(mpg123_handle_struct mh, ByteBuffer outmemory, NativeLong outmemsize, NativeLongByReference done);

    /**
     * Original signature : <code>int mpg123_decode(mpg123_handle*, const unsigned char*, size_t, unsigned char*, size_t, size_t*)</code><br>
     * <i>native declaration : mpg123.h:365</i>
     *
     * @param mh
     * @param inmemory
     * @param inmemsize
     * @param outmemory
     * @param outmemsize
     * @param done
     * @return
     */
    int mpg123_decode(mpg123_handle_struct mh, byte inmemory[], NativeLong inmemsize, byte[] outmemory, NativeLong outmemsize, NativeLongByReference done);

    /**
     * Original signature : <code>int mpg123_decode(mpg123_handle*, const unsigned char*, size_t, unsigned char*, size_t, size_t*)</code><br>
     * <i>native declaration : mpg123.h:365</i>
     *
     * @param mh
     * @param inmemory
     * @param inmemsize
     * @param outmemory
     * @param outmemsize
     * @param done
     * @return
     */
//    int mpg123_decode(mpg123_handle_struct mh, ByteBuffer inmemory, NativeLong inmemsize, ByteBuffer outmemory, NativeLong outmemsize, NativeLongByReference done);

    /**
     * Original signature : <code>int mpg123_decode_frame(mpg123_handle*, int*, unsigned char**, size_t*)</code><br>
     * <i>native declaration : mpg123.h:374</i>
     *
     * @param mh
     * @param num
     * @param audio
     * @param bytes
     * @return
     */
    int mpg123_decode_frame(mpg123_handle_struct mh, IntByReference num, PointerByReference audio, NativeLongByReference bytes);

    int mpg123_decode_frame_64(mpg123_handle_struct mh, IntByReference num, PointerByReference audio, NativeLongByReference bytes);

    /**
     * Original signature : <code>int mpg123_tell(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:400</i>
     *
     * @param mh
     * @return
     */
    int mpg123_tell(mpg123_handle_struct mh);

    int mpg123_tell_64(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_tellframe(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:403</i>
     *
     * @param mh
     * @return
     */
    int mpg123_tellframe(mpg123_handle_struct mh);

    int mpg123_tellframe_64(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_tell_stream(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:406</i>
     *
     * @param mh
     * @return
     */
    int mpg123_tell_stream(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_seek(mpg123_handle*, int, int)</code><br>
     * <i>native declaration : mpg123.h:411</i>
     *
     * @param mh
     * @param sampleoff
     * @param whence
     * @return
     */
    int mpg123_seek(mpg123_handle_struct mh, int sampleoff, int whence);

    int mpg123_seek_64(mpg123_handle_struct mh, NativeLong sampleoff, long whence);

    /**
     * Original signature : <code>int mpg123_feedseek(mpg123_handle*, int, int, int*)</code><br>
     * <i>native declaration : mpg123.h:418</i>
     *
     * @param mh
     * @param sampleoff
     * @param whence
     * @param input_offset
     * @return
     */
    int mpg123_feedseek(mpg123_handle_struct mh, int sampleoff, int whence, IntBuffer input_offset);

    /**
     * Original signature : <code>int mpg123_seek_frame(mpg123_handle*, int, int)</code><br>
     * <i>native declaration : mpg123.h:423</i>
     *
     * @param mh
     * @param frameoff
     * @param whence
     * @return
     */
    int mpg123_seek_frame(mpg123_handle_struct mh, int frameoff, int whence);

    int mpg123_seek_frame_64(mpg123_handle_struct mh, NativeLong frameoff, long whence);

    /**
     * Original signature : <code>int mpg123_timeframe(mpg123_handle*, double)</code><br>
     * <i>native declaration : mpg123.h:428</i>
     *
     * @param mh
     * @param sec
     * @return
     */
    int mpg123_timeframe(mpg123_handle_struct mh, double sec);

    int mpg123_timeframe_64(mpg123_handle_struct mh, double sec);

    /**
     * Original signature : <code>int mpg123_index(mpg123_handle*, int**, int*, size_t*)</code><br>
     * <i>native declaration : mpg123.h:435</i>
     *
     * @param mh
     * @param offsets
     * @param step
     * @param fill
     * @return
     */
    int mpg123_index(mpg123_handle_struct mh, PointerByReference offsets, IntBuffer step, NativeLongByReference fill);

    /**
     * Original signature : <code>int mpg123_position(mpg123_handle*, int, int, int*, int*, double*, double*)</code><br>
     * <i>native declaration : mpg123.h:443</i>
     *
     * @param mh
     * @param frame_offset
     * @param buffered_bytes
     * @param current_frame
     * @param frames_left
     * @param current_seconds
     * @param seconds_left
     * @return
     */
    int mpg123_position(mpg123_handle_struct mh, int frame_offset, int buffered_bytes, IntBuffer current_frame, IntBuffer frames_left, DoubleBuffer current_seconds, DoubleBuffer seconds_left);

    int mpg123_position_64(mpg123_handle_struct mh, int frame_offset, int buffered_bytes, IntBuffer current_frame, IntBuffer frames_left, DoubleBuffer current_seconds, DoubleBuffer seconds_left);

    /**
     * Original signature : <code>int mpg123_eq(mpg123_handle*, mpg123_channels, int, double)</code><br>
     * <i>native declaration : mpg123.h:467</i><br>
     *
     * @param mh
     * @param channel @see mpg123.Mpg123Library#mpg123_channels
     * @param band
     * @param val
     * @return
     */
    int mpg123_eq(mpg123_handle_struct mh, int channel, int band, double val);

    /**
     * Original signature : <code>double mpg123_geteq(mpg123_handle*, mpg123_channels, int)</code><br>
     * <i>native declaration : mpg123.h:473</i><br>
     *
     * @param mh
     * @param channel @see mpg123.Mpg123Library#mpg123_channels
     * @param band
     * @return
     */
    double mpg123_geteq(mpg123_handle_struct mh, int channel, int band);

    /**
     * Original signature : <code>int mpg123_reset_eq(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:476</i>
     *
     * @param mh
     * @return
     */
    int mpg123_reset_eq(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_volume(mpg123_handle*, double)</code><br>
     * <i>native declaration : mpg123.h:480</i>
     *
     * @param mh
     * @param vol
     * @return
     */
    int mpg123_volume(mpg123_handle_struct mh, double vol);

    /**
     * Original signature : <code>int mpg123_volume_change(mpg123_handle*, double)</code><br>
     * <i>native declaration : mpg123.h:483</i>
     *
     * @param mh
     * @param change
     * @return
     */
    int mpg123_volume_change(mpg123_handle_struct mh, double change);

    /**
     * Original signature : <code>int mpg123_getvolume(mpg123_handle*, double*, double*, double*)</code><br>
     * <i>native declaration : mpg123.h:489</i>
     *
     * @param mh
     * @param base
     * @param really
     * @param rva_db
     * @return
     */
    int mpg123_getvolume(mpg123_handle_struct mh, DoubleBuffer base, DoubleBuffer really, DoubleBuffer rva_db);

    /**
     * Original signature : <code>int mpg123_info(mpg123_handle*, mpg123_frameinfo*)</code><br>
     * <i>native declaration : mpg123.h:551</i>
     *
     * @param mh
     * @param mi
     * @return
     */
    int mpg123_info(mpg123_handle_struct mh, libmpg123.mpg123_frameinfo mi);

    /**
     * Original signature : <code>size_t mpg123_safe_buffer()</code><br>
     * <i>native declaration : mpg123.h:554</i>
     *
     * @return
     */
    NativeLong mpg123_safe_buffer();

    /**
     * Original signature : <code>int mpg123_scan(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:562</i>
     *
     * @param mh
     * @return
     */
    int mpg123_scan(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_length(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:566</i>
     *
     * @param mh
     * @return
     */
    int mpg123_length(mpg123_handle_struct mh);

    int mpg123_length_64(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_set_filesize(mpg123_handle*, int)</code><br>
     * <i>native declaration : mpg123.h:571</i>
     *
     * @param mh
     * @param size
     * @return
     */
    int mpg123_set_filesize(mpg123_handle_struct mh, int size);

    /**
     * Original signature : <code>double mpg123_tpf(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:574</i>
     *
     * @param mh
     * @return
     */
    double mpg123_tpf(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_getstate(mpg123_handle*, mpg123_state, long*, double*)</code><br>
     * <i>native declaration : mpg123.h:592</i><br>
     *
     * @param mh
     * @param key  @see mpg123.Mpg123Library#mpg123_state
     * @param val
     * @param fval
     * @return
     */
    int mpg123_getstate(mpg123_handle_struct mh, int key, NativeLongByReference val, DoubleBuffer fval);

    /**
     * Original signature : <code>void mpg123_init_string(mpg123_string*)</code><br>
     * <i>native declaration : mpg123.h:615</i>
     *
     * @param sb
     */
    void mpg123_init_string(mpg123_string sb);

    /**
     * Original signature : <code>void mpg123_free_string(mpg123_string*)</code><br>
     * <i>native declaration : mpg123.h:618</i>
     *
     * @param sb
     */
    void mpg123_free_string(mpg123_string sb);

    /**
     * Original signature : <code>int mpg123_resize_string(mpg123_string*, size_t)</code><br>
     * <i>native declaration : mpg123.h:622</i>
     *
     * @param sb
     * @param news
     * @return
     */
    int mpg123_resize_string(mpg123_string sb, NativeLong news);

    /**
     * Original signature : <code>int mpg123_grow_string(mpg123_string*, size_t)</code><br>
     * <i>native declaration : mpg123.h:628</i>
     *
     * @param sb
     * @param news
     * @return
     */
    int mpg123_grow_string(mpg123_string sb, NativeLong news);

    /**
     * Original signature : <code>int mpg123_copy_string(mpg123_string*, mpg123_string*)</code><br>
     * <i>native declaration : mpg123.h:632</i>
     *
     * @param from
     * @param to
     * @return
     */
    int mpg123_copy_string(mpg123_string from, mpg123_string to);

    /**
     * Original signature : <code>int mpg123_add_string(mpg123_string*, const char*)</code><br>
     * <i>native declaration : mpg123.h:636</i>
     *
     * @param sb
     * @param stuff
     * @return
     */
    int mpg123_add_string(mpg123_string sb, String stuff);

    /**
     * Original signature : <code>int mpg123_add_substring(mpg123_string*, const char*, size_t, size_t)</code><br>
     * <i>native declaration : mpg123.h:642</i>
     *
     * @param sb
     * @param stuff
     * @param from
     * @param count
     * @return
     */
    int mpg123_add_substring(mpg123_string sb, String stuff, NativeLong from, NativeLong count);

    /**
     * Original signature : <code>int mpg123_set_string(mpg123_string*, const char*)</code><br>
     * <i>native declaration : mpg123.h:646</i>
     *
     * @param sb
     * @param stuff
     * @return
     */
    int mpg123_set_string(mpg123_string sb, String stuff);

    /**
     * Original signature : <code>int mpg123_set_substring(mpg123_string*, const char*, size_t, size_t)</code><br>
     * <i>native declaration : mpg123.h:652</i>
     *
     * @param sb
     * @param stuff
     * @param from
     * @param count
     * @return
     */
    int mpg123_set_substring(mpg123_string sb, String stuff, NativeLong from, NativeLong count);

    /**
     * Original signature : <code>int mpg123_meta_check(mpg123_handle*)</code><br>
     *
     * @param mh On error (no valid handle) just 0 is returned.<br>
     *           On error (no valid handle) just 0 is returned.<br>
     *           <i>native declaration : mpg123.h:713</i>
     * @return
     */
    int mpg123_meta_check(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_id3(mpg123_handle*, mpg123_id3v1**, mpg123_id3v2**)</code><br>
     * <i>native declaration : mpg123.h:718</i>
     *
     * @param mh
     * @param v1
     * @param v2
     * @return
     */
    int mpg123_id3(mpg123_handle_struct mh, PointerByReference v1, PointerByReference v2);

    /**
     * Original signature : <code>int mpg123_icy(mpg123_handle*, char**)</code><br>
     *
     * @param mh       same for ICY meta string<br>
     * @param icy_meta same for ICY meta string<br>
     *                 same for ICY meta string<br>
     *                 <i>native declaration : mpg123.h:722</i>
     * @return
     */
    int mpg123_icy(mpg123_handle_struct mh, PointerByReference icy_meta);

    /**
     * Original signature : <code>char* mpg123_icy2utf8(const char*)</code><br>
     * <i>native declaration : mpg123.h:727</i>
     *
     * @param icy_text
     * @return
     */
    ByteByReference mpg123_icy2utf8(String icy_text);

    /**
     * Original signature : <code>mpg123_handle* mpg123_parnew(mpg123_pars*, const char*, int*)</code><br>
     * <i>native declaration : mpg123.h:755</i>
     *
     * @param mp
     * @param decoder
     * @param error
     * @return
     */
    mpg123_handle_struct mpg123_parnew(libmpg123.mpg123_pars_struct mp, String decoder, IntBuffer error);

    /**
     * Original signature : <code>mpg123_pars* mpg123_new_pars(int*)</code><br>
     * <i>native declaration : mpg123.h:758</i>
     *
     * @param error
     * @return
     */
    libmpg123.mpg123_pars_struct mpg123_new_pars(IntBuffer error);

    /**
     * Original signature : <code>void mpg123_delete_pars(mpg123_pars*)</code><br>
     * <i>native declaration : mpg123.h:761</i>
     *
     * @param mp
     */
    void mpg123_delete_pars(libmpg123.mpg123_pars_struct mp);

    /**
     * Original signature : <code>int mpg123_fmt_none(mpg123_pars*)</code><br>
     * <i>native declaration : mpg123.h:765</i>
     *
     * @param mp
     * @return
     */
    int mpg123_fmt_none(mpg123_pars_struct mp);

    /**
     * Original signature : <code>int mpg123_fmt_all(mpg123_pars*)</code><br>
     * <i>native declaration : mpg123.h:769</i>
     *
     * @param mp
     * @return
     */
    // @com.ochafik.lang.jnaerator.Mangling({"_Z14mpg123_fmt_allP18mpg123_pars_struct", "?mpg123_fmt_all@@YAHPA18mpg123_pars_struct@Z"})
    int mpg123_fmt_all(mpg123_pars_struct mp);

    /**
     * Original signature : <code>int mpg123_fmt(mpg123_pars*, long, int, int)</code><br>
     *
     * @param mh        0 is good, -1 is error<br>
     * @param rate      0 is good, -1 is error<br>
     * @param channels  0 is good, -1 is error<br>
     * @param encodings 0 is good, -1 is error<br>
     *                  0 is good, -1 is error<br>
     *                  <i>native declaration : mpg123.h:777</i>
     * @return
     */
    int mpg123_fmt(mpg123_pars_struct mh, NativeLong rate, int channels, int encodings);

    /**
     * Original signature : <code>int mpg123_fmt_support(mpg123_pars*, long, int)</code><br>
     * <i>native declaration : mpg123.h:783</i>
     *
     * @param mh
     * @param rate
     * @param encoding
     * @return
     */
    int mpg123_fmt_support(mpg123_pars_struct mh, NativeLong rate, int encoding);

    /**
     * Original signature : <code>int mpg123_par(mpg123_pars*, mpg123_parms, long, double)</code><br>
     * <i>native declaration : mpg123.h:787</i><br>
     *
     * @param mp
     * @param type   @see mpg123.Mpg123Library#mpg123_parms
     * @param value
     * @param fvalue
     * @return
     */
    // @com.ochafik.lang.jnaerator.Mangling({"_Z10mpg123_parP18mpg123_pars_struct12mpg123_parmsld", "?mpg123_par@@YAHPA18mpg123_pars_struct12mpg123_parmsJN@Z"})
    int mpg123_par(libmpg123.mpg123_pars_struct mp, int type, NativeLong value, double fvalue);

    /**
     * Original signature : <code>int mpg123_getpar(mpg123_pars*, mpg123_parms, long*, double*)</code><br>
     * <i>native declaration : mpg123.h:791</i><br>
     *
     * @param mp
     * @param type @see mpg123.Mpg123Library#mpg123_parms
     * @param val
     * @param fval
     * @return
     */
    int mpg123_getpar(libmpg123.mpg123_pars_struct mp, int type, NativeLongByReference val, DoubleBuffer fval);

    /**
     * Original signature : <code>int mpg123_replace_buffer(mpg123_handle*, unsigned char*, size_t)</code><br>
     * <i>native declaration : mpg123.h:803</i>
     *
     * @param mh
     * @param data
     * @param size
     * @return
     */
    int mpg123_replace_buffer(mpg123_handle_struct mh, ByteBuffer data, NativeLong size);

    /**
     * Original signature : <code>size_t mpg123_outblock(mpg123_handle*)</code><br>
     * <i>native declaration : mpg123.h:807</i>
     *
     * @param mh
     * @return
     */
    NativeLong mpg123_outblock(mpg123_handle_struct mh);

    /**
     * Original signature : <code>int mpg123_replace_reader(mpg123_handle*, r_lseek)</code><br>
     * <i>native declaration : mpg123.h:814</i>
     *
     * @param mh
     * @param arg1
     * @return
     */
    int mpg123_replace_reader(mpg123_handle_struct mh, r_lseek arg1);

    public static class mpg123_pars_struct extends com.sun.jna.PointerType {
        public mpg123_pars_struct(com.sun.jna.Pointer pointer) {
            super(pointer);
        }

        public mpg123_pars_struct() {
            super();
        }
    }

    public static class mpg123_handle_struct extends com.sun.jna.PointerType {
        public mpg123_handle_struct(com.sun.jna.Pointer pointer) {
            super(pointer);
        }

        public mpg123_handle_struct() {
            super();
        }
    }
}
