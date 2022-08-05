package team.unnamed.mappa.util;

import team.unnamed.mappa.throwable.FindException;

import java.util.Map;

@SuppressWarnings("unchecked")
public interface MapUtils {

    static  <T> T find(Map<String, Object> mapped, Class<T> type, String[] nodes, int index) throws FindException {
        String node = nodes[index];
        if (index == 0) {
            if (nodes.length - 1 <= index) {
                throw new FindException(
                    "Trying to find object from path " + String.join(".", node)
                        + ", index ends at " + index
                );
            }
            return find(mapped, type, nodes, ++index);
        }
        Object object = mapped.get(node);
        if (object == null) {
            throw new FindException(
                "Trying to find object from path " + String.join(".", node)
                    + ", found null at index " + index
            );
        } else if (type.isAssignableFrom(object.getClass())
            // +1 to match nodes length
            && index + 1 == nodes.length) {

            return (T) object;
        } else if (object instanceof Map) {
            if (index == node.length() - 1) {
                throw new FindException(
                    "Trying to find object from absolute path " + String.join(".", node)
                        + ", found a map");
            }

            return find((Map<String, Object>) object, type, nodes, ++index);
        } else {
            throw new FindException(
                "Trying to find object from absolute path " + String.join(".", node)
                    + ", found an unknown object (" + object.getClass().getSimpleName() + ")");
        }
    }

    static void put(Map<String, Object> mapped, String[] nodes, String key, Object value, int index) throws FindException {
        String node = nodes[index];
        if (index == 0) {
            if (nodes.length - 1 <= index) {
                throw new FindException(
                    "Trying to find object from path " + String.join(".", node)
                        + ", index ends at " + index
                );
            }
            put(mapped, nodes, key, value, ++index);
            return;
        }
        Object object = mapped.get(node);
        if (object == null) {
            throw new FindException(
                "Trying to find object from path " + String.join(".", node)
                    + ", found null at index " + index
            );
        } else if (object instanceof Map) {
            if (index == node.length() - 1) {
                Map<String, Object> map = (Map<String, Object>) object;
                map.put(key, value);
                return;
            }

            put((Map<String, Object>) object, nodes, key, value, ++index);
        } else {
            throw new FindException(
                "Trying to find object from absolute path " + String.join(".", node)
                    + ", found an unknown object (" + object.getClass().getSimpleName() + ")");
        }
    }

    static void remove(Map<String, Object> mapped, String[] nodes, String key, int index) throws FindException {
        String node = nodes[index];
        if (index == 0) {
            if (nodes.length - 1 <= index) {
                throw new FindException(
                    "Trying to find object from path " + String.join(".", node)
                        + ", index ends at " + index
                );
            }

            remove(mapped, nodes, key, ++index);
            return;
        }
        Object object = mapped.get(node);
        if (object == null) {
            throw new FindException(
                "Trying to find object from path " + String.join(".", node)
                    + ", found null at index " + index
            );
        } else if (object instanceof Map) {
            if (index == node.length() - 1) {
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
            throw new FindException(
                "Trying to find object from absolute path " + String.join(".", node)
                    + ", found an unknown object (" + object.getClass().getSimpleName() + ")");
        }
    }
}
