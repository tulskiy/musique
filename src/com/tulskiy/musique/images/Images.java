/*
 * Copyright (c) 2008, 2009, 2010 Denis Tulskiy
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * version 3 along with this work.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.tulskiy.musique.images;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.net.URL;

/**
 * Author: Denis Tulskiy
 * Date: Jul 22, 2010
 */
public class Images {
    public static ImageIcon loadIcon(String name) {
        URL url = Images.class.getResource(name);
        if (url != null)
            return new ImageIcon(url);
        else
            return null;
    }

    public static Image loadImage(String name) {
        ImageIcon icon = loadIcon(name);
        if (icon != null)
            return icon.getImage();
        else
            return null;
    }

    public static Icon getEmptyIcon() {
        if (System.getProperty("java.runtime.name").contains("OpenJDK")) {
            return new ImageIcon(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));
        }
        return null;
    }
}
