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

package com.git.ifly6.communique.data;

import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.git.ifly6.communique.data.CommuniqueFilterType.EXCLUDE;
import static com.git.ifly6.communique.data.CommuniqueFilterType.INCLUDE;
import static com.git.ifly6.communique.data.CommuniqueFilterType.NORMAL;
import static com.git.ifly6.communique.data.CommuniqueFilterType.REQUIRE_REGEX;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.NATION;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.NONE;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.REGION;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.REGION_TAG;
import static com.git.ifly6.communique.data.CommuniqueRecipientType._VOTING;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CommuniqueRecipientTest {

    // DO NOT RE-ORDER! testToString requires order!
    static Map<String, CommuniqueRecipient> reversible = new HashMap<>();
    static List<String> parseFails = new ArrayList<>();

    static {
        reversible.put("+nation:panem", new CommuniqueRecipient(INCLUDE, NATION, "panem"));
        reversible.put("-nation:et", new CommuniqueRecipient(EXCLUDE, NATION, "et"));
        reversible.put("+region:circenses", new CommuniqueRecipient(INCLUDE, REGION, "circenses"));
        reversible.put("+regex:[A-Z].*", new CommuniqueRecipient(REQUIRE_REGEX, NONE, "[A-Z].*"));
        reversible.put("imperium_anglorum", new CommuniqueRecipient(NORMAL, NATION, "imperium_anglorum"));
        reversible.put("_voting:ga; for", new CommuniqueRecipient(NORMAL, _VOTING, "ga; for"));
        reversible.put("nation:   A b C d E ", new CommuniqueRecipient(NORMAL, NATION, "a_b_c_d_e"));
        reversible.put("-region_tag:massive", new CommuniqueRecipient(EXCLUDE, REGION_TAG, "massive"));

        parseFails.add("PEN:15");
        parseFails.add("hari:seldon_has");
        parseFails.add("a_great:foundational");
        parseFails.add("plan:for_the");
        parseFails.add("long_long:ages");
    }

    private String firstTranslated(String input) {
        List<String> r = CommuniqueRecipient.translateTokens(List.of(input));
        if (r.isEmpty())
            throw new IllegalArgumentException(String.format("input %s is bad", input));

        return r.get(0);
    }

    @Test
    void parseRecipient() {
        for (Map.Entry<String, CommuniqueRecipient> entry : reversible.entrySet())
            assertEquals(
                    entry.getValue(),
                    CommuniqueRecipient.parseRecipient(entry.getKey()));
        for (Map.Entry<String, CommuniqueRecipient> entry : reversible.entrySet()) // test if upper case
            assertEquals(
                    entry.getValue(),
                    CommuniqueRecipient.parseRecipient(entry.getKey().toUpperCase()));

        // test the double prefix undo-er
        assertEquals(
                CommuniqueRecipient.parseRecipient("nation:nation:Imperium Anglorum"),
                new CommuniqueRecipient(NORMAL, NATION, "imperium anglorum")
        );

        // test the comment
        assertEquals(
                CommuniqueRecipient.parseRecipient("nation:imperium anglorum  # program author"),
                new CommuniqueRecipient(NORMAL, NATION, "imperium anglorum")
        );

        for (String s : parseFails)
            assertThrows(IllegalArgumentException.class, () -> CommuniqueRecipient.parseRecipient(s));
    }

    @Test
    void translateToken() {
        // world tags
        assertEquals("tag:delegates", firstTranslated("wa:delegate"));
        assertEquals("tag:delegates", firstTranslated("wa:delegates"));
        assertEquals("tag:wa", firstTranslated("wa:members"));
        assertEquals("tag:wa", firstTranslated("wa:all"));
        assertEquals("tag:new", firstTranslated("world:new"));
        assertEquals("tag:all", firstTranslated("world:all"));

        // region tags
        assertEquals("region:europe", firstTranslated("region:europe"));
        assertEquals("-region:europe", firstTranslated("/region:europe"));

        // nation tags
        assertEquals("nation:imperium_anglorum", firstTranslated("imperium_anglorum"));
    }

    @Test
    void translateTokens() {
        // exclude and include tokens
        assertEquals(
                new ArrayList<>(List.of("region:Europe", "-nation:imperium_anglorum")),
                CommuniqueRecipient.translateTokens(
                        List.of("region:Europe -- imperium_anglorum")));
        assertEquals(
                new ArrayList<>(List.of("tag:delegates", "+region:Europe")),
                CommuniqueRecipient.translateTokens(
                        List.of("wa:delegates -> region:Europe")));
        assertEquals(
                new ArrayList<>(List.of("tag:delegates", "+region:Europe")),
                CommuniqueRecipient.translateTokens(
                        List.of("wa:delegates->region:Europe")));  // spacing

        // new tests from 2022's communique3
        List<String> tokens = CommuniqueRecipient.translateTokens(Arrays.asList(
                "wa:members -- region:Europe",
                "/imperium_anglorum",
                "wa:delegates",
                "wa:nations -> region:the_north_pacific",
                "region:the_east_pacific->wa:members"
        ));
        assertEquals(tokens, Arrays.asList(
                "tag:wa", "-region:europe",
                "-nation:imperium_anglorum",
                "tag:delegates",
                "tag:wa", "+region:the_north_pacific",
                "region:the_east_pacific", "+tag:wa"
        ));
    }

    @Test
    void testToString() {
        // new tests from 2022's communique3
        List<Map.Entry<String, CommuniqueRecipient>> entries = new ArrayList<>(reversible.entrySet());
        for (Map.Entry<String, CommuniqueRecipient> entry : entries) {
            if (!entry.getKey().contains(":")) continue;
            assertEquals(entry.getKey(), entry.getValue().toString());
        }

        // old tests
        assertEquals("-nation:imperium_anglorum",
                new CommuniqueRecipient(CommuniqueFilterType.EXCLUDE, CommuniqueRecipientType.NATION,
                        "imperium_anglorum").toString());
        assertEquals("-nation:imperium_anglorum",
                new CommuniqueRecipient(CommuniqueFilterType.EXCLUDE, CommuniqueRecipientType.NATION,
                        "imperium anglorum").toString());
    }
}