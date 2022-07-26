package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.property.MapCollectionProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.object.Text;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.*;

public class MapEditSession implements MapSession {
    private String id;
    private boolean warning;

    private final Map<String, MapProperty> properties;
    private final Map<String, Object> parseConfiguration;

    private final String schemeName;
    private final MapScheme scheme;

    private Deque<String> setupQueue;

    public MapEditSession(String id, MapScheme scheme) {
        this.id = id;
        this.scheme = scheme;
        this.schemeName = scheme.getName();
        this.properties = new LinkedHashMap<>();
        this.parseConfiguration = new LinkedHashMap<>(scheme.getParseConfiguration());
        scheme.getProperties()
            .forEach((key, value) -> {
                MapProperty clone = value.clone();

                if (clone.isImmutable()) {
                    clone.applyDefaultValue(this);
                }

                this.properties.put(key, clone);
            });
    }

    public void setId(String id) {
        this.id = id;
    }

    public MapSession addAuthor(String author) throws ParseException {
        return buildProperty("author", author);
    }

    public MapSession mapName(String mapName) throws ParseException {
        return buildProperty("name", mapName);
    }

    public MapSession version(String version) throws ParseException {
        return buildProperty("version", version);
    }

    public MapSession removeAuthor(String author) throws ParseException {
        return removeBuildPropertyValue("author", author);
    }

    public MapSession property(String propertyName, Object value) throws ParseException {
        MapProperty property = properties.get(propertyName);
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

    public MapSession buildProperty(String buildProperty, Object value) throws ParseException {
        String propertyPath = getMetadataPath(buildProperty);
        if (propertyPath == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", buildProperty,
                        "{scheme}", schemeName));
        }
        MapProperty property = properties.get(propertyPath);
        property.parseValue(value);
        return this;
    }

    public MapSession cleanProperty(String propertyName) throws InvalidPropertyException {
        MapProperty property = properties.get(propertyName);
        if (property == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        property.clearValue();
        return this;
    }

    public boolean removePropertyValue(String propertyName, Object value) throws InvalidPropertyException {
        MapProperty property = properties.get(propertyName);
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

    private MapSession removeBuildPropertyValue(String propertyName, Object value) throws ParseException {
        String propertyPath = getMetadataPath(propertyName);
        if (propertyPath == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        MapProperty property = properties.get(propertyPath);
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
        MapProperty mapProperty = properties.get(property);
        return isSet(mapProperty);
    }

    public boolean containsBuildProperty(String property) {
        return containsProperty(getMetadataPath(property));
    }

    public boolean setup() {
        if (setupQueue == null) {
            this.setupQueue = new ArrayDeque<>(properties.keySet());
        }

        this.setupQueue.removeIf(this::containsProperty);
        return this.setupQueue.peekFirst() != null;
    }

    public String currentSetup() {
        if (setupQueue == null) {
            throw new IllegalStateException("setup queue is null!");
        }

        return this.setupQueue.peekFirst();
    }

    public String skipSetup() {
        if (setupQueue == null) {
            throw new IllegalStateException("setup queue is null!");
        }

        this.setupQueue.pollFirst();
        return this.setupQueue.peekFirst();
    }

    public Map<String, Text> checkWithScheme() {
        return checkWithScheme(true);
    }

    public Map<String, Text> checkWithScheme(boolean failFast) {
        Map<String, Text> errors = new LinkedHashMap<>();
        for (Map.Entry<String, MapProperty> entry : properties.entrySet()) {
            String path = entry.getKey();
            MapProperty property = entry.getValue();
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

    public String getId() {
        return id;
    }

    public String getWorldName() {
        return getMetadataValue("world");
    }

    public String getMapName() {
        return getMetadataValue("name");
    }

    public String getSchemeName() {
        return schemeName;
    }

    public MapScheme getScheme() {
        return scheme;
    }

    public String getVersion() {
        return getMetadataValue("version");
    }

    public List<String> getAuthors() {
        return getMetadataValue("author");
    }

    public Map<String, MapProperty> getProperties() {
        return properties;
    }

    public Map<String, Object> getParseConfiguration() {
        return parseConfiguration;
    }

    public MapProperty getProperty(String node) {
        return properties.get(node);
    }

    @SuppressWarnings("unchecked")
    public Map<String, String> getMetadata() {
        return (Map<String, String>)
            parseConfiguration.get(ParseContext.METADATA.getName());
    }

    public String getMetadataPath(String metadataName) {
        Map<String, String> metadata = getMetadata();
        return metadata == null || !metadata.containsKey(metadataName)
            ? null
            : metadata.get(metadataName);
    }

    public <T> T getMetadataValue(String propertyName) {
        String property = getMetadataPath(propertyName);
        return getPropertyValue(property);
    }

    public <T> T getPropertyValue(String node) {
        MapProperty property = getProperty(node);
        return (T) property.getValue();
    }

    public boolean isWarning() {
        return warning;
    }

    public void setWarning(boolean warning) {
        this.warning = warning;
    }
}
