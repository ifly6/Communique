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

import com.git.ifly6.CommuniqueApplication;
import com.git.ifly6.CommuniqueUtilities;
import com.git.ifly6.communique.data.Communique7Monitor;
import com.git.ifly6.communique.data.Communique7Parser;
import com.git.ifly6.communique.data.CommuniqueRecipients;
import com.git.ifly6.communique.ngui.components.CommuniqueEditor;
import com.git.ifly6.communique.ngui.components.CommuniqueEditorManager;
import com.git.ifly6.communique.ngui.components.CommuniqueFactory;
import com.git.ifly6.communique.ngui.components.CommuniqueLogHandler;
import com.git.ifly6.communique.ngui.components.CommuniqueLogViewer;
import com.git.ifly6.communique.ngui.components.CommuniqueSwingUtilities;
import com.git.ifly6.communique.ngui.components.CommuniqueTimerBar;
import com.git.ifly6.communique.ngui.components.dialogs.CommuniqueSendDialog;
import com.git.ifly6.communique.ngui.components.dialogs.CommuniqueTextDialog;
import com.git.ifly6.nsapi.NSConnection;
import com.git.ifly6.nsapi.NSException;
import com.git.ifly6.nsapi.NSIOException;
import com.git.ifly6.nsapi.ctelegram.CommSender;
import com.git.ifly6.nsapi.ctelegram.CommSenderInterface;
import org.apache.commons.text.WordUtils;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenuBar;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.PatternSyntaxException;

import static com.git.ifly6.CommuniqueApplication.COMMUNIQUE;
import static com.git.ifly6.communique.ngui.components.CommuniqueFactory.createMenuItem;

/**
 * <code>Communiqué</code> is the main class of the Communiqué system. It handles the GUI aspect of the entire program
 * and other actions.
 */
public class Communique extends AbstractCommunique implements CommSenderInterface {

    private static final Logger LOGGER = Logger.getLogger(Communique.class.getName());

    private Communique7Monitor monitor;
    private CommSender sender;
    private Map<String, Boolean> rSuccessTracker;

    private JButton btnParse;
    private CommuniqueLogViewer logViewer;
    private CommuniqueEditor focusedEditor;

    private JComboBox<CommuniqueEditor> editorSelector;
    private CommuniqueTimerBar progressBar;
    private JLabel progressLabel;

    public static void main(String[] args) {
        // initialisation cascade
        CommuniqueApplication.setupLogger(COMMUNIQUE);
        CommuniqueApplication.setLogLevel(Level.INFO);
        CommuniqueApplication.nativise(COMMUNIQUE);
        CommuniqueApplication.compressLogs();
        CommuniqueApplication.setLAF();

        // start the gui
        EventQueue.invokeLater(() -> {
            try {
                // initialise main window
                Communique window = new Communique();
                window.frame.setVisible(true);

                // initialise the editors
                CommuniqueEditorManager.getInstance().initialiseEditors();
                window.frame.toFront();

            } catch (Exception e) {
                LOGGER.log(Level.SEVERE, "Encountered error on Communique window instantiation!", e);
            }
        });

        // set up shutdown hook
        LOGGER.info("Shutdown hook added");
        LOGGER.info("Communiqué loaded");
    }

    public Communique() {
        // initialise components and subparts
        initialise();

        // Make sure user is connected to the Internet
        try {
            new NSConnection("https://www.nationstates.net/").connect();
        } catch (IOException e) {
            this.showErrorDialog(CommuniqueConstants.INTERNET_ERROR);
            LOGGER.log(Level.SEVERE, "No connection to NationStates!", e);
        }
    }

    /** Initialise frame contents. */
    private void initialise() {

        frame = new JFrame();
        frame.setTitle("Communiqué " + Communique7Parser.VERSION);
        frame.setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);
        initialiseClosingActions();
        CommuniqueSwingUtilities.setupDimensions(
                frame,
                new Dimension(400, 600), // minimum size
                new Dimension(600, 600), // default size
                false
        );

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
        editorSelector = new JComboBox<>();
        editorSelector.addActionListener(ae -> {
            CommuniqueEditor newFocusedEditor = CommuniqueSwingUtilities.getSelected(editorSelector);
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
            public void focusLost(FocusEvent e) { }
        });
        contentPane.add(editorSelector, BorderLayout.NORTH);

        // bottom
        JPanel bottom = new JPanel();
        bottom.setLayout(new BorderLayout(5, 5));

        progressBar = new CommuniqueTimerBar();
        progressLabel = new JLabel("? / ?");
        progressLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 0, 2));
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
        this.addFileMenu(
                List.of(createMenuItem(  // save action
                        "Save All", KeyEvent.VK_S,
                        ae -> CommuniqueEditorManager.getInstance().saveAll()
                ))
        );

        // add default menus
        this.addEditMenu();
        this.addWindowMenu();
        this.addHelpMenu();

        // log view initialisation
        Logger.getLogger("").addHandler(new CommuniqueLogHandler(logViewer));
    }

    private void initialiseClosingActions() {
        frame.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent we) { doQuit(); }
        });
        Desktop d = Desktop.getDesktop();
        if (CommuniqueUtilities.IS_OS_MAC)
            if (d.isSupported(Desktop.Action.APP_QUIT_HANDLER))
                d.setQuitHandler((e, r) -> doQuit());
    }

    private void doQuit() {
        CommuniqueEditorManager cem = CommuniqueEditorManager.getInstance();
        cem.savePaths(); // must save before the editor frames are closed
        cem.getActiveEditors().forEach(
                e -> e.frame.dispatchEvent(new WindowEvent(e.frame, WindowEvent.WINDOW_CLOSING)));
        if (cem.getActiveEditors().isEmpty())
            System.exit(0);
    }

    /**
     * This massive method parses the data and does internal Communique checks; it then passes on to the send method to
     * start execution of the sending process.
     */
    private void setupSend() {

        // Process in the case that the button currently says stop
        if (btnParse.getText().equalsIgnoreCase("Stop")) {
            if (sender != null) sender.stopSend();
            return;
        }

        // initialise the tracker
        rSuccessTracker = new HashMap<>();

        // Call and do the parsing
        LOGGER.info("Initialising sender");
        try {
            monitor = new Communique7Monitor(focusedEditor);
            sender = monitor.constructSender(this);

            List<String> initialRecipients = monitor.preview();
            LOGGER.info(String.format("Found %d initial recipients", initialRecipients.size()));

            // Change GUI elements
            EventQueue.invokeLater(() -> {
                progressLabel.setText("0 / " + initialRecipients.size());
                editorSelector.setEditable(false);
            });

            // Ask for confirmation
            CommuniqueSendDialog sendDialog = new CommuniqueSendDialog(frame, initialRecipients, currentWaitTime());
            LOGGER.info("CommuniqueSendDialog " + (sendDialog.getValue() == 0
                    ? "cancelled"
                    : "accepted with " + sendDialog.getValue()));
            if (sendDialog.getValue() == CommuniqueSendDialog.SEND) send();

        } catch (PatternSyntaxException pse) {
            // note 2020-01-27: better that regex errors are shown in monospaced font
            JLabel label = new JLabel(
                    String.format("<html>Regex pattern syntax error. <br /><pre>%s</pre></html>",
                            pse.getMessage().replace("\n", "<br />"))
            );
            // pass to message dialogs
            LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", pse);
            this.showErrorDialog(label);

        } catch (NSException | NSIOException | IllegalArgumentException jte) {
            LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", jte);
            this.showErrorDialog(jte.getMessage());
        }
    }

    private void send() {
        sender.startSend(); // do start
        btnParse.setText("Stop"); // set it to run
    }

    /**
     * Cleanup commands to be done when sending is complete.
     */
    private void cleanupSend() {
        LOGGER.info("Queries complete");

        List<String> messages = new ArrayList<>();
        messages.add(String.format("Successful queries to %d of %d nations.\n",
                rSuccessTracker.entrySet().stream().filter(e -> e.getValue() == Boolean.TRUE).count(),  // # successes
                rSuccessTracker.entrySet().size()
        ));

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
            editorSelector.setEditable(true);
            btnParse.setText("Parse"); // reset parse button
        });
    }

    /**
     * @return currently selected wait time if present, otherwise, defualt wait time (in milliseconds)
     */
    private Duration currentWaitTime() {
        return focusedEditor.getTelegramInterval();
    }

    @Override
    public void processed(String recipient, int numberSent, CommSender.SendingAction action) {
        LOGGER.info(String.format("%s recipient \"%s\"",
                WordUtils.capitalize(action.toString().toLowerCase()),
                recipient));
        rSuccessTracker.put(recipient, action == CommSender.SendingAction.SENT); // handles both sent and skipped
        if (action == CommSender.SendingAction.SENT) {
            // add the excluded nation to the focused editor
            String toAdd = CommuniqueRecipients.createExcludedNation(recipient).toString();
            focusedEditor.appendLine(numberSent == 0 ? "\n" + toAdd : toAdd); // scrolls to the bottom automatically

            // reset the timing progress bar
            progressBar.reset();
            progressBar.start(System.currentTimeMillis(), System.currentTimeMillis() + currentWaitTime().toMillis());

            // Update the label and log successes as relevant
            String denominator = (monitor.recipientsCount().isEmpty())
                    ? "?"
                    : String.valueOf(monitor.recipientsCount().getAsLong());
            progressLabel.setText(String.format("%d / %s", numberSent, denominator));
        }
    }

    @Override
    public void onTerminate() {
        cleanupSend();
    }

    @Override
    public void onError(String m, Throwable e) {
        LOGGER.log(Level.SEVERE, "Exception in parsing recipients. Displaying to user", e);
        this.showErrorDialog(m);
    }
}
