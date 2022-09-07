package team.unnamed.mappa.object;

import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;

/**
 * {@link Map} iterator to find a specific type of objects.
 * <p>It will iterate each sub-map too if it finds someone.
 *
 * @param <T> Object type filter
 */
// This is going be the most sh1tt1est anti-generic code you've ever seen.
// (if Mappa isn't)
@SuppressWarnings({"rawtypes", "unchecked"})
public class MapTypeIterator<T> implements Iterator<T> {
    protected final Class<T> type;
    protected final Iterator<Map.Entry> rawIterator;
    protected MapTypeIterator<T> childIterator;
    protected boolean ends;
    protected T last;

    public MapTypeIterator(Class<T> type, Map rawMaps) {
        this(type, rawMaps.entrySet().iterator());
    }

    public MapTypeIterator(Class<T> type, Iterator<Map.Entry> rawIterator) {
        this.type = type;
        this.rawIterator = rawIterator;
    }

    @Override
    public boolean hasNext() {
        if (ends) {
            return false;
        }

        // Iterate all entries and sub-maps to found
        // the first object that matches generic type T
        boolean found = false;
        while (rawIterator.hasNext()) {
            Map.Entry entry = rawIterator.next();
            Object value = entry.getValue();
            if (type.isAssignableFrom(value.getClass())) {
                last = (T) value;
                found = true;
                break;
            } else if (value instanceof Map) {
                Map child = (Map) value;
                childIterator = new MapTypeIterator<>(type, child.entrySet().iterator());
                if (childIterator.hasNext()) {
                    last = childIterator.last;
                    found = true;
                    break;
                }
            }
        }

        if (!found) {
            ends = true;
        }
        return ends;
    }

    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException();
        }
        return last;
    }
}
