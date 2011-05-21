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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.util.LinkedList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultCellEditor;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import com.tulskiy.musique.gui.components.GroupTable;
import com.tulskiy.musique.gui.model.MultiTagFieldModel;
import com.tulskiy.musique.gui.model.SingleTagFieldModel;
import com.tulskiy.musique.gui.model.TagFieldModel;
import com.tulskiy.musique.util.Util;

/**
 * Author: Denis Tulskiy
 * Date: Jul 15, 2010
 */
public class TracksInfoEditFieldDialog extends JDialog {
    private JButton cancel;
    private int DEFAULT_COLUMN_WIDTH = 280;

    public TracksInfoEditFieldDialog(final GroupTable properties, final SingleTagFieldModel tagFieldModel) {
        setTitle(tagFieldModel.isMultiTrackEditMode() ? "Edit multiple files" : "Edit single file");
        setModal(false);

        JComponent tagsTable = createTable(properties, tagFieldModel);

        add(tagsTable, BorderLayout.CENTER);

        Box b1 = new Box(BoxLayout.X_AXIS);
        b1.add(Box.createHorizontalGlue());
        JButton update = new JButton("Update");
        b1.add(update);
        update.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	// update state with this dialog values
            	tagFieldModel.approveModel();
            	// sync parent dialog values with approved state
            	((TagFieldModel) properties.getModel()).refreshModel();
            	properties.revalidate();
            	properties.repaint();
                setVisible(false);
                dispose();
                properties.requestFocus();
            }
        });
        cancel = new JButton("Cancel");
        cancel.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	tagFieldModel.rejectModel();
            	setVisible(false);
                dispose();
                properties.requestFocus();
            }
        });

        b1.add(Box.createHorizontalStrut(5));
        b1.add(cancel);
        b1.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        add(b1, BorderLayout.SOUTH);

        setSize(600, 380);
        setLocationRelativeTo(SwingUtilities.windowForComponent(properties));
    }

    private JComponent createTable(final GroupTable parent, final TableModel model) {
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
//        table.getColumn("Key").setMaxWidth(120);

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
                if (tableModel instanceof SingleTagFieldModel) {
                    if (((SingleTagFieldModel) tableModel).getTrackInfoItem().isMultiple()) {
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
                	}
                    JPopupMenu contextMenu = buildContextMenu(table);
                    contextMenu.show(e.getComponent(), e.getX(), e.getY());
                }
            }
		});

        JScrollPane scrollPane = new JScrollPane(table);
        scrollPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        return scrollPane;
    }

    private JPopupMenu buildContextMenu(final GroupTable properties) {
    	final SingleTagFieldModel tagFieldModel = (SingleTagFieldModel) properties.getModel();
        
        final List<Integer> selectedRows = new LinkedList<Integer>();
    	if (properties.getSelectedRowCount() > 0) {
	    	for (int row : properties.getSelectedRows()) {
	    		selectedRows.add(row);
	    	}
    	}

    	ImageIcon emptyIcon = new ImageIcon(new BufferedImage(10, 10, BufferedImage.TYPE_INT_ARGB));

        final JPopupMenu menu = new JPopupMenu();

        if (tagFieldModel.isMultiTrackEditMode()) {
            if (!selectedRows.isEmpty()) {
            	final SingleTagFieldModel editTagFieldModel = new SingleTagFieldModel(tagFieldModel.getTrackInfoItem(),
            			tagFieldModel.getTrackInfoItem().getTracks().get(selectedRows.get(0)));

    	        JMenuItem menuItemEdit = new JMenuItem("Edit");
    	        menuItemEdit.setIcon(emptyIcon);
    	        menu.add(menuItemEdit).addActionListener(new ActionListener() {
    				@Override
    				public void actionPerformed(ActionEvent e) {
    					TracksInfoEditFieldDialog dialog = new TracksInfoEditFieldDialog(properties, editTagFieldModel);
    					dialog.setVisible(true);
    				}
    	        });
             }
        }
        else {
	        JMenuItem menuItemAdd = new JMenuItem("Add");
	        menuItemAdd.setIcon(emptyIcon);
	        menu.add(menuItemAdd).addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					tagFieldModel.addValue();
					properties.clearSelection();
					properties.revalidate();
					properties.repaint();
				}
	        });

	    	if (!selectedRows.isEmpty()) {
		        JMenuItem menuItemRemove = new JMenuItem("Remove");
		        menuItemRemove.setIcon(emptyIcon);
		        menu.add(menuItemRemove).addActionListener(new ActionListener() {
					@Override
					public void actionPerformed(ActionEvent e) {
						for (int row : selectedRows) {
							tagFieldModel.removeValue(row);
						}
						properties.clearSelection();
						properties.revalidate();
						properties.repaint();
					}
				});
	        }
        }
        
        return menu;
    }

}
