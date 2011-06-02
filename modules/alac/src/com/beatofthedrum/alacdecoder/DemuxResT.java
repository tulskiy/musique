/*
** DemuxResT.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/
package com.beatofthedrum.alacdecoder;

class DemuxResT
{
	public int format_read;

	public int num_channels;
	public int sample_size;
	public int sample_rate;
	public int format;
	public int[] buf = new int[1024*80];

	public SampleInfo[] time_to_sample = new SampleInfo[16];
	public int num_time_to_samples;

	public int[] sample_byte_size;

	public int codecdata_len;

	public int[] codecdata = new int[1024];

    public int[] stco;
    public ChunkInfo[] stsc;

	public int mdat_len;
	
	public DemuxResT()
	{
		// not sure how many of these I need, so make 16
		for (int i = 0; i < 16; i++)
		{
			time_to_sample[i] = new SampleInfo();
		}
	}
}
