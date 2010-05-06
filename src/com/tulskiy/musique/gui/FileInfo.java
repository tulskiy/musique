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

package com.tulskiy.musique.gui;

import com.tulskiy.musique.db.DBMapper;
import com.tulskiy.musique.gui.custom.*;
import com.tulskiy.musique.playlist.Song;

import javax.swing.*;
import java.awt.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

/**
 * @Author: Denis Tulskiy
 * @Date: 27.10.2008
 */
public class FileInfo extends JDialog {
    private static DBMapper<Song> songDBMapper = DBMapper.create(Song.class);
    private Song song;
    private java.util.List<TableItem> metadataList = new ArrayList<TableItem>();
    private java.util.List<TableItem> propertiesList = new ArrayList<TableItem>();

    public FileInfo() throws HeadlessException {
        buildGui();
        buildListeners();

        Dimension dim = getToolkit().getScreenSize();
        setSize(400, 400);
        Rectangle abounds = getBounds();
        setLocation((dim.width - abounds.width) / 2,
                (dim.height - abounds.height) / 2);

    }

    public void load(Song song) {
        this.song = song;
    }

    private void buildGui() {
        JPanel main = new JPanel(new BorderLayout());
        setContentPane(main);
        JTabbedPane tabs = new JTabbedPane();
        main.add(tabs, BorderLayout.CENTER);

        SeparatorTable metadata = new SeparatorTable();
        tabs.addTab("Metadata", new JScrollPane(metadata));
        SeparatorTable properties = new SeparatorTable();
        tabs.addTab("Properties", new JScrollPane(properties));

        ArrayList<Column> columns = new ArrayList<Column>();
//        columns.add(new Column("Name", false));
//        columns.add(new Column("Value", true));

//        metadata.setModel(metadataList, columns);
//        properties.setModel(propertiesList, columns);

//        metadataList.add(new StringTableItem());
    }

    private void buildListeners() {

    }

    private void readTags() {

    }

    class BeanItem implements TableItem {
        Object obj;
        DBMapper.Property property;

        BeanItem(Object obj, DBMapper.Property property) {
            this.obj = obj;
            this.property = property;
        }

        public Object getValue(int column) {
            return null;
        }

        public void setValue(Object value, int column) {

        }
    }

    public static void main(String[] args) {
        FileInfo fileInfo = new FileInfo();
        fileInfo.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                System.exit(0);
            }
        });
        fileInfo.setVisible(true);
    }
}
