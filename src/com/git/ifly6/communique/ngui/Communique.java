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
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.Event;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.SystemUtils;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.CConfig;
import com.git.ifly6.communique.io.CLoader;
import com.git.ifly6.communique.io.CNetLoader;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;

/**
 * <code>Communiqué</code> is the main class of the Communiqué system. It handles the GUI aspect of the entire program
 * and other actions.
 */
public class Communique implements JTelegramLogger {

	private static final Logger log = Logger.getLogger(Communique.class.getName());

	private boolean parsed = false;
	private JavaTelegram client;
	private Thread sendingThread = new Thread();

	private JFrame frame;
	private JPanel panel;

	private JTextField txtClientKey;
	private JTextField txtSecretKey;
	private JTextField txtTelegramId;

	private JButton btnSend;
	private JProgressBar progressBar;
	private JTextArea txtrCode;

	private JCheckBoxMenuItem chckbxmntmRandomiseRecipients;
	private JCheckBoxMenuItem chckbxmntmPrioritiseDelegates;
	private JCheckBox chckbxRecruitment;

	private String[] parsedRecipients;

	private static Path appSupport;

	private static String codeHeader = "# == Communiqué Recipients Code ==\n"
			+ "# Enter recipients, one for each line or use 'region:', 'WA:', etc tags.\n"
			+ "# Use '/' to say: 'not'. Ex: 'region:europe, /imperium anglorum'.\n\n";
	private static String recipientsHeader = "# == Communiqué Recipients ==\n"
			+ "# This tab shows all recipients after parsing of the Code tab.\n\n";

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException lfE) {
			lfE.printStackTrace();
		}

		if (SystemUtils.IS_OS_WINDOWS) {
			appSupport = Paths.get(System.getenv("LOCALAPPDATA"));

		} else if (SystemUtils.IS_OS_MAC) {
			appSupport = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Communique");
			System.setProperty("apple.laf.useScreenMenuBar", "true");

		} else {
			appSupport = Paths.get(System.getProperty("user.dir"), "config");
		}

		try {
			Files.createDirectories(appSupport);
		} catch (IOException e1) {
			e1.printStackTrace();
			log.warning("Cannot create directory");
		}

		EventQueue.invokeLater(new Runnable() {
			@Override public void run() {
				try {
					Communique window = new Communique();
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
	public Communique() {

		client = new JavaTelegram(this);
		initialize();

	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {

		frame = new JFrame();

		Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
		double sWidth = screenDimensions.getWidth();
		double sHeight = screenDimensions.getHeight();

		frame.setTitle("Communiqué " + CommuniqueParser.version);
		frame.setBounds(100, 100, (int) Math.round(2 * sWidth / 3), (int) Math.round(2 * sHeight / 3));
		frame.setMinimumSize(new Dimension(600, 400));
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		JPanel contentPane = new JPanel();
		contentPane.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		frame.setContentPane(contentPane);

		contentPane.setLayout(new BorderLayout(5, 5));

		JPanel controlPanel = new JPanel();
		contentPane.add(controlPanel, BorderLayout.SOUTH);
		controlPanel.setLayout(new GridLayout(1, 0, 0, 0));

		txtClientKey = new JTextField();
		txtClientKey.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtClientKey.setText(CLoader.readProperties());
		controlPanel.add(txtClientKey);
		txtClientKey.setColumns(10);

		txtSecretKey = new JTextField();
		txtSecretKey.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtSecretKey.setText("Secret Key");
		controlPanel.add(txtSecretKey);
		txtSecretKey.setColumns(10);

		txtTelegramId = new JTextField();
		txtTelegramId.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtTelegramId.setText("Telegram ID");
		controlPanel.add(txtTelegramId);
		txtTelegramId.setColumns(10);

		btnSend = new JButton("Parse");

		chckbxRecruitment = new JCheckBox("Recruitment");
		chckbxRecruitment.addActionListener(l -> {
			triggerParsed(false);
		});
		controlPanel.add(chckbxRecruitment);
		controlPanel.add(btnSend);

		JPanel dataPanel = new JPanel();
		contentPane.add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new GridLayout(1, 0, 5, 5));

		txtrCode = new JTextArea();
		txtrCode.setText(
				"# == Communiqué Recipients Code ==\n# Enter recipients, one for each line or use 'region:', 'WA:', etc tags.\n# Use '/' to say: 'not'. Ex: 'region:europe, /imperium anglorum'.\n\n");
		txtrCode.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtrCode.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dataPanel.add(new JScrollPane(txtrCode));

		panel = new JPanel();
		dataPanel.add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JTextArea txtrRecipients = new JTextArea();
		txtrRecipients.setText(
				"# == Communiqué Recipients ==\n# This tab shows all recipients after parsing of the Code tab.\n\n");
		txtrRecipients.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtrRecipients.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(new JScrollPane(txtrRecipients), BorderLayout.CENTER);

		progressBar = new JProgressBar();
		panel.add(progressBar, BorderLayout.SOUTH);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSave = new JMenuItem("Save");
		if (SystemUtils.IS_OS_MAC) {
			mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.META_MASK));
		} else {
			mntmSave.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_S, Event.CTRL_MASK));
		}
		mntmSave.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {

				FileDialog fDialog = new FileDialog(frame, "Save file as...", FileDialog.SAVE);
				fDialog.setDirectory(appSupport.toFile().toString());
				fDialog.setVisible(true);

				String file = fDialog.getFile();
				if (file == null) {
					log.info("User cancelled file save dialog");

				} else {

					Path savePath = appSupport.toAbsolutePath().resolve(file);
					log.info("User elected to save file at " + savePath.toAbsolutePath().toString());

					CLoader loader = new CLoader(savePath);
					try {
						loader.save(exportState());

					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		});
		mnFile.add(mntmSave);

		JMenuItem mntmOpen = new JMenuItem("Open");

		if (SystemUtils.IS_OS_MAC) {
			mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.META_MASK));
		} else {
			mntmOpen.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_O, Event.CTRL_MASK));
		}

		mntmOpen.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {

				FileDialog fDialog = new FileDialog(frame, "Load file...", FileDialog.LOAD);
				fDialog.setDirectory(appSupport.toFile().toString());
				fDialog.setVisible(true);

				String file = fDialog.getFile();
				if (file == null) {
					log.info("User cancelled file load dialog");

				} else {

					Path savePath = appSupport.resolve(file);
					log.info("User elected to load file at " + savePath.toAbsolutePath().toString());

					CLoader loader = new CLoader(savePath);
					try {
						importState(loader.load());

					} catch (IOException e1) {
						e1.printStackTrace();
					}

				}
			}
		});
		mnFile.add(mntmOpen);

		mnFile.addSeparator();

		JMenuItem mntmShowDirectory = new JMenuItem("Show Directory");
		mntmShowDirectory.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().open(appSupport.toFile());
				} catch (IOException e1) {
					e1.printStackTrace();
				}
			}
		});
		mnFile.add(mntmShowDirectory);

		JMenuItem mntmExportLog = new JMenuItem("Export Log");
		mnFile.add(mntmExportLog);

		mnFile.addSeparator();

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mntmQuit.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				System.exit(0);
			}
		});
		mnFile.add(mntmQuit);

		JMenu mnData = new JMenu("Data");
		menuBar.add(mnData);

		JMenuItem mntmImportKeysFrom = new JMenuItem("Import Keys from Telegram URL");
		mntmImportKeysFrom.addActionListener(new ActionListener() {

			@Override public void actionPerformed(ActionEvent e) {

				String rawURL = JOptionPane
						.showInputDialog("Paste in keys from the URL provided by receipt by the Telegrams API");

				// Verify that it is a valid NationStates URL
				if (rawURL.startsWith("http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&")) {

					rawURL = rawURL
							.replace("http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&", "");
					rawURL = rawURL.replace("&to=NATION_NAME", "");

					String[] tags = rawURL.split("&");
					txtTelegramId.setText(tags[0].substring(tags[0].indexOf("=") + 1, tags[0].length()));
					txtSecretKey.setText(tags[1].substring(tags[1].indexOf("=") + 1, tags[1].length()));

				} else {
					Alert alert = new Alert(AlertType.ERROR);
					alert.setTitle("Communiqué Error");
					alert.setHeaderText("Please input a correct URL.");
					alert.showAndWait();
				}
			}
		});
		mnData.add(mntmImportKeysFrom);

		JMenu mnImportRecipients = new JMenu("Import Recipients");
		mnData.add(mnImportRecipients);

		JMenuItem mntmFromWaDelegate = new JMenuItem("From WA Delegate List");
		mntmFromWaDelegate.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				appendCode("wa:delegates");
			}
		});

		JMenuItem mntmAsCommaSeparated = new JMenuItem("As Comma Separated List");
		mntmAsCommaSeparated.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				String input = JOptionPane.showInputDialog("Input the string of delegates:");
				if (input != null) {

					input = input.replaceAll("\\(.+?\\)", "");
					String[] list = input.split(",");

					for (String element : list) {
						appendCode(element.toLowerCase().trim().replace(" ", "_"));
					}
				}
			}
		});
		mnImportRecipients.add(mntmAsCommaSeparated);
		mnImportRecipients.add(mntmFromWaDelegate);

		JMenuItem mntmFromAtVote = new JMenuItem("From At Vote Screen");
		mntmFromAtVote.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {

				Object[] possibilities = { "GA For", "GA Against", "SC For", "SC Against" };
				String s = (String) JOptionPane.showInputDialog(frame, "Select which chamber and side you want to address:",
						"Select Chamber and Side", JOptionPane.PLAIN_MESSAGE, null, possibilities, "GA For");

				// If a string was returned, say so.
				if ((s != null) && (s.length() > 0)) {
					String[] elements = s.split(" ");
					if (elements.length > 0) {

						String chamber = (elements[0].equals("GA")) ? CNetLoader.GA : CNetLoader.SC;
						String side = (elements[1].equals("For")) ? CNetLoader.FOR : CNetLoader.AGAINST;

						try {
							txtrCode.append(CommuniqueUtilities
									.joinListWith(Arrays.asList(CNetLoader.importAtVoteDelegates(chamber, side)), '\n'));
						} catch (NullPointerException exc) {
							JOptionPane.showMessageDialog(Communique.this.frame,
									"Cannot import data from NationStates website.");
							log.warning("Cannot import data.");
						}

					}
				}

			}
		});
		mnImportRecipients.add(mntmFromAtVote);

		mnData.addSeparator();

		chckbxmntmRandomiseRecipients = new JCheckBoxMenuItem("Randomise Recipients");
		mnData.add(chckbxmntmRandomiseRecipients);

		chckbxmntmPrioritiseDelegates = new JCheckBoxMenuItem("Prioritise Delegates");
		mnData.add(chckbxmntmPrioritiseDelegates);

		JMenu mnHelp = new JMenu("Help");
		menuBar.add(mnHelp);

		JMenuItem mntmAbout = new JMenuItem("About");
		mntmAbout.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				new CTextDialog(frame, "About",
						"Developed by His Grace, Cyril Parsons, the Duke of Geneva and the staff of the Democratic Empire of Imperium Anglorum's "
								+ "Delegation to the World Assembly.\n" + "OOC: Created by ifly6.");
			}
		});
		mnHelp.add(mntmAbout);

		mnHelp.addSeparator();

		JMenuItem mntmDocumentation = new JMenuItem("Documentation");
		mntmDocumentation.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URL("https://github.com/iFlyCode/Communique#communiqué").toURI());
				} catch (IOException | URISyntaxException e1) {
					log.warning("Cannot open Communiqué documentation.");
					e1.printStackTrace();
				}
			}
		});
		mnHelp.add(mntmDocumentation);

		JMenuItem mntmForumThread = new JMenuItem("Forum Thread");
		mntmForumThread.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URI("http://forum.nationstates.net/viewtopic.php?f=15&t=352065"));
				} catch (IOException | URISyntaxException e1) {
					log.warning("Cannot open NationStates forum support thread for Communiqué.");
					e1.printStackTrace();
				}
			}
		});
		mnHelp.add(mntmForumThread);

		mnHelp.addSeparator();

		JMenuItem mntmClaims = new JMenuItem("Claims");
		mntmClaims.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				new CTextDialog(frame, "Claims",
						"The Software is provided 'as is', without warrant of any kind, express or implied, including but not limited to the"
								+ "warranties of merchantability, fitness for a particular purpose and noninfringement. In no even shall the authors or"
								+ "copyright holders be liable for any claim, damages, or other liability, whether in an action of contract, tort, or"
								+ "otherwise, arising from, out of, or in connect with the Software or the use or other dealings in the Software.");
			}
		});
		mnHelp.add(mntmClaims);

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {

					// Save it to application support
					CLoader loader = new CLoader(appSupport.resolve("autosave.txt"));
					loader.save(exportState());

				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		});

		// Parse action for above
		btnSend.addActionListener(new ActionListener() {

			@Override public void actionPerformed(ActionEvent e) {

				CommuniqueParser parser = new CommuniqueParser(Communique.this);

				if (!parsed) {

					// Call and do the parsing
					log.info("Called parser");
					Communique.this.parsedRecipients = parser.recipientsParse(txtrCode.getText().split("\n"));

					// Estimate Time Needed
					double numRecipients = parsedRecipients.length;
					int seconds = (int) Math.round(numRecipients * ((chckbxRecruitment.isSelected()) ? 180.05 : 30.05));
					String timeNeeded = CommuniqueUtilities.time(seconds);

					// If it needs to be randomised, do so.
					if (chckbxmntmRandomiseRecipients.isSelected()) {
						parsedRecipients = CommuniqueUtilities.randomiseArray(parsedRecipients);
					}

					// Show Recipients
					StringBuilder builder = new StringBuilder();
					builder.append("# == Communiqué Recipients ==\n" + "# This tab shows all " + parsedRecipients.length
							+ " recipients after parsing of the Code tab.\n# Estimated time needed is " + timeNeeded
							+ "\n\n");
					for (String element : parsedRecipients) {
						builder.append(element + "\n");
					}

					log.info("Recipients Parsed.");

					// Change GUI elements
					txtrRecipients.setText(builder.toString());
					triggerParsed(true);

				} else {

					// sending logic
					if (!sendingThread.isAlive()) {
						client.setKillThread(false);

						Runnable runner = new Runnable() {
							@Override public void run() {

								client.setRecipients(parsedRecipients);	// Set recipients
								client.setKeys(new JTelegramKeys(txtClientKey.getText(), txtSecretKey.getText(),
										txtTelegramId.getText()));

								// Set Recruitment Status
								client.setRecruitment(chckbxRecruitment.isSelected());

								// Save client key
								try {
									CLoader.writeProperties(txtClientKey.getText());
									CLoader loader = new CLoader(appSupport.resolve("autosave.txt"));
									loader.save(exportState());

								} catch (IOException e) {
									System.err.println("Exception in writing autosave or properties file.");
								}

								client.connect();
								log.info("Queries Complete.");
								JOptionPane.showMessageDialog(Communique.this.frame,
										"Queries to " + (progressBar.getValue() + 1) + " nations complete.");

								// Reset the progress bar
								progressBar.setValue(0);
								progressBar.setMaximum(0);

								// Update code has been removed because of a change to the JTelegramLogger which allows
								// for direct event dispatch.
							}
						};

						sendingThread = new Thread(runner);
						sendingThread.start();

					} else {
						log.info("There is already a campaign running. Terminate that campaign and then retry.");
					}

				}
			}
		});

		// Document listener logic
		txtrCode.getDocument().addDocumentListener(new DocumentListener() {

			@Override public void removeUpdate(DocumentEvent e) {
				executeInTheKingdomOfNouns();
			}

			@Override public void insertUpdate(DocumentEvent e) {
				executeInTheKingdomOfNouns();
			}

			@Override public void changedUpdate(DocumentEvent e) {
			}

			// It's a joke, look here: http://steve-yegge.blogspot.com/2006/03/execution-in-kingdom-of-nouns.html
			public void executeInTheKingdomOfNouns() {
				if (parsed) {	// to avoid constant resetting of the variable
					triggerParsed(false);
				}
			}
		});

		log.info("Shutdown hook added.");
		log.info("Communiqué loaded.");
	}

	public CConfig exportState() {

		CConfig config = new CConfig();

		config.isRecruitment = chckbxRecruitment.isSelected();
		config.isRandomised = chckbxmntmRandomiseRecipients.isSelected();
		config.isDelegatePrioritised = chckbxmntmPrioritiseDelegates.isSelected();
		config.keys = new JTelegramKeys(txtClientKey.getText(), txtSecretKey.getText(), txtTelegramId.getText());

		String[][] recipientsAndSents = filterSents(txtrCode.getText().split("\n"));

		config.recipients = recipientsAndSents[0];
		config.sentList = recipientsAndSents[1];

		config.defaultVersion();

		log.info("Communiqué config exported.");
		return config;

	}

	public void importState(CConfig config) {

		chckbxRecruitment.setSelected(config.isRecruitment);
		chckbxmntmRandomiseRecipients.setSelected(config.isRandomised);
		chckbxmntmPrioritiseDelegates.setSelected(config.isDelegatePrioritised);

		txtClientKey.setText(config.keys.getClientKey());
		txtSecretKey.setText(config.keys.getSecretKey());
		txtTelegramId.setText(config.keys.getTelegramId());

		txtrCode.setText(codeHeader + CommuniqueUtilities.joinListWith(Arrays.asList(config.recipients), '\n'));

		// Format the universal negation.
		for (int x = 0; x < config.sentList.length; x++) {
			config.sentList[x] = "/" + config.sentList[x];
		}
		txtrCode.append("\n\n" + CommuniqueUtilities.joinListWith(Arrays.asList(config.sentList), '\n'));

		log.info("Communique info imported");

	}

	private void appendCode(String input) {
		txtrCode.append("\n" + input);
	}

	private String[][] filterSents(String[] input) {

		List<String> inputList = Arrays.asList(input);

		List<String> recipients = new ArrayList<>();
		List<String> sents = new ArrayList<>();

		for (int x = 0; x < inputList.size(); x++) {

			if (StringUtils.isEmpty(inputList.get(x)) && !inputList.get(x).startsWith("#")) {

				if (!inputList.get(x).startsWith("/")) {
					recipients.add(inputList.get(x));

				} else {
					sents.add(inputList.get(x).replaceFirst("/", ""));
				}

			}

		}

		return new String[][] { recipients.toArray(new String[recipients.size()]), sents.toArray(new String[sents.size()]) };
	}

	// Changes the state of the button to reflect whether it is ready to send and or parsed
	private void triggerParsed(boolean parsed) {
		this.parsed = parsed;
		btnSend.setText((parsed) ? "Send" : "Parse");
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String)
	 */
	@Override public void log(String input) {
		log.info(input);
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int)
	 */
	@Override public void sentTo(String recipient, int x, int length) {

		if (!(progressBar.getMaximum() == length - 1)) {
			progressBar.setMaximum(length - 1);
		}

		txtrCode.append((x == 0) ? "\n\n/" + recipient : "\n/" + recipient);
		progressBar.setValue(x);
	}
}
