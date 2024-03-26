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
 * This interface is to make sure that the JavaTelegram is provided with a logger with the correct methods included. It
 * should give an output to an acceptable location and a logger which also gives information to an acceptable location.
 * <p>
 * More advanced functionality should be provided by creating an extension to the {@link JavaTelegram} class and
 * implementing a better sending method which gives the feedback wanted. The JavaTelegram implementation is designed to
 * be modular with a large number of methods so it can be extended easily within its pre-existing framework.
 * </p>
 */
public interface JTelegramLogger {

    /**
     * Make sure that there is a way of giving the logging information which might be relevant to the reader.
     * @param input is the <code>String</code> which is to be processed by this function.
     */
    void log(String input);

    /**
     * Inform the logger that some <code>recipient</code> was sent to, this is the <i>x</i> of the recipients, which
     * total <code>length</code>.
     * @param recipient    name
     * @param recipientNum this is the x-th recipient
     * @param length       of the recipients list
     */
    void sentTo(String recipient, int recipientNum, int length);

}
