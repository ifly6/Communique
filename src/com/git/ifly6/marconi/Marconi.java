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
		if (args.length > 0) {	// If there is not a provided file, do nothing.

			try {
				loadConfig(new File(args[0]));		// Load the keys and recipients from the configuration file in.
			} catch (FileNotFoundException e) {
				util.log("Cannot find your file. Provide a real file and try again.");
			} catch (JTelegramException e) {
				util.log("Incorrect file version. This is a version " + version + " client.");
			}

			// Give a chance to check the keys.
			String keysResponse = util.prompt("Are these keys correct? " + keys.getClientKey() + ", "
					+ keys.getSecretKey() + ", " + keys.getTelegramId() + " [Yes] or [No]?",
					new String[] { "yes", "no", "y", "n" });

			if (keysResponse.startsWith("y")) {
				String recruitmentResponse = util.prompt(
						"Is the current recruitment flag (" + isRecruitment + ") set correctly? [Yes] or [No]?",
						new String[] { "yes", "no", "y", "n" });

				if (recruitmentResponse.startsWith("n")) {
					isRecruitment = !isRecruitment;
				}

				// Process the Recipients list into a string with two columns.
				CommuniquéParser parser = new CommuniquéParser(util);
				String[] expandedRecipients = parser.recipientsParse(recipients);

				for (int x = 0; x < expandedRecipients.length; x = x + 2) {
					try {
						System.out.printf("%-30.30s  %-30.30s%n", expandedRecipients[x], expandedRecipients[x + 1]);
					} catch (IndexOutOfBoundsException e) {
						System.out.println(expandedRecipients[x] + "\n");
					}
				}

				// Give a chance to check the recipients.
				String recipientsReponse = util.prompt(
						"Are you sure you want to send to these recipients? [Yes] or [No]?",
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
						util.log("Internal Error. File either does not exist or is in the incorrect encoding.");
					}

				} else {
					util.log("Please make any alterations needed and restart this program.");
				}
			} else {
				util.log("Please make any alterations needed and restart this program.");
			}
		} else {
			util.log("Please provide a configuration file of the same type compatible with Communiqué " + version
					+ " in the arguments.");
		}
	}

	/**
	 * @param file to where this program will write.
	 * @throws FileNotFoundException if the file cannot be found
	 * @throws UnsupportedEncodingException if the file's encoding is unsupported
	 */
	private static void appendSent(File file) throws FileNotFoundException, UnsupportedEncodingException {
		CommuniquéFileWriter fileWriter = new CommuniquéFileWriter(file);
		fileWriter.setKeys(keys);
		String[] body = Stream.concat(Arrays.stream(recipients), Arrays.stream(client.getSentList()))
				.toArray(String[]::new);
		String bodyText = "";
		for (String element : body) {
			bodyText = bodyText + "/" + element + "\n";
		}
		fileWriter.setBody(bodyText);
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
}
