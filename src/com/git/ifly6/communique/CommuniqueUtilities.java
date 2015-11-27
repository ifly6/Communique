package com.git.ifly6.communique;

import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

/**
 * This class is nothing more than a container for utility methods used inside Communique programmes.
 *
 * @author ifly6
 */
public class CommuniqueUtilities {

	/**
	 * Randomises an array's sequence of contents.
	 *
	 * @param inputArray which is to be shuffled
	 * @return a copy of the inputArray which is shuffled randomly
	 */
	public static String[] randomiseArray(String[] inputArray) {
		Random rnd = ThreadLocalRandom.current();
		for (int i = inputArray.length - 1; i > 0; i--) {
			int index = rnd.nextInt(i + 1);

			String a = inputArray[index];
			inputArray[index] = inputArray[i];
			inputArray[i] = a;
		}
		return inputArray;
	}

	/**
	 * This changes raw seconds directly into days, hours, minutes, and seconds. Very helpful for creating a system of
	 * information which humans can use and are not just machine constructs.
	 *
	 * @param seconds elapsed
	 * @return a string in days, hours, minutes, and seconds
	 */
	public static String time(int seconds) {
		int minutes = seconds / 60;
		seconds -= minutes * 60;
		int hours = minutes / 60;
		minutes -= hours * 60;
		int days = hours / 24;
		hours -= days * 24;
		return days + "d:" + hours + "h:" + minutes + "m:" + seconds + "s";
	}

	/**
	 * This filters out all new lines from an array.
	 *
	 * @param array which contains new lines
	 * @return the array without new lines
	 */
	public static String[] filterNewLines(String[] array) {
		ArrayList<String> temp = new ArrayList<String>();

		for (String element : array) {
			if (!element.equals("\n")) {
				temp.add(element);
			}
		}

		return temp.toArray(new String[temp.size()]);
	}
}
