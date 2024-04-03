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

package com.git.ifly6.nsapi.ctelegram.monitors;

import com.git.ifly6.nsapi.ApiUtils;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Arrays;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Collectors;

/**
 * Framework for creating monitors which update. Each monitor caches data and provides it per {@link CommMonitor};
 * update monitors have defined update intervals where their cached data change.
 * @since version 13
 */
public abstract class CommUpdatableMonitor implements CommMonitor {

    private Instant lastUpdate;

    public CommUpdatableMonitor() { }

    /**
     * Actual implementation of {@link #getRecipients}.
     * @return a list of recipients to pass on
     */
    protected abstract List<String> getAction();

    /**
     * Gets recipients from the monitor. When called, it forces the monitor to update unless it last updated within the
     * last 20 seconds.
     * @return recipients in the monitor
     * @see #getAction()
     */
    public final List<String> getRecipients() {
        Instant TWENTY_SECONDS_AGO = Instant.now().minus(20, ChronoUnit.SECONDS);
        if (lastUpdate == null || lastUpdate.isBefore(TWENTY_SECONDS_AGO)) update();
        return getAction();
    }

    /**
     * Actual implementation of {@link #update()}. Prior to calling this method, the monitor makes sure that it is
     * not exhausted. It then calls the update action. After updating, it saves the time of completion as
     * {@link #lastUpdate}.
     * @see #update()
     */
    protected abstract void updateAction();

    private CommMonitor update() {
        if (this.recipientsExhausted())
            throw new ExhaustedException(String.format("%s (%s) is exhausted",
                    this.getClass().getSimpleName(), this.toString()
            ));
        updateAction();
        lastUpdate = Instant.now();
        return this;
    }

    /** @return {@link Instant} of last update completion. */
    public Instant getLastUpdate() { return lastUpdate; }

    /**
     * Turns provided list, as string, in format {@code BLAH, BLAH, BLAH} into a list of strings. Splits only on commas.
     * All elements are normalised with {@link ApiUtils#ref(List)}.
     * @param delimitedString to parse
     * @return {@code List<String>} representation thereof
     */
    public static List<String> parseList(String delimitedString) {
        List<String> newList;
        if (!delimitedString.contains(",")) newList = List.of(ApiUtils.ref(delimitedString));
        else newList = Arrays.stream(delimitedString.split(",\\s+"))
                .map(ApiUtils::ref)
                .filter(Predicate.not(String::isBlank))
                .collect(Collectors.toList());
        return newList;
    }

}
