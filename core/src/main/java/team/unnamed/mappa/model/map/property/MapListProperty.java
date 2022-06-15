package team.unnamed.mappa.model.map.property;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class MapListProperty extends AbstractMapCollectionProperty {
    private final List<Object> listValue;

    public MapListProperty(MapNodeProperty<?> delegate) {
        this(new ArrayList<>(), delegate);
    }

    public MapListProperty(List<Object> listValue, MapNodeProperty<?> delegate) {
        super(listValue, delegate);

        this.listValue = listValue;
    }

    @Override
    public Object getValue(int slot) {
        return listValue.get(slot);
    }

    @Override
    public Type getCollectionType() {
        return List.class;
    }

    @Override
    public MapProperty clone() {
        return new MapListProperty(delegate);
    }
}
