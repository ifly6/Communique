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

package com.git.ifly6.communique.ngui.components.subcomponents;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;

public class CommuniqueDelayedExecutor<T> {

    // initially tried https://stackoverflow.com/a/31955279
    // that didn't work though

    private final Consumer<T> consumer;
    private final int DELAY;
    private ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
    private List<ScheduledFuture<?>> futureList = new ArrayList<>();

    public CommuniqueDelayedExecutor(int secondsDelayed, Consumer<T> consumer) {
        this.consumer = consumer;
        this.DELAY = secondsDelayed;
    }

    public void reschedule(T event) {
        // cancel all futures
        futureList.removeIf(Future::isDone);
        futureList.forEach(f -> f.cancel(false));

        // create a new future
        ScheduledFuture<?> future = executor.schedule(() -> consumer.accept(event), DELAY, TimeUnit.SECONDS);
        futureList.add(future);
    }

}
