package net.sourceforge.jaad.mp4.boxes.impl.meta;

import net.sourceforge.jaad.mp4.boxes.BoxTypes;
import net.sourceforge.jaad.mp4.boxes.FullContainerBox;

/**
 * Author: Denis Tulskiy
 * Date: 4/3/11
 */
public class Mp4TagBox extends FullContainerBox {
    protected Mp4TagBox(String name, String shortName) {
        super(name, shortName);
    }

    public String getKey() {
        NameBox name = (NameBox) getChild(BoxTypes.NAME_BOX);
        if (name != null) {
            return name.getData();
        } else {
            return getName();
        }
    }

    public String getValue() {
        DataBox child = (DataBox) getChild(BoxTypes.DATA_BOX);
        if (child != null)
            return child.getData();
        return null;
    }
}
