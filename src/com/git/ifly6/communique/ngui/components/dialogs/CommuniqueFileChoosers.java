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

package com.git.ifly6.communique.ngui.components.dialogs;

import com.git.ifly6.CommuniqueUtilities;

import javax.swing.JFileChooser;
import java.awt.FileDialog;
import java.awt.Frame;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.git.ifly6.CommuniqueApplication.APP_SUPPORT;

public class CommuniqueFileChoosers {

    private static final Logger LOGGER = Logger.getLogger(CommuniqueFileChoosers.class.getName());
    private static final Path LAST_PATH = APP_SUPPORT.resolve(".last_path");
    private static final Path DEFAULT = Paths.get(System.getProperty("user.home"));

    /**
     * Creates a file chooser (in an OS specific manner) and shows it to the user.
     * @param parent {@link Frame} to show the chooser from, starts at the last chosen path or {@code user.home}
     * @param type   either {@code FileDialog.SAVE} or {@code FileDialog.LOAD}
     * @return the {@link Path} selected by the user
     */
    public static Path show(Frame parent, int type) {

        Path savePath;

        // 2020-06-26 Due to a problem in Windows and its AWT FileDialog,show a JFileChooser thereon
        if (CommuniqueUtilities.IS_OS_MAC) {

            FileDialog fDialog = new FileDialog(parent, "Choose file...", type);
            if (type == FileDialog.SAVE) fDialog.setTitle("Save session as...");
            fDialog.setDirectory(getLast().toFile().toString());
            fDialog.setVisible(true);

            String fileName = fDialog.getFile();
            if (fileName == null) {
                LOGGER.info("User cancelled file file dialogs");
                return null;

            } else savePath = Paths.get(
                    fDialog.getDirectory() == null
                            ? ""
                            : fDialog.getDirectory()
            ).resolve(fDialog.getFile());

        } else {

            JFileChooser fChooser = new JFileChooser(getLast().toFile());
            fChooser.setDialogTitle("Choose file...");

            int returnVal;
            // returnVal = (type == FileDialog.SAVE) ? fChooser.showSaveDialog(parent) :
            // fChooser.showOpenDialog(parent);
            if (type == FileDialog.SAVE) {
                fChooser.setDialogTitle("Save session as...");
                returnVal = fChooser.showSaveDialog(parent);
            } else returnVal = fChooser.showOpenDialog(parent);
            fChooser.setVisible(true);

            if (returnVal == JFileChooser.APPROVE_OPTION) savePath = fChooser.getSelectedFile().toPath();
            else return null;

        }

        // Make it end in txt if saving
        if (type == FileDialog.SAVE && !savePath.toString().endsWith("txt")) {
            LOGGER.info("Append txt to savePath");
            savePath = savePath.resolveSibling(savePath.getFileName() + ".txt");
        }

        LOGGER.info(String.format("%s file at %s",
                type == FileDialog.SAVE ? "Saving" : "Loading",
                savePath.normalize())
        );
        setLast(savePath.getParent());
        return savePath;
    }

    /**
     * Get {@link Path} from {@code LAST_PATH} via {@code Gson}
     * @return the last selected path, {@code DEFAULT} (user's home directory) on failure
     */
    private static Path getLast() {
        try {
            Path last = Paths.get(new String(Files.readAllBytes(LAST_PATH), StandardCharsets.UTF_8));
            if (!Files.exists(last) && !Files.exists(last.getParent())) {
                LOGGER.log(Level.WARNING, String.format(
                        ".last_path points to %s but that directory does not exist", last));
                return DEFAULT;
            }
            if (Files.isDirectory(last)) return last;
            return last.getParent();

        } catch (FileNotFoundException | NoSuchFileException fnf) {
            LOGGER.log(Level.INFO, "No .last_path file present; opening at user home");
            return DEFAULT;

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not read last used path from file", e);
            return DEFAULT;
        }
    }

    /**
     * @param last {@link Path} to persist at {@code LAST_PATH} as Json
     */
    private static void setLast(Path last) {
        try {
            // it is not possible to serialise a path?
            // https://stackoverflow.com/a/35494088
            Files.write(LAST_PATH, last.normalize().toString().getBytes(StandardCharsets.UTF_8));

        } catch (IOException e) {
            LOGGER.log(Level.WARNING, "Could not save last used path to file", e);
        }
    }

}
