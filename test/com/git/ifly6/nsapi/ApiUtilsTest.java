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

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueFilterType;
import com.git.ifly6.communique.data.CommuniqueRecipientType;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertLinesMatch;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings("ALL")
class ApiUtilsTest {

    @Test
    void ref() {
        Map<String, String> map = new HashMap<>();
        map.put("imperium anglorum", "imperium_anglorum");
        map.put("europe ", "europe");
        map.put("Europe ", "europe");
        map.put("     imperium anglorum  ", "imperium_anglorum");
        map.put("-123", "-123");

        for (Map.Entry<String, String> entry : map.entrySet())
            assertEquals(entry.getValue(), ApiUtils.ref(entry.getKey()));

        assertLinesMatch(map.keySet().stream().map(ApiUtils::ref), map.values().stream());
    }

    @Test
    void isEmpty() {
    }

    @Test
    void isNotEmpty() {
    }

    @Test
    void contains() {
        Map<Object[], Object> trues = new HashMap<>();
        trues.put(new String[] {"imperium anglorum", "123"}, "imperium anglorum");
        trues.put(new CommuniqueRecipient[] {
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.REGION, "europe"),
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.REGION, "the_north_pacific"),
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.NATION, "transilia")
                },
                new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.REGION, "europe"));

        for (Map.Entry<Object[], Object> entry : trues.entrySet()) {
            assertTrue(ApiUtils.contains(entry.getKey(), entry.getValue()));
        }

        Map<Object[], Object> falses = new HashMap<>();
        trues.put(new String[] {"imperium anglorum", "123"}, "separatist peoples");
        trues.put(new CommuniqueRecipient[] {
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.REGION, "europe"),
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.REGION, "the_north_pacific"),
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.NATION, "transilia")
                },
                new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.REGION, "the_rejected_realms"));

        for (Map.Entry<Object[], Object> entry : falses.entrySet())
            assertFalse(ApiUtils.contains(entry.getKey(), entry.getValue()));
    }
}