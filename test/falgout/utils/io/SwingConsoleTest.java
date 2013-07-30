package falgout.utils.io;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.awt.Window;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.utils.swing.SwingUtils;

@RunWith(JukitoRunner.class)
public class SwingConsoleTest {
    @Inject private SwingConsole c;
    
    @After
    public void after() throws InterruptedException, ExecutionException {
        SwingUtils.runOnEDT(new Runnable() {
            @Override
            public void run() {
                Window w = SwingUtils.getParent(Window.class, c.getConsole());
                if (w != null) {
                    w.dispose();
                }
            }
        });
    }
    
    @Test
    public void PopsUpWhenWrittenTo() throws InterruptedException, ExecutionException {
        Callable<Boolean> checkVisibility = new Callable<Boolean>() {
            @Override
            public Boolean call() throws Exception {
                Window w = SwingUtils.getParent(Window.class, c.getConsole());
                return w == null ? false : w.isVisible();
            }
        };
        assertFalse(SwingUtils.runOnEDT(checkVisibility));
        
        c.writer().println("Hello World!");
        
        assertTrue(SwingUtils.runOnEDT(checkVisibility));
    }
}
