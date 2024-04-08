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

package com.git.ifly6.nsapi.telegram;

/**
 * Enumerates known telegram response codes, the match strings to determine whether they are applicable from the
 * response, and an explanation for the user. For further information see this forum
 * <a href="https://forum.nationstates.net/viewtopic.php?p=41497878#p41497878">post</a>.
 * @since version 13 (2024-03-31)
 */
public enum JTelegramResponseCode {

    QUEUED("queued", "Telegram queued"),

    REGION_MISMATCH(
            "region mismatch between telegram and client api key",
            "Client key is associated with a region; telegram is not recruiting for that region"),

    CLIENT_NOT_REGISTERED(
            "client not registered for api",
            "Client key is invalid"),

    RATE_LIMIT_EXCEEDED(
            "api recruitment tg rate-limit exceeded",
            "Too many telegrams sent within the rate limit"),

    SECRET_KEY_MISMATCH("incorrect secret key", "Cannot send a telegram with the wrong secret key"),

    NO_SUCH_TELEGRAM(
            "no such api telegram template",
            "Cannot send a telegram that does not exist"),

    SENDER_CTE(
            "sender nation ceased to exist",
            "The telegram sender no longer exists"
    ),

    REGION_CTE(
            "sender region ceased to exist",
            "Telegram is sent on behalf of a region which has ceased to exist"
    ),

    SENDER_NOT_IN_REGION(
            "author not resident in region",
            "Telegram is sent on behalf of a region but the author thereof is not a regional resident"
    ),

    SENDER_NOT_AUTHORISED(
            "sender nation not authorized to recruit in region",
            "Telegram is sent on behalf of a region but the author thereof is not authorised to send telegrams on "
                    + "behalf of that region"
    ),

    UNKNOWN_ERROR("", "Unidentified error!");

    private final String matchString;
    private final String explanation;

    JTelegramResponseCode(String matchString, String explanation) {
        this.matchString = matchString.toLowerCase();
        this.explanation = explanation;
    }

    public String getMatchString() {
        return matchString;
    }

    public String getExplanation() {
        return explanation;
    }
}
