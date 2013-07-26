package falgout.utils.swing;

import javax.swing.text.Style;
import javax.swing.text.StyledDocument;

public class StyledDocumentAppender extends DocumentAppender {
    private final String styleName;
    
    public StyledDocumentAppender(StyledDocument doc, String styleName) {
        super(doc);
        this.styleName = styleName;
    }
    
    @Override
    public StyledDocument getDocument() {
        return (StyledDocument) super.getDocument();
    }
    
    public String getStyleName() {
        return styleName;
    }
    
    public Style getStyle() {
        return getDocument().getStyle(styleName);
    }
    
    @Override
    protected Style getAttributeSet() {
        return getStyle();
    }
}
