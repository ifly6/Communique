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

import java.awt.EventQueue;
import java.util.logging.Handler;
import java.util.logging.LogRecord;

public class CommuniqueLogHandler extends Handler {


    private CommuniqueLogViewer viewer;

    public CommuniqueLogHandler(CommuniqueLogViewer viewer) {
        this.viewer = viewer;
    }

    @Override
    public void publish(LogRecord record) {
        if (record == null) return; // ignore
        EventQueue.invokeLater(() -> viewer.getModel().appendRecord(record));
    }

    @Override
    public void flush() { // ignored
    }

    @Override
    public void close() throws SecurityException { // ignored
    }

}
