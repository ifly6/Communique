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

package com.git.ifly6.nsapi.ctelegram.monitors.rules;

import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;

import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;

/**
 * Creates a {@link CommMonitor} which passes provided elements only once.
 */
public class CommExhaustiveMonitor implements CommMonitor {

    private final CommMonitor monitor;
    private final Set<String> passed;

    private CommExhaustiveMonitor(Set<String> passCache, CommMonitor monitor) {
        this.passed = passCache;
        this.monitor = monitor;
    }

    public CommExhaustiveMonitor(CommMonitor monitor) {
        this(new HashSet<>(), monitor);
    }

    /**
     * "Updates" the existing monitor such that the list of sent nations is retained.
     * @param newMonitor to use for future data calls
     * @return new {@code CommExhaustiveMonitor} with retained {@code passed} set
     */
    public CommExhaustiveMonitor with(CommMonitor newMonitor) {
        return new CommExhaustiveMonitor(passed, newMonitor);
    }

    /**
     * @return recipients, less any recipients already passed.
     */
    @Override
    public List<String> getRecipients() {
        List<String> result = monitor.getRecipients();
        result.removeIf(passed::contains);

        passed.addAll(result);
        return result;
    }

    @Override
    public boolean recipientsExhausted() {
        return monitor.recipientsExhausted();
    }

    @Override
    public OptionalLong recipientsCountIfKnown() {
        return monitor.recipientsCountIfKnown();
    }
}
