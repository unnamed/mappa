package team.unnamed.mappa.internal;

import team.unnamed.mappa.model.map.MapSession;
import team.unnamed.mappa.model.map.scheme.MapScheme;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class MapRegistryImpl implements MapRegistry {
    private final Map<String, MapSession> sessions = new HashMap<>();
    private final Map<String, MapScheme> schemes = new HashMap<>();

    @Override
    public void registerMapSession(MapSession session) {
        sessions.put(session.getId(), session);
    }

    @Override
    public void unregisterMapSession(String id) {
        sessions.remove(id);
    }

    @Override
    public void registerMapScheme(MapScheme scheme) {
        schemes.put(scheme.getName(), scheme);
    }

    @Override
    public void unregisterMapScheme(String id) {
        schemes.remove(id);
    }

    @Override
    public void unregisterAll() {
        sessions.clear();
        schemes.clear();
    }

    @Override
    public boolean containsMapSessionId(String id) {
        return sessions.containsKey(id);
    }

    @Override
    public boolean containsMapSchemeId(String id) {
        return schemes.containsKey(id);
    }

    @Override
    public MapSession getMapSession(String id) {
        return sessions.get(id);
    }

    @Override
    public MapScheme getMapScheme(String id) {
        return schemes.get(id);
    }

    @Override
    public Collection<MapSession> getMapSessions() {
        return sessions.values();
    }

    @Override
    public Collection<MapScheme> getMapSchemes() {
        return schemes.values();
    }
}
