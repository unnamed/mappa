package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.TextNode;

public class InvalidPropertyException extends ParseException {

    public InvalidPropertyException(String errMessage, Object... objects) {
        super(TextNode.withFormal(errMessage, objects));
    }

    public InvalidPropertyException(String errMessage, Throwable t) {
        super(TextNode.withFormal(errMessage), t);
    }

    public InvalidPropertyException(Throwable t) {
        super(t);
    }
}
