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

package com.git.ifly6.communique;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.Scanner;

import com.git.ifly6.javatelegram.JTelegramKeys;

public class CommuniquéFileReader {

	ArrayList<String> fileContents = new ArrayList<String>(0);
	static final int version = CommuniquéParser.getVersion();
	Object[] information = {};
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
	public JTelegramKeys getKeys() {
		return (JTelegramKeys) information[0];
	}

	public String[] getRecipients() {
		return (String[]) information[1];
	}

	public boolean getRecruitmentFlag() {
		return isRecruitment;
	}

	private Object[] parseConfig() {
		JTelegramKeys keys = new JTelegramKeys();
		ArrayList<String> recipientsList = new ArrayList<String>(0);

		for (String element : fileContents) {
			if (element.startsWith("client_key=")) {
				keys.setClientKey(element.replace("client_key=", ""));

			} else if (element.startsWith("secret_key=")) {
				keys.setSecretKey(element.replace("secret_key=", ""));

			} else if (element.startsWith("telegram_id=")) {
				keys.setTelegramId(element.replace("telegram_id=", ""));

			} else if (element.startsWith("isRecruitment=")) {
				isRecruitment = Boolean.getBoolean(element.replace("isRecruitment=", ""));

			} else if (!(element.startsWith("#")) && !(element.isEmpty())) {
				// TODO fix this bloody error for Marconi
				recipientsList.add(element.toLowerCase().replace(" ", "_"));
			}
		}

		return new Object[] { keys, recipientsList.toArray(new String[recipientsList.size()]) };
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
		int fileVersion = Integer.parseInt(getFileVersion());

		if (fileVersion < version) {
			return true;
		}
		return false;
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
