package com.git.ifly6.marconi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Scanner;

import com.git.ifly6.javatelegram.JTelegramLogger;

public class MarconiLogger implements JTelegramLogger {

	Scanner scan = new Scanner(System.in);

	@Override
	public void log(String output) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println("[" + dateFormat.format(date) + "] " + output);
	}

	public void output(String output) {
		System.out.println(output);
	}

	/**
	 * Shorthand for the scanner creation, the posing of the question, and the getting of the response. This version of
	 * the prompt method will not return all responses in lower case.
	 *
	 * @param prompt
	 *            The question posed to the user.
	 * @return
	 */
	public String prompt(String prompt) {

		this.output(prompt);
		String response = scan.nextLine();

		return response;
	}

	/**
	 * Sends data and requests that you sanitise it to avoid stupid errors. All responses will be in lower case. This is
	 * the only way the data can be effectively sanitised.
	 *
	 * @param prompt
	 *            The question posed to the user.
	 * @param conditions
	 *            A list of valid responses.
	 * @return
	 */
	public String prompt(String prompt, String[] conditions) {
		String response;

		while (true) {
			System.out.println(prompt);
			response = scan.nextLine().toLowerCase();

			boolean fine = false;
			for (String element : conditions) {
				if (element.equals(response)) {
					fine = true;
					break;
				}
			}

			if (fine) {
				break;
			} else {
				System.out.println("Please provide an acceptable answer.");
			}
		}

		return response;
	}

}
