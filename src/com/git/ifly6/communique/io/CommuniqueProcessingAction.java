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
package com.git.ifly6.communique.io;

import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.ctelegram.io.cache.CommDelegatesCache;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;

/**
 * This implements post-processing actions on the recipient list as a whole for Communique and Marconi clients. It has
 * various functions, like randomisation, reversing the order of recipients, and prioritisation of delegates.
 * @since version 8 (2018-01-07)
 */
public enum CommuniqueProcessingAction implements Function<List<String>, List<String>> {

    /**
     * Randomises the order of recipients
     * @since version 8 (2018-01-07)
     */
    RANDOMISE {
        @Override
        public List<String> apply(List<String> input) {
            Collections.shuffle(input);
            return input; // do nothing
        }

        @Override
        public String toString() {
            return "Randomise order";
        }
    },

    /**
     * Reverses the initial order of recipients
     * @since version 8 (2018-01-07)
     */
    REVERSE {
        @Override
        public List<String> apply(List<String> input) {
            for (int i = 0; i < input.size() / 2; i++) { // algorithm to quickly reverse a list
                String original = input.get(i);
                int other = input.size() - 1 - i;
                input.set(i, input.get(other));
                input.set(other, original);
            }
            return input; // it happened in place
        }

        @Override
        public String toString() {
            return "Reverse order";
        }
    },

    /**
     * Moves all delegates in the list of recipients to the front, randomises its order, then places all other nations
     * in a random order after those delegates.
     * @since version 8 (2018-01-07)
     */
    DELEGATE_PRIORITISE {
        @Override
        public List<String> apply(List<String> input) {
            Set<String> delegates = new HashSet<>(CommDelegatesCache.getInstance().getDelegates());

            List<String> listDelegates = new ArrayList<>(); // delegates we have
            List<String> nonDelegate = new ArrayList<>(); // the rest
            for (String s : input)
                if (delegates.contains(s)) listDelegates.add(s); // see docs on this algorithm
                else nonDelegate.add(s);

            ApiUtils.shuffle(listDelegates);
            ApiUtils.shuffle(nonDelegate);

            List<String> newList = new ArrayList<>(listDelegates.size() + nonDelegate.size());
            newList.addAll(listDelegates);
            newList.addAll(nonDelegate);
            return newList;
        }

        @Override
        public String toString() {
            return "Prioritise delegates";
        }
    },

    NONE {
        @Override
        public List<String> apply(List<String> input) {
            return input;
        }

        @Override
        public String toString() {
            return "None";
        }
    };

    /**
     * Applies the processing action to the provided list, which should be of raw NationStates reference names
     */
    public abstract List<String> apply(List<String> input);

    /**
     * Should always return the name of the <code>CommuiniqueProcessingAction</code> in the code
     */
    @Override
    public abstract String toString();
}
