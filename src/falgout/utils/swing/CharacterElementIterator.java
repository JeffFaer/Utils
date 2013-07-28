package falgout.utils.swing;

import javax.swing.text.Element;
import javax.swing.text.StyledDocument;

public class CharacterElementIterator extends ElementIterator {
    public CharacterElementIterator(StyledDocument d, int initialIndex) {
        super(d, initialIndex);
    }
    
    public CharacterElementIterator(StyledDocument d) {
        super(d);
    }
    
    @Override
    protected Element getElement(int index) {
        return getDocument().getCharacterElement(index);
    }
}
