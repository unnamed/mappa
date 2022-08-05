package team.unnamed.mappa.model.map.scheme;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.node.SchemeNode;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.FindException;
import team.unnamed.mappa.util.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;
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

    @SuppressWarnings("unchecked")
    public <T> T find(@NotNull String absolutePath, Class<T> type) throws FindException {
        if (absolutePath.isEmpty()) {
            throw new IllegalArgumentException("Cannot find mapped object from null absolute path");
        }

        if (absolutePath.length() == 1) {
            Object object = mappedObjects.get(absolutePath);
            return object.getClass() == type ? (T) object : null;
        }

        String[] nodes = absolutePath.split("\\.");
        return MapUtils.find(mappedObjects, type, nodes, 0);
    }

    public void removeProperty(String path) throws FindException {
        if (!path.contains(".")) {
            rawProperties.remove(path);
            return;
        }

        int lastDot = path.lastIndexOf(".");
        String name = path.substring(lastDot + 1);
        String[] split = path
            .substring(0, lastDot)
            .split("\\.");
        MapUtils.remove(rawProperties, split, name, 0);
    }

    public void putProperty(String path, MapProperty property) throws FindException {
        MapUtils.put(rawProperties, path.split("\\."), property.getName(), property, 0);
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
