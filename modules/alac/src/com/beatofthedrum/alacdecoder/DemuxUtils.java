/*
** DemuxUtils.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

package com.beatofthedrum.alacdecoder;


class DemuxUtils
{
	public static int MakeFourCC(int ch0, int ch1, int ch2, int ch3)
	{
		return ( ((ch0) << 24)| ((ch1) << 16)| ((ch2) << 8) | ((ch3)) );
	}

	public static int MakeFourCC32(int ch0, int ch1, int ch2, int ch3)
	{
		int retval = 0;
		int tmp = ch0;

		retval = tmp << 24;

		tmp = ch1;

		retval = retval | (tmp << 16);
		tmp = ch2;

		retval = retval | (tmp << 8);
		tmp = ch3;

		retval = retval | tmp;

		return (retval);
	}
	
	public static String SplitFourCC(int code)
	{
		String retstr;
		char c1;
		char c2;
		char c3;
		char c4;
		
		c1 = (char)((code >> 24) & 0xFF); 
		c2 = (char)((code >> 16) & 0xFF); 
		c3 = (char)((code >> 8) & 0xFF); 
		c4 = (char)(code & 0xFF); 
		retstr = c1 + " " + c2 + " " + c3 + " " + c4;		
		
		return retstr;
		
	}


	public static int qtmovie_read(java.io.DataInputStream file, QTMovieT qtmovie, DemuxResT demux_res)
	{
		int found_moov = 0;
		int found_mdat = 0;

		/* construct the stream */
		qtmovie.qtstream.stream = file;

		qtmovie.res = demux_res;

		// reset demux_res	TODO

		/* read the chunks */
		while (true)
		{
			int chunk_len;
			int chunk_id = 0;

			try
			{
				chunk_len = StreamUtils.stream_read_uint32(qtmovie.qtstream);
			}
			catch (Exception e)
			{
				System.err.println("(top) error reading chunk_len - possibly number too large");
				chunk_len = 1;
			}
			
			if (StreamUtils.stream_eof(qtmovie.qtstream) != 0)
			{
				return 0;
			}

			if (chunk_len == 1)
			{
				System.err.println("need 64bit support");
				return 0;
			}
			chunk_id = StreamUtils.stream_read_uint32(qtmovie.qtstream);

			if(chunk_id == MakeFourCC32(102,116,121,112))	// fourcc equals ftyp
			{
				read_chunk_ftyp(qtmovie, chunk_len);
			}
			else if(chunk_id == MakeFourCC32(109,111,111,118) )	// fourcc equals moov
			{
				if (read_chunk_moov(qtmovie, chunk_len) == 0)
					return 0; // failed to read moov, can't do anything
				if (found_mdat != 0)
				{
					return set_saved_mdat(qtmovie);
				}
				found_moov = 1;
			}
				/* if we hit mdat before we've found moov, record the position
				 * and move on. We can then come back to mdat later.
				 * This presumes the stream supports seeking backwards.
				 */
			else if(chunk_id == MakeFourCC32(109,100,97,116))	// fourcc equals mdat
			{
				int not_found_moov = 0;
				if(found_moov==0)
					not_found_moov = 1;
				read_chunk_mdat(qtmovie, chunk_len, not_found_moov);
				if (found_moov != 0)
				{
					return 1;
				}
				found_mdat = 1;
			}
				/*  these following atoms can be skipped !!!! */
			else if(chunk_id ==  MakeFourCC32(102,114,101,101))	// fourcc equals free
			{
				StreamUtils.stream_skip(qtmovie.qtstream, chunk_len - 8); // FIXME not 8
			}
			else
			{
				System.err.println("(top) unknown chunk id: " + SplitFourCC(chunk_id));
				return 0;
			}
		}
	}


	/* chunk handlers */
	static void read_chunk_ftyp(QTMovieT qtmovie, int chunk_len)
	{
		int type = 0;
		int minor_ver = 0;
		int size_remaining = chunk_len - 8; // FIXME: can't hardcode 8, size may be 64bit

		type = StreamUtils.stream_read_uint32(qtmovie.qtstream);
		size_remaining-=4;

		if(type != MakeFourCC32(77,52,65,32) )		// "M4A " ascii values
		{
			System.err.println("not M4A file");
			return;
		}
		minor_ver = StreamUtils.stream_read_uint32(qtmovie.qtstream);
		size_remaining-=4;

		/* compatible brands */
		while (size_remaining != 0)
		{
			/* unused */
			/*fourcc_t cbrand =*/
			StreamUtils.stream_read_uint32(qtmovie.qtstream);
			size_remaining-=4;
		}
	}

	static void read_chunk_tkhd(QTMovieT qtmovie, int chunk_len)
	{
		/* don't need anything from here atm, skip */
		int size_remaining = chunk_len - 8; // FIXME WRONG

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	static void read_chunk_mdhd(QTMovieT qtmovie, int chunk_len)
	{
		/* don't need anything from here atm, skip */
		int size_remaining = chunk_len - 8; // FIXME WRONG

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	static void read_chunk_edts(QTMovieT qtmovie, int chunk_len)
	{
		/* don't need anything from here atm, skip */
		int size_remaining = chunk_len - 8; // FIXME WRONG

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	static void read_chunk_elst(QTMovieT qtmovie, int chunk_len)
	{
		/* don't need anything from here atm, skip */
		int size_remaining = chunk_len - 8; // FIXME WRONG

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	/* media handler inside mdia */
	static void read_chunk_hdlr(QTMovieT qtmovie, int chunk_len)
	{
		int comptype = 0;
		int compsubtype = 0;
		int size_remaining = chunk_len - 8; // FIXME WRONG

		int strlen;

		/* version */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 1;
		/* flags */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 3;

		/* component type */
		comptype = StreamUtils.stream_read_uint32(qtmovie.qtstream);
		compsubtype = StreamUtils.stream_read_uint32(qtmovie.qtstream);
		size_remaining -= 8;

		/* component manufacturer */
		StreamUtils.stream_read_uint32(qtmovie.qtstream);
		size_remaining -= 4;

		/* flags */
		StreamUtils.stream_read_uint32(qtmovie.qtstream);
		StreamUtils.stream_read_uint32(qtmovie.qtstream);
		size_remaining -= 8;

		/* name */
		strlen = StreamUtils.stream_read_uint8(qtmovie.qtstream);

		/* 
		** rewrote this to handle case where we actually read more than required 
		** so here we work out how much we need to read first
		*/

		size_remaining -= 1;

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	static int read_chunk_stsd(QTMovieT qtmovie, int chunk_len)
	{
		int i;
		int numentries = 0;
		int size_remaining = chunk_len - 8; // FIXME WRONG

		/* version */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 1;
		/* flags */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 3;

		try
		{
			numentries = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
		}
		catch (Exception e)
		{
			System.err.println("(read_chunk_stsd) error reading numentries - possibly number too large");
			numentries = 0;
		}		
		

		size_remaining -= 4;

		if (numentries != 1)
		{
			System.err.println("only expecting one entry in sample description atom!");
			return 0;
		}

		for (i = 0; i < numentries; i++)
		{
			int entry_size;
			int version;

			int entry_remaining;

			entry_size = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			qtmovie.res.format = StreamUtils.stream_read_uint32(qtmovie.qtstream);
			entry_remaining = entry_size;
			entry_remaining -= 8;

			/* sound info: */

			StreamUtils.stream_skip(qtmovie.qtstream, 6); // reserved
			entry_remaining -= 6;

			version = StreamUtils.stream_read_uint16(qtmovie.qtstream);

			if (version != 1)
				System.err.println("unknown version??");
			entry_remaining -= 2;

			/* revision level */
			StreamUtils.stream_read_uint16(qtmovie.qtstream);
			/* vendor */
			StreamUtils.stream_read_uint32(qtmovie.qtstream);
			entry_remaining -= 6;

			/* EH?? spec doesn't say theres an extra 16 bits here.. but there is! */
			StreamUtils.stream_read_uint16(qtmovie.qtstream);
			entry_remaining -= 2;

			qtmovie.res.num_channels = StreamUtils.stream_read_uint16(qtmovie.qtstream);

			qtmovie.res.sample_size = StreamUtils.stream_read_uint16(qtmovie.qtstream);
			entry_remaining -= 4;

			/* compression id */
			StreamUtils.stream_read_uint16(qtmovie.qtstream);
			/* packet size */
			StreamUtils.stream_read_uint16(qtmovie.qtstream);
			entry_remaining -= 4;

			/* sample rate - 32bit fixed point = 16bit?? */
			qtmovie.res.sample_rate = StreamUtils.stream_read_uint16(qtmovie.qtstream);
			entry_remaining -= 2;

			/* skip 2 */
			StreamUtils.stream_skip(qtmovie.qtstream, 2);
			entry_remaining -= 2;

			/* remaining is codec data */

			/* 12 = audio format atom, 8 = padding */
			qtmovie.res.codecdata_len = entry_remaining + 12 + 8;

			for (int count = 0; count < qtmovie.res.codecdata_len; count++)
			{
				qtmovie.res.codecdata[count] = 0;
			}

			/* audio format atom */
			qtmovie.res.codecdata[0] = 0x0c000000;
			qtmovie.res.codecdata[1] = MakeFourCC(97,109,114,102);		// "amrf" ascii values
			qtmovie.res.codecdata[2] = MakeFourCC(99,97,108,97);		// "cala" ascii values

			StreamUtils.stream_read(qtmovie.qtstream, entry_remaining, qtmovie.res.codecdata, 12);	// codecdata buffer should be +12
			entry_remaining -= entry_remaining;

			if (entry_remaining != 0)	// was comparing to null
				StreamUtils.stream_skip(qtmovie.qtstream, entry_remaining);

			qtmovie.res.format_read = 1;
			if(qtmovie.res.format != MakeFourCC32(97,108,97,99) )		// "alac" ascii values
			{
				return 0;
			}
		}

		return 1;
	}

	static void read_chunk_stts(QTMovieT qtmovie, int chunk_len)
	{
		int i;
		int numentries = 0;
		int size_remaining = chunk_len - 8; // FIXME WRONG

		/* version */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 1;
		/* flags */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 3;

		try
		{
			numentries = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
		}
		catch (Exception e)
		{
			System.err.println("(read_chunk_stts) error reading numentries - possibly number too large");
			numentries = 0;
		}

		size_remaining -= 4;

		qtmovie.res.num_time_to_samples = numentries;

		for (i = 0; i < numentries; i++)
		{
			qtmovie.res.time_to_sample[i].sample_count = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			qtmovie.res.time_to_sample[i].sample_duration = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			size_remaining -= 8;
		}

		if (size_remaining != 0)
		{
			System.err.println("(read_chunk_stts) size remaining?");
			StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
		}
	}

	static void read_chunk_stsz(QTMovieT qtmovie, int chunk_len)
	{
		int i;
		int numentries = 0;
		int uniform_size = 0;
		int size_remaining = chunk_len - 8; // FIXME WRONG

		/* version */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 1;
		/* flags */
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		StreamUtils.stream_read_uint8(qtmovie.qtstream);
		size_remaining -= 3;

		/* default sample size */
		uniform_size = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
		if (uniform_size != 0)
		{
			/*
			** Normally files have intiable sample sizes, this handles the case where
			** they are all the same size
			*/
	
			int uniform_num = 0;
			
			uniform_num = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			
			qtmovie.res.sample_byte_size = new int[uniform_num];
			
			for (i = 0; i < uniform_num; i++)
			{
				qtmovie.res.sample_byte_size[i] = uniform_size;
			}
			size_remaining -= 4;
			return;
		}
		size_remaining -= 4;

		try
		{
			numentries = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
		}
		catch (Exception e)
		{
			System.err.println("(read_chunk_stsz) error reading numentries - possibly number too large");
			numentries = 0;
		}

		size_remaining -= 4;

		qtmovie.res.sample_byte_size = new int[numentries];

		for (i = 0; i < numentries; i++)
		{
			qtmovie.res.sample_byte_size[i] = (StreamUtils.stream_read_uint32(qtmovie.qtstream));

			size_remaining -= 4;
		}

		if (size_remaining != 0)
		{
			System.err.println("(read_chunk_stsz) size remaining?");
			StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
		}
	}

	static int read_chunk_stbl(QTMovieT qtmovie, int chunk_len)
	{
		int size_remaining = chunk_len - 8; // FIXME WRONG

		while (size_remaining != 0)
		{
			int sub_chunk_len;
			int sub_chunk_id = 0;
			
			try
			{
				sub_chunk_len = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			}
			catch (Exception e)
			{
				System.err.println("(read_chunk_stbl) error reading sub_chunk_len - possibly number too large");
				sub_chunk_len = 0;
			}

			if (sub_chunk_len <= 1 || sub_chunk_len > size_remaining)
			{
				System.err.println("strange size for chunk inside stbl " + sub_chunk_len + " (remaining: " + size_remaining + ")");
				return 0;
			}

			sub_chunk_id = StreamUtils.stream_read_uint32(qtmovie.qtstream);

			if(sub_chunk_id ==  MakeFourCC32(115,116,115,100) )	// fourcc equals stsd
			{
				if (read_chunk_stsd(qtmovie, sub_chunk_len) == 0)
					return 0;
			}
			else if(sub_chunk_id ==  MakeFourCC32(115,116,116,115) )	// fourcc equals stts
			{
				read_chunk_stts(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(115,116,115,122) )	// fourcc equals stsz
			{
				read_chunk_stsz(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(115,116,115,99) )	// fourcc equals stsc
			{
				read_chunk_stsc(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(115,116,99,111) )	// fourcc equals stco
			{
				read_chunk_stco(qtmovie, sub_chunk_len);
			}
			else
			{
				System.err.println("(stbl) unknown chunk id: " + SplitFourCC(sub_chunk_id));
				return 0;
			}

			size_remaining -= sub_chunk_len;
		}

		return 1;
	}

    /*
     * chunk to offset box
     */
    private static void read_chunk_stco(QTMovieT qtmovie, int sub_chunk_len) {
        //skip header and size
        MyStream stream = qtmovie.qtstream;
        StreamUtils.stream_skip(stream, 4);

        int num_entries = StreamUtils.stream_read_uint32(stream);

        qtmovie.res.stco = new int[num_entries];
        for (int i = 0; i < num_entries; i++) {
            qtmovie.res.stco[i] = StreamUtils.stream_read_uint32(stream);
        }
    }

    /*
     * sample to chunk box
     */
    private static void read_chunk_stsc(QTMovieT qtmovie, int sub_chunk_len) {
        //skip header and size
        MyStream stream = qtmovie.qtstream;
        //skip version and other junk
        StreamUtils.stream_skip(stream, 4);
        int num_entries = StreamUtils.stream_read_uint32(stream);
        qtmovie.res.stsc = new ChunkInfo[num_entries];
        for (int i = 0; i < num_entries; i++) {
            ChunkInfo entry = new ChunkInfo();
            entry.first_chunk = StreamUtils.stream_read_uint32(stream);
            entry.samples_per_chunk = StreamUtils.stream_read_uint32(stream);
            entry.sample_desc_index = StreamUtils.stream_read_uint32(stream);
            qtmovie.res.stsc[i] = entry;
        }
    }

    static int read_chunk_minf(QTMovieT qtmovie, int chunk_len)
	{
		int dinf_size;
		int stbl_size;
		int size_remaining = chunk_len - 8; // FIXME WRONG
		int media_info_size;

	  /**** SOUND HEADER CHUNK ****/
	  
	  	try
		{
			media_info_size = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
		}
		catch (Exception e)
		{
			System.err.println("(read_chunk_minf) error reading media_info_size - possibly number too large");
			media_info_size = 0;
		}
				
		if (media_info_size != 16)
		{
			System.err.println("unexpected size in media info\n");
			return 0;
		}
		if (StreamUtils.stream_read_uint32(qtmovie.qtstream) != MakeFourCC32(115,109,104,100))	// "smhd" ascii values
		{
			System.err.println("not a sound header! can't handle this.");
			return 0;
		}
		/* now skip the rest */
		StreamUtils.stream_skip(qtmovie.qtstream, 16 - 8);
		size_remaining -= 16;
	  /****/

	  /**** DINF CHUNK ****/

	  	try
		{
			dinf_size = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
		}
		catch (Exception e)
		{
			System.err.println("(read_chunk_minf) error reading dinf_size - possibly number too large");
			dinf_size = 0;
		}	  

		if (StreamUtils.stream_read_uint32(qtmovie.qtstream) != MakeFourCC32(100,105,110,102))	// "dinf" ascii values
		{
			System.err.println("expected dinf, didn't get it.");
			return 0;
		}
		/* skip it */
		StreamUtils.stream_skip(qtmovie.qtstream, dinf_size - 8);
		size_remaining -= dinf_size;
	  /****/


	  /**** SAMPLE TABLE ****/
	  	try
		{
			stbl_size = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
		}
		catch (Exception e)
		{
			System.err.println("(read_chunk_minf) error reading stbl_size - possibly number too large");
			stbl_size = 0;
		}	
		
		if (StreamUtils.stream_read_uint32(qtmovie.qtstream) != MakeFourCC32(115,116,98,108))	// "stbl" ascii values
		{
			System.err.println("expected stbl, didn't get it.");
			return 0;
		}
		if (read_chunk_stbl(qtmovie, stbl_size) == 0)
			return 0;
		size_remaining -= stbl_size;

		if (size_remaining != 0)
		{
			System.err.println("(read_chunk_minf) - size remaining?");
			StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
		}

		return 1;
	}

	static int read_chunk_mdia(QTMovieT qtmovie, int chunk_len)
	{
		int size_remaining = chunk_len - 8; // FIXME WRONG

		while (size_remaining != 0)
		{
			int sub_chunk_len;
			int sub_chunk_id  = 0;

			try
			{
				sub_chunk_len = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			}
			catch (Exception e)
			{
				System.err.println("(read_chunk_mdia) error reading sub_chunk_len - possibly number too large");
				sub_chunk_len = 0;
			}			

			if (sub_chunk_len <= 1 || sub_chunk_len > size_remaining)
			{
				System.err.println("strange size for chunk inside mdia\n");
				return 0;
			}

			sub_chunk_id = StreamUtils.stream_read_uint32(qtmovie.qtstream);

			if(sub_chunk_id ==  MakeFourCC32(109,100,104,100) )	// fourcc equals mdhd
			{
				read_chunk_mdhd(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(104,100,108,114) )	// fourcc equals hdlr
			{
				read_chunk_hdlr(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(109,105,110,102) )	// fourcc equals minf
			{
				if (read_chunk_minf(qtmovie, sub_chunk_len) == 0)
					return 0;
			}
			else
			{
				System.err.println("(mdia) unknown chunk id: " + SplitFourCC(sub_chunk_id));
				return 0;
			}

			size_remaining -= sub_chunk_len;
		}

		return 1;
	}

	/* 'trak' - a movie track - contains other atoms */
	static int read_chunk_trak(QTMovieT qtmovie, int chunk_len)
	{
		int size_remaining = chunk_len - 8; // FIXME WRONG

		while (size_remaining != 0)
		{
			int sub_chunk_len;
			int sub_chunk_id = 0;

			try
			{
				sub_chunk_len = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			}
			catch (Exception e)
			{
				System.err.println("(read_chunk_trak) error reading sub_chunk_len - possibly number too large");
				sub_chunk_len = 0;
			}			

			if (sub_chunk_len <= 1 || sub_chunk_len > size_remaining)
			{
				System.err.println("strange size for chunk inside trak");
				return 0;
			}

			sub_chunk_id = StreamUtils.stream_read_uint32(qtmovie.qtstream);

			if(sub_chunk_id ==  MakeFourCC32(116,107,104,100) )	// fourcc equals tkhd
			{
				read_chunk_tkhd(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(109,100,105,97) )	// fourcc equals mdia
			{
				if (read_chunk_mdia(qtmovie, sub_chunk_len) == 0)
					return 0;
			}
			else if(sub_chunk_id ==  MakeFourCC32(101,100,116,115) )	// fourcc equals edts
			{
				read_chunk_edts(qtmovie, sub_chunk_len);
			}
			else
			{
				System.err.println("(trak) unknown chunk id: " + SplitFourCC(sub_chunk_id));
				return 0;
			}

			size_remaining -= sub_chunk_len;
		}

		return 1;
	}

	/* 'mvhd' movie header atom */
	static void read_chunk_mvhd(QTMovieT qtmovie, int chunk_len)
	{
		/* don't need anything from here atm, skip */
		int size_remaining = chunk_len - 8; // FIXME WRONG

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	/* 'udta' user data.. contains tag info */
	static void read_chunk_udta(QTMovieT qtmovie, int chunk_len)
	{
		/* don't need anything from here atm, skip */
		int size_remaining = chunk_len - 8; // FIXME WRONG

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	/* 'iods' */
	static void read_chunk_iods(QTMovieT qtmovie, int chunk_len)
	{
		/* don't need anything from here atm, skip */
		int size_remaining = chunk_len - 8; // FIXME WRONG

		StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
	}

	/* 'moov' movie atom - contains other atoms */
	static int read_chunk_moov(QTMovieT qtmovie, int chunk_len)
	{
		int size_remaining = chunk_len - 8; // FIXME WRONG

		while (size_remaining != 0)
		{
			int sub_chunk_len;
			int sub_chunk_id = 0;
			
			try
			{
				sub_chunk_len = (StreamUtils.stream_read_uint32(qtmovie.qtstream));
			}
			catch (Exception e)
			{
				System.err.println("(read_chunk_moov) error reading sub_chunk_len - possibly number too large");
				sub_chunk_len = 0;
			}			

			if (sub_chunk_len <= 1 || sub_chunk_len > size_remaining)
			{
				System.err.println("strange size for chunk inside moov");		
				return 0;
			}

			sub_chunk_id = StreamUtils.stream_read_uint32(qtmovie.qtstream);

			if(sub_chunk_id ==  MakeFourCC32(109,118,104,100) )	// fourcc equals mvhd
			{		
				read_chunk_mvhd(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(116,114,97,107) )	// fourcc equals trak
			{
				if (read_chunk_trak(qtmovie, sub_chunk_len) == 0)
					return 0;
			}
			else if(sub_chunk_id ==  MakeFourCC32(117,100,116,97) )	// fourcc equals udta
			{
				read_chunk_udta(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(101,108,115,116) )	// fourcc equals elst
			{
				read_chunk_elst(qtmovie, sub_chunk_len);
			}
			else if(sub_chunk_id ==  MakeFourCC32(105,111,100,115) )	// fourcc equals iods
			{
				read_chunk_iods(qtmovie, sub_chunk_len);
			}
			else
			{
				System.err.println("(moov) unknown chunk id: " + SplitFourCC(sub_chunk_id));
				return 0;
			}

			size_remaining -= sub_chunk_len;
		}

		return 1;
	}

	static void read_chunk_mdat(QTMovieT qtmovie, int chunk_len, int skip_mdat)
	{
		int size_remaining = chunk_len - 8; // FIXME WRONG

		if (size_remaining == 0)
			return;

		qtmovie.res.mdat_len = size_remaining;
		if (skip_mdat != 0)
		{
			qtmovie.saved_mdat_pos = StreamUtils.stream_tell(qtmovie.qtstream);

			StreamUtils.stream_skip(qtmovie.qtstream, size_remaining);
		}
	}

	static int set_saved_mdat(QTMovieT qtmovie)
	{
		// returns as follows
		// 1 - all ok
		// 2 - do not have valid saved mdat pos
		// 3 - have valid saved mdat pos, but cannot seek there - need to close/reopen stream

		if (qtmovie.saved_mdat_pos == -1)
		{
			System.err.println("stream contains mdat before moov but is not seekable");
			return 2;
		}

		if (StreamUtils.stream_setpos(qtmovie.qtstream, qtmovie.saved_mdat_pos) != 0)
		{
			return 3;
		}

		return 1;
	}
}


