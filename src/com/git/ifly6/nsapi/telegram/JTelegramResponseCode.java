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
