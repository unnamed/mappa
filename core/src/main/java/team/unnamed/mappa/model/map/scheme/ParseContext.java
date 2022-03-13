package team.unnamed.mappa.model.map.scheme;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.FindContextException;

import java.util.LinkedHashMap;
import java.util.Map;

public class ParseContext {
    public static final String BUILD_PROPERTIES = "build-properties";

    protected final String schemeName;
    protected String currentPath = "";

    protected SchemeNode currentNode;
    protected final Map<String, Object> parseConfiguration;
    protected final Map<String, Object> mappedObjects;
    protected final Map<String, MapProperty> properties;

    public ParseContext(String schemeName,
                        Map<String, Object> mappedObjects,
                        Map<String, MapProperty> properties) {
        this.schemeName = schemeName;
        this.mappedObjects = mappedObjects;
        this.properties = properties;
        this.parseConfiguration = new LinkedHashMap<>();
    }

    @SuppressWarnings("unchecked")
    public <T> T find(@NotNull String absolutePath, Class<T> type) throws FindContextException {
        if (absolutePath.isEmpty()) {
            throw new IllegalArgumentException("Cannot find mapped object from null absolute path");
        }

        if (absolutePath.length() == 1) {
            Object object = mappedObjects.get(absolutePath);
            return object.getClass() == type ? (T) object : null;
        }

        String[] nodes = absolutePath.split("\\.");
        return find(mappedObjects, type, nodes, 0);
    }

    @SuppressWarnings("unchecked")
    private <T> T find(Map<String, Object> mapped, Class<T> type, String[] nodes, int index) throws FindContextException {
        String node = nodes[index];
        if (node.equals(schemeName) && index == 0) {
            if (nodes.length - 1 <= index) {
                throw new FindContextException(
                    "Trying to find object from path " + String.join(".", node)
                        + ", index ends at " + index
                );
            }
            return find(mapped, type, nodes, ++index);
        }
        Object object = mapped.get(node);
        if (object == null) {
            throw new FindContextException(
                "Trying to find object from path " + String.join(".", node)
                    + ", found null at index " + index
            );
        } else if (type.isAssignableFrom(object.getClass())
            // +1 to match nodes length
            && index + 1 == nodes.length) {

            return (T) object;
        } else if (object instanceof Map) {
            if (index == node.length() - 1) {
                throw new FindContextException(
                    "Trying to find object from absolute path " + String.join(".", node)
                        + ", found a map");
            }

            return find((Map<String, Object>) object, type, nodes, ++index);
        } else {
            throw new FindContextException(
                "Trying to find object from absolute path " + String.join(".", node)
                    + ", found an unknown object (" + object.getClass().getSimpleName() + ")");
        }
    }

    public void setCurrentNode(SchemeNode currentNode) {
        this.currentNode = currentNode;
    }

    public void setCurrentPath(String currentPath) {
        this.currentPath = currentPath;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public String getCurrentPath() {
        return currentPath;
    }

    public SchemeNode getCurrentNode() {
        return currentNode;
    }

    public String getAbsolutePath() {
        return currentNode != null
            ? currentPath + "." + currentNode.getName()
            : currentPath;
    }

    public Map<String, Object> getParseConfiguration() {
        return parseConfiguration;
    }

    public Map<String, Object> getMappedObjects() {
        return mappedObjects;
    }

    public Map<String, MapProperty> getProperties() {
        return properties;
    }
}
