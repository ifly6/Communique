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
import java.util.Arrays;
import java.util.stream.Stream;

import com.git.ifly6.communique.CommuniquéFileReader;
import com.git.ifly6.communique.CommuniquéFileWriter;
import com.git.ifly6.communique.CommuniquéParser;
import com.git.ifly6.javatelegram.JTelegramKeys;
import com.git.ifly6.javatelegram.JavaTelegram;
import com.git.ifly6.javatelegram.util.JTelegramException;

public class Marconi {

	static MarconiLogger util = new MarconiLogger();
	static JavaTelegram client = new JavaTelegram(util);
	static JTelegramKeys keys = new JTelegramKeys();
	static String[] recipients = {};
	static boolean isRecruitment = true;
	public static final int version = CommuniquéParser.getVersion();

	public static void main(String[] args) {
		if (args.length == 1) {	// If there is not a provided file, do nothing.

			try {
				loadConfig(new File(args[0]));		// Load the keys and recipients from the configuration file in.
			} catch (FileNotFoundException e) {
				util.err("Cannot find your file. Provide a real file and try again.");
			} catch (JTelegramException e) {
				util.err("Incorrect file version. This is a version " + version + " client.");
			}

			// Give a chance to check the keys.
			String keysResponse = util.prompt(
					"Are these keys correct? " + keys.getClientKey() + ", " + keys.getSecretKey() + ", " + keys.getTelegramId() + " [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });

			if (keysResponse.startsWith("y")) {

				while (true) {
					String recruitmentResponse = util.prompt("Is the current recruitment flag (" + isRecruitment + ") set correctly? [Yes] or [No]?",
							new String[] { "yes", "no", "y", "n" });

					if (recruitmentResponse.startsWith("n")) {
						isRecruitment = !isRecruitment;
					} else if (recruitmentResponse.startsWith("y")) {
						break;
					}
				}

				// Process the Recipients list into a string with two columns.
				CommuniquéParser parser = new CommuniquéParser(util);
				String[] expandedRecipients = parser.recipientsParse(recipients);
				System.out.println("");

				for (int x = 0; x < expandedRecipients.length; x = x + 2) {
					try {
						System.out.printf("%-30.30s  %-30.30s%n", expandedRecipients[x], expandedRecipients[x + 1]);
					} catch (IndexOutOfBoundsException e) {
						System.out.printf(expandedRecipients[x] + "\n");
					}
				}
				System.out.println("");
				System.out.println("This will take " + time((int) Math.round(expandedRecipients.length * ((isRecruitment) ? 180.05 : 30.05))));
				System.out.println("");

				// Give a chance to check the recipients.
				String recipientsReponse = util.prompt("Are you sure you want to send to these recipients? [Yes] or [No]?",
						new String[] { "yes", "no", "y", "n" });
				if (recipientsReponse.startsWith("y")) {

					// Set the client up and go.
					client.setKeys(keys);
					client.setRecruitment(isRecruitment);
					client.setRecipients(expandedRecipients);

					client.connect();

					// Update the configuration file to reflect the changed reality.
					try {
						appendSent(new File(args[0]));
					} catch (FileNotFoundException | UnsupportedEncodingException e) {
						util.err("Internal Error. File either does not exist or is in the incorrect encoding.");
					}

				} else {
					util.err("Please make any alterations needed and restart this program.");
				}
			} else {
				util.err("Please make any alterations needed and restart this program.");
			}
		} else {
			util.err("Please provide a single configuration file of the same type compatible with Communiqué " + version + " in the arguments.");
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
	}

	private static String time(int seconds) {
		int minutes = seconds / 60;
		seconds -= minutes * 60;
		int hours = minutes / 60;
		minutes -= hours * 60;
		int days = hours / 24;
		hours -= days * 24;
		return days + "d:" + hours + "h:" + minutes + "m:" + seconds + "s";
	}
}
