package team.unnamed.mappa.internal.command.parts;

import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;

import java.util.Map;

public enum PropertyType {
    PROPERTY(MapProperty.class),
    COLLECTION(MapCollectionProperty.class),
    SECTION(Map.class),
    ALL(null);

    private final Class<?> clazz;


    PropertyType(Class<?> clazz) {
        this.clazz = clazz;
    }

    public boolean typeEquals(Object o) {
        return o != null && (clazz == null || clazz.isAssignableFrom(o.getClass()));
    }
}
