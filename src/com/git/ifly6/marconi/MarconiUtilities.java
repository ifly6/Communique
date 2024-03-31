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

import com.git.ifly6.commons.CommuniqueApplication;
import com.git.ifly6.commons.CommuniqueUtilities;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.logging.Logger;

/**
 * Utilities associated with Marconi.
 * @since version 1.6 (build 6) (file initially created in build 1)
 */
public class MarconiUtilities {

    private static final Logger LOGGER = Logger.getLogger(MarconiUtilities.class.getName());
    private static final Path lockFile = CommuniqueApplication.APP_SUPPORT.resolve("marconi.lock");

    /**
     * Creates two column string, each column is 30 characters wide.
     * @returns string with lines
     * @since version 3.0 (build 13)
     */
    protected static String twoColumn(List<String> items) {
        List<String> lines = new ArrayList<>();
        for (int x = 0; x < items.size(); x = x + 2) // iterate two-by-two
            try {
                lines.add(String.format("%-30.30s  %-30.30s", items.get(x), items.get(x + 1)));
            } catch (IndexOutOfBoundsException e) {
                lines.add(items.get(x)); // odd number of entries
            }
        return String.join("\n", lines);
    }

    /**
     * Creates Marconi lock file.
     * @since version 3.0 (build 13)
     */
    static void createFileLock() {
        try {
            if (!Files.exists(lockFile)) {
                Files.write(lockFile, Collections.singletonList(
                        String.format("marconi. started %s.", CommuniqueUtilities.getDate())));
                lockFile.toFile().deleteOnExit(); // delete lock file when marconi closes!
            }
        } catch (IOException e) {
            LOGGER.severe("Cannot get or create lock file!");
            e.printStackTrace();
        }
    }

    /**
     * Determines whether there is another instance of Marconi which is already sending.
     * @return boolean, whether lock file already exists
     */
    static boolean isFileLocked() {
        return Files.exists(lockFile);
    }

    /**
     * Shorthand for the scanner creation, the posing of the question, and the getting of the response. This version of
     * the prompt method will not return all responses in lower case.
     * @param prompt the string posed to the user.
     * @return the user's answer
     */
    static String prompt(String prompt) {
        System.out.print(prompt + "\t");
        return scan.nextLine();
    }

    /**
     * Sends data and requests that you sanitise it to avoid stupid errors. All responses will be in lower case. This is
     * the only way the data can be effectively sanitised.
     * @param prompt            the question posed to the user.
     * @param acceptableAnswers list of valid responses.
     * @return the user's answer, which is required to be in the list of valid responses
     */
    static String prompt(String prompt, List<String> acceptableAnswers) {
        if (acceptableAnswers.size() == 0)
            throw new UnsupportedOperationException("Must provide some acceptable answers");
        final List<String> accepted = acceptableAnswers
                .stream()
                .map(String::toLowerCase)
                .collect(Collectors.toList());

        String response;
        while (true) {
            response = prompt(prompt).toLowerCase();
            if (accepted.contains(response)) break;
            else System.out.println("Please provide an acceptable answer.");
        }
        return response;
    }

    /**
     * @param prompt the question posed to the user.
     * @return answer
     */
    static String promptYN(String prompt) {
        return prompt(prompt, List.of("yes", "no", "y", "n"));
    }
}
