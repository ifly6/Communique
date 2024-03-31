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
package com.git.ifly6.nsapi;

import javax.annotation.Nonnull;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Random;
import java.util.regex.Pattern;

/**
 * Utility functions for handling or parsing information from the NationStates API. See also <a
 * href="https://www.nationstates.net/pages/api.html">API documentation</a>.
 */
public class ApiUtils {

    /**
     * Stable randomisation seed.
     * @since version 3.0 (build 13)
     */
    public static final Random RANDOM = new Random(81141418);

    private static final Pattern WHITESPACE = Pattern.compile("\\s");

    private ApiUtils() {
    }


    /**
     * Changes some name into a reference name.
     * @param input to turn into a reference name
     * @return reference name form of the input name
     */
    public static String ref(String input) {
        if (input == null) throw new NullPointerException("Cannot convert null string to reference format");
        return WHITESPACE.matcher(input.trim().toLowerCase()).replaceAll("_");
        // 2024-03-29 MUST be replaceAll and NOT replace
        // 2024-03-30 change to pattern
    }

    /**
     * Applies {@link ApiUtils#ref(String)} to elements; removes empty (or all-white-space) elements.
     * @param list to convert to reference format
     * @return elements in reference form
     */
    public static List<String> ref(List<String> list) {
        List<String> refs = new ArrayList<>(list.size());
        for (String s : list)
            if (ApiUtils.isNotEmpty(s))
                refs.add(ApiUtils.ref(s));

        return refs;
    }

    /**
     * Tests whether first string starts with the latter string; case insensitive.
     * @param s      to test
     * @param prefix to look for prefix
     * @return true if prefix (case-insensitive) present; false otherwise
     */
    public static boolean startsWithLowerCase(String s, String prefix) {
        return s.toLowerCase().startsWith(prefix.toLowerCase());
    }

    /**
     * Determines whether a given array is empty.
     * @param a array
     * @return true if empty
     */
    public static boolean isEmpty(Object[] a) {
        return a == null || a.length == 0;
    }

    /**
     * @param s to check for empty-ness
     * @return inverted {@link String#isBlank()}
     */
    public static boolean isNotEmpty(String s) {
        return !s.isBlank();
    }

    /**
     * Determines whether an array contains some value. Method does not check type.
     * @param hay    to check in
     * @param needle to check for
     * @return whether array contains needle
     */
    public static boolean contains(Object[] hay, Object needle) {
        if (isEmpty(hay)) return false;
        if (needle == null) return false;
        for (Object element : hay)
            if (element.equals(needle))
                return true;
        return false;
    }

    /**
     * Shuffles array and returns it. Always shuffles with the same {@link Random} source: {@link #RANDOM}.
     * @param list to shuffle
     * @returns shuffled list; can be ignored because {@link Collections#shuffle(List, Random)} acts in-place
     * @since version 3.0 (build 13)
     */
    public static <T> List<T> shuffle(List<T> list) {
        Collections.shuffle(list, RANDOM);
        return list;
    }

}
