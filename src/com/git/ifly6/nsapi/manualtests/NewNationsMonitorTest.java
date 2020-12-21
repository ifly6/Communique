/*
 * Copyright (c) 2020 ifly6
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

package com.git.ifly6.nsapi.manualtests;

import com.git.ifly6.nsapi.ctelegram.monitors.reflected.CommNewNationsMonitor;

import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class NewNationsMonitorTest {
    public static void main(String[] args) throws InterruptedException {
        CommNewNationsMonitor nnm = CommNewNationsMonitor.getInstance();

        Set<String> allRecipients = new HashSet<>();
        Map<Instant, Set<String>> l = new HashMap<>();
        while (allRecipients.size() < 100) {
            System.out.println("getting recipients");
            List<String> r = nnm.getRecipients();
            l.put(Instant.now(), new HashSet<>(r));
            allRecipients.addAll(r);

            System.out.println("recipients are " + r.toString());
            Thread.sleep(nnm.getUpdateInterval().dividedBy(2).toMillis());
        }

        System.out.println("total results");
        System.out.println(UpdatingMonitorTest.prettyPrint(l));

        nnm.stop();
        System.out.println("stopped");

    }
}