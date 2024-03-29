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

import com.git.ifly6.communique.io.HappeningsParser;
import com.git.ifly6.nsapi.telegram.JTelegramException;
import com.git.ifly6.nsapi.telegram.util.JInfoFetcher;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * Defines a number of recipient types and provides methods to decompose those types into lists of
 * <code>CommuniqueRecipient</code>.
 * @author ifly6
 */
public enum RecipientType {

    /**
     * Declares the recipient is a nation and requires no further processing in decomposition.
     */
    NATION {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            // return singleton list
            return Collections.singletonList(cr);
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    },

    // This code block must be before the REGION code block otherwise it will get substring matched over :(
    /**
     * Declares that the recipient is a REGION TAG and that it needs decomposing into a list of regions which then is
     * decomposed into the nations therein.
     */
    REGION_TAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
            // new 2020-04-04
            List<String> regions = JInfoFetcher.instance().getRegionTag(cr.getName());
            LOGGER.info(String.format("Tag %s: %d regions", cr.getName(), regions.size()));
            return regions.stream()
                    .map(s -> new CommuniqueRecipient(cr.getFilterType(), RecipientType.REGION, s))
                    .map(CommuniqueRecipient::decompose)
                    .flatMap(List::stream)
                    .collect(Collectors.toList());
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    },

    /**
     * Declares the recipient is a region, allowing for decomposition into a list of {@link CommuniqueRecipient} nations
     * in the region.
     */
    REGION {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
            List<String> regionMembers = JInfoFetcher.instance().getRegion(cr.getName());
            LOGGER.info(String.format("Region %s: %d nations", cr.getName(), regionMembers.size()));
            return newRecipients(regionMembers, cr.getFilterType());
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    },

    /**
     * Declares the recipient is one of various tags, which can be used to get the members of the World Assembly,
     * delegates thereof, or new nations.
     */
    TAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) throws JTelegramException {
            String tag = cr.getName();
            if (tag.equals("wa")) return newRecipients(JInfoFetcher.instance().getWAMembers(), cr.getFilterType());
            if (tag.equals("delegates"))
                return newRecipients(JInfoFetcher.instance().getDelegates(), cr.getFilterType());
            if (tag.equals("new")) return newRecipients(JInfoFetcher.instance().getNew(), cr.getFilterType());
            if (tag.equals("all")) return newRecipients(JInfoFetcher.instance().getAll(), cr.getFilterType());
            throw new JTelegramException("Invalid flag: \"" + cr.toString() + "\"");
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    },

    /**
     * Declares that the recipient is an internal Commmunique flag.
     */
    FLAG {
        @Override
        public List<CommuniqueRecipient> decompose(CommuniqueRecipient cr) {
            String tag = cr.getName();
            if (tag.equals("recruit"))
                return Collections.emptyList(); // recruit is handled by Communique logic, not here
            if (tag.equals("repeat")) return Collections.emptyList(); // repeat last pull and continue
            if (tag.equals("active")) return HappeningsParser.getActiveNations();  // active
            return Collections.emptyList();
        }

        @Override
        public String toString() {
            return this.name().toLowerCase();
        }
    };

    private static final Logger LOGGER = Logger.getLogger(RecipientType.class.getName());

    /**
     * Allows for the recipient type to be compatible with the NationStates telegram system by providing the same tag
     * nomenclature.
     */
    @Override
    public abstract String toString();

    /**
     * Decomposes a tag into a list of <code>CommuniqueRecipient</code> which can then be more easily used.
     * @param cr to be decomposed
     * @return a list of <code>CommuniqueRecipient</code>
     */
    public abstract List<CommuniqueRecipient> decompose(CommuniqueRecipient cr);

    /**
     * Translates a list of nation reference names into a list of valid <code>CommuniqueRecipient</code>s.
     * @param list       of nation reference names
     * @param filterType from which to extract type data
     * @return list of CommuniqueRecipients
     */
    private static List<CommuniqueRecipient> newRecipients(List<String> list, FilterType filterType) {
        // we use this a lot, probably better to use a loop for speed
        List<CommuniqueRecipient> result = new ArrayList<>();
        for (String s : list)
            result.add(CommuniqueRecipients.createNation(filterType, s));
        return result;
    }
}
