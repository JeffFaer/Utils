package falgout.utils.swing;

import java.util.EventListener;

public interface ConsoleListener extends EventListener {
    public void textWritten(ConsoleEvent e);
}
