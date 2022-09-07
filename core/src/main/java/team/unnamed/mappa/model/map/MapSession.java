package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.ParseException;

public interface MapSession {

    void setId(String id);

    void setWarning(boolean b);

    boolean containsProperty(String property);

    MapSession property(String property, Object value) throws ParseException;

    String getId();

    String getSchemeName();

    MapScheme getScheme();

    boolean isWarning();

    @NotNull
    @Override
    default Iterator<MapProperty> iterator() {
        return new MapTypeIterator<>(MapProperty.class, getRawProperties());
    }
}
