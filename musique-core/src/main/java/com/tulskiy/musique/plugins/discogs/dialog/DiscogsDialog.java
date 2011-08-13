package com.tulskiy.musique.plugins.discogs.dialog;

import java.awt.BorderLayout;
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import javax.swing.*;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import org.discogs.model.Artist;
import org.discogs.model.ArtistRelease;
import org.discogs.model.LabelRelease;
import org.discogs.model.Release;
import org.jaudiotagger.tag.FieldKey;

import com.tulskiy.musique.gui.dialogs.ProgressDialog;
import com.tulskiy.musique.gui.dialogs.Task;
import com.tulskiy.musique.playlist.Playlist;
import com.tulskiy.musique.playlist.Track;
import com.tulskiy.musique.playlist.TrackData;
import com.tulskiy.musique.plugins.discogs.DiscogsCaller;
import com.tulskiy.musique.plugins.discogs.DiscogsListener;
import com.tulskiy.musique.plugins.discogs.model.DiscogsArtistListModel;
import com.tulskiy.musique.plugins.discogs.model.DiscogsDefaultListModel;
import com.tulskiy.musique.plugins.discogs.model.DiscogsReleaseListModel;
import com.tulskiy.musique.plugins.discogs.model.DiscogsTrackListModel;
import com.tulskiy.musique.plugins.discogs.model.MusiqueTrackListModel;
import com.tulskiy.musique.plugins.discogs.model.ReleaseTracklistingModel;
import com.tulskiy.musique.plugins.discogs.util.DiscogsModelUtil;
import com.tulskiy.musique.system.TrackIO;
import com.tulskiy.musique.util.Util;

public class DiscogsDialog extends JDialog implements DiscogsListener {
	private static final String CARD_RELEASE = "name_58493966786713";
	private static final String CARD_ARTIST = "name_58558508937237";

	private Thread thread;
	private ArrayList<Track> tracks;
	private Playlist playlist;
	private Release release;

	private JTextField txtArtist;
	private JTextField txtFilter;
	private JTextField txtReleaseartist;
	private JTextField txtReleasealbum;
	private JTextField txtReleaselabel;
	private JTextField txtReleasecatalogno;
	private JTextField txtReleaseformat;
	private JTextField txtReleasecountry;
	private JTextField txtReleasegenre;
	private JTextField txtReleasestatus;
	private JTextField txtReleaseyear;

	private DiscogsDialog me;
	private JProgressBar progressBarArtist = new JProgressBar();
	private JProgressBar progressBarRelease = new JProgressBar();
	private JSplitPane splitPaneArtist = new JSplitPane();
	private JSplitPane splitPaneRelease = new JSplitPane();
	private JPanel panelReleaseInfo = new JPanel();
	private JList lstArtists = new JList();
	private JList lstReleases = new JList();
	private JList lstDiscogsTracks = new JList();
	private JList lstMusiqueTracks = new JList();
	private JButton btnQuery = new JButton("Query");
	private JButton btnSelect = new JButton("Select");
	private JButton btnWrite = new JButton("Write");
	private JButton btnDiscogstrackup = new JButton("Up");
	private JButton btnDiscogstrackremove = new JButton("Remove");
	private JButton btnDiscogstrackdown = new JButton("Down");
	private JButton btnMusiquetrackremove = new JButton("Remove");
	private JCheckBox chckbxUseanv = new JCheckBox("Use ANV");

	/**
	 * Create the dialog.
	 */
	public DiscogsDialog(final ArrayList<Track> tracks, final Playlist playlist) {
		me = this;
		this.tracks = tracks;
		this.playlist = playlist;

		setTitle("Discogs");
		setBounds(100, 100, 700, 550);
		getContentPane().setLayout(new CardLayout(0, 0));
		
		JPanel panelArtist = new JPanel();
		getContentPane().add(panelArtist, CARD_ARTIST);
		panelArtist.setLayout(new BorderLayout(0, 0));
		
		JPanel panelArtistControls = new JPanel();
		panelArtistControls.setPreferredSize(new Dimension(10, 50));
		panelArtist.add(panelArtistControls, BorderLayout.SOUTH);
		panelArtistControls.setLayout(new BoxLayout(panelArtistControls, BoxLayout.X_AXIS));
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panelArtistControls.add(horizontalStrut_1);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		panelArtistControls.add(horizontalGlue_1);

		progressBarArtist.setToolTipText("Indicates that Discogs is querying at the moment.");
		progressBarArtist.setVisible(false);
		progressBarArtist.setString("Querying Discogs...");
		progressBarArtist.setIndeterminate(true);
		panelArtistControls.add(progressBarArtist);
		
		Component horizontalGlue = Box.createHorizontalGlue();
		panelArtistControls.add(horizontalGlue);
		btnSelect.setToolTipText("Go to release details page.");
		btnSelect.setPreferredSize(new Dimension(81, 0));
		
		btnSelect.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ListSelectionModel selectionModel = lstReleases.getSelectionModel();
				DiscogsReleaseListModel model = (DiscogsReleaseListModel) lstReleases.getModel();

				thread = new Thread(new DiscogsCaller(
						DiscogsCaller.CallMode.RELEASE, model.getEx(selectionModel.getMinSelectionIndex()).getId(), me), "");
				thread.start();

				CardLayout cl = (CardLayout) getContentPane().getLayout();
			    cl.show(getContentPane(), CARD_RELEASE);
			}
		});
		btnSelect.setEnabled(false);
		panelArtistControls.add(btnSelect);
		
		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(10, 0));
		horizontalStrut.setMinimumSize(new Dimension(10, 0));
		panelArtistControls.add(horizontalStrut);
		
		JButton btnCancelArtist = new JButton("Cancel");
		btnCancelArtist.setToolTipText("Abort querying and/or close Discogs dialog.");
		btnCancelArtist.setPreferredSize(new Dimension(81, 0));
		btnCancelArtist.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				close();
			}
		});
		panelArtistControls.add(btnCancelArtist);
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		panelArtistControls.add(horizontalStrut_3);
		
		splitPaneArtist.setResizeWeight(0.3);
		panelArtist.add(splitPaneArtist, BorderLayout.CENTER);
		
		JScrollPane scrollPane = new JScrollPane();
		splitPaneArtist.setLeftComponent(scrollPane);
		
		JPanel panel_1 = new JPanel();
		panel_1.setPreferredSize(new Dimension(10, 40));
		scrollPane.setColumnHeaderView(panel_1);
		panel_1.setLayout(new BoxLayout(panel_1, BoxLayout.X_AXIS));
		
		Component horizontalStrut_5 = Box.createHorizontalStrut(20);
		panel_1.add(horizontalStrut_5);
		
		JLabel lblArtist = new JLabel("Artist");
		panel_1.add(lblArtist);
		
		Component horizontalStrut_6 = Box.createHorizontalStrut(20);
		horizontalStrut_6.setPreferredSize(new Dimension(10, 0));
		panel_1.add(horizontalStrut_6);
		
		txtArtist = new JTextField();
		txtArtist.setToolTipText("Artist query string.");
		txtArtist.addKeyListener(new KeyAdapter() {
			@Override
			public void keyPressed(KeyEvent e) {
				if (KeyEvent.VK_ENTER  == e.getKeyCode()) {
					btnQuery.doClick();
				}
			}
		});
		txtArtist.setMaximumSize(new Dimension(2147483647, 24));
		txtArtist.setText("Artist");
		panel_1.add(txtArtist);
		txtArtist.setColumns(10);
		txtArtist.setText(tracks.get(0).getTrackData().getArtist());
		txtArtist.setCaretPosition(0);
		
		JScrollPane scrollPane_1 = new JScrollPane();
		splitPaneArtist.setRightComponent(scrollPane_1);
		
		JPanel panel_2 = new JPanel();
		panel_2.setPreferredSize(new Dimension(10, 40));
		scrollPane_1.setColumnHeaderView(panel_2);

		lstReleases.setToolTipText("List of releases belong to selected artist. One is to be selected to continue.");
		lstReleases.setModel(new DiscogsReleaseListModel());
		lstReleases.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					ListSelectionModel selectionModel = lstReleases.getSelectionModel();
					btnSelect.setEnabled(!selectionModel.isSelectionEmpty());
				}
			}
		});
		lstReleases.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					ListSelectionModel selectionModel = lstReleases.getSelectionModel();
					if (!selectionModel.isSelectionEmpty()) {
						DiscogsReleaseListModel releaseModel = (DiscogsReleaseListModel) lstReleases.getModel();
						ArtistRelease release = releaseModel.getEx(selectionModel.getMinSelectionIndex());
				        if(java.awt.Desktop.isDesktopSupported()) {
				        	java.awt.Desktop desktop = java.awt.Desktop.getDesktop();
				        	if(desktop.isSupported(java.awt.Desktop.Action.BROWSE)) {
				        		try {
									desktop.browse(new URI("http://www.discogs.com/release/" + release.getId()));
								}
				        		catch (Exception exc) {
									// ignore any exception since it's absolutely optional feature
								}
				        	}
				        }
						
					}
				}
			}
		});
		scrollPane_1.setViewportView(lstReleases);

		lstArtists.setToolTipText("List of artists fit query string.");
		lstArtists.setModel(new DiscogsArtistListModel());
		lstArtists.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				DiscogsArtistListModel artistModel = (DiscogsArtistListModel) lstArtists.getModel();
				ListSelectionModel selectionModel = lstArtists.getSelectionModel();
				if (!selectionModel.isSelectionEmpty() && !arg0.getValueIsAdjusting()) {
					Artist artist = artistModel.getEx(selectionModel.getMinSelectionIndex());
					if (artist != null) {
						DiscogsReleaseListModel releaseModel = (DiscogsReleaseListModel) lstReleases.getModel();

						releaseModel.clear();
						for (ArtistRelease release : artist.getReleases()) {
							releaseModel.addElement(release);
						}

						lstReleases.revalidate();
						lstReleases.clearSelection();
						lstReleases.repaint();
					}
				}
			}
		});
		scrollPane.setViewportView(lstArtists);

		btnQuery.setToolTipText("Query Discogs database.");
		btnQuery.setPreferredSize(new Dimension(81, 0));
		btnQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				if (!Util.isEmpty(txtArtist.getText())) {
					thread = new Thread(new DiscogsCaller(
							DiscogsCaller.CallMode.SEARCH_ARTISTS, txtArtist.getText(), me), "");
					thread.start();
				}
			}
		});
		
		Component horizontalStrut_7 = Box.createHorizontalStrut(20);
		horizontalStrut_7.setPreferredSize(new Dimension(10, 0));
		panel_1.add(horizontalStrut_7);
		panel_1.add(btnQuery);
		
		Component horizontalStrut_8 = Box.createHorizontalStrut(20);
		panel_1.add(horizontalStrut_8);
		panel_2.setLayout(new BoxLayout(panel_2, BoxLayout.X_AXIS));
		
		Component horizontalStrut_9 = Box.createHorizontalStrut(20);
		panel_2.add(horizontalStrut_9);
		
		JLabel lblFilter = new JLabel("Filter");
		panel_2.add(lblFilter);
		
		txtFilter = new JTextField();
		txtFilter.setToolTipText("Release filter string.");
		txtFilter.setMaximumSize(new Dimension(2147483647, 24));
		txtFilter.getDocument().addDocumentListener(new DocumentListener() {
			public void changedUpdate(DocumentEvent e) {
				filterReleases(lstReleases);
			}
			public void removeUpdate(DocumentEvent e) {
				filterReleases(lstReleases);
			}
			public void insertUpdate(DocumentEvent e) {
				filterReleases(lstReleases);
			}
		});
		
		Component horizontalStrut_10 = Box.createHorizontalStrut(20);
		horizontalStrut_10.setPreferredSize(new Dimension(10, 0));
		panel_2.add(horizontalStrut_10);
		txtFilter.setText("Filter");
		panel_2.add(txtFilter);
		txtFilter.setColumns(10);
		txtFilter.setText(tracks.get(0).getTrackData().getAlbum());
		txtFilter.setCaretPosition(0);
		DiscogsReleaseListModel model = (DiscogsReleaseListModel) lstReleases.getModel();
		model.setFilter(txtFilter.getText());
		
		JButton btnClear = new JButton("Clear");
		btnClear.setToolTipText("Clear release filter field.");
		btnClear.setPreferredSize(new Dimension(81, 0));
		btnClear.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtFilter.setText("");
			}
		});
		
		Component horizontalStrut_11 = Box.createHorizontalStrut(20);
		horizontalStrut_11.setPreferredSize(new Dimension(10, 0));
		panel_2.add(horizontalStrut_11);
		panel_2.add(btnClear);
		
		Component horizontalStrut_12 = Box.createHorizontalStrut(20);
		panel_2.add(horizontalStrut_12);
		
		JPanel panelRelease = new JPanel();
		getContentPane().add(panelRelease, CARD_RELEASE);
		panelRelease.setLayout(new BorderLayout(0, 0));
		
		JPanel panelReleaseControls = new JPanel();
		panelReleaseControls.setPreferredSize(new Dimension(10, 50));
		panelRelease.add(panelReleaseControls, BorderLayout.SOUTH);
		panelReleaseControls.setLayout(new BoxLayout(panelReleaseControls, BoxLayout.X_AXIS));
		
		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		panelReleaseControls.add(horizontalStrut_2);
		
		JButton btnBack = new JButton("Back");
		btnBack.setToolTipText("Return back to artist/release page.");
		btnBack.setPreferredSize(new Dimension(81, 0));
		btnBack.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
			    CardLayout cardLayout = (CardLayout) getContentPane().getLayout();
			    cardLayout.show(getContentPane(), CARD_ARTIST);
			}
		});
		panelReleaseControls.add(btnBack);
		
		Component horizontalGlue_2 = Box.createHorizontalGlue();
		panelReleaseControls.add(horizontalGlue_2);

		progressBarRelease.setToolTipText("Indicates that Discogs is querying at the moment.");
		progressBarRelease.setVisible(false);
		progressBarRelease.setString("Querying Discogs...");
		progressBarRelease.setIndeterminate(true);
		panelReleaseControls.add(progressBarRelease);
		
		Component horizontalGlue2 = Box.createHorizontalGlue();
		panelReleaseControls.add(horizontalGlue2);

		btnWrite.setToolTipText("Write tags to files.");
		btnWrite.setPreferredSize(new Dimension(81, 0));
		btnWrite.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				updateTracks();
				writeTracks();
			}
		});
		
		btnWrite.setEnabled(false);
		panelReleaseControls.add(btnWrite);
		
		Component horizontalStrut2 = Box.createHorizontalStrut(20);
		horizontalStrut2.setPreferredSize(new Dimension(10, 0));
		horizontalStrut2.setMinimumSize(new Dimension(10, 0));
		panelReleaseControls.add(horizontalStrut2);
		
		JButton btnCancelRelease = new JButton("Cancel");
		btnCancelRelease.setToolTipText("Abort querying and/or close Discogs dialog.");
		btnCancelRelease.setPreferredSize(new Dimension(81, 0));
		btnCancelRelease.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
            	close();
			}
		});
		panelReleaseControls.add(btnCancelRelease);
		
		Component horizontalStrut_4 = Box.createHorizontalStrut(20);
		panelReleaseControls.add(horizontalStrut_4);

		panelReleaseInfo.setToolTipText("General release information. Same for all tracks.");
		panelReleaseInfo.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "General Info", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panelRelease.add(panelReleaseInfo, BorderLayout.NORTH);
		
		JLabel lblReleaseartist = new JLabel("Artist");
		
		JLabel lblReleasealbum = new JLabel("Album");
		
		JLabel lblReleaselabel = new JLabel("Label");
		
		JLabel lblReleasecatalogno = new JLabel("Catalog #");
		
		txtReleaseartist = new JTextField();
		txtReleaseartist.setToolTipText("Release artist(s).");
		txtReleaseartist.setEnabled(false);
		txtReleaseartist.setEditable(false);
		txtReleaseartist.setColumns(10);
		
		txtReleasealbum = new JTextField();
		txtReleasealbum.setToolTipText("Release title.");
		txtReleasealbum.setEnabled(false);
		txtReleasealbum.setEditable(false);
		txtReleasealbum.setColumns(10);
		
		txtReleaselabel = new JTextField();
		txtReleaselabel.setToolTipText("Release record label(s).");
		txtReleaselabel.setEnabled(false);
		txtReleaselabel.setEditable(false);
		txtReleaselabel.setColumns(10);
		
		txtReleasecatalogno = new JTextField();
		txtReleasecatalogno.setToolTipText("Release record label catalog number(s).");
		txtReleasecatalogno.setEnabled(false);
		txtReleasecatalogno.setEditable(false);
		txtReleasecatalogno.setColumns(10);
		
		JLabel lblReleaseyear = new JLabel("Year");
		
		txtReleaseformat = new JTextField();
		txtReleaseformat.setToolTipText("Release format(s).");
		txtReleaseformat.setEnabled(false);
		txtReleaseformat.setEditable(false);
		txtReleaseformat.setColumns(10);
		
		txtReleasecountry = new JTextField();
		txtReleasecountry.setToolTipText("Release country.");
		txtReleasecountry.setEnabled(false);
		txtReleasecountry.setEditable(false);
		txtReleasecountry.setColumns(10);
		
		txtReleasegenre = new JTextField();
		txtReleasegenre.setToolTipText("Release genre(s).");
		txtReleasegenre.setEnabled(false);
		txtReleasegenre.setEditable(false);
		txtReleasegenre.setColumns(10);
		
		txtReleasestatus = new JTextField();
		txtReleasestatus.setToolTipText("Release status.");
		txtReleasestatus.setEnabled(false);
		txtReleasestatus.setEditable(false);
		txtReleasestatus.setColumns(10);
		
		txtReleaseyear = new JTextField();
		txtReleaseyear.setToolTipText("Release date.");
		txtReleaseyear.setEnabled(false);
		txtReleaseyear.setEditable(false);
		txtReleaseyear.setColumns(10);
		
		JLabel lblReleasestatus = new JLabel("Status");
		lblReleasestatus.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JLabel lblReleaseformat = new JLabel("Format");
		lblReleaseformat.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JLabel lblReleasecountry = new JLabel("Country");
		lblReleasecountry.setHorizontalAlignment(SwingConstants.RIGHT);
		
		JLabel lblReleasegenre = new JLabel("Genre");
		lblReleasegenre.setHorizontalAlignment(SwingConstants.RIGHT);
		GroupLayout gl_panelReleaseInfo = new GroupLayout(panelReleaseInfo);
		gl_panelReleaseInfo.setHorizontalGroup(
			gl_panelReleaseInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelReleaseInfo.createSequentialGroup()
					.addGap(20)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblReleaseyear)
						.addComponent(lblReleaseartist)
						.addComponent(lblReleaselabel)
						.addComponent(lblReleasealbum)
						.addComponent(lblReleasecatalogno))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.LEADING)
						.addComponent(txtReleaseyear, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
						.addComponent(txtReleasealbum, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
						.addComponent(txtReleaselabel, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
						.addComponent(txtReleaseartist, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE)
						.addComponent(txtReleasecatalogno, GroupLayout.DEFAULT_SIZE, 250, Short.MAX_VALUE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.TRAILING)
						.addComponent(lblReleasestatus)
						.addComponent(lblReleaseformat)
						.addComponent(lblReleasegenre)
						.addComponent(lblReleasecountry, GroupLayout.PREFERRED_SIZE, 71, GroupLayout.PREFERRED_SIZE))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.LEADING)
						.addComponent(txtReleaseformat, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
						.addComponent(txtReleasecountry, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
						.addComponent(txtReleasegenre, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
						.addComponent(txtReleasestatus, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE)
						.addComponent(chckbxUseanv, GroupLayout.DEFAULT_SIZE, 226, Short.MAX_VALUE))
					.addContainerGap())
		);
		gl_panelReleaseInfo.setVerticalGroup(
			gl_panelReleaseInfo.createParallelGroup(Alignment.LEADING)
				.addGroup(gl_panelReleaseInfo.createSequentialGroup()
					.addContainerGap()
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblReleaseartist)
						.addComponent(txtReleaseartist, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtReleasestatus, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblReleasestatus))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblReleasealbum)
						.addComponent(txtReleasealbum, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtReleaseformat, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblReleaseformat))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblReleaselabel)
						.addComponent(txtReleaselabel, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtReleasecountry, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblReleasecountry))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblReleasecatalogno)
						.addComponent(txtReleasegenre, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(txtReleasecatalogno, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(lblReleasegenre))
					.addPreferredGap(ComponentPlacement.RELATED)
					.addGroup(gl_panelReleaseInfo.createParallelGroup(Alignment.BASELINE)
						.addComponent(lblReleaseyear)
						.addComponent(txtReleaseyear, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
						.addComponent(chckbxUseanv))
					.addContainerGap(GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
		);
		panelReleaseInfo.setLayout(gl_panelReleaseInfo);
		
		JPanel panelTracklisting = new JPanel();
		panelTracklisting.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Tracklisting", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		panelTracklisting.setLayout(new BorderLayout(0, 0));
		panelRelease.add(panelTracklisting, BorderLayout.CENTER);

		splitPaneRelease.setResizeWeight(0.5);
		panelTracklisting.add(splitPaneRelease);
		
		JScrollPane scrollPane_2 = new JScrollPane();
		splitPaneRelease.setLeftComponent(scrollPane_2);
		
		JPanel panel = new JPanel();
		panel.setPreferredSize(new Dimension(10, 40));
		scrollPane_2.setColumnHeaderView(panel);

		btnDiscogstrackup.setToolTipText("Move selected items up.");
		btnDiscogstrackup.setEnabled(false);
		btnDiscogstrackup.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				moveSelectedTracklistingItems(lstDiscogsTracks, -1);
			}
		});
		panel.add(btnDiscogstrackup);

		btnDiscogstrackremove.setToolTipText("Remove selected items.");
		btnDiscogstrackremove.setEnabled(false);
		btnDiscogstrackremove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeSelectedTracklistingItems(lstDiscogsTracks);
			}
		});
		panel.add(btnDiscogstrackremove);

		btnDiscogstrackdown.setToolTipText("Move selected items down.");
		btnDiscogstrackdown.setEnabled(false);
		btnDiscogstrackdown.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				moveSelectedTracklistingItems(lstDiscogsTracks, 1);
			}
		});
		panel.add(btnDiscogstrackdown);

		lstDiscogsTracks.setToolTipText("List of tracks read from Discogs database.");
		lstDiscogsTracks.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lstDiscogsTracks.setModel(new DiscogsTrackListModel());
		lstDiscogsTracks.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					ListSelectionModel lsm = lstDiscogsTracks.getSelectionModel();
					btnDiscogstrackup.setEnabled(!lsm.isSelectionEmpty() &&
							lsm.getMinSelectionIndex() > 0);
					btnDiscogstrackremove.setEnabled(!lsm.isSelectionEmpty());
					btnDiscogstrackdown.setEnabled(!lsm.isSelectionEmpty() &&
							lsm.getMaxSelectionIndex() < lstDiscogsTracks.getModel().getSize() - 1);
				}
			}
		});
		scrollPane_2.setViewportView(lstDiscogsTracks);
		
		JScrollPane scrollPane_3 = new JScrollPane();
		splitPaneRelease.setRightComponent(scrollPane_3);
		
		JPanel panel_3 = new JPanel();
		panel_3.setPreferredSize(new Dimension(10, 40));
		scrollPane_3.setColumnHeaderView(panel_3);

		btnMusiquetrackremove.setToolTipText("Remove selected items.");
		btnMusiquetrackremove.setEnabled(false);
		btnMusiquetrackremove.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				removeSelectedTracklistingItems(lstMusiqueTracks);
			}
		});
		panel_3.add(btnMusiquetrackremove);

		lstMusiqueTracks.setToolTipText("List of playlist tracks to be tagged.");
		lstMusiqueTracks.addListSelectionListener(new ListSelectionListener() {
			public void valueChanged(ListSelectionEvent arg0) {
				if (!arg0.getValueIsAdjusting()) {
					ListSelectionModel lsm = lstMusiqueTracks.getSelectionModel();
					btnMusiquetrackremove.setEnabled(!lsm.isSelectionEmpty());
				}
			}
		});

		lstMusiqueTracks.setSelectionMode(ListSelectionModel.SINGLE_INTERVAL_SELECTION);
		lstMusiqueTracks.setModel(new MusiqueTrackListModel());
		scrollPane_3.setViewportView(lstMusiqueTracks);

		chckbxUseanv.addItemListener(new ItemListener() {
			public void itemStateChanged(ItemEvent arg0) {
				fillReleaseInfo(release);

				updateDiscogsTracklistingWithUseAnv();
				lstDiscogsTracks.revalidate();
				lstDiscogsTracks.repaint();
			}
		});
		chckbxUseanv.setToolTipText("Use artist variation name.");
		chckbxUseanv.setSelected(true);
	}
	
	private void filterReleases(final JList list) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DiscogsReleaseListModel model = (DiscogsReleaseListModel) list.getModel();
                model.setFilter(txtFilter.getText());
                list.clearSelection();
            }
        });
	}

	@Override
	public void onRetrieveStart(final DiscogsCaller.CallMode callMode) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                switch (callMode) {
                    case ARTIST:
                    case SEARCH_ARTISTS:
                        clearReleaseInfo();
                        btnSelect.setEnabled(false);
                        progressBarArtist.setVisible(true);
                        setComponentChildrenState(splitPaneArtist, false);
                        break;
                    case RELEASE:
                        btnWrite.setEnabled(false);
                        progressBarRelease.setVisible(true);
                        setComponentChildrenState(panelReleaseInfo, false);
                        setComponentChildrenState(splitPaneRelease, false);
                        break;
                    default:
                        break;
                }
            }
        });
	}

	@Override
	public void onRetrieveFinish(final DiscogsCaller.CallMode callMode, final Object data) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                switch (callMode) {
                    case ARTIST:
                    case SEARCH_ARTISTS:
                        if (lstArtists != null && data != null) {
                            DefaultListModel listModel = (DefaultListModel) lstArtists.getModel();

                            listModel.clear();
                            if (callMode == DiscogsCaller.CallMode.ARTIST) {
                                Artist artist = (Artist) data;
                                listModel.addElement(artist);
                            }
                            else if (callMode == DiscogsCaller.CallMode.SEARCH_ARTISTS) {
                                List<Artist> artists = (List<Artist>) data;
                                for (Artist artist : artists) {
                                    listModel.addElement(artist);
                                }
                            }

                            lstArtists.setSelectedIndex(0);
                        }

                        progressBarArtist.setVisible(false);
                        setComponentChildrenState(splitPaneArtist, true);
                        break;
                    case RELEASE:
                        if (data != null) {
                            release = (Release) data;
                            if (lstDiscogsTracks != null) {
                                fillReleaseInfo(release);
                                updateDiscogsTracklistingWithUseAnv();
                                fillTracklisting(lstDiscogsTracks, release.getTracks());
                                fillTracklisting(lstMusiqueTracks, tracks);

                                btnWrite.setEnabled(true);
                            }
                        }

                        progressBarRelease.setVisible(false);
                        setComponentChildrenState(panelReleaseInfo, true);
                        setComponentChildrenState(splitPaneRelease, true);
                        // to set proper up/remove/down buttons state
                        // i know it's bad bad bad, fix if you can
                        lstDiscogsTracks.getSelectionModel().setSelectionInterval(0, 0);
                        lstDiscogsTracks.getSelectionModel().clearSelection();
                        lstMusiqueTracks.getSelectionModel().setSelectionInterval(0, 0);
                        lstMusiqueTracks.getSelectionModel().clearSelection();
                        break;
                    default:
                        break;
                }
            }
        });
	}
	
	private void setComponentChildrenState(JComponent component, boolean state) {
		component.setEnabled(state);
		for (Component child : component.getComponents()) {
			child.setEnabled(state);
			if (child instanceof JComponent) {
				setComponentChildrenState((JComponent) child, state);
			}
		}
	}
	
	private void fillReleaseInfo(Release release) {
		if (release != null) {
			txtReleaseartist.setText(DiscogsModelUtil.getReleaseArtistDescription(
					release.getArtists(), chckbxUseanv.isSelected(), true));
			txtReleasealbum.setText(release.getTitle());
			txtReleaselabel.setText(DiscogsModelUtil.getReleaseLabelDescription(release));
			txtReleasecatalogno.setText(DiscogsModelUtil.getReleaseCatalogNoDescription(release));
			txtReleaseyear.setText(DiscogsModelUtil.getReleaseDateDescription(release));
			txtReleasestatus.setText(release.getStatus());
			txtReleaseformat.setText(DiscogsModelUtil.getReleaseFormatDescription(release));
			txtReleasecountry.setText(release.getCountry());
			txtReleasegenre.setText(Util.formatFieldValues(release.getStyles(), ", "));
		}
	}
	
	private void fillTracklisting(final JList list, final List<?> tracks) {
        DefaultListModel listModel = (DefaultListModel) list.getModel();
        listModel.clear();
        for (Object track : tracks) {
            listModel.addElement(track);
        }
	}
	
	private void moveSelectedTracklistingItems(final JList list, final int direction) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DiscogsDefaultListModel listModel = (DiscogsDefaultListModel) list.getModel();
                ListSelectionModel lsm = list.getSelectionModel();

                int indexStart = list.getSelectedIndices()[0];
                int indexEnd = list.getSelectedIndices()[list.getSelectedIndices().length - 1];
                int indexObj;
                Object obj;

                if (direction < 0) {
                    indexObj = indexStart - 1;
                    obj = listModel.getEx(indexObj);
                    listModel.remove(indexObj);
                    if (listModel.size() == indexEnd) {
                        listModel.addElement(obj);
                    }
                    else {
                        listModel.add(indexEnd, obj);
                    }
                    lsm.setSelectionInterval(indexStart - 1, indexEnd - 1);
                }
                else if (direction > 0) {
                    indexObj = indexEnd + 1;
                    obj = listModel.getEx(indexObj);
                    listModel.remove(indexObj);
                    listModel.add(indexStart, obj);
                    lsm.setSelectionInterval(indexStart + 1, indexEnd + 1);
                }
            }
        });
	}
	
	private void removeSelectedTracklistingItems(final JList list) {
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                DefaultListModel listModel = (DefaultListModel) list.getModel();
                listModel.removeRange(list.getSelectedIndices()[0],
                        list.getSelectedIndices()[list.getSelectedIndices().length - 1]);
            }
        });
	}
	
	private void clearReleaseInfo() {
		txtReleaseartist.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleasealbum.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleaselabel.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleasecatalogno.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleaseformat.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleasecountry.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleasegenre.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleasestatus.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
		txtReleaseyear.setText(DiscogsModelUtil.DEFAULT_RELEASE_INFO_TEXT);
	}
	
	private void updateDiscogsTracklistingWithUseAnv() {
		((DiscogsTrackListModel) lstDiscogsTracks.getModel()).setUseAnv(chckbxUseanv.isSelected());
	}
	
	private void close() {
		if (thread != null && thread.isAlive()) {
			thread.interrupt();
		}
    	setVisible(false);
        dispose();
	}

	private void updateTracks() {
		MusiqueTrackListModel musiqueModel = (MusiqueTrackListModel) lstMusiqueTracks.getModel();
		DiscogsTrackListModel discogsModel = (DiscogsTrackListModel) lstDiscogsTracks.getModel();
		
		ReleaseTracklistingModel rtm = DiscogsModelUtil.getReleaseTracklistingModel(release);
		for (int i = 0; i < musiqueModel.getSize() && i < discogsModel.getSize(); i++) {
			Track musiqueTrack = musiqueModel.getEx(i);
			org.discogs.model.Track discogsTrack = discogsModel.getEx(i);
			
			TrackData trackData = musiqueTrack.getTrackData();
			
			String albumArtist = DiscogsModelUtil.getReleaseArtistDescription(
					release.getArtists(), chckbxUseanv.isSelected(), false);
			String trackArtist = DiscogsModelUtil.getReleaseArtistDescription(
					discogsTrack.getArtists(), chckbxUseanv.isSelected(), false);

			trackData.setTagFieldValues(FieldKey.ALBUM_ARTIST, albumArtist);
			trackData.setTagFieldValues(FieldKey.ALBUM, release.getTitle());
			trackData.setTagFieldValues(FieldKey.RECORD_LABEL, "");
			trackData.setTagFieldValues(FieldKey.CATALOG_NO, "");
			for (LabelRelease label : release.getLabelReleases()) {
				trackData.addRecordLabel(label.getLabelName());
				trackData.addCatalogNo(label.getCatalogNumber());
			}
			trackData.setTagFieldValues(FieldKey.YEAR, DiscogsModelUtil.getReleaseDateDescription(release));
			trackData.setTagFieldValues(FieldKey.GENRE, "");
			for (String style : release.getStyles()) {
				trackData.addGenre(style);
			}
			
			trackData.setTagFieldValues(FieldKey.ARTIST, Util.firstNotEmpty(trackArtist, albumArtist));
			trackData.setTagFieldValues(FieldKey.TITLE, DiscogsModelUtil.getTrackTitleCleared(discogsTrack.getTitle()));

			trackData.setTagFieldValues(FieldKey.TRACK, rtm.getTrackTrack(discogsTrack));
			trackData.setTagFieldValues(FieldKey.TRACK_TOTAL, rtm.getTrackTrackTotal(discogsTrack));
			trackData.setTagFieldValues(FieldKey.DISC_NO, rtm.getTrackDisc(discogsTrack));
			trackData.setTagFieldValues(FieldKey.DISC_TOTAL, rtm.getTrackDiscTotal(discogsTrack));
		}
	}

	private void writeTracks() {
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
                HashMap<File, ArrayList<Track>> cues =
                	new HashMap<File, ArrayList<Track>>();

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

                playlist.firePlaylistChanged();
                close();
            }
        });
    }
}
