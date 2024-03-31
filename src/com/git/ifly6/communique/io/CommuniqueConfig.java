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

package com.git.ifly6.communique.io;

import com.git.ifly6.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import javax.annotation.Nullable;
import javax.validation.constraints.NotNull;
import java.io.Serializable;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import static com.git.ifly6.CommuniqueUtilities.NORMAL_FORM;

/**
 * <code>CommuniqueConfig</code> creates a unified object for the storage and retrieval of configuration information
 * necessary to have persistent states between Communiqué or Marconi instances.
 */
@SuppressWarnings("DeprecatedIsStillUsed")
public class CommuniqueConfig implements Serializable {

    // NOTE! For backwards compatibility, do not change field names!
    private static final long serialVersionUID = Communique7Parser.VERSION;

    public String HEADER =
            "Communiqué Configuration file. Do not edit by hand. Produced at: " // cannot be static!
                    + CommuniqueUtilities.getTime(NORMAL_FORM) + ". Produced by version " + Communique7Parser.VERSION;
    public int version = Communique7Parser.VERSION;

    public JTelegramKeys keys;

    private JTelegramType telegramType;

    @Nullable
    private Duration telegramInterval;

    public CommuniqueProcessingAction processingAction;

    public boolean repeats;
    @Nullable
    private Duration repeatInterval;

    /**
     * Holds the Communique recipients in <code>String</code>s so that it can be edited by hand and not as
     * {@link CommuniqueRecipient}.
     * <p>To keep this editable by hand, the configuration system uses getters and setters to translate to and from the
     * string state representations to present to the programmer a {@link CommuniqueRecipient} API but actually store
     * everything in strings.</p>
     */
    private ArrayList<String> cRecipients; // must be mutable, use ArrayList

    // These should be deprecated, but are kept for backward compatibility
    @Deprecated
    public String[] recipients; // consider removing
    @Deprecated
    public String[] sentList;   // consider removing

    @Deprecated
    protected boolean isRecruitment;
    @Deprecated
    public String waitString;

    /**
     * Constructs with default parameters
     */
    public CommuniqueConfig() {
        this.keys = new JTelegramKeys();
        this.telegramType = JTelegramType.NONE;
        this.telegramInterval = null;
        this.processingAction = CommuniqueProcessingAction.NONE;
        this.repeats = false;
        this.repeatInterval = null;
    }

    /**
     * Constructs configuration file with given parameters. The recipients should be specified after the fact with
     * {@link this#setcRecipients(List)} or {@link this#addcRecipient(CommuniqueRecipient)}.
     * @param keys             identifying the telegram to send
     * @param telegramType     of telegram to send
     * @param telegramInterval between which to send telegrams (only set when telegram type is
     *                         {@link JTelegramType#CUSTOM}
     * @param processingAction to apply after parsing
     * @param repeats          flag whether the client should interrupt and repeat
     * @param repeatInterval   over which to interrupt and repeat (only set when {@code repeats} is true)
     */
    public CommuniqueConfig(
            JTelegramKeys keys, JTelegramType telegramType, Duration telegramInterval,
            CommuniqueProcessingAction processingAction,
            boolean repeats, Duration repeatInterval
    ) {
        this();
        this.keys = keys;
        this.telegramType = telegramType;
        this.telegramInterval = (this.telegramType == JTelegramType.CUSTOM) ? telegramInterval : null;
        this.processingAction = processingAction;
        this.repeats = repeats;
        this.repeatInterval = (this.repeats)
                ? (repeatInterval.compareTo(Duration.ofMinutes(3)) > 0 ? repeatInterval : Duration.ofMinutes(3))
                : null;
    }

    /**
     * Sets the default version to the version in {@link Communique7Parser}.
     * @return the version in <code>Communique7Parser</code>
     */
    public int defaultVersion() {
        this.version = Communique7Parser.VERSION;
        return Communique7Parser.VERSION;
    }

    /**
     * Returns converted <code>cRecipients</code> to <code>List&lt;CommuniqueRecipient&gt;</code>
     * @return <code>cRecipients</code> converted to <code>List&lt;CommuniqueRecipient&gt;</code>
     */
    public List<CommuniqueRecipient> getcRecipients() {
        if (cRecipients == null) return null; // deal with null case

        // use imperative for speed
        List<CommuniqueRecipient> list = new ArrayList<>(cRecipients.size());
        for (String s : cRecipients)
            list.add(CommuniqueRecipient.parseRecipient(s));
        return list;
    }

    /**
     * Returns raw <code>cRecipients</code>, which is <code>List&lt;String&gt;</code>
     * @return <code>cRecipients</code>
     */
    public List<String> getcRecipientsString() {
        return cRecipients;
    }

    /**
     * Sets {@link #cRecipients} with {@link List} of {@link CommuniqueRecipient}, automatically translates to
     * {@link String} on the fly.
     * @param crs {@link CommuniqueRecipient}s to set
     */
    public void setcRecipients(List<CommuniqueRecipient> crs) {
        // NOTE: No setcRecipients(List<String> crs) because need for verification
        // use imperative for speed
        ArrayList<String> list = new ArrayList<>(crs.size());
        for (CommuniqueRecipient cr : crs)
            list.add(cr.toString());
        cRecipients = list;
    }

    public void addcRecipient(CommuniqueRecipient cr) {
        cRecipients.add(cr.toString());
    }

    /**
     * Gets telegram type
     * @return telegram type, {@link JTelegramType#NONE} if null
     */
    public JTelegramType getTelegramType() {
        return telegramType == null ? JTelegramType.NONE : telegramType;
    }

    public void setTelegramType(JTelegramType type) {
        this.telegramType = Objects.requireNonNull(type);
    }

    /**
     * Gets processing action
     * @return processing action, {@link CommuniqueProcessingAction#NONE} if null
     */
    public CommuniqueProcessingAction getProcessingAction() {
        return processingAction == null ? CommuniqueProcessingAction.NONE : processingAction;
    }

    @NotNull
    public Duration getTelegramInterval() {
        if (telegramInterval == null) return this.getTelegramType().getWaitDuration();
        return telegramInterval;
    }

    public void setTelegramInterval(Duration interval) {
        this.telegramInterval = Objects.requireNonNull(interval);
    }

    @NotNull
    public Duration getRepeatInterval() {
        if (!repeats || repeatInterval == null) return ChronoUnit.FOREVER.getDuration();
        return repeatInterval;
    }

    /**
     * Checks all the data kept in {@link CommuniqueConfig#cRecipients} and makes they are distinct and applicable to
     * save to the program. For backward compatibility, it also applies these changes to the old {@code recipients} and
     * the {@code sentList}. It also updates the {@code CommuniqueConfig} version <i>field</i>, not the one in the
     * header, to the version of the program on which it was saved.
     */
    void clean() {
        version = this.defaultVersion(); // updates version

        if (Objects.nonNull(cRecipients) && !cRecipients.isEmpty()) // apparently cRecipients is nullable
            cRecipients = cRecipients.stream().distinct()
                    .map(CommuniqueConfig::cleanNation)
                    .collect(Collectors.toCollection(ArrayList::new));
        else
            cRecipients = new ArrayList<>();

        if (Objects.nonNull(recipients) && recipients.length > 0)
            recipients = Arrays.stream(recipients)
                    .distinct()
                    .map(CommuniqueConfig::cleanNation)
                    .toArray(String[]::new);

        if (Objects.nonNull(sentList) && sentList.length > 0)
            sentList = Arrays.stream(sentList)
                    .distinct()
                    .map(CommuniqueConfig::cleanNation)
                    .toArray(String[]::new);
    }

    /**
     * Cleans nation names that could have been prefixed accidentally in a previous version of Communique
     * @param recipientString is the string-name of the nation
     * @return the same with all the extra 'nation:'s removed.
     */
    private static String cleanNation(String recipientString) {
        return recipientString.replace(":(nation:)*", ":");
    }

}
