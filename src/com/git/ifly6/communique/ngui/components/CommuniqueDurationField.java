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

import com.git.ifly6.communique.ngui.components.subcomponents.CommuniqueDigitFilter;
import com.git.ifly6.communique.ngui.components.subcomponents.CommuniqueMouseListener;

import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import javax.swing.text.AbstractDocument;
import java.awt.Font;
import java.time.Duration;
import java.time.temporal.ChronoUnit;

public class CommuniqueDurationField extends JTextField {

    private final ChronoUnit TIME_UNIT;
    private final Duration MINIMUM_DURATION;

    public CommuniqueDurationField(ChronoUnit unit, String tooltip, DocumentListener listener) {
        this(unit, Duration.ofSeconds(30), tooltip, listener);
    }

    public CommuniqueDurationField(ChronoUnit unit, Duration minimum, String tooltip, DocumentListener listener) {
        this.TIME_UNIT = unit;
        this.MINIMUM_DURATION = minimum;

        // visuals
        this.setToolTipText(tooltip);
        this.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));

        // listening and document filter
        this.getDocument().addDocumentListener(listener);
        this.addMouseListener(new CommuniqueMouseListener(me -> this.selectAll()));
        AbstractDocument document = (AbstractDocument) this.getDocument();
        document.setDocumentFilter(new CommuniqueDigitFilter());
    }

    /**
     * Parses the text in the field for the relevant number of seconds, adds five milliseconds. If the text is empty,
     * returns {@link Duration#ZERO}.
     * @return duration specified + 5 ms
     */
    public Duration getDuration() {
        if (this.getText().isBlank()) return ChronoUnit.FOREVER.getDuration();
        Duration rawDuration = Duration
                .of(Integer.parseInt(this.getText()), TIME_UNIT)
                .plus(Duration.ofMillis(5));
        return (rawDuration.compareTo(MINIMUM_DURATION) < 0) ? MINIMUM_DURATION : rawDuration;
    }

    /**
     * Sets the text as the value of the given duration, rounded (always down) to the established {@link #TIME_UNIT}. If
     * the duration is {@link ChronoUnit#FOREVER}, it clears the box.
     * @param interval to set as digits
     */
    public void setDuration(Duration interval) {
        if (ChronoUnit.FOREVER.getDuration().equals(interval)) this.setText("");
        else this.setText(String.valueOf(interval.dividedBy(TIME_UNIT.getDuration())));
    }
}
