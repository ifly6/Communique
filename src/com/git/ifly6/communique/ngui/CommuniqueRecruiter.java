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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.communique.data.CConfig;
import com.git.ifly6.communique.io.CLoader;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JInfoFetcher;

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

	private JProgressBar progressBar;

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

		int windowWidth = 600;
		int windowHeight = 400;

		frame.setBounds((int) (sWidth / 2 - windowWidth / 2), (int) (sHeight / 2 - windowHeight / 2), windowWidth,
				windowHeight);
		frame.setMinimumSize(new Dimension(600, 400));

		JPanel panel = new JPanel();
		panel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
		frame.setContentPane(panel);
		panel.setLayout(new GridLayout(0, 2, 5, 5));

		JPanel leftPanel = new JPanel();
		panel.add(leftPanel);
		GridBagLayout gbl_panel_1 = new GridBagLayout();
		gbl_panel_1.columnWidths = new int[] { 0, 0, 0, 0 };
		gbl_panel_1.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0 };
		gbl_panel_1.columnWeights = new double[] { 0.0, 1.0, 0.0, Double.MIN_VALUE };
		gbl_panel_1.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, 0.0,
				Double.MIN_VALUE };
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
								System.out.println("var\t" + Arrays.toString(recipients));

								String intendedRecipient = null;
								for (String element : recipients) {
									if (!sentList.contains(element)) {
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
										Thread.sleep(1000);
									} catch (InterruptedException e) {

										e.printStackTrace();

										// Terminate by setting isSending to false, then breaking out of the time-loop.
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
							e1.printStackTrace();
							// do nothing else.
							// will also catch in case it doesn't exist, etc.
						}

						// In case nothing comes of loading and it is null
						if (config.sentList == null) {
							config.sentList = new String[] {};
						}

						String[] sentArray = sentList.toArray(new String[sentList.size()]);

						String[] mergedSentArray = new String[config.sentList.length + sentArray.length];
						System.arraycopy(config.sentList, 0, mergedSentArray, 0, config.sentList.length);
						System.arraycopy(sentArray, 0, mergedSentArray, config.sentList.length, sentArray.length);

						config.sentList = mergedSentArray;

						try {
							loader.save(config);

						} catch (IOException e1) {
							e1.printStackTrace();
						}

					}

				}
			}
		});

		JCheckBox chckbxThePacific = new JCheckBox("The Pacific");
		GridBagConstraints gbc_chckbxThePacific = new GridBagConstraints();
		gbc_chckbxThePacific.anchor = GridBagConstraints.WEST;
		gbc_chckbxThePacific.gridwidth = 2;
		gbc_chckbxThePacific.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxThePacific.gridx = 1;
		gbc_chckbxThePacific.gridy = 4;
		leftPanel.add(chckbxThePacific, gbc_chckbxThePacific);

		JCheckBox chckbxTheNorthPacific = new JCheckBox("The North Pacific");
		GridBagConstraints gbc_chckbxTheNorthPacific = new GridBagConstraints();
		gbc_chckbxTheNorthPacific.anchor = GridBagConstraints.WEST;
		gbc_chckbxTheNorthPacific.gridwidth = 2;
		gbc_chckbxTheNorthPacific.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxTheNorthPacific.gridx = 1;
		gbc_chckbxTheNorthPacific.gridy = 5;
		leftPanel.add(chckbxTheNorthPacific, gbc_chckbxTheNorthPacific);

		JCheckBox chckbxTheEastPacific = new JCheckBox("The East Pacific");
		GridBagConstraints gbc_chckbxTheEastPacific = new GridBagConstraints();
		gbc_chckbxTheEastPacific.anchor = GridBagConstraints.WEST;
		gbc_chckbxTheEastPacific.gridwidth = 2;
		gbc_chckbxTheEastPacific.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxTheEastPacific.gridx = 1;
		gbc_chckbxTheEastPacific.gridy = 6;
		leftPanel.add(chckbxTheEastPacific, gbc_chckbxTheEastPacific);

		JCheckBox chckbxTheWestPacific = new JCheckBox("The West Pacific");
		GridBagConstraints gbc_chckbxTheWestPacific = new GridBagConstraints();
		gbc_chckbxTheWestPacific.anchor = GridBagConstraints.WEST;
		gbc_chckbxTheWestPacific.gridwidth = 2;
		gbc_chckbxTheWestPacific.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxTheWestPacific.gridx = 1;
		gbc_chckbxTheWestPacific.gridy = 7;
		leftPanel.add(chckbxTheWestPacific, gbc_chckbxTheWestPacific);

		JCheckBox chckbxTheSouthPacific = new JCheckBox("The South Pacific");
		GridBagConstraints gbc_chckbxTheSouthPacific = new GridBagConstraints();
		gbc_chckbxTheSouthPacific.anchor = GridBagConstraints.WEST;
		gbc_chckbxTheSouthPacific.gridwidth = 2;
		gbc_chckbxTheSouthPacific.insets = new Insets(0, 0, 5, 0);
		gbc_chckbxTheSouthPacific.gridx = 1;
		gbc_chckbxTheSouthPacific.gridy = 8;
		leftPanel.add(chckbxTheSouthPacific, gbc_chckbxTheSouthPacific);

		progressBar = new JProgressBar();
		progressBar.setMaximum(180);
		GridBagConstraints gbc_progressBar = new GridBagConstraints();
		gbc_progressBar.fill = GridBagConstraints.HORIZONTAL;
		gbc_progressBar.gridwidth = 3;
		gbc_progressBar.insets = new Insets(0, 0, 5, 5);
		gbc_progressBar.gridx = 0;
		gbc_progressBar.gridy = 10;
		leftPanel.add(progressBar, gbc_progressBar);
		GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
		gbc_btnNewButton.fill = GridBagConstraints.HORIZONTAL;
		gbc_btnNewButton.gridwidth = 3;
		gbc_btnNewButton.gridx = 0;
		gbc_btnNewButton.gridy = 11;
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

	public void appendToSentList(String input) {
		sentList.add(input);
	}

	public void appendToSentList(Collection<String> strings) {
		sentList.addAll(strings);
	}

	@Override public void log(String input) {
		System.err.println(input);
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int)
	 */
	@Override public void sentTo(String recipient, int x, int length) {
		sentList.add(recipient);
		sentListArea.append("\n/" + recipient);
	}
}
