package com.git.ifly6.nsapi.telegram;

import com.git.ifly6.nsapi.NSConnection;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 * <code>JTelegramConnection</code> is the system used to connect to the NationStates API.
 * <p>
 * <p>
 * It creates a <code>URLConnection</code> which is used to connect to the API. It then uses allows for error-checking
 * and processing through other methods.
 * </p>
 * <p>
 * <p>
 * This class is utilised by {@link JavaTelegram} to actually send the relevant data to the NationStates API.
 * </p>
 */
public class JTelegramConnection {

	static final int QUEUED = 0;
	static final int UNKNOWN_ERROR = 1;
	static final int REGION_MISMATCH = 2;
	static final int CLIENT_NOT_REGISTERED = 3;
	static final int RATE_LIMIT_EXCEEDED = 4;
	static final int SECRET_KEY_MISMATCH = 5;
	static final int NO_SUCH_TELEGRAM = 6;

	private HttpURLConnection apiConnection;

	/**
	 * Creates and establishes a <code>JTelegramConenction</code> with the relevant codes and keys (Death Cab for
	 * Cutie?). It automatically connects when established.
	 * @param clientKey  is a <code>String</code> which contains the client key
	 * @param secretKey  is a <code>String</code> which contains the secret key
	 * @param telegramId is a <code>String</code> which contains the telegram ID
	 * @param recipient  is a <code>String</code> which contains the name of the recipient in NationStates back-end
	 *                   format (that is, all spaces turned into underscores)
	 * @throws IOException if there is a problem in connecting to the API
	 */
	public JTelegramConnection(String clientKey, String secretKey, String telegramId, String recipient) throws IOException {
		URL tgURL = new URL(NSConnection.API_PREFIX + "a=sendTG&client=" + clientKey + "&key=" + secretKey + "&tgid="
				+ telegramId + "&to=" + recipient);
		apiConnection = (HttpURLConnection) tgURL.openConnection();
		apiConnection.setRequestProperty("User-Agent",
				"NationStates JavaTelegram (maintained by Imperium Anglorum, used by " + clientKey + ")");
		apiConnection.connect();
	}

	/**
	 * Creates and establishes a <code>JTelegramConnection</code> based off of a <code>JTelegramKeys</code> instead of
	 * the discrete codes and keys. It automatically connects when established.
	 * @param keys      is a <code>JTelegramKeys</code> which contains all of the relevant keys
	 * @param recipient is a <code>String</code> which contains name of the recipient in NationStates back-end
	 * @throws IOException if there is a problem in connecting to the API
	 */
	public JTelegramConnection(JTelegramKeys keys, String recipient) throws IOException {
		this(keys.getClientKey(), keys.getSecretKey(), keys.getTelegramId(), recipient);
	}

	/**
	 * Verifies whether the telegram was queued or not.
	 * <p>
	 * There are error codes. If the error code is 0, there is no problem. If the code is 1, then it failed to queue for
	 * an unknown reason. If the code is 2, it failed to code due to a region mismatch between the telegram and the
	 * client key.
	 * </p>
	 * @return an <code>int</code> which contains an error code.
	 * @throws IOException if error in queuing the telegram
	 */
	int verify() throws IOException {

		BufferedReader webReader = apiConnection.getResponseCode() < HttpURLConnection.HTTP_BAD_REQUEST
				? new BufferedReader(new InputStreamReader(apiConnection.getInputStream()))
				: new BufferedReader(new InputStreamReader(apiConnection.getErrorStream()));
		String response = webReader.lines().collect(Collectors.joining("\n"));
		webReader.close();

		if (response.startsWith("queued")) return QUEUED;
		if (response.contains("API Recruitment TG rate-limit exceeded")) return RATE_LIMIT_EXCEEDED;
		if (response.contains("Region mismatch between Telegram and Client API Key")) return REGION_MISMATCH;
		if (response.contains("Client Not Registered For API")) return CLIENT_NOT_REGISTERED;
		if (response.contains("Incorrect Secret Key")) return SECRET_KEY_MISMATCH;
		if (response.contains("No Such API Telegram Template")) return NO_SUCH_TELEGRAM;

		// else, print and return
		Logger.getLogger(this.getClass().getName()).warning(String.format("Unknown error at code (%d):\n%s",
				apiConnection.getResponseCode(), response));
		return UNKNOWN_ERROR;
	}

}
