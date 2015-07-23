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
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.LinkedHashSet;

import com.git.ifly6.javatelegram.JTelegramKeys;

public class CommuniquéFileWriter {

	static final int version = CommuniquéParser.getVersion();
	PrintWriter writer;
	JTelegramKeys keys = new JTelegramKeys();
	String recipients = "";
	boolean isRecruitment = true;

	public CommuniquéFileWriter(File file) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(file, "UTF-8");
	}

	public void setKeys(String clientKey, String secretKey, String telegramId) {
		keys.setClientKey(clientKey);
		keys.setSecretKey(secretKey);
		keys.setTelegramId(telegramId);
	}

	@Deprecated
	public void setKeys(String[] inputKeys) {
		keys.setKeys(inputKeys);
	}

	public void setKeys(JTelegramKeys inputKeys) {
		keys = inputKeys;
	}

	public void setBody(String codeContents) {
		recipients = codeContents;
	}

	/**
	 * Convenience method if you don't want to turn your String array into a string just to fit the original setBody
	 * method.
	 *
	 * @param codeContents
	 */
	public void setBody(String[] codeContents) {
		String recipients = "";
		for (String element : codeContents) {
			recipients = recipients + element + "\n";
		}

		setBody(recipients);
	}

	public void setRecuitment(boolean recuitment) {
		isRecruitment = recuitment;
	}

	public void write() {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();

		String header = "# Communiqué Configuration File. Do not edit by hand.";
		String headerDate = "# Produced at: " + dateFormat.format(date);
		String headerVers = "# Produced by version " + version;

		writer.println(header);
		writer.println(headerDate);
		writer.println(headerVers + "\n");
		writer.println("client_key=" + keys.getClientKey());
		writer.println("secret_key=" + keys.getSecretKey());
		writer.println("telegram_id=" + keys.getTelegramId());
		writer.println("isRecruitment=" + isRecruitment);
		writer.println("\n");

		// Sort out the comments.
		String rawInput = recipients;
		String[] rawArr = rawInput.split("\n");
		ArrayList<String> contentList = new ArrayList<String>(0);
		for (String element : rawArr) {
			if (!element.startsWith("#") && !element.isEmpty()) {
				contentList.add(element);
			}
		}

		// Sort out the recipients from the sent and get rid of duplicates.
		LinkedHashSet<String> recipList = new LinkedHashSet<String>(0);
		LinkedHashSet<String> nopeList = new LinkedHashSet<String>(0);
		for (String element : contentList) {
			if (element.startsWith("/")) {
				nopeList.add(element);
			} else {
				recipList.add(element);
			}
		}

		// Print in the recipients
		for (String element : recipList) {
			writer.println(element);
		}

		// Print in the nopeList
		for (String element : nopeList) {
			writer.println(element);
		}

		writer.close();
	}
}
