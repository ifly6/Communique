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

    QUEUED {
        @Override
        public String getExplanation() {
            return "Telegram queued";
        }
    },
    UNKNOWN_ERROR {
        @Override
        public String getExplanation() {
            return "Unidentified error!";
        }
    },
    REGION_MISMATCH {
        @Override
        public String getExplanation() {
            return "Client key is associated with a region; telegram is not recruiting for that region";
        }
    },
    CLIENT_NOT_REGISTERED {
        @Override
        public String getExplanation() {
            return "Client key is invalid";
        }
    },
    RATE_LIMIT_EXCEEDED {
        @Override
        public String getExplanation() {
            return "Too many telegrams sent within the rate limit";
        }
    },
    SECRET_KEY_MISMATCH {
        @Override
        public String getExplanation() {
            return "Cannot send a telegram with the wrong secret key";
        }
    },
    NO_SUCH_TELEGRAM {
        @Override
        public String getExplanation() {
            return "Cannot send a telegram that does not exist";
        }
    };

    public abstract String getExplanation();
    }
