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

package com.git.ifly6.nsapi.ctelegram.monitors.rules;

import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatingMonitor;

import java.util.List;
import java.util.OptionalLong;
import java.util.function.Supplier;
import java.util.logging.Logger;

/**
 * Decorates an {@link CommUpdatingMonitor} and forces anything calling it to wait until that monitor fully initialises,
 * see {@link CommUpdatingMonitor#getLatch()}, before allowing any {@link CommMonitor} calls.
 * @since version 13
 */
public class CommDelayedMonitor implements CommMonitor {

    private static final Logger LOGGER = Logger.getLogger(CommDelayedMonitor.class.getName());
    private CommUpdatingMonitor monitor;

    /**
     * Constructs decorator for the provided monitor.
     * @param monitor to wrap
     */
    public CommDelayedMonitor(CommUpdatingMonitor monitor) {
        this.monitor = monitor;
    }

    /** {@inheritDoc} Waits until monitor has initialised. */
    @Override
    public List<String> getRecipients() {
        return wait(monitor::getRecipients);
    }

    /** {@inheritDoc} Only determines exhaustion after monitor has initialised. */
    @Override
    public boolean recipientsExhausted() {
        return wait(monitor::recipientsExhausted);
    }

    /** {@inheritDoc} */
    @Override
    public OptionalLong recipientsCountIfKnown() {
        return wait(monitor::recipientsCountIfKnown);
    }

    /**
     * Waits for completed initialisation, then calls supplier.
     * @param supplier thing that actually provides what we want
     * @param <T>      return type
     * @return output of supplier after waiting (if necessary)
     */
    private <T> T wait(Supplier<T> supplier) {
        try {
            monitor.getLatch().await();
            return supplier.get();

        } catch (InterruptedException e) {
            LOGGER.warning("Awaiting monitor interrupted");
            throw new UnsupportedOperationException("Await monitor refuses to be interrupted!");
        }
    }
}
