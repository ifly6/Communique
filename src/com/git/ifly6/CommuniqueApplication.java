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

package com.git.ifly6;

import com.formdev.flatlaf.FlatLightLaf;
import com.formdev.flatlaf.themes.FlatMacLightLaf;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.marconi.Marconi;
import org.apache.commons.text.WordUtils;

import javax.swing.ImageIcon;
import javax.swing.UIManager;
import java.awt.Taskbar;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.MessageFormat;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Objects;
import java.util.function.Supplier;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;
import java.util.zip.GZIPOutputStream;

import static com.git.ifly6.CommuniqueUtilities.NO_COLONS;
import static java.nio.file.StandardOpenOption.CREATE_NEW;

/**
 * Enumerates Communique-based applications.
 * @since version 3.0 (build 13)
 */
public enum CommuniqueApplication {

    COMMUNIQUE("Communiqué", "communique"), MARCONI("Marconi", "marconi");

    private static final Logger LOGGER = Logger.getLogger(CommuniqueApplication.class.getName());
    public final String displayName;
    public final String ref;

    CommuniqueApplication(String displayName, String ref) {
        this.displayName = displayName;
        this.ref = ref;
    }

    /**
     * Generates display name; includes build variable if {@code includeBuild}.
     * @return names like {@code Communiqué 2.5 (build 12)}
     * @since version 3.0 (build 13)
     */
    public String generateName() {
        return MessageFormat.format("{0} {1}", displayName, version());
    }

    /**
     * @return {@link Communique7Parser#VERSION} string
     */
    public int version() {
        return Communique7Parser.VERSION;
    }

    public static final Path APP_SUPPORT = ((Supplier<Path>) () -> { // determine where the APP_SUPPORT location is
        if (CommuniqueUtilities.IS_OS_WINDOWS)
            return Paths.get(System.getenv("LOCALAPPDATA"), "Communique");
        if (CommuniqueUtilities.IS_OS_MAC)
            return Paths.get(System.getProperty("user.home"), "Library", "Application Support", "Communique");

        return Paths.get(System.getProperty("user.home"), ".communique");
    }).get();

    static {
        // try get or create app support directory if not existent
        try {
            Files.createDirectories(APP_SUPPORT);
        } catch (IOException e1) {
            LOGGER.log(Level.WARNING, "Cannot get or create directory", e1);
        }
    }

    /**
     * Nativises for Mac by putting name in screen menu bar.
     * @since version 1 (setting macOS properties); version 13 (task bar image)
     */
    public static void nativise(CommuniqueApplication app) {
        if (CommuniqueUtilities.IS_OS_MAC) {
            System.setProperty("apple.laf.useScreenMenuBar", "true");
            System.setProperty("com.apple.mrj.application.apple.menu.about.name", app.generateName());
        }

        // 2024-03-30
        // JDK 9 set taskbar image
        try {
            Taskbar taskbar = Taskbar.getTaskbar();
            taskbar.setIconImage(new ImageIcon(
                    Objects.requireNonNull(CommuniqueApplication.class.getResource("/icon.png"))).getImage());

        } catch (UnsupportedOperationException e) {
            LOGGER.log(Level.WARNING, "Cannot set taskbar icon because it is not supported", e);

        } catch (SecurityException e) {
            LOGGER.log(Level.WARNING, "Cannot set taskbar icon due to security exception", e);
        }

    }

    /**
     * Handles logging format, setting up default exception handler, and gets name of the application JAR.
     * @param app starting up
     * @return name of the application's JAR file
     * @since version 13
     */
    public static String setupLogger(CommuniqueApplication app) {
        // better log format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

        try {
            // Add in a logging file handler
            Path logLocation = APP_SUPPORT.resolve("log").resolve(String.format(
                    "%s-session-%s.log", app.ref, CommuniqueUtilities.getTime(NO_COLONS)));
            Files.createDirectories(logLocation.getParent()); // make sure logging folder exists

            FileHandler handler = new FileHandler(logLocation.toString());
            handler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(handler); // empty string gets root logger

        } catch (SecurityException | IOException e) {
            LOGGER.log(Level.WARNING, "Failed to set up logging file handler", e);
        }

        // set up default exception handler with root logger
        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            Logger.getLogger("").log(Level.SEVERE, String.format("Exception in %s: %s", t, e.toString()), e);
        });

        // log start up
        LOGGER.info(String.format("Started up %s", app.generateName()));

        // get jar name
        return getJARName(app);
    }

    /**
     * Sets logging level for root logger to provided level
     * @param level to set
     */
    public static void setLogLevel(Level level) {
        Logger rootLogger = Logger.getLogger("");
        rootLogger.setLevel(level);

        Handler[] handlers = rootLogger.getHandlers();
        if (handlers.length == 0) LOGGER.severe("No handlers for root logger!");

        for (Handler handler : handlers)
            handler.setLevel(level);
    }

    /**
     * Sets system look and feel; if not available, sets Nimbus.
     */
    public static void setLAF() {
        // Set our look and feel
        try {
            if (com.git.ifly6.CommuniqueUtilities.IS_OS_MAC) FlatMacLightLaf.setup();
            else FlatLightLaf.setup();

        } catch (Exception lfe) {
            LOGGER.log(Level.WARNING, "Failed to load FlatLAF GUI theme. Falling back on system theme.", lfe);
            try {
                UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Could not initialise system look and feel", e);
            }
        }
    }

    /**
     * @return name of the starting executable.
     */
    private static String getJARName(CommuniqueApplication app) {
        try {
            return Paths.get(Marconi.class.getProtectionDomain().getCodeSource().getLocation().toURI())
                    .getFileName().toString();
        } catch (URISyntaxException e) {
            // default to standard naming format.
            return String.format("%s_%d.jar", WordUtils.capitalize(app.ref), Communique7Parser.VERSION);
        }
    }

    /**
     * Compresses logs in the application support {@code log} sub-folder if they are older than a day.
     */
    public static void compressLogs() {
        try {
            final Instant yesterday = Instant.now().minus(1, ChronoUnit.DAYS);
            DirectoryStream<Path> stream = Files.newDirectoryStream(APP_SUPPORT.resolve("log"), "*.log");
            for (Path p : stream) {
                Instant fileModified = Files.getLastModifiedTime(p).toInstant();
                boolean earlier = fileModified.compareTo(yesterday) < 0;
                if (earlier) {
                    // write new compressed file
                    Path newPath = p.resolveSibling(p.getFileName() + ".gz");
                    GZIPOutputStream zipStream = new GZIPOutputStream(Files.newOutputStream(newPath, CREATE_NEW));
                    zipStream.write(Files.readAllBytes(p));
                    zipStream.close();
                    Files.deleteIfExists(p); // delete old file if present
                }
            }
        } catch (IOException e) {
            LOGGER.log(Level.INFO, "Encountered exception in compression task", e);
        }
    }

}
