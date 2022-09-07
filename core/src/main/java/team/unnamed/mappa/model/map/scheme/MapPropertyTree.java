package team.unnamed.mappa.model.map.scheme;

import org.jetbrains.annotations.NotNull;
import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.object.MapTypeIterator;
import team.unnamed.mappa.throwable.FindException;

import java.util.Iterator;
import java.util.Map;

public interface MapPropertyTree extends Cloneable, Iterable<MapProperty> {

    Map<String, Object> getRawMaps();

    MapProperty find(String path) throws FindException;

    MapProperty tryFind(String path);

    Map<String, Object> findAll(String path) throws FindException;

    void property(String path, Object value) throws FindException;

    void clear(String path) throws FindException;

    void clearAll(String path) throws FindException;

    boolean isReadOnly();

    MapPropertyTree cloneBy(MapEditSession session);

    @NotNull
    @Override
    default Iterator<MapProperty> iterator() {
        return new MapTypeIterator<>(MapProperty.class, getRawMaps());
    }
}
