/*
 * Copyright (c) 2022 ifly6
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

import com.git.ifly6.nsapi.NSNation;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.Reader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.git.ifly6.commons.CommuniqueApplication.APP_SUPPORT;

/**
 * Caches information about nations. Expiration duration is {@link CommCache#DEFAULT_EXPIRATION_DURATION}.
 * @since version 3.0 (build 13)
 */
public class CommNationCache extends CommCache<NSNation> {

    private static final Path LOCATION = APP_SUPPORT.resolve("nation_cache.json");
    private static final Logger LOGGER = Logger.getLogger(CommNationCache.class.getName());
    private static final Duration CACHE_DURATION = Duration.of(10, ChronoUnit.DAYS);

    private static CommNationCache instance;

    private CommNationCache() {
        super(CACHE_DURATION);
        this.setFinaliser(() -> {
            try (BufferedWriter bw = Files.newBufferedWriter(LOCATION);) {
                Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
                gson.toJson(this, bw);
            } catch (IOException e) {
                LOGGER.log(Level.SEVERE, "Unable to save nation cache!", e);
            }
        });
    }

    private static CommNationCache makeInstance() {
        if (!Files.exists(LOCATION))
            return new CommNationCache();

        try {
            Reader reader = Files.newBufferedReader(LOCATION);
            // should be irrelevant... the finaliser is a transient runnable and should never be serialised
//            if (!cache.hasFinaliser())
//                LOGGER.log(Level.SEVERE, "Serialised version of CommNationCache lacks save finaliser!");
            return new Gson().fromJson(reader, CommNationCache.class);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to load persist version of nation cache!", e);
            return new CommNationCache();
        }
    }

    public static CommNationCache getInstance() {
        if (instance == null) instance = makeInstance();
        return instance;
    }

    /**
     * {@inheritDoc} Here applies to {@link NSNation}s.
     * @param s name of nation
     * @return cached data-populated corresponding {@link NSNation}
     * @throws NSNation.NSNoSuchNationException if attempting to load non-existent nation
     */
    @Override
    public NSNation lookupObject(String s) {
        return super.lookupObject(s);
    }

    /** @throws NSNation.NSNoSuchNationException from {@link NSNation#populateData()} */
    @Override
    protected NSNation createNewObject(String s) {
        return new NSNation(s).populateData();
    }
}
