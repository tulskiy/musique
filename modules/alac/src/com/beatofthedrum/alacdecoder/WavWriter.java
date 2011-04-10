/*
** WavWriter.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/
package com.beatofthedrum.alacdecoder;

public class WavWriter
{
	static void write_uint32(java.io.FileOutputStream f, int v)
	{
		byte[] outputBytes = new byte[4];
		
		outputBytes[3] = (byte) (v >>> 24);
		outputBytes[2] = (byte) (v >>> 16);
		outputBytes[1] = (byte) (v >>> 8);
		outputBytes[0] = (byte) (v);
		try
		{
			f.write(outputBytes, 0, 4 );
		}
		catch(java.io.IOException ioe)
		{
		}
	}

	static void write_uint16(java.io.FileOutputStream f, int v)
	{
		byte[] outputBytes = new byte[2];
		
		outputBytes[1] = (byte) (v >>> 8);
		outputBytes[0] = (byte) (v);
		try
		{
			f.write(outputBytes, 0, 2 );
		}
		catch(java.io.IOException ioe)
		{
		}
	}

	public static void wavwriter_writeheaders(java.io.FileOutputStream f, int datasize, int numchannels, int samplerate, int bytespersample, int bitspersample)
	{
		byte[] buffAsBytes = new byte[4];

		/* write RIFF header */
		buffAsBytes[0]=82;
		buffAsBytes[1]=73;
		buffAsBytes[2]=70;
		buffAsBytes[3]=70; 	// "RIFF" ascii values

		try
		{
			f.write(buffAsBytes, 0, 4 );
		}
		catch(java.io.IOException ioe)
		{
		}

		write_uint32(f, (36 + datasize));
		buffAsBytes[0]=87;
		buffAsBytes[1]=65;
		buffAsBytes[2]=86;
		buffAsBytes[3]=69;   // "WAVE" ascii values

		try
		{
			f.write(buffAsBytes, 0, 4 );
		}
		catch(java.io.IOException ioe)
		{
		}

		/* write fmt header */
		buffAsBytes[0]=102;
		buffAsBytes[1]=109;
		buffAsBytes[2]=116;
		buffAsBytes[3]=32;  // "fmt " ascii values

		try
		{
			f.write(buffAsBytes, 0, 4 );
		}
		catch(java.io.IOException ioe)
		{
		}
		
		write_uint32(f, 16);
		write_uint16(f, 1); // PCM data
		write_uint16(f, numchannels);
		write_uint32(f, samplerate);
		write_uint32(f, (samplerate * numchannels * bytespersample)); // byterate
		write_uint16(f, (numchannels * bytespersample ));
		write_uint16(f, bitspersample);

		/* write data header */
		buffAsBytes[0]=100;
		buffAsBytes[1]=97;
		buffAsBytes[2]=116;
		buffAsBytes[3]=97;  // "data" ascii values

		try
		{
			f.write(buffAsBytes, 0, 4 );
		}
		catch(java.io.IOException ioe)
		{
		}
		
		write_uint32(f, datasize);
	}
}

