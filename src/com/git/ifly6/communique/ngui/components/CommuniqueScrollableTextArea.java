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

import com.git.ifly6.communique.data.CommuniqueRecipient;

import javax.swing.JScrollBar;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import java.awt.EventQueue;
import java.util.Arrays;
import java.util.List;

public class CommuniqueScrollableTextArea extends JScrollPane {

    private JTextArea area;

    public CommuniqueScrollableTextArea(JTextArea area) {
        super(area);
        this.area = area;
    }

    public void scrollBottom() {
        JScrollBar vs = this.getVerticalScrollBar();
        EventQueue.invokeLater(() -> vs.setValue(vs.getMaximum() - 1));
    }

    public String getText() {
        return area.getText();
    }

    public void setText(String t) {
        area.setText(t);
    }

    /**
     * Append the input object (intended to be either {@link CommuniqueRecipient} or {@link String}) with a new line to
     * to the internal text area. Scrolls to the bottom automatically.
     * @param obj object to append
     */
    public void appendLine(Object obj) {
        area.append("\n" + obj.toString());
        this.scrollBottom();
    }

    public List<String> getLines() {
        return Arrays.asList(this.getText().split("\n"));
    }

    public void setFontSize(int points) {
        area.setFont(area.getFont().deriveFont((float) points)); // must cast to float for size
    }

    public void setEditable(boolean b) {
        area.setEditable(b);
    }

}
