/* Copyright (c) 2018 ifly6
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

package com.git.ifly6.nsapi;

import com.git.ifly6.nsapi.builders.NSRegionQueryBuilder;
import com.git.ifly6.nsapi.builders.NSRegionShard;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class NSRegion {

	// So we don't need to fetch it again over different regions
	private static Set<String> worldWAMembers;

	private String regionName;
	private String regionOfficialName;
	private String founderName;
	private String delegateName;

	private List<String> regionMembers = new ArrayList<>();
	private List<String> waMembers = new ArrayList<>();

	public NSRegion(String name) {

		name = name.toLowerCase().replace("\\s", "_").trim();
		regionName = name;

		// populate world data
		if (worldWAMembers == null || worldWAMembers.isEmpty()) try {
			worldWAMembers = new HashSet<>(NSWorld.getWAMembers());
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Populates data for all variables
	 * @return the regions which was populated
	 */
	public NSRegion populateData() throws IOException {
		try {
			// build the query
			NSRegionQueryBuilder builder = new NSRegionQueryBuilder(this.regionName);
			builder.addQuery(NSRegionShard.PROPER_NAME);
			builder.addQuery(NSRegionShard.FOUNDER);
			builder.addQuery(NSRegionShard.DELEGATE);
			builder.addQuery(NSRegionShard.NATIONS_LIST);
			NSConnection apiConnect = new NSConnection(builder.toString());

			// check existence
			String retInfo = apiConnect.connect().getResponse();

			// populate relevant fields
			XML xml = new XMLDocument(retInfo);
			regionOfficialName = xml.xpath("/REGION/NAME/text()").get(0);
			founderName = xml.xpath("/REGION/FOUNDER/text()").get(0);
			delegateName = xml.xpath("/REGION/DELEGATE/text()").get(0);

			// get populace
			String regionMemberString = "";
			try {
				regionMemberString = xml.xpath("/REGION/NATIONS/text()").get(0);
			} catch (IndexOutOfBoundsException ignored) {  // catches com.jcabi.xml.ListWrapper$NodeNotFoundException
				// pass, do nothing and keep default
			}
			regionMembers = Stream.of(regionMemberString.split(":"))
					.filter(s -> s.trim().length() != 0)
					.map(s -> s.trim().toLowerCase().replace("\\s", "_"))
					.collect(Collectors.toList());

		} catch (FileNotFoundException e) {
			throw new NSException("Region '" + this.regionName + "' does not exist.");    // no region -> 404

		} catch (IOException e) {
			throw new NSException("Check your Internet connection.");    // otherwise, internet
		}

		return this;
	}

	/**
	 * Uses the NSWorld object to get the list of World Assembly members. Should a nation appear on both the list of
	 * World Assembly members and the region list, it will be put on the WA members list.
	 * @return the list of WA members in a region
	 * @throws IOException if there is an issue with getting the WA members
	 */
	public List<String> getWAMembers() throws IOException {
		if (waMembers.isEmpty()) {
			waMembers = regionMembers.stream()
					.filter(n -> worldWAMembers.contains(n))
					.collect(Collectors.toList());
		}
		return waMembers;
	}

	/**
	 * Queries the NationStates API for a listing of all the members of a region.
	 * @return <code>List&lt;String&gt;</code> with the recipients inside
	 * @throws IOException in case the NationStates API is unreachable for some reason
	 */
	public List<String> getRegionMembers() throws IOException {
		return regionMembers;
	}

	public int getPopulation() {
		return regionMembers.size();
	}

	/**
	 * Queries the NationStates API for a the reference name of the Delegate.
	 * @return a <code>String</code> with the name of the Delegate
	 * @throws IOException in case the NationStates API is unreachable for some reason
	 */
	public String getDelegateName() throws IOException {
		return delegateName;
	}

	/**
	 * Queries the NationStates API for the Delegate, loads that into a <code>NSNation</code> and populates the data for
	 * that nation.
	 * @return a <code>NSNation</code> with the name of the Delegate and populated data.
	 * @throws IOException in case the NationStates API is unreachable for some reason
	 */
	public NSNation getDelegate() throws IOException {
		return new NSNation(delegateName).populateData();
	}

	/**
	 * Queries the NationStates API for a the reference name of the Founder.
	 * @return a <code>String</code> with the name of the Founder
	 * @throws IOException in case the NationStates API is unreachable for some reason
	 */
	public String getFounderName() throws IOException {
		return founderName;
	}

	/**
	 * Queries the NationStates API for the founder, loads that into a <code>NSNation</code> and populates the data for
	 * that nation.
	 * @return a <code>NSNation</code> with the name of the Founder and populated data.
	 * @throws IOException in case the NationStates API is unreachable for some reason
	 */
	public NSNation getFounder() throws IOException {
		return new NSNation(founderName);
	}

	/**
	 * Queries the NationStates API for the reference name of the region.
	 * @return the reference name of the region.
	 */
	public String getRefName() {
		return regionName;
	}

	/**
	 * Queries the NationStates API for the proper name of the region.
	 * @return the proper name of the region.
	 */
	public String getName() {
		return regionOfficialName;
	}
}
