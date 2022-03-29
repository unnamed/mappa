package team.unnamed.mappa.throwable;

import team.unnamed.mappa.object.Text;

public class InvalidPropertyException extends ParseException {

    public InvalidPropertyException(String errMessage, Object... objects) {
        super(Text.withFormal(errMessage, objects));
    }

    public InvalidPropertyException(String errMessage, Throwable t) {
        super(Text.withFormal(errMessage), t);
    }

    public InvalidPropertyException(Throwable t) {
        super(t);
    }
}
