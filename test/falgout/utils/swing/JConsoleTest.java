package falgout.utils.swing;

import static org.hamcrest.CoreMatchers.endsWith;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.awt.AWTException;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Point;
import java.awt.Robot;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.Iterator;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;
import javax.swing.WindowConstants;
import javax.swing.text.AttributeSet;
import javax.swing.text.Element;
import javax.swing.text.SimpleAttributeSet;
import javax.swing.text.Style;
import javax.swing.text.StyleConstants;
import javax.swing.text.StyledDocument;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InOrder;
import org.mockito.Matchers;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.runners.MockitoJUnitRunner;
import org.mockito.stubbing.Answer;

@RunWith(MockitoJUnitRunner.class)
public class JConsoleTest {
    private static Robot r;
    private JFrame frame;
    private JConsole c;
    
    @Mock private ConsoleListener listener;
    
    @BeforeClass
    public static void beforeClass() throws AWTException {
        r = new Robot();
        r.setAutoWaitForIdle(true);
        r.setAutoDelay(50);
    }
    
    @Before
    public void init() throws InvocationTargetException, InterruptedException {
        final WindowListener l = mock(WindowListener.class);
        doAnswer(unlock()).when(l).windowOpened(Matchers.<WindowEvent> any());
        
        SwingUtilities.invokeLater(new Runnable() {
            @Override
            public void run() {
                c = new JConsole();
                c.addConsoleListener(listener);
                
                frame = new JFrame("JConsoleTest");
                frame.addWindowListener(l);
                frame.setContentPane(c);
                frame.pack();
                frame.setLocation(300, 300);
                frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                frame.setVisible(true);
            }
        });
        
        synchronized (l) {
            l.wait();
        }
        
        giveFrameFocus();
    }
    
    private Answer<?> unlock() {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                WindowListener mock = (WindowListener) invocation.getMock();
                synchronized (mock) {
                    mock.notify();
                }
                
                WindowEvent e = (WindowEvent) invocation.getArguments()[0];
                e.getWindow().removeWindowListener(mock);
                return null;
            }
        };
    }
    
    private void giveFrameFocus() throws InvocationTargetException, InterruptedException {
        final AtomicReference<Point> location = new AtomicReference<>();
        final AtomicReference<Dimension> size = new AtomicReference<>();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                location.set(frame.getLocationOnScreen());
                size.set(frame.getSize());
            }
        });
        
        Point middle = location.get();
        middle.x += size.get().width / 2;
        middle.y += size.get().height / 2;
        
        r.mouseMove(middle.x, middle.y);
        r.mousePress(InputEvent.BUTTON1_DOWN_MASK);
        r.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
    }
    
    @After
    public void after() throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                frame.dispose();
            }
        });
    }
    
    @Test
    public void ConsoleListenerReceivesInformationAboutOutputStreamsAndInputStream() throws InvocationTargetException,
            InterruptedException {
        c.getWriter(JConsole.OUTPUT).write("foo");
        c.getWriter(JConsole.INPUT).write("middle");
        c.getWriter(JConsole.ERROR).write("bar");
        c.getWriter(JConsole.INPUT).println();
        
        InOrder i = inOrder(listener);
        i.verify(listener).textWritten(new ConsoleEvent(c, JConsole.OUTPUT, "foo"));
        i.verify(listener).textWritten(new ConsoleEvent(c, JConsole.ERROR, "bar"));
        i.verify(listener).textWritten(new ConsoleEvent(c, JConsole.INPUT, "middle\n"));
        
        checkText("foomiddlebar\n");
    }
    
    private void checkText(final String expected) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                assertEquals(expected, c.getTextPane().getText());
            }
        });
    }
    
    @Test
    public void ConsoleListenerReceivesFullLinesFromInputWriter() {
        doAnswer(checkTrailingLineBreak("foobar\n")).when(listener).textWritten(Matchers.<ConsoleEvent> any());
        c.getWriter(JConsole.INPUT).write("foo");
        c.getWriter(JConsole.INPUT).println("bar");
    }
    
    private Answer<?> checkTrailingLineBreak(final String expected) {
        return new Answer<Void>() {
            @Override
            public Void answer(InvocationOnMock invocation) throws Throwable {
                ConsoleEvent e = (ConsoleEvent) invocation.getArguments()[0];
                assertThat(e.getText(), endsWith("\n"));
                assertEquals(expected, e.getText());
                return null;
            }
        };
    }
    
    @Test
    public void ReaderGetsTextFromInputWriter() throws IOException {
        c.getWriter(JConsole.INPUT).println("foobar");
        assertEquals("foobar", c.getInput().readLine());
    }
    
    @Test
    public void CannotEditOutputStreamText() throws InvocationTargetException, InterruptedException {
        checkDeletion(JConsole.OUTPUT, "foobar");
    }
    
    private void checkDeletion(String name, String expected) throws InvocationTargetException, InterruptedException {
        c.getWriter(name).write("foobar");
        
        keyType(KeyEvent.VK_END);
        keyType(KeyEvent.VK_BACK_SPACE, 3);
        
        checkText(expected);
    }
    
    private void keyType(int... keys) {
        for (int key : keys) {
            keyType(key, 1);
        }
    }
    
    private void keyType(int key, int times) {
        for (int i = 0; i < times; i++) {
            r.keyPress(key);
            r.keyRelease(key);
        }
    }
    
    @Test
    public void CanEditInputStreamText() throws InvocationTargetException, InterruptedException {
        checkDeletion(JConsole.INPUT, "foo");
    }
    
    @Test
    public void CannotInsertIntoOutputStreamText() throws InvocationTargetException, InterruptedException {
        checkInsertion(JConsole.OUTPUT, "barfoo");
    }
    
    private void checkInsertion(String name, String expected) throws InvocationTargetException, InterruptedException {
        c.getWriter(name).write("bar");
        
        keyType(KeyEvent.VK_HOME, KeyEvent.VK_F, KeyEvent.VK_O, KeyEvent.VK_O);
        
        checkText(expected);
    }
    
    @Test
    public void CanInsertIntoInputStreamText() throws InvocationTargetException, InterruptedException {
        checkInsertion(JConsole.INPUT, "foobar");
    }
    
    @Test
    public void CannotPasteOverOutputStreamText() throws InvocationTargetException, InterruptedException {
        copyToClipboard("middle");
        checkPaste(JConsole.OUTPUT, "foo123barmiddle");
    }
    
    private void copyToClipboard(String text) throws InvocationTargetException, InterruptedException {
        String initialText = getCurrentText();
        
        c.getWriter(JConsole.INPUT).write(text);
        
        select(KeyEvent.VK_LEFT, text.length(), text);
        
        r.keyPress(KeyEvent.VK_CONTROL);
        keyType(KeyEvent.VK_X);
        r.keyRelease(KeyEvent.VK_CONTROL);
        
        checkText(initialText);
    }
    
    private String getCurrentText() throws InvocationTargetException, InterruptedException {
        final AtomicReference<String> text = new AtomicReference<>();
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                text.set(c.getTextPane().getText());
            }
        });
        return text.get();
    }
    
    private void select(int key, int length, String expected) throws InvocationTargetException, InterruptedException {
        r.keyPress(KeyEvent.VK_SHIFT);
        keyType(key, length);
        r.keyRelease(KeyEvent.VK_SHIFT);
        
        checkSelection(expected);
    }
    
    private void checkSelection(final String expected) throws InvocationTargetException, InterruptedException {
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                assertEquals(expected, c.getTextPane().getSelectedText());
            }
        });
    }
    
    private void checkPaste(String name, String expected) throws InvocationTargetException, InterruptedException {
        c.getWriter(name).write("foo123bar");
        
        keyType(KeyEvent.VK_LEFT, 3);
        select(KeyEvent.VK_LEFT, 3, "123");
        
        paste();
        
        checkText(expected);
    }
    
    private void paste() {
        r.keyPress(KeyEvent.VK_CONTROL);
        keyType(KeyEvent.VK_V);
        r.keyRelease(KeyEvent.VK_CONTROL);
    }
    
    @Test
    public void CanPasteOverInputStreamText() throws InvocationTargetException, InterruptedException {
        copyToClipboard("middle");
        checkPaste(JConsole.INPUT, "foomiddlebar");
    }
    
    @Test
    public void CannotEditPreviouslyEnteredLinesOfInput() throws InvocationTargetException, InterruptedException {
        c.getWriter(JConsole.INPUT).println("foobar");
        keyType(KeyEvent.VK_BACK_SPACE, 4);
        checkText("foobar\n");
    }
    
    @Test
    public void CanEditBothSidesOfInputIfSplitByOutput() throws InvocationTargetException, InterruptedException {
        writeInterleaved();
        
        copyToClipboard("foo");
        
        keyType(KeyEvent.VK_LEFT, 2);
        keyType(KeyEvent.VK_DELETE); // remove the 8
        keyType(KeyEvent.VK_LEFT, 5);
        paste(); // insert foo between 2 and 3
        
        checkText("12foo345679");
    }
    
    private void writeInterleaved() {
        c.getWriter(JConsole.INPUT).write("123");
        c.getWriter(JConsole.OUTPUT).write("456");
        c.getWriter(JConsole.INPUT).write("789");
    }
    
    @Test
    public void SubmittingInterleavedTextDoesntIncludeOutput() throws InvocationTargetException, InterruptedException,
            IOException {
        writeInterleaved();
        keyType(KeyEvent.VK_ENTER);
        
        assertEquals("123789", c.getInput().readLine());
        verify(listener).textWritten(new ConsoleEvent(c, JConsole.INPUT, "123789\n"));
        checkText("123456789\n");
    }
    
    @Test
    public void TypingEnterFromMiddleOfInputDoesntSplitLine() throws IOException, InvocationTargetException,
            InterruptedException {
        c.getWriter(JConsole.INPUT).write("foobar");
        keyType(KeyEvent.VK_LEFT, 3);
        keyType(KeyEvent.VK_ENTER);
        
        assertEquals("foobar", c.getInput().readLine());
        verify(listener).textWritten(new ConsoleEvent(c, JConsole.INPUT, "foobar\n"));
        checkText("foobar\n");
    }
    
    @Test(expected = IllegalStateException.class)
    public void CannotCreateExistingOutputStream() {
        c.createWriter(JConsole.OUTPUT);
    }
    
    @Test
    public void CannotTypeInOutputStreamText() throws InvocationTargetException, InterruptedException {
        c.getWriter(JConsole.OUTPUT).write("foobar");
        keyType(KeyEvent.VK_LEFT, 3);
        keyType(KeyEvent.VK_A);
        
        checkText("foobara");
    }
    
    @Test
    public void AlteringStylePropogatesChanges() throws InvocationTargetException, InterruptedException {
        c.getWriter(JConsole.OUTPUT).write("one");
        c.getWriter(JConsole.ERROR).write("two");
        c.getWriter(JConsole.OUTPUT).write("three");
        
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                Style s = c.getStyle(JConsole.OUTPUT);
                StyleConstants.setForeground(s, Color.GREEN);
                
                StyledDocument d = c.getTextPane().getStyledDocument();
                
                Iterator<Element> i = new CharacterElementIterator(d);
                while (i.hasNext()) {
                    Element e = i.next();
                    if (e.getAttributes().containsAttribute(AttributeSet.NameAttribute, s.getName())) {
                        assertEquals(new SimpleAttributeSet(s), new SimpleAttributeSet(e.getAttributes()));
                    }
                }
            }
        });
    }
}
