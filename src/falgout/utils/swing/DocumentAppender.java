package falgout.utils.swing;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.io.Writer;
import java.util.concurrent.ExecutionException;

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
        
        try {
            SwingUtils.runOnEDT(new Runnable() {
                @Override
                public void run() {
                    doWrite(s);
                }
            });
        } catch (InterruptedException e) {
            throw new InterruptedIOException(e.getMessage());
        } catch (ExecutionException e) {
            // Do we want to use e.getCause() here?
            throw new Error("This shouldn't happen, we're already catching the only Exception that could be thrown", e);
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
