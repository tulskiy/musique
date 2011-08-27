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

package com.tulskiy.musique.system.configuration;

import java.util.List;

import com.tulskiy.musique.system.Application;

/**
 * Author: Maksim Liauchuk
 * Date: Aug 27, 2011
 */
public class LibraryConfiguration {
    
    private LibraryConfiguration() {
        // prevent instantiation
    }
    
    public static String getFolderKey() {
        return "library.folders.folder";
    }
    
    public static List<String> getFolders() {
        Configuration config = Application.getInstance().getConfiguration();
        return (List<String>) config.getList(getFolderKey());
    }
    
    public static List<String> getFolders(List<String> def) {
        Configuration config = Application.getInstance().getConfiguration();
        return (List<String>) config.getList(getFolderKey(), def);
    }
    
    public static void setFolders(List<String> values) {
        Configuration config = Application.getInstance().getConfiguration();
        config.setList(getFolderKey(), values);
    }
}


