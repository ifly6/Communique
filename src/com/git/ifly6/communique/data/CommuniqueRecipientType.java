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

import com.git.ifly6.CommuniqueSplitter;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSNation;
import com.git.ifly6.nsapi.NSRegion;
import com.git.ifly6.nsapi.NSWorld;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommNationCache;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommActiveMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommApprovalMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommMovementMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommNewNationMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.updaters.CommVoteMonitor;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Defines a number of recipient types and provides methods to decompose those types {@link CommuniqueRecipient}s. The
 * <b>order</b> of the {@code enum}s matters because the code is parsed in their "natural" order.
 * <p>
 * Tags prefixed with {@code _} (such as {@code _movement} are stateful. They create caches. The <i>changes</i> in those
 * caches are then what defines the recipient lists that are created. When a tag with that prefix is used, it is only
 * meaningful, though it may work, when Communiqué has {@code repeat} as true.
 * </p>
 * @author ifly6
 * @since version 7
 */
public enum CommuniqueRecipientType {

    /**
     * Declares the recipient is a nation and requires no further processing in decomposition.
     * @since version 7 (2016-12-16)
     */
    NATION {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            return Collections.singletonList(cr); // return singleton list
        }
    },

    // This code block must be before the REGION code block otherwise it will get substring matched over :(
    /**
     * Declares that the recipient is a REGION TAG and that it needs decomposing into a list of regions which then is
     * decomposed into the nations therein.
     * @since version 11 (2020-04-04)
     */
    REGION_TAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            try {
                List<String> regions = NSWorld.getRegionTag(cr.getName());
                return regions.stream()
                        .map(s -> new CommuniqueRecipient(cr.getFilterType(), CommuniqueRecipientType.REGION, s))
                        .map(CommuniqueRecipient::decompose)
                        .flatMap(List::stream)
                        .collect(Collectors.toList());

            } catch (IOException e) {
                throw new NSIOException("Could not get nations by tag!", e);
            }
        }
    },

    /**
     * Declares the recipient is a region, allowing for decomposition into a list of {@link CommuniqueRecipient} nations
     * in the region.
     * @since version 7 (2016-12-16)
     */
    REGION {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            NSRegion region = new NSRegion(cr.getName());
            return newRecipients(region.getRegionMembers(), cr.getFilterType());
        }
    },

    /**
     * Declares the recipient is one of various tags, which can be used to get the members of the World
     * Assembly, delegates thereof, or new nations. These tags should be consistent with the standard NationStates
     * telegram tags:
     * <ul>
     *     <li><strike>{@code tag:all}: all nations</strike> NOT SUPPORTED</li>
     *     <li>{@code tag:wa}: all WA nations</li>
     *     <li>{@code tag:delegates}: all WA delegates</li>
     *     <li>
     *         {@code tag:new}: new nations (but see the
     *         <a href="https://www.nationstates.net/cgi-bin/api.cgi?q=newnations">
     *             raw call</a>)
     *     </li>
     * </ul>
     * @since version 7 (2016-12-16)
     */
    TAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String tag = cr.getName();
            try {
                if (tag.equals("wa"))
                    return newRecipients(NSWorld.getWAMembers(), cr.getFilterType());
                if (tag.equals("delegates"))
                    return newRecipients(
                            CommDelegatesCache.getInstance().getDelegatesNow(),
                            cr.getFilterType());
                if (tag.equals("new"))
                    return newRecipients(CommNewNationMonitor.getInstance().getRecipients(),
                            cr.getFilterType());

                throw newException(cr);
            } catch (IOException e) {
                throw new NSIOException(String.format("Failed to decompose tag %s!", cr), e);
            }
        }
    },

    /**
     * Lists endorsers of a given nation. {@code endorsers_of:imperium_anglorum}, eg, lists all nations which have
     * endorsed the nation {@code imperium_anglorum}. The contrary list, WA members not endorsing
     * {@code imperium_anglorum}, is more complex. It would instead be something like
     * {@code region :europe // -endorses_of:imperium_anglorum // +tag:wa}
     * @since version 13
     */
    ENDORSERS_OF {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            NSNation nation = CommNationCache.getInstance().lookupNationNow(cr.getName());
            return newRecipients(nation.getEndoList(), cr.getFilterType());
        }
    },

//    /**
//     * Declares that the recipient is an internal Communiqué flag.
//     * @since version 7 (2016-12-16)
//     */
    /*
    FLAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String tag = cr.getName();
            if (tag.equals("recruit"))
                return Collections.emptyList(); // recruit is handled by Communiqué logic, not here
            if (tag.equals("active")) return HappeningsParser.getActiveNations();  // active
            return Collections.emptyList();
        }
    },
    */

    /**
     * Yields filters related to the NationStates Happenings. Valid input includes the following.
     * <ul>
     *     <li>{@code _happenings:active} (see {@link CommActiveMonitor}) returning nations active in the last
     *     10 minutes.</li>
     * </ul>
     * @see #stateful()
     * @since version 13 (2020-12-24 in "Communique 3"), replacing {@code flag:active} in version 9
     */
    _HAPPENINGS {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String tag = cr.getName();
            if (tag.equals("active")) {// active nations
                CommMonitor activeMonitor = CommActiveMonitor.getInstance();
                return newRecipients(activeMonitor.getRecipients(),
                        cr.getFilterType());
            }
            throw newException(cr);
        }
    },

    /**
     * Nations moving into or out of a list of regions. Eg, {@code _movement:into;europe,the_north_pacific} or
     * {@code _movement:out_of;europe}.
     * @see #stateful()
     * @see CommMovementMonitor.Direction
     * @since version 13 (2020-12-24 in "Communique 3")
     */
    _MOVEMENT {
        private final CommuniqueSplitter splitter = new CommuniqueSplitter(this.toString(), 2);

        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String[] tags = splitter.split(cr.getName());
            String direction = tags[0];
            String region = tags[1];
            CommMonitor monitor = CommMovementMonitor.getInstance(direction, region);
            return newRecipients(monitor.getRecipients(), cr.getFilterType());
        }
    },

    /**
     * Delegates approving a proposal. Eg {@code _approvals:given_to; PROPOSAL_ID} or
     * {@code _approvals:removed_from; PROPOSAL_ID}.
     * @see CommApprovalMonitor.Action
     * @see #stateful()
     * @since version 13 (2020-12-24 in "Communique 3")
     */
    _APPROVALS {
        private final CommuniqueSplitter splitter = new CommuniqueSplitter(this.toString(), 2);

        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String[] tags = splitter.split(cr.getName());
            String givenOrRemoved = tags[0];
            String proposal = tags[1];
            CommMonitor monitor = CommApprovalMonitor.getInstance(givenOrRemoved, proposal);
            return newRecipients(monitor.getRecipients(), cr.getFilterType());
        }
    },

    /**
     * Persons voting on a proposal. Eg {@code _voting:ga; for}.
     * @see #stateful()
     * @since version 13 (2020-12-24 in "Communique 3")
     */
    _VOTING {
        private final CommuniqueSplitter splitter = new CommuniqueSplitter(this.toString(), 2);

        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String[] split = splitter.split(cr.getName());
            String chamber = split[0];
            String voting = split[1];
            CommMonitor monitor = CommVoteMonitor.getInstance(chamber, voting);
            return newRecipients(monitor.getRecipients(), cr.getFilterType());
        }
    },

    /**
     * Declares that the recipient has no recipient type. This element must be parsed last.
     * @since version 7
     */
    NONE {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return "";
        }
    };

    private static final Logger LOGGER = Logger.getLogger(CommuniqueRecipientType.class.getName());

    /**
     * Recipient type prefixes should be compatible with the NationStates telegram system.
     * @since version 7
     */
    @Override
    public String toString() {
        return this.name().toLowerCase();
    }

    /**
     * Returns true if the recipient type is stateful: ie it generates <i>meaningful</i> recipients only over time or
     * when the recipients of previous calls become obsolete. This includes tags such as {@code _movement}. All stateful
     * tags begin with an underscore.
     * <ul>
     *     <li>{@code _happenings}</li> generates recipients that become irrelevant rapidly
     *     <li>{@code _movement}</li> cannot generate recipients without waiting
     *     <li>{@code _approval}</li> exhausts when the proposal expires
     *     <li>{@code _voting}</li> exhausts when the resolution at vote expires
     * </ul>
     * @return true if tag begins with {@code _}.
     */
    public boolean stateful() {
        return toString().startsWith("_");
    }

    /**
     * Decomposes tag into {@code List<{@link CommuniqueRecipient}>}.
     * @param cr tag to be decomposed
     * @return list of recipients
     * @since version 7
     */
    public abstract List<CommuniqueRecipient> decompose(CommuniqueRecipient cr);

    /**
     * Translates nation reference names into {@link CommuniqueRecipientType#NATION} {@code CommuniqueRecipient}s.
     * @param list       of nation reference names
     * @param filterType from which to extract filter type data
     * @return list of recipients
     * @since version 7
     */
    private static List<CommuniqueRecipient> newRecipients(List<String> list, CommuniqueFilterType filterType) {
        // we use this a lot, probably better to use a loop for speed
        List<CommuniqueRecipient> result = new ArrayList<>(list.size());
        for (String s : list)
            result.add(CommuniqueRecipients.createNation(filterType, s));
        return result;
    }

    /**
     * Creates illegal argument exception with preformatted string.
     * @since version 13 (2020-12-24 in "Communique 3")
     */
    private static IllegalArgumentException newException(CommuniqueRecipient s) {
        return new IllegalArgumentException(String.format("Invalid tag string \"%s\"", s));
    }
}
