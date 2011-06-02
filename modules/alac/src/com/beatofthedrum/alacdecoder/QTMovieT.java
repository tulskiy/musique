/*
** QTMovieT.java
**
** Copyright (c) 2011 Peter McQuillan
**
** All Rights Reserved.
**                       
** Distributed under the BSD Software License (see license.txt)  
**
*/

package com.beatofthedrum.alacdecoder;

class QTMovieT
{
	public MyStream qtstream;
	public DemuxResT res;
	public int saved_mdat_pos;

	public QTMovieT()
	{
		saved_mdat_pos = 0;
		qtstream = new MyStream();
	}
}