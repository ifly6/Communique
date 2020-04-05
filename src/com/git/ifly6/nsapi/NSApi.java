package com.git.ifly6.nsapi;

public class NSApi {

	private NSApi() {
	}

	public static String ref(String input) {
		return input.trim().toLowerCase().replace(" ", "_");
	}

}
