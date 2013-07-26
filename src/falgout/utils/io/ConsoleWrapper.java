package falgout.utils.io;

import java.io.BufferedReader;
import java.io.PrintWriter;

class ConsoleWrapper extends Console {
    private final java.io.Console c;
    private volatile BufferedReader reader;
    
    public ConsoleWrapper(java.io.Console c) {
        this.c = c;
    }
    
    @Override
    public PrintWriter writer() {
        return c.writer();
    }
    
    @Override
    public BufferedReader reader() {
        if (reader == null) {
            synchronized (this) {
                if (reader == null) {
                    reader = new BufferedReader(c.reader());
                }
            }
        }
        return reader;
    }
    
    @Override
    public char[] readPassword(String fmt, Object... args) {
        return c.readPassword(fmt, args);
    }
    
    @Override
    public void flush() {
        c.flush();
    }
}
