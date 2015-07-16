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
import java.io.FileReader;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Properties;
import java.util.Scanner;
import java.util.Set;

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
import com.git.ifly6.javatelegram.JTelegramFetcher;
import com.git.ifly6.javatelegram.JavaTelegram;

public class Communiqué {

	// TODO Import from URL
	// TODO Live updating recipients list
	// TODO Interface with NS Happenings

	CommuniquéLogger util = new CommuniquéLogger();
	JavaTelegram client = new JavaTelegram(util);
	static int version = 1;

	private JFrame frame;
	private JTextField txtClientKey;
	private JTextField txtSecretKey;
	private JTextField txtTelegramId;
	static JTextArea logPane = new JTextArea("== Communiqué " + version + " ==\n"
			+ "Enter information or load file to proceed.");
	static JTextArea recipientsPane = new JTextArea(
			"# == Communiqué Recipients ==\n# Enter recipients, one for each line or use 'region:', 'WA:', etc tags.\n# Use '/' to say: 'not'. Ex: 'region:europe, /imperium anglorum'.");
	private JCheckBoxMenuItem chckbxmntmShowRecipients = new JCheckBoxMenuItem("Show All Recipients");
	private JCheckBoxMenuItem chckbxmntmDisableSending = new JCheckBoxMenuItem("Disable Sending");
	static Font textStandard = new Font("Monospaced", Font.PLAIN, 11);

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
		frame.setBounds(0, 0, 550, 405);
		frame.setMinimumSize(new Dimension(550, 400));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel panel = new JPanel();
		frame.getContentPane().add(panel, BorderLayout.SOUTH);
		panel.setLayout(new GridLayout(1, 0, 0, 0));

		txtClientKey = new JTextField();
		txtClientKey.setFont(textStandard);
		try {
			txtClientKey.setText(readProperties());	// Attempt to fetch client key.
		} catch (IOException e3) {
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

		JCheckBox chckbxRecruitment = new JCheckBox("Recruitment");
		chckbxRecruitment.setSelected(true);
		panel.add(chckbxRecruitment);

		JButton btnSend = new JButton("SEND");
		btnSend.addActionListener(al -> {

			client.setKillThread(false);

			// Create another thread so we do not freeze up the GUI
			Runnable runner = new Runnable() {
				@Override
				public void run() {
					String[] recipients = recipientsParse(recipientsPane.getText());	// Get recipients
					client.setRecipients(recipients);									// Set recipients

					// Review Recipients
					if (chckbxmntmShowRecipients.isSelected()) {
						for (String element : recipients) {
							util.log("Recipient: " + element);
						}
					}

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
				}
			};

			new Thread(runner).start();
		});
		panel.add(btnSend);

		JTabbedPane tabbedPane = new JTabbedPane(JTabbedPane.TOP);
		frame.getContentPane().add(tabbedPane, BorderLayout.CENTER);

		JScrollPane logScrollPane = new JScrollPane();
		tabbedPane.addTab("Log", null, logScrollPane, null);

		logPane.setFont(textStandard);
		logPane.setEditable(false);
		logScrollPane.setViewportView(logPane);

		JScrollPane recipientsScrollPane = new JScrollPane();
		tabbedPane.addTab("Recipients", null, recipientsScrollPane, null);

		recipientsPane.setFont(textStandard);
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
				saveConfiguration(saveFile);
			}
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
			}
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
							"Import Delegates from At Vote Resolution", JOptionPane.PLAIN_MESSAGE);
			String[] list = input.split(",");
			for (String element : list) {
				recipientsPane.append("\n" + element.trim().toLowerCase().replace(" ", "_"));
			}
		});
		mnCommands.add(mntmImportVoting);

		JMenuItem mntmImportApprovingDelegates = new JMenuItem("Import Approving Delegates");
		mntmImportApprovingDelegates.addActionListener(al -> {
			String input = JOptionPane.showInputDialog(frame,
					"Paste in the list of delegates approving. Ex: 'Blah, Bleh, Ecksl'. Include only commas.",
					"Import Delegates from Proposal Approval", JOptionPane.PLAIN_MESSAGE);
			input = input.replaceAll("\\(.+?\\)", "");
			String[] list = input.split(",");
			for (String element : list) {
				recipientsPane.append("\n" + element.trim().toLowerCase().replace(" ", "_"));
			}
		});
		mnCommands.add(mntmImportApprovingDelegates);

		JSeparator separator_3 = new JSeparator();
		mnCommands.add(separator_3);

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
			writer.println(recipientsPane.getText());

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			util.log("Internal Error. Could not exporting the log.");
		}
	}

	private void saveConfiguration(File file) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		String header = "# Communiqué Configuration File. Do not edit by hand.";
		String headerDate = "# Produced at: " + dateFormat.format(date);
		String headerVers = "# Produced by version " + version;

		try {
			PrintWriter writer = new PrintWriter(file, "UTF-8");

			writer.println(header);
			writer.println(headerDate);
			writer.println(headerVers + "\n");
			writer.println("client_key=" + txtClientKey.getText());
			writer.println("secret_key=" + txtSecretKey.getText());
			writer.println("telegram_id=" + txtTelegramId.getText() + "\n");
			writer.println(recipientsPane.getText());

			for (String element : client.getSentList()) {
				writer.println("/" + element);
			}

			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			util.log("Internal Error. Could not save this configuration.");
		}
	}

	/**
	 * Load a configuration file.
	 *
	 * @param file
	 * @throws FileNotFoundException
	 * @throws JTelegramException
	 */
	private void loadConfiguration(File file) throws JTelegramException {
		ArrayList<String> fileContents = new ArrayList<String>(0);

		// Load the file
		try {
			FileReader configRead = new FileReader(file);
			Scanner scan = new Scanner(configRead);
			while (scan.hasNextLine()) {
				fileContents.add(scan.nextLine());
			}
			scan.close();
		} catch (IOException e) {
			util.log("Cannot find the specified file.");
			return;
		}

		// Check file version.
		boolean correctVersion = false;
		for (String element : fileContents) {
			if (element.equals("# Produced by version 1") || element.equals("# Produced by version 0.1.0")) {
				correctVersion = true;
			}
		}

		// Only do if correctVersion is true
		if (correctVersion) {
			for (int x = 0; x < fileContents.size(); x++) {
				String element = fileContents.get(x);
				if (element.startsWith("client_key=")) {
					txtClientKey.setText(element.replace("client_key=", ""));
					util.log("Found Client Key.");
				} else if (element.startsWith("secret_key=")) {
					txtSecretKey.setText(element.replace("secret_key=", ""));
					util.log("Found Secret Key.");
				} else if (element.startsWith("telegram_id=")) {
					txtTelegramId.setText(element.replace("telegram_id=", ""));
					util.log("Found Telegram ID.");
				} else if (!(element.startsWith("#")) && !(element.isEmpty())) {
					recipientsPane.append(element.toLowerCase().replace(" ", "_") + "\n");
					util.log("Loaded: " + element);
				}
			}
		} else {
			throw new JTelegramException();
		}
	}

	/**
	 * This parses the contents of the recipients
	 *
	 * @param input
	 * @return
	 */
	private String[] recipientsParse(String input) {
		JTelegramFetcher fetcher = new JTelegramFetcher();
		ArrayList<String> finalRecipients = new ArrayList<String>(0);
		String[] rawRecipients = input.split("\n");

		// Remove commented lines.
		ArrayList<String> unComments = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (!element.startsWith("#")) {
				unComments.add(element);
			}
		}
		rawRecipients = unComments.toArray(new String[unComments.size()]);

		// Process based on notChar
		String notChar = "/";

		// Form of all the nation we want in this bloody list.
		ArrayList<String> whitelist = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (!(element.startsWith(notChar))) {
				whitelist.add(element.toLowerCase().replace(" ", "_"));
			}
		}

		// Form a list of all nations we can't have in this bloody list.
		ArrayList<String> blacklist = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (element.startsWith(notChar)) {
				blacklist.add(element.replaceFirst(notChar, "").toLowerCase().replace(" ", "_"));
			}
		}

		// Expand the blacklist.
		String[] whitelistExpanded = expandList(fetcher, whitelist);
		String[] blacklistExpanded = expandList(fetcher, blacklist);

		// Only add from white-list if it does not appear on blacklist.
		for (String wList : whitelistExpanded) {
			boolean toAdd = true;

			for (String bList : blacklistExpanded) {
				if (wList.equals(bList)) {
					toAdd = false;
					break;
				}
			}

			if (toAdd) {
				finalRecipients.add(wList);
			}
		}

		return finalRecipients.toArray(new String[finalRecipients.size()]);
	}

	/**
	 * What it says on the tin. It expands the list given in recipients into a full list of nations.
	 *
	 * @param fetcher
	 * @param tagsList
	 */
	private String[] expandList(JTelegramFetcher fetcher, ArrayList<String> tagsList) {
		for (int x = 0; x < tagsList.size(); x++) {
			String element = tagsList.get(x).toLowerCase();
			if (element.startsWith("region:")) {
				try {
					String[] regionContentsArr = fetcher.getRegion(element.replace("region:", ""));
					List<String> regionContents = Arrays.asList(regionContentsArr);
					tagsList.addAll(regionContents);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch members of region " + element.replace("region:", "") + ".");
				}
			} else if (element.startsWith("wa:delegates")) {
				try {
					String[] delegatesArr = fetcher.getDelegates();
					List<String> delegates = Arrays.asList(delegatesArr);
					tagsList.addAll(delegates);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch WA delegates");
				}
			} else if (element.toLowerCase().startsWith("wa:nations") || element.startsWith("wa:members")) {
				try {
					String[] waNationsArr = fetcher.getWAMembers();
					List<String> waNations = Arrays.asList(waNationsArr);
					tagsList.addAll(waNations);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch WA members.");
				}
			} else if (element.startsWith("world:newnations")) {
				try {
					String[] newNationsArr = fetcher.getNew();
					List<String> newNations = Arrays.asList(newNationsArr);
					tagsList.addAll(newNations);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch new nations.");
				}
			}

		}

		// Remove duplicates
		Set<String> tagsSet = new LinkedHashSet<String>();
		tagsSet.addAll(tagsList);
		tagsList.clear();
		tagsList.addAll(tagsSet);

		// Return!
		return tagsList.toArray(new String[tagsList.size()]);
	}

	private String readProperties() throws IOException {
		Properties prop = new Properties();
		FileInputStream stream = new FileInputStream(new File(System.getProperty("user.dir") + "/config.properties"));
		util.log(System.getProperty("user.dir") + "/config.properties");
		prop.load(stream);
		return prop.getProperty("client_key");
	}

	private void writeProperties() throws IOException {
		Properties prop = new Properties();
		FileOutputStream output = new FileOutputStream(System.getProperty("user.dir") + "/config.properties");
		prop.setProperty("client_key", txtClientKey.getText());
		prop.store(output, "");
		output.close();
	}
}