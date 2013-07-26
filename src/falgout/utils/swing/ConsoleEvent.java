package falgout.utils.swing;

import java.util.EventObject;

public class ConsoleEvent extends EventObject {
    private static final long serialVersionUID = -1996332863892175068L;
    
    private final String writer;
    private final String text;
    
    public ConsoleEvent(JConsole source, String writer, String text) {
        super(source);
        this.writer = writer;
        this.text = text;
    }
    
    public String getWriter() {
        return writer;
    }
    
    public String getText() {
        return text;
    }
    
    @Override
    public JConsole getSource() {
        return (JConsole) super.getSource();
    }
    
    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((text == null) ? 0 : text.hashCode());
        result = prime * result + ((writer == null) ? 0 : writer.hashCode());
        return result;
    }
    
    @Override
    public boolean equals(Object obj) {
        if (this == obj) { return true; }
        if (obj == null) { return false; }
        if (!(obj instanceof ConsoleEvent)) { return false; }
        ConsoleEvent other = (ConsoleEvent) obj;
        if (text == null) {
            if (other.text != null) { return false; }
        } else if (!text.equals(other.text)) { return false; }
        if (writer == null) {
            if (other.writer != null) { return false; }
        } else if (!writer.equals(other.writer)) { return false; }
        return true;
    }
    
    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("ConsoleEvent [getSource()=");
        builder.append(getSource());
        builder.append(", writer=");
        builder.append(writer);
        builder.append(", text=");
        builder.append(text);
        builder.append("]");
        return builder.toString();
    }
}
