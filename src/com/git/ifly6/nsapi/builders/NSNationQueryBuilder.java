package com.git.ifly6.nsapi.builders;

import com.git.ifly6.nsapi.NSConnection;

import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

public class NSNationQueryBuilder {

	private List<Entry<NSNationShard, Integer>> censusShards = new ArrayList<>(8);
	private List<NSNationShard> queryShards = new ArrayList<>(8);

	private StringBuilder builder = new StringBuilder();

	public NSNationQueryBuilder(String nationName) {
		builder.append(NSConnection.API_PREFIX);
		builder.append(NSNationShard.NATION);
		builder.append(nationName);
		builder.append(NSConnection.QUERY_PREFIX);
	}

	public NSNationQueryBuilder addQuery(NSNationShard query) {
		addQuery(query, -1);    // -1 is a dummy var if not specified
		return this;
	}

	public NSNationQueryBuilder addQuery(NSNationShard query, int censusId) {
		// for non-census shards, ignore censusId
		if (query == NSNationShard.CENSUS && censusId != -1)
			censusShards.add(new AbstractMap.SimpleEntry<>(query, censusId));
		else queryShards.add(query);
		return this;
	}

	public NSNationQueryBuilder addQueries(NSNationShard[] queries) {
		for (NSNationShard query : queries) addQuery(query);
		return this;
	}

	/**
	 * Returns the relevant NS API <code>String</code> that can be used to create the relevant URL which can then call
	 * the document desired.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {

		// For each query element, append to builder
		StringBuilder output = new StringBuilder(builder);
		queryShards.forEach(s -> output.append(s.toString()).append("+"));

		if (censusShards.size() > 0) {
			output.append("census;mode=score;scale=");
			// For each census element, append to builder
			censusShards.stream()
					.map(Entry::getValue)
					.forEach(i -> output.append(i.toString()).append("+"));
		}

		String outString = output.toString();
		return outString.endsWith("+") ? outString.substring(0, outString.length() - 1) : outString;
	}

}
