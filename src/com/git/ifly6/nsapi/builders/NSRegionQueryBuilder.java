package com.git.ifly6.nsapi.builders;

import com.git.ifly6.nsapi.NSConnection;

public class NSRegionQueryBuilder {

	private StringBuilder builder = new StringBuilder();

	/**
	 * Constructs query builder.
	 * @param regionRef is the reference name of the region
	 */
	public NSRegionQueryBuilder(String regionRef) {
		builder.append(NSConnection.API_PREFIX);
		builder.append(NSRegionShard.REGION);
		builder.append(regionRef);
		builder.append(NSConnection.QUERY_PREFIX);
	}

	/**
	 * Adds a shard to the query
	 * @param query is the shard to query
	 * @return the builder
	 */
	public NSRegionQueryBuilder addQuery(NSRegionShard query) {
		if (builder.toString().endsWith("+")) builder.append(query.toString());
		else {
			builder.append("+");
			builder.append(query.toString());
		}
		return this;
	}

	/**
	 * Adds multiple shards to the query
	 * @param queries to add
	 * @return the builder
	 */
	public NSRegionQueryBuilder addQueries(NSRegionShard[] queries) {
		for (NSRegionShard query : queries)
			addQuery(query);
		return this;
	}

	/**
	 * Returns the relevant NS API <code>String</code> that can be used to create the relevant URL which can then call
	 * the document desired.
	 * @see java.lang.Object#toString()
	 */
	@Override
	public String toString() {
		return builder.toString();
	}

}
