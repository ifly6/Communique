/* Copyright (c) 2018 Kevin Wong
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

package com.git.ifly6.nsapi.telegram.util;

import com.git.ifly6.nsapi.telegram.JTelegramException;
import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSException;
import com.git.ifly6.nsapi.NSRegion;
import com.git.ifly6.nsapi.NSWorld;
import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * The class <code>JTelegramFetcher</code> is a collection of methods of use when querying data from the NationStates
 * API for the easy location of relevant recipients. It uses lazy loading.
 * <p>
 * <code>JTelegramFetcher</code> allows the fetching of <code>List&lt;String&gt;</code>s full of the list of World
 * Assembly delegates, World Assembly members, new players, or all of the inhabitants of a region.
 * </p>
 * <p>
 * Note that all of these functions require file-system and Internet access to download and parse the files provided by
 * the NationStates API.
 * </p>
 */
public class JInfoFetcher {

	private static JInfoFetcher singleton;

	private Map<String, List<String>> regionList = new HashMap<>();
	private Map<String, List<String>> regionTags = new HashMap<>();

	private List<String> allNations;
	private List<String> delegates;
	private List<String> waMembers;

	private JInfoFetcher() {
	}

	public static JInfoFetcher instance() {
		if (singleton == null) singleton = new JInfoFetcher();
		return singleton;
	}

	/**
	 * Queries the NationStates API for a listing of all World Assembly delegates.
	 * @return <code>List&lt;String&gt;</code> with the recipients inside
	 * @throws JTelegramException in case there is a problem with connecting to the NS API
	 */
	public List<String> getDelegates() throws JTelegramException {
		if (delegates == null) try {
			delegates = NSWorld.getDelegates();
		} catch (IOException e) {
			throw new JTelegramException("Failed to get list of delegates", e);
		}
		return delegates;
	}

	/**
	 * Queries the NationStates API for a listing of 50 new nations.
	 * @return <code>List&lt;String&gt;</code> with the recipients inside
	 * @throws JTelegramException in case the NationStates API is unreachable for some reason
	 */
	public List<String> getNew() throws JTelegramException {
		try {
			NSConnection connection = new NSConnection(NSConnection.API_PREFIX + "q=newnations");
			String response = connection.connect().getResponse();
			String newNations = new XMLDocument(response).xpath("/WORLD/NEWNATIONS/text()").get(0);
			return Stream.of(newNations.split(","))
					.map(String::trim)
					.filter(s -> s.trim().length() != 0)
					.collect(Collectors.toList());
		} catch (IOException e) {
			throw new JTelegramException("Failed to get new nations", e);
		}
	}

	/**
	 * Queries the NationStates API for a listing of all the members of a region.
	 * @return <code>List&lt;String&gt;</code> with the recipients inside
	 * @throws JTelegramException in case the NationStates API is unreachable for some reason
	 */
	public List<String> getRegion(String region) throws JTelegramException {
		try {
			NSRegion nsRegion = new NSRegion(region).populateData();
			if (regionList.get(region) == null || !regionList.containsKey(region))
				regionList.put(region, nsRegion.getRegionMembers());
		} catch (NSException e) {    // non-existent -> throw NSException
			throw new JTelegramException("Region \"" + region + "\" does not exist", e);

		} catch (IOException e) {
			throw new JTelegramException("Failed to fetch region members for " + region, e);
		}
		return regionList.get(region);
	}

	/**
	 * Queries the NationStates API for a list of all the regions declaring some tag.
	 * @param regionTag to query (e.g. 'LGBT', 'Massive')
	 * @return <code>List&lt;String&gt;</code> of regions with that tag
	 * @throws JTelegramException on IO Exception, likely due to API being unreachable
	 */
	public List<String> getRegionTag(String regionTag) throws JTelegramException {
		try {
			if (!regionTags.containsKey(regionTag)) {
				regionTags.put(regionTag, NSWorld.getRegionTag(regionTag));
			}
		} catch (IOException e) {
			throw new JTelegramException("Failed to fetch regions declaring tag " + regionTag, e);
		}

		return regionTags.get(regionTag);
	}

	/**
	 * Queries the NationStates API for a listing of every single World Assembly member.
	 * @return <code>List&lt;String&gt;</code> with the recipients inside
	 * @throws JTelegramException in case the NationStates API is unreachable for some reason
	 */
	public List<String> getWAMembers() throws JTelegramException {
		if (waMembers == null) try {
			waMembers = NSWorld.getWAMembers();
		} catch (IOException e) {
			throw new JTelegramException("Cannot fetch World Assembly members", e);
		}
		return waMembers;
	}

	/**
	 * Queries the NationStates API for a listing of every nation in the game.
	 * @return a list of all nations in the game
	 * @throws JTelegramException in case the NationStates API is unreachable for some reason
	 */
	public List<String> getAll() throws JTelegramException {
		if (allNations == null) try {
			allNations = NSWorld.getAllNations();
		} catch (IOException e) {
			throw new JTelegramException("Cannot fetch all nations", e);
		}
		return allNations;
	}
}
