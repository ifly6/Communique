/*
 * Copyright (c) 2024 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.nsapi;

import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.time.Instant;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.MatchResult;
import java.util.regex.Pattern;

public class NSHappenings {

    private static final String HAPPENINGS_URL = NSConnection.API_PREFIX
            + "q=happenings;filter=law+change+dispatch+rmb+embassy+admin+vote+resolution+member";
    private static final Pattern PATTERN = Pattern.compile("(?<=@@).*?(?=@@)");

    /** Gets list of nations appearing in happenings right now. */
    public static Map<String, Instant> getActiveNations() throws NSIOException {
        try {
            NSConnection connection = new NSConnection(HAPPENINGS_URL).connect();
            XMLDocument xml = new XMLDocument(connection.getResponse());
            List<XML> nodes = xml.nodes("/WORLD/HAPPENINGS/EVENT");

            Map<String, Instant> map = new HashMap<>();
            for (XML node : nodes) {
                Instant theInstant = Instant.ofEpochSecond(Long.parseLong(node.xpath("TIMESTAMP/text()").get(0)));
                PATTERN.matcher(node.xpath("TEXT/text()").get(0))
                        .results()
                        .map(MatchResult::group)
                        .forEach(s -> map.put(s, theInstant));
            }
            return map;

        } catch (IOException e) {
            throw new NSIOException("Encountered IO exception when getting active nations", e);
        }
    }
}
