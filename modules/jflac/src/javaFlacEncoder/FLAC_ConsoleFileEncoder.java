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

import java.io.IOException;
import java.io.File;
/**
 * FLAC_ConsoleFileEncoder is the command line interface to this FLAC encoder.
 * It accepts several flags and parameters to alter the encoding configuration.
 *
 * @author Preston Lacey
 * 
 */
public class FLAC_ConsoleFileEncoder {
    /** flag to display help */
    public static String HELP = "-h";
    /** flag to specify minimum block size. An integer must follow */
    public static String MIN_BLOCK = "-bmin";
    /** flag to specify maximum block size. An integer must follow */
    public static String MAX_BLOCK = "-bmax";
    /** flag to specify minimum LPC order to use. An integer must follow */
    public static String MIN_LPC = "-lpcmin";
    /** flag to specify maximum LPC order to use. An integer must follow */
    public static String MAX_LPC = "-lpcmax";
    /** flag to specify whether to use threads. Must follow by a zero(no threads)
     * or a one(use threads)
     */
    public static String THREADS = "-Threads";
    /** flag to specify subframe type to use. Must be followed by a valid type
     * as given by the fields TYPE_*.
     */
    public static String SUBFRAME_TYPE = "-sf";
    /** LPC Subframe type identifier string */
    public static String TYPE_LPC = "lpc";
    /** Fixed Subframe type identifier string */
    public static String TYPE_FIXED = "fixed";
    /** Exhaustive Subframe type identifier string(tells encoder to try each
     * subframe type and choose the smallest one */
    public static String TYPE_EXHAUSTIVE = "exhaustive";
    /** Verbatime subframe type identifier string */
    public static String TYPE_VERBATIM = "verbatim";
    /** Error message given for incorrect minimum block size specified */
    public static final String MIN_BLOCK_ERROR = "Error with minimum block "+
        "size: integer value between "+StreamConfiguration.MIN_BLOCK_SIZE+" & "+
        StreamConfiguration.MAX_BLOCK_SIZE +" must follow \""+MIN_BLOCK+"\"";
    /** Error message given for incorrect maximum block size specified */
    public static final String MAX_BLOCK_ERROR = "Error with maximum block "+
        "size: integer value between "+StreamConfiguration.MIN_BLOCK_SIZE+" & "+
        StreamConfiguration.MAX_BLOCK_SIZE +" must follow \""+MIN_BLOCK+"\"";
    /** Error message given for incorrect minimum LPC order given */
    public static final String MIN_LPC_ERROR = "MIN_LPC ERROR";
    /** Error message given for incorrect maximum LPC order given */
    public static final String MAX_LPC_ERROR = "MAX_LPC Error";
    /** Error message given for incorrect specification of Thread usage */
    public static final String THREADS_ERROR = "Error setting threads: \""+
            THREADS+"\" must be followed by an integer(max number of EXTRA" +
            " threads to use)";

    private File inputFile = null;
    private File outputFile = null;
    private boolean canEncode = false;
    private boolean useThreads = true;
    int threadCount = 2;
    EncodingConfiguration encodingConfig;
    StreamConfiguration streamConfig;
    private boolean attemptEncode = true;
    
    /**
     * Run ConsoleFileEncoder with given arguments.
     *
     * @param args Arguments for encoding.
     */
    public static void main(String[] args) {
        //long startTime = System.currentTimeMillis();
        //Encoder_Test test = new Encoder_Test();
        FLAC_ConsoleFileEncoder cfe = new FLAC_ConsoleFileEncoder(args);
        //System.err.println("Total Time in LPC: "+Subframe_LPC.totalTime);
        //long endTime = System.currentTimeMillis();
        //System.out.println("Seconds taken : "+(endTime-startTime));
    }

    /**
     * Constructor. Uses given arguments to immediately attempts to encode the
     * file given. Uses stdout and stderr for notices of progress and error.
     *
     * @param args Encoding arguments to use.
     */
    public FLAC_ConsoleFileEncoder(String []args) {
        outputFile = null;
        inputFile = null;
        //create configurations/handle input arguments and set configurations
        boolean valid = handleArguments(args);
        if(attemptEncode) {
           printSmallCopyright();
        }
        //create encoder with configurations
        FLAC_FileEncoder enc = new FLAC_FileEncoder();
        enc.setStreamConfig(streamConfig);
        enc.setEncodingConfig(encodingConfig);
        enc.useThreads(useThreads);
        //encode file with encoder
        FLAC_FileEncoder.Status st = FLAC_FileEncoder.Status.UNKNOWN;
        if(inputFile != null && outputFile != null && attemptEncode)
             st = enc.encode(inputFile, outputFile);
        else if(inputFile == null && attemptEncode) {
            System.err.println("error: inputFile not given or an error occured");
            attemptEncode = false;
        }
        else if(attemptEncode) {
            System.err.println(inputFile.getName());
            System.err.println("error: outputFile not given or an error occured");
            attemptEncode = false;
        }
        if(attemptEncode)
          System.err.println("Status: " + st.toString());
        //fileEncoderTest();
    }
    private void printSmallCopyright() {
       String message = "javaFlacEncoder Copyright (C) 2010  Preston Lacey\n"+
       "javaFlacEncoder comes with ABSOLUTELY NO WARRANTY. This is free software,\n"+
       "you may redistribute it under the terms of the Lesser GPL. For more details\n"+
       "or usage help, use option \"-h\"\n\n";
       System.out.println(message);
    }
    private void printCopyright() {
      String copyrightNotice =
      "Copyright (C) 2010  Preston Lacey\n"+
      "All Rights Reserved.\n\n"+

      "This library is free software; you can redistribute it and/or\n"+
      "modify it under the terms of the GNU Lesser General Public\n"+
      "License as published by the Free Software Foundation; either\n"+
      "version 2.1 of the License, or (at your option) any later version.\n\n"+

      "This library is distributed in the hope that it will be useful,\n"+
      "but WITHOUT ANY WARRANTY; without even the implied warranty of\n"+
      "MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU\n"+
      "Lesser General Public License for more details.\n\n"+

      "You should have received a copy of the GNU Lesser General Public\n"+
      "License along with this library; if not, write to the Free Software\n"+
      "Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301\n"+
      "USA\n";
      System.out.println(copyrightNotice);
    }
    private void printVersion() {

    }
    private void printUsage() {
      printCopyright();
      String output =
"Usage:\n"+
"  <commandName> [options] inputFilename outputFilename\n"+
"options:\n"+
"  -bmin <x>     minimum block size, where <x> is an integer in range (16-65535)\n"+
"  -bmax <x>     maximum block size, where <x> is an integer in range (16-65535)\n"+
"  -lpcmin <x>   minimum LPC order, where <x> is an integer in range (1-32)\n"+
"  -lpcmax <x>   maximum LPC order, where <x> is an integer in range (1-32)\n"+
"  -Threads <x>  Specify whether to use threads. 0 turns threading off, greater\n"+
"                than 0 turns threading on\n"+
"  -sf <type>    Specify which subframe type to use, where <type> may be:\n"+
"      exhaustive(this is default and recommended)\n"+
"      fixed\n"+
"      lpc\n"+
"      verbatim";
      System.err.println(output);
    }
    private boolean handleArguments(String[] args) {
        //while arguments remaining, compare against argument list
        boolean valid = true;
        encodingConfig = new EncodingConfiguration();
        streamConfig = new StreamConfiguration();
        int intArg = -1;
        //System.err.println("Args length: "+args.length);
        for(int i = 0; i < args.length; i++) {
            if(args[i].equals(MIN_BLOCK)) {
               // System.err.println("Setting MIN_BLOCK");
                i++;
                valid &= ((intArg = getInt(args, i)) >= 0);
                if(valid)
                    streamConfig.setMinBlockSize(intArg);
                else
                    System.err.println(MIN_BLOCK_ERROR);
            }
            else if(args[i].equals(MAX_BLOCK)) {
                //System.err.println("Setting MAX_BLOCK");
                i++;
                valid &= ((intArg = getInt(args, i)) >= 0);
                if(valid)
                    streamConfig.setMaxBlockSize(intArg);
                else
                    System.err.println(MAX_BLOCK_ERROR);
            }
            else if(args[i].equals(MIN_LPC)) {
                //System.err.println("Setting MIN_LPC");
                i++;
                valid &= ((intArg = getInt(args, i)) >= 0);
                if(valid)
                    encodingConfig.setMinLPCOrder(intArg);
                else
                    System.err.println(MIN_LPC_ERROR);
            }
            else if(args[i].equals(MAX_LPC)) {
                //System.err.println("Setting MAX_LPC");
                i++;
                valid &= ((intArg = getInt(args, i)) >= 0);
                if(valid) 
                    encodingConfig.setMaxLPCOrder(intArg);
                else
                    System.err.println(MAX_LPC_ERROR);
            }
            else if(args[i].equals(SUBFRAME_TYPE)) {
                i++;
                String type = (i < args.length) ? args[i]:"";
                //System.err.println("Setting subframe to: "+type);
                EncodingConfiguration.SubframeType subType =
                        EncodingConfiguration.SubframeType.EXHAUSTIVE;
                if(type.equals(TYPE_LPC))
                    subType = EncodingConfiguration.SubframeType.LPC;
                else if(type.equals(TYPE_FIXED))
                    subType = EncodingConfiguration.SubframeType.FIXED;
                else if(type.equals(TYPE_EXHAUSTIVE))
                    subType = EncodingConfiguration.SubframeType.EXHAUSTIVE;
                else if(type.equals(TYPE_VERBATIM))
                    subType = EncodingConfiguration.SubframeType.VERBATIM;
                else {
                    valid = false;
                    System.err.println("Incorrect subframe type");
                }
                encodingConfig.setSubframeType(subType);

            }
            else if(args[i].equals(THREADS)) {
                i++;
                valid &= ((intArg = getInt(args, i)) >= 0);
                if(valid) {
                    if(intArg > 0) {
                        useThreads = true;
                       // System.err.println("Using threads");
                    }
                    else {
                       // System.err.println("Not using threads");
                        useThreads = false;
                    }
                    threadCount = intArg;
                }
                else {
                    valid = false;
                    System.err.println(THREADS_ERROR);
                }
            }
            else if(args[i].equals(HELP)) {
               attemptEncode = false;
               printUsage();
               valid = false;
            }
            else if(inputFile == null) {
                valid &= getInputFile(args[i]);
                //System.err.println("input:"+args[i]);
            }
            else if(outputFile == null) {
                valid &= getOutputFile(args[i]);
                //System.err.println("output:"+args[i]);
            }
            else {
                System.err.println("Invalid command switch");
            }
            if(!valid) {
                if(i > args.length)
                    i = args.length-1;//last argument that could have ran
                //System.err.println("Option invalid: "+args[i]);
                break;
            }
        }
     //   encodingConfig.setChannelConfig(EncodingConfiguration.ChannelConfig.EXHAUSTIVE);
       // streamConfig.setMaxBlockSize(1152);
       // streamConfig.setMinBlockSize(1152);
        return valid;
    }
    private boolean getOutputFile(String filename) {
        boolean result = true;
        File file = new File(filename);
       /* if(file.exists() ) {
            System.err.println("Output file exists, exiting");
            result = false;
        }*/
         {
            try {
                if(!file.exists())
                    file.createNewFile();
                if( (result &= file.canWrite()) == false) {
                    System.err.println("Error, can't write to output file: "+filename);
                }
                else {
                    outputFile = file;
                }
            }catch(IOException e) {

            }
        }
        
        return result;
    }

    private boolean getInputFile(String filename) {
        boolean result = true;
        File file = new File(filename);
        if ( (result = file.exists()) == false) {
            System.err.println("Error, input file not found: "+filename);
        }
        else if( (result = file.isFile()) == false) {
            System.err.println("Error, input is not a file: "+filename);
        }
        else if( (result = file.canRead()) == false) {
            System.err.println("Error, can't read input file: "+filename);
        }
        else {
            inputFile = file;
        }
        //System.err.println("Input file boolean: "+result);
        return result;
    }

    /**
     * Utility function to parse a positive integer argument out of a String
     * array at the given index.
     * @param args String array containing element to find.
     * @param index Index of array element to parse integer from.
     * @return Integer parsed, or -1 if error.
     */
    int getInt(String[] args, int index) {
        int result = -1;
        if(index >= 0 && index < args.length) {
            try {
                result = Integer.parseInt(args[index]);
            }catch(NumberFormatException e) {
                result = -1;
            }
        }
        return result;
    }

    private void fileEncoderTest() {
        File fIn = new File("input.wav");
        File fOut = new File("fileEncoderTest.flac");
        FLAC_FileEncoder enc = new FLAC_FileEncoder();        
        enc.encode(fIn, fOut);
    }
}
