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
import java.util.Vector;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.io.IOException;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.concurrent.LinkedBlockingQueue;

/**
 * This class defines a FLAC Encoder with a simple interface for enabling FLAC
 * encoding support in an application. This class is appropriate for use in the
 * case where you have raw pcm audio samples that you wish to encode. Currently,
 * fixed-blocksize only is implemented, and the "Maximum Block Size" set in the
 * StreamConfiguration object is used as the actual block size.
 * <br><br><br>
 * An encoding process is simple, and should follow these steps:<br>
 * <BLOCKQUOTE>
 *   1) Set StreamConfiguration to appropriate values. After a stream is opened,
 *       this must not be altered until the stream is closed.<br>
 *   2) Set FLACOutputStream, object to write results to.<br>
 *   3) Open FLAC Stream<br>
 *   4) Set EncodingConfiguration(if defaults are insufficient).<br>
 *   5) Add samples to encoder<br>
 *   6) Encode Samples<br>
 *   7) Close stream<br>
 *   (note: steps 4,5, and 6 may be done repeatedly, in any order. However, see
 *   related method documentation for info on concurrent use)
 * </BLOCKQUOTE><br><br>
 *
 * @author Preston Lacey
 */
public class FLACEncoder {

    /* For debugging, higher level equals more output */
    int DEBUG_LEV = 0;

    /**
     * Maximum Threads to use for encoding frames(more threads than this will
     * exist, these threads are reserved for encoding of frames only).
     */
    public int MAX_THREADED_FRAMES = 2;

    /* encodingConfig: Must never stay null(default supplied by constructor) */
    EncodingConfiguration encodingConfig = null;

    /* streamConfig: Must never stay null(default supplied by constructor) */
    StreamConfiguration streamConfig = null;

    /* Set true if frames are actively being encoded(can't change settings
     * while this is true) */
    volatile Boolean isEncoding = false;

    /* synchronize on this object when encoding or changing configurations */
    private final Object configWriteLock = new Object();

    /* Store for blocks which are ready to encode. Always insert end, pop head*/
    private Vector<int[]> blockQueue = null;

    /* Stores samples for a block which is not yet full(not ready for queue) */
    private int[] unfinishedBlock = null;

    /* Stores count of inter-frame samples in unfinishedBlock */
    private int unfinishedBlockUsed = 0;

    /* Object to write results to. Must be set before opening stream */
    private FLACOutputStream out = null;

    /* contains FLAC_id used in the flac stream header to signify FLAC format */
    EncodedElement FLAC_id = FLACStreamIdentifier.getIdentifier();

    /* Frame object used to encode when not using threads */
    Frame frame = null;

    
    /* md object used to calculate MD5 hash */
    MessageDigest md = null;

    /* threadManager used with threaded encoding  */
    BlockThreadManager2 threadManager = null;

    /* threagedFrames keeps track of frames given to threadManager. We must still
     * update the configurations of them as needed. If we ever create new
     * frames(e.g, when changing stream configuration), we must create a new
     * threadManager as well.
     */
    Frame[] threadedFrames = null;

    /* minimum frame size seen so far. Used in the stream header */
    int minFrameSize = 0x7FFFFFFF;

    /* maximum frame size seen so far. Used in stream header */
    int maxFrameSize = 0;

    /* minimum block size used so far. Used in stream header */
    int minBlockSize = 0x7FFFFFFF;

    /* maximum block size used so far. Used in stream header */
    int maxBlockSize = 0;

    /* total number of samples encoded to output. Used in stream header */
    volatile long samplesInStream;

    /* next frame number to use */
    long nextFrameNumber = 0;

    /* position of header in output stream location(needed so we can update
     * the header info(md5, minBlockSize, etc), once encoding is done
     */
    long streamHeaderPos = 0;

    /* should be set when any error has occured that invalidates results.
     * This should not be relied on currently, practice not followed well.
     */ 
    boolean error = false;

    /* store used encodeRequests so we don't have to reallocate space for them*/
    LinkedBlockingQueue<BlockEncodeRequest> usedBlockEncodeRequests = null;
    LinkedBlockingQueue<int[]> usedIntArrays = null;
    /**
     * Constructor which creates a new encoder object with the default settings.
     * The StreamConfiguration should be reset to match the audio used and an
     * output stream set, but the default EncodingConfiguration should be ok for
     * most purposes. When using threaded encoding, the default number of
     * threads used is equal to FLACEncoder.MAX_THREADED_FRAMES.
     */
    public FLACEncoder() {
        usedBlockEncodeRequests = new LinkedBlockingQueue<BlockEncodeRequest>();
        usedIntArrays = new LinkedBlockingQueue<int[]>();
        
        blockQueue = new Vector<int[]>();
        StreamConfiguration defaultStreamConfig = new StreamConfiguration();
        encodingConfig = new EncodingConfiguration();
        frame = new Frame(defaultStreamConfig);
        frame.registerConfiguration(encodingConfig);
        //frameThread = new FrameThread(frame);
        threadManager = new BlockThreadManager2(this);
        threadedFrames = new Frame[MAX_THREADED_FRAMES];
        for(int i = 0; i < MAX_THREADED_FRAMES; i++) {
            threadedFrames[i]  = new Frame(defaultStreamConfig);
            threadManager.addFrameThread(threadedFrames[i]);
        }
        try {
            md = MessageDigest.getInstance("md5");
            reset();
        }catch(NoSuchAlgorithmException e) {
            System.err.println("Critical Error: No md5 algorithm exists. " +
                    "This encoder can not function.");
        }
    }

    /**
     * Set the encoding configuration to that specified. The given encoding
     * configuration is not stored by this object, but instead copied. This
     * is to prevent the alteration of the config during an encode process.
     * 
     * @param ec EncodingConfiguration to use.
     * @return true if the configuration was altered; false if the configuration
     *         cannot be altered(such as if another thread is currently encoding).
     */
    public boolean setEncodingConfiguration(EncodingConfiguration ec) {
        boolean changed = false;
        if(!isEncoding && ec != null) {
            synchronized(configWriteLock) {
                encodingConfig = ec;
                frame.registerConfiguration(ec);
                for(int i = 0; i < MAX_THREADED_FRAMES; i++)
                    threadedFrames[i].registerConfiguration(ec);
            }
            changed = true;
        }
        return changed;
    }

    /**
     * Set the stream configuration to that specified. The given stream
     * configuration is not stored by this object, but instead copied. This
     * is to prevent the alteration of the config during an encode process.
     * This method must not be called in the middle of a stream, stream contents
     * may become invalid. A call to setStreamConfiguration() should
     * be followed next by setting the output stream if not yet done, and then
     * calling openFLACStream();
     *
     * @param sc StreamConfiguration to use.
     * @return true if the configuration was altered; false if the configuration
     * cannot be altered(such as if another thread is currently encoding).
     */
    public boolean setStreamConfiguration(StreamConfiguration sc) {
        boolean changed = false;
        if(!isEncoding && sc != null) {
            synchronized(configWriteLock) {
                streamConfig = sc;
                frame = new Frame(sc);
                threadManager = new BlockThreadManager2(this);
                threadedFrames = new Frame[MAX_THREADED_FRAMES];
                for(int i = 0; i < MAX_THREADED_FRAMES; i++) {
                    threadedFrames[i]  = new Frame(sc);
                    threadManager.addFrameThread(threadedFrames[i]);
                }
                this.setEncodingConfiguration(this.encodingConfig);
            }
            changed = true;
        }
        return changed;
    }

    /**
     * Reset the values to their initial state, in preparation of starting a
     * new stream.
     */
    private void reset() {
        //reset stream
        md.reset();
        minFrameSize = minFrameSize = 0x7FFFFFFF;
        maxFrameSize = 0;
        minBlockSize = 0x7FFFFFFF;
        maxBlockSize = 0;
        samplesInStream = 0;
        streamHeaderPos = 0;
        unfinishedBlock = null;
        unfinishedBlockUsed = 0;
        blockQueue.clear();
        nextFrameNumber = 0;
    
    }

    /**
     * Close the current FLAC stream. Updates the stream header information.
     * If called on an open stream, operation is undefined. Do not do this.
     */
    private void closeFLACStream() {
        //reset position in output stream to beginning.
        //re-write the updated stream info.

        if(DEBUG_LEV > 0)
            System.err.println("FLACEncoder::closeFLACStream : Begin");
        streamConfig.setMaxBlockSize(maxBlockSize);
        streamConfig.setMinBlockSize(minBlockSize);
        byte[] md5 = md.digest();
        EncodedElement streamInfo =  MetadataBlockStreamInfo.getStreamInfo(
                streamConfig, minFrameSize, maxFrameSize, samplesInStream,
                md5);
        out.seek(streamHeaderPos);
        try {
            this.writeDataToOutput(streamInfo);
        }catch(IOException e) {
            System.err.println("FLACEncoder::closeFLACStream():  ERROR WRiting to output");
        }
    }

    /**
     * Begin a new FLAC stream. Prior to calling this, you must have already
     * set the StreamConfiguration and the output stream, both of which must not
     * change until encoding is finished and the stream is closed.
     * @throws IOException if there is an error writing the headers to output.
     */
    public void openFLACStream() throws IOException {
        //reset all data.
        reset();
        //write FLAC stream identifier
        out.write(FLAC_id.getData(), 0, FLAC_id.getUsableBits()/8);
        //write stream headers. These must be updated at close of stream
        byte[] md5Hash = {0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0};//blank hash. Don't know it yet.
        EncodedElement streamInfo =  MetadataBlockStreamInfo.getStreamInfo(
                streamConfig, minFrameSize, maxFrameSize, samplesInStream,
                md5Hash);
        //mark stream info location(so we can return to it and re-write headers,
        //  assuming stream is seekable. Then write header.        
        int size = streamInfo.getUsableBits()/8;
        EncodedElement metadataBlockHeader =
                MetadataBlockHeader.getMetadataBlockHeader(true,
                MetadataBlockHeader.MetadataBlockType.STREAMINFO, size);
        this.writeDataToOutput(metadataBlockHeader);
        streamHeaderPos = out.getPos();
        out.write(streamInfo.getData(), 0, size);
    }

    /**
     * Add samples to the encoder, so they may then be encoded. This method uses
     * breaks the samples into blocks, which will then be made available to
     * encode.
     *
     * @param samples Array holding the samples to encode. For all multi-channel
     * audio, the samples must be interleaved in this array. For example, with
     * stereo: sample 0 will belong to the first channel, 1 the second, 2 the
     * first, 3 the second, etc. Samples are interpreted according to the
     * current configuration(for things such as channel and bits-per-sample).
     *
     * @param count Number of interchannel samples to add. For example, with
     * stero: if this is 4000, then "samples" must contain 4000 left samples and
     * 4000 right samples, interleaved in the array.
     *
     * @return true if samples were added, false otherwise. A value of false may
     * result if "count" is set to a size that is too large to be valid with the
     * given array and current configuration.
     */
    public boolean addSamples(int[] samples, int count) {
        boolean added = false;
        //get number of channels
        int channels = streamConfig.getChannelCount();
        int maxFrames = samples.length/channels;//input wav frames, not flac
        int validSamples = count*channels;
        if(DEBUG_LEV > 0) {
            System.err.println("addSamples(...): ");
            System.err.println("maxFrames: "+maxFrames);
            System.err.println("validSamples: "+validSamples);
            if(DEBUG_LEV > 10)
               System.err.println("count:"+count+":channels:"+channels);
        }

        if(count <= maxFrames) {//sample count is ok
            added = true;
            //break sample input into appropriately sized blocks
            int samplesUsed = 0;//number of input samples used
            if(unfinishedBlock != null) {
                //finish off last block first.
                if(DEBUG_LEV > 10) {
                    System.err.println("addSamples(...): filling unfinishedBlock");
                }
                
                int blockSize = streamConfig.getMaxBlockSize();
                int[] block = unfinishedBlock;
                int unfinishedBlockRemaining = blockSize*channels-unfinishedBlockUsed;
                if(unfinishedBlockRemaining <=0) {
                    System.err.println("MAJOR ERROR HERE. Unfinsihed block remaining invalid: "+
                            unfinishedBlockRemaining);
                    System.exit(-1);
                }
                
                int nextSampleStop = samplesUsed+unfinishedBlockRemaining;
                if(nextSampleStop > validSamples) {
                    nextSampleStop = validSamples;
                }
                int i;
                for(i = 0; i < unfinishedBlockRemaining && i < nextSampleStop; i++) {
                    block[unfinishedBlockUsed+i] = samples[samplesUsed+i];
                }
                unfinishedBlockUsed += i;
                samplesUsed = nextSampleStop;
                if(unfinishedBlockUsed == blockSize*channels) {
                    //System.err.println("Adding block: "+blocksAdded++ +":"+blockQueue.size());
                    blockQueue.add(block);
                    unfinishedBlockUsed = 0;
                    unfinishedBlock = null;
                }
                else if(unfinishedBlockUsed > blockSize*channels) {
                    System.err.println("Error: FLACEncoder.addSamples(...) " +
                            "unfinished block = "+unfinishedBlockUsed);
                    System.exit(-1);
                }
            }
            while(samplesUsed < validSamples) {
                if(DEBUG_LEV > 20)
                    System.err.println("addSamples(...): creating new block");
                //copy values to approrpiate locations
                //add each finished array to the queue
                /*<implement_for_variable_blocksize>
                 * blockSize = this.getNextBlockSize(samples, validSamples);*/
                int blockSize = streamConfig.getMaxBlockSize();
                //int[] block = new int[blockSize*channels];
                int[] block = getBlock(blockSize*channels);
                int nextSampleStop = samplesUsed+blockSize*channels;
                if(nextSampleStop > validSamples) {
                    //We don't have enough samples to make a full block.
                    if(DEBUG_LEV > 20)
                        System.err.println("addSamples(...): setting partial Block");
                    //fill unfinishedBlock
                    nextSampleStop = validSamples;
                    unfinishedBlock = block;
                    unfinishedBlockUsed = validSamples-samplesUsed;
                }
                else {                    
                    blockQueue.add(block);
                    //System.err.println("Adding block: "+blocksAdded++ +":"+blockQueue.size());
                }
                //System.err.println("samplesUsed: " + samplesUsed);
                //System.err.println("Nextsamplestop: " + nextSampleStop);
                for(int i = 0; i < nextSampleStop-samplesUsed; i++)
                        block[i] = samples[samplesUsed+i];
                samplesUsed = nextSampleStop;
            }
        }
        else {
            System.err.println("Error: FLACEncoder.addSamples "+
                    "given count out of bounds");
        }

        if(DEBUG_LEV > 20) {
            System.err.println("Blocks stored: " +blockQueue.size());
            System.err.println("Samples in partial block: " + unfinishedBlockUsed);
        }
        return added;
    }

    /**
     * This function is for development purposes only. It likely serves no
     * further point and perhaps is worthy of being removed.
     * @param block
     * @param count
     * @param iter
     */
    private void outputBlockToFile(int[] block, int count, int iter) {
        //DEBUGGING, for development only!
        try {
            FileOutputStream fout = new FileOutputStream("samples.txt");
            //OutputStreamWriter tOut = new OutputStreamWriter(fout);
            PrintWriter pOut = new PrintWriter(fout);
            for(int i  = 0; i < count; i++) {
                String temp = Integer.toString(i)+":";
                temp = temp + Integer.toString(block[i*iter]);
                System.err.print(temp);
                pOut.println(temp);
            }
            pOut.flush();
            pOut.close();
            fout.close();
            System.exit(0);
            System.err.println("sample file written:");
        }
        catch(FileNotFoundException e) {
            System.err.println("Error creating file");
        }catch(IOException e) {
            System.err.println("Error handling file");
        }

    }

    /**
     * Notify the encoder that a BlockEncodeRequest has finished, and is now
     * ready to be written to file. The encoder expects that these requests come
     * back in the same order the encoder sent them out. This is intended to
     * be used in threading mode only at the moment(sending them to a
     * BlockThreadManager object)
     *
     * @param ber BlockEncodeRequest that is ready to write to file.
     */
    public void blockFinished(BlockEncodeRequest ber) {
        synchronized (ber) {
            try {
                writeDataToOutput(ber.result.getNext());
            }catch(IOException e) {
                System.err.println("blockFinished: Error writing to output");
                e.printStackTrace();
                error = true;
            }

            //update encodedCount and count, and blocks, MD5
            if(ber.count != ber.encodedSamples) {
                System.err.println("Error encoding frame number: "+
                        ber.frameNumber+", FLAC stream potentially invalid");
            }
            samplesInStream += ber.encodedSamples;
            if(ber.encodedSamples > maxBlockSize)
                maxBlockSize = ber.encodedSamples;
            if(ber.encodedSamples < minBlockSize)
                minBlockSize = ber.encodedSamples;
            int frameSize = ber.result.getTotalBits()%8;
            if(frameSize > maxFrameSize) maxFrameSize = frameSize;
            if(frameSize < minFrameSize) minFrameSize = frameSize;
            addSamplesToMD5(ber.samples, ber.encodedSamples, ber.skip+1,
                        streamConfig.getBitsPerSample());
            usedIntArrays.add(ber.samples);
            ber.samples = null;
            ber.result = null;
            usedBlockEncodeRequests.add(ber);

        }
    }

    /**
     * Attempt to Encode a certain number of samples(threaded version).
     * Encodes as close to count as possible. Uses multiple threads to speed up
     * encoding.
     *
     * @param count number of samples to attempt to encode. Actual number
     * encoded may be greater or less if count does not end on a block boundary.
     * 
     * @param end true to finalize stream after encode, false otherwise. If set
     * to true, no more encoding must be attempted until a new stream is began.
     *
     * @return number of samples encoded. This may be greater or less than
     * requested count if count does not end on a block boundary. This is NOT an
     * error condition.
     * 
     * @throws IOException if there was an error writing the results to file.
     */
    public int t_encodeSamples(int count, boolean end) throws IOException {
        int encodedCount = 0;

        //pull blocks from the queue, check size, and encode if size is smaller
        //than remaining count.
        int blocksLeft = blockQueue.size();
        int channels = streamConfig.getChannelCount();
        while(count > 0 && blocksLeft > 0) {
            if(DEBUG_LEV > 20) {
                System.err.println("while: count:blocksLeft  : "+
                        count+":"+blocksLeft);
            }
            int[] block = blockQueue.elementAt(0);
            if(block.length <= count*channels) {
                //encode
                int encodedSamples = block.length/channels;//interchannel samples
                EncodedElement result = new EncodedElement();
                //BlockEncodeRequest ber = new BlockEncodeRequest();
                BlockEncodeRequest ber = usedBlockEncodeRequests.poll();
                if(ber == null) ber = new BlockEncodeRequest();
                ber.setAll(block, encodedSamples, 0,channels-1, nextFrameNumber++,
                        result);
                threadManager.addRequest(ber);
                blockQueue.remove(0);
                blocksLeft--;
                count -= encodedSamples;
                encodedCount += encodedSamples;
            }
            else {
                //can't encode a full block.
               System.err.println("Error with block in queue?");
                break;
            }
        }
        //block while requests remain!!!!
        threadManager.blockWhileQueueExceeds(5);
        if(end) {
           threadManager.stop();
           threadManager.blockWhileQueueExceeds(0);
        }
        //handle "end" setting
        if(end && count >= 0 && this.samplesAvailableToEncode() >= count) {
            //handle remaining count
            if(count > 0 && unfinishedBlockUsed >= count) {
                int[] block = null;
                if(blockQueue.size() > 0) {
                   block = blockQueue.elementAt(0);
                }
                else
                   block = unfinishedBlock;
                int encodedSamples = count;//interchannel samples
                EncodedElement result = new EncodedElement();
                int encoded = frame.encodeSamples(block, encodedSamples, 0,
                        channels-1, result, nextFrameNumber);
                if(encoded != encodedSamples) {
                    //ERROR! Return immediately. Do not add results to output.
                    System.err.println("FLACEncoder::encodeSamples : (end)Error in encoding");
                    count = -1;
                }
                else {
                    writeDataToOutput(result.getNext());
                    //update encodedCount and count
                    encodedCount += encodedSamples;
                    count -= encodedSamples;
                    //addSamplesToMD5(block, encodedSamples, 0,channels);
                    addSamplesToMD5(block, encodedSamples, channels,
                            streamConfig.getBitsPerSample());
                    samplesInStream += encodedSamples;
                    nextFrameNumber++;
                    if(encodedSamples > maxBlockSize) maxBlockSize = encodedSamples;

                    if(encodedSamples < minBlockSize) minBlockSize = encodedSamples;

                    int frameSize = result.getTotalBits()%8;
                    if(frameSize > maxFrameSize) maxFrameSize = frameSize;
                    if(frameSize < minFrameSize) minFrameSize = frameSize;

                    //System.err.println("Count: " + count);
                }
            }
            //close stream if all requested were written.
            if(count == 0) {
                closeFLACStream();
            }
        }
        else if (end == true) {
                System.err.println("End set but not done. Error likely. "+
                        "This can happen if number of samples requested to " +
                        "encode exeeds available samples");
        }

        return encodedCount;
    }

    /**
     * Attempt to Encode a certain number of samples. Encodes as close to count
     * as possible.
     *
     * @param count number of samples to attempt to encode. Actual number
     * encoded may be greater or less if count does not end on a block boundary.
     *
     * @param end true to finalize stream after encode, false otherwise. If set
     * to true, no more encoding must be attempted until a new stream is began.
     * @return number of samples encoded. This may be greater or less than
     * requested count if count does not end on a block boundary. This is NOT an
     * error condition.
     * @throws IOException if there was an error writing the results to file.
     */
    public int encodeSamples(int count, boolean end) throws IOException {
       //  System.err.println("starting encoding :");
        int encodedCount = 0;
        
        //pull blocks from the queue, check size, and encode if size is smaller
        //than remaining count.
        int blocksLeft = blockQueue.size();
        int channels = streamConfig.getChannelCount();
        while(count > 0 && blocksLeft > 0) {
            if(DEBUG_LEV > 20) {
                System.err.println("while: count:blocksLeft  : "+
                        count+":"+blocksLeft);
            }
            int[] block = blockQueue.elementAt(0);
            if(block.length <= count*channels) {
                //encode
                int encodedSamples = block.length/channels;//interchannel samples
                //count -= encodedSamples;
                EncodedElement result = new EncodedElement();
                int encoded = frame.encodeSamples(block, encodedSamples, 0,
                        channels-1, result, nextFrameNumber);
                if(encoded != encodedSamples) {
                    //ERROR! Return immediately. Do not add results to output.
                    System.err.println("FLACEncoder::encodeSamples : Error in encoding");
                    count = -1;
                    break;
                }
                //write encoded results to output.
                //System.err.println("writing frame: "+nextFrameNumber);
                writeDataToOutput(result.getNext());
                //update encodedCount and count, and blocks, MD5
                blockQueue.remove(0);
                blocksLeft--;
                encodedCount += encodedSamples;
                //System.err.println("Count pre: " + count);
                count -= encodedSamples;
                samplesInStream += encodedSamples;
                nextFrameNumber++;
                if(encodedSamples > maxBlockSize)
                    maxBlockSize = encodedSamples;
                if(encodedSamples < minBlockSize)
                    minBlockSize = encodedSamples;
                int frameSize = result.getTotalBits()%8;
                if(frameSize > maxFrameSize) maxFrameSize = frameSize;
                if(frameSize < minFrameSize) minFrameSize = frameSize;
                //addSamplesToMD5(block, encodedSamples, 0,channels);
                addSamplesToMD5(block, encodedSamples, channels,
                            streamConfig.getBitsPerSample());
                usedIntArrays.add(block);
                //System.err.println("Count post: " + count);
            }
            else {
               if(blockQueue.size() > 0) {
                  System.err.println("Can't encode full but blocksize != 0");
                  System.err.println("Blockqueue size: "+blockQueue.size());
                  System.err.println("Block size: "+block.length);
                  System.err.println("Count: "+count);
               }
                //can't encode a full block.
                break;
            }
        }
        //handle "end" setting
        if(end)
           threadManager.stop();
        if(end && count >= 0 && this.samplesAvailableToEncode() >= count) {
            //handle remaining count
            if(count > 0 && unfinishedBlockUsed >= count) {
                int[] block = null;
                if(blockQueue.size() > 0) {
                   block = blockQueue.elementAt(0);
                }
                else
                  block = unfinishedBlock;
                int encodedSamples = count;//interchannel samples                
                EncodedElement result = new EncodedElement();
                int encoded = frame.encodeSamples(block, encodedSamples, 0,
                        channels-1, result, nextFrameNumber);
                if(encoded != encodedSamples) {
                    //ERROR! Return immediately. Do not add results to output.
                    System.err.println("FLACEncoder::encodeSamples : (end)Error in encoding");
                    count = -1;
                }
                else {
                    writeDataToOutput(result.getNext());
                    //update encodedCount and count
                    encodedCount += encodedSamples;
                    count -= encodedSamples;
                    //addSamplesToMD5(block, encodedSamples, 0,channels);
                    addSamplesToMD5(block, encodedSamples, channels,
                            streamConfig.getBitsPerSample());
                    samplesInStream += encodedSamples;
                    nextFrameNumber++;
                    if(encodedSamples > maxBlockSize) maxBlockSize = encodedSamples;
                        
                    if(encodedSamples < minBlockSize) minBlockSize = encodedSamples;
                        
                    int frameSize = result.getTotalBits()%8;
                    if(frameSize > maxFrameSize) maxFrameSize = frameSize;
                    if(frameSize < minFrameSize) minFrameSize = frameSize;

                    System.err.println("Count: " + count);
                }
            }
            //close stream if all requested were written.
            if(count == 0) {
                closeFLACStream();
            }
        }
        else if (end == true) {
                System.err.println("End set but not done. Error likely." );
        }
        return encodedCount;
    }

    /**
     * Add samples to the MD5 hash.
     * CURRENTLY ONLY MAY WORK FOR: sample sizes which are divisible by 8. Need
     * to create some audio to test with.
     * @param samples
     * @param count
     * @param channels
     */
    private void addSamplesToMD5(int[] samples, int count, int channels, 
            int sampleSize) {

        int bytesPerSample = sampleSize/8;
        if(sampleSize%8 != 0)
            bytesPerSample++;
        byte[] dataMD5 = new byte[count*bytesPerSample*channels];
        for(int i = 0; i < count*channels; i++) {
            for(int x = 0; x < bytesPerSample; x++) {
                dataMD5[i*bytesPerSample+x] = (byte)(samples[i] >> x*8);
            }
        }
        md.update(dataMD5, 0, count*bytesPerSample*channels);
    }

    /**
     * Write the data stored in an EncodedElement to the output stream.
     * All data will be written along byte boundaries, but the elements in the
     * given list need not end on byte boundaries. If the data of an element
     * does not end on a byte boundary, then the space remaining in that last
     * byte will be used as an offset, and merged(using an "OR"), with the first
     * byte of the following element.
     * 
     * @param data
     * @return
     * @throws IOException
     */
    private int writeDataToOutput(EncodedElement data) throws IOException {
        int writtenBytes = 0;
        int offset = 0;
        EncodedElement current = data;
        int currentByte = 0;
        byte unfullByte = 0;
        byte[] eleData = null;
        int usableBits = 0;
        int lastByte = 0;
        while(current != null) {
            //System.err.println("current != null" );
            eleData = current.getData();
            usableBits = current.getUsableBits();
            currentByte = 0;
            //if offset is not zero, merge first byte with existing byte
            if(offset != 0) {
                unfullByte = (byte)(unfullByte | eleData[currentByte++]);
                out.write(unfullByte);
            }
            //write all full bytes of element.
            lastByte = usableBits/8;
            //System.err.println("eleData.length:currentByte:length   :   "+
            //        eleData.length+":"+currentByte+":"+(lastByte-currentByte));
            if(lastByte > 0)
                out.write(eleData, currentByte, lastByte-currentByte);
            //save non-full byte(if present), and set "offset" for next element.
            //offset = usableBits - lastByte*8;
            offset = usableBits %8;
            if(offset != 0) {
                unfullByte = eleData[lastByte];
            }
            //update current.
            current = current.getNext();
        }
        //if non-full byte remains. write.
        if(offset != 0) {
            out.write(eleData, lastByte, 1);
        }
        return writtenBytes;
    }

    /**
     * Get number of samples which are ready to encode. More samples may exist
     * in the encoder as a partial block. Use samplesAvailableToEncode() if you
     * wish to include those as well.
     * @return number of samples in full blocks, ready to encode.
     */
    public int fullBlockSamplesAvailableToEncode() {
        int available = 0;
        int channels = streamConfig.getChannelCount();
        for(int[] block: blockQueue) {
            available += block.length/channels;
        }
        return available;
    }

    /**
     * Get number of samples that are available to encode. This includes samples
     * which are in a partial block(and so would only be written if "end" was
     * set true in encodeSamples(int count,boolean end);
     * @return number of samples availble to encode.
     */
    public int samplesAvailableToEncode() {
        int available = 0;
        //sum all in blockQueue
        int channels = streamConfig.getChannelCount();
        for(int[] block : blockQueue) {
            available += block.length/channels;
        }
        //add remaining in unfinishedBlock.
        available += unfinishedBlockUsed;
        return available;
    }

    /**
     * Set the output stream to use. This must not be called while an encode
     * process is active.
     * @param fos output stream to use. This must not be null.
     */
    public void setOutputStream(FLACOutputStream fos) {
        out = fos;
    }

    public int[] getBlock(int size) {
       int[] result = usedIntArrays.poll();
       if(result == null) {
          result = new int[size];
          //System.err.println("Created new int array from null");
       }
       else if(result.length < size) {
          usedIntArrays.offer(result);
          result = new int[size];
          //System.err.println("created new int array from bad size");
       }
       return result;
    }
    //int[] block = new int[blockSize*channels];
}
