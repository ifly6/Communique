package com.git.ifly6.nsapi;

/**
 * Catch this exception to deal with the possibility that the NationStates API may be down. One could consider it little
 * more than a wrapper around <code>IOException</code>, and one would probably be correctly considering this design
 * choice.
 */
public class NSIOException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NSIOException() {
		super();
	}

	public NSIOException(String message) {
		super(message);
	}

}
