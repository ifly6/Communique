/* Copyright (c) 2015 ifly6
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

package com.git.ifly6.communique.data;

/**
 * <code>CommuniquéFlags</code> creates a unified object for the storage and retrieval of Communiqué flags.
 *
 * <p>
 * Because it is needed to be able to send all the Communiqué flags as one object, this object was created as an
 * integrated system to do so.
 * </p>
 *
 * <p>
 * It gives multiple methods which are able to access and get the keys. It also gives ways to translate those keys into
 * a <code>String[]</code> and a <code>String</code>.
 * </p>
 *
 */
public class CFlags {

	private boolean isRecruitment;
	private boolean randomSort;

	private CFlags() {

	}

	private CFlags(boolean isRecruitment, boolean randomSort) {
		this();
		setRecruitment(isRecruitment);
		setRandomSort(randomSort);
	}

	@Override
	/**
	 * Converts <code>CommuniquéFlags</code> into a <code>String</code> with a comma delimiter.
	 */
	public String toString() {
		return isRecruitment + ", " + randomSort;
	}

	/**
	 * Converts <code>CommuniquéFlags</code> into a <code>boolean[]</code> with <code>isRecruitment</code> first,
	 * followed by <code>randomSort</code>.
	 */
	public boolean[] toBooleanArray() {
		return new boolean[] { isRecruitment, randomSort };
	}

	public void setWithBooleanArray(boolean[] arr) {
		if (arr.length == 2) {
			isRecruitment = arr[0];
			randomSort = arr[1];
		}
	}

	/**
	 * @return the value of <code>randomSort</code>
	 */
	public boolean isRandomSort() {
		return randomSort;
	}

	/**
	 * @param sortRandom sets the <code>randomSort</code> value
	 */
	public void setRandomSort(boolean sortRandom) {
		this.randomSort = sortRandom;
	}

	/**
	 * @return the value of <code>isRecruitment</code>
	 */
	public boolean isRecruitment() {
		return isRecruitment;
	}

	/**
	 * @param recruitment sets the <code>isRecruitment</code> value
	 */
	public void setRecruitment(boolean recruitment) {
		this.isRecruitment = recruitment;
	}
}
