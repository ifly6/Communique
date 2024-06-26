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

package com.git.ifly6.communique.io;

import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.nsapi.telegram.JTelegramType;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static com.git.ifly6.communique.data.CommuniqueFilterType.INCLUDE;
import static com.git.ifly6.communique.data.CommuniqueFilterType.NORMAL;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.NATION;
import static com.git.ifly6.communique.data.CommuniqueRecipientType.REGION;
import static org.junit.jupiter.api.Assertions.assertEquals;

class CommuniqueConfigTest {
    static CommuniqueConfig config;
    static List<CommuniqueRecipient> crs;

    static {
        config = new CommuniqueConfig();
        crs = List.of(
                new CommuniqueRecipient(NORMAL, NATION, "imperium anglorum"),
                new CommuniqueRecipient(INCLUDE, REGION, "europe"),
                new CommuniqueRecipient(NORMAL, NATION, "transilia")
        );

        for (CommuniqueRecipient cr : crs)
            config.addcRecipient(cr);
    }

    @Test
    void getcRecipients() {
        assertEquals(config.getcRecipients(), crs);
    }

    @Test
    void getcRecipientsString() {
        assertEquals(crs.stream()
                        .map(CommuniqueRecipient::toString)
                        .collect(Collectors.toList()),
                config.getcRecipientsString());
    }

    @Test
    void getProcessingAction() {
        assertEquals(config.getProcessingAction(), CommuniqueProcessingAction.NONE);
    }

    @Test
    void getTelegramType() {
        assertEquals(config.getTelegramType(), JTelegramType.NONE);
    }

    @Test
    void clean() {
        // nation:nation:nation removal test
        CommuniqueConfig config = new CommuniqueConfig();
        CommuniqueRecipient bonkered =
                new CommuniqueRecipient(NORMAL, NATION, "transilia");

        try {
            Field nameField = CommuniqueRecipient.class.getDeclaredField("name");
            nameField.setAccessible(true);
            nameField.set(bonkered, "nation:imperium_anglorum");
        } catch (IllegalAccessException | NoSuchFieldException e) { }
        assertEquals(bonkered.toString(), "nation:nation:imperium_anglorum");

        config.addcRecipient(bonkered);
        config.clean();
        List<String> cleanedString = config.getcRecipientsString();
        assertEquals(cleanedString.get(0), "nation:imperium_anglorum");
    }
}