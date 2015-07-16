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

import java.util.Scanner;

public class DelegateVotingListParse {

	public static void main(String[] args) {
		Scanner scan = new Scanner(System.in);

		System.out.println("Input your data.");

		String inputData = scan.nextLine();

		inputData = inputData.replaceAll("\\(.+?\\)", "");
		System.out.println(inputData);

		String[] recipients = inputData.split(",");

		for (int x = 0; x < recipients.length; x++) {
			recipients[x] = recipients[x].trim();
		}

		for (String element : recipients) {
			element = element.toLowerCase().replace(" ", "_");
			System.out.println(element);
		}

		scan.close();
	}
}
