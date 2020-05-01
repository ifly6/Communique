package com.git.ifly6.nsapi.telegram;

/**
 * <code>JTelegramException</code> indicates conditions that an application might want to catch when related to issues
 * which can only be created by a failure to sanitise data which is recognised by <code>JavaTelegram</code>.
 * <p>
 * Proper use of this class is to catch an <code>Exception</code> which would arise, but can only arise due to a failure
 * to handle data correctly. The purpose of this class is to replace the use of <code>catch (Exception e)</code> in code
 * for the purpose of being more accurate in cases where an <code>IOException</code> could also occur.
 */
public class JTelegramException extends RuntimeException {

	private static final long serialVersionUID = 1L;

	public JTelegramException() {
		super();
	}

	public JTelegramException(String message) {
		super(message);
	}

	public JTelegramException(String message, Throwable e) {
		super(message, e);
	}
}
