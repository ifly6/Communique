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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

/** @author ifly6 */
public class Communique7Parser {
	
	List<CommuniqueRecipient> recipients = new ArrayList<>(0);

	public Communique7Parser() {
	}
	
	public Communique7Parser apply(List<String> list) {
		list.stream().filter(s -> !s.startsWith("#")).filter(s -> !StringUtils.isEmpty(s))
				.map(CommuniqueRecipient::parseRecipient).forEach(this::apply);
		return this;
	}
	
	public Communique7Parser apply(CommuniqueRecipient input) {
		recipients = input.getFilterType().apply(recipients, input);
		return this;
	}
	
	public Communique7Parser apply(CommuniqueRecipient... inputs) {
		Arrays.stream(inputs).forEach(this::apply);
		return this;
	}

	public List<String> getRecipients() {
		return recipients.stream().map(CommuniqueRecipient::getName).collect(Collectors.toList());
	}
	
	public static List<String> translateTokens(List<String> oldTokens) {
		List<String> tokens = new ArrayList<>();
		for (String oldToken : oldTokens) {
			if (oldToken.startsWith("flag:recruit")) {
				tokens.add(oldToken);
				continue;
			}
			if (oldToken.contains("->")) {
				String[] split = oldToken.split("->");
				tokens.add(translateToken(split[0]));
				tokens.add(translateToken("->" + split[1]));
				continue;
			}
			if (oldToken.contains("--")) {
				String[] split = oldToken.split("--");
				tokens.add(translateToken(split[0]));
				tokens.add(translateToken("--" + split[1]));
				continue;
			}
			tokens.add(translateToken(oldToken));

		}
		return tokens;
	}
	
	private static String translateToken(String oldToken) {

		// logic tags
		if (oldToken.startsWith("/")) { return "-" + translateToken(oldToken.replaceFirst("/", "").trim()); }
		if (oldToken.startsWith("--")) { return "-" + translateToken(oldToken.replace("--", "").trim()); }
		if (oldToken.startsWith("->")) { return "+" + translateToken(oldToken.replace("->", "").trim()); }
		
		// decomposition tags
		if (oldToken.equalsIgnoreCase("wa:delegates")) { return "tag:delegates"; }
		if (oldToken.equalsIgnoreCase("wa:members") || oldToken.equalsIgnoreCase("wa:nations")) { return "tag:wa"; }
		if (oldToken.startsWith("world:new")) { return "tag:new"; }
		
		// recipient tags
		if (oldToken.startsWith("region:")) { return oldToken; }
		return "nation:" + oldToken;
		
	}
}
