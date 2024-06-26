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

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

/**
 * Stores information about a recipient. It is based on three characteristics, a <code>FilterType</code>, a
 * <code>RecipientType</code>, and the name. The filter type can be used to exclude, include, or simply add. The
 * recipient type can be used to specify multiple recipients, like in a region or in the set of World Assembly
 * delegates. All <code>CommuniqueRecipient</code>s have names which are reference-name safe.
 * @author ifly6
 * @since version 7
 */
public class CommuniqueRecipient {

    public static final CommuniqueRecipient DELEGATES =
            new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.TAG, "delegates");
    public static final CommuniqueRecipient WA_MEMBERS =
            new CommuniqueRecipient(CommuniqueFilterType.NORMAL, CommuniqueRecipientType.TAG, "wa");

    private final CommuniqueFilterType filterType;
    private final CommuniqueRecipientType recipientType;
    private final String name;

    /**
     * Creates a <code>CommuniqueRecipient</code> with certain characteristics.
     */
    public CommuniqueRecipient(CommuniqueFilterType filterType, CommuniqueRecipientType recipientType, String name) {
        this.filterType = filterType;
        this.recipientType = recipientType;
        this.name = ApiUtils.ref(name);    // convert to reference name

        // some format checking for the name
        if (name.contains(":"))
            throw new IllegalArgumentException(String.format("nation name <%s> is invalid", name));
    }

    /**
     * Returns the name, which, for all elements, will be the reference name format.
     * @return the specific thing which is being requested
     */
    public String getName() {
        return name;
    }

    /**
     * Returns the type of the filter or token, defined in {@link CommuniqueFilterType FilterType}.
     * @return the type of filter or token
     */
    public CommuniqueFilterType getFilterType() {
        return filterType;
    }

    /**
     * Returns the type of the recipient, defined in {@link CommuniqueRecipientType RecipientType}.
     * @return the type of recipient
     */
    public CommuniqueRecipientType getRecipientType() {
        return recipientType;
    }

    /**
     * Returns a string representation of the recipient, in the same form which is used by the NationStates telegram
     * system to specify large numbers of nations. For example, <code>tag:wa</code> or
     * <code>nation:imperium_anglorum</code>.
     */
    @Override
    public String toString() {
        return filterType.toString() + recipientType.toString() + ":" + this.getName();
    }

    /**
     * Decomposes a tag to its constituent nations. All decompositions are done in {@link CommuniqueRecipientType}
     * class.
     * @return a list of <code>CommuniqueRecipient</code>s
     */
    public List<CommuniqueRecipient> decompose() throws JTelegramException {
        return getRecipientType().decompose(this);
    }

    /**
     * Parses a <code>CommuniqueRecipient</code> of the same form defined in the
     * {@link com.git.ifly6.communique.data.CommuniqueRecipient#toString toString()} method. Allows for fast and simple
     * access between <code>String</code> representations of a recipient and the computer's conception of the object.
     * <p>
     * If a reference name is provided without an accompanying recipient-type declaration, in the form
     * <code>imperium_anglorum</code>, it is assumed that this is a <code>FilterType.NORMAL</code> nation with that
     * name.
     * </p>
     * @return a <code>CommuniqueRecipient</code> representing that string
     */
    public static CommuniqueRecipient parseRecipient(final String raw) {
        // 2020-12-24 do not put a toLowerCase here: it breaks case-sensitive regex raw!
        String s = raw.trim();

        CommuniqueFilterType fType = CommuniqueFilterType.NORMAL; // default
        for (CommuniqueFilterType type : CommuniqueFilterType.values())
            if (s.startsWith(type.toString())) {
                fType = type;
                s = s.substring(type.toString().length());
                break;
            }

        CommuniqueRecipientType rType = CommuniqueRecipientType.NATION; // default
        for (CommuniqueRecipientType type : CommuniqueRecipientType.values())
            if (s.startsWith(type.toString())) {
                rType = type;
                s = s.substring(type.toString().length());
                break;
            }

        // 2020-12-24 insert override for RecipientType.NONE -> NATION if FilterType.NORMAL
        // this is to correctly parse something like `imperium_anglorum` without tags
        if (fType == CommuniqueFilterType.NORMAL && rType == CommuniqueRecipientType.NONE)
            rType = CommuniqueRecipientType.NATION;

        if (s.contains(":")) { // ie is an prefix to be looking at!
            String expectedPrefix = fType.toString() + rType.toString();
            String actualPrefix = raw.substring(0, raw.indexOf(":"));
            if (!expectedPrefix.equalsIgnoreCase(actualPrefix))
                throw new IllegalArgumentException(String.format("Expected prefix %s, got prefix %s; parse failed!",
                        expectedPrefix, actualPrefix));
        }

        // 2017-03-30 use lastIndexOf to deal with strange name changes, can cause error in name `+region:euro:pe`
        return new CommuniqueRecipient(fType, rType, s.substring(s.lastIndexOf(":") + 1));
    }

    /**
     * Parses recipients.
     * @param c collection to parse
     * @return list of parsed recipients
     * @see #parseRecipient(String)
     */
    public static List<CommuniqueRecipient> parseRecipients(final Collection<String> c) {
        List<CommuniqueRecipient> parsed = new ArrayList<>(c.size());
        for (final String e : c)
            parsed.add(parseRecipient(e));
        return parsed;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CommuniqueRecipient that = (CommuniqueRecipient) o;
        return filterType == that.filterType && recipientType == that.recipientType
                && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(filterType, recipientType, name);
    }

    /** Recruiter flag prior to version 7 */
    private static final String OLD_RECRUIT_FLAG = "flag:recruit";

    /**
     * The old include flag, which served the purpose of something like the current {@code +} tag, eg
     * {@code region:Europe, +tag:wa} was a two-part flag on one line as {@code region:Europe -> wa:all}.
     * @see CommuniqueRecipient#translateTokens(List)
     */
    private static final String OLD_INCLUDE = "->";

    /**
     * The old exclude flag was badly designed. If used simply, ie {@code --}  it would fail to work with nations that
     * have prefixed hyphens in their names. Instead, here, we use the two hyphens with spaces on both sides, which only
     * partially solves the problem because spaces were allowed in names too. This is only done as a means to lower the
     * number of false positives.
     * @see CommuniqueRecipient#translateTokens(List)
     */
    private static final String OLD_EXCLUDE = " -- "; // must include whitespace on both sides

    /**
     * Translates a number of old tokens into the new Communique 7 tokens.
     * <p>This should translate tokens like the following:</p>
     * <table>
     * <tr>
     * <th>Old tag</th>
     * <th>New tag</th>
     * </tr>
     * <tr>
     * <td>{@code region:Europe}</td>
     * <td>{@code region:Europe}</td>
     * </tr>
     * <tr>
     * <td>{@code wa:all}</td>
     * <td>{@code tag:wa}</td>
     * </tr>
     * <tr>
     * <td>{@code wa:delegates}</td>
     * <td>{@code tag:delegates}</td>
     * </tr>
     * <tr>
     * <td>{@code region:Europe -> wa:all}</td>
     * <td>{@code region:Europe, +tag:WA}</td>
     * </tr>
     * <tr>
     * <td>{@code region:Europe -- nation:imperium_anglorum}</td>
     * <td>{@code region:Europe, -nation:imperium_anglorum}</td>
     * </tr>
     * </table>
     * @param oldTokens to translate
     * @return a list of tokens which means the same thing in the new system
     * @see Communique7Parser
     */
    public static List<String> translateTokens(List<String> oldTokens) {
        List<String> tokens = new ArrayList<>();
        for (String oldToken : oldTokens) {

            if (oldToken.startsWith(OLD_RECRUIT_FLAG)) {
                tokens.add(OLD_RECRUIT_FLAG);
                if (oldToken.trim().equalsIgnoreCase(OLD_RECRUIT_FLAG)) {
                    // it's a recruit flag with nothing else
                    continue; // next

                } else {
                    // otherwise, there's some other flag buried in here, we need to find it
                    // `flag:recruit` already added, remove it and continue parsing
                    oldToken = oldToken.substring(OLD_RECRUIT_FLAG.length()).trim();
                }
            }

            // keep parsing
            if (oldToken.contains(OLD_INCLUDE)) {
                String[] split = oldToken.split(OLD_INCLUDE);
                tokens.add(translateToken(split[0]));
                tokens.add(translateToken(OLD_INCLUDE + split[1]));
                continue; // to next!
            }

            if (oldToken.contains(OLD_EXCLUDE)) {
                String[] split = oldToken.split(OLD_EXCLUDE);
                if (split.length == 2) {
                    if (ApiUtils.isNotEmpty(split[0]))
                        tokens.add(translateToken(split[0].trim()));
                    if (ApiUtils.isNotEmpty(split[1]))
                        tokens.add(translateToken(OLD_EXCLUDE + split[1])); // must trim!
                    continue; // to next!
                }
            }
            tokens.add(translateToken(oldToken));

        }
        return ApiUtils.ref(tokens);
    }

    /**
     * Translates a single token from the old system to the new Communique 7 system. This method should not change any
     * Communique 7 tokens and only translate applicable Communique 6 tokens.
     * @param token in a <code>String</code> form, like "wa:delegates"
     * @return the token in the Communique 7 form, which, for "wa:delegates", would turn into "tag:delegates"
     */
    private static String translateToken(final String token) {
        String oldToken = token.trim();

        // deal with mixed new and old tokens
        if (oldToken.startsWith("tag")) return oldToken;

        // logic tags, somewhat recursive to ease translation of sub-tokens
        // no need to use HashMap, that seems over-engineered for something this simple
        if (oldToken.startsWith("/")) return "-" + translateToken(oldToken.replaceFirst("/", "").trim());
        if (oldToken.startsWith("-- ") || oldToken.startsWith(" -- "))
            return "-" + translateToken(oldToken.replaceFirst("-- ", "").trim());
        if (oldToken.startsWith(OLD_INCLUDE) || oldToken.startsWith(OLD_INCLUDE + " "))
            return "+" + translateToken(oldToken.replaceFirst(OLD_INCLUDE, "").trim());

        // translate tags which can be decomposed
        if (oldToken.trim().equalsIgnoreCase("wa:delegates")) return "tag:delegates";
        if (oldToken.trim().equalsIgnoreCase("wa:delegate")) return "tag:delegates";
        if (oldToken.trim().equalsIgnoreCase("wa:members")
                || oldToken.trim().equalsIgnoreCase("wa:nations")
                || oldToken.trim().equalsIgnoreCase("wa:all"))
            return "tag:wa";
        if (oldToken.startsWith("world:new")) return "tag:new";
        if (oldToken.startsWith("world:all")) return "tag:all";

        // somewhat-direct recipient tags, like region and nation
        if (oldToken.startsWith("region:")) return oldToken;
        return "nation:" + oldToken;

    }

}
