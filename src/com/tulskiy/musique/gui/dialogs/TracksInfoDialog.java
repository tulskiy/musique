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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.gui.components.GroupTable;
import com.tulskiy.musique.gui.playlist.PlaylistTable;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.Util;

/**
 * Author: Denis Tulskiy
 * Date: Jul 15, 2010
 */
public class TracksInfoDialog extends JDialog {
    private JButton cancel;
    private PlaylistTable parent;
    private int DEFAULT_COLUMN_WIDTH = 430;

    public TracksInfoDialog(final PlaylistTable parent, final List<Track> tracks) {
        this.parent = parent;
        setTitle("Properties");
        setModal(false);

        final MetadataModel metaModel = new MetadataModel(tracks);
        JComponent tagsTable = createTable(metaModel);
        JComponent propsTable = createTable(new FileInfoModel(tracks));

        JTabbedPane tp = new JTabbedPane();
        tp.setFocusable(false);
        tp.addTab("Metadata", tagsTable);
        tp.addTab("Properties", propsTable);

        add(tp, BorderLayout.CENTER);

        Box b1 = new Box(BoxLayout.X_AXIS);
        b1.add(Box.createHorizontalGlue());
        JButton write = new JButton("Write");
        b1.add(write);
        write.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                writeTracks(metaModel, tracks);
            }
        });
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
                parent.requestFocus();
            }
        });

        b1.add(Box.createHorizontalStrut(5));
        b1.add(cancel);
        b1.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        add(b1, BorderLayout.SOUTH);

        setSize(600, 380);
        setLocationRelativeTo(SwingUtilities.windowForComponent(parent));
    }

    private void writeTracks(final MetadataModel metaModel, final List<Track> tracks) {
        ProgressDialog dialog = new ProgressDialog(this, "Writing tags");
        dialog.show(new Task() {
            String status;
            boolean abort = false;
            public int processed;

            @Override
            public boolean isIndeterminate() {
                return false;
            }

            @Override
            public float getProgress() {
                return (float) processed / tracks.size();
            }

            @Override
            public String getStatus() {
                return "Writing Tags to: " + status;
            }

            @Override
            public void abort() {
                abort = true;
            }

            @Override
            public void start() {
                HashMap<File, ArrayList<Track>> cues = new HashMap<File, ArrayList<Track>>();

                for (Track track : tracks) {
                	TrackData trackData = track.getTrackData();
                    if (!trackData.isFile()) {
                        processed++;
                        continue;
                    }

                    if (abort)
                        break;

                    if (trackData.isCue()) {
                        File file;
                        if (trackData.isCueEmbedded()) {
                            file = trackData.getFile();
                        } else {
                            file = new File(trackData.getCueLocation());
                        }

                        if (!cues.containsKey(file)) {
                            cues.put(file, new ArrayList<Track>());
                        }

                        cues.get(file).add(track);
                        continue;
                    }
                    status = trackData.getFile().getName();
                    for (TrackInfoItem item : metaModel.trackInfoItems) {
                    	item.updateTracks();
                    }
                    TrackIO.write(track);
                    processed++;
                }

                // now let's write cue files
                // not implemented for now
//                CUEWriter writer = new CUEWriter();
//                for (File file : cues.keySet()) {
//                    status = file.getName();
//                    writer.write(file, cues.get(file));
//                }

                parent.getPlaylist().firePlaylistChanged();
                setVisible(false);
                dispose();
                parent.requestFocus();
            }
        });
    }

    private JComponent createTable(TableModel model) {
        final GroupTable table = new GroupTable() {
            public Component prepareRenderer(final TableCellRenderer renderer,
                                             final int row, final int column) {
                final Component prepareRenderer = super
                        .prepareRenderer(renderer, row, column);
                final TableColumn tableColumn = getColumnModel().getColumn(column);

                tableColumn.setPreferredWidth(Math.max(
                        prepareRenderer.getPreferredSize().width + 20,
                        tableColumn.getPreferredWidth()));

                tableColumn.setPreferredWidth(Math.max(
                        DEFAULT_COLUMN_WIDTH,
                        tableColumn.getPreferredWidth()));

                return prepareRenderer;
            }
        };
        table.setModel(model);
        table.setFont(table.getFont().deriveFont(11f));

        table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
        table.getColumn("Key").setMaxWidth(120);

        table.setShowVerticalLines(true);
        table.setIntercellSpacing(new Dimension(1, 1));
        table.setGridColor(Color.lightGray);
        table.getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        final JTextField editor = new JTextField();
        table.setDefaultEditor(Object.class, new DefaultCellEditor(editor) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                TableModel tableModel = table.getModel();
                if (tableModel instanceof MetadataModel) {
                    if (((MetadataModel) tableModel).trackInfoItems.get(row).isMultiple()) {
                        value = "";
                    }
                }
                JTextField c = (JTextField) super.getTableCellEditorComponent(table, value, isSelected, row, column);
                c.setBorder(BorderFactory.createEmptyBorder());
                c.setFont(table.getFont());
                c.selectAll();
                return c;
            }

            @Override
            public void cancelCellEditing() {
                super.cancelCellEditing();
            }

            @Override
            protected void fireEditingStopped() {
                TableModel tableModel = table.getModel();
                if (tableModel instanceof MetadataModel) {
                    String value = (String) table.getCellEditor().getCellEditorValue();
                    if (Util.isEmpty(value) && ((MetadataModel) tableModel).trackInfoItems.get(table.getEditingRow()).isMultiple()) {
                        super.fireEditingCanceled();
                        return;
                    }
                }

                super.fireEditingStopped();
            }
        });
        table.addKeyboardAction(KeyStroke.getKeyStroke("ENTER"), "startEditing", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.editCellAt(table.getSelectedRow(), 1);
                editor.requestFocusInWindow();
            }
        });
        table.addKeyboardAction(KeyStroke.getKeyStroke("DELETE"), "clearCell", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                table.getModel().setValueAt("", table.getSelectedRow(), 1);
                table.repaint();
            }
        });
        table.addKeyboardAction(KeyStroke.getKeyStroke("ESCAPE"), "exitOrStop", new AbstractAction() {
            @Override
            public void actionPerformed(ActionEvent e) {
                if (table.isEditing()) {
                    table.getCellEditor().cancelCellEditing();
                } else {
                    cancel.doClick();
                }
            }
        });
        editor.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (table.isEditing() && (
                        e.getKeyCode() == KeyEvent.VK_DOWN ||
                        e.getKeyCode() == KeyEvent.VK_UP)) {
                    table.getCellEditor().cancelCellEditing();
                }
            }
        });

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return scrollPane;
    }

    private class FileInfoModel extends MetadataModel {
        class Entry {
            String key;
            Object value;

            Entry(String key, Object value) {
                this.key = key;
                this.value = value;
            }
        }

        private ArrayList<Entry> list;

        private FileInfoModel(List<Track> tracks) {
            super(tracks);
        }

        @Override
        protected void loadTracks(List<Track> tracks) {
            list = new ArrayList<Entry>();
            if (tracks.size() == 1) {
                fillSingleTrack(tracks.get(0));
            } else {
                fillMultipleTracks(tracks);
            }
        }

        private void fillMultipleTracks(List<Track> tracks) {
            list.add(new Entry("Tracks selected", tracks.size()));
            long fileSize = 0;
            double length = 0;
            HashMap<String, Integer> formats = new HashMap<String, Integer>();
            HashMap<String, Integer> channels = new HashMap<String, Integer>();
            HashMap<String, Integer> sampleRate = new HashMap<String, Integer>();
            HashSet<String> files = new HashSet<String>();
            for (Track track : tracks) {
            	TrackData trackData = track.getTrackData();
                if (trackData.isFile()) {
                    fileSize += trackData.getFile().length();
                    length += trackData.getTotalSamples() / (double) trackData.getSampleRate();
                    files.add(trackData.getFile().getAbsolutePath());
                    increment(formats, trackData.getCodec());
                    increment(channels, trackData.getChannelsAsString());
                    increment(sampleRate, trackData.getSampleRate() + " Hz");
                }
            }

            list.add(new Entry("Files", files.toString()));
            list.add(new Entry("Total size", fileSize + " bytes"));
            list.add(new Entry("Total Length", Util.formatSeconds(length, 3)));
            list.add(new Entry("Format", calcPercentage(formats)));
            list.add(new Entry("Channels", calcPercentage(channels)));
            list.add(new Entry("Sample Rate", calcPercentage(sampleRate)));
        }

        private Object calcPercentage(Map<String, Integer> map) {
            double total = 0;
            for (Integer val : map.values()) {
                total += val;
            }
            ArrayList<Map.Entry<String, Integer>> list = new ArrayList<Map.Entry<String, Integer>>(map.entrySet());
            Collections.sort(list, new Comparator<Map.Entry<String, Integer>>() {
                @Override
                public int compare(Map.Entry<String, Integer> o1, Map.Entry<String, Integer> o2) {
                    return o2.getValue().compareTo(o1.getValue());
                }
            });

            boolean single = map.size() == 1;
            StringBuilder sb = new StringBuilder();
            for (Map.Entry<String, Integer> entry : list) {
                sb.append(entry.getKey());
                if (!single) {
                    sb.append(" (").append(String.format("%.2f", entry.getValue() / total * 100))
                            .append("%), ");
                }
            }

            return sb.toString().replaceAll(", $", "");
        }

        private void increment(Map<String, Integer> map, String key) {
            Integer val = map.get(key);
            if (val == null) {
                map.put(key, 1);
            } else {
                map.put(key, val + 1);
            }
        }

        private void fillSingleTrack(Track track) {
        	TrackData trackData = track.getTrackData();
            list.add(new Entry("Location", trackData.getLocation().toString().replaceAll("%\\d\\d", " ")));
            if (trackData.isFile())
                list.add(new Entry("File Size (bytes)", trackData.getFile().length()));
            if (trackData.getTotalSamples() >= 0)
                list.add(new Entry("Length", Util.samplesToTime(trackData.getTotalSamples(), trackData.getSampleRate(), 3) +
                                             " (" + trackData.getTotalSamples() + " samples)"));
            list.add(new Entry("Subsong Index", trackData.getSubsongIndex()));
            if (trackData.isCue()) {
                list.add(new Entry("Cue Embedded", trackData.isCueEmbedded()));
                if (!trackData.isCueEmbedded()) {
                    list.add(new Entry("Cue Path", trackData.getCueLocation()));
                }
            }
            list.add(new Entry("Format", trackData.getCodec()));
            if (!Util.isEmpty(trackData.getEncoder())) {
            	list.add(new Entry("Encoder", trackData.getEncoder()));
            }
            list.add(new Entry("Channels", trackData.getChannels()));
            if (trackData.getSampleRate() > 0)
                list.add(new Entry("Sample Rate", trackData.getSampleRate() + " Hz"));
        }

        @Override
        public int getRowCount() {
            return list.size();
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            Entry entry = list.get(rowIndex);
            if (columnIndex == 0)
                return entry.key;
            else
                return String.valueOf(entry.value);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return false;
        }
    }

    // TODO add support of MultiValue editing
    private class TrackInfoItem {

    	private FieldKey key;
    	private List<Track> tracks;

    	private Set<String> allValues;
    	private boolean isUpdated;
    	
    	public TrackInfoItem(FieldKey key, List<Track> tracks) {
    		this.key = key;
    		this.tracks = tracks;
    		
    		allValues = new LinkedHashSet<String>();
    		for (Track track : tracks) {
    			allValues.addAll(track.getTrackData().getTagFieldValuesSafeAsSet(key));
    		}
    		
    		isUpdated = false;
    	}
    	
    	public FieldKey getKey() {
    		return key;
    	}
    	
    	public void setCommonValue(String value) {
    		allValues.clear();
    		allValues.add(value);
    		isUpdated = true;
    	}
    	
    	public void updateTracks() {
    		if (isUpdated) {
	    		for (Track track : tracks) {
	    			track.getTrackData().setTagFieldValues(key, allValues);
	    		}
    		}
    	}
    	
    	public boolean isMultiple() {
    		return allValues.size() > 1;
    	}
    	
    	public String toString() {
    		String result = null;

    		if (allValues.size() > 1) {
    			result = "<multiple values> " + allValues.toString();
            }
            else if (allValues.size() == 1) {
            	result = allValues.iterator().next();
            }
    		
    		return result;
    	}

    }
    
    private class TrackInfoItemComparator implements Comparator<TrackInfoItem> {

    	// TODO move keys to enum with human readable name and priority props
    	private FieldKey[] sortedKeys = {
    			FieldKey.ARTIST,
    			FieldKey.ALBUM_ARTIST,
    			FieldKey.TITLE,
    			FieldKey.ALBUM,
    			FieldKey.YEAR,
    			FieldKey.COMMENT,
    			FieldKey.GENRE,
    			FieldKey.TRACK,
    			FieldKey.TRACK_TOTAL,
    			FieldKey.DISC_NO,
    			FieldKey.DISC_TOTAL,
    			FieldKey.IS_COMPILATION,
    			FieldKey.RECORD_LABEL,
    			FieldKey.CATALOG_NO,
    			FieldKey.LYRICS
    	};

		@Override
		public int compare(TrackInfoItem o1, TrackInfoItem o2) {
			int index1;
			boolean index1found = false;
			int index2;
			boolean index2found = false;

			for (index1 = 0; index1 < sortedKeys.length; index1++) {
				if (sortedKeys[index1].equals(o1.getKey())) {
					index1found = true;
					break;
				}
			}

			for (index2 = 0; index2 < sortedKeys.length; index2++) {
				if (sortedKeys[index2].equals(o2.getKey())) {
					index2found = true;
					break;
				}
			}

			if (index1found && index2found) {
				return index1 - index2;
			}
			else if (!(index1found && index2found)) {
				return o1.getKey().compareTo(o2.getKey());
			}
			
			return index1found ? -1 : 1;
		}

   	
    }

    private class MetadataModel extends AbstractTableModel {
    	
    	private List<TrackInfoItem> trackInfoItems = new LinkedList<TrackInfoItem>();

        private MetadataModel(List<Track> tracks) {
            loadTracks(tracks);
        }

        protected void loadTracks(List<Track> tracks) {
        	Set<FieldKey> usedKeys = new LinkedHashSet<FieldKey>();
            for (int i = 0; i < tracks.size(); i++) {
            	TrackData trackData = tracks.get(i).getTrackData();
            	trackData.populateWithEmptyCommonTagFields();

            	Iterator<Entry<FieldKey, Set<String>>> entries =
            			trackData.getAllTagFieldValuesIterator();
            	while (entries.hasNext()) {
            		Entry<FieldKey, Set<String>> entry = entries.next();
            		if (!usedKeys.contains(entry.getKey())) {
            			usedKeys.add(entry.getKey());
            			trackInfoItems.add(new TrackInfoItem(entry.getKey(), tracks));
            		}
            	}
            }
            Collections.sort(trackInfoItems, new TrackInfoItemComparator());
        }

        @Override
        public int getRowCount() {
            return trackInfoItems.size();
        }

        @Override
        public int getColumnCount() {
            return 2;
        }

        @Override
        public Object getValueAt(int rowIndex, int columnIndex) {
            if (rowIndex < 0 || rowIndex > trackInfoItems.size())
                return null;

            if (columnIndex == 0)
                return Util.humanize(trackInfoItems.get(rowIndex).getKey().toString());
            else
                return trackInfoItems.get(rowIndex).toString();
        }

        @Override
        public String getColumnName(int column) {
            return column == 0 ? "Key" : "Value";
        }

        @Override
        public void setValueAt(Object aValue, int rowIndex, int columnIndex) {
        	trackInfoItems.get(rowIndex).setCommonValue((String) aValue);
        }

        @Override
        public boolean isCellEditable(int rowIndex, int columnIndex) {
            return columnIndex == 1;
        }
    }
}
