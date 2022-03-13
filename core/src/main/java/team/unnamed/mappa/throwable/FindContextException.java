package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.TextNode;

public class FindContextException extends ParseException {

    public FindContextException(String errMessage) {
        super(TextNode.with(errMessage, false));
    }

    public FindContextException(TextNode errMessage) {
        super(errMessage);
    }
}
