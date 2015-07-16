package com.git.ifly6.autotelegram;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Scanner;

import com.git.ifly6.javatelegram.JTelegramException;
import com.git.ifly6.javatelegram.JTelegramFetcher;
import com.git.ifly6.javatelegram.JavaTelegram;

/**
 * This class was created to extend JavaTelegram's functionalities. Instead of calling methods inside JavaTelegram which
 * would have violated the design philosophy of 'fundamental and non-excludable elements only', this extension was made
 * so the class would be called with some extra methods which would not affect some other programmer's attempts to call
 * JavaTelegram directly. By moving the implementation code out of JavaTelegram, the Morse implementation is no
 * longer tied into JavaTelegram itself.
 *
 * @author ifly6
 */
public class Morse extends JavaTelegram {

	MorseUtilities util;

	public Morse(MorseUtilities logger) {
		super(logger);
		util = logger;
	}

	/**
	 * Read out the people you want the telegrams sent to on file. Will immediately query for a file called
	 * 'auto_recipients.txt'. If it exists, then it will not bother asking you for the location of a file.
	 *
	 * @return String array with the recipients inside.
	 */
	private String[] getManual() {

		File location = new File("auto_recipients.txt");

		if (!(location.exists())) {
			location = new File(util.prompt("What is the location of the file you are referencing this data from? "
					+ "We want this data with each recipient on a new line."));
		}

		ArrayList<String> contents = new ArrayList<String>(0);

		try {
			FileReader configRead = new FileReader(location.getCanonicalPath());
			Scanner scan = new Scanner(configRead);
			while (scan.hasNextLine()) {
				contents.add(scan.nextLine());
			}
			scan.close();
		} catch (IOException e) {
			getLog().log("Error. Cannot find file. Double check the file name.");
			return getManual();
		}

		util.log("Attempted to Load Manual File.");
		return contents.toArray(new String[contents.size()]);
	}

	/**
	 *
	 * @param type
	 *            The kind of recipients to which you want the file sent.
	 * @return The recipients to which you want the file sent.
	 * @throws JTelegramExeception
	 *             Just in case some processing went wrong.
	 * @throws IOException
	 *             Because it has to throw something with all these IO functions it calls.
	 */
	public void setRecipients(String type) throws JTelegramException, IOException {
		JTelegramFetcher fetcher = new JTelegramFetcher();

		if (type.equals("delegates")) {
			setRecipients(fetcher.getDelegates());
			setRecruitment(false);

		} else if (type.equals("new")) {
			setRecipients(fetcher.getNew());
			// Defaults to recruitment = true

		} else if (type.equals("manual")) {
			setRecipients(this.getManual());
			String response = util.prompt("Is this a recruitment telegram? [Yes] or [No]?", new String[] { "yes", "no",
					"y", "n" });

			if (response.startsWith("y")) {
				setRecruitment(true);
			} else {
				setRecruitment(false);
			}

		} else {
			throw new JTelegramException();
		}
	}
}
