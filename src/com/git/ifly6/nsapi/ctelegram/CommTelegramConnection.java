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

import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramResponseCode;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.logging.Logger;

import static com.git.ifly6.nsapi.telegram.JTelegramResponseCode.UNKNOWN_ERROR;

/**
 * Creates a call to the telegram API for a specific set of keys and recipients; connects automatically (unless
 * otherwise specified); parses the response. <b>Telegram API</b> rate limits are not here implemented; but the API rate
 * limit is implemented from extension of {@link NSConnection}.
 * @since version 13
 */
public class CommTelegramConnection extends NSConnection {

    private static final Logger LOGGER = Logger.getLogger(CommTelegramConnection.class.getName());
    private final boolean doingNothing;

    /**
     * Creates an {@code NSConnection} to query the specified URL. Connects immediately unless doing nothing. This
     * method is private to allow for pre-processing.
     * @param urlString to connect to
     * @param doNothing if true, does not connect; otherwise connects immediately
     * @throws NSIOException if {@code urlString} is invalid URL
     */
    private CommTelegramConnection(String urlString, boolean doNothing) {
        super(urlString);
        this.doingNothing = doNothing;

        // immediately make the call
        if (!this.doingNothing)
            try { this.connect(); } catch (IOException e) {
                throw new NSIOException(
                        String.format("Failed to connect with URL %s", this.url), e);
            }
    }

    /**
     * Creates a new telegram connection from the given keys.
     * @param keys      to use to send
     * @param recipient to send to
     * @param doNothing to set whether a dry run is happening
     * @return the connection
     * @see #CommTelegramConnection(String, boolean)
     */
    public static CommTelegramConnection create(JTelegramKeys keys, String recipient, boolean doNothing) {
        String urlString = MessageFormat.format("{0}a=sendTG&client={1}&key={2}&tgid={3}&to={4}",
                NSConnection.API_PREFIX,
                keys.getClientKey(),
                keys.getSecretKey(),
                keys.getTelegramID(),
                recipient);
        return new CommTelegramConnection(urlString, doNothing);
    }

    public JTelegramResponseCode verify() throws IOException {
        if (this.doingNothing) // assume queued if on a dry run
            return JTelegramResponseCode.QUEUED;

        // parse the right telegram response code from the match strings
        String response = this.getResponse().trim().toLowerCase();
        for (JTelegramResponseCode r : JTelegramResponseCode.values())
            if (response.contains(r.getMatchString()))
                return r;

        // else, print and return
        LOGGER.severe(String.format("Unknown error with code %d: %s",
                httpResponse.statusCode(), httpResponse.body().trim()));
        return UNKNOWN_ERROR;
    }
}
