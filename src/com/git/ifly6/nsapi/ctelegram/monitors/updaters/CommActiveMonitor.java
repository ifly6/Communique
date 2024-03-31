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

import com.git.ifly6.nsapi.ctelegram.io.CommHappenings;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.rules.CommExhaustiveMonitor;

import java.time.Duration;
import java.time.Instant;
import java.util.AbstractMap.SimpleEntry;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

/**
 * Creates an updating monitor for the NationStates Happenings API. Delivers, non-exhaustively, any nation which
 * appeared in the happenings in the last {@value MINUTES} minutes.
 */
public class CommActiveMonitor extends CommUpdatingMonitor {

    private static final long MINUTES = 10; // must be separate to allow for @value
    private static final Duration ACTIVE_MIN = Duration.ofMinutes(MINUTES);
    private static CommActiveMonitor instance;
    private static CommMonitor exhaustiveInstance;

    private Set<Entry<Instant, String>> cache;

    public CommActiveMonitor() {
        this.cache = new HashSet<>();
    }

    /** @returns instance; there is only one happenings to be monitoring */
    public static CommActiveMonitor getInstance() {
        if (instance == null)
            instance = new CommActiveMonitor();
        return instance;
    }

    /** @return exhaustive instance, decorated such that it never gives the same nation twice */
    public static CommMonitor getExhaustiveInstance() {
        if (exhaustiveInstance == null)
            exhaustiveInstance = new CommExhaustiveMonitor(CommActiveMonitor.getInstance());
        return exhaustiveInstance;
    }

    @Override
    public List<String> getRecipients() {
        Instant now = Instant.now();
        List<String> toReturn = new ArrayList<>();
        for (Entry<Instant, String> entry : cache)
            if (entry.getKey().isAfter(now.minus(ACTIVE_MIN)))
                toReturn.add(entry.getValue());

        return toReturn;
    }

    /** Does not exhaust. */
    @Override
    public boolean recipientsExhausted() {
        return false;
    }

    @Override
    protected void updateAction() {
        Instant now = Instant.now();
        List<String> newActiveNations = CommHappenings.getActiveNations();
        for (String s : newActiveNations)
            cache.add(new SimpleEntry<>(now, s));
    }

}
