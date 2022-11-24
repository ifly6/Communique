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

import com.git.ifly6.nsapi.NSNation;
import com.git.ifly6.nsapi.NSWorld;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommNationCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Creates an updating monitor for the NationStates Happenings API. Delivers, non-exhaustively, the last ten recently
 * created nations.
 */
public class CommRecruitMonitor extends CommUpdatingMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommRecruitMonitor.class.getName());
    private static CommRecruitMonitor instance;

    private long returnLimit;
    private Map<String, Instant> cache; // k : v => nation name : observation time
    private Set<String> passed;

    public CommRecruitMonitor() {
        this.returnLimit = 10;
        this.cache = new HashMap<>();
        this.passed = new HashSet<>();
    }

    /** @returns instance; there is only one feed of new nations to be monitoring */
    public static CommRecruitMonitor getInstance() {
        if (instance == null)
            instance = new CommRecruitMonitor();
        return instance;
    }

    /**
     * Sets the batch return limit and returns this monitor after that limit is set.
     * @param i is the limit to set
     * @return this
     */
    public CommRecruitMonitor setBatchLimit(long i) {
        this.returnLimit = i;
        return this;
    }

    /**
     * {@inheritDoc} If you want to limit the number of recipients, use {@link #setBatchLimit(long)} first before
     * calling {@link #getRecipients()}
     * @return list of new nations, up to the {@link #returnLimit}
     */
    @Override
    public List<String> getRecipients() {
        // 1st. sort to get newest nations in the list, return only
        List<String> toReturn = cache.entrySet().stream()
                .sorted(Entry.comparingByValue(Comparator.reverseOrder())) // sort the entry set, newest first
                .map(Entry::getKey)
                .filter(s -> !passed.contains(s)) // remove if already passed
                .collect(Collectors.toList()); // should still pass all instantly-available values
        LOGGER.info(String.format("Found %d un-passed new nations; passing %d", toReturn.size(), returnLimit));

        // 2nd. sort out the ones which can be recruited until you get to the recruit limit
        toReturn = toReturn.stream()
                .map(s -> CommNationCache.getInstance().lookupObject(s))
                .filter(NSNation::isRecruitable) // filter is lazy
                .limit(returnLimit) // get the N nations most recently observed; limit works with lazy filter
                .map(NSNation::getRefName)
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
        Collections.reverse(newNations); // acts in-place

        // force the "instant" at which something was added to the cache to be different
        Instant now = Instant.now();
        for (int i = 0; i < newNations.size(); i++)
            cache.put(
                    newNations.get(i),
                    now.plus(Duration.ofMillis(i)) // adds 1 ms to the "now" time without taking actual time
            );
    }
}
