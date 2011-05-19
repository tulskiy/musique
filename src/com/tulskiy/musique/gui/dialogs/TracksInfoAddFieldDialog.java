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
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JTextField;
import javax.swing.SwingUtilities;

import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.gui.components.GroupTable;
import com.tulskiy.musique.gui.model.MultiTagFieldModel;
import com.tulskiy.musique.gui.model.TrackInfoItem;
import com.tulskiy.musique.gui.playlist.PlaylistTable;
import com.tulskiy.musique.util.FieldKeyMetaHelper;
import com.tulskiy.musique.util.FieldKeyMetaHelper.FieldKeyMeta;

/**
 * Author: Maksim Liauchuk
 * Date: 08.05.2011
 */
public class TracksInfoAddFieldDialog extends JDialog {

	private JButton cancel;

    public TracksInfoAddFieldDialog(final GroupTable properties, final MultiTagFieldModel tagFieldsModel) {
        setTitle("Add field");
        setModal(false);

        final JComboBox key = new JComboBox(new FieldKeyModel(getAvailableFieldKeys(tagFieldsModel)));
        add(key, BorderLayout.NORTH);

        final JTextField value = new JTextField();
        add(value, BorderLayout.CENTER);

        Box b1 = new Box(BoxLayout.X_AXIS);
        b1.add(Box.createHorizontalGlue());
        JButton add = new JButton("Add");
        b1.add(add);
        add.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
            	FieldKeyMeta keyMeta = (FieldKeyMeta) key.getSelectedItem();
            	TrackInfoItem trackInfoItem = new TrackInfoItem(keyMeta.getKey(),
            			tagFieldsModel.getTrackInfoItems().get(0).getTracks());
            	trackInfoItem.getState().setValue(value.getText());
            	trackInfoItem.approveState(false);
            	
            	tagFieldsModel.addTrackInfoItem(trackInfoItem);
            	tagFieldsModel.sort();

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
                setVisible(false);
                dispose();
                properties.requestFocus();
            }
        });

        b1.add(Box.createHorizontalStrut(5));
        b1.add(cancel);
        b1.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 10));

        add(b1, BorderLayout.SOUTH);

        setSize(300, 115);
        setLocationRelativeTo(SwingUtilities.windowForComponent(properties));
    }
    
    private List<FieldKey> getAvailableFieldKeys(final MultiTagFieldModel tagFieldsModel) {
    	List<FieldKey> result = new ArrayList<FieldKey>(Arrays.asList(FieldKey.values()));
    	result.removeAll(tagFieldsModel.getAllUsedFieldKeys());
    	return result;
    }
    
    private class FieldKeyModel extends DefaultComboBoxModel {
    	
    	private List<FieldKey> keys;
    	
    	public FieldKeyModel(List<FieldKey> keys) {
    		this.keys = keys;
    	}

		@Override
		public Object getElementAt(int arg0) {
			return FieldKeyMetaHelper.getFieldKeyMeta(keys.get(arg0));
		}

		@Override
		public int getSize() {
			return keys.size();
		}
    	
    }

}
