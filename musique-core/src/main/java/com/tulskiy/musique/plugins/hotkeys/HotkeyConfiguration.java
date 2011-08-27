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

package com.tulskiy.musique.plugins.hotkeys;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.KeyStroke;

import org.apache.commons.collections.CollectionUtils;

import com.tulskiy.musique.plugins.hotkeys.GlobalHotKeysPlugin.HotKeyEvent;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.configuration.Configuration;

/**
 * Author: Maksim Liauchuk
 * Date: Aug 27, 2011
 */
public class HotkeyConfiguration {
    
    private HotkeyConfiguration() {
        // prevent instantiation
    }

    // TODO refactor column to store separate fields (event/shortcut) instead of solid formatted string
    public static String getHotkeyKey() {
        return "hotkeys.hotkey";
    }
    
    public static Map<KeyStroke, HotKeyEvent> getHotkeys(Logger logger) {
        Configuration config = Application.getInstance().getConfiguration();
        List<String> hotkeysRaw = (List<String>) config.getList(getHotkeyKey());
        Map<KeyStroke, HotKeyEvent> hotkeys = new LinkedHashMap<KeyStroke, HotKeyEvent>();
        if (!CollectionUtils.isEmpty(hotkeysRaw)) {
            for (String hotkeyRaw : hotkeysRaw) {
                try {
                    String[] tokens = hotkeyRaw.split(": ");

                    HotKeyEvent event = HotKeyEvent.valueOf(tokens[0]);
                    KeyStroke keyStroke = KeyStroke.getKeyStroke(tokens[1]);

                    hotkeys.put(keyStroke, event);
                } catch (IllegalArgumentException e) {
                    logger.warning("Could not parse hotkey for string: " + hotkeyRaw);
                }
            }
        }

        return hotkeys;
    }

    @Deprecated
    /*
     * Temporary method to convert old configuration values.
     */
    public static void setHotkeysRaw(List<String> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getHotkeyKey(), values);
    }
    
    public static void setHotkeys(Vector<Vector> values) {
        Configuration config = Application.getInstance().getConfiguration();
        ArrayList<String> hotkeysRaw = new ArrayList<String>();
        for (Vector value : values) {
            hotkeysRaw.add(value.get(0) + ": " + value.get(1));
        }
        config.setList(getHotkeyKey(), hotkeysRaw);
    }

}


