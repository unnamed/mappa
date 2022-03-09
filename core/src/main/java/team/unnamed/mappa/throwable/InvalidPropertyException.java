package team.unnamed.mappa.throwable;

public class InvalidPropertyException extends ParseException {

    public InvalidPropertyException(String errMessage) {
        super(errMessage);
    }

    public InvalidPropertyException(String errMessage, Throwable t) {
        super(errMessage, t);
    }

    public InvalidPropertyException(Throwable t) {
        super(t);
    }
}
