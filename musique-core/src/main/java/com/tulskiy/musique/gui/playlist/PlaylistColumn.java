/*
 * Copyright (c) 2008, 2009, 2010, 2011 Denis Tulskiy
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

import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;

import java.text.ChoiceFormat;
import java.text.MessageFormat;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 12, 2010
 */
public class PlaylistColumn implements Comparable<PlaylistColumn> {
    private static MessageFormat format = new MessageFormat("\"{0}\" \"{1}\" {2,number,integer} {3}");
    private static ChoiceFormat choice = new ChoiceFormat(new double[]{0, 2, 4}, new String[]{"CENTER", "LEFT", "RIGHT"});

    static {
        format.setFormatByArgumentIndex(3, choice);
    }

    private String name;
    private String expression;
    private int size = 150;
    private int allign = 2; //LEFT by default

    //position in the model, used for sorting purposes only
    private int position;

    private Expression expr;

    public PlaylistColumn() {
    }

    public PlaylistColumn(String fmt) {
        try {
            Object[] objects = format.parse(fmt);
            setName((String) objects[0]);
            setExpression((String) objects[1]);
            setSize(((Long) objects[2]).intValue());
            setAllign(((Double) objects[3]).intValue());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public PlaylistColumn(String name, int size, String expression) {
        this.name = name;
        this.size = size;
        setExpression(expression);
    }

    public Object getValue(Track track) {
        return expr.eval(track);
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
            expr = Parser.parse(expression);
        this.expression = expression;
    }

    public int getSize() {
        return size;
    }

    public void setSize(int size) {
        this.size = size;
    }

    public int getAllign() {
        return allign;
    }

    public void setAllign(int allign) {
        this.allign = allign;
    }

    public void setPosition(int position) {
        this.position = position;
    }

    @Override
    public String toString() {
        return format.format(new Object[]{getName(), getExpression(), getSize(), getAllign()});
    }

    @Override
    public int compareTo(PlaylistColumn o) {
        return ((Integer) position).compareTo(o.position);
    }
}
