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

package com.tulskiy.musique.gui.dialogs;

import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.TagProcessor;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.LinkedList;

/**
 * Author: Denis Tulskiy
 * Date: Jun 10, 2010
 */
public class ProgressDialog extends JDialog {
    private JProgressBar progress;
    private JLabel status;
    private boolean stopLoading = false;
    private TagProcessor tagProcessor;

    public ProgressDialog(Frame owner, String message) {
        super(owner, message, true);

        setDefaultCloseOperation(JDialog.DISPOSE_ON_CLOSE);
        setSize(400, 100);
        setLocationRelativeTo(owner);

        progress = new JProgressBar(0, 100);
        status = new JLabel("Scanning folders...");
        status.setBorder(BorderFactory.createMatteBorder(2, 0, 0, 0, Color.gray));

        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                stopLoading = true;
                if (tagProcessor != null) {
                    tagProcessor.cancel();
                }
            }
        });

        Box box = Box.createHorizontalBox();
        box.add(progress);
        box.add(Box.createHorizontalStrut(10));
        box.add(cancel);

        box.setBorder(BorderFactory.createEmptyBorder(10, 10, 0, 10));

        add(box, BorderLayout.NORTH);
        add(status, BorderLayout.SOUTH);
    }

    public void addFiles(final Playlist playlist, final java.util.List<File> files) {
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                stopLoading = false;
                progress.setIndeterminate(true);
                LinkedList<File> list = new LinkedList<File>();
                for (File f : files) {
                    if (f.isDirectory()) {
                        loadDirectory(f, list);
                    } else if (f.isFile()) {
                        list.add(f);
                    }
                }
                progress.setIndeterminate(false);
                progress.setMinimum(0);
                progress.setMaximum(list.size());

                tagProcessor = new TagProcessor(list, playlist);

                Timer timer = new Timer(100, new ActionListener() {
                    @Override
                    public void actionPerformed(ActionEvent e) {
                        progress.setValue(progress.getMaximum() - tagProcessor.getFilesLeft());
                        File currentFile = tagProcessor.getCurrentFile();
                        if (currentFile != null)
                            status.setText(currentFile.getPath());
                    }
                });
                timer.start();

                tagProcessor.start();
                setVisible(false);
            }
        });
        t.start();

        setVisible(true);
    }

    private void loadDirectory(File dir, LinkedList<File> list) {
        if (stopLoading) {
            return;
        }
        if (dir.isDirectory()) {
            File[] files = dir.listFiles();

            for (File file : files) {
                if (stopLoading) {
                    break;
                }
                if (file.isFile()) {
                    list.add(file);
                } else {
                    loadDirectory(file, list);
                }
            }
        }
    }
}
