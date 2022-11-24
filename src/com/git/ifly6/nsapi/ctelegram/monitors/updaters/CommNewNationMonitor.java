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

package com.git.ifly6.nsapi.ctelegram.monitors.updaters;

import com.git.ifly6.nsapi.NSWorld;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Creates an updating monitor for the NationStates Happenings API. Delivers, non-exhaustively, the last ten recently
 * created nations.
 */
public class CommNewNationMonitor extends CommUpdatingMonitor {

    private static CommNewNationMonitor instance;

    private long returnLimit;
    private Map<String, Instant> cache; // k : v == nation name : observation time
    private Set<String> passed;

    public CommNewNationMonitor() {
        this.returnLimit = 10;
        this.cache = new HashMap<>();
        this.passed = new HashSet<>();
    }

    /** @returns instance; there is only one feed of new nations to be monitoring */
    public static CommNewNationMonitor getInstance() {
        if (instance == null){
            instance = new CommNewNationMonitor();
            instance.setUpdateInterval(Duration.ofSeconds(180 - 10)); // 170 seconds
        }
        return instance;
    }

    public CommNewNationMonitor setBatch(long i) {
        this.returnLimit = i;
        return this;
    }

    @Override
    public List<String> getRecipients() {
        List<String> toReturn = cache.entrySet().stream()
                .sorted(Entry.comparingByValue()) // sort the entry set, newest first
                .filter(e -> !passed.contains(e.getKey())) // remove if already passed
                .limit(returnLimit) // get the N nations most recently observed
                .map(Entry::getKey)
                .collect(Collectors.toList());

        passed.addAll(toReturn);
        return toReturn;
    }

    /** Does not exhaust. */
    @Override
    public boolean recipientsExhausted() {
        return false;
    }

    @Override
    protected void updateAction() {
        List<String> newNations = NSWorld.getNew();
        for (String s : newNations)
            cache.put(s, Instant.now());  // preserve encounter order, ns still pass
    }
}
