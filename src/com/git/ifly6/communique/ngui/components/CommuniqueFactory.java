package com.git.ifly6.communique.ngui.components;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import java.awt.Font;

import static com.git.ifly6.communique.ngui.components.CommuniqueConstants.CODE_HEADER;

public class CommuniqueFactory {

	/**
	 * Creates a {@link JTextField} with tooltip text and document listener, pre-loaded with monospaced font setting.
	 * @param text to initialise with
	 * @param tooltip to give
	 * @param listener to execute
	 * @return constructed <code>JTextField</code>
	 */
	public static JTextField createField(String text, String tooltip, DocumentListener listener) {
		JTextField field = new JTextField();
		field.setToolTipText(tooltip);
		field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		field.setText(text);
		field.getDocument().addDocumentListener(listener);
		return field;
	}

	public static JTextArea createArea(String defaultText, DocumentListener listener) {
		JTextArea area = new JTextArea();
		area.setText(CODE_HEADER);
		area.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		area.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		area.getDocument().addDocumentListener(listener);
		return area;
	}

}
