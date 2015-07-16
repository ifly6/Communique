package com.git.ifly6.marconi;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.stream.Stream;

import com.git.ifly6.communique.CommuniquéFileReader;
import com.git.ifly6.communique.CommuniquéFileWriter;
import com.git.ifly6.communique.CommuniquéParser;
import com.git.ifly6.javatelegram.JTelegramException;
import com.git.ifly6.javatelegram.JavaTelegram;

public class Marconi {

	static MarconiLogger util = new MarconiLogger();
	static JavaTelegram client = new JavaTelegram(util);
	static String[] keys = { "", "", "" };
	static String[] recipients = {};
	public static final int version = 1;

	public static void main(String[] args) {
		if (args.length > 0) {		// If there is not a provided file, do nothing.
			try {
				loadConfig(new File(args[0]));		// Load the keys and recipients from the configuration file in.
			} catch (FileNotFoundException e) {
				util.log("Cannot find your file. Provide a real file and try again.");
			} catch (JTelegramException e) {
				util.log("Incorrect file version. This is a version " + version + " client.");
			}

			// Give a chance to check the keys.
			String keysResponse = util.prompt("Are these keys correct? " + keys[0] + " " + keys[1] + " " + keys[2]
					+ " ", new String[] { "yes", "no", "y", "n" });
			if (keysResponse.startsWith("y")) {

				// Process the Recipients list into a string with two columns.
				String recipientOutput = "";
				for (int x = 0; x < recipients.length; x = x + 2) {
					try {
						recipientOutput = recipientOutput + "\n" + recipients[x] + "\t\t" + recipients[x + 1];
					} catch (IndexOutOfBoundsException e) {
						recipientOutput = recipientOutput + "\n" + recipients[x];
					}
				}

				// Give a chance to check the recipients.
				String recipientsReponse = util.prompt(recipientOutput
						+ "\nAre you sure you want to send to these recipients? [Yes] or [No]?", new String[] { "yes",
						"no", "y", "n" });
				if (recipientsReponse.startsWith("y")) {

					// Parse the recipients list using the standard Communiqué parser.
					CommuniquéParser parser = new CommuniquéParser(util);

					// Set the client up and go.
					client.setKeys(keys);
					client.setRecipients(parser.recipientsParse(recipients));
					client.connect();

					// Update the configuration file to reflect the changed reality.
					try {
						appendSent(new File(args[0]));
					} catch (FileNotFoundException | UnsupportedEncodingException e) {
						util.log("Internal Error. File either does not exist or is in the incorrect encoding.");
					}

				} else {
					util.log("Please make any alterations needed and then restart this program.");
				}
			} else {
				util.log("Please make any alterations needed and then restart this program.");
			}
		} else {
			util.log("Please provide a configuration file in the same type produced by Communiqué.");
		}
	}

	/**
	 *
	 * @param file
	 *            The place to where this program will write.
	 * @throws FileNotFoundException
	 * @throws UnsupportedEncodingException
	 */
	private static void appendSent(File file) throws FileNotFoundException, UnsupportedEncodingException {
		CommuniquéFileWriter fileWriter = new CommuniquéFileWriter(file);
		fileWriter.setKeys(keys);
		String[] body = Stream.concat(Arrays.stream(recipients), Arrays.stream(client.getSentList())).toArray(
				String[]::new);
		String bodyText = "";
		for (String element : body) {
			bodyText = bodyText + element + "\n";
		}
		fileWriter.setBody(bodyText);
		fileWriter.write();
	}

	/**
	 *
	 * @param file
	 *            The place at which this program will look.
	 * @throws FileNotFoundException
	 * @throws JTelegramException
	 */
	private static void loadConfig(File file) throws FileNotFoundException, JTelegramException {
		CommuniquéFileReader fileReader = new CommuniquéFileReader(file);
		if (fileReader.isCompatible(version)) {
			keys = fileReader.getKeys();
			recipients = fileReader.getRecipients();
		} else {
			throw new JTelegramException();
		}
	}
}
