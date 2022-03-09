package team.unnamed.mappa.model.map;

import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.model.map.scheme.MapScheme;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.*;

public class MapSession {
    private String mapName;
    private String version;

    private final List<String> authors = new ArrayList<>();
    private final Map<UUID, Boolean> viewers = new LinkedHashMap<>();
    private final Map<String, MapProperty> properties;

    private final String schemeName;
    private final MapScheme scheme;

    public MapSession(MapScheme scheme) {
        this.scheme = scheme;
        this.schemeName = scheme.getName();
        this.properties = new LinkedHashMap<>(scheme.getProperties());
    }

    public MapSession addViewer(UUID uuid, boolean canModify) {
        viewers.put(uuid, canModify);
        return this;
    }

    public MapSession addViewer(UUID uuid) {
        return addViewer(uuid, false);
    }

    public MapSession addAuthor(String author) {
        authors.add(author);
        return this;
    }

    public MapSession mapName(String mapName) {
        this.mapName = mapName;
        return this;
    }

    public MapSession version(String version) {
        this.version = version;
        return this;
    }

    public MapSession canModify(UUID uuid, boolean canModify) {
        viewers.computeIfPresent(uuid, (id, modify) -> canModify);
        return this;
    }

    public MapSession removeViewer(UUID uuid) {
        viewers.remove(uuid);
        return this;
    }

    public MapSession removeAuthor(String author) {
        authors.remove(author);
        return this;
    }

    public MapSession property(String propertyName, Object value) throws ParseException {
        MapProperty property = properties.get(propertyName);
        if (property == null) {
            throw new InvalidPropertyException(
                String.format("Property %s not found (does not belong to this scheme?)", propertyName));
        }
        property.parseValue(value);
        return this;
    }

    public MapSession cleanProperty(String propertyName) {
        MapProperty property = properties.get(propertyName);
        if (property != null) {
            property.clearValue();
        }
        return this;
    }

    public String getMapName() {
        return mapName;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public MapScheme getScheme() {
        return scheme;
    }

    public String getVersion() {
        return version;
    }

    public List<String> getAuthors() {
        return authors;
    }

    public Map<UUID, Boolean> getViewers() {
        return viewers;
    }

    public Map<String, MapProperty> getProperties() {
        return properties;
    }

    public MapProperty getProperty(String node) {
        return properties.get(node);
    }
}
