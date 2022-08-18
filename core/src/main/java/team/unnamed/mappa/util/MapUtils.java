package team.unnamed.mappa.util;

import team.unnamed.mappa.throwable.FindException;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public interface MapUtils {

    static <T> T find(Map<String, Object> mapped, Class<T> type, String[] nodes, int index) throws FindException {
        String node = nodes[index];
        Object object = mapped.get(node);
        if (object == null) {
            throw new FindException(
                "Trying to find object " + type.getSimpleName() + " from path " + String.join(".", nodes)
                    + ", found null at index " + index + " (" + node + ")"
            );
        } else if (type.isAssignableFrom(object.getClass())
            // +1 to match nodes length
            && index + 1 == nodes.length) {

            return (T) object;
        } else if (object instanceof Map) {
            if (index == nodes.length - 1) {
                throw new FindException(
                    "Trying to find object " + type.getSimpleName() + " from absolute path " + String.join(".", nodes)
                        + ", found a map" + " (" + node + ")");
            }

            return find((Map<String, Object>) object, type, nodes, ++index);
        } else {
            throw new FindException(
                "Trying to find object " + type.getSimpleName() + " from absolute path " + String.join(".", nodes)
                    + ", found an unknown object (" + object.getClass().getSimpleName() + ")");
        }
    }

    static void put(Map<String, Object> mapped, String[] nodes, String key, Object value, int index) throws FindException {
        String node = nodes[index];
        int length = nodes.length - 1;

        if (index == length) {
            mapped.put(key, value);
            return;
        }

        Object object = mapped.get(node);
        if (object instanceof Map) {
            Map<String, Object> map = (Map<String, Object>) object;
            put(map, nodes, key, value, ++index);
        } else {
            Map<String, Object> newMapped = new LinkedHashMap<>();
            mapped.put(node, newMapped);

            put(newMapped, nodes, key, value, ++index);
        }
    }

    static void remove(Map<String, Object> mapped, String[] nodes, String key, int index) throws FindException {
        String node = nodes[index];
        Object object = mapped.get(node);
        int length = nodes.length - 1;
        if (object instanceof Map) {
            if (index == length) {
                Map<String, Object> map = (Map<String, Object>) object;
                if (key == null) {
                    map.clear();
                } else {
                    map.remove(key);
                }
                return;
            }

            remove((Map<String, Object>) object, nodes, key, ++index);
        } else {
            if (index == length) {
                mapped.remove(key);
                return;
            }

            throw new FindException(
                "Trying to remove object from path " + String.join(".", nodes)
                    + ", index ends at " + index);
        }
    }
}
