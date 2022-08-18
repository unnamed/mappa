package team.unnamed.mappa.model.map.scheme;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.FindException;
import team.unnamed.mappa.util.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.Function;

public class ParseContext {
    public static final Key<Map<String, String>> METADATA = new Key<>("metadata");
    public static final Key<Map<String, String>> IMMUTABLE = new Key<>("immutable");

    protected final String schemeName;
    protected String currentPath = "";

    protected SchemeNode currentNode;
    protected final Map<String, Object> parseConfiguration;
    protected final Map<String, Object> mappedObjects;
    protected final Map<String, Object> rawProperties;
    protected final MapPropertyTree properties;

    public ParseContext(String schemeName,
                        Map<String, Object> rawProperties,
                        Map<String, Object> mappedObjects,
                        MapPropertyTree properties) {
        this.schemeName = schemeName;
        this.rawProperties = rawProperties;
        this.mappedObjects = mappedObjects;
        this.properties = properties;
        this.parseConfiguration = new LinkedHashMap<>();
    }

    public <T> T find(@NotNull String absolutePath, Class<T> type) throws FindException {
        return findMapped(mappedObjects, absolutePath, type);
    }

    @SuppressWarnings("unchecked")
    public <T> T findMapped(Map<String, Object> mapped, @NotNull String absolutePath, Class<T> type) throws FindException {
        if (absolutePath.isEmpty()) {
            throw new IllegalArgumentException("Cannot find mapped object " + type.getSimpleName() + " from empty absolute path");
        }

        String[] nodes = absolutePath.split("\\.");
        if (nodes.length == 1) {
            Object object = Objects.requireNonNull(mapped.get(absolutePath));
            Class<?> clazz = object.getClass();
            if (!type.isAssignableFrom(clazz)) {
                throw new FindException("Trying to find object "
                    + type.getSimpleName() + " directly from properties " +
                    "returns other object " + clazz.getSimpleName());
            }
            return (T) object;
        }

        return MapUtils.find(mapped, type, nodes, 0);
    }

    public void removeProperty(String path) throws FindException {
        if (!path.contains(".")) {
            rawProperties.remove(path);
            return;
        }

        int lastDot = path.lastIndexOf(".");
        String name = path.substring(lastDot + 1);
        String[] split = path.split("\\.");
        MapUtils.remove(rawProperties, split, name, 0);
    }

    public void putProperty(String path, MapProperty property) throws FindException {
        String[] nodes = path.split("\\.");
        if (nodes.length == 1) {
            rawProperties.put(path, property);
            return;
        }

        MapUtils.put(rawProperties,
            nodes,
            property.getName(),
            property,
            0);
    }

    public void put(String path, Object value) throws FindException {
        String[] nodes = path.split("\\.");
        if (nodes.length == 1) {
            rawProperties.put(path, value);
            return;
        }

        String name = nodes[nodes.length - 1];
        MapUtils.put(rawProperties,
            nodes,
            name,
            value,
            0);
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
        if (currentNode != null) {
            String[] split = currentPath.split("\\.");
            if (split.length > 1) {
                return currentPath + "." + currentNode.getName();
            }
        }
        return currentPath;
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Key<T> key) {
        return (T) parseConfiguration.get(key.getName());
    }

    @SuppressWarnings("unchecked")
    public <T> T getObject(Key<T> key, Function<String, T> provide) {
        return (T) parseConfiguration.computeIfAbsent(key.getName(), provide);
    }

    public Map<String, Object> getParseConfiguration() {
        return parseConfiguration;
    }

    public MapPropertyTree getTreeProperties() {
        return properties;
    }
}
