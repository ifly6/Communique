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

import java.util.HashSet;
import java.util.List;
import java.util.stream.Collectors;

/** @author ifly6 */
public enum FilterType {

	NORMAL {
		@Override public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			recipients.addAll(provided.decompose());
			return recipients;
		}
		
		@Override public String toString() {
			return "";
		}
	},

	INCLUDE {
		@Override public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			HashSet<CommuniqueRecipient> set = provided.decompose().stream().collect(Collectors.toCollection(HashSet::new));
			return recipients.stream().filter(r -> set.contains(r)).collect(Collectors.toList());
		}
		
		@Override public String toString() {
			return "+";
		}
	},
	
	EXCLUDE {
		@Override public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients,
				CommuniqueRecipient provided) {
			HashSet<CommuniqueRecipient> set = provided.decompose().stream().collect(Collectors.toCollection(HashSet::new));
			return recipients.stream().filter(r -> !set.contains(r)).collect(Collectors.toList());
		}
		
		@Override public String toString() {
			return "-";
		}
	};
	
	/** @param recipients
	 * @param provided
	 * @return */
	public List<CommuniqueRecipient> apply(List<CommuniqueRecipient> recipients, CommuniqueRecipient provided) {
		return FilterType.NORMAL.apply(recipients, provided);
	}
	
}
