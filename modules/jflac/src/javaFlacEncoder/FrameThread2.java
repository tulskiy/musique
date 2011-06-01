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
 * multi-threaded encodings of FLAC frames. It's job is to repeatedly get a
 * BlockEncodeRequest from a BlockThreadManager, and encode it.
 *
 * @author Preston Lacey
 */
public class FrameThread2 implements Runnable{
    Frame frame = null;
    ReentrantLock runLock = null;
    BlockThreadManager2 manager = null;
    /**
     * Constructor. Private to prevent it's use, as a Frame must be provided for
     * this FrameThread to be of any use.
     */
    private FrameThread2() {}

    /**
     * Constructor. Sets the Frame object that this FrameThread will use for
     * encodings.
     *
     * @param f Frame object to use for encoding.
     * @param manager BlockThreadManager to use as the BlockEncodeRequest source
     * and destination.
     */
    public FrameThread2(Frame f, BlockThreadManager2 manager) {
        super();
        if(f == null)
           System.err.println("Frame is null. Error.");
        frame = f;
        runLock = new ReentrantLock();
        this.manager = manager;
    }

    /**
     * Run method. This FrameThread will get a BlockEncodeRequest from the
     * BlockThreadManager, encode the block, return it to the manager, then
     * repeat. If no BlockEncodeRequest is available, or if it recieves a
     * request with the "frameNumber" field set to a negative value, it will
     * break the loop and end, notifying the manager it has ended.
     * 
     */
    public void run() {
        boolean process = true;
        synchronized(this) {
            BlockEncodeRequest ber = manager.getWaitingRequest();
            if(ber != null && ber.frameNumber < 0)
               ber = null;
            while(ber != null && process) {
               if(ber.frameNumber < 0) {
                  process = false;
               }
               else {//get available BlockEncodeRequest from manager
                  ber.encodedSamples = frame.encodeSamples(ber.samples, ber.count,
                          ber.start, ber.skip, ber.result, ber.frameNumber);
                  ber.valid = true;
                  manager.returnFinishedRequest(ber);
                  ber = manager.getWaitingRequest();
               }
            }
            manager.notifyFrameThreadExit(this);
        }
    }
}
