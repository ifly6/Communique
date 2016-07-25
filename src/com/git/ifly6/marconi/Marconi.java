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

import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.git.ifly6.communique.CommuniqueParser;
import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.io.CConfig;
import com.git.ifly6.communique.ngui.AbstractCommunique;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

public class Marconi extends AbstractCommunique implements JTelegramLogger {

	private MarconiLogger util = new MarconiLogger();
	private JavaTelegram client = new JavaTelegram(util);
	private CConfig config;

	private boolean skipChecks = false;

	public void setSkipChecks(boolean skipChecks) {
		this.skipChecks = skipChecks;
	}

	public void send() {

		// If we to check the keys, check.
		if (!skipChecks) {
			manualFlagCheck();
		}

		// Process the Recipients list into a string with two columns.
		CommuniqueParser parser = new CommuniqueParser();
		List<String> expandedRecipients = parser.recipientsParse(Arrays.asList(config.recipients),
				Arrays.asList(config.sentList));

		// If it needs to be randomised, do so.
		if (config.isRandomised) {
			Collections.shuffle(expandedRecipients);
		}

		// Show the recipients in the order we are to send the telegrams.
		System.out.println();
		for (int x = 0; x < expandedRecipients.size(); x = x + 2) {
			try {
				System.out.printf("%-30.30s  %-30.30s%n", expandedRecipients.get(x), expandedRecipients.get(x + 1));
			} catch (IndexOutOfBoundsException e) {
				System.out.printf(expandedRecipients.get(x) + "\n");
			}
		}

		System.out.println();
		System.out
				.println(
						"This will take "
								+ CommuniqueUtilities.time((int) Math
										.round(expandedRecipients.size() * ((config.isRecruitment) ? 180.05 : 30.05)))
								+ " to send " + expandedRecipients.size() + " telegrams.");

		if (!skipChecks) {
			// Give a chance to check the recipients.
			String recipientsReponse = util.prompt("Are you sure you want to send to these recipients? [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });
			if (recipientsReponse.startsWith("n")) {
				System.exit(0);
			}
		}

		// Set the client up and go.
		client.setKeys(config.keys);
		client.setRecruitment(config.isRecruitment);
		client.setRecipients(expandedRecipients);

		client.connect();
	}

	/**
	 * Should the problem be prompted to manually check all flags, this method does so, retrieving the flags and asking
	 * for the user to reconfirm them.
	 */
	private void manualFlagCheck() {

		String[] ynArr = new String[] { "yes", "no", "y", "n" };

		// Give a chance to check the keys.
		String keysResponse = util.prompt("Are these keys correct? " + config.keys.getClientKey() + ", "
				+ config.keys.getSecretKey() + ", " + config.keys.getTelegramId() + " [Yes] or [No]?", ynArr);
		if (!keysResponse.startsWith("y")) { return; }

		// Confirm the recruitment flag.
		sanitisedPrompt("Is the recruitment flag (" + config.isRecruitment + ") set correctly? [Yes] or [No]?", ynArr);

		// Confirm the randomisation flag.
		sanitisedPrompt("Is the randomisation flag (" + config.isRandomised + ") set correctly? [Yes] or [No]?", ynArr);
	}

	private void sanitisedPrompt(String prompt, String[] ynArr) {
		while (true) {
			String randomResponse = util.prompt(prompt, ynArr);
			if (randomResponse.startsWith("n")) {
				config.isRandomised = !config.isRandomised;
			} else if (randomResponse.startsWith("y")) {
				break;
			}
		}
	}

	/**
	 * @see com.git.ifly6.communique.ngui.AbstractCommunique#exportState()
	 */
	@Override public CConfig exportState() {
		return config;
	}

	/**
	 * @see com.git.ifly6.communique.ngui.AbstractCommunique#importState(com.git.ifly6.communique.io.CConfig)
	 */
	@Override public void importState(CConfig config) {
		this.config = config;
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String)
	 */
	@Override public void log(String input) {
		util.log(input);
	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int)
	 */
	@Override public void sentTo(String recipient, int x, int length) {

		config.sentList = ArrayUtils.add(config.sentList, recipient);

		Calendar now = Calendar.getInstance();
		now.add(Calendar.SECOND, (config.isRecruitment) ? 180 : 30);
		util.log("Next telegram at " + new SimpleDateFormat("HH:mm:ss").format(now.getTime()));
	}
}
