/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.git.ifly6.javatelegram.util.JInfoFetcher;

/** Defines a number of recipient types and provides methods to decompose those types into lists of
 * <code>CommuniqueRecipient</code>.
 * @author ifly6 */
public enum RecipientType {
	
	// private JInfoFetcher fetcher = JInfoFetcher.getInstance();
	
	/** Declares the recipient is a nation and requires no further processing in decomposition. */
	NATION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			return Stream.of(cr).collect(Collectors.toList());
		}
	},
	
	/** Declares the recipient is a region, allowing for decomposition into a list of {@link CommuniqueRecipient}
	 * nations in the region. */
	REGION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			return createRecipients(JInfoFetcher.instance().getRegion(cr.getName()), cr);
		}
	},
	
	/** Declares the recipient is one of various tags, which can be used to get the members of the World Assembly,
	 * delegates thereof, or new nations. */
	TAG {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			String tag = cr.getName();
			if (tag.equals("wa")) { return createRecipients(JInfoFetcher.instance().getWAMembers(), cr); }
			if (tag.equals("delegates")) { return createRecipients(JInfoFetcher.instance().getDelegates(), cr); }
			if (tag.equals("new")) { return createRecipients(JInfoFetcher.instance().getNew(), cr); }
			return Stream.of(cr).collect(Collectors.toList());
		}
	},
	
	/** Declares that the recipient is an internal Commmunique flag, and therefore, should not return any real
	 * recipients. */
	FLAG {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			return new ArrayList<>();
		}
	};
	
	/** Allows for the recipient type to be compatible with the NationStates telegram system by providing the same tag
	 * nomenclature. */
	@Override public String toString() {
		return super.toString().toLowerCase();
	}
	
	/** Decomposes a tag into a list of <code>CommuniqueRecipient</code> which can then be more easily used.
	 * @param communiqueRecipient to be decomposed
	 * @return a list of <code>CommuniqueRecipient</code> */
	public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
		return NATION.decompose(cr);
	}
	
	/** Translates a nation reference names into a valid <code>CommuniqueRecipient</code>s.
	 * @param s, containing a nation reference name
	 * @param cr from which to extract type data
	 * @return a CommuniqueRecipient with the filter type defined in <code>cr</code>, nation recipient type, and same
	 *         reference name as given */
	private static CommuniqueRecipient createRecipient(String s, CommuniqueRecipient cr) {
		return new CommuniqueRecipient(cr.getFilterType(), NATION, s);
	}
	
	/** Translates a list of nation reference names into a list of valid <code>CommuniqueRecipient</code>s.
	 * @param list of nation reference names
	 * @param cr from which to extract type data
	 * @return list of CommuniqueRecipients */
	private static List<CommuniqueRecipient> createRecipients(List<String> list, CommuniqueRecipient cr) {
		return list.stream()
				.map(s -> createRecipient(s, cr))
				.collect(Collectors.toList());
	}
}
