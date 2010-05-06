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

import java.io.IOException;

/**
 * Author: Dmitry Vaguine
 * Date: 04.03.2004
 * Time: 14:51:31
 */
public class jmacp {
    private String fFilename = null;

    public static void main(String[] args) {
        int retval = 0;
        try {
            jmacp player = createInstance(args);
            if (player != null)
                player.play();
        } catch (Exception ex) {
            System.err.println(ex);
            ex.printStackTrace(System.err);
            retval = 1;
        }
        System.exit(retval);
    }

    static public jmacp createInstance(String[] args) {
        jmacp player = new jmacp();
        if (!player.parseArgs(args))
            player = null;
        return player;
    }

    private jmacp() {
    }

    public jmacp(String filename) {
        init(filename);
    }

    protected void init(String filename) {
        fFilename = filename;
    }

    protected boolean parseArgs(String[] args) {
        boolean parsed = false;
        if (args.length == 1) {
            init(args[0]);
            parsed = true;
        } else if (args.length == 2) {
            showUsage();
        } else {
            showUsage();
        }
        return parsed;
    }

    public void showUsage() {
        System.out.println("Usage: jmacp <filename>");
        System.out.println("");
        System.out.println(" e.g. : java davaguine.jmac.player.jmacp localfile.ape");
    }

    public void play() {
        try {
            System.out.println("playing " + fFilename + "...");
            AudioDevice dev = getAudioDevice();
            Player player = new Player(fFilename, dev);
            player.play();
        } catch (IOException ex) {
            throw new JMACPlayerException("Problem playing file " + fFilename, ex);
        } catch (Exception ex) {
            throw new JMACPlayerException("Problem playing file " + fFilename, ex);
        }
    }

    protected AudioDevice getAudioDevice() {
        return FactoryRegistry.systemRegistry().createAudioDevice();
    }

}
