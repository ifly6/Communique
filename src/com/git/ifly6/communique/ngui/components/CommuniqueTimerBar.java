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

package com.git.ifly6.communique.ngui.components;

import com.git.ifly6.nsapi.telegram.JavaTelegram;

import javax.swing.JProgressBar;
import javax.swing.Timer;
import java.awt.EventQueue;

public class CommuniqueTimerBar extends JProgressBar {

    private Timer timer;

    public CommuniqueTimerBar() {
        super();
    }

    /**
     * Creates a "countdown" to the end time over the progress bar. Cancels if the thread which it is waiting on is
     * interrupted. You must pass the thread here instead of, in an alternative structure, in the constructor because
     * the thread that is saved in the constructor is not necessarily the <i>same</i> thread as that which you are
     * checking for interruption.
     * @param startMillis starting time in milliseconds
     * @param endMillis   ending time in milliseconds
     */
    public void start(long startMillis, long endMillis) {
        long diffMillis = endMillis - startMillis;
        timer = new Timer(1000 / 20, null); // time between updates in ms
        timer.addActionListener( // must split assignment to properly reference for stopping
                ae -> {
                    // it is not possible to tell if a thread is interrupted after swallowing the exception thus
                    // use static-value of client (there can only be one!) instead of threads to check for interruption
                    // https://stackoverflow.com/a/16324615
                    if (JavaTelegram.isKilled() || System.currentTimeMillis() > endMillis)
                        timer.stop();

                    else  // otherwise keep invoking updates
                        EventQueue.invokeLater(() -> {
                            CommuniqueTimerBar bar = CommuniqueTimerBar.this;
                            bar.setMaximum((int) diffMillis);
                            bar.setValue((int) (System.currentTimeMillis() - startMillis));
                            bar.setToolTipText(String.format("%.1f seconds remaining",
                                    (double) (endMillis - System.currentTimeMillis()) / 1000
                            ));
                        });
                });

        timer.start();
    }

    public void stop() {
        if (isRunning()) timer.stop();
    }

    public boolean isRunning() {
        if (timer == null) return false;
        return timer.isRunning();
    }

    public void reset() {
        this.stop();
        this.setValue(0);
        this.setToolTipText("");
    }

}
