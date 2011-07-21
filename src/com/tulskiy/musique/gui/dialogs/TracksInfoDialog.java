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
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.ActionMap;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.InputMap;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.tulskiy.musique.gui.components.GroupTable;
import com.tulskiy.musique.gui.cpp.TrackInfoItemSelection;
import com.tulskiy.musique.gui.model.FileInfoModel;
import com.tulskiy.musique.gui.model.MultiTagFieldModel;
import com.tulskiy.musique.gui.model.SingleTagFieldModel;
import com.tulskiy.musique.gui.model.Tools;
import com.tulskiy.musique.gui.model.TrackInfoItem;
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

        final MultiTagFieldModel tagFieldsModel = new MultiTagFieldModel(tracks);
        final JComponent tagsTable = createTable(tagFieldsModel);
        final JComponent propsTable = createTable(new FileInfoModel(tracks));

        JTabbedPane tp = new JTabbedPane();
        tp.setFocusable(false);
        tp.addTab("Metadata", tagsTable);
        tp.addTab("Properties", propsTable);

        add(tp, BorderLayout.CENTER);

        Box b1 = new Box(BoxLayout.X_AXIS);
        b1.add(Box.createHorizontalStrut(10));
        final JButton tools = new JButton("Tools");
        b1.add(tools);
        tools.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                final JPopupMenu menu = new JPopupMenu();

                JMenuItem menuItemEdit = new JMenuItem("Auto track number");
                menu.add(menuItemEdit).addActionListener(new ActionListener() {
    				@Override
    				public void actionPerformed(ActionEvent e) {
    					Tools.autoTrackNumber(tagFieldsModel);
    					tagFieldsModel.sort();
    					
    			    	tagsTable.revalidate();
    			    	tagsTable.repaint();
    				}
    	        });
                
                menu.show(tools, 0, tools.getBounds().height);
            }
        });
        b1.add(Box.createHorizontalGlue());
        JButton write = new JButton("Write");
        b1.add(write);
        write.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	tagFieldsModel.approveModel();
                writeTracks(tracks);
            }
        });
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	tagFieldsModel.rejectModel();
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

    private void writeTracks(final List<Track> tracks) {
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
        table.getSelectionModel().setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
        table.putClientProperty("terminateEditOnFocusLost", Boolean.TRUE);
        final JTextField editor = new JTextField();
        table.setDefaultEditor(Object.class, new DefaultCellEditor(editor) {
            @Override
            public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
                TableModel tableModel = table.getModel();
                if (tableModel instanceof MultiTagFieldModel) {
                    if (((MultiTagFieldModel) tableModel).getTrackInfoItems().get(row).isMultiple()) {
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
                if (tableModel instanceof MultiTagFieldModel) {
                    String value = (String) table.getCellEditor().getCellEditorValue();
                    if (Util.isEmpty(value) && ((MultiTagFieldModel) tableModel).getTrackInfoItems().get(table.getEditingRow()).isMultiple()) {
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
        
        buildActions(table);
        
        table.addMouseListener(new MouseAdapter() {
			
			@Override
			public void mouseReleased(MouseEvent e) {
				show(e);
			}
			
			@Override
			public void mousePressed(MouseEvent e) {
				show(e);
			}

            public void show(MouseEvent e) {
                if (e.isPopupTrigger()) {
                	int index = table.rowAtPoint(e.getPoint());
                	if (index != -1) {
                        if (!table.isRowSelected(index)) {
                        	table.setRowSelectionInterval(index, index);
                        }
	                    JPopupMenu contextMenu = buildContextMenu(parent, table);
	                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                	}
                }
            }
		});

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return scrollPane;
    }

    private MultiTagFieldModel getTagFieldModel(final GroupTable properties) {
    	return (MultiTagFieldModel) properties.getModel();
    }

    private List<TrackInfoItem> getSelectedItems(final GroupTable properties) {
    	final List<TrackInfoItem> trackInfoItemsSelected = new LinkedList<TrackInfoItem>();

    	if (properties.getSelectedRowCount() > 0) {
	    	for (int row : properties.getSelectedRows()) {
	    		trackInfoItemsSelected.add(getTagFieldModel(properties).getTrackInfoItems().get(row));
	    	}
    	}
    	
    	return trackInfoItemsSelected;
    }
    
    private void buildActions(final GroupTable table) {
        ActionMap aMap = table.getActionMap();
        InputMap iMap = table.getInputMap(table.WHEN_ANCESTOR_OF_FOCUSED_COMPONENT);

        aMap.put("edit", new AbstractAction("Edit") {
            @Override
            public void actionPerformed(ActionEvent e) {
            	List<TrackInfoItem> selectedItems = getSelectedItems(table);
            	TrackInfoItem firstItem = selectedItems.get(0);
            	if (!selectedItems.isEmpty()) {
		        	final SingleTagFieldModel tagFieldModel = firstItem.getTracks().size() == 1 ?
		        			new SingleTagFieldModel(firstItem, firstItem.getTracks().get(0)) :
		       				new SingleTagFieldModel(firstItem);
					TracksInfoEditFieldDialog dialog = new TracksInfoEditFieldDialog(table, tagFieldModel);
					dialog.setVisible(true);
            	}
            }
        });

        aMap.put("capitalize", new AbstractAction("Capitalize") {
            @Override
            public void actionPerformed(ActionEvent e) {
            	List<TrackInfoItem> selectedItems = getSelectedItems(table);
            	if (!selectedItems.isEmpty()) {
                	Tools.capitalize(selectedItems);
                	table.revalidate();
                	table.repaint();
            	}
            }
        });

        aMap.put("cut", new AbstractAction("Cut fields") {
            @Override
            public void actionPerformed(ActionEvent e) {
            	List<TrackInfoItem> selectedItems = getSelectedItems(table);
            	if (!selectedItems.isEmpty()) {
					TrackInfoItemSelection selection = new TrackInfoItemSelection(selectedItems);
					getToolkit().getSystemClipboard().setContents(selection, selection);
					getTagFieldModel(table).removeTrackInfoItems(selectedItems);
					refreshTable(table);
            	}
            }
        });

        aMap.put("copy", new AbstractAction("Copy fields") {
            @Override
            public void actionPerformed(ActionEvent e) {
            	List<TrackInfoItem> selectedItems = getSelectedItems(table);
            	if (!selectedItems.isEmpty()) {
					TrackInfoItemSelection selection = new TrackInfoItemSelection(selectedItems);
					getToolkit().getSystemClipboard().setContents(selection, selection);
            	}
            }
        });

        aMap.put("paste", new AbstractAction("Paste fields") {
            @Override
            public void actionPerformed(ActionEvent e) {
				List<TrackInfoItem> items = null;
				try {
					items = (List<TrackInfoItem>) getToolkit().getSystemClipboard().getContents(null).
							getTransferData(TrackInfoItemSelection.objectFlavor);
				}
				catch (IOException ioe) {
					// ignore, treating as the clipboard is empty
				}
				catch (UnsupportedFlavorException ufe) {
					// ignore since we already checked at menu construction that flavor is supported
				}
				if (items != null) {
					MultiTagFieldModel model = getTagFieldModel(table);
					model.mergeTrackInfoItems(items);
					model.sort();
					refreshTable(table);
				}
            }
        });

        aMap.put("add", new AbstractAction("Add") {
            @Override
            public void actionPerformed(ActionEvent e) {
				TracksInfoAddFieldDialog dialog = new TracksInfoAddFieldDialog(table, getTagFieldModel(table));
				dialog.setVisible(true);
				refreshTable(table);
            }
        });

        aMap.put("remove", new AbstractAction("Remove") {
            @Override
            public void actionPerformed(ActionEvent e) {
            	List<TrackInfoItem> selectedItems = getSelectedItems(table);
            	if (!selectedItems.isEmpty()) {
					getTagFieldModel(table).removeTrackInfoItems(selectedItems);
					refreshTable(table);
            	}
            }
        });

        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_X,
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK), "cut");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_C,
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK), "copy");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_V,
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK), "paste");
        iMap.put(KeyStroke.getKeyStroke(KeyEvent.VK_N,
        		ActionEvent.CTRL_MASK), "add");
    }

    private JPopupMenu buildContextMenu(final PlaylistTable playlist, final GroupTable properties) {
    	boolean isAnyRowSelected = properties.getSelectedRowCount() > 0;
        Transferable clipboardContent = getToolkit().getSystemClipboard().getContents(null);
    	
    	ImageIcon emptyIcon = new ImageIcon(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));

        final JPopupMenu menu = new JPopupMenu();

        JMenuItem menuItemEdit = new JMenuItem("Edit");
        menuItemEdit.setIcon(emptyIcon);
        menuItemEdit.setEnabled(isAnyRowSelected);
        menu.add(menuItemEdit).addActionListener(properties.getActionMap().get("edit"));

        JMenuItem menuItemCapitalize = new JMenuItem("Capitalize");
        menuItemCapitalize.setIcon(emptyIcon);
        menuItemCapitalize.setEnabled(isAnyRowSelected);
        menu.add(menuItemCapitalize).addActionListener(properties.getActionMap().get("capitalize"));
        
        menu.addSeparator();

        JMenuItem menuItemCut = new JMenuItem("Cut fields");
        menuItemCut.setIcon(emptyIcon);
        menuItemCut.setEnabled(isAnyRowSelected);
        menuItemCut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X,
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        menu.add(menuItemCut).addActionListener(properties.getActionMap().get("cut"));

        JMenuItem menuItemCopy = new JMenuItem("Copy fields");
        menuItemCopy.setIcon(emptyIcon);
        menuItemCopy.setEnabled(isAnyRowSelected);
        menuItemCopy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C,
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        menu.add(menuItemCopy).addActionListener(properties.getActionMap().get("copy"));

        JMenuItem menuItemPaste = new JMenuItem("Paste fields");
        menuItemPaste.setIcon(emptyIcon);
        menuItemPaste.setEnabled(clipboardContent != null && clipboardContent.isDataFlavorSupported(TrackInfoItemSelection.objectFlavor));
        menuItemPaste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V,
        		ActionEvent.CTRL_MASK + ActionEvent.SHIFT_MASK));
        menu.add(menuItemPaste).addActionListener(properties.getActionMap().get("paste"));

        menu.addSeparator();

        JMenuItem menuItemAdd = new JMenuItem("Add");
        menuItemAdd.setIcon(emptyIcon);
        menuItemAdd.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_N,
        		ActionEvent.CTRL_MASK));
        menu.add(menuItemAdd).addActionListener(properties.getActionMap().get("add"));
        
        JMenuItem menuItemRemove = new JMenuItem("Remove");
        menuItemRemove.setIcon(emptyIcon);
        menuItemRemove.setEnabled(isAnyRowSelected);
        menu.add(menuItemRemove).addActionListener(properties.getActionMap().get("remove"));
        
        return menu;
    }
    
    private void refreshTable(JTable table) {
    	table.clearSelection();
    	table.revalidate();
    	table.repaint();
    }

}
