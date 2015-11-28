/* Copyright (c) 2015 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */

package com.git.ifly6.communique;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.util.JTelegramFetcher;

/**
 * This class is the central hub of the Communiqué system. It parses the <code>String</code> given to it with all the
 * recipients (and tags which stand in for multiple recipients) into a <code>String[]</code> which has every single
 * recipient, expanded, on each index. It also handles the removal of certain recipients from the list and filtering of
 * recipients as well.
 *
 * <h2>Tags</h2> There are a number of tags which stand for large defined lists of recipients.
 * <table>
 * <tr>
 * <td><code>region:[regionName]</code></td>
 * <td>this tag inserts every occupant of a region given in <code>[regionName]</code> into the recipients list.</td>
 * </tr>
 * <tr>
 * <td><code>wa:delegates</code></td>
 * <td>this tag inserts every World Assembly delegate into the recipients list.</td>
 * </tr>
 * <tr>
 * <td><code>wa:members</code></td>
 * <td>this tag inserts every World Assembly member into the recipients list. It is best to use this on a regional basis
 * with the <code>-></code> operator to filter out the World Assembly members inside a certain region.</td>
 * </tr>
 * <tr>
 * <td><code>world:new</code></td>
 * <td>this tag inserts 50 new nations into the recipients list.</td>
 * </tr>
 * </table>
 *
 * <h2>Grammar</h2> There are a few logical operators for this program to help you refine your list more efficiently.
 * <table>
 * <tr>
 * <td><code>/&emsp;</code></td>
 * <td>tells the parser to exclude all instances of the following nation from the final list, for example,
 * <code>/imperium_anglorum</code> would exclude the nation of Imperium Anglorum from the final list. It is a
 * <code>NOT</code> operator. All lines are invertable.</td>
 * </tr>
 * <tr>
 * <td><code>-></code></td>
 * <td>tells the parser to only add nations which are in both lists (the one before and after the arrow), for example,
 * <code>region:europe -> wa:members</code> would yield the list of European nations in WA members. It is a
 * <code>IN</code> operator.</td>
 * </tr>
 * </table>
 */
public class CommuniquéParser {

	/**
	 * This string determines what version of the parser is currently being used. The entire program is build around
	 * this string for extended compatibility purposes. However, due to the separation between the Parser itself and the
	 * IO system, either of them can trigger a change in the version number.
	 */
	public static final int version = 4;
	JTelegramLogger util;
	private JTelegramFetcher fetcher = new JTelegramFetcher();

	/**
	 * Method constructs the object. A JTelegramLogger must be provided to effectively provide an outlet for
	 * information.
	 *
	 * @param logger
	 */
	public CommuniquéParser(JTelegramLogger logger) {
		util = logger;
	}

	/**
	 * Determine whether a <code>String</code> is a special tag or not. What strings are tags is determined in the
	 * documentation on the grammar of the Communiqué syntax.
	 *
	 * @param input
	 * @return
	 */
	private boolean isTag(String input) {

		if (input.startsWith("region:")) {
			return true;
		} else if (input.equals("wa:delegates")) {
			return true;
		} else if (input.equals("wa:nations") || input.equals("wa:members")) {
			return true;
		} else if (input.equals("world:new")) { return true; }

		return false;
	}

	/**
	 * Tag expansion system for Communiqué.
	 *
	 * @param element The tag you want expanded
	 * @return
	 */
	private String[] expandTag(String element) {
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
	 * @param input an array of the recipients, each one on an individual index
	 * @return a final array of the recipients, compatible with JavaTelegram
	 */
	public String[] recipientsParse(String[] input) {
		ArrayList<String> finalRecipients = new ArrayList<String>(0);

		// Remove commented or empty lines.
		ArrayList<String> unComments = new ArrayList<String>(0);
		for (String element : input) {
			if (!element.startsWith("#") && !element.isEmpty()) {
				unComments.add(element);
			}
		}
		input = unComments.toArray(new String[unComments.size()]);

		// Form a list of all the nation we want in this list.
		ArrayList<String> whitelist = new ArrayList<String>(0);
		for (String element : input) {
			if (!element.startsWith("/")) {
				whitelist.add(element.toLowerCase().trim().replace(" ", "_"));
			}
		}

		// Form a list of all nations we can't have in this list.
		ArrayList<String> blacklist = new ArrayList<String>(0);
		for (String element : input) {
			if (element.startsWith("/")) {
				blacklist.add(element.replaceFirst("/", "").toLowerCase().trim().replace(" ", "_"));
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
	 * Convenience method for <code>recipientsParse(String[] input)</code> if you don't feel like rewriting the code to
	 * make it an actual <code>String</code>.
	 *
	 * @param input A <code>String</code>, with each line separated by new line.
	 * @return
	 */
	public String[] recipientsParse(String input) {
		return recipientsParse(input.split("\n"));
	}

	public static int getVersion() {
		return version;
	}
}
