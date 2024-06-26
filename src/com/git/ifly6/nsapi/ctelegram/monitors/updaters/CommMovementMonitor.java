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

import com.git.ifly6.nsapi.NSRegion;
import com.git.ifly6.nsapi.ctelegram.io.CommParseException;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommPermanentCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatableMonitor;
import com.google.common.collect.EvictingQueue;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.OptionalLong;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Monitors movement in or out, see {@link Direction}, of a specified region.
 * @since version 13
 */
public class CommMovementMonitor extends CommUpdatableMonitor implements CommMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommMovementMonitor.class.getName());
    private static final CommPermanentCache<CommMovementMonitor> cache = new CommPermanentCache<>();
    private static final int EVICT_QUEUE_SIZE = 100;

    private final List<String> regions;
    private final Direction direction;

    private EvictingQueue<String> latestMovedRecipients = EvictingQueue.create(EVICT_QUEUE_SIZE);
    private Set<String> inhabitantsBefore;
    private Set<String> inhabitantsNow;

    /**
     * Creates a movement monitor.
     * @param regions   to monitor
     * @param direction of travel to monitor
     */
    public CommMovementMonitor(List<String> regions, Direction direction) {
        this.regions = regions;
        this.direction = direction;
    }

    /**
     * Gets or creates monitor from string input.
     * @param dirString  direction string {@link Direction}
     * @param regionList list of regions, separated by commas
     * @return new monitor or existing instance if already exists
     */
    public static CommMovementMonitor getInstance(String dirString, String regionList) {
        List<String> regions = parseList(regionList);
        try {
            Direction direction = Direction.valueOf(dirString);
            return cache.get(CommPermanentCache.createKey(regionList, dirString),
                    () -> new CommMovementMonitor(regions, direction));
        } catch (IllegalArgumentException e) { throw CommParseException.make(dirString, Direction.values(), e); }
    }

    /** Recipients generate slowly, but never exhaust. */
    @Override
    public boolean recipientsExhausted() {
        return false;
    }

    @Override
    public OptionalLong recipientsCount() {
        return OptionalLong.empty();
    }

    @Override
    protected void updateAction() {
        if (Objects.isNull(regions) || regions.isEmpty())
            throw new IllegalArgumentException("job started without specifying region");

        inhabitantsBefore = inhabitantsNow;
        Set<String> newInhabitants = new HashSet<>();
        for (final String regionName : regions)
            newInhabitants.addAll(
                    new NSRegion(regionName).populateData().getRegionMembers()
            );

        inhabitantsNow = newInhabitants;
    }

    /**
     * {@inheritDoc} Recipients can only be found after some amount of time. If not enough time has elapsed, returns an
     * empty list; otherwise, returns nations depending on {@link Direction}.
     * <p>Note that recipients returned for multiple regions are movements in and out of those regions taken together.
     * If monitoring Europe and the North Pacific, someone who moves from the North Pacific <b>to</b> Europe will not be
     * marked.</p>
     * @returns list of last {@value EVICT_QUEUE_SIZE} moved recipients
     */
    @Override
    public List<String> getAction() {
        if (inhabitantsNow == null || inhabitantsBefore == null) {
            LOGGER.info("Not enough information to find any changes.");
            return new ArrayList<>();
        }

        List<String> movedRecipients = direction.apply(inhabitantsBefore, inhabitantsNow);
        latestMovedRecipients.addAll(movedRecipients);
        return new ArrayList<>(latestMovedRecipients);
    }

    /** @return regions at which the movement monitor is pointed. */
    public List<String> getRegions() {
        return regions;
    }

    public enum Direction {

        INTO {
            /** {@inheritDoc} Something that enters present after but not present before. */
            @Override
            public List<String> apply(Set<String> before, Set<String> after) {
                return after.stream()
                        .filter(s -> !before.contains(s))
                        .collect(Collectors.toList());
            }
        },

        OUT_OF {
            /** {@inheritDoc} Something that exits is present before, but not after. */
            @Override
            public List<String> apply(Set<String> before, Set<String> after) {
                return before.stream()
                        .filter(s -> !after.contains(s))
                        .collect(Collectors.toList());
            }
        };

        /**
         * Applies differencing algorithm based on enumerated description
         * @param before elements present before
         * @param after  elements present after
         * @return appropriate difference
         */
        public abstract List<String> apply(Set<String> before, Set<String> after);
    }
}


