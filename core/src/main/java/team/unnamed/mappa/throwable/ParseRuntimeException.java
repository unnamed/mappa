package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.Text;

public class ParseRuntimeException extends RuntimeException {
    private final Text textNode;

    public ParseRuntimeException(Text textNode) {
        super(textNode.getNode());
        this.textNode = textNode;
    }

    public ParseRuntimeException(Text textNode, Throwable t) {
        super(textNode.getNode(), t);
        this.textNode = textNode;
    }

    public ParseRuntimeException(Throwable t) {
        super(t);
        this.textNode = null;
    }

    public Text getTextNode() {
        return textNode;
    }
}
