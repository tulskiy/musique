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

package com.tulskiy.musique.gui.custom;

import com.tulskiy.musique.db.Entity;

import javax.swing.*;

/**
 * @Author: Denis Tulskiy
 * @Date: Sep 30, 2009
 */
public class Column {
    public String name;
    public int size = 50;
    public int textOrientation = SwingConstants.LEFT;
    public boolean isEditable = false;

    public Column() {
    }

    public Column(String name) {
        this.name = name;
    }

//    public Column(String name, int size) {
//        this(name, size, SwingConstants.LEFT);
//    }

//    public Column(String name, int size, int textOrientation) {
//        this.name = name;
//        this.size = size;
//        this.textOrientation = textOrientation;
//    }

//    public Column(String name, boolean editable) {
//        this.name = name;
//        isEditable = editable;
//    }
}
