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

import com.git.ifly6.nsapi.ctelegram.CommSender;

import javax.swing.JProgressBar;
import javax.swing.Timer;
import java.time.Duration;
import java.time.Instant;

public class Communique3ProgressBarHandler {

    public static final long MILLIS_PER_FRAME = Math.round(1000 / (double) 60);

    Communique3 communique;
    JProgressBar bar;
    Timer timer;

    public Communique3ProgressBarHandler(Communique3 communique, JProgressBar bar) {
        this.communique = communique;
        this.bar = bar;
    }

    public void progressInterval(CommSender client) {
        progressIntervalUntil(client.nextAt());
    }

    public void progressIntervalUntil(Instant at) {
        progressIntervalUntil(at, null);
    }

    public void progressIntervalUntil(Instant future, String description) {
        // provide description
        if (description != null) {
            bar.setStringPainted(true);
            bar.setString(description);
        }

        // draw timer changes
        Duration duration = Duration.between(Instant.now(), future);
        bar.setMaximum((int) duration.toMillis());
        timer = new Timer((int) Duration.ofMillis(MILLIS_PER_FRAME).toMillis(),
                e -> {
                    try {
                        int timeElapsed = bar.getMaximum()
                                - (int) Duration.between(Instant.now(), future).toMillis();
                        bar.setValue(timeElapsed);

                    } catch (UnsupportedOperationException exception) {
                        this.reset();
                    }
                });
        timer.start();
    }

    public void reset() {
        bar.setValue(0);
        if (timer != null)
            timer.stop(); // should never be null
    }
}
