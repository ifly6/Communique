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

package com.git.ifly6.nsapi.ctelegram.monitors.rules;

import com.git.ifly6.nsapi.ctelegram.monitors.CommMonitor;

import java.util.List;
import java.util.OptionalLong;
import java.util.stream.Collectors;

/**
 * Creates a monitor which auto-exhausts after passing {@link #limit} recipients.
 */
public class CommLimitedMonitor implements CommMonitor {

    CommMonitor monitor;
    private int limit;
    private int passCount;

    public CommLimitedMonitor(CommMonitor m, int limit) {
        this.monitor = m;
        this.limit = limit;
    }

    @Override
    public List<String> getRecipients() {
        List<String> l = monitor.getRecipients();
        int permits = limit - passCount;
        if (permits <= 0)
            throw new CommMonitor.ExhaustedException(String.format("passed more than permitted %d", passCount));

        List<String> permittedPass = l.stream().limit(permits).collect(Collectors.toList());
        passCount += permittedPass.size();
        return permittedPass;
    }

    @Override
    public boolean recipientsExhausted() {
        return passCount > limit;
    }

    @Override
    public OptionalLong recipientsCountIfKnown() {
        return monitor.recipientsCountIfKnown();
    }
}
