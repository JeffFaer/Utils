package falgout.utils.swing;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.lang.reflect.InvocationTargetException;

import javax.swing.SwingUtilities;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.SimpleAttributeSet;

public class DocumentAppender extends Writer {
    private final Document doc;
    
    public DocumentAppender(Document doc) {
        this.doc = doc;
    }
    
    public Document getDocument() {
        return doc;
    }
    
    @Override
    public void write(char[] cbuf, int off, int len) throws IOException {
        final String s = new String(cbuf, off, len);
        
        if (SwingUtilities.isEventDispatchThread()) {
            doWrite(s);
        } else {
            try {
                SwingUtilities.invokeAndWait(new Runnable() {
                    @Override
                    public void run() {
                        doWrite(s);
                    }
                });
            } catch (InvocationTargetException e) {
                throw new Error(e);
            } catch (InterruptedException e) {
                throw new InterruptedIOException(e.getMessage());
            }
        }
    }
    
    protected void doWrite(String s) {
        try {
            doc.insertString(doc.getLength(), s, getAttributeSet());
        } catch (BadLocationException e) {
            throw new Error("We're appending at the end, this shouldn't happen.");
        }
    }
    
    protected AttributeSet getAttributeSet() {
        return SimpleAttributeSet.EMPTY;
    }
    
    @Override
    public void flush() {}
    
    @Override
    public void close() {}
}
