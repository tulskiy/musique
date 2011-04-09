package net.sourceforge.jaad.mp4.boxes.impl.meta;

import net.sourceforge.jaad.mp4.MP4InputStream;
import net.sourceforge.jaad.mp4.boxes.Box;
import net.sourceforge.jaad.mp4.boxes.FullContainerBox;

import java.io.IOException;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: 4/3/11
 */
public class IlstBox extends FullContainerBox {
    public IlstBox() {
        super("ilst", "ilst");
    }

    public List<Box> getChildren() {
        return children;
    }

    @Override
    public void decode(MP4InputStream in) throws IOException {
        readChildren(in);
    }
}
