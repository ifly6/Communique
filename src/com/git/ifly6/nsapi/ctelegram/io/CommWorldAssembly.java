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

package com.git.ifly6.nsapi.ctelegram.io;

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSTimeStamped;
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.text.MessageFormat;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Gets information about the World Assembly.
 * @since version 13
 */
public class CommWorldAssembly {

    private CommWorldAssembly() { }

    /** Formats URL to get information on the resolution. */
    private static String formatResolutionURL(Chamber c) {
        return NSConnection.API_PREFIX
                + MessageFormat.format("wa={0}&q=resolution", c.getCouncilCode());
    }

    /** Formats URL for NS chamber vote; nation. */
    private static String formatNationsURL(Chamber c) {
        return NSConnection.API_PREFIX
                + MessageFormat.format("wa={0}&q=resolution+voters", c.getCouncilCode());
    }

    /** Formats URL for NS chamber vote; delegates. */
    public static String formatDelegatesURL(Chamber c, Vote vote) {
        // https://www.nationstates.net/cgi-bin/api.cgi?wa=1&q=resolution+delvotes
        return NSConnection.API_PREFIX
                + MessageFormat.format("wa={0}&q=resolution+delvotes", c.getCouncilCode());
    }

    /** Formats URL for NS chamber vote; delegates. */
    public static String formatProposalURL(Chamber c) {
        // https://www.nationstates.net/cgi-bin/api.cgi?wa=1&q=proposals
        return NSConnection.API_PREFIX
                + MessageFormat.format("wa={0}&q=proposals", c.getCouncilCode());
    }

    /**
     * Gets resolution ID for proposal in chamber.
     * @throws NoSuchProposalException if no proposal at vote in chamber
     */
    public static String getResolutionID(Chamber chamber) {
        try {
            NSConnection apiConnect = new NSConnection(formatResolutionURL(chamber));
            XML xml = new XMLDocument(apiConnect.getResponse());
            return xml.xpath("/WA/RESOLUTION/ID/text()").get(0);

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);

        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchProposalException(String.format("No resolution at vote in chamber %s", chamber));
        }
    }

    /**
     * Gets voters in a chamber who are voting a certain way. The API returns <b>all</b> voters; including delegates.
     * Delegates are included <b>without</b> their voting weights.
     * @param chamber to look in
     * @return voters who are voting in specified chamber with specified vote
     * @throws NoSuchProposalException if chamber is empty
     */
    public static BothChamberVoters getVoters(Chamber chamber) {
        try {
            NSConnection apiConnect = new NSConnection(formatNationsURL(chamber));
            XML xml = new XMLDocument(apiConnect.getResponse());
            return new BothChamberVoters(
                    xml.xpath("/WA/RESOLUTION/ID/text()").get(0),
                    xml.xpath(MessageFormat.format("/WA/RESOLUTION/{0}/N/text()", Vote.FOR.getNationXMLTag())),
                    xml.xpath(MessageFormat.format("/WA/RESOLUTION/{0}/N/text()",
                            Vote.AGAINST.getNationXMLTag()))
            );

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);

        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchProposalException(String.format("No proposal at vote in chamber %s", chamber));
        }
    }

    /**
     * Gets delegates voting in specified chamber with certain direction. Results are sorted: delegates by vote weight,
     * descending; ties broken by name.
     * @param chamber to look in
     * @param voting  position
     * @return list of {@link Delegate}s
     * @throws NoSuchProposalException if no proposal at vote
     */
    public static List<Delegate> getDelegates(Chamber chamber, Vote voting) {
        try {
            NSConnection apiConnect = new NSConnection(formatDelegatesURL(chamber, voting));
            XML xml = new XMLDocument(apiConnect.getResponse());
            List<String> delegates = xml.xpath(
                    MessageFormat.format("/WA/RESOLUTION/{0}/DELEGATE/NATION/text()", // load all these values
                            voting.getDelegateXMLTag())); // get elements voting this direction

            List<String> votingWeights = xml.xpath(
                    MessageFormat.format("/WA/RESOLUTION/{0}/DELEGATE/VOTES/text()", // load all these values
                            voting.getDelegateXMLTag())); // get elements voting this direction

            if (delegates.size() != votingWeights.size())
                throw new UnsupportedOperationException("Error in NS API; every delegate must have a voting weight!");

            return IntStream.range(0, delegates.size()).boxed() // enumerate values
                    .map(i -> new Delegate(delegates.get(i), Integer.parseInt(votingWeights.get(i)))) // zip values
                    .sorted(Comparator.comparing(Delegate::getWeight) // compare by weight
                            .reversed()                               // make descending
                            .thenComparing(Delegate::getName))        // break ties by name
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);

        } catch (IndexOutOfBoundsException e) {
            throw new NoSuchProposalException(String.format("No proposal at vote in chamber %s", chamber));
        }
    }

    /**
     * Gets people who approved the specified proposal. It first enumerates all proposals in both chambers. Then it
     * yields the proposal specified. The API does not permit querying only one proposal; and to get any
     * possible proposal we need to check both chambers.
     * @return list of delegate approvers
     * @throws NoSuchProposalException if proposal does not exist
     */
    public static List<String> getApprovers(String proposalID) {
        final List<Proposal> proposals = getAllProposals();
        for (Proposal p : proposals)
            if (p.id.equalsIgnoreCase(proposalID))
                return p.approvers;

        throw new NoSuchProposalException(String.format("Proposal %s does not exist", proposalID));
    }

    /**
     * Queries for current approvals on all proposals. This must make two calls for both chambers because
     * {@code _approvals} is agnostic as to chamber and could be in either.
     * @returns list of {@link Proposal}s
     */
    public static List<Proposal> getAllProposals() {
        try {
            List<Proposal> proposals = new ArrayList<>();
            for (Chamber c : Chamber.values()) {
                XML xml = new XMLDocument(new NSConnection(formatProposalURL(c)).getResponse());
                for (XML xmlNode : xml.nodes("/WA/PROPOSALS/PROPOSAL")) {
                    String thisProposalID = xmlNode.node().getAttributes().getNamedItem("id").getTextContent();
                    List<String> approvers = ApiUtils.ref(Arrays.asList(
                            xmlNode.nodes("APPROVALS").get(0).node().getTextContent().split(":")));
                    proposals.add(new Proposal(thisProposalID, approvers));
                }
            }
            return proposals;

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);
        }
    }

    /** Record for delegate names and weights. */
    public static class Delegate {
        public final String name;
        public final Integer weight;

        public Delegate(String name, Integer weight) {
            this.name = ApiUtils.ref(name);
            this.weight = weight;
        }

        public String getName() {
            return name;
        }

        public Integer getWeight() {
            return weight;
        }
    }

    /** Enumerates voting positions. */
    public enum Vote {
        FOR("VOTES_FOR"),
        AGAINST("VOTES_AGAINST");

        private String nationXMLTag;

        Vote(String nationXMLTag) {
            this.nationXMLTag = nationXMLTag;
        }

        public String getNationXMLTag() {
            return this.nationXMLTag;
        }

        public String getDelegateXMLTag() {
            return "DEL" + getNationXMLTag();
        }
    }

    /** Enumerates World Assembly chambers. */
    public enum Chamber {
        UN(0, "NS United Nations"),
        GA(1, "General Assembly"),
        SC(2, "Security Council");

        private final int councilCode;
        private final String properName;

        Chamber(int council_code, String properName) {
            councilCode = council_code;
            this.properName = properName;
        }

        public int getCouncilCode() {
            return this.councilCode;
        }

        public String properName() {
            return this.properName;
        }
    }

    /** Thrown if the proposal queried does not exist */
    public static class NoSuchProposalException extends NoSuchElementException {
        public NoSuchProposalException(String s) {
            super(s);
        }
    }

    /** Holds information on a proposal. */
    public static class Proposal implements NSTimeStamped {

        public final String id;
        public final List<String> approvers;
        public final Instant timeStamp;

        private Proposal(String id, List<String> approvers) {
            this.id = id;
            this.approvers = approvers;
            this.timeStamp = Instant.now();
        }

        @Override
        public Instant timestamp() { return timeStamp; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof Proposal)) return false;
            Proposal proposal = (Proposal) o;
            return Objects.equals(id, proposal.id) && Objects.equals(timeStamp, proposal.timeStamp);
        }

        @Override
        public int hashCode() {
            return Objects.hash(id, timeStamp);
        }
    }

    public static class BothChamberVoters implements NSTimeStamped {

        private Instant timestamp;

        public final String proposalID;
        public final List<String> votersFor;
        public final List<String> votersAgainst;

        public BothChamberVoters(String proposalID, List<String> votersFor, List<String> votersAgainst) {
            this.proposalID = proposalID;
            this.votersFor = votersFor;
            this.votersAgainst = votersAgainst;
            this.timestamp = Instant.now();
        }

        public List<String> getVoters(Vote vote) {
            if (vote == Vote.FOR) return votersFor;
            if (vote == Vote.AGAINST) return votersAgainst;
            throw new UnsupportedOperationException(
                    String.format("Asked to get voters of unsupported position %s", vote));
        }

        @Override
        public Instant timestamp() { return timestamp; }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (!(o instanceof BothChamberVoters)) return false;
            BothChamberVoters voters = (BothChamberVoters) o;
            return Objects.equals(timestamp, voters.timestamp) && Objects.equals(proposalID,
                    voters.proposalID);
        }

        @Override
        public int hashCode() {
            return Objects.hash(timestamp, proposalID);
        }
    }

}
