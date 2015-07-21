package com.git.ifly6.communique;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import com.git.ifly6.javatelegram.JTelegramFetcher;
import com.git.ifly6.javatelegram.JTelegramLogger;

public class CommuniquéParser {

	JTelegramLogger util;

	public CommuniquéParser(JTelegramLogger logger) {
		util = logger;
	}

	/**
	 * What it says on the tin. It expands the list given in recipients into a full list of nations. If you give it
	 * something like 'region:europe', then you'll get back the entire list of nations in Europe. Same with
	 * 'WA:delegates' or 'WA:nations'.
	 *
	 * @param fetcher
	 * @param tagsList
	 */
	private String[] expandList(JTelegramFetcher fetcher, ArrayList<String> tagsList) {
		for (int x = 0; x < tagsList.size(); x++) {
			String element = tagsList.get(x).toLowerCase();
			if (element.startsWith("region:")) {
				try {
					String[] regionContentsArr = fetcher.getRegion(element.replace("region:", ""));
					List<String> regionContents = Arrays.asList(regionContentsArr);
					tagsList.addAll(regionContents);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch members of region " + element.replace("region:", "") + ".");
				}
			} else if (element.equals("wa:delegates")) {
				try {
					String[] delegatesArr = fetcher.getDelegates();
					List<String> delegates = Arrays.asList(delegatesArr);
					tagsList.addAll(delegates);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch WA delegates");
				}
			} else if (element.equals("wa:nations") || element.equals("wa:members")) {
				try {
					String[] waNationsArr = fetcher.getWAMembers();
					List<String> waNations = Arrays.asList(waNationsArr);
					tagsList.addAll(waNations);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch WA members.");
				}
			} else if (element.equals("world:new")) {
				try {
					String[] newNationsArr = fetcher.getNew();
					List<String> newNations = Arrays.asList(newNationsArr);
					tagsList.addAll(newNations);
					tagsList.remove(x);
				} catch (IOException e) {
					util.log("Internal Error. Cannot fetch new nations.");
				}
			}

		}

		// Remove duplicates
		Set<String> tagsSet = new LinkedHashSet<String>();
		tagsSet.addAll(tagsList);
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
		JTelegramFetcher fetcher = new JTelegramFetcher();
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

		// Process based on notChar
		String notChar = "/";

		// Form of all the nation we want in this bloody list.
		ArrayList<String> whitelist = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (!(element.startsWith(notChar))) {
				whitelist.add(element.toLowerCase().replace(" ", "_"));
			}
		}

		// Form a list of all nations we can't have in this bloody list.
		ArrayList<String> blacklist = new ArrayList<String>(0);
		for (String element : rawRecipients) {
			if (element.startsWith(notChar)) {
				blacklist.add(element.replaceFirst(notChar, "").toLowerCase().replace(" ", "_"));
			}
		}

		// Expand the blacklist.
		String[] whitelistExpanded = expandList(fetcher, whitelist);
		String[] blacklistExpanded = expandList(fetcher, blacklist);

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
		String carryThrough = "";

		for (String element : input) {
			carryThrough = carryThrough + element + "\n";
		}

		return recipientsParse(carryThrough);
	}

}
