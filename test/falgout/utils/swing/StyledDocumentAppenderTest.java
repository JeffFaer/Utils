package falgout.utils.swing;

import java.io.IOException;
import java.io.Writer;

import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class StyledDocumentAppenderTest {
    private StyledDocument d;
    private Writer w1;
    private Style s1;
    private Writer w2;
    private Style s2;
    
    @Before
    public void init() {
        d = new DefaultStyledDocument();
        s1 = d.addStyle("s1", null);
        s2 = d.addStyle("s2", null);
        
        w1 = new StyledDocumentAppender(d, "s1");
        w2 = new StyledDocumentAppender(d, "s2");
    }
    
    @Test
    public void AppendsWithStyleToDocument() throws IOException, BadLocationException {
        w1.write("foo");
        w2.write("bar");
        
        assertEquals(s1, getElement(0).getAttributes());
        assertEquals(s2, getElement(3).getAttributes());
        Assert.assertEquals("foobar", d.getText(0, d.getLength()));
    }
    
    private void assertEquals(Style s, AttributeSet attributes) {
        Assert.assertEquals(new SimpleAttributeSet(s), attributes);
    }
    
    private Element getElement(int off) {
        return d.getCharacterElement(off);
    }
}
