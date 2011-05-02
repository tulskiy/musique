package net.sourceforge.jaad.mp4.boxes;

import net.sourceforge.jaad.mp4.MP4InputStream;

import java.io.IOException;

/**
 * Simple box consists of a name and a string for data
 *
 * Author: Denis Tulskiy
 * Date: 4/3/11
 */
public class SimpleBox extends FullBox {
    private String data;

    public SimpleBox(String name, String shortName) {
        super(name, shortName);
    }

    public String getData() {
        return data;
    }

    @Override
    public void decode(MP4InputStream in) throws IOException {
        super.decode(in);
        data = in.readString((int) left);
        left -= data.length();
    }
}
