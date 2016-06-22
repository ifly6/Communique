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
package com.git.ifly6.communique.ngui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Toolkit;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import com.git.ifly6.javatelegram.JTelegramLogger;

/**
 * @author Kevin
 *
 */
public class CGuiLogger implements JTelegramLogger {

	private JTextArea textArea;
	private JFrame frame;

	public CGuiLogger() {

		frame = new JFrame("JTelegramLogger");
		frame.setDefaultCloseOperation(JFrame.HIDE_ON_CLOSE);

		int width = 300;
		int height = 300;
		frame.setSize(width, height);

		Dimension sSize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setLocation((int) ((sSize.getWidth() / 2) - width / 2), (int) ((sSize.getHeight() / 2) - height / 2));

		JPanel panel = new JPanel();
		frame.setContentPane(panel);

		panel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		panel.setLayout(new BorderLayout(5, 5));

		textArea = new JTextArea();
		textArea.setFont(new Font(Font.MONOSPACED, 0, 11));

		panel.add(textArea, BorderLayout.CENTER);

	}

	/**
	 * @see com.git.ifly6.javatelegram.JTelegramLogger#log(java.lang.String)
	 */
	@Override public void log(String input) {
		textArea.append("\n" + input);
	}

	public void setVisible(boolean tf) {
		frame.setVisible(tf);
	}
}
