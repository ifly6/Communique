package com.git.ifly6.nsapi;

// So you can catch this if there is no region.

/**
 * Catch this exception to deal with the possibility that there is no unit to be found where your API call is pointing.
 */
public class NSException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public NSException() {
		super();
	}

	public NSException(String message) {
		super(message);
	}

	public NSException(String message, Throwable throwable) {
		super(message, throwable);
	}
}