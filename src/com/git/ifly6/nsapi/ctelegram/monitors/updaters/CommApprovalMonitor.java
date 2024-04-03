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

import com.git.ifly6.nsapi.ctelegram.io.CommParseException;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommPermanentCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatableMonitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.OptionalLong;
import java.util.Set;
import java.util.stream.Collectors;

import static com.git.ifly6.nsapi.ctelegram.io.cache.CommPermanentCache.createKey;

/**
 * Monitors approval actions related to a World Assembly proposal.
 * @since version 13
 */
public class CommApprovalMonitor extends CommUpdatableMonitor implements CommMonitor {

    private static final CommPermanentCache<CommApprovalMonitor> cache = new CommPermanentCache<>();

    private boolean exhausted;

    private final String proposalID;
    private final Action action;

    private Set<String> allApproversEver = new HashSet<>();
    private Set<String> currentApprovers = new HashSet<>();

    /** Creates monitor to monitor provided proposal ID for specified action. */
    private CommApprovalMonitor(String proposalID, Action action) {
        this.proposalID = proposalID;
        this.action = action;
    }

    /**
     * Gets or creates monitor from string input.
     * @param proposalID   as passed
     * @param actionString {@link Action}
     * @return existing cached instance or, if no such instance, new monitor
     */
    public static CommApprovalMonitor getInstance(String actionString, String proposalID) {
        try {
            Action action = Action.valueOf(actionString.toUpperCase());
            return cache.get(
                    createKey(proposalID, actionString),
                    () -> new CommApprovalMonitor(proposalID, action));
        } catch (IllegalArgumentException e) { throw CommParseException.make(actionString, Action.values(), e); }
    }

    /**
     * {@inheritDoc} Only returns approvers which undertook the specified {@link Action} at any time.
     * @return list of delegates that took specified action.
     */
    @Override
    public List<String> getAction() {
        if (exhausted)
            throw new ExhaustedException(
                    String.format("Proposal %s no longer exists; approval stream exhausted.",
                            proposalID));

        Set<String> changedApprovers = action.find(currentApprovers, allApproversEver);
        return new ArrayList<>(changedApprovers);
    }

    @Override
    public boolean recipientsExhausted() {
        return exhausted;
    }

    @Override
    public OptionalLong recipientsCount() {
        return OptionalLong.of(this.getRecipients().size());
    }

    @Override
    protected void updateAction() {
        try {
            currentApprovers = new HashSet<>(CommWorldAssembly.getApprovers(proposalID));
            allApproversEver.addAll(currentApprovers); // set deals with duplicates automatically
        } catch (CommWorldAssembly.NoSuchProposalException e) { exhausted = true; }
    }

    /**
     * Enumerates actions undertaken by delegates.
     * @since version 13
     */
    public enum Action {
        GIVEN_TO {
            @Override
            public Set<String> find(Set<String> current, Set<String> allApproversEver) {
                return current;
                // elements in after that are were not in before
                // return new ArrayList<>(Sets.difference(new HashSet<>(after), new HashSet<>(before)));
            }
        },

        REMOVED_FROM {
            @Override
            public Set<String> find(Set<String> current, Set<String> allApproversEver) {
                return allApproversEver.stream()
                        .filter(s -> !current.contains(s)) // every approver ever who is not in current are removed
                        .collect(Collectors.toSet());
                // elements in before that are not in afterSet
                // return new ArrayList<>(Sets.difference(new HashSet<>(before), new HashSet<>(after)));
            }
        };

        public abstract Set<String> find(Set<String> current, Set<String> allApproversEver);
    }
}
