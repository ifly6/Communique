package com.git.ifly6.communique;

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
public class CommuniquéFlags {

	private boolean isRecruitment;

	private boolean randomSort;

	public CommuniquéFlags() {

	}

	public CommuniquéFlags(boolean isRecruitment, boolean randomSort) {
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
	public boolean[] toBoolean() {
		return new boolean[] { isRecruitment, randomSort };
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
