package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.property.MapListProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.object.TextNode;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.*;

public class MapSession {
    private final String id;

    private final Map<String, MapProperty> properties;
    private final Map<String, Object> parseConfiguration;

    private final String schemeName;
    private final MapScheme scheme;

    private Deque<String> setupQueue;

    public MapSession(String id, MapScheme scheme) {
        this.id = id;
        this.scheme = scheme;
        this.schemeName = scheme.getName();
        this.properties = new LinkedHashMap<>(scheme.getProperties());
        this.parseConfiguration = new LinkedHashMap<>(scheme.getParseConfiguration());
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
        return property(propertyName, value, "Undefined property {property}");
    }

    public MapSession property(String propertyName, Object value, String errMessage) throws ParseException {
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
        String propertyPath = getBuildPropertyPath(buildProperty);
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

    public MapSession removePropertyValue(String propertyName, Object value) throws InvalidPropertyException {
        MapProperty property = properties.get(propertyName);
        if (!(property instanceof MapListProperty)) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                "{scheme}", schemeName));
        }
        MapListProperty listProperty = (MapListProperty) property;
        listProperty.remove(value);
        return this;
    }

    private MapSession removeBuildPropertyValue(String propertyName, Object value) throws ParseException {
        String propertyPath = getBuildPropertyPath(propertyName);
        if (propertyPath == null) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        MapProperty property = properties.get(propertyPath);
        if (!(property instanceof MapListProperty)) {
            throw new InvalidPropertyException(
                TranslationNode
                    .INVALID_PROPERTY
                    .with("{property}", propertyName,
                        "{scheme}", schemeName));
        }
        MapListProperty listProperty = (MapListProperty) property;
        listProperty.remove(value);
        return this;
    }

    public boolean isSet(MapProperty mapProperty) {
        if (mapProperty instanceof MapListProperty) {
            MapListProperty list = (MapListProperty) mapProperty;
            return !list.isEmpty();
        }
        return mapProperty.getValue() != null;
    }

    public boolean containsProperty(String property) {
        MapProperty mapProperty = properties.get(property);
        return isSet(mapProperty);
    }

    public boolean containsBuildProperty(String property) {
        return containsProperty(getBuildPropertyPath(property));
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

    public TextNode checkWithScheme() {
        for (MapProperty property : properties.values()) {
            if (property.isOptional()) {
                continue;
            }

            if (!isSet(property)) {
                return TranslationNode.UNDEFINED_PROPERTY.formalText();
            }

            TextNode errMessage = property.verify(this);
            if (errMessage != null) {
                return errMessage;
            }
        }
        return null;
    }

    public Deque<String> getSetupQueue() {
        return setupQueue;
    }

    public String getId() {
        return id;
    }

    public String getWorldName() {
        return getBuildPropertyPath("world");
    }

    public String getMapName() {
        return getBuildPropertyValue("name");
    }

    public String getSchemeName() {
        return schemeName;
    }

    public MapScheme getScheme() {
        return scheme;
    }

    public String getVersion() {
        return getBuildPropertyValue("version");
    }

    public List<String> getAuthors() {
        return getBuildPropertyValue("author");
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
    public Map<String, String> getBuildProperties() {
        return (Map<String, String>) parseConfiguration.get(ParseContext.BUILD_PROPERTIES);
    }

    public String getBuildPropertyPath(String propertyName) {
        Map<String, String> buildProperties = getBuildProperties();
        return buildProperties == null || !buildProperties.containsKey(propertyName)
            ? null
            : buildProperties.get(propertyName);
    }

    public <T> T getBuildPropertyValue(String buildNode) {
        String property = getBuildPropertyPath(buildNode);
        return getPropertyValue(property);
    }

    public <T> T getPropertyValue(String node) {
        MapProperty property = getProperty(node);
        return (T) property.getValue();
    }
}
