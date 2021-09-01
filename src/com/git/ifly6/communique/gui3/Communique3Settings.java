/*
 * Copyright (c) 2021 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this class file and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of
 * the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS
 * OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE.
 */

package com.git.ifly6.communique.gui3;

import com.git.ifly6.communique.io.CommuniqueConfig;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;

import static com.git.ifly6.commons.CommuniqueApplication.APP_SUPPORT;

/**
 * Holds settings information for Communique3. Class should be started at initialisation.
 * @since version 3.0 (build 13)
 */
public class Communique3Settings {

    private transient static final Path SETTINGS_PATH = APP_SUPPORT.resolve("client_settings.properties");
    private transient static Communique3Settings instance;

    private File lastSavedPath; // muse use file, path doesn't serialise properly
    private Level loggingLevel;

    private Communique3Settings() {
        lastSavedPath = Paths.get(System.getProperty("user.home")).toFile();
        loggingLevel = Level.INFO;
    }

    public static Communique3Settings getInstance() {
        if (instance == null) instance = Communique3Settings.load();
        return instance;
    }

    public Path getLastSavedPath() {
        return lastSavedPath.toPath();
    }

    public void setLastSavedPath(Path lastSavedPath) {
        this.lastSavedPath = lastSavedPath.toFile();
    }

    public Level getLoggingLevel() {
        return loggingLevel;
    }

    public void setLoggingLevel(Level loggingLevel) {
        this.loggingLevel = loggingLevel;
    }

    public void save() throws IOException {
        BufferedWriter bw = Files.newBufferedWriter(SETTINGS_PATH);
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        gson.toJson(this, bw);
        bw.close();
    }

    /**
     * Loads settings from file if present, puts defaults if not, but substitutes client key from {@link
     * CommuniqueConfig}.
     * @return settings
     */
    public static Communique3Settings load() {
        try {
            Gson gson = new Gson();
            return gson.fromJson(Files.newBufferedReader(SETTINGS_PATH), Communique3Settings.class);
        } catch (IOException e) {
            return new Communique3Settings();
        }
    }
}
