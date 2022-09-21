package droid64.gui;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

import javax.swing.JTextArea;

public class ConsoleStream extends OutputStream {
	private final JTextArea textArea;
	public ConsoleStream(JTextArea textArea) {
		this.textArea = textArea;
	}
    @Override
    public void write(int b) throws IOException {
        // redirects data to the text area
        textArea.append(String.valueOf((char)b));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    @Override
    public void write(byte[] data, int off, int len) throws IOException {
        // redirects data to the text area
        textArea.append(new String(data, off, len, StandardCharsets.ISO_8859_1));
        // scrolls the text area to the end of data
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
}