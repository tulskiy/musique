/*
 *  21.04.2004 Original verion. davagin@udm.ru.
 *-----------------------------------------------------------------------
 *  This program is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation; either version 2 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program; if not, write to the Free Software
 *  Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
 *----------------------------------------------------------------------
 */
package davaguine.jmac.encoder;

import davaguine.jmac.info.CompressionLevel;
import davaguine.jmac.info.InputSource;
import davaguine.jmac.info.WaveFormat;
import davaguine.jmac.tools.ByteBuffer;
import davaguine.jmac.tools.File;
import davaguine.jmac.tools.IntegerPointer;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 07.05.2004
 * Time: 13:10:46
 */
public abstract class IAPECompress {
    public final static int CREATE_WAV_HEADER_ON_DECOMPRESSION = -1;
    public final static int MAX_AUDIO_BYTES_UNKNOWN = -1;

    /**
     * ******************************************************************************************
     * Start
     * *******************************************************************************************
     */

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Start(...) / StartEx(...) - starts encoding
    //
    // Parameters:
    //	CIO * pioOutput / const wchar_t * pFilename
    //		the output... either a filename or an I/O source
    //	WAVEFORMATEX * pwfeInput
    //		format of the audio to encode (use FillWaveFormatEx() if necessary)
    //	int nMaxAudioBytes
    //		the absolute maximum audio bytes that will be encoded... encoding fails with a
    //		ERROR_APE_COMPRESS_TOO_MUCH_DATA if you attempt to encode more than specified here
    //		(if unknown, use MAX_AUDIO_BYTES_UNKNOWN to allocate as much storage in the seek table as
    //		possible... limit is then 2 GB of data (~4 hours of CD music)... this wastes around
    //		30kb, so only do it if completely necessary)
    //	int nCompressionLevel
    //		the compression level for the APE file (fast - extra high)
    //		(note: extra-high is much slower for little gain)
    //	const unsigned char * pHeaderData
    //		a pointer to a buffer containing the WAV header (data before the data block in the WAV)
    //		(note: use NULL for on-the-fly encoding... see next parameter)
    //	int nHeaderBytes
    //		number of bytes in the header data buffer (use CREATE_WAV_HEADER_ON_DECOMPRESSION and
    //		NULL for the pHeaderData and MAC will automatically create the appropriate WAV header
    //		on decompression)
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void Start(String pOutputFilename, WaveFormat pwfeInput) throws IOException {
        Start(pOutputFilename, pwfeInput, MAX_AUDIO_BYTES_UNKNOWN, CompressionLevel.COMPRESSION_LEVEL_NORMAL, null, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void Start(String pOutputFilename, WaveFormat pwfeInput, int nMaxAudioBytes) throws IOException {
        Start(pOutputFilename, pwfeInput, nMaxAudioBytes, CompressionLevel.COMPRESSION_LEVEL_NORMAL, null, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void Start(String pOutputFilename, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel) throws IOException {
        Start(pOutputFilename, pwfeInput, nMaxAudioBytes, nCompressionLevel, null, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void Start(String pOutputFilename, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData) throws IOException {
        Start(pOutputFilename, pwfeInput, nMaxAudioBytes, nCompressionLevel, pHeaderData, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public abstract void Start(String pOutputFilename, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData, int nHeaderBytes) throws IOException;

    public void StartEx(File pioOutput, WaveFormat pwfeInput) throws IOException {
        StartEx(pioOutput, pwfeInput, MAX_AUDIO_BYTES_UNKNOWN, CompressionLevel.COMPRESSION_LEVEL_NORMAL, null, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void StartEx(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes) throws IOException {
        StartEx(pioOutput, pwfeInput, nMaxAudioBytes, CompressionLevel.COMPRESSION_LEVEL_NORMAL, null, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void StartEx(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel) throws IOException {
        StartEx(pioOutput, pwfeInput, nMaxAudioBytes, nCompressionLevel, null, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public void StartEx(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData) throws IOException {
        StartEx(pioOutput, pwfeInput, nMaxAudioBytes, nCompressionLevel, pHeaderData, CREATE_WAV_HEADER_ON_DECOMPRESSION);
    }

    public abstract void StartEx(File pioOutput, WaveFormat pwfeInput, int nMaxAudioBytes, int nCompressionLevel, byte[] pHeaderData, int nHeaderBytes) throws IOException;

    /**
     * ******************************************************************************************
     * Add / Compress Data
     * - there are 3 ways to add data:
     * 1) simple call AddData(...)
     * 2) lock MAC's buffer, copy into it, and unlock (LockBuffer(...) / UnlockBuffer(...))
     * 3) from an I/O source (AddDataFromInputSource(...))
     * *******************************************************************************************
     */

    //////////////////////////////////////////////////////////////////////////////////////////////
    // AddData(...) - adds data to the encoder
    //
    // Parameters:
    //	unsigned char * pData
    //		a pointer to a buffer containing the raw audio data
    //	int nBytes
    //		the number of bytes in the buffer
    //////////////////////////////////////////////////////////////////////////////////////////////
    public abstract void AddData(byte[] pData, int nBytes) throws IOException;

    //////////////////////////////////////////////////////////////////////////////////////////////
    // GetBufferBytesAvailable(...) - returns the number of bytes available in the buffer
    //	(helpful when locking)
    //////////////////////////////////////////////////////////////////////////////////////////////
    public abstract int GetBufferBytesAvailable();

    //////////////////////////////////////////////////////////////////////////////////////////////
    // LockBuffer(...) - locks MAC's buffer so we can copy into it
    //
    // Parameters:
    //	int * pBytesAvailable
    //		returns the number of bytes available in the buffer (DO NOT COPY MORE THAN THIS IN)
    //
    // Return:
    //	pointer to the buffer (add at that location)
    //////////////////////////////////////////////////////////////////////////////////////////////
    public abstract ByteBuffer LockBuffer(IntegerPointer pBytesAvailable);

    //////////////////////////////////////////////////////////////////////////////////////////////
    // UnlockBuffer(...) - releases the buffer
    //
    // Parameters:
    //	int nBytesAdded
    //		the number of bytes copied into the buffer
    //	BOOL bProcess
    //		whether MAC should process as much as possible of the buffer
    //////////////////////////////////////////////////////////////////////////////////////////////
    public void UnlockBuffer(int nBytesAdded) throws IOException {
        UnlockBuffer(nBytesAdded, true);
    }

    public abstract void UnlockBuffer(int nBytesAdded, boolean bProcess) throws IOException;

    //////////////////////////////////////////////////////////////////////////////////////////////
    // AddDataFromInputSource(...) - use a CInputSource (input source) to add data
    //
    // Parameters:
    //	CInputSource * pInputSource
    //		a pointer to the input source
    //	int nMaxBytes
    //		the maximum number of bytes to let MAC add (-1 if MAC can add any amount)
    //	int * pBytesAdded
    //		returns the number of bytes added from the I/O source
    //////////////////////////////////////////////////////////////////////////////////////////////
    public int AddDataFromInputSource(InputSource pInputSource) throws IOException {
        return AddDataFromInputSource(pInputSource, -1);
    }

    public abstract int AddDataFromInputSource(InputSource pInputSource, int nMaxBytes) throws IOException;

    /**
     * ******************************************************************************************
     * Finish / Kill
     * *******************************************************************************************
     */

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Finish(...) - ends encoding and finalizes the file
    //
    // Parameters:
    //	unsigned char * pTerminatingData
    //		a pointer to a buffer containing the information to place at the end of the APE file
    //		(comprised of the WAV terminating data (data after the data block in the WAV) followed
    //		by any tag information)
    //	int nTerminatingBytes
    //		number of bytes in the terminating data buffer
    //	int nWAVTerminatingBytes
    //		the number of bytes of the terminating data buffer that should be appended to a decoded
    //		WAV file (it's basically nTerminatingBytes - the bytes that make up the tag)
    //////////////////////////////////////////////////////////////////////////////////////////////
    public abstract void Finish(byte[] pTerminatingData, int nTerminatingBytes, int nWAVTerminatingBytes) throws IOException;

    //////////////////////////////////////////////////////////////////////////////////////////////
    // Kill(...) - stops encoding and deletes the output file
    // --- NOT CURRENTLY IMPLEMENTED ---
    //////////////////////////////////////////////////////////////////////////////////////////////
    public abstract void Kill();

    public static IAPECompress CreateIAPECompress() {
        return new APECompress();
    }

}
