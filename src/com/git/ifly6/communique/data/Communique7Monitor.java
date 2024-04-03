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

package com.git.ifly6.communique.data;

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatableMonitor;

import java.util.List;
import java.util.OptionalLong;
import java.util.logging.Logger;

/**
 * Packages the recipients into a single monitor for reproducibility.
 * @since version 13
 */
public class Communique7Monitor extends CommUpdatableMonitor {

    private static final Logger LOGGER = Logger.getLogger(Communique7Monitor.class.getName());
    private static final int TRUNCATION_LIMIT = 6; // every 3 minutes

    private State state;

    private CommuniqueConfig theConfig;

    private int updateCalls = 0;
    private List<String> lastParse;

    public Communique7Monitor(CommuniqueConfig communiqueConfig) {
        this.theConfig = communiqueConfig;
        this.state = State.INIT;
    }

    public Communique7Monitor setConfig(CommuniqueConfig theConfig) {
        this.theConfig = theConfig;
        return this;
    }

    @Override
    protected List<String> getAction() {
        return lastParse;
    }

    @Override
    protected void updateAction() {
        updateCalls++;
        parseRecipients();
    }

    /**
     * Previews recipients the monitor expects to provide. If not loaded, loads them.
     * @return first update list of recipients
     * @throws UnsupportedOperationException if not in initialisation
     * @see #getRecipients()
     */
    public List<String> preview() {
        if (state != State.INIT) throw new UnsupportedOperationException("Cannot peek outside of initialisation");
        if (lastParse == null) parseRecipients();
        return lastParse;
    }

    /**
     * Parses recipients based on the current configuration setting. Caches this information in {@link #lastParse}.
     * @return {@link #lastParse} after updating it from {@link Communique7Parser}
     */
    private List<String> parseRecipients() {
        this.lastParse = theConfig.processingAction.apply(
                new Communique7Parser().apply(theConfig.getcRecipients()).listRecipients());

        LOGGER.finer(String.format("Monitor parsed %d recipients", lastParse.size()));
        return this.lastParse;
    }

    @Override
    public boolean recipientsExhausted() {
        if (this.state == State.INIT) return false;
        if (!theConfig.repeats && updateCalls >= 1) return true;
        return false;
    }

    /**
     * {@inheritDoc} For Communique7Monitor, returns the size of {@link #lastParse}, ie all known possible recipients.
     * This number can change over time if repeating.
     * @return estimated number of remaining recipients; {@link OptionalLong#empty()} if repeating.
     */
    @Override
    public OptionalLong recipientsCount() {
        if (theConfig.repeats) return OptionalLong.empty();
        return OptionalLong.of(lastParse.size());
    }

    /** Expresses the state – {@link #INIT} or {@link #RUNNING} – of the monitor. */
    public enum State {
        INIT, RUNNING
    }
}
