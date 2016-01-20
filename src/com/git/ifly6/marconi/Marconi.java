/* Copyright (c) 2015 ifly6
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

package com.git.ifly6.marconi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.CommuniquéFileReader;
import com.git.ifly6.communique.CommuniquéFileWriter;
import com.git.ifly6.communique.CommuniquéParser;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JTelegramException;

public class Marconi {

	public static final int version = CommuniquéParser.getVersion();
	private static String jarLocation = "Marconi_" + version + ".jar";
	private static File execConfiguration;

	private static MarconiLogger util = new MarconiLogger();
	private static JavaTelegram client = new JavaTelegram(util);
	private static JTelegramKeys keys = new JTelegramKeys();
	private static String[] recipients = {};

	private static Options options = new Options();
	private static HelpFormatter helpFormatter = new HelpFormatter();

	private static boolean isRecruitment = true;
	private static boolean randomSort = false;
	private static boolean skipChecks = false;

	private static boolean recruitSet = false;
	private static boolean sortSet = false;

	public static void main(String[] args) {

		if (args.length == 0) {
			util.err("Runtime Error. Please provide a single valid Communiqué configuration file of version " + version + ".");
			quit();
		}

		CommandLineParser cliParse = new DefaultParser();

		// Define execution options
		options.addOption("h", "help", false, "Displays this message");
		options.addOption("S", "skip", false, "Skips all checks for confirmation such that the command immediately executes");
		options.addOption("I", false, "Forces an opporitunity to double-check all keys");
		options.addOption("R", false, "Forces recruitment time (180 seconds) for the sending list");
		options.addOption("C", false, "Forces campaign time (30 seconds) for the sending list");
		options.addOption("r", false, "Forces randomisation of the sending list");
		options.addOption("s", "seq", false, "Forces sequential sending of the sending list");
		options.addOption("a", "auto", false, "Allows program to autonomously update the sending list");
		options.addOption("v", "version", false, "Prints version");

		try {
			CommandLine commandLine = cliParse.parse(options, args);

			// Deal with options
			if (commandLine.hasOption("h")) {
				quit();
			}
			if (commandLine.hasOption("S")) {
				// This option is overridden by the interactivity flag
				skipChecks = true;
			}
			if (commandLine.hasOption("I")) {
				// This option overrides the attempt to skip checks
				skipChecks = false;
			}
			if (commandLine.hasOption("C")) {
				// This option is overridden by the recruitment-delay setting
				isRecruitment = false;
				recruitSet = true;
			}
			if (commandLine.hasOption("R")) {
				// This option overrides the campaign-delay setting
				isRecruitment = true;
				recruitSet = true;
			}
			if (commandLine.hasOption("r")) {
				randomSort = true;
				sortSet = true;
			}
			if (commandLine.hasOption("s")) {
				randomSort = false;
				sortSet = true;
			}
			if (commandLine.hasOption("a")) {
				// IMPLEMENT.
			}
			if (commandLine.hasOption("v")) {
				System.out.println("Marconi version " + version + "\n" + "Please visit https://github.com/iFlyCode/Communique/releases.");
				System.exit(0);
			}

			// Deal with the argument
			String[] fileList = commandLine.getArgs();
			if (fileList.length != 1) {
				quitMessage("Please only provide ONE file argument to the program.");
			} else {
				execConfiguration = new File(fileList[0]);
			}
		} catch (ParseException e1) {
			util.err("Error. Failed to parse options and commands.");
		}

		// Load in information and keys from configuration file
		try {
			loadConfig(execConfiguration);
		} catch (FileNotFoundException e) {
			util.err("Internal Error. File either does not exist or is in the incorrect encoding.");
		} catch (JTelegramException e) {
			util.err("Incorrect file version. This is a version " + version + " client.");
		}

		// When asked to shut down, write the sentList to disc
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					appendSent(execConfiguration);
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					util.err("Internal Error. File either does not exist or is in the incorrect encoding. File not saved in shutdown.");
				}
			}
		});

		// If we are to check the keys, check.
		if (!skipChecks) {
			manualFlagCheck();
		}

		// Process the Recipients list into a string with two columns.
		CommuniquéParser parser = new CommuniquéParser(util);
		String[] expandedRecipients = parser.recipientsParse(recipients);

		if (randomSort) {
			expandedRecipients = CommuniqueUtilities.randomiseArray(expandedRecipients);
		}

		// Show the recipients in the order we are to send the telegrams.
		System.out.println();
		for (int x = 0; x < expandedRecipients.length; x = x + 2) {
			try {
				System.out.printf("%-30.30s  %-30.30s%n", expandedRecipients[x], expandedRecipients[x + 1]);
			} catch (IndexOutOfBoundsException e) {
				System.out.printf(expandedRecipients[x] + "\n");
			}
		}

		System.out.println();
		System.out.println(
				"This will take " + CommuniqueUtilities.time((int) Math.round(expandedRecipients.length * ((isRecruitment) ? 180.05 : 30.05)))
						+ " to send " + expandedRecipients.length + " telegrams.");

		if (!skipChecks) {
			// Give a chance to check the recipients.
			String recipientsReponse = util.prompt("Are you sure you want to send to these recipients? [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });
			if (recipientsReponse.startsWith("n")) {
				quit();
			}
		}

		// Set the client up and go.
		client.setKeys(keys);
		client.setRecruitment(isRecruitment);
		client.setRecipients(expandedRecipients);

		client.connect();

		// Update the configuration file to reflect the changed reality.
		try {
			appendSent(execConfiguration);
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			util.err("Internal Error. File either does not exist or is in the incorrect encoding.");
		}
	}

	/**
	 * Should the problem be prompted to manually check all flags, this method does so, retrieving the flags and asking
	 * for the user to reconfirm them.
	 */
	private static void manualFlagCheck() {
		// Give a chance to check the keys.
		String keysResponse = util.prompt(
				"Are these keys correct? " + keys.getClientKey() + ", " + keys.getSecretKey() + ", " + keys.getTelegramId() + " [Yes] or [No]?",
				new String[] { "yes", "no", "y", "n" });
		if (!keysResponse.startsWith("y")) {
			quit();
		}

		// Confirm the recruitment flag.
		while (true) {
			String recruitmentResponse = util.prompt("Is the recruitment flag (" + isRecruitment + ") set correctly? [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });

			if (recruitmentResponse.startsWith("n")) {
				isRecruitment = !isRecruitment;
			} else if (recruitmentResponse.startsWith("y")) {
				break;
			}
		}

		// Confirm the randomisation flag.
		while (true) {
			String randomResponse = util.prompt("Is the randomisation flag (" + randomSort + ") set correctly? [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });

			if (randomResponse.startsWith("n")) {
				randomSort = !randomSort;
			} else if (randomResponse.startsWith("y")) {
				break;
			}
		}
	}

	/**
	 * Gets the list of recipients to which the client has already sent telegrams, appends that to the sent list, then
	 * creates a <code>CommuniquéFileWriter</code> to write that new payload to disc.
	 *
	 * @param file to where this program will write.
	 * @throws FileNotFoundException if the file cannot be found
	 * @throws UnsupportedEncodingException if the file's encoding is unsupported
	 */
	private static void appendSent(File file) throws FileNotFoundException, UnsupportedEncodingException {
		String[] sentList = client.getSentList();
		for (int x = 0; x < sentList.length; x++) {
			sentList[x] = "/" + sentList[x];
		}

		String[] body = new String[recipients.length + sentList.length];
		System.arraycopy(recipients, 0, body, 0, recipients.length);
		System.arraycopy(sentList, 0, body, recipients.length, sentList.length);

		CommuniquéFileWriter fileWriter = new CommuniquéFileWriter(file, keys, isRecruitment, body);
		fileWriter.write();
	}

	/**
	 * Loads configuration and sets all flags based off that configuration. Only sets flags given that the
	 * <code>-s</code> option is not already set.
	 *
	 * @param file at which this program will look.
	 * @throws FileNotFoundException
	 * @throws JTelegramException if the fileReader is not compatible
	 */
	private static void loadConfig(final File file) throws FileNotFoundException, JTelegramException {
		CommuniquéFileReader fileReader = new CommuniquéFileReader(file);
		keys = fileReader.getKeys();
		recipients = fileReader.getRecipients();

		if (!sortSet) {
			randomSort = fileReader.getRandomSortFlag();
		}

		if (!recruitSet) {
			isRecruitment = fileReader.getRecruitmentFlag();
		}
	}

	/**
	 * Prints out help information to <code>System.out</code> using the Apache Commons CLI library, then quits the
	 * application.
	 */
	private static void quit() {
		helpFormatter.printHelp("java -jar " + jarLocation + " [options] <FILE>", options);
		System.exit(0);
	}

	/**
	 * Prints out help information to <code>System.out</code> using the Apache Commons CLI library, preceded by a
	 * message, then quits the application.
	 *
	 * @param message which prefaces the help text
	 */
	private static void quitMessage(final String message) {
		util.err(message);
		quit();
	}
}
