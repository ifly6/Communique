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

import java.awt.Point;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

public class CommuniqueEditorManager {
    private static CommuniqueEditorManager instance = new CommuniqueEditorManager();
    private static final Logger LOGGER = Logger.getLogger(CommuniqueEditorManager.class.getName());
    private static final Path PATH_LOCATION = CommuniqueLAF.APP_SUPPORT.resolve(".editor-paths");
    private static final Path AUTOSAVE = CommuniqueLAF.APP_SUPPORT.resolve("autosave.txt");

    private CommuniqueEditorManager() {
    }

    public static CommuniqueEditorManager getInstance() {
        return instance;
    }

    public List<CommuniqueEditor> getEditors() {
        return CommuniqueEditor.INSTANCES;
    }

    public List<CommuniqueEditor> getActiveEditors() {
        return getEditors().stream().filter(CommuniqueEditor::active).collect(Collectors.toList());
    }

    /**
     * Attempt to construct a new editor
     * @param p is the {@link Path} to be editing at
     */
    public void newEditor(Path p) {
        if (getActiveEditors().stream()
                .map(CommuniqueEditor::getPath)
                .anyMatch(p::equals))
            return;

        new CommuniqueEditor(p); // create otherwise
    }

    public void savePaths() {
        try {
            List<String> pathList = getActiveEditors().stream()
                    .map(CommuniqueEditor::getPath) // nullable!
                    .filter(Objects::nonNull)
                    .map(Path::normalize)
                    .map(Path::toString)
                    .collect(Collectors.toList());
            Files.write(PATH_LOCATION, pathList);

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to save the paths of previously-open files", e);
        }
    }

    public List<String> getPaths() {
        try {
            return Files.readAllLines(PATH_LOCATION);

        } catch (FileNotFoundException | NoSuchFileException e) {
            LOGGER.info("No \".editor-paths\" file to read! Passing autosave only");

        } catch (IOException e) {
            LOGGER.log(Level.SEVERE, "Unable to read the paths of previously open files", e);
        }

        return new ArrayList<>(List.of(AUTOSAVE.normalize().toString()));
    }

    public void initialiseEditors() {
        List<String> paths = getPaths();
        for (String s : paths)
            newEditor(Paths.get(s));
    }

    public void saveAll() {
        savePaths();
        List<CommuniqueEditor> l = getActiveEditors();
        for (CommuniqueEditor e : l)
            e.save();
    }

    private Set<Point> getLocations() {
        return getEditors().stream() // use all editors even if not yet displayable
                .map(CommuniqueEditor::getLocation)
                .collect(Collectors.toSet());
    }

    public boolean isLocationUsed(Point p) {
        for (Point location : getLocations())
            if (p.distance(location) < 5) return true;
        return false;
    }
}

