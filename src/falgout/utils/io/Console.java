package falgout.utils.io;

import java.io.BufferedReader;
import java.io.Flushable;
import java.io.IOError;
import java.io.IOException;
import java.io.PrintWriter;

/**
 * A wrapper class around {@link java.io.Console}'s api. This allows for a
 * graphical console to be created if a tty console is not present.
 * 
 * @author jeffrey
 * 
 */
public abstract class Console implements Flushable {
    public abstract PrintWriter writer();
    
    public abstract BufferedReader reader();
    
    public Console format(String fmt, Object... args) {
        writer().format(fmt, args);
        return this;
    }
    
    public Console printf(String format, Object... args) {
        writer().printf(format, args);
        return this;
    }
    
    public String readLine(String fmt, Object... args) {
        format(fmt, args);
        try {
            return reader().readLine();
        } catch (IOException e) {
            throw new IOError(e);
        }
    }
    
    public String readLine() {
        return readLine("");
    }
    
    public abstract char[] readPassword(String fmt, Object... args);
    
    public char[] readPassword() {
        return readPassword("");
    }
    
    private static volatile Console c;
    
    public static Console getInstance() {
        if (c == null) {
            synchronized (Console.class) {
                if (c == null) {
                    java.io.Console cons = System.console();
                    if (cons != null) {
                        c = new ConsoleWrapper(cons);
                    } else {
                        c = new SwingConsole();
                    }
                }
            }
        }
        
        return c;
    }
}
