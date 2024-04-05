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

import java.util.Arrays;
import java.util.Objects;

/**
 * <code>JTelegramKeys</code> creates a unified object for the storage and retrieval of the client, secret, and
 * telegram keys. This replaced the original method of holding this in a {@link String[]}. Since 2024-04-05, this is
 * immutable.
 * @since JavaTelegram (2015-07-16)
 */
public final class JTelegramKeys {

    private final String clientKey;
    private final String secretKey;
    private final String telegramId; // cannot be renamed

    /**
     * Creates a <code>JTelegramKeys</code> which holds the client, secret, and telegram keys provided in the
     * constructor.
     * @param client a <code>String</code> which contains the <code>clientKey</code>
     * @param secret a <code>String</code> which contains the <code>secretKey</code>
     * @param teleID a <code>String</code> which contains the <code>telegramId</code>
     */
    public JTelegramKeys(String client, String secret, String teleID) {
        clientKey = client.trim();
        secretKey = secret.trim();
        telegramId = teleID.trim();
    }

    /** Creates a purposely invalid "empty" <code>JTelegramKeys</code>. */
    public JTelegramKeys() {
        // cannot be removed; IO code relies on this 'empty' constructorclientKey = "CLIENT_KEY";
        clientKey = "CLIENT_KEY";
        secretKey = "SECRET_KEY";
        telegramId = "TELEGRAM_ID";
    }

    @Override
    public String toString() {
        return String.join(", ", Arrays.asList(clientKey, secretKey, telegramId));
    }

    public String getClientKey() {
        return (clientKey != null) ? clientKey : "CLIENT_KEY";
    }

    public String getSecretKey() {
        return (secretKey != null) ? secretKey : "SECRET_KEY";
    }

    public String getTelegramID() {
        return (telegramId != null) ? telegramId : "TELEGRAM_ID";
    }

    /** @return {@code String[]} with elements {@code { clientKey, secretKey, telegramId }}. */
    @Deprecated
    public String[] toArray() {
        return new String[] {clientKey, secretKey, telegramId};
    }

    /** @return true if any key is empty or null. */
    @SuppressWarnings("RedundantIfStatement")
    public boolean anyEmpty() {
        if (Objects.isNull(clientKey) || clientKey.isEmpty() || clientKey.equals("CLIENT_KEY")) return true;
        if (Objects.isNull(secretKey) || secretKey.isEmpty()|| secretKey.equals("SECRET_KEY")) return true;
        if (Objects.isNull(telegramId) || telegramId.isEmpty()|| telegramId.equals("TELEGRAM_ID")) return true;
        return false;
    }

}
