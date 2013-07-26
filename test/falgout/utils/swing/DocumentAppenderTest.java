package falgout.utils.swing;

import static org.junit.Assert.assertEquals;

import java.io.IOException;
import java.io.Writer;

import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.PlainDocument;

import org.junit.Before;
import org.junit.Test;

public class DocumentAppenderTest {
    private Document d;
    private Writer w;
    
    @Before
    public void init() {
        d = new PlainDocument();
        w = new DocumentAppender(d);
    }
    
    @Test
    public void AppendsToDocument() throws IOException, BadLocationException {
        w.write("foo");
        assertEquals("foo", getText());
        w.write("bar");
        assertEquals("foobar", getText());
    }
    
    private String getText() throws BadLocationException {
        return d.getText(0, d.getLength());
    }
}
