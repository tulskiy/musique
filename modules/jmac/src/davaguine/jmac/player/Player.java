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

package davaguine.jmac.player;

import davaguine.jmac.decoder.IAPEDecompress;
import davaguine.jmac.tools.File;

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class Player {

    public final static int BLOCKS_PER_DECODE = 9216;

    /**
     * The MAC audio core.
     */
    /*final*/
    private File io;
    private IAPEDecompress decoder;

    /**
     * The AudioDevice the audio samples are written to.
     */
    private AudioDevice audio;

    /**
     * Has the player been closed?
     */
    private boolean closed = false;

    /**
     * Has the player played back all blocks from the stream?
     */
    private boolean complete = false;

    private int lastPosition = 0;

    /**
     * Creates a new <code>Player</code> instance.
     */
    public Player(String file) throws IOException {
        this(file, null);
    }

    public Player(String file, AudioDevice device) throws IOException {
        io = File.createFile(file, "r");
        decoder = IAPEDecompress.CreateIAPEDecompress(io);

        if (device != null) {
            audio = device;
        } else {
            FactoryRegistry r = FactoryRegistry.systemRegistry();
            audio = r.createAudioDevice();
        }
        audio.open(decoder);
    }

    public IAPEDecompress getDecoder() {
        return decoder;
    }

    /**
     * Plays all MAC audio blocks.
     *
     * @return	true if the last block was played, or false if there are
     * more blocks.
     */
    public boolean play() throws IOException {

        int nBlocksLeft = decoder.getApeInfoDecompressTotalBlocks();
        int blockAlign = decoder.getApeInfoBlockAlign();

        // allocate space for decompression
        byte[] spTempBuffer = new byte[blockAlign * BLOCKS_PER_DECODE];

        while (nBlocksLeft > 0) {
            int nBlocksDecoded = decoder.GetData(spTempBuffer, BLOCKS_PER_DECODE);

            // update amount remaining
            nBlocksLeft -= nBlocksDecoded;

            synchronized (this) {
                if (audio != null) {
                    audio.write(spTempBuffer, 0, nBlocksDecoded * blockAlign);
                }
            }
        }

        // last block, ensure all data flushed to the audio device.
        AudioDevice out = audio;
        if (out != null) {
            out.flush();
            synchronized (this) {
                complete = (!closed);
                close();
            }
        }
        return true;
    }

    /**
     * Cloases this player. Any audio currently playing is stopped
     * immediately.
     */
    public synchronized void close() throws IOException {
        AudioDevice out = audio;
        if (out != null) {
            closed = true;
            audio = null;
            // this may fail, so ensure object state is set up before
            // calling this method.
            out.close();
            lastPosition = out.getPosition();
            io.close();
            io = null;
            decoder = null;
        }
    }

    /**
     * Returns the completed status of this player.
     *
     * @return	true if all available MAC audio blocks have been
     * decoded, or false otherwise.
     */
    public synchronized boolean isComplete() {
        return complete;
    }

    /**
     * Retrieves the position in milliseconds of the current audio
     * sample being played. This method delegates to the <code>
     * AudioDevice</code> that is used by this player to sound
     * the decoded audio samples.
     */
    public int getPosition() {
        int position = lastPosition;

        AudioDevice out = audio;
        if (out != null) {
            position = out.getPosition();
        }
        return position;
    }

}
