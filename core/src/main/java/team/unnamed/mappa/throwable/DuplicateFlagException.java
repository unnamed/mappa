package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TextNode;

public class DuplicateFlagException extends ParseException {

    public DuplicateFlagException(TextNode errMessage) {
        super(errMessage);
    }

    public DuplicateFlagException(TextNode errMessage, Throwable t) {
        super(errMessage, t);
    }

    public DuplicateFlagException(String errMessage) {
        super(Text.with(errMessage));
    }

    public DuplicateFlagException(String errMessage, Throwable t) {
        super(Text.with(errMessage), t);
    }
}
