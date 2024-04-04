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

import com.git.ifly6.nsapi.NSHappenings;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatableMonitor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.OptionalLong;
import java.util.stream.Collectors;

/**
 * Creates an updating monitor for the NationStates Happenings API. Delivers, non-exhaustively, any nation which
 * appeared in the happenings in the last {@value MINUTES} minutes.
 */
public class CommActiveMonitor extends CommUpdatableMonitor {

    private static final long MINUTES = 10; // must be separate to allow for @value
    private static final Duration ACTIVE_MIN = Duration.ofMinutes(MINUTES);
    private static CommActiveMonitor instance;

    private Map<String, Instant> cache;

    private CommActiveMonitor() { this.cache = new HashMap<>(); }

    public static CommActiveMonitor getInstance() {
        if (instance == null) instance = new CommActiveMonitor();
        return instance;
    }

    @Override
    public List<String> getAction() {
        Instant cutoff = Instant.now().minus(ACTIVE_MIN);
        return cache.entrySet().stream()
                .filter(entry -> entry.getValue().isAfter(cutoff))
                .map(Entry::getKey)
                .collect(Collectors.toList());
    }

    /** Does not exhaust. */
    @Override
    public boolean recipientsExhausted() { return false; }

    @Override
    public OptionalLong recipientsCount() { return OptionalLong.of(getRecipients().size()); }

    @Override
    protected void updateAction() {
        Map<String, Instant> newActiveNations = NSHappenings.getActiveNations();
        if (newActiveNations.isEmpty()) return;

        Instant THIRTY_MINUTES_AGO = Instant.now().minus(30, ChronoUnit.MINUTES);
        cache.putAll(newActiveNations);
        cache.entrySet().removeIf(entry -> entry.getValue().isBefore(THIRTY_MINUTES_AGO));
    }

}
