package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.TextNode;

public class ParseException extends Exception {
    private final TextNode textNode;

    public ParseException(TextNode textNode) {
        super(textNode.getNode());
        this.textNode = textNode;
    }

    public ParseException(TextNode textNode, Throwable t) {
        super(textNode.getNode(), t);
        this.textNode = textNode;
    }

    public ParseException(String node) {
        this(TextNode.with(node));
    }

    public ParseException(String node, Throwable t) {
        this(TextNode.with(node));
    }

    public ParseException(Throwable t) {
        super(t);
        this.textNode = null;
    }

    public TextNode getTextNode() {
        return textNode;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return textNode.isFormal() ? this : super.fillInStackTrace();
    }
}
