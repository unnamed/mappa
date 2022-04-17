package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.Text;

public class InvalidPropertyException extends ParseException {

    public InvalidPropertyException(Text text) {
        super(text);
    }

    public InvalidPropertyException(Text text, Throwable t) {
        super(text, t);
    }

    public InvalidPropertyException(Throwable t) {
        super(t);
    }
}
