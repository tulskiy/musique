/*
 * Copyright (C) 2010  Preston Lacey http://javaflacencoder.sourceforge.net/
 * All Rights Reserved.
 *
 * This library is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public
 * License as published by the Free Software Foundation; either
 * version 2.1 of the License, or (at your option) any later version.
 *
 * This library is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this library; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package javaFlacEncoder;
import java.util.concurrent.locks.ReentrantLock;
/**
 * The FrameThread class provides threading support for a Frame object, allowing
 * multi-threaded encodings of FLAC frames.
 *
 * @author Preston Lacey
 */
public class FrameThread implements Runnable{
    private boolean dataLoaded = false;
    Frame frame = null;
    BlockEncodeRequest ber = null;
    ReentrantLock runLock = null;

    /**
     * Constructor. Private to prevent it's use, as a Frame must be provided for
     * this FrameThread to be of any use.
     */
    private FrameThread() {}

    /**
     * Constructor. Sets the Frame object that this FrameThread will use for
     * encodings.
     *
     * @param f Frame object to use for encoding.
     */
    public FrameThread(Frame f) {
        super();
        frame = f;
        runLock = new ReentrantLock();
        //frame.encodeSamples(samples, count, start, skip, null, frameNumber);
    }

    /**
     * Prepare to encode a frame, providing the BlockEncodeRequest which
     * contains the block of samples and necessary information to encode that
     * sample. Results of the encode will be available in the BlockEncodeRequest.
     *
     * @param ber BlockEncodeRequest containing necessary data to encode a frame.
     */
    synchronized public void prepareToEncodeFrame(BlockEncodeRequest ber) {
        this.ber = ber;
        dataLoaded = true;

    }

    /**
     * Get the BlockEncodeRequest currently set in this FrameThread, and
     * optionally clear the request from this object.
     *
     * @param clear true to remove the BlockEncodeRequest from this object, false
     * otherwise.
     *
     * @return BlockEncodeRequest that is currently set, null if unset.
     */
    synchronized public BlockEncodeRequest getRequest(boolean clear) {
        BlockEncodeRequest temp = ber;
        if(clear) {
            ber = null;
            dataLoaded = false;
        }
        return temp;
    }

    /**
     * Run method. If a BlockEncodeRequest has been set(by method
     * prepareToEncodeFrame(...) ), then calling this method will start the
     * encode of the frame.
     */
    public void run() {
        synchronized(this) {
            if(dataLoaded) {
                ber.encodedSamples = frame.encodeSamples(ber.samples, ber.count, ber.start,
                        ber.skip, ber.result, ber.frameNumber);
                dataLoaded = false;
                ber.valid = true;
                //_samples = null;//clear object we no longer care about
                //_result = null;//clear object we no longer care about
            }
            else {

            }
        }
    }
}
