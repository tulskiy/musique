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

import com.tulskiy.musique.system.Application;
import com.tulskiy.musique.system.configuration.Configuration;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.*;
import java.util.*;
import java.util.List;

import static com.tulskiy.musique.plugins.hotkeys.GlobalHotKeysPlugin.*;

/**
 * Author: Denis Tulskiy
 * Date: 8/25/11
 */
public class GlobalHotKeysSettings extends javax.swing.JDialog {
    private Application app = Application.getInstance();
    private Configuration config = app.getConfiguration();
    private DefaultTableModel tableModel;

    // need to keep it here to make NetBeans happy
    @SuppressWarnings({"UnusedDeclaration"})
    public GlobalHotKeysSettings() {
        initComponents();
    }

    public GlobalHotKeysSettings(Window owner) {
        super(owner, ModalityType.APPLICATION_MODAL);
        initComponents();
        initTable();
        initListeners();

        setLocationRelativeTo(null);
    }

    private void initTable() {
        tableModel = new HotKeyTableModel();
        hotKeysTable.setModel(tableModel);

        DefaultCellEditor eventEditor = new DefaultCellEditor(new JComboBox(HotKeyEvent.values()));
        eventEditor.setClickCountToStart(2);
        hotKeysTable.setDefaultEditor(HotKeyEvent.class, eventEditor);
        hotKeysTable.setDefaultEditor(KeyStroke.class, new HotKeyEditor());
    }

    public void init(Map<KeyStroke, HotKeyEvent> activeHotKeys) {
        DefaultTableModel model = (DefaultTableModel) hotKeysTable.getModel();
        model.setRowCount(0);
        for (Map.Entry<KeyStroke, HotKeyEvent> entry : activeHotKeys.entrySet()) {
            model.addRow(new Object[]{entry.getValue(), entry.getKey().toString().replaceAll("pressed ", "")});
        }
    }

    private void initListeners() {
        addButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.addRow(new Object[] {HotKeyEvent.PLAYER_PLAY_PAUSE, ""});
                hotKeysTable.editCellAt(tableModel.getRowCount(), 1);
            }
        });

        removeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                int selectedRow = hotKeysTable.getSelectedRow();
                if (selectedRow != -1) {
                    tableModel.removeRow(selectedRow);
                }
            }
        });

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosed(WindowEvent e) {
                updateConfig();
            }
        });

        closeButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
                dispose();
            }
        });

        clearButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                tableModel.setRowCount(0);
            }
        });
    }

    @SuppressWarnings({"unchecked"})
    private void updateConfig() {
        Vector<Vector> dataVector = tableModel.getDataVector();
        ArrayList<String> list = new ArrayList<String>();
        for (Vector o : dataVector) {
            list.add(o.get(0) + ": " + o.get(1));
        }

        config.setList("hotkeys.list", list);
    }

    private static class HotKeyTableModel extends DefaultTableModel {
        public HotKeyTableModel() {
            super(new Object[][]{}, new String[]{"Command", "HotKey"});
        }

        @Override
        public Class<?> getColumnClass(int columnIndex) {
            switch (columnIndex) {
                case 0:
                    return HotKeyEvent.class;
                case 1:
                    return KeyStroke.class;
            }
            return super.getColumnClass(columnIndex);
        }
    }

    private static class HotKeyEditor extends DefaultCellEditor {
        private static final List<Integer> MODIFIERS = Arrays.asList(KeyEvent.VK_ALT, KeyEvent.VK_CONTROL, KeyEvent.VK_SHIFT, KeyEvent.VK_META);
        private static final String REPLACE_TEXT = "New HotKey...";

        public HotKeyEditor() {
            super(new JTextField());
            setClickCountToStart(1);

            final JTextField textField = (JTextField) editorComponent;
            textField.setEditable(false);
            textField.addKeyListener(new KeyAdapter() {
                @Override
                public void keyPressed(KeyEvent e) {
                    if (!MODIFIERS.contains(e.getKeyCode())) {
                        textField.setText(KeyStroke.getKeyStrokeForEvent(e).toString().replaceAll("pressed ", ""));
                        stopCellEditing();
                    }
                }
            });
        }

        @Override
        public boolean stopCellEditing() {
            if (getCellEditorValue().equals(REPLACE_TEXT))
                cancelCellEditing();
            return super.stopCellEditing();
        }

        @Override
        public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column) {
            return super.getTableCellEditorComponent(table, REPLACE_TEXT, isSelected, row, column);
        }
    }


    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
    private void initComponents() {

        com.tulskiy.musique.gui.components.SeparatorLabel separatorLabel5 = new com.tulskiy.musique.gui.components.SeparatorLabel();
        javax.swing.JScrollPane jScrollPane2 = new javax.swing.JScrollPane();
        hotKeysTable = new javax.swing.JTable();
        addButton = new javax.swing.JButton();
        removeButton = new javax.swing.JButton();
        javax.swing.JSeparator jSeparator1 = new javax.swing.JSeparator();
        closeButton = new javax.swing.JButton();
        clearButton = new javax.swing.JButton();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setTitle("Global HotKeys Configuration");

        separatorLabel5.setText("Global HotKeys");

        hotKeysTable.setRowHeight(25);
        hotKeysTable.setRowMargin(3);
        jScrollPane2.setViewportView(hotKeysTable);

        addButton.setText("Add");

        removeButton.setText("Remove");

        closeButton.setText("Close");

        clearButton.setText("Clear");

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jSeparator1, javax.swing.GroupLayout.Alignment.TRAILING, javax.swing.GroupLayout.DEFAULT_SIZE, 571, Short.MAX_VALUE)
                    .addGroup(layout.createSequentialGroup()
                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                                            .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 458, Short.MAX_VALUE)
                                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                            .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                                                    .addComponent(removeButton, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                                                    .addComponent(addButton, javax.swing.GroupLayout.DEFAULT_SIZE, 95, Short.MAX_VALUE)
                                                    .addComponent(clearButton, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                                    .addComponent(separatorLabel5, javax.swing.GroupLayout.DEFAULT_SIZE, 559, Short.MAX_VALUE))
                            .addContainerGap())
                    .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                            .addComponent(closeButton, javax.swing.GroupLayout.PREFERRED_SIZE, 95, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addContainerGap())))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addContainerGap()
                .addComponent(separatorLabel5, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addGap(8, 8, 8)
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                            .addComponent(addButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(removeButton)
                            .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                            .addComponent(clearButton))
                    .addComponent(jScrollPane2, javax.swing.GroupLayout.DEFAULT_SIZE, 196, Short.MAX_VALUE))
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(jSeparator1, javax.swing.GroupLayout.PREFERRED_SIZE, 8, javax.swing.GroupLayout.PREFERRED_SIZE)
                .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                .addComponent(closeButton)
                .addContainerGap())
        );

        pack();
    }// </editor-fold>//GEN-END:initComponents

    // Variables declaration - do not modify//GEN-BEGIN:variables
    private javax.swing.JButton addButton;
    private javax.swing.JButton clearButton;
    private javax.swing.JButton closeButton;
    private javax.swing.JTable hotKeysTable;
    private javax.swing.JButton removeButton;
    // End of variables declaration//GEN-END:variables
}
