package team.unnamed.mappa.throwable;

public class InvalidFormatException extends Exception {

    public InvalidFormatException(String errMessage) {
        super(errMessage);
    }

    public InvalidFormatException(String errMessage, Throwable t) {
        super(errMessage, t);
    }

    public InvalidFormatException(Throwable t) {
        super(t);
    }
}
