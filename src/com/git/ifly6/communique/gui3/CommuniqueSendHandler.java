/*
 * Copyright (c) 2021 ifly6
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

package com.git.ifly6.communique.gui3;

import com.git.ifly6.communique.data.Communique7Monitor;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.nsapi.ctelegram.CommSender;
import com.git.ifly6.nsapi.ctelegram.monitors.CommExhaustiveMonitor;
import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;

import java.time.Duration;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Logger;

import static java.lang.Thread.sleep;

/**
 * Creates a separate thread on which Communique can repeat sending.
 */
public class CommuniqueSendHandler {

    public static final Logger LOGGER = Logger.getLogger(CommuniqueSendHandler.class.getName());
    private static final Duration MIN_DURATION = Duration.of(5, ChronoUnit.MINUTES);
    private ExecutorService managementExecutor = Executors.newSingleThreadExecutor();

    private CommMonitor exhaustiveMonitor;

    private CommuniqueConfig config;
    private Communique3 communique3;

    private Runnable onAutoStop;

    public CommuniqueSendHandler(Communique7Monitor monitor, CommuniqueConfig config, Communique3 communique3) {
        this.exhaustiveMonitor = new CommExhaustiveMonitor(monitor);
        this.config = config;
        this.communique3 = communique3;
    }

    public void onAutoStop(Runnable r) {
        onAutoStop = r;
    }

    /**
     * Starts sending. Control flow: (1) an auto-stop is configured to stop sending at a certain time, if the auto-stop
     * is defined, (2) in the other thread, a new {@link CommSender} is defined and assigned to {@link
     * Communique3#client}. This is let to run, blocking further action until it completes or is stopped. If config
     * defines a repeat loop, then execute again, but do not execute any loop more frequently than {@link
     * #MIN_DURATION}.
     * <p>While the scheduler is let to run, there is a time-out. That time-out is at 365 days. The program should
     * never be running that long, but it will auto-stop after a year without any intervention.</p>
     */
    @SuppressWarnings("BusyWait")
    public void execute() {
        // preset the auto-stop
        AtomicBoolean stopping = new AtomicBoolean(false);
        if (config.getAutoStop().isPresent())
            Executors.newSingleThreadScheduledExecutor().schedule(() -> {
                stopping.set(true);
                if (communique3.client != null) this.stopSend();
                if (onAutoStop != null) onAutoStop.run();

            }, config.getAutoStop().get().toMillis(), TimeUnit.MILLISECONDS);

        managementExecutor.execute(() -> {
            if (Thread.currentThread().isInterrupted()) {
                LOGGER.info("Send handler received interrupt gracefully, stopping");
                stopSendTasks();
                return;
            }

            int execCount = 0;
            LOGGER.info("Starting execution management thread");
            if (config.repeats)
                LOGGER.info("Repeating script execution");

            // if not repeating, still execute; if repeating, continue doing so
            // do-whiles are so rare... it's actually useful here!
            do {
                if (config.repeats && execCount > 0)
                    LOGGER.info(String.format("Send restart on loop %d", execCount));

                try {
                    final Instant start = Instant.now();
                    communique3.client = new CommSender(config.keys, exhaustiveMonitor, config.getTelegramType(), communique3);
                    communique3.client.startSend();

                    LOGGER.fine("Awaiting send client termination");
                    boolean timedOut = !communique3.client
                            .getScheduler()
                            .awaitTermination(365, TimeUnit.DAYS); // totally unrealistic max time

                    // on finish, what to do?
                    if (timedOut) {
                        LOGGER.severe("Sending client timed out after 1 year. Ending repeat loop.");
                        break;

                    } else if (!config.repeats) {
                        // if config does not repeat, immediately break loop
                        break;

                    } else if (Instant.now().isBefore(start.plus(MIN_DURATION))) {
                        // if repeating...
                        // do not start the next iteration until MIN_DURATION has passed
                        long waitMillis = Duration.between(Instant.now(), start.plus(MIN_DURATION)).toMillis();
                        LOGGER.info(String.format(
                                "Client finished sending before min duration, starting wait for %d ms",
                                waitMillis));
                        sleep(waitMillis);
                    }

                } catch (InterruptedException e) {
                    LOGGER.info("Interrupted while awaiting client termination");
                    stopSendTasks();
                    return;
                }
                execCount++;

            } while (config.repeats && !stopping.get());
            stopSendTasks();
        });
    }

    /** Defines tasks to be taken on send stop. Should be followed with {@code return;}. */
    private void stopSendTasks() {
        LOGGER.info("Executing stop send tasks");
        communique3.client.stopSend();
        communique3.onTerminate();
    }

    /**
     * Triggers immediate shutdown for the management executor via {@link InterruptedException}.
     */
    public void stopSend() {
        managementExecutor.shutdownNow();
    }

    /**
     * Determines if the executor and client are shut down.
     * @return true if the this thread managing the client is shutdown <b>and</b> the client also is not running
     */
    public boolean isShutdown() {
        boolean executorDown = managementExecutor.isShutdown() || managementExecutor.isTerminated();
        boolean clientDown = !communique3.client.isRunning();
        return executorDown && clientDown;
    }
}
