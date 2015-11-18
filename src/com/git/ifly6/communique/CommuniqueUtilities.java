package com.git.ifly6.communique;

import java.util.ArrayList;
import java.util.Collections;

/**
 * This class is nothing more than a container for utility methods used inside Communique programmes.
 *
 * @author ifly6
 */
public class CommuniqueUtilities {

	/**
	 * Randomises an array. Probably best to do it more efficiently, but right now, it uses a Collection to shuffle it
	 * in basically for simplicity of coding...
	 *
	 * @param inputArray which is to be shuffled
	 * @return a copy of the inputArray which is shuffled randomly
	 */
	public static String[] randomiseArray(String[] inputArray) {
		ArrayList<String> tempList = new ArrayList<String>();
		for (String element : inputArray) {
			tempList.add(element);
		}
		Collections.shuffle(tempList);
		return tempList.toArray(new String[tempList.size()]);
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
}
