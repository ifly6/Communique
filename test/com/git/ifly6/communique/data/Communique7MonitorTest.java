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

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.nsapi.ctelegram.CommSender;
import com.git.ifly6.nsapi.ctelegram.CommSenderInterface;
import org.junit.jupiter.api.Test;

import java.time.Duration;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static java.time.temporal.ChronoUnit.SECONDS;
import static org.junit.jupiter.api.Assertions.assertEquals;

class Communique7MonitorTest {

    private static final Logger LOGGER = Logger.getLogger(Communique7MonitorTest.class.getName());

    @Test
    void testUpdateMutability() {
        CommuniqueConfig testConfig = new CommuniqueConfig();
        testConfig.addcRecipient(CommuniqueRecipients.createNation("imperium_anglorum"));
        testConfig.addcRecipient(CommuniqueRecipients.createNation("transilia"));

        Communique7Monitor m = new Communique7Monitor(testConfig);
        CommSender sender = m.constructSender(new CommSenderInterface() {
            @Override
            public void processed(String recipient, int numberSent, CommSender.SendingAction action) {
                List<CommuniqueRecipient> newList = testConfig.getcRecipients();
                newList.add(CommuniqueRecipients.createExcludedNation(recipient));
                testConfig.setcRecipients(newList);
            }

            @Override
            public void onTerminate() { }

            @Override
            public void onError(String m, Throwable e) { LOGGER.log(Level.SEVERE, m, e); }
        });
        sender.setDryRun(true);
        sender.startSend();

        try {
            Thread.sleep(Duration.of(21, SECONDS).toMillis());
        } catch (InterruptedException ignored) { }
        assertEquals(1, m.getRecipients().size());
        sender.stopSend(); // remember to stop
    }

}