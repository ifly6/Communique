package com.git.ifly6.nsapi.telegram;

import java.util.Objects;

/**
 * <code>JTelegramKeys</code> creates a unified object for the storage and retrieval of the client, secret, and
 * telegram keys.
 * <p>
 * Due to the problems of hardcoding a direct <code>String[]</code> as the system for holding the keys as well as not
 * being modular in any way, this system was created to organise them and simplify usage. It gives multiple methods
 * which are able to access and get the keys. It also gives ways to translate those keys into a <code>String[]</code>
 * and a <code>String</code>.
 * </p>
 * <p>
 * All <code>setX</code> methods trim their inputs.
 * </p>
 */
public class JTelegramKeys {

	private String clientKey;
	private String secretKey;
	private String telegramId;

	/**
	 * Creates a <code>JTelegramKeys</code> which holds the client, secret, and telegram keys provided in the
	 * constructor.
	 * @param client a <code>String</code> which contains the <code>clientKey</code>
	 * @param secret a <code>String</code> which contains the <code>secretKey</code>
	 * @param tgId   a <code>String</code> which contains the <code>telegramId</code>
	 */
	public JTelegramKeys(String client, String secret, String tgId) {
		clientKey = client;
		secretKey = secret;
		telegramId = tgId;
	}

	/**
	 * Creates a purposely invalid "empty" <code>JTelegramKeys</code>.
	 */
	public JTelegramKeys() {
		clientKey = "CLIENT_KEY";
		secretKey = "SECRET_KEY";
		telegramId = "TELEGRAM_ID";
	}

	/**
	 * Converts <code>JTelegramKeys</code> into a string with a comma delimiter.
	 */
	@Override
	public String toString() {
		return clientKey + ", " + secretKey + ", " + telegramId;
	}

	/**
	 * Sets the keys using a <code>String[]</code> which contains all of the keys in the order <code>clientKey</code>,
	 * <code>secretKey</code>, <code>telegramId</code>.
	 * @param input <code>String[]</code> in the form <code>{ clientKey, secretKey, telegramId }</code>.
	 */
	public void setKeys(String[] input) {
		clientKey = input[0];
		secretKey = input[1];
		telegramId = input[2];
	}

	/**
	 * Gets all keys as a <code>String[]</code>. This method is the same as <code>toArray()</code>.
	 * @return <code>String[]</code> containing all the keys in the form
	 * <code>{ clientKey, secretKey, telegramId }</code>
	 */
	@Deprecated
	public String[] getKeys() {
		return toArray();
	}

	/**
	 * Gets the client key as a <code>String</code>.
	 * @return <code>String</code> containing whatever the client key was already set to.
	 */
	public String getClientKey() {
		return clientKey;
	}

	/**
	 * Gets the secret key as a <code>String</code>.
	 * @return <code>String</code> containing whatever the secret key was already set to.
	 */
	public String getSecretKey() {
		return secretKey;
	}

	/**
	 * Gets the telegram ID as a <code>String</code>.
	 * @return <code>String</code> containing whatever the telegram ID was already set to.
	 */
	public String getTelegramId() {
		return telegramId;
	}

	/**
	 * Sets the client key using a <code>String</code>.
	 * @param input is a <code>String</code> containing the client key
	 */
	public void setClientKey(String input) {
		clientKey = input.trim();
	}

	/**
	 * Sets the secret key using a <code>String</code>.
	 * @param input is a <code>String</code> containing the secret key
	 */
	public void setSecretKey(String input) {
		secretKey = input.trim();
	}

	/**
	 * Sets the telegram ID using a <code>String</code>.
	 * @param input is a <code>String</code> containing the telegram ID
	 */
	public void setTelegramId(String input) {
		telegramId = input.trim();
	}

	/**
	 * Gets all keys as a <code>String[]</code>.
	 * @return <code>String[]</code> containing all the keys in the form
	 * <code>{ clientKey, secretKey, telegramId }</code>
	 */
	public String[] toArray() {
		return new String[]{clientKey, secretKey, telegramId};
	}

	public boolean anyEmpty() {
		if (Objects.isNull(clientKey) || clientKey.isEmpty()) return true;
		if (Objects.isNull(secretKey) || secretKey.isEmpty()) return true;

		//noinspection RedundantIfStatement
		if (Objects.isNull(telegramId) || telegramId.isEmpty()) return true;
		// ^ IntelliJ says 'simplify!', this is more clear

		return false;
	}

}
