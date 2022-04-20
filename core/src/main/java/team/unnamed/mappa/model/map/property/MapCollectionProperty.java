package team.unnamed.mappa.model.map.property;

import java.lang.reflect.Type;

public interface MapCollectionProperty extends MapProperty {

    Object getValue(int slot);

    boolean remove(Object value);

    boolean isEmpty();

    Type getCollectionType();
}
