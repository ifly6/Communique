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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CommuniqueRecipientTest {

    private String firstTranslated(String input) {
        List<String> r = CommuniqueRecipient.translateTokens(List.of(input));
        if (r.isEmpty())
            throw new IllegalArgumentException(String.format("input %s is bad", input));

        return r.get(0);
    }

    @Test
    void parseRecipient() {
        // normal, name shenanigans
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.NATION, "imperium_anglorum"),
                CommuniqueRecipient.parseRecipient("nation:imperium anglorum"));
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.NATION, "imperium_anglorum"),
                CommuniqueRecipient.parseRecipient("nation:ImPerIUm_angloRuM"));
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.NATION, "imperium_anglorum"),
                CommuniqueRecipient.parseRecipient("nation:imperium_anglorum"));
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.NATION, "imperium_anglorum"),
                CommuniqueRecipient.parseRecipient("nation:   imperium_anglorum      "));
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.NATION, "a_b_c_d_e"),
                CommuniqueRecipient.parseRecipient("nation:   A b C d E "));

        // other normal types
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.REGION, "europe"),
                CommuniqueRecipient.parseRecipient("region:europe"));
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.TAG, "delegates"),
                CommuniqueRecipient.parseRecipient("tag:delegates"));
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.TAG, "all"),
                CommuniqueRecipient.parseRecipient("tag:all"));

        // exclude
        assertEquals(new CommuniqueRecipient(FilterType.EXCLUDE, RecipientType.REGION, "europe"),
                CommuniqueRecipient.parseRecipient("-region:europe"));
        assertEquals(new CommuniqueRecipient(FilterType.EXCLUDE, RecipientType.REGION_TAG, "massive"),
                CommuniqueRecipient.parseRecipient("-region_tag:massive"));

        // include
        assertEquals(new CommuniqueRecipient(FilterType.INCLUDE, RecipientType.REGION, "europe"),
                CommuniqueRecipient.parseRecipient("+region:europe"));

        // flag
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.FLAG, "recruit"),
                CommuniqueRecipient.parseRecipient("flag:recruit"));
        assertEquals(new CommuniqueRecipient(FilterType.NORMAL, RecipientType.FLAG, "active"),
                CommuniqueRecipient.parseRecipient("flag:active"));

        // regular expressions
        assertEquals(new CommuniqueRecipient(FilterType.EXCLUDE_REGEX, RecipientType.NATION, "\\d+"),
                CommuniqueRecipient.parseRecipient("-regex:\\d+"));
        assertEquals(new CommuniqueRecipient(FilterType.REQUIRE_REGEX, RecipientType.NATION, "[A-Z]+"),
                CommuniqueRecipient.parseRecipient("+regex:[A-Z]+"));

        // weird shit
        // "2017-03-30 use lastIndexOf to deal with strange name changes, can cause error in name `+region:euro:pe`"
        assertEquals(new CommuniqueRecipient(FilterType.INCLUDE, RecipientType.REGION, "pe"),
                CommuniqueRecipient.parseRecipient("+region:euro:pe"));
    }

    @Test
    void translateTokens() {
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
    }

    @Test
    void testToString() {
        assertEquals("-nation:imperium_anglorum",
                new CommuniqueRecipient(FilterType.EXCLUDE, RecipientType.NATION,
                        "imperium_anglorum").toString());
        assertEquals("-nation:imperium_anglorum",
                new CommuniqueRecipient(FilterType.EXCLUDE, RecipientType.NATION,
                        "imperium anglorum").toString());
    }
}