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

package com.git.ifly6.nsapi;

import com.git.ifly6.nsapi.builders.NSNationQueryBuilder;
import com.git.ifly6.nsapi.builders.NSNationShard;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;

/**
 * This class is an object to hold information on a NS nation. It also provides methods to retrieve relevant information
 * directly from the NationStates API to get things such as endorsement counts or influence counts.
 * <p>
 * <code>NSNation</code> currently only deals with endorsement counts and infulence. It does not deal with the other
 * census scores. Implementation of those other scores is certainly possible, especially given the fact that such
 * implementation was attempted before. However, due to the network delays and the API's slow speed, you will be locked
 * out if you attempt mass information gathering of every single census score.
 * </p>
 */
public class NSNation {

	public static final HashMap<String, List<String>> CATEGORIES_MAP = new HashMap<>();

	static {
		CATEGORIES_MAP.put("balanced",
				Arrays.asList("Anarchy", "Capitalizt", "New York Times Democracy", "Benevolent Dictatorship",
						"Inoffensive Centrist Democracy", "Tyranny By Majority", "Father Knows Best State",
						"Authoritarian Democracy", "Psychotic Dictatorship"));
		CATEGORIES_MAP.put("right-leaning",
				Arrays.asList("Corporate Bordello", "Capitalist Paradise", "Conservative Democracy",
						"Compulsory Consumerist State", "Moralistic Democracy", "Iron Fist Consumerists"));
		CATEGORIES_MAP.put("left-leaning",
				Arrays.asList("Civil Rights Lovefest", "Left Leaning College State", "Liberal Democratic Socialists",
						"Libertarian Police State", "Democratic Socialists", "Corrupt Dictatorship"));
		CATEGORIES_MAP.put("right-wing",
				Arrays.asList("Free Market Paradise", "Right Wing Utopia", "Corporate Police State"));
		CATEGORIES_MAP.put("left-wing",
				Arrays.asList("Left Wing Utopia", "Scandinavian Liberal Paradise", "Iron Fist Socialists"));
	}

	// Nation identifiers
	private String nationName = "";
	private boolean isPopulated;
	private Date datePopulated;

	// State of nation
	private String properName;
	private List<String> endorsingNations;
	private Integer endoCount;
	private Double infuCount;

	// Quite important, these
	private boolean canRecruit;
	private boolean canCampaign;

	private String region;
	private String category;

	/**
	 * Constructs the nation. You must provide the name of the nation.
	 * @param name of the nation
	 */
	public NSNation(String name) {
		name = name.toLowerCase().replace(' ', '_').trim();
		nationName = name;
	}

	/**
	 * Fetches the endorsement and influence counts at the same time. Calling the <code>getInfluenceCount()</code> or
	 * <code>getEndoCount()</code> only provides information already loaded. This method also updates the influence
	 * count and information.
     * @throws IOException from {@link NSConnection}, itself from {@link java.net.URLConnection}
	 * @returns <code>boolean</code> referring to whether the nation in question exists
	 */
	public NSNation populateData() {
		try {
			NSNationQueryBuilder queryBuilder = new NSNationQueryBuilder(nationName);
			queryBuilder.addQuery(NSNationShard.PROPER_NAME);
			queryBuilder.addQuery(NSNationShard.ENDORSEMENT_LIST);
			queryBuilder.addQuery(NSNationShard.REGION);
			queryBuilder.addQuery(NSNationShard.CATEGORY);
			queryBuilder.addQuery(NSNationShard.CAN_RECRUIT);
			queryBuilder.addQuery(NSNationShard.CAN_CAMPAIGN);
			queryBuilder.addQuery(NSNationShard.CENSUS, 65);

			// Do the query
			NSConnection apiConnect = new NSConnection(queryBuilder.toString());
			XML xml = new XMLDocument(apiConnect.getResponse());

			// Get endorsement count and load endorsement data
			try {
				// Split endorsements string
				String endorsements = xml.xpath("/NATION/ENDORSEMENTS/text()").get(0);
				endorsingNations = Arrays.asList(endorsements.split(","));
				endoCount = endorsingNations.size();

			} catch (RuntimeException e) {
				// If that endorsements string does not exist, load default data for 0
				endorsingNations = new ArrayList<>();
				endoCount = 0;
			}

			// Get influence count
			infuCount = Double.parseDouble(xml.xpath("/NATION/CENSUS/SCALE[@id=65]/SCORE/text()").get(0));
			properName = xml.xpath("/NATION/NAME/text()").get(0);

			// Get region and other data
			region = xml.xpath("/NATION/REGION/text()").get(0);
			category = xml.xpath("/NATION/CATEGORY/text()").get(0);

			// Get the populated date
			datePopulated = new GregorianCalendar().getTime();
			isPopulated = true;

			// Get recruitment and campaign flags
			canRecruit = xml.xpath("/NATION/TGCANRECRUIT/text()").get(0).equals("1");
			canCampaign = xml.xpath("/NATION/TGCANCAMPAIGN/text()").get(0).equals("1");

		} catch (FileNotFoundException e) {
			throw new NSException("Nation " + nationName + " does not exist.");

		} catch (IOException e) {
			throw new NSIOException("Cannot connect to Internet to query " + nationName);
		}

		return this;
	}

	/**
	 * Returns the reference name of the nation in computer form, that is, in lower case and with underscores.
	 * @return the computerised name of the nation
	 */
	public String getRefName() {
		return nationName;
	}

	/**
	 * Returns the date at which the data in the nation was populated. Returns null if it was never populated.
	 * @return the timestamp of the data contained in this object, if it was ever retrieved automatically
	 */
	public Date dataTimestamp() {
		return datePopulated;
	}

	/**
	 * Gets the endorsement count for the nation.
	 * @return the nation's number of endorsements
	 */
	public int getEndoCount() {
		return endoCount;
	}

	/**
	 * Gets the list of nations have endorsed that nation.
	 * @return a list of nations in a <code>List&lt;String&gt;</code> which have endorsed the nation
	 */
	public List<String> getEndoList() {
		return endorsingNations;
	}

	/**
	 * Gets the influence score for the nation. If the influence score is already loaded into memory, it will provide
	 * that value. If it is not, it will query the API and get that value for you. In doing so, it will also populate
	 * the list of nations which have endorsed that nation as well as the nation's endorsement count.
	 * @return the nation's influence score
	 */
	public double getInfluenceCount() {
		return infuCount;
	}

	/**
	 * Allows for the number of endorsements to be set.
	 * @param endos which the nation has
	 * @return
	 */
	public NSNation setEndoCount(int endos) {
		endoCount = endos;
		return this;
	}

	/**
	 * Allows for influence to be set.
	 * @param influence which the nation has
	 */
	public NSNation setInfuCount(double influence) {
		infuCount = influence;
		return this;
	}

	/**
	 * Allows for the endorsements list to be set.
	 * @param endoList is the list of nations in a <code>String[]</code> which are endorsing the nation
	 */
	public NSNation setEndoList(List<String> endoList) {
		endorsingNations = endoList;
		return this;
	}

	/**
     * Gets the proper name of the nation. If the nation data has not yet been populated (and therefore, the proper name
     * queried from the NationStates servers), it will return <code>null</code>.
	 * @return the proper name of the nation
	 */
	public String getNationName() {
		return properName;
	}

	/**
	 * Returns the reference name of the nation in computer form, that is, in lower case and with underscores. This is
	 * the same as the method to return the nation reference name. It is kept here because of a merger between the fork
	 * conducted on the ifly Region Protection code and the Communique code.
	 * @return the computerised name of the nation
	 */
	@Deprecated
	public String getNationComputerisedName() {
		return getRefName();
	}

	/**
	 * Gets the region in which the nation resides.
	 * @return the name of the region
	 */
	public String getRegion() {
		return region;
	}

	/**
	 * Gets the government category of the region. There are quite a few.
	 * <ul>
	 * <p>
	 * Balanced: Anarchy, Capitalizt, New York Times Democracy, Benevolent Dictatorship, Inoffensive Centrist Democracy,
	 * Tyranny By Majority, Father Knows Best State, Authoritarian Democracy, Psychotic Dictatorship
	 * </p>
	 * <p>
	 * Right-leaning: Corporate Bordello, Capitalist Paradise, Conservative Democracy, Compulsory Consumerist State,
	 * Moralistic Democracy, Iron Fist Consumerists
	 * </p>
	 * <p>
	 * Left-leaning: Civil Rights Lovefest, Left Leaning College State, Liberal Democratic Socialists, Libertarian
	 * Police State, Democratic Socialists, Corrupt Dictatorship
	 * </p>
	 * <p>
	 * Right wing: Free Market Paradise, Right Wing Utopia, Corporate Police State
	 * </p>
	 * <p>
	 * Left wing: Left Wing Utopia, Scandinavian Liberal Paradise, Iron Fist Socialists
	 * </p>
	 * </ul>
	 * @return the nation category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * Returns data on whether that nation ignores recruitment telegrams.
	 * @return whether the nation will accept recruitment telegrams
	 */
	public boolean isRecruitable() {
		return canRecruit;
	}

	/**
	 * Returns data on whether that nation ignores campaign telegrams.
	 * @return whether the nation will accept campaign telegrams
	 */
	public boolean isCampaignable() {
		return canCampaign;
	}

	/**
	 * Can be queried to determine whether data exists for the nation at hand.
	 * @return a boolean flag returning whether data has been loaded for the nation at hand.
	 */
	public boolean hasData() {
		return isPopulated;
	}

	/**
	 * Can be queried to determine whether an endorsement list exists for the nation at hand.
	 * @return a boolean flag returning whether endorsement list data has been loaded for the nation at hand.
	 */
	public boolean hasEndoList() {
		return endorsingNations.size() > 0;
	}
}