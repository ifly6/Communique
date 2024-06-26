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
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Communique7ParserTest {

    @Test
    void listRecipients() {
        List<String> empty = new ArrayList<>(
                new Communique7Parser().apply(
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.NATION, "imperium_anglorum"),
                        new CommuniqueRecipient(CommuniqueFilterType.EXCLUDE, CommuniqueRecipientType.NATION, "imperium_anglorum")
                ).listRecipients());
        assertEquals(empty, new ArrayList<>());

        List<String> results = new ArrayList<>(
                new Communique7Parser().apply(
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.NATION, "imperium_anglorum"),
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.NATION, "transilia")
                ).listRecipients());
        assertEquals(results, List.of("imperium_anglorum", "transilia"));

        List<String> regexRemove = new ArrayList<>(
                new Communique7Parser().apply(
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.NATION, "imperium_anglorum"),
                        new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.NATION, "transilia"),
                        new CommuniqueRecipient(CommuniqueFilterType.EXCLUDE_REGEX, CommuniqueRecipientType.NATION, "imperium_[A-Z]+")
                ).listRecipients());
        assertEquals(regexRemove, List.of("transilia"));
    }
}