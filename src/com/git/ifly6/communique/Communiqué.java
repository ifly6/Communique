/*
 * Copyright (c) 2015 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

/*
 * This class here is designed to be an instance of an object which can be called upon for sending.
 * It is not static, since having it as static would likely mean an annoying amount of code. Hence,
 * with instances, it can be called from anywhere, and thus, have multiple front-ends. The purpose
 * of this was so multiple front-ends could be built -- a GUI or CLI -- and thus, solve problems
 * much easier than if they were hardcoded for one another.
 */

package com.git.ifly6.communique;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.Properties;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

import com.git.ifly6.javatelegram.JTelegramException;
import com.git.ifly6.javatelegram.JavaTelegram;

public class Communiqué {

	// TODO Import from URL
	// TODO Live updating recipients list
	// TODO Interface with NS Happenings
	// TODO Save the recuitment flag!
	// TODO Find some way to show all recipients

	CommuniquéLogger util = new CommuniquéLogger();
	JavaTelegram client = new JavaTelegram(util);
	static int version = 1;

	private JFrame frame;
	private JTextField txtClientKey;
	private JTextField txtSecretKey;
	private JTextField txtTelegramId;
	static JTextArea logPane = new JTextArea("== Communiqué " + version + " ==\n"
			+ "Enter information or load file to proceed.\n");
	static JTextArea codePane = new JTextArea("# == Communiqué Recipients Code ==\n"
			+ "# Enter recipients, one for each line or use 'region:', 'WA:', etc tags.\n"
			+ "# Use '/' to say: 'not'. Ex: 'region:europe, /imperium anglorum'.\n");
	private JCheckBoxMenuItem chckbxmntmShowRecipients = new JCheckBoxMenuItem("Show All Recipients");
	private JCheckBoxMenuItem chckbxmntmDisableSending = new JCheckBoxMenuItem("Disable Sending");
	static Font textStandard = new Font("Monospaced", Font.PLAIN, 11);
	private JTextArea recipientsPane = new JTextArea();
	private JCheckBox chckbxRecruitment = new JCheckBox("Recruitment");
	private JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		// Apple Stuff
		System.setProperty("apple.laf.useScreenMenuBar", "true");
		System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Communiqué");

		EventQueue.invokeLater(new Runnable() {
			@Override
			public void run() {
				try {
					Communiqué window = new Communiqué();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public Communiqué() {
		initialise();
	}

	/**
	 * Initialise the contents of the frame.
	 */
	private void initialise() {
		frame = new JFrame("Communiqué " + version);
		frame.setBounds(0, 0, 600, 425);
		frame.setMinimumSize(new Dimension(600, 425));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(1, 0, 0, 0));

		txtClientKey = new JTextField();
		txtClientKey.setFont(textStandard);
		try {
			txtClientKey.setText(readProperties());	// Attempt to fetch client key.
		} catch (IOException e) {
			txtClientKey.setText("Client Key");
		}
		panel.add(txtClientKey);
		txtClientKey.setColumns(10);

		txtSecretKey = new JTextField();
		txtSecretKey.setFont(textStandard);
		txtSecretKey.setText("Secret Key");
		panel.add(txtSecretKey);
		txtSecretKey.setColumns(10);

		txtTelegramId = new JTextField();
		txtTelegramId.setFont(textStandard);
		txtTelegramId.setText("Telegram ID");
		panel.add(txtTelegramId);
		txtTelegramId.setColumns(10);

		chckbxRecruitment.setSelected(true);
		panel.add(chckbxRecruitment);

		JButton btnSend = new JButton("SEND");
		btnSend.addActionListener(al -> {

			client.setKillThread(false);

			// Create another thread so we do not freeze up the GUI
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					CommuniquéParser parser = new CommuniquéParser(util);
					String[] recipients = parser.recipientsParse(codePane.getText());	// Get recipients
					client.setRecipients(recipients);									// Set recipients

					// Review Recipients
					String recipient = "# == Communiqué Recipients ==\n"
							+ "# This tab shows all recipients after parsing of the Code tab.\n\n";
					for (String element : recipients) {
						recipient = recipient + element + "\n";
					}
					recipientsPane.setText(recipient);

					util.log("Recipients set.");

					client.setKeys(new String[] { txtClientKey.getText(), txtSecretKey.getText(),
							txtTelegramId.getText() });

					// Set Recruitment Status
					client.setRecruitment(chckbxRecruitment.isSelected());

					// Save client key
					try {
						writeProperties();
					} catch (IOException e) {
					}

					// In case you need a dry run, it will do everything but send.
					if (!chckbxmntmDisableSending.isSelected()) {
						client.connect();
						util.log("Queries Complete.");
					} else {
						util.log("Sending is disabled. Enable sending to send telegrams.");
					}

					// Update recipients pane.
					String[] sentList = client.getSentList();
					for (String element : sentList) {
						codePane.append("\n/" + element);
					}
				}
			};

			new Thread(runner).start();
			tabbedPane.setSelectedIndex(0);
		});

		JButton btnParse = new JButton("PARSE");
		btnParse.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				CommuniquéParser parser = new CommuniquéParser(util);
				String[] recipients = parser.recipientsParse(codePane.getText());	// Get recipients

				// Show Recipients
				String recipient = "# == Communiqué Recipients ==\n"
						+ "# This tab shows all recipients after parsing of the Code tab.\n\n";
				for (String element : recipients) {
					recipient = recipient + element + "\n";
				}
				recipientsPane.setText(recipient);
				tabbedPane.setSelectedIndex(2);
			}
		});
		panel.add(btnParse);
		panel.add(btnSend);

		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JScrollPane logScrollPane = new JScrollPane();
		tabbedPane.addTab("Log", null, logScrollPane, null);

		logPane.setFont(textStandard);
		logPane.setEditable(false);
		logScrollPane.setViewportView(logPane);

		JScrollPane codeScrollPane = new JScrollPane();
		tabbedPane.addTab("Code", null, codeScrollPane, null);

		codePane.setFont(textStandard);
		codeScrollPane.setViewportView(codePane);

		JScrollPane recipientsScrollPane = new JScrollPane();
		tabbedPane.addTab("Recipients", null, recipientsScrollPane, null);
		recipientsPane.setFont(new Font("Monospaced", Font.PLAIN, 11));
		recipientsPane.setEditable(false);

		recipientsPane.setText("# == Communiqué Recipients ==\n"
				+ "# This tab shows all recipients after parsing of the Code tab.\n");
		recipientsScrollPane.setViewportView(recipientsPane);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSaveConfiguration = new JMenuItem("Save Configuration");
		mntmSaveConfiguration.addActionListener(ae -> {
			FileDialog fileDialog = new FileDialog(frame, "Load Configuration", FileDialog.SAVE);
			fileDialog.setVisible(true);

			String returnFile = fileDialog.getFile();
			File saveFile = new File(fileDialog.getDirectory() + returnFile);

			if (returnFile != null && !returnFile.equals("")) {		// In case they pressed cancel.
					try {
						saveConfiguration(saveFile);
					} catch (FileNotFoundException e1) {
						util.log("Cannot find the location of the selected document.");
					} catch (UnsupportedEncodingException e) {
						util.log("Encoding of selected document is not supported. Create a new savefile.");
					}
				}
				util.log("Configuration saved.");
			});
		mnFile.add(mntmSaveConfiguration);

		JMenuItem mntmLoadConfiguration = new JMenuItem("Load Configuration");
		mntmLoadConfiguration.addActionListener(ae -> {
			FileDialog fileDialog = new FileDialog(frame, "Load Configuration");
			fileDialog.setVisible(true);

			String returnFile = fileDialog.getFile();
			File configFile = new File(fileDialog.getDirectory() + returnFile);

			try {
				if (returnFile != null && !returnFile.equals("")) {		// In case they pressed cancel.
					loadConfiguration(configFile);
				}
			} catch (JTelegramException e2) {
				util.log("Version of file provided mismatches with version here.");
			} catch (FileNotFoundException e) {
				util.log("Cannot find file provided.");
			}
			util.log("Configuration Loaded.");
		});
		mnFile.add(mntmLoadConfiguration);

		JSeparator separator = new JSeparator();
		mnFile.add(separator);

		JMenuItem mntmShowWorkingDirectory = new JMenuItem("Show Working Directory");
		mntmShowWorkingDirectory.addActionListener(ae -> {
			try {
				Desktop.getDesktop().open(new File(System.getProperty("user.dir")));
			} catch (Exception e) {
				util.log("Cannot open working directory.");
			}
		});
		mnFile.add(mntmShowWorkingDirectory);

		JMenuItem mntmExportLog = new JMenuItem("Export Log");
		mntmExportLog.addActionListener(ae -> {
			FileDialog fileDialog = new FileDialog(frame, "Load Configuration", FileDialog.SAVE);
			fileDialog.setVisible(true);
			File exportFile = new File(fileDialog.getDirectory() + fileDialog.getFile());
			exportLog(exportFile);
		});
		mnFile.add(mntmExportLog);

		JSeparator separator_1 = new JSeparator();
		mnFile.add(separator_1);

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmQuit);

		JMenu mnCommands = new JMenu("Commands");
		menuBar.add(mnCommands);

		JMenuItem mntmImportKeysFrom = new JMenuItem("Import Keys from URL");
		mntmImportKeysFrom.addActionListener(al -> {
			String rawURL = JOptionPane.showInputDialog(frame,
					"Paste in the URL provided by NationStates after sending your telegram to tag:api.\n",
					"Import keys from URL", JOptionPane.PLAIN_MESSAGE);

			rawURL = rawURL.replace("http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&",
					"");
			rawURL = rawURL.replace("&to=NATION_NAME", "");

			String[] tags = rawURL.split("&");
			txtTelegramId.setText(tags[0].replace("tgid=", ""));
			txtSecretKey.setText(tags[1].replace("key=", ""));
		});
		mnCommands.add(mntmImportKeysFrom);

		JSeparator separator_4 = new JSeparator();
		mnCommands.add(separator_4);

		JMenuItem mntmImportVoting = new JMenuItem("Import Voting Delegates");
		mntmImportVoting
				.addActionListener(al -> {
					String input = JOptionPane
							.showInputDialog(
									frame,
									"Paste in the list of delegates at vote. Ex: 'Blah (150), Bleh (125), Ecksl (104)'. Include only brackets and commas.",
									"Import Delegates from Proposal Approval", JOptionPane.PLAIN_MESSAGE);
					input = input.replaceAll("\\(.+?\\)", "");
					String[] list = input.split(",");
					for (String element : list) {
						codePane.append("\n" + element.trim().toLowerCase().replace(" ", "_"));
					}
				});
		mnCommands.add(mntmImportVoting);

		JMenuItem mntmImportApprovingDelegates = new JMenuItem("Import Approving Delegates");
		mntmImportApprovingDelegates.addActionListener(al -> {
			String input = JOptionPane.showInputDialog(frame,
					"Paste in the list of delegates approving. Ex: 'Blah, Bleh, Ecksl'. Include only commas.",
					"Import Delegates from At Vote Resolution", JOptionPane.PLAIN_MESSAGE);
			String[] list = input.split(",");
			for (String element : list) {
				codePane.append("\n" + element.trim().toLowerCase().replace(" ", "_"));
			}
		});
		mnCommands.add(mntmImportApprovingDelegates);

		JSeparator separator_3 = new JSeparator();
		mnCommands.add(separator_3);

		chckbxmntmShowRecipients.setSelected(true);
		mnCommands.add(chckbxmntmShowRecipients);
		mnCommands.add(chckbxmntmDisableSending);

		JSeparator separator_2 = new JSeparator();
		mnCommands.add(separator_2);

		JMenuItem mntmKillConnectionThread = new JMenuItem("Kill All Connection Threads");
		mntmKillConnectionThread.addActionListener(ae -> {
			client.setKillThread(true);
			util.log("Kill signal sent. By next sending loop, it should be dead.");
		});
		mnCommands.add(mntmKillConnectionThread);
	}

	private void exportLog(File file) {
		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");
			String header = "# Communiqué Log Export File. Produced by Communiqué version " + version;

			writer.println(header + "\n");
			writer.println("client_key=" + txtClientKey.getText());
			writer.println("secret_key=" + txtSecretKey.getText());
			writer.println("telegram_id=" + txtTelegramId.getText() + "\n");
			writer.println(codePane.getText());

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			util.log("Internal Error. Could not exporting the log.");
		}
	}

	/**
	 * Load a configuration file.
	 *
	 * @param file
	 * @throws FileNotFoundException
	 * @throws JTelegramException
	 */
	private void loadConfiguration(File file) throws JTelegramException, FileNotFoundException {
		CommuniquéFileReader fileReader = new CommuniquéFileReader(file);

		// Check file version.
		if (fileReader.isCompatible(version)) {
			// Set Keys
			String[] keys = fileReader.getKeys();
			txtClientKey.setText(keys[0]);
			txtSecretKey.setText(keys[1]);
			txtTelegramId.setText(keys[2]);

			// Set Recruitment Flag
			chckbxRecruitment.setSelected(fileReader.getRecruitmentFlag());

			// Set Recipients
			for (String element : fileReader.getRecipients()) {
				codePane.append(element);
			}
		} else {
			throw new JTelegramException();
		}
	}

	private String readProperties() throws IOException {
		Properties prop = new Properties();
		FileInputStream stream = new FileInputStream(new File(System.getProperty("user.dir") + "/config.properties"));
		prop.load(stream);
		String clientKey = prop.getProperty("client_key");
		if (clientKey != null && !clientKey.isEmpty()) {
			return clientKey;
		} else {
			return "Client Key";
		}
	}

	private void saveConfiguration(File file) throws FileNotFoundException, UnsupportedEncodingException {
		CommuniquéFileWriter fileWriter = new CommuniquéFileWriter(file);
		fileWriter.setKeys(new String[] { txtClientKey.getText(), txtSecretKey.getText(), txtTelegramId.getText() });
		fileWriter.setBody(codePane.getText());
		fileWriter.setRecuitment(chckbxRecruitment.isSelected());
		fileWriter.write();
	}

	private void writeProperties() throws IOException {
		Properties prop = new Properties();
		FileOutputStream output = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
		prop.setProperty("client_key", txtClientKey.getText());
		prop.store(output, "");
		output.close();
	}
}