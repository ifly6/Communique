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
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Chamber;
import com.git.ifly6.nsapi.ctelegram.io.CommWorldAssembly.Vote;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommPermanentCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommUpdatableMonitor;
import org.javatuples.Pair;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.OptionalLong;
import java.util.Set;
import java.util.logging.Logger;

import static com.git.ifly6.nsapi.ctelegram.io.cache.CommPermanentCache.createKey;

/**
 * Monitors voters votes for or against a resolution at vote. Automatically exhausts if the resolution at vote changes.
 * @since version 13
 */
public class CommVoteMonitor extends CommUpdatableMonitor {

    public static final Logger LOGGER = Logger.getLogger(CommVoteMonitor.class.getName());
    private static CommPermanentCache<CommVoteMonitor> cache = new CommPermanentCache<>();

    private final Chamber chamber;
    private final Vote voting;
    private final String proposalID;

    private Map<Pair<Chamber, Vote>, Set<String>> voteData;
    private Instant monitorLastUpdate;

    /**
     * @param chamber to check for voters
     * @param voting  direction; choose one
     */
    private CommVoteMonitor(Chamber chamber, Vote voting) {
        this.chamber = chamber;
        this.voting = voting;
        this.voteData = new HashMap<>();
        proposalID = CommWorldAssembly.getResolutionID(chamber);
    }

    /**
     * Gets or creates monitor from string input.
     * @return new monitor or existing instance if already exists
     */
    public static CommVoteMonitor getInstance(String chamber, String voting) {
        Pair<Chamber, Vote> values = parseStrings(chamber, voting);
        return cache.get(createKey(chamber, voting),
                () -> new CommVoteMonitor(values.getValue0(), values.getValue1()));
    }

    /** Updates the vote data if not updated within the last three seconds. */
    @Override
    protected void updateAction() {
        final Instant THREE_SECONDS_AGO = Instant.now().minus(Duration.ofSeconds(3));
        if (monitorLastUpdate.isBefore(THREE_SECONDS_AGO)) {
            CommWorldAssembly.BothChamberVoters voters = CommWorldAssembly.getVoters(chamber);
            for (Vote position : Vote.values())
                voteData.put(
                        new Pair<>(chamber, position),
                        new HashSet<>(voters.getVoters(position))
                );
            monitorLastUpdate = Instant.now();
        }
    }

    @Override
    public Instant getLastUpdate() {
        return monitorLastUpdate;
    }

    @Override
    public List<String> getAction() {
        return new ArrayList<>(voteData.get(new Pair<>(this.chamber, this.voting)));
    }

    @Override
    public boolean recipientsExhausted() {
        try {
            String currentID = CommWorldAssembly.getResolutionID(chamber);
            if (currentID.equals(proposalID)) return false;
            return true;
        } catch (CommWorldAssembly.NoSuchProposalException e) { return false; }
    }

    @Override
    public OptionalLong recipientsCount() {
        return OptionalLong.of(getRecipients().size());
    }

    /**
     * Parses data from provided string
     * @param chamber {@link Chamber}
     * @param voting  {@link Vote}
     * @return internal representations in quartet
     */
    private static Pair<Chamber, Vote> parseStrings(String chamber, String voting) {
        Chamber chamberEnum;
        Vote voteEnum;

        try {
            chamberEnum = Chamber.valueOf(chamber.toUpperCase());
        } catch (IllegalArgumentException e) { throw CommParseException.make(chamber, Chamber.values(), e); }

        try {
            voteEnum = Vote.valueOf(voting.toUpperCase());
        } catch (IllegalArgumentException e) { throw CommParseException.make(voting, Vote.values(), e); }

        return new Pair<>(chamberEnum, voteEnum);
    }

}
