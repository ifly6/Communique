/*
 * Copyright (c) 2024 ifly6
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

package com.git.ifly6.communique.ngui.components;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.Taskbar;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.GZIPOutputStream;

import static java.nio.file.StandardOpenOption.CREATE_NEW;

public class CommuniqueLAF {

    private static final Logger LOGGER = Logger.getLogger(CommuniqueLAF.class.getName());
    public static Path APP_SUPPORT;
    public static FileHandler loggerFileHandler = null; // Save logs to file, if null... uh stuff

    static {
        // Do this static initialisation block when LAF is called
        // Find or create the application support directory
        if (CommuniqueUtilities.IS_OS_WINDOWS)
            APP_SUPPORT = Paths.get(System.getenv("LOCALAPPDATA"), "Communique");

        else if (CommuniqueUtilities.IS_OS_MAC) {
            APP_SUPPORT = Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Communique");
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", "Communiqué " + Communique7Parser.version);
            System.setProperty("apple.awt.application.name", "Communiqué"); // dock
//            System.setProperty("apple.awt.application.appearance", "system"); // macOS dark and light theme

        } else APP_SUPPORT = Paths.get(System.getProperty("user.home"), ".communique");

        // Create the application support directory
        try {
            Files.createDirectories(APP_SUPPORT);
        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Cannot create directory", e);
        }

        // Get us a reasonable-looking log format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

        // Make sure we can also log to file, apply this to the root logger
        try {
            Path logFile = APP_SUPPORT
                    .resolve("log")
                    .resolve(String.format("communique-session-%s.log", CommuniqueUtilities.getTime()));

            Files.createDirectories(logFile.getParent()); // make directory
            loggerFileHandler = new FileHandler(logFile.toString());
            loggerFileHandler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(loggerFileHandler);

        } catch (SecurityException | IOException | NullPointerException e) {
            LOGGER.log(Level.SEVERE, "Could not set up logging", e);
        }
    }

    public static void setLAF() {
        // Set our look and feel
        try {
            if (CommuniqueUtilities.IS_OS_MAC) FlatMacLightLaf.setup();
            else FlatLightLaf.setup();

        } catch (Exception lfe) {
            LOGGER.log(Level.WARNING, "Failed to load FlatLAF GUI theme", lfe);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not initialise system look and feel", e);
            }
        }

        // 2024-03-30
        // JDK 9 set taskbar image
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(new ImageIcon(
                    Objects.requireNonNull(CommuniqueLAF.class.getResource("/icon.png"))).getImage());

        } catch (UnsupportedOperationException e) {
            LOGGER.log(Level.WARNING, "Cannot set taskbar icon because it is not supported", e);

        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Cannot set taskbar icon due to security exception", e);
        }
    }

    /**
     * Compresses logs present in the application support <code>log</code> sub-folder if they are older than a day. Uses
     * G-ZIP compression. Should be called on execution.
     */
    public static void compressLogs() {
        try {
            DirectoryStream<Path> stream = Files.newDirectoryStream(APP_SUPPORT.resolve("log"), "*.log");
            final Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
            for (Path p : stream) {
                try {
                    Instant fileModified = Files.getLastModifiedTime(p).toInstant();
                    boolean earlier = fileModified.compareTo(yesterday) < 0;
                    if (earlier) {
                        // write new compressed file
                        Path newPath = p.resolveSibling(p.getFileName() + ".gz");
                        GZIPOutputStream zipStream = new GZIPOutputStream(Files.newOutputStream(newPath, CREATE_NEW));
                        zipStream.write(Files.readAllBytes(p));
                        zipStream.close();

                        // delete old file
                        Files.deleteIfExists(p);
                    }
                } catch (IOException e) {
                    LOGGER.log(Level.WARNING, String.format("Failed to compress %s", p), e);
                    break; // better safe than sorry
                }
            }

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Unclear IO exception", e);
        }
    }

}
