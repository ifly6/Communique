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

public class CommuniquéFileWriter {

	static final int version = Communiqué.version;
	PrintWriter writer;
	String[] keys = { "", "", "" };
	String recipients = "";

	public CommuniquéFileWriter(File file) throws FileNotFoundException, UnsupportedEncodingException {
		writer = new PrintWriter(file, "UTF-8");
	}

	public void setKeys(String clientKey, String secretKey, String telegramId) {
		keys[0] = clientKey;
		keys[1] = secretKey;
		keys[2] = telegramId;
	}

	public void setKeys(String[] inputKeys) {
		if (keys.length == inputKeys.length) {
			keys = inputKeys;
		}
	}

	public void setBody(String recipiContents) {
		recipients = recipiContents;
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
		writer.println("client_key=" + keys[0]);
		writer.println("secret_key=" + keys[1]);
		writer.println("telegram_id=" + keys[2] + "\n");

		// Sort out the comments.
		String rawInput = recipients;
		String[] rawArr = rawInput.split("\n");
		ArrayList<String> contentList = new ArrayList<String>(0);
		for (String element : rawArr) {
			if (!element.startsWith("#") || !element.isEmpty()) {
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
