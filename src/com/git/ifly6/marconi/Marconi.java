/* Copyright (c) 2017 ifly6. All Rights Reserved. */
package com.git.ifly6.marconi;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.ngui.AbstractCommunique;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.JavaTelegram;

import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class Marconi extends AbstractCommunique implements JTelegramLogger {

	private static final Logger LOGGER = Logger.getLogger(Marconi.class.getName());

	private JavaTelegram client = new JavaTelegram(this);
	private CommuniqueConfig config;
	
	private boolean skipChecks = false;
	private boolean recruiting = false;
	
	public Marconi(boolean skipChecks, boolean recruiting) {
		this.skipChecks = skipChecks;
		this.recruiting = recruiting;
	}
	
	public void send() {
		
		// Process the Recipients list into a string with two columns.
		Communique7Parser parser = new Communique7Parser();
		List<String> expandedRecipients = parser.apply(config.getcRecipients()).getRecipients();
		
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
		System.out.println("This will take "
				+ CommuniqueUtilities
						.time((int) Math.round(expandedRecipients.size() * (config.isRecruitment ? 180.05 : 30.05)))
				+ " to send " + expandedRecipients.size() + " telegrams.");
		
		if (!skipChecks) {
			// Give a chance to check the recipients.
			String recipientsReponse = MarconiUtilities
					.promptYN("Are you sure you want to send to these recipients? [Yes] or [No]?");
			if (recipientsReponse.startsWith("n")) {
				System.exit(0);
			}
		}
		
		// Set the client up and go.
		client.setKeys(config.keys);
		client.setRecruitment(config.isRecruitment);
		client.setRecipients(expandedRecipients);
		
		// Check for file lock
		if (!MarconiUtilities.isFileLocked()) {
			client.connect();
		} else {
			throw new RuntimeException("Cannot send, as another instance of Marconi is already sending.");
		}
	}
	
	/** Should the problem be prompted to manually check all flags, this method does so, retrieving the flags and asking
	 * for the user to reconfirm them. */
	public void manualFlagCheck() {
		
		if (!skipChecks) {
			
			// Give a chance to check the keys.
			String keysResponse = MarconiUtilities.promptYN("Are these keys correct? " + config.keys.getClientKey() + ", "
					+ config.keys.getSecretKey() + ", " + config.keys.getTelegramId() + " [Yes] or [No]?");
			if (!keysResponse.startsWith("y")) { return; }
			
			if (!recruiting) {
				// Confirm the recruitment flag.
				while (true) {
					String recruitResponse = MarconiUtilities.promptYN(
							"Is the recruitment flag (" + config.isRecruitment + ") set correctly? [Yes] or [No]?");
					if (recruitResponse.startsWith("n")) {
						config.isRecruitment = !config.isRecruitment;
					} else if (recruitResponse.startsWith("y")) {
						break;
					}
				}
				
				// Confirm the randomisation flag.
				while (true) {
					String randomResponse = MarconiUtilities.promptYN(
							"Is the randomisation flag (" + config.isRandomised + ") set correctly? [Yes] or [No]?");
					if (randomResponse.startsWith("n")) {
						config.isRandomised = !config.isRandomised;
					} else if (randomResponse.startsWith("y")) {
						break;
					}
				}
			}
		}
	}
	
	/** Note that this will not return what is loaded. It will return a sentList whose duplicates have been removed and,
	 * if any elements start with a negation <code>/</code>, it will remove it.
	 * @see com.git.ifly6.communique.ngui.AbstractCommunique#exportState() */
	@Override public CommuniqueConfig exportState() {
		// Remove duplicates from the sentList as part of save action
		config.setcRecipients(config.getcRecipients().stream().distinct().collect(Collectors.toList()));
		return config;
		
	}
	
	/** @see com.git.ifly6.communique.ngui.AbstractCommunique#importState(com.git.ifly6.communique.io.CommuniqueConfig) */
	@Override public void importState(CommuniqueConfig config) {
		this.config = config;
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String) */
	@Override public void log(String input) {
		LOGGER.info(input);
	}
	
	/** @see com.git.ifly6.javatelegram.JTelegramLogger#sentTo(java.lang.String, int, int) */
	@Override public void sentTo(String nationName, int x, int length) {
		config.addcRecipient(CommuniqueRecipients.createExcludedNation(nationName));
	}
}
