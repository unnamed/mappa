package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.TextNode;

public class ParseRuntimeException extends RuntimeException {
    private final TextNode textNode;

    public ParseRuntimeException(TextNode textNode) {
        super(textNode.getNode());
        this.textNode = textNode;
    }

    public ParseRuntimeException(TextNode textNode, Throwable t) {
        super(textNode.getNode(), t);
        this.textNode = textNode;
    }

    public ParseRuntimeException(Throwable t) {
        super(t);
        this.textNode = null;
    }

    public TextNode getTextNode() {
        return textNode;
    }
}
