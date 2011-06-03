/*
** AlacUtils.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/
package com.beatofthedrum.alacdecoder;

public class AlacUtils
{
    public static AlacContext AlacOpenFileInput(String inputfilename)
    {
		int headerRead;
		QTMovieT qtmovie = new QTMovieT();
		DemuxResT demux_res = new DemuxResT();
		AlacContext ac = new AlacContext();
		AlacInputStream input_stream;
		AlacFile alac;
		
		ac.error = false;
		
		try
		{
			java.io.FileInputStream fistream;
			fistream = new java.io.FileInputStream(inputfilename);
			input_stream = new AlacInputStream(fistream);
		}
		catch (java.io.FileNotFoundException fe)
		{
			ac.error_message = "Input file not found";
			ac.error = true;
			return (ac);
		}
		
		ac.input_stream = input_stream;
		
		/* if qtmovie_read returns successfully, the stream is up to
		 * the movie data, which can be used directly by the decoder */
		headerRead = DemuxUtils.qtmovie_read(input_stream, qtmovie, demux_res);

		if (headerRead == 0)
		{
			ac.error = true;
			if (demux_res.format_read == 0)
			{
				ac.error_message = "Failed to load the QuickTime movie headers.";
				if (demux_res.format_read != 0)
					ac.error_message = ac.error_message + " File type: " + DemuxUtils.SplitFourCC(demux_res.format);
			}
			else
			{
				ac.error_message = "Error while loading the QuickTime movie headers.";
			}
			return (ac);
		}
		else if(headerRead == 3)
		{
			/*
			** This section is used when the stream system being used doesn't support seeking
			** We have kept track within the file where we need to go to, we close the file and
			** skip bytes to go directly to that point
			*/
			
			try
			{
				ac.input_stream.close();
			}
			catch(java.io.IOException ioe)
			{
				ac.error_message = "Error when seeking to start of music data";
				ac.error = true;
				return (ac);
			}
			
			try
			{
				java.io.FileInputStream fistream;
				fistream = new java.io.FileInputStream(inputfilename);
				input_stream = new AlacInputStream(fistream);
				ac.input_stream = input_stream;
				
				qtmovie.qtstream.stream = input_stream;
				qtmovie.qtstream.currentPos = 0;
				StreamUtils.stream_skip(qtmovie.qtstream, qtmovie.saved_mdat_pos);
			}
			catch (java.io.FileNotFoundException fe)
			{
				ac.error_message = "Input file not found";
				ac.error = true;
				return (ac);
			}
		}
		
		/* initialise the sound converter */
		
		alac = AlacDecodeUtils.create_alac(demux_res.sample_size, demux_res.num_channels);

		AlacDecodeUtils.alac_set_info(alac, demux_res.codecdata);

		ac.demux_res = demux_res;
		ac.alac = alac;
		
		return (ac);
			
	}
	
	public static void AlacCloseFile(AlacContext ac)
	{
		if(null != ac.input_stream)
		{
			try
			{
				ac.input_stream.close();
			}
			catch(java.io.IOException ioe)
			{
			}
		}
	}
	
	// Heres where we extract the actual music data
	
	public static int AlacUnpackSamples(AlacContext ac, int[] pDestBuffer)
	{
        int sample_byte_size;
		SampleDuration sampleinfo = new SampleDuration();
        byte[] read_buffer = ac.read_buffer;
		int destBufferSize = 1024 *24 * 3; // 24kb buffer = 4096 frames = 1 alac sample (we support max 24bps)
		int outputBytes;
		MyStream inputStream = new MyStream();

		inputStream.stream = ac.input_stream;
		
		// if current_sample_block is beyond last block then finished
		
		if(ac.current_sample_block >= ac.demux_res.sample_byte_size.length)
		{
			return 0;
		}
		
		if (get_sample_info(ac.demux_res, ac.current_sample_block , sampleinfo) == 0)
		{
			// getting sample failed
				return 0;
		}

        sample_byte_size = sampleinfo.sample_byte_size;

		StreamUtils.stream_read(inputStream, sample_byte_size, read_buffer, 0);
		
		/* now fetch */
		outputBytes = destBufferSize;

		outputBytes = AlacDecodeUtils.decode_frame(ac.alac, read_buffer, pDestBuffer, outputBytes);
		
		ac.current_sample_block = ac.current_sample_block + 1;
		outputBytes -= ac.offset * AlacGetBytesPerSample(ac);
        System.arraycopy(pDestBuffer, ac.offset, pDestBuffer, 0, outputBytes);
        ac.offset = 0;
        return outputBytes;
	
	}
	

	// Returns the sample rate of the specified ALAC file

    public static int AlacGetSampleRate(AlacContext ac)
    {
        if ( null != ac && ac.demux_res.sample_rate != 0)
        {
            return ac.demux_res.sample_rate;
        }
        else
        {
            return (44100);
        }
    }
	
	public static int AlacGetNumChannels(AlacContext ac)
    {
        if ( null != ac && ac.demux_res.num_channels != 0)
        {
            return ac.demux_res.num_channels;
        }
        else
        {
            return 2;
        }
    }
	
	public static int AlacGetBitsPerSample(AlacContext ac)
    {
        if (null != ac && ac.demux_res.sample_size != 0)
        {
            return ac.demux_res.sample_size;
        }
        else
        {
            return 16;
        }
    }
	

	public static int AlacGetBytesPerSample(AlacContext ac)
    {
        if ( null != ac && ac.demux_res.sample_size != 0)
        {
            return (int)Math.ceil(ac.demux_res.sample_size/8);
        }
        else
        {
            return 2;
        }
    }
	
	
	// Get total number of samples contained in the Apple Lossless file, or -1 if unknown

    public static int AlacGetNumSamples(AlacContext ac)
    {
		/* calculate output size */
		int num_samples = 0;
		int thissample_duration;
		int thissample_bytesize = 0;
		SampleDuration sampleinfo = new SampleDuration();
		int i;
		boolean error_found = false;
		int retval = 0;
			
		for (i = 0; i < ac.demux_res.sample_byte_size.length; i++)
		{
			thissample_duration = 0;
			thissample_bytesize = 0;

			retval = get_sample_info(ac.demux_res, i, sampleinfo);
			
			if(retval == 0)
			{
				return (-1);
			}
			thissample_duration = sampleinfo.sample_duration;
			thissample_bytesize = sampleinfo.sample_byte_size;

			num_samples += thissample_duration;
		}
		
		return (num_samples);
	}
	

	static int get_sample_info(DemuxResT demux_res, int samplenum, SampleDuration sampleinfo)
	{
		int duration_index_accum = 0;
		int duration_cur_index = 0;

		if (samplenum >= demux_res.sample_byte_size.length)
		{
			System.err.println("sample " + samplenum + " does not exist ");
			return 0;
		}

		if (demux_res.num_time_to_samples == 0)		// was null
		{
			System.err.println("no time to samples");
			return 0;
		}
		while ((demux_res.time_to_sample[duration_cur_index].sample_count + duration_index_accum) <= samplenum)
		{
			duration_index_accum += demux_res.time_to_sample[duration_cur_index].sample_count;
			duration_cur_index++;
			if (duration_cur_index >= demux_res.num_time_to_samples)
			{
				System.err.println("sample " + samplenum + " does not have a duration");
				return 0;
			}
		}

		sampleinfo.sample_duration = demux_res.time_to_sample[duration_cur_index].sample_duration;
		sampleinfo.sample_byte_size = demux_res.sample_byte_size[samplenum];

		return 1;
	}

    /**
     * sets position in pcm samples
     * @param ac alac context
     * @param position position in pcm samples to go to
     */

    public static void AlacSetPosition(AlacContext ac, long position) {
        DemuxResT res = ac.demux_res;

        int current_position = 0;
        int current_sample = 0;
        SampleDuration sample_info = new SampleDuration();
        for (int i = 0; i < res.stsc.length; i++) {
            ChunkInfo chunkInfo = res.stsc[i];
            int last_chunk;

            if (i < res.stsc.length - 1) {
                last_chunk = res.stsc[i + 1].first_chunk;
            } else {
                last_chunk = res.stco.length;
            }

            for (int chunk = chunkInfo.first_chunk; chunk <= last_chunk; chunk++) {
                int pos = res.stco[chunk - 1];
                int sample_count = chunkInfo.samples_per_chunk;
                while (sample_count > 0) {
                    int ret = get_sample_info(res, current_sample, sample_info);
                    if (ret == 0) return;
                    current_position += sample_info.sample_duration;
                    if (position < current_position) {
                        ac.input_stream.seek(pos);
                        ac.current_sample_block = current_sample;
                        ac.offset =
                                (int) (position - (current_position - sample_info.sample_duration))
                                        * AlacGetNumChannels(ac);
                        return;
                    }
                    pos += sample_info.sample_byte_size;
                    current_sample++;
                    sample_count--;
                }
            }
        }
    }
}