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

import java.time.Instant;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;

/**
 * This class is a collection of utility methods used inside Communique programs.
 */
public class CommuniqueUtilities {

    public static final boolean IS_OS_MAC = System.getProperty("os.name").toLowerCase().startsWith("mac");
    public static final boolean IS_OS_WINDOWS = System.getProperty("os.name").toLowerCase().startsWith("windows");

    public static final int NORMAL_FORM = 0;
    public static final int NO_COLONS = 1;

    // Prevent initialisation
    private CommuniqueUtilities() {
    }

    /**
     * Converts raw seconds into days, hours, minutes, and seconds.
     * @param seconds elapsed
     * @return a string in days, hours, minutes, and seconds
     */
    public static String time(long seconds) {
        long minutes = seconds / 60;
        seconds -= minutes * 60;
        long hours = minutes / 60;
        minutes -= hours * 60;
        long days = hours / 24;
        hours -= days * 24;
        return String.format("%dd:%dh:%dm:%ds", days, hours, minutes, seconds);
    }

    /**
     * Gets a Java ISO local date time formatted with colons replaced for hyphens. This should be Windows
     * @return date with format like {@code 2024-03-31 02:41:49}
     */
    public static String getTime() {
        return getTime(NORMAL_FORM);
    }

    /**
     * Gets a Java ISO local date time formatted with colons replaced for hyphens. This should be Windows
     * @param option replaces the colons with hyphens if {@link CommuniqueUtilities#NO_COLONS}
     * @return date with format like {@code 2024-03-31 02-41-49} when {@code onWindows} is true
     */
    public static String getTime(int option) {
        // must avoid colons in file names because windows doesn't like it apparently
        String s = DateTimeFormatter.ISO_LOCAL_DATE_TIME.withZone(ZoneId.systemDefault())
                .format(Instant.now().truncatedTo(ChronoUnit.SECONDS)) // truncate to seconds to ignore decimals

                .replace('T', ' ');
        return option == NO_COLONS ? s.replace(':', '-') // hacky, whatever
                : s;
    }

}