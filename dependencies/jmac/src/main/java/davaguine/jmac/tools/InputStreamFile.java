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

package davaguine.jmac.tools;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;

/**
 * Author: Dmitry Vaguine
 * Date: 12.03.2004
 * Time: 13:35:13
 */
public class InputStreamFile extends File {
    private DataInputStream stream = null;
    private String name = null;

    public InputStreamFile(final URL url) throws IOException {
        this(url.openStream(), url.getPath());
    }

    public InputStreamFile(final InputStream stream) {
        this(stream, null);
    }

    public InputStreamFile(final InputStream stream, final String name) {
        this.stream = new DataInputStream(new BufferedInputStream(stream));
        this.name = name;
    }

    public void mark(int readlimit) throws IOException {
        stream.mark(readlimit);
    }

    public void reset() throws IOException {
        stream.reset();
    }

    public int read() throws IOException {
        return stream.read();
    }

    public void readFully(byte[] b) throws IOException {
        stream.readFully(b);
    }

    public void readFully(byte[] b, int offs, int len) throws IOException {
        stream.readFully(b, offs, len);
    }

    public int read(byte[] b) throws IOException {
        return stream.read(b);
    }

    public int read(byte[] b, int offs, int len) throws IOException {
        return stream.read(b, offs, len);
    }

    public void close() throws IOException {
        stream.close();
    }

    public boolean readBoolean() throws IOException {
        return stream.readBoolean();
    }

    public byte readByte() throws IOException {
        return stream.readByte();
    }

    public char readChar() throws IOException {
        return stream.readChar();
    }

    public double readDouble() throws IOException {
        return stream.readDouble();
    }

    public float readFloat() throws IOException {
        return stream.readFloat();
    }

    public int readInt() throws IOException {
        return stream.readInt();
    }

    public String readLine() throws IOException {
        StringBuffer input = new StringBuffer();
        int c = -1;
        boolean eol = false;

        while (!eol) {
            switch (c = read()) {
                case -1:
                case '\n':
                    eol = true;
                    break;
                case '\r':
                    eol = true;
                    mark(1);
                    if ((read()) != '\n')
                        reset();
                    break;
                default:
                    input.append((char) c);
                    break;
            }
        }

        if ((c == -1) && (input.length() == 0))
            return null;

        return input.toString();
    }

    public long readLong() throws IOException {
        return stream.readLong();
    }

    public short readShort() throws IOException {
        return stream.readShort();
    }

    public int readUnsignedByte() throws IOException {
        return stream.readUnsignedByte();
    }

    public int readUnsignedShort() throws IOException {
        return stream.readUnsignedShort();
    }

    public String readUTF() throws IOException {
        return stream.readUTF();
    }

    public int skipBytes(int n) throws IOException {
        return stream.skipBytes(n);
    }

    public long length() throws IOException {
        throw new JMACException("Unsupported Method");
    }

    public void seek(long pos) throws IOException {
        throw new JMACException("Unsupported Method");
    }

    public long getFilePointer() throws IOException {
        throw new JMACException("Unsupported Method");
    }

    public void setLength(long newLength) throws IOException {
        throw new JMACException("Unsupported Method");
    }

    public void write(byte[] b, int off, int len) throws IOException {
        throw new JMACException("Unsupported Method");
    }

    public boolean isLocal() {
        return stream == null;
    }

    public String getFilename() {
        return name;
    }
}
