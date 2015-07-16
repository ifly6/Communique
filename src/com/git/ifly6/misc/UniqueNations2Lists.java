/*
 * Copyright (c) 2015 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining
 * a copy of this software and associated documentation files (the
 * "Software"), to deal in the Software without restriction, including
 * without limitation the rights to use, copy, modify, merge, publish,
 * distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to
 * the following conditions:
 *
 * The above copyright notice and this permission notice shall be
 * included in all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
 * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF
 * MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
 * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT HOLDERS BE
 * LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION
 * OF CONTRACT, TORT OR OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION
 * WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.misc;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;

public class UniqueNations2Lists {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);

		// Get names of delegates
		System.out.println("Input names of recipients 1");
		String xml_raw = scan.nextLine();
		String xml_no_return = xml_raw.replace("\n", ",");
		xml_no_return = xml_no_return.replace(", ", ","); // Replaces ', ' with ','
		String[] recipients_array = xml_no_return.split(","); // Turns it into an array.

		// Get names of delegates 2
		System.out.println("Input names of recipients 2");
		String xml_raw_2 = scan.nextLine();
		String xml_no_return_2 = xml_raw_2.replace("\n", ",");
		xml_no_return = xml_no_return_2.replace(", ", ","); // Replaces ', ' with ','
		String[] recipients_array_2 = xml_no_return.split(","); // More arrays!

		List<String> l1 = Arrays.asList(recipients_array);
		l1.addAll(Arrays.asList(recipients_array_2));
		String[] result = (String[]) l1.toArray();

		Set<String> h = new HashSet<String>(Arrays.asList(result));
		String[] uniqueValues = h.toArray(new String[0]);

		// Print unique values
		for (String element : uniqueValues) {
			System.out.println(element);
		}

		scan.close();

	}

}
