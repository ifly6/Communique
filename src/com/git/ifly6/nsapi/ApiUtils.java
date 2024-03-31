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

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;

public class ApiUtils {

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
     * Applies the {@link ApiUtils#ref(String)} to all elements in a <code>List</code>
     * @param list of strings to convert to reference format
     * @return a <code>List</code> with all elements having ref applied
     */
    public static List<String> ref(List<String> list) {
        List<String> refs = new ArrayList<>(list.size());
        for (String s : list) refs.add(ApiUtils.ref(s));
        return refs;
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
     * Determines whether an array contains some value, utilising the standard <code>Object</code> equals method. This
     * method does not do type-checking.
     * @param array  to check in
     * @param needle to check for
     * @return whether array contains needle
     */
    public static boolean contains(Object[] array, Object needle) {
        if (isEmpty(array)) return false;
        if (needle == null) return false;
        for (Object element : array)
            if (element.equals(needle))
                return true;
        return false;
    }

}
