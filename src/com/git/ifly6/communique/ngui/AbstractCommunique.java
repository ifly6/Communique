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

import com.git.ifly6.communique.CommuniqueUtilities;
import com.git.ifly6.communique.ngui.components.CommuniqueConstants;
import com.git.ifly6.communique.ngui.components.CommuniqueFactory;
import com.git.ifly6.communique.ngui.components.CommuniqueEditorManager;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;
import javax.swing.text.DefaultEditorKit;
import javax.swing.undo.UndoManager;
import java.awt.Desktop;
import java.awt.FileDialog;
import java.awt.Frame;
import java.awt.event.ActionListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.io.IOException;
import java.nio.file.Path;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.git.ifly6.communique.ngui.CommuniqueMessages.ERROR;
import static com.git.ifly6.communique.ngui.CommuniqueMessages.TITLE;
import static com.git.ifly6.communique.ngui.components.CommuniqueConstants.COMMAND_KEY;
import static com.git.ifly6.communique.ngui.components.CommuniqueFactory.createMenuItem;
import static com.git.ifly6.communique.ngui.components.CommuniqueLAF.APP_SUPPORT;
import static com.git.ifly6.communique.ngui.components.CommuniqueFileChoosers.show;

public abstract class AbstractCommunique {

    private static final Logger LOGGER = Logger.getLogger(AbstractCommunique.class.getName());

    protected JFrame frame;
    protected JMenuBar menuBar;

    protected JMenu addFileMenu(JMenuItem saveItem) {
        // create menu and add
        JMenu mnFile = new JMenu("File");
        menuBar.add(mnFile);

        // set up action listener
        ActionListener openFileAction = ae -> {
            Path p = show(frame, FileDialog.LOAD);
            if (p == null) {
                LOGGER.info("New file at null path");
                return;
            }
            CommuniqueEditorManager.getInstance().newEditor(p);
        };

        // new, open, and save all
        mnFile.add(createMenuItem("New", KeyEvent.VK_N, openFileAction));
        mnFile.add(createMenuItem("Open", KeyEvent.VK_O, openFileAction));

        // add the save menu item
        mnFile.add(saveItem);

        mnFile.addSeparator();
        mnFile.add(createMenuItem("Close", KeyEvent.VK_W, ae -> {
            frame.setVisible(false);
            frame.dispose();
        }));

        mnFile.addSeparator();
        mnFile.add(CommuniqueFactory.createMenuItem(
                "Show Application Support Directory",
                KeyStroke.getKeyStroke(KeyEvent.VK_O, COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK), ae -> {
                    try {
                        Desktop.getDesktop().open(APP_SUPPORT.toFile());
                    } catch (IOException e) {
                        String s = "Failed to open application support directory";
                        showErrorDialog(s);
                        LOGGER.log(Level.WARNING, s, e);
                    }
                }
        ));

        // Only add the Quit menu item if the OS is not Mac
        if (!CommuniqueUtilities.IS_OS_MAC) {
            mnFile.addSeparator();
            mnFile.add(CommuniqueFactory.createMenuItem(
                    "Quit",
                    KeyEvent.VK_Q, ae -> System.exit(0)
            ));
        }

        return mnFile;
    }

    public JMenu addEditMenu() {
        JMenu mnEdit = new JMenu("Edit");
        menuBar.add(mnEdit);

        // Create undo manager to get that dope functionality
        UndoManager undoManager = new UndoManager();

        JMenuItem mntmUndo = new JMenuItem("Undo");
        mntmUndo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY));
        mntmUndo.addActionListener(e -> {
            if (undoManager.canUndo()) undoManager.undo();
        });
        mnEdit.add(mntmUndo);

        JMenuItem mntmRedo = new JMenuItem("Redo");
        mntmRedo.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_Z, COMMAND_KEY | InputEvent.SHIFT_DOWN_MASK));
        mntmRedo.addActionListener(e -> {
            if (undoManager.canRedo()) undoManager.redo();
        });
        mnEdit.add(mntmRedo);

        mnEdit.addSeparator();

        JMenuItem cut = new JMenuItem(new DefaultEditorKit.CutAction());
        cut.setText("Cut");
        cut.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_X, COMMAND_KEY));
        mnEdit.add(cut);

        JMenuItem copy = new JMenuItem(new DefaultEditorKit.CopyAction());
        copy.setText("Copy");
        copy.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_C, COMMAND_KEY));
        mnEdit.add(copy);

        JMenuItem paste = new JMenuItem(new DefaultEditorKit.PasteAction());
        paste.setText("Paste");
        paste.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_V, COMMAND_KEY));
        mnEdit.add(paste);

        return mnEdit;
    }

    protected JMenu addWindowMenu() {
        JMenu mnWindow = new JMenu("Window");
        menuBar.add(mnWindow);

        JMenuItem mntmMinimise = new JMenuItem("Minimise");
        mntmMinimise.setAccelerator(KeyStroke.getKeyStroke(KeyEvent.VK_M, COMMAND_KEY));
        mntmMinimise.addActionListener(e -> {
            if (frame.getState() == Frame.NORMAL) frame.setState(Frame.ICONIFIED);
        });
        mnWindow.add(mntmMinimise);

        return mnWindow;
    }

    protected JMenu addHelpMenu() {
        JMenu mnHelp = new JMenu("Help");
        menuBar.add(mnHelp);

        mnHelp.add(createMenuItem(
                "About",
                ae -> CommuniqueTextDialog.createMonospacedDialog(
                        frame, "About", CommuniqueMessages.acknowledgement,
                        true)
        ));
        mnHelp.addSeparator();
        mnHelp.add(createMenuItem("Documentation", ae -> {
            try {
                Desktop.getDesktop().browse(CommuniqueConstants.GITHUB_URI);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot open Communiqué GitHub page", e);
            }
        }));
        mnHelp.add(createMenuItem("Forum Thread", ae -> {
            try {
                Desktop.getDesktop().browse(CommuniqueConstants.FORUM_THREAD);
            } catch (IOException e) {
                LOGGER.log(Level.WARNING, "Cannot open NationStates forum support thread for Communiqué", e);
            }
        }));
        mnHelp.addSeparator();
        mnHelp.add(createMenuItem(
                "Licence",
                e -> CommuniqueTextDialog.createMonospacedDialog(frame, "Licence",
                        CommuniqueMessages.getLicence(), false)
        ));

        return mnHelp;
    }

    protected void showErrorDialog(String text) {
        // new 2020-01-27
        text = text.endsWith(".") ? text : text + "."; // append a dot
        JOptionPane.showMessageDialog(frame, text, ERROR, JOptionPane.PLAIN_MESSAGE, null);
    }

    protected void showErrorDialog(JLabel label) {
        // new 2020-01-27
        JOptionPane.showMessageDialog(frame, label, ERROR, JOptionPane.PLAIN_MESSAGE, null);
    }

    protected String showInputDialog(String text) {
        return JOptionPane.showInputDialog(frame, text, TITLE, JOptionPane.PLAIN_MESSAGE);
    }
}
