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

import net.java.balloontip.BalloonTip;
import net.java.balloontip.styles.RoundedBalloonStyle;

import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.EventQueue;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Toolkit;
import java.awt.Window;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Graphical utilities for Communique 3.
 * @since version 13
 */
public class CommuniqueSwingUtilities {

    private static final Dimension SCREEN_DIMENSIONS = Toolkit.getDefaultToolkit().getScreenSize();
    private static final ScheduledExecutorService BALLOON_SCHEDULER = Executors.newScheduledThreadPool(2);

    /**
     * Gets selected element from {@link JComboBox}. This honestly should be in the standard library as an instance
     * method. I don't know why you have to roll your own.
     * @param box with item selected
     * @param <T> type of object
     * @return selected item
     */
    public static <T> T getSelected(JComboBox<T> box) {
        int selection = Objects.requireNonNull(box).getSelectedIndex();
        if (selection != -1 && selection < box.getModel().getSize()) return box.getItemAt(selection);
        throw new IllegalArgumentException(String.format("Selection value %d is is invalid", selection));
    }

    /**
     * Sets up minimum dimensions for provided {@link Window}.
     * @param window      to set up
     * @param minimumSize of window
     * @param initialSize of window
     * @param centering   true if centered on screen
     */
    public static void setupDimensions(
            Window window, Dimension minimumSize, Dimension initialSize,
            boolean centering) {
        window.setMinimumSize(minimumSize);
        window.setPreferredSize(initialSize);
        if (centering)
            window.setBounds(
                    (int) (SCREEN_DIMENSIONS.getWidth() / 2 - initialSize.getWidth() / 2),
                    (int) (SCREEN_DIMENSIONS.getHeight() / 2 - initialSize.getHeight() / 2),
                    (int) initialSize.getWidth(),
                    (int) initialSize.getHeight());
        else
            // traditional communique window start location
            window.setBounds(50, 50,
                    (int) initialSize.getWidth(),
                    (int) initialSize.getHeight());
    }

    /**
     * Creates balloon tip with the specified message, emanating from the provided component. Tip has auto-scheduled
     * destruction after two seconds.
     * @param component to attach tool-tip to
     * @param message   to display
     */
    public static void createBalloonTip(JComponent component, String message) {
        EventQueue.invokeLater(() -> {
            // create
            BalloonTip t = new BalloonTip(component, message,
                    new RoundedBalloonStyle(5, 5,
                            UIManager.getColor("Label.background"),
                            Color.DARK_GRAY),
                    false);

            // schedule its doing away after 2 seconds
            BALLOON_SCHEDULER.schedule(() ->
                            EventQueue.invokeLater(t::closeBalloon),
                    2, TimeUnit.SECONDS);
        });
    }

    public static void addComponents(JPanel panel, LinkedHashMap<String, Component> components) {
        // we're using grid bag layout
        panel.setLayout(new GridBagLayout());

        // set up grid
        List<Map.Entry<String, Component>> entryList = new ArrayList<>(components.entrySet());
        for (int i = 0; i < entryList.size(); i++) {
            Map.Entry<String, Component> entry = entryList.get(i);
            GridBagConstraints column1 = CommuniqueFactory.createGridBagConstraints(0, i, false);
            column1.insets.right = 4; // internal padding

            panel.add(new JLabel(entry.getKey()), column1);
            panel.add(entry.getValue(), CommuniqueFactory.createGridBagConstraints(1, i, true));
        }
    }

}
