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

import com.git.ifly6.CommuniqueUtilities;
import com.google.common.util.concurrent.RateLimiter;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * This class regulates how the program connects to the NationStates API.
 * <p>
 * It only operates for one program at a time. If you run multiple instances, it will go over the API rate limit and
 * will result in your computer getting locked out from the API for 15 minutes. The program uses a
 * <code>synchronized</code> block to force that only one connection exists at one time.
 * </p>
 * @since JavaTelegram (2016-07-26)
 */
@SuppressWarnings("UnstableApiUsage")
public class NSConnection {

    public static final String API_PREFIX = "https://www.nationstates.net/cgi-bin/api.cgi?";
    public static final String QUERY_PREFIX = "&q=";

    private static final Logger LOGGER = Logger.getLogger(NSConnection.class.getName());
    private static final double PERMITS_PER_SECOND = 40 / (double) 30; // 50 requests per 30 seconds is max
    public final static long WAIT_TIME = 1000 * Math.round(Math.pow(PERMITS_PER_SECOND, -1)); // 750 ms

    private static final RateLimiter limiter = RateLimiter.create(PERMITS_PER_SECOND);
    private static final HttpClient CLIENT = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();

    protected URL url;
    protected HttpResponse<String> httpResponse;

    /**
     * Creates an unconnected {@code NSConnection} to query the specified URL.
     * @param urlString to query
     * @throws NSIOException if {@code urlString} is invalid URL
     */
    public NSConnection(String urlString) {
        try {
            this.url = new URL(urlString);
            LOGGER.finest(String.format("Connecting to %s", urlString));
        } catch (MalformedURLException e) {
            throw new NSIOException(String.format("Input URL <%s> malformed", urlString), e);
        }
    }

    /**
     * Connects instantiated {@code NSConnection}.
     * @return this
     * @throws FileNotFoundException if nation does not exist
     * @throws IOException           if connection otherwise fails
     * @throws NSIOException         if rate limit exceeded
     * @throws NSException           if other error
     */
    public NSConnection connect() throws IOException {
        // Implement the rate limit
        double secondsWaited = limiter.acquire();
        LOGGER.finest(String.format("NSConnection rate limit -> waited %.3f seconds", secondsWaited));

        // todo rewrite with HttpClient
        HttpRequest request;
        try {
            request = HttpRequest.newBuilder(url.toURI())
                    .header(
                            "User-Agent",
                            "NS API request; maintained by Imperium Anglorum (cyrilparsons.london@gmail.com); "
                                    + "see IP")
                    .GET()
                    .build();
        } catch (URISyntaxException e) {
            LOGGER.log(Level.SEVERE,
                    String.format(
                            "Unable to convert URL [%s] to URI. This should never happen! Cannot query NationStates!",
                            url),
                    e);
            throw new RuntimeException("Unable to convert URL to URI", e);
        }

        try {
            httpResponse = CLIENT.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (InterruptedException e) {
            LOGGER.log(Level.SEVERE, "Interrupted when sending NS API request", e);
            throw new RuntimeException("Interrupted when sending NS API request", e);
        }

        if (httpResponse.statusCode() == 429)
            throw new NSIOException(String.format("API rate limit exceeded! Retry after %s",
                    CommuniqueUtilities.time(Long.parseLong(
                                    httpResponse.headers().firstValue("X-Retry-After").orElse("-1")
                            )
                    )
            ));

        if (httpResponse.statusCode() == 404)
            throw new NSIOException(String.format("No result for URL %s", url.toString()));

        if (httpResponse.statusCode() != 200)
            throw new NSIOException(String.format("Received non-200 response code %d from API at URL %s",
                    httpResponse.statusCode(), url.toString()
            ));

        return this;
    }

    /**
     * Delivers response as {@link String}. If {@link NSConnection} has already connected, ie is spent, returns result
     * thereof. If not yet connected, invokes {@link #connect()} automatically.
     * @return response
     * @throws IOException if connection fails
     */
    public String getResponse() throws IOException {
        if (httpResponse == null) { connect(); }
        return httpResponse.body();
    }
}