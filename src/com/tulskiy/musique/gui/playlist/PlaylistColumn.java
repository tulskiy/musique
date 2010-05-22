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

package com.tulskiy.musique.gui.playlist;

import com.tulskiy.musique.db.Column;
import com.tulskiy.musique.db.Entity;
import com.tulskiy.musique.db.Id;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 12, 2010
 */
@Entity("playlist_columns")
public class PlaylistColumn {
    private static Parser parser = new Parser();

    @Id
    private int id = -1;

    @Column
    private String name;
    @Column
    private String expression;
    @Column
    private int size;
    @Column
    private int position;
    @Column
    private int orientation;
    @Column
    private boolean editable;

    private Expression expr;

    public Object getValue(Song song) {
        return expr.eval(song);
    }

    public Class getType() {
        return expr.getType();
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getExpression() {
        return expression;
    }

    public void setExpression(String expression) {
        if (expression != null)
            expr = parser.parse(expression);
        this.expression = expression;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getPosition() {
        return position;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    public int getOrientation() {
        return orientation;
    }

    public void setOrientation(int orientation) {
        this.orientation = orientation;
    }

    public boolean isEditable() {
        return editable;
    }

    public void setEditable(boolean editable) {
        this.editable = editable;
    }
}
