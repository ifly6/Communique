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

package com.git.ifly6.nsapi.ctelegram;

import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.ctelegram.io.NSTGSettingsException;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor.ExhaustedException;
import com.git.ifly6.nsapi.telegram.JTelegramConnection;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramResponseCode;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Queue;
import java.util.Set;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Sends telegrams on the NationStates API. Instantiation is not restricted; regardless, there should never be more than
 * one. Sender sends indefinitely or until linked {@link CommMonitor}
 * {@link CommMonitor#recipientsExhausted() exhuasts}.
 * @since version 13
 */
public class CommSender {

    public static final Logger LOGGER = Logger.getLogger(CommSender.class.getName());
    private static final String NO_RECIPIENT = "__NO_RECIPIENT__";

    /**
     * One thread for many clients.
     */
    private static ScheduledExecutorService scheduler = Executors.newSingleThreadScheduledExecutor();
    private ScheduledFuture<?> job;

    private final CommSenderInterface outputInterface;
    private final Instant initAt = Instant.now();
    private final JTelegramKeys keys;
    private final JTelegramType telegramType;
    private final CommMonitor monitor;

    /**
     * If true, does not send telegrams.
     */
    private boolean dryRun = false;

    /** First-in-first-out send queue. */
    private final Queue<String> sendQueue = new SynchronousQueue<>(true);
    private Instant lastTGAttempt;

    /**
     * Recipients to which the telegram has already been sent are put in the sent list.
     */
    private Set<String> sentList = new LinkedHashSet<>(); // ordered
    private Set<String> skipList = new LinkedHashSet<>();

    /**
     * Constructs a {@link CommSender}.
     * @param anInterface  to report to
     * @param keys         to send with
     * @param monitor      to provide recipients
     * @param telegramType to provide delay timings
     */
    public CommSender(
            JTelegramKeys keys, CommMonitor monitor, JTelegramType telegramType,
            CommSenderInterface anInterface) {
        this.keys = keys;
        this.monitor = monitor;
        this.telegramType = telegramType;
        outputInterface = anInterface;
    }

    /**
     * If true, no telegrams are actually sent. Should be used for debugging purposes only. Default is false.
     * @param dryRun if true
     */
    public void setDryRun(boolean dryRun) {
        this.dryRun = dryRun;
    }

    /**
     * @return {@link CommMonitor} instantiating
     */
    public CommMonitor getMonitor() {
        return monitor;
    }

    /**
     * Finds the <i>single</i> next valid recipient from the monitor and places it into the queue. If this takes longer
     * than 10 seconds, the single threaded {@link ScheduledExecutorService} will block until it is available.
     */
    private void findNext() {
        List<String> recipients = monitor.getRecipients();
        for (String i : recipients)
            if (!processListsContain(i) && new CommRecipientChecker(i, this.telegramType).check()) {
                // log that it was got, then return
                if (Duration.between(initAt, Instant.now()).compareTo(Duration.ofSeconds(20)) < 0)
                    LOGGER.info(String.format("Got recipient %s on initialisation", i));
                else
                    LOGGER.info(String.format("Got recipient %s at NEXT-%.2f seconds", i,
                            (double) Duration.between(Instant.now(), this.nextAt()).toMillis() / 1000
                    ));

                // add to queue and stop
                sendQueue.add(i);
                return;

            } else this.reportProcessed(i, SendingAction.SKIPPED); // report skipped when skipping

        // if there is nothing, do nothing
    }

    /**
     * Sends telegram to recipient, with recipient determined as first thing in the queue.
     */
    private void executeSend() {
        if (dryRun) LOGGER.warning("SENDING AS DRY RUN!");
        if (Instant.now().isAfter(this.nextAt().plus(250, ChronoUnit.MILLIS)))
            LOGGER.info(String.format("Expected to send telegram at time %s but delayed by %s ms",
                    this.nextAt().toString(), Duration.between(this.nextAt(), Instant.now()).toMillis()
            ));

        if (sendQueue.peek() == null) {
            // try once to get a recipient
            findNext();
            if (sendQueue.peek() == null) {
                lastTGAttempt = Instant.now();
                return; // do nothing but throw no error; this is possible when the event monitored hasn't happened yet
            }
        }

        // if we have a valid recipient, get it
        String recipient = sendQueue.poll();
        LOGGER.info(String.format("Got recipient %s from queue", recipient));

        // do the actual send
        JTelegramConnection connection;
        try {
            LOGGER.finer("Creating telegram connection ");
            connection = new JTelegramConnection(keys, recipient, dryRun);
            lastTGAttempt = Instant.now();

        } catch (IOException e) { // rethrow
            throw new NSIOException("Cannot initialise connection to telegram API", e);
        }

        // parse the response
        try {
            // get response code
            JTelegramResponseCode responseCode = connection.verify();
            LOGGER.info(String.format("Received response code %s", responseCode));

            // if there is an error
            if (responseCode != JTelegramResponseCode.QUEUED)
                throw NSTGSettingsException.createException(keys, responseCode);

            // we sent to this recipient
            reportProcessed(recipient, SendingAction.SENT);
            sentList.add(recipient);

            // check for exhaustion
            if (monitor.recipientsExhausted())
                throw new ExhaustedException("Monitor politely reports exhaustion");

        } catch (IOException e) {
            throw new NSIOException("Cannot get response code from telegram API", e);
        }

        // schedule for getting the next recipient
        long MILLIS_UNTIL_10_SECONDS_BEFORE_NEXT = Duration
                .between(Instant.now(), this.nextAt().minus(10, ChronoUnit.SECONDS))
                .toMillis();
        scheduler.schedule(this::findNext, MILLIS_UNTIL_10_SECONDS_BEFORE_NEXT, TimeUnit.MILLISECONDS);
    }

    private void reportProcessed(String recipient, SendingAction action) {
        if (action == SendingAction.SENT) sentList.add(recipient);
        else if (action == SendingAction.SKIPPED) skipList.add(recipient);
        outputInterface.processed(recipient, sentList.size() + skipList.size(), action);
    }

    public void startSend() {
        LOGGER.info("Starting send thread");
        if (isRunning())
            throw new UnsupportedOperationException("Cannot start sending after it already started");

        if (scheduler.isShutdown() || scheduler.isTerminated()) {
            LOGGER.info("Initialising new scheduler, old schedule facility is closed");
            scheduler = Executors.newSingleThreadScheduledExecutor();
        }

        // schedule with fixed delay automatically repeats
        job = scheduler.scheduleWithFixedDelay(() -> {
            if (Thread.currentThread().isInterrupted()) return;
            try {
                executeSend();

            } catch (ExhaustedException e) {
                LOGGER.info("Recipients exhausted; shutting down sending facility");
                this.stopSend();
                // graceful stop required

            } catch (Throwable e) {
                // loud stop
                final String message = "Encountered error in sending thread!";
                LOGGER.log(Level.SEVERE, message, e);
                this.outputInterface.onError(message, e);
                this.stopSend();
            }
        }, 0, telegramType.getWaitDuration().toMillis(), TimeUnit.MILLISECONDS);

        // log sending thread creation
        LOGGER.info(String.format("Scheduled sending thread start with wait duration %d ms",
                telegramType.getWaitDuration().toMillis()));
    }

    public void stopSend() {
        LOGGER.info("Stopping CommuniqueSender sending thread");
        if (isRunning())
            job.cancel(true);

        if (!scheduler.isShutdown())
            LOGGER.info(String.format("Shutdown left %d incomplete tasks",
                    scheduler.shutdownNow().size()));

        outputInterface.onTerminate(); // trigger the interface termination task
    }

    /**
     * Returns list of sent recipients.
     */
    public Set<String> getSentList() {
        return sentList;
    }

    /**
     * Returns list of skipped recipients.
     */
    public Set<String> getSkipList() {
        return skipList;
    }

    /**
     * @param s is string name to check
     * @return true if the recipient appears in one of the processing lists
     */
    public boolean processListsContain(String s) {
        return sentList.contains(s) || skipList.contains(s);
    }

    /**
     * @returns {@link CommSender} initialisation time
     */
    public Instant getInitAt() {
        return initAt;
    }

    /**
     * @return true if sending telegrams.
     */
    public boolean isRunning() {
        if (job == null) return false;
        if (job.isDone() || job.isCancelled()) return false;
        return !scheduler.isShutdown() && !scheduler.isTerminated();
    }

    public ScheduledExecutorService getScheduler() {
        return scheduler;
    }

    /**
     * @return {@link Duration} until next telegram is sent, if available
     * @throws UnsupportedOperationException if client is not running, does not attempt to stop
     */
    public Instant nextAt() {
        if (isRunning()) {
            if (lastTGAttempt == null) {
                LOGGER.warning("Asked for next telegram time but last telegram time is null?");
                return Instant.now();
            }
            return lastTGAttempt.plus(job.getDelay(TimeUnit.MILLISECONDS), ChronoUnit.MILLIS);
        }
        throw new UnsupportedOperationException("No duration to next telegram; no telegrams are being sent");
    }

    /**
     * Thrown if no recipient is found in the queue
     */
    public static class EmptyQueueException extends NoSuchElementException {
    }

    /**
     * Indicates whether something was sent or skipped.
     */
    public enum SendingAction {
        SENT, SKIPPED
    }
}
