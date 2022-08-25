package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.Text;

public class ParseException extends Exception {
    private final Text textNode;

    public ParseException(Text textNode) {
        super(textNode.toString());
        this.textNode = textNode;
    }

    public ParseException(Text textNode, Throwable t) {
        super(textNode.toString(), t);
        this.textNode = textNode;
    }

    public ParseException(String node) {
        this(Text.with(node));
    }

    public ParseException(String node, Throwable t) {
        this(Text.with(node), t);
    }

    public ParseException(Throwable t) {
        super(t);
        this.textNode = null;
    }

    public Text getTextNode() {
        return textNode;
    }

    @Override
    public synchronized Throwable fillInStackTrace() {
        return realStackTrace();
    }

    public synchronized Throwable realStackTrace() {
        return super.fillInStackTrace();
    }
}
