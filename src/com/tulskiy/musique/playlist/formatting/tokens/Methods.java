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

package com.tulskiy.musique.playlist.formatting.tokens;

import com.tulskiy.musique.audio.player.PlayerState;
import com.tulskiy.musique.playlist.Song;
import com.tulskiy.musique.system.Application;

import javax.swing.*;
import java.util.ArrayList;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Methods {
    private static ImageIcon playingIcon = new ImageIcon("resources/images/play.png");
    private static ImageIcon pausedIcon = new ImageIcon("resources/images/pause.png");
    private Application app = Application.getInstance();

    public String if3(Song song, ArrayList<Expression> args) {
        for (Expression t : args) {
            String s = (String) t.eval(song);
            if (notEmpty(s))
                return s;
        }
        return null;
    }

    public String if1(Song song, ArrayList<Expression> args) {
        if (args.size() != 3)
            return null;
        if (notEmpty((String) args.get(0).eval(song))) {
            return (String) args.get(1).eval(song);
        } else {
            return (String) args.get(2).eval(song);
        }
    }

    public String strcmp(Song song, ArrayList<Expression> args) {
        if (args.size() != 2)
            return null;

        if (args.get(0).eval(song).equals(
                args.get(1).eval(song)
        ))
            return "1";

        return null;
    }

    public Object eval(Song song, ArrayList<Expression> args) {
        if (args.size() == 1)
            return args.get(0).eval(song);

        StringBuilder sb = new StringBuilder();
        for (Expression expression : args) {
            String str = (String) expression.eval(song);
            if (str != null)
                sb.append(str);
        }
        if (sb.length() > 0)
            return sb.toString();
        else
            return null;
    }

    private boolean notEmpty(String str) {
        return str != null && str.length() > 0;
    }

    public String notNull(Song song, ArrayList<Expression> args) {
        StringBuilder sb = new StringBuilder();
        boolean notEmpty = true;
        for (Expression expression : args) {
            String str = (String) expression.eval(song);
            if (!(expression instanceof TextExpression))
                notEmpty &= notEmpty(str);
            sb.append(str);
        }

        if (notEmpty)
            return sb.toString();
        else
            return "";
    }

    public ImageIcon isPlaying(Song song, ArrayList<Expression> args) {
        if (app.getPlayer().getSong() == song) {
            if (app.getPlayer().getState() == PlayerState.PAUSED)
                return pausedIcon;
            else
                return playingIcon;
        } else {
            return null;
        }
    }
}
