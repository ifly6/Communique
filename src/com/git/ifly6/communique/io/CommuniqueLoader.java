/* Copyright (c) 2016 Kevin Wong. All Rights Reserved. */
package com.git.ifly6.communique.io;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Path;
import java.util.Properties;

import com.git.ifly6.communique.CommuniqueUtils;
import com.git.ifly6.communique.ngui.Communique;
import com.git.ifly6.javatelegram.util.JTelegramException;

/** <code>CLoader</code> is a class allowing the easy abstraction of access to a single point and simplifying the
 * process of reading and writing to that data. It uses the <code>{@link CommuniqueReader}</code> and
 * <code>{@link CommuniqueWriter}</code> classes to access that data. They are based on reading and writing via use of
 * Google's Gson library, which then allow for reading, writing, and manipulation of a
 * {@link com.git.ifly6.communique.io.CommuniqueConfig CConfig} object. */
public class CommuniqueLoader {
	
	Path path;
	
	// Force initialisation with appropriate variables
	@SuppressWarnings(value = { "unused" }) private CommuniqueLoader() {
	}
	
	/** Creates the <code>CLoader</code> and sets the path at which the program will do its file operations. The
	 * Communique program attempts to default this to the relevant application support folder, resolved to the
	 * Communique folder, by specifying such in the program's myriad file dialog prompts.
	 * @param path */
	public CommuniqueLoader(Path path) {
		this.path = path;
	}
	
	/** Saves a configuration file based on the provided <code>CCoNfig</code>.
	 * @param config
	 * @throws IOException */
	public void save(CommuniqueConfig config) throws IOException {
		CommuniqueWriter writer = new CommuniqueWriter(path, config);
		writer.write();
	}
	
	/** Loads a configuration file to a new CConfig.
	 * @return a <code>CConfig</code> based on the loaded data from disc
	 * @throws IOException */
	public CommuniqueConfig load() throws IOException {
		CommuniqueReader reader = new CommuniqueReader(path);
		return reader.read();
	}
	
	/** Writes the standard configuration file for the currently used client key. Properties writing here has been
	 * localised for this setup using this method.
	 * @throws IOException */
	public static void writeProperties(String clientKey) {
		try {
			Properties prop = new Properties();
			prop.setProperty("clientKey", clientKey);
			FileOutputStream output = new FileOutputStream(Communique.appSupport.resolve("config.properties").toFile());
			prop.store(output, "Communique Properties");
			output.close();
		} catch (IOException e) {
			throw new JTelegramException("Cannot write Communique properties", e);
		}
	}
	
	/** Returns the last used client key from the configuration file.
	 * @return the client key from file
	 * @throws IOException if there was a problem in reading or finding the configuration file */
	public static String getClientKey() {
		try {
			Properties prop = new Properties();
			prop.load(new FileInputStream(Communique.appSupport.resolve("config.properties").toFile()));
			String clientKey = prop.getProperty("clientKey");
			return CommuniqueUtils.isEmpty(clientKey) ? "Client Key" : clientKey;
		} catch (IOException e) {
			return "Client Key";
		}
	}
}