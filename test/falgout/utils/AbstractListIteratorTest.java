package falgout.utils;

import static org.junit.Assert.assertEquals;

import java.util.ListIterator;

import org.junit.Before;
import org.junit.Test;

public class AbstractListIteratorTest extends AbstractIteratorTest {
    @Override
    @Before
    public void init() {
        itr = new AbstractListIterator<String>() {
            @Override
            protected int getNextIndex(String last, int index) {
                return index + 1;
            }
            
            @Override
            protected int getPreviousIndex(String last, int index) {
                return index - 1;
            }
            
            @Override
            protected String getElement(int index) {
                return elements.get(index);
            }
            
            @Override
            protected int getUpperBound() {
                return elements.size();
            }
        };
    }
    
    @Override
    public void NullIteratorWontWork() {
        new AbstractListIterator<Object>() {
            @Override
            protected int getNextIndex(Object last, int index) {
                return 0;
            }
            
            @Override
            protected int getPreviousIndex(Object last, int index) {
                return -1;
            }
            
            @Override
            protected Object getElement(int index) {
                return null;
            }
            
            @Override
            protected int getUpperBound() {
                return 0;
            }
        }.next();
    }
    
    @Test
    public void IteratingBackwards() {
        while (itr.hasNext()) {
            itr.next();
        }
        
        ListIterator<String> it = (ListIterator<String>) itr;
        int i = elements.size() - 1;
        while (it.hasPrevious()) {
            assertEquals(elements.get(i--), it.previous());
        }
    }
}
