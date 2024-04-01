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

/**
 * This package handles the loading and processing of information for Communique on disc. Internet-related loading is
 * handled in {@link com.git.ifly6.nsapi nsapi}. The main three classes are
 * {@link com.git.ifly6.communique.io.CommuniqueConfig CommuniqueConfig},
 * {@link com.git.ifly6.communique.io.CommuniqueReader CommuniqueReader}, and
 * {@link com.git.ifly6.communique.io.CommuniqueWriter CommuniqueWriter}. Classes which serve mainly to scrape data from
 * the internet in this package are deprecated.
 * @see com.git.ifly6.nsapi
 */
package com.git.ifly6.communique.io;