package com.git.ifly6.misc;

import com.git.ifly6.javatelegram.JTelegramKeys;

public class JTelegramKeysDebug {

	public static void main(String[] args) {
		JTelegramKeys keys = new JTelegramKeys();
		keys.setClientKey("inniemeenie");
		System.err.println(keys.getClientKey());
	}

}
