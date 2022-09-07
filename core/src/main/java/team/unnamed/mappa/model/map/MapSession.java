package team.unnamed.mappa.model.map;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.object.MapTypeIterator;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Iterator;
import java.util.Map;

public interface MapSession extends Iterable<MapProperty> {

    void setId(String id);

    void setWarning(boolean b);

    boolean containsProperty(String property);

    MapSession property(String property, Object value) throws ParseException;

    String getId();

    String getSchemeName();

    Map<String, Object> getRawProperties();

    MapScheme getScheme();

    boolean isWarning();

    @NotNull
    @Override
    default Iterator<MapProperty> iterator() {
        return new MapTypeIterator<>(MapProperty.class, getRawProperties());
    }
}
