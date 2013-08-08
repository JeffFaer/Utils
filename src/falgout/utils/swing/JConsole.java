package falgout.utils.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.concurrent.ExecutionException;

import javax.swing.JComponent;
import javax.swing.JTextPane;
import javax.swing.SwingUtilities;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.text.AbstractDocument;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.DefaultStyledDocument;
import javax.swing.text.Document;
import javax.swing.text.DocumentFilter;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

public class JConsole extends JComponent {
    private class ConsoleFilter extends DocumentFilter {
        @Override
        public void insertString(FilterBypass fb, int offset, String string, AttributeSet attr)
                throws BadLocationException {
            StyledDocument d = (StyledDocument) fb.getDocument();
            boolean isInput = isInput(attr);
            boolean isNewLine = false;
            boolean canModify = false;
            if (isInput) {
                isNewLine = isNewLine(string);
                if (!isNewLine) {
                    int len = isInput(d.getCharacterElement(offset - 1)) ? 0 : 1;
                    canModify = canModify(d, offset, len);
                }
            }
            offset = canModify ? offset : d.getLength();
            
            super.insertString(fb, offset, string, attr);
            
            moveCaret(offset + string.length());
            
            if (!isInput) {
                fireConsoleEvent(attr.getAttribute(AttributeSet.NameAttribute).toString(), string);
            }
            
            if (isInput && isNewLine) {
                String content = getInputLine(d);
                inputSource.write(content);
                inputSource.flush();
                
                fireConsoleEvent(INPUT, content);
            }
        }
        
        private boolean isInput(Element e) {
            return isInput(e.getAttributes());
        }
        
        private boolean isInput(AttributeSet a) {
            return a.isEqual(textPane.getStyle(INPUT));
        }
        
        private boolean isNewLine(String str) {
            // DefaultEditorKit.InsertBreakAction uses only \n for new lines
            return str.contains("\n");
        }
        
        private boolean canModify(StyledDocument d, int offset, int len) throws BadLocationException {
            Iterator<Element> i = new CharacterElementIterator(d, offset);
            while (i.hasNext()) {
                Element e = i.next();
                if (isInput(e)) {
                    String content = getContent(d, e);
                    
                    if (isNewLine(content)) { return false; }
                } else if (len > 0) { return false; }
                
                if (len > 0) {
                    len -= e.getEndOffset() - offset;
                }
            }
            return true;
        }
        
        private String getContent(Document d, Element e) throws BadLocationException {
            return d.getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset());
        }
        
        private void moveCaret(int pos) {
            textPane.setCaretPosition(pos);
        }
        
        // reverse search the document for a full line of INPUT
        private String getInputLine(StyledDocument d) throws BadLocationException {
            List<String> pieces = new ArrayList<>();
            
            ListIterator<Element> i = new CharacterElementIterator(d, d.getLength() - 1);
            while (i.hasPrevious()) {
                Element e = i.previous();
                if (isInput(e)) {
                    String content = getContent(d, e);
                    if (isNewLine(content) && !pieces.isEmpty()) {
                        break;
                    }
                    
                    pieces.add(content);
                }
            }
            
            Collections.reverse(pieces);
            StringBuilder b = new StringBuilder();
            for (String s : pieces) {
                b.append(s);
            }
            return b.toString();
        }
        
        @Override
        public void remove(FilterBypass fb, int offset, int length) throws BadLocationException {
            if (canModify((StyledDocument) fb.getDocument(), offset, length)) {
                super.remove(fb, offset, length);
            } else {
                moveCaret(fb.getDocument().getLength());
            }
        }
        
        @Override
        public void replace(FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
                throws BadLocationException {
            remove(fb, offset, length);
            insertString(fb, textPane.getCaretPosition(), text, attrs);
        }
    }
    
    /**
     * The name of the default {@code Style}
     */
    public static final String DEFAULT = "default";
    /**
     * The name of the default "output" {@code Style}
     */
    public static final String OUTPUT = "out";
    /**
     * The name of the default "error" {@code Style}
     */
    public static final String ERROR = "err";
    /**
     * The name of the default "input" {@code Style}. User input is decorated
     * with this {@code Style} and is then available from the
     * {@link #getInput() BufferedReader}. Using the {@link #getWriter(String)
     * PrintWriter} for this {@code Style} simulates user input: it is also
     * available via the {@code BufferedReader}.
     */
    public static final String INPUT = "in";
    
    private static final long serialVersionUID = 5938244600144385811L;
    private static final MutableAttributeSet DEFAULT_STYLE = new SimpleAttributeSet();
    private static final MutableAttributeSet OUTPUT_STYLE = new SimpleAttributeSet();
    private static final MutableAttributeSet ERROR_STYLE = new SimpleAttributeSet();
    private static final MutableAttributeSet INPUT_STYLE = new SimpleAttributeSet();
    static {
        StyleConstants.setFontFamily(DEFAULT_STYLE, "Monospaced");
        StyleConstants.setFontSize(DEFAULT_STYLE, 12);
        StyleConstants.setForeground(DEFAULT_STYLE, Color.BLACK);
        
        StyleConstants.setForeground(ERROR_STYLE, Color.RED);
        
        StyleConstants.setForeground(INPUT_STYLE, Color.BLUE);
    }
    
    private final JTextPane textPane = new JTextPane();
    private final Style defaultStyle;
    
    private final ConcurrentMap<String, PrintWriter> outputs = new ConcurrentHashMap<>();
    private final BufferedReader input;
    private final PrintWriter inputSource;
    
    public JConsole() {
        textPane.setEditorKit(new StyledEditorKit() {
            private static final long serialVersionUID = 6409936544429332707L;
            
            // the default StyledEditorKit tracks which Style we're around and
            // uses that for getInputAttributes. We want to always use the INPUT
            // style, so we need to override it.
            @Override
            public MutableAttributeSet getInputAttributes() {
                MutableAttributeSet in = textPane.getStyle(INPUT);
                // after the setEditorKit method call, getInputAttributes is
                // called even though our INPUT Style hasn't been setup yet. Use
                // the static constant instead.
                if (in == null) {
                    in = INPUT_STYLE;
                }
                return new SimpleAttributeSet(in);
            }
        });
        StyledDocument doc = textPane.getStyledDocument();
        DocumentFilter filter = new ConsoleFilter();
        if (doc instanceof AbstractDocument) {
            ((AbstractDocument) doc).setDocumentFilter(filter);
        } else {
            DefaultStyledDocument d = new DefaultStyledDocument();
            d.setDocumentFilter(filter);
            textPane.setDocument(d);
        }
        
        defaultStyle = createStyle(DEFAULT, DEFAULT_STYLE);
        
        PipedReader pipe = new PipedReader();
        input = new BufferedReader(pipe);
        try {
            inputSource = new PrintWriter(new PipedWriter(pipe), true);
        } catch (IOException e) {
            throw new Error("Pipe shouldn't be closed or already connected. This shouldn't happen", e);
        }
        
        createWriter(OUTPUT, OUTPUT_STYLE);
        createWriter(ERROR, ERROR_STYLE);
        createWriter(INPUT, INPUT_STYLE);
        
        setLayout(new BorderLayout());
        add(textPane);
        setPreferredSize(new Dimension(500, 300));
    }
    
    public JTextPane getTextPane() {
        return textPane;
    }
    
    /**
     * Returns a {@code BufferedReader} which is sources by user input to this
     * component. This method is thread safe.
     * 
     * @return A {@code BufferedReader} that is safe for use off of the EDT.
     */
    public BufferedReader getInput() {
        return input;
    }
    
    /**
     * Returns a {@code PrintWriter} wrapping the {@code PipedWriter} that is
     * the source for the {@link #getInput() input}. Using this
     * {@code PrintWriter} disables echoing onto the {@code JTextPane}.
     * 
     * @return The source for {@code getInput()}.
     */
    public PrintWriter getInputSource() {
        return inputSource;
    }
    
    /**
     * Returns a {@code PrintWriter} wrapping a
     * {@link falgout.utils.swing.StyledDocumentAppender} for this component.
     * This method is thread safe.
     * 
     * @param name The name of the {@code Style}.
     * @return A {@code PrintWriter} that is safe for use off of the EDT.
     */
    public PrintWriter getWriter(String name) {
        return outputs.get(name);
    }
    
    /**
     * Creates a new {@code StyledDocumentAppender} which will appear as the
     * {@link #DEFAULT default Style}. This method is thread safe. If the
     * {@code PrintWriter} is created by another thread while this method is
     * executing, that {@code PrintWriter} will be returned instead.
     * 
     * @param name The name of the writer
     * @return A {@code PrintWriter} that is safe for use off of the EDT.
     */
    public PrintWriter createWriter(String name) {
        return createWriter(name, SimpleAttributeSet.EMPTY);
    }
    
    /**
     * Creates a new {@code StyledDocumentAppender} and a matching {@code Style}
     * which will have all of the given attributes. This method is thread
     * safe.If the {@code PrintWriter} is created by another thread while this
     * method is
     * executing, that {@code PrintWriter} will be returned instead.
     * 
     * @param name The name of the writer and {@code Style}
     * @param style The attributes for the {@code Style}. The parent of this
     *        {@code Style} will be the {@link #DEFAULT default Style}.
     * @return A {@code PrintWriter} that is safe for use off of the EDT.
     */
    public PrintWriter createWriter(String name, AttributeSet style) {
        PrintWriter w = outputs.get(name);
        if (w != null) { return w; }
        
        w = new PrintWriter(new StyledDocumentAppender(textPane.getStyledDocument(), name), true);
        createStyle(name, style);
        PrintWriter check = outputs.putIfAbsent(name, w);
        if (check != null) { return check; }
        
        return w;
    }
    
    private Style createStyle(final String name, final AttributeSet style) {
        try {
            // Styles are a part of Swing/AWT, they should probably only be
            // manipulated on the EDT
            return SwingUtils.runOnEDT(new Callable<Style>() {
                @Override
                public Style call() {
                    // prevent Style from being overridden. Despite the
                    // JavaDoc's assurances that the `name` must be unique, it
                    // doesn't actually need to be.
                    Style s = textPane.getStyle(name);
                    if (s != null) { return s; }
                    
                    s = textPane.addStyle(name, defaultStyle);
                    s.addAttributes(style);
                    // propagate changes to this Style to all text written with
                    // it.
                    s.addChangeListener(new ChangeListener() {
                        @Override
                        public void stateChanged(ChangeEvent e) {
                            Style s = (Style) e.getSource();
                            String name = s.getName();
                            StyledDocument d = textPane.getStyledDocument();
                            Iterator<Element> i = new CharacterElementIterator(d);
                            while (i.hasNext()) {
                                Element elem = i.next();
                                AttributeSet attrs = elem.getAttributes();
                                String appliedStyle = (String) attrs.getAttribute(AttributeSet.NameAttribute);
                                
                                // update any children of this Style as well
                                do {
                                    if (attrs.containsAttribute(AttributeSet.NameAttribute, name)) {
                                        d.setCharacterAttributes(elem.getStartOffset(),
                                                elem.getEndOffset() - elem.getStartOffset(), getStyle(appliedStyle),
                                                true);
                                        break;
                                    }
                                } while ((attrs = attrs.getResolveParent()) != null);
                            }
                        }
                    });
                    return s;
                }
            });
        } catch (ExecutionException e) {
            throw new Error(e.getCause());
        } catch (InterruptedException e) {
            throw new Error(e);
        }
    }
    
    public Style getStyle(String name) {
        return textPane.getStyle(name);
    }
    
    public void addConsoleListener(ConsoleListener l) {
        listenerList.add(ConsoleListener.class, l);
    }
    
    public void removeConsoleListener(ConsoleListener l) {
        listenerList.remove(ConsoleListener.class, l);
    }
    
    public ConsoleListener[] getConsoleListeners() {
        return listenerList.getListeners(ConsoleListener.class);
    }
    
    protected void fireConsoleEvent(String writer, String text) {
        Object[] listeners = listenerList.getListenerList();
        ConsoleEvent e = null;
        for (int i = listeners.length - 2; i >= 0; i -= 2) {
            if (listeners[i] == ConsoleListener.class) {
                if (e == null) {
                    e = new ConsoleEvent(this, writer, text);
                }
                ((ConsoleListener) listeners[i + 1]).textWritten(e);
            }
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(new Runnable() {
            
            @Override
            public void run() {
                JConsole c = new JConsole();
                Style in = c.getStyle(INPUT);
                System.out.println(c.createStyle(INPUT, SimpleAttributeSet.EMPTY) == in);
            }
            
        });
    }
}
