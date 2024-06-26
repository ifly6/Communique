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

package com.git.ifly6.communique.io;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Writes Communique configuration files.
 * @since version 5
 */
class CommuniqueWriter {

    private Path path;
    private CommuniqueConfig config;

    /**
     * Creates <code>CommuniqueWriter</code> pointing to some path with some loaded configuration data.
     * @param path   on which to write
     * @param config data
     */
    CommuniqueWriter(Path path, CommuniqueConfig config) {
        this.path = path;
        this.config = config;
    }

    /**
     * Writes the configuration data to the path specified in the constructor.
     * @throws IOException if there is an error in writing the file
     */
    void write() throws IOException {
        config.clean(); // Have configuration clean itself
        Gson gson = new GsonBuilder().setPrettyPrinting().serializeNulls().create();
        Files.writeString(path, gson.toJson(config));
    }

}
