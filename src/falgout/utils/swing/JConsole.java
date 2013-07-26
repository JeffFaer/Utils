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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
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
            return new SimpleAttributeSet(a).equals(textPane.getStyle(INPUT));
        }
        
        private boolean isNewLine(String str) {
            // DefaultEditorKit.InsertBreakAction uses only \n
            return str.contains("\n");
        }
        
        private boolean canModify(StyledDocument d, int offset, int len) throws BadLocationException {
            while (offset < d.getLength()) {
                Element e = d.getCharacterElement(offset);
                if (isInput(e)) {
                    String content = getContent(d, e);
                    
                    if (isNewLine(content)) { return false; }
                } else if (len > 0) { return false; }
                
                if (len > 0) {
                    len -= e.getEndOffset() - offset;
                }
                offset = e.getEndOffset();
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
            
            int offset = d.getLength();
            while (offset >= 0) {
                Element e = d.getCharacterElement(offset);
                if (isInput(e)) {
                    String content = getContent(d, e);
                    if (isNewLine(content) && !pieces.isEmpty()) {
                        break;
                    }
                    
                    pieces.add(content);
                }
                
                offset = e.getStartOffset() - 1;
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
    
    public static final String DEFAULT = "default";
    public static final String OUTPUT = "out";
    public static final String ERROR = "err";
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
    
    private final Map<String, PrintWriter> outputs = Collections.synchronizedMap(new LinkedHashMap<String, PrintWriter>());
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
        add(new JScrollPane(textPane));
        textPane.setPreferredSize(new Dimension(500, 300));
    }
    
    public JTextPane getTextPane() {
        return textPane;
    }
    
    public BufferedReader getInput() {
        return input;
    }
    
    public PrintWriter getWriter(String name) {
        return outputs.get(name);
    }
    
    public PrintWriter createWriter(String name) {
        return createWriter(name, SimpleAttributeSet.EMPTY);
    }
    
    public PrintWriter createWriter(String name, AttributeSet style) {
        checkName(name);
        synchronized (outputs) {
            checkName(name);
            
            createStyle(name, style);
            PrintWriter w = new PrintWriter(new StyledDocumentAppender(textPane.getStyledDocument(), name), true);
            outputs.put(name, w);
            
            return w;
        }
    }
    
    private Style createStyle(String name, AttributeSet style) {
        Style s = textPane.addStyle(name, defaultStyle);
        s.addAttributes(style);
        // propagate changes to this Style to all text written with it.
        s.addChangeListener(new ChangeListener() {
            @Override
            public void stateChanged(ChangeEvent e) {
                StyledDocument d = textPane.getStyledDocument();
                int offset = 0;
                while (offset < d.getLength()) {
                    Element elem = d.getCharacterElement(offset);
                    Style s = (Style) e.getSource();
                    String name = s.getName();
                    if (elem.getAttributes().containsAttribute(AttributeSet.NameAttribute, name)) {
                        d.setCharacterAttributes(elem.getStartOffset(), elem.getEndOffset() - elem.getStartOffset(), s,
                                true);
                    }
                    
                    offset = elem.getEndOffset();
                }
            }
        });
        return s;
    }
    
    private void checkName(String name) {
        if (outputs.containsKey(name)) { throw new IllegalStateException(name + " already exists."); }
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
    
    public static void main(String[] args) throws IOException {
        final JConsole c = new JConsole();
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                JFrame frame = new JFrame();
                frame.setContentPane(c);
                frame.pack();
                frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
                frame.setLocationByPlatform(true);
                frame.setVisible(true);
                
                c.getWriter(INPUT).write("foo");
                c.getWriter(OUTPUT).write("bar");
            }
        });
        
        c.addConsoleListener(new ConsoleListener() {
            @Override
            public void textWritten(ConsoleEvent e) {
                String text = e.getText().trim();
                switch (e.getWriter()) {
                case INPUT:
                    switch (text) {
                    case "exit":
                        System.exit(0);
                        break;
                    case "now":
                        c.getWriter(INPUT).write("foo");
                        c.getWriter(ERROR).write("bar");
                        
                        StyleConstants.setBackground(c.getStyle(INPUT), Color.BLACK);
                        break;
                    default:
                        System.out.println(text);
                    }
                }
            }
        });
        
        String line;
        while ((line = c.getInput().readLine()) != null) {
            System.out.println(">" + line);
        }
    }
}
