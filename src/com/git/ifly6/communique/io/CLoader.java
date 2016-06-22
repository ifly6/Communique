/* Copyright (c) 2016 ifly6
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated
 * documentation files (the "Software"), to deal in the Software without restriction, including without limitation the
 * rights to use, copy, modify, merge, publish, distribute, sublicense, and/or sell copies of the Software, and to
 * permit persons to whom the Software is furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all copies or substantial portions of the
 * Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE
 * WARRANTIES OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR
 * COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR
 * OTHERWISE, ARISING FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE SOFTWARE. */
package com.git.ifly6.communique.io;

import java.io.IOException;
import java.nio.file.Path;

import com.git.ifly6.communique.data.CConfig;

/**
 * <code>CLoader</code> is a class allowing the easy abstraction of access to a single point and simplifying the process
 * of reading and writing to that data. It uses the <code>{@link CReader}</code> and <code>{@link CWriter}</code>
 * classes to access that data. They are based on reading and writing through the Java properties system, which then use
 * functions to pass around, read, and write to a {@link com.git.ifly6.communique.data.CConfig CConfig} object.
 */
public class CLoader {

	Path path;

	// Force initialisation with appropriate variables
	@SuppressWarnings(value = { "unused" }) private CLoader() {
	}

	public CLoader(Path path) {
		this.path = path;
	}

	public void save(CConfig config) throws IOException {
		CWriter writer = new CWriter(path, config);
		writer.write();
	}

	public CConfig read() throws IOException {
		CReader reader = new CReader(path);
		return reader.read();
	}
}
