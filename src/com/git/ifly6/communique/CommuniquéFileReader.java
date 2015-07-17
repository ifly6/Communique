package com.git.ifly6.communique;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

public class CommuniquéFileReader {

	ArrayList<String> fileContents = new ArrayList<String>(0);
	static final int version = Communiqué.version;
	String[][] information = { {}, {} };
	private boolean isRecruitment;

	/**
	 * @return the version
	 */
	public static int getVersion() {
		return version;
	}

	/**
	 * Provide me the file where this thing is located. COOKIES!
	 *
	 * @param file
	 * @throws FileNotFoundException
	 */
	public CommuniquéFileReader(File file) throws FileNotFoundException {
		FileReader configRead = new FileReader(file);
		Scanner scan = new Scanner(configRead);

		while (scan.hasNextLine()) {
			fileContents.add(scan.nextLine());
		}

		scan.close();
		information = parseConfig();
	}

	/**
	 * Get the keys.
	 *
	 * @return String[] containing the keys in the order { clientKey, secretKey, telegramId }.
	 */
	public String[] getKeys() {
		return information[0];
	}

	public String[] getRecipients() {
		return information[1];
	}

	public boolean getRecruitmentFlag() {
		return isRecruitment;
	}

	private String[][] parseConfig() {
		String[] keys = { "", "", "" };
		ArrayList<String> recipientsList = new ArrayList<String>(0);

		for (String element : fileContents) {
			if (element.startsWith("client_key=")) {
				keys[0] = element.replace("client_key=", "");

			} else if (element.startsWith("secret_key=")) {
				keys[1] = element.replace("secret_key=", "");

			} else if (element.startsWith("telegram_id=")) {
				keys[2] = element.replace("telegram_id=", "");

			} else if (element.startsWith("isRecruitment=")) {
				isRecruitment = Boolean.getBoolean(element.replace("isRecruitment=", ""));

			} else if (!(element.startsWith("#")) && !(element.isEmpty())) {
				recipientsList.add(element.toLowerCase().replace(" ", "_") + "\n");
			}
		}

		return new String[][] { keys, recipientsList.toArray(new String[recipientsList.size()]) };
	}

	/**
	 * Queries the line for version on whether it was made by the same version of the program. If so, it returns true.
	 * Otherwise, it will return false.
	 *
	 * @param version
	 *            Boolean containing true or false on whether the configuration file is compatible.
	 * @return
	 */
	public boolean isCompatible(int version) {
		boolean correctVersion = false;
		for (String element : fileContents) {
			if (element.equals("# Produced by version " + version)) {
				correctVersion = true;
			}
		}
		return correctVersion;
	}

	/**
	 * Find the file version declarer. Return its contents.
	 *
	 * @return String containing the ending of the standard protocol version line.
	 */
	public String getFileVersion() {
		for (String element : fileContents) {
			if (element.startsWith("# Produced by version ")) {
				return element.replace("# Produced by version ", "");
			}
		}
		return null;
	}

}
