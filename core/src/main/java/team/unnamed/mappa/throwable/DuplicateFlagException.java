package team.unnamed.mappa.throwable;

public class DuplicateFlagException extends ParseException {

    public DuplicateFlagException(String errMessage) {
        super(errMessage);
    }

    public DuplicateFlagException(String errMessage, Throwable t) {
        super(errMessage, t);
    }
}
