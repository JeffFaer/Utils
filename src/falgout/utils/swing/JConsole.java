package falgout.utils.swing;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.PipedReader;
import java.io.PipedWriter;
import java.io.PrintWriter;
import java.io.Serializable;
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
import javax.swing.event.DocumentListener;
import javax.swing.event.UndoableEditListener;
import javax.swing.text.AttributeSet;
import javax.swing.text.BadLocationException;
import javax.swing.text.Element;
import javax.swing.text.MutableAttributeSet;
import javax.swing.text.Position;
import javax.swing.text.Segment;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;
import javax.swing.text.StyledEditorKit;

public class JConsole extends JComponent {
    private class ConsoleDocument implements StyledDocument, Serializable {
        private static final long serialVersionUID = -7203947224984750337L;
        
        private final StyledDocument d;
        
        public ConsoleDocument(StyledDocument d) {
            this.d = d;
        }
        
        @Override
        public Style addStyle(String nm, Style parent) {
            return d.addStyle(nm, parent);
        }
        
        @Override
        public void removeStyle(String nm) {
            d.removeStyle(nm);
        }
        
        @Override
        public Style getStyle(String nm) {
            return d.getStyle(nm);
        }
        
        @Override
        public void setCharacterAttributes(int offset, int length, AttributeSet s, boolean replace) {
            d.setCharacterAttributes(offset, length, s, replace);
        }
        
        @Override
        public void setParagraphAttributes(int offset, int length, AttributeSet s, boolean replace) {
            d.setParagraphAttributes(offset, length, s, replace);
        }
        
        @Override
        public void setLogicalStyle(int pos, Style s) {
            d.setLogicalStyle(pos, s);
        }
        
        @Override
        public Style getLogicalStyle(int p) {
            return d.getLogicalStyle(p);
        }
        
        @Override
        public Element getParagraphElement(int pos) {
            return d.getParagraphElement(pos);
        }
        
        @Override
        public Element getCharacterElement(int pos) {
            return d.getCharacterElement(pos);
        }
        
        @Override
        public Color getForeground(AttributeSet attr) {
            return d.getForeground(attr);
        }
        
        @Override
        public Color getBackground(AttributeSet attr) {
            return d.getBackground(attr);
        }
        
        @Override
        public Font getFont(AttributeSet attr) {
            return d.getFont(attr);
        }
        
        @Override
        public int getLength() {
            return d.getLength();
        }
        
        @Override
        public void addDocumentListener(DocumentListener listener) {
            d.addDocumentListener(listener);
        }
        
        @Override
        public void removeDocumentListener(DocumentListener listener) {
            d.removeDocumentListener(listener);
        }
        
        @Override
        public void addUndoableEditListener(UndoableEditListener listener) {
            d.addUndoableEditListener(listener);
        }
        
        @Override
        public void removeUndoableEditListener(UndoableEditListener listener) {
            d.removeUndoableEditListener(listener);
        }
        
        @Override
        public Object getProperty(Object key) {
            return d.getProperty(key);
        }
        
        @Override
        public void putProperty(Object key, Object value) {
            d.putProperty(key, value);
        }
        
        @Override
        public void remove(int offs, int len) throws BadLocationException {
            if (canModify(offs, len)) {
                d.remove(offs, len);
            }
        }
        
        @Override
        public void insertString(int offset, String str, AttributeSet a) throws BadLocationException {
            boolean isInput = isInput(a);
            boolean isNewLine = false;
            boolean canModify = false;
            if (isInput) {
                isNewLine = isNewLine(str);
                if (!isNewLine) {
                    int len = isInput(getCharacterElement(offset - 1)) ? 0 : 1;
                    canModify = canModify(offset, len);
                }
            }
            offset = canModify ? offset : getLength();
            
            d.insertString(offset, str, a);
            
            if (textPane.getSelectedText() == null) {
                textPane.setCaretPosition(offset + str.length());
            } else {
                textPane.moveCaretPosition(offset + str.length());
            }
            
            if (!isInput) {
                fireConsoleEvent(a.getAttribute(AttributeSet.NameAttribute).toString(), str);
            }
            
            if (isInput && isNewLine) {
                String content = getInputLine();
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
        
        private boolean canModify(int offset, int len) throws BadLocationException {
            while (offset < getLength()) {
                Element e = getCharacterElement(offset);
                if (isInput(e)) {
                    String content = getContent(e);
                    
                    if (isNewLine(content)) { return false; }
                } else if (len > 0) { return false; }
                
                if (len > 0) {
                    len -= e.getEndOffset() - offset;
                }
                offset = e.getEndOffset();
            }
            return true;
        }
        
        private String getContent(Element e) throws BadLocationException {
            return getText(e.getStartOffset(), e.getEndOffset() - e.getStartOffset());
        }
        
        // reverse search the document for a full line of INPUT
        private String getInputLine() throws BadLocationException {
            List<String> pieces = new ArrayList<>();
            
            int offset = getLength();
            while (offset >= 0) {
                Element e = getCharacterElement(offset);
                if (isInput(e)) {
                    String content = getContent(e);
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
        public String getText(int offset, int length) throws BadLocationException {
            return d.getText(offset, length);
        }
        
        @Override
        public void getText(int offset, int length, Segment txt) throws BadLocationException {
            d.getText(offset, length, txt);
        }
        
        @Override
        public Position getStartPosition() {
            return d.getStartPosition();
        }
        
        @Override
        public Position getEndPosition() {
            return d.getEndPosition();
        }
        
        @Override
        public Position createPosition(int offs) throws BadLocationException {
            return d.createPosition(offs);
        }
        
        @Override
        public Element[] getRootElements() {
            return d.getRootElements();
        }
        
        @Override
        public Element getDefaultRootElement() {
            return d.getDefaultRootElement();
        }
        
        @Override
        public void render(Runnable r) {
            d.render(r);
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
        textPane.setStyledDocument(new ConsoleDocument(textPane.getStyledDocument()));
        
        defaultStyle = textPane.addStyle(DEFAULT, null);
        defaultStyle.addAttributes(DEFAULT_STYLE);
        
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
            
            Style s = textPane.addStyle(name, defaultStyle);
            s.addAttributes(style);
            PrintWriter w = new PrintWriter(new StyledDocumentAppender(textPane.getStyledDocument(), name), true);
            outputs.put(name, w);
            
            return w;
        }
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
