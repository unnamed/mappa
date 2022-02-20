package team.unnamed.mappa.object.throwable;

public class RegionParseException extends Exception {

    public RegionParseException(String errMessage) {
        super(errMessage);
    }

    public RegionParseException(String errMessage, Throwable t) {
        super(errMessage, t);
    }
}
