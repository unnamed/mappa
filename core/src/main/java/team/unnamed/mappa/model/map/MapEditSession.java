package team.unnamed.mappa.model.map;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.Key;
import team.unnamed.mappa.model.map.scheme.MapPropertyTree;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.FindException;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.*;
import java.util.function.Function;

public class MapEditSession implements MapSession {
    private String id;
    private boolean warning;

    private final MapPropertyTree properties;
    private final Map<String, Object> parseConfiguration;

    private final String schemeName;
    private final MapScheme scheme;

    private Deque<String> setupQueue;

    public MapEditSession(String id, MapScheme scheme) throws ParseException {
        this.id = id;
        this.scheme = scheme;
        this.schemeName = scheme.getName();
        this.properties = scheme.getTreeProperties().cloneBy(this);
        this.parseConfiguration = new LinkedHashMap<>(scheme.getParseConfiguration());
    }

    @Override
    public void setId(String id) {
        this.id = id;
    }

    @Override
    public void setWarning(boolean warning) {
        this.warning = warning;
    }

    @Override
    public MapSession property(String propertyName, Object value) throws ParseException {
        MapProperty property = properties.find(propertyName);
        if (property == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        property.parseValue(value);
        return this;
    }

    public MapSession metadataProperty(String metadataProperty, Object value) throws ParseException {
        String propertyPath = getMetadataPath(metadataProperty);
        if (propertyPath == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", metadataProperty,
                        "{scheme}", schemeName));
        }
        MapProperty property = properties.find(propertyPath);
        property.parseValue(value);
        return this;
    }

    @Override
    public MapSession cleanProperty(String propertyName) throws ParseException {
        MapProperty property = properties.find(propertyName);
        if (property == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName));
        }
        property.clearValue();
        return this;
    }

    @Override
    public boolean removePropertyValue(String propertyName, Object value) throws ParseException {
        MapProperty property = properties.find(propertyName);
        if (!(property instanceof MapCollectionProperty)) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        MapCollectionProperty listProperty = (MapCollectionProperty) property;
        return listProperty.remove(value);
    }

    private MapSession removeMetadataPropertyValue(String propertyName, Object value) throws ParseException {
        String propertyPath = getMetadataPath(propertyName);
        if (propertyPath == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        MapProperty property = properties.find(propertyPath);
        if (!(property instanceof MapCollectionProperty)) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        MapCollectionProperty listProperty = (MapCollectionProperty) property;
        listProperty.remove(value);
        return this;
    }

    public boolean isSet(MapProperty mapProperty) {
        if (mapProperty instanceof MapCollectionProperty) {
            MapCollectionProperty list = (MapCollectionProperty) mapProperty;
            return !list.isEmpty();
        }
        return mapProperty.getValue() != null;
    }

    @Override
    public boolean containsProperty(String property) {
        MapProperty mapProperty = properties.tryFind(property);
        return isSet(mapProperty);
    }

    public boolean containsMetadataProperty(String property) {
        return containsProperty(getMetadataPath(property));
    }

    @Override
    public boolean setup() {
        if (setupQueue == null) {
            Set<String> keys = scheme.getObject(MapScheme.PLAIN_KEYS);
            this.setupQueue = new ArrayDeque<>(keys);
        }

        this.setupQueue.removeIf(property -> {
            MapProperty mapProperty = getProperty(property);
            if (mapProperty instanceof MapCollectionProperty) {
                MapCollectionProperty collectionProperty = (MapCollectionProperty) mapProperty;
                return !collectionProperty.isEmpty() || mapProperty.isImmutable();
            }
            return mapProperty.getValue() != null || mapProperty.isImmutable();
        });

        return this.setupQueue.peekFirst() != null;
    }

    @Override
    public String currentSetup() {
        if (setupQueue == null) {
            throw new IllegalStateException("setup queue is null!");
        }

        return this.setupQueue.peekFirst();
    }

    @Override
    public String skipSetup() {
        if (setupQueue == null) {
            throw new IllegalStateException("setup queue is null!");
        }

        this.setupQueue.pollFirst();
        return this.setupQueue.peekFirst();
    }

    @Override
    public Map<String, Text> checkWithScheme() throws ParseException {
        return checkWithScheme(true);
    }

    @Override
    public Map<String, Text> checkWithScheme(boolean failFast) throws ParseException {
        Map<String, Text> errors = new LinkedHashMap<>();
        Set<String> keys = scheme.getObject(MapScheme.PLAIN_KEYS);
        for (String path : keys) {
            MapProperty property = properties.find(path);
            if (property.isOptional() || property.isImmutable()) {
                continue;
            }

            if (!isSet(property) && !property.hasVerification()) {
                Text node = TranslationNode
                    .UNDEFINED_PROPERTY
                    .with("{property}", property.getName());
                errors.put(path, node);
                if (failFast) {
                    break;
                }
            }

            Text errMessage = property.verify(this);
            if (errMessage != null) {
                errors.put(path, errMessage);
                if (failFast) {
                    break;
                }
            }

        }
        return errors;
    }

    public Deque<String> getSetupQueue() {
        return setupQueue;
    }

    @Override
    public String getId() {
        return id;
    }

    @Override
    public String getDate() {
        return getMetadataValue("date");
    }

    @Override
    public String getMapName() {
        return getMetadataValue("name");
    }

    @Override
    public String getWorldName() {
        return getMetadataValue("world");
    }

    @Override
    public String getVersion() {
        return getMetadataValue("version");
    }

    @Override
    public Collection<String> getAuthors() {
        return getMetadataValue("author");
    }

    @Override
    public String getSchemeName() {
        return schemeName;
    }

    @Override
    public Map<String, Object> getRawProperties() {
        return properties.getRawMaps();
    }

    @Override
    public MapScheme getScheme() {
        return scheme;
    }

    @Override
    public MapPropertyTree getProperties() {
        return properties;
    }

    public Map<String, Object> getParseConfiguration() {
        return parseConfiguration;
    }

    @Override
    public MapProperty getProperty(String node) {
        return properties.tryFind(node);
    }

    @Override
    public MapProperty getCheckProperty(String node) throws FindException {
        return properties.find(node);
    }

    public Map<String, String> getMetadata() {
        return getObject(ParseContext.METADATA);
    }

    public String getMetadataPath(String metadataName) {
        Map<String, String> metadata = getMetadata();
        return metadata == null || !metadata.containsKey(metadataName)
            ? null
            : metadata.get(metadataName);
    }

    public <T> T getMetadataValue(String metadataName) {
        String property = getMetadataPath(metadataName);
        return getPropertyValue(property);
    }

    public MapProperty getMetadataProperty(String metadataName) {
        String property = getMetadataPath(metadataName);
        return properties.tryFind(property);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyValue(String node) {
        MapProperty property = getProperty(node);
        return (T) property.getValue();
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(Key<T> key) {
        return (T) parseConfiguration.get(key.getName());
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> T getObject(Key<T> key, Function<String, T> provide) {
        return (T) parseConfiguration.computeIfAbsent(key.getName(), provide);
    }

    @Override
    public boolean isWarning() {
        return warning;
    }

    @Override
    public @NotNull Iterator<MapProperty> iterator() {
        return properties.iterator();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MapEditSession that = (MapEditSession) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }
}
