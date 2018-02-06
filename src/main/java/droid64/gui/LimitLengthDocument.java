package droid64.gui;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.PlainDocument;
import javax.swing.text.SimpleAttributeSet;

public class LimitLengthDocument extends PlainDocument {
	private static final long serialVersionUID = 1L;
	private int limit;
	
	public LimitLengthDocument(int limit) {
		super();
		this.limit = limit;
	}

	public LimitLengthDocument(int limit, String text) {
		super();
		this.limit = limit;
		try {
			insertString(0, text,  new SimpleAttributeSet());
		} catch (BadLocationException e) {}
	}
	
	public void insertString(int offset, String  str, AttributeSet attr) throws BadLocationException {
		if (str == null) {
			return;
		}
		if ((getLength() + str.length()) <= limit) {
			super.insertString(offset, str, attr);
		}
	}
	
	public void setText(String text) {
		AttributeSet attrs = new SimpleAttributeSet();
		try {
			super.replace(0, getLength(), text, attrs);
			insertString(0, text, attrs);
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
	}
	
}
