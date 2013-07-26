package falgout.utils.swing;

import java.awt.Component;
import java.awt.Container;

public class SwingUtils {
    public static <C extends Container> C getParent(Class<C> clazz, Component c) {
        Container parent;
        while ((parent = c.getParent()) != null && !clazz.isInstance(parent)) {
            c = parent;
        }
        
        return parent == null ? null : clazz.cast(parent);
    }
}
