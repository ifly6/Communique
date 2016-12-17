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

/** An object in which to store information about a recipient.
 * @author ifly6 */
public class CommuniqueRecipient {

	private RecipientType recipientType;
	private String name;
	private FilterType filterType;
	
	/** Creates a <code>CommuniqueRecipient</code> with certain characteristics. */
	public CommuniqueRecipient(FilterType filterType, RecipientType recipientType, String name) {
		this.recipientType = recipientType;
		this.name = name;
		this.filterType = filterType;
	}

	public String getName() {
		return name;
	}
	
	public RecipientType getRecipientType() {
		return recipientType;
	}
	
	public FilterType getFilterType() {
		return filterType;
	}

	@Override public String toString() {
		return filterType.toString() + recipientType.toString() + ":" + name;
	}

	public List<CommuniqueRecipient> decompose() {
		return recipientType.decompose(this);
	}
	
	public static CommuniqueRecipient parseRecipient(String s) {
		
		s = s.trim();

		FilterType fType = FilterType.NORMAL;
		for (FilterType type : FilterType.values()) {
			if (s.startsWith(type.toString())) {
				fType = type;
				s = s.substring(type.toString().length());
				break;
			}
		}
		
		RecipientType rType = RecipientType.NATION;
		for (RecipientType type : RecipientType.values()) {
			if (s.startsWith(type.toString())) {
				rType = type;
				s = s.substring(type.toString().length());
				break;
			}
		}
		
		return new CommuniqueRecipient(fType, rType, s.substring(s.indexOf(":") + 1, s.length()));
	}
}
