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

package com.git.ifly6.nsapi.ctelegram.monitors.updaters;

import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSTimeStamped;
import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.Collectors;

import static com.git.ifly6.nsapi.ApiUtils.ref;

public class CommNewNation implements NSTimeStamped {

    public static final Set<String> sinkers = Set.of("lazarus", "balder", "osiris");

    public final String name;
    public final String foundingRegion;
    public final Instant timestamp;

    public CommNewNation(String name, String foundingRegion, Instant timestamp) {
        this.name = ref(name);
        this.foundingRegion = ref(foundingRegion);
        this.timestamp = timestamp;
    }

    @Override
    public Instant timestamp() {
        return timestamp;
    }

    /** @return true if the refounding region is a sinker */
    public boolean isRefound() {
        return sinkers.contains(ref(foundingRegion));
    }

    @Override
    public String toString() {
        return String.format("CommRecruitNation{name=%s, foundingRegion=%s, timestamp=%s}",
                name, foundingRegion, timestamp);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CommNewNation)) return false;
        CommNewNation nation = (CommNewNation) o;
        return Objects.equals(name, nation.name) && Objects.equals(timestamp, nation.timestamp);
    }

    @Override
    public int hashCode() {
        return Objects.hash(name, timestamp);
    }

    public static List<CommNewNation> getNewNations() throws IOException {
        final NSConnection conn = new NSConnection("https://www.nationstates.net/cgi-bin/api.cgi?q=newnationdetails");
        XMLDocument xml = new XMLDocument(conn.connect().getResponse());

        // ln = list name; lr = list region; lf = list founding time
        List<String> ln = xml.xpath("/WORLD/NEWNATIONDETAILS/NEWNATION/@name");
        List<String> lr = xml.xpath("/WORLD/NEWNATIONDETAILS/NEWNATION/REGION/text()");
        List<Instant> lf = xml.xpath("/WORLD/NEWNATIONDETAILS/NEWNATION/FOUNDEDTIME/text()").stream()
                .map(Long::parseLong)
                .map(Instant::ofEpochSecond)
                .collect(Collectors.toList());

        // create objects
        List<CommNewNation> toReturn = new ArrayList<>(ln.size());
        for (int i = 0; i < ln.size(); i++)
            toReturn.add(new CommNewNation(ln.get(i), lr.get(i), lf.get(i)));

        return toReturn;
    }

}
