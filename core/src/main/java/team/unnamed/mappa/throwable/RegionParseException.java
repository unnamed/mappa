package team.unnamed.mappa.throwable;

public class RegionParseException extends ParseException {

    public RegionParseException(String errMessage) {
        super(errMessage);
    }

    public RegionParseException(String errMessage, Throwable t) {
        super(errMessage, t);
    }
}
