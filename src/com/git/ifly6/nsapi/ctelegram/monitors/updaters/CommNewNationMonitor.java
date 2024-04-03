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

package com.git.ifly6.nsapi.ctelegram.monitors.updaters;

import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatableMonitor;

import java.io.IOException;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Comparator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Creates an updating monitor for the NationStates Happenings API. Delivers all known nations founded within the last
 * thirty minutes.
 */
public class CommNewNationMonitor extends CommUpdatableMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommNewNationMonitor.class.getName());
    private static CommNewNationMonitor instance;

    private Set<CommNewNation> cache;

    public CommNewNationMonitor() {
        this.cache = new LinkedHashSet<>();
    }

    /** @returns instance; there is only one feed of new nations to be monitoring */
    public static CommNewNationMonitor getInstance() {
        if (instance == null) instance = new CommNewNationMonitor();
        return instance;
    }

    /**
     * {@inheritDoc} This yields all nations that it is aware of which were founded within thirty minutes of the
     * {@link #getLastUpdate()}.
     * @return list of new nations
     */
    @Override
    public List<String> getAction() {
        return cache.stream()
                .sorted(Comparator.comparing(CommNewNation::timestamp).reversed()) // sort newest first
                .map(i -> i.name)
                .distinct()
                .collect(Collectors.toList());
    }

    /** Does not exhaust. */
    @Override
    public boolean recipientsExhausted() { return false; }

    @Override
    public OptionalLong recipientsCount() { return OptionalLong.empty(); }

    @Override
    protected void updateAction() {
        Instant THIRTY_MINUTES_AGO = Instant.now().minus(30, ChronoUnit.SECONDS);
        var toAdd = getNewest();
        if (toAdd.isEmpty())
            return; // preserve existing data!

        cache.removeIf(n -> n.timestamp().isAfter(THIRTY_MINUTES_AGO)); // no ~~capes~~ memory leaks
        cache.addAll(toAdd);
    }

    private Set<CommNewNation> getNewest() {
        try {
            List<CommNewNation> newest = CommNewNation.getNewNations();
            newest.sort(Comparator.comparing(CommNewNation::timestamp));
            return new LinkedHashSet<>(newest);

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Encountered exception when getting new nations!", e);
            return new LinkedHashSet<>();
        }
    }
}
