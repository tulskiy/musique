/*
** DecoderDemo.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/
import com.beatofthedrum.alacdecoder.*;

class DecoderDemo
{

	static java.io.FileOutputStream output_stream;
	static int output_opened;

	static int write_wav_format = 1;

	static String input_file_n = "";
	static String output_file_n = "";


    // Reformat samples from longs in processor's native endian mode to
    // little-endian data with (possibly) less than 3 bytes / sample.

    public static byte[] format_samples(int bps, int[] src, int samcnt)
    {
        int temp = 0;
        int counter = 0;
        int counter2 = 0;
        byte[] dst = new byte[65536];

        switch (bps)
        {
            case 1:
                while (samcnt > 0)
                {
                    dst[counter] =  (byte)(0x00FF & (src[counter] + 128));
                    counter++;
                    samcnt--;
                }
				break;

			case 2:
				while (samcnt > 0)
				{
					temp = src[counter2];
					dst[counter] =  (byte)temp;
					counter++;
					dst[counter] =  (byte)(temp >>> 8);
					counter++;
					counter2++;
					samcnt = samcnt - 2;
                }
				break;

            case 3:
                while (samcnt > 0)
                {
                    dst[counter] =  (byte)src[counter2];
                    counter++;
                    counter2++;
                    samcnt--;
                }
				break;
        }

        return dst;
    }

	
	static void setup_environment(int argc, String[] argv)
	{
		int i = argc;

		int escaped = 0;

		if (argc < 2)
			usage();

		int arg_idx = 0;
		// loop through command-line arguments
		while (arg_idx < argc)
		{
			if (argv[arg_idx].startsWith("-"))
			{
				if (argv[arg_idx].startsWith("-r") || argv[arg_idx].startsWith("-R"))
				{
					// raw PCM output
					write_wav_format = 0;
				}
			}
			else if (input_file_n.length() == 0)
			{
				input_file_n = argv[arg_idx];
			}
			else if (output_file_n.length() == 0)
			{
				output_file_n = argv[arg_idx];
			}
			else
			{
				System.out.println("extra unknown argument: " + argv[arg_idx]);
				usage();
			}
			arg_idx++;
		}

		if (input_file_n.length() == 0 || output_file_n.length() == 0 )
			usage();

	}

	static void GetBuffer(AlacContext ac)
	{
		int destBufferSize = 1024 *24 * 3; // 24kb buffer = 4096 frames = 1 alac sample (we support max 24bps)
		byte[] pcmBuffer = new byte[65536];
		int total_unpacked_bytes = 0;
		int bytes_unpacked;
		
		int[] pDestBuffer = new int[destBufferSize]; 

		int bps = AlacUtils.AlacGetBytesPerSample(ac);
		
		
		while (true)
		{
			bytes_unpacked = AlacUtils.AlacUnpackSamples(ac, pDestBuffer).bytesUnpacked;

			total_unpacked_bytes += bytes_unpacked;

			if (bytes_unpacked > 0)
			{
				pcmBuffer = format_samples(bps, pDestBuffer, bytes_unpacked);
				try
				{
					output_stream.write(pcmBuffer, 0, bytes_unpacked);
				}
				catch(java.io.IOException ioe)
				{
					System.err.println("Error writing data to output file. Error: " + ioe);
				}
			}

			if (bytes_unpacked == 0)
				break;
		} // end of while
		

	}


	static void usage() 
	{
		System.out.println("Usage: alac [options] inputfile outputfile");
		System.out.println("Decompresses the ALAC file specified");
		System.out.println("Options:");
		System.out.println("  -r                write output as raw PCM data. Default");
		System.out.println("                    is in WAV format.");
		System.out.println("");
		System.out.println("This port of the code is (c) Peter McQuillan 2011");
		System.out.println("Original software is (c) 2005 David Hammerton");
		System.exit(1);
	}

	public static void main(String [] args)
	{
		AlacContext ac = new AlacContext();
		int output_size;
		int total_samples; 
		int sample_rate;
        int num_channels;
		int byteps;
		int bitps;

		output_opened = 0;

		setup_environment(args.length, args);	// checks all the parameters passed on command line
		
		try
		{
			output_stream = new java.io.FileOutputStream(output_file_n);
			output_opened = 1;
		}
		catch(java.io.IOException ioe)
		{
			System.out.println("Cannot open output file: " + output_file_n + " : Error : " + ioe);
			output_opened = 0;
			System.exit(1);
		}
		
		ac = AlacUtils.AlacOpenFileInput(input_file_n);
		
		if (ac.error)
        {
            System.err.println("Sorry an error has occured");
            System.err.println(ac.error_message);
            System.exit(1);
        }
		
		num_channels = AlacUtils.AlacGetNumChannels(ac);

        System.out.println("The Apple Lossless file has " + num_channels + " channels");

        total_samples = AlacUtils.AlacGetNumSamples(ac);

        System.out.println("The Apple Lossless file has " + total_samples + " samples");

        byteps = AlacUtils.AlacGetBytesPerSample(ac);

        System.out.println("The Apple Lossless file has " + byteps + " bytes per sample");
		
		sample_rate = AlacUtils.AlacGetSampleRate(ac);
		
		bitps = AlacUtils.AlacGetBitsPerSample(ac);


		/* write wav output headers */
		if (write_wav_format != 0)
		{
			WavWriter.wavwriter_writeheaders(output_stream, (total_samples * byteps * num_channels), num_channels, sample_rate, byteps, bitps);
		}

		/* will convert the entire buffer */
		GetBuffer(ac);
		
		AlacUtils.AlacCloseFile(ac);

		if (output_opened != 0)
		{
			try
			{
				output_stream.close();
			}
			catch(java.io.IOException ioe)
			{
			}
		}
	}
}

