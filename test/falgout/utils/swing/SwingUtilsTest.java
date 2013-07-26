package falgout.utils.swing;

import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import org.junit.Before;
import org.junit.Test;

public class SwingUtilsTest {
    
    private JFrame top;
    private JPanel mid;
    private JButton bot;
    
    @Before
    public void init() {
        top = new JFrame();
        mid = new JPanel();
        bot = new JButton();
        
        mid.add(bot);
        top.setContentPane(mid);
    }
    
    @Test
    public void GetParentTest() {
        assertSame(top, SwingUtils.getParent(JFrame.class, bot));
        assertSame(mid, SwingUtils.getParent(JPanel.class, bot));
        assertNull(SwingUtils.getParent(JFrame.class, top));
    }
    
    @Test
    public void RunOnEDTTest() throws InvocationTargetException, InterruptedException, ExecutionException {
        final Runnable checkEDT = new Runnable() {
            @Override
            public void run() {
                assertTrue(SwingUtilities.isEventDispatchThread());
            }
        };
        SwingUtilities.invokeAndWait(new Runnable() {
            @Override
            public void run() {
                try {
                    SwingUtils.runOnEDT(checkEDT);
                } catch (InterruptedException | ExecutionException e) {
                    throw new Error(e);
                }
            }
        });
        
        SwingUtils.runOnEDT(checkEDT);
    }
}
