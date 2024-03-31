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

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JPanel;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.KeyStroke;
import javax.swing.border.Border;
import javax.swing.border.TitledBorder;
import javax.swing.event.DocumentListener;
import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionListener;

import static com.git.ifly6.communique.ngui.components.CommuniqueConstants.COMMAND_KEY;

public class CommuniqueFactory {

    /**
     * Creates a {@link JTextField} with tooltip text and document listener, pre-loaded with monospaced font setting.
     * @param text     to initialise with
     * @param tooltip  to give
     * @param listener to execute
     * @return constructed <code>JTextField</code>
     */
    public static JTextField createField(String text, String tooltip, DocumentListener listener) {
        JTextField field = new JTextField();

        // visuals
        field.setToolTipText(tooltip);
        field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        field.setText(text);

        // listeners
        field.getDocument().addDocumentListener(listener);
        field.addMouseListener(new CommuniqueMouseListener(me -> field.selectAll()));

        return field;
    }

    public static JTextArea createArea(String text, DocumentListener listener) {
        JTextArea area = new JTextArea();
        area.setText(text);
        area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
        area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
        if (listener != null)
            area.getDocument().addDocumentListener(listener);
        return area;
    }

    public static JPanel labelledComponent(String label, Component component) {
        JPanel f = new JPanel();
        f.setLayout(new BorderLayout(5, 5));
        f.add(new JLabel(label), BorderLayout.WEST);
        f.add(component, BorderLayout.CENTER);
        return f;
    }

    public static Border createBorder(int pixels) {
        return BorderFactory.createEmptyBorder(pixels, pixels, pixels, pixels);
    }

    public static TitledBorder createTitledBorder(String text) {
        TitledBorder tb = new TitledBorder(text);
        tb.setTitlePosition(TitledBorder.ABOVE_TOP);
        tb.setTitleFont(tb.getTitleFont().deriveFont(Font.BOLD));
        tb.setBorder(BorderFactory.createEmptyBorder(2, 4, 0, 0));
        return tb;
    }

    public static JMenuItem createMenuItem(String label, ActionListener... als) {
        return createMenuItem(label, null, als);
    }

    public static JMenuItem createMenuItem(String label, int shortcut, ActionListener... als) {
        return createMenuItem(label, KeyStroke.getKeyStroke(shortcut, COMMAND_KEY), als);
    }

    public static JMenuItem createMenuItem(String label, KeyStroke shortcut, ActionListener... als) {
        JMenuItem menuItem = new JMenuItem(label);
        for (ActionListener al : als)
            menuItem.addActionListener(al);
        if (shortcut != null)
            menuItem.setAccelerator(shortcut);

        return menuItem;
    }

    /**
     * Create Communique-default {@link GridBagConstraints} for {@link GridBagLayout} that align-left with top and
     * bottom insets of 2 pixels.
     * @param gridX on the layout
     * @param gridY on the layout
     * @param hFill {@code true} if it should fill horizontal space
     * @return constructed constraints
     */
    public static GridBagConstraints createGridBagConstraints(int gridX, int gridY, boolean hFill) {
        GridBagConstraints c = new GridBagConstraints();
        c.anchor = GridBagConstraints.LINE_START;
        c.insets = new Insets(0, 0, 2, 0);
        c.fill = (hFill) ? GridBagConstraints.HORIZONTAL : GridBagConstraints.NONE;
        c.weightx = (hFill) ? 1 : 0;
        c.gridx = gridX;
        c.gridy = gridY;
        return c;
    }
}
