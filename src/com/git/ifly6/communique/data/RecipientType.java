/* Copyright (c) 2016 ifly6
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
package com.git.ifly6.communique.data;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.git.ifly6.javatelegram.util.JInfoFetcher;

/** @author ifly6 */
public enum RecipientType {
	
	NATION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			return Stream.of(cr).collect(Collectors.toList());
		}
	},
	
	REGION {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			return stringToCr(JInfoFetcher.getInstance().getRegion(cr.getName()), cr);
		}
	},
	
	TAG {
		@Override public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
			String tag = cr.getName();
			
			if (tag.equals("wa")) {
				return stringToCr(JInfoFetcher.getInstance().getWAMembers(), cr);
				
			} else if (tag.equals("delegates")) {
				return stringToCr(JInfoFetcher.getInstance().getDelegates(), cr);

			} else if (tag.equals("new")) { return stringToCr(JInfoFetcher.getInstance().getNew(), cr); }
			
			return Stream.of(cr).collect(Collectors.toList());
		}
	};

	@Override public String toString() {
		return super.toString().toLowerCase();
	}

	/** @param communiqueRecipient
	 * @return */
	public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
		return Stream.of(cr).collect(Collectors.toList());
	}
	
	private static List<CommuniqueRecipient> stringToCr(List<String> list, CommuniqueRecipient cr) {
		return list.stream().map(s -> new CommuniqueRecipient(cr.getFilterType(), NATION, s)).collect(Collectors.toList());
	}
}
