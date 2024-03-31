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

import com.git.ifly6.nsapi.NSConnection;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.text.MessageFormat;
import java.util.logging.Logger;

import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.CLIENT_NOT_REGISTERED;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.NO_SUCH_TELEGRAM;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.QUEUED;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.RATE_LIMIT_EXCEEDED;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.REGION_MISMATCH;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.SECRET_KEY_MISMATCH;
import static com.git.ifly6.nsapi.telegram.JTelegramConnection.ResponseCode.UNKNOWN_ERROR;

/**
 * {@code JTelegramConnection} connects to NationStates telegram API. This is done by creating a
 * {@link HttpURLConnection} to the appropriately generated URL with provided keys. On connection, also parses response
 * to appropriate response codes.
 * <p>Note that there are no in-built rate limits in this class! This is unlike {@link NSConnection}, which limits
 * connections with {@link NSConnection#connect()} to every {@value NSConnection#WAIT_TIME} milliseconds. Rate limits
 * for this class must be implemented externally.</p>
 */
public class JTelegramConnection {

    private static final Logger LOGGER = Logger.getLogger(JTelegramConnection.class.getName());

//    public static final int QUEUED = 0;
//    public static final int UNKNOWN_ERROR = 1;
//    public static final int REGION_MISMATCH = 2;
//    public static final int CLIENT_NOT_REGISTERED = 3;
//    public static final int RATE_LIMIT_EXCEEDED = 4;
//    public static final int SECRET_KEY_MISMATCH = 5;
//    public static final int NO_SUCH_TELEGRAM = 6;

    private static HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private HttpResponse<String> httpResponse;

    /**
     * Creates a telegram connection. If {@code doNothing} is true, does nothing.
     * @param keys      to use to send
     * @param recipient to send to
     * @param doNothing if true, does nothing
     * @throws IOException if {@link MalformedURLException} or if cannot connect to Internet
     */
    public JTelegramConnection(JTelegramKeys keys, String recipient, boolean doNothing) throws IOException {
        String urlString = MessageFormat.format("{0}a=sendTG&client={1}&key={2}&tgid={3}&to={4}",
                NSConnection.API_PREFIX,
                keys.getClientKey(),
                keys.getSecretKey(),
                keys.getTelegramId(),
                recipient);
        URL tgURL = new URL(urlString);
        try {
            HttpRequest request = HttpRequest.newBuilder(tgURL.toURI())
                    .header("User-Agent",
                            String.format("JavaTelegram (maintained by Imperium Anglorum, used by %s)", clientKey))
                    .GET().build();
            if (doNothing) {
                LOGGER.fine(String.format("Constructed JTelegramConnection for TG <%s> to <%s>. Did not send.",
                        keys.getTelegramId(), recipient));
                return;
            }
            httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, String.format("Encountered normally-impossible %s",
                    e.getClass().getSimpleName()), e);
        }
    }

    /**
     * Creates and establishes a telegram connection. Automatically connects when instantiated.
     * @param keys      {@link JTelegramKeys} containing all of the relevant keys
     * @param recipient is a <code>String</code> which contains name of the recipient in NationStates back-end
     * @throws IOException if there is a problem in connecting to the API
     */
    public JTelegramConnection(JTelegramKeys keys, String recipient) throws IOException {
        this(keys, recipient, false);
    }

    /**
     * Verifies whether the telegram was queued or not.
     * @return a {@code ResponseCode}
     * @throws IOException if error in queuing the telegram
     */
    public ResponseCode verify() throws IOException {
        String response = httpResponse.body().trim().toLowerCase();

        if (response.startsWith("queued")) return QUEUED;
        if (httpResponse.statusCode() == 429 || response.contains("api recruitment tg rate-limit exceeded"))
            return RATE_LIMIT_EXCEEDED;
        if (response.contains("region mismatch between telegram and client api key")) return REGION_MISMATCH;
        if (response.contains("client not registered for api")) return CLIENT_NOT_REGISTERED;
        if (response.contains("incorrect secret key")) return SECRET_KEY_MISMATCH;
        if (response.contains("no such api telegram template")) return NO_SUCH_TELEGRAM;

        // else, print and return
        LOGGER.severe(String.format("Unknown error with code %d: %s",
                httpResponse.statusCode(), httpResponse.body().trim()
        ));
        return UNKNOWN_ERROR;
    }

    public enum ResponseCode {
        QUEUED, UNKNOWN_ERROR, REGION_MISMATCH, CLIENT_NOT_REGISTERED, RATE_LIMIT_EXCEEDED,
        SECRET_KEY_MISMATCH, NO_SUCH_TELEGRAM
    }
}
