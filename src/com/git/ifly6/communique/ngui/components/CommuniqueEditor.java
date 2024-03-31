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

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueFilterType;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.io.CommuniqueConfig;
import com.git.ifly6.communique.io.CommuniqueLoader;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.communique.io.CommuniqueScraper;
import com.git.ifly6.communique.ngui.AbstractCommunique;
import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.NSException;
import com.git.ifly6.nsapi.telegram.JTelegramKeys;
import com.git.ifly6.nsapi.telegram.JTelegramType;

import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.FileDialog;
import java.awt.GridBagLayout;
import java.awt.Point;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.function.Function;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.git.ifly6.communique.ngui.components.CommuniqueConstants.CODE_HEADER;
import static com.git.ifly6.communique.ngui.components.CommuniqueConstants.COMMAND_KEY;
import static com.git.ifly6.communique.ngui.components.CommuniqueFactory.createMenuItem;
import static com.git.ifly6.communique.ngui.components.CommuniqueFileChoosers.show;

public class CommuniqueEditor extends AbstractCommunique {

    public static final ArrayList<CommuniqueEditor> INSTANCES = new ArrayList<>();
    private static final Logger LOGGER = Logger.getLogger(CommuniqueEditor.class.getName());

    private Path path;
    private JFrame frame;

    private CommuniqueScrollableTextArea area;
    private JTextField fieldClientKey;
    private JTextField fieldSecretKey;
    private JTextField fieldTelegramID;

    private CommuniqueDurationField textTelegramDelay;
    private JComboBox<CommuniqueProcessingAction> fieldAction;
    private JComboBox<JTelegramType> fieldType;

    private JCheckBox checkboxRepeat;
    private CommuniqueDurationField textRepeatDelay;

    private CommuniqueConfig config; // initialise a new configuration on nothing

    CommuniqueEditor(Path path) {
        this.path = path;
        frame = new JFrame(constructTitle());
        frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        frame.setMinimumSize(new Dimension(725, 400));

        // if there is a file try to load it
        config = new CommuniqueConfig();
        try {
            config = load();
        } catch (IOException e) {
            return;  // abort construction
        }

        // set up default location
        Point startingLocation = new Point(50 + 400, 50 + 50); // default location
        for (int i = 0; i < 10; i++) {
            Point hypothetical = new Point(startingLocation.x + 50 * i, startingLocation.y + 50 * i);
            if (!CommuniqueEditorManager.getInstance().isLocationUsed(hypothetical)) {
                frame.setLocation(hypothetical);
                break;
            }
        }
        frame.setSize(new Dimension(800, 600));

        // initialise content pane components
        initialise();

        // initialise menu bar
        initialiseMenuBar();

        // finalise
        synchronise(config);
        frame.setVisible(true);

        // register the editor with yourself and clean up inactive editors
        INSTANCES.removeIf(editor -> !editor.active());
        INSTANCES.add(this);
    }

    private String constructTitle() {
        return String.format("Communiqué %d – %s", Communique7Parser.VERSION,
                path.getFileName().toString().replaceFirst("\\..+$", ""));
    }

    private void initialise() {
        // fields
        CommuniqueDelayedDocumentListener saveListener = new CommuniqueDelayedDocumentListener(i -> this.save());
        area = new CommuniqueScrollableTextArea(CommuniqueFactory.createArea("", saveListener));
        fieldClientKey = CommuniqueFactory.createField("CLIENT_KEY", "Client key", saveListener);
        fieldSecretKey = CommuniqueFactory.createField("SECRET_KEY", "Secret key", saveListener);
        fieldTelegramID = CommuniqueFactory.createField("TELEGRAM_ID", "Telegram ID", saveListener);

        // create components for the action type, telegram type, etc
        CommuniqueDelayedActionListener delayedSave = new CommuniqueDelayedActionListener(e -> this.save());
        fieldAction = new JComboBox<>(CommuniqueProcessingAction.values());
        fieldAction.setSelectedItem(CommuniqueProcessingAction.NONE);
        fieldAction.setToolTipText(
                "Processing actions can be applied to the list of recipients after they " + "are " + "parsed. Select "
                        + "a processing action here");
        fieldAction.addActionListener(delayedSave);

        fieldType = new JComboBox<>(JTelegramType.values());
        fieldType.setSelectedItem(JTelegramType.RECRUIT); // default to recruitment
        fieldType.setToolTipText("Telegram types are declared site-side in the telegram sent to \"tag:api\"");
        fieldType.addActionListener(delayedSave);
        fieldType.addActionListener(ae -> { // force default delays when not selecting custom
            JTelegramType telegramType = CommuniqueSwingUtilities.getSelected(fieldType);
            if (!telegramType.equals(JTelegramType.CUSTOM)) textTelegramDelay.setText("");
        });

        textTelegramDelay = new CommuniqueDurationField(ChronoUnit.MILLIS, "Time between telegrams in seconds",
                saveListener);
        textTelegramDelay.addActionListener(
                ae -> JTelegramType.CUSTOM.setWaitDuration(getTelegramInterval())); // must have this to sync

        LinkedHashMap<String, Component> southComponents = new LinkedHashMap<>();
        southComponents.put("Post-processing action", fieldAction);
        southComponents.put("Telegram type", fieldType);
        southComponents.put("Telegram delay (s)", textTelegramDelay);

        // create components for the repeat panel
        checkboxRepeat = new JCheckBox("", false);
        checkboxRepeat.addActionListener(delayedSave);
        textRepeatDelay = new CommuniqueDurationField(ChronoUnit.SECONDS,
                "Time between client initiation repeat", saveListener);

        LinkedHashMap<String, Component> repeatComponents = new LinkedHashMap<>();
        repeatComponents.put("Repeat", checkboxRepeat);
        repeatComponents.put("Repeat delay (s)", textRepeatDelay);

        // frame layout
        JPanel contentPane = new JPanel();
        contentPane.setLayout(new BorderLayout(5, 5));
        contentPane.add(area, BorderLayout.CENTER);
        contentPane.setBorder(CommuniqueFactory.createBorder(5));
        frame.setContentPane(contentPane);

        // content pane layout
        JPanel sidebarFrame = new JPanel();
        sidebarFrame.setLayout(new BorderLayout(5, 5));

        JPanel northFrame = new JPanel();
        northFrame.setBorder(CommuniqueFactory.createTitledBorder("Telegram keys"));
        CommuniqueSwingUtilities.addComponents(
                northFrame,
                Stream.of(fieldClientKey, fieldSecretKey, fieldTelegramID)
                        .collect(Collectors.toMap(
                                JTextField::getToolTipText,
                                Function.identity(),
                                (x, y) -> { throw new IllegalStateException(); }, LinkedHashMap::new
                        ))
        );
        sidebarFrame.add(northFrame, BorderLayout.NORTH);

        JPanel panelRepeat = new JPanel();
        panelRepeat.setBorder(CommuniqueFactory.createTitledBorder("Repeat settings"));
        CommuniqueSwingUtilities.addComponents(panelRepeat, repeatComponents);

        JPanel panelHandling = new JPanel();
        panelHandling.setBorder(CommuniqueFactory.createTitledBorder("Message handling"));
        CommuniqueSwingUtilities.addComponents(panelHandling, southComponents);

        JPanel superSouthPanel = new JPanel();
        superSouthPanel.setLayout(new GridBagLayout());
        superSouthPanel.add(panelRepeat, CommuniqueFactory.createGridBagConstraints(0, 0, true));
        superSouthPanel.add(panelHandling, CommuniqueFactory.createGridBagConstraints(0, 1, true));
        sidebarFrame.add(superSouthPanel, BorderLayout.SOUTH);

        contentPane.add(sidebarFrame, BorderLayout.EAST);
    }

    private void initialiseMenuBar() {
        // initialise menubar
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // file
        this.addFileMenu(List.of(createMenuItem("Save", KeyEvent.VK_S, ae -> this.save()), createMenuItem("Save as",
                KeyStroke.getKeyStroke(KeyEvent.VK_S, COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK), ae -> {
                    Path newPath = CommuniqueFileChoosers.show(this.frame, FileDialog.SAVE);
                    if (newPath == null) {
                        LOGGER.info("\"Save as\" cancelled");
                        return;
                    }
                    saveAs(path);
                })));

        // edit
        JMenu mnEdit = this.addEditMenu();
        mnEdit.addSeparator();

        mnEdit.add(createMenuItem("Import keys from telegram URL", ae -> {
            String rawURL = this.showInputDialog(
                    "Paste in keys from the URL provided by receipt by the " + "Telegrams API");

            // verify that it is a valid NationStates URL
            String raw1 = "https://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&";
            String raw2 = "http://www.nationstates.net/cgi-bin/api.cgi?a=sendTG&client=YOUR_API_CLIENT_KEY&";
            if (rawURL.startsWith(raw1) || rawURL.startsWith(raw2)) {

                rawURL = rawURL.substring(rawURL.indexOf(
                        "a=sendTG&client=YOUR_API_CLIENT_KEY&") + ("a=sendTG&client" + "=YOUR_API_CLIENT_KEY"
                        + "&").length()); // use substring
                rawURL = rawURL.replace("&to=NATION_NAME", "");

                String[] shards = rawURL.split("&");
                if (shards.length == 2) {
                    String secretKey = shards[1].substring(shards[1].indexOf("=") + "=".length());
                    this.setSecretKey(secretKey);

                    String telegramID = shards[0].substring(shards[0].indexOf("=") + "=".length());
                    this.setTelegramID(telegramID);
                }

            } else this.showErrorDialog(
                    "Input a properly formatted NationStates URL in the form displayed " + "when a " + "telegram " +
                            "is sent to 'tag:api'");
        }));

        JMenu mnImportRecipients = new JMenu("Import recipients");
        mnEdit.add(mnImportRecipients);
        mnImportRecipients.add(
                createMenuItem("From WA Delegate List", ae -> this.appendLine(CommuniqueRecipient.DELEGATES)));
        mnImportRecipients.add(createMenuItem("As Comma Separated List", ae -> {
            String message = "Input a string of delegates, as found on a list of delegates\nin one of the " +
                    "NationStates World Assembly pages:";
            String input = this.showInputDialog(message);
            if (input != null) {
                input = input.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
                Arrays.stream(input.split(",\\s*?")).map(CommuniqueRecipients::createNation) // createNation auto-trims
                        .map(CommuniqueRecipient::toString).forEach(this::appendLine);
            }
        }));
        mnImportRecipients.add(createMenuItem("Delegates from at vote screen", ae -> {
            Object[] possibilities = { "GA for", "GA against", "SC for", "SC against" };
            String selection = (String) JOptionPane.showInputDialog(frame,
                    "Select which chamber and side you want " + "to" + " address:", "Select Chamber and Side",
                    JOptionPane.PLAIN_MESSAGE, null, possibilities, "GA For");

            if (!selection.isBlank()) {
                LOGGER.info("Starting scrape of NS WA voting page, " + selection);
                String[] elements = selection.toLowerCase().split("\\s+?");
                String chamber = elements[0].equals("ga") ? CommuniqueScraper.GA : CommuniqueScraper.SC;
                String side = elements[1].equals("for") ? CommuniqueScraper.FOR : CommuniqueScraper.AGAINST;
                try {
                    CommuniqueScraper.importAtVoteDelegates(chamber, side).stream().map(CommuniqueRecipient::toString)
                            .forEach(this::appendLine);

                } catch (NSException nre) {
                    this.showErrorDialog("No resolution is at vote in that chamber");

                } catch (RuntimeException exc) {
                    LOGGER.log(Level.WARNING, "Cannot import data.", exc);
                    this.showErrorDialog("Cannot import data from NationStates website");
                }
            }
        }));
        mnImportRecipients.add(createMenuItem("From text file", event -> {
            Path path = show(frame, FileDialog.LOAD);
            if (path != null) {
                try (Stream<String> lines = Files.lines(path)) {
                    lines.filter(s -> !s.startsWith("#")).filter(ApiUtils::isNotEmpty).map(ApiUtils::ref) // process
                            .forEach(this::appendLine); // append to text area
                } catch (IOException e1) {
                    LOGGER.log(Level.WARNING, "Cannot read file, IOException", e1);
                    this.showErrorDialog(String.format("Cannot read file at %s", path));
                }
            }
        }));

        mnEdit.addSeparator();
        mnEdit.add(createMenuItem("Exclude nations", ae -> {
            String message = "Input nations to exclude as comma-separated list (Do not include trailing 'and'.)";
            String input = this.showInputDialog(message);
            if (input != null) {
                input = input.replaceAll("\\(.+?\\)", ""); // get rid of brackets and anything in them
                Arrays.stream(input.split(","))
                        .map(n -> CommuniqueRecipients.createNation(CommuniqueFilterType.EXCLUDE, n))
                        .map(CommuniqueRecipient::toString).forEach(this::appendLine);
            }
        }));

        this.addWindowMenu();
        this.addHelpMenu();
    }

    public CommuniqueConfig getConfig() {
        config = new CommuniqueConfig(
                new JTelegramKeys(fieldClientKey.getText(), fieldSecretKey.getText(), fieldTelegramID.getText()),
                CommuniqueSwingUtilities.getSelected(fieldType), textTelegramDelay.getDuration(),
                CommuniqueSwingUtilities.getSelected(fieldAction),
                checkboxRepeat.isSelected(), textRepeatDelay.getDuration());
        config.setcRecipients(area.getLines().stream()
                .filter(ApiUtils::isNotEmpty)
                .filter(s -> !s.startsWith("#"))
                .map(CommuniqueRecipient::parseRecipient)
                .collect(Collectors.toList()));
        return config;
    }

    public void save() {
        Runnable r = () -> {
            try {
                CommuniqueLoader loader = new CommuniqueLoader(path);
                loader.save(getConfig());
            } catch (IOException e) {
                showWarning(String.format("Failed to save configuration to file at %s", path), e);
            }
        };
        r.run();
    }

    private CommuniqueConfig load() throws IOException {
        CommuniqueLoader loader = new CommuniqueLoader(path);
        if (Files.exists(path)) {
            try {
                return loader.load();

            } catch (IOException e) {
                showWarning(String.format("Failed to read configuration file at %s", path), e);
                EventQueue.invokeLater(() -> {
                    frame.setVisible(false);
                    frame.dispose();
                });
                throw e;
            }
        }

        return new CommuniqueConfig();
    }

    private void saveAs(Path newPath) {
        LOGGER.info(String.format("Saving file %s as %s", this.path.getFileName(), newPath));
        this.path = newPath;
        save();
        frame.setTitle(constructTitle());
    }

    public void synchronise(CommuniqueConfig theConfig) {
        config = theConfig;
        this.setClientKey(config.keys.getClientKey());
        this.setSecretKey(config.keys.getSecretKey());
        this.setTelegramID(config.keys.getTelegramID());
        fieldType.setSelectedItem(config.getTelegramType());
        fieldAction.setSelectedItem(config.getProcessingAction());
        textTelegramDelay.setText(String.valueOf(config.getTelegramInterval().getSeconds()));
        area.setText(CODE_HEADER + String.join("\n", config.getcRecipientsString()));
    }

    private void showWarning(String text, Throwable e) {
        JOptionPane.showMessageDialog(frame, text, "Warning!", JOptionPane.PLAIN_MESSAGE, null);
        if (e != null) LOGGER.log(Level.WARNING, text, e);
    }

    public boolean active() {
        return frame.isDisplayable();
    }

    /**
     * The {@link Path} at which the editor is pointed
     * @return that path
     */
    public Path getPath() {
        return path;
    }

    public Point getLocation() {
        return frame.getLocation();
    }

    @Override
    public String toString() {
        String fileName = path.getFileName().toString().replaceFirst("\\..+$", "");
        return fileName.equalsIgnoreCase("autosave") ? "Autosave" : fileName;
    }

    public void appendLine(Object obj) {
        area.appendLine(obj.toString());
    }

    public Duration getTelegramInterval() {
        return getConfig().getTelegramInterval();
    }

    public JTelegramType getTelegramType() {
        getConfig();
        return config.getTelegramType();
    }

    public void setClientKey(String s) {
        fieldClientKey.setText(s);
    }

    public void setSecretKey(String s) {
        fieldSecretKey.setText(s);
    }

    public void setTelegramID(String s) {
        fieldTelegramID.setText(s);
    }

    public void toFront() {
        frame.toFront();
    }

}

