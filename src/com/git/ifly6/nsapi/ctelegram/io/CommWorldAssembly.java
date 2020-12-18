/*
 * Copyright (c) 2020 ifly6
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
import com.jcabi.xml.XML;
import com.jcabi.xml.XMLDocument;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Gets information about the World Assembly.
 * @since version 3.0 (build 13)
 */
public class CommWorldAssembly {

    private CommWorldAssembly() {
    }

    /** Formats URL to get information on the resolution. */
    private static String formatResolutionURL(Chamber c) {
        return NSConnection.API_PREFIX
                + MessageFormat.format("wa={0}&q=resolution", c.getCouncilCode());
    }

    /** Formats URL for NS chamber vote; nation. */
    private static String formatNationsURL(Chamber c, Vote v) {
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

    /** Gets resolution ID for proposal in chamber. */
    public static String getProposalID(Chamber chamber) {
        try {
            NSConnection apiConnect = new NSConnection(formatResolutionURL(chamber));
            XML xml = new XMLDocument(apiConnect.getResponse());
            return xml.xpath("/WA/RESOLUTION/ID/text()").get(0);

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);
        }
    }

    /**
     * Gets voters in a chamber who are voting a certain way
     * @param chamber to look in
     * @param voting  direction to look for
     * @return voters who are voting in specified chamber with specified vote
     */
    public static List<String> getVoters(Chamber chamber, Vote voting) {
        try {
            NSConnection apiConnect = new NSConnection(formatNationsURL(chamber, voting));
            XML xml = new XMLDocument(apiConnect.getResponse());
            List<String> voters = xml.xpath(
                    MessageFormat.format("/WA/RESOLUTION/{0}/N/text()", // load all these values
                            voting.getNationXMLTag())); // get elements voting this direction
            return ApiUtils.ref(voters); // ref

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);
        }
    }

    /**
     * Gets delegates voting in specified chamber with certain direction. Results are sorted: delegates by vote weight,
     * descending; ties broken by name.
     * @param chamber to look in
     * @param voting  position
     * @return names of delegates
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
                            .reversed()                                            // make descending
                            .thenComparing(Delegate::getName))                       // break ties by name
                    .collect(Collectors.toList());

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);
        }
    }

    /**
     * Gets people who approved the specified proposal.
     * <p>This takes two API calls to query for each chamber.</p>
     * @throws NoSuchProposalException if proposal does not exist
     */
    public static List<String> getApprovers(String proposalID) {
        try {
            for (Chamber c : Chamber.values()) {
                XML xml = new XMLDocument(new NSConnection(formatProposalURL(c)).getResponse());
                for (XML xmlNode : xml.nodes("/WA/PROPOSALS/PROPOSAL")) {
                    String thisProposalID = xmlNode.node().getAttributes().getNamedItem("id").getTextContent();
                    if (ApiUtils.ref(proposalID).equals(ApiUtils.ref(thisProposalID))) // have correct proposal
                        return ApiUtils.ref(
                                Arrays.asList(
                                        xmlNode.nodes("APPROVALS").get(0).node()
                                                .getTextContent().split(":")
                                )
                        );
                }
            }

            throw new NoSuchProposalException(String.format("Proposal %s does not exist", proposalID));

        } catch (IOException e) {
            throw new NSIOException("Could not connect to NationStates API", e);
        }
    }

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
        FOR {
            @Override
            public String getNationXMLTag() {
                return "VOTES_FOR";
            }

            @Override
            public String getDelegateXMLTag() {
                return "DELVOTES_FOR";
            }
        }, AGAINST {
            @Override
            public String getNationXMLTag() {
                return "VOTES_AGAINST";
            }

            @Override
            public String getDelegateXMLTag() {
                return "DELVOTES_AGAINST";
            }
        };

        public abstract String getNationXMLTag();

        public abstract String getDelegateXMLTag();
    }

    /** Enumerates World Assembly chambes. */
    public enum Chamber {
        GA {
            @Override
            public int getCouncilCode() {
                return 1;
            }

            @Override
            public String properName() {
                return "General Assembly";
            }
        }, SC {
            @Override
            public int getCouncilCode() {
                return 2;
            }

            @Override
            public String properName() {
                return "Security Council";
            }
        };

        public abstract int getCouncilCode();

        public abstract String properName();
    }

    /** Thrown if the proposal queried does not exist */
    public static class NoSuchProposalException extends NoSuchElementException {
        public NoSuchProposalException(String s) {
            super(s);
        }
    }
}
