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

import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/* There is only one World, so this is going to be static. */
public class NSWorld {

	private NSWorld() {
	}

	/**
	 * Queries the NationStates API for a listing of all nations in the game.
	 * @return <code>List&lt;String&gt;</code> with the reference name of every NS nation
	 * @throws IOException from {@link java.net.URLConnection}
	 */
	public static List<String> getAllNations() throws IOException {
		String x = new NSConnection(NSConnection.API_PREFIX + "q=nations").getResponse();
		return processArray(new XMLDocument(x).xpath("/WORLD/NATIONS/text()").get(0).split(","));
	}

	/**
	 * Queries the NationStates API for a listing of every single World Assembly member.
	 * @return <code>List&lt;String&gt;</code> with the reference name of every World Assembly member
	 * @throws IOException from {@link java.net.URLConnection}
	 */
	public static List<String> getWAMembers() throws IOException {
		String x = new NSConnection(NSConnection.API_PREFIX + "wa=1&q=members").getResponse();
		return processArray(new XMLDocument(x).xpath("/WA/MEMBERS/text()").get(0).split(","));
	}

	/**
	 * Queries the NationStates API for a listing of all World Assembly delegates.
	 * @return <code>List&lt;String&gt;</code> with the reference name of every delegate
	 * @throws IOException from {@link java.net.URLConnection}
	 */
	public static List<String> getDelegates() throws IOException {
		String x = new NSConnection(NSConnection.API_PREFIX + "wa=1&q=delegates").getResponse();
		return processArray(new XMLDocument(x).xpath("/WA/DELEGATES/text()").get(0).split(","));
	}

	/**
	 * Queries the NS API for list of regions which declare the parameter tag.
	 * @param regionTag to declare
	 * @return list of regions with names by string
	 * @throws IOException from {@link java.net.URLConnection}
	 */
	public static List<String> getRegionTag(String regionTag) throws IOException {
		// https://www.nationstates.net/cgi-bin/api.cgi?q=regionsbytag;tags=-medium,class,-minuscule
		String content = new NSConnection(NSConnection.API_PREFIX + "q=regionsbytag;tags=" + regionTag)
				.getResponse();
		return processArray(new XMLDocument(content).xpath("/WORLD/REGIONS/text()").get(0).split(","));
	}

	/**
	 * Processes input array by trimming all elements, removing empty elements, forcing lower-case, replacing spaces
	 * with an underscore, and then collecting to a list.
	 * @param input array to be processed
	 * @return list of strings in NationStates reference name form
	 */
	private static List<String> processArray(String[] input) {
		return Arrays.stream(input)
				.filter(s -> s.trim().length() != 0)
				.map(s -> s.trim().toLowerCase().replace("\\s", "_"))
				.collect(Collectors.toList());
	}

}