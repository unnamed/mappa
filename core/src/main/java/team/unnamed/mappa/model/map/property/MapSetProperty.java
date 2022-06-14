package team.unnamed.mappa.model.map.property;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class MapSetProperty extends AbstractMapCollectionProperty{
    private final Set<Object> setValue;

    public MapSetProperty(MapNodeProperty<?> delegate) {
        this(new LinkedHashSet<>(), delegate);
    }

    public MapSetProperty(Set<Object> values, MapNodeProperty<?> delegate) {
        super(values, delegate);

        this.setValue = values;
    }

    @Override
    public Object getValue(int slot) {
        Iterator<Object> iterator = setValue.iterator();
        int index = 0;
        Object object = null;
        while (index < slot) {
            object = iterator.next();
            ++index;
        }
        return object;
    }

    @Override
    public MapProperty clone() {
        return new MapSetProperty(delegate);
    }
}
