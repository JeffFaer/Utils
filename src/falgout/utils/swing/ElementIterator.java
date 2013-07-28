package falgout.utils.swing;

import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

import falgout.utils.AbstractListIterator;

public abstract class ElementIterator extends AbstractListIterator<Element> {
    private final StyledDocument d;
    
    public ElementIterator(StyledDocument d) {
        this(d, 0);
    }
    
    public ElementIterator(StyledDocument d, int initialIndex) {
        super(initialIndex);
        this.d = d;
    }
    
    public StyledDocument getDocument() {
        return d;
    }
    
    @Override
    protected int getNextIndex(Element last, int index) {
        return last.getEndOffset();
    }
    
    @Override
    protected int getPreviousIndex(Element last, int index) {
        int i = last.getStartOffset() - 1;
        return i < 0 ? i : getElement(i).getStartOffset();
    }
    
    @Override
    protected int getUpperBound() {
        return d.getLength();
    }
}
