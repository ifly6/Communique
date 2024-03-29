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

package com.git.ifly6.marconi;

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.FileHandler;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

/**
 * @author ifly6
 */
public class MarconiLauncher {

    private static final Logger LOGGER = Logger.getLogger(MarconiLauncher.class.getName());

    // Deal with command line options
    private static final Options COMMAND_LINE_OPTIONS;
    private static boolean recruiting = false;

    static {
        Options options = new Options();
        options.addOption("h", "help", false, "Displays this message");
        options.addOption("R", false, "Uses input data as configuration to call a recruiter that sends infinitely");
        options.addOption("v", "version", false, "Prints version");

        COMMAND_LINE_OPTIONS = options;
    }

    public static void main(String[] args) {

        // Get us a reasonable-looking log format
        System.setProperty("java.util.logging.SimpleFormatter.format", "%1$tF %1$tT %4$s %2$s %5$s%6$s%n");

        // Add in a logging file handler
        try {
            // Directory is defined as the same directory in which Marconi is run
            Path logLocation = Paths.get(String.format("marconi-session-%s.log",
                    CommuniqueUtilities.getTime()));
            FileHandler handler = new FileHandler(logLocation.toString());
            handler.setFormatter(new SimpleFormatter());
            Logger.getLogger("").addHandler(handler); // gets the root logger

        } catch (SecurityException | IOException e) {
            e.printStackTrace();
        }

        // Get the file name
        String fileName;
        try {
            URI u = MarconiLauncher.class.getProtectionDomain().getCodeSource().getLocation().toURI();
            fileName = new File(u).getName();
        } catch (URISyntaxException | RuntimeException e1) {
            fileName = "Marconi_" + Communique7Parser.version + ".jar"; // default to standard naming format.
        }

        Thread.setDefaultUncaughtExceptionHandler((Thread t, Throwable e) -> {
            e.printStackTrace();
            LOGGER.log(Level.SEVERE, "Exception in " + t + ": " + e.toString(), e);
        });

        CommandLineParser cliParse = new DefaultParser();

        try {
            CommandLine commandLine = cliParse.parse(COMMAND_LINE_OPTIONS, args);

            // Deal with options
            if (commandLine.hasOption("h") || commandLine.getArgs().length != 1) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(String.format("java -jar %s COMMUNIQUE_CONFIGURATION_FILE", fileName),
                        "Send telegrams on NationStates from the command line",
                        COMMAND_LINE_OPTIONS,
                        "Please report issues to the NationStates nation Imperium Anglorum via telegram or to "
                                + "https://forum.nationstates.net/viewtopic.php?f=15&t=352065.",
                        true);
                System.out.println();
                return;
            }
            if (commandLine.hasOption("R")) recruiting = true;
            if (commandLine.hasOption("v")) {
                System.out.println("Marconi version " + Communique7Parser.version + "\n"
                        + "Please visit https://github.com/iFlyCode/Communique/releases.\n");
                return; // exit
            }

            Path configPath = Paths.get(commandLine.getArgs()[0]);
            initSend(configPath);

        } catch (ParseException e) {
            LOGGER.severe("Please refer to the help, accessible using '-h'\n");
            e.printStackTrace();

        } catch (IOException e) {
            LOGGER.severe("Please provide a valid or existing file argument to the program.\n");
            e.printStackTrace();
        }
    }

    private static void initSend(Path configPath) throws IOException {

        Marconi marconi = new Marconi(recruiting);
        marconi.load(configPath);

        // If there is a recruit flag, set it to true
        boolean recruiting = marconi.exportState().getcRecipients()
                .contains(CommuniqueRecipients.createFlag("recruit"));

        // Add shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            try {
                LOGGER.info("Attempting to save to:" + configPath.normalize());    // save config
                marconi.save(configPath);
                if (Files.deleteIfExists(MarconiUtilities.lockFile))
                    LOGGER.info("Removed file lock");    // remove file lock, if it exists
            } catch (IOException e) {
                e.printStackTrace();
            }
        }));

        if (recruiting) {
            MarconiRecruiter recruiter = new MarconiRecruiter(marconi);
            recruiter.setConfig(marconi.exportState());
            recruiter.send();
            // Indefinite ending point, use ShutdownHook to save

        } else {
            marconi.send();
            marconi.save(configPath);

        }
    }
}
