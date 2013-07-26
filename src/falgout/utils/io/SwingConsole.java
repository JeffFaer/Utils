package falgout.utils.io;

import java.awt.Window;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import falgout.utils.swing.ConsoleEvent;
import falgout.utils.swing.ConsoleListener;
import falgout.utils.swing.JConsole;
import falgout.utils.swing.SwingUtils;

public class SwingConsole extends Console {
    private final JConsole console;
    
    public SwingConsole() {
        try {
            console = SwingUtils.runOnEDT(new Callable<JConsole>() {
                @Override
                public JConsole call() {
                    JConsole console = new JConsole();
                    console.addConsoleListener(new ConsoleListener() {
                        @Override
                        public void textWritten(ConsoleEvent e) {
                            if (!e.getWriter().equals(JConsole.INPUT)) {
                                JConsole console = e.getSource();
                                Window w = SwingUtils.getParent(Window.class, console);
                                if (w == null) {
                                    JFrame frame = new JFrame("Console");
                                    frame.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                                    frame.setLocationByPlatform(true);
                                    frame.setContentPane(new JScrollPane(console));
                                    frame.pack();
                                    
                                    w = frame;
                                }
                                
                                if (!w.isVisible()) {
                                    w.setVisible(true);
                                } else {
                                    w.requestFocus();
                                }
                            }
                        }
                    });
                    
                    return console;
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new Error(e);
        }
    }
    
    public JConsole getConsole() {
        return console;
    }
    
    @Override
    public PrintWriter writer() {
        return console.getWriter(JConsole.OUTPUT);
    }
    
    @Override
    public BufferedReader reader() {
        return console.getInput();
    }
    
    @Override
    public char[] readPassword(String fmt, Object... args) {
        throw new Error("Not yet implemented.");
        
        // TODO
    }
    
    @Override
    public void flush() {}
}
