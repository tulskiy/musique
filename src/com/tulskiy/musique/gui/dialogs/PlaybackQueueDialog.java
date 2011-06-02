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

import com.tulskiy.musique.playlist.PlaybackOrder;
import com.tulskiy.musique.system.Application;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.*;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: 11/4/10
 */
public class PlaybackQueueDialog extends JDialog {

    public PlaybackQueueDialog(final JComponent owner) {
        super(SwingUtilities.windowForComponent(owner), "Playback Queue", ModalityType.MODELESS);
        Application app = Application.getInstance();
        final PlaybackOrder playbackOrder = app.getPlayer().getPlaybackOrder();
        final List<PlaybackOrder.QueueTuple> queue = playbackOrder.getQueue();
        final JList list = new JList(new DefaultListModel() {
            @Override
            public int getSize() {
                return queue.size();
            }

            @Override
            public Object getElementAt(int index) {
                return (index + 1) + ". " + queue.get(index);
            }
        });
        list.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);

        setLayout(new BorderLayout());
        JScrollPane scrollPane = new JScrollPane(list);
        scrollPane.setBorder(BorderFactory.createEtchedBorder());
        add(scrollPane, BorderLayout.CENTER);

        JPanel buttons = new JPanel(new GridLayout(0, 1));
        JButton up = new JButton("Up");
        JButton down = new JButton("Down");
        JButton remove = new JButton("  Remove  ");
        JButton clear = new JButton("Clear");
        up.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getSelectedIndex();
                if (index > 0) {
                    List<PlaybackOrder.QueueTuple> toAdd = new ArrayList<PlaybackOrder.QueueTuple>();
                    int[] selectedValues = list.getSelectedIndices();
                    for (int i : selectedValues) {
                        toAdd.add(queue.get(i));
                    }
                    queue.removeAll(toAdd);
                    index--;
                    queue.addAll(index, toAdd);
                    list.getSelectionModel().setSelectionInterval(index, index + selectedValues.length - 1);
                    playbackOrder.updateQueuePositions();
                    list.repaint();
                    owner.repaint();
                }
            }
        });
        down.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int[] indices = list.getSelectedIndices();
                if (indices.length > 0) {
                    int index = indices[0] + 1;
                    if (index <= queue.size() - indices.length) {
                        List<PlaybackOrder.QueueTuple> toAdd = new ArrayList<PlaybackOrder.QueueTuple>();
                        int[] selectedValues = list.getSelectedIndices();
                        for (int i : selectedValues) {
                            toAdd.add(queue.get(i));
                        }
                        queue.removeAll(toAdd);
                        queue.addAll(index, toAdd);
                        list.getSelectionModel().setSelectionInterval(index, index + selectedValues.length - 1);
                        playbackOrder.updateQueuePositions();
                        list.repaint();
                        owner.repaint();
                    }
                }
            }
        });
        remove.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int index = list.getSelectedIndex() - 1;
                if (queue.size() > 0) {
                    for (int i : list.getSelectedIndices()) {
                        PlaybackOrder.QueueTuple tuple = queue.get(i);
                        tuple.track.setQueuePosition(-1);
                        queue.remove(tuple);
                    }
                }
                list.clearSelection();
                playbackOrder.updateQueuePositions();
                list.repaint();
                owner.repaint();
            }
        });
        clear.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                playbackOrder.flushQueue();
                list.repaint();
                owner.repaint();
            }
        });

        buttons.add(up);
        buttons.add(down);
        buttons.add(remove);
        buttons.add(clear);
        JPanel p1 = new JPanel(new FlowLayout());
        p1.add(buttons);
        add(p1, BorderLayout.LINE_END);

        setSize(400, 300);
        setLocationRelativeTo(SwingUtilities.windowForComponent(owner));
    }
}
