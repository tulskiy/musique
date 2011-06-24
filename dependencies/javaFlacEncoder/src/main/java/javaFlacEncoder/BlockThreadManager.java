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
import java.util.concurrent.locks.ReentrantLock;
import java.lang.Thread.UncaughtExceptionHandler;
/**
 * BlockThreadManager is used by FLACEncoder(when encoding with threads), to
 * dispatch BlockEncodeRequests to ThreadFrames which do the actual encode.
 * @author Preston Lacey
 */
public class BlockThreadManager implements Runnable,UncaughtExceptionHandler {
    /* queue: These are requests that are actively being processed */
    LinkedBlockingQueue<Thread> activeFrames = null;

    /* inactiveFrames: FrameThreads which may be used to encode(not busy yet) */
    LinkedBlockingQueue<FrameThread> inactiveFrames = null;

    /* unassignedEncodeRequests: Requests waiting to be assigned a thread */
    LinkedBlockingQueue<BlockEncodeRequest> unassignedEncodeRequests = null;

    /* frameThreadMap: Keep track of which thread is handling which frame */
    Map<Thread, FrameThread> frameThreadMap = null;

    /* Thread which watches for finished encodes and alerts FLACEncoder of their
     * finished state. This thread will die when there is no data to encode, but
     * should remain valid and unchanged so long as blocks are encoding or
     * queued. It may therefore be used to monitor/interrupt, an encode process.
     */
    volatile Thread managerThread = null;

    /* This lock must be locked whenever there is data remaining to encode;
     * nothing may exist in activeFrames or unassignedEncodeRequests if unlocked
     */
    ReentrantLock encodingLock = null;

    volatile FLACEncoder encoder = null;

    /**
     * Constructor. Must supply a valid FLACEncoder object which will be alerted
     * when a block is finished encoding. 
     * @param encoder FLACEncoder to use in encoding process.
     */
    public BlockThreadManager(FLACEncoder encoder) {
        this.encoder = encoder;
        activeFrames = new LinkedBlockingQueue<Thread>();
        inactiveFrames = new LinkedBlockingQueue<FrameThread>();
        unassignedEncodeRequests = new LinkedBlockingQueue<BlockEncodeRequest>();
        frameThreadMap = Collections.synchronizedMap(new HashMap<Thread, FrameThread>());
        managerThread = null;
        encodingLock = new ReentrantLock();
    }
    synchronized public int getTotalManagedCount() {
       return activeFrames.size()+inactiveFrames.size()+
               unassignedEncodeRequests.size();
    }
    /**
     * This function is used to help control flow of blockEncodeRequests into
     * this manager. It will block so long as their is at least as many 
     * unprocessed blocks waiting to be encoded as the value given.
     * @param count
     */
    public void blockWhileQueueExceeds(int count) {
         int waitingCount = unassignedEncodeRequests.size();
         while(waitingCount > count) {
            Thread temp = activeFrames.peek();
            try {
               temp.join();
               waitingCount = unassignedEncodeRequests.size();
            }
            catch(InterruptedException e) {

            }
         }
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
        FrameThread ft = new FrameThread(frame);
        boolean r = true;
        try {
            inactiveFrames.put(ft);
            if (managerThread == null)
                restartManager();
        }catch(InterruptedException e) {
            r = false;
        }
        return r;
    }

    /**
     * Assign a request to an inactive FrameThread, and start encoding.
     * @return boolean true if new thread was started, false otherwise. False 
     * will occur if no frames or requests are available.
     */
    synchronized private boolean assignRequest() {
        boolean result = false;

        BlockEncodeRequest request = unassignedEncodeRequests.peek();
        FrameThread frame = inactiveFrames.peek();
        if (request != null && frame != null) {
            request = unassignedEncodeRequests.poll();
            frame = inactiveFrames.poll();
            frame.prepareToEncodeFrame(request);
            Thread thread = new Thread(frame);
            thread.setUncaughtExceptionHandler(this);
            frameThreadMap.put(thread, frame);
            try {
                activeFrames.put(thread);
                thread.start();
                result = true;
            }catch(InterruptedException e) {
                System.err.println("assignRequest: Error! Interrupted");
            }
        }
        return result;
    }

    /**
     * Attempt to restart the managerThread if possible/needed. This is the only
     * function that should *ever* call managerThread.start().
     */
    synchronized private void restartManager() {
        //System.err.println("restartManager: inactiveFramesCount : "+inactiveFrames.size());
        if(managerThread == null && (unassignedEncodeRequests.size() > 0 ||
                activeFrames.size() > 0 ) ) {
            managerThread = new Thread(this);
            managerThread.start();
            //System.err.println("New Manager Thread #: "+managerThread.getName());
        }
        else {

        }

    }

    /**
     * Add a BlockEncodeRequest to the manager. This will immediately attempt
     * to assign a request to an encoding thread(which may not occur if no
     * threads are currently available)
     * @param ber Block request to encode
     * @return boolean true if block added, false if an error occured.
     */
    synchronized public boolean addRequest(BlockEncodeRequest ber) {
        //add request to the manager(requests are automatically removed when complete)
        //if manager is dead, hire new manager
        boolean r = true;
        try {
            unassignedEncodeRequests.put(ber);
            assignRequest();
            if(managerThread == null)
                restartManager();
        }catch(InterruptedException e) {
            r = false;
        }
        return r;
    }

    /**
     * Manager's run method. Repeatedly joins on the oldest encode thread, sends
     * results to the FLACEncoder object, starts a new thread(if possible), and
     * waits again. Dies when no more encode requests are active, or able to be
     * started. This should *only* be started by the restartManager() method.
     */
    public void run() {
        //GET_TOP: We'll join on the top thread in FIFO
        //      if FIFO is empty, destroy managerThread
        //if (frame is valid)
            //Write to output
            //dump request and thread
            //add framethread back to inactive queue.
            //attempt to start a new frame
            //jump to GET_TOP

        //encodingLock.lock();
        //System.err.println("Locking encodeLock");
        
        Thread topThread = null;
        synchronized(this) {
             topThread = activeFrames.poll();
             //FrameThread readyFrame = inactiveFrames.peek();
         //    while(assignRequest());
        }

        while(topThread != null) {
            try {
                //topThread.start();
                //System.err.println("Joining: "+topThread.getName());
                topThread.join();
                synchronized (this) {
                    FrameThread frame = frameThreadMap.get(topThread);
                    frameThreadMap.remove(topThread);
                    //TELL ENCODER FRAME IS DONE!
                    BlockEncodeRequest ber = frame.ber;
//                    finalizer.add(ber);
                    frame.ber = null;
                    inactiveFrames.put(frame);
                    assignRequest();
                    //encoder.blockFinished(frame.ber);
                    encoder.blockFinished(ber);

                    //frame.ber = null;
                    /*inactiveFrames.put(frame);
                    assignRequest();*/
                    topThread = activeFrames.poll();
                    if(topThread == null)
                       managerThread = null;
                }

            }
            catch(InterruptedException e) {
                //silently try again, pretend nothing happened on join.
                System.err.println("thread interrupted: ");
                //topThread = null;
            }
            finally {
            }
        }

        synchronized(this) {
            managerThread = null;
            //System.err.println("Unlocking encodeLock");
            //encodingLock.unlock();
            restartManager();
        }
    }


    /**
     * Get the current encoding manager thread. This thread should stay active
     * so long as there are available Frame's to encode with, and availabe
     * requests to encode.
     * @return Thread used by this manager
     */
    synchronized public Thread getEncodingThread() {
        return managerThread;
    }


    /**
     * WOn't likely keep this method
     * @param t
     * @param e
     */
    public void uncaughtException(Thread t, Throwable e) {
       System.err.println("Exception in thread: "+t.toString());
       e.printStackTrace();
    }
}
