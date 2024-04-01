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

import com.git.ifly6.communique.ngui.components.CommuniqueFactory;
import com.git.ifly6.communique.ngui.components.CommuniqueSwingUtilities;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.nio.file.Path;
import java.util.List;

public class CommuniqueSaveDialog extends JDialog {

    public static final int CANCEL = 0;
    public static final int SAVE = 1;
    public static final int DISCARD = 2;

    private int value = -1;

    public CommuniqueSaveDialog(Frame owner, Path path) {
        super(owner, "", true);
        CommuniqueSwingUtilities.setupDimensions(
                this,
                new Dimension(200, 200),
                new Dimension(200, 200),
                true);
        this.setResizable(false);

//        this.setUndecorated(true);
        this.setLayout(new BorderLayout(5, 5));
        this.getRootPane().setBorder(CommuniqueFactory.createBorder(5));

        JPanel top = new JPanel();
        {
            top.setLayout(new BorderLayout());
            JLabel label = new JLabel(String.format("<html>%s</html>",
                    String.format("Do you want to save \"%s\"?", path.getFileName().toString()
                    )));
            label.setHorizontalAlignment(JLabel.CENTER);
            top.add(label, BorderLayout.CENTER);
        }

        JPanel bottom = new JPanel();
        {
            bottom.setLayout(new GridLayout(3, 1, 2, 2));

            JButton save = new JButton("Save");
            save.addActionListener(ae -> closeWith(SAVE));

            JButton noSave = new JButton("Discard");
            noSave.setForeground(Color.RED);
            noSave.addActionListener(ae -> closeWith(DISCARD));

            JButton cancel = new JButton("Cancel");
            cancel.addActionListener(ae -> closeWith(CANCEL));

            List<JButton> buttons = List.of(save, noSave, cancel);
            buttons.forEach(bottom::add);
            this.getRootPane().setDefaultButton(save);
        }

        this.addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                closeWith(CANCEL);
            }
        });

        this.add(top, BorderLayout.NORTH);
        this.add(bottom, BorderLayout.SOUTH);
        this.setVisible(true);
    }

    public void closeWith(int i) {
        value = i;
        this.setVisible(false);
        this.dispose();
    }

    public int getValue() { return value; }

}
