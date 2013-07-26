package falgout.utils.swing;

import java.awt.Component;
import java.awt.Container;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.FutureTask;

import javax.swing.SwingUtilities;

public final class SwingUtils {
    private SwingUtils() {}
    
    public static <C extends Container> C getParent(Class<C> clazz, Component c) {
        Container parent;
        while ((parent = c.getParent()) != null && !clazz.isInstance(parent)) {
            c = parent;
        }
        
        return parent == null ? null : clazz.cast(parent);
    }
    
    public static void runOnEDT(final Runnable r) throws InterruptedException, ExecutionException {
        runOnEDT(new Callable<Void>() {
            @Override
            public Void call() throws Exception {
                r.run();
                return null;
            }
        });
    }
    
    public static <T> T runOnEDT(Callable<T> c) throws InterruptedException, ExecutionException {
        if (SwingUtilities.isEventDispatchThread()) {
            try {
                return c.call();
            } catch (Exception e) {
                throw new ExecutionException(e);
            }
        } else {
            FutureTask<T> t = new FutureTask<>(c);
            SwingUtilities.invokeLater(t);
            return t.get();
        }
    }
}
