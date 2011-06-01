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
import java.util.concurrent.LinkedBlockingQueue;
import java.util.Map;
import java.util.HashMap;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.Vector;

/**
 * BlockThreadManager is used by FLACEncoder(when encoding with threads), to
 * dispatch BlockEncodeRequests to ThreadFrames which do the actual encode.
 *
 *
 * BlockThreadManager2 accepts BlockEncodeRequest objects to encode.
 * For each Frame object given to encode with, a Thread is supplied. This thread
 * will take a BlockEncodeRequest, encode it, then give it back to the BlockThreadManager2
 * The thread will then take another BlockencodeRequest, and repeat. If no block
 * encode requests are available, it will block till available.
 *
 * 
 * The main thread of the BlockThreadManager2 will be waiting for additions to
 * the finished queue. If the item is not the next in line to be written to file,
 * it will be saved temporarily. If it is the next item to be written, it will
 * be given back to the FLACEncoder to be written to output, and any saved objects
 * will be searched to see if more can be written. The main thread will then wait
 * for the next frame again.
 *
 *
 *
 *
 * @author Preston Lacey
 */
public class BlockThreadManager2 implements Runnable{
    /* unassignedEncodeRequests: Requests waiting to be assigned a thread */
    LinkedBlockingQueue<BlockEncodeRequest> unassignedEncodeRequests = null;

    /* requests that have just finished encoding, and are ready for writing.
     * Since run() polls against this for newly finished requests, requests
     * recieved out of order will be temporarily stored in finishedRequestStore
     */
    LinkedBlockingQueue<BlockEncodeRequest> finishedEncodeRequests = null;

    /* pending requests in the order that we must pass them to the FLACEncoder.
     * The top object is moved to nextTarget until it is found in
     * finishedEncodeRequests and passed to the encoder. */
    LinkedBlockingQueue<BlockEncodeRequest> orderedEncodeRequests = null;

    /* frameThreadMap: Keep track of which thread is handling which frame */
    Map<FrameThread2, Thread> frameThreadMap = null;

    /* Thread which watches for finished encodes and alerts FLACEncoder of their
     * finished state. This thread will die when there is no data to encode, but
     * should remain valid and unchanged so long as blocks are encoding or
     * queued. It may therefore be used to monitor/interrupt, an encode process.
     */
    volatile Thread managerThread = null;

    /* FLACEncoder object we will send finished requests to */
    volatile FLACEncoder encoder = null;

    /* Must be false if we've been explicitly told to stop. Adding new requests
     * will reset this value to true */
    volatile boolean process = true;

    /* FrameThreads that are not currently assigned to a Thread */
    Vector<FrameThread2> inactiveFrameThreads = null;

    /* Lock ensures that only one FrameThread may get a new request at a time */
    private final Object getLock = new Object();

    /* Store finished requests that are not yet passed back to the FLACEncoder*/
    Vector<BlockEncodeRequest> finishedRequestStore = null;

    /* blockWhileQueueExceeds() waits on this lock for changes to queue size */
    private final Object outstandingCountLock = new Object();

    /* Next request which must be found and returned to the FLACEncoder. */
    volatile BlockEncodeRequest nextTarget = null;

    /* Number of requests added but not yet returned to FLACEncoder */
    volatile int outstandingCount = 0;
    
    /**
     * Constructor. Must supply a valid FLACEncoder object which will be alerted
     * when a block is finished encoding. 
     * @param encoder FLACEncoder to use in encoding process.
     */
    public BlockThreadManager2(FLACEncoder encoder) {
        this.encoder = encoder;
        unassignedEncodeRequests = new LinkedBlockingQueue<BlockEncodeRequest>();
        finishedEncodeRequests = new LinkedBlockingQueue<BlockEncodeRequest>();
        orderedEncodeRequests = new LinkedBlockingQueue<BlockEncodeRequest>();
        frameThreadMap = Collections.synchronizedMap(new HashMap<FrameThread2, Thread>());
        inactiveFrameThreads = new Vector<FrameThread2>();
        finishedRequestStore  = new Vector<BlockEncodeRequest>();
        managerThread = null;
    }

    /**
     * Get total number of BlockEncodeRequests added to this manager, but not
     * yet passed back to the FLACEncoder object.
     * @return number of BlockEncodeRequests remaining in this manager.
     */
    synchronized public int getTotalManagedCount() {
       return outstandingCount;
    }

    /**
     * This function is used to help control flow of BlockEncodeRequests into
     * this manager. It will block so long as their is at least as many 
     * unprocessed blocks waiting to be encoded as the value given.
     *
     * @param count Maximum number of outstanding requests that may exist before
     * this method may return.
     */
    public void blockWhileQueueExceeds(int count) {
         boolean loop = true;
         do {
            synchronized(outstandingCountLock) {
               if(outstandingCount > count) {
                  try {
                     outstandingCountLock.wait();
                  }catch(InterruptedException e) {
                     
                  }
               }
               else
                  loop = false;
            }
         }while(loop);
    }

    /**
     * Add a Frame to this manager, which it will use to encode a block. Each
     * Frame added allows one more thread to be used for encoding. At least one
     * Frame must be added for this manager to encode.
     * @param frame Frame to use for encoding.
     * @return boolean false if there was an error adding the frame, true
     * otherwise.
     */
    synchronized public boolean addFrameThread(Frame frame) {
        FrameThread2 ft = new FrameThread2(frame, this);
        inactiveFrameThreads.add(ft);
        boolean r = true;
        startFrameThreads();
        return r;
    }

    /**
     * Start any available FrameThread objects encoding, so long as there are
     * waiting BlockEncodeRequest objects.
     *
     */
    synchronized private void startFrameThreads() {
       if(!process)
          return;
       int requests = unassignedEncodeRequests.size();
       int frames = inactiveFrameThreads.size();
       frames = (requests <= frames) ? requests:frames;
       for(int i = 0; i < frames; i++) {
          FrameThread2 ft = inactiveFrameThreads.remove(0);
          Thread thread = new Thread(ft);
          frameThreadMap.put(ft, thread);
          thread.start();
       }
    }

    /**
     * Notify this manager that a FrameThread has ended it's run() method,
     * returning the FrameThread object to the manager for use in future Threads.
     *
     * @param ft FrameThread2 object which is ending.
     */
    synchronized public void notifyFrameThreadExit(FrameThread2 ft) {
       frameThreadMap.remove(ft);
       inactiveFrameThreads.add(ft);
       startFrameThreads();
    }

    /**
     * Get a BlockEncodeRequest object which is queued for encoding, pausing for
     * up to 0.5 seconds till one is available. It is expected that this object
     * will later(after encoding to a FLAC frame) be returned to this manager
     * through the returnFinishedRequest method. Failure to return the finished
     * object will cause the encoding process to hang.
     *
     * @return BlockEncodingRequest to encode, null if none available.
     */
    public BlockEncodeRequest getWaitingRequest() {
       BlockEncodeRequest result = null;
       synchronized(getLock) {
          boolean loop = true;
          while(loop) {
             try {
                result = unassignedEncodeRequests.poll(500, TimeUnit.MILLISECONDS);
                if(result != null) {
                  synchronized(outstandingCountLock) {
                     outstandingCountLock.notifyAll();
                  }
                  orderedEncodeRequests.add(result);
                }
                loop = false;
             }catch(InterruptedException e) {

             }
         }
      }
      return result;
    }

    /**
     * Notify this manager that it may stop as soon as all currently outstanding
     * requests are completed. Future calls to addRequest() will clear this stop
     * state.
     */
    synchronized public void stop() {
       process = false;
       BlockEncodeRequest temp = new BlockEncodeRequest();
       temp.setAll(null, -1, -1, -1, -1, null);
       int count = frameThreadMap.size();
       for(int i = 0; i < count; i++) {
          unassignedEncodeRequests.add(temp);
       }
    }

    /**
     * Used to return a finished BlockEncodeRequest from a FrameThread. This
     * must only be called with a finished request, which was originally added
     * to this manager through the addRequest() method.
     *
     * @param ber finished BlockEncodeRequest that needs passed back to the
     * FLACEncoder object.
     * 
     */
    synchronized public void returnFinishedRequest(BlockEncodeRequest ber) {
      try {
         finishedEncodeRequests.put(ber);
         restartManager();
      }catch(InterruptedException e) {
         returnFinishedRequest(ber);
      }
    }

    /**
     * Waits for the next BlockEncodeRequest that needs to be sent back to the
     * FLACEncoder for finalizing. If no request is finished, or currently
     * assigned to an encoding thread, will timeout after 0.5 seconds and end.
     */
    public void run () {
       //wait for finished item
       //send finished item to encoder
       //loop to top
       boolean loop = true;
       while(loop) {
         try {
            if(nextTarget == null)
               nextTarget = orderedEncodeRequests.poll(500,TimeUnit.MILLISECONDS);
            if(nextTarget == null) {
               loop = false;
            }
            else if(nextTarget.frameNumber < 0) {
               loop = false;
               nextTarget = null;
               orderedEncodeRequests.clear();
            }
            else if(finishedRequestStore.remove(nextTarget)) {
               encoder.blockFinished(nextTarget);
               nextTarget = null;
               synchronized(outstandingCountLock) {
                  outstandingCount--;
                  outstandingCountLock.notifyAll();
               }
            }
            else {
               BlockEncodeRequest ber = finishedEncodeRequests.poll(500, TimeUnit.MILLISECONDS);
               if(ber == null) {//nothing to process yet, let this thread end.
                  loop = false;
               }
               else if(nextTarget == ber) {
                  encoder.blockFinished(ber);
                  nextTarget = null;
                  synchronized(outstandingCountLock) {
                     outstandingCount--;
                     outstandingCountLock.notifyAll();
                  }
               }
               else {
                  finishedRequestStore.add(ber);
               }

            }
         }catch(InterruptedException e) {

         }
       }
       synchronized(this) {
            managerThread = null;
            restartManager();
       }
    }

    /**
     * Attempt to restart the managerThread if possible/needed. This is the only
     * function that should *ever* call managerThread.start(). We should call
     * this at any time a managerThread may be needed but not started. For
     * example, after returning a BlockEncodeRequest from an encoding thread.
     */
    synchronized private void restartManager() {
        if(managerThread == null && orderedEncodeRequests.size() > 0 ) {
            managerThread = new Thread(this);
            managerThread.start();
        }
    }

    /**
     * Add a BlockEncodeRequest to the manager. This will immediately attempt
     * to assign a request to an encoding thread(which may not occur if no
     * threads are currently available). Requests are passed back to the
     * currently set FLACEncoder object when finished and ready to be written
     * to output.
     * 
     * @param ber Block request to encode
     * @return boolean true if block added, false if an error occured.
     */
    synchronized public boolean addRequest(BlockEncodeRequest ber) {
        //add request to the manager(requests are automatically removed when complete)
        process = true;
        boolean r = true;
        try {
            unassignedEncodeRequests.put(ber);
            synchronized(outstandingCountLock) {
               outstandingCount++;
            }
            startFrameThreads();
        }catch(InterruptedException e) {
            r = false;
        }
        return r;
    }
}
