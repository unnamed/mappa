package team.unnamed.mappa.model.map.scheme;

import team.unnamed.mappa.model.map.MapEditSession;
import team.unnamed.mappa.model.map.property.MapProperty;
import team.unnamed.mappa.throwable.FindException;
import team.unnamed.mappa.throwable.ParseException;

import java.util.Map;

public interface MapPropertyTree extends Cloneable {

    Map<String, Object> getRawMaps();

    MapProperty find(String path) throws ParseException;

    MapProperty tryFind(String path);

    Map<String, Object> findAll(String path) throws FindException;

    void property(String path, Object value) throws ParseException;

    void clear(String path) throws ParseException;

    void clearAll(String path) throws FindException;

    boolean isReadOnly();

    MapPropertyTree cloneBy(MapEditSession session);
}
