package com.git.ifly6.marconi;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import com.git.ifly6.javatelegram.JTelegramLogger;

public class MarconiLogger implements JTelegramLogger {

	@Override
	public void log(String output) {
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		System.out.println("[" + dateFormat.format(date) + "] " + output);
	}

	public void output(String output) {
		System.out.println(output);
	}

}
