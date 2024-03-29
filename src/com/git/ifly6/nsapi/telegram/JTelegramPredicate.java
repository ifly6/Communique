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

import com.git.ifly6.nsapi.NSNation;

import java.util.function.Predicate;

public class JTelegramPredicate implements Predicate<NSNation> {

    private final String name;

    private final Predicate<NSNation> predicate;

    public JTelegramPredicate(String name, Predicate<NSNation> predicate) {
        this.name = name;
        this.predicate = predicate;
    }

    public String getName() {
        return name;
    }

    @Override
    public boolean test(NSNation nsNation) {
        return predicate.test(nsNation);
    }

    @Override
    public Predicate<NSNation> and(Predicate<? super NSNation> other) {
        return predicate.and(other);
    }

    @Override
    public Predicate<NSNation> negate() {
        return predicate.negate();
    }

    @Override
    public Predicate<NSNation> or(Predicate<? super NSNation> other) {
        return predicate.or(other);
    }

    public static <T> Predicate<T> isEqual(Object targetRef) {
        return Predicate.isEqual(targetRef);
    }

}
