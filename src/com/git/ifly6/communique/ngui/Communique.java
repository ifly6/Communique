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

package com.git.ifly6.communique.ngui;

import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipient;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.data.CommuniqueRecipientType;
import com.git.ifly6.communique.io.CommuniqueProcessingAction;
import com.git.ifly6.communique.ngui.components.CommuniqueConstants;
import com.git.ifly6.communique.ngui.components.CommuniqueEditor;
import com.git.ifly6.communique.ngui.components.CommuniqueEditorManager;
import com.git.ifly6.communique.ngui.components.CommuniqueFactory;
import com.git.ifly6.communique.ngui.components.CommuniqueLAF;
import com.git.ifly6.communique.ngui.components.CommuniqueLogHandler;
import com.git.ifly6.communique.ngui.components.CommuniqueLogViewer;
import com.git.ifly6.communique.ngui.components.CommuniqueTimerBar;
import com.git.ifly6.nsapi.ApiUtils;
import com.git.ifly6.nsapi.telegram.JTelegramException;
import com.git.ifly6.nsapi.telegram.JTelegramLogger;
import com.git.ifly6.nsapi.telegram.JavaTelegram;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.Toolkit;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import static com.git.ifly6.communique.ngui.components.CommuniqueFactory.createMenuItem;

/**
 * <code>Communiqué</code> is the main class of the Communiqué system. It handles the GUI aspect of the entire program
 * and other actions.
 */
@SuppressWarnings("ALL")
public class Communique extends AbstractCommunique implements JTelegramLogger {

    private static final Logger LOGGER = Logger.getLogger(Communique.class.getName());

    private JavaTelegram client; // Sending client
    private Thread sendingThread = new Thread(); // The one sending thread

    private JFrame frame;
    private JButton btnParse;
    private CommuniqueLogViewer logViewer;
    private CommuniqueEditor focusedEditor;

    private List<String> parsedRecipients;
    private Map<String, Boolean> rSuccessTracker;
    private CommuniqueRecruiter recruiter;
    private CommuniqueTimerBar progressBar;
    private JLabel progressLabel;

    public static void main(String[] args) {
        CommuniqueLAF.setLAF(); // note that this line will also set up the static initialisation for appSupport etc
        CommuniqueLAF.compressLogs(); // compresses logs one day older than this initialisation

        EventQueue.invokeLater(() -> {
            try {
                Communique window = new Communique();
                window.frame.setVisible(true);
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
    }

    /**
     * Create the application.
     * @wbp.parser.entryPoint
     */
    public Communique() {
        super();

        client = new JavaTelegram(this);
        initialise();

        // Make sure user is connected to the Internet
        try {
            new URL("https://www.nationstates.net").openConnection().connect();
        } catch (IOException e) {
            this.showErrorDialog(CommuniqueConstants.INTERNET_ERROR);
            LOGGER.log(Level.SEVERE, "No connection to NationStates!", e);
        }
    }

    /**
     * Initialise the contents of the frame.
     */
    private void initialise() {

        frame = new JFrame();

        Dimension screenDimensions = Toolkit.getDefaultToolkit().getScreenSize();
        double sWidth = screenDimensions.getWidth();
        double sHeight = screenDimensions.getHeight();

        frame.setTitle("Communiqué " + Communique7Parser.version);
        frame.setBounds(50, 50, 600, 600);
        frame.setMinimumSize(new Dimension(400, 600));
        frame.setDefaultCloseOperation(WindowConstants.EXIT_ON_CLOSE);

        // set up content pane
        JPanel contentPane = new JPanel();
        contentPane.setBorder(CommuniqueFactory.createBorder(5));
        contentPane.setLayout(new BorderLayout(5, 5));
        frame.setContentPane(contentPane);

        // set up panes for centre and bottom
        JPanel dataPanel = new JPanel();
        contentPane.add(dataPanel, BorderLayout.CENTER);

        // centre
        logViewer = new CommuniqueLogViewer();
        contentPane.add(logViewer, BorderLayout.CENTER);

        // top
        JComboBox<CommuniqueEditor> editorSelector = new JComboBox<>();
        editorSelector.addActionListener(ae -> {
            CommuniqueEditor newFocusedEditor = editorSelector.getItemAt(editorSelector.getSelectedIndex());
            if (newFocusedEditor == null)
                focusedEditor = null;

            if (focusedEditor != newFocusedEditor) {
                focusedEditor = newFocusedEditor;
                focusedEditor.toFront();
                LOGGER.info(String.format("Selected editor for path %s", focusedEditor.getPath().getFileName()));
            }
        });
        editorSelector.addFocusListener(new FocusListener() {
            @Override
            public void focusGained(FocusEvent e) {
                EventQueue.invokeLater(() -> {
                    List<CommuniqueEditor> activeEditors = CommuniqueEditorManager.getInstance().getActiveEditors();
                    HashSet<CommuniqueEditor> displaySet = new HashSet<>();
                    for (int i = 0; i < editorSelector.getItemCount(); i++)
                        displaySet.add(editorSelector.getItemAt(i));

                    for (CommuniqueEditor activeEditor : activeEditors) // add editors not present
                        if (!displaySet.contains(activeEditor))
                            editorSelector.addItem(activeEditor);

                    for (CommuniqueEditor displayed : displaySet) // remove unregistered editors
                        if (!activeEditors.contains(displayed))
                            editorSelector.removeItem(displayed);
                });
            }

            @Override
            public void focusLost(FocusEvent e) {
            }
        });
        contentPane.add(editorSelector, BorderLayout.NORTH);

        // bottom
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout(5, 5));

        progressBar = new CommuniqueTimerBar();
        progressLabel = new JLabel("? / ?");
        progressLabel.setBorder(BorderFactory.createEmptyBorder(0, 0,0, 2));
        JPanel bottomCentre = new JPanel();
        bottomCentre.setLayout(new BorderLayout(5, 5));
        bottomCentre.add(progressBar, BorderLayout.CENTER);
        bottomCentre.add(progressLabel, BorderLayout.EAST);
        bottom.add(bottomCentre, BorderLayout.CENTER);

        btnParse = new JButton("Parse");
        btnParse.addActionListener(ae -> setupSend());
        bottom.add(btnParse, BorderLayout.SOUTH);

        contentPane.add(bottom, BorderLayout.SOUTH);

        // set up the menubar
        menuBar = new JMenuBar();
        frame.setJMenuBar(menuBar);

        // create file menu
        JMenu fileMenu = this.addFileMenu(
                List.of(createMenuItem(  // save action
                        "Save All", KeyEvent.VK_S,
                        ae -> CommuniqueEditorManager.getInstance().saveAll()
                ))
        );

        // default edit menu
        this.addEditMenu();

        // window menu
        JMenu mnWindow = this.addWindowMenu();
        mnWindow.addSeparator();
        mnWindow.add(createMenuItem("Recruiter...", KeyEvent.VK_R, ae -> showRecruiter()));

        // help menu
        this.addHelpMenu();

        // log initialisation
        Logger.getLogger("").addHandler(new CommuniqueLogHandler(logViewer));

        // post initialisation
        Runtime.getRuntime().addShutdownHook(new Thread(() -> CommuniqueEditorManager.getInstance().saveAll()));
        LOGGER.info("Shutdown hook added");
        LOGGER.info("Communiqué loaded");
        CommuniqueEditorManager.getInstance().initialiseEditors();
    }

    /**
     * This massive method parses the data and does internal Communique checks; it then passes on to the send method to
     * start execution of the sending process.
     */
    private void setupSend() {

        // Process in the case that the button currently says stop
        if (sendingThread.isAlive() && btnParse.getText().equalsIgnoreCase("Stop")) {
            // kill the thread
            sendingThread.interrupt();
            client.kill();
            return;
        }

        List<CommuniqueRecipient> tokens = focusedEditor.getConfig().getcRecipients();

        // Check if a recruit-flag has been used.
        boolean rfPresent = tokens.stream()
                .filter(t -> t.getRecipientType() == CommuniqueRecipientType.FLAG)
                .anyMatch(t -> t.getName().equals("recruit"));
        if (rfPresent) {
            showRecruiter();
            return;
        }

        // Call and do the parsing
        LOGGER.info("Called parser");
        Communique7Parser parser = new Communique7Parser();
        try {
            parsedRecipients = parser.apply(tokens).listRecipients();
            if (!ApiUtils.contains(CommuniqueProcessingAction.values(),
                    focusedEditor.getConfig().processingAction)) {
                // if config.processingAction not in CommuniqueProcessingAction.values
                // deal with invalid processing action
                this.showErrorDialog("Invalid processing action.\n"
                        + "Select a valid processing action");
                return;
            }
            parsedRecipients = focusedEditor.getConfig().processingAction.apply(parsedRecipients);

        } catch (PatternSyntaxException pse) {
            // note 2020-01-27: better that regex errors are shown in monospaced font
            JLabel label = new JLabel(
                    String.format("<html>Regex pattern syntax error. <br /><pre>%s</pre></html>",
                            pse.getMessage().replace("\n", "<br />"))
            );
            // pass to message dialog
            LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", pse);
            this.showErrorDialog(label);
            return;

        } catch (JTelegramException | IllegalArgumentException jte) {
            LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", jte);
            this.showErrorDialog(jte.getMessage());
            return;
        }
        LOGGER.info(String.format("Recipients parsed. Found %d recipients", parsedRecipients.size()));

        // Change GUI elements
        progressLabel.setText("0 / " + parsedRecipients.size());

        // Check that there are in fact recipients
        if (parsedRecipients.size() == 0) {
            Communique.this.showErrorDialog("No recipients specified.");
            return;
        }

        // Ask for confirmation
        CommuniqueSendDialog sendDialog = new CommuniqueSendDialog(frame, parsedRecipients, currentWaitTime());
        LOGGER.info("CommuniqueSendDialog " + (sendDialog.getValue() == 0
                ? "cancelled"
                : "accepted with " + sendDialog.getValue()));
        if (sendDialog.getValue() == CommuniqueSendDialog.SEND) send();
    }

    /**
     * Sending thread. It executes all of these commands in the {@link Runnable} thread and then calls the completion
     * method.
     */
    private void send() {

        // sending logic
        if (!sendingThread.isAlive()) {
            client.resetKill();
            Runnable runner = () -> {

                // save the configuration
                focusedEditor.save();

                // pass information to client
                client.setRecipients(parsedRecipients);
                client.setKeys(focusedEditor.getConfig().keys);
                client.setTelegramType(focusedEditor.getTelegramType());
                client.setWaitTime(this.currentWaitTime());

                // Create tracker, initialise success tracking HashMap
                rSuccessTracker = new LinkedHashMap<>();
                parsedRecipients.forEach(r -> rSuccessTracker.put(r, false));
                if (rSuccessTracker == null) LOGGER.severe("Success tracker is null");

                try {
                    client.connect();
                } catch (JTelegramException jte) {  // JTE occurring during send?
                    LOGGER.log(Level.SEVERE, "JTelegramException in send", jte);
                    Communique.this.showErrorDialog(jte.getMessage());
                    return;
                }

                cleanupSend();
            };

            sendingThread = new Thread(runner);
            sendingThread.start();

            btnParse.setText("Stop");
        }
    }

    /**
     * Cleanup commands to be done when sending is complete.
     */
    private void cleanupSend() {
        LOGGER.info("Queries complete");

        List<String> messages = new ArrayList<>();
        messages.add(String.format("Successful queries to %d of %d nations.\n",
                rSuccessTracker.entrySet().stream().filter(e -> e.getValue() == Boolean.TRUE).count(),  // # successes
                parsedRecipients.size()));

        if (rSuccessTracker.containsValue(Boolean.FALSE)) { // if there was a failure to connect,
            messages.add("Failure to dispatch to the following nations, not auto-excluded:\n");  // add formatting,
            rSuccessTracker.entrySet().stream() // and then list the relevant nations to which there was a failure
                    .filter(e -> e.getValue() == Boolean.FALSE)
                    .forEach(e -> messages.add("- " + e.getKey()));
        }

        if (!rSuccessTracker.containsValue(Boolean.TRUE))   // if does not contain any trues, i.e. all false
            messages.add("\nNo successful queries. Check the log file for errors and report to author as necessary.");

        // display that data in a CommuniqueTextDialog
        CommuniqueTextDialog.createMonospacedDialog(frame, "Results",
                String.join("\n", messages), true);

        // Graphical reset
        EventQueue.invokeLater(() -> {
            progressBar.reset();
            progressLabel.setText("? / ?"); // reset progress label
            btnParse.setText("Parse"); // reset parse button
        });
    }

    /**
     * @return currently selected wait time if present, otherwise, defualt wait time (in milliseconds)
     */
    private int currentWaitTime() {
        return focusedEditor.getDelay();
    }

    /**
     * Shows and initialises the Communique Recruiter.
     * @see com.git.ifly6.communique.ngui.CommuniqueRecruiter
     * @since 6
     */
    private void showRecruiter() {
        if (recruiter == null || !recruiter.isDisplayable()) {
            recruiter = new CommuniqueRecruiter(focusedEditor);
        } else recruiter.toFront();
    }

    /**
     * @see com.git.ifly6.nsapi.telegram.JTelegramLogger#log(java.lang.String)
     */
    @Override
    public void log(String input) {
        LOGGER.info(input);
    }

    /**
     * @see com.git.ifly6.nsapi.telegram.JTelegramLogger#sentTo(java.lang.String, int, int)
     */
    @Override
    public void sentTo(String recipientName, int x, int length) {
        String recipient = CommuniqueRecipients.createExcludedNation(recipientName).toString();
        focusedEditor.appendLine(x == 0 ? "\n" + recipient : recipient); // scrolls to the bottom automatically

        progressBar.reset();
        progressBar.start(System.currentTimeMillis(), System.currentTimeMillis() + currentWaitTime());

        // Update the label and log successes as relevant
        progressLabel.setText(String.format("%d / %d", x + 1, length));
        rSuccessTracker.put(recipientName, true);
    }

}
