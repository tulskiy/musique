package org.jaudiotagger.tag.id3;

import org.jaudiotagger.audio.mp3.MPEGFrameHeader;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.nio.ByteBuffer;

/**
 * Performs unsynchronization and synchronization tasks on a buffer.
 * <p/>
 * Is currently required for V23Tags and V24Frames
 */
public class ID3Unsynchronization {
    //Logger
    //public static Logger logger = //logger.getLogger("org.jaudiotagger.tag.id3");

    /**
     * Check if a byte array will require unsynchronization before being written as a tag.
     * If the byte array contains any $FF $E0 bytes, then it will require unsynchronization.
     *
     * @param abySource the byte array to be examined
     * @return true if unsynchronization is required, false otherwise
     */
    public static boolean requiresUnsynchronization(byte[] abySource) {
        for (int i = 0; i < abySource.length - 1; i++) {
            if (((abySource[i] & MPEGFrameHeader.SYNC_BYTE1) == MPEGFrameHeader.SYNC_BYTE1) && ((abySource[i + 1] & MPEGFrameHeader.SYNC_BYTE2) == MPEGFrameHeader.SYNC_BYTE2)) {
//                //logger.finest("Unsynchronisation required found bit at:" + i);
                return true;
            }
        }

        return false;
    }

    /**
     * Unsynchronize an array of bytes, this should only be called if the decision has already been made to
     * unsynchronize the byte array
     * <p/>
     * In order to prevent a media player from incorrectly interpreting the contents of a tag, all $FF bytes
     * followed by a byte with value >=224 must be followed by a $00 byte (thus, $FF $F0 sequences become $FF $00 $F0).
     * Additionally because unsynchronisation is being applied any existing $FF $00 have to be converted to
     * $FF $00 $00
     *
     * @param abySource a byte array to be unsynchronized
     * @return a unsynchronized representation of the source
     */
    public static byte[] unsynchronize(byte[] abySource) {
        ByteArrayInputStream oBAIS = new ByteArrayInputStream(abySource);
        ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();

        int count = 0;
        while (oBAIS.available() > 0) {
            int iVal = oBAIS.read();
            count++;
            oBAOS.write(iVal);
            if ((iVal & MPEGFrameHeader.SYNC_BYTE1) == MPEGFrameHeader.SYNC_BYTE1) {
                // if byte is $FF, we must check the following byte if there is one
                if (oBAIS.available() > 0) {
                    oBAIS.mark(1);  // remember where we were, if we don't need to unsynchronize
                    int iNextVal = oBAIS.read();
                    if ((iNextVal & MPEGFrameHeader.SYNC_BYTE2) == MPEGFrameHeader.SYNC_BYTE2) {
                        // we need to unsynchronize here
//                        //logger.finest("Writing unsynchronisation bit at:" + count);
                        oBAOS.write(0);
                        oBAOS.write(iNextVal);
                    } else if (iNextVal == 0) {
                        // we need to unsynchronize here
//                        //logger.finest("Inserting zero unsynchronisation bit at:" + count);
                        oBAOS.write(0);
                        oBAOS.write(iNextVal);
                    } else {
                        oBAIS.reset();
                    }
                }
            }
        }
        // if we needed to unsynchronize anything, and this tag ends with 0xff, we have to append a zero byte,
        // which will be removed on de-unsynchronization later
        if ((abySource[abySource.length - 1] & MPEGFrameHeader.SYNC_BYTE1) == MPEGFrameHeader.SYNC_BYTE1) {
            oBAOS.write(0);
        }
        return oBAOS.toByteArray();
    }

    /**
     * Synchronize an array of bytes, this should only be called if it has been determined the tag is unsynchronised
     * <p/>
     * Any patterns of the form $FF $00 should be replaced by $FF
     *
     * @param source a ByteBuffer to be unsynchronized
     * @return a synchronized representation of the source
     */
    public static ByteBuffer synchronize(ByteBuffer source) {
        ByteArrayOutputStream oBAOS = new ByteArrayOutputStream();
        while (source.hasRemaining()) {
            int byteValue = source.get();
            oBAOS.write(byteValue);
            if ((byteValue & MPEGFrameHeader.SYNC_BYTE1) == MPEGFrameHeader.SYNC_BYTE1) {
                // we are skipping if $00 byte but check not an end of stream
                if (source.hasRemaining()) {
                    int unsyncByteValue = source.get();
                    //If its the null byte we just ignore it
                    if (unsyncByteValue != 0) {
                        oBAOS.write(unsyncByteValue);
                    }
                }
            }
        }
        return ByteBuffer.wrap(oBAOS.toByteArray());
    }

}
