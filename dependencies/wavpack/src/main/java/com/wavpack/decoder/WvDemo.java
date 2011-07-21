package com.wavpack.decoder;

import java.io.RandomAccessFile;

/*
** WvDemo.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

public class WvDemo {
    static int[] temp_buffer = new int[Defines.SAMPLE_BUFFER_SIZE];

    static byte[] pcm_buffer = new byte[4 * Defines.SAMPLE_BUFFER_SIZE];

    public static void main(String[] args) {
        ChunkHeader FormatChunkHeader = new ChunkHeader();
        ChunkHeader DataChunkHeader = new ChunkHeader();
        RiffChunkHeader myRiffChunkHeader = new RiffChunkHeader();
        WaveHeader WaveHeader = new WaveHeader();
        byte[] myRiffChunkHeaderAsByteArray = new byte[12];
        byte[] myFormatChunkHeaderAsByteArray = new byte[8];
        byte[] myWaveHeaderAsByteArray = new byte[16];
        byte[] myDataChunkHeaderAsByteArray = new byte[8];

        long total_unpacked_samples = 0, total_samples; // was uint32_t in C
        int num_channels, bps;
        WavpackContext wpc = new WavpackContext();
        java.io.FileInputStream fistream;
        java.io.FileOutputStream fostream;
        java.io.DataInputStream in;
        long start, end;

        String inputWVFile;

        if (args.length == 0) {
            inputWVFile = "testfiles/wavpack/Paul Oakenfold - Swordfish __The Album__.wv";
        } else {
            inputWVFile = args[0];
        }

        try {
            RandomAccessFile ras = new RandomAccessFile(inputWVFile, "r");
            wpc = WavPackUtils.WavpackOpenFileInput(ras);
        }
        catch (java.io.FileNotFoundException fe) {
            System.err.println("Input file not found");
            System.exit(1);
        }

        if (wpc.error) {
            System.err.println("Sorry an error has occured");
            System.err.println(wpc.error_message);
            System.exit(1);
        }

        num_channels = WavPackUtils.WavpackGetReducedChannels(wpc);

        System.out.println("The wavpack file has " + num_channels + " channels");

        total_samples = WavPackUtils.WavpackGetNumSamples(wpc);

        System.out.println("The wavpack file has " + total_samples + " samples");

        bps = WavPackUtils.WavpackGetBytesPerSample(wpc);

        System.out.println("The wavpack file has " + bps + " bytes per sample");

        myRiffChunkHeader.ckID[0] = 'R';
        myRiffChunkHeader.ckID[1] = 'I';
        myRiffChunkHeader.ckID[2] = 'F';
        myRiffChunkHeader.ckID[3] = 'F';

        myRiffChunkHeader.ckSize = total_samples * num_channels * bps + 8 * 2 + 16 + 4;
        myRiffChunkHeader.formType[0] = 'W';
        myRiffChunkHeader.formType[1] = 'A';
        myRiffChunkHeader.formType[2] = 'V';
        myRiffChunkHeader.formType[3] = 'E';

        FormatChunkHeader.ckID[0] = 'f';
        FormatChunkHeader.ckID[1] = 'm';
        FormatChunkHeader.ckID[2] = 't';
        FormatChunkHeader.ckID[3] = ' ';

        FormatChunkHeader.ckSize = 16;

        WaveHeader.FormatTag = 1;
        WaveHeader.NumChannels = num_channels;
        WaveHeader.SampleRate = WavPackUtils.WavpackGetSampleRate(wpc);
        WaveHeader.BlockAlign = num_channels * bps;
        WaveHeader.BytesPerSecond = WaveHeader.SampleRate * WaveHeader.BlockAlign;
        WaveHeader.BitsPerSample = WavPackUtils.WavpackGetBitsPerSample(wpc);

        DataChunkHeader.ckID[0] = 'd';
        DataChunkHeader.ckID[1] = 'a';
        DataChunkHeader.ckID[2] = 't';
        DataChunkHeader.ckID[3] = 'a';
        DataChunkHeader.ckSize = total_samples * num_channels * bps;

        myRiffChunkHeaderAsByteArray[0] = (byte) myRiffChunkHeader.ckID[0];
        myRiffChunkHeaderAsByteArray[1] = (byte) myRiffChunkHeader.ckID[1];
        myRiffChunkHeaderAsByteArray[2] = (byte) myRiffChunkHeader.ckID[2];
        myRiffChunkHeaderAsByteArray[3] = (byte) myRiffChunkHeader.ckID[3];

        // swap endians here

        myRiffChunkHeaderAsByteArray[7] = (byte) (myRiffChunkHeader.ckSize >>> 24);
        myRiffChunkHeaderAsByteArray[6] = (byte) (myRiffChunkHeader.ckSize >>> 16);
        myRiffChunkHeaderAsByteArray[5] = (byte) (myRiffChunkHeader.ckSize >>> 8);
        myRiffChunkHeaderAsByteArray[4] = (byte) (myRiffChunkHeader.ckSize);

        myRiffChunkHeaderAsByteArray[8] = (byte) myRiffChunkHeader.formType[0];
        myRiffChunkHeaderAsByteArray[9] = (byte) myRiffChunkHeader.formType[1];
        myRiffChunkHeaderAsByteArray[10] = (byte) myRiffChunkHeader.formType[2];
        myRiffChunkHeaderAsByteArray[11] = (byte) myRiffChunkHeader.formType[3];

        myFormatChunkHeaderAsByteArray[0] = (byte) FormatChunkHeader.ckID[0];
        myFormatChunkHeaderAsByteArray[1] = (byte) FormatChunkHeader.ckID[1];
        myFormatChunkHeaderAsByteArray[2] = (byte) FormatChunkHeader.ckID[2];
        myFormatChunkHeaderAsByteArray[3] = (byte) FormatChunkHeader.ckID[3];

        // swap endians here
        myFormatChunkHeaderAsByteArray[7] = (byte) (FormatChunkHeader.ckSize >>> 24);
        myFormatChunkHeaderAsByteArray[6] = (byte) (FormatChunkHeader.ckSize >>> 16);
        myFormatChunkHeaderAsByteArray[5] = (byte) (FormatChunkHeader.ckSize >>> 8);
        myFormatChunkHeaderAsByteArray[4] = (byte) (FormatChunkHeader.ckSize);

        // swap endians
        myWaveHeaderAsByteArray[1] = (byte) (WaveHeader.FormatTag >>> 8);
        myWaveHeaderAsByteArray[0] = (byte) (WaveHeader.FormatTag);

        // swap endians
        myWaveHeaderAsByteArray[3] = (byte) (WaveHeader.NumChannels >>> 8);
        myWaveHeaderAsByteArray[2] = (byte) WaveHeader.NumChannels;


        // swap endians
        myWaveHeaderAsByteArray[7] = (byte) (WaveHeader.SampleRate >>> 24);
        myWaveHeaderAsByteArray[6] = (byte) (WaveHeader.SampleRate >>> 16);
        myWaveHeaderAsByteArray[5] = (byte) (WaveHeader.SampleRate >>> 8);
        myWaveHeaderAsByteArray[4] = (byte) (WaveHeader.SampleRate);

        // swap endians

        myWaveHeaderAsByteArray[11] = (byte) (WaveHeader.BytesPerSecond >>> 24);
        myWaveHeaderAsByteArray[10] = (byte) (WaveHeader.BytesPerSecond >>> 16);
        myWaveHeaderAsByteArray[9] = (byte) (WaveHeader.BytesPerSecond >>> 8);
        myWaveHeaderAsByteArray[8] = (byte) (WaveHeader.BytesPerSecond);

        // swap endians
        myWaveHeaderAsByteArray[13] = (byte) (WaveHeader.BlockAlign >>> 8);
        myWaveHeaderAsByteArray[12] = (byte) WaveHeader.BlockAlign;

        // swap endians
        myWaveHeaderAsByteArray[15] = (byte) (WaveHeader.BitsPerSample >>> 8);
        myWaveHeaderAsByteArray[14] = (byte) WaveHeader.BitsPerSample;

        myDataChunkHeaderAsByteArray[0] = (byte) DataChunkHeader.ckID[0];
        myDataChunkHeaderAsByteArray[1] = (byte) DataChunkHeader.ckID[1];
        myDataChunkHeaderAsByteArray[2] = (byte) DataChunkHeader.ckID[2];
        myDataChunkHeaderAsByteArray[3] = (byte) DataChunkHeader.ckID[3];

        // swap endians

        myDataChunkHeaderAsByteArray[7] = (byte) (DataChunkHeader.ckSize >>> 24);
        myDataChunkHeaderAsByteArray[6] = (byte) (DataChunkHeader.ckSize >>> 16);
        myDataChunkHeaderAsByteArray[5] = (byte) (DataChunkHeader.ckSize >>> 8);
        myDataChunkHeaderAsByteArray[4] = (byte) DataChunkHeader.ckSize;

        try {

            fostream = new java.io.FileOutputStream("output.wav");
            fostream.write(myRiffChunkHeaderAsByteArray);
            fostream.write(myFormatChunkHeaderAsByteArray);
            fostream.write(myWaveHeaderAsByteArray);
            fostream.write(myDataChunkHeaderAsByteArray);

            start = System.currentTimeMillis();

            while (true) {
                long samples_unpacked; // was uint32_t in C

                samples_unpacked = WavPackUtils.WavpackUnpackSamples(wpc, temp_buffer, Defines.SAMPLE_BUFFER_SIZE / num_channels);

                total_unpacked_samples += samples_unpacked;

                if (samples_unpacked > 0) {
                    samples_unpacked = samples_unpacked * num_channels;

                    pcm_buffer = format_samples(bps, temp_buffer, samples_unpacked);
                    fostream.write(pcm_buffer, 0, (int) samples_unpacked * bps);
                }

                if (samples_unpacked == 0)
                    break;

            } // end of while

            end = System.currentTimeMillis();

            System.out.println(end - start + " milli seconds to process WavPack file in main loop");
        }
        catch (Exception e) {
            System.err.println("Error when writing wav file, sorry: ");
            e.printStackTrace();
            System.exit(1);
        }

        if ((WavPackUtils.WavpackGetNumSamples(wpc) != -1)
                && (total_unpacked_samples != WavPackUtils.WavpackGetNumSamples(wpc))) {
            System.err.println("Incorrect number of samples");
            System.exit(1);
        }

        if (WavPackUtils.WavpackGetNumErrors(wpc) > 0) {
            System.err.println("CRC errors detected");
            java.lang.System.exit(1);
        }

        java.lang.System.exit(0);
    }


    // Reformat samples from longs in processor's native endian mode to
    // little-endian data with (possibly) less than 4 bytes / sample.

    static byte[] format_samples(int bps, int src[], long samcnt) {
        int temp;
        int counter = 0;
        int counter2 = 0;
        byte[] dst = new byte[4 * Defines.SAMPLE_BUFFER_SIZE];

        switch (bps) {
            case 1:
                while (samcnt > 0) {
                    dst[counter] = (byte) (0x00FF & (src[counter] + 128));
                    counter++;
                    samcnt--;
                }
                break;

            case 2:
                while (samcnt > 0) {
                    temp = src[counter2];
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;

            case 3:
                while (samcnt > 0) {
                    temp = src[counter2];
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    dst[counter] = (byte) (temp >>> 16);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;

            case 4:
                while (samcnt > 0) {
                    temp = src[counter2];
                    dst[counter] = (byte) temp;
                    counter++;
                    dst[counter] = (byte) (temp >>> 8);
                    counter++;
                    dst[counter] = (byte) (temp >>> 16);
                    counter++;
                    dst[counter] = (byte) (temp >>> 24);
                    counter++;
                    counter2++;
                    samcnt--;
                }

                break;
        }

        return dst;
    }
}