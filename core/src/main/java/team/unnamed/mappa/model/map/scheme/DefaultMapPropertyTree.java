package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.TranslationNode;
import team.unnamed.mappa.throwable.FindCastException;
import team.unnamed.mappa.throwable.FindException;
import team.unnamed.mappa.throwable.InvalidPropertyException;
import team.unnamed.mappa.throwable.ParseException;
import team.unnamed.mappa.util.MapUtils;

import java.util.LinkedHashMap;
import java.util.Map;

@SuppressWarnings("unchecked")
public class DefaultMapPropertyTree implements MapPropertyTree {
    private final boolean readOnly;
    private final Map<String, Object> rawMaps;

    public DefaultMapPropertyTree(Map<String, Object> maps, boolean readOnly) {
        this.rawMaps = maps;
        this.readOnly = readOnly;
    }

    @Override
    public Map<String, Object> getRawMaps() {
        return rawMaps;
    }

    @Override
    public MapProperty find(String path) throws ParseException {
        int aDot = path.indexOf(".");
        MapProperty property;
        if (aDot == -1) {
            property = (MapProperty) rawMaps.get(path);
            if (property == null) {
                throw new InvalidPropertyException(
                    TranslationNode
                        .INVALID_PROPERTY
                        .with("{property}", path));
            }
        } else {
            property = MapUtils.find(rawMaps,
                MapProperty.class,
                path.split("\\."),
                0);
        }

        return property;
    }

    @Override
    public MapProperty tryFind(String path) {
        try {
            return find(path);
        } catch (ParseException e) {
            return null;
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    public Map<String, Object> findAll(String path) throws FindException {
        if (path.isEmpty()) {
            return rawMaps;
        } else if (!path.contains(".")) {
            Object o = rawMaps.get(path);
            if (o == null) {
                throw new FindException("Trying to find map at index 0 found nothing");
            } else if (o instanceof Map) {
                return (Map<String, Object>) o;
            } else {
                throw new FindCastException(
                    "Trying to find map at index 0 found other object ("
                        + o.getClass().getSimpleName() + ")");
            }
        }
        return MapUtils.find(rawMaps,
            Map.class,
            path.split("\\."),
            0);
    }

    @Override
    public void property(String path, Object value) throws ParseException {
        if (readOnly) {
            return;
        }
        MapProperty property = find(path);
        property.parseValue(value);
    }

    @Override
    public void clear(String path) throws ParseException {
        if (readOnly) {
            return;
        }
        MapProperty property = find(path);
        property.clearValue();
    }

    @Override
    public void clearAll(String path) throws FindException {
        if (readOnly) {
            return;
        }
        deepClear(findAll(path));
    }

    private void deepClear(Map<String, Object> all) {
        for (Map.Entry<String, Object> entry : all.entrySet()) {
            Object value = entry.getValue();
            if (value instanceof MapProperty) {
                MapProperty property = (MapProperty) value;
                property.clearValue();
            } else if (value instanceof Map) {
                deepClear((Map<String, Object>) value);
            }
        }
    }

    @Override
    public boolean isReadOnly() {
        return readOnly;
    }

    @Override
    public MapPropertyTree cloneBy(MapEditSession session) {
        Map<String, Object> map = cloneMap(rawMaps, new LinkedHashMap<>(), session);
        return new DefaultMapPropertyTree(map, false);
    }

    private Map<String, Object> cloneMap(Map<String, Object> reference,
                                         Map<String, Object> newMap,
                                         MapEditSession session) {
        for (Map.Entry<String, Object> entry : reference.entrySet()) {
            String path = entry.getKey();
            Object object = entry.getValue();

            if (object instanceof Map) {
                Map<String, Object> map = cloneMap(
                    (Map<String, Object>) object,
                    new LinkedHashMap<>(),
                    session);
                newMap.put(path, map);
            } else if (object instanceof MapProperty) {
                MapProperty property = (MapProperty) object;
                property = property.clone();
                if (property.isImmutable()) {
                    property.applyDefaultValue(session);
                }
                newMap.put(path, property);
            } else {
                throw new IllegalArgumentException();
            }
        }
        return newMap;
    }
}
