package falgout.utils.io;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.PrintWriter;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicReference;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JScrollPane;
import javax.swing.WindowConstants;

import com.google.inject.Inject;

import falgout.utils.swing.ConsoleEvent;
import falgout.utils.swing.ConsoleListener;
import falgout.utils.swing.JConsole;
import falgout.utils.swing.SwingUtils;

public class SwingConsole extends Console {
    private final JConsole console;
    
    @Inject
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
    public char[] readPassword(final String fmt, final Object... args) {
        try {
            return SwingUtils.runOnEDT(new Callable<char[]>() {
                @Override
                public char[] call() throws Exception {
                    Window w = SwingUtils.getParent(Window.class, console);
                    final JDialog d = new JDialog(w, "Enter Password");
                    
                    final AtomicReference<char[]> password = new AtomicReference<>();
                    final JPasswordField pass = new JPasswordField(20);
                    
                    ActionListener dispose = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            d.dispose();
                        }
                    };
                    ActionListener updatePassword = new ActionListener() {
                        @Override
                        public void actionPerformed(ActionEvent e) {
                            password.set(pass.getPassword());
                        }
                    };
                    
                    JLabel prompt = new JLabel(String.format(fmt, args));
                    prompt.setLabelFor(pass);
                    
                    pass.addActionListener(dispose);
                    pass.addActionListener(updatePassword);
                    
                    JPanel textField = new JPanel();
                    textField.add(prompt);
                    textField.add(pass);
                    
                    JButton submit = new JButton("Submit");
                    submit.addActionListener(dispose);
                    submit.addActionListener(updatePassword);
                    
                    JButton cancel = new JButton("Cancel");
                    cancel.addActionListener(dispose);
                    
                    JPanel buttons = new JPanel();
                    buttons.add(submit);
                    buttons.add(cancel);
                    
                    JPanel content = new JPanel();
                    content.setLayout(new BoxLayout(content, BoxLayout.Y_AXIS));
                    content.add(textField);
                    content.add(buttons);
                    
                    d.setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);
                    d.setContentPane(content);
                    d.pack();
                    d.setResizable(false);
                    d.setLocationRelativeTo(w);
                    d.setModal(true);
                    d.setVisible(true);
                    
                    return password.get();
                }
            });
        } catch (InterruptedException | ExecutionException e) {
            throw new Error(e);
        }
    }
    
    @Override
    public void flush() {}
}
