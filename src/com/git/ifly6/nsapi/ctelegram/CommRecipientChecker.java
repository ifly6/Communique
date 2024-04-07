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

package com.git.ifly6.nsapi.ctelegram;

import com.git.ifly6.nsapi.NSNation;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommNationCache;
import com.git.ifly6.nsapi.telegram.JTelegramType;

/**
 * Checks whether recipient accepts telegrams based on telegram type.
 * @since version 13
 */
public class CommRecipientChecker {

    private final String name;
    private final JTelegramType type;

    /**
     * Constructs new checker based on the name and the telegram type. Executes on {@link #check()}.
     * @param name recipient to check
     * @param type {@link JTelegramType type} of telegram
     */
    public CommRecipientChecker(String name, JTelegramType type) {
        this.name = name;
        this.type = type;
    }

    /** @return true if recipient accepts telegram */
    public boolean check() {
        try {
            NSNation n = CommNationCache.getInstance().lookupNation(name);
            if (type == JTelegramType.RECRUIT) return n.isRecruitable();
            if (type == JTelegramType.CAMPAIGN) return n.isCampaignable();
            return true; // default

        } catch (NSNation.NSNoSuchNationException e) {
            return false;
        }
    }
}
