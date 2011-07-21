package com.wavpack.decoder;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Arrays;

/*
** WavPackUtils.java
**
** Copyright (c) 2007 - 2008 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

public class WavPackUtils {


    ///////////////////////////// local table storage ////////////////////////////

    static long sample_rates[] =
            {
                    6000, 8000, 9600, 11025, 12000, 16000, 22050, 24000, 32000, 44100, 48000, 64000, 88200, 96000, 192000
            };
    ///////////////////////////// executable code ////////////////////////////////


    // This function reads data from the specified stream in search of a valid
    // WavPack 4.0 audio block. If this fails in 1 megabyte (or an invalid or
    // unsupported WavPack block is encountered) then an appropriate message is
    // copied to "error" and NULL is returned, otherwise a pointer to a
    // WavpackContext structure is returned (which is used to call all other
    // functions in this module). This can be initiated at the beginning of a
    // WavPack file, or anywhere inside a WavPack file. To determine the exact
    // position within the file use WavpackGetSampleIndex().  Also,
    // this function will not handle "correction" files, plays only the first
    // two channels of multi-channel files, and is limited in resolution in some
    // large integer or floating point files (but always provides at least 24 bits
    // of resolution).

    public static WavpackContext WavpackOpenFileInput(RandomAccessFile infile) {
        WavpackContext wpc = new WavpackContext();
        WavpackStream wps = wpc.stream;

        wpc.infile = infile;
        wpc.total_samples = -1;
        wpc.norm_offset = 0;
        wpc.open_flags = 0;


        // open the source file for reading and store the size

        while (wps.wphdr.block_samples == 0) {

            wps.wphdr = read_next_header(wpc.infile, wps.wphdr);

            if (wps.wphdr.status == 1) {
                wpc.error_message = "not compatible with this version of WavPack file!";
                wpc.error = true;
                return (wpc);
            }

            if (wps.wphdr.block_samples > 0 && wps.wphdr.total_samples != -1) {
                wpc.total_samples = wps.wphdr.total_samples;
            }

            // lets put the stream back in the context

            wpc.stream = wps;

            if ((UnpackUtils.unpack_init(wpc)) == Defines.FALSE) {
                wpc.error = true;
                return wpc;
            }
        } // end of while

        wpc.config.flags = wpc.config.flags & ~0xff;
        wpc.config.flags = wpc.config.flags | (wps.wphdr.flags & 0xff);

        wpc.config.bytes_per_sample = (int) ((wps.wphdr.flags & Defines.BYTES_STORED) + 1);
        wpc.config.float_norm_exp = wps.float_norm_exp;

        wpc.config.bits_per_sample = (int) ((wpc.config.bytes_per_sample * 8)
                - ((wps.wphdr.flags & Defines.SHIFT_MASK) >> Defines.SHIFT_LSB));

        if ((wpc.config.flags & Defines.FLOAT_DATA) > 0) {
            wpc.config.bytes_per_sample = 3;
            wpc.config.bits_per_sample = 24;
        }

        if (wpc.config.sample_rate == 0) {
            if (wps.wphdr.block_samples == 0 || (wps.wphdr.flags & Defines.SRATE_MASK) == Defines.SRATE_MASK)
                wpc.config.sample_rate = 44100;
            else
                wpc.config.sample_rate = sample_rates[(int) ((wps.wphdr.flags & Defines.SRATE_MASK)
                        >> Defines.SRATE_LSB)];
        }

        if (wpc.config.num_channels == 0) {
            if ((wps.wphdr.flags & Defines.MONO_FLAG) > 0) {
                wpc.config.num_channels = 1;
            } else {
                wpc.config.num_channels = 2;
            }

            wpc.config.channel_mask = 0x5 - wpc.config.num_channels;
        }

        if ((wps.wphdr.flags & Defines.FINAL_BLOCK) == 0) {
            if ((wps.wphdr.flags & Defines.MONO_FLAG) != 0) {
                wpc.reduced_channels = 1;
            } else {
                wpc.reduced_channels = 2;
            }
        }

        return wpc;
    }

    // This function obtains general information about an open file and returns
    // a mask with the following bit values:

    // MODE_LOSSLESS:  file is lossless (pure lossless only)
    // MODE_HYBRID:  file is hybrid mode (lossy part only)
    // MODE_FLOAT:  audio data is 32-bit ieee floating point (but will provided
    //               in 24-bit integers for convenience)
    // MODE_HIGH:  file was created in "high" mode (information only)
    // MODE_FAST:  file was created in "fast" mode (information only)


    static int WavpackGetMode(WavpackContext wpc) {
        int mode = 0;

        if (null != wpc) {
            if ((wpc.config.flags & Defines.CONFIG_HYBRID_FLAG) != 0)
                mode |= Defines.MODE_HYBRID;
            else if ((wpc.config.flags & Defines.CONFIG_LOSSY_MODE) == 0)
                mode |= Defines.MODE_LOSSLESS;

            if (wpc.lossy_blocks != 0)
                mode &= ~Defines.MODE_LOSSLESS;

            if ((wpc.config.flags & Defines.CONFIG_FLOAT_DATA) != 0)
                mode |= Defines.MODE_FLOAT;

            if ((wpc.config.flags & Defines.CONFIG_HIGH_FLAG) != 0)
                mode |= Defines.MODE_HIGH;

            if ((wpc.config.flags & Defines.CONFIG_FAST_FLAG) != 0)
                mode |= Defines.MODE_FAST;
        }

        return mode;
    }


    // Unpack the specified number of samples from the current file position.
    // Note that "samples" here refers to "complete" samples, which would be
    // 2 longs for stereo files. The audio data is returned right-justified in
    // 32-bit longs in the endian mode native to the executing processor. So,
    // if the original data was 16-bit, then the values returned would be
    // +/-32k. Floating point data will be returned as 24-bit integers (and may
    // also be clipped). The actual number of samples unpacked is returned,
    // which should be equal to the number requested unless the end of fle is
    // encountered or an error occurs.

    public static long WavpackUnpackSamples(WavpackContext wpc, int[] buffer, long samples) {
        int[] temp_buffer = wpc.temp_buffer;
        WavpackStream wps = wpc.stream;
        long samples_unpacked = 0, samples_to_unpack;
        int num_channels = wpc.config.num_channels;
        int bcounter = 0;

        Arrays.fill(temp_buffer, 0);
        int buf_idx = 0;
        int bytes_returned;

        while (samples > 0) {
            if (wps.wphdr.block_samples == 0 || (wps.wphdr.flags & Defines.INITIAL_BLOCK) == 0
                    || wps.sample_index >= wps.wphdr.block_index + wps.wphdr.block_samples) {

                wps.wphdr = read_next_header(wpc.infile, wps.wphdr);

                if (wps.wphdr.status == 1)
                    break;

                if (wps.wphdr.block_samples == 0 || wps.sample_index == wps.wphdr.block_index) {
                    if ((UnpackUtils.unpack_init(wpc)) == Defines.FALSE)
                        break;
                }
            }

            if (wps.wphdr.block_samples == 0 || (wps.wphdr.flags & Defines.INITIAL_BLOCK) == 0
                    || wps.sample_index >= wps.wphdr.block_index + wps.wphdr.block_samples)
                continue;

            if (wps.sample_index < wps.wphdr.block_index) {
                samples_to_unpack = wps.wphdr.block_index - wps.sample_index;

                if (samples_to_unpack > samples)
                    samples_to_unpack = samples;

                wps.sample_index += samples_to_unpack;
                samples_unpacked += samples_to_unpack;
                samples -= samples_to_unpack;

                if (wpc.reduced_channels > 0)
                    samples_to_unpack *= wpc.reduced_channels;
                else
                    samples_to_unpack *= num_channels;

                while (samples_to_unpack > 0) {
                    temp_buffer[bcounter] = 0;
                    bcounter++;
                    samples_to_unpack--;
                }

                continue;
            }

            samples_to_unpack = wps.wphdr.block_index + wps.wphdr.block_samples - wps.sample_index;

            if (samples_to_unpack > samples)
                samples_to_unpack = samples;

            UnpackUtils.unpack_samples(wpc, temp_buffer, samples_to_unpack);

            if (wpc.reduced_channels > 0)
                bytes_returned = (int) (samples_to_unpack * wpc.reduced_channels);
            else
                bytes_returned = (int) (samples_to_unpack * num_channels);

            System.arraycopy(temp_buffer, 0, buffer, buf_idx, bytes_returned);

            buf_idx += bytes_returned;

            samples_unpacked += samples_to_unpack;
            samples -= samples_to_unpack;

            if (wps.sample_index == wps.wphdr.block_index + wps.wphdr.block_samples) {
                if (UnpackUtils.check_crc_error(wpc) > 0)
                    wpc.crc_errors++;
            }

            if (wps.sample_index == wpc.total_samples)
                break;
        }

        return (samples_unpacked);
    }


    // Get total number of samples contained in the WavPack file, or -1 if unknown

    public static long WavpackGetNumSamples(WavpackContext wpc) {
        // -1 would mean an unknown number of samples

        if (null != wpc) {
            return (wpc.total_samples);
        } else {
            return (long) -1;
        }
    }


    // Get the current sample index position, or -1 if unknown

    static long WavpackGetSampleIndex(WavpackContext wpc) {
        if (null != wpc)
            return wpc.stream.sample_index;

        return (long) -1;
    }


    // Get the number of errors encountered so far

    static long WavpackGetNumErrors(WavpackContext wpc) {
        if (null != wpc) {
            return wpc.crc_errors;
        } else {
            return (long) 0;
        }
    }


    // return if any uncorrected lossy blocks were actually written or read


    static int WavpackLossyBlocks(WavpackContext wpc) {
        if (null != wpc) {
            return wpc.lossy_blocks;
        } else {
            return 0;
        }
    }


    // Returns the sample rate of the specified WavPack file

    public static long WavpackGetSampleRate(WavpackContext wpc) {
        if (null != wpc && wpc.config.sample_rate != 0) {
            return wpc.config.sample_rate;
        } else {
            return (long) 44100;
        }
    }


    // Returns the number of channels of the specified WavPack file. Note that
    // this is the actual number of channels contained in the file, but this
    // version can only decode the first two.

    static int WavpackGetNumChannels(WavpackContext wpc) {
        if (null != wpc && wpc.config.num_channels != 0) {
            return wpc.config.num_channels;
        } else {
            return 2;
        }
    }


    // Returns the actual number of valid bits per sample contained in the
    // original file, which may or may not be a multiple of 8. Floating data
    // always has 32 bits, integers may be from 1 to 32 bits each. When this
    // value is not a multiple of 8, then the "extra" bits are located in the
    // LSBs of the results. That is, values are right justified when unpacked
    // into longs, but are left justified in the number of bytes used by the
    // original data.

    public static int WavpackGetBitsPerSample(WavpackContext wpc) {
        if (null != wpc && wpc.config.bits_per_sample != 0) {
            return wpc.config.bits_per_sample;
        } else {
            return 16;
        }
    }


    // Returns the number of bytes used for each sample (1 to 4) in the original
    // file. This is required information for the user of this module because the
    // audio data is returned in the LOWER bytes of the long buffer and must be
    // left-shifted 8, 16, or 24 bits if normalized longs are required.

    static int WavpackGetBytesPerSample(WavpackContext wpc) {
        if (null != wpc && wpc.config.bytes_per_sample != 0) {
            return wpc.config.bytes_per_sample;
        } else {
            return 2;
        }
    }


    // This function will return the actual number of channels decoded from the
    // file (which may or may not be less than the actual number of channels, but
    // will always be 1 or 2). Normally, this will be the front left and right
    // channels of a multi-channel file.

    public static int WavpackGetReducedChannels(WavpackContext wpc) {
        if (null != wpc && wpc.reduced_channels != 0) {
            return wpc.reduced_channels;
        } else if (null != wpc && wpc.config.num_channels != 0) {
            return wpc.config.num_channels;
        } else {
            return 2;
        }
    }

    public static void setTime(WavpackContext wpc, double milliseconds) {
        long targetSample = (long) (milliseconds / 1000 * wpc.config.sample_rate);
        try {
            seek(wpc, wpc.infile, wpc.infile.getFilePointer(), targetSample);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static void setSample(WavpackContext wpc, long sample) {
        seek(wpc, wpc.infile, 0, sample);
    }

    // Find the WavPack block that contains the specified sample. If "header_pos"
    // is zero, then no information is assumed except the total number of samples
    // in the file and its size in bytes. If "header_pos" is non-zero then we
    // assume that it is the file position of the valid header image contained in
    // the first stream and we can limit our search to either the portion above
    // or below that point. If a .wvc file is being used, then this must be called
    // for that file also.
    private static void seek(WavpackContext wpc, RandomAccessFile infile, long headerPos, long targetSample) {
        try {
            WavpackStream wps = wpc.stream;
            long file_pos1 = 0, file_pos2 = wpc.infile.length();
            long sample_pos1 = 0, sample_pos2 = wpc.total_samples;
            double ratio = 0.96;
            int file_skip = 0;

            if (targetSample >= wpc.total_samples)
                return;

            if (headerPos > 0 && wps.wphdr.block_samples > 0) {
                if (wps.wphdr.block_index > targetSample) {
                    sample_pos2 = wps.wphdr.block_index;
                    file_pos2 = headerPos;
                } else if (wps.wphdr.block_index + wps.wphdr.block_samples <= targetSample) {
                    sample_pos1 = wps.wphdr.block_index;
                    file_pos1 = headerPos;
                } else
                    return;
            }

            while (true) {
                double bytes_per_sample;
                long seek_pos;

                bytes_per_sample = file_pos2 - file_pos1;
                bytes_per_sample /= sample_pos2 - sample_pos1;
                seek_pos = file_pos1 + (file_skip > 0 ? 32 : 0);
                seek_pos += (long) (bytes_per_sample * (targetSample - sample_pos1) * ratio);
                infile.seek(seek_pos);
                long temppos = infile.getFilePointer();
                wps.wphdr = read_next_header(infile, wps.wphdr);

                //todo check this
//                if (ret != 1)
//                    wps.wphdr.block_index -= wpc.initial_index;

                if (wps.wphdr.status == 1 || seek_pos >= file_pos2) {
                    if (ratio > 0.0) {
                        if ((ratio -= 0.24) < 0.0)
                            ratio = 0.0;
                    } else
                        return;
                } else if (wps.wphdr.block_index > targetSample) {
                    sample_pos2 = wps.wphdr.block_index;
                    file_pos2 = seek_pos;
                } else if (wps.wphdr.block_index + wps.wphdr.block_samples <= targetSample) {
                    if (seek_pos == file_pos1)
                        file_skip = 1;
                    else {
                        sample_pos1 = wps.wphdr.block_index;
                        file_pos1 = seek_pos;
                    }
                } else {
                    int index = (int) (targetSample - wps.wphdr.block_index);
                    infile.seek(temppos);
                    WavpackContext c = WavpackOpenFileInput(infile);
                    wpc.stream = c.stream;
                    int[] temp_buf = new int[Defines.SAMPLE_BUFFER_SIZE];
                    while (index > 0) {
                        int toUnpack = Math.min(index, Defines.SAMPLE_BUFFER_SIZE / WavpackGetReducedChannels(wpc));
                        WavpackUnpackSamples(wpc, temp_buf, toUnpack);
                        index -= toUnpack;
                    }
                    return;
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // Read from current file position until a valid 32-byte WavPack 4.0 header is
    // found and read into the specified pointer. If no WavPack header is found within 1 meg,
    // then an error is returned. No additional bytes are read past the header. 

    static WavpackHeader read_next_header(RandomAccessFile infile, WavpackHeader wphdr) {
        byte buffer[] = new byte[32]; // 32 is the size of a WavPack Header
        byte temp[] = new byte[32];

        long bytes_skipped = 0;
        int bleft = 0; // bytes left in buffer
        int counter;
        int i;

        while (true) {
            for (i = 0; i < bleft; i++) {
                buffer[i] = buffer[32 - bleft + i];
            }

            counter = 0;

            try {
                if (infile.read(temp, 0, 32 - bleft) != (int) 32 - bleft) {
                    wphdr.status = 1;
                    return wphdr;
                }
            } catch (Exception e) {
                wphdr.status = 1;
                return wphdr;
            }

            for (i = 0; i < 32 - bleft; i++) {
                buffer[bleft + i] = temp[i];
            }

            bleft = 32;

            if (buffer[0] == 'w' && buffer[1] == 'v' && buffer[2] == 'p' && buffer[3] == 'k'
                    && (buffer[4] & 1) == 0 && buffer[6] < 16 && buffer[7] == 0 && buffer[9] == 4
                    && buffer[8] >= (Defines.MIN_STREAM_VERS & 0xff) && buffer[8] <= (Defines.MAX_STREAM_VERS & 0xff)) {

                wphdr.ckID[0] = 'w';
                wphdr.ckID[1] = 'v';
                wphdr.ckID[2] = 'p';
                wphdr.ckID[3] = 'k';

                wphdr.ckSize = (long) ((buffer[7] & 0xFF) << 24);
                wphdr.ckSize += (long) ((buffer[6] & 0xFF) << 16);
                wphdr.ckSize += (long) ((buffer[5] & 0xFF) << 8);
                wphdr.ckSize += (long) (buffer[4] & 0xFF);

                wphdr.version = (short) (buffer[9] << 8);
                wphdr.version += (short) (buffer[8]);

                wphdr.track_no = buffer[10];
                wphdr.index_no = buffer[11];

                wphdr.total_samples = (long) ((buffer[15] & 0xFF) << 24);
                wphdr.total_samples += (long) ((buffer[14] & 0xFF) << 16);
                wphdr.total_samples += (long) ((buffer[13] & 0xFF) << 8);
                wphdr.total_samples += (long) (buffer[12] & 0xFF);

                wphdr.block_index = (long) ((buffer[19] & 0xFF) << 24);
                wphdr.block_index += (long) ((buffer[18] & 0xFF) << 16);
                wphdr.block_index += (long) ((buffer[17] & 0xFF) << 8);
                wphdr.block_index += (long) (buffer[16]) & 0XFF;

                wphdr.block_samples = (long) ((buffer[23] & 0xFF) << 24);
                wphdr.block_samples += (long) ((buffer[22] & 0xFF) << 16);
                wphdr.block_samples += (long) ((buffer[21] & 0xFF) << 8);
                wphdr.block_samples += (long) (buffer[20] & 0XFF);

                wphdr.flags = (long) ((buffer[27] & 0xFF) << 24);
                wphdr.flags += (long) ((buffer[26] & 0xFF) << 16);
                wphdr.flags += (long) ((buffer[25] & 0xFF) << 8);
                wphdr.flags += (long) (buffer[24] & 0xFF);

                wphdr.crc = (long) ((buffer[31] & 0xFF) << 24);
                wphdr.crc += (long) ((buffer[30] & 0xFF) << 16);
                wphdr.crc += (long) ((buffer[29] & 0xFF) << 8);
                wphdr.crc += (long) (buffer[28] & 0xFF);

                wphdr.status = 0;

                return wphdr;
            } else {
                counter++;
                bleft--;
            }

            while (bleft > 0 && buffer[counter] != 'w') {
                counter++;
                bleft--;
            }

            bytes_skipped = bytes_skipped + counter;

            if (bytes_skipped > 1048576L) {
                wphdr.status = 1;
                return wphdr;
            }
        }
    }
}