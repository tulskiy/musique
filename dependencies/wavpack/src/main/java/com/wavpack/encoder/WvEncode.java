/*
** WvEncode.java
**
** Copyright (c) 2008 Peter McQuillan
**
** All Rights Reserved.
**
** Distributed under the BSD Software License (see license.txt)
**
*/
package com.wavpack.encoder;

import java.io.IOException;
import java.io.RandomAccessFile;

public class WvEncode {
    public static void main(String[] args) {
        // This is the main module for the demonstration WavPack command-line
        // encoder using the "tiny encoder". It accepts a source WAV file, a
        // destination WavPack file (.wv) and an optional WavPack correction file
        // (.wvc) on the command-line. It supports all 4 encoding qualities in
        // pure lossless, hybrid lossy and hybrid lossless modes. Valid input are
        // mono or stereo integer WAV files with bitdepths from 8 to 24.
        // This program (and the tiny encoder) do not handle placing the WAV RIFF
        // header into the WavPack file. The latest version of the regular WavPack
        // unpacker (4.40) and the "tiny decoder" will generate the RIFF header
        // automatically on unpacking. However, older versions of the command-line
        // program will complain about this and require unpacking in "raw" mode.
        ///////////////////////////// local variable storage //////////////////////////
        String VERSION_STR = "4.40";
        String DATE_STR = "2007-01-16";

        String sign_on1 = "Java WavPack Encoder (c) 2008 Peter McQuillan";
        String sign_on2 = "based on TINYPACK - Tiny Audio Compressor  Version " + VERSION_STR +
                          " " + DATE_STR + " Copyright (c) 1998 - 2007 Conifer Software.  All Rights Reserved.";

        String usage0 = "";
        String usage1 = " Usage:   java WvEncode [-options] infile.wav outfile.wv [outfile.wvc]";
        String usage2 = " (default is lossless)";
        String usage3 = "  Options: -b n = enable hybrid compression, n = 2.0 to 16.0 bits/sample - need space between b and number";
        String usage4 = "       -c  = create correction file (.wvc) for hybrid mode (=lossless)";
        String usage5 = "       -cc = maximum hybrid compression (hurts lossy quality & decode speed)";
        String usage6 = "       -f  = fast mode (fast, but some compromise in compression ratio)";
        String usage7 = "       -h  = high quality (better compression in all modes, but slower)";
        String usage8 = "       -hh = very high quality (best compression in all modes, but slowest";
        String usage9 = "                              and NOT recommended for portable hardware use)";
        String usage10 = "       -jn = joint-stereo override (0 = left/right, 1 = mid/side)";
        String usage11 = "       -sn = noise shaping override (hybrid only, n = -1.0 to 1.0, 0 = off)";

        //////////////////////////////////////////////////////////////////////////////
        // The "main" function for the command-line WavPack compressor.             //
        //////////////////////////////////////////////////////////////////////////////
        String infilename = "";
        String outfilename = "";
        String out2filename = "";
        WavpackConfig config = new WavpackConfig();
        int error_count = 0;
        int result;
        int i;
        int arg_idx = 0;

        // loop through command-line arguments
        while (arg_idx < args.length) {
            if (args[arg_idx].startsWith("-")) {
                if (args[arg_idx].startsWith("-c") || args[arg_idx].startsWith("-C")) {
                    if (args[arg_idx].startsWith("-cc") || args[arg_idx].startsWith("-CC")) {
                        config.flags |= Defines.CONFIG_CREATE_WVC;
                        config.flags |= Defines.CONFIG_OPTIMIZE_WVC;
                    } else {
                        config.flags |= Defines.CONFIG_CREATE_WVC;
                    }
                } else if (args[arg_idx].startsWith("-f") || args[arg_idx].startsWith("-F")) {
                    config.flags |= Defines.CONFIG_FAST_FLAG;
                } else if (args[arg_idx].startsWith("-h") || args[arg_idx].startsWith("-H")) {
                    if (args[arg_idx].startsWith("-hh") || args[arg_idx].startsWith("-HH")) {
                        config.flags |= Defines.CONFIG_VERY_HIGH_FLAG;
                    } else {
                        config.flags |= Defines.CONFIG_HIGH_FLAG;
                    }
                } else if (args[arg_idx].startsWith("-k") || args[arg_idx].startsWith("-K")) {
                    int passedInt = 0;

                    if (args[arg_idx].length() > 2) {
                        try {
                            String substring = args[arg_idx].substring(2);
                            passedInt = Integer.parseInt(substring);
                        }
                        catch (java.lang.NumberFormatException ne) {
                        }
                    } else {
                        arg_idx++;

                        try {
                            passedInt = Integer.parseInt(args[arg_idx]);
                        }
                        catch (java.lang.NumberFormatException ne) {
                        }
                    }

                    config.block_samples = passedInt;
                } else if (args[arg_idx].startsWith("-b") || args[arg_idx].startsWith("-B")) {
                    int passedInt = 0;
                    config.flags |= Defines.CONFIG_HYBRID_FLAG;

                    if (args[arg_idx].length() > 2) {
                        try {
                            String substring = args[arg_idx].substring(2);
                            passedInt = Integer.parseInt(substring);
                            config.bitrate = (int) (passedInt * 256.0);
                        }
                        catch (java.lang.NumberFormatException ne) {
                            config.bitrate = 0;
                        }
                    } else {
                        arg_idx++;

                        try {
                            passedInt = Integer.parseInt(args[arg_idx]);
                            config.bitrate = (int) (passedInt * 256.0);
                        }
                        catch (java.lang.NumberFormatException ne) {
                            config.bitrate = 0;
                        }
                    }

                    if ((config.bitrate < 512) || (config.bitrate > 4096)) {
                        System.err.println("hybrid spec must be 2.0 to 16.0!");
                        ++error_count;
                    }
                } else if (args[arg_idx].startsWith("-j") || args[arg_idx].startsWith("-J")) {
                    int passedInt = 2;

                    if (args[arg_idx].length() > 2) // handle the case where the string is passed in form -j0 (number beside j)
                    {
                        try {
                            String substring = args[arg_idx].substring(2);
                            passedInt = Integer.parseInt(substring);
                        }
                        catch (java.lang.NumberFormatException ne) {
                        }
                    } else // handle the case where the string is passed in form -j 0 (space between number and j)
                    {
                        arg_idx++;

                        try {
                            passedInt = Integer.parseInt(args[arg_idx]);
                        }
                        catch (java.lang.NumberFormatException ne) {
                        }
                    }

                    if (passedInt == 0) {
                        config.flags |= Defines.CONFIG_JOINT_OVERRIDE;
                        config.flags &= ~Defines.CONFIG_JOINT_STEREO;
                    } else if (passedInt == 1) {
                        config.flags |= (Defines.CONFIG_JOINT_OVERRIDE |
                                         Defines.CONFIG_JOINT_STEREO);
                    } else {
                        System.err.println("-j0 or -j1 only!");
                        ++error_count;
                    }
                } else if (args[arg_idx].startsWith("-s") || args[arg_idx].startsWith("-S")) {
                    double passedDouble = 0; // noise shaping off

                    if (args[arg_idx].length() > 2) // handle the case where the string is passed in form -s0 (number beside s)
                    {
                        try {
                            String substring = args[arg_idx].substring(2);
                            passedDouble = Double.parseDouble(substring);
                        }
                        catch (java.lang.NumberFormatException ne) {
                        }
                    } else // handle the case where the string is passed in form -s 0 (space between number and s)
                    {
                        arg_idx++;

                        try {
                            passedDouble = Double.parseDouble(args[arg_idx]);
                        }
                        catch (java.lang.NumberFormatException ne) {
                        }
                    }

                    config.shaping_weight = (int) (passedDouble * 1024.0);

                    if (config.shaping_weight == 0) {
                        config.flags |= Defines.CONFIG_SHAPE_OVERRIDE;
                        config.flags &= ~Defines.CONFIG_HYBRID_SHAPE;
                    } else if ((config.shaping_weight >= -1024) && (config.shaping_weight <= 1024)) {
                        config.flags |= (Defines.CONFIG_HYBRID_SHAPE |
                                         Defines.CONFIG_SHAPE_OVERRIDE);
                    } else {
                        System.err.println("-s-1.00 to -s1.00 only!");
                        ++error_count;
                    }
                } else {
                    System.err.println("illegal option: " + args[arg_idx]);
                    ++error_count;
                }
            } else if (infilename.length() == 0) {
                infilename = args[arg_idx];
            } else if (outfilename.length() == 0) {
                outfilename = args[arg_idx];
            } else if (out2filename.length() == 0) {
                out2filename = args[arg_idx];
            } else {
                System.err.println("extra unknown argument: " + args[arg_idx]);
                ++error_count;
            }

            arg_idx++;
        }

        // check for various command-line argument problems
        if ((~config.flags & (Defines.CONFIG_HIGH_FLAG | Defines.CONFIG_FAST_FLAG)) == 0) {
            System.err.println("high and fast modes are mutually exclusive!");
            ++error_count;
        }

        if ((config.flags & Defines.CONFIG_HYBRID_FLAG) != 0) {
            if (((config.flags & Defines.CONFIG_CREATE_WVC) != 0) && (out2filename.length() == 0)) {
                System.err.println("need name for correction file!");
                ++error_count;
            }
        } else {
            if ((config.flags & (Defines.CONFIG_SHAPE_OVERRIDE | Defines.CONFIG_CREATE_WVC)) != 0) {
                System.err.println("-s and -c options are for hybrid mode (-b) only!");
                ++error_count;
            }
        }

        if ((out2filename.length() != 0) && ((config.flags & Defines.CONFIG_CREATE_WVC) == 0)) {
            System.err.println("third filename specified without -c option!");
            ++error_count;
        }

        if (error_count == 0) {
            System.err.println(sign_on1);
            System.err.println(sign_on2);
        } else {
            System.exit(1);
        }

        if ((infilename.length() == 0) || (outfilename.length() == 0) ||
            ((out2filename.length() == 0) && ((config.flags & Defines.CONFIG_CREATE_WVC) != 0))) {
            System.out.println(usage0);
            System.out.println(usage1);
            System.out.println(usage2);
            System.out.println(usage3);
            System.out.println(usage4);
            System.out.println(usage5);
            System.out.println(usage6);
            System.out.println(usage7);
            System.out.println(usage8);
            System.out.println(usage9);
            System.out.println(usage10);
            System.out.println(usage11);

            System.exit(1);
        }

        result = pack_file(infilename, outfilename, out2filename, config);

        if (result > 0) {
            System.err.println("error occured!");
            ++error_count;
        }
    }

    // This function packs a single file "infilename" and stores the result at
    // "outfilename". If "out2filename" is specified, then the "correction"
    // file would go there. The files are opened and closed in this function
    // and the "config" structure specifies the mode of compression.
    static int pack_file(String infilename, String outfilename, String out2filename,
                         WavpackConfig config) {
        long total_samples;
        long bcount;
        WavpackConfig loc_config = config;
        byte[] riff_chunk_header = new byte[12];
        RandomAccessFile wv_file;
        RandomAccessFile wvc_file;
        byte[] chunk_header = new byte[8];
        byte[] WaveHeader = new byte[40];
        int whBlockAlign = 1;
        int whFormatTag = 0;
        int whSubFormat = 0;
        int whBitsPerSample = 0;
        int whValidBitsPerSample = 0;
        int whNumChannels = 0;
        long whSampleRate = 0;

        WavpackContext wpc = new WavpackContext();
        java.io.FileInputStream infile;
        java.io.DataInputStream in;
        int result;

        // open the source file for reading
        try {
            infile = new java.io.FileInputStream(infilename);
            in = new java.io.DataInputStream(infile);
        }
        catch (java.io.FileNotFoundException fe) {
            System.err.println("Can't open file " + infilename);

            return Defines.SOFT_ERROR;
        }

        // open output file for writing
        try {
            wv_file = new RandomAccessFile(outfilename, "rw");
            wv_file.setLength(0);
        }
        catch (IOException fe) {
            System.err.println("can't create file " + outfilename);

            return Defines.SOFT_ERROR;
        }

        wpc.outfile = wv_file;

        bcount = 0;

        // 12 is the size of the RIFF Chunk header
        bcount = DoReadFile(in, riff_chunk_header, 12);

        if ((bcount != 12) || (riff_chunk_header[0] != 'R') || (riff_chunk_header[1] != 'I') ||
            (riff_chunk_header[2] != 'F') || (riff_chunk_header[3] != 'F') ||
            (riff_chunk_header[8] != 'W') || (riff_chunk_header[9] != 'A') ||
            (riff_chunk_header[10] != 'V') || (riff_chunk_header[11] != 'E')) {
            System.err.println(infilename + " is not a valid .WAV file!");

            try {
                infile.close();
                wv_file.close();
            }
            catch (Exception e) {
            }

            return Defines.SOFT_ERROR;
        }

        // loop through all elements of the RIFF wav header (until the data chuck)
        long chunkSize = 0;

        while (true) {
            // ChunkHeader has a size of 8
            bcount = DoReadFile(in, chunk_header, 8);

            if (bcount != 8) {
                System.err.println(infilename + " is not a valid .WAV file!");

                try {
                    infile.close();
                    wv_file.close();
                }
                catch (Exception e) {
                }

                return Defines.SOFT_ERROR;
            }

            chunkSize = (chunk_header[4] & 0xFF) + ((chunk_header[5] & 0xFF) << 8) +
                        ((chunk_header[6] & 0xFF) << 16) + ((chunk_header[7] & 0xFF) << 24);

            // if it's the format chunk, we want to get some info out of there and
            // make sure it's a .wav file we can handle
            if ((chunk_header[0] == 'f') && (chunk_header[1] == 'm') && (chunk_header[2] == 't') &&
                (chunk_header[3] == ' ')) {
                int supported = Defines.TRUE;
                int format;
                int check = 0;

                if ((chunkSize >= 16) && (chunkSize <= 40)) {
                    int ckSize = (int) chunkSize;

                    bcount = DoReadFile(in, WaveHeader, ckSize);

                    if (bcount != 16) {
                        check = 1;
                    }
                } else {
                    check = 1;
                }

                if (check == 1) {
                    System.err.println(infilename + " is not a valid .WAV file!");

                    try {
                        infile.close();
                        wv_file.close();
                    }
                    catch (Exception e) {
                    }

                    return Defines.SOFT_ERROR;
                }

                whFormatTag = (WaveHeader[0] & 0xFF) + ((WaveHeader[1] & 0xFF) << 8);

                if ((whFormatTag == 0xfffe) && (chunkSize == 40)) {
                    whSubFormat = (WaveHeader[24] & 0xFF) + ((WaveHeader[25] & 0xFF) << 8);
                    format = whSubFormat;
                } else {
                    format = whFormatTag;
                }

                whBitsPerSample = (WaveHeader[14] & 0xFF) + ((WaveHeader[15] & 0xFF) << 8);

                if (chunkSize == 40) {
                    whValidBitsPerSample = (WaveHeader[18] & 0xFF) +
                                           ((WaveHeader[19] & 0xFF) << 8);
                    loc_config.bits_per_sample = whValidBitsPerSample;
                } else {
                    loc_config.bits_per_sample = whBitsPerSample;
                }

                if (format != 1) {
                    supported = Defines.FALSE;
                }

                whBlockAlign = (WaveHeader[12] & 0xFF) + ((WaveHeader[13] & 0xFF) << 8);
                whNumChannels = (WaveHeader[2] & 0xFF) + ((WaveHeader[3] & 0xFF) << 8);

                if ((whNumChannels == 0) || (whNumChannels > 2) ||
                    ((whBlockAlign / whNumChannels) < ((loc_config.bits_per_sample + 7) / 8)) ||
                    ((whBlockAlign / whNumChannels) > 3) ||
                    ((whBlockAlign % whNumChannels) > 0)) {
                    supported = Defines.FALSE;
                }

                if ((loc_config.bits_per_sample < 1) || (loc_config.bits_per_sample > 24)) {
                    supported = Defines.FALSE;
                }

                whSampleRate = (WaveHeader[4] & 0xFF) + ((WaveHeader[5] & 0xFF) << 8) +
                               ((WaveHeader[6] & 0xFF) << 16) + ((WaveHeader[7] & 0xFF) << 24);

                if (supported != Defines.TRUE) {
                    System.err.println(infilename + " is an unsupported .WAV format!");

                    try {
                        infile.close();
                        wv_file.close();
                    }
                    catch (Exception e) {
                    }

                    return Defines.SOFT_ERROR;
                }
            } else if ((chunk_header[0] == 'd') && (chunk_header[1] == 'a') &&
                       (chunk_header[2] == 't') && (chunk_header[3] == 'a')) {
                // on the data chunk, get size and exit loop
                total_samples = chunkSize / whBlockAlign;

                break;
            } else { // just skip over unknown chunks

                int bytes_to_skip = (int) ((chunkSize + 1) & ~1L);
                byte[] buff = new byte[bytes_to_skip];

                bcount = DoReadFile(in, buff, bytes_to_skip);

                if (bcount != bytes_to_skip) {
                    System.err.println("error occurred in skipping bytes");

                    try {
                        infile.close();
                        wv_file.close();
                    }
                    catch (Exception e) {
                    }

                    //remove (outfilename);
                    return Defines.SOFT_ERROR;
                }
            }
        }

        loc_config.bytes_per_sample = whBlockAlign / whNumChannels;
        loc_config.num_channels = whNumChannels;
        loc_config.sample_rate = whSampleRate;

        WavPackUtils.WavpackSetConfiguration(wpc, loc_config, total_samples);

        // if we are creating a "correction" file, open it now for writing
        if (out2filename.length() > 0) {
            try {
                wvc_file = new RandomAccessFile(out2filename, "rw");
                wvc_file.setLength(0);
                wpc.correction_outfile = wvc_file;
            }
            catch (IOException fe) {
                System.err.println("can't create file " + outfilename);

                return Defines.SOFT_ERROR;
            }
        }

        // pack the audio portion of the file now
        result = pack_audio(wpc, in);

        try {
            infile.close(); // we're now done with input file, so close
        }
        catch (java.io.IOException e) {
        }

        // we're now done with any WavPack blocks, so flush any remaining data
        if ((result == Defines.NO_ERROR) && (WavPackUtils.WavpackFlushSamples(wpc) == 0)) {
            System.err.println(WavPackUtils.WavpackGetErrorMessage(wpc));
            result = Defines.HARD_ERROR;
        }

        // At this point we're done writing to the output files. However, in some
        // situations we might have to back up and re-write the initial blocks.
        // Currently the only case is if we're ignoring length.
        if ((result == Defines.NO_ERROR) &&
            (WavPackUtils.WavpackGetNumSamples(wpc) != WavPackUtils.WavpackGetSampleIndex(wpc))) {
            System.err.println("couldn't read all samples, file may be corrupt!!");
            result = Defines.SOFT_ERROR;
        }

        // at this point we're done with the files, so close 'em whether there
        // were any other errors or not
        try {
            wv_file.close();
        }
        catch (java.io.IOException e) {
            System.err.println("Can't close WavPack file!");

            if (result == Defines.NO_ERROR) {
                result = Defines.SOFT_ERROR;
            }
        }

        // if there were any errors then return the error
        if (result != Defines.NO_ERROR) {
            return result;
        }

        return Defines.NO_ERROR;
    }

    // This function handles the actual audio data compression. It assumes that the
    // input file is positioned at the beginning of the audio data and that the
    // WavPack configuration has been set. This is where the conversion from RIFF
    // little-endian standard the executing processor's format is done.
    static int pack_audio(WavpackContext wpc, java.io.DataInputStream in) {
        long samples_remaining;
        int bytes_per_sample;

        WavPackUtils.WavpackPackInit(wpc);

        bytes_per_sample = WavPackUtils.WavpackGetBytesPerSample(wpc) * WavPackUtils.WavpackGetNumChannels(wpc);

        samples_remaining = WavPackUtils.WavpackGetNumSamples(wpc);

        byte[] input_buffer = new byte[Defines.INPUT_SAMPLES * bytes_per_sample];
        long[] sample_buffer = new long[(Defines.INPUT_SAMPLES * 4 * WavPackUtils.WavpackGetNumChannels(wpc))];

        int temp = 0;

        //while (temp < 1)
        while (true) {
            long sample_count;
            long bytes_read = 0;
            int bytes_to_read;

            temp = temp + 1;

            if (samples_remaining > Defines.INPUT_SAMPLES) {
                bytes_to_read = Defines.INPUT_SAMPLES * bytes_per_sample;
            } else {
                bytes_to_read = (int) (samples_remaining * bytes_per_sample);
            }

            samples_remaining -= (bytes_to_read / bytes_per_sample);
            bytes_read = DoReadFile(in, input_buffer, bytes_to_read);
            sample_count = bytes_read / bytes_per_sample;

            if (sample_count == 0) {
                break;
            }

            if (sample_count > 0) {
                int cnt = (int) (sample_count * WavPackUtils.WavpackGetNumChannels(wpc));

                byte[] sptr = input_buffer;
                long[] dptr = sample_buffer;
                int loopBps = 0;

                loopBps = WavPackUtils.WavpackGetBytesPerSample(wpc);

                if (loopBps == 1) {
                    int intermalCount = 0;

                    while (cnt > 0) {
                        dptr[intermalCount] = (sptr[intermalCount] & 0xff) - 128;
                        intermalCount++;
                        cnt--;
                    }
                } else if (loopBps == 2) {
                    int dcounter = 0;
                    int scounter = 0;

                    while (cnt > 0) {
                        dptr[dcounter] = (sptr[scounter] & 0xff) | (sptr[scounter + 1] << 8);
                        scounter = scounter + 2;
                        dcounter++;
                        cnt--;
                    }
                } else if (loopBps == 3) {
                    int dcounter = 0;
                    int scounter = 0;

                    while (cnt > 0) {
                        dptr[dcounter] = (sptr[scounter] & 0xff) |
                                         ((sptr[scounter + 1] & 0xff) << 8) | (sptr[scounter + 2] << 16);
                        scounter = scounter + 3;
                        dcounter++;
                        cnt--;
                    }
                }
            }

            wpc.byte_idx = 0; // new WAV buffer data so reset the buffer index to zero

            if (WavPackUtils.WavpackPackSamples(wpc, sample_buffer, sample_count) == 0) {
                System.err.println(WavPackUtils.WavpackGetErrorMessage(wpc));

                return Defines.HARD_ERROR;
            }
        }

        if (WavPackUtils.WavpackFlushSamples(wpc) == 0) {
            System.err.println(WavPackUtils.WavpackGetErrorMessage(wpc));

            return Defines.HARD_ERROR;
        }

        return Defines.NO_ERROR;
    }

    //////////////////////////// File I/O Wrapper ////////////////////////////////
    static long DoReadFile(java.io.DataInputStream hFile, byte[] lpBuffer, int nNumberOfBytesToRead) {
        long bcount;
        byte[] tempBuffer = new byte[(int) (nNumberOfBytesToRead + (long) 1)];
        long bufferCounter = 0;
        long lpNumberOfBytesRead = 0;

        while (nNumberOfBytesToRead > 0) {
            try {
                bcount = hFile.read(tempBuffer, 0, nNumberOfBytesToRead);
            }
            catch (Exception e) {
                bcount = 0;
            }

            if (bcount > 0) {
                for (long i = 0; i < nNumberOfBytesToRead; i++) {
                    lpBuffer[(int) (bufferCounter + i)] = tempBuffer[(int) i];
                }

                lpNumberOfBytesRead += bcount;
                nNumberOfBytesToRead -= bcount;
            } else {
                break;
            }
        }

        return lpNumberOfBytesRead;
    }
}
