/* Copyright (c) 2018 Kevin Wong
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique;

/** This class contains methods which have been written based on the methods used in Apache Commons Lang 3.5. */
public class CommuniqueUtils {

	public static final boolean IS_OS_MAC = System.getProperty("os.name").toLowerCase().contains("mac");
	public static final boolean IS_OS_WINDOWS = System.getProperty("os.name").toLowerCase().contains("windows");

	/**
	 * Determines whether a <code>String</code> is empty.
	 * @param string to check
	 * @return <code>boolean</code> answering that question
	 */
	public static boolean isEmpty(String string) {
		return string == null || string.trim().length() == 0;
	}

	/**
	 * Determines whether an array is empty.
	 * @param a array
	 * @return <code>boolean</code> answering that question
	 */
	public static boolean isEmpty(Object[] a) {
		return a == null || a.length == 0;
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
