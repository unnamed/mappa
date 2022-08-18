package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;

public class FindException extends ParseException {

    public FindException(String errMessage) {
        super(Text.with(errMessage));
    }

    public FindException(TextNode errMessage) {
        super(errMessage);
    }
}
