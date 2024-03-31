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
import java.net.URL;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * <code>JTelegramConnection</code> is the system used to connect to the NationStates API.
 * <p>
 * <p>
 * It creates a <code>URLConnection</code> which is used to connect to the API. It then uses allows for error-checking
 * and processing through other methods.
 * </p>
 * <p>
 * <p>
 * This class is utilised by {@link JavaTelegram} to actually send the relevant data to the NationStates API.
 * </p>
 */
public class JTelegramConnection {

    public static final Logger LOGGER = Logger.getLogger(JTelegramConnection.class.getName());

    static final int QUEUED = 0;
    static final int UNKNOWN_ERROR = 1;
    static final int REGION_MISMATCH = 2;
    static final int CLIENT_NOT_REGISTERED = 3;
    static final int RATE_LIMIT_EXCEEDED = 4;
    static final int SECRET_KEY_MISMATCH = 5;
    static final int NO_SUCH_TELEGRAM = 6;

    private static HttpClient client = HttpClient.newBuilder().connectTimeout(Duration.ofSeconds(10)).build();
    private HttpResponse<String> httpResponse;

    /**
     * Creates and establishes a <code>JTelegramConenction</code> with the relevant codes and keys (Death Cab for
     * Cutie?). It automatically connects when established.
     * @param clientKey  is a <code>String</code> which contains the client key
     * @param secretKey  is a <code>String</code> which contains the secret key
     * @param telegramID is a <code>String</code> which contains the telegram ID
     * @param recipient  is a <code>String</code> which contains the name of the recipient in NationStates back-end
     *                   format (that is, all spaces turned into underscores)
     * @throws IOException if there is a problem in connecting to the API
     */
    public JTelegramConnection(String clientKey, String secretKey, String telegramID, String recipient) throws IOException {
        URL tgURL = new URL(NSConnection.API_PREFIX + "a=sendTG&client=" + clientKey + "&key=" + secretKey + "&tgid="
                + telegramID + "&to=" + recipient);
        try {
            HttpRequest request = HttpRequest.newBuilder(tgURL.toURI())
                    .header("User-Agent",
                            String.format("JavaTelegram (maintained by Imperium Anglorum, used by %s)", clientKey))
                    .GET().build();
            httpResponse = client.send(request, HttpResponse.BodyHandlers.ofString());

        } catch (URISyntaxException | InterruptedException e) {
            LOGGER.log(Level.SEVERE, String.format("Encountered normally-impossible %s",
                    e.getClass().getSimpleName()), e);
        }
    }

    /**
     * Creates and establishes a <code>JTelegramConnection</code> based off of a <code>JTelegramKeys</code> instead of
     * the discrete codes and keys. It automatically connects when established.
     * @param keys      is a <code>JTelegramKeys</code> which contains all of the relevant keys
     * @param recipient is a <code>String</code> which contains name of the recipient in NationStates back-end
     * @throws IOException if there is a problem in connecting to the API
     */
    public JTelegramConnection(JTelegramKeys keys, String recipient) throws IOException {
        this(keys.getClientKey(), keys.getSecretKey(), keys.getTelegramID(), recipient);
    }

    /**
     * Verifies whether the telegram was queued or not.
     * <p>
     * There are error codes. If the error code is 0, there is no problem. If the code is 1, then it failed to queue for
     * an unknown reason. If the code is 2, it failed to code due to a region mismatch between the telegram and the
     * client key.
     * </p>
     * @return an <code>int</code> which contains an error code.
     * @throws IOException if error in queuing the telegram
     */
    int verify() throws IOException {
        String response = httpResponse.body().trim().toLowerCase();
        if (response.startsWith("queued"))
            return QUEUED;
        if (response.contains("api recruitment tg rate-limit exceeded")) return RATE_LIMIT_EXCEEDED;
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

}
