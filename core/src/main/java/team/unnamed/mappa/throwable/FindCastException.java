package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.TextNode;

public class FindCastException extends FindException {
    public FindCastException(String errMessage) {
        super(errMessage);
    }

    public FindCastException(TextNode errMessage) {
        super(errMessage);
    }
}
