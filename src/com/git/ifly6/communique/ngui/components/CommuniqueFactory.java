package com.git.ifly6.communique.ngui.components;

import com.git.ifly6.communique.io.CommuniqueLoader;

import javax.swing.BorderFactory;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.event.DocumentListener;
import java.awt.Font;

import static com.git.ifly6.communique.ngui.components.CommuniqueConstants.CODE_HEADER;

public class CommuniqueFactory {

	public static JTextField createField(String name, DocumentListener listener) {
		JTextField field = new JTextField();
		field.setToolTipText(name);
		field.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 11));
		field.setText(CommuniqueLoader.getClientKey());
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
