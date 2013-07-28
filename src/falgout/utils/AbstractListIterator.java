package falgout.utils;

import java.util.ListIterator;
import java.util.NoSuchElementException;

public abstract class AbstractListIterator<E> extends AbstractIterator<E> implements ListIterator<E> {
    private int previousIndex;
    private int nextIndex;
    protected int lastIndex;
    protected E previous;
    
    public AbstractListIterator() {
        this(0);
    }
    
    public AbstractListIterator(int initialIndex) {
        lastIndex = initialIndex;
    }
    
    @Override
    protected boolean init() {
        boolean init = super.init();
        if (init) {
            previous = findPrevious(true);
        }
        
        return init;
    }
    
    @Override
    public boolean hasPrevious() {
        init();
        return previous != null;
    }
    
    @Override
    public E previous() {
        if (previous == null) {
            init();
            if (previous == null) { throw new NoSuchElementException(); }
        }
        nextIndex = previousIndex;
        next = previous;
        lastIndex = previousIndex;
        last = previous;
        previous = findPrevious();
        
        return last;
    }
    
    @Override
    public E next() {
        previousIndex = nextIndex;
        previous = next;
        lastIndex = nextIndex;
        return super.next();
    }
    
    @Override
    public int nextIndex() {
        init();
        return nextIndex;
    }
    
    @Override
    public int previousIndex() {
        init();
        return previousIndex;
    }
    
    @Override
    public void set(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    public void add(E e) {
        throw new UnsupportedOperationException();
    }
    
    @Override
    protected E findNext() {
        nextIndex = init ? getNextIndex(last, lastIndex) : lastIndex;
        return getElement(nextIndex);
    }
    
    protected E findPrevious() {
        return findPrevious(false);
    }
    
    private E findPrevious(boolean useLastIndex) {
        previousIndex = useLastIndex ? lastIndex : getPreviousIndex(last, lastIndex);
        return getElement(previousIndex);
    }
    
    protected abstract int getNextIndex(E last, int index);
    
    protected abstract int getPreviousIndex(E last, int index);
    
    protected abstract E getElement(int index);
}
