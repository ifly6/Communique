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

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.git.ifly6.javatelegram.JTelegramFetcher;
import com.git.ifly6.javatelegram.JTelegramLogger;

public class CommuniquéParser {

	JTelegramLogger util;

	public CommuniquéParser(JTelegramLogger logger) {
		util = logger;
	}

	/**
	 * Determine whether a String is a special tag or not.
	 *
	 * @param element
	 * @return
	 */
	private boolean isTag(String element) {

		if (element.startsWith("region:")) {
			return true;
		} else if (element.equals("wa:delegates")) {
			return true;
		} else if (element.equals("wa:nations") || element.equals("wa:members")) {
			return true;
		} else if (element.equals("world:new")) {
			return true;
		}

		return false;
	}

	/**
	 * Tag expansion system for Communiqué.
	 *
	 * @param element
	 *            The tag you want expanded
	 * @return
	 */
	private String[] expandTag(String element) {
		JTelegramFetcher fetcher = new JTelegramFetcher();

		if (element.startsWith("region:")) {
			try {
				String[] regionContentsArr = fetcher.getRegion(element.replace("region:", ""));
				return regionContentsArr;
			} catch (IOException e) {
				util.log("Internal Error. Cannot fetch members of region " + element.replace("region:", "") + ".");
			}
		} else if (element.startsWith("wa:delegate")) {
			try {
				String[] delegatesArr = fetcher.getDelegates();
				return delegatesArr;
			} catch (IOException e) {
				util.log("Internal Error. Cannot fetch WA delegates");
			}
		} else if (element.equals("wa:nations") || element.equals("wa:members")) {
			try {
				String[] waNationsArr = fetcher.getWAMembers();
				return waNationsArr;
			} catch (IOException e) {
				util.log("Internal Error. Cannot fetch WA members.");
			}
		} else if (element.equals("world:new")) {
			try {
				String[] newNationsArr = fetcher.getNew();
				return newNationsArr;
			} catch (IOException e) {
				util.log("Internal Error. Cannot fetch new nations.");
			}
		}

		// If all else fails...
		return new String[] {};
	}

	/**
	 * What it says on the tin. It expands the list given in recipients into a full list of nations. If you give it
	 * something like 'region:europe', then you'll get back the entire list of nations in Europe. Same with
	 * 'WA:delegates' or 'WA:nations'.
	 *
	 * @param fetcher
	 * @param tagsList
	 */
	private String[] expandList(ArrayList<String> tagsList) {
		ArrayList<String> expandedList = new ArrayList<String>();

		for (int x = 0; x < tagsList.size(); x++) {
			String element = tagsList.get(x).toLowerCase();

			// Operator meaning-- 'region:europe->wa:nations' would be 'those in Europe in (who are) WA nations'
			if (element.contains("->")) {
				String[] bothArr = element.split("->");

				// Remove leading and trailing underscores.
				for (int i = 0; i < bothArr.length; i++) {
					if (bothArr[i].startsWith("_")) {
						bothArr[i] = bothArr[i].substring(1, bothArr[i].length());
					}
					if (bothArr[i].endsWith("_")) {
						bothArr[i] = bothArr[i].substring(0, bothArr[i].length() - 1);
					}
				}

				// Split into the two lists
				String[] firsts = expandTag(bothArr[0]);
				String[] seconds = expandTag(bothArr[1]);

				ArrayList<String> both = new ArrayList<String>();

				// If it appears in both lists, add it.
				for (String first : firsts) {
					for (String second : seconds) {
						if (first.equals(second)) {
							both.add(first);
							break;
						}
					}
				}

				expandedList.addAll(both);

			} else if (isTag(element)) {
				expandedList.addAll(Arrays.asList(expandTag(element)));

			} else {
				expandedList.add(element);
			}
		}

		// Remove duplicates
		Set<String> tagsSet = new LinkedHashSet<String>();
		tagsSet.addAll(expandedList);
		tagsList.clear();
		tagsList.addAll(tagsSet);

		// Return!
		return tagsList.toArray(new String[tagsList.size()]);
	}

	/**
	 * This parses the contents of the recipients and allows us to actually make the tag system work through interfacing
	 * with the expansion system above.
	 *
	 * @param input
	 * @return
	 */
	public String[] recipientsParse(String input) {
		ArrayList<String> finalRecipients = new ArrayList<String>(0);
		String[] rawRecipients = input.split("\n");

		// Remove commented or empty lines.
		ArrayList<String> unComments = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (!element.startsWith("#") && !element.isEmpty()) {
				unComments.add(element);
			}
		}
		rawRecipients = unComments.toArray(new String[unComments.size()]);

		// Form of all the nation we want in this bloody list.
		ArrayList<String> whitelist = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (!element.startsWith("/")) {
				whitelist.add(element.toLowerCase().replace(" ", "_"));
			}
		}

		// Form a list of all nations we can't have in this bloody list.
		ArrayList<String> blacklist = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (element.startsWith("/")) {
				blacklist.add(element.replaceFirst("/", "").toLowerCase().replace(" ", "_"));
			}
		}

		// Expand the blacklist.
		String[] whitelistExpanded = expandList(whitelist);
		String[] blacklistExpanded = expandList(blacklist);

		// Only add from white-list if it does not appear on blacklist.
		for (String wList : whitelistExpanded) {
			boolean toAdd = true;

			for (String bList : blacklistExpanded) {
				if (wList.equals(bList)) {
					toAdd = false;
					break;
				}
			}

			if (toAdd) {
				finalRecipients.add(wList);
			}
		}

		return finalRecipients.toArray(new String[finalRecipients.size()]);
	}

	/**
	 * Convenience method for recipientsParse(String input) if you don't feel like rewriting the code to make it an
	 * actual String.
	 *
	 * @param input
	 *            A String array, with each line on each index. Basically, it undoes the .split("\n") so commonly used
	 *            to make String[]'s.
	 * @return
	 */
	public String[] recipientsParse(String[] input) {
		String output = "";

		for (String element : input) {
			output = output + element + "\n";
		}

		return recipientsParse(output);
	}

}
