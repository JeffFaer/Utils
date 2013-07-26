package falgout.utils.swing;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Before;
import org.junit.Test;

import falgout.utils.swing.SwingUtils;

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
    public void getParentTest() {
        assertSame(top, SwingUtils.getParent(JFrame.class, bot));
        assertSame(mid, SwingUtils.getParent(JPanel.class, bot));
        assertNull(SwingUtils.getParent(JFrame.class, top));
    }
}
