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
package com.git.ifly6.nsapi.ctelegram.io.cache;

import com.git.ifly6.gson.CommuniqueGson;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSTimeStamped;
import com.git.ifly6.nsapi.NSWorld;
import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Objects;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.git.ifly6.CommuniqueApplication.APP_SUPPORT;

/**
 * Caches NationStates delegates for later access. <b>Only invoke with {@link #DELEGATE_KEY}!</b> Cache expiration time
 * for this cache is thirty minutes; prefer {@link #getDelegates()}.
 * @since version 13
 */
public class CommDelegatesCache extends CommCache<CommDelegatesCache.Delegates> {

    private static final Logger LOGGER = Logger.getLogger(CommDelegatesCache.class.getName());
    public static final String DELEGATE_KEY = "__delegates__";
    private static final Path LOCATION = APP_SUPPORT.resolve("delegate_cache.json");
    public static final Duration CACHE_DURATION = Duration.ofMinutes(30);

    private static CommDelegatesCache instance;

    private CommDelegatesCache() {
        super(CACHE_DURATION);
        this.setFinaliser(() -> {
            try (BufferedWriter bw = Files.newBufferedWriter(LOCATION)) {
                Gson gson = CommuniqueGson.createNew();
                gson.toJson(this, bw);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to save nation cache!", e);
            }
        });
    }

    private static CommDelegatesCache makeInstance() {
        try {
            CommDelegatesCache c = CommuniqueGson.createNew().fromJson(
                    Files.newBufferedReader(LOCATION), CommDelegatesCache.class);
            c.purge();
            return c;

        } catch (NoSuchFileException | FileNotFoundException e) {
            return new CommDelegatesCache();

        } catch (JsonSyntaxException | IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to load persist version of delegates cache!", e);
            return new CommDelegatesCache();
        }
    }

    /** Gets the one cache. */
    public static CommDelegatesCache getInstance() {
        if (instance == null) instance = makeInstance();
        return instance;
    }

    @Override
    protected Delegates createNewObject(String s) { return new Delegates(); }

    /** Prefer {@link #getDelegates()}. */
    @Override
    public Delegates lookupObject(String s) {
        if (s.equals(DELEGATE_KEY)) return super.lookupObject(s);
        throw new UnsupportedOperationException("Delegates cache only supports looking up with " + DELEGATE_KEY);
    }

    /**
     * Gets cached delegates list
     * @return cached delegates list
     * @see #CACHE_DURATION
     */
    public List<String> getDelegates() {
        return lookupObject(DELEGATE_KEY).getDelegates();
    }

    /**
     * Gets delegates with a maximum age of 70 seconds.
     * @return list of delegates no older than 70 seconds ago
     */
    public List<String> getDelegatesNow() {
        return lookupObject(DELEGATE_KEY, Duration.of(60 + 10, ChronoUnit.SECONDS)).getDelegates();
    }

    /** Holds a time-stamped version of NationStates delegates. */
    public static class Delegates implements NSTimeStamped {

        private Instant timestamp;
        private List<String> delegates;

        public Delegates() {
            try {
                delegates = NSWorld.getDelegates();
                timestamp = Instant.now();

            } catch (UnknownHostException e) {
                throw new NSIOException("No connection to NationStates", e);
            } catch (IOException e) {
                throw new NSIOException("Could not get delegates from NationStates", e);
            }
        }

        public List<String> getDelegates() { return delegates; }

        @Override
        public Instant timestamp() { return timestamp; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Delegates)) return false;
            Delegates delegates1 = (Delegates) o;
            return Objects.equals(timestamp, delegates1.timestamp) && Objects.equals(delegates,
                    delegates1.delegates);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, delegates);
        }
    }
}
