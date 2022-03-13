package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.property.MapListProperty;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.model.map.scheme.ParseContext;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class MapSession {
    private final String worldName;

    private final Map<UUID, Boolean> viewers = new LinkedHashMap<>();
    private final Map<String, MapProperty> properties;
    private final Map<String, Object> parseConfiguration;

    private final String schemeName;
    private final MapScheme scheme;

    public MapSession(String worldName, MapScheme scheme) {
        this.scheme = scheme;
        this.worldName = worldName;
        this.schemeName = scheme.getName();
        this.properties = new LinkedHashMap<>(scheme.getProperties());
        this.parseConfiguration = new LinkedHashMap<>(scheme.getParseConfiguration());
    }

    public MapSession addViewer(UUID uuid, boolean canModify) {
        viewers.put(uuid, canModify);
        return this;
    }

    public MapSession addViewer(UUID uuid) {
        return addViewer(uuid, false);
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

    public MapSession canModify(UUID uuid, boolean canModify) {
        viewers.computeIfPresent(uuid, (id, modify) -> canModify);
        return this;
    }

    public MapSession removeViewer(UUID uuid) {
        viewers.remove(uuid);
        return this;
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
                errMessage,
                "{property}", propertyName,
                "{scheme}", schemeName);
        }
        property.parseValue(value);
        return this;
    }

    private MapSession buildProperty(String buildProperty, Object value) throws ParseException {
        String propertyPath = getBuildPropertyPath(buildProperty);
        MapProperty property = properties.get(propertyPath);
        property.parseValue(value);
        return this;
    }

    public MapSession cleanProperty(String propertyName) throws InvalidPropertyException {
        MapProperty property = properties.get(propertyName);
        if (property == null) {
            throw new InvalidPropertyException(
                "Undefined property {property}",
                "{property}", propertyName,
                "{scheme}", schemeName);
        }
        property.clearValue();
        return this;
    }

    public MapSession removePropertyValue(String propertyName, Object value) throws InvalidPropertyException {
        MapProperty property = properties.get(propertyName);
        if (!(property instanceof MapListProperty)) {
            throw new InvalidPropertyException(
                "Property {property} is undefined or not list",
                "{property}", propertyName,
                "{scheme}", schemeName);
        }
        MapListProperty listProperty = (MapListProperty) property;
        listProperty.remove(value);
        return this;
    }

    private MapSession removeBuildPropertyValue(String propertyName, Object value) throws ParseException {
        String propertyPath = getBuildPropertyPath(propertyName);
        MapProperty property = properties.get(propertyPath);
        if (!(property instanceof MapListProperty)) {
            throw new InvalidPropertyException(
                "Property {property} is not list",
                "{property}", propertyName,
                "{scheme}", schemeName);
        }
        MapListProperty listProperty = (MapListProperty) property;
        listProperty.remove(value);
        return this;
    }

    public String getWorldName() {
        return worldName;
    }

    public String getMapName() {
        return getPropertyValue("name");
    }

    public String getSchemeName() {
        return schemeName;
    }

    public MapScheme getScheme() {
        return scheme;
    }

    public String getVersion() {
        return getPropertyValue("version");
    }

    public List<String> getAuthors() {
        return getPropertyValue("author");
    }

    public Map<UUID, Boolean> getViewers() {
        return viewers;
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

    public String getBuildPropertyPath(String propertyName) throws ParseException {
        Map<String, String> buildProperties = (Map<String, String>)
            parseConfiguration.get(ParseContext.BUILD_PROPERTIES);
        if (buildProperties == null || !buildProperties.containsKey(propertyName)) {
            throw new InvalidPropertyException(
                "Undefined property {property}",
                "{property}", propertyName,
                "{scheme}", schemeName);
        }
        return buildProperties.get(propertyName);
    }

    @SuppressWarnings("unchecked")
    public <T> T getPropertyValue(String node) {
        return (T) properties.get(node);
    }
}
