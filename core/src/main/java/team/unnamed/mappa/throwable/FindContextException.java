package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;

public class FindContextException extends ParseException {

    public FindContextException(String errMessage) {
        super(Text.with(errMessage, false));
    }

    public FindContextException(TextNode errMessage) {
        super(errMessage);
    }
}
