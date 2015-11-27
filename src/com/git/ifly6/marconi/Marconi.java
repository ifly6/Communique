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

package com.git.ifly6.marconi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.stream.Stream;

import org.apache.commons.cli.Options;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.CommuniquéFileReader;
import com.git.ifly6.communique.CommuniquéFileWriter;
import com.git.ifly6.communique.CommuniquéParser;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JTelegramException;

public class Marconi {

	public static final int version = CommuniquéParser.getVersion();

	static MarconiLogger util = new MarconiLogger();
	static JavaTelegram client = new JavaTelegram(util);
	static JTelegramKeys keys = new JTelegramKeys();
	static String[] recipients = {};

	static boolean isRecruitment = true;
	static boolean randomSort = false;
	static boolean skipChecks = false;

	static boolean recruitSet = false;
	static boolean sortSet = false;

	public static void main(String[] args) {

		// Define Options
		Options options = new Options();
		options.addOption("h", "Provides assistance");
		options.addOption("s", "Skips all checks for confirmation such that the command immediately executes");
		options.addOption("R", "Forces recruitment time (180 seconds) for the sending list");
		options.addOption("C", "Forces campaign time (30 seconds) for the sending list");
		options.addOption("r", "Forces randomisation of the sending list");
		options.addOption("a", "Forces campaign time (30 seconds) for the sending list");

		if (args.length == 0) {
			util.err("Runtime Error. Please provide a single valid Communiqué configuration file of version " + version + ".");
			quitFailed();
		}

		ArrayList<String> rawFlags = new ArrayList<String>();
		ArrayList<String> fileList = new ArrayList<String>();

		// Sort out flags.
		for (String element : args) {
			if (element.startsWith("-")) {
				rawFlags.add(element);
			} else {
				fileList.add(element);
			}
		}
		String[] flags = rawFlags.toArray(new String[rawFlags.size()]);
		String[] files = fileList.toArray(new String[rawFlags.size()]);

		// Process flags
		for (String element : flags) {
			if (element.equals("--help") || element.equals("-h")) {
				System.out.println("Usage: screen -S [name] java -jar Marconi_" + version + ".jar [-options] file\n" + "Options:\n"
						+ "  -s \t\t\tSkips all checks for confirmation such that the command immediately executes\n"
						+ "  -R \t\t\tForces recruitment time (180 seconds) for the sending list. Only works with -s\n"
						+ "  -C \t\t\tForces campaign time (30 seconds) for the sending list. Only works with -s\n"
						+ "  -r \t\t\tForces randomisation of the sending list. Only works with -s\n"
						+ "  -a=[delegates,new] \tEnables the program to execute commands autonomously");
				quitFailed();
			} else {
				if (element.equals("-s")) {
					skipChecks = true;
				}
				if (element.equals("-r")) {
					randomSort = true;
				}
				if (element.equals("-R")) {
					isRecruitment = true;
				}
				if (element.equals("-C")) {
					isRecruitment = false;
				}
			}
		}

		// Load in information and keys from configuration file
		try {
			loadConfig(new File(files[0]));
		} catch (FileNotFoundException e) {
			util.err("Internal Error. File either does not exist or is in the incorrect encoding.");
		} catch (JTelegramException e) {
			util.err("Incorrect file version. This is a version " + version + " client.");
		}

		// Make sure there is only one configuration file to speak of
		if (fileList.size() > 1) {
			util.err("Runtime Error. Please provide a single valid Communiqué configuration file of version " + version + ".");
			quitFailed();
		}

		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override public void run() {
				try {
					appendSent(new File(files[0]));
				} catch (FileNotFoundException | UnsupportedEncodingException e) {
					util.err("Internal Error. File either does not exist or is in the incorrect encoding.");
				}
			}
		});

		// If we are not to check the keys, check.
		if (!skipChecks) {
			manualFlagCheck();
		}

		// Process the Recipients list into a string with two columns.
		CommuniquéParser parser = new CommuniquéParser(util);
		String[] expandedRecipients = parser.recipientsParse(recipients);

		if (randomSort) {
			expandedRecipients = CommuniqueUtilities.randomiseArray(expandedRecipients);
		}

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
				quitFailed();
			}
		}

		// Set the client up and go.
		client.setKeys(keys);
		client.setRecruitment(isRecruitment);
		client.setRecipients(expandedRecipients);

		client.connect();

		// Update the configuration file to reflect the changed reality.
		try {
			appendSent(new File(files[0]));
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			util.err("Internal Error. File either does not exist or is in the incorrect encoding.");
		}
	}

	/**
	 * Should the problem be prompted to manually check all flags,
	 */
	private static void manualFlagCheck() {
		// Give a chance to check the keys.
		String keysResponse = util.prompt(
				"Are these keys correct? " + keys.getClientKey() + ", " + keys.getSecretKey() + ", " + keys.getTelegramId() + " [Yes] or [No]?",
				new String[] { "yes", "no", "y", "n" });
		if (!keysResponse.startsWith("y")) {
			quitFailed();
		}

		// Confirm the recruitment flag.
		while (true) {
			String recruitmentResponse = util.prompt("Is the current recruitment flag (" + isRecruitment + ") set correctly? [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });

			if (recruitmentResponse.startsWith("n")) {
				isRecruitment = !isRecruitment;
			} else if (recruitmentResponse.startsWith("y")) {
				break;
			}
		}

		// Confirm the randomisation flag.
		while (true) {
			String randomResponse = util.prompt("Is the current randomisation flag (" + randomSort + ") set correctly? [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });

			if (randomResponse.startsWith("n")) {
				randomSort = !randomSort;
			} else if (randomResponse.startsWith("y")) {
				break;
			}
		}
	}

	/**
	 * @param file to where this program will write.
	 * @throws FileNotFoundException if the file cannot be found
	 * @throws UnsupportedEncodingException if the file's encoding is unsupported
	 */
	private static void appendSent(File file) throws FileNotFoundException, UnsupportedEncodingException {
		String[] sentList = client.getSentList();
		for (int x = 0; x < sentList.length; x++) {
			sentList[x] = "/" + sentList[x];
		}

		String[] body = Stream.concat(Arrays.stream(recipients), Arrays.stream(sentList)).toArray(String[]::new);

		CommuniquéFileWriter fileWriter = new CommuniquéFileWriter(file, keys, isRecruitment, body);
		fileWriter.write();
	}

	/**
	 * @param file at which this program will look.
	 * @throws FileNotFoundException
	 * @throws JTelegramException if the fileReader is not compatible
	 */
	private static void loadConfig(File file) throws FileNotFoundException, JTelegramException {
		CommuniquéFileReader fileReader = new CommuniquéFileReader(file);
		keys = fileReader.getKeys();
		recipients = fileReader.getRecipients();
		isRecruitment = fileReader.getRecruitmentFlag();
		randomSort = fileReader.getRandomSortFlag();
	}

	private static void quitFailed() {
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			System.exit(0);
		}
		System.exit(0);
	}
}
