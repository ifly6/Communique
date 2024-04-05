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
import com.git.ifly6.nsapi.ctelegram.CommSender;
import com.git.ifly6.nsapi.ctelegram.CommSenderInterface;
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

    private State state;
    private CommuniqueConfig theConfig; // this wraps the cRecipient list and should therefore mutate!

    private int updateCalls = 0;
    private List<String> lastParse;

    public Communique7Monitor(CommuniqueConfig communiqueConfig) {
        this.theConfig = communiqueConfig;
        this.state = State.INIT;
    }

    @Override
    protected List<String> getAction() {
        if (state == State.INIT) state = State.RUNNING;
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
     * Operates in place.
     */
    private void parseRecipients() {
        if (lastParse != null && this.state == State.INIT)
            /*
            When the monitor initialises, Communique will first call for a preview. This will populate lastParse.
            After that, it passes the monitor to CommSender, which calls getRecipients(). That will first call update,
            which then will call updateAction(), calling this method. At that time, lastParse will be non-null and the
            state will still be INIT because only AFTER parseRecipients() returns its payload will the state be tripped
            to RUNNING by getAction().
            */
            return;

        List<String> parseResults = new Communique7Parser().apply(theConfig.getcRecipients()).listRecipients();
        this.lastParse = theConfig.processingAction.apply(parseResults);

        LOGGER.info(String.format("Monitor parsed %d recipients", lastParse.size()));
    }

    /**
     * If the monitor is initialising, this will never exhaust. Once it initialises, if the configuration says it
     * repeats, it will never exhaust. If the configuration is <b>not</b> repeating, it has been parsed more than once,
     * and the last parsing attempt returned empty, it will exhaust.
     * @return whether the parse recipients are exhausted
     */
    @Override
    public boolean recipientsExhausted() {
        if (this.state == State.INIT || theConfig.repeats) return false;
        if (updateCalls >= 1 && lastParse.isEmpty()) return true;
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

    /**
     * Convenience method for constructing {@link CommSender}.
     * @param withInterface interface to export data from sender with
     */
    public CommSender constructSender(CommSenderInterface withInterface) {
        return new CommSender(theConfig.keys, this, theConfig.getTelegramType(), withInterface);
    }

    /** Expresses the state – {@link #INIT} or {@link #RUNNING} – of the monitor. */
    public enum State {
        INIT, RUNNING
    }
}
