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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.git.ifly6.communique.data.CFlags;
import com.git.ifly6.javatelegram.JTelegramLogger;
import com.git.ifly6.javatelegram.util.JInfoFetcher;

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
 * with the <code>-></code> operator to filter out the World Assembly members inside a certain region or to use the
 * <code>--</code> operator to remove WA members as necessary.</td>
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
 * <td>tells the parser to exclude <b>all</b> instances of the following nation from the final list, for example,
 * <code>/imperium_anglorum</code> would exclude the nation of Imperium Anglorum from the final list. It is a
 * <code>NOT</code> operator. All lines are invertable.</td>
 * </tr>
 * <tr>
 * <td><code>-></code></td>
 * <td>tells the parser to only add nations which are in both lists (the one before and after the arrow), for example,
 * <code>region:europe -> wa:members</code> would yield the list of European nations in WA members. It is a
 * <code>IN</code> operator.</td>
 * </tr>
 * <tr>
 * <td>--</td>
 * <td>tells the parser to remove any nations listed in the second tag from the first tag, for example,
 * <code>region:europe -- wa:members</code> would leave all nations of Europe who are not WA members. This tag functions
 * much like the <code>/</code> tag, but this negation is not a global negation, as the slash uses, but rather, a local
 * negation for that tag only.</td>
 * </tr>
 * </table>
 */
public class CommuniqueParser {

	/**
	 * This string determines what version of the parser is currently being used. The entire program is build around
	 * this string for extended compatibility purposes. However, due to the separation between the Parser itself and the
	 * IO system, either of them can trigger a change in the version number.
	 */
	public static final int version = 6;
	private static JInfoFetcher fetcher = new JInfoFetcher();
	private CFlags flags;

	JTelegramLogger util;

	/**
	 * Method constructs the object. A JTelegramLogger must be provided to effectively provide an outlet for
	 * information.
	 *
	 * @param logger
	 */
	public CommuniqueParser(JTelegramLogger logger) {
		util = logger;
	}

	public CommuniqueParser(JTelegramLogger logger, CFlags config) {
		util = logger;
		this.flags = config;
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
			String[] regionContentsArr = fetcher.getRegion(element.replace("region:", ""));
			return regionContentsArr;

		} else if (element.startsWith("wa:delegate")) {
			String[] delegatesArr = fetcher.getDelegates();
			return delegatesArr;

		} else if (element.equals("wa:nations") || element.equals("wa:members")) {
			String[] waNationsArr = fetcher.getWAMembers();
			return waNationsArr;

		} else if (element.equals("world:new")) {
			String[] newNationsArr = fetcher.getNew();
			return newNationsArr;
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
	private String[] expandList(List<String> tagsList) {
		ArrayList<String> expandedList = new ArrayList<String>();

		for (int x = 0; x < tagsList.size(); x++) {
			String element = tagsList.get(x).toLowerCase();

			// Operator meaning-- 'region:europe->wa:nations' would be 'those in Europe in (who are) WA nations'
			if (element.contains("->") || element.contains("--")) {

				String[] bothArr = new String[2];
				if (element.contains("->")) {
					bothArr = element.split("->");

				} else if (element.contains("--")) {
					bothArr = element.split("--");
				}

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
				// firsts and seconds refer to the elements on either side of the '->' operator
				String[] firsts = expandTag(bothArr[0]);
				String[] seconds = expandTag(bothArr[1]);

				ArrayList<String> both = new ArrayList<String>();

				// This section is for
				if (element.contains("->")) {

					// If it appears in both lists, add it. Use the new 'contains' algorithm instead of the old 'nested
					// for loops' algorithm. This gives significant speed advantages.
					HashSet<String> firstsSet = new HashSet<String>(Arrays.asList(firsts));
					for (String second : seconds) {
						if (firstsSet.contains(second)) {
							both.add(second);
						}
					}

				} else if (element.contains("--")) {

					// If an element in the first list is also contained in the second list, do not add it to the 'both'
					// list. This basically removes it from the firsts list as output is concerned.
					HashSet<String> secondsSet = new HashSet<>(Arrays.asList(seconds));
					for (String first : firsts) {
						if (!secondsSet.contains(first)) {
							both.add(first);
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
	 * with the expansion system above. Note that this method automatically handles the removal of commented lines with
	 * <code>#</code> as the comment.
	 *
	 * @param input an array of the recipients, each one on an individual index, which can include commented lines
	 * @return a final array of the recipients, compatible with JavaTelegram
	 */
	public String[] recipientsParse(String[] input) {

		// Remove commented or empty lines.
		List<String> unComments = new ArrayList<String>();
		for (String element : input) {
			if (!element.startsWith("#") && !element.isEmpty()) {
				unComments.add(element);
			}
		}
		input = unComments.toArray(new String[unComments.size()]);

		// Form a list of all the nation we want in this list.
		List<String> whitelist = new ArrayList<String>();
		for (String element : input) {
			if (!element.startsWith("/")) {
				whitelist.add(element.toLowerCase().trim().replace(" ", "_"));
			}
		}

		// Form a list of all nations we can't have in this list.
		List<String> blacklist = new ArrayList<String>();
		for (String element : input) {
			if (element.startsWith("/")) {
				blacklist.add(element.replaceFirst("/", "").toLowerCase().trim().replace(" ", "_"));
			}
		}

		// Expand the lists.
		String[] whitelistExpanded = expandList(whitelist);
		String[] blacklistExpanded = expandList(blacklist);

		List<String> finalRecipients = new ArrayList<String>();

		// Filter using new algorithm
		HashSet<String> blackSet = new HashSet<>(Arrays.asList(blacklistExpanded));
		for (String element : whitelistExpanded) {
			if (!blackSet.contains(element)) {
				finalRecipients.add(element);
			}
		}

		// Old implementations will not have an input configuration
		if (flags != null) {
			if (flags.isRandomSort()) {
				Collections.shuffle(finalRecipients);
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
