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

import com.git.ifly6.CommuniqueApplication;
import com.git.ifly6.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Monitor;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.ngui.Communique;
import com.git.ifly6.nsapi.ctelegram.CommSender;
import com.git.ifly6.nsapi.ctelegram.CommSenderInterface;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;


/**
 * Command line program executing {@link Communique} configuration files.
 * @since version 1
 */
public class Marconi implements CommSenderInterface {

    private static final Logger LOGGER = Logger.getLogger(Marconi.class.getName());
    private static final Options COMMAND_LINE_OPTIONS;

    static {
        Options options = new Options();
        options.addOption("h", "help", false, "Displays this message");
        options.addOption("v", "version", false, "Prints version");
        options.addOption("l", "loglevel", true, "Sets logging level");
        COMMAND_LINE_OPTIONS = options;
    }

    private CommuniqueLoader loader;
    private CommuniqueConfig config;
    private CommSender client;

    private Marconi(Path configPath) {
        loader = new CommuniqueLoader(configPath);

        // every marconi instance, on shutdown, should save files
        Runtime.getRuntime().addShutdownHook(
                new Thread(() -> {
                    try {
                        loader.save(this.config);
                    } catch (IOException e) {
                        LOGGER.severe("Could not save updated configuration on shutdown!");
                    }
                })
        );

        try {
            if (Files.exists(configPath)) config = loader.load();
            else throw new FileNotFoundException(String.format("File %s does not exist", configPath));

        } catch (FileNotFoundException e) {
            LOGGER.severe(e.getMessage());

        } catch (IOException e) {
            String ioMessage = "Configuration file at %s; read error.";
            LOGGER.severe(ioMessage);
        }
    }

    public static void main(String[] args) {
        String fileName = CommuniqueApplication.setupLogger(CommuniqueApplication.MARCONI);

        // parse command line options
        try {
            CommandLineParser cliParse = new DefaultParser();
            CommandLine commandLine = cliParse.parse(COMMAND_LINE_OPTIONS, args);
            LOGGER.info(String.format("Initialised with arguments: %s", commandLine.getArgList().toString()));

            // help option
            if (commandLine.hasOption("h") || commandLine.getArgs().length != 1) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp(String.format("java -jar %s COMMUNIQUE_CONFIGURATION_FILE", fileName),
                        "Send telegrams based on Communique configuration files",
                        COMMAND_LINE_OPTIONS,
                        "Report issues to https://github.com/ifly6/Communique.",
                        true);
                System.out.println();
                System.exit(0); // terminate
            }

            // version option
            if (commandLine.hasOption("v")) {
                System.out.println("Marconi version " + Communique7Parser.VERSION + "\n"
                        + "See https://github.com/ifly6/Communique/releases.\n");
                System.exit(0); // terminate
            }

            if (commandLine.hasOption("l")) {
                // by command line, set logger level
                Level level = Level.parse(commandLine.getOptionValue("l"));
                CommuniqueApplication.setLogLevel(level);
                System.out.printf("Set logging level to %s.%n", level);
            }

            Marconi m = new Marconi(Paths.get(commandLine.getArgs()[0]));
            m.send();
        } catch (ParseException e) {
            final String parseErrorMessage = "Cannot parse command arguments. Refer help, accessible with '-h'.";
            LOGGER.severe(parseErrorMessage);
        }
    }

    /**
     * Displays to the user relevant information, awaits user input, sets up client (see {@link CommSender}), and sends
     * telegrams. Also creates a file lock to prevent multiple instances of Marconi from running at the same time.
     */
    private void send() {
        // set up monitor
        // preview recipients
        Communique7Monitor communique7Monitor = new Communique7Monitor(config);
        List<String> expandedRecipients = communique7Monitor.preview();

        // Show the recipients in the order we are to send the telegrams.
        System.out.println();
        System.out.println(MarconiUtilities.twoColumn(expandedRecipients));
        System.out.println();

        System.out.printf(config.repeats
                        ? "Initially %d telegrams will be sent.%n"
                        : "In total %d telegrams will be sent.%n",
                CommuniqueUtilities.time(Math.round(expandedRecipients.size()
                        * (config.getTelegramType().getWaitTime() / (double) 1000))),
                expandedRecipients.size());

        // allow cancel
        System.out.println("You have 3 (three) seconds to cancel.");
        try {
            Thread.sleep(Duration.ofSeconds(3).toMillis());
        } catch (InterruptedException e) {
            System.out.println("Exiting.");
            System.exit(0);
        }

        // Set the client up and go
        client = new CommSender(config.keys, communique7Monitor,
                config.getTelegramType(), this);

        // Check for file lockand send
        if (!MarconiUtilities.isFileLocked()) {
            MarconiUtilities.createFileLock(); // create file lock
            client.startSend();
        } else throw new RuntimeException("Cannot send! Another instance of Marconi is already sending.");

    }

    @Override
    public void processed(String recipient, int numberSent, CommSender.SendingAction action) {
        config.addcRecipient(CommuniqueRecipients.createExcludedNation(recipient));
        if (action == CommSender.SendingAction.SENT)
            System.out.printf("Sent telegram %s to recipient %s%n", numberSent, recipient);

        if (action == CommSender.SendingAction.SKIPPED)
            System.out.printf("Skipped recipient %s%n", recipient);
    }

    @Override
    public void onTerminate() {
        System.out.println("Sending thread terminated");
        List<String> items = new ArrayList<>(client.getSentList());

        System.out.println("Sent telegrams to following nations:");
        System.out.println("\n" + MarconiUtilities.twoColumn(items) + "\n");

        System.out.printf("Sent %d telegrams%n", items.size());

        System.out.println();
        System.out.printf("Sending thread elapsed: %s",
                CommuniqueUtilities.time(
                        Duration.between(client.getInitAt(), Instant.now()).getSeconds()));

        System.exit(0);
    }

    @Override
    public void onError(String m, Throwable e) {
        LOGGER.log(Level.SEVERE, m, e);
    }
}
