package falgout.utils.io;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertNull;

import java.awt.Window;
import java.util.concurrent.ExecutionException;

import javax.swing.JButton;
import javax.swing.JTextField;

import org.jukito.JukitoRunner;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.google.inject.Inject;

import falgout.utils.swing.SwingUtils;

@RunWith(JukitoRunner.class)
public class SwingConsolePasswordDialogTest {
    @Inject private SwingConsole c;
    
    @Before
    public void init() {
        c.writer().println("Getting password...");
    }
    
    @After
    public void after() throws InterruptedException, ExecutionException {
        SwingUtils.runOnEDT(new Runnable() {
            @Override
            public void run() {
                SwingUtils.getParent(Window.class, c.getConsole()).dispose();
            }
        });
    }
    
    @Test
    public void NullPasswordIfPromptClosed() {
        doDialogTest(new Runnable() {
            @Override
            public void run() {
                getPasswordDialog().dispose();
            }
        });
        assertNull(c.readPassword());
    }
    
    private void doDialogTest(final Runnable test) {
        new Thread(new Runnable() {
            @Override
            public void run() {
                getPasswordDialog();
                
                try {
                    SwingUtils.runOnEDT(test);
                } catch (InterruptedException | ExecutionException e) {
                    e.printStackTrace();
                }
            }
        }).start();
    }
    
    private Window getPasswordDialog() {
        for (;;) {
            for (Window w : Window.getWindows()) {
                Window owner = w.getOwner();
                if (owner != null && owner.equals(SwingUtils.getParent(Window.class, c.getConsole()))) { return w; }
            }
        }
    }
    
    @Test
    public void NullPasswordIfPromptCancelled() {
        doDialogTest(clickButton("Cancel"));
        assertNull(c.readPassword());
    }
    
    private Runnable clickButton(final String text) {
        return new Runnable() {
            @Override
            public void run() {
                for (JButton b : SwingUtils.getChildren(JButton.class, getPasswordDialog())) {
                    if (b.getText().equalsIgnoreCase(text)) {
                        b.doClick();
                    }
                }
            }
        };
    }
    
    @Test
    public void GetCharArrayIfSubmitted() {
        doDialogTest(clickButton("Submit"));
        assertArrayEquals(new char[0], c.readPassword());
    }
    
    @Test
    public void GetCharArrayIfEnterInTextField() {
        doDialogTest(new Runnable() {
            @Override
            public void run() {
                JTextField f = SwingUtils.getChildren(JTextField.class, getPasswordDialog()).get(0);
                f.postActionEvent();
            }
        });
        assertArrayEquals(new char[0], c.readPassword());
    }
}
