package com.git.ifly6.marconi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import com.git.ifly6.javatelegram.JTelegramException;

public class Marconi {

	static MarconiLogger util = new MarconiLogger();
	static String[] keys = { "", "", "" };
	static String[] recipients = {};

	public static void main(String[] args) {
		if (args.length > 0) {
			// TODO File loader
			try {
				loadConfig(null);
			} catch (FileNotFoundException e) {
				util.log("Internal Error. File not found.");
			} catch (JTelegramException e) {
				util.log("Internal Error. Version not compatible.");
			}
		} else {
			util.log("Please provide a configuration file in the same type produced by Communiqué.");
		}
	}

	/**
	 *
	 * @param input
	 * @return
	 * @throws JTelegramException
	 * @throws FileNotFoundException
	 */
	private static void loadConfig(File file) throws JTelegramException, FileNotFoundException {
		ArrayList<String> fileContents = new ArrayList<String>(0);

		// Load the file
		FileReader configRead = new FileReader(file);
		Scanner scan = new Scanner(configRead);
		while (scan.hasNextLine()) {
			fileContents.add(scan.nextLine());
		}
		scan.close();

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
					keys[0] = (element.replace("client_key=", ""));
					util.log("Found Client Key.");
				} else if (element.startsWith("secret_key=")) {
					keys[1] = (element.replace("secret_key=", ""));
					util.log("Found Secret Key.");
				} else if (element.startsWith("telegram_id=")) {
					keys[3] = (element.replace("telegram_id=", ""));
					util.log("Found Telegram ID.");
				} else if (!(element.startsWith("#")) && !(element.isEmpty())) {
					keys[4] = (element.toLowerCase().replace(" ", "_") + "\n");
					util.log("Loaded: " + element);
				}
			}
		} else {
			throw new JTelegramException();
		}
	}

}
