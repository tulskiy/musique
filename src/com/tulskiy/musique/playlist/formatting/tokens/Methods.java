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

import java.util.ArrayList;
import java.util.Set;

import javax.swing.ImageIcon;

import com.tulskiy.musique.audio.player.Player;
import com.tulskiy.musique.images.Images;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.util.Util;

/**
 * @Author: Denis Tulskiy
 * @Date: Feb 6, 2010
 */
@SuppressWarnings({"UnusedDeclaration"})
public class Methods {
    private static ImageIcon playingIcon = Images.loadIcon("play.png");
    private static ImageIcon pausedIcon = Images.loadIcon("pause.png");
    private Application app = Application.getInstance();

    public String if3(Track track, ArrayList<Expression> args) {
        for (Expression t : args) {
            String s = (String) t.eval(track);
            if (notEmpty(s))
                return s;
        }
        return null;
    }

    public String if1(Track track, ArrayList<Expression> args) {
        if (args.size() != 3)
            return null;
        if (notEmpty((String) args.get(0).eval(track))) {
            return (String) args.get(1).eval(track);
        } else {
            return (String) args.get(2).eval(track);
        }
    }

    public String strcmp(Track track, ArrayList<Expression> args) {
        if (args.size() != 2)
            return null;

        if (args.get(0).eval(track).equals(
                args.get(1).eval(track)
        ))
            return "1";

        return null;
    }

    public String greater(Track track, ArrayList<Expression> args) {
        if (args.size() != 2) {
            return null;
        }

        try {
            String sop1 = (String) args.get(0).eval(track);
            String sop2 = (String) args.get(1).eval(track);
            if (sop1 != null && sop2 != null) {
                int op1 = Integer.valueOf(sop1);
                int op2 = Integer.valueOf(sop2);

                if (op1 > op2) return "1";
            }
        } catch (NumberFormatException ignored) {
        }

        return null;
    }

    public Object eval(Track track, ArrayList<Expression> args) {
        if (args.size() == 1)
            return args.get(0).eval(track);

        StringBuilder sb = new StringBuilder();
        for (Expression expression : args) {
            String str = (String) expression.eval(track);
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

    public String notNull(Track track, ArrayList<Expression> args) {
        StringBuilder sb = new StringBuilder();
        boolean notEmpty = false;
        for (Expression expression : args) {
            String str = (String) expression.eval(track);
            if (!(expression instanceof TextExpression))
                notEmpty |= notEmpty(str);
            if (str != null)
                sb.append(str);
        }

        if (notEmpty)
            return sb.toString();
        else
            return "";
    }

    public Object isPlaying(Track track, ArrayList<Expression> args) {
        if (track != null) {
            if (app.getPlayer().getTrack() == track) {
                if (app.getPlayer().isPaused())
                    return pausedIcon;
                if (app.getPlayer().isPlaying())
                    return playingIcon;
            } else if (track.getQueuePosition() != -1) {
                return track.getQueuePosition();
            }
        }

        return null;
    }

    public String playingTime(Track track, ArrayList<Expression> args) {
        Player player = app.getPlayer();
        if (player.isPlaying()) {
            return Util.samplesToTime(player.getCurrentSample(), player.getTrack().getTrackData().getSampleRate(), 0);
        } else {
            return null;
        }
    }
    
    public String combine(Track track, ArrayList<Expression> args) {
        if (args.size() != 2) {
            return null;
        }

    	Object tagFieldValues = args.get(0).eval(track);
        String separator = (String) args.get(1).eval(track);
        if (tagFieldValues != null && separator != null) {
        	if (tagFieldValues instanceof String) {
        		return (String) tagFieldValues;
        	}
        	else {
            	StringBuilder sb = new StringBuilder();
            	for (Object value : ((Set<String>) tagFieldValues).toArray()) {
            		if (sb.length() != 0) {
            			sb.append(separator);
            		}
            		sb.append(value.toString());
            	}
            	return sb.toString();
        	}
        }

        return null;
    }
}
