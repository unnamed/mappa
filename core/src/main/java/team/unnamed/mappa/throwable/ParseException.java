package team.unnamed.mappa.throwable;

public class ParseException extends Exception {

    public ParseException(String errMessage) {
        super(errMessage);
    }

    public ParseException(String errMessage, Throwable t) {
        super(errMessage, t);
    }

    public ParseException(Throwable t) {
        super(t);
    }
}
