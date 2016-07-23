/* Copyright (c) 2016 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique.ngui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.GridLayout;
import java.awt.Insets;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;

import org.apache.commons.lang.SystemUtils;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.communique.data.CConfig;
import com.git.ifly6.communique.io.CLoader;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JInfoFetcher;
import com.git.ifly6.nsapi.NSNation;

/**
 * @author Kevin
 *
 */
public class CommuniqueRecruiter implements JTelegramLogger {

	private static final Logger log = Logger.getLogger(CommuniqueRecruiter.class.getName());

	private JFrame frame;
	private JTextField clientKeyField;
	private JTextField secretKeyField;
	private JTextField telegramIdField;
	private JTextArea sentListArea;

	private HashSet<String> sentList = new HashSet<>();

	// To keep track of the nations to whom we have sent a telegram
	private JLabel lblNationsCount;
	private JProgressBar progressBar;

	// To keep track of the feeders
	private JList<String> excludeList;
	private static String[] regionList = new String[] { "the Pacific", "the North Pacific", "the East Pacific",
			"the West Pacific", "the South Pacific", "Lazarus", "Balder", "Osiris", "the Rejected Realms" };

	/**
	 * Create the application.
	 */
	public CommuniqueRecruiter() {
		initialize();
		frame.setVisible(true);
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);

		Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
		double sWidth = screenDimensions.getWidth();
		double sHeight = screenDimensions.getHeight();

		frame.setTitle("Communiqué Recruiter " + CommuniqueParser.version);
		// frame.setBounds(100, 100, (int) Math.round(2 * sWidth / 3), (int) Math.round(2 * sHeight / 3));

		int windowWidth = 800;
		int windowHeight = 500;

		frame.setBounds((int) (sWidth / 2 - windowWidth / 2), (int) (sHeight / 2 - windowHeight / 2), windowWidth,
				windowHeight);
		frame.setMinimumSize(new Dimension(600, 400));

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		frame.setContentPane(panel);
		panel.setLayout(new GridLayout(0, 2, 5, 5));

		JPanel leftPanel = new JPanel();
		leftPanel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
		panel.add(leftPanel);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
		leftPanel.setLayout(gbl_panel_1);

		JLabel lblClientKey = new JLabel("Client Key");
		GridBagConstraints gbc_lblClientKey = new GridBagConstraints();
		gbc_lblClientKey.anchor = GridBagConstraints.EAST;
		gbc_lblClientKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblClientKey.gridx = 0;
		gbc_lblClientKey.gridy = 0;
		leftPanel.add(lblClientKey, gbc_lblClientKey);

		clientKeyField = new JTextField();
		clientKeyField.setFont(new Font(Font.MONOSPACED, 0, 11));
		GridBagConstraints gbc_clientKeyField = new GridBagConstraints();
		gbc_clientKeyField.gridwidth = 2;
		gbc_clientKeyField.insets = new Insets(0, 0, 5, 0);
		gbc_clientKeyField.fill = GridBagConstraints.HORIZONTAL;
		gbc_clientKeyField.gridx = 1;
		gbc_clientKeyField.gridy = 0;
		leftPanel.add(clientKeyField, gbc_clientKeyField);
		clientKeyField.setColumns(10);

		JLabel lblSecretKey = new JLabel("Secret Key");
		GridBagConstraints gbc_lblSecretKey = new GridBagConstraints();
		gbc_lblSecretKey.anchor = GridBagConstraints.EAST;
		gbc_lblSecretKey.insets = new Insets(0, 0, 5, 5);
		gbc_lblSecretKey.gridx = 0;
		gbc_lblSecretKey.gridy = 1;
		leftPanel.add(lblSecretKey, gbc_lblSecretKey);

		secretKeyField = new JTextField();
		secretKeyField.setFont(new Font(Font.MONOSPACED, 0, 11));
		GridBagConstraints gbc_secretKeyField = new GridBagConstraints();
		gbc_secretKeyField.gridwidth = 2;
		gbc_secretKeyField.insets = new Insets(0, 0, 5, 0);
		gbc_secretKeyField.fill = GridBagConstraints.HORIZONTAL;
		gbc_secretKeyField.gridx = 1;
		gbc_secretKeyField.gridy = 1;
		leftPanel.add(secretKeyField, gbc_secretKeyField);
		secretKeyField.setColumns(10);

		JLabel lblTelegramId = new JLabel("Telegram ID");
		GridBagConstraints gbc_lblTelegramId = new GridBagConstraints();
		gbc_lblTelegramId.anchor = GridBagConstraints.EAST;
		gbc_lblTelegramId.insets = new Insets(0, 0, 5, 5);
		gbc_lblTelegramId.gridx = 0;
		gbc_lblTelegramId.gridy = 2;
		leftPanel.add(lblTelegramId, gbc_lblTelegramId);

		telegramIdField = new JTextField();
		telegramIdField.setFont(new Font(Font.MONOSPACED, 0, 11));
		GridBagConstraints gbc_telegramIdField = new GridBagConstraints();
		gbc_telegramIdField.gridwidth = 2;
		gbc_telegramIdField.insets = new Insets(0, 0, 5, 0);
		gbc_telegramIdField.fill = GridBagConstraints.HORIZONTAL;
		gbc_telegramIdField.gridx = 1;
		gbc_telegramIdField.gridy = 2;
		leftPanel.add(telegramIdField, gbc_telegramIdField);
		telegramIdField.setColumns(10);

		JSeparator separator = new JSeparator();
		GridBagConstraints gbc_separator = new GridBagConstraints();
		gbc_separator.gridwidth = 3;
		gbc_separator.insets = new Insets(0, 0, 5, 5);
		gbc_separator.gridx = 0;
		gbc_separator.gridy = 3;
		leftPanel.add(separator, gbc_separator);

		JLabel lblExclude = new JLabel("Exclude");
		GridBagConstraints gbc_lblExclude = new GridBagConstraints();
		gbc_lblExclude.anchor = GridBagConstraints.EAST;
		gbc_lblExclude.insets = new Insets(0, 0, 5, 5);
		gbc_lblExclude.gridx = 0;
		gbc_lblExclude.gridy = 4;
		leftPanel.add(lblExclude, gbc_lblExclude);

		JButton btnSendButton = new JButton("Send");
		btnSendButton.addActionListener(new ActionListener() {

			private Thread thread;

			@Override public void actionPerformed(ActionEvent e) {

				if (btnSendButton.getText().equals("Send")) {		// STARTING UP

					btnSendButton.setText("Stop");

					Runnable runner = new Runnable() {
						@Override public void run() {

							boolean isSending = true;
							while (isSending) {

								String[] recipients = new String[] {};
								recipients = new JInfoFetcher().getNew();
								// System.out.println("var\t" + Arrays.toString(recipients));

								String intendedRecipient = null;
								for (String element : recipients) {
									if (!sentList.contains(element) && !isProscribed(element)) {
										intendedRecipient = element;
										break;
									}
								}

								// Otherwise, start sending.
								JavaTelegram client = new JavaTelegram(CommuniqueRecruiter.this);
								client.setKeys(new JTelegramKeys(clientKeyField.getText(), secretKeyField.getText(),
										telegramIdField.getText()));
								client.setRecipients(new String[] { intendedRecipient });
								client.connect();

								for (int x = 0; x < 180; x++) {
									try {
										progressBar.setValue(x);
										Thread.sleep(1000);	// 1-second intervals, wake to update the progressBar
									} catch (InterruptedException e) {
										isSending = false;
										break;
									}
								}
							}
						}
					};

					thread = new Thread(runner);
					thread.start();

				} else {	// SHUTTING DOWN

					btnSendButton.setText("Send");
					thread.interrupt();

					// Save the file
					FileDialog fDialog = new FileDialog(frame, "Save file as...", FileDialog.SAVE);
					fDialog.setDirectory(Communique.appSupport.toFile().toString());
					fDialog.setVisible(true);

					String file = fDialog.getFile();
					if (file == null) {
						log.info("User cancelled file save dialog");

					} else {

						// Save the current campaign as a file
						save(file);

						// Close the window
						frame.setVisible(false);
						frame.dispose();

					}
				}
			}
		});

		excludeList = new JList<String>(regionList);
		// excludeList.setFont(new Font(Font.MONOSPACED, 0, 11));
		excludeList.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
		GridBagConstraints gbc_excludeList = new GridBagConstraints();
		gbc_excludeList.gridwidth = 2;
		gbc_excludeList.gridheight = 4;
		gbc_excludeList.insets = new Insets(0, 0, 5, 0);
		gbc_excludeList.fill = GridBagConstraints.BOTH;
		gbc_excludeList.gridx = 1;
		gbc_excludeList.gridy = 4;
		leftPanel.add(new JScrollPane(excludeList), gbc_excludeList);

		JLabel lblSentTo = new JLabel("Sent to");
		GridBagConstraints gbc_lblSentTo = new GridBagConstraints();
		gbc_lblSentTo.anchor = GridBagConstraints.EAST;
		gbc_lblSentTo.insets = new Insets(0, 0, 5, 5);
		gbc_lblSentTo.gridx = 0;
		gbc_lblSentTo.gridy = 8;
		leftPanel.add(lblSentTo, gbc_lblSentTo);

		lblNationsCount = new JLabel("0 nations");
		lblNationsCount.setText(sentList.size() + ((sentList.size() == 1) ? " nation" : " nations"));
		GridBagConstraints gbc_lblNationscount = new GridBagConstraints();
		gbc_lblNationscount.anchor = GridBagConstraints.WEST;
		gbc_lblNationscount.insets = new Insets(0, 0, 5, 5);
		gbc_lblNationscount.gridx = 1;
		gbc_lblNationscount.gridy = 8;
		leftPanel.add(lblNationsCount, gbc_lblNationscount);

		progressBar = new JProgressBar();
		progressBar.setMaximum(180);
		if (SystemUtils.IS_OS_MAC) {
			// Mac, make progress bar around the same length as the button
			progressBar.setBorder(BorderFactory.createEmptyBorder(0, 8, 0, 8));
		}
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.insets = new Insets(0, 0, 5, 0);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 9;
		leftPanel.add(progressBar, gbc_progressBar);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridwidth = 3;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 10;
		leftPanel.add(btnSendButton, gbc_btnNewButton);

		JPanel rightPanel = new JPanel();
		panel.add(rightPanel);
		rightPanel.setLayout(new BorderLayout(0, 0));

		sentListArea = new JTextArea("# List of nations to which a\n" + "# recruitment telegram has been sent.\n");
		sentListArea.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		sentListArea.setFont(new Font(Font.MONOSPACED, 0, 11));
		rightPanel.add(new JScrollPane(sentListArea));
	}

	public void setClientKey(String key) {
		clientKeyField.setText(key);
	}

	public void setSecretKey(String key) {
		secretKeyField.setText(key);
	}

	public void setTelegramId(String id) {
		telegramIdField.setText(id);
	}

	public void setUsingRecipients(String[] cRecipients) {
		for (String element : cRecipients) {

			if (element.startsWith("flag:recruit")) {

				if (element.contains("-- region:") && !element.trim().endsWith("-- region:")) {
					int index = -1;
					for (int x = 0; x < regionList.length; x++) {
						if (regionList[x].equalsIgnoreCase(element.split("-- region:")[1].trim().replace("_", " "))) {
							index = x;
							break;
						}
					}
					if (index > -1) {
						excludeList.addSelectionInterval(index, index);
					}
				}

			} else if (element.startsWith("/")) {
				sentList.add(element.replaceAll("/", ""));
			}
		}

		// Update graphical component
		lblNationsCount.setText(sentList.size() + ((sentList.size() == 1) ? " nation" : " nations"));
	}

	@Override public void log(String input) {
		// Filter out the stuff we don't care about
		if (!input.equals("API Queries Complete.")) {
			System.err.println(input);
		}
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int)
	 */
	@Override public void sentTo(String recipient, int x, int length) {
		sentList.add(recipient);
		lblNationsCount.setText(sentList.size() + ((sentList.size() == 1) ? " nation" : " nations"));
		sentListArea.append("\n/" + recipient);
	}

	private HashSet<String> excludeRegions() {

		HashSet<String> hashSet = new HashSet<>();
		int[] sIndices = excludeList.getSelectedIndices();
		for (int x : sIndices) {
			hashSet.add(regionList[x]);
		}

		return hashSet;
	}

	private boolean isProscribed(String element) {

		NSNation nation = new NSNation(element);

		try {

			nation.populateData();
			String region = nation.getRegion();
			if (excludeRegions().contains(region)) { return true; }

		} catch (IOException e) {

		}

		// assume that is is not proscribed if we cannot populate the data.
		return false;
	}

	/**
	 * @param file
	 */
	private void save(String file) {

		Path savePath = Communique.appSupport.toAbsolutePath().resolve(file);
		log.info("User elected to save file at " + savePath.toAbsolutePath().toString());

		// If it does not end in txt, make it end in txt
		if (!savePath.toAbsolutePath().toString().endsWith(".txt")) {
			savePath = new File(savePath.toAbsolutePath().toString() + ".txt").toPath();
		}

		// Prepare to save
		CLoader loader = new CLoader(savePath);
		CConfig config = new CConfig();

		try {
			config = loader.load();
		} catch (IOException e1) {
			// do nothing else.
			// will also catch in case it doesn't exist, etc.
		}

		config.defaultVersion();
		config.isRecruitment = true;
		config.keys = new JTelegramKeys(clientKeyField.getText(), secretKeyField.getText(), telegramIdField.getText());

		// In case nothing comes of loading and it is null
		if (config.sentList == null) {
			config.sentList = new String[] {};
		}

		// Create and set recipients list
		List<String> recipients = new ArrayList<>(0);
		recipients.add("flag:recruit");
		for (String element : excludeRegions()) {
			recipients.add("flag:recruit -- " + element);
		}
		config.recipients = recipients.toArray(new String[recipients.size()]);

		// Find the old sent-list, merge it with the new one
		String[] sentArray = sentList.toArray(new String[sentList.size()]);
		String[] mergedSentArray = new String[config.sentList.length + sentArray.length];
		System.arraycopy(config.sentList, 0, mergedSentArray, 0, config.sentList.length);
		System.arraycopy(sentArray, 0, mergedSentArray, config.sentList.length, sentArray.length);
		config.sentList = mergedSentArray;

		// Save
		try {
			loader.save(config);

		} catch (IOException e1) {
			e1.printStackTrace();
		}
	}
}
