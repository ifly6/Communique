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
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
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
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;

import org.apache.commons.lang3.SystemUtils;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.CConfig;
import com.git.ifly6.communique.io.CLoader;
import com.git.ifly6.javatelegram.JTelegramKeys;

/**
 * <code>Communiqué</code> is the main class of the Communiqué system. It handles the GUI aspect of the entire program
 * and other actions.
 */
public class Communique {

	private static final Logger log = Logger.getLogger(Communique.class.getName());

	private JFrame frame;
	private JTextField txtClientKey;
	private JTextField txtSecretKey;
	private JTextField txtTelegramId;
	private JPanel panel;
	private JProgressBar progressBar;

	private JTextArea txtrCode;
	private JCheckBoxMenuItem chckbxmntmRandomiseRecipients;
	private JCheckBoxMenuItem chckbxmntmPrioritiseDelegates;
	private JCheckBox chckbxRecruitment;

	private static Path appSupport;
	private static CGuiLogger cLogger = new CGuiLogger();

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {

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
		frame.setBounds(100, 100, (int) sWidth / 2, (int) sHeight / 2);
		frame.setMinimumSize(new Dimension(400, 500));
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
		txtClientKey.setText("Client Key");
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

		JButton btnSend = new JButton("SEND");
		btnSend.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
			}
		});

		chckbxRecruitment = new JCheckBox("Recruitment");
		controlPanel.add(chckbxRecruitment);
		controlPanel.add(btnSend);

		JPanel dataPanel = new JPanel();
		contentPane.add(dataPanel, BorderLayout.CENTER);
		dataPanel.setLayout(new GridLayout(1, 0, 5, 5));

		txtrCode = new JTextArea();
		txtrCode.setText("Code");
		txtrCode.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtrCode.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		dataPanel.add(new JScrollPane(txtrCode));

		panel = new JPanel();
		dataPanel.add(panel);
		panel.setLayout(new BorderLayout(0, 0));

		JTextArea txtrRecipients = new JTextArea();
		txtrRecipients.setText("Recipients");
		txtrRecipients.setFont(new Font(Font.MONOSPACED, 0, 11));
		txtrRecipients.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.add(new JScrollPane(txtrRecipients), BorderLayout.CENTER);

		// Document parser for above
		txtrCode.getDocument().addDocumentListener(new DocumentListener() {

			CommuniqueParser parser = new CommuniqueParser(cLogger);

			@Override public void removeUpdate(DocumentEvent e) {
				callParser(e);
			}

			@Override public void insertUpdate(DocumentEvent e) {
				callParser(e);
			}

			@Override public void changedUpdate(DocumentEvent e) {
			}

			private void callParser(DocumentEvent e) {
				log.info("Called parser");
				String[] output = parser.recipientsParse(txtrCode.getText().split("\n"));
				txtrRecipients.setText(CommuniqueUtilities.joinListWith(Arrays.asList(output), '\n'));
			}
		});

		progressBar = new JProgressBar();
		panel.add(progressBar, BorderLayout.SOUTH);

		JMenuBar menuBar = new JMenuBar();
		frame.setJMenuBar(menuBar);

		JMenu mnFile = new JMenu("File");
		menuBar.add(mnFile);

		JMenuItem mntmSave = new JMenuItem("Save");
		mnFile.add(mntmSave);

		JMenuItem mntmOpen = new JMenuItem("Open");
		mnFile.add(mntmOpen);

		mnFile.addSeparator();

		JMenuItem mntmShowDirectory = new JMenuItem("Show Directory");
		mnFile.add(mntmShowDirectory);

		JMenuItem mntmExportLog = new JMenuItem("Export Log");
		mnFile.add(mntmExportLog);

		mnFile.addSeparator();

		JMenuItem mntmQuit = new JMenuItem("Quit");
		mnFile.add(mntmQuit);

		JMenu mnData = new JMenu("Data");
		menuBar.add(mnData);

		JMenuItem mntmImportKeysFrom = new JMenuItem("Import Keys from URL");
		mnData.add(mntmImportKeysFrom);

		JMenu mnImportRecipients = new JMenu("Import Recipients");
		mnData.add(mnImportRecipients);

		JMenuItem mntmFromWaDelegate = new JMenuItem("From WA Delegate List");
		mntmFromWaDelegate.addActionListener(new ActionListener() {
			@Override public void actionPerformed(ActionEvent e) {
				appendCode("wa:delegates");
			}
		});
		mnImportRecipients.add(mntmFromWaDelegate);

		JMenuItem mntmFromAtVote = new JMenuItem("From At Vote Screen");
		mntmFromAtVote.addActionListener(new ActionListener() {
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

		log.finer("Communiqué config exported.");
		return config;

	}

	private void appendCode(String input) {
		txtrCode.append("\n" + input);
	}

	private String[][] filterSents(String[] input) {

		List<String> inputList = Arrays.asList(input);

		List<String> recipients = new ArrayList<>();
		List<String> sents = new ArrayList<>();

		for (int x = 0; x < inputList.size(); x++) {

			if (!inputList.get(x).startsWith("#")) {

				if (!inputList.get(x).startsWith("/")) {
					recipients.add(inputList.get(x));

				} else {
					sents.add(inputList.get(x));
				}

			}

		}

		return new String[][] { recipients.toArray(new String[recipients.size()]), sents.toArray(new String[sents.size()]) };
	}
}
