package com.tulskiy.musique.plugins.discogs.dialog;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FilenameFilter;
import java.io.IOException;

import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.GroupLayout;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTabbedPane;
import javax.swing.JTextField;
import javax.swing.GroupLayout.Alignment;
import javax.swing.LayoutStyle.ComponentPlacement;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;
import javax.swing.border.TitledBorder;

import com.tulskiy.musique.gui.dialogs.TreeFileChooser;
import com.tulskiy.musique.plugins.discogs.DiscogsCaller;
import com.tulskiy.musique.plugins.discogs.DiscogsPlugin;
import com.tulskiy.musique.system.Application;
import javax.swing.event.ChangeListener;
import javax.swing.event.ChangeEvent;
import javax.swing.JLabel;
import javax.swing.SwingConstants;

public class SettingsDialog extends JDialog {
	
	private final String CACHE_FILE_NAME_END_MASK = DiscogsPlugin.API_KEY + ".xml";

	private final JPanel contentPanel = new JPanel();
	private JTextField txtSystemTempFolder;
	private JTextField txtApplicationFolder;
	private JTextField txtCustomFolder;
	
	JCheckBox chckbxCacheenabled;
	JRadioButton rdbtnSystemTempFolder;
	JRadioButton rdbtnApplicationFolder;
	JRadioButton rdbtnCustomFolder;

	/**
	 * Create the dialog.
	 */
	public SettingsDialog() {
		setMinimumSize(new Dimension(500, 300));
		final FilenameFilter filter = new FilenameFilter() {
			@Override
			public boolean accept(File dir, String name) {
				return name.endsWith(CACHE_FILE_NAME_END_MASK);
			}
		};
		
		setTitle("Settings");
		setBounds(100, 100, 600, 300);
		getContentPane().setLayout(new BorderLayout());
		contentPanel.setBorder(new EmptyBorder(5, 5, 5, 5));
		getContentPane().add(contentPanel, BorderLayout.CENTER);
		contentPanel.setLayout(new BorderLayout(0, 0));

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		tabbedPane.setEnabled(true);
		contentPanel.add(tabbedPane, BorderLayout.CENTER);

		JPanel tabCache = new JPanel();
		tabbedPane.addTab("Cache", null, tabCache, null);
		tabCache.setLayout(new BorderLayout(0, 0));

		JPanel panelLocation = new JPanel();
		panelLocation.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "Location", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
		tabCache.add(panelLocation, BorderLayout.CENTER);

		rdbtnSystemTempFolder = new JRadioButton("System temp folder");
		rdbtnSystemTempFolder.setSelected(DiscogsPlugin.getCacheDirType() == 1);

		rdbtnApplicationFolder = new JRadioButton("Application folder");
		rdbtnApplicationFolder.setSelected(DiscogsPlugin.getCacheDirType() == 2);

		rdbtnCustomFolder = new JRadioButton("Custom folder");
		rdbtnCustomFolder.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent arg0) {
				txtCustomFolder.setEnabled(rdbtnCustomFolder.isSelected());
			}
		});
		rdbtnCustomFolder.setSelected(DiscogsPlugin.getCacheDirType() == 3);

		ButtonGroup group = new ButtonGroup();
	    group.add(rdbtnSystemTempFolder);
	    group.add(rdbtnApplicationFolder);
	    group.add(rdbtnCustomFolder);
	    
	    txtSystemTempFolder = new JTextField();
	    txtSystemTempFolder.setEnabled(false);
	    txtSystemTempFolder.setColumns(10);
	    txtSystemTempFolder.setText(DiscogsPlugin.DEFAULT_CACHE_ROOT_DIR);
	    
	    txtApplicationFolder = new JTextField();
	    txtApplicationFolder.setEnabled(false);
	    txtApplicationFolder.setColumns(10);
	    txtApplicationFolder.setText(Application.getInstance().CONFIG_HOME.getPath());
	    
	    txtCustomFolder = new JTextField();
	    txtCustomFolder.setMinimumSize(new Dimension(120, 19));
	    txtCustomFolder.setEnabled(rdbtnCustomFolder.isSelected());
	    txtCustomFolder.setColumns(10);
	    txtCustomFolder.setText(DiscogsPlugin.getCacheRootDir());
	    
	    final JButton btnBrowse = new JButton("Browse...");
	    btnBrowse.addActionListener(new ActionListener() {
	    	public void actionPerformed(ActionEvent arg0) {
	            TreeFileChooser fc = new TreeFileChooser(btnBrowse, "Open folder", false);
	            File[] files = fc.showOpenDialog();
	            if (files != null) {
	            	try {
						txtCustomFolder.setText(files[0].getCanonicalPath());
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
	            }
	    	}
	    });

	    GroupLayout gl_panelLocation = new GroupLayout(panelLocation);
	    gl_panelLocation.setHorizontalGroup(
	    	gl_panelLocation.createParallelGroup(Alignment.LEADING)
	    		.addGroup(gl_panelLocation.createSequentialGroup()
	    			.addContainerGap()
	    			.addGroup(gl_panelLocation.createParallelGroup(Alignment.LEADING)
	    				.addGroup(gl_panelLocation.createSequentialGroup()
	    					.addComponent(rdbtnSystemTempFolder)
	    					.addGap(18)
	    					.addComponent(txtSystemTempFolder, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE))
	    				.addGroup(gl_panelLocation.createSequentialGroup()
	    					.addComponent(rdbtnApplicationFolder)
	    					.addGap(31)
	    					.addComponent(txtApplicationFolder, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE))
	    				.addGroup(gl_panelLocation.createSequentialGroup()
	    					.addComponent(rdbtnCustomFolder)
	    					.addGap(57)
	    					.addComponent(txtCustomFolder, GroupLayout.DEFAULT_SIZE, 368, Short.MAX_VALUE)
	    					.addPreferredGap(ComponentPlacement.RELATED)
	    					.addComponent(btnBrowse)))
	    			.addContainerGap())
	    );
	    gl_panelLocation.setVerticalGroup(
	    	gl_panelLocation.createParallelGroup(Alignment.LEADING)
	    		.addGroup(gl_panelLocation.createSequentialGroup()
	    			.addGroup(gl_panelLocation.createParallelGroup(Alignment.BASELINE)
	    				.addComponent(rdbtnSystemTempFolder)
	    				.addComponent(txtSystemTempFolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
	    			.addPreferredGap(ComponentPlacement.UNRELATED)
	    			.addGroup(gl_panelLocation.createParallelGroup(Alignment.BASELINE)
	    				.addComponent(rdbtnApplicationFolder)
	    				.addComponent(txtApplicationFolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE))
	    			.addPreferredGap(ComponentPlacement.UNRELATED)
	    			.addGroup(gl_panelLocation.createParallelGroup(Alignment.BASELINE)
	    				.addComponent(rdbtnCustomFolder)
	    				.addComponent(txtCustomFolder, GroupLayout.PREFERRED_SIZE, GroupLayout.DEFAULT_SIZE, GroupLayout.PREFERRED_SIZE)
	    				.addComponent(btnBrowse))
	    			.addGap(135))
	    );
	    gl_panelLocation.setAutoCreateGaps(true);
	    panelLocation.setLayout(gl_panelLocation);
	    
	    JPanel panelGeneral = new JPanel();
	    panelGeneral.setBorder(new TitledBorder(new LineBorder(new Color(184, 207, 229)), "General", TitledBorder.LEADING, TitledBorder.TOP, null, new Color(51, 51, 51)));
	    tabCache.add(panelGeneral, BorderLayout.NORTH);

		chckbxCacheenabled = new JCheckBox("Cache query results");
		chckbxCacheenabled.setSelected(DiscogsPlugin.isCacheEnabled());
		
		final JLabel lblCacheStats = new JLabel(getLabelStats(filter));
		lblCacheStats.setHorizontalAlignment(SwingConstants.TRAILING);
		lblCacheStats.setPreferredSize(new Dimension(140, 15));
		
		JButton btnClearCache = new JButton("Clear cache");
		panelGeneral.setLayout(new BoxLayout(panelGeneral, BoxLayout.X_AXIS));
		
		Component horizontalStrut_5 = Box.createHorizontalStrut(15);
		panelGeneral.add(horizontalStrut_5);
		panelGeneral.add(chckbxCacheenabled);
		
		Component horizontalStrut_3 = Box.createHorizontalStrut(20);
		panelGeneral.add(horizontalStrut_3);
		
		Component horizontalGlue_1 = Box.createHorizontalGlue();
		panelGeneral.add(horizontalGlue_1);
		panelGeneral.add(lblCacheStats);
		
		Component horizontalStrut_1 = Box.createHorizontalStrut(20);
		panelGeneral.add(horizontalStrut_1);
		panelGeneral.add(btnClearCache);
		
		Component horizontalStrut_4 = Box.createHorizontalStrut(15);
		panelGeneral.add(horizontalStrut_4);
		btnClearCache.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				File cacheDir = new File(DiscogsPlugin.getCacheDir());
				File[] cacheFiles = cacheDir.listFiles(filter);
				if (cacheFiles != null) {
					for (File cacheFile : cacheFiles) {
						cacheFile.delete();
					}
					lblCacheStats.setText(getLabelStats(filter));
				}
			}
		});

		JPanel buttonPane = new JPanel();
		buttonPane.setPreferredSize(new Dimension(10, 50));
		getContentPane().add(buttonPane, BorderLayout.SOUTH);
		buttonPane.setLayout(new BoxLayout(buttonPane, BoxLayout.X_AXIS));

		Component horizontalGlue = Box.createHorizontalGlue();
		buttonPane.add(horizontalGlue);

		JButton btnSave = new JButton("Save");
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				int locationType;
				String location;

				if (rdbtnApplicationFolder.isSelected()) {
					locationType = 2;
					location = txtApplicationFolder.getText();
				}
				else if (rdbtnCustomFolder.isSelected()) {
					locationType = 3;
					location = txtCustomFolder.getText();
				}
				else {
					locationType = 1;
					location = txtSystemTempFolder.getText();
				}

				DiscogsPlugin.setCacheEnabled(chckbxCacheenabled.isSelected());
				DiscogsPlugin.setCacheDirType(locationType);
				DiscogsPlugin.setCacheRootDir(location);
				
				DiscogsCaller.updateCachingConfiguration();

				setVisible(false);
		        dispose();
			}
		});
		btnSave.setPreferredSize(new Dimension(81, 0));
		buttonPane.add(btnSave);

		Component horizontalStrut = Box.createHorizontalStrut(20);
		horizontalStrut.setPreferredSize(new Dimension(10, 0));
		buttonPane.add(horizontalStrut);

		JButton btnCancel = new JButton("Cancel");
		btnCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
		    	setVisible(false);
		        dispose();
			}
		});
		btnCancel.setPreferredSize(new Dimension(81, 0));
		buttonPane.add(btnCancel);

		Component horizontalStrut_2 = Box.createHorizontalStrut(20);
		buttonPane.add(horizontalStrut_2);
	}
	
	private String getLabelStats(FilenameFilter filter) {
		return calculateStats(DiscogsPlugin.getCacheDir(), filter);
	}
	
	private String calculateStats(String s, FilenameFilter filter) {
		int totalAmount = 0;
		long totalSize = 0;

		File dir = new File(s);
		File[] files = dir.listFiles(filter);
		if (files != null) {
			for (File file : files) {
				totalAmount++;
				totalSize += file.length();
			}
		}
		
		return String.format("%.2f MB in %d file(s)", totalSize == 0 ? 0 : (double) totalSize / 1048576, totalAmount);
	}
}
