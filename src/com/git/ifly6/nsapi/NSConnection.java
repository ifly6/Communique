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

import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * This class regulates how the program connects to the NationStates API.
 * <p>
 * It only operates for one program at a time. If you run multiple instances, it will go over the API rate limit and
 * will result in your computer getting locked out from the API for 15 minutes. The program uses a
 * <code>synchronized</code> block to force that only one connection exists at one time.
 * </p>
 */
public class NSConnection {

    /**
     * This is the API delay timer, in milliseconds.
     */
    public final static int WAIT_TIME = 610;

    /**
     * The NationStates API call prefix, "<code>https://www.nationstates.net/cgi-bin/api.cgi?</code>".
     */
    public static final String API_PREFIX = "https://www.nationstates.net/cgi-bin/api.cgi?";

    /**
     * The NationStates API query prefix, "<code>&q=</code>".
     */
    public static final String QUERY_PREFIX = "&q=";

    private URL url;
    private String xml_raw;
    private boolean hasConnected;

    private Map<String, String> entries;

    /**
     * Creates an unconnected <code>NSConnection</code> which will query the specified URL.
     * @param urlString to query
     */
    public NSConnection(String urlString) {
        try {
            this.url = new URL(urlString);
        } catch (MalformedURLException e) {
            throw new NSException(String.format("URL string '%s' invalid URL", urlString), e);
        }
    }

    public NSConnection connect() throws IOException {
        // Implement the rate limit
        rateLimit();

        // Create connection, add request properties
        HttpURLConnection apiConnection = (HttpURLConnection) url.openConnection();
        apiConnection.addRequestProperty("User-Agent",
                "NS API request; maintained by Imperium Anglorum, email: cyrilparsons.london@gmail.com; see IP");
        if (entries != null && !entries.isEmpty())
            for (Map.Entry<String, String> entry : entries.entrySet())
                apiConnection.addRequestProperty(entry.getKey(), entry.getValue());

        apiConnection.connect(); // do connection
        hasConnected = true; // update API

        if (apiConnection.getResponseCode() == 200) { // if normal
            BufferedReader reader = new BufferedReader(new InputStreamReader(apiConnection.getInputStream()));
            xml_raw = reader.lines().collect(Collectors.joining("\n"));
            reader.close();

        } else { // otherwise, read error stream
            BufferedReader reader = new BufferedReader(new InputStreamReader(apiConnection.getErrorStream()));
            xml_raw = reader.lines().collect(Collectors.joining("\n"));
            reader.close();

            if (apiConnection.getResponseCode() == 429)
                throw new NSIOException("Api ratelimit exceeded");

            if (xml_raw.contains("Unknown nation"))
                throw new NSException("Nation does not exist");

            System.err.println(String.format("API called URL:\t%s", url.toString()));
            throw new JTelegramException(String.format("Cannot get data from the API,\nHTTP response code %d",
                    apiConnection.getResponseCode()));
        }

        return this;
    }

    public NSConnection setHeaders(Map<String, String> entries) {
        this.entries = entries;
        return this;
    }

    /**
     * Makes sure that the many different instances have to compete for a single API call which is regulated to at least
     * the number of milliseconds defined in {@link NSConnection#WAIT_TIME}.
     */
    private synchronized void rateLimit() {
        try {
            Thread.sleep(WAIT_TIME);
        } catch (InterruptedException e) {
            System.err.println("Rate limit was interrupted.");
        }
    }

    /**
     * Gets response from server as a <code>String</code>.
     * @return response in a String form
     * @throws JTelegramException if thrown from connect method
     * @throws IOException        from {@link java.net.URLConnection}
     */
    public String getResponse() throws JTelegramException, IOException {
        // if it has connected, get response. otherwise, connect and return response
        return hasConnected ? xml_raw : connect().xml_raw;
    }
}