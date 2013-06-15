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

package com.tulskiy.musique.gui.dialogs;

import javax.swing.plaf.nimbus.NimbusLookAndFeel;
import com.tulskiy.musique.gui.components.GroupTable;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.formatting.Parser;
import com.tulskiy.musique.playlist.formatting.tokens.Expression;
import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.Configuration;
import com.tulskiy.musique.util.FileUtils;
import com.tulskiy.musique.util.Util;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.text.JTextComponent;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.channels.ClosedChannelException;
import java.nio.channels.FileChannel;
import java.util.*;
import java.util.List;

/**
 * Author: Denis Tulskiy
 * Date: Aug 1, 2010
 */
public class FileOperations extends JDialog {
    private static final ArrayList<String> DEFAULT_PATTERNS = new ArrayList<String>(Arrays.asList(
            "%fileName%", "[%artist% - ]%title%",
            "[%trackNumber% - ]%title%",
            "[%year% - ]%album%/%fileName%")
    );

    private JComboBox namePattern;
    private PathChooser folder;
    private DefaultTableModel previewModel;
    private HashMap<Track, File> paths = new LinkedHashMap<Track, File>();
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();

    public enum Operation {
        Copy,
        Move,
        Rename
    }

    private Operation mode;
    private List<Track> tracks;

    public FileOperations(final JComponent owner, Operation mode, List<Track> tracks) {
        super(SwingUtilities.windowForComponent(owner), "File Operations");
        this.mode = mode;
        this.tracks = tracks;
        setDefaultCloseOperation(DISPOSE_ON_CLOSE);
        JPanel top = new JPanel(new GridLayout(3, 2, 5, 5));
        top.setBorder(BorderFactory.createTitledBorder("Options"));
        final JComboBox op = new JComboBox(Operation.values());
        top.add(new JLabel("Operation"));
        top.add(op);

        folder = new PathChooser("");
        folder.addPropertyChangeListener("path", new PropertyChangeListener() {
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                updatePreview();
            }
        });
        top.add(new JLabel("Destination"));
        top.add(folder);

        final ArrayList<String> patterns = config.getList("fileOperations.patterns", DEFAULT_PATTERNS);
        namePattern = new JComboBox(patterns.toArray());
        namePattern.setEditable(true);
        top.add(new JLabel("File name pattern"));
        top.add(namePattern);

        JPanel main = new JPanel(new BorderLayout());
        main.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        main.add(top, BorderLayout.NORTH);

        GroupTable preview = new GroupTable() {
            public Component prepareRenderer(final TableCellRenderer renderer,
                                             final int row, final int column) {
                final Component prepareRenderer = super
                        .prepareRenderer(renderer, row, column);
                final TableColumn tableColumn = getColumnModel().getColumn(column);

                tableColumn.setPreferredWidth(Math.max(
                        prepareRenderer.getPreferredSize().width,
                        tableColumn.getPreferredWidth()));

                return prepareRenderer;
            }
        };
        previewModel = new DefaultTableModel(new Object[]{"Source", "Destination"}, 10) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
        preview.setModel(previewModel);
        preview.setFont(preview.getFont().deriveFont(11f));
        preview.setGridColor(Color.lightGray);
        preview.setShowVerticalLines(true);
        preview.setIntercellSpacing(new Dimension(1, 1));

        JScrollPane scroll = new JScrollPane(preview);
        scroll.setBorder(BorderFactory.createTitledBorder("Preview"));
        main.add(scroll, BorderLayout.CENTER);

        op.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                FileOperations.this.mode = (Operation) op.getSelectedItem();
                if (FileOperations.this.mode == Operation.Rename) {
                    folder.setEnabled(false);
                } else {
                    folder.setEnabled(true);
                }

                updatePreview();
            }
        });
        op.setSelectedItem(mode);

        JTextComponent textComp = (JTextComponent) namePattern.getEditor().getEditorComponent();
        textComp.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                updatePreview();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                updatePreview();
            }
        });

        namePattern.setSelectedItem(config.getString("fileOperations.selectedPattern", patterns.get(0)));
        folder.setPath(config.getString("fileOperations.path", ""));

        Box buttons = Box.createHorizontalBox();
        buttons.add(Box.createHorizontalGlue());
        JButton ok = new JButton(" Start ");
        buttons.add(ok);
        ok.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                config.setString("fileOperations.path", folder.getPath());
                String selectedItem = (String) namePattern.getSelectedItem();
                config.setString("fileOperations.selectedPattern", selectedItem);
                if (!patterns.contains(selectedItem))
                    patterns.add(selectedItem);
                config.setList("fileOperations.patterns", patterns);
                setVisible(false);
                dispose();

                ProgressDialog progress = new ProgressDialog(owner, "File Operations");
                progress.show(new CopyTask());
            }
        });
        buttons.add(Box.createHorizontalStrut(3));
        JButton cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });
        buttons.add(cancel);
        buttons.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        main.add(buttons, BorderLayout.PAGE_END);
        updatePreview();
        setContentPane(main);
        setSize(700, 500);
        setLocationRelativeTo(null);
    }

    public void updatePreview() {
        String text = (String) namePattern.getEditor().getItem();
        previewModel.setRowCount(0);
        paths.clear();
        if (!Util.isEmpty(text)) {
            text = text.replaceAll("%([\\w]+)%", "\\$escape($0)");
            Expression expression = Parser.parse(text);
            for (Track track : tracks) {
                Object key;
                if (track.getTrackData().isFile() && !track.getTrackData().isCue()) {
                    File file = track.getTrackData().getFile();
                    key = file.getName();
                    StringBuilder sb = new StringBuilder();
                    Object o = expression.eval(track);
                    if (o == null)
                        continue;
                    sb.append(o);
                    sb.append(".").append(Util.getFileExt(file));
                    File output = null;
                    if (mode == Operation.Rename) {
                        output = new File(file.getParent(), sb.toString());
                    } else {
                        File path = new File(folder.getPath());
                        if (path.exists() && path.isDirectory())
                            output = new File(folder.getPath(), sb.toString());
                    }

                    if (output != null) {
                        previewModel.addRow(new Object[]{key, output.getAbsolutePath()});
                        paths.put(track, output);
                    }
                }
            }
        }
    }

    class CopyTask extends Task {
        private File src;
        private File dest;
        private long processed;
        private long totalSize;
        private boolean abort = false;

        @Override
        public String getStatus() {
            StringBuilder sb = new StringBuilder();
            switch (mode) {
                case Copy:
                    sb.append("Copying: ");
                    break;
                case Move:
                    sb.append("Moving: ");
                    break;
                case Rename:
                    sb.append("Renaming: ");
            }

            sb.append(src).append("\n");
            sb.append("Destination: ").append(dest);
            return sb.toString();
        }

        @Override
        public void abort() {
            abort = true;
        }

        @SuppressWarnings({"ResultOfMethodCallIgnored"})
        @Override
        public void start() {
            totalSize = 0;
            for (Track track : paths.keySet()) {
                totalSize += track.getTrackData().getFile().length();
            }

            ArrayList<String> log = new ArrayList<String>();
            processed = 0;
            for (Map.Entry<Track, File> entry : paths.entrySet()) {
                if (abort)
                    break;
                src = entry.getKey().getTrackData().getFile();
                dest = entry.getValue();
                dest.getParentFile().mkdirs();

                if (mode == Operation.Rename) {
                    if (!src.renameTo(dest))
                        log.add("Failed to rename " + src + " to " + dest);
                    processed += src.length();
                } else {
                    if (mode == Operation.Move) {
                        //try to rename first

                        if (src.renameTo(dest)) {
                            processed += src.length();
                            entry.getKey().getTrackData().setLocation(dest.toURI().toString());
                            // TODO "delete yes/no" decision to be remembered
                            FileUtils.deleteEmptyParentFolders(src, true);
                            continue;
                        }
                    }

                    //now copy
                    try {
                        if (src.canRead() || dest.canWrite()) {
                            FileChannel from = new FileInputStream(src).getChannel();
                            FileChannel to = new FileOutputStream(dest).getChannel();
                            long transferred = 0;
                            long length = from.size();
                            int chunkSize = 10000000;
                            while (transferred < length) {
                                if (abort)
                                    break;
                                long len = from.transferTo(
                                        transferred, chunkSize, to);
                                transferred += len;
                                processed += len;
                            }
                            from.close();
                            to.close();
                        } else {
                            log.add("Not enough permissions to copy/move " + src + " to " + dest);
                        }
                    } catch (ClosedChannelException ignore) {
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (abort) {
                        dest.delete();
                    } else if (mode == Operation.Move) {
                        if (src.delete()) {
                            // TODO "delete yes/no" decision to be remembered
                            FileUtils.deleteEmptyParentFolders(src, true);
                        } else {
                            log.add("Failed to remove " + src);
                        }
                    }


                }

                if ((mode == Operation.Move || mode == Operation.Rename)
                        && dest.exists()) {
                    entry.getKey().getTrackData().setLocation(dest.toURI().toString());
                }
            }
        }

        @Override
        public boolean isIndeterminate() {
            return false;
        }

        @Override
        public float getProgress() {
            return (float) processed / totalSize;
        }
    }

    public static void main(String[] args) {
        Application app = Application.getInstance();
        app.load();
        try {
            UIManager.setLookAndFeel(new NimbusLookAndFeel());
        } catch (UnsupportedLookAndFeelException e) {
            e.printStackTrace();
        }
        Playlist pl = new Playlist();
        JLabel label = new JLabel();
        pl.insertItem("/windows/Users/tulskiy/Music/Avril Lavigne", 0, true, null);
        FileOperations dialog = new FileOperations(label, Operation.Rename, pl);
        dialog.setVisible(true);
    }
}


