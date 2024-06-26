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

package com.git.ifly6.nsapi.telegram;

import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSException;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.NSNation;

import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <code>JavaTelegram</code> is the heart of the library. It coordinates the monotony of handling keys, recruitment
 * flags, recipients, and sending.
 * <p>
 * <code>JavaTelegram</code> operates through a few paths. It has a dedicated input and output stream. Since
 * implementations of output change from different implementations to another, it requires a {@link JTelegramLogger} to
 * operate with the provision of output. The system accepts input by using <code>set</code> methods, setting three
 * different fields, the keys (contained in a {@link JTelegramKeys}), the <code>boolean</code> recruitment flag, and the
 * recipients contained in a <code>String[]</code>.
 * </p>
 * <p>
 * The function, due to its use inside a thread, provides a system for shutting down all threads with the volatile
 * <code>boolean killThread</code>. This allows the <code>JavaTelegram</code> to be multi-threaded whilst also allowing
 * for a safe shutdown.
 * </p>
 * <p>
 * The entire class can be overridden if a programmer feels that it implements functions incorrectly. Most sections will
 * not operate much differently if overridden, since the output of information is nearly entirely in the purview of
 * <code>JTelegramLogger</code> and the raw connection system is in <code>JTelegramConnection</code>.
 * </p>
 */
@Deprecated
public class JavaTelegram {

    public static final Logger LOGGER = Logger.getLogger(JavaTelegram.class.getName());

    private static volatile boolean killThread = false;

    protected JTelegramKeys keys = new JTelegramKeys();

    private List<String> recipients = new ArrayList<>();
    private List<String> sentList = new ArrayList<>();

    private final JTelegramLogger util;

    private JTelegramType telegramType = JTelegramType.RECRUIT;   // Defaults to 'true' to keep on the safe side.
    private int waitTime = telegramType.getWaitTime();

    /**
     * A list of manualtests to run on each recipient. A <code>NSNation</code> is created for each recipient in
     * {@link JavaTelegram#connect()} and populated before it is tested by the predicate. If any predicate returns
     * false, the recipient will be skipped. A default predicate, which cannot be removed, is statically initialised to
     * prevent telegrams from being sent based on this algorithm:
     * <p>
     * <code>if we are recruiting and nation is not recruitable -> false
     * <br /> else (we are campaigning) and nation is not campaignable -> false</code>
     * </p>
     */
    private List<JTelegramPredicate> predicates = new ArrayList<>();  // additional predicates here

    {
        predicates.add(new JTelegramPredicate(
                "telegram type receivable", n -> {
            // if we are recruiting and nation is not recruitable -> false
            // if campaigning and nation is not campaignable -> false
            // otherwise return true
            if (telegramType == JTelegramType.RECRUIT) return n.isRecruitable();
            if (telegramType == JTelegramType.CAMPAIGN) return n.isCampaignable();
            return true;
        }));
    }

    /**
     * Creates a JavaTelegram function with a way of returning information and status reports as well as immediate
     * initialisation of the keys, and the immediate setting of the isRecruitment flag.
     * @param providedLogger is a <code>JTelegramLogger</code> which replaces the old logger for the output of
     *                       information
     * @param inputKeys      is a <code>JTelegramKeys</code> containing the keys to directly initialise
     * @param m              is the mode
     */
    public JavaTelegram(JTelegramLogger providedLogger, JTelegramKeys inputKeys, JTelegramType m) {
        util = providedLogger;    // to avoid creating a new method for no reason
        this.setKeys(inputKeys);
        this.setTelegramType(m);
    }

    /**
     * Creates a JavaTelegram function with a way of returning information and status reports. All other variables will
     * have to be set manually later if one uses this constructor.
     * @param logger is a <code>JTelegramLogger</code> which replaces the old logger for the output of information
     */
    public JavaTelegram(JTelegramLogger logger) {
        util = logger;
    }

    /**
     * Sets the time between telegrams which the program is set to wait. Note that this is implemented in
     * {@link JavaTelegram#connect()} to automatically deduct the time necessary to populate <code>NSNation</code> data
     * and check the provided predicates.
     * @param waitTime is the time to wait between telegrams, in milliseconds
     */
    public void setWaitTime(int waitTime) {
        if (waitTime < NSConnection.WAIT_TIME)
            throw new JTelegramException(
                    String.format("Telegram wait time, %d ms, cannot be less API rate-limit %d milliseconds",
                            waitTime, NSConnection.WAIT_TIME));
        if (waitTime < 1000)
            throw new JTelegramException(
                    String.format("Telegram wait time less than 1000! Is input %d not in milliseconds?",
                            waitTime));
        if (waitTime < JTelegramType.NONE.getWaitTime())
            throw new JTelegramException(
                    String.format("Telegram wait time %d ms must exceed API rate limit minimum %d",
                            waitTime, JTelegramType.NONE.getWaitTime()));
        this.waitTime = waitTime;
    }

    /**
     * Changes the keys which the instance will use.
     * @param inputKeys are the keys which will be set contained in a <code>JTelegramKeys</code>
     */
    public void setKeys(JTelegramKeys inputKeys) {
        this.keys = inputKeys;
    }

    /**
     * Changes or sets the recipients who will be used in the connect() method.
     * @param list is an array of all the recipients, each one for each index
     */
    public void setRecipients(List<String> list) {
        recipients = list;
    }

    public void setRecipient(String recipient) {
        recipients = List.of(recipient);
    }

    /**
     * Sets the <code>isRecruitment</code> flag inside the client. This flag defaults to <code>true</code>. When it is
     * set, it overwrites {@link JavaTelegram#waitTime} to the default constants for recruitment and campaign delays.
     * @param m is the mode we are using, modes declare their own default times
     */
    public void setTelegramType(JTelegramType m) {
        this.telegramType = m;
        this.setWaitTime(m.getWaitTime());
    }

    /**
     * Shuts down the connect method, interrupting immediately. If interrupt fails, next cycle will fail and dump out.
     */
    public void kill() {
        killThread = true;
        Thread.currentThread().interrupt();
    }

    /**
     * Resets {@code killThread} to {@code false}.
     */
    public void resetKill() {
        killThread = false;
    }

    /**
     * @return whether the client has been killed
     */
    public static boolean isKilled() {
        return killThread;
    }

    public void addFilter(JTelegramPredicate p) {
        this.predicates.add(p);
    }

    public List<String> getSentList() {
        return sentList;
    }

    /**
     * Connects to the NationStates API and starts sending telegrams to the provided recipients with the provided keys.
     * Note that checks are made in this method ({@link JavaTelegram#predicates}) to make sure that telegrams are sent
     * to nations which do not opt out of those telegrams. All output is logged using {@link JTelegramLogger}.
     * @see JTelegramConnection
     * @see com.git.ifly6.nsapi.telegram.util.JInfoFetcher JInfoFetcher
     */
    public void connect() {

        // Do some null-checks to make sure we can actually send things
        if (keys.anyEmpty()) {
            util.log("Check your keys, one of them is null or empty");
            return;
        }

        if (recipients == null || recipients.isEmpty()) {
            util.log("Error, no recipients.");
            return;
        }

        // Make sure we can actually run a cycle
        killThread = false;
        int totalTelegrams = recipients.size();
        for (int i = 0; i < recipients.size(); i++) { // No iterator due to need for indexing

            String recipient = recipients.get(i);

            // Verify the defaultPredicate
            Map<JTelegramPredicate, Boolean> results = new LinkedHashMap<>();
            NSNation nation = new NSNation(recipient);
            try {
                nation.populateData();
                for (JTelegramPredicate predicate : predicates) {
                    if (predicate == null) continue; // skip null predicates
                    results.put(predicate, predicate.test(nation));
                }

            } catch (NSException e) {
                util.log(String.format("Nation %s does not exist. Skipping nation.", recipient));
                continue;

            } catch (NSIOException e) {
                util.log(String.format("Cannot query for data on %s, assuming check passed, continuing", recipient));

            }

            if (results.containsValue(false)) {
                // list the failed predicates
                List<String> failedPredicates = results.entrySet().stream()
                        .filter(e -> !e.getValue())
                        .map(e -> e.getKey().getName())
                        .collect(Collectors.toList());
                util.log(String.format("Failed predicates %s skipping recipient %s",
                        failedPredicates,
                        recipient));
                continue;
            }

            try {

                // Connect to the API
                JTelegramConnection connection = new JTelegramConnection(keys, recipient);
                JTelegramResponseCode errorCode = connection.verify();

                // Verify Status, then deal with all the possible error codes...
                if (errorCode == JTelegramResponseCode.QUEUED) {
                    util.sentTo(recipient, i, totalTelegrams);
                    sentList.add(recipient);

                } else
                    util.log(formatError(errorCode.getExplanation(), recipient, i + 1, totalTelegrams));

            } catch (IOException e) {
                util.log(formatError("Error in queuing. Check your Internet connection",
                        recipient, i + 1, totalTelegrams));
                LOGGER.log(Level.SEVERE, "IO Exception in JavaTelegram sending thread", e);
            }

            // Implement the rate limit, is skipped if campaign not possible
            try {

                if (killThread) { // terminate if requested
                    util.log("Sending thread terminated quietly.");
                    break;

                } else if (i + 1 == totalTelegrams) util.log("API Queries Complete.");
                else {
                    util.log(String.format("[%d of %d] Queried for %s, next delivery in %.2f seconds",
                            i + 1, totalTelegrams, recipient, (double) waitTime / 1000));
                    Thread.sleep(waitTime - NSConnection.WAIT_TIME);
                }

            } catch (InterruptedException e) {
                util.log("Sending thread was forced to terminate.");    // Report.
                killThread = true;
                break;
            }
        }
    }

    /**
     * Generates an error message in form: <code>Failed to queue delivery to: $rName, $i of $ofI. $message</code>
     */
    private String formatError(String message, String rName, int i, int ofI) {
        if (i == 1 && ofI == 1) return String.format("Failed to queue delivery to: %s. %s", rName, message);
        return String.format("Failed to queue delivery to: %s, %d of %d. %s",
                rName, i, ofI, message);
    }
}
